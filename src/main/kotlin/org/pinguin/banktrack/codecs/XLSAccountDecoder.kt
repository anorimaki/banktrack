package org.pinguin.banktrack.codecs

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.pinguin.banktrack.model.Account
import org.pinguin.banktrack.model.Transaction
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class XLSAccountDecoder(private val structure: SourceStructure): AccountDecoder  {
    private val dateFormat = SimpleDateFormat(structure.dateFormat)

    override fun decode(input: InputStream): Account {
        WorkbookFactory.create(input).use { wb ->
            if (wb.numberOfSheets != 1) {
                throw Exception("Unexpected format")
            }
            val sheet = wb.iterator().next()
            val transactions = sheet.asSequence().drop(structure.startRow).mapNotNull(this::decodeRow)
            return Account(structure.name, AccountDecoderHelper.order(transactions.toList()))
        }
    }

    private fun decodeRow( row: Row ): Transaction? {
        if ( !row.cellIterator().hasNext() ) {
            return null
        }

        val date = parseDate( row.getCell(structure.dateColumn) )
        val memo = row.getCell(structure.memoColumn).stringCellValue
        val amount = parseCurrency( row.getCell(structure.amountColumn) )
        val balance = parseCurrency( row.getCell(structure.balanceColumn) )
        return Transaction(structure.name, date, memo, amount, balance, structure.categories.find(memo))
    }

    private fun parseCurrency( cell: Cell ) =
            BigDecimal( cell.numericCellValue ).setScale(2, RoundingMode.HALF_EVEN)

    private fun parseDate( cell: Cell): Calendar =
        when( cell.cellType ) {
            CellType.STRING -> {
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(cell.stringCellValue)
                calendar
            }
            else -> throw Exception("Unexpected cell type")
        }
}