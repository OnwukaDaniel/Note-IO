package com.iodaniel.notesio.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iodaniel.notesio.room_package.HistoryNote


class Converters {
    @TypeConverter
    fun listToJson(arrayList: ArrayList<HistoryNote>) = Gson().toJson(arrayList)

    @TypeConverter
    fun jsonToArrayList(json: String) : ArrayList<HistoryNote>{
        val itemType = object : TypeToken<ArrayList<HistoryNote>>(){}.type
        return Gson().fromJson<ArrayList<HistoryNote>>(json, itemType)
    }
}