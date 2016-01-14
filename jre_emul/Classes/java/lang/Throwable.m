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
#import "java/util/ArrayList.h"
#import "libcore/util/EmptyArray.h"

#import <TargetConditionals.h>
#import <execinfo.h>

#ifndef MAX_STACK_FRAMES
// This defines the upper limit of the stack frames for any exception.
#define MAX_STACK_FRAMES 128
#endif

#define JavaLangThrowable_serialVersionUID -3042686055658047285LL

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

// Instance variables. These are defined using Objective C associative references,
// so that java.lang.Throwable can be mapped directly to NSException using a category.

// JavaLangThrowable *cause_;
static char const * const CauseTagKey = "CauseTag";
static JavaLangThrowable *GetCause(id self) {
  return (JavaLangThrowable *)objc_getAssociatedObject(self, CauseTagKey);
}
static void SetCause(id self, JavaLangThrowable *cause) {
  if (self != cause) {  // May happen during deserialization.
    objc_setAssociatedObject(self, CauseTagKey, cause, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
  }
}

// IOSObjectArray *stackTrace_;
static char const * const StackTraceTagKey = "StackTraceTag";
static IOSObjectArray *GetStackTrace(id self) {
  return (IOSObjectArray *)objc_getAssociatedObject(self, StackTraceTagKey);
}
static void SetStackTrace(id self, IOSObjectArray *stackTrace) {
  objc_setAssociatedObject(self, StackTraceTagKey, stackTrace, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

// id<JavaUtilList> suppressedExceptions_;
static char const * const SuppressedExceptionsTagKey = "SuppressedExceptionsTag";
static id<JavaUtilList> GetSuppressedExceptions(id self) {
  return (id<JavaUtilList>)objc_getAssociatedObject(self, SuppressedExceptionsTagKey);
}
static void SetSuppressedExceptions(id self, id<JavaUtilList>list) {
  objc_setAssociatedObject(self, SuppressedExceptionsTagKey, list,
      OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

// RawStack *rawStack;
static char const * const RawStackTagKey = "RawStackTag";
static RawStack *GetRawStack(id self) {
  return (RawStack *)objc_getAssociatedObject(self, RawStackTagKey);
}
static void SetRawStack(id self) {
  RawStack *rawStack = [[RawStack alloc] init];
  objc_setAssociatedObject(self, RawStackTagKey, rawStack, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
  [rawStack release];
}
static void FreeRawStack(id self) {
  // Setting the associated object to null releases the previous value.
  objc_setAssociatedObject(self, RawStackTagKey, nil, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

// NSString *detailMessage_; This is a special case, since by default Throwable's detailMessage
// is mapped to NSException's reason property. Since reason is read-only, though, an associated
// object is necessary to support deserialized exceptions, since serialization always creates
// objects with the class's default constructor. Normal exceptions (those not created by
// deserialization) won't set this associated object.
static char const * const DetailMessageTagKey = "DetailMessageTag";
static NSString *GetDetailMessage(id self) {
  return (NSString *)objc_getAssociatedObject(self, DetailMessageTagKey);
}
static void SetDetailMessage(id self, NSString *message) {
  objc_setAssociatedObject(self, DetailMessageTagKey, message, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

@implementation JavaLangThrowable

J2OBJC_IGNORE_DESIGNATED_BEGIN
- (instancetype)init {
  JavaLangThrowable_init(self);
  return self;
}
J2OBJC_IGNORE_DESIGNATED_END

- (instancetype)initWithNSString:(NSString *)detailMessage {
  JavaLangThrowable_initWithNSString_(self, detailMessage);
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage
           withJavaLangThrowable:(JavaLangThrowable *)cause {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_(self, detailMessage, cause);
  return self;
}

- (instancetype)initWithJavaLangThrowable:(JavaLangThrowable *)cause {
  JavaLangThrowable_initWithJavaLangThrowable_(self, cause);
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage
 withJavaLangThrowable:(JavaLangThrowable *)cause
           withBoolean:(jboolean)enableSuppression
           withBoolean:(jboolean)writeableStackTrace {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, detailMessage, cause, enableSuppression, writeableStackTrace);
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
      self, message, causeArg, true, true);
}

JavaLangThrowable *new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    NSString *message, JavaLangThrowable *causeArg) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, true, true);
  return self;
}

void JavaLangThrowable_initWithJavaLangThrowable_(
    JavaLangThrowable *self, JavaLangThrowable *causeArg) {
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, causeArg ? [causeArg description] : nil, causeArg, true, true);
}

JavaLangThrowable *new_JavaLangThrowable_initWithJavaLangThrowable_(JavaLangThrowable *causeArg) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithJavaLangThrowable_(self, causeArg);
  return self;
}

void JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    JavaLangThrowable *self, NSString *message, JavaLangThrowable *causeArg,
    jboolean enableSuppression, jboolean writeableStackTrace) {
  [self initWithName:[[self class] description] reason:message userInfo:nil];
  if (causeArg) {
    SetCause(self, causeArg);
  }
  if (enableSuppression) {
    JavaUtilArrayList *newArray = new_JavaUtilArrayList_init();
    SetSuppressedExceptions(self, newArray);
  }
  if (writeableStackTrace) {
    SetRawStack(self);
  }
}

JavaLangThrowable *
    new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    NSString *message, JavaLangThrowable *causeArg, jboolean enableSuppression,
    jboolean writeableStackTrace) {
  JavaLangThrowable *self = [JavaLangThrowable alloc];
  JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
      self, message, causeArg, enableSuppression, writeableStackTrace);
  return self;
}

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

- (IOSObjectArray *)filterStackTrace {
  RawStack *rawStack = GetRawStack(self);
  if (rawStack) {
    @synchronized (self) {
      rawStack = GetRawStack(self);
      if (rawStack) {
        NSMutableArray *frames = [NSMutableArray array];
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
        SetStackTrace(self, [IOSObjectArray arrayWithNSArray:frames
                                                        type:JavaLangStackTraceElement_class_()]);
        FreeRawStack(self);
      }
    }
  }
  return GetStackTrace(self);
}

- (JavaLangThrowable *)fillInStackTrace {
  @synchronized (self) {
    SetRawStack(self);
  }
  return self;
}

- (JavaLangThrowable *)getCause {
  JavaLangThrowable *cause = GetCause(self);
  if (cause == self) {
    return nil;
  }
  return cause;
}

- (NSString *)getLocalizedMessage {
  return [self getMessage];
}

- (NSString *)getMessage {
  // Return an associated message if it exists, otherwise NSException's read-only reason.
  NSString *associatedMessage = GetDetailMessage(self);
  return associatedMessage ? associatedMessage : self.reason;
}

- (IOSObjectArray *)getStackTrace {
  return [self filterStackTrace];
}

- (JavaLangThrowable *)initCauseWithJavaLangThrowable:
    (JavaLangThrowable *)causeArg {
  JavaLangThrowable *cause = GetCause(self);
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

- (void)printStackTrace {
  [self printStackTraceWithJavaIoPrintStream:JavaLangSystem_get_err()];
}

- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)pw {
  [pw printlnWithNSString:[self description]];
  IOSObjectArray *trace = [self filterStackTrace];
  for (jint i = 0; i < trace->size_; i++) {
    [pw printWithNSString:@"\tat "];
    id frame = trace->buffer_[i];
    [pw printlnWithId:frame];
  }
  JavaLangThrowable *cause = GetCause(self);
  if (cause) {
    [pw printWithNSString:@"Caused by: "];
    [cause printStackTraceWithJavaIoPrintWriter:pw];
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
  JavaLangThrowable *cause = GetCause(self);
  if (cause) {
    [ps printWithNSString:@"Caused by: "];
    [cause printStackTraceWithJavaIoPrintStream:ps];
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
    SetStackTrace(self, stackTraceArg);
    FreeRawStack(self);
  }
}

- (void)addSuppressedWithJavaLangThrowable:(JavaLangThrowable *)exception {
  // Always check arg whether or not stack trace suppression is enabled.
  nil_chk(exception);
  if (exception == self) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  id<JavaUtilList> suppressedExceptions = GetSuppressedExceptions(self);
  if (suppressedExceptions) {
    @synchronized (self) {
      [suppressedExceptions addWithId:exception];
    }
  }
}

- (IOSObjectArray *)getSuppressed {
  id<JavaUtilList> suppressedExceptions = GetSuppressedExceptions(self);
  if (suppressedExceptions && ![suppressedExceptions isEmpty]) {
    return [suppressedExceptions
            toArrayWithNSObjectArray:[IOSObjectArray arrayWithLength:[suppressedExceptions size]
                                                                type:JavaLangThrowable_class_()]];
  } else {
    return JreLoadStatic(LibcoreUtilEmptyArray, THROWABLE);
  }
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

// Accessor methods for virtual fields, used by reflection.
- (JavaLangThrowable *)__cause {
  return GetCause(self);
}
- (void)__setcause:(JavaLangThrowable *)cause {
  SetCause(self, cause);
}
- (NSString *)__detailMessage {
  return [self getMessage];
}
- (void)__setdetailMessage:(NSString *)message {
  SetDetailMessage(self, message);
}
- (IOSObjectArray *)__stackTrace {
  return GetStackTrace(self);
}
- (void)__setstackTrace:(IOSObjectArray *)trace {
  SetStackTrace(self, trace);
}
- (id<JavaUtilList>)__suppressedExceptions {
  return GetSuppressedExceptions(self);
}
- (void)__setsuppressedExceptions:(id<JavaUtilList>)list {
  SetSuppressedExceptions(self, list);
}

// Generated by running the translator over the java.lang.Throwable stub file.
+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "init", "Throwable", NULL, 0x1, NULL, NULL },
    { "initWithNSString:", "Throwable", NULL, 0x1, NULL, NULL },
    { "initWithNSString:withJavaLangThrowable:", "Throwable", NULL, 0x1, NULL, NULL },
    { "initWithJavaLangThrowable:", "Throwable", NULL, 0x1, NULL, NULL },
    { "initWithNSString:withJavaLangThrowable:withBoolean:withBoolean:", "Throwable", NULL, 0x4,
        NULL, NULL },
    { "fillInStackTrace", NULL, "Ljava.lang.Throwable;", 0x1, NULL, NULL },
    { "getCause", NULL, "Ljava.lang.Throwable;", 0x1, NULL, NULL },
    { "getLocalizedMessage", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getMessage", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getStackTrace", NULL, "[Ljava.lang.StackTraceElement;", 0x1, NULL, NULL },
    { "initCauseWithJavaLangThrowable:", "initCause", "Ljava.lang.Throwable;", 0x1, NULL, NULL },
    { "printStackTrace", NULL, "V", 0x1, NULL, NULL },
    { "printStackTraceWithJavaIoPrintWriter:", "printStackTrace", "V", 0x1, NULL, NULL },
    { "printStackTraceWithJavaIoPrintStream:", "printStackTrace", "V", 0x1, NULL, NULL },
    { "setStackTraceWithJavaLangStackTraceElementArray:", "setStackTrace", "V", 0x1, NULL, NULL },
    { "addSuppressedWithJavaLangThrowable:", "addSuppressed", "V", 0x11, NULL, NULL },
    { "getSuppressed", NULL, "[Ljava.lang.Throwable;", 0x11, NULL, NULL },
    { "description", "toString", "Ljava.lang.String;", 0x1, NULL, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "detailMessage_", NULL, 0x2, "Ljava.lang.String;", NULL, NULL, .constantValue.asLong = 0 },
    { "cause_", NULL, 0x2, "Ljava.lang.Throwable;", NULL, NULL, .constantValue.asLong = 0 },
    { "stackTrace_", NULL, 0x2, "[Ljava.lang.StackTraceElement;", NULL, NULL,
        .constantValue.asLong = 0 },
    { "suppressedExceptions_", NULL, 0x2, "Ljava.util.List;", NULL,
        "Ljava/util/List<Ljava/lang/Throwable;>;", .constantValue.asLong = 0 },
    { "serialVersionUID", "serialVersionUID", 0x1a, "J", NULL, NULL,
        .constantValue.asLong = JavaLangThrowable_serialVersionUID },
  };
  static const J2ObjcClassInfo _JavaLangThrowable = { 2, "Throwable", "java.lang", NULL, 0x1,
      18, methods, 5, fields, 0, NULL, 0, NULL, NULL, NULL };
  return &_JavaLangThrowable;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangThrowable)
