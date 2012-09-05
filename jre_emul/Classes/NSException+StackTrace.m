// Copyright 2011 Google Inc. All Rights Reserved.
//
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
//  NSException+StackTrace.m
//  JreEmulation
//
//  Created by Tom Ball on 11/5/11.
//

#import "NSException+StackTrace.h"

#import <TargetConditionals.h>
#ifndef TARGET_OS_IPHONE
#import <NSExceptionHandler.h>
#endif // TARGET_OS_IPHONE

@implementation NSException (StackTrace)

// TODO:(tball) add support for printStackTrace(PrintStream),
// using NSTask/NSPipe to set stdio stream.

- (void)printStackTrace {

#ifdef TARGET_OS_IPHONE
  [self printCallStackSymbols];
#else
  NSString *atosPath = @"/usr/bin/atos";
  if ([[NSFileManager defaultManager] fileExistsAtPath:atosPath]) {
    NSString *stack = [[self userInfo] objectForKey:NSStackTraceKey];
    if (stack != nil && ![stack isEqualToString:@"(null)"]) {
      NSString *pid = [[NSNumber numberWithInt:getpid()] stringValue];
      NSMutableArray *args = [NSMutableArray arrayWithCapacity:20];

      [args addObject:@"-p"];
      [args addObject:pid];

      // Function addresses are separated by double spaces.
      [args addObjectsFromArray:[stack componentsSeparatedByString:@"  "]];

      NSTask *atos = [NSTask launchedTaskWithLaunchPath:atosPath 
                                              arguments:args];
      [atos waitUntilExit];
    }
  } else {
    [self printCallStackSymbols];
  }
#endif // TARGET_OS_IPHONE
}

- (void)printCallStackSymbols {
  if([self respondsToSelector: @selector(callStackSymbols)]) {
    NSArray *callStackSymbols = [self callStackSymbols];
    NSString *symbol;
    int count, i;
    count = [callStackSymbols count];
    for (i=0; i<count; i++) {
      symbol = [callStackSymbols objectAtIndex:i];
      fputs([[NSString stringWithFormat:@"%@\n", symbol] UTF8String], stdout);
    }
  }
}

+ (void)initialize {
#ifndef TARGET_OS_IPHONE
  [[NSExceptionHandler defaultExceptionHandler] setExceptionHandlingMask:
      NSHandleUncaughtExceptionMask | NSHandleUncaughtSystemExceptionMask |
      NSHandleUncaughtRuntimeErrorMask | NSHandleOtherExceptionMask |
      NSHandleTopLevelExceptionMask];
#endif // TARGET_OS_IPHONE
}

@end
