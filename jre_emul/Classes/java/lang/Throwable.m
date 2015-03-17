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
#import "J2ObjC_source.h"
#import "java/io/PrintStream.h"
#import "java/io/PrintWriter.h"
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/StackTraceElement.h"
#import "java/lang/System.h"
#import "java/lang/Throwable.h"

#import <TargetConditionals.h>
#import <execinfo.h>

#ifndef MAX_STACK_FRAMES
// This defines the upper limit of the stack frames for any exception.
#define MAX_STACK_FRAMES 128
#endif

#define JavaLangThrowable_serialVersionUID -3042686055658047285LL

@implementation JavaLangThrowable

void FillInStackTraceInternal(JavaLangThrowable *self) {
  void *callStack[MAX_STACK_FRAMES];
  self->rawFrameCount = backtrace(callStack, MAX_STACK_FRAMES);
  unsigned nBytes = self->rawFrameCount * sizeof(void *);
  self->rawCallStack = (void **)malloc(nBytes);
  memcpy(self->rawCallStack, callStack, nBytes);
}

- (instancetype)init {
  JavaLangThrowable_init(self);
  return self;
}

- (instancetype)initWithNSString:(NSString *)message {
  JavaLangThrowable_initWithNSString_(self, message);
  return self;
}

- (instancetype)initWithNSString:(NSString *)message
    withJavaLangThrowable:(JavaLangThrowable *)causeArg {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_(self, message, causeArg);
  return self;
}

- (instancetype)initWithJavaLangThrowable:(JavaLangThrowable *)causeArg {
  JavaLangThrowable_initWithJavaLangThrowable_(self, causeArg);
  return self;
}

- (instancetype)initWithNSString:(NSString *)message
 withJavaLangThrowable:(JavaLangThrowable *)causeArg
           withBoolean:(BOOL)enableSuppression
           withBoolean:(BOOL)writeableStackTrace {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, enableSuppression, writeableStackTrace);
  return self;
}

void JavaLangThrowable_init(JavaLangThrowable *self) {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_(self, nil, nil);
}

JavaLangThrowable *new_JavaLangThrowable_init() {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_init(self);
  return self;
}

void JavaLangThrowable_initWithNSString_(JavaLangThrowable *self, NSString *message) {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_(self, message, nil);
}

JavaLangThrowable *new_JavaLangThrowable_initWithNSString_(NSString *message) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithNSString_(self, message);
  return self;
}

// This init message implementation is hand-modified to
// invoke NSException.initWithName:reason:userInfo:.  This
// is necessary so that JRE exceptions can be caught by
// class name.
void JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    JavaLangThrowable *self, NSString *message, JavaLangThrowable *causeArg) {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, YES, YES);
}

JavaLangThrowable *new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    NSString *message, JavaLangThrowable *causeArg) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, YES, YES);
  return self;
}

void JavaLangThrowable_initWithJavaLangThrowable_(
    JavaLangThrowable *self, JavaLangThrowable *causeArg) {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, causeArg ? [causeArg description] : nil, causeArg, YES, YES);
}

JavaLangThrowable *new_JavaLangThrowable_initWithJavaLangThrowable_(JavaLangThrowable *causeArg) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithJavaLangThrowable_(self, causeArg);
  return self;
}

void JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    JavaLangThrowable *self, NSString *message, JavaLangThrowable *causeArg, BOOL enableSuppression,
    BOOL writeableStackTrace) {
  [self initWithName:[[self class] description] reason:message userInfo:nil];
  self->cause = RETAIN_(causeArg);
  self->detailMessage = RETAIN_(message);
  if (enableSuppression) {
    self->suppressedExceptions =
        [IOSObjectArray newArrayWithLength:0
                                      type:JavaLangThrowable_class_()];
  } else {
    // nil indicates that exceptions are suppressed for this throwable.
    self->suppressedExceptions = nil;
  }
  if (writeableStackTrace) {
    FillInStackTraceInternal(self);
  }
}

JavaLangThrowable *
    new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    NSString *message, JavaLangThrowable *causeArg, BOOL enableSuppression,
    BOOL writeableStackTrace) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, enableSuppression, writeableStackTrace);
  return self;
}

// Filter out native functions (no class), NSInvocation methods, and internal constructor.
static BOOL ShouldFilterStackElement(JavaLangStackTraceElement *element) {
  NSString *className = [element getClassName];
  if (!className) {
    return YES;
  }
  if ([className isEqualToString:@"NSInvocation"]) {
    return YES;
  }
  if ([className isEqualToString:@"java.lang.Throwable"]
      && [[element getMethodName] isEqualToString:@"<init>"]) {
    return YES;
  }
  return NO;
}

- (IOSObjectArray *)filterStackTrace {
  if (rawCallStack) {
    @synchronized (self) {
      if (rawCallStack) {
        NSMutableArray *frames = [NSMutableArray array];
        for (unsigned i = 0; i < rawFrameCount; i++) {
          JavaLangStackTraceElement *element = AUTORELEASE(
              [[JavaLangStackTraceElement alloc] initWithLong:(long long int) rawCallStack[i]]);
          if (!ShouldFilterStackElement(element)) {
            [frames addObject:element];
          }
        }
        JavaLangStackTraceElement *element = [frames lastObject];
        // Remove initial Method.invoke(), so app's main method is last.
        if ([[element getClassName] isEqualToString:@"JavaLangReflectMethod"] &&
            [[element getMethodName] isEqualToString:@"invoke"]) {
          [frames removeLastObject];
        }
        stackTrace =  RETAIN_([IOSObjectArray arrayWithNSArray:frames
                                                          type:JavaLangStackTraceElement_class_()]);
        free(rawCallStack);
        rawCallStack = NULL;
      }
    }
  }
  return stackTrace;
}

- (JavaLangThrowable *)fillInStackTrace {
  @synchronized (self) {
    FillInStackTraceInternal(self);
  }
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
  return [self filterStackTrace];
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
  self->cause = RETAIN_(causeArg);
  return self;
}

- (void)printStackTrace {
  [self printStackTraceWithJavaIoPrintStream:JavaLangSystem_get_err_()];
}

- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)pw {
  [pw printlnWithNSString:[self description]];
  IOSObjectArray *trace = [self filterStackTrace];
  for (jint i = 0; i < trace->size_; i++) {
    [pw printWithNSString:@"\tat "];
    id frame = trace->buffer_[i];
    [pw printlnWithId:frame];
  }
  if (self->cause) {
    [pw printWithNSString:@"Caused by: "];
    [self->cause printStackTraceWithJavaIoPrintWriter:pw];
  }
}

- (void)printStackTraceWithJavaIoPrintStream:(JavaIoPrintStream *)ps {
  [ps printlnWithNSString:[self description]];
  IOSObjectArray *trace = [self filterStackTrace];
  for (jint i = 0; i < trace->size_; i++) {
    [ps printWithNSString:@"\tat "];
    id frame = trace->buffer_[i];
    [ps printlnWithId:frame];
  }
  if (self->cause) {
    [ps printWithNSString:@"Caused by: "];
    [self->cause printStackTraceWithJavaIoPrintStream:ps];
  }
}

- (void)setStackTraceWithJavaLangStackTraceElementArray:
    (IOSObjectArray *)stackTraceArg {
  // Always check args whether or not stack trace is writeable (not nil).
  nil_chk(stackTraceArg);
  jint count = stackTraceArg->size_;
  for (jint i = 0; i < count; i++) {
    nil_chk(stackTraceArg->buffer_[i]);
  }
  @synchronized (self) {
    if (self->stackTrace || self->rawCallStack) {
      [self maybeFreeRawCallStack];
      [self->stackTrace autorelease];
    }
    self->stackTrace = [stackTraceArg retain];
  }
}

- (void)addSuppressedWithJavaLangThrowable:(JavaLangThrowable *)exception {
  // Always check arg whether or not stack trace suppression is enabled.
  nil_chk(exception);
  if (exception == self) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  if (suppressedExceptions) {
    @synchronized (self) {
      jint existingCount = suppressedExceptions ? suppressedExceptions->size_ : 0;
      IOSObjectArray *newArray = [IOSObjectArray newArrayWithLength:existingCount + 1
          type:JavaLangThrowable_class_()];
      for (jint i = 0; i < existingCount; i++) {
        [newArray replaceObjectAtIndex:i withObject:suppressedExceptions->buffer_[i]];
      }
      [newArray replaceObjectAtIndex:existingCount
                          withObject:exception];
      RELEASE_(suppressedExceptions);
      suppressedExceptions = newArray;
    }
  }
}

- (IOSObjectArray *)getSuppressed {
  return suppressedExceptions
      ? [IOSObjectArray arrayWithArray:suppressedExceptions]
      : [IOSObjectArray arrayWithLength:0 type:JavaLangThrowable_class_()];
}

- (NSString *)description {
  NSString *className = [[self getClass] getName];
  NSString *msg = [self getMessage];
  if (msg) {
    return [NSString stringWithFormat:@"%@: %@", className, msg];
  } else {
    return className;
  }
}

- (void)maybeFreeRawCallStack {
  if (rawCallStack) {
    free(rawCallStack);
    rawCallStack = NULL;
  }
}

- (void)dealloc {
  [self maybeFreeRawCallStack];
  [cause release];
  [detailMessage release];
  [stackTrace release];
  [super dealloc];
}

// Generated by running the translator over the java.lang.Throwable stub file.
+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { "initWithNSString:withJavaLangThrowable:withBoolean:withBoolean:", NULL, NULL, 0x4, NULL },
    { "fillInStackTrace", NULL, "Ljava/lang/Throwable;", 0x1, NULL },
    { "getCause", NULL, "Ljava/lang/Throwable;", 0x1, NULL },
    { "getLocalizedMessage", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "getMessage", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "getStackTrace", NULL, "Ljava/lang/StackTraceElement;", 0x1, NULL },
    { "initCauseWithJavaLangThrowable:", NULL, "Ljava/lang/Throwable;", 0x1, NULL },
    { "addSuppressedWithJavaLangThrowable:", NULL, "V", 0x11, NULL },
    { "getSuppressed", NULL, "[Ljava/lang/Throwable;", 0x11, NULL },
  };
  static J2ObjcFieldInfo fields[] = {
    { "serialVersionUID_", NULL, 0x1a, "J", NULL,
      .constantValue.asLong = JavaLangThrowable_serialVersionUID },
  };
  static J2ObjcClassInfo _JavaLangThrowable = {
    1, "Throwable", "java.lang", NULL, 0x1, 9, methods, 1, fields, 0, NULL
  };
  return &_JavaLangThrowable;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangThrowable)
