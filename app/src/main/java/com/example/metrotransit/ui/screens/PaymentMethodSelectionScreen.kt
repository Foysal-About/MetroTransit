package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.PaymentMethod
import com.example.metrotransit.viewmodel.MRTPassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelectionScreen(
    amount: String,
    viewModel: MRTPassViewModel,
    onBack: () -> Unit,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Payment Method", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            PaymentSummaryBottomBar(amount)
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
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF005DC0),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Collect payment voucher & get extra savings on your purchase!",
                        color = Color(0xFF005DC0),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recommended section
            SectionHeader("Recommended method(s)")
            PaymentMethodItem(
                name = "Credit/Debit Card",
                subtitle = "Credit/Debit Card",
                showCardLogos = true,
                onClick = { 
                    onMethodSelected(PaymentMethod("Card", "Card")) 
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Other methods
            SectionHeader("Other Payment Methods")
            
            val otherMethods = viewModel.paymentMethods.filter { 
                val name = it.name.lowercase()
                !name.contains("visa") && 
                !name.contains("mastercard") && 
                !name.contains("cash on delivery") && 
                !name.contains("installment") &&
                !name.contains("instalment")
            }

            Column(modifier = Modifier.background(Color.White)) {
                otherMethods.forEachIndexed { index, method ->
                    PaymentMethodItem(
                        name = if (method.name.lowercase() == "bkash") "bKash " else method.name,
                        iconRes = method.iconRes,
                        onClick = { onMethodSelected(method) }
                    )
                    if (index < otherMethods.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFEEEEEE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Logos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Norton | PCI | Visa | Mastercard", fontSize = 10.sp, color = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun PaymentMethodItem(
    name: String,
    subtitle: String? = null,
    showCardLogos: Boolean = false,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Icon Container
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    // Fallback visual using brand colors and emojis/initials
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(6.dp),
                        color = getBrandColor(name).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val iconText = when {
                                name.lowercase().contains("bkash") -> "\uD83D\uDD4A" // Bird/Pigeon-ish
                                name.lowercase().contains("nagad") -> "\uD83D\uDDF3" // Vote/Box-ish
                                name.lowercase().contains("rocket") -> "\uD83D\uDE80" // Rocket
                                name.lowercase().contains("card") -> "\uD83D\uDCB3" // Card
                                else -> name.take(1)
                            }
                            Text(
                                iconText,
                                color = getBrandColor(name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (showCardLogos) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // Mini card brand indicators
                    Box(modifier = Modifier.size(20.dp, 14.dp).background(Color(0xFF0056B3), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(20.dp, 14.dp).background(Color(0xFFEB001B), RoundedCornerShape(2.dp)))
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PaymentSummaryBottomBar(amount: String) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = Color.Gray, fontSize = 14.sp)
                Text("\u09f3 $amount", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    "\u09f3 $amount",
                    color = Color(0xFFF36F21), // Orange-ish Total
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

fun getBrandColor(name: String): Color {
    val cleanName = name.lowercase()
    return when {
        cleanName.contains("bkash") -> Color(0xFFE2136E)
        cleanName.contains("nagad") -> Color(0xFFF7941D)
        cleanName.contains("rocket") -> Color(0xFF8C3494)
        cleanName.contains("upay") -> Color(0xFF00ADEF)
        cleanName.contains("card") || cleanName.contains("visa") || cleanName.contains("mastercard") -> Color(0xFF0061C1)
        cleanName.contains("ibbl") -> Color(0xFF008000)
        else -> Color(0xFF555555)
    }
}
