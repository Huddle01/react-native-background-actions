package com.backgroundactions

import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap

class BackgroundTaskOptions {
    private val extras: Bundle

    constructor(extras: Bundle) {
        this.extras = extras
    }

    constructor(reactContext: ReactContext, options: ReadableMap) {
        // Create extras
        extras = Arguments.toBundle(options) ?: throw IllegalArgumentException("Could not convert arguments to bundle")

        // Get taskTitle
        options.getString("taskTitle") ?: throw IllegalArgumentException("Task title cannot be null")

        // Get taskDesc
        options.getString("taskDesc") ?: throw IllegalArgumentException("Task description cannot be null")

        // Get iconInt
        try {
            val iconMap = options.getMap("taskIcon") ?: throw IllegalArgumentException("Task icon not found")
            val iconName = iconMap.getString("name") ?: throw IllegalArgumentException("Icon name not found")
            val iconType = iconMap.getString("type") ?: throw IllegalArgumentException("Icon type not found")
            val iconPackage = iconMap.getString("package") ?: reactContext.packageName

            val iconInt = reactContext.resources.getIdentifier(iconName, iconType, iconPackage)
            if (iconInt == 0) throw IllegalArgumentException("Icon resource not found")
            extras.putInt("iconInt", iconInt)
        } catch (e: Exception) {
            throw IllegalArgumentException("Task icon not found")
        }

        // Get color
        extras.putInt("color", try {
            Color.parseColor(options.getString("color"))
        } catch (e: Exception) {
            Color.parseColor("#ffffff")
        })

        // Get priority
        extras.putInt("priority", try {
            options.getInt("priority")
        } catch (e: Exception) {
            NotificationCompat.PRIORITY_DEFAULT
        })

        // Get importance
        extras.putInt("importance", try {
            options.getInt("importance")
        } catch (e: Exception) {
            NotificationManager.IMPORTANCE_DEFAULT
        })

        // Get ongoing
        extras.putBoolean("ongoing", try {
            options.getBoolean("ongoing")
        } catch (e: Exception) {
            true
        })

        // Get autoCancel
        extras.putBoolean("autoCancel", try {
            options.getBoolean("autoCancel")
        } catch (e: Exception) {
            false
        })

        // Get stopOnTerminate
        extras.putBoolean("stopOnTerminate", try {
            options.getBoolean("stopOnTerminate")
        } catch (e: Exception) {
            false
        })

        // Get serviceTypes
        extras.putStringArrayList("serviceTypes", try {
            options.getString("serviceTypes")?.split(",")?.let { ArrayList(it) } ?: ArrayList()
        } catch (e: Exception) {
            ArrayList()
        })
    }

    // Getters using Kotlin properties
    val taskTitle: String
        get() = extras.getString("taskTitle", "")

    val taskDesc: String
        get() = extras.getString("taskDesc", "")

    @get:IdRes
    val iconInt: Int
        get() = extras.getInt("iconInt")

    @get:ColorInt
    val color: Int
        get() = extras.getInt("color")

    val linkingURI: String?
        get() = extras.getString("linkingURI")

    val progressBar: Bundle?
        get() = extras.getBundle("progressBar")

    val priority: Int
        get() = extras.getInt("priority")

    val importance: Int
        get() = extras.getInt("importance")

    val isOngoing: Boolean
        get() = extras.getBoolean("ongoing")

    val isAutoCancel: Boolean
        get() = extras.getBoolean("autoCancel")

    val isStopOnTerminate: Boolean
        get() = extras.getBoolean("stopOnTerminate")

    val serviceTypes: ArrayList<String>?
        get() = extras.getStringArrayList("serviceTypes")

    // Keep the getExtras() method for compatibility
    fun getExtras(): Bundle = extras
}