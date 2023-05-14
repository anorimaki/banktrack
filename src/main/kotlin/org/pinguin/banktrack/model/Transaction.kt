package org.pinguin.banktrack.model

import java.math.BigDecimal
import java.util.*


data class Transaction(
        val account: String,
        val date: Calendar,
        val memo: String,
        val amount: BigDecimal,
        val balance: BigDecimal,
        val category: Category) {

    fun equivalent(other: Transaction) =
            (account == other.account) &&
                    (date == other.date) &&
                    (memo.lowercase(Locale.getDefault()) == other.memo.lowercase(Locale.getDefault())) &&
                    (amount == other.amount) &&
                    (balance == other.balance) &&
                    (category == other.category)

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(date.time.toString())
        builder.append("; ")
        builder.append(category.name)
        builder.append("; ")
        builder.append(memo)
        builder.append("; ")
        builder.append(amount)
        return builder.toString()
    }
}

val Calendar.year: Int
    get() = get(Calendar.YEAR)

val Calendar.month: Int
    get() = get(Calendar.MONTH)