package com.example.neurology_project_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch

class SavedNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val formId = intent.getIntExtra("formId", -1)
        setContent {
            SavedNIHFormScreen(formId)
        }
    }
}

@Composable
fun SavedNIHFormScreen(formId: Int) {
    val context = LocalContext.current
    val nihFormDao = NIHFormDatabase.getDatabase(context).nihFormDao()
    val coroutineScope = rememberCoroutineScope()

    var form by remember { mutableStateOf<NIHForm?>(null) }
    val questions = remember { StrokeScaleQuestions.questions }
    val selectedOptions = remember { mutableStateListOf<Int?>().apply { repeat(questions.size) { add(null) } } }

    LaunchedEffect(formId) {
        form = nihFormDao.getFormById(formId)
        form?.let {
            val values = it.formData.split(",").map { score -> score.toIntOrNull() ?: 9 }
            values.forEachIndexed { index, score -> selectedOptions[index] = if (score != 9) score else null }
        }
    }

    if (form == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
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
                    text = "Patient Name: ${form?.patientName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "DOB: ${form?.dob}",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Date: ${form?.date}",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(questions) { question ->
                    ReadOnlyQuestionCard(question, selectedOptions)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {
                        // Navigate back
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Done", color = Color.Black)
                }

                Button(
                    onClick = {
                        form?.let {
                            coroutineScope.launch {
                                nihFormDao.deleteForm(it)
                                (context as? ComponentActivity)?.finish()
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

