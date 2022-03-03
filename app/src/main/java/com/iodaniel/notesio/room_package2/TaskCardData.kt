package com.iodaniel.notesio.room_package2

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
class TaskCardData (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo var cardTitle: String = "",
    @ColumnInfo var dateCreated: String = "",
    @ColumnInfo var taskData: ArrayList<TaskData> = arrayListOf(),
    @ColumnInfo var image: String = "",
)

class TaskData (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo var taskTitle: String = "",
    @ColumnInfo var note: String = "",
    @ColumnInfo var dateCreated: String = "",
    @ColumnInfo var deadline: String = "",
    @ColumnInfo var taskCategory: String = "",
    @ColumnInfo var image: String = ""
)