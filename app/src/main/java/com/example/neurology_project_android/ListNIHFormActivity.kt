package com.example.neurology_project_android

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class ListNIHFormActivity : ComponentActivity() {
    private var refreshTrigger by mutableStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListNIHFormScreen(refreshTrigger)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrigger++
    }
}

@Composable
fun ListNIHFormScreen(refreshTrigger: Int) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val client = sessionManager.client
    val username = sessionManager.fetchUsername() ?: "anonymous"

    var savedForms by remember { mutableStateOf<List<NIHForm>>(emptyList()) }

    LaunchedEffect(refreshTrigger) { // runs every time refreshTrigger changes
        val fetchedForms = FormManager.fetchFormsForUser(username, sessionManager.client)
        savedForms = fetchedForms
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Plain white background
            .padding(24.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved Forms",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    val intent = Intent(context, NewNIHFormActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text(text = "New Form", fontSize = 16.sp)
            }
        }

        // Scrollable List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savedForms) { form ->
                SavedFormItem(
                    form = SavedForm(form.patientName, form.date),
                    onClick = {
                        val intent = Intent(context, SavedNIHFormActivity::class.java).apply {
                            putExtra("formId", form.id)
                            putExtra("patientName", form.patientName)
                            putExtra("dob", form.dob)
                            putExtra("date", form.date)
                            putExtra("formData", form.formData)
                            putExtra("username", form.username)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun SavedFormItem(form: SavedForm, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = form.patientName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = form.dateRecorded,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(onClick = onClick) {
                Text(text = "View")
            }
        }
    }
}

data class SavedForm(val patientName: String, val dateRecorded: String)
