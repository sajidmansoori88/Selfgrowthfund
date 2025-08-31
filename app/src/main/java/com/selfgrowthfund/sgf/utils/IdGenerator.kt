package com.selfgrowthfund.sgf.utils

object IdGenerator {

    /**
     * Generates the next ID based on the last known ID, prefix, and padding.
     * Example: lastId = "SH002", prefix = "SH", padLength = 3 → returns "SH003"
     */
    fun generateNextId(lastId: String?, prefix: String, padLength: Int): String {
        val number = lastId
            ?.removePrefix(prefix)
            ?.toIntOrNull() ?: 0

        val nextNumber = number + 1
        return prefix + nextNumber.toString().padStart(padLength, '0')
    }

    // 🔹 Specific generators for each entity type
    fun nextShareholderId(lastId: String?) = generateNextId(lastId, "SH", 3)
    fun nextDepositId(lastId: String?) = generateNextId(lastId, "D", 4)
    fun nextBorrowId(lastId: String?) = generateNextId(lastId, "B", 4)
    fun nextRepaymentId(lastId: String?) = generateNextId(lastId, "RP", 4)
    fun nextInvestmentId(lastId: String?) = generateNextId(lastId, "INV", 3)
    fun nextInvestmentReturnsId(lastId: String?) = generateNextId(lastId, "IR", 3)
    fun nextExpenseId(lastId: String?) = generateNextId(lastId, "EX", 4)
    fun nextIncomeId(lastId: String?) = generateNextId(lastId, "INC", 4)
}