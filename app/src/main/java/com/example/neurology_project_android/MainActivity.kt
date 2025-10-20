package com.example.neurology_project_android



import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.imagecapture.CameraRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {


    @SuppressLint("RestrictedApi")
    private lateinit var cameraRequest: CameraRequest
//    private lateinit var videoProcessor: VideoProcessor
//    private lateinit var videoSource: VideoSource
//    private lateinit var capturerObserver: CapturerObserver
    private var isInCall by mutableStateOf(false)
    private var cameraInitialized by mutableStateOf(false)
    private lateinit var signalingClient: SignalingClient



    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("${applicationContext.packageName}.USB_PERMISSION"),
            PendingIntent.FLAG_IMMUTABLE
        )

        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                Manifest.permission.RECORD_AUDIO
            ), 1
        )

        

        enableEdgeToEdge()

        setContent {
            NeurologyProjectAndroidTheme {
                val userIdState = remember { mutableStateOf<String?>(null) }
                val peersState = remember { mutableStateOf<List<String>>(emptyList()) }

                // Fetch user ID once
                LaunchedEffect(Unit) {
                    val fetchedId = fetchUserId()
                    userIdState.value = fetchedId

                     //Now safe to start SignalingClient
                    signalingClient = SignalingClient(
                        this@MainActivity,

                        { peers ->
                            runOnUiThread {
                                peersState.value = peers.filter { it != fetchedId }
                            }
                        }

                    )

//                    // Fetch peers
//                    GetPeers { peers ->
//                        runOnUiThread {
//                            peersState.value = peers.filter { it != fetchedId }
//                        }
//                    }
                }

                val userId = userIdState.value

                if (userId == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        content = {
                        innerPadding ->

                            myApp(peersState.value, innerPadding)
                        }


                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun OnlineNowSection(peers: List<String>,  onNavigateToOnlineScreen: () -> Unit) {


//    LaunchedEffect(isInCall) {
//        if (isInCall) {
//            navController.navigate("callScreen")
//        } else {
//            navController.navigate("home") // Navigate back when call ends
//        }
//    }





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
                    OnlineUserCard(userId, onNavigateToOnlineScreen)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun OnlineUserCard(userId: String, onNavigateToOnlineScreen: () -> Unit) {
        var navController = rememberNavController()
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
                    onClick = {

                        signalingClient.joinRoom(userId)
                        onNavigateToOnlineScreen()
                    }, //signalingClient.startCall(userId)
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(text = "Call")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun myApp(peers: List<String>, innerPadding: PaddingValues) {

        var navController = rememberNavController()

        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onNavigateToProfile = { navController.navigate("callScreen") },
                    modifier = Modifier.padding(innerPadding),
                    peerId = "VR CLIENT",
                    peers = peers
                )

                // A simple loading/home screen
                //Greeting()
            }
            composable("callScreen") {
                CallScreen()
            }
        }

    }



    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun HomeScreen(modifier: Modifier = Modifier, peerId: String, peers: List<String>, onNavigateToProfile: () -> Unit) {
        val context = LocalContext.current


        val sessionManager = remember { SessionManager(context) }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        sessionManager.logout()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PeerIdSection("VR CLIENT") // Displays the correct Peer ID

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnlineNowSection(peers,onNavigateToProfile) // No need for additional state
            }

            NIHFormsButton()
        }
    }


}

suspend fun fetchUserId(): String {
    return withContext(Dispatchers.IO) {
        try {
            val idUrl = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/id"
            val client = OkHttpClient()
            val request = Request.Builder().url(idUrl).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "unknown"
            } else {
                "unknown"
            }
        } catch (e: Exception) {
            "unknown"
        }
    }
}




@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    signalingClient: SignalingClient,
    cameraInitialized: Boolean,
    @SuppressLint("RestrictedApi") cameraRequest: () -> CameraRequest,
    isInCall: Boolean
) {


//    LaunchedEffect(isInCall) {
//        if (isInCall) {
//            navController.navigate("callScreen")
//        } else {
//            navController.navigate("home") // Navigate back when call ends
//        }
//    }



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
fun NIHFormsButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(context, ListNIHFormActivity::class.java)
            context.startActivity(intent)
        },
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
        //Greeting("Android", signalingClient = signalingClient)
    }
}