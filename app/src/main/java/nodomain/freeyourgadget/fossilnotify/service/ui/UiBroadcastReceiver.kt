package nodomain.freeyourgadget.fossilnotify.service.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nodomain.freeyourgadget.fossilnotify.ui.view_model.ViewModel

class UiBroadcastReceiver(
    private val viewModel: ViewModel,
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val upperText0 = intent?.getStringExtra("upper_text0")
        val lowerText0 = intent?.getStringExtra("lower_text0")
        val upperText1 = intent?.getStringExtra("upper_text1")
        val lowerText1 = intent?.getStringExtra("lower_text1")

        viewModel.updateText(String.format("NOTIF: %s, %s, %s, %s", upperText0, lowerText0, upperText1, lowerText1))
    }

}