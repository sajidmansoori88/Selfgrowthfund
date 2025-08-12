package com.selfgrowthfund.sgf.data.local.types

data class DueMonth(val year: Int, val month: Int) {
    override fun toString(): String = "%04d-%02d".format(year, month)

    companion object {
        fun parse(value: String): DueMonth {
            val parts = value.split("-")
            return DueMonth(parts[0].toInt(), parts[1].toInt())
        }
    }
}