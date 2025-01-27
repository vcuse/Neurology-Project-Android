package com.example.neurology_project_android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.PendingIntentCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.example.neurology_project_android.ui.theme.NeurologyProjectAndroidTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        var deviceList  = usbManager.deviceList
        val intent = PendingIntent.getBroadcast(
            this, 0, Intent("${applicationContext.packageName}.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE
        )
        for ((_, device) in deviceList) {
            Log.d("USBDevice", "Device: ${device.deviceName}, Vendor ID: ${device.vendorId}, Product ID: ${device.productId}")
            usbManager.requestPermission(device, intent)

        }





        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1)
        val signalingClient = SignalingClient("https://videochat-signaling-app.ue.r.appspot.com:443/peerjs?id=3da89534895638&token=6789&key=peerjs"
        , this)
        Log.d("MainActivitiy", "SignalingClient should be set")
        enableEdgeToEdge()
        setContent {
            NeurologyProjectAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        signalingClient
                    )



                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, signalingClient: SignalingClient) {
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
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NeurologyProjectAndroidTheme {
        val navController = rememberNavController()
        //Greeting("Android", signalingClient = signalingClient)
    }
}