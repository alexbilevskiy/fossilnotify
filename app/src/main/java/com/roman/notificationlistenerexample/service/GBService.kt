package com.roman.notificationlistenerexample.service

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.roman.notificationlistenerexample.data.*

class GBService(
    private val context: Context
) {
    fun sendWidgetData(upperText: String, lowerText: String) {
        val push = GBPush(Push(PushParams(upperText, lowerText)))
        val pushConfigIntent = Intent(GBPushConfigAction)
        pushConfigIntent.putExtra(GBPushExtra, Gson().toJson(push))
        context.sendBroadcast(pushConfigIntent)
    }
}