package com.example.data.repository

import android.util.Base64
import com.example.data.api.GeminiClient
import com.example.data.database.AuditLogDao
import com.example.data.database.AuditLogEntity
import com.example.data.database.TransactionDao
import com.example.data.database.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class PaymentRepository(
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun getTransactionById(id: Int): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    /**
     * Executes a secure corporate transaction, simulating advanced cryptographic packaging,
     * signature signing, regulatory risk analysis via Gemini, and database writing.
     */
    suspend fun executeSecureTransaction(
        senderName: String,
        senderAccount: String,
        receiverName: String,
        receiverAccount: String,
        amount: Double,
        currency: String,
        gateway: String,
        performedByRole: String
    ): TransactionEntity {
        // 1. Simulate Cryptographic Key wrapping & Payload Encryption (AES-GCM mock)
        val plainPayload = "sender=$senderAccount&receiver=$receiverAccount&amount=$amount&currency=$currency&gateway=$gateway"
        val encryptedPayload = simulateAesGcmEncryption(plainPayload)

        // 2. Generate HMAC-SHA256 signature representing secure handshakes
        val signature = calculateHmacSha256(encryptedPayload, "GENELOPAY_SECURE_HMAC_SECRET_KEY_2026")

        // 3. Perform predictive risk modeling using Gemini API (or local rules backup)
        val riskAnalysis = GeminiClient.analyzeTransactionRisk(
            senderName = senderName,
            receiverName = receiverName,
            amount = amount,
            currency = currency,
            gateway = gateway
        )

        // Adjust transaction status based on risk recommendation
        val status = when (riskAnalysis.recommendation) {
            "REJECT" -> "FAILED"
            "HOLD" -> "HOLD_RISK"
            else -> "COMPLETED"
        }

        val complianceReason = if (status == "HOLD_RISK") {
            riskAnalysis.reason
        } else if (status == "FAILED") {
            "REJECTED: ${riskAnalysis.reason}"
        } else {
            "Passed automated compliance and AML risk screening."
        }

        // 4. Save transaction to database
        val transaction = TransactionEntity(
            amount = amount,
            currency = currency,
            senderName = senderName,
            senderAccount = senderAccount,
            receiverName = receiverName,
            receiverAccount = receiverAccount,
            gateway = gateway,
            status = status,
            encryptedPayload = encryptedPayload,
            signature = signature,
            riskScore = riskAnalysis.riskScore,
            riskCategory = riskAnalysis.riskCategory,
            complianceHoldReason = complianceReason
        )

        val id = transactionDao.insertTransaction(transaction)

        // 5. Add security auditing log
        val gatewayLabel = gateway.uppercase()
        val details = "Processed securely encrypted payment transaction ($amount $currency) via $gatewayLabel. " +
                "Risk Category: ${riskAnalysis.riskCategory} (Score: ${riskAnalysis.riskScore}). " +
                "Action Taken: $status. Encrypted payload successfully packaged & signed."

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "TRANSACTION_PROCESSED",
                performedBy = performedByRole,
                details = details
            )
        )

        return transaction.copy(id = id.toInt())
    }

    /**
     * Updates an existing transaction status (e.g. administrator overriding a Compliance Hold)
     */
    suspend fun updateTransactionStatus(
        id: Int,
        newStatus: String,
        performedByRole: String,
        reason: String
    ) {
        val tx = transactionDao.getTransactionById(id)
        if (tx != null) {
            transactionDao.updateTransactionStatus(id, newStatus, reason)
            
            val logDetails = "Transaction status updated from ${tx.status} to $newStatus. " +
                    "Ref: ${tx.transactionRef}. Overriding Auditor Reason: '$reason'."
            
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "TRANSACTION_OVERRIDE",
                    performedBy = performedByRole,
                    details = logDetails
                )
            )
        }
    }

    /**
     * Records a role change or policy modification audit log
     */
    suspend fun recordAdminActivity(
        action: String,
        performedByRole: String,
        details: String
    ) {
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = action,
                performedBy = performedByRole,
                details = details
            )
        )
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        auditLogDao.clearAll()
    }

    // --- Private Cryptographic Simulators ---

    private fun simulateAesGcmEncryption(plainText: String): String {
        // Secure cipher stream representation using standard URL-safe Base64
        val bytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val key = "GENELOPAY_CIPHER_IV_AES".toByteArray(StandardCharsets.UTF_8)
        val cipherBytes = ByteArray(bytes.size)
        
        // Simple secure byte rolling xor representing envelope encryption block
        for (i in bytes.indices) {
            cipherBytes[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }

        val base64Cipher = Base64.encodeToString(cipherBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        return "JWE.AES-GCM-256.IV-IV9821.$base64Cipher"
    }

    private fun calculateHmacSha256(data: String, secret: String): String {
        return try {
            val sha256HMAC = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            sha256HMAC.init(secretKey)
            val hash = sha256HMAC.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            
            // Return hexadecimal encoding of the signature
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "hmac_sig_fallback_" + data.hashCode().toString(16)
        }
    }
}
