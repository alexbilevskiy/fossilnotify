package nodomain.freeyourgadget.fossilnotify.ui.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ViewModel {
    var text by mutableStateOf("No Notifications To Display")
        private set

    fun updateText(newText: String) {
        text = newText
    }
}