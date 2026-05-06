package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: () -> Unit,
    onUpdatePassword: () -> Unit
) {
    val cyanColor = Color(0xFF00ACC1)
    var isBackTriggered by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MRT Portal", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBackTriggered) {
                            isBackTriggered = true
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Profile Image Placeholder
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = Color(0xFF1976D2) // Standard blue for profile icon
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name
            Text(
                text = "Syed Foysal",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(24.dp))

            // Phone Number
            Text(
                text = "01876069132",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = cyanColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email with verified badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "foysalislam76@gmail.com",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyanColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = cyanColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(40.dp))

            // Menu Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    label = "Update Profile",
                    iconColor = cyanColor,
                    onClick = onUpdateProfile
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    label = "Update Password",
                    iconColor = cyanColor,
                    onClick = onUpdatePassword
                )
                ProfileMenuItem(
                    icon = Icons.Default.PowerSettingsNew,
                    label = "Sign Out",
                    iconColor = cyanColor,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
