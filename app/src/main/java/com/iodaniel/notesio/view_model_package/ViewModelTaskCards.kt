package com.iodaniel.notesio.view_model_package

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase

class ViewModelTaskCards : ViewModel() {
    fun getAllTaskCards(context: Context): LiveData<List<TaskCardData>> {
        val cardDatabase = TaskCardDatabase.getDatabaseInstance(context)!!
        return cardDatabase.taskDao().returnAllTaskCards()
    }
}