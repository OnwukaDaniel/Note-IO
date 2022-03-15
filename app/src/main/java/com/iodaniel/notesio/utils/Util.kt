package com.iodaniel.notesio.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.iodaniel.notesio.R
import java.text.SimpleDateFormat
import java.util.*

object Util {

    val JUST_CREATED = Color.LightGray.toArgb()
    val IN_PROGRESS = Color.Cyan.toArgb()
    val DONE_25 = Color.DarkGray.toArgb()
    val DONE_250 = Color.Magenta.toArgb()
    val DONE_75 = Color.Yellow.toArgb()
    val DONE = Color.Green.toArgb()
    val MISSED = Color.Red.toArgb()
    val INCOMPLETE = Color.Black.toArgb()

    var taskLabelData: ArrayList<Int> = arrayListOf(
        Color.LightGray.toArgb(),
        Color.Cyan.toArgb(),
        Color.DarkGray.toArgb(),
        Color.Magenta.toArgb(),
        Color.Yellow.toArgb(),
        Color.Green.toArgb(),
        Color.Red.toArgb(),
        Color.Black.toArgb(),
    )

    val digitToAmPm = arrayListOf("AM", "PM")

    val months = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    val todoLabels = arrayListOf(
        "Just Created", "In progress", "25% Done", "50% Done",
        "75% Done", "Done", "Missed", "Incomplete"
    )
    fun keepEditing(context: Context,activity: Activity){

        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Stop editing?")
            .setItems(arrayOf("Keep editing", "Discard")) { dialog, which ->
                when (which) {
                    0 -> dialog.dismiss()
                    1 -> activity.onBackPressed()
                }
            }.show()
    }

    fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date).split(" ")[0]
    }

    fun createNotification(
        title: String,
        text: String,
        applicationContext: Context,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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