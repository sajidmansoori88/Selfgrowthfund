package com.selfgrowthfund.sgf.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShareholderSummaryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _summary = MutableStateFlow(ShareholderSummary())
    val summary: StateFlow<ShareholderSummary> = _summary

    fun loadSummary(user: User) {
        viewModelScope.launch {
            firestore.collection("Shareholders").document(user.id).get()
                .addOnSuccessListener { doc ->
                    val shares = doc.getLong("shares")?.toInt() ?: 0
                    val lastDateStr = doc.getString("lastContribution") ?: "2025-08-10"
                    val lastDate = LocalDate.parse(lastDateStr, DateTimeFormatter.ISO_DATE)
                    val borrowing = doc.getDouble("outstandingBorrowing") ?: 0.0

                    firestore.collection("GroupMeta").document("meta").get()
                        .addOnSuccessListener { groupDoc ->
                            val totalShares = groupDoc.getLong("totalShares") ?: 1
                            val assetValue = groupDoc.getDouble("totalAssetValue") ?: 0.0

                            val contribution = shares * 2000
                            val sharePercent = shares.toDouble() / totalShares
                            val currentValue = sharePercent * assetValue
                            val growthPercent = ((currentValue - contribution) / contribution) * 100
                            val nextDue = LocalDate.of(
                                lastDate.plusMonths(1).year,
                                lastDate.plusMonths(1).month,
                                10
                            )

                            _summary.value = ShareholderSummary(
                                totalShareContribution = contribution,
                                currentValue = currentValue,
                                growthPercent = growthPercent,
                                lastContribution = lastDate,
                                nextDue = nextDue,
                                outstandingBorrowing = borrowing
                            )
                        }
                }
        }
    }
}

data class ShareholderSummary(
    val totalShareContribution: Int = 0,
    val currentValue: Double = 0.0,
    val growthPercent: Double = 0.0,
    val lastContribution: LocalDate = LocalDate.now(),
    val nextDue: LocalDate = LocalDate.now(),
    val outstandingBorrowing: Double = 0.0
)