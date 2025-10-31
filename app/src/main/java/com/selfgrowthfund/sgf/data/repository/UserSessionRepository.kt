package com.selfgrowthfund.sgf.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.selfgrowthfund.sgf.data.local.dao.UserSessionDao
import com.selfgrowthfund.sgf.session.SessionEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.CurrentUser
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserSessionRepository
 *
 * - Provides user session analytics (not synced back to Firestore).
 * - Firestore → Room session sync handled globally by RealtimeSyncRepository.
 * - Room is treated as local analytical cache.
 */
@Singleton
class UserSessionRepository @Inject constructor(
    private val dao: UserSessionDao,
    private val auth: FirebaseAuth,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // Session Analytics
    // =============================

    suspend fun getUserSessions(shareholderNames: Map<String, String>): Result<List<SessionEntry>> = try {
        val raw = dao.getSessionSummary()
        val mapped = raw.mapIndexed { index, s ->
            SessionEntry(
                sr = index + 1,
                shareholderId = s.shareholderId,
                name = shareholderNames[s.shareholderId] ?: "",
                currentMonthSessions = s.currentMonthSessions,
                lifetimeSessions = s.lifetimeSessions
            )
        }
        Result.Success(mapped)
    } catch (e: Exception) {
        Timber.e(e, "Failed to get user sessions")
        Result.Error(e)
    }

    // =============================
    // Current User Context
    // =============================

    fun getCurrentUser(): CurrentUser? {
        val firebaseUser = auth.currentUser ?: return null

        // TODO: Replace hardcoded role lookup with real shareholder-role mapping
        return CurrentUser(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            role = MemberRole.MEMBER_ADMIN
        )
    }

    // =============================
    // Analytics Helpers
    // =============================

    suspend fun getTotalActiveMembers(): Int = withContext(Dispatchers.IO) {
        try {
            dao.countActiveMembers()
        } catch (e: Exception) {
            Timber.e(e, "Failed to count active members")
            0
        }
    }

    // =============================
    // Optional: Trigger sync manually (for future use)
    // =============================

    suspend fun syncSessionsFromFirestore(): Result<Unit> = try {
        // Firestore → Room handled automatically by RealtimeSyncRepository
        realtimeSyncRepository.pushAllUnsynced() // no effect, just placeholder consistency
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
