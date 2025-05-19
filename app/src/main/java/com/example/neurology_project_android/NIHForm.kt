package com.example.neurology_project_android

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nih_forms")
data class NIHForm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val dob: String,
    val date: String,
    val formData: String,
    val username: String
)