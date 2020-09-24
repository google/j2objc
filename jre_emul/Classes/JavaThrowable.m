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
//  JavaThrowable.m
//  JreEmulation
//
//  Provides the native implementations for java.lang.Throwable.
//

#import "JavaThrowable.h"

#import "IOSObjectArray.h"
#import "J2ObjC_source.h"
#import "java/lang/StackTraceElement.h"
#import "jni.h"

#import <execinfo.h>

#ifndef MAX_STACK_FRAMES
// This defines the upper limit of the stack frames for any exception.
#define MAX_STACK_FRAMES 128
#endif

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
#if !__has_feature(objc_arc)
  [super dealloc];
#endif
}

@end

jobject Java_java_lang_Throwable_nativeFillInStackTrace(JNIEnv *_env_, jclass _cls_) {
  return AUTORELEASE([[RawStack alloc] init]);
}

// Filter out native functions (no class), NSInvocation methods, and internal constructor.
static jboolean ShouldFilterStackElement(JavaLangStackTraceElement *element) {
  NSString *className = [element getClassName];
  if ([className hasPrefix:JavaLangStackTraceElement_STRIPPED]) {
    return true;
  }
  if ([className isEqualToString:@"NSInvocation"]) {
    return true;
  }
  if ([className isEqualToString:@"java.lang.Throwable"]) {
    NSString *methodName = [element getMethodName];
    if ([methodName isEqualToString:@"<init>"]
        || [methodName isEqualToString:@"fillInStackTrace"]) {
      return true;
    }
  }
  return false;
}

static void ProcessRawStack(RawStack *rawStack, NSMutableArray *frames, jboolean applyFilter) {
  for (unsigned i = 0; i < rawStack->count_; i++) {
    JavaLangStackTraceElement *element =
        [[JavaLangStackTraceElement alloc] initWithLong:(jlong)rawStack->frames_[i]];
    if (!applyFilter || !ShouldFilterStackElement(element)) {
      [frames addObject:element];
    }
    [element release];
  }
}

jarray Java_java_lang_Throwable_nativeGetStackTrace(
    JNIEnv *_env_, jclass _cls_, jobject stackState) {
  RawStack *rawStack = stackState;
  NSMutableArray *frames = [NSMutableArray array];
  if (rawStack) {
    ProcessRawStack(rawStack, frames, true);
    JavaLangStackTraceElement *element = [frames lastObject];
    // Remove initial Method.invoke(), so app's main method is last.
    if ([[element getClassName] isEqualToString:@"JavaLangReflectMethod"] &&
        [[element getMethodName] isEqualToString:@"invoke"]) {
      [frames removeLastObject];
    }
    // If symbols were removed, the stack trace will be empty at this point.
    // In order to help with debugging, return the raw stack trace.
    if ([frames count] == 0) {
      ProcessRawStack(rawStack, frames, false);
    }
  }
  return [IOSObjectArray arrayWithNSArray:frames type:JavaLangStackTraceElement_class_()];
}

void NSException_initWithNSString_(NSException *self, NSString *message) {
  // The NSException reason string is based on java.lang.Throwable.toString():
  //   . if there is a message, then the reason is "class-name: message",
  //   . otherwise, it's "class-name".
  NSString *clsName = [[self java_getClass] getName];
  NSString *reason = message ? [NSString stringWithFormat:@"%@: %@", clsName, message] : clsName;
  [self initWithName:[[self class] description] reason:reason userInfo:nil];
}
