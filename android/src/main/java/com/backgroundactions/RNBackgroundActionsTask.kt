package com.backgroundactions

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactNativeHost
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.modules.core.DeviceEventManagerModule

class RNBackgroundActionsTask : HeadlessJsTaskService() {

    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        return intent.extras?.let { extras ->
            HeadlessJsTaskConfig(
                extras.getString("taskName"),
                Arguments.fromBundle(extras),
                0,
                true
            )
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras ?: throw IllegalArgumentException("Extras cannot be null")
        val bgOptions = BackgroundTaskOptions(extras)

        // Create notification channel for API 26+
        createNotificationChannel(bgOptions.taskTitle, bgOptions.taskDesc)

        // Create the notification
        val notification = buildNotification(this, bgOptions)

        if (bgOptions.isOngoing) {
            notification.flags = notification.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        }

        val type = getServiceType(bgOptions.serviceTypes ?: ArrayList())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SERVICE_NOTIFICATION_ID, notification, type)
        } else {
            startForeground(SERVICE_NOTIFICATION_ID, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        handleTaskTermination()
    }

    override fun onDestroy() {
        super.onDestroy()
        handleTaskTermination()
    }

    private fun handleTaskTermination() {
        try {
            val params = Arguments.createMap()
            getReactContext()?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("appKilled", params)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (currentBgOptions?.isStopOnTerminate == true) {
            stopSelf()
        }
    }

    private fun createNotificationChannel(taskTitle: String, taskDesc: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, taskTitle, importance).apply {
                description = taskDesc
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun getServiceType(serviceTypeArray: ArrayList<String>): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return 0

        return serviceTypeArray.fold(0) { acc, serviceType ->
            acc or when (serviceType) {
                "camera" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                "connectedDevice" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                "dataSync" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                "health" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                } else 0
                "location" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                "mediaPlayback" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                "mediaProjection" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                "microphone" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                "phoneCall" -> ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                "remoteMessaging" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
                } else 0
                "shortService" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
                } else 0
                "specialUse" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else 0
                "systemExempted" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                } else 0
                else -> 0
            }
        }
    }

    companion object {
        const val SERVICE_NOTIFICATION_ID = 92901
        private const val CHANNEL_ID = "RN_BACKGROUND_ACTIONS_CHANNEL"
        private var currentBgOptions: BackgroundTaskOptions? = null

        @SuppressLint("UnspecifiedImmutableFlag")
        fun buildNotification(context: Context, bgOptions: BackgroundTaskOptions): Notification {
            currentBgOptions = bgOptions

            val taskTitle = bgOptions.taskTitle
            val taskDesc = bgOptions.taskDesc
            val iconInt = bgOptions.iconInt
            val color = bgOptions.color
            val linkingURI = bgOptions.linkingURI

            val notificationIntent = if (linkingURI != null) {
                Intent(Intent.ACTION_VIEW, Uri.parse(linkingURI))
            } else {
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val contentIntent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    PendingIntent.getActivity(
                        context, 0, notificationIntent,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                }
               /* Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    PendingIntent.getActivity(
                        context, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } */
                else -> {
                    PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setContentTitle(taskTitle)
                setContentText(taskDesc)
                setSmallIcon(iconInt)
                setContentIntent(contentIntent)
                setOngoing(bgOptions.isOngoing)
                setAutoCancel(bgOptions.isAutoCancel)
                setPriority(bgOptions.priority)
                setColor(color)

                bgOptions.progressBar?.let { progressBarBundle ->
                    val progressMax = progressBarBundle.getDouble("max").toInt()
                    val progressCurrent = progressBarBundle.getDouble("value").toInt()
                    val progressIndeterminate = progressBarBundle.getBoolean("indeterminate")
                    setProgress(progressMax, progressCurrent, progressIndeterminate)
                }
            }

            return builder.build()
        }
    }
}
