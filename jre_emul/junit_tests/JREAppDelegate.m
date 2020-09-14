// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  JREAppDelegate.m
//  junit_tests
//

#import "JREAppDelegate.h"
#import "JRETestsTableViewController.h"

@implementation JREAppDelegate

- (BOOL)application:(UIApplication *)app didFinishLaunchingWithOptions:(NSDictionary *)options {
  self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];

  JRETestsTableViewController *testsViewController = [[JRETestsTableViewController alloc] init];
  UINavigationController *navController =
       [[UINavigationController alloc] initWithRootViewController:testsViewController];
  self.window.rootViewController = navController;

  self.window.backgroundColor = [UIColor whiteColor];
  [self.window makeKeyAndVisible];
  return YES;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [_window release];
  [super dealloc];
}
#endif

@end
