import 'dart:math';

import 'package:flutter/material.dart';

class Message {
  /// uniquely identifies the message
  int _messageId;
  /// whom the message is intended to
  int number;
  /// the message to send
  String message;
  /// when should the message be sent
  DateTime scheduledTime;
  /// if the message was sent
  bool _sent;

  /// getters
  int get messageId => _messageId;
  bool get sent => _sent;

  Message({
    @required this.number,
    @required this.message,
    this.scheduledTime}){
    this.scheduledTime ??= DateTime.now();
    this._sent = false;
    this._messageId ??= Random().nextInt(pow(2, 31));
  }

  factory Message.fromJson(Map<String, dynamic> json) {
    var message = Message(
      number: json["number"],
      message: json["message"],
      scheduledTime: DateTime.fromMillisecondsSinceEpoch(json["scheduled_at"]),
    );

    message._messageId = json["id"];
    message._sent = json["sent"];
    return message;
  }
  
  Map<String, dynamic> toJson() => {
   'id' : _messageId,
    'number': this.number,
    'message': this.message,
    'scheduled_at': this.scheduledTime.millisecondsSinceEpoch.toString()
  };


}