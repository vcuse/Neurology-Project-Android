package com.example.neurology_project_android

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
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class SignalingClient @OptIn(UnstableApi::class) constructor
    (
    context: Context,
    private val onPeersFetched: (List<String>) -> Unit

) {
//    private lateinit var localPeer: PeerConnection
    private lateinit var httpUrl: String
    private lateinit var theirID: String
    private lateinit var context: Context
    private lateinit var webSocketListener: WebSocketListener
    private lateinit var client: OkHttpClient
    private lateinit var mediaID: String
    private lateinit var webSocket: WebSocket
//    private lateinit var localSDP: SessionDescription
//    private lateinit var track: VideoTrack
//    private val server =
//        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
//    private var candidatesList = ArrayList<IceCandidate>()
    private var isReadyToAddIceCandidate: Boolean = false
    private var candidateMessagesToSend = ArrayList<String>()

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


        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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

    fun joinRoom(room_id: String){
        var currentRoomClient = RoomClient(room_id, "david_android", socket, context = this.context)


    }


    fun getAuthToken(){

    }
    private var rooms: Array<String> = emptyArray()
    private var roomList = mutableListOf<String>()
    private lateinit var socket: Socket;


    @OptIn(UnstableApi::class)
    fun getRoomList(){
        Log.d("SIGNALING CLIENT", "Attempting to emit getRoomList with Ack")

        // 1. Prepare the payload (empty, as the server doesn't need it for this event)
        val emptyPayload = JSONObject() // Or simply passing 'null' might work, but this is safer

        // 2. Emit the event with two arguments: Payload + Ack Callback
        socket.emit("getRoomList", emptyPayload, Ack { args ->

            // This block runs when the server executes 'callback(roomList)'

            if (args.isEmpty() || args[0] == null) {
                Log.e("SIGNALING CLIENT", "No room list received.")
                return@Ack
            }

            // Assuming the room list is the first argument in the callback's arguments array
            val responseData = args[0]

            if (responseData is JSONArray) {


                Log.d("SIGNALING CLIENT", "SUCCESS! Room List received: $responseData")
                // 1. Initialize a mutable list to hold the extracted room IDs

                var roomList = mutableListOf<String>()

                // 2. Loop through the JSONArray
                for (i in 0 until responseData.length()) {
                    // 3. Safely extract each element as a String
                    val roomId = responseData.getString(i)
                    roomList.add(roomId)
                    onPeersFetched(roomList)
                }




                // TODO: Update ViewModel/Activity state here

            } else {
                Log.e("SIGNALING CLIENT", "Received unexpected response type: ${responseData.javaClass.name}")
            }
        })

    }


    init {
        this.context = context
        var sessionManager = SessionManager(context)
        var token = sessionManager.fetchAuthToken()
        Log.d("SIGNALING CLIENT", "token is " + token)
        val authMap = mapOf("cookie" to listOf(token))




        val options = IO.Options.builder()
            .setReconnection(true)
            .setForceNew(true)
            .setExtraHeaders(authMap)

            .build()


        try{
            socket = IO.socket("https://meechie.techkit.xyz:3016", options)


        }catch(e: Error){
            Log.d("SOCKET ERROR:" ,e.toString())
        }

        socket.connect()
        fixedRateTimer("getRoomList timer", false, 0L, 175000L) {
            getRoomList()
        }



    }

}