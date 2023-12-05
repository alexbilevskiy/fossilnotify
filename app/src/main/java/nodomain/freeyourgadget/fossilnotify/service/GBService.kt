package nodomain.freeyourgadget.fossilnotify.service

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import nodomain.freeyourgadget.fossilnotify.data.*

class GBService(
    private val context: Context
) {
    fun sendWidgetData(upperText0: String, lowerText0: String, upperText1: String = "", lowerText1: String = "", ) {
        val push = GBPush(Push(PushParams(upperText0, lowerText0, upperText1, lowerText1)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        context.sendBroadcast(pushConfigIntent)
    }
}