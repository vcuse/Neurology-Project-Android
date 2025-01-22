package com.example.neurology_project_android

import android.util.Log
import okhttp3.*
import okio.IOException

class GetPeers {
    private val url = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/peers"
    var peers = ""
    init{
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Request", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    peers = response.body.string()
                    Log.d("Response", "Response: $peers")
                } else {
                    Log.d("Response", "Request failed: ${response.code}");
                }
            }
        })
    }
}