package com.example.mytodo

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.Manifest


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Show the notification when alarm is triggered
        showTaskNotification(context!!, "16.50 PM") // Pass task time dynamically if required
    }

    fun showTaskNotification(context: Context, taskTime: String) {
        // Check if permission is required and granted (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use the correct permission for posting notifications
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request the permission
                if (context is Activity) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
                return
            }
        }

        // Proceed to show the notification if permission is granted
        val notificationBuilder = NotificationCompat.Builder(context, "task_channel_id")
            .setSmallIcon(R.drawable.baseline_notifications_active_24) // Replace with your notification icon
            .setContentTitle("Task Reminder")
            .setContentText("You have a task starting at $taskTime Today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notificationBuilder.build())
    }
    }



