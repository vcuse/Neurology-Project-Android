package com.example.neurology_project_android

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String, username: String) {
        val editor = prefs.edit()
        editor.putString("TOKEN", token)
        editor.putString("USERNAME", username)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    fun fetchAuthToken(): String? = prefs.getString("TOKEN", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)
    fun fetchUsername(): String? = prefs.getString("USERNAME", null)

    fun logout() {
        prefs.edit().clear().apply()
    }
}