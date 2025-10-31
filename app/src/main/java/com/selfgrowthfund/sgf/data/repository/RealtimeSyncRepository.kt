// RealtimeSyncRepository.kt
package com.selfgrowthfund.sgf.data.repository

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.*
import com.selfgrowthfund.sgf.model.enums.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.forEach
import kotlin.runCatching

/**
 * RealtimeFirestore <-> Room synchronizer.
 *
 * - startRealtimeSync() registers snapshot listeners for collections.
 * - stopRealtimeSync() removes them.
 * - pushAllUnsynced() pushes local rows with isSynced == false to Firestore.
 *
 * NOTE: Adjust DAO method names (getByProvisionalId / insert / update / deleteByProvisionalId) if needed.
 */
@Singleton
class RealtimeSyncRepository @Inject constructor(
    private val borrowingDao: BorrowingDao,
    private val repaymentDao: RepaymentDao,
    private val depositDao: DepositDao,
    private val investmentDao: InvestmentDao,
    private val returnsDao: InvestmentReturnsDao,
    private val shareholderDao: ShareholderDao,
    private val approvalFlowDao: ApprovalFlowDao,
    private val actionItemDao: ActionItemDao,
    private val penaltyDao: PenaltyDao,
    private val otherExpenseDao: OtherExpenseDao,
    private val otherIncomeDao: OtherIncomeDao,
    private val userSessionDao: UserSessionDao,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {

    // scope for background coroutines
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // registrations to remove listeners later
    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        // Enable offline persistence (recommended)
        try {
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        } catch (t: Throwable) {
            Timber.w(t, "Could not enable Firestore persistence (may be already set).")
        }
    }

    /** Start realtime listeners for all collections. */
    fun startRealtimeSync() {
        Timber.i("RealtimeSyncRepository: starting realtime listeners")

        listeners += firestore.collection("borrowings")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "borrowings listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Borrowing::class.java)
                }
            }

        listeners += firestore.collection("repayments")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "repayments listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Repayment::class.java)
                }
            }

        listeners += firestore.collection("deposits")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "deposits listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Deposit::class.java)
                }
            }

        listeners += firestore.collection("investments")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "investments listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Investment::class.java)
                }
            }

        listeners += firestore.collection("investmentReturns")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "investmentReturns listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, TransactionType.InvestmentReturn::class.java)
                }
            }

        listeners += firestore.collection("shareholders")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "shareholders listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Shareholder::class.java)
                }
            }

        listeners += firestore.collection("approvals")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "approvals listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, ApprovalFlow::class.java)
                }
            }

        listeners += firestore.collection("actionItems")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "actionItems listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, ActionItem::class.java)
                }
            }

        listeners += firestore.collection("penalties")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "penalties listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, Penalty::class.java)
                }
            }

        listeners += firestore.collection("expenses")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "expenses listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, OtherExpense::class.java)
                }
            }

        listeners += firestore.collection("incomes")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "incomes listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, OtherIncome::class.java)
                }
            }

        // sessions collection — we can treat it read-only (analytics) or insert into Room if you want.
        listeners += firestore.collection("sessions")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Timber.e(err, "sessions listener error")
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach {
                    handleDocumentChange(it.type, it.document.data, UserSessionHistory::class.java)
                }
            }
    }

    /** Stop listening (call on app exit or sign out) */
    fun stopRealtimeSync() {
        Timber.i("RealtimeSyncRepository: stopping listeners")
        listeners.forEach { runCatching { it.remove() } }
        listeners.clear()
    }

    // ------------------------------
    // Generic handler: Map -> entity class -> call appropriate DAO upsert/delete
    // ------------------------------
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> handleDocumentChange(type: DocumentChange.Type, data: Map<String, Any>, clazz: Class<T>) {
        scope.launch {
            try {
                val json = gson.toJson(data)
                val remote = gson.fromJson(json, clazz)

                when (clazz) {
                    Borrowing::class.java -> handleBorrowingChange(type, remote as Borrowing)
                    Repayment::class.java -> handleRepaymentChange(type, remote as Repayment)
                    Deposit::class.java -> handleDepositChange(type, remote as Deposit)
                    Investment::class.java -> handleInvestmentChange(type, remote as Investment)
                    TransactionType.InvestmentReturn::class.java -> handleReturnChange(type, remote as InvestmentReturns)
                    Shareholder::class.java -> handleShareholderChange(type, remote as Shareholder)
                    ApprovalFlow::class.java -> handleApprovalChange(type, remote as ApprovalFlow)
                    ActionItem::class.java -> handleActionItemChange(type, remote as ActionItem)
                    Penalty::class.java -> handlePenaltyChange(type, remote as Penalty)
                    OtherExpense::class.java -> handleOtherExpenseChange(type, remote as OtherExpense)
                    OtherIncome::class.java -> handleOtherIncomeChange(type, remote as OtherIncome)
                    UserSessionHistory::class.java -> handleUserSessionChange(type, remote as UserSessionHistory)
                    else -> Timber.w("Unhandled class in generic handler: ${clazz.name}")
                }
            } catch (t: Throwable) {
                Timber.e(t, "handleDocumentChange failed for ${clazz.simpleName}")
            }
        }
    }

    // ------------------------------
    // Handlers per entity (remote -> local)
    // Use defensive DAO calls; adapt DAO method names if required (TODO markers)
    // ------------------------------

    private suspend fun handleBorrowingChange(type: DocumentChange.Type, remote: Borrowing) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching {
                        borrowingDao.getBorrowingById(remote.borrowId ?: "")
                    }.getOrNull() ?: runCatching {
                        borrowingDao.getByProvisionalId(remote.provisionalId)
                    }.getOrNull()

                    if (local == null) {
                        borrowingDao.insertBorrowing(remote.copy(isSynced = true))
                    } else {
                        val localTs = local.updatedAt
                        val remoteTs = remote.updatedAt
                        if (remoteTs >= localTs) borrowingDao.updateBorrowing(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { borrowingDao.deleteBorrowing(remote) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleBorrowingChange error")
        }
    }

    private suspend fun handleRepaymentChange(type: DocumentChange.Type, remote: Repayment) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching {
                        repaymentDao.getByProvisionalId(remote.provisionalId)
                    }.getOrNull()

                    if (local == null) {
                        repaymentDao.insert(remote.copy(isSynced = true))
                    } else {
                        val localTs = local.updatedAt
                        val remoteTs = remote.updatedAt
                        if (remoteTs >= localTs) repaymentDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { repaymentDao.deleteByProvisionalId(remote.provisionalId) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleRepaymentChange error")
        }
    }

    private suspend fun handleDepositChange(type: DocumentChange.Type, remote: Deposit) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching { depositDao.getByProvisionalId(remote.provisionalId) }.getOrNull()
                    if (local == null) depositDao.insert(remote.copy(isSynced = true))
                    else {
                        val remoteTs = remote.updatedAt
                        val localTs = local.updatedAt
                        if (remoteTs >= localTs) depositDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    // Optional: implement a delete in DAO if needed
                    Timber.w("Deposit REMOVED from Firestore but no local delete implemented.")
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleDepositChange error")
        }
    }

    private suspend fun handleInvestmentChange(type: DocumentChange.Type, remote: Investment) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching {
                        investmentDao.getByProvisionalId(remote.provisionalId)
                    }.getOrNull()

                    if (local == null) {
                        investmentDao.insert(remote.copy(isSynced = true))
                    } else {
                        val remoteTs = remote.updatedAt
                        val localTs = local.updatedAt
                        if (remoteTs >= localTs) investmentDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { investmentDao.deleteByProvisionalId(remote.provisionalId) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleInvestmentChange error")
        }
    }

    private suspend fun handleReturnChange(type: DocumentChange.Type, remote: InvestmentReturns) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching { returnsDao.getByProvisionalId(remote.provisionalId) }.getOrNull()
                    if (local == null) returnsDao.insertAll(listOf(remote.copy(isSynced = true)))
                    else {
                        val remoteTs = remote.updatedAt
                        val localTs = local.updatedAt
                        if (remoteTs >= localTs) returnsDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> { /* optional delete */ }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleReturnChange error")
        }
    }

    private suspend fun handleShareholderChange(type: DocumentChange.Type, remote: Shareholder) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val local = runCatching { shareholderDao.getShareholderById(remote.shareholderId) }.getOrNull()
                    if (local == null) shareholderDao.insertShareholder(remote.copy(isSynced = true))
                    else {
                        val remoteTs = remote.updatedAt.toEpochMilli() ?: 0L
                        val localTs = local.updatedAt?.toEpochMilli() ?: 0L
                        if (remoteTs >= localTs) shareholderDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> runCatching { shareholderDao.deleteById(remote.shareholderId) }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleShareholderChange error")
        }
    }

    private suspend fun handleApprovalChange(type: DocumentChange.Type, remote: ApprovalFlow) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val localList = runCatching { approvalFlowDao.getAllApprovalsOnce() }.getOrNull().orEmpty()
                    val local = localList.find { it.id == remote.id }

                    if (local == null) {
                        approvalFlowDao.insertAll(listOf(remote.copy(isSynced = true)))
                    } else {
                        approvalFlowDao.update(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    // Optional: no delete defined in DAO
                    Timber.w("ApprovalFlow REMOVED from Firestore; no local delete implemented.")
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleApprovalChange error")
        }
    }

    private suspend fun handleActionItemChange(type: DocumentChange.Type, remote: ActionItem) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    // no getByProvisionalId — rely on full replace
                    val unsynced = actionItemDao.getUnsynced()
                    val local = unsynced.find { it.actionId == remote.actionId }

                    if (local == null) {
                        actionItemDao.insertAction(remote.copy(isSynced = true))
                    } else {
                        actionItemDao.updateAction(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { actionItemDao.deleteAction(remote) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleActionItemChange error")
        }
    }

    private suspend fun handlePenaltyChange(type: DocumentChange.Type, remote: Penalty) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    // Simple: check if already exists in unsynced, then upsert
                    val unsynced = penaltyDao.getUnsynced()
                    val local = unsynced.find { it.provisionalId == remote.provisionalId }

                    if (local == null) {
                        penaltyDao.insert(remote.copy(isSynced = true))
                    } else {
                        penaltyDao.insertAll(listOf(remote.copy(isSynced = true))) // overwrite
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    // optional delete — no delete function in DAO
                    Timber.w("Penalty REMOVED remotely; local delete skipped (no DAO delete).")
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handlePenaltyChange error")
        }
    }

    private suspend fun handleOtherExpenseChange(type: DocumentChange.Type, remote: OtherExpense) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val unsynced = otherExpenseDao.getUnsynced()
                    val local = unsynced.find { it.id == remote.id }

                    if (local == null) {
                        otherExpenseDao.insertExpense(remote.copy(isSynced = true))
                    } else {
                        otherExpenseDao.updateExpense(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { otherExpenseDao.deleteExpense(remote) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleOtherExpenseChange error")
        }
    }

    private suspend fun handleOtherIncomeChange(type: DocumentChange.Type, remote: OtherIncome) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    val unsynced = otherIncomeDao.getUnsynced()
                    val local = unsynced.find { it.id == remote.id }

                    if (local == null) {
                        otherIncomeDao.insertIncome(remote.copy(isSynced = true))
                    } else {
                        otherIncomeDao.updateIncome(remote.copy(isSynced = true))
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    runCatching { otherIncomeDao.deleteIncome(remote) }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleOtherIncomeChange error")
        }
    }

    private suspend fun handleUserSessionChange(type: DocumentChange.Type, remote: UserSessionHistory) {
        try {
            when (type) {
                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                    // For now, treat sessions as analytics only — not inserted back into local DB
                    Timber.i("User session ${remote.shareholderId} synced from Firestore (ignored locally).")
                }
                DocumentChange.Type.REMOVED -> {
                    Timber.i("User session removed remotely; ignoring locally.")
                }
            }
        } catch (t: Throwable) {
            Timber.e(t, "handleUserSessionChange error")
        }
    }

    // ------------------------------
    // Local -> Remote push
    // ------------------------------

    /**
     * Push all unsynced rows for every DAO.
     * Call this after local writes or schedule it periodically.
     */
    fun pushAllUnsynced() {
        scope.launch {
            pushBorrowingsUnsynced()
            pushRepaymentsUnsynced()
            pushDepositsUnsynced()
            pushInvestmentsUnsynced()
            pushReturnsUnsynced()
            pushShareholdersUnsynced()
            pushApprovalsUnsynced()
            pushActionItemsUnsynced()
            pushPenaltiesUnsynced()
            pushOtherExpensesUnsynced()
            pushOtherIncomesUnsynced()
            // sessions are typically write-only to Firestore by the app; you may not push them from Room
        }
    }

    private suspend fun pushBorrowingsUnsynced() {
        runCatching {
            val unsynced = borrowingDao.getUnsynced()
            unsynced.forEach { b ->
                val id = b.borrowId?.takeIf { it.isNotBlank() } ?: b.provisionalId
                try {
                    firestore.collection("borrowings").document(id).set(b).await()
                    borrowingDao.updateBorrowing(b.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushBorrowings failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushBorrowingsUnsynced failed overall") }
    }

    private suspend fun pushRepaymentsUnsynced() {
        runCatching {
            val unsynced = repaymentDao.getUnsynced()
            unsynced.forEach { r ->
                val id = r.repaymentId?.takeIf { it.isNotBlank() } ?: r.provisionalId
                try {
                    firestore.collection("repayments").document(id).set(r).await()
                    repaymentDao.update(r.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushRepayments failed for ${r.provisionalId}")
                }
            }
        }.onFailure { Timber.e(it, "pushRepaymentsUnsynced failed overall") }
    }

    private suspend fun pushDepositsUnsynced() {
        runCatching {
            val unsynced = depositDao.getUnsynced()
            unsynced.forEach { d ->
                val id = d.depositId?.takeIf { it.isNotBlank() } ?: d.provisionalId
                try {
                    firestore.collection("deposits").document(id).set(d).await()
                    depositDao.update(d.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushDeposits failed for ${d.provisionalId}")
                }
            }
        }.onFailure { Timber.e(it, "pushDepositsUnsynced failed overall") }
    }

    private suspend fun pushInvestmentsUnsynced() {
        runCatching {
            val unsynced = investmentDao.getUnsynced()
            unsynced.forEach { inv ->
                val id = inv.investmentId?.takeIf { it.isNotBlank() } ?: inv.provisionalId
                try {
                    firestore.collection("investments").document(id).set(inv).await()
                    investmentDao.update(inv.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushInvestments failed for ${inv.provisionalId}")
                }
            }
        }.onFailure { Timber.e(it, "pushInvestmentsUnsynced failed overall") }
    }

    private suspend fun pushReturnsUnsynced() {
        runCatching {
            val unsynced = returnsDao.getUnsynced()
            unsynced.forEach { ret ->
                val id = ret.returnId?.takeIf { it.isNotBlank() } ?: ret.provisionalId
                try {
                    firestore.collection("investmentReturns").document(id).set(ret).await()
                    returnsDao.update(ret.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushReturns failed for ${ret.provisionalId}")
                }
            }
        }.onFailure { Timber.e(it, "pushReturnsUnsynced failed overall") }
    }

    private suspend fun pushShareholdersUnsynced() {
        runCatching {
            val unsynced = shareholderDao.getUnsynced()
            unsynced.forEach { s ->
                val id = s.shareholderId
                try {
                    firestore.collection("shareholders").document(id).set(s).await()
                    shareholderDao.update(s.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushShareholders failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushShareholdersUnsynced failed overall") }
    }

    private suspend fun pushApprovalsUnsynced() {
        runCatching {
            val unsynced = approvalFlowDao.getUnsynced()
            unsynced.forEach { a ->
                val id = a.id?.toString() ?: return@forEach
                try {
                    firestore.collection("approvals").document(id).set(a).await()
                    approvalFlowDao.update(a.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushApprovals failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushApprovalsUnsynced failed overall") }
    }

    private suspend fun pushActionItemsUnsynced() {
        runCatching {
            val unsynced = actionItemDao.getUnsynced()
            unsynced.forEach { ai ->
                val id = ai.actionId
                if (id.isNullOrBlank()) return@forEach
                try {
                    firestore.collection("actionItems").document(id).set(ai).await()
                    actionItemDao.updateAction(ai.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushActionItems failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushActionItemsUnsynced failed overall") }
    }

    private suspend fun pushPenaltiesUnsynced() {
        runCatching {
            val unsynced = penaltyDao.getUnsynced()
            unsynced.forEach { p ->
                val id = p.provisionalId ?: p.id?.toString()
                if (id.isNullOrBlank()) return@forEach
                try {
                    firestore.collection("penalties").document(id).set(p).await()
                    penaltyDao.insertAll(listOf(p.copy(isSynced = true)))
                } catch (t: Throwable) {
                    Timber.e(t, "pushPenalties failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushPenaltiesUnsynced failed overall") }
    }

    private suspend fun pushOtherExpensesUnsynced() {
        runCatching {
            val unsynced = otherExpenseDao.getUnsynced()
            unsynced.forEach { e ->
                val id = e.id?.toString() ?: e.provisionalId
                if (id.isNullOrBlank()) return@forEach
                try {
                    firestore.collection("expenses").document(id).set(e).await()
                    otherExpenseDao.updateExpense(e.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushOtherExpenses failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushOtherExpensesUnsynced failed overall") }
    }

    private suspend fun pushOtherIncomesUnsynced() {
        runCatching {
            val unsynced = otherIncomeDao.getUnsynced()
            unsynced.forEach { i ->
                val id = i.id?.toString() ?: i.provisionalId
                if (id.isNullOrBlank()) return@forEach
                try {
                    firestore.collection("incomes").document(id).set(i).await()
                    otherIncomeDao.updateIncome(i.copy(isSynced = true))
                } catch (t: Throwable) {
                    Timber.e(t, "pushOtherIncomes failed for $id")
                }
            }
        }.onFailure { Timber.e(it, "pushOtherIncomesUnsynced failed overall") }
    }

    // You can add fine-grained pushX(entity) functions if you prefer to call per-save instead of pushAllUnsynced()
}
