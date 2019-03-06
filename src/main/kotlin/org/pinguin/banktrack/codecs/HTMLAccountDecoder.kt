package org.pinguin.banktrack.codecs

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.pinguin.banktrack.model.Account
import org.pinguin.banktrack.model.Transaction
import java.io.InputStream
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class HTMLAccountDecoder(private val structure: SourceStructure): AccountDecoder {
    private val columnsIndexes = listOf(structure.dateColumn, structure.memoColumn,
                                        structure.amountColumn, structure.balanceColumn)
    private val dateFormat = SimpleDateFormat(structure.dateFormat)

    override fun decode(input: InputStream): Account {
        // null charset. Let jsoup to determine encoding based on meta[charset]
        val doc = Jsoup.parse(input, null, "")

        val tables = doc.getElementsByTag("table")
        if (tables.size != 1) {
            throw Exception("Unexpected format")
        }
        val table = tables.first()
        val rows = table.getElementsByTag("tr")

        val transactions = decode(rows)
        return Account(structure.name, AccountDecoderHelper.order(transactions))
    }

    private fun decode(rows: Elements): List<Transaction> {
        val filteredRows = if (structure.endRow == null) rows.dropLastWhile { isEmptyRow(it) }
        else rows.dropLast(structure.endRow)
        return filteredRows.drop(structure.startRow).map { decodeRow(it) }
    }

    private fun decodeRow(row: Element): Transaction {
        val columns = extractColumns(row).iterator()
        val date = parseDate(columns.next())
        val memo = columns.next()
        val amount = parseCurrency(columns.next())
        val balance = parseCurrency(columns.next())
        return Transaction(structure.name, date, memo, amount, balance, structure.categories.find(memo))
    }

    private fun parseDate( text: String ): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(text)
        return calendar
    }

    private fun parseCurrency( text: String ): BigDecimal =
            amountFormat.parse(text) as BigDecimal

    private fun isEmptyRow(row: Element): Boolean =
        extractColumns(row).all { it.isEmpty() }

    private fun extractColumns(row: Element): Iterable<String> =
        row.getElementsByTag("td").
            slice(columnsIndexes).
            map{ it.text().replace('\u00A0', ' ').trim() }

    companion object {
        //DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("es", "ES")) )
        val amountFormat = DecimalFormat("#,##0.00" )

        init {
            amountFormat.isParseBigDecimal = true
        }
    }
}