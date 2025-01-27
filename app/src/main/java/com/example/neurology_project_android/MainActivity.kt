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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

                            //connection.setInterface(usbInterface)
                            Log.d("USBDevice", "Interface claimed: ${usbInterface.id}")
                            Log.d("USBDEVICE", "Inteface Class: ${usbInterface.interfaceClass}")
                            // Check if the interface is for video
                            if (usbInterface.interfaceClass == 14 && usbInterface.endpointCount > 0) {

                                for (i in 0 until usbInterface.endpointCount) {
                                    Log.d("USB INTERFACE" , "Interface Type " + usbInterface.getEndpoint(0).type.toString())




                                    for (j in 0 until usbInterface.endpointCount) {
                                        val endpoint = usbInterface.getEndpoint(j)
                                        Log.d("USBEndpoint", "Endpoint $j: Type=${endpoint.type}, Direction=${endpoint.direction}, MaxPacket=${endpoint.maxPacketSize}, Number=${endpoint.endpointNumber}")
                                        val interfaceClass = usbInterface.interfaceClass
                                        val interfaceSubclass = usbInterface.interfaceSubclass
                                        Log.d("USBDevice", "Interface Class: $interfaceClass, Subclass: $interfaceSubclass")
                                        if(endpoint.type == UsbConstants.USB_ENDPOINT_XFER_ISOC or UsbConstants.USB_ENDPOINT_XFER_BULK){
                                            Log.d("USBEndpoint", "Address: ${endpoint.address}, Type: ${endpoint.type}, Direction: ${endpoint.direction}")
                                            val request = UsbRequest()
                                            connection.claimInterface(usbInterface, true)
                                            val byteArray = ByteArray(64)

                                            val bytesRead = connection.bulkTransfer(endpoint, byteArray, byteArray.size, 1000)
                                            if (bytesRead >= 0) {
                                                Log.d("USBTransfer", "Received $bytesRead bytes: ${byteArray.take(bytesRead).joinToString(", ")}")
                                            } else {
                                                Log.e("USBTransfer", "Failed to receive data")
                                            }

                                            //connection.claimInterface(usbInterface, true)
                                            if (request.initialize(connection, endpoint)) {
                                                val buffer = ByteBuffer.allocate(endpoint.maxPacketSize)

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        while (true) {
                                                            Log.d("USBData", "WE ARE IN USBDATA")
                                                            if (request.queue(buffer)) {
                                                                Log.d("USBDATA", "WE Are QUEUING DATA")
                                                                val result = connection.requestWait()
                                                                if (result != null) {
                                                                    Log.d("USBDATA", "WE GOT A RESULT")
                                                                    // Process the received data
                                                                    Log.d("USBData", "Received video data: ${buffer.position()} bytes")
                                                                    val receivedData = ByteArray(buffer.position())
                                                                    buffer.flip()
                                                                    buffer.get(receivedData)

                                                                    // Inspect the first few bytes
                                                                    Log.d("USBData", receivedData.joinToString(", ", limit = 10) { it.toString(16) })

                                                                    // Decode the video data (MJPEG, H.264, etc.)
                                                                    // decodeVideoFrame(receivedData)
                                                                } else {
                                                                    Log.e("USBData", "Failed to receive data")
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("USBError", "Error: ${e.message}")
                                                    } finally {
                                                        request.close()
                                                        connection.releaseInterface(usbInterface)
                                                        connection.close()
                                                    }
                                                }.start()
                                            } else {
                                                Log.e("USBRequest", "Failed to initialize UsbRequest")
                                            }
                                        }
                                        else {
                                            if(endpoint.type == UsbConstants.USB_ENDPOINT_XFER_INT || endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                val buffer = ByteArray(256)
                                                connection.claimInterface(usbInterface, true)
                                                val result = connection.controlTransfer(
                                                    UsbConstants.USB_DIR_IN or UsbConstants.USB_TYPE_STANDARD ,
                                                    0x01, // Example request code
                                                    0x0100, // Example value (e.g., resolution ID)
                                                    usbInterface.id, // Interface ID
                                                    buffer, // Optional data buffer (if no additional data, pass null)
                                                    256, // Data length (0 if no data buffer)
                                                    1000 // Timeout in milliseconds
                                                )

                                                if (result >= 0) {
                                                    var newString = java.lang.String(
                                                        buffer,
                                                        2,
                                                        result - 2,
                                                        "UTF-16LE"
                                                    )
                                                    Log.d("USBControl", "Control transfer successful ${newString}" )
                                                } else {
                                                    Log.e("USBControl", "Control transfer failed")
                                                }

                                            }

                                        //
                                        //
                                        //                                            Log.e("USBRequest", "Starting in the else (for non type == 1)")
//                                            var videoEndPoint = usbInterface.getEndpoint(0)
//                                            val buffer = ByteArray(1024 * 1024) // Buffer size large enough for video packets
//                                            val request = UsbRequest()
//                                            if (request.initialize(connection, videoEndPoint)) {
//                                                val buffer = ByteBuffer.allocate(1024) // Allocate a buffer for video data
//
//                                                try {
//                                                    // Queue the request
//                                                    if (request.queue(buffer, buffer.capacity())) {
//                                                        // Wait for the request to complete
//                                                        val result = connection.requestWait()
//                                                        if (result != null) {
//                                                            Log.d("USBData", "Received video data")
//
//                                                            // Inspect the first few bytes of the buffer
//                                                            val receivedData = ByteArray(buffer.position())
//                                                            buffer.flip() // Prepare the buffer for reading
//                                                            buffer.get(receivedData)
//
//                                                            Log.d(
//                                                                "USBVideo",
//                                                                receivedData.joinToString(", ", limit = 10) { it.toString(16) }
//                                                            )
//
//                                                            // Process the video frame (e.g., decode it)
//                                                            // processVideoFrame(receivedData)
//                                                        } else {
//                                                            Log.e("USBRequest", "Failed to receive video data")
//                                                        }
//                                                    } else {
//                                                        Log.e("USBRequest", "Failed to queue request")
//                                                    }
//                                                } catch (e: Exception) {
//                                                    Log.e("USBError", "Error while processing USB request: ${e.message}")
//                                                } finally {
//                                                    // Clean up resources
//                                                    request.close()
//                                                    connection.releaseInterface(usbInterface)
//                                                    connection.close()
//                                                    Log.d("USBRequest", "Resources released")
//                                                }
//                                            } else {
//                                                Log.e("USBRequest", "Failed to initialize request")
//                                            }
                                        }
                                    }



                                }
                                Log.d("USBDevice", "Found a video interface")

                                // Here, you would need to handle the video feed. Typically,
                                // you use a library like UVCCamera to capture the feed.
                                //initializeUVCCamera(device, connection, usbInterface)
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