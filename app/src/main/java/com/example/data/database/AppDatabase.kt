package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [TransactionEntity::class, AuditLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genelopay_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.transactionDao(), database.auditLogDao())
                }
            }
        }

        private suspend fun populateInitialData(transactionDao: TransactionDao, auditLogDao: AuditLogDao) {
            // Seed audit logs
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "SYSTEM_INITIALIZATION",
                    performedBy = "System Admin",
                    details = "GeneloPay platform initialized with local encryption keys (AES-GCM-256) and TLS 1.3 tunnels."
                )
            )
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "GATEWAY_VERIFICATION",
                    performedBy = "Compliance Officer",
                    details = "Verified APIs for Clickpesa (Tanzania Mobile Money aggregator) and Stripe (International gateways)."
                )
            )

            // Seed corporate transactions for trends
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val txs = listOf(
                TransactionEntity(
                    amount = 1250000.0,
                    currency = "TZS",
                    senderName = "Bakhresa Group Ltd",
                    senderAccount = "ACT-TZ-9821",
                    receiverName = "Safaricom Supplier",
                    receiverAccount = "ACT-KE-7721",
                    gateway = "clickpesa",
                    status = "COMPLETED",
                    timestamp = now - 5 * dayMs,
                    encryptedPayload = "eyJhbGciOiJBMjU2R0NNS0VTIiwidHlwIjoiSldFIn0.encPayloadExample_Clickpesa",
                    signature = "hmac_sig_7f8a9b",
                    riskScore = 15,
                    riskCategory = "LOW"
                ),
                TransactionEntity(
                    amount = 4500.0,
                    currency = "USD",
                    senderName = "Stripe Client Corp",
                    senderAccount = "ACT-US-0012",
                    receiverName = "Bakhresa Group Ltd",
                    receiverAccount = "ACT-TZ-9821",
                    gateway = "stripe",
                    status = "COMPLETED",
                    timestamp = now - 4 * dayMs,
                    encryptedPayload = "eyJhbGciOiJBMjU2R0NNS0VTIiwidHlwIjoiSldFIn0.encPayloadExample_Stripe",
                    signature = "hmac_sig_aa99bc",
                    riskScore = 22,
                    riskCategory = "LOW"
                ),
                TransactionEntity(
                    amount = 850000.0,
                    currency = "TZS",
                    senderName = "Mo Dewji Foundation",
                    senderAccount = "ACT-TZ-5511",
                    receiverName = "Airtel Payee",
                    receiverAccount = "0784992312",
                    gateway = "airtelmoney",
                    status = "COMPLETED",
                    timestamp = now - 3 * dayMs,
                    encryptedPayload = "eyJhbGciOiJBMjU2R0NNS0VTIiwidHlwIjoiSldFIn0.encPayloadExample_Airtel",
                    signature = "hmac_sig_ef22bc",
                    riskScore = 8,
                    riskCategory = "LOW"
                ),
                TransactionEntity(
                    amount = 9200.0,
                    currency = "USD",
                    senderName = "Unknown International LLC",
                    senderAccount = "ACT-CY-4411",
                    receiverName = "Local Merchant TZ",
                    receiverAccount = "ACT-TZ-1122",
                    gateway = "stripe",
                    status = "HOLD_RISK",
                    timestamp = now - 2 * dayMs,
                    encryptedPayload = "eyJhbGciOiJBMjU2R0NNS0VTIiwidHlwIjoiSldFIn0.encPayloadExample_Risk",
                    signature = "hmac_sig_ab8892",
                    riskScore = 78,
                    riskCategory = "HIGH",
                    complianceHoldReason = "Unusual overseas transaction frequency matching shell structure behaviors."
                ),
                TransactionEntity(
                    amount = 3200000.0,
                    currency = "TZS",
                    senderName = "Zantel Bulk Sender",
                    senderAccount = "ACT-ZN-4422",
                    receiverName = "Arusha Retailers",
                    receiverAccount = "0655909012",
                    gateway = "mpesa",
                    status = "COMPLETED",
                    timestamp = now - dayMs,
                    encryptedPayload = "eyJhbGciOiJBMjU2R0NNS0VTIiwidHlwIjoiSldFIn0.encPayloadExample_Mpesa",
                    signature = "hmac_sig_cc2211",
                    riskScore = 12,
                    riskCategory = "LOW"
                )
            )

            txs.forEach { transactionDao.insertTransaction(it) }
        }
    }
}
