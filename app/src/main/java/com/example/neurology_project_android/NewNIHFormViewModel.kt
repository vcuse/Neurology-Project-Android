package com.example.neurology_project_android

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewNIHFormViewModel @Inject constructor(private val client: SignalingClient) : ViewModel() {

    // --- UI State Management ---

    // Holds the patient's name, exposed to the View.
    var patientName = mutableStateOf("")
        private set // The View can read, but only the ViewModel can write to it.

    // Holds the selected option index for each of the 15 NIHSS items.
    // The index in the list corresponds to the item number (e.g., index 0 is for item 1a).
    // A null value means no option has been selected for that item.
    var itemScores = mutableStateOf(List<Int?>(15) { null })
        private set

    // Exposes the current status of the form submission process to the UI.
    private val _submissionStatus = MutableStateFlow<SubmissionStatus>(SubmissionStatus.Idle)
    val submissionStatus = _submissionStatus.asStateFlow()

    // --- User Events ---

    /**
     * Called by the View when the patient name TextField changes.
     */
    fun onPatientNameChange(newName: String) {
        patientName.value = newName
    }

    /**
     * Called by the View when the user selects an answer for a specific NIHSS item.
     * @param itemIndex The index of the question (0-14).
     * @param score The selected score for that item.
     */
    fun onScoreSelected(itemIndex: Int, score: Int) {
        // Create a new list with the updated score
        val newScores = itemScores.value.toMutableList()
        if (itemIndex in newScores.indices) {
            newScores[itemIndex] = score
            itemScores.value = newScores
        }
    }

    fun automateQuestion(questionIndex: Int) {
        // TODO: implement automation logic later
        Log.d("NEWNIHFORMVIEWMODEL", "Automate clicked for question index = $questionIndex")
    }

    /**
     * Called by the View when the "Save" button is clicked.
     * It validates the input, builds the NIHForm object, and calls the repository to submit it.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun submitForm() {
//        // 1. Validate Input
//        if (patientName.value.isBlank()) {
//            _submissionStatus.value = SubmissionStatus.Error("Patient name cannot be empty.")
//            return
//        }
//        if (itemScores.value.any { it == null }) {
//            _submissionStatus.value = SubmissionStatus.Error("All NIHSS items must be answered.")
//            return
//        }

        _submissionStatus.value = SubmissionStatus.Loading

        // 2. Build the NIHForm object using the current state
        val scores = itemScores.value.mapNotNull { it } // Get a non-null list of scores
        val formToSubmit = NIHForm(

            patientName = patientName.value,
            // Map the scores from the list to the corresponding data class fields
            item1aLocLevel = scores.getOrNull(0),
            item1bLocCommands = scores.getOrNull(1),
            item1cLocBestGaze = scores.getOrNull(2),
            item2BestMotorGaze = scores.getOrNull(3),
            item3Visual = scores.getOrNull(4),
            item4FacialPalsy = scores.getOrNull(5),
            item5LeftArmMotor = scores.getOrNull(6),
            item6RightArmMotor = scores.getOrNull(7),
            item7LeftLegMotor = scores.getOrNull(8),
            item8RightLegMotor = scores.getOrNull(9),
            item9Ataxia = scores.getOrNull(10),
            item10Sensory = scores.getOrNull(11),
            item11Language = scores.getOrNull(12),
            item12Dysarthria = scores.getOrNull(13),
            item13ExtinctionInattention = scores.getOrNull(14),
            totalNihssScore = scores.sum()
            // Add other fields like username or date if available
        )
        val formJsonObject = JSONObject()
        // 1. Manually .put() each field into the JSONObject.
        // The keys are the strings you want in the final JSON, matching your @SerialName annotations.
        formJsonObject.put("patient_name", patientName.value)
        formJsonObject.put("item_1a_loc_level", scores.getOrNull(0))
        formJsonObject.put("item_1b_loc_commands", scores.getOrNull(1))
        formJsonObject.put("item_1c_loc_best_gaze", scores.getOrNull(2))
        formJsonObject.put("item_2_best_motor_gaze", scores.getOrNull(3))
        formJsonObject.put("item_3_visual", scores.getOrNull(4))
        formJsonObject.put("item_4_facial_palsy", scores.getOrNull(5))
        formJsonObject.put("item_5_left_arm_motor", scores.getOrNull(6))
        formJsonObject.put("item_6_right_arm_motor", scores.getOrNull(7))
        formJsonObject.put("item_7_left_leg_motor", scores.getOrNull(8))
        formJsonObject.put("item_8_right_leg_motor", scores.getOrNull(9))
        formJsonObject.put("item_9_ataxia", scores.getOrNull(10))
        formJsonObject.put("item_10_sensory", scores.getOrNull(11))
        formJsonObject.put("item_11_language", scores.getOrNull(12))
        formJsonObject.put("item_12_dysarthria", scores.getOrNull(13))
        formJsonObject.put("item_13_extinction_inattention", scores.getOrNull(14))
        formJsonObject.put("username", "thera")
        // Calculate and put the total score
        //formJsonObject.put("total_nihss_score", scores.mapNotNull { it }.sum())

        // 3. Launch a coroutine to call the repository
        viewModelScope.launch {
            try {
                var jsonString = formToSubmit.toJson()
                Log.d("NEWNIHFORMVIEWMODEL", "JSON: $jsonString")
                var stringToSubmit = JSONObject().put("payload", formJsonObject)
                var success = client.submitToServer("CREATEFORM", stringToSubmit)
                Log.d("NEWNIHFORMVIEWMODEL", "SUCCESS: $success")
                _submissionStatus.value = if (success) SubmissionStatus.Success else SubmissionStatus.Error("Failed to submit form to server.")

            } catch (e: Exception) {
                _submissionStatus.value = SubmissionStatus.Error("An error occurred: ${e.message}")
                Log.e("Error", "An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Resets the submission status, typically called after the UI has shown a message.
     */
    fun resetSubmissionStatus() {
        _submissionStatus.value = SubmissionStatus.Idle
    }
}


/**
 * A sealed interface to represent the different states of the form submission process,
 * making it easy for the UI to react to changes.
 */
sealed interface SubmissionStatus {
    object Idle : SubmissionStatus
    object Loading : SubmissionStatus
    object Success : SubmissionStatus
    data class Error(val message: String) : SubmissionStatus
}
