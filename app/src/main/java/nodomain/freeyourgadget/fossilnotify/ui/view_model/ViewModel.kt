package nodomain.freeyourgadget.fossilnotify.ui.view_model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.ContextWrapper
import android.util.Log

class ViewModel : ContextWrapper {
    companion object {
        const val EMPTY_TEXT = "empty"
        const val TAG = "ViewModel"

        const val INTENT_UI_UPDATE =
            "nodomain.freeyourgadget.fossilnotify.UI_UPDATE"
    }
    var text by mutableStateOf(EMPTY_TEXT)
    var pebbleStatus by mutableStateOf(EMPTY_TEXT)
    private var nlServiceReceiver: ViewModelReceiver

    constructor(base: Context) : super(base) {
        nlServiceReceiver = ViewModelReceiver()
        registerReceiver(ViewModelReceiver(), IntentFilter(INTENT_UI_UPDATE))
    }

    internal inner class ViewModelReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getStringExtra("action") == "clear") {
                text = EMPTY_TEXT
                return
            }
            var newtext: String = ""
            if (intent?.getStringExtra("action") == "fossil_result") {
                val upperText0 = intent.getStringExtra("upper_text0")
                val lowerText0 = intent.getStringExtra("lower_text0")
                val upperText1 = intent.getStringExtra("upper_text1")
                val lowerText1 = intent.getStringExtra("lower_text1")

                newtext = String.format("FOSSIL NOTIF: `%s`, `%s`, `%s`, `%s`", upperText0, lowerText0, upperText1, lowerText1)
            } else if (intent?.getStringExtra("action") == "pebble_result") {
                val secondaryText0 = intent.getStringExtra("secondary_text0")
                val secondaryText1 = intent.getStringExtra("secondary_text1")
                val secondaryText2 = intent.getStringExtra("secondary_text2")
                val secondaryText3 = intent.getStringExtra("secondary_text3")

                newtext = String.format("PEBBLE NOTIF: `%s`, `%s`, `%s`, `%s`", secondaryText0, secondaryText1, secondaryText2, secondaryText3)
            } else if (intent?.getStringExtra("action") == "pebble_status") {
                pebbleStatus = intent.getStringExtra("status").toString()
                return
            } else {
                Log.d(TAG, "unknown intent for ui: $intent")
                return
            }
            if (text == EMPTY_TEXT) {
                text = newtext
            } else {
                text = text + "\n" + newtext
            }
        }
    }
}