package nodomain.freeyourgadget.fossilnotify.service

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NLService : NotificationListenerService() {

    companion object {
        const val TAG = "NotificationListener"
        const val  INTENT_FILTER_ACTION = "nodomain.freeyourgadget.fossilnotify.NOTIFICATION_LISTENER_EXAMPLE"
        const val  INTENT_FILTER_GB = "nodomain.freeyourgadget.fossilnotify.NOTIFICATION_LISTENER_GB"
    }

    private lateinit var nlServiceReceiver : NLServiceReceiver
    private lateinit var gbService: GBService

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        nlServiceReceiver = NLServiceReceiver()
        val filter = IntentFilter(INTENT_FILTER_ACTION)
        registerReceiver(nlServiceReceiver, filter)

        gbService = GBService(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nlServiceReceiver)
        Log.d(TAG, "onDestroy")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "onNotificationPosted")
        countNotifications()
        sbn?.let {
            Log.d(TAG, "Id: ${sbn.id}  ${sbn.notification.tickerText}  ${sbn.packageName}")
            val intent = Intent(INTENT_FILTER_ACTION)
            intent.putExtra("notification_event", "onNotificationPosted : ${sbn.packageName} n")
            sendBroadcast(intent)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "onNotificationRemoved")
        countNotifications()
        sbn?.let {
            Log.d(TAG, "Id: ${sbn.id}  ${sbn.notification.tickerText}  ${sbn.packageName}")
            val intent = Intent(INTENT_FILTER_ACTION)
            intent.putExtra("notification_event", "onNotificationRemoved : ${sbn.packageName} n")
            sendBroadcast(intent)
        }
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        countNotifications()
        Log.d(TAG, "onNotificationRankingUpdate")
    }

    internal inner class NLServiceReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.getStringExtra("command") == "clearall") {
                    this@NLService.cancelAllNotifications()
                } else if (it.getStringExtra("command") == "list") {
                    val i1 = Intent(INTENT_FILTER_ACTION)
                    i1.putExtra("notification_event", "=======================")
                    sendBroadcast(i1)
                    var i = 1
                    for (sbn in this@NLService.activeNotifications) {
                        val i2 = Intent(INTENT_FILTER_ACTION)
                        i2.putExtra("notification_event", "($i) Channel: ${sbn.notification.channelId}, Package: ${sbn.packageName}\n")
                        sendBroadcast(i2)
                        i++
                    }
                    val i3 = Intent(INTENT_FILTER_ACTION)
                    i3.putExtra("notification_event", "===== Notification List ====")
                    sendBroadcast(i3)
                } else if (it.getStringExtra("command") == "count") {
                    countNotifications(true)
                }
            }
        }
    }

    private fun countNotifications(fromUi: Boolean = false) {
        var tgCount = 0
        var latestSender = ""
        var upperText = ""
        var lowerText = ""
        for (sbn in this@NLService.activeNotifications) {
            if (sbn.packageName == "org.telegram.messenger") {
                if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                    val subText = sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT, "")
                    Log.d(TAG, String.format("Group summary: %s", subText))
                    lowerText = reformatSummary(subText)

                    continue
                }
                tgCount++
                if (latestSender == "") {
                    latestSender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
                }
                val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT, "")
                Log.d(TAG, String.format("sub text: %s", subText))
                if (lowerText == "") {
                    lowerText = reformatSummary(subText)
                }
            }
        }
        Log.d(TAG, "COUNT: $tgCount")
        if (latestSender != "") {
            upperText = String.format("%d/%s", tgCount, latestSender.split(" ")[0])
        } else {
            upperText = String.format("%d", tgCount)
        }
        if (tgCount == 0) {
            upperText = "no"
            lowerText = "notif."
        }
        if(fromUi) {
            val iTg = Intent(INTENT_FILTER_GB)
            iTg.putExtra("upper_text", upperText)
            iTg.putExtra("lower_text", lowerText)
            sendBroadcast(iTg)
        } else {
            gbService.sendWidgetData(upperText, lowerText)
        }
    }

    private fun reformatSummary(summary: String): String {
        //10 new messages from 7 chats
        val r = Regex("(?<messages>\\d+) new messages from (?<chats>\\d+) chats")
        val m = r.matchEntire(summary)
        if (m != null) {
            return String.format("%sc %sm", m.groupValues[2], m.groupValues[1])
        }

        return ""
    }
}