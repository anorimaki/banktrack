package org.pinguin.banktrack

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.pinguin.banktrack.lang.GroupBy
import org.pinguin.banktrack.lang.GroupListAdaptor
import org.pinguin.banktrack.loader.AccountsLoader
import org.pinguin.banktrack.model.Category
import org.pinguin.banktrack.model.Transaction
import org.pinguin.banktrack.model.month
import org.pinguin.banktrack.model.year
import org.pinguin.banktrack.xlsx.Styles
import org.pinguin.banktrack.xlsx.createCategoriesSheet
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal


private typealias GroupedTransactions<K, C> = GroupBy<K, Transaction, C>

private class MutableTransactionsByAccount: GroupedTransactions<String, GroupListAdaptor<Transaction>>(
        { GroupListAdaptor() }, { it.account } )

private class MutableTransactionsByMonth: GroupedTransactions<Int, GroupListAdaptor<Transaction>>  (
        { GroupListAdaptor() }, { it.date.month } )

private class MutableTransactionsByCategory: GroupedTransactions<Category, MutableTransactionsByMonth>  (
        { MutableTransactionsByMonth() }, { it.category } )

private class MutableTransactionsByMainCategory: GroupedTransactions<String, MutableTransactionsByCategory>  (
        { MutableTransactionsByCategory() }, { it.category.components.first() } )

private class ReportModel {
    val accounts = MutableTransactionsByAccount()
    val incomes = MutableTransactionsByMainCategory()
    val expenses = MutableTransactionsByMainCategory()

    fun add( transaction: Transaction ): ReportModel {
        accounts.add( transaction )
        if ( transaction.amount >= BigDecimal.ZERO) {
            incomes.add(transaction)
        }
        else {
            expenses.add(transaction)
        }

        return this
    }
}

class XLSReport: CliCommand( "report", help = "Generates spreadsheet reports from bank transaction files" ) {
    private val outputPattern by option( "-o", "--output", help = "Pattern for output files" ).
                                required()

    override fun run() {
        val loader = AccountsLoader(printer)
        val accounts = loader.load(inputs, sourceDefinitionFile)

        val model = accounts.asSequence().
                        flatten().
                        groupingBy { it.date.year }.
                        fold( { _, _ -> ReportModel() },
                            { _, model, transaction -> model.add(transaction) })

        model.entries.forEach { yearData ->
            XSSFWorkbook().use { wb ->
                val styles = Styles(wb)

                writeTransactions( wb, yearData.key, yearData.value.accounts )
                createCategoriesSheet( wb.createSheet("Categories"),
                        styles, yearData.value.incomes, yearData.value.expenses )

                val pattenFile = File( outputPattern )
                val outputFile = if (pattenFile.isDirectory) File( pattenFile, "${yearData.key}.xlsx" )
                                else File("$outputPattern${yearData.key}.xlsx")
                FileOutputStream(outputFile).use { stream -> wb.write(stream) }
            }
        }
    }

    private fun writeTransactions( wb: Workbook, year: Int, accounts: Map<String, List<Transaction>> ) {
        accounts.forEach {

        }
    }
}
