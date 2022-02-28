package com.iodaniel.notesio.room_package

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iodaniel.notesio.utils.Converters

@Database(entities = [NoteData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private const val DATABASE_NAME = "note data"
        private var instance: NoteDatabase? = null
        fun getDatabaseInstance(context: Context): NoteDatabase? {
            if (instance == null) {
                synchronized(NoteDatabase::class) {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(context, NoteDatabase::class.java, DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return instance
        }
    }
}