package com.example.androidstudioproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class pushNotification : Service() {

        private lateinit var handler: Handler
        private lateinit var runnable: Runnable
        private lateinit var notificationManager: NotificationManager
        private var notificationId = 0

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()

            // Initialize the NotificationManager
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Initialize the Handler and Runnable
            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    // Send a "Hello" notification
                    val channelId = "default_channel_id"
                    val title = "Hello"
                    val message = "It's been 5 seconds since the last notification"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "Default Channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        notificationManager.createNotificationChannel(channel)
                    }

                    val notificationBuilder = NotificationCompat.Builder(this@pushNotification, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)

                    val notification = notificationBuilder.build()

                    notificationManager.notify(notificationId++, notification)

                    // Schedule the next notification
                    handler.postDelayed(this, 5000)
                }
            }
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // Start the Runnable
            handler.postDelayed(runnable, 5000)

            return START_STICKY
        }

        override fun onDestroy() {
            super.onDestroy()

            // Remove the Runnable callbacks
            handler.removeCallbacks(runnable)
        }
    }

