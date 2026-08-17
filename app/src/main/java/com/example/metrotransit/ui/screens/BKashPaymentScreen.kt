package com.example.metrotransit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.R
import com.example.metrotransit.ui.theme.AppFont
import kotlinx.coroutines.delay

enum class BKashStep {
    ACCOUNT_NUMBER,
    OTP,
    PIN,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BKashPaymentScreen(
    amount: String,
    onPaymentSuccess: () -> Unit,
    onClose: () -> Unit
) {
    var currentStep by remember { mutableStateOf(BKashStep.ACCOUNT_NUMBER) }
    var accountNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val bKashPink = Color(0xFFE2136E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF757575).copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = when(currentStep) {
                            BKashStep.ACCOUNT_NUMBER -> "Agreement only"
                            BKashStep.OTP -> "bKash Verification"
                            BKashStep.PIN -> "Enter PIN"
                            BKashStep.SUCCESS -> "Payment Successful"
                        },
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

                // bKash Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bkash_logo),
                        contentDescription = "bKash",
                        modifier = Modifier.height(35.dp)
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

                // Merchant Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFF00ADEF).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("M", color = Color(0xFF00ADEF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("MetroTransit", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("\u09f3 $amount", fontWeight = FontWeight.Bold, color = bKashPink)
                }

                // Main Pink Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bKashPink)
                        .padding(horizontal = 20.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        BKashStep.ACCOUNT_NUMBER -> {
                            Text(
                                "Your bKash Account Number",
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            TextField(
                                value = accountNumber,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 11) accountNumber = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                placeholder = { Text("e.g 01XXXXXXXXX", color = Color.LightGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(2.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = bKashPink
                                ),
                                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Confirm and proceed, ")
                                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                        append("terms & conditions")
                                    }
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        BKashStep.OTP -> {
                            Text(
                                "Enter Verification Code (OTP) sent to $accountNumber",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            TextField(
                                value = otp,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 6) otp = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                placeholder = { Text("Enter OTP", color = Color.LightGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(2.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 18.sp)
                            )
                        }
                        BKashStep.PIN -> {
                            Text(
                                "Enter PIN of your bKash Account",
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            TextField(
                                value = pin,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 5) pin = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                placeholder = { Text("Enter PIN", color = Color.LightGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(2.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 18.sp)
                            )
                        }
                        BKashStep.SUCCESS -> {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Should be a checkmark
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Payment Successful!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = AppFont.display,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Buttons
                if (currentStep != BKashStep.SUCCESS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                        
                        val isEnabled = when(currentStep) {
                            BKashStep.ACCOUNT_NUMBER -> accountNumber.length == 11
                            BKashStep.OTP -> otp.length >= 4
                            BKashStep.PIN -> pin.length >= 4
                            else -> true
                        }
                        
                        Button(
                            onClick = {
                                if (currentStep == BKashStep.PIN) {
                                    isProcessing = true
                                } else {
                                    currentStep = when(currentStep) {
                                        BKashStep.ACCOUNT_NUMBER -> BKashStep.OTP
                                        BKashStep.OTP -> BKashStep.PIN
                                        else -> currentStep
                                    }
                                }
                            },
                            enabled = isEnabled && !isProcessing,
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEnabled) Color(0xFFE0E0E0) else Color(0xFFF5F5F5),
                                contentColor = if (isEnabled) Color.Black else Color.LightGray,
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledContentColor = Color.LightGray
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = bKashPink, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (currentStep == BKashStep.PIN) "Confirm" else "Proceed",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Phone icon placeholder
                        Text("\uD83D\uDCDE", fontSize = 12.sp, color = bKashPink)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("16247", color = bKashPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "\u00a9 2026 bKash, All Rights Reserved",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    if (isProcessing) {
        LaunchedEffect(Unit) {
            delay(2000)
            isProcessing = false
            currentStep = BKashStep.SUCCESS
            delay(2000)
            onPaymentSuccess()
        }
    }
}
