
#import "com/google/j2objc/testing/JUnitTestRunner.h"

#include <errno.h>
#include <execinfo.h>
#include <signal.h>
#include <stdio.h>

#import <UIKit/UIKit.h>

static void signalHandler(int sig) {
  // Ensure this looks like a GTM_UNIT_TESTING failure.
  printf("[  FAILED  ]\n");

  // Get void*'s for all entries on the stack.
  void *array[64];
  int frame_count = (int) backtrace(array, 64);

  // Print all the frames to stderr.
  fprintf(stderr, "Terminating j2objc JUnit TestRunnerMain.m due to signal: %d\n", sig);
  fprintf(stderr, "Stack trace:\n");
  backtrace_symbols_fd(array, frame_count, 2);

  exit(EXIT_FAILURE);
}

void installSignalHandler() {
  signal(SIGABRT, signalHandler);
  signal(SIGILL, signalHandler);
  signal(SIGSEGV, signalHandler);
  signal(SIGFPE, signalHandler);
  signal(SIGBUS, signalHandler);
  signal(SIGPIPE, signalHandler);
}

@interface TestRunnerAppDelegate : UIResponder <UIApplicationDelegate>
@end

@implementation TestRunnerAppDelegate

- (void)applicationDidFinishLaunching:(UIApplication *)application {
  [self performSelector:@selector(runTestsAndExit) withObject:nil afterDelay:0];
}

- (void)runTestsAndExit {
  jint exitStatus = [ComGoogleJ2objcTestingJUnitTestRunner mainWithNSStringArray:nil];
  exit(exitStatus);
}

@end

int main(int argc, char *argv[]) {
  installSignalHandler();
  UIApplicationMain(argc, argv, nil, @"TestRunnerAppDelegate");
}
