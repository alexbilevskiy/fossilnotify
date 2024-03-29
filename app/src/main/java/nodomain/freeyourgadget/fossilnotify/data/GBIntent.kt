package nodomain.freeyourgadget.fossilnotify.data

import com.google.gson.annotations.SerializedName

const val GBPushConfigAction = "nodomain.freeyourgadget.gadgetbridge.Q_PUSH_CONFIG"
const val GBPushExtra = "EXTRA_CONFIG_JSON"

//{"push":{"set":{"widgetCustom0._.config.upper_text":"%par1","widgetCustom0._.config.lower_text":"%par2"}}}
data class GBPush(
    val push: Push
)

data class Push(
    val set: PushParams
)

data class PushParams(
    @SerializedName("widgetCustom0._.config.upper_text") val UpperText0: String,
    @SerializedName("widgetCustom0._.config.lower_text") val LowerText0: String,
    @SerializedName("widgetCustom1._.config.upper_text") val UpperText1: String = "",
    @SerializedName("widgetCustom1._.config.lower_text") val LowerText1: String = ""
)
