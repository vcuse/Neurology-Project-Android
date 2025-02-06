package com.example.neurology_project_android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun LoginScreen() {
    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login Header
                    Text(
                        text = "Login",
                        fontSize = TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp), // Explicitly define font size as SP
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your credentials to access your account",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Input
                    androidx.compose.material3.OutlinedTextField(
                        value = "",
                        onValueChange = { /* Handle input */ },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Password Input
                    androidx.compose.material3.OutlinedTextField(
                        value = "",
                        onValueChange = { /* Handle input */ },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { /* Handle show/hide password */ }) {
                                androidx.compose.material3.Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_visibility),
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Button
                    androidx.compose.material3.Button(
                        onClick = { /* Handle Sign In */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Sign In")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Create Account Button
                    androidx.compose.material3.TextButton(
                        onClick = { /* Navigate to Create Account */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Create Account", color = Color.Black)
                    }
                }
            }
        }
    }
}
