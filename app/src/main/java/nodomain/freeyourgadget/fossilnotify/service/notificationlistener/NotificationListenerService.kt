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
import kotlin.and
import kotlin.collections.get

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
                val sender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
                if (sender == "Ongoing Video Chat" || sender == "Ongoing Telegram call") {
                    continue
                }
                if(summary.messengerInfo[pkg] == null) {
                    summary.messengerInfo[pkg] = MessengerInfo()
                }
                summary.messengerInfo[pkg]?.lastSenderName = sender
                summary.messengerInfo[pkg]?.unreadDialogsCount++

                if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                    val subText = sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT, "")
                    //Log.d(GBService.Companion.TAG, String.format("Group summary: %s", subText))
                    val groupSummary = reformatSummary(subText)
                    if (groupSummary.isNotEmpty()) {
                        summary.messengerInfo[pkg]?.unreadChatsCount = groupSummary[1]
                        summary.messengerInfo[pkg]?.unreadMessagesCount = groupSummary[0]
                    }
                }
                // or use sub text
                // val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT, "")
                continue
            }
            //Log.d(TAG, String.format("SKIP: %s", pkg))
        }
        summary.totalInfo.totalNotificationsCount = uniq.keys.size
        gbService.countNotifications(summary, fromUi)
    }

    private fun reformatSummary(summary: String): List<Int> {
        val l: MutableList<Int> = mutableListOf<Int>()
        //Alex Surname * 10 new messages from 7 chats
        val r = Regex(".*(?<messages>\\d+) new messages from (?<chats>\\d+) chats")
        val m = r.matchEntire(summary)
        if (m == null) {
            return l
        }
        l[0] = m.groupValues[1].toInt()
        l[1] = m.groupValues[2].toInt()

        return l
    }

}