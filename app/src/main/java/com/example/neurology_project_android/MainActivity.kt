package com.example.neurology_project_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signalingClient = SignalingClient("https://videochat-signaling-app.ue.r.appspot.com:443/peerjs?id=3489534895638&token=6789&key=peerjs"
        , this)
        Log.d("MainActivitiy", "SignalingClient should be set")
        enableEdgeToEdge()
        /*
        setContent {
            NeurologyProjectAndroidTheme {
                GradientBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Make the Scaffold background transparent
                        content = { innerPadding ->
                            HomeScreen(modifier = Modifier.padding(innerPadding))
                        }
                    )
                }
            }
        }

         */

        setContent {
            NeurologyProjectAndroidTheme {
                LoginScreen()
            }
        }


    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Your Peer ID Section
        PeerIdSection()

        // Online Now Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnlineNowSection()
        }

        // NIH Forms Button
        NIHFormsButton()
    }
}

@Composable
fun PeerIdSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
    shape = RoundedCornerShape(8.dp)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Your Peer ID:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "2E1D13AC-02BE-41C6-A4FD-BE4768A3E2F1") // Placeholder ID
        }
    }
}

@Composable
fun OnlineNowSection() {
    Text(
        text = "Online Now:",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // Example list of online users
    val onlineUsers = listOf(
        "2B07C9B0-3ED2-4475-B91E-D51598284689",
        "4B0720D5-9D6C-4A9E-B266-B5F57D9023FC",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "D336DD45-055C-4019-A035-C2AF8D0CD009",
        "E60A7900-6FE9-4F34-A7DE-8B1AB54E6979"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        onlineUsers.forEach { userId ->
            OnlineUserCard(userId)
        }
    }
}

@Composable
fun OnlineUserCard(userId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userId,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Button(
                onClick = { /* Placeholder for Call button */ },
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(text = "Call")
            }
        }
    }
}

@Composable
fun NIHFormsButton() {
    Button(
        onClick = { /* Placeholder for NIH Forms action */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = "NIH Forms")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NeurologyProjectAndroidTheme {
        val navController = rememberNavController()
    }
}