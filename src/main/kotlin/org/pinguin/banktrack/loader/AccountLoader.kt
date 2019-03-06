package org.pinguin.banktrack.loader

import org.pinguin.banktrack.Printer
import org.pinguin.banktrack.model.Account
import java.io.File

class AccountLoader(private val printer: Printer) {
    fun load(inputs: Iterable<File>,
             sourceDefinitionFile: File? ): Account {
        val loader = AccountsLoader(printer)
        val accounts = loader.load(inputs, sourceDefinitionFile)
        if ( accounts.size > 1 ) {
            throw Exception( "Account from multiples accounts found in input files: ${accounts.size}" )
        }
        return accounts.first()
    }
}