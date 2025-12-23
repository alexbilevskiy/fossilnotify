package nodomain.freeyourgadget.fossilnotify.service.notificationlistener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import nodomain.freeyourgadget.fossilnotify.service.gb.GBService

class NotificationListenerService : NotificationListenerService() {

    companion object {
        const val TAG = "NotificationListener"
        const val INTENT_FILTER_ACTION =
            "nodomain.freeyourgadget.fossilnotify.NOTIFICATION_LISTENER_EXAMPLE"
    }

    private lateinit var nlServiceReceiver: NLServiceReceiver
    private lateinit var gbService: GBService

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        nlServiceReceiver = NLServiceReceiver()
        val filter = IntentFilter(INTENT_FILTER_ACTION)
        registerReceiver(nlServiceReceiver, filter)

        this.gbService = GBService(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nlServiceReceiver)
        Log.d(TAG, "onDestroy")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        gbService.countNotifications(this@NotificationListenerService.activeNotifications)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        gbService.countNotifications(this@NotificationListenerService.activeNotifications)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        gbService.countNotifications(this@NotificationListenerService.activeNotifications)
    }

    internal inner class NLServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.getStringExtra("command") == "count") {
                    gbService.countNotifications(this@NotificationListenerService.activeNotifications, true)
                }
            }
        }
    }
}