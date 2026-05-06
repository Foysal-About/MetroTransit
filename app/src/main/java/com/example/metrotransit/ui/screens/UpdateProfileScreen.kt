package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("Syed") }
    var lastName by remember { mutableStateOf("Foysal") }
    var email by remember { mutableStateOf("foysalislam76@gmail.com") }
    var isBackTriggered by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // First Name Field
            LabelWithAsterisk("First Name")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1976D2)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Last Name Field
            LabelWithAsterisk("Last Name")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1976D2)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Field with Verified Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabelWithAsterisk("Email")
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Verified",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Email usually read-only if verified in this web UI
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.LightGray,
                    disabledTextColor = Color.DarkGray,
                    disabledContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Image Section
            Text(
                text = buildAnnotatedString {
                    append("Image ")
                    withStyle(style = SpanStyle(color = Color(0xFF00ACC1), fontSize = 12.sp)) {
                        append("(image size should be max 200 KB)")
                    }
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mock File Chooser for Mobile
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                onClick = { /* Open Image Picker */ },
                color = Color.White
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEEEEEE),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(1.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text("Choose File", fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("No file chosen", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Image Preview (Cartoonish silhouette from image)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF455A64)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Update Button
            Button(
                onClick = onUpdateSuccess,
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("UPDATE", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun LabelWithAsterisk(label: String) {
    Text(
        text = buildAnnotatedString {
            append(label)
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.DarkGray
    )
}
