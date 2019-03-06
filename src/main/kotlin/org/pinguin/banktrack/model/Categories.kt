package org.pinguin.banktrack.model

import org.ini4j.Profile


class Categories( private val categories: Collection<Category> ) {
    fun find(memo: String): Category {
        val matches = categories.filter { category -> category.match(memo) }
        if (matches.size > 1) {
            throw Exception( "Ambiguous category for description $memo. Matches: ${matches.map { it.name }}")
        }
        return matches.firstOrNull() ?: UNCLASSIFIED
    }

    companion object {
        val UNCLASSIFIED = Category("", emptySet(), emptySet() )

        fun parse( section: Profile.Section ): Categories {
            val categories = mutableMapOf<String, Pair<Set<Regex>, Set<Regex>>>()
            section.keys.forEach { key ->
                val c = section.length(key)
                val regexpSet = mutableSetOf<Regex>()
                for( i in 0..(c-1) ) {
                    val value = section.fetch( key, i )
                    val currentRegexps = value.split(";").map { regExp -> Regex(regExp.trim()) }
                    regexpSet.addAll(currentRegexps)
                }

                val categoryName = parseName(key)
                val previousVal = categories[categoryName] ?: Pair(emptySet(), emptySet())
                val newVal = if (isNegate(key)) Pair( previousVal.first, regexpSet )
                            else Pair( regexpSet, previousVal.second )
                categories[categoryName] = newVal
            }

            return Categories( categories.map { Category( it.key, it.value.first, it.value.second ) })
        }

        private fun isNegate( key: String ) =
                key[0] == '^'

        private fun parseName( key: String ) =
                key.removePrefix("^").replace('.', ':')
    }
}
