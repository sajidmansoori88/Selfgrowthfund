package com.selfgrowthfund.sgf.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.R
import com.selfgrowthfund.sgf.ui.navigation.Screen
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ Modern activity launcher (replaces deprecated startActivityForResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                if (account != null) {
                    loading = true
                    scope.launch { handleGoogleSignIn(account, auth, firestore, navController) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Google Sign-In failed.")
                errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
                loading = false
            }
        } else {
            errorMessage = "Sign-in canceled"
        }
    }

    // ✅ UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to Self Growth Fund",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(onClick = {
                    launcher.launch(googleSignInClient.signInIntent)
                }) {
                    Text("Continue with Google")
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

// ✅ Refactored logic block (clean & coroutine-safe)
private suspend fun handleGoogleSignIn(
    account: GoogleSignInAccount,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    navController: NavController
) {
    try {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnSuccessListener {
            val email = account.email ?: return@addOnSuccessListener
            Timber.i("✅ Signed in as $email")

            // --- Bootstrap Admin ---
            if (email == "selfgrowthf@gmail.com") {
                navController.navigate(Screen.CreatePin.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                return@addOnSuccessListener
            }

            // --- Role-based Access ---
            firestore.collection("shareholders")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val role = query.documents[0].getString("role") ?: "Unknown"
                        Timber.i("User role = $role")
                        navController.navigate(Screen.CreatePin.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        Timber.w("Access denied: user not found in shareholders.")
                        navController.navigate(Screen.AccessDenied.route)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Error verifying user role: ${e.message}")
                }
        }.addOnFailureListener {
            Timber.e(it, "Firebase authentication failed.")
        }
    } catch (e: Exception) {
        Timber.e(e, "Google authentication error.")
    }
}
