package com.example.neurology_project_android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val peerIdState = mutableStateOf<String?>(null)
        val peersState = mutableStateOf<List<String>>(emptyList())

        val signalingClient = SignalingClient("https://videochat-signaling-app.ue.r.appspot.com:443/peerjs?id=3489534895638&token=6789&key=peerjs"
        , this)
        Log.d("MainActivitiy", "SignalingClient should be set")
        enableEdgeToEdge()

        // Fetch peers and update state (excluding own peer ID)
        GetPeers { peers ->
            runOnUiThread {
                peerIdState.value?.let { id ->
                    peersState.value = peers.filter { it != id }
                } ?: run {
                    peersState.value = peers
                }
            }
        }

        setContent {
            NeurologyProjectAndroidTheme {
                GradientBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Make the Scaffold background transparent
                        content = { innerPadding ->
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                peerId = peerIdState.value ?: "Fetching...",
                                peers = peersState.value
                            )                        }
                    )
                }
            }
        }


        /*

       setContent {
           NeurologyProjectAndroidTheme {
               LoginScreen()
           }
       }

         */


    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, peerId: String, peers: List<String>) {
    // Refresh UI every 3 seconds
    LaunchedEffect(peers) {
        // This will trigger recomposition whenever peers update
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PeerIdSection(peerId) // Displays the correct Peer ID

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnlineNowSection(peers) // No need for additional state
        }

        NIHFormsButton()
    }
}




@Composable
fun PeerIdSection(peerId: String) {
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
            Text(text = peerId) // Display dynamic peer ID
        }
    }
}


@Composable
fun OnlineNowSection(peers: List<String>) {
    Text(
        text = "Online Now:",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        if (peers.isEmpty()) {
            Text(text = "No peers online", modifier = Modifier.padding(16.dp))
        } else {
            peers.forEach { userId ->
                OnlineUserCard(userId)
            }
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