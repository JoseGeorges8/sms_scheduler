package com.example.sms_scheduler;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * SmsSchedulerPlugin
 */
public class SmsSchedulerPlugin implements
        FlutterPlugin,
        MethodCallHandler,
        StreamHandler,
        ActivityAware {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private EventChannel eventChannel;

    private static final String channelName = "sms_scheduler";
    private static final String eventChannelName = "sms_scheduler_stream";

    private PermissionsService permissionsService;
    private Activity activity;
    private @Nullable FlutterPluginBinding flutterPluginBinding;
    private BroadcastReceiver scheduledMessagesBroadcastReceiver;


    private static void register(SmsSchedulerPlugin plugin, BinaryMessenger binaryMessenger) {
        plugin.channel = new MethodChannel(binaryMessenger, channelName, JSONMethodCodec.INSTANCE);
        plugin.channel.setMethodCallHandler(plugin);
        plugin.eventChannel = new EventChannel(binaryMessenger, eventChannelName, JSONMethodCodec.INSTANCE);
        plugin.eventChannel.setStreamHandler(plugin);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
        register(this, this.flutterPluginBinding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = null;
        this.channel.setMethodCallHandler(null);
        this.channel = null;
        this.eventChannel.setStreamHandler(null);
        this.eventChannel = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        // getting the arguments from the call
        JSONArray arguments = (JSONArray) call.arguments;

        switch (call.method) {
          case "getScheduledMessages":
                final Set<String> messages = SmsService.getScheduledMessages(this.activity);
                // updating the stream
                activity.sendBroadcast(new Intent(SmsService.UPDATE_SCHEDULE_MESSAGES));
                result.success(messages);
                break;
            case "scheduleMessage":
                permissionsService.requestPermissions((String errCode, String errDesc) -> {
                    if (errCode == null) {
                      try {
                          SmsService.scheduleMessage(this.activity, arguments.getInt(0), arguments.getString(1), arguments.getLong(2), arguments.getLong(3));
                          SmsService.addScheduledMessage(this.activity, arguments.getInt(0), arguments.getString(1), arguments.getLong(2), arguments.getLong(3));
                          result.success(null);
                      } catch (JSONException e) {
                          e.printStackTrace();
                          result.error("JSON", e.getMessage(), null);
                      } catch (Exception e){
                          result.error("UNKNOWN", e.getMessage(), null);
                      }
                    } else {
                      result.error(errCode, errDesc, null);
                    }
                });
                break;
          case "cancelMessage":
              try {
                  SmsService.cancelScheduledMessage(this.activity, arguments.getInt(0));
                  SmsService.removeScheduledMessage(this.activity, arguments.getInt(0));
                  result.success(null);
              } catch (JSONException e) {
                  result.error("JSON", e.getMessage(), null);
              }
              break;
          case "updateMessage":
              SmsService.updateScheduledMessages(this.activity, new HashSet<>());
              result.success(null);
              break;
            default:
                result.notImplemented();
                break;
        }

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
        NotificationService.createNotificationChannel(activity);
        assert flutterPluginBinding != null;
        setUpPermissions(
                activity,
                binding::addRequestPermissionsResultListener
        );
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onDetachedFromActivity() {
      channel.setMethodCallHandler(null);
      this.activity = null;
    }

    private void setUpPermissions(
            Activity activity,
            PermissionsService.PermissionsRegistry permissionsRegistry
    ) {
        permissionsService = new PermissionsService(
                activity,
                permissionsRegistry
        );
    }

    @Override
    public void onListen(Object arguments, EventSink events) {
        scheduledMessagesBroadcastReceiver = createScheduledMessagesReceiver(events);
        activity.registerReceiver(scheduledMessagesBroadcastReceiver, new IntentFilter(SmsService.UPDATE_SCHEDULE_MESSAGES));
    }

    @Override
    public void onCancel(Object arguments) {
        activity.unregisterReceiver(scheduledMessagesBroadcastReceiver);
        scheduledMessagesBroadcastReceiver = null;
    }

    private BroadcastReceiver createScheduledMessagesReceiver(final EventSink events) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Set<String> messages = SmsService.getScheduledMessages(context);
                events.success(messages);
            }
        };
    }
}
