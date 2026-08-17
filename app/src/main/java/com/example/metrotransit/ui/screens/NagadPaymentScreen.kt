package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
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
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlinx.coroutines.delay

enum class NagadStep {
    ACCOUNT_NUMBER,
    OTP,
    PIN,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NagadPaymentScreen(
    amount: String,
    onPaymentSuccess: () -> Unit,
    onClose: () -> Unit
) {
    var currentStep by remember { mutableStateOf(NagadStep.ACCOUNT_NUMBER) }
    var accountNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val nagadRed = Color(0xFFD12030)
    val extendedColors = MetroTransitTheme.extendedColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = extendedColors.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = when(currentStep) {
                            NagadStep.ACCOUNT_NUMBER -> "Nagad Authorization"
                            NagadStep.OTP -> "Verification OTP"
                            NagadStep.PIN -> "Nagad PIN"
                            NagadStep.SUCCESS -> "Payment Successful"
                        },
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = extendedColors.textPrimary
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = extendedColors.textSecondary)
                    }
                }

                // Red Main Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(nagadRed)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when(currentStep) {
                            NagadStep.ACCOUNT_NUMBER -> "Pay with Nagad"
                            NagadStep.OTP -> "Enter OTP sent to $accountNumber"
                            NagadStep.PIN -> "Enter Nagad PIN"
                            NagadStep.SUCCESS -> "Success!"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Payable Amount: ৳ $amount", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(32.dp))

                    when(currentStep) {
                        NagadStep.ACCOUNT_NUMBER -> {
                            Text(
                                "Nagad Account Number",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 11) {
                                    val char = if (i < accountNumber.length) accountNumber[i].toString() else ""
                                    Box(
                                        modifier = Modifier
                                            .size(width = 24.dp, height = 36.dp)
                                            .background(Color.White, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(char, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                            }
                            BasicTextField(
                                value = accountNumber,
                                onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) accountNumber = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.size(width = 280.dp, height = 36.dp).alpha(0f)
                            )
                        }
                        NagadStep.OTP -> {
                            Text(
                                "Enter 6-Digit OTP",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 6) {
                                    val char = if (i < otp.length) otp[i].toString() else ""
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.White, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(char, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = AppFont.display, fontSize = 20.sp)
                                    }
                                }
                            }
                            BasicTextField(
                                value = otp,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otp = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.size(width = 200.dp, height = 40.dp).alpha(0f)
                            )
                        }
                        NagadStep.PIN -> {
                            Text(
                                "Enter 4-Digit PIN",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 4) {
                                    val char = if (i < pin.length) "●" else ""
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(char, color = Color.Black, fontSize = 16.sp)
                                    }
                                }
                            }
                            BasicTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.size(width = 160.dp, height = 40.dp).alpha(0f)
                            )
                        }
                        NagadStep.SUCCESS -> {
                             Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
                             Spacer(modifier = Modifier.height(16.dp))
                             Text("Payment Successful!", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFont.display, fontSize = 20.sp)
                        }
                    }

                    if (currentStep != NagadStep.SUCCESS) {
                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    when(currentStep) {
                                        NagadStep.ACCOUNT_NUMBER -> if(accountNumber.length == 11) currentStep = NagadStep.OTP
                                        NagadStep.OTP -> if(otp.length >= 4) currentStep = NagadStep.PIN
                                        NagadStep.PIN -> if(pin.length >= 4) isProcessing = true
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = nagadRed)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = nagadRed, strokeWidth = 2.dp)
                                } else {
                                    Text("PROCEED", fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedButton(
                                onClick = onClose,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Image(
                        painter = painterResource(id = R.drawable.nagad_logo),
                        contentDescription = "Nagad",
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }

    if (isProcessing) {
        LaunchedEffect(Unit) {
            delay(2000)
            isProcessing = false
            currentStep = NagadStep.SUCCESS
            delay(1500)
            onPaymentSuccess()
        }
    }
}
