package com.example.neurology_project_android

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NIHFormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: NIHForm)

    @Query("SELECT * FROM nih_forms ORDER BY date DESC")
    fun getAllForms(): Flow<List<NIHForm>>

    @Delete
    suspend fun deleteForm(form: NIHForm)

    @Query("SELECT * FROM nih_forms WHERE id = :formId")
    suspend fun getFormById(formId: Int): NIHForm?
}
