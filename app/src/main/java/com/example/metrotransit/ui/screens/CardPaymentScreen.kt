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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credit/Debit Card", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                CardSummaryBottomBar(amount)
                Button(
                    onClick = { isProcessing = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF36F21)),
                    enabled = cardNumber.length >= 16 && expiry.length >= 4 && cvv.length >= 3 && !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Pay Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F2F6))
                .verticalScroll(rememberScrollState())
        ) {
            // Voucher Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE6F0FF))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF005DC0),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Collect payment voucher & get extra savings on your purchase!",
                        color = Color(0xFF005DC0),
                        fontSize = 11.sp
                    )
                }
            }

            // Protection Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Covered by MetroTransit Payment Protection",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDCB3", fontSize = 18.sp) // American Express placeholder
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\uD83D\uDCB3", fontSize = 18.sp) // Mastercard placeholder
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("\uD83D\uDCB3", fontSize = 18.sp) // Visa placeholder
                }

                CardTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16) cardNumber = it },
                    label = "Card number",
                    keyboardType = KeyboardType.Number
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTextField(
                        value = expiry,
                        onValueChange = { if (it.length <= 4) expiry = it },
                        label = "Expiry (MM/YY)",
                        modifier = Modifier.weight(1f),
                        showHelp = true,
                        keyboardType = KeyboardType.Number
                    )
                    CardTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it },
                        label = "CVV",
                        modifier = Modifier.weight(1f),
                        showHelp = true,
                        keyboardType = KeyboardType.Number
                    )
                }

                CardTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = "Name on card",
                    showHelp = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We will save this card for your convenience. If required, you can remove the card in the \"Payment Options\" section in the \"Account\" menu.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
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
    showHelp: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(label, color = Color.LightGray, fontSize = 14.sp) },
        trailingIcon = {
            if (showHelp) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(2.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedBorderColor = Color(0xFFF36F21),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
}

@Composable
fun CardSummaryBottomBar(amount: String) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = Color.Gray, fontSize = 14.sp)
                Text("\u09f3 $amount", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    "\u09f3 $amount",
                    color = Color(0xFFF36F21),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
