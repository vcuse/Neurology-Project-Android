package com.example.neurology_project_android

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

class SignalingRepository(private val client: SignalingClient) {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun submitToServer(header: String, data: JSONObject){
        client.submitToServer(header, data)
    }
}