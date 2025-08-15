package com.selfgrowthfund.sgf.features.editshareholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

data class EditShareholderUiState(
    val name: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val success: Boolean = false,
    val isLoading: Boolean = false
)

class EditShareholderViewModel(
    private val dao: ShareholderDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditShareholderUiState())
    val uiState: StateFlow<EditShareholderUiState> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            dao.getShareholderById(id)?.let {
                _uiState.update {
                    it.copy(name = it.name, role = it.role, isLoading = false)
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateRole(role: MemberRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun save(id: String) {
        viewModelScope.launch {
            val updated = Shareholder(
                shareholderId = id,
                fullName = uiState.value.name,
                role = uiState.value.role,
                address = "Unknown",
                joinDate = Date(),
                mobileNumber = "0000000000",
                shareBalance = 0.0
            )
            dao.updateShareholder(updated)
            _uiState.update { it.copy(success = true) }
        }
    }}