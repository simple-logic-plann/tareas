import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:tareas/tareas_method_channel.dart';

void main() {
  MethodChannelTareas platform = MethodChannelTareas();
  const MethodChannel channel = MethodChannel('tareas');

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
    expect(await platform.getPlatformVersion(), '42');
  });
}
