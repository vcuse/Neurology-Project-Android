package com.example.neurology_project_android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbRequest
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
import java.nio.ByteBuffer

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
            Log.d("USBDevice", "Device: ${device.deviceName}, Vendor ID: ${device.vendorId}, Product ID: ${device.productId} \n")

            usbManager.requestPermission(device, intent)
            if(true){

                val connection = usbManager.openDevice(device)
                if (connection != null) {
                    val interfaceCount = device.interfaceCount
                    for (i in 0 until interfaceCount) {
                        val usbInterface = device.getInterface(i)

                        // Claim the interface to establish communication
                        if (connection.claimInterface(usbInterface, true)) {
                            Log.d("USBDevice", "Interface claimed: ${usbInterface.id}")

                            // Check if the interface is for video
                            if (usbInterface.interfaceClass == 14 && usbInterface.endpointCount > 0) {

                                for (i in 0 until usbInterface.endpointCount) {
                                    Log.d("USB INTERFACE" , "Interface Type " + usbInterface.getEndpoint(0).type.toString())
                                    Log.d("USB INTERFACE", "DIRECTION " + usbInterface.getEndpoint(0).direction)
                                    Log.d("USB INTERFACE" ,"END POINT TYPE" + usbInterface.getEndpoint(0).type)

                                    if(usbInterface.getEndpoint(0).type != 1){
                                        break
                                    }
                                    var videoEndPoint = usbInterface.getEndpoint(0)
                                    val buffer = ByteArray(1024 * 1024) // Buffer size large enough for video packets
                                    val request = UsbRequest()
                                    if (request.initialize(connection, videoEndPoint)) {
                                        val buffer = ByteBuffer.allocate(1024) // Allocate a buffer for video data

                                        try {
                                            // Queue the request
                                            if (request.queue(buffer, buffer.capacity())) {
                                                // Wait for the request to complete
                                                val result = connection.requestWait()
                                                if (result != null) {
                                                    Log.d("USBData", "Received video data")

                                                    // Inspect the first few bytes of the buffer
                                                    val receivedData = ByteArray(buffer.position())
                                                    buffer.flip() // Prepare the buffer for reading
                                                    buffer.get(receivedData)

                                                    Log.d(
                                                        "USBVideo",
                                                        receivedData.joinToString(", ", limit = 10) { it.toString(16) }
                                                    )

                                                    // Process the video frame (e.g., decode it)
                                                    // processVideoFrame(receivedData)
                                                } else {
                                                    Log.e("USBRequest", "Failed to receive video data")
                                                }
                                            } else {
                                                Log.e("USBRequest", "Failed to queue request")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("USBError", "Error while processing USB request: ${e.message}")
                                        } finally {
                                            // Clean up resources
                                            request.close()
                                            connection.releaseInterface(usbInterface)
                                            connection.close()
                                            Log.d("USBRequest", "Resources released")
                                        }
                                    } else {
                                        Log.e("USBRequest", "Failed to initialize request")
                                    }

                                }
                                Log.d("USBDevice", "Found a video interface")

                                // Here, you would need to handle the video feed. Typically,
                                // you use a library like UVCCamera to capture the feed.
                                //initializeUVCCamera(device, connection, usbInterface)
                            }
                        } else {
                            Log.e("USBDevice", "Failed to claim interface: ${usbInterface.id}")

                        }
                    }
                } else {
                    Log.e("USBDevice", "Failed to open connection for device: ${device.deviceName}")
                }
            }
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