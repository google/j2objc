//
//  JUnitRunner.m
//  JreEmulation
//
//  Created by Tom Ball on 11/10/11.
//

#import "JreEmulation.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "java/io/PrintStream.h"
#include "java/lang/ClassNotFoundException.h"
#include "java/lang/IllegalAccessException.h"
#include "java/lang/NoSuchMethodException.h"
#include "java/lang/System.h"
#include "java/lang/reflect/InvocationTargetException.h"
#include "java/lang/reflect/Method.h"
#include "junit/framework/TestFailure.h"
#include "junit/framework/TestResult.h"
#include "junit/framework/TestSuite.h"
#include "org/junit/runner/JUnitCore.h"

#include <execinfo.h>


static void signalHandler(int sig) {
  // Get void*'s for all entries on the stack.
  void *array[64];
  size_t frame_count = backtrace(array, 64);

  // Print all the frames to stderr.
  fprintf(stderr, "Error: signal %d:\n", sig);
  backtrace_symbols_fd(array, frame_count, 2);
  exit(1);
}

void installSignalHandler() {
  signal(SIGABRT, signalHandler);
  signal(SIGILL, signalHandler);
  signal(SIGSEGV, signalHandler);
  signal(SIGFPE, signalHandler);
  signal(SIGBUS, signalHandler);
  signal(SIGPIPE, signalHandler);
}

// Variant of J2ObjCMain main function, hard-coded to invoke JUnit's
// junit.textui.TestRunner.
int main( int argc, const char *argv[] ) {
  JrePrintNilChkCountAtExit();
  int exitCode = 0;
  installSignalHandler();
  @autoreleasepool {
    @try {
      IOSClass *clazz = [OrgJunitRunnerJUnitCore getClass];
      IOSClass *stringArrayClass =
          [IOSObjectArray iosClassWithType:[NSString getClass]];
      IOSObjectArray *paramTypes =
          [IOSObjectArray arrayWithObjects:(id[]) { stringArrayClass }
                                     count:1
                                      type:[IOSClass getClass]];
      JavaLangReflectMethod *mainMethod =
          [clazz getDeclaredMethod:@"main" parameterTypes:paramTypes];
      IOSObjectArray *mainArgs = JreEmulationMainArguments(argc, argv);
      IOSObjectArray *params =
          [IOSObjectArray arrayWithObjects:(id[]) { mainArgs }
                                     count:1
                                      type:[NSObject getClass]];
      (void) [mainMethod invokeWithId:nil withNSObjectArray:params];
    }
    @catch (JavaLangClassNotFoundException *e) {
      fprintf(stderr,
          "Error: could not find or load junit.textui.TestRunner\n");
      exitCode = 1;
    }
    @catch (JavaLangNoSuchMethodException *e) {
      fprintf(stderr,
          "Error: main method not found in class junit.textui.TestRunner\n");
      exitCode = 1;
    }
    @catch (JavaLangReflectInvocationTargetException *e) {
      [[JavaLangSystem err] printlnWithId:e];
      exitCode = 1;
    }
    @catch (JavaLangIllegalAccessException *e) {
      [[JavaLangSystem err] printlnWithId:e];
      exitCode = 1;
    }
  }
  return exitCode;
}
