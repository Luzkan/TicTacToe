package com.luzkan.tictactoe.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.luzkan.tictactoe.interfaces.Util

class MyFirebaseInstanceIdService : FirebaseMessagingService() {

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

    companion object {
        private val LOG_TAG = "MyFirebaseInstanceId"
    }
}
