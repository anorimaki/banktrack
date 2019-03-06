package org.pinguin.banktrack.codecs

import org.pinguin.banktrack.model.Categories
import org.pinguin.banktrack.model.Transaction
import java.io.OutputStream
import java.io.Writer
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// See: https://en.wikipedia.org/wiki/Quicken_Interchange_Format
class QifTransactionsEncoder: TransactionsEncoder{
    override fun encode(input: Iterable<Transaction>, output: OutputStream) {
        output.writer(Charsets.UTF_8).use { writer ->
            writer.write("!Type:Bank\n" )
            input.forEach { transaction ->
                encodeTransaction( transaction, writer )
            }
        }
    }

    private fun encodeTransaction( transaction: Transaction, writer: Writer ) {
        writer.write( "D${encodeDate(transaction.date)}\n" )
        writer.write( "T${encodeMoney(transaction.amount)}\n" )
        writer.write( "M${transaction.memo}\n" )
        if ( transaction.category != Categories.UNCLASSIFIED ) {
            writer.write("L${transaction.category.name}\n")
        }
        writer.write( "^\n" )
    }

    private fun encodeDate( date: Calendar ) = dateFormatter.format(date.time)

    private fun encodeMoney( q: BigDecimal ) = currencyFormatter.format(q)

    companion object {
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        private val currencyFormatter = DecimalFormat("#,##0.00")
    }
}