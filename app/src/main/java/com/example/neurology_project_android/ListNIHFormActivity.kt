package com.example.neurology_project_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext

class ListNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GradientBackground {
                ListNIHFormScreen()
            }
        }
    }
}

@Composable
fun ListNIHFormScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top Row with Title and Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved Forms",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Button(
                onClick = {
                    val intent = Intent(context, NewNIHFormActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Create New Form"
                )
            }
        }

        // List of Saved Forms
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Placeholder - will be populated with Room DB data later
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListNIHFormScreenPreview() {
    GradientBackground {
        ListNIHFormScreen()
    }
}
