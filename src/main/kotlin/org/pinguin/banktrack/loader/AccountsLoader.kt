package org.pinguin.banktrack.loader

import org.pinguin.banktrack.Printer
import org.pinguin.banktrack.codecs.SourceStructure
import org.pinguin.banktrack.codecs.parseSourceStructure
import org.pinguin.banktrack.codecs.sourceDefinitionForFile
import org.pinguin.banktrack.codecs.transactionsDecoderFactory
import org.pinguin.banktrack.model.Account
import org.pinguin.banktrack.model.Categories
import org.pinguin.banktrack.model.MutableAccount
import org.pinguin.banktrack.model.Transaction
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class AccountsLoader( private val printer: Printer) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val currencyFormat = DecimalFormat( "#,##0.00")

    fun load( inputs: Iterable<File>,
              sourceDefinitionFile: File? ): Collection<Account> {
        val sourceDefinition = if (sourceDefinitionFile!=null) parseSourceStructure(sourceDefinitionFile)
                                else null

        val accounts = inputs.
                map { file ->
                    printer.section( "Parsing input source $file" ) {
                        val transactions = decode(file, sourceDefinition)
                        reportUnclassifiedTransactions(transactions)
                        transactions
                    }
                }
                .fold(mutableMapOf<String, MutableAccount>()) { result, current ->
                    merge( result, current );
                    result
                }

        reportMismatchingBalances(accounts.values)

        return accounts.values
    }

    private fun merge(container: MutableMap<String, MutableAccount>, account: Account ) {
        val existingAccount = container[account.name]
        if ( existingAccount == null ) {
            container[account.name] = account.toMutable()
        }
        else {
            existingAccount.merge( account )
        }
    }

    private fun reportUnclassifiedTransactions( transactions: Account ) {
        val unclassifiedTransactions = transactions.withCategory( Categories.UNCLASSIFIED )
        if ( !unclassifiedTransactions.isEmpty() ) {
            printer.section("Unclassified transactions:" ) {
                unclassifiedTransactions.forEach(::printTransaction)
            }
        }
    }

    private fun reportMismatchingBalances( accounts: Collection<Account> ) =
        accounts.forEach{ reportMismatchingBalances(it) }


    private fun reportMismatchingBalances(account: Account) {
        val mismatchingBalances = account.mismatchingBalances
        if ( !mismatchingBalances.isEmpty() ) {
            val formatted = mismatchingBalances.
                    joinToString("\n   ") { pair ->
                        val expected = pair.first.balance + pair.second.amount
                        "In ${dateFormat.format(pair.second.date.time)}, " +
                                "expected balanced ${currencyFormat.format(expected)} but get " +
                                "${currencyFormat.format(pair.second.balance)}"}
            printer.log("Warning: find missing account in account " +
                    "${account.name} at dates:\n   $formatted")
        }
    }

    private fun printTransaction( transaction: Transaction) {
        val memo = if ( transaction.memo.length < 80 ) transaction.memo
        else transaction.memo.substring( 0, 80 ) + "... "
        val amount = String.format( "%13s", currencyFormat.format(transaction.amount) )
        printer.log( "${dateFormat.format(transaction.date.time)} $amount   $memo" )
    }

    private fun decode(file: File, specifiedSourceDefinition: SourceStructure? ): Account {
        val sourceDefinition = specifiedSourceDefinition ?: sourceDefinitionForFile( file )
        return transactionsDecoderFactory(sourceDefinition).decode(file)
    }
}