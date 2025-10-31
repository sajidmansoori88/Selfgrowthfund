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

@Composable
fun CreatePinScreen(navController: NavHostController) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // ✅ Correct MasterKey builder
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // ✅ Correct argument order for your Security Crypto version (context first)
    val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs", // filename
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Set your 4-digit PIN", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { it.isDigit() }.take(4) },
                    label = { Text("Enter PIN") },
                    visualTransformation = PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it.filter { it.isDigit() }.take(4) },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(Modifier.height(24.dp))

                Button(onClick = {
                    if (pin.length != 4 || pin != confirmPin) {
                        error = "PINs do not match or not 4 digits"
                        return@Button
                    }

                    // ✅ Works correctly — SharedPreferences.edit() is valid now
                    sharedPrefs.edit().putString("user_pin", pin).apply()

                    navController.navigate(Screen.PinEntry.route) {
                        popUpTo(Screen.CreatePin.route) { inclusive = true }
                    }
                }) {
                    Text("Save PIN")
                }

                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
