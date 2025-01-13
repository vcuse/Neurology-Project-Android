package com.example.neurology_project_android

import android.content.Context
import android.os.Build
import android.util.JsonWriter
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.Debug
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.common.util.JsonUtils
import com.google.firebase.database.connection.ConnectionContext
import com.google.firebase.database.tubesock.WebSocket
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocketListener
import okhttp3.internal.http.HttpMethod
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription


data class SDP(val type: String, val typeAns: String, val sdp: String, val sdpValue: String)

class SignalingClient @OptIn(UnstableApi::class) constructor
    (url: String, context: Context) {

        lateinit var localPeer : PeerConnection
        lateinit var httpUrl : HttpUrl
        lateinit var theirID : String
        lateinit var webSocketListener: WebSocketListener
        lateinit var client: OkHttpClient
        lateinit var mediaID: String

    private val remoteObserver = object: SdpObserver{
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            @OptIn(UnstableApi::class)
            override fun onCreateSuccess(sdp: SessionDescription?) {
                Log.d("RemoteObserver", "Answer SDP was Created")

                var sdpMsg = JSONObject()
                sdpMsg .append("type", "answer")
                sdpMsg. append("sdp",
                    sdp?.description
                )

                var payload = JSONObject()
                payload.append("sdp", sdpMsg)
                payload.append("type", "media")
                payload.append("browser","firefox")
                payload.append("connectionId",mediaID)

                var msg = JSONObject()
                msg.append("type", "ANSWER")
                msg.append("payload", payload)
                msg.append("dst",theirID)


                var request = msg.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                Log.d("Remote Observer", "payload: " + msg.toString())

                var newRequest = Request.Builder().url(httpUrl).method("POST", request).build()
                client.newCall(newRequest)

            }

            @OptIn(UnstableApi::class)
            override fun onSetSuccess() {
                Log.d("SignalingClient", "RemoteSDP set succesfully")
                var mediaConstraints1 = MediaConstraints.KeyValuePair("kRTCMediaConstraintsOfferToReceiveAudio","kRTCMediaConstraintsValueTrue")
                var mediaConstraints2 = MediaConstraints.KeyValuePair("kRTCMediaConstraintsOfferToReceiveVideo", "kRTCMediaConstraintsValueTrue")
                var mediaConstraints3 = MediaConstraints.KeyValuePair("kRTCMediaStreamTrackKindVideo", "kRTCMediaConstraintsValueTrue")
                var mediaConstraints = MediaConstraints()
                mediaConstraints.mandatory.add(mediaConstraints1)
                mediaConstraints.mandatory.add(mediaConstraints2)
                mediaConstraints.mandatory.add(mediaConstraints3)
                localPeer.createAnswer(this,mediaConstraints )
            }

            override fun onCreateFailure(error: String?) {
                TODO("Not yet implemented")
            }

            override fun onSetFailure(error: String?) {
                TODO("Not yet implemented")
            }

        }

    private val peerConnObserver = object : PeerConnection.Observer {
        @OptIn(UnstableApi::class)
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            Log.d("Signaling State", "SignalingChange " + p0.toString())

            return

        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            TODO("Not yet implemented")
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            TODO("Not yet implemented")
        }

        override fun onIceCandidate(p0: IceCandidate?) {
            TODO("Not yet implemented")
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onAddStream(p0: MediaStream?) {
            Log.d("PeerConnection", "MediaStream added" )

        }

        override fun onRemoveStream(p0: MediaStream?) {
            TODO("Not yet implemented")
        }

        override fun onDataChannel(p0: DataChannel?) {
            TODO("Not yet implemented")
        }

        @OptIn(UnstableApi::class)
        override fun onRenegotiationNeeded() {
            Log.d("PeerConnection", "Renegotation Needed")

        }

    }


    init {


        var options = PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        var factoryInit = PeerConnectionFactory.initialize(options)
        var factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        val server = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        var config = PeerConnection.RTCConfiguration(listOf(server))
        localPeer = factory.createPeerConnection(config, peerConnObserver)!!
        client = OkHttpClient().newBuilder().build()
        httpUrl = url.toHttpUrlOrNull()!!
        if(httpUrl != null){
            Log.d("SignalingClient","URL IS " + httpUrl.host)
            Log.d("SignalingClient", "isHttps?: " + httpUrl.isHttps)
            var request = Request.Builder().url(httpUrl).build()
            Log.d("SignalingClient",  request.method)


            // Create the WebSocket listener
            webSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.d("SignalingClient", "WebSocket opened: ${response.message}")
                }

                override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.d("SignalingClient", "Message received: $text")
                    val jsonMessage = JSONObject(text)
                    if(text.contains("OFFER") && text.contains("media")){
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
                        var sessionDescription = SessionDescription(SessionDescription.Type.OFFER, theirSDP)
                        Log.d("signaling client", "sdp is: " + theirSDP)
                        localPeer?.setRemoteDescription(remoteObserver,sessionDescription)
                        Log.d("Signaling Client", "set remote SDP " + localPeer?.remoteDescription?.description.toString())
                    }
                }



                override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    Log.d("SignalingClient", "WebSocket closing: $reason (code $code)")
                }

                override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.d("SignalingClient", "WebSocket closed: $reason (code $code)")
                }

                override fun onFailure(
                    webSocket: okhttp3.WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    super.onFailure(webSocket, t, response)
                    Log.e("SignalingClient", "WebSocket failure: ${t.message}")
                }
            }
            client.newWebSocket(request, webSocketListener)

        }
        else {
            Log.d("Signaling Client", "url is null")
        }


    }

}