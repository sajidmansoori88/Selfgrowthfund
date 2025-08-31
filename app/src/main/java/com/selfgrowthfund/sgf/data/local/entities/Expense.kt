package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.LocalDate

@Entity(tableName = "expenses")
@TypeConverters(AppTypeConverters::class)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: Double,
    val remarks: String, // replaces 'description' and 'category'
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val recordedBy: String // shareholderId or adminId
)
