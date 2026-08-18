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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.MRTPassViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MRTPassRechargeScreen(
    onBack: () -> Unit,
    onProceedToPayment: (String) -> Unit,
    viewModel: MRTPassViewModel
) {
    val scrollState = rememberScrollState()
    val extendedColors = MetroTransitTheme.extendedColors
    
    var isBackTriggered by remember { mutableStateOf(false) }
    
    val selectedCard = viewModel.selectedCard ?: return
    
    val amounts = listOf("100", "200", "500", "1,000", "2,000", "Other")
    var selectedAmountStr by remember { mutableStateOf("100") }
    
    val amountDouble = selectedAmountStr.replace(",", "").toDoubleOrNull() ?: 0.0
    val gatewayFee = amountDouble * 0.0083 
    val totalAmount = amountDouble + gatewayFee

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { NavTitle("Recharge Card") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBackTriggered) {
                            isBackTriggered = true
                            onBack()
                        }
                    }) {
                        BackIcon()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    .padding(20.dp)
                    .padding(bottom = LocalJourneyBarInset.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Card (Glass)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PhysicalCardVisual()
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        RechargeDetailRow("Card", selectedCard.cardNumber)
                        RechargeDetailRow("Name", selectedCard.cardName)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Amount Title
                Text(
                    text = "Select Recharge Amount",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Large Amount Display in Glass
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.glass,
                    border = BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "৳ ${if (selectedAmountStr == "Other") "0" else selectedAmountStr}",
                            fontSize = 48.sp,
                            fontFamily = AppFont.display,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(amounts) { amount ->
                        val isSelected = selectedAmountStr == amount
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { selectedAmountStr = amount },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else extendedColors.glass,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.glassBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (amount == "Other") amount else "৳ $amount",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textPrimary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Payment Summary Glass
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Payment Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SummaryRow("Recharge Amount", "৳ ${String.format(Locale.US, "%,d", amountDouble.toInt())}")
                        SummaryRow("Gateway Fee (0.83%)", "৳ ${String.format(Locale.US, "%.2f", gatewayFee)}")
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = extendedColors.glassBorder
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Payable", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary)
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", totalAmount)}",
                                fontSize = 22.sp,
                                fontFamily = AppFont.display,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // PAY Button
                Button(
                    onClick = {
                        viewModel.rechargeAmount = amountDouble.toString()
                        onProceedToPayment(String.format(Locale.US, "%.2f", totalAmount))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PROCEED TO PAY",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "© 2026 Dhaka Mass Transit Company Limited",
                    fontSize = 11.sp,
                    color = extendedColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RechargeDetailRow(label: String, value: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.width(60.dp), fontSize = 14.sp, color = extendedColors.textSecondary, fontWeight = FontWeight.Medium)
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, extendedColors.glassBorder)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = extendedColors.textSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.textPrimary)
    }
}
