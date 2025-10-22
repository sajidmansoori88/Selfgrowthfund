package com.selfgrowthfund.sgf.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor() : ViewModel() {

        private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser

    // ✅ Initialize with a default Treasurer (can be changed later)
    init {
        _currentUser.value = User(
            id = "U_TREASURER",
            name = "Treasurer User",
            role = MemberRole.MEMBER_TREASURER,
            shareholderId = "SH002"
        )
    }


    fun updateUser(user: User) {
        viewModelScope.launch {
            _currentUser.value = user
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            _currentUser.value = User(
                id = "",
                name = "",
                role = MemberRole.MEMBER,
                shareholderId = ""
            )
        }
    }

    /** ✅ Utility for test/mocked users (Admin / Treasurer / Member) */
    fun setMockUser(user: User) {
        viewModelScope.launch {
            _currentUser.value = user
        }
    }

    /** ✅ Toggle between Admin and Treasurer easily for testing */
    fun toggleUserRole() {
        viewModelScope.launch {
            val current = _currentUser.value
            val newRole = when (current.role) {
                MemberRole.MEMBER_ADMIN -> MemberRole.MEMBER_TREASURER
                MemberRole.MEMBER_TREASURER -> MemberRole.MEMBER_ADMIN
                else -> MemberRole.MEMBER_TREASURER
            }

            val newShareholderId = when (newRole) {
                MemberRole.MEMBER_ADMIN -> "SH001"
                MemberRole.MEMBER_TREASURER -> "SH002"
                else -> current.shareholderId
            }

            _currentUser.value = current.copy(role = newRole, shareholderId = newShareholderId)
        }
    }
    fun setMockUserSync(user: User) {
        _currentUser.value = user
    }
}
