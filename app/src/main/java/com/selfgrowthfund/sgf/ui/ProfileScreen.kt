package com.selfgrowthfund.sgf.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import com.selfgrowthfund.sgf.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    shareholderId: String = "",
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    SGFScaffoldWrapper(
        title = "Profile",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Name: ${user.name}")
            Text("Role: ${user.role.label}")
            Text("User ID: $shareholderId")

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()

                userSessionViewModel.updateUser(
                    User(
                        id = "", name = "", role = MemberRole.MEMBER,
                        shareholderId = shareholderId
                    )
                )

                val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                prefs.edit {
                    remove("user_pin")
                }

                navController.navigate(Screen.Welcome.route) {
                    popUpTo(0)
                }
            }) {
                Text("Logout")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ProfileScreen(
        navController = rememberNavController(),
        shareholderId = "SH001",
        drawerState = drawerState,
        scope = scope
    )
}