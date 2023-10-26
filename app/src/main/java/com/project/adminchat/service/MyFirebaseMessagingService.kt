package com.project.adminchat.service

import android.R
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.adminchat.MainActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 필요한 경우 서버에 토큰을 전송합니다.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
Log.d("JIWOUNG","fnwelkfewfwef")
        var intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = NotificationManagerCompat.from(
            applicationContext
        )

        var builder: NotificationCompat.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel("CHANNEL_ID1") == null) {
                val channel = NotificationChannel(
                    "CHANNEL_ID1",
                    "CHANNEL_NAME1",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            builder = NotificationCompat.Builder(applicationContext, "CHANNEL_ID1")
        } else {
            builder = NotificationCompat.Builder(applicationContext)
        }

        var title = "메세지가 왔어요."
        var body = "메세지를 확인해주세요."
        if (remoteMessage.notification != null){
            title = remoteMessage.notification!!.title ?: "타이틀없음"
            body = remoteMessage.notification!!.body?: "바디없음"
        }
        val data = remoteMessage.data


        val message = data["message"] ?: "0"
        val location = data["location"] ?: "장소없음"
        val myName = data["myName"] ?: "이름없음"
        val yourName = data["yourName"] ?: "이름없음"
        val content = data["content"] ?: "내용없음"
        val toToken = data["toToken"] ?: ""
        val fromToken = data["fromToken"] ?: ""

        Log.d("JWIJU","fklnewmrs4lm "+message+"||"+location+"||"+myName+"||"+yourName+"||"+content)

        builder
            .setSmallIcon(com.project.adminchat.R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notification: Notification = builder!!.build()
        notificationManager.notify(2, notification)

        if (isAppInForeground()) {
            Log.d("JIWOUNG","ernklgerg" +location)
            val intent = Intent("FCM_INTENT_FILTER")
            intent.putExtra("message",message)
            intent.putExtra("location",location)
            intent.putExtra("myName",myName)
            intent.putExtra("yourName",yourName)
            intent.putExtra("content",content)
            intent.putExtra("toToken",toToken)
            intent.putExtra("fromToken",fromToken)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else {
            saveMessageToSharedPreferences(message,location,myName,yourName,content,toToken,fromToken)
        }

    }
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun saveMessageToSharedPreferences(message: String, location: String,myName:String,yourName:String,content:String,toToken:String,fromToken:String) {
        val currentTimeMillis = System.currentTimeMillis()
        val sharedPreferences = getSharedPreferences("FCM_DATA", Context.MODE_PRIVATE)
        val messages = sharedPreferences.getStringSet("messages", mutableSetOf()) ?: mutableSetOf()

        val messageData = "$currentTimeMillis|~|$message|~|$location|~|$myName|~|$yourName|~|$content|~|$toToken|~|$fromToken"
        messages.add(messageData)

        sharedPreferences.edit().apply {
            putStringSet("messages", messages)
            apply()
        }
    }
}