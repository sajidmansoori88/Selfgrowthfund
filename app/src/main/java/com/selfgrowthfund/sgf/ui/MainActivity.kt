package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import com.selfgrowthfund.sgf.SGFApp
import com.selfgrowthfund.sgf.ui.theme.SGFTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SGFTheme {
                SGFApp()
            }
        }
    }
}