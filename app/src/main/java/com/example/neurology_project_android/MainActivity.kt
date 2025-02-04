package com.example.neurology_project_android


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.usb.USBMonitor
import com.jiangdg.uvc.UVCCamera

class MainActivity : ComponentActivity() {

    private lateinit var multiCameraClient: MultiCameraClient
    private lateinit var camera: CameraUVC
    private lateinit var cameraRequest: CameraRequest
    private var cameraInitialized by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)




        val intent = PendingIntent.getBroadcast(
            this, 0, Intent("${applicationContext.packageName}.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE
        )






        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
        val signalingClient = SignalingClient("https://videochat-signaling-app.ue.r.appspot.com:443/peerjs?id=3da89534895638&token=6789&key=peerjs"
        , this)
        Log.d("MainActivitiy", "SignalingClient should be set")

        val iDeviceCallback = object: IDeviceConnectCallBack {

            override fun onAttachDev(device: UsbDevice?) {

                if(device!!.productName == "USB Camera"){

                    multiCameraClient.requestPermission(device)
                    camera = CameraUVC(this@MainActivity, device)
                    cameraRequest = CameraRequest.Builder().setPreviewWidth(1280).setPreviewHeight(720).create()

                    cameraInitialized = true






                }

            }

            override fun onDetachDec(device: UsbDevice?) {

            }

            @OptIn(UnstableApi::class)
            override fun onConnectDev(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?
            ) {
                Log.d("CONNECT", "camera connection. pid: ${device!!.productId}, vid: ${device.vendorId}")
                camera.setUsbControlBlock(ctrlBlock)
                camera.openCamera(this@MainActivity, cameraRequest)
            }

            override fun onDisConnectDec(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?
            ) {

            }

            override fun onCancelDev(device: UsbDevice?) {

            }

        }


        multiCameraClient = MultiCameraClient(this@MainActivity, iDeviceCallback)
        multiCameraClient.register()


        enableEdgeToEdge()
        setContent {
            NeurologyProjectAndroidTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Suppose you have the camera & request object set up
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        signalingClient = signalingClient,
                        cameraInitialized = cameraInitialized,
                        camera = { camera }, cameraRequest = { cameraRequest }

                    )

                }
            }
        }
    }
}



@Composable
fun UsbCameraPreview(
    modifier: Modifier = Modifier,
    camera: CameraUVC,
    cameraRequest: CameraRequest
) {

   // val navController = rememberNavController()


    AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        // 1) Wrap the TextureView's SurfaceTexture in a Surface
                        val previewSurface = Surface(surfaceTexture)

                        // 2) Open the camera, providing the real Surface
                        //    The library will render frames onto this Surface




                    }

                    override fun onSurfaceTextureSizeChanged(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) = Unit

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        // Return true to let the system release the SurfaceTexture automatically
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                }
            }
        }
    )
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

    Box(


        modifier = Modifier.fillMaxSize(), // Make the Box take up the entire screen
        contentAlignment = Alignment.Center // Center its content
    ) {
        Button(
            onClick = { signalingClient.changeCamera() } // Wrap the function call in a lambda
        ) {
            Text("Change Camera")
        }
    }


    NavHost(navController, startDestination = "home") {
        composable("home") {
            // A simple loading/home screen
            //Greeting()
        }
        composable("preview") {
            // Our camera preview screen
            UsbCameraPreview(
                camera = camera(),
                cameraRequest = cameraRequest()
            )
        }
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