package com.example.neurology_project_android

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * SessionManager
 * 
 * Handles all API operations for sessions:
 */
object SessionManagerApi {

    private const val BASE_URL = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/post"

    /**
     * Fetch all sessions for a specific user
     * 
     * @param username The username to fetch sessions for
     * @param client OkHttpClient for making network requests
     * @return List of Session objects
     */
    suspend fun fetchSessionsForUser(
        username: String,
        client: OkHttpClient
    ): List<Session> = withContext(Dispatchers.IO) {
        val sessions = mutableListOf<Session>()

        val json = JSONObject().apply {
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(BASE_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "getUsersSessions")  // Action header for backend to identify request type
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                val jsonArray = JSONArray(bodyString)

                // Parse each session from the JSON array
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    sessions.add(
                        Session(
                            id = item.getInt("id"),
                            sessionId = item.getString("session_id"),
                            date = item.getString("date"),
                            time = item.getString("time"),
                            attendingDoctors = item.getString("attending_doctors"),
                            patientName = item.getString("patient_name"),
                            patientDob = item.getString("patient_dob"),
                            formIds = item.getString("form_ids"),
                            videoUrl = item.getString("video_url"),
                            videoSize = item.getString("video_size"),
                            aiFormOutput = item.getString("ai_form_output"),
                            username = item.getString("username")
                        )
                    )
                }
            } else {
                Log.e("SESSION_MANAGER", "Server error: ${response.code}")
            }
        } catch (e: IOException) {
            Log.e("SESSION_MANAGER", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SESSION_MANAGER", "Parsing error: ${e.message}")
        }

        return@withContext sessions
    }

    /**
     * Submit a new session to the server
     * 
     * @param session The Session object to submit
     * @param client OkHttpClient for making network requests
     * @param onResult Callback with success/failure result
     */
    fun submitSessionToServer(session: Session, client: OkHttpClient, onResult: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("sessionId", session.sessionId)
            put("date", session.date)
            put("time", session.time)
            put("attendingDoctors", session.attendingDoctors)
            put("patientName", session.patientName)
            put("patientDob", session.patientDob)
            put("formIds", session.formIds)
            put("videoUrl", session.videoUrl)
            put("videoSize", session.videoSize)
            put("aiFormOutput", session.aiFormOutput)
            put("username", session.username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(BASE_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "submitSession")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SESSION_SUBMIT", "Failed: ${e.message}")
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("SESSION_SUBMIT", "Response code: ${response.code}")
                onResult(response.isSuccessful)
            }
        })
    }

    /**
     * Delete a session from the server
     * 
     * @param sessionId The ID of the session to delete
     * @param username The username of the session owner
     * @param client OkHttpClient for making network requests
     * @param callback Callback with success/failure result
     */
    fun deleteSession(sessionId: Int, username: String, client: OkHttpClient, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id", sessionId)
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(BASE_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "deleteSession")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DELETE_SESSION", "Failed: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DELETE_SESSION", "Response code: ${response.code}")
                callback(response.isSuccessful)
            }
        })
    }
}
