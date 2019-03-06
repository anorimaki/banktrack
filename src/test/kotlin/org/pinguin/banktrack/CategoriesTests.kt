package org.pinguin.banktrack

import org.ini4j.Ini
import org.ini4j.Profile
import org.junit.Test
import org.pinguin.banktrack.model.Categories
import kotlin.test.assertEquals


class CategoriesTests {
    @Test
    fun testMultiples() {
        val categoriesDef = profileSection(
            "Colegio:Escola1" to "AMPA ESCOLA1; Escola1; C\\.P\\. Escola; ESCOLA PUBLICA",
            "Comunidad vecinos:casa1" to "FINQUES pepito",
            "Comunidad vecinos:casa2" to "PROPIETARIOS DE casa2; CMDAD DE PROPIETARIOS DE casa; casa2",
            "Efectivo" to "DISPOSICION EN CAJERO",
            "Teléfono:Móvil pepe" to "Pepemobile; pepe; PEPEPHONE; PEPEMOBILE"
        )
        val categories = Categories.parse(categoriesDef)

        assertEquals( "Comunidad vecinos:casa1",
                categories.find("RECIBO ADMINISTRACIO FINQUES pepito").name )

        assertEquals( "Colegio:Escola1",
                categories.find("RECIBO C.P. Escola Nº RECIBO").name )

        assertEquals( "Teléfono:Móvil pepe",
                categories.find("RECIBO PEPEMOBILE, S.L. Nº RECIBO").name )
    }

    private fun profileSection( vararg entries: Pair<String, String> ): Profile.Section {
        val ini = Ini()
        val section = ini.add("categories")
        section.putAll( entries )
        return section
    }
}