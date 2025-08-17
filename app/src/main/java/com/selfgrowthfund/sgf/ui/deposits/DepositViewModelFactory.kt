package com.selfgrowthfund.sgf.ui.deposits

import com.selfgrowthfund.sgf.model.enums.MemberRole
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface DepositViewModelFactory {
    fun create(
        @Assisted("role") role: MemberRole,
        @Assisted("shareholderId") shareholderId: String,
        @Assisted("shareholderName") shareholderName: String,
        @Assisted("lastDepositId") lastDepositId: String?
    ): DepositViewModel
}
