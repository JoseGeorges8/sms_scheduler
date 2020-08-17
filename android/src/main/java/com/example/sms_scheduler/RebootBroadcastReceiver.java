package com.example.sms_scheduler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class RebootBroadcastReceiver extends BroadcastReceiver {

    public static void disableRescheduleOnReboot(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i("RebootBroadcastReceiver", "Rescheduling after boot!");
            SmsService.rescheduleMessage(context);
        }
    }

    /**
     * Schedules this {@code RebootBroadcastReceiver} to be run whenever the Android device reboots.
     */
    public static void enableRescheduleOnReboot(Context context) {
        scheduleOnReboot(context);
    }

    private static void scheduleOnReboot(Context context) {
        ComponentName receiver = new ComponentName(context, RebootBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}