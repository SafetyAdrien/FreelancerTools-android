package com.freelancertools.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FreelancerToolsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createTimerNotificationChannel()
    }

    private fun createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Smart Timer",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Minuteur de travail en cours avec le gain calculé en temps réel"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "smart_timer_channel"
    }
}
