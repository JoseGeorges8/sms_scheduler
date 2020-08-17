import 'package:flutter/material.dart';

import 'package:sms_scheduler/models/message.dart';
import 'package:sms_scheduler/sms_scheduler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  final _dismissibleKey = GlobalKey();

  @override
  void initState() {
    SmsScheduler.smsSchedulerStream.listen((messages) {
    });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      SmsScheduler.getScheduledMessages;
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: StreamBuilder(
          stream: SmsScheduler.smsSchedulerStream,
            builder: (context, AsyncSnapshot<List<Message>> snapshot){
              if (snapshot.hasData) {
                List<Message> messages = snapshot.data..sort((a, b) => a.scheduledTime.compareTo(b.scheduledTime));

                if(messages.isEmpty){

                  return Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.send, size: MediaQuery.of(context).size.width * .6,),
                        Text('No messages pending!'),
                      ],
                    ),
                  );

                }
                return Center(
                  child: ListView.builder(
                      itemCount: messages.length,
                      itemBuilder: (context, index) => Dismissible(
                        key: GlobalKey(),
                        onDismissed: (direction) {
                          SmsScheduler.cancelMessage(messages[index]);
                          // Show a snackbar. This snackbar could also contain "Undo" actions.
                          Scaffold
                              .of(context)
                              .showSnackBar(SnackBar(content: Text("Message dismissed")));
                        },
                        child: ListTile(
                          title: Text('${messages[index].number}'),
                          subtitle: Text('${messages[index].message}'),
                        ),
                      )
                  ),
                );
              } else if (snapshot.hasError) {
                return Center(
                  child: Text('something went wrong!'),
                );
              }

              return Center(
                child: CircularProgressIndicator(),
              );
            }
        ),
        floatingActionButton: Builder(
          builder: (context) => FloatingActionButton(
            child: Icon(Icons.add),
            onPressed: () {
              SmsScheduler.scheduleMessage(Message(
                  number: 2262022111,
                  message: 'Your sms scheduler works!',
                  scheduledTime: DateTime.now().add(Duration(seconds: 10))
              ))
                  .then((value) {
                    Scaffold.of(context).showSnackBar(SnackBar(content: Text('Message scheduled!', style: TextStyle(color: Colors.white),), backgroundColor: Colors.green));
                    setState(() {});
                  });
            },
          ),
        ),
      ),
    );
  }
}
