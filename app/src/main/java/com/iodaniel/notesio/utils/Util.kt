package com.iodaniel.notesio.utils

import java.text.SimpleDateFormat
import java.util.*

object Util {

    val digitToAmPm = arrayListOf("am", "pm")

    val months = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    val todoLabels = arrayListOf(
        "Just Created", "In progress", "25% Done", "50% Done",
        "75% Done", "Done", "Missed", "Incomplete"
    )
    fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date).split(" ")[0]
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm")
        return format.format(date).split(" ")[0]
    }

    fun convert24HrTo12Hr(hourOfDay: Int): Pair<String, Int> {
        val amPm = if (hourOfDay > 12) "PM" else "AM"
        val hour = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
        val pair = Pair(amPm, hour)
        return pair
    }
}