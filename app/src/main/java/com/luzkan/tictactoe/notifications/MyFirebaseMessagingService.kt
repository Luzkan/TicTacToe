package com.luzkan.tictactoe.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.luzkan.tictactoe.activity.MainActivity
import com.luzkan.tictactoe.R
import com.luzkan.tictactoe.activity.PlayActivity

import android.support.v4.app.NotificationCompat.PRIORITY_MAX
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.luzkan.tictactoe.interfaces.Util
import com.luzkan.tictactoe.interfaces.Util.getCurrentUserId

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        Log.d("MyFirebaseInstanceId", "onTokenRefresh: $token")
        val refreshedToken = FirebaseInstanceId.getInstance().token
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            return
        }
        //sendRegistrationToServer(token)

        Util.savePushToken(refreshedToken, currentUser.uid)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val fromPushId = remoteMessage.getData().get("fromPushId")
        val fromId = remoteMessage.getData().get("fromId")
        val fromName = remoteMessage.getData().get("fromName")
        val type = remoteMessage.getData().get("type")
        Log.d(LOG_TAG, "onMessageReceived: ")

        if (type == "invite") {
            handleInviteIntent(fromPushId!!, fromId!!, fromName!!)
        } else if (type == "accept") {
            startActivity(
                Intent(getBaseContext(), MainActivity::class.java)
                    .putExtra("type", "wifi")
                    .putExtra("me", "x")
                    .putExtra("gameId", getCurrentUserId() + "-" + fromId)
                    .putExtra("withId", fromId)
            )
        } else if (type == "reject") {
            val mBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MAX)
                .setContentTitle(String.format("%s rejected your invite!", fromName))

            val resultIntent = Intent(this, MainActivity::class.java)
                .putExtra("type", "wifi")
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(PlayActivity::class.java!!)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            mBuilder.setContentIntent(resultPendingIntent)
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(1, mBuilder.build())
        }
    }

    private fun handleInviteIntent(fromPushId: String, fromId: String, fromName: String) {
        val rejectIntent = Intent(getApplicationContext(), MyReceiver::class.java)
            .setAction("reject")
            .putExtra("withId", fromId)
            .putExtra("to", fromPushId)

        val pendingIntentReject = PendingIntent.getBroadcast(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val gameId = fromId + "-" + getCurrentUserId()
        //            FirebaseDatabase.getInstance().getReference().child("games")
        //                    .child(gameId)
        //                    .setValue(null);
        val acceptIntent = Intent(getApplicationContext(), MyReceiver::class.java)
            .setAction("accept")
            .putExtra("withId", fromId)
            .putExtra("to", fromPushId)
        val pendingIntentAccept = PendingIntent.getBroadcast(this, 2, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val resultIntent = Intent(this, MainActivity::class.java)
            .putExtra("type", "wifi")
            .putExtra("withId", fromId)
            .putExtra("to", fromPushId)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(PlayActivity::class.java!!)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val build = NotificationCompat.Builder(this,
            INVITE
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MAX)
            .setContentTitle(String.format("%s invites you to play!", fromName))
            .addAction(R.drawable.accept, "Accept", pendingIntentAccept)
            .setVibrate(LongArray(3000))
            .setChannelId(INVITE)
            .setContentIntent(resultPendingIntent)
            .addAction(R.drawable.cancel, "Reject", pendingIntentReject)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(
                INVITE,
                INVITE, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(1, build)
    }

    companion object {
        private val LOG_TAG = "MyFirebaseMessaging"
        val INVITE = "invite"
    }
}
