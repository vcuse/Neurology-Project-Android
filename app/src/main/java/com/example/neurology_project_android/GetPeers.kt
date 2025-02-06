package com.example.neurology_project_android

import android.util.Log
import okhttp3.*
import okio.IOException
import kotlin.concurrent.fixedRateTimer

class GetPeers(private val onPeersFetched: (List<String>) -> Unit) {
    private val url = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/peers"
    private val client = OkHttpClient()

    init {
        // Fetch peers immediately
        fetchPeers()

        // Start a timer to fetch peers every 2 seconds
        fixedRateTimer("fetchPeersTimer", false, 0L, 2000L) {
            fetchPeers()
        }
    }

    private fun fetchPeers() {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Request", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        val peerList = body
                            .trim()
                            .removeSurrounding("[", "]") // Remove JSON array brackets
                            .split(",")
                            .map { it.trim().removeSurrounding("\"") } // Remove extra quotes

                        onPeersFetched(peerList)
                    }
                } else {
                    Log.d("Response", "Request failed: ${response.code}")
                }
            }
        })
    }
}
