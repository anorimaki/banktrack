package org.pinguin.banktrack.xlsx

import org.pinguin.banktrack.model.Category
import org.pinguin.banktrack.model.Transaction
import java.util.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellAddress
import org.apache.poi.ss.util.CellRangeAddress
import org.pinguin.banktrack.model.Categories
import java.math.BigDecimal
import java.text.DecimalFormat


private val MONTHS = Calendar.getInstance().
        getDisplayNames( Calendar.MONTH, Calendar.LONG_STANDALONE, Locale.getDefault() ).
        entries.
        sortedBy { it.value }.
        map { it.key }

private const val TYPE_COLUMN = 0
private const val LABELS_COLUMN = 1
private const val MONTH_COLUMN = 2
private val TOTAL_COLUMN = MONTH_COLUMN + MONTHS.size

private const val MONTH_COLUMN_WIDTH = 256 * 12

private const val INCOMES_LABEL = "Incomes"
private const val EXPENSES_LABEL = "Expenses"
private const val TOTAL_LABEL = "Total"

private val DAY_FORMAT = DecimalFormat("00")
private val QUANTITY_FORMAT = DecimalFormat("#,##0.00" )


typealias TransactionsByMonth = Map<Int, List<Transaction>>
typealias TransactionsByCategory = Map<Category, TransactionsByMonth>
typealias TransactionsByMainCategory = Map<String, TransactionsByCategory>

fun createCategoriesSheet(sheet: Sheet, styles: Styles,
                          incomes: TransactionsByMainCategory,
                          expenses: TransactionsByMainCategory) {
    val generator = CategoriesSheetGenerator( sheet, styles)
    generator.generate( incomes, expenses )
}

private class CategoriesSheetGenerator( private val sheet: Sheet,
                                        private val styles: Styles) {
    fun generate( incomes: TransactionsByMainCategory, expenses: TransactionsByMainCategory ) {
        var currentRow = createHeader()
        currentRow = createBlock( currentRow, INCOMES_LABEL, incomes )
        createBlock( currentRow+1, EXPENSES_LABEL, expenses )

        sheet.workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        adjustColumnWidth()
        sheet.createFreezePane(LABELS_COLUMN+1, 1)
    }

    private fun createHeader(): Int {
        val row = sheet.createRow(0)
        var i = MONTH_COLUMN
        for (month in MONTHS) {
            val cell = row.createCell(i++)
            cell.setCellValue(month)
            cell.cellStyle = styles.cellStyles.header
        }

        val cell = row.createCell(TOTAL_COLUMN)
        cell.setCellValue(TOTAL_LABEL)
        cell.cellStyle = styles.cellStyles.header
        return 1
    }

    private fun createBlock( firstRow: Int, label: String, transactions: TransactionsByMainCategory ): Int {
        var currentRow = createBlockHeader( firstRow, label )

        var rowsToSum = transactions.map {
            if (it.key != Categories.UNCLASSIFIED.name) {
                currentRow = createCategoryBlock(currentRow, it.key, it.value)
            }
            currentRow-1
        }

        val unclassifiedTransactions = transactions[Categories.UNCLASSIFIED.name]
        if ( unclassifiedTransactions != null ) {
            assert( unclassifiedTransactions.size == 1 )

            rowsToSum += currentRow
            createUnclassifiedTransactions( currentRow, unclassifiedTransactions.values.first() )
            ++currentRow
        }

        return createBlockFooter( currentRow, rowsToSum )
    }

    private fun createBlockHeader( firstRow: Int, label: String ): Int {
        val typeLabelRow = sheet.createRow(firstRow)
        val cell = typeLabelRow.createCell(TYPE_COLUMN)
        cell.setCellValue(label)
        cell.cellStyle = styles.cellStyles.header
        return firstRow+1
    }

    private fun createBlockFooter( firstRow: Int, rowsToSum: Collection<Int> ): Int {
        val totalRow = sheet.createRow(firstRow)
        val labelCell = totalRow.createCell(LABELS_COLUMN)
        labelCell.setCellValue(TOTAL_LABEL)
        labelCell.cellStyle = styles.cellStyles.totalCategoryName

        for ( month in Calendar.JANUARY..(Calendar.DECEMBER+1) ) {
            val currentColumn = MONTH_COLUMN + month
            val cell = totalRow.createCell(currentColumn)
            cell.cellStyle = styles.cellStyles.totalAmount

            if (rowsToSum.isEmpty()) {
                cell.setCellValue(0.0)
            }
            else {
                cell.cellFormula = Formulas.sum(rowsToSum.map { CellAddress(it, currentColumn) })
            }
        }

        return firstRow+1
    }

    private fun createUnclassifiedTransactions( row: Int,
                                                transactions: TransactionsByMonth) {
        createCategoryRow( row, "Unclassified", transactions,
                styles.cellStyles.unclassifiedCategoryName,
                styles.cellStyles.normalAmount )
    }

    private fun createCategoryBlock( firstRow: Int, category: String,
                                     transactions: TransactionsByCategory ): Int {
        var currentRow = firstRow
        if ( transactions.size > 1 ) {
            transactions.forEach {
                createCategoryRow( currentRow, it.key.name, it.value,
                        styles.cellStyles.splitedCategoryName,
                        styles.cellStyles.splitedAmount )
                ++currentRow
            }
            currentSubtotalRow( currentRow, category, firstRow, currentRow-1 )
        }
        else {
            createCategoryRow( currentRow, category, transactions.values.iterator().next(),
                                styles.cellStyles.normalCategoryName,
                                styles.cellStyles.normalAmount )
        }
        return currentRow+1
    }

    private fun currentSubtotalRow( rowIndex: Int, category: String, fromRow: Int, toRow: Int ) {
        val row = sheet.createRow(rowIndex)

        val labelCell = row.createCell(LABELS_COLUMN)
        labelCell.setCellValue(category)

        for( month in Calendar.JANUARY..Calendar.DECEMBER ) {
            val currentColumn = MONTH_COLUMN + month
            val cell = row.createCell(currentColumn)
            cell.cellStyle = styles.cellStyles.normalAmount
            cell.cellFormula = Formulas.subtotal(
                            CellRangeAddress( fromRow, toRow, currentColumn, currentColumn ) )
        }

        createTotalRowCell( row, styles.cellStyles.normalAmount )
    }

    private fun createCategoryRow( rowIndex: Int, category: String,
                                   transactionsByMonth: TransactionsByMonth,
                                   nameCellStyle: CellStyle,
                                   amountCellStyle: CellStyle ) {
        val row = sheet.createRow(rowIndex)

        val labelCell = row.createCell(LABELS_COLUMN)
        labelCell.setCellValue(category)
        labelCell.cellStyle = nameCellStyle

        for( month in Calendar.JANUARY..Calendar.DECEMBER ) {
            val transactions = transactionsByMonth[month]

            if ( transactions != null ) {
                val cellIndex = MONTH_COLUMN + month
                val cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)

                val total = createCellFromTransactions(cell, transactions)

                cell.setCellValue(total.toDouble())
                cell.cellStyle = amountCellStyle
            }
        }

        createTotalRowCell( row, amountCellStyle )
    }

    private fun createTotalRowCell( row: Row, cellStyle: CellStyle ) {
        val cell = row.createCell(TOTAL_COLUMN)
        cell.cellStyle = cellStyle
        cell.cellFormula = Formulas.sum(
                CellRangeAddress(row.rowNum, row.rowNum, MONTH_COLUMN, MONTH_COLUMN + MONTHS.size - 1))
    }

    private fun createCellFromTransactions(cell: Cell, transactions: Collection<Transaction>): BigDecimal {
        data class StringWithFormatter( val content: StringBuilder,
                                        val formatters: MutableList<(RichTextString) -> Unit>,
                                        val maxLineSize: Int,
                                        val total: BigDecimal ) {
            constructor(): this( StringBuilder(), mutableListOf(), 0, BigDecimal.ZERO )

            fun append( item: String, font: Font ) {
                val startIndex = content.length
                val endIndex = content.length+item.length
                formatters.add { richText ->
                    richText.applyFont(startIndex, endIndex, font)
                }
                content.append( item )
            }
        }

        val content = transactions.fold( StringWithFormatter() ) { result, transaction ->
            if ( !result.content.isEmpty() ) {
                result.content.append('\n')
            }

            val startLine = result.content.length

            val day = DAY_FORMAT.format(transaction.date.get(Calendar.DAY_OF_MONTH))
            result.append( day, styles.fonts.commentDay)

            result.content.append(": ")

            val amount = QUANTITY_FORMAT.format(transaction.amount)
            result.append( amount, if (transaction.amount >= BigDecimal.ZERO)
                                        styles.fonts.commentPositiveQuantity
                                    else styles.fonts.commentNegativeQuantity)

            result.content.append(' ')
            result.append(transaction.memo, styles.fonts.commentMemo)

            StringWithFormatter( result.content,
                    result.formatters,
                    maxOf( result.maxLineSize, result.content.length - startLine ),
                    result.total + transaction.amount )
        }

        val creationHelper = sheet.workbook.creationHelper

        val cellCommentAnchor = creationHelper.createClientAnchor()
        cellCommentAnchor.setCol2(content.maxLineSize / 5)
        cellCommentAnchor.row2 = transactions.size + 1

        val richText = creationHelper.createRichTextString( content.content.toString() )
        content.formatters.forEach { formatter -> formatter(richText) }

        val drawing = sheet.createDrawingPatriarch()
        val cellComment = drawing.createCellComment(cellCommentAnchor)

        cellComment.string = richText

        cell.cellComment = cellComment

        return content.total
    }

    private fun adjustColumnWidth() {
        for (columnIndex in 0..MONTH_COLUMN) {
            sheet.autoSizeColumn(columnIndex)
        }
        for (columnIndex in MONTH_COLUMN..TOTAL_COLUMN) {
            sheet.setColumnWidth(columnIndex, MONTH_COLUMN_WIDTH)
        }
        sheet.autoSizeColumn(TOTAL_COLUMN)
    }
}
