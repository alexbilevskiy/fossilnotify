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

<details>
  <summary>watch photo:</summary>

  <img width="500" height="700" alt="image" src="https://github.com/user-attachments/assets/a1013ea8-0f2d-4932-9067-7b820b8c5f5d" />
  
</details>

## Pebble (beta)
**Note!** 
Requires beta version of new pebble [officiall app](https://play.google.com/store/apps/details?id=coredevices.coreapp) >= 1.0.7.10  
Also requires watchface modification, such as https://github.com/alexbilevskiy/halcyon

<details>
  <summary>no text:</summary>
  <img width="360" height="418" alt="image" src="https://github.com/user-attachments/assets/8debd0a6-688a-49a1-94d1-a135a0f45d99" />

</details>

<details>
  <summary>same 4 lines of text as fossil:</summary>
<img width="360" height="422" alt="image" src="https://github.com/user-attachments/assets/6eda33f0-22fc-4d86-a113-dea8f2607ed5" />
</details>



