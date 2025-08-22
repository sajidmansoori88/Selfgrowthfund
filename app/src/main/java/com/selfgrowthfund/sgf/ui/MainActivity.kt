// MainActivity.kt
package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.ui.addshareholders.shareholderNavGraph
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SGFTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "addShareholder" // Start with Add screen
                ) {
                    shareholderNavGraph(navController = navController)
                }
            }
        }
    }
}