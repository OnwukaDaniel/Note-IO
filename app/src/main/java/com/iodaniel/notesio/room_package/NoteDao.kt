package com.iodaniel.notesio.room_package

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(noteData: NoteData) : Long

    @Delete
    fun deleteNote(noteData: NoteData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(noteData: NoteData)

    @Query("SELECT * FROM notes_table")
    fun returnAllNotes(): LiveData<List<NoteData>>

    @Delete
    fun deleteListOfNotes(list: ArrayList<NoteData>)
}