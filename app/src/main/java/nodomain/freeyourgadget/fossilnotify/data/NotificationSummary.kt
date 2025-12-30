package nodomain.freeyourgadget.fossilnotify.data

data class NotificationSummary(
    var messengerInfo: MutableMap<String, MessengerInfo>,
    var mediaInfo: MutableMap<String, MediaInfo>,
    var totalInfo: TotalInfo
)

data class MessengerInfo(
    var lastSenderName: String = "",
    var unreadDialogsCount: Int = 0,
    var unreadMessagesCount: Int = 0,
    var unreadChatsCount: Int = 0,
)

enum class MediaState {
    Playing,
    Paused,
    Unknown
}
data class MediaInfo(
    var artist: String,
    var title: String,
    var state: MediaState,
)

data class TotalInfo(
    var totalNotificationsCount: Int,
)
