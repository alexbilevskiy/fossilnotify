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
        var totalCount = 0
        var latestSender = ""
        var upperText0 = ""
        var lowerText0 = ""
        var upperText1 = ""
        var lowerText1 = ""
        var playbackSet = false
        var uniq: MutableMap<String, Int> = mutableMapOf()
        for (sbn in this@NLService.activeNotifications) {
            totalCount++

            if (sbn.packageName == "org.telegram.messenger.web") {
                if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                    val subText = sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT, "")
                    Log.d(TAG, String.format("Group summary: %s", subText))
                    lowerText0 = reformatSummary(subText)

                    continue
                }
                if (latestSender == "") {
                    latestSender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
                }
                Log.d(TAG, String.format("sender: %s", latestSender))

                if (latestSender == "Ongoing Video Chat" || latestSender == "Ongoing Telegram call") {
                    latestSender = ""
                    continue
                }
                tgCount++
                val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT, "")
                Log.d(TAG, String.format("sub text: %s", subText))
                if (lowerText0 == "") {
                    lowerText0 = reformatSummary(subText)
                }
            } else {
//                Log.d(TAG, String.format("SKIP: %s", sbn.packageName))
            }
            if(sbn.notification.channelId == "playback") {
                totalCount--
                if (sbn.notification.actions[1].title == "Pause") {
                    Log.d(TAG, String.format("PLAYING: <%s> title: %s, artist: %s", sbn.notification.actions[1].title, sbn.notification.extras.getString(Notification.EXTRA_TITLE), sbn.notification.extras.getString(Notification.EXTRA_TEXT)))
                    upperText1 = sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
                    lowerText1 = sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
                    playbackSet = true
                } else {
                    Log.d(TAG, String.format("NOT PLAYING: <%s> title: %s, artist: %s", sbn.notification.actions[1].title, sbn.notification.extras.getString(Notification.EXTRA_TITLE), sbn.notification.extras.getString(Notification.EXTRA_TEXT)))
                }
            } else {
                if (!uniq.keys.contains(sbn.packageName)) {
                    uniq.put(sbn.packageName, 0)
                } else {
                    uniq.put(sbn.packageName, uniq.getValue(sbn.packageName)+1)
                }
            }
        }
        Log.d(TAG, "COUNT: $tgCount")
        if (latestSender != "") {
            upperText0 = String.format("%d/%s", tgCount, latestSender.split(" ")[0])
        } else {
            upperText0 = String.format("%d", tgCount)
        }
        if (tgCount == 0) {
            upperText0 = ""
            lowerText0 = ""
        }
        if (!playbackSet) {
            lowerText1 = ""
            if (uniq.keys.size == 0) {
                lowerText1 = ""
            } else {
                lowerText1 = String.format("%d", uniq.keys.size)
            }
        }
        if(fromUi) {
            val iTg = Intent(INTENT_FILTER_GB)
            iTg.putExtra("upper_text0", upperText0)
            iTg.putExtra("lower_text0", lowerText0)
            iTg.putExtra("upper_text1", upperText1)
            iTg.putExtra("lower_text1", lowerText1)
            sendBroadcast(iTg)
        } else {
            gbService.sendWidgetData(upperText0, lowerText0, upperText1, lowerText1, )
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