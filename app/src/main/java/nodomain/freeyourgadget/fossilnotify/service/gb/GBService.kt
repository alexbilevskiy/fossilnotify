package nodomain.freeyourgadget.fossilnotify.service.gb

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import nodomain.freeyourgadget.fossilnotify.data.GBPush
import nodomain.freeyourgadget.fossilnotify.data.GBPushConfigAction
import nodomain.freeyourgadget.fossilnotify.data.GBPushExtra
import nodomain.freeyourgadget.fossilnotify.data.MediaState
import nodomain.freeyourgadget.fossilnotify.data.NotificationSummary
import nodomain.freeyourgadget.fossilnotify.data.Push
import nodomain.freeyourgadget.fossilnotify.data.PushParams
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService.Companion.INTENT_FILTER_ACTION
import java.util.UUID

const val SecondaryText0 = 18u
const val SecondaryText1 = 19u
const val SecondaryText2 = 20u
const val SecondaryText3 = 21u

class GBService {
    companion object {
        const val TAG = "GBService"
    }

    private var upperText0Prev: String = ""
    private var lowerText0Prev: String = ""
    private var upperText1Prev: String = ""
    private var lowerText1Prev: String = ""
    private var secondaryText0Prev: String = ""
    private var secondaryText1Prev: String = ""
    private var secondaryText2Prev: String = ""
    private var secondaryText3Prev: String = ""

    val watchfaceUUID: UUID
    private val applicationContext: Context
    private val infoRetriever: DefaultPebbleInfoRetriever
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pebbleJob: Job? = null
    private var fossilEnabled: Boolean = false
    private val prefs: SharedPreferences

    private val watches: MutableMap<WatchIdentifier, String> = mutableMapOf()
    private val apps: MutableMap<WatchIdentifier, UUID> = mutableMapOf()

    constructor(applicationContext: Context) {
        this.applicationContext = applicationContext
        this.watchfaceUUID = UUID.fromString("756405d7-3bb5-4ad8-85c8-886025076b3b")

        this.infoRetriever = DefaultPebbleInfoRetriever(applicationContext)

        this.prefs = applicationContext.getSharedPreferences("gb", Context.MODE_PRIVATE)

        if (prefs.getBoolean("pebble_enabled", false)) {
            initPebble()
        }
        if (prefs.getBoolean("fossil_enabled", false)) {
            initFossil()
        }
    }

    fun initPebble() {
        if (pebbleJob != null) {
            Log.d(TAG, "not initing pebble again")
            return
        }
        Log.d(TAG, "init pebble")
        pebbleJob = serviceScope.launch {
            updateWatches()
        }
    }

    fun closePebble() {
        Log.d(TAG, "close pebble")
        pebbleJob?.cancel()
        pebbleJob = null
        watches.clear()
        apps.clear()
    }

    fun initFossil() {
        if (fossilEnabled) {
            Log.d(TAG, "not initing fossil again")
        }
        Log.d(TAG, "init fossil")
        fossilEnabled = true
    }

    fun closeFossil() {
        Log.d(TAG, "close fossil")
        fossilEnabled = false
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

    fun sendPebbleData(secondaryText0: String, secondaryText1: String, secondaryText2: String, secondaryText3: String) {
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

        Log.d(TAG, String.format("NOTIFY PEBBLE: `%s`, `%s`, `%s`, `%s`", secondaryText0, secondaryText1, secondaryText2, secondaryText3))
        val dataToSend = mapOf(
            SecondaryText0 to PebbleDictionaryItem.String(secondaryText0),
            SecondaryText1 to PebbleDictionaryItem.String(secondaryText1),
            SecondaryText2 to PebbleDictionaryItem.String(secondaryText2),
            SecondaryText3 to PebbleDictionaryItem.String(secondaryText3),
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

    fun cachedSendPebble(fromUi: Boolean, secondaryText0: String, secondaryText1: String, secondaryText2: String, secondaryText3: String) {
        if (pebbleJob == null) {
            Log.d(TAG, "pebble not active")
            return
        }
        var changed = false
        if (secondaryText0 != secondaryText0Prev ||
            secondaryText1 != secondaryText1Prev ||
            secondaryText2 != secondaryText2Prev ||
            secondaryText3 != secondaryText3Prev
            ) {
            changed = true
            secondaryText0Prev = secondaryText0
            secondaryText1Prev = secondaryText1
            secondaryText2Prev = secondaryText2
            secondaryText3Prev = secondaryText3
        }
        if (fromUi) {
            Log.d(TAG, String.format("sending pebble: from UI"))
        } else if (changed) {
            Log.d(TAG, String.format("sending pebble: has changes"))
        } else {
            Log.d(TAG, String.format("not sending pebble: nothing changed"))
            return
        }

        sendPebbleData(secondaryText0, secondaryText1, secondaryText2, secondaryText3)
    }

    fun sendFossilWidgetData(upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        Log.d(TAG, String.format("NOTIFY FOSSIL: `%s`, `%s`, `%s`, `%s`", upperText0, lowerText0, upperText1, lowerText1))
        val push = GBPush(Push(PushParams(upperText0, lowerText0, upperText1, lowerText1)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        applicationContext.sendBroadcast(pushConfigIntent)
    }

    fun cachedSendFossil(fromUi: Boolean, upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "") {
        if (!fossilEnabled) {
            Log.d(TAG, "fossil not active")
            return
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
            iTg.putExtra("action", "count_result")
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
            if (tgSummary.unreadChatsCount != 0 || tgSummary.unreadMessagesCount != 0) {
                lowerText0 = String.format("%dc, %dm", tgSummary.unreadChatsCount, tgSummary.unreadMessagesCount)
            }
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
        cachedSendPebble(fromUi, upperText0, lowerText0, upperText1, lowerText1)
    }

    fun close() {
        closePebble()
        closeFossil()
    }
}