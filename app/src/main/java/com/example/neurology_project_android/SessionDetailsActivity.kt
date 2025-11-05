package com.example.neurology_project_android

import android.content.Intent
import android.net.Uri
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

/**
 * SessionDetailsActivity
 * 
 * Shows detailed information about a specific consultation session:
 */
class SessionDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract session data from Intent extras
        val session = Session(
            id = intent.getIntExtra("sessionId", -1),
            sessionId = intent.getStringExtra("sessionIdString") ?: "",
            date = intent.getStringExtra("date") ?: "",
            time = intent.getStringExtra("time") ?: "",
            attendingDoctors = intent.getStringExtra("attendingDoctors") ?: "",
            patientName = intent.getStringExtra("patientName") ?: "",
            patientDob = intent.getStringExtra("patientDob") ?: "",
            formIds = intent.getStringExtra("formIds") ?: "",
            videoUrl = intent.getStringExtra("videoUrl") ?: "",
            videoSize = intent.getStringExtra("videoSize") ?: "",
            aiFormOutput = intent.getStringExtra("aiFormOutput") ?: "",
            username = intent.getStringExtra("username") ?: ""
        )

        setContent {
            SessionDetailsScreen(session)
        }
    }
}

@Composable
fun SessionDetailsScreen(session: Session) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

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

        // Header - Session Information
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Session Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "${session.date} ${session.time}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Patient: ${session.patientName}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "DOB: ${session.patientDob}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Attending Doctors Section
            item {
                SessionDetailCard(
                    title = "Attending Doctors",
                    content = session.attendingDoctors.replace(",", ", ")
                )
            }

            // Patient Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Patient Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = session.patientName,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "DOB: ${session.patientDob}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "Date: ${session.date}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // View Form Button
                        Button(
                            onClick = {
                                // TODO: Navigate to form details
                                // Parse formIds and open the first form
                                val formIdsList = session.formIds.split(",")
                                if (formIdsList.isNotEmpty() && formIdsList[0].isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Opening form ${formIdsList[0]}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // You can add navigation to SavedNIHFormActivity here
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No forms associated with this session",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "View Form")
                        }
                    }
                }
            }

            // Video File Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Video File",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Size: ${session.videoSize}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // View Video Button
                        Button(
                            onClick = {
                                // Open video player or browser with video URL
                                if (session.videoUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.videoUrl))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No video available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "View Video")
                        }
                    }
                }
            }

            // AI Form Output Section (if available)
            if (session.aiFormOutput.isNotEmpty()) {
                item {
                    SessionDetailCard(
                        title = "AI Assessment",
                        content = session.aiFormOutput
                    )
                }
            }
        }

        // Delete Button at bottom
        Button(
            onClick = {
                SessionManagerApi.deleteSession(
                    session.id,
                    session.username,
                    sessionManager.client
                ) { success ->
                    (context as? ComponentActivity)?.runOnUiThread {
                        if (success) {
                            Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                            (context as? ComponentActivity)?.finish()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to delete session",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Delete Session", color = Color.White)
        }
    }
}

/**
 * Reusable card component for displaying session detail sections
 */
@Composable
fun SessionDetailCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
