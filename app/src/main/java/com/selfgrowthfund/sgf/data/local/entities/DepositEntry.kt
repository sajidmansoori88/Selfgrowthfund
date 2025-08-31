package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.model.enums.DepositStatus
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(
    tableName = "deposit_entries",
    indices = [
        Index(value = ["shareholderId"], name = "index_deposit_entries_shareholderId"),
        Index(value = ["dueMonth"], name = "index_deposit_entries_dueMonth")
    ]
)
@TypeConverters(AppTypeConverters::class)
data class DepositEntry(
    @PrimaryKey
    @ColumnInfo(name = "depositId")
    val depositId: String,

    @ColumnInfo(name = "shareholderId")
    val shareholderId: String,

    @ColumnInfo(name = "shareholderName")
    val shareholderName: String,

    @ColumnInfo(name = "dueMonth")
    val dueMonth: DueMonth,

    @ColumnInfo(name = "paymentDate")
    val paymentDate: LocalDate,

    @ColumnInfo(name = "shareNos")
    val shareNos: Int,

    @ColumnInfo(name = "shareAmount")
    val shareAmount: Double = 2000.0,

    @ColumnInfo(name = "additionalContribution")
    val additionalContribution: Double = 0.0,

    @ColumnInfo(name = "penalty")
    val penalty: Double = 0.0,

    @ColumnInfo(name = "totalAmount")
    val totalAmount: Double,

    @ColumnInfo(name = "paymentStatus")
    val paymentStatus: String,

    @ColumnInfo(name = "modeOfPayment")
    val modeOfPayment: PaymentMode?,

    @ColumnInfo(name = "status")
    val status: DepositStatus = DepositStatus.Pending,

    @ColumnInfo(name = "approvedBy")
    val approvedBy: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "createdAt")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "entrySource")
    val entrySource: EntrySource = EntrySource.USER
) {
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_APPROVED = "Approved"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_AUTO_REJECTED = "Auto-Rejected"

        const val PAYMENT_ON_TIME = "On-time"
        const val PAYMENT_LATE = "Late"
        const val MODE_CASH = "Cash"
        const val MODE_ONLINE = "Online"

        fun calculatePenalty(dueMonth: String, paymentDate: LocalDate): Double {
            return try {
                val formatter = DateTimeFormatter.ofPattern("MMM-yyyy")
                val dueDate = LocalDate.parse(dueMonth, formatter).withDayOfMonth(10)

                if (paymentDate.isAfter(dueDate)) {
                    val daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate).toInt()
                    daysLate * 5.0
                } else {
                    0.0
                }
            } catch (e: Exception) {
                0.0
            }
        }

        fun calculateTotalAmount(
            shareNos: Int,
            shareAmount: Double,
            additionalContribution: Double,
            penalty: Double
        ): Double {
            return (shareNos * shareAmount) + additionalContribution + penalty
        }
    }
}