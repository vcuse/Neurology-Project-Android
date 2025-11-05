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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ListSessionActivity - WITH MOCK DATA FOR TESTING
 * 
 * This version includes sample/test data so you can see how the UI looks
 * with actual sessions displayed.
 * 
 */
class ListSessionActivity : ComponentActivity() {
    private var refreshTrigger by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListSessionScreen(refreshTrigger)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrigger++
    }
}

@Composable
fun ListSessionScreen(refreshTrigger: Int) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val username = sessionManager.fetchUsername() ?: "anonymous"

    // State to hold the list of sessions
    var savedSessions by remember { mutableStateOf<List<Session>>(emptyList()) }

    // MOCK DATA - Replace this section when backend is ready
    LaunchedEffect(refreshTrigger) {
        // Option 1: Try to fetch real data from server
        // val fetchedSessions = SessionManagerApi.fetchSessionsForUser(username, sessionManager.client)
        
        // Option 2: If no data from server, use mock data
        val mockSessions = listOf(
            Session(
                id = 1,
                sessionId = "session-001",
                date = "10/29/2025",
                time = "2:17pm",
                attendingDoctors = "Dr. Smith, Dr. Johnson",
                patientName = "John Doe",
                patientDob = "09/26/1985",
                formIds = "1,2",
                videoUrl = "https://example.com/video1.mp4",
                videoSize = "50MB",
                aiFormOutput = "NIHSS Score: 8 - Moderate stroke severity",
                username = username
            ),
            Session(
                id = 2,
                sessionId = "session-002",
                date = "10/28/2025",
                time = "3:45pm",
                attendingDoctors = "Dr. Williams, Dr. Brown, Dr. Davis",
                patientName = "Jane Smith",
                patientDob = "03/15/1972",
                formIds = "3",
                videoUrl = "https://example.com/video2.mp4",
                videoSize = "65MB",
                aiFormOutput = "NIHSS Score: 3 - Minor stroke severity",
                username = username
            ),
            Session(
                id = 3,
                sessionId = "session-003",
                date = "10/27/2025",
                time = "10:30am",
                attendingDoctors = "Dr. Miller",
                patientName = "Robert Johnson",
                patientDob = "11/08/1968",
                formIds = "4,5,6",
                videoUrl = "https://example.com/video3.mp4",
                videoSize = "42MB",
                aiFormOutput = "NIHSS Score: 15 - Severe stroke",
                username = username
            ),
            Session(
                id = 4,
                sessionId = "session-004",
                date = "10/26/2025",
                time = "9:15am",
                attendingDoctors = "Dr. Garcia, Dr. Martinez",
                patientName = "Mary Wilson",
                patientDob = "07/22/1990",
                formIds = "7",
                videoUrl = "https://example.com/video4.mp4",
                videoSize = "38MB",
                aiFormOutput = "NIHSS Score: 1 - Very minor symptoms",
                username = username
            ),
            Session(
                id = 5,
                sessionId = "session-005",
                date = "10/25/2025",
                time = "1:00pm",
                attendingDoctors = "Dr. Anderson",
                patientName = "Michael Brown",
                patientDob = "12/30/1956",
                formIds = "8,9",
                videoUrl = "https://example.com/video5.mp4",
                videoSize = "55MB",
                aiFormOutput = "NIHSS Score: 12 - Moderate to severe stroke",
                username = username
            )
        )
        
        savedSessions = mockSessions
        
        // TO USE REAL DATA INSTEAD:
        // 1. Comment out the mockSessions lines above
        // 2. Uncomment this line:
        // savedSessions = SessionManagerApi.fetchSessionsForUser(username, sessionManager.client)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Header Row with title and "New Session" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved Sessions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    val intent = Intent(context, NewSessionActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text(text = "New Session", fontSize = 16.sp)
            }
        }

        // Scrollable list of sessions
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savedSessions) { session ->
                SessionItem(
                    session = session,
                    onClick = {
                        // Navigate to session details screen
                        val intent = Intent(context, SessionDetailsActivity::class.java).apply {
                            putExtra("sessionId", session.id)
                            putExtra("sessionIdString", session.sessionId)
                            putExtra("date", session.date)
                            putExtra("time", session.time)
                            putExtra("attendingDoctors", session.attendingDoctors)
                            putExtra("patientName", session.patientName)
                            putExtra("patientDob", session.patientDob)
                            putExtra("formIds", session.formIds)
                            putExtra("videoUrl", session.videoUrl)
                            putExtra("videoSize", session.videoSize)
                            putExtra("aiFormOutput", session.aiFormOutput)
                            putExtra("username", session.username)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

/**
 * SessionItem Composable
 * 
 * Displays a single session card in the list.
 * Shows the date/time and a "View" button.
 */
@Composable
fun SessionItem(session: Session, onClick: () -> Unit) {
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
                // Display date and time
                Text(
                    text = "Date: ${session.date}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Time: ${session.time}",
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