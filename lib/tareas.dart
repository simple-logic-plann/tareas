import 'package:tareas/model/params.dart';

import 'tareas_platform_interface.dart';

class Tareas {
  Future<String?> getPlatformVersion() {
    return TareasPlatform.instance.getPlatformVersion();
  }

  Future<bool> initialize(Params params) {
    return TareasPlatform.instance.initialize(params);
  }

  Future<bool> stop() {
    return TareasPlatform.instance.stop();
  }
}
