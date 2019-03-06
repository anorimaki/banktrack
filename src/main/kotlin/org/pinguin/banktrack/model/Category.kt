package org.pinguin.banktrack.model

class Category( val name: String,
                private val accepted: Set<Regex>,
                private val negative: Set<Regex> ) {
    val components: List<String> = name.split(':')

    fun match( memo: String ): Boolean =
            accepted.any { regexp -> regexp.containsMatchIn(memo) } &&
                    negative.all { regexp -> !regexp.containsMatchIn(memo) }

    override fun equals(other: Any?): Boolean {
        return (other is Category) && other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}