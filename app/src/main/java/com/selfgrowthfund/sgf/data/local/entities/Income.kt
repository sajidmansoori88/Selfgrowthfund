package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import java.time.LocalDate

@Entity(tableName = "incomes")
@TypeConverters(AppTypeConverters::class)
data class  Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: Double,
    val remarks: String, // replaces 'source' and 'description'
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val approvalStatus: ApprovalAction=ApprovalAction.PENDING,
    val recordedBy: String // shareholderId or adminId
)
