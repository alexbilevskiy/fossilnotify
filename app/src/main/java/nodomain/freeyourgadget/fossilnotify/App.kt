package nodomain.freeyourgadget.fossilnotify

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import nodomain.freeyourgadget.fossilnotify.service.notificationsender.NotificationService

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationService.CHANNEL_ID,
            "My Notification Name In App Settings",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "My notification description shows in app settings"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}