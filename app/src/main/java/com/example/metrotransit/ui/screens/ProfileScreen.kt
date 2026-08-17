package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
 import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.MetroTransitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: () -> Unit,
    onUpdatePassword: () -> Unit
) {
    var isBackTriggered by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MRT Portal", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.textPrimary
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBackTriggered) {
                            isBackTriggered = true
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = extendedColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    .padding(bottom = LocalJourneyBarInset.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Profile Header Card (Glass)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Syed Foysal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                        
                        Text(
                            text = "foysalislam76@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verified", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Menu Section (Glass)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.AccountCircle,
                            label = "Update Profile",
                            onClick = onUpdateProfile
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = extendedColors.glassBorder)
                        ProfileMenuItem(
                            icon = Icons.Default.Lock,
                            label = "Change Password",
                            onClick = onUpdatePassword
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = extendedColors.glassBorder)
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Sign Out",
                            isDestructive = true,
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = (if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = extendedColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
