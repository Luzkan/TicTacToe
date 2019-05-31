package com.luzkan.tictactoe.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luzkan.tictactoe.interfaces.Util
import com.luzkan.tictactoe.interfaces.Util.getCurrentUserId
import com.luzkan.tictactoe.activity.MainActivity
import com.luzkan.tictactoe.database.models.User

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(LOG_TAG, "onReceive: " + intent.action!!)
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(Util.getCurrentUsername()).child("info")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val me = dataSnapshot.getValue(User::class.java)

                    val client = OkHttpClient()

                    val to = intent.extras!!.getString("to")
                    val withId = intent.extras!!.getString("withId")

                    val format = String
                        .format(
                            "%s/sendNotification?to=%s&fromPushId=%s&fromId=%s&fromName=%s&type=%s",
                            "https://tictactoe-fb78f.firebaseio.com/",
                            to,
                            me!!.uid,
                            getCurrentUserId(),
                            me.displayName,
                            intent.action
                        )

                    Log.d(LOG_TAG, "onDataChange: $format")
                    val request = Request.Builder()
                        .url(format)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {

                        }
                    })

                    if (intent.action == "accept") {
                        val gameId = withId + "-" + getCurrentUserId()
                        FirebaseDatabase.getInstance().reference.child("games")
                            .child(gameId)
                            .setValue(null)

                        context.startActivity(
                            Intent(context, MainActivity::class.java)
                                .putExtra("type", "wifi")
                                .putExtra("me", "o")
                                .putExtra("gameId", gameId)
                                .putExtra("with", withId)
                        )
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

    }

    companion object {
        private val LOG_TAG = "MyReceiver"
    }
}
