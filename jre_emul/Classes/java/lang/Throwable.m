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

#import "java/io/PrintWriter.h"
#import "java/lang/Throwable.h"
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/IllegalArgumentException.h"

#import <TargetConditionals.h>
#ifndef TARGET_OS_IPHONE
#import <NSExceptionHandler.h>
#endif

#import "NSException+StackTrace.h"

@implementation JavaLangThrowable

// These init message implementations are hand-modified to
// invoke NSException.initWithName:reason:userInfo:.  This
// is necessary so that JRE exceptions can be caught by 
// class name.
- (id)init {
  return (self = [super initWithName:[[self class] description]
                              reason:detailMessage
                            userInfo:nil]);
}

- (id)initWithNSString:(NSString *)message {
  if ((self = [super initWithName:[[self class] description]
                           reason:message
                         userInfo:nil])) {
#if __has_feature(objc_arc)
    detailMessage = message;
#else
    detailMessage = [message retain];
#endif
  }
  return self;
}

- (id)initWithNSString:(NSString *)message
 withJavaLangThrowable:(JavaLangThrowable *)causeArg {
  if ((self = [super initWithName:[[self class] description]
                           reason:message
                         userInfo:nil])) {
#if __has_feature(objc_arc)
      self->cause = causeArg;
      detailMessage = message;
#else
    self->cause = [causeArg retain];
    detailMessage = [message retain];
#endif
  }
  return self;
}

- (id)initWithJavaLangThrowable:(JavaLangThrowable *)causeArg {
  if ((self = [super initWithName:[[self class] description]
                           reason:[NSString stringWithFormat:@"cause: %@",
                                   [causeArg description]]
                         userInfo:nil])) {
    detailMessage = (causeArg == nil) ? nil : [causeArg description];
#if __has_feature(objc_arc)
    self->cause = causeArg;
#else
    self->cause = [causeArg retain];
    [detailMessage retain];
#endif
  }
  return self;
}

// The following message implementations are unmodified translator output.
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

- (NSMutableArray *)getStackTrace {
  id exception = [[JavaLangAssertionError alloc] initWithId:@"not implemented"];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
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
  [super printStackTrace];
}

- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)writer {
  // Not implemented until there is similar support in NSException+StackTrace.
}

- (void)setStackTraceWithJavaLangStackTraceElementArray:(IOSObjectArray *)stackTrace {
  id exception = [[JavaLangAssertionError alloc] initWithId:@"not implemented"];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
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
  [cause release];
  [detailMessage release];
  [super dealloc];
}
#endif

@end
