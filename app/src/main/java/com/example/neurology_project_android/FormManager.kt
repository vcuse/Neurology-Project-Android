package com.example.neurology_project_android

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

object FormManager {

    var TAG = "FormManager"
    fun submitFormToServer(form: NIHForm, client: OkHttpClient, onResult: (Boolean) -> Unit) {
        val jsonString = Gson().toJson(form)
        Log.d(TAG, "values are " + form.toString())
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonString
        )

        val request = Request.Builder()
            .url("https://meechie.techkit.xyz:3016/key=peerjs/post")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "start_new_nihss_form")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, e.toString())
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, response.toString())
                onResult(response.isSuccessful)
            }
        })
    }

    suspend fun fetchFormsForUser(
        username: String,
        client: OkHttpClient
    ): List<NIHForm> = withContext(Dispatchers.IO) {
        val forms = mutableListOf<NIHForm>()

        val json = JSONObject().apply {
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://meechie.techkit.xyz:3016/key=peerjs/post")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "getUsersForms")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                val jsonArray = JSONArray(bodyString)

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
//                    forms.add(
//                        NIHForm(
//                            form_id = UUID.randomUUID(),
////                            patientName = item.getString("patient_name"),
////                            dob = item.getString("patient_dob"),
////                            date = item.getString("form_date"),
////                            formData = item.getString("results"),
//                            username = item.getString("username")
//                        )
//                    )
                }
            } else {
                Log.e("FORM_MANAGER", "Server error: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("FORM_MANAGER", "Error opening form: ${e.message}")
        }

        return@withContext forms
    }

    fun deleteForm(formId: UUID, username: String, client: OkHttpClient, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id", formId)
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://meechie.techkit.xyz:3016/key=peerjs/post")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "deleteForm")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DELETE", "Failed: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DELETE", "Response code: ${response.code}")
                callback(response.isSuccessful)
            }
        })
    }

    fun updateForm(form: NIHForm, client: OkHttpClient, onComplete: (Boolean) -> Unit) {
        val json = JSONObject().apply {
//            put("id", form.id)
//            put("patientName", form.patientName)
//            put("patientDob", form.dob)
//            put("formDate", form.date)
//            put("results", form.formData)
//            put("username", form.username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://meechie.techkit.xyz:3016/key=peerjs/post")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "updateForm")
            .build()

        client.newCall(request).enqueue(SimpleCallback("UPDATE", onComplete))
    }

    private fun SimpleCallback(tag: String, onComplete: (Boolean) -> Unit) = object : Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            Log.e(tag, "Request failed: ${e.message}")
            onComplete(false)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (response.isSuccessful) {
                Log.d(tag, "Request successful")
                onComplete(true)
            } else {
                Log.e(tag, "Server error: ${response.code}")
                onComplete(false)
            }
        }
    }
}
