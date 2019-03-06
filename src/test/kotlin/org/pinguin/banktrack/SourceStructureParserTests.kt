package org.pinguin.banktrack

import org.junit.Test
import org.pinguin.banktrack.codecs.SourceType
import org.pinguin.banktrack.codecs.parseSourceStructure
import kotlin.test.assertEquals


class SourceStructureParserTests {
    @Test
    fun parseHTML() {
        val structure = SourceStructureParserTests::class.java.getResource("html.def").
                openStream().use { parseSourceStructure(it) }
        assertEquals( structure.type, SourceType.HTML )
        assertEquals( 11, structure.startRow )
        assertEquals( 1, structure.dateColumn )
        assertEquals( 5, structure.memoColumn )
        assertEquals( 7, structure.amountColumn )

        assertEquals( "Cat1:Subcat1", structure.categories.find("MEMO example cat1.1" ).name )
        assertEquals( "Cat1:Subcat1", structure.categories.find("MEMO example c1.1" ).name )
        assertEquals( "Cat1:Others", structure.categories.find("MEMO example c1" ).name )
        assertEquals( "Cat2:Subcat1", structure.categories.find("MEMO example cat2.1" ).name )
    }
}