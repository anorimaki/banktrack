package org.pinguin.banktrack.xlsx

import org.apache.poi.ss.usermodel.Workbook


class Styles(workbook: Workbook) {
    val cellStyles = CellStyles(workbook)
    val fonts = Fonts(workbook)
}
