package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DummyPaymentGatewayScreen(
    amount: String,
    onPaymentComplete: (Boolean) -> Unit
) {
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000) // Simulate gateway delay
        isProcessing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessing) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SSLCommerz",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(text = "Dummy Payment Gateway", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(text = "Processing Payment of", fontSize = 16.sp)
                    Text(text = "\u09f3 $amount", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Please do not close this window...", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\u2705",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Payment Successful",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onPaymentComplete(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A4E))
                    ) {
                        Text("Return to Merchant")
                    }
                }
            }
        }
    }
}
