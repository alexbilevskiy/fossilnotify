package nodomain.freeyourgadget.fossilnotify.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import nodomain.freeyourgadget.fossilnotify.ui.view_model.ViewModel

class GBReceiver(
    private val viewModel: ViewModel,
    private val gbService: GBService
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val upperText0 = intent?.getStringExtra("upper_text0")
        val lowerText0 = intent?.getStringExtra("lower_text0")
        val upperText1 = intent?.getStringExtra("upper_text1")
        val lowerText1 = intent?.getStringExtra("lower_text1")
        Log.d("test", String.format("NOTIF: %s, %s, %s, %s", upperText0, lowerText0, upperText1, lowerText1))

        gbService.sendWidgetData(upperText0.toString(), lowerText0.toString(), upperText1.toString(), lowerText1.toString())
        viewModel.updateText(String.format("NOTIF: %s, %s, %s, %s", upperText0, lowerText0, upperText1, lowerText1))
    }

}