#import "J2ObjCTestRunnerSceneDelegate.h"

#import <UIKit/UIKit.h>

@implementation J2ObjCTestRunnerSceneDelegate

- (void)scene:(UIScene *)scene
    willConnectToSession:(UISceneSession *)session
                 options:(UISceneConnectionOptions *)connectionOptions {
  if (![scene isKindOfClass:[UIWindowScene class]]) {
    return;
  }

  UIWindowScene *windowScene = (UIWindowScene *)scene;
  UIWindow *window = [[UIWindow alloc] initWithWindowScene:windowScene];
  self.window = window;
  window.backgroundColor = [UIColor whiteColor];
  UIViewController *controller = [[UIViewController alloc] initWithNibName:nil bundle:nil];
  window.rootViewController = controller;
  [window makeKeyAndVisible];
}

@end
