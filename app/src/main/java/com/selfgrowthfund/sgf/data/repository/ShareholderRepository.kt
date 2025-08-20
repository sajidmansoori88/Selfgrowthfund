package com.selfgrowthfund.sgf.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject


class ShareholderRepository @Inject constructor(
    private val dao: ShareholderDao,
    private val dates: Dates,
    private val firestore: FirebaseFirestore
) {

    // Reactive streams
    fun getAllShareholdersStream(): Flow<List<Shareholder>> = dao.getAllShareholdersFlow()
    fun getShareholderByIdStream(id: String): Flow<Shareholder?> = dao.getShareholderByIdFlow(id)

    // Direct access
    suspend fun getShareholderById(id: String): Shareholder? = dao.getShareholderById(id)

    suspend fun searchShareholders(query: String): List<Shareholder> =
        dao.searchShareholders("%$query%")

    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.insertShareholder(shareholder.withTimestamps())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> = try {
        dao.updateShareholder(shareholder.copy(updatedAt = Date(dates.now()))) // ✅ Convert Long → Date
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteShareholderById(id: String): Result<Unit> = try {
        dao.getShareholderById(id)?.let {
            dao.deleteShareholder(it)
            Result.Success(Unit)
        } ?: Result.Error(Exception("Shareholder not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getLastShareholderId(): String? = dao.getLastId()

    // 🔄 Firestore sync
    suspend fun syncShareholderToFirestore(input: ShareholderEntry): Result<Unit> = try {
        val now = dates.now()
        val doc = firestore.collection("shareholder").document()
        val data = mapOf(
            "name" to input.fullName,
            "dob" to dates.format(input.dob),
            "joiningDate" to dates.format(input.joiningDate),
            "mobileNumber" to input.mobileNumber,
            "email" to input.email,
            "role" to input.role,
            "createdAt" to now,
            "createdAtFormatted" to dates.format(now),
            "uid" to null
        )
        doc.set(data).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Helper
    private fun Shareholder.withTimestamps(): Shareholder =
        copy(createdAt = Date(dates.now()), updatedAt = Date(dates.now()))
}