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
//  JRETestsTableViewController.m
//  JreEmulation
//

#import "JRETestsTableViewController.h"
#import "JRELogOutputStream.h"
#import "JRELogPaneViewController.h"

@interface JRETestsTableViewController ()

@property (nonatomic, strong) NSMutableArray *testNames;
@property (nonatomic, strong) NSMutableDictionary *testClasses;
@property (nonatomic, strong) NSString *testName;

@end

@implementation JRETestsTableViewController

- (instancetype)init {
  return [self initWithStyle:UITableViewStylePlain];
}

- (instancetype)initWithStyle:(UITableViewStyle)style {
  self = [super initWithStyle:style];
  if (self) {
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.navigationItem.title = @"JRE Test Suites";
    [self loadTests];
  }
  return self;
}

- (void)loadTests {
  NSArray *initialTests = @[
    // Test display name          Test class name
    @"All JRE Tests",             @"AllJreTests",
    @"Concurrency Tests",         @"jsr166.ConcurrencyTests",
    @"Crypto Tests",              @"com.google.j2objc.crypto.CryptoTests",
    @"IosSecurityProvider Tests", @"com.google.j2objc.security.IosSecurityProviderTests",
    @"java.beans Tests",          @"org.apache.harmony.beans.tests.java.beans.AllTests",
    @"java.io Tests",             @"libcore.java.io.SmallTests",
    @"java.net Tests",            @"libcore.java.net.SmallTests",
    @"java.nio Tests",            @"com.google.j2objc.nio.NioTests",
    @"java.text Tests",           @"libcore.java.text.SmallTests",
    @"java.util.zip Tests",       @"libcore.java.util.zip.SmallTests",
    @"Java 8 Tests",              @"com.google.j2objc.java8.SmallTests",
    @"JSON Tests",                @"libcore.org.json.SmallTests",
    @"Reflection Tests",          @"com.google.j2objc.ReflectionTests",
    @"Security Tests",            @"com.google.j2objc.security.SecurityTests",
  ];
  self.testNames = [NSMutableArray array];
  self.testClasses = [NSMutableDictionary dictionary];
  for (int i = 0; i < [initialTests count]; i += 2) {
    [self.testNames addObject:[initialTests objectAtIndex:i]];
    [self.testClasses setObject:[initialTests objectAtIndex:i + 1]
                         forKey:[initialTests objectAtIndex:i]];
  }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  [tableView deselectRowAtIndexPath:indexPath animated:NO];
  UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
  NSString *testName = cell.textLabel.text;
  NSString *className = [self.testClasses objectForKey:testName];
  if (!className) {
    NSLog(@"No test class name specified for %@", testName);
    return;
  }
  JRELogPaneViewController *logPane = AUTORELEASE(
      [[JRELogPaneViewController alloc] initWithTest:testName className:className]);
  [[self navigationController] pushViewController:logPane animated:YES];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [self.testNames count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"TestCell"
                                                          forIndexPath:indexPath];
  cell.textLabel.text = [self.testNames objectAtIndex:indexPath.row];
  return cell;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"TestCell"];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [_testNames release];
  [super dealloc];
}
#endif

@end
