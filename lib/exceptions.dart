/// This is thrown when the plugin reports an error.
abstract class SmsSchedulerException implements Exception {
  final String code;
  final String description;

  SmsSchedulerException(this.code, this.description);

  @override
  String toString() => '$runtimeType($code, $description)';
}

class SmsSchedulerPermissionException extends SmsSchedulerException {
  SmsSchedulerPermissionException(String code, String description) : super(code, description);
}
class UnknownException extends SmsSchedulerException {
  UnknownException(String code, String description) : super(code, description);
}