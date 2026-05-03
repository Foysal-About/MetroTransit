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
import com.example.metrotransit.viewmodel.HomeViewModel
import com.example.metrotransit.viewmodel.Transaction

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Card Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212))
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Scanning indicator ─────────────────────────────────────────
            if (isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Reading card…",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                return@Scaffold
            }

            // ── Error state ────────────────────────────────────────────────
            if (scanError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "⚠️ Scan Failed",
                            color = Color(0xFFE57373),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            scanError,
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                viewModel.resetScan()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Try Again", color = Color.White)
                        }
                    }
                }
                return@Scaffold
            }

            // ── Balance card ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4CAF50))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "MRT / Rapid Pass",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Rescan",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    viewModel.resetScan()
                                    onBack()
                                },
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
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
                            color = Color.LightGray,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "৳ ${balance.toInt()}",
                            color = if (balance < 20) Color(0xFFE57373) else Color.White,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (balance < 20) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Balance too low for the next trip. Top up needed.",
                                color = Color(0xFFE57373),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Recent journeys card ───────────────────────────────────────
            if (transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Recent Journeys (${transactions.size})",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No trip history found on this card.",
                            color = Color.Gray,
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

@Composable
fun ResultTransactionItem(
    route: String,
    date: String,
    amount: Int,
    balanceAfter: Int
) {
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
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                date,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            // Balance after this trip, shown in muted text
            if (balanceAfter > 0) {
                Text(
                    "Balance after: ৳ ${balanceAfter / 100}",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            if (amount >= 0) "+৳ $amount" else "৳ $amount",
            color = if (amount >= 0) Color(0xFF81C784) else Color(0xFFE57373),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}