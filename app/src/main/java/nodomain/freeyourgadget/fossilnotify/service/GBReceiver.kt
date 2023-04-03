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
        val upperText = intent?.getStringExtra("upper_text")
        val lowerText = intent?.getStringExtra("lower_text")
        Log.d("test", String.format("TG: %s, %s", upperText, lowerText))

        gbService.sendWidgetData(upperText.toString(), lowerText.toString())
        viewModel.updateText(String.format("TG: %s, %s", upperText, lowerText))
    }

}