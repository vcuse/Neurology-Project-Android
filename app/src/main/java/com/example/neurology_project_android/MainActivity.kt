package com.example.neurology_project_android


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.callback.IEncodeDataCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.usb.USBMonitor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.webrtc.Camera1Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.NV21Buffer
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    private lateinit var multiCameraClient: MultiCameraClient
    private lateinit var camera: CameraUVC
    private lateinit var cameraRequest: CameraRequest
    private lateinit var videoProcessor: VideoProcessor
    private lateinit var videoSource: VideoSource
    private lateinit var capturerObserver: CapturerObserver
    private var cameraInitialized by mutableStateOf(false)

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

        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)

        val userId = fetchUserId()
        val peerIdState = mutableStateOf<String?>(userId)
        val peersState = mutableStateOf<List<String>>(emptyList())

        val signalingClient = SignalingClient(
            "https://videochat-signaling-app.ue.r.appspot.com:443/peerjs?id=$userId&token=6789&key=peerjs",
            this
        )
        Log.d("MainActivitiy", "SignalingClient should be set")
        //videoSource = signalingClient.getVideoSource()

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

        enableEdgeToEdge()
        setContent {
            NeurologyProjectAndroidTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent, // Make the Scaffold background transparent
                    content = { innerPadding ->
                        // Suppose you have the camera & request object set up
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            peerId = peerIdState.value ?: userId,
                            peers = peersState.value
                        )
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding),
                            signalingClient = signalingClient,
                            cameraInitialized = cameraInitialized,
                            camera = { camera }, cameraRequest = { cameraRequest }
                        )
                    })
            }
        }
    }
}

fun fetchUserId(): String {
    val idUrl = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/id"
    val client = OkHttpClient()
    var id = "123"
    var requestReceived = false

    val request = Request.Builder().url(idUrl).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            android.util.Log.d("Request", "Request failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    id = body
                    requestReceived = true
                }
            } else {
                android.util.Log.d("Response", "Request failed: ${response.code}")
            }
        }
    })
    while (!requestReceived) {
        /* Unsure how to make program wait until id is received
           This works for now, but I am sure there are better ways
         */
    }
    return id
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    signalingClient: SignalingClient,
    cameraInitialized: Boolean,
    camera: () -> CameraUVC,
    cameraRequest: () -> CameraRequest
) {
    val navController = rememberNavController()
    LaunchedEffect(cameraInitialized) {
        if (cameraInitialized) {
            navController.navigate("preview")
        }
    }




    NavHost(navController, startDestination = "home") {
        composable("home") {
            // A simple loading/home screen
            //Greeting()
        }

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
        //Greeting("Android", signalingClient = signalingClient)
    }
}