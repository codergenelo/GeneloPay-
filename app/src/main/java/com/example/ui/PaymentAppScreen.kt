package com.example.ui

import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AuditLogEntity
import com.example.data.database.TransactionEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAppScreen(
    viewModel: PaymentViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FintechHeaderBg,
                    titleContentColor = Color.White,
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = FintechBorder,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "GENELOPAY ADMIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechPrimary,
                                letterSpacing = 1.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ColorSuccess)
                                )
                                Text(
                                    text = "Tanzania Hub",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                actions = {
                    // API Status Chip
                    Row(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (viewModel.isGeminiKeyConfigured) ColorSuccess.copy(alpha = 0.15f)
                                else ColorPending.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isGeminiKeyConfigured) ColorSuccess else ColorPending)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.isGeminiKeyConfigured) "AI SHIELD" else "AI LOCAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isGeminiKeyConfigured) ColorSuccess else ColorPending
                        )
                    }

                    // Quick User Role Dropdown Switcher
                    RoleSwitcher(
                        currentRole = viewModel.currentUserRole,
                        onRoleSelected = { viewModel.changeUserRole(it) }
                    )

                    // JD Avatar
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(FintechPrimary, FintechSecondary)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = FintechSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = FintechBorder,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                val screens = listOf(
                    NavigationItem("Dashboard", Icons.Default.TrendingUp),
                    NavigationItem("Send Fund", Icons.Default.Payments),
                    NavigationItem("Risk Modeling", Icons.Default.Security),
                    NavigationItem("Audit Logs", Icons.Default.History),
                    NavigationItem("Settings", Icons.Default.Settings)
                )

                screens.forEach { item ->
                    val isSelected = viewModel.currentScreen == item.title
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.title) },
                        label = { Text(item.title, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = "${item.title} icon",
                                tint = if (isSelected) FintechPrimary else Color.White.copy(alpha = 0.5f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FintechPrimary,
                            selectedTextColor = FintechPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f),
                            indicatorColor = FintechSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${item.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    "Dashboard" -> DashboardScreen(viewModel, transactions)
                    "Send Fund" -> SendFundScreen(viewModel)
                    "Risk Modeling" -> RiskModelingScreen(viewModel)
                    "Audit Logs" -> AuditLogsScreen(viewModel, auditLogs)
                    "Settings" -> SettingsScreen(viewModel)
                }
            }

            // Global Transaction Detail Modal Popup
            viewModel.selectedTransaction?.let { tx ->
                TransactionDetailDialog(
                    transaction = tx,
                    currentUserRole = viewModel.currentUserRole,
                    onDismiss = { viewModel.selectedTransaction = null },
                    onOverrideAction = { approve, comment ->
                        viewModel.adminOverrideStatus(tx.id, approve, comment)
                    }
                )
            }
        }
    }
}

data class NavigationItem(val title: String, val icon: ImageVector)

// --- Composable Sub-Components ---

@Composable
fun RoleSwitcher(
    currentRole: String,
    onRoleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Business Owner", "Compliance Officer", "Finance Manager", "Admin")

    Box(modifier = Modifier.padding(end = 8.dp)) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
                .height(34.dp)
                .testTag("role_switcher_button")
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Role", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(currentRole, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    },
                    modifier = Modifier.testTag("role_item_${role.lowercase().replace(" ", "_")}")
                )
            }
        }
    }
}

// --- SCREEN 1: DASHBOARD ---
@Composable
fun DashboardScreen(
    viewModel: PaymentViewModel,
    transactions: List<TransactionEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = FintechSurface
                ),
                border = BorderStroke(1.dp, FintechBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Habari, ${viewModel.currentUserRole}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Monitoring corporate assets and real-time banking gateways.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Bank Icon",
                        tint = FintechPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Summary Metric Rows
        item {
            val totalTzs = transactions.filter { it.currency == "TZS" && it.status == "COMPLETED" }.sumOf { it.amount }
            val totalUsd = transactions.filter { it.currency == "USD" && it.status == "COMPLETED" }.sumOf { it.amount }
            val holdsCount = transactions.count { it.status == "HOLD_RISK" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Cleared (TZS)",
                    value = formatCurrency(totalTzs, "TZS"),
                    icon = Icons.Default.Payments,
                    color = ColorSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Cleared (USD)",
                    value = formatCurrency(totalUsd, "USD"),
                    icon = Icons.Default.AccountBalance,
                    color = FintechPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Alert Locks",
                    value = "$holdsCount Active",
                    icon = Icons.Default.Warning,
                    color = if (holdsCount > 0) ColorFailed else ColorSuccess,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // Live interactive flow custom trend chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = FintechSurface
                ),
                border = BorderStroke(1.dp, FintechBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real-Time Transaction Trend (TZS Equivalents)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple Canvas drawing of our corporate flow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val points = listOf(
                                Offset(0f, 110f),
                                Offset(120f, 80f),
                                Offset(240f, 100f),
                                Offset(360f, 40f),
                                Offset(480f, 70f),
                                Offset(600f, 20f),
                                Offset(720f, 50f)
                            )
                            
                            // Draw background gradient under path
                            path.moveTo(0f, size.height)
                            path.lineTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                path.quadraticTo(
                                    (points[i-1].x + points[i].x)/2, (points[i-1].y + points[i].y)/2,
                                    points[i].x, points[i].y
                                )
                            }
                            path.lineTo(size.width, size.height)
                            path.close()
                            
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        ColorSuccess.copy(alpha = 0.3f),
                                        ColorSuccess.copy(alpha = 0.0f)
                                    )
                                )
                            )

                            // Draw line
                            val linePath = Path()
                            linePath.moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                linePath.quadraticTo(
                                    (points[i-1].x + points[i].x)/2, (points[i-1].y + points[i].y)/2,
                                    points[i].x, points[i].y
                                )
                            }
                            drawPath(
                                path = linePath,
                                color = ColorSuccess,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Draw gridlines
                            for (y in 0..4) {
                                val gridY = size.height * (y / 4f)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, gridY),
                                    end = Offset(size.width, gridY),
                                    strokeWidth = 1f
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 Days Ago", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        Text("3 Days Ago", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        Text("Today", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Corporate Financial Audit Trails",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${transactions.size} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Transaction items
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions logged yet.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionRowItem(transaction = tx, onClick = {
                    viewModel.selectedTransaction = tx
                })
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = FintechSurface),
        border = BorderStroke(1.dp, FintechBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = FintechSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FintechBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon representing payment system gateway
            val (gateIcon, gateColor) = when (transaction.gateway) {
                "stripe" -> Pair(Icons.Default.AccountBalance, FintechPrimary)
                "clickpesa" -> Pair(Icons.Default.Security, FintechSecondary)
                else -> Pair(Icons.Default.Payments, ColorSuccess) // Mobile Money (mpesa, tigo, airtel)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(gateColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = gateIcon, contentDescription = null, tint = gateColor, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.receiverName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrency(transaction.amount, transaction.currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (transaction.status) {
                            "FAILED" -> ColorFailed
                            "HOLD_RISK" -> ColorPending
                            else -> ColorSuccess
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${transaction.gateway.uppercase()} • ${transaction.senderName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Small status chip
                    val statusText = when (transaction.status) {
                        "COMPLETED" -> "RECONCILED"
                        "HOLD_RISK" -> "HOLD RISK"
                        "FAILED" -> "REJECTED"
                        else -> transaction.status
                    }
                    val statusBg = when (transaction.status) {
                        "COMPLETED" -> ColorSuccess.copy(alpha = 0.12f)
                        "HOLD_RISK" -> ColorPending.copy(alpha = 0.12f)
                        else -> ColorFailed.copy(alpha = 0.12f)
                    }
                    val statusColor = when (transaction.status) {
                        "COMPLETED" -> ColorSuccess
                        "HOLD_RISK" -> ColorPending
                        else -> ColorFailed
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: SEND SECURE FUNDS ---
@Composable
fun SendFundScreen(viewModel: PaymentViewModel) {
    var handshakeOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Execute Secured Fund Transfer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Every GeneloPay payment automatically bundles enterprise security JWE encryption and HSM signatures under Bank of Tanzania compliance regulations.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Gateway Selection (Stripe vs Clickpesa vs Mobile money)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Corporate Banking Gateway", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("clickpesa", "Clickpesa (TZ)", FintechSecondary),
                        Triple("stripe", "Stripe Global", FintechPrimary),
                        Triple("mpesa", "M-Pesa (Local)", ColorSuccess),
                        Triple("tigopesa", "Tigo Pesa", ColorPending)
                    ).forEach { (key, label, color) ->
                        val selected = viewModel.formGateway == key
                        Button(
                            onClick = { viewModel.formGateway = key },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("gateway_select_$key"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) color else FintechSecondary,
                                contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Form Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Currency & Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.width(100.dp)) {
                        var currencyExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { currencyExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(viewModel.formCurrency)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            listOf("TZS", "USD", "EUR").forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        viewModel.formCurrency = curr
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.formAmount,
                        onValueChange = { viewModel.formAmount = it },
                        label = { Text("Transfer Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_amount")
                    )
                }

                // Sender Info (Pre-populated based on company registration)
                OutlinedTextField(
                    value = viewModel.formSenderName,
                    onValueChange = { viewModel.formSenderName = it },
                    label = { Text("Sender Corporate Entity (Tanzania KYC)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_sender_name")
                )

                OutlinedTextField(
                    value = viewModel.formSenderAccount,
                    onValueChange = { viewModel.formSenderAccount = it },
                    label = { Text("Sender Bank / Account Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Receiver Info
                OutlinedTextField(
                    value = viewModel.formReceiverName,
                    onValueChange = { viewModel.formReceiverName = it },
                    label = { Text("Receiver Legal Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_receiver_name")
                )

                OutlinedTextField(
                    value = viewModel.formReceiverAccount,
                    onValueChange = { viewModel.formReceiverAccount = it },
                    label = { Text("Receiver Account / Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("input_receiver_acc")
                )
            }
        }

        // Live Cryptographic Tunnel Display
        if (viewModel.formAmount.isNotEmpty() && viewModel.formReceiverName.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Tunnel", tint = ColorSecure, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Cipher Payload Wrap (TLS 1.3)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorSecure)
                    }

                    val rawPayload = "sender=${viewModel.formSenderAccount}&receiver=${viewModel.formReceiverAccount}&amount=${viewModel.formAmount}"
                    val base64String = Base64.encodeToString(rawPayload.toByteArray(), Base64.NO_WRAP)
                    
                    Text(
                        text = "JWE-AES256-SHA256: e3b0c442...$base64String",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Display feedback messages
        viewModel.submissionSuccessMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorSuccess.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, fontSize = 12.sp, color = ColorSuccess, fontWeight = FontWeight.Medium)
                }
            }
        }

        viewModel.submissionErrorMessage?.let { err ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorFailed.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ColorFailed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, fontSize = 12.sp, color = ColorFailed, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Submit Button
        Button(
            onClick = { viewModel.submitPayment() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("execute_payment_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ColorSecure),
            enabled = !viewModel.isSubmittingPayment
        ) {
            if (viewModel.isSubmittingPayment) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Process Secure Handshake", fontWeight = FontWeight.Bold)
            }
        }

        // Clickable interactive mobile money clearing simulation
        OutlinedButton(
            onClick = { handshakeOpen = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = "Local Money", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Mobile Money Cleardown Node")
        }
    }

    if (handshakeOpen) {
        MobileMoneySimulationDialog(onDismiss = { handshakeOpen = false })
    }
}

// --- SCREEN 3: AI RISK MODELING PLAYGROUND ---
@Composable
fun RiskModelingScreen(viewModel: PaymentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI Predictive Risk Modeling & AML Shield", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Configure corporate risk simulations below. The server-side Gemini intelligence evaluates suspicious routing, structural asset flows, and outputs BOT compliance advice.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Simulate Transaction Parameters", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.riskPlaygroundAmount,
                        onValueChange = { viewModel.riskPlaygroundAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.weight(0.6f)) {
                        var currExp by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { currExp = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(viewModel.riskPlaygroundCurrency)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = currExp, onDismissRequest = { currExp = false }) {
                            listOf("TZS", "USD", "EUR").forEach { curr ->
                                DropdownMenuItem(text = { Text(curr) }, onClick = {
                                    viewModel.riskPlaygroundCurrency = curr
                                    currExp = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.riskPlaygroundSender,
                    onValueChange = { viewModel.riskPlaygroundSender = it },
                    label = { Text("Sender Business Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.riskPlaygroundReceiver,
                    onValueChange = { viewModel.riskPlaygroundReceiver = it },
                    label = { Text("Receiver Entity Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Gateway Dropdown
                var gateExp by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { gateExp = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Gateway: ${viewModel.riskPlaygroundGateway.uppercase()}")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = gateExp, onDismissRequest = { gateExp = false }) {
                        listOf("clickpesa", "stripe", "mpesa", "tigopesa").forEach { gate ->
                            DropdownMenuItem(text = { Text(gate.uppercase()) }, onClick = {
                                viewModel.riskPlaygroundGateway = gate
                                gateExp = false
                            })
                        }
                    }
                }

                Button(
                    onClick = { viewModel.runPredictiveRiskModeling() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_risk_analysis"),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorShield),
                    enabled = !viewModel.riskPlaygroundIsAnalyzing
                ) {
                    if (viewModel.riskPlaygroundIsAnalyzing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Security, contentDescription = "Shield", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute AI AML Analysis", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Result Card
        viewModel.riskPlaygroundResult?.let { result ->
            val color = when (result.riskCategory) {
                "LOW" -> ColorSuccess
                "MEDIUM" -> ColorPending
                else -> ColorFailed
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.5.dp, color),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Risk Score Rating", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${result.riskCategory} (${result.riskScore}/100)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "Regulatory Advice:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = result.reason,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automated Engine Action:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result.recommendation,
                            color = color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: AUDIT TRAIL LOGS ---
@Composable
fun AuditLogsScreen(
    viewModel: PaymentViewModel,
    auditLogs: List<AuditLogEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Compliance Financial Audit Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Tanzanian Financial Intelligence Unit (FIU) compliance reporting requirements mandate logging all platform sessions, keys, settings updates, and transaction clearances.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (auditLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No audit trails created yet.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(auditLogs) { log ->
                AuditLogRowItem(log)
            }
        }
    }
}

@Composable
fun AuditLogRowItem(log: AuditLogEntity) {
    val accentColor = when (log.action) {
        "SYSTEM_INITIALIZATION" -> ColorSecure
        "TRANSACTION_PROCESSED" -> ColorSuccess
        "TRANSACTION_OVERRIDE" -> ColorFailed
        "SECURITY_POLICY_UPDATE" -> ColorShield
        else -> FintechPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FintechSurface),
        border = BorderStroke(1.dp, FintechBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.action,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Text(
                    text = formatDateTime(log.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = log.details,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            HorizontalDivider(thickness = 0.5.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = "Auditor", modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Operator: ${log.performedBy}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- SCREEN 5: SETTINGS & OPERATIONS ---
@Composable
fun SettingsScreen(viewModel: PaymentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Administrative Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Operational overrides, network toggles, and cryptographic configurations for managing Tanzania localized clearance limits.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Security & Anti-Money Laundering Regulations", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Cryptographic Envelope", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Wrapping transaction parameters inside TLS 1.3 tunnels securely using local hardware keys.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = viewModel.settingsEncryptedPayloadEnabled,
                        onCheckedChange = { viewModel.toggleEncryptedPayloadSetting(it) },
                        modifier = Modifier.testTag("toggle_crypto_payload")
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automated AI AML Shield", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Automatically routes anomalous corporate parameters through Gemini context analysis before clearance.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = viewModel.settingsAmlShieldEnabled,
                        onCheckedChange = { viewModel.toggleAmlShieldSetting(it) },
                        modifier = Modifier.testTag("toggle_aml_shield")
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Corporate Handshake Limits", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

                var limitStr by remember { mutableStateOf(viewModel.settingsMaxDailyLimitTzs.toString()) }
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = {
                        limitStr = it
                        val doubleVal = it.toDoubleOrNull()
                        if (doubleVal != null) {
                            viewModel.settingsMaxDailyLimitTzs = doubleVal
                        }
                    },
                    label = { Text("Max Automated Transaction Limit (TZS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_settings_limit")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clearing Sandbox State", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = viewModel.settingsSandboxModeEnabled,
                        onCheckedChange = { viewModel.settingsSandboxModeEnabled = it }
                    )
                }

                Button(
                    onClick = { viewModel.saveOperationalSettings() },
                    modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
                ) {
                    Text("Save Operational Parameters")
                }
            }
        }

        // Danger Zone
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Operational Danger Zone", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Resetting databases destroys audit logs and transaction caches for localized development. Highly restricted.", fontSize = 11.sp)

                OutlinedButton(
                    onClick = { viewModel.resetAppSandbox() },
                    modifier = Modifier.fillMaxWidth().testTag("reset_sandbox_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Purge & Re-Initialize Database Sandbox")
                }
            }
        }
    }
}

// --- SECURE DIALOG: OVERRIDE HOLD ACTION FOR ADMINS/AUDITORS ---
@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    currentUserRole: String,
    onDismiss: () -> Unit,
    onOverrideAction: (approve: Boolean, comment: String) -> Unit
) {
    var auditorComment by remember { mutableStateOf("") }
    val (statusLabel, statusColor) = when (transaction.status) {
        "COMPLETED" -> Pair("RECONCILED", ColorSuccess)
        "HOLD_RISK" -> Pair("AUDIT RISK HOLD", ColorPending)
        else -> Pair("REJECTED / FAILED", ColorFailed)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaction Audit inspector", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Currency details
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatCurrency(transaction.amount, transaction.currency),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Text(
                        text = "Ref: ${transaction.transactionRef}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Security parameter grids
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TransactionMetaRow("Sender legal Entity", transaction.senderName)
                        TransactionMetaRow("Sender Route Account", transaction.senderAccount)
                        TransactionMetaRow("Receiver Legal Payee", transaction.receiverName)
                        TransactionMetaRow("Receiver Route Account", transaction.receiverAccount)
                        TransactionMetaRow("Gateway Conduit", transaction.gateway.uppercase())
                        TransactionMetaRow("Local Clearing UTC", formatDateTime(transaction.timestamp))
                    }
                }

                // AI Risk Meter and Reason
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FintechSurface),
                    border = BorderStroke(1.dp, FintechBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Compliance AI Assessment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Risk Score: ${transaction.riskScore}/100 (${transaction.riskCategory})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        Text(
                            text = transaction.complianceHoldReason ?: "Passed automated compliance validation.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Cryptographic Envelope inspector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FintechSurface),
                    border = BorderStroke(1.dp, FintechBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Cryptographic Envelope Signing (SHA256)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "ENC_PAYLOAD: ${transaction.encryptedPayload}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HMAC_SIG: ${transaction.signature}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = ColorSecure
                        )
                    }
                }

                // Compliance Officer override interface
                if (transaction.status == "HOLD_RISK") {
                    val canOverride = currentUserRole == "Compliance Officer" || currentUserRole == "Admin"

                    HorizontalDivider()
                    Text(
                        text = if (canOverride) "Compliance Hold Resolution Action" else "Hold resolution restricted to Auditors.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canOverride) ColorSecure else ColorFailed
                    )

                    if (canOverride) {
                        OutlinedTextField(
                            value = auditorComment,
                            onValueChange = { auditorComment = it },
                            label = { Text("Regulatory Auditor Comment (Mandatory)") },
                            modifier = Modifier.fillMaxWidth().testTag("auditor_comment_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (auditorComment.isNotBlank()) {
                                        onOverrideAction(true, auditorComment)
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("auditor_approve_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                enabled = auditorComment.isNotBlank()
                            ) {
                                Text("Approve Clear")
                            }

                            Button(
                                onClick = {
                                    if (auditorComment.isNotBlank()) {
                                        onOverrideAction(false, auditorComment)
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("auditor_reject_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorFailed),
                                enabled = auditorComment.isNotBlank()
                            ) {
                                Text("Freeze / Reject")
                            }
                        }
                    } else {
                        // Explain why it's locked
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColorFailed.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Text(
                                "Locked. Switch your user session role in the top header to 'Compliance Officer' or 'Admin' to override risk hold decisions.",
                                fontSize = 11.sp,
                                color = ColorFailed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Auditor Inspector")
                }
            }
        }
    }
}

@Composable
fun TransactionMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// --- SECURE DIALOG: MOBILE MONEY CLEARDOWN SHIELD ---
@Composable
fun MobileMoneySimulationDialog(
    onDismiss: () -> Unit
) {
    val logs = remember {
        mutableStateListOf(
            "Establishing TLS 1.3 socket to M-Pesa push API endpoint...",
            "Handshake completed: Server cert authenticated.",
            "Sending localized payment token requests to gateway router...",
            "Awaiting response from Tanzania Clearing clearing network..."
        )
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        logs.add("[SUCCESS] M-Pesa callback receiver verified: 200 OK.")
        kotlinx.coroutines.delay(1200)
        logs.add("[INFO] Clickpesa settlement pool successfully verified.")
        kotlinx.coroutines.delay(1200)
        logs.add("[SUCCESS] Cryptographic verification complete. Transaction reconciled.")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FintechSurface),
            border = BorderStroke(1.dp, FintechBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mobile Money Cleardown Console", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Console display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .padding(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (log.contains("[SUCCESS]")) ColorSuccess else if (log.contains("[INFO]")) ColorShield else Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close Terminal")
                }
            }
        }
    }
}

// --- HELPER FORMATTING FUNCTIONS ---

private fun formatCurrency(amount: Double, currency: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = java.util.Currency.getInstance(currency)
        format.format(amount)
    } catch (e: Exception) {
        "$amount $currency"
    }
}

private fun formatDateTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        timestamp.toString()
    }
}
