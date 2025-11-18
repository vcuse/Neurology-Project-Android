package com.example.neurology_project_android

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// --- FIX 1: Import the new models ---
import com.example.neurology_project_android.NIHFormModel
import com.example.neurology_project_android.FormQuestion
import com.example.neurology_project_android.NewNIHFormViewModel
import com.example.neurology_project_android.SubmissionStatus
import dagger.hilt.android.AndroidEntryPoint

// --- You can remove the old StrokeScaleQuestions import if it exists ---
 // This annotation tells Hilt to manage dependencies for this Activity
@AndroidEntryPoint
class NewNIHFormActivity : ComponentActivity() {

    // 1. Get the ViewModel directly from Hilt.
    // The `by viewModels()` delegate handles everything for you.
    private val viewModel: NewNIHFormViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hilt provides the ViewModel and its dependencies automatically.
        // No manual setup needed.
        setContent {
            // 2. Simply pass the Hilt-provided ViewModel to your screen.
            NewNIHFormScreen(viewModel = viewModel)
        }
    }

    // This is the Composable function your Activity is trying to call.
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun NewNIHFormScreen(viewModel: NewNIHFormViewModel) {
        // 1. Observe state directly from the ViewModel
        val patientName by viewModel.patientName
        val itemScores by viewModel.itemScores
        val submissionStatus by viewModel.submissionStatus.collectAsState()
        val context = LocalContext.current

        // 2. React to changes in submissionStatus (e.g., show a toast, navigate away)
        LaunchedEffect(submissionStatus) {
            when (val status = submissionStatus) {
                is SubmissionStatus.Success -> {
                    Toast.makeText(context, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
                    // You could finish the activity upon success
                    // (context as? android.app.Activity)?.finish()
                    viewModel.resetSubmissionStatus()
                }
                is SubmissionStatus.Error -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                    viewModel.resetSubmissionStatus()
                }
                else -> { /* Do nothing for Idle or Loading states here */ }
            }
        }

        // 3. Define the UI structure
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "New NIH Stroke Scale Form",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = patientName,
                // Send user input events up to the ViewModel
                onValueChange = { viewModel.onPatientNameChange(it) },
                label = { Text("Patient Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // A scrollable list for all the questions
            LazyColumn(modifier = Modifier.weight(1f)) {
                // --- FIX 2: Use the new NIHFormModel instead of StrokeScaleQuestions ---
                itemsIndexed(NIHFormModel.questions) { index, question ->
                    QuestionCard(
                        question = question,
                        // Get the currently selected score for this question from the ViewModel's state
                        selectedScore = itemScores.getOrNull(index),
                        // When an option is clicked, notify the ViewModel with the question index and the option's score
                        onOptionClick = { score ->
                            viewModel.onScoreSelected(index, score)
                        }
                    )
                }
            }

            Button(
                // When the button is clicked, call the submitForm function on the ViewModel
                onClick = { viewModel.submitForm() },
                // Disable the button while the form is submitting
                enabled = submissionStatus != SubmissionStatus.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (submissionStatus == SubmissionStatus.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Form")
                }
            }
        }
    }

    /**
     * A reusable Composable for displaying a single question, its options, and highlighting the selection.
     */
    @Composable
    fun QuestionCard(question: FormQuestion, selectedScore: Int?, onOptionClick: (Int) -> Unit) { // --- FIX 3: Use FormQuestion class ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // --- FIX 4: Use properties from the new FormQuestion data class ---
                Text(text = question.questionText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (question.instructionText.isNotEmpty()) {
                    Text(
                        text = question.instructionText,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Create a clickable row for each answer option
                question.options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                // Highlight the row if its score matches the selected score
                                if (selectedScore == option.score) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .clickable { onOptionClick(option.score) } // Pass the option's actual score up
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- FIX 5: Use properties from the new FormOption data class ---
                        Text(text = option.displayText, modifier = Modifier.weight(1f))
                        Text(text = "${option.score}")
                    }
                }
            }
        }
    }
}

// The factory for your ViewModel
class NewNIHFormViewModelFactory(private val neurologyRepository: SignalingClient) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewNIHFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewNIHFormViewModel(neurologyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
