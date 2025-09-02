package com.example.neurology_project_android

// Create a mock SessionManager for previews
class MockSessionManager : ISessionManager {
    override val client: Any = Any() // A dummy object
    override fun fetchUsername(): String? = "Simulated User"
}