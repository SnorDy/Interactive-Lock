package mkn.snordy.interactivelock.notifyService

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NLService : NotificationListenerService() {
    private val channelID = "channelID"
    private val isMyNotificationExtra = "is_my_notification"

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("Msg", "Notification arrived ${sbn.packageName},${sbn.id},${sbn.key},${sbn.uid}")
        if (isMyNotification(sbn)) {
            Log.d("NLService", "Ignoring my notification")
            return
        }
        cancelNotification(sbn.key)
//        createAndSendNotification(sbn)
    }

    private fun isMyNotification(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        return extras.getBoolean(isMyNotificationExtra, false)
    }

    private fun createAndSendNotification(sbn: StatusBarNotification) {
        val title = sbn.notification.extras.getString("android.title")
        val text = sbn.notification.extras.getString("android.text")

        if (title != null && text != null) {
            val builder =
                NotificationCompat.Builder(this, channelID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).setExtras(
                        Bundle().apply {
                            putBoolean(isMyNotificationExtra, true)
                        },
                    )

            val notificationManager = NotificationManagerCompat.from(this)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("NLService", "Missing POST_NOTIFICATIONS permission")
                return
            }

            notificationManager.notify(101, builder.build())
        } else {
            Log.w("NLService", "Could not extract title or text from notification")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("Msg", "Notification Removed")
        clearNotofication(sbn.id)
        cancelNotification(sbn.key)
    }

    private fun clearNotofication(notificationId: Int) {
        val ns = NOTIFICATION_SERVICE
        val nMgr = this.getSystemService(ns) as NotificationManager
        nMgr.cancel(notificationId)
    }
}
