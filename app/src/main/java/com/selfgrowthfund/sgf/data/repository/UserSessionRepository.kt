package com.selfgrowthfund.sgf.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.selfgrowthfund.sgf.data.local.dao.UserSessionDao
import com.selfgrowthfund.sgf.session.SessionEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.CurrentUser
import timber.log.Timber
import javax.inject.Inject

class UserSessionRepository @Inject constructor(
    private val dao: UserSessionDao,
    private val auth: FirebaseAuth
) {
    suspend fun getUserSessions(shareholderNames: Map<String, String>): List<SessionEntry> {
        val raw = dao.getSessionSummary()
        return raw.mapIndexed { index, s ->
            SessionEntry(
                sr = index + 1,
                shareholderId = s.shareholderId,
                name = shareholderNames[s.shareholderId] ?: "",
                currentMonthSessions = s.currentMonthSessions,
                lifetimeSessions = s.lifetimeSessions
            )
        }
    }

    fun getCurrentUser(): CurrentUser? {
        val firebaseUser = auth.currentUser ?: return null

        // TODO: replace this mock mapping with real shareholder/role lookup
        return CurrentUser(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            role = MemberRole.MEMBER_ADMIN // 🔑 TEMP: assign based on your business rules
        )
    }

    // ✅ FIXED FUNCTION
    suspend fun getTotalActiveMembers(): Int {
        return try {
            dao.countActiveMembers()
        } catch (e: Exception) {
            Timber.e(e, "Failed to count active members")
            0
        }
    }
}
