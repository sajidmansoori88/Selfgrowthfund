package com.selfgrowthfund.sgf.session

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor() : ViewModel() {
    var currentUser = mutableStateOf(
        User(
            id = "U001",
            name = "Admin",
            role = MemberRole.MEMBER_ADMIN
        )
    )
}