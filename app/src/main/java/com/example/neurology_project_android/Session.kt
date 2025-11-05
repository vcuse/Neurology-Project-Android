package com.example.neurology_project_android

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Session Data Model
 * 
 * Represents a consultation session that contains:
 * - Session metadata (ID, date, time)
 * - List of attending doctors
 * - Associated stroke scale forms
 * - Video recording information
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,          // Unique session identifier
    val date: String,                // Session date (e.g., "9/30/2025")
    val time: String,                // Session time (e.g., "2:17pm")
    val attendingDoctors: String,    // Comma-separated list of doctor names
    val patientName: String,         // Patient involved in session
    val patientDob: String,          // Patient date of birth
    val formIds: String,             // Comma-separated list of form IDs associated with this session
    val videoUrl: String,            // URL or path to session video recording
    val videoSize: String,           // Video file size (e.g., "50MB")
    val aiFormOutput: String,        
    val username: String             // User who created/owns this session
)
