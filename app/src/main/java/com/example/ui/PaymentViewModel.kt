package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.AuditLogEntity
import com.example.data.database.TransactionEntity
import com.example.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PaymentRepository(database.transactionDao(), database.auditLogDao())

    // UI Navigation State
    var currentScreen by mutableStateOf("Dashboard")
        private set

    // Current User Session State
    var currentUserRole by mutableStateOf("Business Owner") // Business Owner, Compliance Officer, Finance Manager, Admin
        private set

    // Selected Transaction for Detail Modal
    var selectedTransaction by mutableStateOf<TransactionEntity?>(null)

    // Flows for reactive UI updates from Room
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Platform Security & Operational Settings
    var settingsEncryptedPayloadEnabled by mutableStateOf(true)
    var settingsAmlShieldEnabled by mutableStateOf(true)
    var settingsMaxDailyLimitTzs by mutableStateOf(10000000.0)
    var settingsSandboxModeEnabled by mutableStateOf(true)

    // Send Fund Form States
    var formAmount by mutableStateOf("")
    var formCurrency by mutableStateOf("TZS") // TZS, USD, EUR
    var formSenderName by mutableStateOf("Afritech Solutions Ltd")
    var formSenderAccount by mutableStateOf("ACT-TZ-4901")
    var formReceiverName by mutableStateOf("")
    var formReceiverAccount by mutableStateOf("")
    var formGateway by mutableStateOf("clickpesa") // clickpesa, stripe, mpesa, tigopesa, airtelmoney

    // Predictive Risk Modeling Playground State
    var riskPlaygroundAmount by mutableStateOf("250000")
    var riskPlaygroundCurrency by mutableStateOf("TZS")
    var riskPlaygroundSender by mutableStateOf("Alizeti Oil Corp")
    var riskPlaygroundReceiver by mutableStateOf("Sub-saharan Supply")
    var riskPlaygroundGateway by mutableStateOf("clickpesa")

    var riskPlaygroundIsAnalyzing by mutableStateOf(false)
    var riskPlaygroundResult by mutableStateOf<GeminiClient.RiskAnalysisResult?>(null)

    // Async Loading State for Form Submission
    var isSubmittingPayment by mutableStateOf(false)
    var submissionSuccessMessage by mutableStateOf<String?>(null)
    var submissionErrorMessage by mutableStateOf<String?>(null)

    // Key configuration indicator
    val isGeminiKeyConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    fun navigateTo(screen: String) {
        currentScreen = screen
        submissionSuccessMessage = null
        submissionErrorMessage = null
    }

    fun changeUserRole(role: String) {
        val oldRole = currentUserRole
        currentUserRole = role
        viewModelScope.launch {
            repository.recordAdminActivity(
                action = "ROLE_CHANGED",
                performedByRole = oldRole,
                details = "User role switched from '$oldRole' to '$role' in system session."
            )
        }
    }

    fun submitPayment() {
        val amountVal = formAmount.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            submissionErrorMessage = "Please enter a valid transfer amount."
            return
        }
        if (formReceiverName.isBlank() || formReceiverAccount.isBlank()) {
            submissionErrorMessage = "Please provide complete receiver information."
            return
        }

        isSubmittingPayment = true
        submissionSuccessMessage = null
        submissionErrorMessage = null

        viewModelScope.launch {
            try {
                val tx = repository.executeSecureTransaction(
                    senderName = formSenderName,
                    senderAccount = formSenderAccount,
                    receiverName = formReceiverName,
                    receiverAccount = formReceiverAccount,
                    amount = amountVal,
                    currency = formCurrency,
                    gateway = formGateway,
                    performedByRole = currentUserRole
                )

                isSubmittingPayment = false
                if (tx.status == "HOLD_RISK") {
                    submissionSuccessMessage = "Transaction initiated successfully, but placed on COMPLIANCE HOLD for risk modeling review."
                } else if (tx.status == "FAILED") {
                    submissionErrorMessage = "Transaction REJECTED by automated AML and compliance risk engine."
                } else {
                    submissionSuccessMessage = "Transaction executed successfully and reconciled in real-time."
                }

                // Clear form
                formAmount = ""
                formReceiverName = ""
                formReceiverAccount = ""
            } catch (e: Exception) {
                isSubmittingPayment = false
                submissionErrorMessage = "Security / network failure during gateway handshake: ${e.message}"
            }
        }
    }

    fun runPredictiveRiskModeling() {
        val amountVal = riskPlaygroundAmount.toDoubleOrNull() ?: 100.0
        riskPlaygroundIsAnalyzing = true
        riskPlaygroundResult = null

        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeTransactionRisk(
                    senderName = riskPlaygroundSender,
                    receiverName = riskPlaygroundReceiver,
                    amount = amountVal,
                    currency = riskPlaygroundCurrency,
                    gateway = riskPlaygroundGateway
                )
                riskPlaygroundResult = result
                riskPlaygroundIsAnalyzing = false

                repository.recordAdminActivity(
                    action = "PREDICTIVE_RISK_MODEL_RUN",
                    performedByRole = currentUserRole,
                    details = "Ran predictive risk model query for sender: '$riskPlaygroundSender', amount: '$amountVal $riskPlaygroundCurrency'. Resulting Category: ${result.riskCategory}."
                )
            } catch (e: Exception) {
                riskPlaygroundIsAnalyzing = false
                // Local fallback
                riskPlaygroundResult = GeminiClient.RiskAnalysisResult(
                    riskScore = 50,
                    riskCategory = "MEDIUM",
                    reason = "Predictive query error: ${e.message}. Heuristic rule applies standard security buffers.",
                    recommendation = "HOLD"
                )
            }
        }
    }

    fun adminOverrideStatus(transactionId: Int, approve: Boolean, auditorComment: String) {
        val newStatus = if (approve) "COMPLETED" else "FAILED"
        viewModelScope.launch {
            repository.updateTransactionStatus(
                id = transactionId,
                newStatus = newStatus,
                performedByRole = currentUserRole,
                reason = auditorComment
            )
            // Refresh detail modal reference if active
            selectedTransaction?.let { tx ->
                if (tx.id == transactionId) {
                    selectedTransaction = repository.getTransactionById(transactionId)
                }
            }
        }
    }

    fun toggleEncryptedPayloadSetting(enabled: Boolean) {
        settingsEncryptedPayloadEnabled = enabled
        viewModelScope.launch {
            repository.recordAdminActivity(
                action = "SECURITY_POLICY_UPDATE",
                performedByRole = currentUserRole,
                details = "Encrypted JWE transaction payload wrapping toggled to: $enabled."
            )
        }
    }

    fun toggleAmlShieldSetting(enabled: Boolean) {
        settingsAmlShieldEnabled = enabled
        viewModelScope.launch {
            repository.recordAdminActivity(
                action = "COMPLIANCE_POLICY_UPDATE",
                performedByRole = currentUserRole,
                details = "Automated Anti-Money Laundering Shield policy toggled to: $enabled."
            )
        }
    }

    fun saveOperationalSettings() {
        viewModelScope.launch {
            repository.recordAdminActivity(
                action = "SYSTEM_SETTINGS_UPDATE",
                performedByRole = currentUserRole,
                details = "Updated daily operational limits to: $settingsMaxDailyLimitTzs TZS. Sandbox state: $settingsSandboxModeEnabled."
            )
        }
    }

    fun resetAppSandbox() {
        viewModelScope.launch {
            repository.clearAllData()
            // DB callback will trigger, repopulating defaults
        }
    }
}
