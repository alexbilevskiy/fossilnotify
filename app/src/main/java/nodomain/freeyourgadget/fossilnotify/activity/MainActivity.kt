package nodomain.freeyourgadget.fossilnotify.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService.Companion.INTENT_FILTER_ACTION
import nodomain.freeyourgadget.fossilnotify.service.gb.GBService
import nodomain.freeyourgadget.fossilnotify.service.notificationlistener.NotificationListenerService
import nodomain.freeyourgadget.fossilnotify.service.notificationsender.NotificationService
import nodomain.freeyourgadget.fossilnotify.service.ui.UiBroadcastReceiver
import nodomain.freeyourgadget.fossilnotify.ui.screens.MainScreen
import nodomain.freeyourgadget.fossilnotify.ui.theme.NotificationListenerExampleTheme
import nodomain.freeyourgadget.fossilnotify.ui.view_model.ViewModel


class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private val viewModel = ViewModel()

    private lateinit var notificationService: NotificationService
    private lateinit var uiBroadcastReceiver: UiBroadcastReceiver

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(applicationContext,"App is allowed to show notifications",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext,"App is NOT allowed to show notifications!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        notificationService = NotificationService(applicationContext)
        uiBroadcastReceiver = UiBroadcastReceiver(viewModel)
        registerReceiver(uiBroadcastReceiver, IntentFilter(INTENT_FILTER_ACTION))

        askListenNotificationsPermission()
        askPostNotificationsPermission()

        setContent {
            NotificationListenerExampleTheme {
                MainScreen(
                    text = viewModel.text,
                    onClickCreateNotify = {
                        notificationService.showNotification()
                    },
                    onClickCount = {
                        val intent = Intent(INTENT_FILTER_ACTION)
                        intent.putExtra("command", "count")
                        applicationContext.sendBroadcast(intent)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(uiBroadcastReceiver)
    }

    private fun askListenNotificationsPermission() {
        val cn = ComponentName(applicationContext, NotificationListenerService::class.java)
        val flat: String = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val enabled = flat.contains(cn.flattenToString())

        if (!enabled) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }
    }

    private fun askPostNotificationsPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) // temp
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}