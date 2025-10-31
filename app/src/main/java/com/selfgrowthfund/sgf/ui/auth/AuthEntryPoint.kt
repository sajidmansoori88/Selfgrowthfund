package com.selfgrowthfund.sgf.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.selfgrowthfund.sgf.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun AuthEntryPoint(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var nextDestination by remember { mutableStateOf<String?>(null) }

    // ✅ Check encrypted prefs for saved PIN
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val storedPin = sharedPrefs.getString("user_pin", null)

    // 🔄 Decide where to go next
    LaunchedEffect(Unit) {
        delay(600) // splash-style delay for smoother transition
        nextDestination = when {
            user == null -> Screen.Login.route
            storedPin.isNullOrEmpty() -> Screen.CreatePin.route
            else -> Screen.PinEntry.route
        }
    }

    // 🧭 Navigate when destination is ready
    LaunchedEffect(nextDestination) {
        nextDestination?.let {
            navController.navigate(it) {
                popUpTo(Screen.AuthEntry.route) { inclusive = true }
            }
        }
    }

    // 🧱 Minimal placeholder UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "Checking credentials...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 120.dp)
        )
    }
}
