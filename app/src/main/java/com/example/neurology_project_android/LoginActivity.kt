
package com.example.neurology_project_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.neurology_project_android.BuildConfig.API_POST_URL
import com.example.neurology_project_android.BuildConfig.BASE_API_URL
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        // If user already logged in, skip login screen
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            LoginScreen(sessionManager, onLoginSuccess = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
        }
    }
}

@Composable
fun LoginScreen(sessionManager: SessionManager, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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
                    Text("Login", fontSize = TextUnit(24f, TextUnitType.Sp), modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    if (error != null) {
                        Text(text = error ?: "", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            error = null

                            val client = OkHttpClient()
                            val json = """
                                {
                                    "username": "$username",
                                    "password": "$password"
                                }
                            """.trimIndent()

                            val requestBody = RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                json
                            )
                            val postURL = API_POST_URL
                            Log.d("LoginActivity", "Post URL: " + postURL)
                            val request = Request.Builder()
                                .url(postURL)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Action", "login")
                                .post(requestBody)
                                .build()

                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    isLoading = false
                                    error = "Network error: ${e.message}"
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        val token = response.body?.string()?.trim() ?: ""
                                        var authToken = response.headers.value(9).substringAfter("authorization=")
                                        authToken = authToken.substringBefore(";")

                                        sessionManager.saveAuthToken(authToken.toString(), username)
                                        (context as ComponentActivity).runOnUiThread {
                                            onLoginSuccess()
                                        }
                                    } else {
                                        error = "Login failed: ${response.code}"
                                    }
                                }
                            })
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (isLoading) "Signing In..." else "Sign In")
                    }

                    TextButton(
                        onClick = { /* Handle create account */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Account", color = Color.Black)
                    }
                }
            }
        }
    }
}

