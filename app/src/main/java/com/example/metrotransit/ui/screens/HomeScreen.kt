package com.example.metrotransit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onShowTrains: (Int, Int) -> Unit,
    onViewStations: () -> Unit,
    onNavigateToMRTPass: () -> Unit,
    onNavigateToNFCResult: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    var showNFCSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // When a scan completes (isScanning goes false AND we have a balance),
    // close the bottom sheet and navigate to the results screen.
    // This replaces the old activity?.lastScannedTag polling approach.
    val isScanning       = viewModel.isScanning
    val hasResult        = viewModel.scannedBalance != null || viewModel.scanError != null

    LaunchedEffect(isScanning, hasResult) {
        if (!isScanning && hasResult && showNFCSheet) {
            showNFCSheet = false
            onNavigateToNFCResult()
        }
    }

    if (showNFCSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showNFCSheet = false
                viewModel.resetScan()
            },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            NFCScanBottomSheetContent(
                isScanning = isScanning,
                onCancel = {
                    showNFCSheet = false
                    viewModel.resetScan()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "MetroTransit BD",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Dhaka Metro Rail",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onViewStations) {
                        Icon(
                            Icons.Default.Train,
                            contentDescription = "Stations",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // ── Station selector card ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JourneyPoint(
                                icon = Icons.Default.MyLocation,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            StationSelector(
                                label = "From Station",
                                selectedStation = viewModel.fromStation,
                                onStationSelected = { viewModel.setFrom(it) }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(2.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.swapStations() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JourneyPoint(
                                icon = Icons.Default.LocationOn,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            StationSelector(
                                label = "To Station",
                                selectedStation = viewModel.toStation,
                                onStationSelected = { viewModel.setTo(it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Show trains button ─────────────────────────────────────────
            Button(
                onClick = {
                    val fromId = viewModel.fromStation?.id ?: 0
                    val toId   = viewModel.toStation?.id   ?: 0
                    if (fromId != toId) onShowTrains(fromId, toId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Next Trains", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "MRT Line-6 Route",
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // ── Info cards ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    title = "Stations",
                    value = StationData.stations.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    title = "Duration",
                    value = "~35 min",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── MRT Pass card ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = onNavigateToMRTPass
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF006A4E).copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Train,
                                contentDescription = null,
                                tint = Color(0xFF006A4E),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "MRT Pass Portal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Manage your cards online",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8E9))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MRTPassFeatureItem(icon = "\uD83D\uDCB3", label = "Balance")
                        MRTPassFeatureItem(icon = "\uD83D\uDCAF", label = "Recharge")
                        MRTPassFeatureItem(icon = "\u2705",       label = "Manage")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToMRTPass,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A4E))
                    ) {
                        Text("Sign In to Manage Card", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Metro Buddy / NFC card ─────────────────────────────────────
            MetroBuddyCard(onScanClick = {
                viewModel.resetScan()   // clear any previous result before new scan
                showNFCSheet = true
            })

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Metro Buddy card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MetroBuddyCard(onScanClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Contactless,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Metro Buddy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Check balance & travel history with NFC",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetroBuddyFeatureItem(icon = Icons.Default.AccountBalanceWallet, label = "Balance")
                MetroBuddyFeatureItem(icon = Icons.AutoMirrored.Filled.ReceiptLong, label = "Last 19 Trips")
                MetroBuddyFeatureItem(icon = Icons.Default.Analytics, label = "Insights")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(
                    Icons.Default.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tap Card to Scan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NFC bottom sheet — now shows a spinner while the card is being read
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NFCScanBottomSheetContent(
    isScanning: Boolean,
    onCancel: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("nfcLottie.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            if (isScanning) "Reading Card…" else "Ready to Scan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            if (isScanning)
                "Hold the card still while we read it"
            else
                "Hold your phone near your transit card",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp
                )
            } else {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCancel,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text("Cancel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small reusable composables
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MetroBuddyFeatureItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MRTPassFeatureItem(icon: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun JourneyPoint(icon: ImageVector, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.size(26.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelector(
    label: String,
    selectedStation: MetroStation?,
    onStationSelected: (MetroStation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedStation?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            StationData.stations.forEach { station ->
                DropdownMenuItem(
                    text = { Text(station.name) },
                    onClick = {
                        onStationSelected(station)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}