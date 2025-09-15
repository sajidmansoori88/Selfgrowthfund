package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "other_expenses")
data class OtherExpenses(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: Double,
    val remarks: String, // replaces 'description' and 'category'
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val approvalStatus: ApprovalAction = ApprovalAction.PENDING,
    val recordedBy: String, // shareholderId or adminId
    val createdAt: Instant = Instant.now()
)
