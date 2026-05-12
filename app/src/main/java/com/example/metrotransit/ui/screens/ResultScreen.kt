package com.example.metrotransit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.metrotransit.data.TrainSchedule
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    fromId: Int,
    toId: Int,
    onBack: () -> Unit,
    viewModel: ResultViewModel = viewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    LaunchedEffect(fromId, toId) {
        viewModel.initData(fromId, toId)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "STATION MONITOR", 
                        color = Color.White, 
                        letterSpacing = 1.5.sp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            // Header Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROUTE",
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${viewModel.fromStation?.name} \u2192 ${viewModel.toStation?.name}",
                        color = Color(0xFFFFD700), // Gold
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CURRENT TIME",
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTime,
                        color = Color(0xFF39FF14), // Neon Green
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF006A4E)) // MRT Brand Green
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "ESTIMATED TRAVEL TIME: ${viewModel.estimatedTime} MIN (${viewModel.stationCount} STATIONS)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(14.dp)
            ) {
                Text("DESTINATION", Modifier.weight(2.3f), color = Color(0xFFAAAAAA), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("PLATFORM", Modifier.weight(1f), color = Color(0xFFAAAAAA), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, letterSpacing = 1.sp)
                Text("DEPARTURE", Modifier.weight(1.2f), color = Color(0xFFAAAAAA), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, letterSpacing = 1.sp)
            }

            // Train List
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(Color(0xFF050505), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                ) {
                    items(viewModel.trains) { train ->
                        TrainRow(train)
                        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFF39FF14).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "NETWORK STATUS: OPERATIONAL",
                    color = Color(0xFF39FF14),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun TrainRow(train: TrainSchedule) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = train.destination.uppercase(),
            modifier = Modifier.weight(2.3f),
            color = Color(0xFF39FF14), // Neon Green
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = train.platform.toString(),
            modifier = Modifier.weight(1f),
            color = Color(0xFFFFD700), // Gold
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            text = train.departureTime,
            modifier = Modifier.weight(1.2f),
            color = Color(0xFF39FF14), // Neon Green
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End
        )
    }
}
