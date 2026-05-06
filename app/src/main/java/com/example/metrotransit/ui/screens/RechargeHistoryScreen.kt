package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.RechargeTransaction
import com.example.metrotransit.viewmodel.MRTPassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeHistoryScreen(
    onBack: () -> Unit,
    viewModel: MRTPassViewModel
) {
    val history by viewModel.rechargeHistory.collectAsState()
    var isBackTriggered by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recharge History", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBackTriggered) {
                            isBackTriggered = true
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { transaction ->
                RechargeHistoryItem(transaction)
            }
        }
    }
}

@Composable
fun RechargeHistoryItem(transaction: RechargeTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SL: ${transaction.sl}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "\u09f3 ${transaction.amount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            HistoryDetailRow("Card Number", transaction.cardNumber)
            HistoryDetailRow("Payment ID", transaction.paymentId)
            HistoryDetailRow("Date & Time", transaction.dateTime)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = transaction.paymentStatus,
                    isSuccess = transaction.paymentStatus.contains("Successful")
                )
                if (transaction.rechargeStatus != null) {
                    StatusBadge(
                        text = transaction.rechargeStatus,
                        isSuccess = transaction.rechargeStatus.contains("Successful")
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}

@Composable
fun StatusBadge(text: String, isSuccess: Boolean) {
    Surface(
        color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
