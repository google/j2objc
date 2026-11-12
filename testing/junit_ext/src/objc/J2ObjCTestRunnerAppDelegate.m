#import "J2ObjCTestRunnerAppDelegate.h"
#import "J2ObjCTestRunnerSceneDelegate.h"

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@implementation J2ObjCTestRunnerAppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary<UIApplicationLaunchOptionsKey, id> *)launchOptions {
  return YES;
}

- (UIWindow *)window {
  for (UIScene *scene in [UIApplication sharedApplication].connectedScenes) {
    if ([scene.delegate isKindOfClass:[J2ObjCTestRunnerSceneDelegate class]]) {
      J2ObjCTestRunnerSceneDelegate *sceneDelegate = (J2ObjCTestRunnerSceneDelegate *)scene.delegate;
      if (sceneDelegate.window) {
        return sceneDelegate.window;
      }
    }
  }
  return nil;
}

- (void)setWindow:(UIWindow *)window {
  [NSException
       raise:NSInternalInconsistencyException
      format:@"Setting the window directly on the UIApplicationDelegate is not supported "
             @"under the UIScene lifecycle. Use the window from the connected scene instead."];
}

- (UISceneConfiguration *)application:(UIApplication *)application
    configurationForConnectingSceneSession:(UISceneSession *)connectingSceneSession
                                   options:(UISceneConnectionOptions *)options {
  UISceneConfiguration *config =
      [[UISceneConfiguration alloc] initWithName:@"Default Configuration"
                                     sessionRole:connectingSceneSession.role];
  config.delegateClass = [J2ObjCTestRunnerSceneDelegate class];
  return config;
}

@end
