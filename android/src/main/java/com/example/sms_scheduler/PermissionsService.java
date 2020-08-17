package com.example.sms_scheduler;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.flutter.plugin.common.PluginRegistry;

final class PermissionsService {
    interface PermissionsRegistry {
        void addListener(PluginRegistry.RequestPermissionsResultListener handler);
    }

    interface ResultCallback {
        void onResult(String errorCode, String errorDescription);
    }

    private static final int SMS_REQUEST_ID = 8;


    private  Activity activity;
    private PermissionsRegistry permissionsRegistry;

    public PermissionsService(Activity activity, PermissionsRegistry permissionsRegistry) {
        this.activity = activity;
        this.permissionsRegistry = permissionsRegistry;
    }

    private boolean ongoing = false;

    void requestPermissions(
            ResultCallback callback) {
        if (ongoing) {
            callback.onResult("permission", "Permission request ongoing");
        }
        if (!hasPermission(activity)) {
            permissionsRegistry.addListener(
                    new RequestPermissionsListener(
                            (String errorCode, String errorDescription) -> {
                                ongoing = false;
                                callback.onResult(errorCode, errorDescription);
                            }));
            ongoing = true;
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_REQUEST_ID
            );
        } else {
            // Permissions already exist. Call the callback with success.
            callback.onResult(null, null);
        }
    }

    private boolean hasPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @VisibleForTesting
    static final class RequestPermissionsListener
            implements PluginRegistry.RequestPermissionsResultListener {

        // There's no way to unregister permission listeners in the v1 embedding, so we'll be called
        // duplicate times in cases where the user denies and then grants a permission. Keep track of if
        // we've responded before and bail out of handling the callback manually if this is a repeat
        // call.
        boolean alreadyCalled = false;

        final ResultCallback callback;

        @VisibleForTesting
        RequestPermissionsListener(ResultCallback callback) {
            this.callback = callback;
        }

        @Override
        public boolean onRequestPermissionsResult(int id, String[] permissions, int[] grantResults) {
            if (alreadyCalled || id != SMS_REQUEST_ID) {
                return false;
            }

            alreadyCalled = true;
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                callback.onResult("permission", "Permission not granted");
            } else {
                callback.onResult(null, null);
            }
            return true;
        }
    }
}
