package com.selfgrowthfund.sgf.ui.editshareholders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class EditShareholderUiState(
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val dob: LocalDate? = LocalDate.now(),
    val address: String = "",
    val shareBalance: String = "",
    val joinDate: LocalDate? = LocalDate.now(),
    val role: MemberRole = MemberRole.MEMBER, // ✅ Enum type-safe
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditShareholderViewModel @Inject constructor(
    private val repository: ShareholderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditShareholderUiState())
    val uiState: StateFlow<EditShareholderUiState> = _uiState

    private var currentShareholder: Shareholder? = null

    fun load(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val shareholder = repository.getShareholderById(id)
            if (shareholder != null) {
                currentShareholder = shareholder
                _uiState.value = EditShareholderUiState(
                    name = shareholder.fullName,
                    mobile = shareholder.mobileNumber,
                    email = shareholder.email,
                    dob = shareholder.dob,
                    address = shareholder.address,
                    shareBalance = shareholder.shareBalance.toString(),
                    joinDate = shareholder.joiningDate,
                    role = shareholder.role, // ✅ Now a MemberRole directly
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Shareholder not found"
                )
            }
        }
    }

    // Field updates
    fun updateName(name: String) = update { it.copy(name = name) }
    fun updateMobile(mobile: String) = update { it.copy(mobile = mobile) }
    fun updateEmail(email: String) = update { it.copy(email = email) }
    fun updateDob(dob: LocalDate) = update { it.copy(dob = dob) }
    fun updateAddress(address: String) = update { it.copy(address = address) }
    fun updateShareBalance(balance: String) = update { it.copy(shareBalance = balance) }
    fun updateJoinDate(date: LocalDate) = update { it.copy(joinDate = date) }
    fun updateRole(role: MemberRole) = update { it.copy(role = role) }

    private fun update(transform: (EditShareholderUiState) -> EditShareholderUiState) {
        _uiState.value = transform(_uiState.value)
    }

    // Save changes
    fun save(id: String) {
        val state = _uiState.value
        val balance = state.shareBalance.toDoubleOrNull() ?: 0.0

        val updated = currentShareholder?.copy(
            fullName = state.name,
            mobileNumber = state.mobile,
            email = state.email,
            dob = state.dob,
            address = state.address,
            shareBalance = balance,
            joiningDate = state.joinDate,
            role = state.role, // ✅ Stored as enum
            updatedAt = Instant.now(),
            lastUpdated = Instant.now()
        ) ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.updateShareholder(updated)
                syncShareholderToFirestore(updated) // ✅ Firestore sync
                _uiState.value = _uiState.value.copy(success = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    // Firestore sync
    private fun syncShareholderToFirestore(shareholder: Shareholder) {
        val db = Firebase.firestore
        val formatter = DateTimeFormatter.ISO_DATE

        val firestoreData = mapOf(
            "shareholderId" to shareholder.shareholderId,
            "fullName" to shareholder.fullName,
            "mobileNumber" to shareholder.mobileNumber,
            "email" to shareholder.email,
            "dob" to shareholder.dob?.format(formatter),
            "address" to shareholder.address,
            "shareBalance" to shareholder.shareBalance,
            "joiningDate" to shareholder.joiningDate?.format(formatter),
            "role" to shareholder.role.name // ✅ Enum name saved to Firestore
        )

        db.collection("shareholders").document(shareholder.shareholderId)
            .set(firestoreData)
            .addOnSuccessListener {
                Log.d("Firestore", "Shareholder updated: ${shareholder.shareholderId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Update failed", e)
            }
    }

    // Delete Shareholder
    fun deleteShareholder() {
        val shareholder = currentShareholder ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.deleteShareholder(shareholder)
                _uiState.value = _uiState.value.copy(success = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }
}