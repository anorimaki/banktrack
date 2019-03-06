package org.pinguin.banktrack

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class BankTrack: CliktCommand(help = "Bank track utility") {
    override fun run() = Unit
}


fun main(args : Array<String>) =
        BankTrack().subcommands(XLS2QIF(), XLSReport()).main(args)