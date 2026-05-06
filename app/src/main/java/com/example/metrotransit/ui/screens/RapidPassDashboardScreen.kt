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
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3269B5))
                        .padding(vertical = 40.dp, horizontal = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Syed Foysal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "syed.foysal@example.com",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drawer Items
                NavigationDrawerItem(
                    label = { Text("My Cards") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Recharge History") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onShowHistory()
                        } 
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("MRT Portal") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onShowProfile()
                        } 
                    },
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                NavigationDrawerItem(
                    label = { Text("Sign Out") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onLogout()
                        } 
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = { /* Toggle Language */ }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83C\uDF10 ", fontSize = 14.sp)
                                Text("\u09ac\u09be\u0982\u09b2\u09be", color = Color(0xFF0056B3), fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onShowProfile) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color(0xFF5A67D8), modifier = Modifier.size(32.dp))
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
                    .background(Color.White)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text(
                            text = "A maximum of five cards can be registered from each account.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
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
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign Out")
                            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Physical Card Visual Simulation
            PhysicalCardVisual(type = card.type)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Text Details matching screenshot
            DetailLine("Name", card.cardName, isBoldValue = true)
            DetailLine("Card Number", card.cardNumber)
            DetailLine("Card Status", card.status)
            DetailLine("Balance", "\u09f3 ${String.format(Locale.US, "%.2f", card.balance)}")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "See Details",
                color = Color(0xFF0056B3),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeDetails() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRecharge,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3269B5))
            ) {
                Text("Recharge", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailLine(label: String, value: String, isBoldValue: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "$label: ", fontSize = 14.sp, color = Color.Gray)
        Text(
            text = value, 
            fontSize = 14.sp,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            color = Color.DarkGray
        )
    }
}
