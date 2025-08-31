package com.selfgrowthfund.sgf.session

import androidx.lifecycle.ViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor() : ViewModel() {
    private val _currentUser = MutableStateFlow(
        User(
            id = "U001",
            name = "Admin",
            role = MemberRole.MEMBER_ADMIN,
            shareholderId = "S001"
        )
    )
    val currentUser: StateFlow<User> = _currentUser

    fun updateUser(user: User) {
        _currentUser.value = user
    }

    fun clearSession() {
        _currentUser.value = User(
            id = "",
            name = "",
            role = MemberRole.MEMBER,
            shareholderId = ""
        )

    }

}
