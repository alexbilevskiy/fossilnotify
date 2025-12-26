package nodomain.freeyourgadget.fossilnotify.service.gb

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import io.rebble.pebblekit2.client.DefaultPebbleInfoRetriever
import io.rebble.pebblekit2.client.DefaultPebbleSender
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.model.ConnectedWatch
import io.rebble.pebblekit2.model.Watchapp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import nodomain.freeyourgadget.fossilnotify.data.GBPush
import nodomain.freeyourgadget.fossilnotify.data.GBPushConfigAction
import nodomain.freeyourgadget.fossilnotify.data.GBPushExtra
import nodomain.freeyourgadget.fossilnotify.data.Push
import nodomain.freeyourgadget.fossilnotify.data.PushParams
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService.Companion.INTENT_FILTER_ACTION
import java.util.UUID

const val AppKeyTotalNotifications = 18u
const val AppKeyTgSummary = 19u

class GBService {
    companion object {
        const val TAG = "GBService"
    }

    private var upperText0Prev: String = ""
    private var lowerText0Prev: String = ""
    private var upperText1Prev: String = ""
    private var lowerText1Prev: String = ""

    val watchfaceUUID: UUID
    private val applicationContext: Context
    private val infoRetriever: DefaultPebbleInfoRetriever
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val watches: MutableMap<WatchIdentifier, String> = mutableMapOf()
    private val apps: MutableMap<WatchIdentifier, UUID> = mutableMapOf()

    constructor(applicationContext: Context) {
        this.applicationContext = applicationContext
        this.watchfaceUUID = UUID.fromString("756405d7-3bb5-4ad8-85c8-886025076b3b")

        this.infoRetriever = DefaultPebbleInfoRetriever(applicationContext)

        serviceScope.launch {
            updateWatches()
        }
    }

    suspend fun updateWatches() {
        Log.d(TAG, "launching getConnectedWatches flow")
        infoRetriever.getConnectedWatches()
            .flowOn(Dispatchers.Default)
            .collect { it: List<ConnectedWatch> ->
                Log.d(TAG, "Connected watches update: $it")
                if (it.count() == 0) {
                    Log.d(TAG, "No watches in update")
                    watches.clear()
                } else {
                    for (w in it) {
                        Log.d(TAG, "Added watch: ${w.id} / ${w.name}")
                        watches[w.id] = w.name
                        updateWatchApps(w.id)
                    }
                }
            }
        Log.d(TAG, "finished getConnectedWatches flow")
    }

    suspend fun updateWatchApps(watchId: WatchIdentifier) {
        Log.d(TAG, "launching updateWatchApps flow for watch $watchId")
        infoRetriever.getActiveApp(watchId).flowOn(Dispatchers.Default).collect { it: Watchapp? ->
            Log.d(TAG, "active app update: $it")
            if (it == null) {
                Log.d(TAG, "no apps in update")
            } else {
                Log.d(TAG, "active app: ${it.id}")
                apps[watchId] = it.id
            }
        }
    }

    fun sendPebbleData(tgSummary: String, notifCount: String) {
        val cnt = watches.count()
        if (cnt == 0) {
            Log.d(TAG, "no connected watches")
            return
        } else if (cnt > 1) {
            Log.d(TAG, "multiple watches connected (${cnt}), dont know which to use")
            return
        }
        val watchId = watches.entries.first().key
        val curApp = apps[watchId]
        if (curApp == null) {
            Log.d(TAG, "no active app for device $watchId")
            return
        }
        if (curApp != watchfaceUUID) {
            Log.d(TAG, "not our app: has $curApp, want: $watchfaceUUID")
            return
        }
        val watchesToSend: List<WatchIdentifier> = listOf(watchId)

        // ring_color_day
        // {"10003":"custom","10004":0,"10005":0,"10006":0,"10007":16777215,"10008":0,"10009":0,"10010":16777215,"10011":16777215,"10012":16777215,"10013":11184810,"10014":11184810,"10015":0,"10016":0,"10017":0}
        // {"10003":"custom","10004":0,"10005":0,"10006":0,"10007":16777215,"10008":0,"10009":0,"10010":16777215,"10011":16777215,"10012":0,"10013":11184810,"10014":11184810,"10015":0,"10016":0,"10017":0}

        // {"LOCATION_LAT":55860619,"LOCATION_LNG":37567214,"LOCATION_GMT_OFFSET":180}

        Log.d(TAG, "sending pebble data")
        val dataToSend = mapOf(
            AppKeyTgSummary to PebbleDictionaryItem.String(tgSummary),
            AppKeyTotalNotifications to PebbleDictionaryItem.String(notifCount),
        )
        serviceScope.launch {
            val sender = DefaultPebbleSender(applicationContext)
            val result = sender.sendDataToPebble(watchfaceUUID, dataToSend, watchesToSend)
            if (result == null) {
                Log.d(TAG, "pebble app not reachable")
            } else if (result.isEmpty()) {
                Log.d(TAG, "sendDataToPebble: no connected watches")
            } else {
                for (r in result) {
                    Log.d(TAG, "transmission result: ${r.key} - ${r.value}")
                }
            }
            sender.close()
        }
    }

    fun sendFossilWidgetData(upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        Log.d(TAG, String.format("NOTIF: %s, %s, %s, %s", upperText0, lowerText0, upperText1, lowerText1))
        val push = GBPush(Push(PushParams(upperText0, lowerText0, upperText1, lowerText1)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        applicationContext.sendBroadcast(pushConfigIntent)
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
        sendPebbleData(upperText0, lowerText1)
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

    fun close() {
        serviceScope.cancel()
    }

}