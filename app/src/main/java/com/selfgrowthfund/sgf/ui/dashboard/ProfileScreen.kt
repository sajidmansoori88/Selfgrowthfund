package com.selfgrowthfund.sgf.ui.dashboard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel

@Composable
fun ProfileScreen(
    shareholderId: String,
    onLogout: () -> Unit // navigation callback
) {
    val context = LocalContext.current
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val activeUser = user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Name: ${activeUser.name}")
        Text("Role: ${activeUser.role.label}")
        Text("User ID: $shareholderId")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            // Clear Firebase session
            FirebaseAuth.getInstance().signOut()

            // Reset session state
            userSessionViewModel.updateUser(
                User(id = "", name = "", role = MemberRole.MEMBER, shareholderId = "")
            )

            // Clear stored PIN
            val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            prefs.edit { remove("user_pin") }

            // Trigger navigation callback
            onLogout()
        }) {
            Text("Logout")
        }
    }
}