import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:tareas/model/params.dart';

import 'tareas_platform_interface.dart';

/// An implementation of [TareasPlatform] that uses method channels.
class MethodChannelTareas extends TareasPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('tareas');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> initialize(Params params) async {
    try {
      return await methodChannel.invokeMethod<bool>(
              "initialize", params.toMap) ??
          false;
    } catch (e) {
      print(e);
      return false;
    }
  }

  @override
  Future<bool> start() async {
    try {
      return await methodChannel.invokeMethod("start");
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> stop() async {
    try {
      return await methodChannel.invokeMethod<bool>("stop") ?? false;
    } catch (e) {
      print(e);
      return false;
    }
  }
}
