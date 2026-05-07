package com.example.metrotransit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.MRTPassCard
import com.example.metrotransit.viewmodel.MRTPassViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MRTPassDashboardScreen(
    onRecharge: (MRTPassCard) -> Unit,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onShowHistory: () -> Unit,
    viewModel: MRTPassViewModel
) {
    val cards by viewModel.cards.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = Color(0xFFF1F5F9),
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                // Drawer Header with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF3269B5), Color(0xFF1E3A8A))
                            )
                        )
                        .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Syed Foysal",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "syed.foysal@example.com",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Drawer Items
                val itemModifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .height(56.dp)

                NavigationDrawerItem(
                    label = { Text("My Cards", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    modifier = itemModifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Recharge History", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onShowHistory()
                        } 
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = itemModifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("MRT Portal", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onShowProfile()
                        } 
                    },
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    modifier = itemModifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                NavigationDrawerItem(
                    label = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onLogout()
                        } 
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = itemModifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1E293B))
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { /* Toggle Language */ },
                            color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("\uD83C\uDF10 ", fontSize = 14.sp)
                                Text("\u09ac\u09be\u0982\u09b2\u09be", color = Color(0xFF3269B5), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        IconButton(onClick = onShowProfile) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color(0xFF3269B5),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F5F9),
                                Color(0xFFE2E8F0),
                                Color(0xFFCBD5E1)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Surface(
                                color = Color(0xFF3269B5).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF3269B5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "A maximum of five cards can be registered from each account.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        items(cards) { card ->
                            EnhancedCardItem(
                                card = card,
                                onSeeDetails = { /* Dummy */ },
                                onRecharge = { onRecharge(card) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.5f),
                                    contentColor = Color(0xFF64748B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedCardItem(
    card: MRTPassCard,
    onSeeDetails: () -> Unit,
    onRecharge: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Physical Card Visual Simulation
            PhysicalCardVisual()
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                DetailLine("Name", card.cardName, isBoldValue = true)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.5f))
                DetailLine("Card Number", card.cardNumber)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.5f))
                DetailLine("Card Status", card.status)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.5f))
                DetailLine("Balance", "\u09f3 ${String.format(Locale.US, "%.2f", card.balance)}")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    color = Color(0xFF3269B5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onSeeDetails() }
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF3269B5))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onRecharge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3269B5), Color(0xFF5A67D8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Recharge Now", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailLine(label: String, value: String, isBoldValue: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF64748B))
        Text(
            text = value, 
            fontSize = 14.sp,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}
