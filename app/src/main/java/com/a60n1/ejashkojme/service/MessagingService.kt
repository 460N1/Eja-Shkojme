package com.a60n1.ejashkojme.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.a60n1.ejashkojme.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

@Suppress("DEPRECATION")
class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        val clickAction = remoteMessage.notification!!.clickAction
        val fromUserId = remoteMessage.data["from_user_id"]
        val addressUserId = remoteMessage.data["address_user_id"]
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round)
        val mBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
        val resultIntent = Intent(clickAction)
        resultIntent.putExtra("user_id", fromUserId)
        resultIntent.putExtra("origin", addressUserId)
        val resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationId = System.currentTimeMillis().toInt()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Objects.requireNonNull(notificationManager).notify(mNotificationId, mBuilder.build())
    }
}