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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.PaymentMethod
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.MRTPassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelectionScreen(
    amount: String,
    viewModel: MRTPassViewModel,
    onBack: () -> Unit,
    onMethodSelected: (PaymentMethod) -> Unit,
    /** What a cancelled, declined or timed-out gateway session left behind. */
    gatewayNotice: String? = null
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = extendedColors.textPrimary) },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = extendedColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            PaymentSummaryBottomBar(amount)
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
                    .padding(bottom = LocalJourneyBarInset.current)
            ) {
                // An unpaid session outranks the voucher pitch, so it goes above it.
                if (gatewayNotice != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                gatewayNotice,
                                color = extendedColors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Voucher Banner (Glass variant)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Collect payment voucher & get extra savings on your purchase!",
                            color = extendedColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Every method in one list. Splitting them into "recommended" and "other",
                // or by wallet vs card, only asked the rider to read headings before finding
                // the one mark they were already looking for.
                val methods = viewModel.paymentMethods

                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column {
                        methods.forEachIndexed { index, method ->
                            PaymentMethodItem(
                                name = method.name,
                                // The gateway row is not a channel, so it says what it opens
                                // rather than leaving its name to stand on its own.
                                subtitle = if (method.type == "Gateway") {
                                    "Cards · Mobile Banking · Internet Banking"
                                } else {
                                    null
                                },
                                // The card row stands for every scheme, so it carries the marks.
                                showCardLogos = method.type == "Card",
                                isInsideContainer = true,
                                onClick = { onMethodSelected(method) }
                            )
                            if (index < methods.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = extendedColors.glassBorder
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer Logos
                Text(
                    text = "Secure Payments Powered by LEADS",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 10.sp,
                    color = extendedColors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    name: String,
    subtitle: String? = null,
    showCardLogos: Boolean = false,
    isInsideContainer: Boolean = false,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val content = @Composable {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // One tile for every mark, whatever the artwork behind it.
            PaymentBrandTile(brand = paymentBrandFor(name), fallbackName = name)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = extendedColors.textSecondary
                    )
                }
            }

            if (showCardLogos) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Box(modifier = Modifier.size(24.dp, 16.dp).background(Color(0xFF0056B3), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(24.dp, 16.dp).background(Color(0xFFEB001B), RoundedCornerShape(2.dp)))
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = extendedColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (isInsideContainer) {
        Box(modifier = Modifier.clickable { onClick() }) {
            content()
        }
    } else {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            color = extendedColors.glass,
            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
        ) {
            content()
        }
    }
}

@Composable
fun PaymentSummaryBottomBar(amount: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        color = extendedColors.surface,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Payable", style = MaterialTheme.typography.labelMedium, color = extendedColors.textSecondary)
                    Text(
                        "৳ $amount",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Button(
                    onClick = { /* Implicit action from parent */ },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp).width(140.dp)
                ) {
                    Text("Proceed", fontWeight = FontWeight.Bold)
                }
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
        else -> Color(0xFF3269B5)
    }
}
