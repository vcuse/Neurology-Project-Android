package com.example.neurology_project_android

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Represents the DB table
@Entity(tableName = "nih_forms")
data class NIHFormEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-incremented primary key
    val patientName: String, // Name of the patient
    val date: String, // Form date
    val selectedOptions: String // JSON string of selected options
)

// Data Access Object interface
@Dao
interface NIHFormDao {
    @Insert
    suspend fun insertForm(form: NIHFormEntity)

    @Query("SELECT * FROM nih_forms ORDER BY id DESC")
    suspend fun getAllForms(): List<NIHFormEntity>
}