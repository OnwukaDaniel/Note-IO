package com.iodaniel.notesio.room_package2

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTaskCard(taskCardData: TaskCardData) : Long

    @Delete
    fun deleteNote(taskCardData: TaskCardData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTaskCard(taskCardData: TaskCardData)

    @Query("SELECT * FROM task_table")
    fun returnAllTaskCards(): LiveData<List<TaskCardData>>

    @Query("SELECT * FROM task_table")
    fun returnAllTaskCardsN(): List<TaskCardData>

    @Delete
    fun deleteListOfTaskCards(list: ArrayList<TaskCardData>)
}