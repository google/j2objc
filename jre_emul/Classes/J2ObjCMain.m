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
//  J2ObjCMain.m
//  JreEmulation
//
//  Main function that provides JVM-like start-up for OS X.
//  Usage: <executable> <class-with-Java-main-method> [args]
//

#import "JreEmulation.h"
#import "IOSClass.h"
#include "IOSObjectArray.h"
#include "java/io/PrintStream.h"
#include "java/lang/ClassNotFoundException.h"
#include "java/lang/IllegalAccessException.h"
#include "java/lang/NoSuchMethodException.h"
#include "java/lang/System.h"
#include "java/lang/Thread.h"
#include "java/lang/Throwable.h"
#include "java/lang/reflect/InvocationTargetException.h"
#include "java/lang/reflect/Method.h"

#include <execinfo.h>

static void signalHandler(int sig) {
  // Get void*'s for all entries on the stack.
  void *array[64];
  int frame_count = (int) backtrace(array, 64);

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

int main( int argc, const char *argv[] ) {
  if (argc < 2) {
    printf("Usage: %s class [args...]\n", *argv);
    return 1;
  }
  JrePrintNilChkCountAtExit();
  installSignalHandler();
  @autoreleasepool {
    const char *className = argv[1];
    IOSClass *clazz = nil;
    JavaLangReflectMethod *mainMethod = nil;

    // Find the main class.
    @try {
      clazz = [IOSClass forName:[NSString stringWithUTF8String:className]];
    }
    @catch (JavaLangClassNotFoundException *e) {
      fprintf(stderr, "Error: could not find or load main class %s\n",
              className);
      return 1;
    }

    // Find the main method.
    @try {
      IOSClass *stringArrayClass =
          [IOSObjectArray iosClassWithType:[NSString getClass]];
      IOSObjectArray *paramTypes =
          [IOSObjectArray arrayWithObjects:(id[]) { stringArrayClass }
                                     count:1
                                      type:[IOSClass getClass]];
      mainMethod = [clazz getDeclaredMethod:@"main" parameterTypes:paramTypes];
    }
    @catch (JavaLangNoSuchMethodException *e) {
      fprintf(stderr, "Error: main method not found in class %s\n", className);
      return 1;
    }

    // Execute the main method.
    @try {
      IOSObjectArray *mainArgs = JreEmulationMainArguments(argc - 1, &argv[1]);
      IOSObjectArray *params =
          [IOSObjectArray arrayWithObjects:(id[]) { mainArgs }
                                     count:1
                                      type:[NSObject getClass]];
      (void) [mainMethod invokeWithId:nil withNSObjectArray:params];
    }
    @catch (JavaLangReflectInvocationTargetException *e) {
      [JavaLangSystem_get_err_() printlnWithId:e];
      return 1;
    }
    @catch (JavaLangIllegalAccessException *e) {
      [JavaLangSystem_get_err_() printlnWithId:e];
      return 1;
    }
    @catch (JavaLangThrowable *e) {
      JavaLangThread *current = [JavaLangThread currentThread];
      id uncaughtHandler = [current getUncaughtExceptionHandler];
      JavaLangThrowable *cause = [e getCause];
      if (!cause) {
        cause = e;
      }
      [uncaughtHandler uncaughtExceptionWithJavaLangThread:current withJavaLangThrowable:cause];
    }
  }
  return 0;
}
