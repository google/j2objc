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
#import "NSException+JavaThrowable.h"
#import "java/io/ObjectInputStream.h"
#import "java/io/PrintStream.h"
#import "java/io/PrintWriter.h"
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/StackTraceElement.h"
#import "java/lang/System.h"
#import "java/util/ArrayList.h"
#import "libcore/util/EmptyArray.h"

#import <TargetConditionals.h>
#import <execinfo.h>

#ifndef MAX_STACK_FRAMES
// This defines the upper limit of the stack frames for any exception.
#define MAX_STACK_FRAMES 128
#endif

#define NSException_serialVersionUID -3042686055658047285LL

@interface RawStack : NSObject {
 @public
  void **frames_;
  unsigned count_;
}
@end

@implementation RawStack

- (instancetype)init {
  NSObject_init(self);
  void *callStack[MAX_STACK_FRAMES];
  self->count_ = backtrace(callStack, MAX_STACK_FRAMES) - 1; // Hide this frame.
  unsigned nBytes = self->count_ * sizeof(void *);
  self->frames_ = (void **)malloc(nBytes);
  memcpy(self->frames_, callStack + 1, nBytes);
  return self;
}

- (void)dealloc {
  free(frames_);
  [super dealloc];
}

@end

// UserInfo dictionary variables.

// NSException *cause_;
static NSString *CauseTagKey = @"CauseTag";
static NSException *GetCause(NSException *self) {
  return (NSException *)[self.userInfo objectForKey:CauseTagKey];
}
static void SetCause(NSException *self, NSException *cause) {
  if (self != cause) {  // May happen during deserialization.
    [(NSMutableDictionary *)self.userInfo setValue:cause forKey:CauseTagKey];
  }
}

// IOSObjectArray *stackTrace_;
static NSString *StackTraceTagKey = @"StackTraceTag";
static IOSObjectArray *GetStackTrace(NSException *self) {
  return (IOSObjectArray *)[self.userInfo objectForKey:StackTraceTagKey];
}
static void SetStackTrace(NSException *self, IOSObjectArray *stackTrace) {
  [(NSMutableDictionary *)self.userInfo setValue:stackTrace forKey:StackTraceTagKey];
}

// id<JavaUtilList> suppressedExceptions_;
static NSString *SuppressedExceptionsTagKey = @"SuppressedExceptionsTag";
static id<JavaUtilList> GetSuppressedExceptions(NSException *self) {
  return (id<JavaUtilList>)[self.userInfo objectForKey:SuppressedExceptionsTagKey];
}
static void SetSuppressedExceptions(NSException *self, id<JavaUtilList>list) {
  [(NSMutableDictionary *)self.userInfo setValue:list forKey:SuppressedExceptionsTagKey];
}

// RawStack *rawStack;
static NSString *RawStackTagKey = @"RawStackTag";
static RawStack *GetRawStack(NSException *self) {
  return (RawStack *)[self.userInfo objectForKey:RawStackTagKey];
}
static void SetRawStack(NSException *self) {
  RawStack *rawStack = [[RawStack alloc] init];
  [(NSMutableDictionary *)self.userInfo setValue:rawStack forKey:RawStackTagKey];
  [rawStack release];
}
static void FreeRawStack(NSException *self) {
  [(NSMutableDictionary *)self.userInfo removeObjectForKey:RawStackTagKey];
}

// NSString *detailMessage_;
static NSString *DetailMessageTagKey = @"DetailMessageTag";
static NSString *GetDetailMessage(NSException *self) {
  return (NSString *)[self.userInfo objectForKey:DetailMessageTagKey];
}
static void SetDetailMessage(NSException *self, NSString *message) {
  [(NSMutableDictionary *)self.userInfo setValue:message forKey:DetailMessageTagKey];
}

@implementation NSException (JavaLangThrowable)

J2OBJC_IGNORE_DESIGNATED_BEGIN
- (instancetype)init {
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage {
  NSException_initWithNSString_(self, detailMessage);
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage
           withNSException:(NSException *)cause {
  NSException_initWithNSString_withNSException_(self, detailMessage, cause);
  return self;
}

- (instancetype)initWithNSException:(NSException *)cause {
  NSException_initWithNSException_(self, cause);
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage
 withNSException:(NSException *)cause
           withBoolean:(jboolean)enableSuppression
           withBoolean:(jboolean)writeableStackTrace {
  NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
      self, detailMessage, cause, enableSuppression, writeableStackTrace);
  return self;
}
J2OBJC_IGNORE_DESIGNATED_END

// Filter out native functions (no class), NSInvocation methods, and internal constructor.
static jboolean ShouldFilterStackElement(JavaLangStackTraceElement *element) {
  NSString *className = [element getClassName];
  if (!className) {
    return true;
  }
  if ([className isEqualToString:@"NSInvocation"]) {
    return true;
  }
  if ([className isEqualToString:@"java.lang.Throwable"]
      && [[element getMethodName] isEqualToString:@"<init>"]) {
    return true;
  }
  return false;
}

IOSObjectArray *FilterStackTrace(RawStack *rawStack) {
  NSMutableArray *frames = [NSMutableArray array];
  if (rawStack) {
    for (unsigned i = 0; i < rawStack->count_; i++) {
      JavaLangStackTraceElement *element =
          [[JavaLangStackTraceElement alloc] initWithLong:(jlong)rawStack->frames_[i]];
      if (!ShouldFilterStackElement(element)) {
        [frames addObject:element];
      }
      [element release];
    }
    JavaLangStackTraceElement *element = [frames lastObject];
    // Remove initial Method.invoke(), so app's main method is last.
    if ([[element getClassName] isEqualToString:@"JavaLangReflectMethod"] &&
        [[element getMethodName] isEqualToString:@"invoke"]) {
      [frames removeLastObject];
    }
  }
  return [IOSObjectArray arrayWithNSArray:frames type:JavaLangStackTraceElement_class_()];
}

IOSObjectArray *InternalGetStackTrace(NSException *self) {
  @synchronized (self) {
    IOSObjectArray *stackTrace = GetStackTrace(self);
    IOSObjectArray *emptyTrace = JreLoadStatic(LibcoreUtilEmptyArray, STACK_TRACE_ELEMENT);
    if (stackTrace == emptyTrace) {
      stackTrace = FilterStackTrace(GetRawStack(self));
      SetStackTrace(self, stackTrace);
      FreeRawStack(self);
      return stackTrace;
    } else if (stackTrace) {
      return stackTrace;
    } else {
      return emptyTrace;
    }
  }
}

- (NSException *)fillInStackTrace {
  @synchronized (self) {
    IOSObjectArray *stackTrace = GetStackTrace(self);
    if (!stackTrace) {
      return self;  // writableStackTrace was false.
    }
    SetRawStack(self);
    SetStackTrace(self, JreLoadStatic(LibcoreUtilEmptyArray, STACK_TRACE_ELEMENT));
  }
  return self;
}

- (NSException *)getCause {
  @synchronized (self) {
    NSException *cause = GetCause(self);
    if (cause == self) {
      return nil;
    }
    return cause;
  }
}

- (NSString *)getLocalizedMessage {
  return [self getMessage];
}

- (NSString *)getMessage {
  @synchronized (self) {
    return GetDetailMessage(self);
  }
}

- (IOSObjectArray *)getStackTrace {
  return [InternalGetStackTrace(self) clone];
}

- (NSException *)initCauseWithNSException:(NSException *)causeArg {
  @synchronized (self) {
    NSException *cause = GetCause(self);
    if (cause) {
      @throw AUTORELEASE([[JavaLangIllegalStateException alloc]
                          initWithNSString:@"Can't overwrite cause"]);
    }
    if (causeArg == self) {
      @throw AUTORELEASE([[JavaLangIllegalStateException alloc]
                          initWithNSString:@"Self-causation not permitted"]);
    }
    SetCause(self, causeArg);
    return self;
  }
}

- (void)printStackTrace {
  [self printStackTraceWithJavaIoPrintStream:JavaLangSystem_get_err()];
}

- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)pw {
  [pw printlnWithNSString:[self description]];
  IOSObjectArray *trace = InternalGetStackTrace(self);
  for (jint i = 0; i < trace->size_; i++) {
    [pw printWithNSString:@"\tat "];
    id frame = trace->buffer_[i];
    [pw printlnWithId:frame];
  }
  NSException *cause = [self getCause];
  if (cause) {
    [pw printWithNSString:@"Caused by: "];
    [cause printStackTraceWithJavaIoPrintWriter:pw];
  }
}

- (void)printStackTraceWithJavaIoPrintStream:(JavaIoPrintStream *)ps {
  [ps printlnWithNSString:[self description]];
  IOSObjectArray *trace = InternalGetStackTrace(self);
  for (jint i = 0; i < trace->size_; i++) {
    [ps printWithNSString:@"\tat "];
    id frame = trace->buffer_[i];
    [ps printlnWithId:frame];
  }
  NSException *cause = [self getCause];
  if (cause) {
    [ps printWithNSString:@"Caused by: "];
    [cause printStackTraceWithJavaIoPrintStream:ps];
  }
}

- (void)setStackTraceWithJavaLangStackTraceElementArray:
    (IOSObjectArray *)stackTraceArg {
  @synchronized (self) {
    IOSObjectArray *stackTrace = GetStackTrace(self);
    if (!stackTrace) {
      return;  // writableStackTrace was false.
    }
    IOSObjectArray *newTrace = [stackTraceArg clone];
    // Always check args whether or not stack trace is writeable (not nil).
    jint count = newTrace->size_;
    for (jint i = 0; i < count; i++) {
      nil_chk(newTrace->buffer_[i]);
    }
    SetStackTrace(self, newTrace);
    FreeRawStack(self);
  }
}

- (void)addSuppressedWithNSException:(NSException *)exception {
  // Always check arg whether or not stack trace suppression is enabled.
  nil_chk(exception);
  if (exception == self) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  @synchronized (self) {
    id<JavaUtilList> suppressedExceptions = GetSuppressedExceptions(self);
    if (suppressedExceptions) {
      [suppressedExceptions addWithId:exception];
    }
  }
}

- (IOSObjectArray *)getSuppressed {
  @synchronized (self) {
    id<JavaUtilList> suppressedExceptions = GetSuppressedExceptions(self);
    if (suppressedExceptions && ![suppressedExceptions isEmpty]) {
      return [suppressedExceptions
          toArrayWithNSObjectArray:[IOSObjectArray arrayWithLength:[suppressedExceptions size]
                                                              type:NSException_class_()]];
    } else {
      return JreLoadStatic(LibcoreUtilEmptyArray, THROWABLE);
    }
  }
}

- (void)readObjectWithJavaIoObjectInputStream:(JavaIoObjectInputStream *)ois {
  NSException_initWithNSString_withNSException_(self, nil, nil);
  [ois defaultReadObject];
}

- (BOOL)isEqual:(id)object {
  // java.lang.Throwable doesn't define equals(), so use object equivalence.
  return self == object;
}

- (NSUInteger)hash {
  return (NSUInteger)self;
}

// Accessor methods for virtual fields, used by reflection.
- (NSException *)__cause {
  @synchronized (self) {
    return GetCause(self);
  }
}
- (void)__setcause:(NSException *)cause {
  @synchronized (self) {
    SetCause(self, cause);
  }
}
- (NSString *)__detailMessage {
  return [self getMessage];
}
- (void)__setdetailMessage:(NSString *)message {
  @synchronized (self) {
    SetDetailMessage(self, message);
  }
}
- (IOSObjectArray *)__stackTrace {
  @synchronized (self) {
    return GetStackTrace(self);
  }
}
- (void)__setstackTrace:(IOSObjectArray *)trace {
  @synchronized (self) {
    SetStackTrace(self, trace);
  }
}
- (id<JavaUtilList>)__suppressedExceptions {
  @synchronized (self) {
    return GetSuppressedExceptions(self);
  }
}
- (void)__setsuppressedExceptions:(id<JavaUtilList>)list {
  @synchronized (self) {
    SetSuppressedExceptions(self, list);
  }
}

// Generated by running the translator over the java.lang.Throwable stub file.
+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 1, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 2, -1, -1, -1, -1 },
    { NULL, NULL, 0x4, -1, 3, -1, -1, -1, -1 },
    { NULL, "LNSException;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSException;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangStackTraceElement;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSException;", 0x1, 4, 2, -1, -1, -1, -1 },
    { NULL, "V", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 5, 6, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 5, 7, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 8, 9, -1, -1, -1, -1 },
    { NULL, "V", 0x11, 10, 2, -1, -1, -1, -1 },
    { NULL, "[LNSException;", 0x11, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 11, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x2, 12, 13, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(initWithNSString:);
  methods[2].selector = @selector(initWithNSString:withNSException:);
  methods[3].selector = @selector(initWithNSException:);
  methods[4].selector = @selector(initWithNSString:withNSException:withBoolean:withBoolean:);
  methods[5].selector = @selector(fillInStackTrace);
  methods[6].selector = @selector(getCause);
  methods[7].selector = @selector(getLocalizedMessage);
  methods[8].selector = @selector(getMessage);
  methods[9].selector = @selector(getStackTrace);
  methods[10].selector = @selector(initCauseWithNSException:);
  methods[11].selector = @selector(printStackTrace);
  methods[12].selector = @selector(printStackTraceWithJavaIoPrintWriter:);
  methods[13].selector = @selector(printStackTraceWithJavaIoPrintStream:);
  methods[14].selector = @selector(setStackTraceWithJavaLangStackTraceElementArray:);
  methods[15].selector = @selector(addSuppressedWithNSException:);
  methods[16].selector = @selector(getSuppressed);
  methods[17].selector = @selector(description);
  methods[18].selector = @selector(readObjectWithJavaIoObjectInputStream:);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "detailMessage_", "LNSString;", .constantValue.asLong = 0, 0x2, -1, -1, -1, -1 },
    { "cause_", "LNSException;", .constantValue.asLong = 0, 0x2, -1, -1, -1, -1 },
    { "stackTrace_", "[LJavaLangStackTraceElement;", .constantValue.asLong = 0, 0x2, -1, -1, -1, -1
    },
    { "suppressedExceptions_", "LJavaUtilList;", .constantValue.asLong = 0, 0x2, -1, -1, 14, -1 },
    { "serialVersionUID", "J", .constantValue.asLong = NSException_serialVersionUID, 0x1a, -1, -1,
      -1, -1 },
  };
  static const void *ptrTable[] = {
    "LNSString;", "LNSString;LNSException;", "LNSException;", "LNSString;LNSException;ZZ",
    "initCause", "printStackTrace", "LJavaIoPrintWriter;", "LJavaIoPrintStream;", "setStackTrace",
    "[LJavaLangStackTraceElement;", "addSuppressed", "toString", "readObject",
    "LJavaIoObjectInputStream;", "Ljava/util/List<Ljava/lang/Throwable;>;" };
  static const J2ObjcClassInfo _NSException = {
    "Throwable", "java.lang", ptrTable, methods, fields, 7, 0x1, 19, 5, -1, -1, -1, -1, -1 };
  return &_NSException;
}

@end

void NSException_init(NSException *self) {
  NSException_initWithNSString_withNSException_(self, nil, nil);
}

NSException *new_NSException_init() {
  J2OBJC_NEW_IMPL(NSException, init)
}

NSException *create_NSException_init() {
  J2OBJC_CREATE_IMPL(NSException, init)
}

void NSException_initWithNSString_(NSException *self, NSString *message) {
  NSException_initWithNSString_withNSException_(self, message, nil);
}

NSException *new_NSException_initWithNSString_(NSString *message) {
  J2OBJC_NEW_IMPL(NSException, initWithNSString_, message)
}

NSException *create_NSException_initWithNSString_(NSString *message) {
  J2OBJC_CREATE_IMPL(NSException, initWithNSString_, message)
}

void NSException_initWithNSString_withNSException_(
    NSException *self, NSString *message, NSException *causeArg) {
  NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
      self, message, causeArg, true, true);
}

NSException *new_NSException_initWithNSString_withNSException_(
    NSString *message, NSException *causeArg) {
  J2OBJC_NEW_IMPL(
    NSException, initWithNSString_withNSException_withBoolean_withBoolean_, message, causeArg, true,
    true)
}

NSException *create_NSException_initWithNSString_withNSException_(
    NSString *message, NSException *causeArg) {
  J2OBJC_CREATE_IMPL(
    NSException, initWithNSString_withNSException_withBoolean_withBoolean_, message, causeArg, true,
    true)
}

void NSException_initWithNSException_(NSException *self, NSException *causeArg) {
  NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
      self, causeArg ? [causeArg description] : nil, causeArg, true, true);
}

NSException *new_NSException_initWithNSException_(NSException *causeArg) {
  J2OBJC_NEW_IMPL(NSException, initWithNSException_, causeArg)
}

NSException *create_NSException_initWithNSException_(NSException *causeArg) {
  J2OBJC_CREATE_IMPL(NSException, initWithNSException_, causeArg)
}

// This init message implementation is modified to invoke
// NSException.initWithName:reason:userInfo:. This is necessary so that JRE
// exceptions can be caught by class name.
void NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSException *self, NSString *message, NSException *causeArg, jboolean enableSuppression,
    jboolean writeableStackTrace) {
  NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];

  // The NSException reason string is based on java.lang.Throwable.toString():
  //   . if there is a message, then the reason is "class-name: message",
  //   . otherwise, it's "class-name".
  // Note: this only affects translator-generated exception instances.
  NSString *clsName = [[self getClass] getName];
  NSString *reason = message ? [NSString stringWithFormat:@"%@: %@", clsName, message] : clsName;

  [self initWithName:[[self class] description] reason:reason userInfo:userInfo];
  if (causeArg && self != causeArg) {
    [(NSMutableDictionary *)userInfo setValue:causeArg forKey:CauseTagKey];
  }
  [userInfo release];
  if (message) {
    SetDetailMessage(self, message);
  }
  if (enableSuppression) {
    JavaUtilArrayList *newArray = new_JavaUtilArrayList_init();
    SetSuppressedExceptions(self, newArray);
    [newArray release];
  }
  if (writeableStackTrace) {
    SetRawStack(self);
    SetStackTrace(self, JreLoadStatic(LibcoreUtilEmptyArray, STACK_TRACE_ELEMENT));
  }
}

NSException *new_NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSString *message, NSException *causeArg, jboolean enableSuppression,
    jboolean writeableStackTrace) {
  J2OBJC_NEW_IMPL(
      NSException, initWithNSString_withNSException_withBoolean_withBoolean_, message, causeArg,
      enableSuppression, writeableStackTrace)
}

NSException *create_NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSString *message, NSException *causeArg, jboolean enableSuppression,
    jboolean writeableStackTrace) {
  J2OBJC_CREATE_IMPL(
      NSException, initWithNSString_withNSException_withBoolean_withBoolean_, message, causeArg,
      enableSuppression, writeableStackTrace)
}

// Empty class to force category to be loaded.
@implementation JreThrowableCategoryDummy
@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSException)
