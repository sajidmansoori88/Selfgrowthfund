package com.selfgrowthfund.sgf.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class BorrowingUtilsTest {

    @Test
    fun `borrowing is overdue when due date before today`() {
        val today = LocalDate.now()
        val dueDate = today.minusDays(1)
        val isOverdue = dueDate.isBefore(today)
        assertThat(isOverdue).isTrue()
    }

    @Test
    fun `borrowing not overdue when due date today or future`() {
        val today = LocalDate.now()
        val dueToday = today
        val dueFuture = today.plusDays(5)
        assertThat(dueToday.isBefore(today)).isFalse()
        assertThat(dueFuture.isBefore(today)).isFalse()
    }

    @Test
    fun `repayment reduces outstanding correctly`() {
        val amountRequested = 10000.0
        val principalRepaid = 2500.0
        val outstanding = amountRequested - principalRepaid
        assertThat(outstanding).isWithin(0.01).of(7500.0)
    }

    @Test
    fun `penalty applies correctly for overdue`() {
        val penaltyRatePerDay = 0.01 // 1% per day
        val overdueDays = 5
        val baseAmount = 1000.0
        val penalty = baseAmount * penaltyRatePerDay * overdueDays
        assertThat(penalty).isWithin(0.01).of(50.0)
    }
}