package com.iodaniel.notesio.room_package2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iodaniel.notesio.utils.TaskConverters

@Database(entities = [TaskCardData::class], version = 1, exportSchema = false)
@TypeConverters(TaskConverters::class)
abstract class TaskCardDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskCardDao

    companion object {
        private const val DATABASE_NAME = "note data"
        private var instance: TaskCardDatabase? = null
        fun getDatabaseInstance(context: Context): TaskCardDatabase? {
            if (instance == null) {
                synchronized(TaskCardDatabase::class) {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(
                                context,
                                TaskCardDatabase::class.java,
                                DATABASE_NAME
                            )
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return instance
        }
    }
}