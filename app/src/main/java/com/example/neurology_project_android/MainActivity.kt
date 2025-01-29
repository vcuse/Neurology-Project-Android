package com.example.neurology_project_android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConfiguration
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
            if(device.productName == "USB Camera-OV580"){

                val connection = usbManager.openDevice(device)
                if (connection != null) {
                    val interfaceCount = device.interfaceCount
                    for (i in 0 until interfaceCount) {

                            val usbInterface = device.getInterface(i)

                            Log.d("USBDevice", "Interface Class: ${usbInterface.interfaceClass}")

                            // Check if the interface is for video
                            if (usbInterface.interfaceClass == 14 && usbInterface.endpointCount > 0 ) {
                                Log.d("USB INTERAFCE", "ENDPOINT COUNT ${usbInterface.endpointCount}")
                                connection.claimInterface(usbInterface, true)
                                for (i in 0 until usbInterface.endpointCount) {
                                    Log.d("USB INTERFACE" , "Interface Type " + usbInterface.getEndpoint(0).type.toString())
                                    Log.d("USB INTERFACE", "DIRECTION " + usbInterface.getEndpoint(0).direction)
                                    Log.d("USB INTERFACE" ,"END POINT TYPE" + usbInterface.getEndpoint(0).type)


                                    var videoEndPoint = usbInterface.getEndpoint(0)
                                    var number = videoEndPoint.address
                                    val request = UsbRequest()

                                    if (number == 129) {
                                        val buffer = ByteBuffer.allocate(videoEndPoint.maxPacketSize) // Allocate a buffer for video data
                                        val bufferForTransfer = ByteArray(videoEndPoint.maxPacketSize) // Buffer size large enough for video packets
                                        val STREAM_ENABLE = ubyteArrayOf(
                                            0x1u, 0x0u, 0x1u, 0x1u, 0x15u, 0x16u, 0x5u,
                                            0x0u, 0x0u, 0x0u, 0x0u, 0x0u, 0x0u, 0x0u, 0x0u, 0x0u,0x0u,0x0u,0x0u, // <repeats 12 times>
                                            0x65u, 0x9u, 0x0u, 0x0u, 0x80u, 0x0u, 0x0u, 0x80u,
                                            0xd1u, 0xf0u, 0x8u, 0x0u, 0x0u, 0x0u, 0x0u
                                        )

                                        try {
                                            // Queue the request

                                            // Wait for the request to complete
                                            Log.d("USBData", "WE QUEUED DATA")
                                            val result = connection.controlTransfer(0x21, 1,
                                                2 shl 8, 1, STREAM_ENABLE.asByteArray(), STREAM_ENABLE.size, 1000)

                                            Log.d("Result", "Result $result")
                                            var PACKETSIZE = videoEndPoint.maxPacketSize
                                            var frameBuffer = ByteArray(videoEndPoint.maxPacketSize)

                                            while(true){
                                                  connection.bulkTransfer(videoEndPoint, frameBuffer,PACKETSIZE, 1000 )
                                                    //Log.d("HEADER", frameBuffer.get(0).toString()   )

                                                // Extract first 12 bytes as header
                                                val header = frameBuffer.copyOfRange(0, 12)
                                                //Log.d("Header", "Header: ${header.joinToString(", ")}")

// Extract buffer data from byte 12 to 50
                                                val bufferData = frameBuffer.copyOfRange(0, 50)
                                                 Log.d("BUFFER", "Buffer data: ${bufferData.joinToString(", ")} ...")
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


                                    }
                                    else{
                                        Log.e("USBDEVICE", "FAILED TO INIT")
                                    }
                                Log.d("USBDevice", "Found a video interface")

                                // Here, you would need to handle the video feed. Typically,
                                // you use a library like UVCCamera to capture the feed.
                                //initializeUVCCamera(device, connection, usbInterface)
                            }
                        }
                    }
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