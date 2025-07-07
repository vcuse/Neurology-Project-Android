package com.example.neurology_project_android

import android.content.Context
import android.content.SharedPreferences
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Use the PersistentCookieJar to automatically persist cookies
    private val cookieJar: ClearableCookieJar = PersistentCookieJar(
        SetCookieCache(),
        SharedPrefsCookiePersistor(context)
    )

    // Shared OkHttpClient for your whole app
    val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    fun saveAuthToken(token: String, username: String) {
        val editor = prefs.edit()
        editor.putString("TOKEN", token)
        editor.putString("USERNAME", username)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove("TOKEN")
        editor.remove("USERNAME")
        editor.putBoolean("IS_LOGGED_IN", false)
        editor.apply()
    }

    fun fetchAuthToken(): String? = prefs.getString("TOKEN", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)
    fun fetchUsername(): String? = prefs.getString("USERNAME", null)

    fun logout() {
        prefs.edit().clear().apply()
        cookieJar.clear()
    }
}
