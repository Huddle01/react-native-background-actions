package com.asterinet.react.bgactions;

import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class BackgroundTaskOptions {
    private final Bundle extras;

    public BackgroundTaskOptions(@NonNull final Bundle extras) {
        this.extras = extras;
    }

    public BackgroundTaskOptions(@NonNull final ReactContext reactContext, @NonNull final ReadableMap options) {
        // Create extras
        extras = Arguments.toBundle(options);
        if (extras == null)
            throw new IllegalArgumentException("Could not convert arguments to bundle");
        // Get taskTitle
        try {
            if (options.getString("taskTitle") == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task title cannot be null");
        }
        // Get taskDesc
        try {
            if (options.getString("taskDesc") == null)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task description cannot be null");
        }
        // Get iconInt
        try {
            final ReadableMap iconMap = options.getMap("taskIcon");
            if (iconMap == null)
                throw new IllegalArgumentException();
            final String iconName = iconMap.getString("name");
            final String iconType = iconMap.getString("type");
            String iconPackage;
            try {
                iconPackage = iconMap.getString("package");
                if (iconPackage == null)
                    throw new IllegalArgumentException();
            } catch (Exception e) {
                // Get the current package as default
                iconPackage = reactContext.getPackageName();
            }
            final int iconInt = reactContext.getResources().getIdentifier(iconName, iconType, iconPackage);
            extras.putInt("iconInt", iconInt);
            if (iconInt == 0)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Task icon not found");
        }
        // Get color
        try {
            final String color = options.getString("color");
            extras.putInt("color", Color.parseColor(color));
        } catch (Exception e) {
            extras.putInt("color", Color.parseColor("#ffffff"));
        }

        try {
            final int priority = options.getInt("priority");
            extras.putInt("priority", priority);
        } catch (Exception e) {
            extras.putInt("priority", NotificationCompat.PRIORITY_DEFAULT);
        }

        try {
            final int importance = options.getInt("importance");
            extras.putInt("importance", importance);
        } catch (Exception e) {
            extras.putInt("importance", NotificationManager.IMPORTANCE_DEFAULT);
        }

        try {
            final boolean ongoing = options.getBoolean("ongoing");
            extras.putBoolean("ongoing", ongoing);
        } catch (Exception e) {
            extras.putBoolean("ongoing", true);
        }

        try {
            final boolean autoCancel = options.getBoolean("autoCancel");
            extras.putBoolean("autoCancel", autoCancel);
        } catch (Exception e) {
            extras.putBoolean("autoCancel", false);
        }

        try {
            final boolean stopOnTerminate = options.getBoolean("stopOnTerminate");
            extras.putBoolean("stopOnTerminate", stopOnTerminate);
        } catch (Exception e) {
            extras.putBoolean("stopOnTerminate", false);
        }

        try {
            final String serviceTypes = options.getString("serviceTypes");
            ArrayList<String> serviceTypeList = new ArrayList<>();
            
            if (serviceTypes != null) {
                Collections.addAll(serviceTypeList, serviceTypes.split(","));    
            }

            extras.putStringArrayList("serviceTypes", serviceTypeList);
        } catch (Exception e) {
            extras.putStringArrayList("serviceTypes", new ArrayList<>());
        }
    }

    public Bundle getExtras() {
        return extras;
    }

    public String getTaskTitle() {
        return extras.getString("taskTitle", "");
    }

    public String getTaskDesc() {
        return extras.getString("taskDesc", "");
    }

    @IdRes
    public int getIconInt() {
        return extras.getInt("iconInt");
    }

    @ColorInt
    public int getColor() {
        return extras.getInt("color");
    }

    @Nullable
    public String getLinkingURI() {
        return extras.getString("linkingURI");
    }

    @Nullable
    public Bundle getProgressBar() {
        return extras.getBundle("progressBar");
    }

    public int getPriority() {
        return extras.getInt("priority");
    }

    public int getImportance() {
        return extras.getInt("importance");
    }

    public boolean isOngoing() {
        return extras.getBoolean("ongoing");
    }

    public boolean isAutoCancel() {
        return extras.getBoolean("autoCancel");
    }

    public boolean isStopOnTerminate() {
        return extras.getBoolean("stopOnTerminate");
    }

    public ArrayList<String> getServiceTypes() {
        return extras.getStringArrayList("serviceTypes");
    }
}