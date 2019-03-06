package org.pinguin.banktrack.codecs

import org.pinguin.banktrack.model.Account
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

enum class SourceType(val type: String) {
    HTML("HTML"),
    XLS("XLS")
}


interface AccountDecoder {
    fun decode( input: InputStream ): Account

    fun decode( file: File ): Account =
        FileInputStream(file).use { decode(it) }
}


fun transactionsDecoderFactory( structure: SourceStructure): AccountDecoder {
    return when( structure.type ) {
        SourceType.HTML -> HTMLAccountDecoder(structure)
        SourceType.XLS -> XLSAccountDecoder(structure)
    }
}