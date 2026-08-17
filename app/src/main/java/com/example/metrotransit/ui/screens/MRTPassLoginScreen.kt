package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.MRTPassViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MRTPassLoginScreen(
    onLoginSuccess: () -> Unit,
    onOpenWebView: () -> Unit,
    onBack: () -> Unit,
    viewModel: MRTPassViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val extendedColors = MetroTransitTheme.extendedColors

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("MRT Pass Portal", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            // Decorative background elements for glass effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = 100.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 50.dp, y = (-100).dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = LocalJourneyBarInset.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Glass Form Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = extendedColors.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login to Portal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.textPrimary
                        )
                        Text(
                            text = "Manage your MRT Pass",
                            style = MaterialTheme.typography.bodySmall,
                            color = extendedColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            label = { Text("Email or Mobile Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = extendedColors.textSecondary.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = extendedColors.textPrimary,
                                unfocusedTextColor = extendedColors.textPrimary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.passwordVisible = !viewModel.passwordVisible }) {
                                    Icon(
                                        imageVector = if (viewModel.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = extendedColors.textSecondary.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = extendedColors.textPrimary,
                                unfocusedTextColor = extendedColors.textPrimary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot password?",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .clickable { /* Dummy */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (viewModel.email.isEmpty() || viewModel.password.isEmpty()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Fields cannot be empty")
                                    }
                                } else if (viewModel.login()) {
                                    onLoginSuccess()
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Invalid credentials (Demo: 1 / 1)")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row {
                            Text("Don't have an account? ", color = extendedColors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Sign Up",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable { /* Dummy */ }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onOpenWebView,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Official Website", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
