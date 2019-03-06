package org.pinguin.banktrack.model

import java.math.BigDecimal
import java.util.*
import sun.text.normalizer.UTF16.append
import jdk.management.resource.internal.ApproverGroup.getGroup
import jdk.nashorn.internal.objects.NativeDate.getTime



data class Transaction(
    val account: String,
    val date: Calendar,
    val memo: String,
    val amount: BigDecimal,
    val balance: BigDecimal,
    val category: Category) {

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
    get() = get( Calendar.YEAR )

val Calendar.month: Int
    get() = get( Calendar.MONTH )