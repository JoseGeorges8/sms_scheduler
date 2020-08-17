package com.example.sms_scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static android.content.Context.ALARM_SERVICE;

public class SmsService {

    public static final String UPDATE_SCHEDULE_MESSAGES = "com.example.sms_scheduler.SmsService.UPDATE_SCHEDULE_MESSAGES";
    private static final String SCHEDULED_SMS_KEY = "sms_set";
    protected static final String SHARED_PREFERENCES_KEY = "sms_scheduler";
    protected static final String NOTIFY_ME = "notify_me";
    private static final Object messagesLock = new Object();

    public static void rescheduleMessage(Context context) {
        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        Set<String> scheduledMessages = p.getStringSet(SCHEDULED_SMS_KEY, null);
        if (scheduledMessages == null) {
            return;
        }
        for (String scheduledMessage : scheduledMessages) {
            int requestCode = Integer.parseInt(scheduledMessage);
            String key = getScheduledMessageKey(requestCode);
            String json = p.getString(key, null);
            try {
                JSONObject sms = new JSONObject(json);
                int id = sms.getInt("id");
                String message = sms.getString("message");
                long startMillis = sms.getLong("scheduled_at");
                long number = sms.getLong("number");
                scheduleMessage(
                        context,
                        id,
                        message,
                        number,
                        startMillis
                );
            } catch (JSONException ignored){}
        }


    }

    private static String getScheduledMessageKey(int requestCode) {
        return "scheduled_message_" + requestCode;
    }

    public static Set<String> getScheduledMessages(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        Set<String> messageIds = prefs.getStringSet(SCHEDULED_SMS_KEY, new HashSet<>());
        Set<String> messages = new HashSet<>();
        synchronized (messagesLock){
            for(String id: messageIds){
                String key = getScheduledMessageKey(Integer.parseInt(id));
                final String message = prefs.getString(key, null);
                if(message != null){
                    messages.add(message);
                }
            }
        }
        return messages;
    }

    public static void scheduleMessage(
            Context context,
            int id,
            String message,
            long number,
            long startMillis
    ) {
        // Create an intent and for sending the text message message to the intent
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("message", message);
        intent.putExtra("number", number);

        // Create a pending intent and set an alarm through the alarm manager
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        // after api 23 we can allow to activate an alarm while idle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
        }else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, startMillis, pendingIntent);
        }
    }

    public static void addScheduledMessage(
            Context context,
            int id,
            String message,
            long number,
            long startMillis
    ){
        HashMap<String, Object> scheduledMessage = new HashMap<>();
        scheduledMessage.put("message", message);
        scheduledMessage.put("number", number);
        scheduledMessage.put("scheduled_at", startMillis);
        scheduledMessage.put("id", id);

        JSONObject obj = new JSONObject(scheduledMessage);
        String key = getScheduledMessageKey(id);
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        Set<String> scheduledMessages = prefs.getStringSet(SCHEDULED_SMS_KEY, null);
        if (scheduledMessages == null) {
            scheduledMessages = new HashSet<>();
        }
        if (scheduledMessages.isEmpty()) {
            RebootBroadcastReceiver.enableRescheduleOnReboot(context);
        }
        scheduledMessages.add(Integer.toString(id));
        prefs
                .edit()
                .putString(key, obj.toString())
                .putStringSet(SCHEDULED_SMS_KEY, scheduledMessages)
                .apply();
        context.sendBroadcast(new Intent(SmsService.UPDATE_SCHEDULE_MESSAGES));
    }

    public static void cancelScheduledMessage(Context context, int id){
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent existingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        if (existingIntent == null) {
            Log.i(TAG, "cancel: broadcast receiver not found");
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(existingIntent);
    }

    public static void removeScheduledMessage(Context context, int id) {
        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        Set<String> scheduledMessages = p.getStringSet(SCHEDULED_SMS_KEY, null);

        if ((scheduledMessages == null) || !scheduledMessages.contains(Integer.toString(id))) {
            return;
        }
        scheduledMessages.remove(Integer.toString(id));
        String key = getScheduledMessageKey(id);
        p.edit().remove(key).putStringSet(SHARED_PREFERENCES_KEY, scheduledMessages).apply();

        if (scheduledMessages.isEmpty()) {
            RebootBroadcastReceiver.disableRescheduleOnReboot(context);
        }
        context.sendBroadcast(new Intent(SmsService.UPDATE_SCHEDULE_MESSAGES));
    }



    public static void updateScheduledMessages(
            Context context,
            Set<String> scheduledMessages
    ){
        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        p.edit().putStringSet(SHARED_PREFERENCES_KEY, scheduledMessages).apply();
        context.sendBroadcast(new Intent(SmsService.UPDATE_SCHEDULE_MESSAGES));
    }

}
