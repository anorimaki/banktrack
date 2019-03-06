package org.pinguin.banktrack.xlsx

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*


class CellStyles(private val workbook: Workbook) {
    enum class AmountType { Splited, Normal, Total }

    val splitedCategoryName: CellStyle
    val splitedAmount: CellStyle
    val normalCategoryName: CellStyle
    val unclassifiedCategoryName: CellStyle
    val totalCategoryName: CellStyle
    val normalAmount: CellStyle
    val totalAmount: CellStyle
    val header: CellStyle
    val date: CellStyle

    init {
        normalCategoryName = createNormalCategoryName()
        splitedCategoryName = createSplitedCategoryName()
        unclassifiedCategoryName = createUnclassifiedCategoryName()
        totalCategoryName = createTotalCategoryName()
        splitedAmount = createAmountStyle(AmountType.Splited)
        normalAmount = createAmountStyle(AmountType.Normal)
        totalAmount = createAmountStyle(AmountType.Total)
        header = createHeaderStyle()
        date = createDateCellStyle()
    }

    private fun createDateCellStyle(): CellStyle {
        val ret = workbook.createCellStyle()
        val dataFormat = workbook.createDataFormat()
        ret.dataFormat = dataFormat.getFormat("d/m/yyyy")
        ret.alignment = HorizontalAlignment.CENTER
        return ret
    }

    private fun createNormalCategoryName(): CellStyle {
        return workbook.createCellStyle()
    }

    private fun createSplitedCategoryName(): CellStyle {
        val ret = workbook.createCellStyle()
        val font = workbook.createFont()
        font.color = HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.index
        ret.setFont(font)
        return ret
    }

    private fun createUnclassifiedCategoryName(): CellStyle {
        val ret = workbook.createCellStyle()
        val font = workbook.createFont()
        font.italic = true
        ret.setFont(font)
        return ret
    }

    private fun createTotalCategoryName(): CellStyle {
        val ret = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        ret.setFont(font)
        return ret
    }

    private fun createAmountStyle(type: AmountType): CellStyle {
        val decimalCellStyle = workbook.createCellStyle()
        val dataFormat = workbook.createDataFormat()
        decimalCellStyle.dataFormat = dataFormat.getFormat("#,##0.00 \u20AC")
        when(type) {
            AmountType.Splited -> {
                val font = workbook.createFont()
                font.color = HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.index
                decimalCellStyle.setFont(font)
            }
            AmountType.Total -> {
                val font = workbook.createFont()
                font.bold = true
                decimalCellStyle.setFont(font)
            }
            AmountType.Normal -> {}
        }
        return decimalCellStyle
    }

    private fun createHeaderStyle(): CellStyle {
        val headerFont = workbook.createFont()
        headerFont.bold = true
        val style = createBorderedStyle(IndexedColors.BLACK.getIndex())
        style.alignment = HorizontalAlignment.CENTER
        style.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.setFont(headerFont)
        return style
    }

    private fun createBorderedStyle(borderColor: Short?): CellStyle {
        val style = workbook.createCellStyle()
        style.borderRight = BorderStyle.THIN
        style.rightBorderColor = borderColor!!
        style.borderBottom = BorderStyle.THIN
        style.bottomBorderColor = borderColor
        style.borderLeft = BorderStyle.THIN
        style.leftBorderColor = borderColor
        style.borderTop = BorderStyle.THIN
        style.topBorderColor = borderColor
        return style
    }
}