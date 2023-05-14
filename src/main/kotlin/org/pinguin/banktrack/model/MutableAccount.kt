package org.pinguin.banktrack.model

import java.text.SimpleDateFormat
import java.util.*

class UnMergeableItemsException(msg: String) : Exception(msg) {
    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        fun becauseInvalidDateRanges(date: Calendar) =
                UnMergeableItemsException("Can't merge items with dates from ${dateFormat.format(date.time)}")

        fun becauseDifferentAccountName(name1: String, name2: String) =
                UnMergeableItemsException("Can't merge items from account $name1 with account $name2")
    }
}

class MutableAccount(name: String, private val mutableItems: MutableList<Transaction>) :
        Account(name, mutableItems) {

    fun merge(other: Account) {
        if (name != other.name) {
            throw UnMergeableItemsException.becauseDifferentAccountName(name, other.name)
        }

        val insertionIndexes = findInsertion(this, other)
        val lastInserted = findLastInserted(other, insertionIndexes)

        this.mutableItems.addAll(insertionIndexes.first,
                other.subList(insertionIndexes.second, lastInserted))
    }

    private fun findLastInserted(others: List<Transaction>, insertionIndexes: Pair<Int, Int>): Int {
        val indexes = findInsertion(others, subList(insertionIndexes.first, size))
        return if (indexes.first == others.size) indexes.first - indexes.second
        else throw UnMergeableItemsException.becauseInvalidDateRanges(others[indexes.first].date)
    }

    companion object {
        private fun findInsertion(items: List<Transaction>, others: List<Transaction>): Pair<Int, Int> {
            if (others.isEmpty()) {
                return Pair(items.size, 0)
            }

            val firstDate = others.first().date
            val firstDateIndex = firstOf(items, firstDate)
            if (firstDateIndex < 0) {
                // Date not found in previous items. firstDateIndex is the index
                // where it should be.
                return Pair(-(firstDateIndex + 1), 0)
            }

            var currentIndex = firstDateIndex
            while ((currentIndex < items.size) && (items[currentIndex].date == firstDate)) {
                val equals = countEquals(items.subList(currentIndex, items.size), others)
                val firstDifferent = currentIndex + equals
                if ((firstDifferent == items.size) || (items[firstDifferent].date != firstDate)) {
                    return Pair(firstDifferent, equals)
                }
                ++currentIndex
            }

            return Pair(if (currentIndex == items.size) items.size else firstDateIndex, 0)
        }

        private fun firstOf(transactions: List<Transaction>, date: Calendar): Int {
            val pos = transactions.binarySearchBy(date, selector = { it.date })
            if (pos < 0) {
                return pos
            }
            val lastOfPreviousDate =
                    transactions.subList(0, pos).asReversed().withIndex().find { item -> item.value.date != date }
            return if (lastOfPreviousDate == null) 0 else pos - lastOfPreviousDate.index
        }

        private fun countEquals(s1: List<Transaction>, s2: List<Transaction>): Int {
            val different = (s1.withIndex() zip s2).find { item -> !item.first.value.equivalent(item.second) }
            return different?.first?.index ?: minOf(s1.size, s2.size)
        }
    }
}