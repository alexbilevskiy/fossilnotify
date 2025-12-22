# FossilNotify
Send audio artist/track and telegram notifications summary to watch's "custom text" widgets.

> This project was created by extending an example of https://github.com/roma321m/NotificationListenerExample.

> This project does not work without [Gadgetbridge](https://codeberg.org/Freeyourgadget/Gadgetbridge/wiki/Fossil-Hybrid-HR#custom-widgets-firmware-dn1-0-2-20r-and-newer)

## Functions
All functions are based on currently displaing notifications (media session is also a notification)

### Telegram (org.telegram.messenger.web)
Prints last mesage sender name and number of unread messages from it in the `custom widget 1` 1st line.  
Reformats telegram's "X new messages from Y chats" and prints it as "Xc Ym" in the `custom widget 1` 2nd line.

### Music
Prints currently playing track artist/title in the `custom widget 2`. Does not show anything if music is paused.  
Supposedly works for any music player.  
Explicitly ignores tiktok.

### Total
If there is no music playing, prints total number of notifications instead of music in the `custom widget 2` 1st line.

### Preview
<img width="957" height="1271" alt="image" src="https://github.com/user-attachments/assets/a1013ea8-0f2d-4932-9067-7b820b8c5f5d" />

## Pebble
Testing on Pebble 2 Duo