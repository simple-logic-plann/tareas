import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:tareas/model/params.dart';

import 'tareas_method_channel.dart';

abstract class TareasPlatform extends PlatformInterface {
  /// Constructs a TareasPlatform.
  TareasPlatform() : super(token: _token);

  static final Object _token = Object();

  static TareasPlatform _instance = MethodChannelTareas();

  /// The default instance of [TareasPlatform] to use.
  ///
  /// Defaults to [MethodChannelTareas].
  static TareasPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [TareasPlatform] when
  /// they register themselves.
  static set instance(TareasPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> initialize(Params params) {
    throw UnimplementedError("initialize() has not been implemented.");
  }

  Future<bool> stop() {
    throw UnimplementedError("stop() has not been implemented");
  }
}
