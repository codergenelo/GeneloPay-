package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionRef: String = "TXN-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
    val amount: Double,
    val currency: String, // TZS, USD, EUR
    val senderName: String,
    val senderAccount: String,
    val receiverName: String,
    val receiverAccount: String,
    val gateway: String, // clickpesa, stripe, mpesa, tigopesa, airtelmoney
    val status: String, // PENDING, COMPLETED, FAILED, HOLD_RISK
    val timestamp: Long = System.currentTimeMillis(),
    val encryptedPayload: String, // Simulated encrypted JWE payload
    val signature: String, // Simulated cryptographic HMAC-SHA256 signature
    val riskScore: Int, // 0 to 100
    val riskCategory: String, // LOW, MEDIUM, HIGH
    val complianceHoldReason: String? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // e.g., ROLE_CHANGED, GATEWAY_TOGGLED, POLICY_UPDATED, TRANS_OVERRIDE
    val performedBy: String, // Current user role or name
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
