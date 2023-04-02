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
        val tgCount = intent?.getIntExtra("tg_count", 0)
        Log.d("test", "TG count: $tgCount")
        gbService.sendWidgetData("Telegram", String.format("%d", tgCount))
        viewModel.updateText("TG COUNT $tgCount \n ${viewModel.text}")
    }

}