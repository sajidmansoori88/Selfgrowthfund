package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ApprovalFlowDao
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import java.time.Instant
import javax.inject.Inject

class ApprovalFlowRepository @Inject constructor(
    private val dao: ApprovalFlowDao
) {
    suspend fun recordApproval(flow: ApprovalFlow) {
        dao.insert(flow)
    }

    suspend fun getFlowsByTypeBetween(
        type: ApprovalType,
        start: Instant,
        end: Instant
    ): List<ApprovalFlow> = dao.getFlowsByTypeBetween(type, start, end)

    suspend fun getAllFlowsBetween(
        start: Instant,
        end: Instant
    ): List<ApprovalFlow> = dao.getAllFlowsBetween(start, end)
}
