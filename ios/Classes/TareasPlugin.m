#import "TareasPlugin.h"
#if __has_include(<tareas/tareas-Swift.h>)
#import <tareas/tareas-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tareas-Swift.h"
#endif

@implementation TareasPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTareasPlugin registerWithRegistrar:registrar];
}
@end
