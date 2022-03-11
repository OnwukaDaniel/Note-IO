package com.iodaniel.notesio

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.utils.Util
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

class Splash : AppCompatActivity() {
    private var taskCardViewModel = ViewModelTaskCards()
    private var taskCardDataset: ArrayList<TaskCardData> = arrayListOf()
    private val channelId = "Notification channelID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            val taskCardDao = TaskCardDatabase.getDatabaseInstance(applicationContext)
            val scope = CoroutineScope(Dispatchers.IO)

            val job = scope.async {
                taskCardDataset =
                    taskCardDao!!.taskDao().returnAllTaskCardsN() as ArrayList<TaskCardData>
                computeData(taskCardDataset)
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
                    else if (currentTime > deadline && !read) continue

                    if (expired && !read) { // EXPIRED BUT NOT SEEN.
                        notificationData.expiryDate = task.deadline

                    } else if (!expired && currentTime > deadline && !read) { // EXPIRED WHILE AWAY FROM APP, BUT NOT SEEN.
                        taskCardData.taskData[index].expired = true
                        notificationData.expired = true
                        println("TASK CARD UPDATED ************************ ")

                    } else if (currentTime < deadline) {
                        val timeDiff = deadline - currentTime
                        notificationData.timeDiff = timeDiff
                    }
                    taskCardDao!!.taskDao().updateTaskCard(taskCardData)
                    notificationData.id = taskCardData.id
                    notificationData.expiryDate = task.deadline
                    notificationData.taskCardTitle = taskCardData.cardTitle
                    notificationData.note.add(task.note)
                }
                createNotification(notificationData)
            }
        } catch (e: Exception) {
            println("Exception while creating notification ---------------- ${e.printStackTrace()}")
        }
    }

    private fun createNotification(notificationData: NotificationData) {
        val appId = "com.iodaniel.notesio"
        val notificationId = notificationData.id
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
                val text = "${notificationData.note} has expired"
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

                val trimmedText = if (notificationData.note.size > 31)
                    notificationData.note.slice(IntRange(0, 30)) else notificationData.note

                val text = "$trimmedText will expire on $displayExpiryDate by $displayExpiryTime"
                smallNotification.setTextViewText(R.id.custom_note_small_text, text)
                bigNotification.setTextViewText(R.id.custom_note_large_text, text)
                with(manager) { notify(notificationId, builder.build()) }
                println("timeDiff ************************ ${notificationData.timeDiff}")
            }
        }
    }
}

class NotificationData(
    var id: Int = 0,
    var taskCardTitle: String = "",
    var note: ArrayList<String> = arrayListOf(),
    var expiryDate: String = "",
    var timeDiff: Long = 0L,
    var expired: Boolean = false,
)