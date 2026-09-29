package com.example.neurology_project_android



import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import com.example.neurology_project_android.BuildConfig.API_GET_ID_URL
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.activity.viewModels
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.BuildConfig.API_GET_PEERS_URL
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme
import com.meta.wearable.dat.core.types.PermissionStatus
import com.meta.wearable.dat.core.Wearables
import com.meta.wearable.dat.core.types.Permission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.withLock
import kotlin.coroutines.resume


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @SuppressLint("RestrictedApi")
    private lateinit var cameraRequest: CameraRequest
//    private lateinit var videoProcessor: VideoProcessor
//    private lateinit var videoSource: VideoSource
//    private lateinit var capturerObserver: CapturerObserver
    private var isInCall by mutableStateOf(false)
    private var cameraInitialized by mutableStateOf(false)
    private lateinit var signalingClient: SignalingClient
    private lateinit var signalingRepository: SignalingRepository
    private val viewModel: MainViewModel by viewModels()
    // 1. Define the Mutex and Continuation at the class level
    private val permissionMutex = Mutex()
    private var permissionContinuation: CancellableContinuation<com.meta.wearable.dat.core.types.DatResult<PermissionStatus, com.meta.wearable.dat.core.types.PermissionError>>? = null

    private val permissionsResultLauncher =
        registerForActivityResult(Wearables.RequestPermissionContract()) { result ->
            // result here is DatResult<PermissionStatus, PermissionError>
            permissionContinuation?.resume(result)
            permissionContinuation = null
        }

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

            @Composable
            fun WearableStatusSection(viewModel: MainViewModel, onRegister: () -> Unit) {
                val regState by viewModel.registrationState.collectAsState()
                val devices by viewModel.devices.collectAsState()

                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registration Status: ${regState.name}")
                    Text("Connected Devices: ${devices.size}")

                    Button(onClick = onRegister) {
                        Text("Register with Meta App")
                    }
                }
            }

            NeurologyProjectAndroidTheme {
                viewModel.connectSignalingClient()
                val uiState by viewModel.uiState.collectAsState()
                // 3. OBSERVE state from the ViewModel
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val state = uiState) {
                        is MainUiState.Loading -> {
                            Log.d("MainActivity", "You are currently loading")
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                        }
                        is MainUiState.Error -> {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("Error: ${state.message}")
                            }
                        }


                        is MainUiState.Success -> {
                            MyApp(
                                userId = state.userId,
                                peers = state.peers,
                                innerPadding = innerPadding,
                                onJoinRoom = { targetId -> viewModel.joinRoom(targetId) },
                                onRequestCameraPermission = {
                                    lifecycleScope.launch {
                                        requestWearablesPermission(Permission.CAMERA)
                                            .onSuccess { status ->
                                                if (status == PermissionStatus.Granted) {
                                                    Log.d("Wearables", "Camera streaming permission approved!")
                                                } else {
                                                    Log.d("Wearables", "Camera streaming permission denied.")
                                                }
                                            }
                                            .onFailure { error, _ ->
                                                Log.d("Wearables", "Permission request failed: ${error.description}")
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 4. The helper function
    suspend fun requestWearablesPermission(
        permission: Permission
    ): com.meta.wearable.dat.core.types.DatResult<PermissionStatus, com.meta.wearable.dat.core.types.PermissionError> {
        return permissionMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                permissionContinuation = continuation
                continuation.invokeOnCancellation { permissionContinuation = null }
                permissionsResultLauncher.launch(permission)
            }
        }
    }



    fun requestWearablesRegistration() {
        Wearables.startRegistration(this)
    }

    fun requestWearablesUnregistration() {
        Wearables.startUnregistration(this)
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

    // All your other @Composable functions should be updated to accept the data and lambdas they need
    // For example, MyApp now takes the userId, peers, and onJoinRoom lambda
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun MyApp(
        userId: String,
        peers: List<String>,
        innerPadding: PaddingValues,
        onJoinRoom: (String) -> Unit,
        onRequestCameraPermission: () -> Unit // Pass action type down
    ) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    userId = userId,
                    peers = peers,
                    onJoinRoom = onJoinRoom,
                    onNavigateToCallScreen = { navController.navigate("callScreen") },
                    onRegisterWearable = { requestWearablesRegistration() },
                    onRequestCameraPermission = onRequestCameraPermission // Pass down to UI
                )
            }
            composable("callScreen") {
                CallScreen()
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun HomeScreen(
        modifier: Modifier = Modifier,
        userId: String,
        peers: List<String>,
        onJoinRoom: (String) -> Unit,
        onRegisterWearable: () -> Unit,
        onNavigateToCallScreen: () -> Unit,
        onRequestCameraPermission: () -> Unit // Receive action here
    ) {
        val context = LocalContext.current
        val sessionManager = remember { SessionManager(context) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Logged in as: $userId", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Button to register the wearable device
            Button(onClick = onRegisterWearable, modifier = Modifier.fillMaxWidth()) {
                Text("1. Register Meta Glasses")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // New Button to request camera approval before launching streams
            Button(
                onClick = onRequestCameraPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("2. Request Glasses Camera Access")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OnlineNowSection(peers = peers, onNavigateToOnlineScreen = onNavigateToCallScreen)
        }
    }


}

suspend fun fetchUserId(): String {
    return withContext(Dispatchers.IO) {
        try {
            val idUrl = API_GET_ID_URL
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
fun OnlineNowSection(
    peers: List<String>,
    onJoinRoom: (String) -> Unit,
    onNavigateToCallScreen: () -> Unit
) {
    Text(
        text = "Online Now:",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    if (peers.isEmpty()) {
        Text(text = "No peers online", modifier = Modifier.padding(16.dp))
    } else {
        peers.forEach { peerId ->
            OnlineUserCard(
                userId = peerId,
                onJoinClick = {
                    // FIX #2: Chain the events. Call ViewModel then navigate.
                    onJoinRoom(peerId)
                    onNavigateToCallScreen()
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun OnlineUserCard(userId: String, onJoinClick: () -> Unit) { // FIX #3: Simplified parameter
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
            Text(text = userId, modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp))
            Button(
                onClick = onJoinClick, // Use the simplified lambda
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(text = "Call")
            }
        }
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