package com.selfgrowthfund.sgf.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.theme.PrimaryGreen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException
import com.selfgrowthfund.sgf.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavHostController,
    shareholderId: String = "" // Default for preview or fallback
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val email = it.user?.email
                    val name = it.user?.displayName ?: "User"
                    val uid = it.user?.uid ?: "unknown"

                    if (email != null) {
                        firestore.collection("Shareholders")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val doc = querySnapshot.documents.first()
                                    val roleString = doc.getString("role") ?: "MEMBER"
                                    val role = MemberRole.valueOf(roleString)

                                    val user = User(
                                        id = uid,
                                        name = name,
                                        role = role,
                                        shareholderId = shareholderId // ✅ Now passed into session
                                    )
                                    userSessionViewModel.updateUser(user)

                                    when (role) {
                                        MemberRole.MEMBER_ADMIN -> navController.navigate(Screen.AdminDashboard.route)
                                        MemberRole.MEMBER_TREASURER -> navController.navigate(Screen.TreasurerDashboard.route)
                                        MemberRole.MEMBER -> navController.navigate(Screen.Home.route)
                                    }
                                } else {
                                    navController.navigate(Screen.AccessDenied.route)
                                }
                            }
                            .addOnFailureListener {
                                navController.navigate(Screen.AccessDenied.route)
                            }
                    } else {
                        navController.navigate(Screen.AccessDenied.route)
                    }
                }
                .addOnFailureListener {
                    navController.navigate(Screen.AccessDenied.route)
                }
        } catch (_: Exception) {
            navController.navigate(Screen.AccessDenied.route)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login to Self Growth Fund",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryGreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID") // Replace with actual client ID
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, gso)
            launcher.launch(client.signInIntent)
        }) {
            Text("Sign in with Google")
        }
    }
}