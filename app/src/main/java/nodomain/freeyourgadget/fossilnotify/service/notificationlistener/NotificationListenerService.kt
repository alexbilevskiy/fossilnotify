package nodomain.freeyourgadget.fossilnotify.service.notificationlistener

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import nodomain.freeyourgadget.fossilnotify.data.MediaInfo
import nodomain.freeyourgadget.fossilnotify.data.MediaState
import nodomain.freeyourgadget.fossilnotify.data.MessengerInfo
import nodomain.freeyourgadget.fossilnotify.data.NotificationSummary
import nodomain.freeyourgadget.fossilnotify.data.TotalInfo
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
        this.gbService.close()
        Log.d(TAG, "onDestroy")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        processNotificationsList(this@NotificationListenerService.activeNotifications)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        processNotificationsList(this@NotificationListenerService.activeNotifications)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        processNotificationsList(this@NotificationListenerService.activeNotifications)
    }

    internal inner class NLServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.getStringExtra("command") == "count") {
                    processNotificationsList(this@NotificationListenerService.activeNotifications, true)
                }
                if (it.getStringExtra("command") == "toggle_pebble") {
                    if (it.getBooleanExtra("enabled", false)) {
                        this@NotificationListenerService.gbService.initPebble()
                    } else {
                        this@NotificationListenerService.gbService.closePebble()
                    }
                }
                if (it.getStringExtra("command") == "toggle_fossil") {
                    if (it.getBooleanExtra("enabled", false)) {
                        this@NotificationListenerService.gbService.initFossil()
                    } else {
                        this@NotificationListenerService.gbService.closeFossil()
                    }
                }
            }
        }
    }

    fun processNotificationsList(notificationsList: Array<StatusBarNotification>, fromUi: Boolean = false) {
        val uniq: MutableMap<String, Int> = mutableMapOf()
        val summary = NotificationSummary(mutableMapOf(), mutableMapOf(), TotalInfo(0))
        for (sbn in notificationsList) {
            val pkg = sbn.packageName
            if (sbn.notification.channelId == "playback") {
                if (pkg == "com.ss.android.ugc.trill" || pkg == "com.zhiliaoapp.musically") {
                    // tiktok spams in media session
                    continue
                }
                var state = MediaState.Unknown
                val artist = sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
                val title = sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
                if (sbn.notification.actions[1].title == "Pause") {
                    state = MediaState.Playing
                } else if (sbn.notification.actions[1].title == "Play") {
                    state = MediaState.Paused
                }
                summary.mediaInfo[pkg] = MediaInfo(artist, title, state)
                continue
            }
            if (!uniq.keys.contains(pkg)) {
                uniq[pkg] = 0
            } else {
                uniq[pkg] = uniq.getValue(pkg) + 1
            }

            if (pkg == "org.telegram.messenger.web") {
                if(summary.messengerInfo[pkg] == null) {
                    summary.messengerInfo[pkg] = MessengerInfo()
                }

                val sender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
                // not counting calls
                if (sender == "Ongoing Video Chat" || sender == "Ongoing Telegram call") {
                    continue
                }
                // group summary internally is a full separate notification, we parse counters from it, but not count notification itself
                if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                    val subText = sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT, "")
                    //Log.d(TAG, String.format("Group summary: %s", subText))
                    val groupSummary = reformatSummary(subText)
                    if (groupSummary != null) {
                        summary.messengerInfo[pkg]?.unreadMessagesCount = groupSummary[0]
                        summary.messengerInfo[pkg]?.unreadChatsCount = groupSummary[1]
                    }
                    continue
                }
                // or use sub text
                // val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT, "")

                if(summary.messengerInfo[pkg]?.lastSenderName == "") {
                    summary.messengerInfo[pkg]?.lastSenderName = sender
                }
                summary.messengerInfo[pkg]?.unreadDialogsCount++

                continue
            }
            //Log.d(TAG, String.format("SKIP: %s", pkg))
        }
        summary.totalInfo.totalNotificationsCount = uniq.keys.size
        gbService.countNotifications(summary, fromUi)
    }

    private fun reformatSummary(summary: String): List<Int>? {
        val l: MutableList<Int> = mutableListOf<Int>()
            //"Alex󠅨󠄣󠄣󠅓󠅝󠅨・18 new messages from 18 chats"
        val r = Regex("(?<messages>\\d+) new messages from (?<chats>\\d+) chats")
        val m = r.find(summary)
        if (m == null) {
            return null
        }
        val mes = m.groups["messages"]?.value?.toInt() ?: 0
        val ch = m.groups["chats"]?.value?.toInt() ?: 0

        return listOf(mes, ch)
    }

}