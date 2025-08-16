package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.deposits.AddDepositScreen
import com.selfgrowthfund.sgf.ui.deposits.DepositViewModelFactory
import com.selfgrowthfund.selfgrowthfund.sgf.ui.theme.SelfGrowthFundTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var depositViewModelFactory: DepositViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SelfGrowthFundTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddDepositScreen(
                        role = MemberRole.MEMBER_ADMIN,
                        shareholderId = "SH001",
                        shareholderName = "Ayesha",
                        lastDepositId = "D0023",
                        onBack = { finish() },
                        factory = depositViewModelFactory,
                        modifier = Modifier.padding(innerPadding) // ✅ Pass padding
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SelfGrowthFundTheme {
        Greeting("Android")
    }
}