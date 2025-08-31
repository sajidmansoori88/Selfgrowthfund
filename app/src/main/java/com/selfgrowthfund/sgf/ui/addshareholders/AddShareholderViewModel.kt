package com.selfgrowthfund.sgf.ui.addshareholders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import com.selfgrowthfund.sgf.utils.mappers.toShareholder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddShareholderViewModel @Inject constructor(
    private val shareholderRepository: ShareholderRepository
) : ViewModel() {

    // ─────────────── Input Fields ───────────────
    val fullName = MutableStateFlow("")
    val mobileNumber = MutableStateFlow("")
    val email = MutableStateFlow("")
    val dob = MutableStateFlow<LocalDate?>(null)
    val address = MutableStateFlow("")
    val joiningDate = MutableStateFlow<LocalDate?>(null)
    val role = MutableStateFlow<MemberRole?>(null)
    val shareBalance = MutableStateFlow("")

    // ─────────────── Validation States ───────────────
    val isFullNameValid = fullName.map { it.trim().split(" ").all { word -> word.firstOrNull()?.isUpperCase() == true } }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isMobileValid = mobileNumber.map { it.matches(Regex("^\\d{10}\$")) }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isEmailValid = email.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isDobValid = dob.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isAddressValid = address.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isJoiningDateValid = joiningDate.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isRoleValid = role.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isShareBalanceValid = shareBalance.map { it.toDoubleOrNull()?.let { it >= 0 } == true }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canSave = combine(
        isFullNameValid,
        isMobileValid,
        isEmailValid,
        isDobValid,
        isAddressValid,
        isJoiningDateValid,
        isRoleValid,
        isShareBalanceValid
    ) { validations -> validations.all { it } }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // ─────────────── Save State ───────────────
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ─────────────── Show Next ID ───────────────
    val nextShareholderId = MutableStateFlow<String?>(null)

    fun fetchNextShareholderId() {
        viewModelScope.launch {
            val lastId = shareholderRepository.getLastShareholderId()
            nextShareholderId.value = IdGenerator.nextShareholderId(lastId)
        }
    }


    // ─────────────── Add Shareholder ───────────────
    fun addShareholder() {
        val entry = ShareholderEntry(
            fullName = fullName.value.trim(),
            mobileNumber = mobileNumber.value.trim(),
            email = email.value.trim(),
            dob = dob.value ?: LocalDate.now(),
            address = address.value.trim(),
            shareBalance = shareBalance.value.toDoubleOrNull() ?: 0.0,
            joiningDate = joiningDate.value ?: LocalDate.now(),
            role = role.value ?: MemberRole.MEMBER
        )


        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _saveSuccess.value = null

            try {
                val lastId = shareholderRepository.getLastShareholderId()
                val shareholder = entry.toShareholder(lastId)
                val result = shareholderRepository.addShareholder(shareholder)

                when (result) {
                    is Result.Success -> {
                        _saveSuccess.value = true
                        syncShareholderToFirestore(shareholder)
                    }
                    is Result.Error -> _errorMessage.value = result.exception.message
                    Result.Loading -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }


    // ─────────────── Firestore Sync ───────────────
    private fun syncShareholderToFirestore(shareholder: com.selfgrowthfund.sgf.data.local.entities.Shareholder) {
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
            "role" to shareholder.role
        )

        db.collection("shareholders").document(shareholder.shareholderId)
            .set(firestoreData)
            .addOnSuccessListener {
                Log.d("Firestore", "Shareholder synced: ${shareholder.shareholderId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Sync failed", e)
            }
    }

    // ─────────────── Firestore Ping ───────────────
    fun testFirestoreConnection() {
        val db = Firebase.firestore
        db.collection("test").document("ping")
            .set(mapOf("status" to "connected"))
            .addOnSuccessListener {
                Log.d("Firestore", "Ping successful")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Ping failed", e)
            }
    }

    // ─────────────── Reset Functions ───────────────
    fun resetSaveSuccess() {
        _saveSuccess.value = null
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }
}