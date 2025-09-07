package com.selfgrowthfund.sgf.ui.deposits

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.selfgrowthfund.sgf.di.DepositViewModelFactoryEntryPoint
import com.selfgrowthfund.sgf.model.enums.MemberRole
import dagger.hilt.android.EntryPointAccessors

@Composable
fun DepositEntryRoute(
    currentUserRole: MemberRole,
    shareholderId: String,
    shareholderName: String,
    lastDepositId: String?,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = EntryPointAccessors.fromApplication(
        context.applicationContext,
        DepositViewModelFactoryEntryPoint::class.java
    ).depositViewModelFactory()

    AddDepositScreen(
        currentUserRole = currentUserRole,
        shareholderId = shareholderId,
        shareholderName = shareholderName,
        lastDepositId = lastDepositId,
        onSaveSuccess = onSaveSuccess,
        factory = factory,
        modifier = modifier
    )
}

@Composable
fun ShareholderDetailsScreen() {
    val context = LocalContext.current
    val showAddDeposit = remember { mutableStateOf(false) }

    Button(onClick = { showAddDeposit.value = true }) {
        Text("Add Deposit")
    }

    if (showAddDeposit.value) {
        DepositEntryRoute(
            currentUserRole = MemberRole.MEMBER_ADMIN,
            shareholderId = "SH123",
            shareholderName = "Sajid",
            lastDepositId = "DPT045",
            onSaveSuccess = {
                showAddDeposit.value = false
                Toast.makeText(context, "Deposit saved!", Toast.LENGTH_SHORT).show()
            },
        )
    }
}