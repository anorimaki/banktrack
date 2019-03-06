package org.pinguin.banktrack.codecs

import org.pinguin.banktrack.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

interface TransactionsEncoder {
    fun encode(input: Iterable<Transaction>, output: OutputStream)

    fun encode(input: Iterable<Transaction>, file: File) =
            FileOutputStream(file).use { encode(input, it) }
}
