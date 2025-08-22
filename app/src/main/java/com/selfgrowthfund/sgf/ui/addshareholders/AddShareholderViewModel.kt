package com.selfgrowthfund.sgf.ui.addshareholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.Result
import com.selfgrowthfund.sgf.utils.mappers.toShareholder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    val isFullNameValid: StateFlow<Boolean> = fullName.map { it.trim().split(" ").all { word -> word.firstOrNull()?.isUpperCase() == true } }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isMobileValid: StateFlow<Boolean> = mobileNumber.map { it.matches(Regex("^\\d{10}\$")) }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isEmailValid: StateFlow<Boolean> = email.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isDobValid: StateFlow<Boolean> = dob.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isAddressValid: StateFlow<Boolean> = address.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isJoiningDateValid: StateFlow<Boolean> = joiningDate.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isRoleValid: StateFlow<Boolean> = role.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isShareBalanceValid: StateFlow<Boolean> = shareBalance.map { it.toDoubleOrNull()?.let { it >= 0 } == true }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // ─────────────── Combined Save Enable ───────────────
    val canSave: StateFlow<Boolean> = combine(
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
            role = role.value?.name ?: MemberRole.MEMBER.name
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
                    is Result.Success -> _saveSuccess.value = true
                    is Result.Error -> _errorMessage.value = result.exception.message
                    Result.Loading -> {} // Optional
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isSaving.value = false
            }
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