package com.selfgrowthfund.sgf.data.local.types

enum class ShareholderStatus(val label: String) {
    Active("Active"),
    Inactive("Inactive");

    companion object {
        fun fromString(value: String): ShareholderStatus =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: Inactive
    }
}
