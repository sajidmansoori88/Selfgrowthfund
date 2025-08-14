package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.selfgrowthfund.sgf.ui.theme.SelfGrowthFundTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity launched")

        setContent {
            SelfGrowthFundTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    Text(
        text = "Welcome to SelfGrowthFund!",
        style = MaterialTheme.typography.headlineMedium
    )
}