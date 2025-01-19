package com.example.neurology_project_android

import android.content.Context
import android.opengl.GLSurfaceView.EGLContextFactory
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.common.base.Objects
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.IOException
import org.json.JSONObject
import org.w3c.dom.ls.LSSerializer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.HardwareVideoDecoderFactory
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoCodecInfo
import org.webrtc.VideoDecoder
import org.webrtc.VideoDecoderFactory
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import kotlin.concurrent.thread


data class SDP(val type: String, val typeAns: String, val sdp: String, val sdpValue: String)

class SignalingClient @OptIn(UnstableApi::class) constructor
    (url: String, context: Context) {

    lateinit var localPeer: PeerConnection
    lateinit var httpUrl: HttpUrl
    lateinit var theirID: String
    lateinit var webSocketListener: WebSocketListener
    lateinit var client: OkHttpClient
    lateinit var mediaID: String
    lateinit var webSocket: WebSocket
    lateinit var localSDP: SessionDescription

    var candidatesList = ArrayList<IceCandidate>()
    var isReadyToAddIceCandidate: Boolean = false
    var candidateMessagesToSend = ArrayList<String>()

    fun setlocalSDP(){
        localPeer.setLocalDescription(remoteObserver, localSDP)
    }

    private var remoteObserver = object : SdpObserver {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @OptIn(UnstableApi::class)
        override fun onCreateSuccess(sdp: SessionDescription?) {
            Log.d("RemoteObserver", "Answer SDP was Created")
            if (sdp != null) {
                localSDP = sdp
                setlocalSDP()
            }
            var sdpMsg = JSONObject()
            sdpMsg.accumulate("type", "answer")
            sdpMsg.accumulate(
                "sdp",
                sdp?.description
            )

            var payload = JSONObject()
            payload.accumulate("sdp", sdpMsg)
            payload.accumulate("type", "media")
            payload.accumulate("browser", "firefox")
            payload.accumulate("connectionId", mediaID)

            var msg = JSONObject()
            msg.accumulate("type", "ANSWER")
            msg.accumulate("payload", payload)
            msg.accumulate("dst", theirID)

            var formBody =
                FormBody.Builder().add("type", "ANSWER").add("payload", payload.toString())
                    .add("dst", theirID).build()


            var request = Request.Builder().url(httpUrl).post(formBody).build()

            webSocket.send(msg.toString())

            for(candidate in candidatesList){
                var status = localPeer.addIceCandidate(candidate)
                Log.d("Adding ICE CANDIDATE" , status.toString())
            }




            isReadyToAddIceCandidate = true

            for(candidate in candidateMessagesToSend){
                //webSocket.send(candidate)
            }
//                val callBack = object :Callback{
//                    override fun onFailure(call: Call, e: IOException) {
//                        Log.d("Callback", "Failure")
//
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        Log.d("Callback", "We got a response " + response.code)
//                    }
//
//                }
//                    var response = client.newCall(request).enqueue(callBack)
//
//                Log.d("Remote Observer", "Response code was: " + response)


        }

        @OptIn(UnstableApi::class)
        override fun onSetSuccess() {
            Log.d("SignalingClient", "RemoteSDP set succesfully")

            var mediaConstraints1 = MediaConstraints.KeyValuePair(
                "kRTCMediaConstraintsOfferToReceiveAudio",
                "kRTCMediaConstraintsValueTrue"
            )
            var mediaConstraints2 = MediaConstraints.KeyValuePair(
                "kRTCMediaConstraintsOfferToReceiveVideo",
                "kRTCMediaConstraintsValueTrue"
            )
            var mediaConstraints3 = MediaConstraints.KeyValuePair(
                "kRTCMediaStreamTrackKindVideo",
                "kRTCMediaConstraintsValueTrue"
            )

            var mediaConstraints4 =  MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "kRTCMediaConstraintsValueTrue")

            var mediaConstraints5 =  MediaConstraints.KeyValuePair("setup", "actpass")
            var mediaConstraints6 = MediaConstraints.KeyValuePair(
                "video",
                "true"
            )
            var mediaConstraints = MediaConstraints()


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

    var peerConnObserver = object : PeerConnection.Observer {
        @OptIn(UnstableApi::class)
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            if (p0 != null) {
                Log.d("Signaling State", "SignalingChange " + p0.name)
            }


        }

        @OptIn(UnstableApi::class)
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            Log.d("ICE Connection", p0.toString())

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
            Log.d("REC ICECandidate", p0.toString())
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onAddStream(p0: MediaStream?) {


            var mediaStreamTrack = p0?.audioTracks?.get(0)
            //val status = localPeer.addTrack(mediaStreamTrack)
            //Log.d("PeerConnection", "MediaStream added $status")

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


    init {

        val videoCodecInfo = VideoCodecInfo.H264_LEVEL_3_1
        var options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)
        val rootEGL = EglBase.create()

        val encoderFactory = DefaultVideoEncoderFactory(rootEGL.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(rootEGL.eglBaseContext)
        var factory = PeerConnectionFactory.builder().setVideoDecoderFactory(decoderFactory)
            .setVideoEncoderFactory(encoderFactory).createPeerConnectionFactory()

        val server =
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        var config = PeerConnection.RTCConfiguration(listOf(server))
        config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        config.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        config.iceTransportsType = PeerConnection.IceTransportsType.ALL




        localPeer = factory.createPeerConnection(config, peerConnObserver)!!

        client = OkHttpClient().newBuilder().build()
        httpUrl = url.toHttpUrlOrNull()!!

        val videoSource = factory.createVideoSource(false)
        val track = factory.createVideoTrack("video0", videoSource)
        //localPeer.addTrack(track)


        if (httpUrl != null) {
            Log.d("SignalingClient", "URL IS " + httpUrl.toString())
            Log.d("SignalingClient", "isHttps?: " + httpUrl.isHttps)
            var request = Request.Builder().url(httpUrl).build()
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
                    if (text.contains("OFFER")){
                        theirID = jsonMessage.get("src").toString()
                    }
                    if (text.contains("OFFER") && text.contains("media")) {
                        Log.d("SignalingClient", "We received an OFFER")
                        //val messageSplit = text.split(",")
                        Log.d("Signaling client", jsonMessage.get("payload").toString())
                        theirID = jsonMessage.get("src").toString()


                        Log.d("Signaling Client", "TheirID: " + theirID)
                        var payload = jsonMessage.get("payload") as JSONObject
                        val sdpMessage = payload.get("sdp") as JSONObject
                        mediaID = payload.get("connectionId").toString()
                        Log.d("Signaling Client", "mediaID: " + mediaID)
                        val theirSDP = sdpMessage.get("sdp").toString()
                        var sessionDescription =
                            SessionDescription(SessionDescription.Type.OFFER, theirSDP)
                        Log.d("signaling client", "sdp is: " + theirSDP)
                        localPeer.setRemoteDescription(remoteObserver, sessionDescription)
                        //Log.d("Signaling Client", "set remote SDP " + localPeer.toString())
                    }

                    if(text.contains("CANDIDATE")){
                        var payload = jsonMessage.get("payload") as JSONObject
                        Log.d("Payload Message", payload.toString())
                        var candidateMsg = payload.get("candidate") as JSONObject
                        Log.d("CANDIADTE MESSAGE", "Candidate vairable contains: " + candidateMsg.toString())
                        var sdpMid = candidateMsg.get("sdpMid").toString()
                        var sdpMLineIndex = candidateMsg.get("sdpMLineIndex").toString()
                        var candidate = candidateMsg.get("candidate").toString()
                        var iceCandidate = IceCandidate(sdpMid, sdpMLineIndex.toInt(), candidate)

                        var msg = JSONObject()
                        msg.accumulate("type", "CANDIDATE")
                        msg.accumulate("payload", payload)
                        msg.accumulate("dst", theirID)



                        if(!isReadyToAddIceCandidate) {
                            candidatesList.add(iceCandidate)
                            candidateMessagesToSend.add(msg.toString())
                        }
                        else
                        {
                            Log.d("IceCANDIDATE", localPeer.addIceCandidate(iceCandidate).toString())
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