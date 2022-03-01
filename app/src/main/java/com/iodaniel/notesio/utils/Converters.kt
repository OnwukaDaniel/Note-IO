package com.iodaniel.notesio.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iodaniel.notesio.room_package.HistoryNote
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskData


class Converters {
    @TypeConverter
    fun listToJson(arrayList: ArrayList<HistoryNote>) = Gson().toJson(arrayList)

    @TypeConverter
    fun jsonToArrayList(json: String) : ArrayList<HistoryNote>{
        val itemType = object : TypeToken<ArrayList<HistoryNote>>(){}.type
        return Gson().fromJson(json, itemType)
    }

    @TypeConverter
    fun historyListToJson(arrayList: ArrayList<NoteData>) = Gson().toJson(arrayList)

    @TypeConverter
    fun historyJsonToArrayList(json: String) : ArrayList<NoteData>{
        val itemType = object : TypeToken<ArrayList<NoteData>>(){}.type
        return Gson().fromJson(json, itemType)
    }
}
class TaskConverters {
    @TypeConverter
    fun listToJson(arrayList: ArrayList<TaskCardData>) = Gson().toJson(arrayList)

    @TypeConverter
    fun jsonToArrayList(json: String) : ArrayList<TaskCardData>{
        val itemType = object : TypeToken<ArrayList<TaskCardData>>(){}.type
        return Gson().fromJson(json, itemType)
    }

    @TypeConverter
    fun historyListToJson(arrayList: ArrayList<TaskData>) = Gson().toJson(arrayList)

    @TypeConverter
    fun historyJsonToArrayList(json: String) : ArrayList<TaskData>{
        val itemType = object : TypeToken<ArrayList<TaskData>>(){}.type
        return Gson().fromJson(json, itemType)
    }
}