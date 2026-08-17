package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroTransitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareCalculatorScreen(onBack: () -> Unit) {
    var fromStation by remember { mutableStateOf<MetroStation?>(StationData.stations.find { it.name == "Uttara South" } ?: StationData.stations.first()) }
    var toStation by remember { mutableStateOf<MetroStation?>(StationData.stations.find { it.name == "Mirpur 11" } ?: StationData.stations.last()) }
    
    val scrollState = rememberScrollState()
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Fare & Timetable", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = extendedColors.textPrimary)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(bottom = LocalJourneyBarInset.current)
            ) {
                // ── Header (Web style from image) ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🚇", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Dhaka Metro Rail",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "MRT Line-6 Timetable & Fare Calculator",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // ── Selectors ──────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .offset(y = (-30).dp) // Overlap with header
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StationSelectorMinimal(
                                    label = "From Station",
                                    selectedStation = fromStation,
                                    onStationSelected = { fromStation = it },
                                    modifier = Modifier.weight(1f)
                                )
                                StationSelectorMinimal(
                                    label = "To Station",
                                    selectedStation = toStation,
                                    onStationSelected = { toStation = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                if (fromStation != null && toStation != null) {
                    val regularFare = FareCalculator.fare(fromStation, toStation)
                    val mrtFare = (regularFare * 0.9).toInt()
                    val pwdFare = (regularFare * 0.85).toInt()

                    // ── Route & Fare Display ──────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Calculated Route",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${fromStation!!.name} → ${toStation!!.name}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FareCard(
                                    type = "Regular",
                                    subtitle = "Single Journey",
                                    amount = regularFare,
                                    modifier = Modifier.weight(1f)
                                )
                                FareCard(
                                    type = "MRT Pass",
                                    subtitle = "10% discount",
                                    amount = mrtFare,
                                    modifier = Modifier.weight(1f)
                                )
                                FareCard(
                                    type = "PWD",
                                    subtitle = "15% off",
                                    amount = pwdFare,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Timetable ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimetableCard(
                            title = "📅 Regular Days",
                            modifier = Modifier.weight(1f),
                            schedules = listOf(
                                "First train" to "6:40 AM",
                                "Last train" to "9:30 PM",
                                "Peak freq" to "~6 mins",
                                "Off-peak" to "~10 mins"
                            )
                        )
                        TimetableCard(
                            title = "🕌 Friday",
                            modifier = Modifier.weight(1f),
                            schedules = listOf(
                                "First train" to "2:30 PM",
                                "Last train" to "9:30 PM",
                                "Frequency" to "~12 mins",
                                "Note" to "No morning"
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Travel Tips & Fare Info ──────────────────────────────
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚇", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Travel Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = extendedColors.glass,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val tips = listOf(
                                    "Get an MRT Pass for 10% discount and extended late-night hours",
                                    "Avoid peak hours (8–10 AM, 5–7 PM) for less crowding",
                                    "Buy tickets before 8:50 PM for late-night travel without a pass",
                                    "Arrive 5–10 minutes early for security checks"
                                )
                                
                                tips.forEach { tip ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("✓", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(tip, style = MaterialTheme.typography.bodySmall, color = extendedColors.textSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = extendedColors.glass,
                            border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💸", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Fare Information",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.textPrimary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                FareInfoRow("Base fare:", "৳20 minimum")
                                FareInfoRow("Distance-based:", "৳5 per km")
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    "💳 MRT Pass holders get 10% discount | ♿ PWD gets 15% off",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extendedColors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FareInfoRow(label: String, badgeText: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = extendedColors.textSecondary)
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                badgeText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun FareCard(type: String, subtitle: String, amount: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(type, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("৳$amount", color = Color.White, fontFamily = AppFont.display, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TimetableCard(title: String, schedules: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            schedules.forEach { (label, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = extendedColors.textSecondary, fontSize = 10.sp)
                    Text(time, color = extendedColors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (label != schedules.last().first) {
                    HorizontalDivider(color = extendedColors.glassBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectorMinimal(
    label: String,
    selectedStation: MetroStation?,
    onStationSelected: (MetroStation) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedStation?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp, color = extendedColors.textSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = extendedColors.glassBorder,
                focusedTextColor = extendedColors.textPrimary,
                unfocusedTextColor = extendedColors.textPrimary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(extendedColors.surface)
        ) {
            StationData.stations.forEach { station ->
                DropdownMenuItem(
                    text = { Text(station.name, color = extendedColors.textPrimary) },
                    onClick = {
                        onStationSelected(station)
                        expanded = false
                    }
                )
            }
        }
    }
}
