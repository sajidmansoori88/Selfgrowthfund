package com.selfgrowthfund.sgf.ui.auth

import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController

@Composable
fun BiometricSetupScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val executor = ContextCompat.getMainExecutor(context)

    var biometricAvailable by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (activity == null) {
            Log.e("Biometric", "Context is not a FragmentActivity: ${context::class.java.name}")
            errorMessage = "Biometric setup failed: invalid activity context"
            return@LaunchedEffect
        }

        val manager = BiometricManager.from(context)
        biometricAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    BiometricManager.BIOMETRIC_SUCCESS
        } else {
            manager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }

        if (biometricAvailable) {
            val promptInfo = PromptInfo.Builder()
                .setTitle("Login with Biometrics")
                .setSubtitle("Authenticate to access SGF")
                .setNegativeButtonText("Use PIN")
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        navController.navigate("home")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        errorMessage = errString.toString()
                    }

                    override fun onAuthenticationFailed() {
                        errorMessage = "Biometric authentication failed"
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!biometricAvailable) {
            Text("Biometric authentication not available on this device.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("pin_entry") }) {
                Text("Use PIN Instead")
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("pin_entry") }) {
                Text("Use PIN Instead")
            }
        }
    }
}