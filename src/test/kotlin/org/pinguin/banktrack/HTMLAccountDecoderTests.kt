package org.pinguin.banktrack


import org.junit.Test
import org.pinguin.banktrack.codecs.parseSourceStructure
import org.pinguin.banktrack.codecs.transactionsDecoderFactory
import java.io.InputStream
import kotlin.test.assertEquals


class HTMLAccountDecoderTests {
    @Test
    fun testSantander() {
        val structure = withResource("html.def"){ parseSourceStructure(it) }
        val decoder = transactionsDecoderFactory(structure)
        val transactions = withResource( "santander_2015_05_23.xls") { decoder.decode(it) }

        assertEquals( 63, transactions.size )
    }

    private fun getResource( name: String ): InputStream =
        HTMLAccountDecoderTests::class.java.getResource(name).openStream()

    private fun <T> withResource( name: String, block: (InputStream) -> T ): T =
        getResource(name).use(block)
}