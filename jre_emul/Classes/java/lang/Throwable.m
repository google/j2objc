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
//  Throwable.m
//  JreEmulation
//
//  Created by Tom Ball on 6/21/11, using j2objc.
//

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "java/io/PrintStream.h"
#import "java/io/PrintWriter.h"
#import "java/lang/Throwable.h"
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/StackTraceElement.h"
#import "java/lang/System.h"

#import <TargetConditionals.h>
#ifndef TARGET_OS_IPHONE
#import <NSExceptionHandler.h>
#endif

@implementation JavaLangThrowable

// This init message implementation is hand-modified to
// invoke NSException.initWithName:reason:userInfo:.  This
// is necessary so that JRE exceptions can be caught by
// class name.
- (id)initJavaLangThrowableWithNSString:(NSString *)message
                  withJavaLangThrowable:(JavaLangThrowable *)causeArg {
  if ((self = [super initWithName:[[self class] description]
                           reason:message
                         userInfo:nil])) {
    JreMemDebugAdd(self);
#if __has_feature(objc_arc)
    cause = causeArg;
    detailMessage = message;
    stackTrace = [JavaLangThrowable stackTraceWithSymbols:[NSThread callStackSymbols]];
#else
    cause = [causeArg retain];
    detailMessage = [message retain];
    stackTrace =
        [[JavaLangThrowable stackTraceWithSymbols:[NSThread callStackSymbols]] retain];
#endif
  }
  return self;
}

- (id)init {
  return [self initJavaLangThrowableWithNSString:nil withJavaLangThrowable:nil];
}

- (id)initWithNSString:(NSString *)message {
  return [self initJavaLangThrowableWithNSString:message withJavaLangThrowable:nil];
}

- (id)initWithNSString:(NSString *)message
    withJavaLangThrowable:(JavaLangThrowable *)causeArg {
  return [self initJavaLangThrowableWithNSString:message withJavaLangThrowable:causeArg];
}

- (id)initWithJavaLangThrowable:(JavaLangThrowable *)causeArg {
  return [self initJavaLangThrowableWithNSString:causeArg ? [causeArg description] : nil
                           withJavaLangThrowable:causeArg];
}

+ (IOSObjectArray *)stackTraceWithSymbols:(NSArray *)symbols {
  IOSObjectArray *stackTrace = [IOSObjectArray arrayWithLength:[symbols count] type:
      [IOSClass classWithClass:[JavaLangStackTraceElement class]]];
  for (int i = 0; i < [symbols count]; i++) {
    NSString *symbol = [symbols objectAtIndex:i];
    JavaLangStackTraceElement *element =
        [[[JavaLangStackTraceElement alloc] initWithNSString:nil
                                                withNSString:symbol
                                                withNSString:nil
                                                     withInt:-1] autorelease];
    [stackTrace replaceObjectAtIndex:i withObject:element];
  }
  return stackTrace;
}

- (JavaLangThrowable *)fillInStackTrace {
  return self;
}

- (JavaLangThrowable *)getCause {
  return cause;
}

- (NSString *)getLocalizedMessage {
  return [self getMessage];
}

- (NSString *)getMessage {
  return detailMessage;
}

- (IOSObjectArray *)getStackTrace {
  return stackTrace;
}

- (JavaLangThrowable *)initCauseWithJavaLangThrowable:
    (JavaLangThrowable *)causeArg {
  if (self->cause != nil) {
    id exception = [[JavaLangIllegalStateException alloc]
                    initWithNSString:@"Can't overwrite cause"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  if (causeArg == self) {
    id exception = [[JavaLangIllegalArgumentException alloc]
                    initWithNSString:@"Self-causation not permitted"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  self->cause = causeArg;
  return self;
}

- (void)printStackTrace {
  [self printStackTraceWithJavaIoPrintStream:[JavaLangSystem err]];
}

- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)pw {
  NSUInteger nFrames = [stackTrace count];
  for (NSUInteger i = 0; i < nFrames; i++) {
    id trace = [stackTrace objectAtIndex:i];
    [pw printlnWithId:trace];
  }
}

- (void)printStackTraceWithJavaIoPrintStream:(JavaIoPrintStream *)ps {
  NSUInteger nFrames = [stackTrace count];
  for (NSUInteger i = 0; i < nFrames; i++) {
    id trace = [stackTrace objectAtIndex:i];
    [ps printlnWithId:trace];
  }
}

- (void)setStackTraceWithJavaLangStackTraceElementArray:(IOSObjectArray *)stackTraceArg {
#if __has_feature(objc_arc)
  stackTrace = stackTraceArg;
#else
  [stackTrace autorelease];
  stackTrace = [stackTraceArg retain];
#endif
}

- (NSString *)description {
  NSString *className = [[self class] description];
  NSString *msg = [self getMessage];
  if (msg != nil) {
    return [NSString stringWithFormat:@"%@: %@", className, msg];
  }
  else {
    return className;
  }
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  JreMemDebugRemove(self);
  [cause release];
  [detailMessage release];
  [super dealloc];
}
#endif

@end
