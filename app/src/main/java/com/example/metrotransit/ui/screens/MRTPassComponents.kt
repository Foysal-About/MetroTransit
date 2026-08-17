package com.example.metrotransit.ui.screens

import com.example.metrotransit.ui.theme.AppFont
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhysicalCardVisual() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Sky Section ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF4A90E2), Color(0xFF87CEEB), Color.White)
                        )
                    )
            ) {
                // Cloud-like patterns (Simplified)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = 100f, center = Offset(100f, 100f))
                    drawCircle(Color.White.copy(alpha = 0.2f), radius = 150f, center = Offset(400f, 50f))
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = 120f, center = Offset(800f, 80f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Top Left Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        CardSmallIcon(Icons.Default.Train)
                        CardSmallIcon(Icons.Default.DirectionsBus)
                        CardSmallIcon(Icons.Default.DirectionsBoat)
                        CardSmallIcon(Icons.Default.ShoppingCart)
                    }

                    // Top Right Text
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "MRT Pass",
                            color = Color(0xFFD32F2F),
                            fontSize = 24.sp,
                            fontFamily = AppFont.display,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Rapid Pass Compatible",
                            color = Color(0xFFD32F2F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Landmarks Silhouette (Simplified)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(60.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height)
                            // Shaheed Minar approx
                            lineTo(size.width * 0.1f, size.height)
                            lineTo(size.width * 0.15f, size.height * 0.4f)
                            lineTo(size.width * 0.2f, size.height)
                            
                            // National Memorial approx
                            lineTo(size.width * 0.4f, size.height)
                            lineTo(size.width * 0.45f, size.height * 0.2f)
                            lineTo(size.width * 0.5f, size.height)
                            
                            // Parliament approx
                            lineTo(size.width * 0.65f, size.height)
                            lineTo(size.width * 0.7f, size.height * 0.5f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.85f, size.height)
                            
                            lineTo(size.width, size.height)
                        }
                        drawPath(path, Color.Gray.copy(alpha = 0.2f))
                    }
                }
            }

            // ── Bottom Green Section ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .background(Color(0xFF006A4E)) // Bangladesh Green
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Govt Logo Placeholder
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🇧🇩", fontSize = 14.sp)
                        }
                    }

                    // Company Name
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Dhaka Mass Transit Company Limited",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Road Transport and Highways Division",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp
                        )
                    }

                    // DMTCL Logo Placeholder
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🚇", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        
        // Glass Reflection Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = 0.1f),
                        0.5f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = 0.05f)
                    )
                )
        )
    }
}

@Composable
fun CardSmallIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(20.dp),
        shape = RoundedCornerShape(2.dp),
        color = Color.White.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(2.dp),
            tint = Color.Black
        )
    }
}
