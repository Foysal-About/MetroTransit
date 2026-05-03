package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhysicalCardVisual(type: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
    ) {
        if (type == "MRT") {
            // MRT Pass Style
            Column {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
                ) {
                    // Scenic background simulation
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Icons at top left
                            Box(modifier = Modifier.size(16.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(16.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(16.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
                        }
                    }
                    Text(
                        "MRT Pass", 
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        "MRT Pass Compatible", 
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 36.dp, end = 12.dp),
                        color = Color(0xFFD32F2F),
                        fontSize = 10.sp
                    )
                    // Simplified National Martyrs' Memorial drawing
                    Box(modifier = Modifier.align(Alignment.BottomCenter).size(100.dp, 60.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.5f)))))
                        Text("\u26F2", fontSize = 40.sp) // Fountain/Monument emoji
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF006A4E))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(100.dp), color = Color.White, modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("\uD83C\uDDE7\uD83C\uDDE9", fontSize = 12.sp) 
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Dhaka Mass Transit Company Limited", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Road Transport and Highways Division", color = Color.White, fontSize = 8.sp)
                        }
                    }
                }
            }
        } else {
            // MRT Pass Style
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F8E9))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) { Box(modifier = Modifier.size(12.dp).background(Color(0xFF1976D2), RoundedCornerShape(2.dp))) }
                        }
                        Text("One Card for All Transport", fontSize = 8.sp, color = Color(0xFF1976D2))
                    }
                    Text("MRT Pass", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83D\uDE86 \uD83D\uDE8C \u26F4", fontSize = 40.sp)
                }
                
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFF1976D2)))
            }
        }
    }
}
