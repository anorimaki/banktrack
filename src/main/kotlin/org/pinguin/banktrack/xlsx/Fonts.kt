package org.pinguin.banktrack.xlsx

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Workbook

class Fonts(private val workbook: Workbook) {
    val commentPositiveQuantity: Font = workbook.createFont()
    val commentNegativeQuantity: Font = workbook.createFont()
    val commentDay: Font = workbook.createFont()
    val commentMemo: Font = workbook.createFont()

    init {
        commentDay.color = HSSFColorPredefined.LIGHT_BLUE.index

        commentPositiveQuantity.bold = true

        commentNegativeQuantity.bold = true
        commentNegativeQuantity.color = HSSFColorPredefined.RED.index
    }
}
