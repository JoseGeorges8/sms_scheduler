# sms_scheduler

A simple flutter plugin for scheduling sms! Currently only supports Android

## Getting Started

To schedule a message, all you need is to call the `scheduleMessage` method and pass the number, message, and when to send the message. If you skip the `scheduledTime` the message will be sent right away

	SmsScheduler.scheduleMessage(Message(
                  number: xxxxxxxxxx,
                  message: 'Your sms scheduler works!',
                  scheduledTime: DateTime.now().add(Duration(seconds: 10))
              )

To get a list of the messages you've scheduled, call this method.

    SmsScheduler.getScheduledMessages;

You can also listen to changes on the list of scheduled messages.

    SmsScheduler.smsSchedulerStream.listen((messages) {
		// do anything with the messages here
	});

Using a stream builder...

	StreamBuilder(
          stream: SmsScheduler.smsSchedulerStream,
            builder: (context, AsyncSnapshot<List<Message>> snapshot){
              if (snapshot.hasData) {
			  /// messages here...
              } else if
			  /// something went wrong...
              }
              return Center(
                child: CircularProgressIndicator(),
              );
            }
        ),

Cancelling an scheduled message is as easy as:

	 SmsScheduler.cancelMessage(message);

