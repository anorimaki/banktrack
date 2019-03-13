package org.pinguin.banktrack

import org.pinguin.banktrack.codecs.QifTransactionsEncoder
import org.pinguin.banktrack.loader.AccountLoader
import org.pinguin.banktrack.loader.AccountsLoader


class XLS2QIF: CliCommand( "qif", help = "Converts bank transactions format to QIF format") {
    private val outputFile by outputFileOption(this)

    override fun run() {
        val loader = AccountsLoader(printer)
        val transactions = loader.load(inputs, sourceDefinitionFile)

        QifTransactionsEncoder().encode( transactions.flatten(), outputFile )

        printer.log( "Generated file $outputFile" )
    }
}


