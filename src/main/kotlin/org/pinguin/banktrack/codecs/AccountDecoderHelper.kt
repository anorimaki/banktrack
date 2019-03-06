package org.pinguin.banktrack.codecs

import org.pinguin.banktrack.model.Transaction

object AccountDecoderHelper {
    fun order( transactions: List<Transaction> ): List<Transaction> {
        if ( transactions.size < 2 ) {
            return transactions
        }
        val t1 = transactions[0]
        val t2 = transactions[1]

        return if (t2.balance != (t1.balance + t2.amount)) transactions.reversed() else transactions
    }
}
