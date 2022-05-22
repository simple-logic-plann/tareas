import 'package:flutter_test/flutter_test.dart';
import 'package:tareas/tareas.dart';
import 'package:tareas/tareas_platform_interface.dart';
import 'package:tareas/tareas_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockTareasPlatform 
    with MockPlatformInterfaceMixin
    implements TareasPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final TareasPlatform initialPlatform = TareasPlatform.instance;

  test('$MethodChannelTareas is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelTareas>());
  });

  test('getPlatformVersion', () async {
    Tareas tareasPlugin = Tareas();
    MockTareasPlatform fakePlatform = MockTareasPlatform();
    TareasPlatform.instance = fakePlatform;
  
    expect(await tareasPlugin.getPlatformVersion(), '42');
  });
}
