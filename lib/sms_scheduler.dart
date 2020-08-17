
import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:sms_scheduler/exceptions.dart';
import 'package:sms_scheduler/models/message.dart';

class SmsScheduler {
  static const MethodChannel _channel =
      const MethodChannel('sms_scheduler',  JSONMethodCodec());

  static const EventChannel _eventChannel = const EventChannel('sms_scheduler_stream',  JSONMethodCodec());
  static Stream<List<Message>> _scheduledMessages;

  static Future<List<Message>> get getScheduledMessages async {
    List<Message> messages = [];
    final List<dynamic> messagesStr = await _channel.invokeMethod('getScheduledMessages');
    if(messagesStr != null){
      for(String messageStr in messagesStr){
        messages.add(Message.fromJson(jsonDecode(messageStr)));
      }
    }
    return messages;
  }

  static Stream<List<Message>> get smsSchedulerStream {
    _scheduledMessages ??= _eventChannel.receiveBroadcastStream().map((messagesStr) {
      List<Message> messages = [];
      for(String messageStr in messagesStr){
        messages.add(Message.fromJson(jsonDecode(messageStr)));
      }
      return messages;
    });
    return _scheduledMessages;
  }

  static Future<void> scheduleMessage(Message message) async {
    try{
      await _channel.invokeMethod('scheduleMessage', [
        message.messageId,
        message.message,
        message.number,
        message.scheduledTime.toUtc().millisecondsSinceEpoch,
      ]);
    } on PlatformException catch (e){
      if(e.code == 'permission'){
        throw SmsSchedulerPermissionException(e.code, e.message);
      }else{
        throw UnknownException(e.code, e.message);
      }
    }
  }

  static Future<void> cancelMessage(Message message) async {
    await _channel.invokeMethod('cancelMessage', [jsonEncode(message.messageId)]);
  }

  static Future<Message> updateMessage(Message newMessage) async {
    final Message message = await _channel.invokeMethod('updateMessage', []);
    return message;
  }
}
