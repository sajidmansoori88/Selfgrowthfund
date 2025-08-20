package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.ui.addshareholders.AddShareholderScreen
import com.selfgrowthfund.sgf.ui.editshareholders.EditShareholderScreen
import com.selfgrowthfund.sgf.ui.shareholders.ShareholderListScreen
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SGFTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val userSession: UserSessionViewModel = hiltViewModel()

                    NavHost(navController = navController, startDestination = "shareholderList") {
                        composable("shareholderList") {
                            ShareholderListScreen(navController = navController)
                        }
                        composable("addShareholder") {
                            AddShareholderScreen(
                                viewModel = hiltViewModel(),
                                navController = navController
                            )
                        }
                        composable("editShareholder/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: return@composable
                            EditShareholderScreen(
                                shareholderId = id,
                                viewModel = hiltViewModel(),
                                userSession = userSession,
                                onDone = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}