package com.selfgrowthfund.sgf.ui.deposits

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.di.DepositViewModelFactoryEntryPoint
import com.selfgrowthfund.sgf.model.enums.MemberRole
import dagger.hilt.android.EntryPointAccessors

@Composable
fun DepositEntryLauncher(
    shareholderId: String,
    shareholderName: String,
    lastDepositId: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showForm = rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = { showForm.value = true }) {
            Text("Add Deposit")
        }

        if (showForm.value) {
            DepositEntryRoute(
                currentUserRole = MemberRole.MEMBER_ADMIN,
                shareholderId = shareholderId,
                shareholderName = shareholderName,
                lastDepositId = lastDepositId,
                onSaveSuccess = {
                    showForm.value = false
                    Toast.makeText(context, "Deposit saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}