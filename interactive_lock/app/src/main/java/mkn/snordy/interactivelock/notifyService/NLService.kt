package mkn.snordy.interactivelock.notifyService

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
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

class NLService : NotificationListenerService() {//отвечает за прослушку уведомлений
    private val channelID = "channelID"
    private val isMyNotificationExtra = "is_my_notification"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("Notification", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNotificationPosted(sbn: StatusBarNotification) {//когда пришло уведомление
        Log.d("Msg", "Notification arrived ${sbn.packageName},${sbn.id},${sbn.key},${sbn.uid}")
        if (isMyNotification(sbn)) {//если это уведомление моего приложения, то ничего не делаем
            Log.d("NLService", "Ignoring my notification")
            return
        }
        if (!sharedPreferences.contains(sbn.packageName)) {//если нет пометки, что у приложения есть новое уведомление
            editor.putBoolean(sbn.packageName, true).commit()
        }
        cancelNotification(sbn.key)//отменяем, чтобы уведомление не показывалось на экране
//        createAndSendNotification(sbn)
    }

    private fun isMyNotification(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        return extras.getBoolean(isMyNotificationExtra, false)
    }

    private fun createAndSendNotification(sbn: StatusBarNotification) {//это пока не работет, но планируется перенаправлять уведомления, чтобы сделать их некликабельными
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
