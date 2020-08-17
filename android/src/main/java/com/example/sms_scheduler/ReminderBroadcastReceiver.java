package com.example.sms_scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

import static com.example.sms_scheduler.SmsService.NOTIFY_ME;
import static com.example.sms_scheduler.SmsService.SHARED_PREFERENCES_KEY;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get info
        String message = intent.getStringExtra("message");
        long number = intent.getLongExtra("number", 0);
        int id = intent.getIntExtra("id", 0);
        if(number == 0) return;

        // Send message
        SmsManager kSmsManager = SmsManager.getDefault();
        kSmsManager.sendTextMessage(String.valueOf(number), null, message, null, null);

        // Now remove it
        SmsService.removeScheduledMessage(context, id);

        // Notification
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        boolean notifyMe = prefs.getBoolean(NOTIFY_ME, false);
        if(notifyMe){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyme")
                .setContentTitle("Message sent")
                .setContentText("Your message for "+ number +" was send successfully!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200, builder.build());
        }

    }
}

