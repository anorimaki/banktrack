package org.pinguin.banktrack.codecs

import org.ini4j.Ini
import org.ini4j.Profile
import org.pinguin.banktrack.model.Categories
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


data class SourceStructure(val type: SourceType,
                           val name: String,
                           val startRow: Int,
                           val endRow: Int?,
                           val dateColumn: Int,
                           val dateFormat: String,
                           val memoColumn: Int,
                           val amountColumn: Int,
                           val balanceColumn: Int,
                           val categories: Categories)

private const val MAIN_SECTION = "main"
private const val CATEGORIES_SECTION = "categories"

private const val TYPE_PROPERTY = "type"
private const val ACCOUNT_NAME = "name"
private const val START_ROW_PROPERTY = "begin"
private const val END_ROW_PROPERTY = "end"
private const val DATE_COLUMN_PROPERTY = "date.column"
private const val DATE_FORMAT_PROPERTY = "date.format"
private const val MEMO_COLUMN_PROPERTY = "memo.column"
private const val AMOUNT_COLUMN_PROPERTY = "amount.column"
private const val BALANCE_COLUMN_PROPERTY = "balance.column"


class InvalidDefinitionException(message: String) : Exception(message) {}

private fun section(ini: Ini, name: String): Profile.Section =
        ini.getOrElse(name) { throw InvalidDefinitionException("Missing section $name") }

private inline fun <reified T> value(section: Profile.Section, name: String): T {
    return section.get(name, T::class.java)
            ?: throw InvalidDefinitionException("Missing property $name in section ${section.name}")
}

private fun String.toColumnReference(): Int {
    val int = toIntOrNull()
    return if (int != null) int - 1 else fold(0) { result, c -> (result * 26) + c.code - 'A'.code }
}

fun parseSourceStructure(input: InputStream): SourceStructure {
    val ini = Ini(InputStreamReader(input, "UTF-8"))

    val mainSection = section(ini, MAIN_SECTION)
    val type = SourceType.valueOf(value<String>(mainSection, TYPE_PROPERTY)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    val name = value<String>(mainSection, ACCOUNT_NAME)
    val startRow = value<Int>(mainSection, START_ROW_PROPERTY) - 1
    val endRow =
            if (mainSection.containsKey(END_ROW_PROPERTY)) mainSection.get(END_ROW_PROPERTY, Int::class.java) - 1
            else null
    val dateColumn = value<String>(mainSection, DATE_COLUMN_PROPERTY).toColumnReference()
    val dateFormat = value<String>(mainSection, DATE_FORMAT_PROPERTY)
    val memoColumn = value<String>(mainSection, MEMO_COLUMN_PROPERTY).toColumnReference()
    val amountColumn = value<String>(mainSection, AMOUNT_COLUMN_PROPERTY).toColumnReference()
    val balanceColumn = value<String>(mainSection, BALANCE_COLUMN_PROPERTY).toColumnReference()
    val categories = Categories.parse(section(ini, CATEGORIES_SECTION))
    return SourceStructure(type, name, startRow, endRow, dateColumn, dateFormat,
            memoColumn, amountColumn, balanceColumn, categories)
}

fun parseSourceStructure(input: File): SourceStructure =
        FileInputStream(input).use { stream -> parseSourceStructure(stream) }