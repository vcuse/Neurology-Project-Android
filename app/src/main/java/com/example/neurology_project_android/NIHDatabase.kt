package com.example.neurology_project_android

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// This class is for managing the database

@Database(entities = [NIHFormEntity::class], version = 1)
abstract class NIHDatabase : RoomDatabase() {
    abstract fun nihFormDao(): NIHFormDao

    companion object {
        @Volatile
        private var INSTANCE: NIHDatabase? = null

        fun getDatabase(context: android.content.Context): NIHDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NIHDatabase::class.java,
                    "nih_forms_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
