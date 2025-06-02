package com.example.neurology_project_android

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.Camera2Capturer
import org.webrtc.CameraVideoCapturer.CameraEventsHandler
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoTrack
import org.webrtc.VideoProcessor
import org.webrtc.VideoSource

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class SignalingClient @OptIn(UnstableApi::class) constructor
    (
    url: String,
    context: Context, ourID: String,
    private val onCallRecieved: () -> Unit,
    private val onCallEnded: () -> Unit
) {
    private lateinit var localPeer: PeerConnection
    private lateinit var httpUrl: String
    private lateinit var theirID: String
    private var ourID = ourID
    private lateinit var webSocketListener: WebSocketListener
    private lateinit var client: OkHttpClient
    private lateinit var mediaID: String
    private lateinit var webSocket: WebSocket
    private lateinit var localSDP: SessionDescription
    private lateinit var track: VideoTrack
    private val server =
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    private var candidatesList = ArrayList<IceCandidate>()
    private var isReadyToAddIceCandidate: Boolean = false
    private var candidateMessagesToSend = ArrayList<String>()
    private lateinit var camera1Capturer: Camera2Capturer
    private lateinit var videoSource: VideoSource
    private lateinit var factory: PeerConnectionFactory
    private lateinit var rootEGL: EglBase
    private lateinit var sdpObserver: SdpObserver

    fun setLocalSDP() {
        localPeer.setLocalDescription(remoteObserver, localSDP)
    }

    private var remoteObserver = object : SdpObserver {
        @OptIn(UnstableApi::class)
        override fun onCreateSuccess(sdp: SessionDescription?) {
            Log.d("RemoteObserver", "Answer SDP was Created")
            if (sdp != null) {
                localSDP = sdp
                setLocalSDP()
            }
            val sdpMsg = JSONObject()
            sdpMsg.accumulate("type", "answer")
            sdpMsg.accumulate(
                "sdp",
                sdp?.description
            )


            val payload = JSONObject()
            payload.accumulate("sdp", sdpMsg)
            payload.accumulate("type", "media")
            payload.accumulate("browser", "firefox")
            payload.accumulate("connectionId", mediaID)

            val msg = JSONObject()
            msg.accumulate("type", "ANSWER")
            msg.accumulate("payload", payload)
            msg.accumulate("dst", theirID)

            val formBody =
                FormBody.Builder().add("type", "ANSWER").add("payload", payload.toString())
                    .add("dst", theirID).build()


            var request = Request.Builder().url(httpUrl).post(formBody).build()

            webSocket.send(msg.toString())

            for (candidate in candidatesList) {
                val status = localPeer.addIceCandidate(candidate)
                Log.d("Adding ICE CANDIDATE", status.toString())
            }

            isReadyToAddIceCandidate = true

        }

        @OptIn(UnstableApi::class)
        override fun onSetSuccess() {
            Log.d("SignalingClient", "RemoteSDP set successfully")

            val mediaConstraints1 = MediaConstraints.KeyValuePair(
                "kRTCMediaConstraintsOfferToReceiveAudio",
                "kRTCMediaConstraintsValueTrue"
            )
            val mediaConstraints2 = MediaConstraints.KeyValuePair(
                "kRTCMediaConstraintsOfferToReceiveVideo",
                "kRTCMediaConstraintsValueTrue"
            )
            val mediaConstraints3 = MediaConstraints.KeyValuePair(
                "kRTCMediaStreamTrackKindVideo",
                "kRTCMediaConstraintsValueTrue"
            )

            val mediaConstraints4 = MediaConstraints.KeyValuePair(
                "DtlsSrtpKeyAgreement",
                "kRTCMediaConstraintsValueTrue"
            )

            val mediaConstraints5 = MediaConstraints.KeyValuePair("setup", "actpass")
            val mediaConstraints6 = MediaConstraints.KeyValuePair(
                "video",
                "true"
            )
            val mediaConstraints = MediaConstraints()


            mediaConstraints.mandatory.add(mediaConstraints1)
            mediaConstraints.mandatory.add(mediaConstraints2)
            mediaConstraints.mandatory.add(mediaConstraints3)
            mediaConstraints.mandatory.add(mediaConstraints4)
            mediaConstraints.mandatory.add(mediaConstraints5)
            mediaConstraints.mandatory.add(mediaConstraints6)
            localPeer.createAnswer(this, mediaConstraints)
        }

        @OptIn(UnstableApi::class)
        override fun onCreateFailure(error: String?) {
            Log.d("OnCreateFailure", error.toString())
        }

        @OptIn(UnstableApi::class)
        override fun onSetFailure(error: String?) {
            Log.d("SDP Observer", error.toString())
        }

    }

    private var peerConnObserver = object : PeerConnection.Observer {
        @OptIn(UnstableApi::class)
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            if (p0 != null) {
                Log.d("Signaling State", "SignalingChange " + p0.name)
            }


        }

        @OptIn(UnstableApi::class)
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            Log.d("ICE Connection", p0.toString())

            if (p0 == PeerConnection.IceConnectionState.DISCONNECTED ||
                p0 == PeerConnection.IceConnectionState.CLOSED) {
                onCallEnded() // Notify MainActivity when the call ends
            }
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            Log.d("ICEGATHERINGSTATE", p0.toString())
        }

        @OptIn(UnstableApi::class)
        override fun onIceCandidate(p0: IceCandidate?) {
            val candidate = JSONObject()
            candidate.accumulate("candidate", p0!!.sdp)
            candidate.accumulate("sdpMLineIndex", p0.sdpMLineIndex)
            candidate.accumulate("sdpMid", p0.sdpMid)

            val payload = JSONObject()
            payload.accumulate("candidate", candidate)
            payload.accumulate("connectionId", mediaID)
            payload.accumulate("type", "media")

            val message = JSONObject()
            message.accumulate("payload", payload)
            message.accumulate("type", "CANDIDATE")
            message.accumulate("dst", theirID)

            webSocket.send(message.toString())

            Log.d("REC ICECandidate", p0.toString())
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onAddStream(p0: MediaStream?) {

            if (p0 != null) {
                // Handle remote video track
                if (p0.videoTracks.isNotEmpty()) {
                    val remoteVideoTrack = p0.videoTracks[0]
                    //localPeer.addTrack(remoteVideoTrack)
                    // You need a way to pass this remoteVideoTrack to your UI
                    // For example, if you have a SurfaceViewRenderer in your Activity/Fragment:
                    // yourRemoteVideoRenderer.addTrack(remoteVideoTrack)
                    Log.d("PeerConnection", "Remote Video Track received: ${remoteVideoTrack.id()}")
                }
                // Handle remote audio track (WebRTC usually plays this automatically once received)
                if (p0.audioTracks.isNotEmpty()) {
                    val remoteAudioTrack = p0.audioTracks[0]
                    //localPeer.addTrack(remoteAudioTrack)
                    Log.d("PeerConnection", "Remote Audio Track received: ${remoteAudioTrack.id()}")
                }
            }

        }

        override fun onRemoveStream(p0: MediaStream?) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onDataChannel(p0: DataChannel?) {

            Log.d("PeerConnection", "DataChannel added")

            //TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onRenegotiationNeeded() {
            Log.d("PeerConnection", "Renegotation Needed")

        }

    }

    private var cameraEventsHandler = object : CameraEventsHandler {
        @OptIn(UnstableApi::class)
        override fun onCameraError(p0: String?) {
            Log.d("CAMERA ERROR", p0!!)
            //TODO("Not yet implemented")
        }

        override fun onCameraDisconnected() {
            TODO("Not yet implemented")
        }

        override fun onCameraFreezed(p0: String?) {

        }

        @OptIn(UnstableApi::class)
        override fun onCameraOpening(p0: String?) {
            Log.d("CAMERA EVENTS", "CAMERA OPEN")
            //TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onFirstFrameAvailable() {
            Log.d("CAMERA", "FIRST FRAME")
        }

        @OptIn(UnstableApi::class)
        override fun onCameraClosed() {
            Log.d("CAMERA","Camera Closed")
        }

    }

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







    private fun generateConfig(): PeerConnection.RTCConfiguration {
        val config = PeerConnection.RTCConfiguration(listOf(server))
        config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        config.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        config.iceTransportsType = PeerConnection.IceTransportsType.ALL
        return config
    }

    private fun buildFactory(rootEGL: EglBase): PeerConnectionFactory? {

        val encoderFactory = DefaultVideoEncoderFactory(rootEGL.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(rootEGL.eglBaseContext)
        val factory = PeerConnectionFactory.builder().setVideoDecoderFactory(decoderFactory)
            .setVideoEncoderFactory(encoderFactory).createPeerConnectionFactory()
        return factory
    }




    @OptIn(UnstableApi::class)
    private fun buildVideoSenders(context: Context, url: String) {


        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        rootEGL = EglBase.create()
        factory = buildFactory(rootEGL)!!

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.registerAvailabilityCallback(availabilityCallback, null)


        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        Log.d("Cameras", cameraManager.toString())
        val cameraList = cameraManager.cameraIdList
        val camera01 = cameraManager.cameraIdList.first()
        val camera02 = cameraManager.cameraIdList.last()
        camera1Capturer = Camera2Capturer(context, camera02, cameraEventsHandler)
        videoSource = factory?.createVideoSource(true)!!

//
//
        val surfaceTexture2 = SurfaceTextureHelper.create("CaptureThread", rootEGL.eglBaseContext)
//        //var test = MultiMediaClient()
        camera1Capturer.initialize(surfaceTexture2, context, videoSource!!.capturerObserver)
        val config2 = generateConfig()

//
       localPeer = factory.createPeerConnection(config2, peerConnObserver)!!
       camera1Capturer.startCapture(1920, 1080, 30)

        client = OkHttpClient().newBuilder().build()
        httpUrl = url
        AudioManager.ADJUST_UNMUTE

        val mediaConstraints1 = MediaConstraints.KeyValuePair(
            "kRTCMediaConstraintsOfferToReceiveAudio",
            "kRTCMediaConstraintsValueTrue"
        )
        val mediaConstraints2 = MediaConstraints.KeyValuePair(
            "kRTCMediaConstraintsOfferToReceiveVideo",
            "kRTCMediaConstraintsValueTrue"
        )
        val mediaConstraints3 = MediaConstraints.KeyValuePair(
            "kRTCMediaStreamTrackKindVideo",
            "kRTCMediaConstraintsValueTrue"
        )

        val mediaConstraints4 = MediaConstraints.KeyValuePair(
            "DtlsSrtpKeyAgreement",
            "kRTCMediaConstraintsValueTrue"
        )

        val mediaConstraints5 = MediaConstraints.KeyValuePair("setup", "actpass")
        val mediaConstraints6 = MediaConstraints.KeyValuePair(
            "video",
            "true"
        )
        val mediaConstraints = MediaConstraints()


        mediaConstraints.mandatory.add(mediaConstraints1)
        mediaConstraints.mandatory.add(mediaConstraints2)
        mediaConstraints.mandatory.add(mediaConstraints3)
        mediaConstraints.mandatory.add(mediaConstraints4)
        mediaConstraints.mandatory.add(mediaConstraints5)
        mediaConstraints.mandatory.add(mediaConstraints6)

        val audioSource = factory.createAudioSource(mediaConstraints)
        val audioTrack = factory.createAudioTrack("0002", audioSource)
        track = factory.createVideoTrack("0001", videoSource)

        localPeer.addTrack(track, listOf("track01"))

        localPeer.addTrack(audioTrack, listOf("track01"))

    }

    fun changeCamera(){
        camera1Capturer.switchCamera(null)
    }

    @OptIn(UnstableApi::class)
    fun changeVideoSource(videoProcessor: VideoProcessor){
        //var frame = VideoFrame()
        //videoSource.capturerObserver.onCapturerStarted(true)


    }

    fun getVideoSource(): VideoSource {
        return videoSource
    }

    fun createAndSendCallMessage(p0: SessionDescription?, userId: String, mediaID: String) {
        var innerSDPMessage = JSONObject()
        innerSDPMessage.put("sdp", p0!!.description)
        innerSDPMessage.put("type", "offer")
        var payloadMessage = JSONObject()
        payloadMessage.put("connectionId", mediaID)
        payloadMessage.put("type", "media")
        payloadMessage.put("sdp", innerSDPMessage)

        var outerObjectMessage = JSONObject()
        outerObjectMessage.put("dst", userId)
        outerObjectMessage.put("src", ourID)
        outerObjectMessage.put("payload", payloadMessage)
        outerObjectMessage.put("type", "OFFER")

        webSocket.send(outerObjectMessage.toString())
    }

    //function to begin calling
    fun startCall(userId: String) {
        theirID = userId

        val mediaConstraints1 = MediaConstraints.KeyValuePair(
            "kRTCMediaConstraintsOfferToReceiveAudio",
            "kRTCMediaConstraintsValueTrue"
        )
        val mediaConstraints2 = MediaConstraints.KeyValuePair(
            "kRTCMediaConstraintsOfferToReceiveVideo",
            "kRTCMediaConstraintsValueTrue"
        )
        val mediaConstraints3 = MediaConstraints.KeyValuePair(
            "kRTCMediaStreamTrackKindVideo",
            "kRTCMediaConstraintsValueTrue"
        )

        val mediaConstraints4 = MediaConstraints.KeyValuePair(
            "DtlsSrtpKeyAgreement",
            "kRTCMediaConstraintsValueTrue"
        )

        val mediaConstraints5 = MediaConstraints.KeyValuePair("setup", "actpass")
        val mediaConstraints6 = MediaConstraints.KeyValuePair(
            "video",
            "true"
        )
        val mediaConstraints = MediaConstraints()


        mediaConstraints.mandatory.add(mediaConstraints1)
        mediaConstraints.mandatory.add(mediaConstraints2)
        mediaConstraints.mandatory.add(mediaConstraints3)
        mediaConstraints.mandatory.add(mediaConstraints4)
        mediaConstraints.mandatory.add(mediaConstraints5)
        mediaConstraints.mandatory.add(mediaConstraints6)
        mediaID = "31480asdf33"
        sdpObserver = object: SdpObserver {
            @OptIn(UnstableApi::class)
            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.d("SignalingClient", "createSDP Success")
                createAndSendCallMessage(p0, userId,mediaID)
                var sdpObserver2 = object: SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {


                        Log.d("SignalingClient", "onCreateSucess")
                    }


                    override fun onSetSuccess() {
                        Log.d("SignalingClient", "Set Peer Success")
                    }

                    override fun onCreateFailure(p0: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onSetFailure(p0: String?) {
                        TODO("Not yet implemented")
                    }

                }
                localPeer.setLocalDescription(sdpObserver2, p0)


            }

            @OptIn(UnstableApi::class)
            override fun onSetSuccess() {
               Log.d("SignalingClient", "Remote SDP Call set Success")
            }

            override fun onCreateFailure(p0: String?) {
                TODO("Not yet implemented")
            }

            override fun onSetFailure(p0: String?) {
                TODO("Not yet implemented")
            }

        }

        var offerSDP = localPeer.createOffer(sdpObserver, mediaConstraints)
        //localPeer.setLocalDescription(sdpObserver)

    }

    init {
        buildVideoSenders(context, url)

        var videoCamera = VideoCameraSetup(context, localPeer, factory, rootEGL)
        var sessionManager = SessionManager(context)
        val authToken = sessionManager.fetchAuthToken()

        if (httpUrl != null) {
            Log.d("SignalingClient", "URL IS $httpUrl")
            Log.d("SignalingClient", "isHttps?: " + httpUrl)
            val request = Request.Builder().url(httpUrl).addHeader("Authorization",
                authToken.toString()
            ).build()
            Log.d("SignalingClient", "auth token" + authToken.toString())
            Log.d("SignalingClient", request.method)


            // Create the WebSocket listener
            webSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.d("SignalingClient", "WebSocket opened: ${response.message}")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.d("SignalingClient", "Message received: $text")
                    val jsonMessage = JSONObject(text)
                    if (text.contains("OFFER")) {
                        theirID = jsonMessage.get("src").toString()
                        onCallRecieved()
                    }
                    if (text.contains("OFFER") && text.contains("media")) {

                        Log.d("SignalingClient", "We received an OFFER")
                        //val messageSplit = text.split(",")
                        Log.d("Signaling client", jsonMessage.get("payload").toString())
                        theirID = jsonMessage.get("src").toString()

                        Log.d("Signaling Client", "TheirID: $theirID")
                        val payload = jsonMessage.get("payload") as JSONObject
                        val sdpMessage = payload.get("sdp") as JSONObject
                        mediaID = payload.get("connectionId").toString()
                        Log.d("Signaling Client", "mediaID: $mediaID")
                        val theirSDP = sdpMessage.get("sdp").toString()
                        val sessionDescription =
                            SessionDescription(SessionDescription.Type.OFFER, theirSDP)
                        Log.d("signaling client", "sdp is: $theirSDP")
                        localPeer.setRemoteDescription(remoteObserver, sessionDescription)

                        //Log.d("Signaling Client", "set remote SDP " + localPeer.toString())
                    }

                    if (text.contains("ANSWER") && text.contains("media")) {

                        Log.d("SignalingClient", "We received an ANSWER")
                        //val messageSplit = text.split(",")
                        Log.d("Signaling client", jsonMessage.get("payload").toString())
                        theirID = jsonMessage.get("src").toString()

                        Log.d("Signaling Client", "TheirID: $theirID")
                        val payload = jsonMessage.get("payload") as JSONObject
                        val sdpMessage = payload.get("sdp") as JSONObject
                        mediaID = payload.get("connectionId").toString()
                        Log.d("Signaling Client", "mediaID: $mediaID")
                        val theirSDP = sdpMessage.get("sdp").toString()
                        val sessionDescription =
                            SessionDescription(SessionDescription.Type.ANSWER, theirSDP)
                        Log.d("signaling client", "sdp is: $theirSDP")
                        Log.d("SignalingClient", "Setting our reemote SDP To their answer")
                        localPeer.setRemoteDescription(sdpObserver, sessionDescription)

                        //Log.d("Signaling Client", "set remote SDP " + localPeer.toString())
                    }

                    if (text.contains("CANDIDATE")) {
                        val payload = jsonMessage.get("payload") as JSONObject
                        Log.d("Payload Message", payload.toString())
                        val candidateMsg = payload.get("candidate") as JSONObject
                        Log.d(
                            "CANDIDATE MESSAGE",
                            "Candidate variable contains: $candidateMsg"
                        )
                        val sdpMid = candidateMsg.get("sdpMid").toString()
                        val sdpMLineIndex = candidateMsg.get("sdpMLineIndex").toString()
                        val candidate = candidateMsg.get("candidate").toString()
                        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex.toInt(), candidate)

                        val msg = JSONObject()
                        msg.accumulate("type", "CANDIDATE")
                        msg.accumulate("payload", payload)
                        msg.accumulate("dst", theirID)



                        if (!isReadyToAddIceCandidate) {
                            candidatesList.add(iceCandidate)
                            candidateMessagesToSend.add(msg.toString())
                            Log.d("Signaling client", "Adding ICE CANDDIATE TO LIST")
                        } else {
                            Log.d(
                                "IceCANDIDATE",
                                localPeer.addIceCandidate(iceCandidate).toString()
                            )
                            //webSocket.send(msg.toString())
                        }


                    }
                }


                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    Log.d("SignalingClient", "WebSocket closing: $reason (code $code)")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.d("SignalingClient", "WebSocket closed: $reason (code $code)")
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    super.onFailure(webSocket, t, response)
                    Log.e("SignalingClient", "WebSocket failure: ${t.message}")
                }
            }
            webSocket = client.newWebSocket(request, webSocketListener)

        } else {
            Log.d("Signaling Client", "url is null")
        }


    }

}