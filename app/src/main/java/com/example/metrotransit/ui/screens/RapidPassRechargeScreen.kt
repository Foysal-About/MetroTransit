package com.example.metrotransit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.MRTPassCard
import com.example.metrotransit.viewmodel.MRTPassViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MRTPassRechargeScreen(
    onBack: () -> Unit,
    onProceedToPayment: (String) -> Unit,
    viewModel: MRTPassViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Guard to prevent multiple back navigation on double tap
    var isBackTriggered by remember { mutableStateOf(false) }
    
    val selectedCard = viewModel.selectedCard ?: return
    
    val amounts = listOf("100", "200", "500", "1,000", "2,000", "Other")
    var selectedAmountStr by remember { mutableStateOf("100") }
    
    val amountDouble = selectedAmountStr.replace(",", "").toDoubleOrNull() ?: 0.0
    val gatewayFee = amountDouble * 0.0083 // 0.83% fee as seen in screenshot (0.83 for 100)
    val totalAmount = amountDouble + gatewayFee

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
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
                actions = {
                    TextButton(onClick = { /* Toggle Language */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83C\uDF10 ", fontSize = 16.sp)
                            Text("\u09ac\u09be\u0982\u09b2\u09be", color = Color(0xFF0056B3), fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color(0xFF5A67D8), modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Card Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                border = BorderStroke(0.5.dp, Color(0xFFD1E4FF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Reusing the visual from dashboard (Simplified version here)
                    PhysicalCardVisual(type = selectedCard.type)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RechargeDetailRow("Card", selectedCard.cardNumber)
                    RechargeDetailRow("Name", selectedCard.cardName)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Large Amount Display
            Text(
                text = "\u09f3${if (selectedAmountStr == "Other") "0" else selectedAmountStr}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007BFF),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Amount Selection
            Text(
                text = "Select recharge amount*",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(amounts) { amount ->
                    val isSelected = selectedAmountStr == amount
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { selectedAmountStr = amount },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF007BFF) else Color(0xFFE0E0E0)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (amount == "Other") amount else "\u09f3 $amount",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF007BFF) else Color.DarkGray
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Payment Summary
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Payment summary",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryRow("Recharge amount:", "\u09f3${String.format(Locale.US, "%,d", amountDouble.toInt())}")
            SummaryRow("Gateway fee:", "\u09f3${String.format(Locale.US, "%.2f", gatewayFee)}")
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total amount:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "\u09f3${String.format(Locale.US, "%.2f", totalAmount)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // PAY Button
            Button(
                onClick = {
                    viewModel.rechargeAmount = amountDouble.toString()
                    onProceedToPayment(String.format(Locale.US, "%.2f", totalAmount))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061C1))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAY \u09f3${String.format(Locale.US, "%.2f", totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Footer Logos Simulation
            Text(
                text = "Pay With:",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder for the long list of payment logos
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp).background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("BKash | Nagad | Visa | Mastercard | Rocket | ...", fontSize = 10.sp, color = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "\u00a9 2026. All Rights Reserved DTCA",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RechargeDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "$label : ", modifier = Modifier.width(60.dp), fontSize = 16.sp, color = Color.DarkGray)
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 16.sp, color = Color.Black)
    }
}
