package com.backgroundactions

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.*

@ReactModule(name = BackgroundActionsModule.NAME)
class BackgroundActionsModule(reactContext: ReactApplicationContext) :
  NativeBackgroundActionsSpec(reactContext) {

  private val reactContext: ReactContext = reactContext
  private var currentServiceIntent: Intent? = null
  override fun getName(): String {
    return NAME
  }

   @ReactMethod
   override fun start(options: ReadableMap, promise: Promise) {
        try {
            // Stop any other intent
            currentServiceIntent?.let { reactContext.stopService(it) }

            // Create the service
            currentServiceIntent = Intent(reactContext, RNBackgroundActionsTask::class.java).apply {
                // Get the task info from the options
                val bgOptions = BackgroundTaskOptions(reactContext, options)
                putExtras(bgOptions.getExtras())
            }

            // Start the task
            reactContext.startService(currentServiceIntent)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
    override fun stop(promise: Promise) {
        currentServiceIntent?.let { reactContext.stopService(it) }
        promise.resolve(null)
    }

    @ReactMethod
  override fun updateNotification(options: ReadableMap, promise: Promise) {
        try {
            val bgOptions = BackgroundTaskOptions(reactContext, options)
            val notification = RNBackgroundActionsTask.buildNotification(reactContext, bgOptions)
            val notificationManager = reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(RNBackgroundActionsTask.SERVICE_NOTIFICATION_ID, notification)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
  override fun addListener(eventName: String) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
  override fun removeListeners(count: Double) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

  companion object {
    const val NAME = "BackgroundActions"
  }
}
