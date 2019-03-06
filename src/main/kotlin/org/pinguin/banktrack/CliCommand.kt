package org.pinguin.banktrack

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

fun outputFileOption( command: CliktCommand ): OptionDelegate<File> =
    command.option( "-o", "--output", help = "Output file" ).file().required()

fun sourceDefinitionFileOption( command: CliktCommand ): OptionDelegate<File> =
        command.option( "-o", "--output", help = "Output file" ).file().required()


abstract class CliCommand( name: String, help: String ): CliktCommand( name = name, help = help ) {
    protected val sourceDefinitionFile: File? by
            option( "-s", "--spec", help = "Source structure definition file" ).file(readable=true)
    protected val inputs by argument( "Input files", help = "Input source files" ).
            file(readable=true).multiple()

    protected val printer = Printer()
}
