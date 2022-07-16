import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:tareas/model/params.dart';
import 'package:tareas/tareas.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _tareasPlugin = Tareas();
  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    bool r = await _tareasPlugin.initialize(Params(
        multa: 1,
        minutosMulta: 16,
        minutosAnticipacion: 30,
        tiempo: 1653136847000,
        viajeId: "cc469250-61d8-11ec-bf11-ab47b812a8f0"));
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.

    print(r);
    if (!mounted) return;

    setState(() {
      _platformVersion = "platformVersion";
    });
  }

  void start() async {
    final r = await _tareasPlugin.start();
    debugPrint("$r");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            ElevatedButton(
                onPressed: () {
                  initPlatformState();
                },
                child: Text("Initialize")),
            ElevatedButton(
                onPressed: () {
                  start();
                },
                child: Text("Start")),
            ElevatedButton(
                onPressed: () async {
                  bool r = await _tareasPlugin.stop();
                  print(r);
                },
                child: Text("Stop"))
          ],
        ),
      ),
    );
  }
}
