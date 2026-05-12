package com.example.metrotransit.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.nfc.NfcManager
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onShowTrains: (Int, Int) -> Unit,
    onViewStations: () -> Unit,
    onNavigateToMRTPass: () -> Unit,
    onNavigateToNFCResult: () -> Unit,
    onNavigateToFareCalculator: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val activity = context as Activity
    val nfcManager = remember { NfcManager(activity) }
    
    val extendedColors = MetroTransitTheme.extendedColors
    
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.isLocating = true
        }
    }

    if (viewModel.isLocating) {
        LaunchedEffect(Unit) {
            try {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val location = locationClient.lastLocation.await()
                    if (location != null) {
                        kotlinx.coroutines.delay(1500)
                        viewModel.findNearestStation(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                viewModel.isLocating = false
            }
        }
    }

    if (viewModel.showScanSheet) {
        DisposableEffect(Unit) {
            nfcManager.startScanning(
                onScanningStatusChange = { viewModel.isScanning = it },
                onResponseRead = { response ->
                    viewModel.processNfcResponse(response)
                },
                onError = { error ->
                    viewModel.scanError = error
                }
            )
            onDispose {
                nfcManager.stopScanning()
            }
        }
    }

    val isScanning = viewModel.isScanning
    val hasResult = viewModel.scannedBalance != null || viewModel.scanError != null

    LaunchedEffect(isScanning, hasResult) {
        if (!isScanning && hasResult && viewModel.showScanSheet) {
            viewModel.showScanSheet = false
            onNavigateToNFCResult()
        }
    }

    if (viewModel.showScanSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.showScanSheet = false
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
                    viewModel.showScanSheet = false
                    viewModel.resetScan()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "MetroTransit BD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = extendedColors.textPrimary
                            )
                        )
                        Text(
                            "Dhaka Metro Rail",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textSecondary
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            ) {
                // ── Station selector card (Glass Effect) ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = extendedColors.glass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
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
                                    onStationSelected = { viewModel.setFrom(it) },
                                    modifier = Modifier.weight(1f),
                                    isLocating = viewModel.isLocating
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            viewModel.isLocating = true
                                        } else {
                                            permissionLauncher.launch(arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            ))
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    if (viewModel.isLocating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = "Find Nearest",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { 
                                        viewModel.swapStations()
                                        rotationAngle += 180f
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(extendedColors.glass)
                                        .border(1.dp, extendedColors.glassBorder, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = "Swap",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .graphicsLayer { rotationZ = animatedRotation },
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                JourneyPoint(
                                    icon = Icons.Default.LocationOn,
                                    color = Color(0xFF10B981)
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

                Spacer(modifier = Modifier.height(8.dp))

                // ── Show trains button (Modern Gradient) ───────────────────────
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Find Next Trains", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Services & Portal",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )

                // ── Info cards (Glass Effect) ──────────────────────────────────
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
                        title = "Full Journey",
                        value = "~35 min",
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── MRT Pass Portal card (Glass Effect) ────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder),
                    onClick = onNavigateToMRTPass
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF006A4E).copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color(0xFF006A4E),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "MRT Pass Portal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textPrimary
                                )
                                Text(
                                    "Manage cards & recharges",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extendedColors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(extendedColors.textSecondary.copy(alpha = 0.1f))
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CardFeatureItem(icon = Icons.Default.AccountBalanceWallet, label = "Balance", themeColor = Color(0xFF006A4E))
                            CardFeatureItem(icon = Icons.Default.AddCard, label = "Recharge", themeColor = Color(0xFF006A4E))
                            CardFeatureItem(icon = Icons.AutoMirrored.Filled.FactCheck, label = "Status", themeColor = Color(0xFF006A4E))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToMRTPass,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A4E))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Access Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Metro Buddy / NFC card (Glass Effect) ──────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF5E42F3).copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.Contactless,
                                    contentDescription = null,
                                    tint = Color(0xFF5E42F3),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Metro Buddy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textPrimary
                                )
                                Text(
                                    "Scan physical card via NFC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extendedColors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(extendedColors.textSecondary.copy(alpha = 0.1f))
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CardFeatureItem(icon = Icons.Default.AccountBalanceWallet, label = "Read Card", themeColor = Color(0xFF5E42F3))
                            CardFeatureItem(icon = Icons.Default.History, label = "History", themeColor = Color(0xFF5E42F3))
                            CardFeatureItem(icon = Icons.AutoMirrored.Filled.TrendingUp, label = "Insights", themeColor = Color(0xFF5E42F3))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.resetScan()
                                viewModel.showScanSheet = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E42F3))
                        ) {
                            Icon(Icons.Default.Contactless, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tap Card to Scan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Fare Calculator card (Glass Effect) ──────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder),
                    onClick = onNavigateToFareCalculator
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Fare Calculator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.textPrimary
                                )
                                Text(
                                    "Check timetable & journey fare",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extendedColors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToFareCalculator,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Check Schedule & Fare", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            if (isScanning)
                "Hold the card still while we read it"
            else
                "Hold your phone near your transit card",
            style = MaterialTheme.typography.bodyMedium,
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
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CardFeatureItem(icon: ImageVector, label: String, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = themeColor,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(6.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MetroTransitTheme.extendedColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
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
    onStationSelected: (MetroStation) -> Unit,
    modifier: Modifier = Modifier,
    isLocating: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = if (isLocating) "Detecting nearby station..." else (selectedStation?.name ?: ""),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select Station", color = extendedColors.textSecondary) },
            label = { 
                Text(
                    label, 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expanded) MaterialTheme.colorScheme.primary else extendedColors.textSecondary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            supportingText = null,
            trailingIcon = { 
                if (!isLocating) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (expanded) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = (if (isLocating) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge).copy(
                fontWeight = FontWeight.Bold,
                color = if (isLocating) MaterialTheme.colorScheme.primary else if (selectedStation == null) extendedColors.textSecondary else extendedColors.textPrimary
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(extendedColors.surface)
                    .border(1.dp, extendedColors.glassBorder, RoundedCornerShape(16.dp))
            ) {
                StationData.stations.forEach { station ->
                    val isSelected = selectedStation?.id == station.id
                    DropdownMenuItem(
                        text = { 
                            Text(
                                station.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textPrimary
                            ) 
                        },
                        leadingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else extendedColors.textSecondary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Train,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onStationSelected(station)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (station != StationData.stations.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = extendedColors.glassBorder
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = extendedColors.textSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = extendedColors.textPrimary)
        }
    }
}
