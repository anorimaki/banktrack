package org.pinguin.banktrack

import org.junit.Test
import org.pinguin.banktrack.model.*
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AccountMergeTests {
    @Test
    fun testMergeEmpty() {
        test(listOf("0a, a1, a2"), listOf(), listOf("0a, a1, a2"))

        test(listOf(), listOf(), listOf())

        test(listOf(), listOf("0a, a1, a2"), listOf("0a, a1, a2"))
    }

    @Test
    fun testYetMerged() {
        test(listOf("4a", "4b", "5a"),
                listOf("4a", "4b", "5a"),
                listOf("4a", "4b", "5a"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("4a", "4b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("0a", "0b", "0c", "1a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("1a", "2b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "4a", "4b"),
                listOf("4a", "4b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "4a", "4b"))
    }

    @Test
    fun testMergeOlderEntries() {
        test(listOf("4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))

        test(listOf("4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))

        test(listOf("4a", "4b", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "4a", "4b", "5a"))

        test(listOf("0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))

        test(listOf("1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))
    }

    @Test
    fun testMergeNewerEntries() {
        test(listOf("0a", "0b", "0c", "1a", "2b", "3c"),
                listOf("4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("4a", "4b", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "4a", "4b", "5a"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"))
    }

    @Test
    fun testMergeInTheMiddle() {
        test(listOf("4a", "4b", "5a", "5a", "7a", "7b", "7a", "7a", "8a"),
                listOf("6a", "6b", "6c"),
                listOf("4a", "4b", "5a", "5a", "6a", "6b", "6c", "7a", "7b", "7a", "7a", "8a"))

        test(listOf("4a", "4b", "5a", "5a", "7a", "7b", "7a", "7a", "8a"),
                listOf("4b", "5a", "5a", "6a", "6b", "6c"),
                listOf("4a", "4b", "5a", "5a", "6a", "6b", "6c", "7a", "7b", "7a", "7a", "8a"))

        test(listOf("4a", "4b", "5a", "5a", "7a", "7b", "7a", "7a", "8a"),
                listOf("6a", "6b", "6c", "7a", "7b"),
                listOf("4a", "4b", "5a", "5a", "6a", "6b", "6c", "7a", "7b", "7a", "7a", "8a"))

        test(listOf("4a", "4b", "5a", "5a", "7a", "7b", "7a", "7a", "8a"),
                listOf("6a", "6b", "6c", "7c", "7d"),
                listOf("4a", "4b", "5a", "5a", "6a", "6b", "6c", "7c", "7d", "7a", "7b", "7a", "7a", "8a"))

        test(listOf("4a", "4b", "5a", "5a", "7a", "7b", "7a", "7a", "8a"),
                listOf("6a", "6b", "6c", "7c", "7d", "7a", "7b"),
                listOf("4a", "4b", "5a", "5a", "6a", "6b", "6c", "7c", "7d", "7a", "7b", "7a", "7a", "8a"))

        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("4a", "4b", "4a", "4b", "5a"),
                listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "4a", "4b", "5a"))
    }


    @Test
    fun testNotMergableNewEntries() {
        test(listOf("0a", "0b", "0c", "1a", "2b", "3c", "4a", "4b", "5a"),
                listOf("4a", "4b", "4a", "4b", "6a"),
                UnMergeableItemsException::class)
    }

    private fun test(s1: List<String>, s2: List<String>, sExpected: List<String>) {
        val t1 = toTransactions(s1)
        val t2 = toTransactions(s2)
        val expected = toTransactions(sExpected)

        t1.merge(t2)
        assertEquals(expected, t1)
    }

    private fun test(s1: List<String>, s2: List<String>, expected: KClass<out Exception>) {
        val t1 = toTransactions(s1)
        val t2 = toTransactions(s2)

        assertFailsWith(expected) {
            t1.merge(t2)
        }
    }

    private fun toTransactions(s: List<String>) =
            MutableAccount("dummy", s.map { str -> toTransaction(str) }.toMutableList())

    private fun toTransaction(s: String): Transaction {
        val date = Calendar.getInstance()
        date.clear()
        date.set(1999, 5, (s[0].code - '0'.code) + 1)
        return Transaction("dummy",
                date,
                s.substring(1), BigDecimal("0.0"),
                BigDecimal("0.0"), Categories.UNCLASSIFIED)
    }
}