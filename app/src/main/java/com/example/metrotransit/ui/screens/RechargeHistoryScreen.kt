package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.MRTPassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeHistoryScreen(
    onBack: () -> Unit,
    viewModel: MRTPassViewModel
) {
    val history by viewModel.rechargeHistory.collectAsState()
    var isBackTriggered by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Recharge History", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.textPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBackTriggered) {
                            isBackTriggered = true
                            onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { transaction ->
                    RechargeHistoryItem(transaction)
                }
            }
        }
    }
}

@Composable
fun RechargeHistoryItem(transaction: RechargeTransaction) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ID: ${transaction.paymentId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "৳ ${transaction.amount}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = extendedColors.textPrimary
                )
            }

            HorizontalDivider(thickness = 1.dp, color = extendedColors.glassBorder)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryDetailLine("Card Number", transaction.cardNumber)
                HistoryDetailLine("Date & Time", transaction.dateTime)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoryStatusBadge(
                    text = transaction.paymentStatus,
                    isSuccess = transaction.paymentStatus.contains("Successful")
                )
                if (transaction.rechargeStatus != null) {
                    HistoryStatusBadge(
                        text = transaction.rechargeStatus,
                        isSuccess = transaction.rechargeStatus.contains("Successful")
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryDetailLine(label: String, value: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = extendedColors.textSecondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.textPrimary)
    }
}

@Composable
fun HistoryStatusBadge(text: String, isSuccess: Boolean) {
    val color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
