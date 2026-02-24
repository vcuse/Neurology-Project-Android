package com.example.neurology_project_android

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

object FormManager {

    private const val POST_URL = "https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/post"
    var TAG = "FormManager"
    fun submitFormToServer(form: NIHForm, client: OkHttpClient, onResult: (Boolean) -> Unit) {
        val jsonString = Gson().toJson(form)
        Log.d(TAG, "values are " + form.toString())
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonString
        )

        val request = Request.Builder()
            .url(POST_URL)
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

    suspend fun loadForm(formId: String, client: OkHttpClient): NIHForm  = withContext(Dispatchers.IO) {
        Log.d(TAG, "formid is ${formId}")

        val json = JSONObject().apply {
            put("form_id", formId)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString())
        val request = Request.Builder()
            .url(POST_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "requestSingleForm")
            .build()

        try {
            // 🔑 Fix 2: Keep the synchronous execute() call, but it's now safe
            // because it's wrapped in withContext(Dispatchers.IO).
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {

                    val bodyString = response.body?.string().toString()
                    Log.d(TAG, "forms reponse ${bodyString}")
                    val formsList = decodeSingleForm(bodyString!!)
                    return@withContext formsList
                } else {
                    Log.e("FORM_MANAGER", "Server error: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("FORM_MANAGER", "Error opening form: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("FORM_MANAGER", "Error opening form: ${e.message}")
        }

        // Return null instead of an empty NIHForm() on failure
        return@withContext NIHForm()
    }

    suspend fun fetchFormsForUser(
        username: String,
        client: OkHttpClient
    ): List<NIHForm> = withContext(Dispatchers.IO) {
        val forms = mutableListOf<NIHForm>()
        Log.d(TAG, "FETCHING FORMS FOR USER")
        val json = JSONObject().apply {
            put("username", username)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(POST_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "getUsersForms")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {

                val bodyString = response.body?.string()
                Log.d(TAG, "forms reponse ${bodyString}")
                val formsList = decodeMultipleForms(bodyString!!)
                return@withContext formsList
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
            .url(POST_URL)
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
            .url(POST_URL)
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

    fun decodeMultipleForms(jsonString: String): List<NIHForm> {
        val jsonParser = Json {
            // Essential for robustness: ignore fields present in JSON but not in your data class
            ignoreUnknownKeys = true
            // Handles cases where null is present for a property with a default value
            coerceInputValues = true
        }
        Log.e(TAG, "Decoding forms")
        return try {
            // The key change: specify List<NIHForm> as the target type
            val formList: List<NIHForm> = jsonParser.decodeFromString(jsonString)
            formList
        } catch (e: Exception) {
            // Log the error and return an empty list or throw a custom exception
            println("Error decoding list of forms: $e")
            emptyList()
        }
    }

    fun decodeSingleForm(jsonString: String): NIHForm {
        val jsonParser = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        Log.e(TAG, "Attempting to decode single form from array.")

        return try {
            // 1. Decode as a List<NIHForm> because the JSON starts with '['
            val formList: List<NIHForm> = jsonParser.decodeFromString(jsonString)

            // 2. Safely return the first element of the list
            if (formList.isNotEmpty()) {
                formList.first()
            } else {
                Log.e(TAG, "Successfully decoded array, but the list was empty.")
                NIHForm() // Return a default empty form if the list is empty
            }

        } catch (e: Exception) {
            // This handles exceptions like malformed JSON or other parsing errors
            Log.e(TAG, "Error decoding single form: ${e.message}")
            NIHForm() // Return a default empty form on failure
        }
    }
}
