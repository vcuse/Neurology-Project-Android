package com.example.neurology_project_android

import androidx.annotation.OptIn
import androidx.constraintlayout.motion.widget.Debug
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.firebase.database.connection.ConnectionContext
import com.google.firebase.database.tubesock.WebSocket
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener

class SignalingClient @OptIn(UnstableApi::class) constructor
    (url: String) {

    init {

        val client = OkHttpClient().newBuilder().build()
        val httpUrl = url.toHttpUrlOrNull()
        if(httpUrl != null){
           Log.d("SignalingClient","URL IS " + httpUrl.host)
            Log.d("SignalingClient", "isHttps?: " + httpUrl.isHttps)
            val request = Request.Builder().url(httpUrl).build()
            Log.d("SignalingClient",  request.method)

            // Create the WebSocket listener
            val webSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.d("SignalingClient", "WebSocket opened: ${response.message}")
                }

                override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.d("SignalingClient", "Message received: $text")
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