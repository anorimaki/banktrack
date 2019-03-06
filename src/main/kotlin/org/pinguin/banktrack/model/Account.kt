package org.pinguin.banktrack.model


open class Account(val name: String,
                   private val items: List<Transaction> ): List<Transaction> by items {
    fun withCategory( category: Category ): Account =
        Account( name, items.filter { transaction -> transaction.category == category } )

    val mismatchingBalances: List<Pair<Transaction, Transaction>>
        get() = this.items.
                    zipWithNext().
                    filter { pair ->
                        val ret = pair.first.balance + pair.second.amount != pair.second.balance
                        if ( ret ) {
                            println("${pair.first.balance}, ${pair.second.amount} = ${pair.second.balance}")
                        }
                        ret
                    }

    fun toMutable() = MutableAccount( name, items.toMutableList() )

    override fun equals(other: Any?): Boolean =
        (other is Account) && items == other.items

    override fun toString(): String = items.toString()
}