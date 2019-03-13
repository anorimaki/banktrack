package org.pinguin.banktrack.model


open class Account(val name: String,
                   private val items: List<Transaction> ): List<Transaction> by items {
    fun withCategory( category: Category ): Account =
        Account( name, items.filter { transaction -> transaction.category == category } )

    val mismatchingBalances: List<Pair<Transaction, Transaction>>
        get() = this.items.
                    zipWithNext().
                    filter { pair ->  pair.first.balance + pair.second.amount != pair.second.balance }

    fun toMutable() = MutableAccount( name, items.toMutableList() )

    override fun equals(other: Any?): Boolean =
        (other is Account) && items == other.items

    override fun toString(): String = items.toString()
}