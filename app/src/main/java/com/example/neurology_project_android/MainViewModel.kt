// In: app/src/main/java/com/example/neurology_project_android/MainViewModel.kt

package com.example.neurology_project_android

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neurology_project_android.BuildConfig.API_GET_ID_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val signalingClient: SignalingClient
) : ViewModel() {

    // --- State Management ---
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Start the process as soon as the ViewModel is created
        initialize()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun initialize() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
//            val userId = fetchUserId()
            val userId = "thera"
            if (userId == "unknown") {
//                _uiState.value = MainUiState.Error("Could not fetch user ID.")
//                return@launch
            }


            // 1. Connect the client
//            signalingClient.connectClient(userId)

            // 2. Start collecting the peer list flow
            signalingClient.peerListFlow
                .catch { exception ->
                    // Handle any errors from the flow
                    _uiState.value = MainUiState.Error(exception.message ?: "An error occurred")
                }
                .collect { peers ->
                    Log.e("MainViewModel", "Received peer list: $peers")

                    // 3. Every time a new peer list arrives, update the UI state
                    // The current user is already filtered on the server in this example,
                    // but filtering here is a good safeguard.
                    val otherPeers = peers.filter { it != userId }
                    _uiState.value = MainUiState.Success(userId, otherPeers)
                }
        }
    }
    // Expose repository methods for the UI to call
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun joinRoom(targetUserId: String) {
        signalingClient.joinRoom(targetUserId)
    }

    // This logic is now self-contained within the ViewModel
    private suspend fun fetchUserId(): String {
        return withContext(Dispatchers.IO) {
            try {
                val idUrl = API_GET_ID_URL
                val client = OkHttpClient()
                val request = Request.Builder().url(idUrl).build()
                val response = client.newCall(request).execute()
                response.body?.string() ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun connectSignalingClient() {
        signalingClient.connectClient()
    }
}

// Sealed interface to represent the different UI states
sealed interface MainUiState {
    object Loading : MainUiState
    data class Success(val userId: String, val peers: List<String>) : MainUiState
    data class Error(val message: String) : MainUiState
}
