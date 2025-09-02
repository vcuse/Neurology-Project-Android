package com.example.neurology_project_android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SavedNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val form = NIHForm(
            id = intent.getIntExtra("formId", -1),
            patientName = intent.getStringExtra("patientName") ?: "",
            dob = intent.getStringExtra("dob") ?: "",
            date = intent.getStringExtra("date") ?: "",
            formData = intent.getStringExtra("formData") ?: "",
            username = intent.getStringExtra("username") ?: ""
        )

        setContent {
            SavedNIHFormScreen(form)
        }
    }
}

@Composable
fun SavedNIHFormScreen(form: NIHForm) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val questions = remember { StrokeScaleQuestions.questions }
    val selectedOptions = remember { mutableStateListOf<Int?>().apply { repeat(questions.size) { add(null) } } }

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(form) {
        val values = form.formData.map { c -> c.toString().toIntOrNull() ?: 9 }
        values.forEachIndexed { index, score ->
            selectedOptions[index] = if (score != 9) score else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Back Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "< Back",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .clickable { (context as? ComponentActivity)?.finish() }
                    .padding(8.dp)
            )
        }

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NIH Stroke Scale Form",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Patient Name: ${form.patientName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "DOB: ${form.dob}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = "Date: ${form.date}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // Question list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(questions) { question ->
                if (isEditing) {
                    QuestionCard(question, selectedOptions)
                } else {
                    ReadOnlyQuestionCard(question, selectedOptions)
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (isEditing) {
                        val updatedForm = NIHForm(
                            id = form.id,
                            patientName = form.patientName,
                            dob = form.dob,
                            date = form.date,
                            formData = selectedOptions.joinToString("") { (it ?: 9).toString() },
                            username = form.username
                        )
                        FormManager.updateForm(updatedForm, sessionManager.client) { success ->
                            (context as? ComponentActivity)?.runOnUiThread {
                                if (success) {
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    Toast.makeText(context, "Failed to update form", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        isEditing = true
                        isEditing = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (isEditing) "Done" else "Update", color = Color.Black)
            }

            Button(
                onClick = {
                    FormManager.deleteForm(form.id, form.username, sessionManager.client) { success ->
                        (context as? ComponentActivity)?.runOnUiThread {
                            if (success) {
                                (context as? ComponentActivity)?.finish()
                            } else {
                                Toast.makeText(context, "Failed to delete form", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Delete", color = Color.White)
            }
        }
    }
}

@Composable
fun ReadOnlyQuestionCard(question: StrokeScaleQuestion, selectedOptions: List<Int?>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = question.questionHeader,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (!question.subHeader.isNullOrEmpty()) {
            Text(
                text = question.subHeader,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        question.options.forEachIndexed { index, option ->
            val isSelected = selectedOptions[question.id] == index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(if (isSelected) Color(0xFFD1C4E9) else Color(0xFFF3E5F5))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = option.title)
                Text(text = if (option.score > 0) "+${option.score}" else "${option.score}")
            }
        }
    }
}
