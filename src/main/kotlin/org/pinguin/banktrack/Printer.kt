package org.pinguin.banktrack

class Printer {
    var indent = 0

    fun log( msg: String ) {
        repeat(indent) { print("  ") }
        println("- $msg")
    }

    fun beginSection( msg: String ) {
        log( msg )
        ++indent
    }

    fun endSection() {
        --indent
    }

    fun <T> section( msg: String, action: () -> T ): T {
        beginSection(msg)
        try {
            return action()
        }
        finally {
            endSection()
        }
    }
}