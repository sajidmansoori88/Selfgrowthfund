package com.selfgrowthfund.sgf.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.selfgrowthfund.sgf.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinEntryScreen(navController: NavHostController) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ✅ Correct order for EncryptedSharedPreferences.create()
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs", // must match CreatePinScreen filename
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val savedPin = sharedPrefs.getString("user_pin", null)

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Enter your PIN", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { it.isDigit() }.take(4) },
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("PIN") }
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = {
                        if (savedPin == null) {
                            error = "No PIN found, please create one"
                        } else if (pin == savedPin) {
                            isLoading = true
                            scope.launch {
                                delay(400) // small delay for animation polish
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.PinEntry.route) { inclusive = true }
                                }
                            }
                        } else {
                            error = "Incorrect PIN"
                        }
                    }) {
                        Text("Unlock")
                    }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
