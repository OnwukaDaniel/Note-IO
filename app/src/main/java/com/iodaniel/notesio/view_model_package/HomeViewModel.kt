package com.iodaniel.notesio.view_model_package

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.room_package.NoteDatabase

class HomeViewModel: ViewModel() {
    private var noteDatabase : NoteDatabase? = null

    fun getAllData(context: Context): LiveData<List<NoteData>>? {
        noteDatabase = NoteDatabase.getDatabaseInstance(context)
        return noteDatabase?.noteDao()?.returnAllNotes()
    }

    fun setNotes(noteData: NoteData, context: Context){
        noteDatabase = NoteDatabase.getDatabaseInstance(context)
        noteDatabase?.noteDao()?.insertNote(noteData)
    }

    fun setNotesInCreateNote(noteData: NoteData, context: Context){
        noteDatabase = NoteDatabase.getDatabaseInstance(context)
        noteDatabase?.noteDao()?.insertNote(noteData)
    }
}