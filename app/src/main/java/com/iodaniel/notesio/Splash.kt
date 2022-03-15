package com.iodaniel.notesio

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

class Splash : AppCompatActivity() {
    private var taskCardDataset: ArrayList<TaskCardData> = arrayListOf()
    private lateinit var settingsPref: SharedPreferences
    private val channelId = "Notification channelID"
    val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPref = getSharedPreferences(
            getString(R.string.SETTINGS_SHAREDPREFERENCE),
            Context.MODE_PRIVATE
        )
        val notify =
            settingsPref.getBoolean(getString(R.string.NOTIFICATION_SHAREDPREFERENCE), false)
        runBlocking {
            val job = scope.async {
                if (notify) {
                    val taskCardDao = TaskCardDatabase.getDatabaseInstance(applicationContext)
                    taskCardDataset =
                        taskCardDao!!.taskDao().returnAllTaskCardsN() as ArrayList<TaskCardData>
                    computeData(taskCardDataset)
                }
            }
            job.join()
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun computeData(taskCardDataset: ArrayList<TaskCardData>) {
        val currentTime = Calendar.getInstance().time.time
        val taskCardDao = TaskCardDatabase.getDatabaseInstance(applicationContext)
        try {
            for (taskCardData in taskCardDataset) {
                val notificationData = NotificationData()
                for ((index, task) in taskCardData.taskData.withIndex()) {
                    val deadline = task.deadline.toLong()
                    val expired = task.expired
                    val read = task.read

                    if (expired && read) continue
                    else if (expired && !read) { // EXPIRED BUT NOT SEEN.
                        notificationData.expiryDate = task.deadline
                        println("SPLASH SCREEN ****************************** 1")

                    } else if (!expired && currentTime > deadline) { // EXPIRED WHILE AWAY FROM APP, BUT NOT SEEN.
                        taskCardData.taskData[index].expired = true
                        taskCardData.taskData[index].color = Util.MISSED
                        notificationData.expired = true
                        notificationData.noteExpiredOffline.add(task.note)
                        println("SPLASH SCREEN ****************************** 2")

                    } else if (currentTime < deadline) {
                        val timeDiff = deadline - currentTime
                        notificationData.timeDiff = timeDiff
                        notificationData.noteNotExpired.add(task.note)
                        println("SPLASH SCREEN ****************************** 3")
                    }
                    taskCardDao!!.taskDao().updateTaskCard(taskCardData)
                    notificationData.id = 2
                    notificationData.expiryDate = deadline.toString()
                    notificationData.taskCardTitle = taskCardData.cardTitle
                }
                createNotification(notificationData)
            }
        } catch (e: Exception) {
            println("Exception while creating notification ---------------- ${e.printStackTrace()}")
        }
    }

    private fun createNotification(notificationData: NotificationData) {
        val appId = "com.iodaniel.notesio"
        val notificationId = 2
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivities(
            applicationContext, 0,
            arrayOf(intent), PendingIntent.FLAG_UPDATE_CURRENT
        )
        val smallNotification = RemoteViews(appId, R.layout.custom_notification_small)
        val bigNotification = RemoteViews(appId, R.layout.custom_notification_big)

        val text = "Task in: ${notificationData.taskCardTitle}"
        smallNotification.setTextViewText(R.id.custom_note_small_title, text)
        bigNotification.setTextViewText(R.id.custom_note_large_title, text)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallNotification)
            .setCustomBigContentView(bigNotification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        when {
            notificationData.expired -> {
                val text = "${notificationData.noteExpiredOffline} has expired"
                smallNotification.setTextViewText(R.id.custom_note_small_text, text)
                bigNotification.setTextViewText(R.id.custom_note_large_text, text)
                with(manager) { notify(notificationId, builder.build()) }
            }

            notificationData.timeDiff > 0 -> {
                val expiryDateTime = notificationData.expiryDate.toLong()
                val calenderInstance = Calendar.getInstance()
                calenderInstance.timeInMillis = expiryDateTime
                var minute = calenderInstance.get(Calendar.MINUTE).toString()
                minute = if (minute.length == 1) "0$minute" else minute
                val hour = calenderInstance.get(Calendar.HOUR)
                val amPm = calenderInstance.get(Calendar.AM_PM)
                val day = calenderInstance.get(Calendar.DAY_OF_WEEK)
                val month = calenderInstance.get(Calendar.MONTH)
                val year = calenderInstance.get(Calendar.YEAR)

                val displayExpiryDate = "$day:$month:$year"
                val displayExpiryTime = "$hour:$minute ${Util.digitToAmPm[amPm]}"

                var tasks = ""
                for ((index, note) in notificationData.noteNotExpired.withIndex()) {
                    tasks += if (index == 0) note else ", $note"
                }

                val trimmedText =
                    if (tasks.length > 51) tasks.slice(IntRange(0, 50)) else "$tasks..."

                val displayText =
                    "$trimmedText will expire on $displayExpiryDate by $displayExpiryTime"
                smallNotification.setTextViewText(R.id.custom_note_small_text, displayText)
                bigNotification.setTextViewText(R.id.custom_note_large_text, displayText)
                with(manager) { notify(notificationId, builder.build()) }
            }
        }
    }
}

class NotificationData(
    var id: Int = 0,
    var taskCardTitle: String = "",
    var noteNotExpired: ArrayList<String> = arrayListOf(),
    var noteExpiredOffline: ArrayList<String> = arrayListOf(),
    var expiryDate: String = "",
    var timeDiff: Long = 0L,
    var expired: Boolean = false,
)