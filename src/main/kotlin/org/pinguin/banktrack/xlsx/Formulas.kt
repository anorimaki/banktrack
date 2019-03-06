package org.pinguin.banktrack.xlsx

import org.apache.poi.ss.util.CellAddress
import org.apache.poi.ss.util.CellRangeAddress


object Formulas {
    fun sum(range: CellRangeAddress): String {
        return "SUM(${range.formatAsString()})"
    }

    fun sum(cells: Collection<CellAddress>): String {
        return cells.joinToString("+" ) {it.formatAsString()}
    }

    fun subtotal(range: CellRangeAddress): String? {
        return "SUBTOTAL(9,${range.formatAsString()})"
    }
}
