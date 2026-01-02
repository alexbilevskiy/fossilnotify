package nodomain.freeyourgadget.fossilnotify.service.gb

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import nodomain.freeyourgadget.fossilnotify.data.GBPush
import nodomain.freeyourgadget.fossilnotify.data.GBPushConfigAction
import nodomain.freeyourgadget.fossilnotify.data.GBPushExtra
import nodomain.freeyourgadget.fossilnotify.data.MediaState
import nodomain.freeyourgadget.fossilnotify.data.NotificationSummary
import nodomain.freeyourgadget.fossilnotify.data.Push
import nodomain.freeyourgadget.fossilnotify.data.PushParams
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService.Companion.INTENT_FILTER_ACTION

class GBService(
    private val applicationContext: Context
) {
    companion object {
        const val TAG = "GBService"
    }

    private var upperText0Prev: String = ""
    private var lowerText0Prev: String = ""
    private var upperText1Prev: String = ""
    private var lowerText1Prev: String = ""

    fun sendFossilWidgetData(upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        Log.d(TAG, String.format("NOTIFY FOSSIL: `%s`, `%s`, `%s`, `%s`", upperText0, lowerText0, upperText1, lowerText1))
        val push = GBPush(Push(PushParams(upperText0, lowerText0, upperText1, lowerText1)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        applicationContext.sendBroadcast(pushConfigIntent)
    }

    fun cachedSendFossil(fromUi: Boolean, upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        var changed = false
        if (upperText0 != upperText0Prev ||
            lowerText0 != lowerText0Prev ||
            upperText1 != upperText1Prev ||
            lowerText1 != lowerText1Prev
        ) {
            changed = true
            upperText0Prev = upperText0
            lowerText0Prev = lowerText0
            upperText1Prev = upperText1
            lowerText1Prev = lowerText1
        }
        if (fromUi) {
            val iTg = Intent(INTENT_FILTER_ACTION)
            iTg.putExtra("command", "count_result")
            iTg.putExtra("upper_text0", upperText0)
            iTg.putExtra("lower_text0", lowerText0)
            iTg.putExtra("upper_text1", upperText1)
            iTg.putExtra("lower_text1", lowerText1)
            applicationContext.sendBroadcast(iTg)
        }
        if (fromUi) {
            Log.d(TAG, String.format("sending: from UI"))
        } else if (changed) {
            Log.d(TAG, String.format("sending: has changes"))
        } else {
            Log.d(TAG, String.format("not sending: nothing changed"))
            return
        }
        sendFossilWidgetData(upperText0, lowerText0, upperText1, lowerText1)
    }

    fun countNotifications(notificationSummary: NotificationSummary, fromUi: Boolean = false) {
        // upper1, lower1 = media or total count. upper0 = tg count+sender, lower0 = tg summary
        var upperText0 = ""
        var lowerText0 = ""
        var upperText1 = ""
        var lowerText1 = ""
        var playbackSet = false
        var tgSummary = notificationSummary.messengerInfo["org.telegram.messenger.web"]
        if (tgSummary != null) {
            if (tgSummary.lastSenderName != "") {
                upperText0 = String.format("%d/%s", tgSummary.unreadDialogsCount, tgSummary.lastSenderName.split(" ")[0])
            } else {
                upperText0 = String.format("%d", tgSummary.unreadDialogsCount)
            }
            lowerText0 = String.format("%dc, %dm", tgSummary.unreadChatsCount, tgSummary.unreadMessagesCount)
        } else {
            upperText0 = ""
            lowerText0 = ""
        }

        if (notificationSummary.mediaInfo.isNotEmpty()) {
            val mediaSession = notificationSummary.mediaInfo.entries.first().value
            if (mediaSession.state == MediaState.Playing) {
                upperText1 = mediaSession.artist
                lowerText1 = mediaSession.title
                playbackSet = true
            }
        }

        if (!playbackSet) {
            if (notificationSummary.totalInfo.totalNotificationsCount > 0) {
                lowerText1 = String.format("%d", notificationSummary.totalInfo.totalNotificationsCount)
            }
        }
        cachedSendFossil(fromUi, upperText0, lowerText0, upperText1, lowerText1)
    }
}