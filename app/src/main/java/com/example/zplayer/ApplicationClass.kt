package com.example.zplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

//this class executes after install app even user start app or not
class ApplicationClass : Application() {
    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"  //both for play/pause
        const val PREVIOUS = "previous"
        const val NEXT = "next"
        const val EXIT = "exit"
    }
    override fun onCreate() {
        super.onCreate()
        // check current android version with android version API 26
        val notificationChannel = NotificationChannel(CHANNEL_ID, "Now Playing song", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = "This is an important channel for showing songs"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}