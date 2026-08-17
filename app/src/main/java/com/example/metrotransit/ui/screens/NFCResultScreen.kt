package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCResultScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val balance      = viewModel.scannedBalance ?: 0.0
    val transactions = viewModel.recentTransactions
    val isScanning   = viewModel.isScanning
    val scanError    = viewModel.scanError
    val scrollState  = rememberScrollState()
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Card Details", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = extendedColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extendedColors.backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = LocalJourneyBarInset.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Scanning indicator ─────────────────────────────────────────
                if (isScanning) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Reading card…",
                                color = extendedColors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    return@Scaffold
                }

                // ── Error state ────────────────────────────────────────────────
                if (scanError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ Scan Failed",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                scanError,
                                color = extendedColors.textSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.resetScan()
                                    onBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Try Again", color = Color.White)
                            }
                        }
                    }
                    return@Scaffold
                }

                // ── Balance card ───────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "MRT / Rapid Pass",
                                modifier = Modifier.align(Alignment.Center),
                                color = extendedColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Rescan",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clickable {
                                        viewModel.resetScan()
                                        viewModel.showScanSheet = true
                                        onBack()
                                    },
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Current Balance",
                                color = extendedColors.textSecondary,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "৳ ${balance.toInt()}",
                                color = if (balance < 20) MaterialTheme.colorScheme.error else extendedColors.textPrimary,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (balance < 20) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Balance too low for the next trip. Top up needed.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Recent journeys card ───────────────────────────────────────
                if (transactions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Recent Journeys (${transactions.size})",
                                color = extendedColors.textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            transactions.forEach { tx ->
                                ResultTransactionItem(
                                    route        = tx.route,
                                    date         = tx.date,
                                    amount       = tx.amount,
                                    balanceAfter = tx.balanceAfter
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No trip history found on this card.",
                                color = extendedColors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ResultTransactionItem(
    route: String,
    date: String,
    amount: Int,
    balanceAfter: Int
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                route,
                color = extendedColors.textPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                date,
                color = extendedColors.textSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            if (balanceAfter >= 0) {
                Text(
                    "Balance after: ৳ $balanceAfter",
                    color = extendedColors.textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Text(
            if (amount >= 0) "৳ $amount" else "৳ $amount",
            color = if (amount >= 0) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(color = extendedColors.glassBorder)
}
