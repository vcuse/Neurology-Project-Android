package com.example.neurology_project_android

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NIHForm::class], version = 2)
abstract class NIHFormDatabase : RoomDatabase() {
    abstract fun nihFormDao(): NIHFormDao

    companion object {
        @Volatile
        private var INSTANCE: NIHFormDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE nih_forms ADD COLUMN username TEXT NOT NULL DEFAULT 'unknown'")
            }
        }

        fun getDatabase(context: Context): NIHFormDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NIHFormDatabase::class.java,
                    "nih_form_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}