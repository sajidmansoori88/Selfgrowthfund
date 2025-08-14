package com.selfgrowthfund.sgf.data.local.types

enum class PaymentStatus(val label: String) {
    ON_TIME("On Time"),
    LATE("Late"),
    PENDING("Pending");

    fun toDisplayString(): String = label

    companion object {
        fun fromString(value: String): PaymentStatus =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) }
                ?: PENDING
    }
}
