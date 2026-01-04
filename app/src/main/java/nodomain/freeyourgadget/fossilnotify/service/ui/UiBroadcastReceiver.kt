package nodomain.freeyourgadget.fossilnotify.service.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nodomain.freeyourgadget.fossilnotify.ui.view_model.ViewModel

class UiBroadcastReceiver(
    private val viewModel: ViewModel
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getStringExtra("action") == "clear") {
            viewModel.updateText("")
        }
        if (intent?.getStringExtra("action") == "fossil_result") {
            val upperText0 = intent.getStringExtra("upper_text0")
            val lowerText0 = intent.getStringExtra("lower_text0")
            val upperText1 = intent.getStringExtra("upper_text1")
            val lowerText1 = intent.getStringExtra("lower_text1")

            viewModel.updateText(viewModel.text + "\n" + String.format("FOSSIL NOTIF: `%s`, `%s`, `%s`, `%s`", upperText0, lowerText0, upperText1, lowerText1))
        }
        if (intent?.getStringExtra("action") == "pebble_result") {
            val secondaryText0 = intent.getStringExtra("secondary_text0")
            val secondaryText1 = intent.getStringExtra("secondary_text1")
            val secondaryText2 = intent.getStringExtra("secondary_text2")
            val secondaryText3 = intent.getStringExtra("secondary_text3")

            viewModel.updateText(viewModel.text + "\n" + String.format("PEBBLE NOTIF: `%s`, `%s`, `%s`, `%s`", secondaryText0, secondaryText1, secondaryText2, secondaryText3))
        }
    }

}