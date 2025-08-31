package com.selfgrowthfund.sgf.utils.mappers

import com.selfgrowthfund.sgf.data.local.entities.Borrowing

fun Borrowing.toFirestoreMap(): Map<String, Any?> = mapOf(
    "borrowId" to borrowId,
    "shareholderId" to shareholderId,
    "shareholderName" to shareholderName,
    "applicationDate" to applicationDate.toString(),
    "amountRequested" to amountRequested,
    "borrowEligibility" to borrowEligibility,
    "approvedAmount" to approvedAmount,
    "borrowStartDate" to borrowStartDate.toString(),
    "dueDate" to dueDate.toString(),
    "status" to status.label,
    "closedDate" to closedDate?.toString(),
    "notes" to notes,
    "createdBy" to createdBy,
    "createdAt" to createdAt.toString()
)