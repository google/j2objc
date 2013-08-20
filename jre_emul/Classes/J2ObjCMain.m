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
#include "java/lang/reflect/InvocationTargetException.h"
#include "java/lang/reflect/Method.h"

int main( int argc, const char *argv[] ) {
  if (argc < 2) {
    printf("Usage: %s class [args...]\n", *argv);
    return 1;
  }
  JrePrintNilChkCountAtExit();
  int exitCode = 0;
  @autoreleasepool {
  const char *className = argv[1];
    @try {
      IOSClass *clazz =
          [IOSClass forName:[NSString stringWithUTF8String:className]];
      IOSClass *stringArrayClass =
          [IOSObjectArray iosClassWithType:[NSString getClass]];
      IOSObjectArray *paramTypes =
          [IOSObjectArray arrayWithObjects:(id[]) { stringArrayClass }
                                     count:1
                                      type:[IOSClass getClass]];
      JavaLangReflectMethod *mainMethod =
          [clazz getDeclaredMethod:@"main" parameterTypes:paramTypes];
      IOSObjectArray *mainArgs = JreEmulationMainArguments(argc - 1, &argv[1]);
      IOSObjectArray *params =
          [IOSObjectArray arrayWithObjects:(id[]) { mainArgs }
                                     count:1
                                      type:[NSObject getClass]];
      (void) [mainMethod invokeWithId:nil withNSObjectArray:params];
    }
    @catch (JavaLangClassNotFoundException *e) {
      fprintf(stderr, "Error: could not find or load main class %s\n",
              className);
      exitCode = 1;
    }
    @catch (JavaLangNoSuchMethodException *e) {
      fprintf(stderr, "Error: main method not found in class %s\n", className);
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
