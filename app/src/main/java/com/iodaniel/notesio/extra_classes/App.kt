package com.iodaniel.notesio.extra_classes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards

class App : Application() {
    private val channelId = "Notification channelID"

    override fun onCreate() {
        super.onCreate()
        // NOTIFICATION
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, "Tasks", importance)
            manager.createNotificationChannel(channel)
        }
    }
}