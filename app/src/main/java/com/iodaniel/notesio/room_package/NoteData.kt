package com.iodaniel.notesio.room_package

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class NoteData (
    @PrimaryKey var id: Int = 0,
    @ColumnInfo var noteTitle: String = "",
    @ColumnInfo var note: String = "",
    @ColumnInfo var dateCreated: String = "",
    @ColumnInfo var modifiedHistory: ArrayList<HistoryNote> = arrayListOf(),
    @ColumnInfo var image: String = "",
)

class HistoryNote(
    @ColumnInfo var noteTitle: String = "",
    @ColumnInfo var note: String = "",
    @ColumnInfo var date: String = "",
)