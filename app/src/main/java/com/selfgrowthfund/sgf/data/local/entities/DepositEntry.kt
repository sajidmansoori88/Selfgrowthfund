package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.DepositStatus
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import java.text.SimpleDateFormat
import java.util.*

// ✅ Room-safe constants (used in default values and queries)
const val STATUS_PENDING = "Pending"
const val STATUS_APPROVED = "Approved"
const val STATUS_REJECTED = "Rejected"
const val STATUS_AUTO_REJECTED = "Auto-Rejected"

// Optional: UI-only constants can stay here too if needed globally
const val PAYMENT_ON_TIME = "On-time"
const val PAYMENT_LATE = "Late"
const val MODE_CASH = "Cash"
const val MODE_ONLINE = "Online"

@Entity(
    tableName = "deposit_entries",
    indices = [Index(value = ["shareholderId"], name = "index_deposit_entries_shareholderId"),
    Index(value = ["dueMonth"],name = "index_deposit_entries_dueMonth")]
)
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
    val paymentDate: String,

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
    val modeOfPayment: String,

    @ColumnInfo(name = "status")
    val status: DepositStatus = DepositStatus.Pending,

    @ColumnInfo(name = "approvedBy")
    val approvedBy: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateNextId(lastId: String?): String {
            return lastId?.let {
                val num = it.removePrefix("D").toIntOrNull() ?: 0
                "D%04d".format(num + 1)
            } ?: "D0001"
        }

        fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
            return try {
                val dueFormatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                val payFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val dueDate = dueFormatter.parse("10-$dueMonth")
                val actualDate = payFormatter.parse(paymentDate)

                if (actualDate != null && dueDate != null && actualDate.after(dueDate)) {
                    val diffMillis = actualDate.time - dueDate.time
                    val daysLate = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
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