package com.iodaniel.notesio.room_package

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    fun insertNote(noteData: NoteData) : Long

    @Delete
    fun deleteNote(noteData: NoteData)

    @Update
    fun updateNote(noteData: NoteData)

    @Delete
    fun deleteListOfNotes(list: ArrayList<NoteData>)
}