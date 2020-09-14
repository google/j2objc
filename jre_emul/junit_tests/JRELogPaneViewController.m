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
//  JRELogPaneViewController.m
//  JreEmulation
//

#import "JRELogOutputStream.h"
#import "JRELogPaneView.h"
#import "JRELogPaneViewController.h"
#import "JRETestRunListener.h"

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "NSString+JavaString.h"
#import "java/io/PrintStream.h"
#import "java/lang/System.h"
#import "java/lang/Thread.h"
#import "org/junit/internal/RealSystem.h"
#import "org/junit/runner/JUnitCore.h"

@interface JRELogPaneViewController ()

@property (nonatomic, strong) NSString *testName;
@property (nonatomic, strong) NSString *className;
@property (atomic, strong) JavaLangThread *testThread;
@property (nonatomic, strong) JRELogPaneView *logPane;

@property (retain, nonatomic) IBOutlet UIView *textPane;
@property (retain, nonatomic) IBOutlet UIButton *saveButton;

@end

@interface OrgJunitRunnerJUnitCore ()

- (OrgJunitRunnerResult *)
    runMainWithOrgJunitInternalJUnitSystem:(id<OrgJunitInternalJUnitSystem>)system
                         withNSStringArray:(IOSObjectArray *)args;

@end


@implementation JRELogPaneViewController

- (instancetype)initWithTest:(NSString *)testName className:(NSString *)className {
  if ((self = [super initWithNibName:nil bundle:nil])) {
    self.testName = testName;
    self.navigationItem.title = testName;
    self.className = className;
  }
  return self;
}

- (void)viewDidAppear:(BOOL)animated {
  CGRect textFrame = self.textPane.frame;
  textFrame.size.height -= 20;
  self.logPane = [[JRELogPaneView alloc] initWithFrame:self.textPane.frame];
  [self.textPane addSubview:self.logPane];
  [self.saveButton setEnabled:NO];
  [self.logPane setDelegate:self];

  // Redirect all stdout and stderr output to the log pane.
  JRELogOutputStream *logStream = [[JRELogOutputStream alloc] initWithJRELogPane:self.logPane];
  JavaIoPrintStream *printStream =
      [[JavaIoPrintStream alloc] initWithJavaIoOutputStream:logStream withBoolean:YES];
  [JavaLangSystem setOutWithJavaIoPrintStream:printStream];
  [JavaLangSystem setErrWithJavaIoPrintStream:printStream];
  RELEASE_(printStream);
  RELEASE_(logStream);

  // Execute test runner on new dispatch queue.
  dispatch_queue_t backgroundQueue =
      dispatch_queue_create("JUnit Test Runner", DISPATCH_QUEUE_CONCURRENT);
  dispatch_async(backgroundQueue, ^{
    self.testThread = [JavaLangThread currentThread];
    IOSObjectArray *testClasses =
        [IOSObjectArray arrayWithObjects:(id[]) { self.className }
                                   count:1
                                    type:NSString_class_()];
    JRETestRunListener *testListener = AUTORELEASE([[JRETestRunListener alloc] init]);
    OrgJunitRunnerJUnitCore *testRunner = AUTORELEASE([[OrgJunitRunnerJUnitCore alloc] init]);
    [testRunner addListenerWithOrgJunitRunnerNotificationRunListener:testListener];
    id<OrgJunitInternalJUnitSystem> junitSystem =
        AUTORELEASE([[OrgJunitInternalRealSystem alloc] init]);
    [testRunner runMainWithOrgJunitInternalJUnitSystem:junitSystem withNSStringArray:testClasses];
    self.testThread = nil;
  });
  [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
  if (self.testThread) {
    [self.testThread interrupt];
    self.testThread = nil;
  }
  [super viewWillDisappear:animated];
}

- (void)textViewDidChange:(UITextView *)textView {
  // Some text has been written to log pane, so enable save button.
  [self.saveButton setEnabled:YES];
  [self.logPane setDelegate:nil];
}

- (IBAction)saveLog:(id)sender {
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSArray *dirPaths =
  NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *docsDir = dirPaths[0];
  NSString *logFileName = [NSString stringWithFormat:@"%@.log", self.className];
  NSString *dataFile = [docsDir stringByAppendingPathComponent:logFileName];
  NSData *databuffer = [self.logPane.text dataUsingEncoding: NSUTF8StringEncoding];
  [filemgr createFileAtPath: dataFile contents: databuffer attributes:nil];

  UIAlertController *alert =
      [UIAlertController alertControllerWithTitle:nil
                                          message:@"Log saved"
                                   preferredStyle:UIAlertControllerStyleAlert];
  [self presentViewController:alert animated:YES completion:nil];
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)),
                 dispatch_get_main_queue(), ^{
    [self dismissViewControllerAnimated:YES completion:nil];
  });
}

@end
