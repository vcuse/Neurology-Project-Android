package com.example.neurology_project_android

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neurology_project_android.BuildConfig.API_POST_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
@HiltViewModel
class SavedNIHFormViewModel @Inject constructor(private val client: SignalingClient) : ViewModel() {

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
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend fun loadExistingForm(formId: String?, client: OkHttpClient){
        val postURL = API_POST_URL
        val request = Request.Builder()
            .url(postURL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Action", "login")
            .build()
        // 2. Launch a coroutine using the ViewModel's scope

            // 3. Call the suspend function (FormManager.loadForm must be suspend!)
            // Note: Since loadForm is a synchronous call wrapped in IO Dispatchers,
            // you should ideally call it directly inside the coroutine.

            // Assuming FormManager.loadForm is correctly updated to be a suspend function:
            val existingForm = FormManager.loadForm(formId!!, client)

            // 4. Update the state based on the result
            if (existingForm != null) {
                // Now call the function to map the NIHForm fields back to the state variables
                mapFormToState(existingForm)
                Log.d("NIHFormViewModel", "Successfully loaded form for ID: $formId")
            } else {
                Log.e("NIHFormViewModel", "Failed to load form for ID: $formId")
                // Handle loading failure (e.g., set an error state)
            }




            //client.submitToServer("requestSingleForm", JSONObject().put("form_id", formId))
    }

    /**
     * Maps the properties of a fully loaded NIHForm object back into the ViewModel's mutable state.
     */
    private fun mapFormToState(existingForm: NIHForm) {
        // This function should be called on the Main thread (which viewModelScope.launch defaults to).

        patientName.value = existingForm.patientName ?: ""

        val loadedScores = listOf(
            existingForm.item1aLocLevel,
            existingForm.item1bLocCommands,
            existingForm.item1cLocBestGaze,
            existingForm.item2BestMotorGaze,
            existingForm.item3Visual,
            existingForm.item4FacialPalsy,
            existingForm.item5LeftArmMotor,
            existingForm.item6RightArmMotor,
            existingForm.item7LeftLegMotor,
            existingForm.item8RightLegMotor,
            existingForm.item9Ataxia,
            existingForm.item10Sensory,
            existingForm.item11Language,
            existingForm.item12Dysarthria,
            existingForm.item13ExtinctionInattention
        )

        if (loadedScores.size == 15) {
            itemScores.value = loadedScores
        } else {
            Log.e("NIHFormViewModel", "Loaded scores list size is not 15, resetting scores.")
            // You may choose to not reset and use the partial data, depending on requirements.
            // itemScores.value = List<Int?>(15) { null }
        }
    }

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

    }

    /**
     * Resets the submission status, typically called after the UI has shown a message.
     */
    fun resetSubmissionStatus() {
        _submissionStatus.value = SubmissionStatus.Idle
    }
}

private fun SignalingClient.getUsername() {
    TODO("Not yet implemented")
}

