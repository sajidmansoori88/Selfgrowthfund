package com.selfgrowthfund.sgf.features.addshareholders.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.selfgrowthfund.sgf.features.addshareholders.ui.AddShareholderScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.shareholderNavGraph() {
    composable("addShareholder") {
        AddShareholderScreen()
    }
}