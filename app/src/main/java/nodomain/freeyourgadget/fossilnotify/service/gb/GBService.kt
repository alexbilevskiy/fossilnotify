package nodomain.freeyourgadget.fossilnotify.service.gb

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.util.Log
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.util.PebbleDictionary
import com.google.gson.Gson
import nodomain.freeyourgadget.fossilnotify.data.GBPush
import nodomain.freeyourgadget.fossilnotify.data.GBPushConfigAction
import nodomain.freeyourgadget.fossilnotify.data.GBPushExtra
import nodomain.freeyourgadget.fossilnotify.data.Push
import nodomain.freeyourgadget.fossilnotify.data.PushParams
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService.Companion.INTENT_FILTER_ACTION
import java.util.UUID

const val AppKeyTotalNotifications = 0
const val AppKeyTgSummary = 1

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

    val halcyonUuid: UUID = UUID.fromString("c8e97ab8-7c12-45fb-975d-e85d5de61e8b")

    fun sendPebbleData() {
        //@TODO: not working - always shows "not connected"
        if (!PebbleKit.isWatchConnected(applicationContext)) {
            Log.d(TAG, "pebble not connected")
            return
        }
        val dict = PebbleDictionary()
        dict.addInt32(AppKeyTotalNotifications, 0)
        dict.addString(AppKeyTgSummary, "sender_name: hello")
        try {
            PebbleKit.sendDataToPebble(applicationContext, halcyonUuid, dict)
            Log.d(TAG, "sent to pebble")
        } catch (e: Exception) {
            Log.d(TAG, "exception sending to pebble, cause" + e.cause + ", full: " + e.toString())
        }
    }

    fun sendFossilWidgetData(upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        val push = GBPush(Push(PushParams(upperText0, lowerText0, upperText1, lowerText1)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        applicationContext.sendBroadcast(pushConfigIntent)
        sendPebbleData()
    }

    fun countNotifications(notificationsList: Array<android.service.notification.StatusBarNotification>, fromUi: Boolean = false) {
        var tgCount = 0
        var totalCount = 0
        var latestSender = ""
        var upperText0 = ""
        var lowerText0 = ""
        var upperText1 = ""
        var lowerText1 = ""
        var playbackSet = false
        var uniq: MutableMap<String, Int> = mutableMapOf()
        for (sbn in notificationsList) {
            totalCount++

            if (sbn.packageName == "org.telegram.messenger.web") {
                if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == Notification.FLAG_GROUP_SUMMARY) {
                    val subText =
                        sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT, "")
                    Log.d(TAG, String.format("Group summary: %s", subText))
                    lowerText0 = reformatSummary(subText)

                    continue
                }
                if (latestSender == "") {
                    latestSender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
                }
//                Log.d(TAG, String.format("sender: %s", latestSender))

                if (latestSender == "Ongoing Video Chat" || latestSender == "Ongoing Telegram call") {
                    latestSender = ""
                    continue
                }
                tgCount++
                val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT, "")
//                Log.d(TAG, String.format("sub text: %s", subText))
                if (lowerText0 == "") {
                    lowerText0 = reformatSummary(subText)
                }
            } else {
//                Log.d(TAG, String.format("SKIP: %s", sbn.packageName))
            }
            if (sbn.notification.channelId == "playback") {
                totalCount--
                if (sbn.packageName == "com.ss.android.ugc.trill" || sbn.packageName == "com.zhiliaoapp.musically") {
                    // tiktok spams in media session
                    continue
                }
                if (sbn.notification.actions[1].title == "Pause") {
//                    Log.d(TAG, String.format("PLAYING: <%s> title: %s, artist: %s", sbn.notification.actions[1].title, sbn.notification.extras.getString(Notification.EXTRA_TITLE), sbn.notification.extras.getString(Notification.EXTRA_TEXT)))
                    upperText1 =
                        sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
                    lowerText1 =
                        sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
                    playbackSet = true
                } else {
//                    Log.d(TAG, String.format("NOT PLAYING: <%s> title: %s, artist: %s", sbn.notification.actions[1].title, sbn.notification.extras.getString(Notification.EXTRA_TITLE), sbn.notification.extras.getString(Notification.EXTRA_TEXT)))
                }
            } else {
                if (!uniq.keys.contains(sbn.packageName)) {
                    uniq.put(sbn.packageName, 0)
                } else {
                    uniq.put(sbn.packageName, uniq.getValue(sbn.packageName) + 1)
                }
            }
        }
//        Log.d(TAG, "COUNT: $tgCount")
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
        var changed = false
        if (upperText0 != upperText0Prev ||
            lowerText0 != lowerText0Prev ||
            upperText1 != upperText1Prev ||
            lowerText1 != lowerText1Prev
        ) {
            changed = true
            Log.d(TAG, String.format("sending: has changes"))
            upperText0Prev = upperText0
            lowerText0Prev = lowerText0
            upperText1Prev = upperText1
            lowerText1Prev = lowerText1
        } else {
            changed = false
            Log.d(TAG, String.format("not sending: nothing changed"))
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
        sendFossilWidgetData(upperText0, lowerText0, upperText1, lowerText1)
    }

    private fun reformatSummary(summary: String): String {
        //Alex Surname * 10 new messages from 7 chats
        val r = Regex(".*(?<messages>\\d+) new messages from (?<chats>\\d+) chats")
        val m = r.matchEntire(summary)
        if (m != null) {
            return String.format("%sc %sm", m.groupValues[2], m.groupValues[1])
        }

        return ""
    }

}