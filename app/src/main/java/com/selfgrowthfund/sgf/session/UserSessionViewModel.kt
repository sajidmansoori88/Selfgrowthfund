package com.selfgrowthfund.sgf.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor() : ViewModel() {

    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser

    /** Called after successful login or registration */
    fun updateUser(user: User) {
        viewModelScope.launch { _currentUser.value = user }
    }

    /** Clears the current session (e.g., on logout) */
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
}
