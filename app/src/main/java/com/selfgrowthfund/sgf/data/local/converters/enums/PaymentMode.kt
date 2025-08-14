package com.selfgrowthfund.sgf.data.local.types

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE_PAYMENT("Online Payment"),
    MOBILE_MONEY("Mobile Money"),
    CHEQUE("Cheque"),
    OTHER("Other Payment Method");  // Added missing OTHER case and semicolon

    companion object {
        fun fromDisplayName(displayName: String): PaymentMode {
            return entries.firstOrNull { it.displayName == displayName } ?: OTHER
        }

        // Bonus: For Spinner/UI usage
        fun getAllDisplayNames(): List<String> = entries.map { it.displayName }
    }
}