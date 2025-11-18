package com.example.neurology_project_android

import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.metrics.Event
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.remember
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import one.dugon.mediasoup_android_sdk.Engine
import one.dugon.mediasoup_android_sdk.Engine.Listener
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.fixedRateTimer
import kotlin.contracts.contract

//import org.webrtc.Camera2Capturer
//import org.webrtc.CameraVideoCapturer.CameraEventsHandler
//import org.webrtc.DataChannel
//import org.webrtc.DefaultVideoDecoderFactory
//import org.webrtc.DefaultVideoEncoderFactory
//import org.webrtc.EglBase
//import org.webrtc.IceCandidate
//import org.webrtc.MediaConstraints
//import org.webrtc.MediaStream
//import org.webrtc.PeerConnection
//import org.webrtc.PeerConnectionFactory
//import org.webrtc.SdpObserver
//import org.webrtc.SessionDescription
//import org.webrtc.SurfaceTextureHelper
//import org.webrtc.VideoTrack
//import org.webrtc.VideoProcessor
//import org.webrtc.VideoSource


class HeaderAndMessage(header: String, message: JSONObject) {

}


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class SignalingClient @OptIn(UnstableApi::class) constructor
    (
    context: Context,
//    private val onPeersFetched: (List<String>) -> Unit

) {
    //    private lateinit var localPeer: PeerConnection
    private lateinit var httpUrl: String
    private lateinit var theirID: String
    private lateinit var context: Context
    private lateinit var webSocketListener: WebSocketListener
    private lateinit var client: OkHttpClient
    private lateinit var mediaID: String
    private lateinit var webSocket: WebSocket
    private lateinit var currentRoomClient: RoomClient
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isReadyToAddIceCandidate: Boolean = false
    private var candidateMessagesToSend = ArrayList<String>()

    // A private, mutable flow that the WebSocketListener can post values to.
    private val _peerListFlow = MutableSharedFlow<List<String>>()

    // A public, read-only SharedFlow that the rest of the app can collect.
    val peerListFlow: SharedFlow<List<String>> = _peerListFlow.asSharedFlow()


    val availabilityCallback = object : CameraManager.AvailabilityCallback() {
        @OptIn(UnstableApi::class)
        override fun onCameraAvailable(cameraId: String) {
            super.onCameraAvailable(cameraId)
            Log.d("CameraManager", "Camera available: $cameraId")
            // Perform actions when a camera becomes available (e.g., USB camera is connected)
        }

        @OptIn(UnstableApi::class)
        override fun onCameraUnavailable(cameraId: String) {
            super.onCameraUnavailable(cameraId)
            Log.d("CameraManager", "Camera unavailable: $cameraId")
            // Perform actions when a camera becomes unavailable (e.g., USB camera is disconnected)
        }
    }


    @OptIn(UnstableApi::class)
    private fun buildVideoSenders(context: Context, url: String) {


        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.registerAvailabilityCallback(availabilityCallback, null)


        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        Log.d("Cameras", cameraManager.toString())
        val cameraList = cameraManager.cameraIdList
        val camera01 = cameraManager.cameraIdList.first()
        val camera02 = cameraManager.cameraIdList.last()

        client = OkHttpClient().newBuilder().build()
        httpUrl = url
        AudioManager.ADJUST_UNMUTE
        val audioDeviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        Log.d("Signaling Client", "Audio devices" + audioDeviceInfo.size)

    }

    fun joinRoom(room_id: String) {
        currentRoomClient = RoomClient(room_id, "david_android", socket, context = this.context)
    }


    @OptIn(UnstableApi::class)
    fun submitToServer(header:String, payload: JSONObject){
        socket.emit(header, payload, Ack { args ->
            val responseData = args[0]
            Log.d(TAG, "Response from server: $responseData")
        })
    }

    private var rooms: Array<String> = emptyArray()
    private var roomList = mutableListOf<String>()
    private lateinit var socket: Socket;


    @OptIn(UnstableApi::class)
    fun getRoomList() {
        Log.d("SIGNALING CLIENT", "Attempting to emit getRoomList with Ack")

        val emptyPayload = JSONObject()

        socket.emit("getRoomList", emptyPayload, Ack { args ->
            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "No room list received.")
                // It's good practice to emit an empty list on failure too
                clientScope.launch { _peerListFlow.emit(emptyList()) }
                return@Ack
            }

            val responseData = args[0]

            if (responseData is JSONArray) {
                Log.d("SIGNALING CLIENT", "SUCCESS! Room List received: $responseData")

                // --- FIX IS HERE ---
                // 1. Create a new list to hold the parsed room IDs.
                val parsedRoomList = mutableListOf<String>()

                // 2. Loop through the JSONArray and extract each string.
                for (i in 0 until responseData.length()) {
                    // Use optString to safely get the string at each index.
                    val roomName = responseData.optString(i)
                    if (roomName.isNotEmpty()) {
                        parsedRoomList.add(roomName)
                    }
                }

                // 3. Launch a coroutine to emit the PARSED list to the flow.
                clientScope.launch {
                    Log.d("SIGNALING CLIENT", "Emitting parsed room list to flow: $parsedRoomList")
                    _peerListFlow.emit(parsedRoomList)
                }
                // --- END OF FIX ---

            } else {
                Log.e(
                    "SIGNALING CLIENT",
                    "Received unexpected response type: ${responseData.javaClass.name}"
                )
                // Also emit an empty list if the data format is wrong
                clientScope.launch { _peerListFlow.emit(emptyList()) }
            }
        })
    }

    private val gson = Gson()
    private val producerMap: MutableMap<String, Any> = mutableMapOf()

    @OptIn(UnstableApi::class)
    fun connectClient(){
        var sessionManager = SessionManager(context)
        var token = sessionManager.fetchAuthToken()
        Log.d("SIGNALING CLIENT", "token is " + token)
        val authMap = mapOf("cookie" to listOf(token))


        val options = IO.Options.builder()
            .setReconnection(true)
            .setForceNew(true)
            .setExtraHeaders(authMap)

            .build()


        try {
            socket = IO.socket("https://meechie.techkit.xyz:3016", options)
            socket.connect()
            // The Emitter.Listener callback runs on a background thread.
            socket.on("newProducers", Emitter.Listener { args ->
//                this.currentRoomClient.consume()
                Log.e(TAG, "RECEIVED PRODUCERS")
                // 1. Get the JSONArray containing the list of producer objects
                val dataArray = args.getOrNull(0) as? JSONArray

                if (dataArray == null || dataArray.length() == 0) {
                    Log.w(TAG, "Received newProducers event but data array was empty or null.")
                    return@Listener
                }

                // 2. Launch a coroutine to handle the asynchronous consumption loop
                // We use Dispatchers.IO for networking/blocking operations.
                CoroutineScope(Dispatchers.IO).launch {

                    // --- CONVERSION ---
                    // Convert the org.json.JSONArray to a List<ProducerInfo> using Gson/TypeToken
                    val listType = object : TypeToken<List<ProducerInfo>>() {}.type

                    // Note: Since JSONArray doesn't have a direct toString() that Gson handles perfectly
                    // across all Android versions, we convert to string and parse.
                    val producerInfoList: List<ProducerInfo> =
                        gson.fromJson(dataArray.toString(), listType)

                    Log.d(TAG, "Attempting to consume ${producerInfoList.size} new producers.")

                    // --- CONSUMPTION LOOP ---
                    for (info in producerInfoList) {

                        val producerId = info.producer_id

                        // 3. Check if we already created this producer (optional self-check)
                        if (!producerMap.containsKey(producerId)) {
                            try {
                                // 4. Await the asynchronous consumption method
                                // This method (which you need to implement) handles signaling and transport setup
                                currentRoomClient.consume(producerId)

                                Log.i(
                                    TAG,
                                    "Successfully consumed stream for Producer ID: $producerId"
                                )

                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to consume producer $producerId", e)
                            }
                        }
                    }
                }
            })

        } catch (e: Error) {
            Log.d("SOCKET ERROR:", e.toString())
        }


        fixedRateTimer("getRoomList timer", false, 0L, 175000L) {
            getRoomList()
        }
    }

    init {
        this.context = context




    }

}