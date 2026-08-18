package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentScreen(
    amount: String,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { NavTitle("Credit/Debit Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackIcon()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = extendedColors.surface,
                shadowElevation = 16.dp,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
            ) {
                Column(modifier = Modifier.navigationBarsPadding().padding(20.dp)) {
                    CardSummaryBottomBar(amount)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isProcessing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = cardNumber.length >= 16 && expiry.length >= 4 && cvv.length >= 3 && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("PAY ৳$amount", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = LocalJourneyBarInset.current)
            ) {
                // Info Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Secure 256-bit SSL Encrypted Payment", color = extendedColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card Form
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        CardTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 16) cardNumber = it },
                            label = "Card Number",
                            keyboardType = KeyboardType.Number
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CardTextField(
                                value = expiry,
                                onValueChange = { if (it.length <= 4) expiry = it },
                                label = "MM/YY",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number
                            )
                            CardTextField(
                                value = cvv,
                                onValueChange = { if (it.length <= 4) cvv = it },
                                label = "CVV",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number
                            )
                        }

                        CardTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = "Cardholder Name"
                        )

                        Text(
                            text = "Your card details are protected by MetroTransit Payment Protection. We do not store your CVV.",
                            color = extendedColors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (isProcessing) {
        LaunchedEffect(Unit) {
            delay(2500)
            isProcessing = false
            onPaymentSuccess()
        }
    }
}

@Composable
fun CardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = extendedColors.textSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = extendedColors.glassBorder,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Black.copy(alpha = 0.05f),
                focusedContainerColor = Color.Black.copy(alpha = 0.05f),
                unfocusedTextColor = extendedColors.textPrimary,
                focusedTextColor = extendedColors.textPrimary
            )
        )
    }
}

@Composable
fun CardSummaryBottomBar(amount: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Amount to Pay", style = MaterialTheme.typography.bodyMedium, color = extendedColors.textSecondary)
        Text(
            "৳ $amount",
            color = extendedColors.textPrimary,
            fontWeight = FontWeight.Black,
            fontFamily = AppFont.display, fontSize = 20.sp
        )
    }
}
