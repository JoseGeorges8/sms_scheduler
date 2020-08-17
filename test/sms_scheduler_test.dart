import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sms_scheduler/sms_scheduler.dart';

void main() {
  const MethodChannel channel = MethodChannel('sms_scheduler');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await SmsScheduler.getScheduledMessages, '42');
  });
}
