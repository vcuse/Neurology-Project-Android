package com.example.neurology_project_android

import android.content.Intent
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
import androidx.compose.ui.tooling.preview.Preview

class SavedNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SavedNIHFormScreen()
        }
    }
}

@Composable
fun SavedNIHFormScreen() {
    val context = LocalContext.current
    val patientName = "lauren"
    val date = "December 2, 2024"

    val questions = remember { StrokeScaleQuestions.questions }
    val selectedOptions = remember { mutableStateListOf<Int?>().apply { repeat(questions.size) { add(null) } } }

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
                text = "Patient Name: $patientName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Date: $date",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(questions) { question ->
                QuestionCard(question, selectedOptions)
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
                    val intent = Intent(context, ListNIHFormActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Done", color = Color.Black)
            }

            Button(
                onClick = {
                    // TODO: Implement Delete functionality
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Delete", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedNIHFormScreenPreview() {
    SavedNIHFormScreen()
}
