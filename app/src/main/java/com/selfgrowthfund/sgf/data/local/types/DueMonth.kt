package com.selfgrowthfund.sgf.data.local.types

@JvmInline
value class DueMonth(val value: String) {
    override fun toString(): String = value

    companion object {
        fun from(value: String): DueMonth = DueMonth(value)
    }
}
