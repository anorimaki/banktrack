package org.pinguin.banktrack.codecs

import org.pinguin.banktrack.BankTrack
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


fun sourceDefinitionForFile( file: File ): SourceStructure {
    val nameParts = file.nameWithoutExtension.split('.')
    if ( nameParts.size == 1 ) {
        throw Exception("Can't infer structure type for source $file")
    }
    val structureDefinitionName = nameParts[nameParts.size-1]

    return findSourceDefinitionStream(folder(file), structureDefinitionName).use {
        parseSourceStructure(it)
    }
}

private fun folder( file: File ) = file.parentFile ?: File(".")


private fun findSourceDefinitionStream( folder: File, sourceDefinitionName: String ): InputStream {
    val defFileName = "$sourceDefinitionName.def"

    //Try in same directory as file
    val defFileInFolder = File(folder, defFileName)
    if ( defFileInFolder.isFile ) {
        return FileInputStream(defFileInFolder)
    }

    //Try in same directory as Xl2Qif jar
    val jarName = BankTrack::class.java.protectionDomain.codeSource.location.path
    val jarFolder = folder(File(jarName))
    val defFileInJarFolder = File(jarFolder, defFileName)
    return if (defFileInJarFolder.isFile) FileInputStream(defFileInJarFolder)
             else BankTrack::class.java.classLoader.getResourceAsStream(defFileName)
}