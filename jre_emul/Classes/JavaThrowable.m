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
#import <objc/objc-exception.h>

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
static bool ShouldFilterStackElement(JavaLangStackTraceElement *element) {
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

static void ProcessRawStack(RawStack *rawStack, NSMutableArray *frames, bool applyFilter) {
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

// ObjC appears to expect that exceptions are meant to be [... raise]'ed or @throw'n near the
// allocation site. As part of that exception raise, CoreFoundation's __exceptionPreprocess() is
// passed the exception. This is what actually seems to create some declared nonnull properties
// on NSException.
//
// __exceptionPreprocess() has the rough psuedocode:
//
//  if (![[[raisedException userInfo] objectForKey:@"NSExceptionOmitCallstacks"] boolValue]) {
//    if (raisedException->privateIvar == nil) {
//      raisedException->privateIvar = CFDictionaryCreateMutable(NULL, 0, NULL);
//      [raisedException->privateIvar setObject:[[NSThread currentThread] callStackReturnAddresses]
//                                       forKey:@"callStackReturnAddresses"];
//      [raisedException->privateIvar setObject:[[NSThread currentThread] callStackSymbols]
//                                       forKey:@"callStackSymbols"];
//    }
//  }
//
// This code is not designed to be thread-safe because exceptions in ObjC are actually exceptional,
// and do not typically migrate across threads. It also assumes the allocation and throw occur in
// the appropriate frame for the stack to be accurate.
//
// In Java, exceptions are often just "error" objects that may not be thrown immediately. However,
// stacks are captured at allocation time (when enabled, see jre_emul/.../Throwable.java and
// Java_java_lang_Throwable_nativeFillInStackTrace() above).
//
// This makes the NSException behavior of capturing stacks at throw time incorrect. More
// dangerously, these Java "error" exceptions may be thrown for the first time later from other
// threads or even concurrently.
//
// There is no direct public API to allow access to CoreFoundation's preprocessor directly, but
// we can install our own preprocessor and use that to get access to the whole preprocessor
// chain.
//
// If that fails we will use NSExceptionOmitCallstacks as a fallback. Note that omitting stacks,
// causes our exceptions to violate some nonnull declared NSException properties. However, this
// appears to be true even of the base class, and so must be something exception handlers and
// other preprocessors must already deal with in practice.
//
// NSLog(@"%@", [[[NSException alloc] initWithName:@"foo" reason:nil userInfo:nil]
//                  callStackReturnAddresses]);
//
#ifdef J2OBJC_EXCEPTION_PREPROCESSING
static dispatch_once_t gExceptionPreprocessorOnce = 0;
static objc_exception_preprocessor gNextExceptionPreprocessor = nullptr;

// Static but named to make it clearer in stacktraces.
static id J2ObjCThrowableExceptionPreprocessor(id exception) {
  // Pure passthrough. gNextExceptionPreprocessor safe because it is guarded by the dispatch_once
  // below.
  if (gNextExceptionPreprocessor) {
    return gNextExceptionPreprocessor(exception);
  } else {
    return exception;
  }
}

static BOOL InstallPreprocessor(void) {
  // Install our preprocessor as late as possible (first Java exception). This gives us the best
  // chance of being the top of the exception preprocessing chain, which increases our chances
  // of executing all preprocessing at allocation time (like Java) as well as ensuring as much
  // thread safety as we can with the limited tools we have.
  //
  // Preprocessors will run a second time if the exception is thrown, the hope is that they
  // have the same basic thread safety as CoreFoundation __exceptionPreprocess() and don't mutate
  // the object a second time.
  dispatch_once(&gExceptionPreprocessorOnce, ^{
    // "Tickle" CoreFoundation in order to try to be sure that it has already installed
    // its preprocessor. The conditions for this are not documented, but it seems reasonable
    // to assume it happens on framework initialization. There is no documented relationship
    // between CFError and CoreFoundation's internal exception preprocessor, but its as good
    // as any other CFType for our purpose.
    CFErrorRef tickleError = CFErrorCreate(kCFAllocatorDefault, CFSTR("ignored"), 0, nullptr);
    if (tickleError) {
      CFRelease(tickleError);
    }

    // As an extra defensive step, raise an NSException and eat the result just in case there
    // is some addition private registration between NSException and CoreFoundation.
    @try {
      NSException *warmupException = AUTORELEASE([[NSException alloc] initWithName:@"J2ObjCWarmUp"
                                                                            reason:nil
                                                                          userInfo:nil]);
      [warmupException raise];
    } @catch (id e) {
      // Ignored.
    }

    // At this point it seems likely that CF __exceptionPreprocess() is before us in the
    // preprocessor chain.
    gNextExceptionPreprocessor =
        objc_setExceptionPreprocessor(&J2ObjCThrowableExceptionPreprocessor);
  });

  // Assume CoreFoundation was somewhere in the preprocessor chain if we have any next preprocessor.
  return gNextExceptionPreprocessor != nullptr;
}

static void PrePreprocessException(id exception) {
  if (InstallPreprocessor() && gNextExceptionPreprocessor) {
    gNextExceptionPreprocessor(exception);
  }
}

#endif  // J2OBJC_EXCEPTION_PREPROCESSING

void NSException_initWithNSString_(NSException *self, NSString *message) {
  // The NSException reason string is based on java.lang.Throwable.toString():
  //   . if there is a message, then the reason is "class-name: message",
  //   . otherwise, it's "class-name".
  NSString *clsName = [[self java_getClass] getName];
  NSString *reason = message ? [NSString stringWithFormat:@"%@: %@", clsName, message] : clsName;
  NSDictionary *userInfo = nil;
  BOOL preprocessingAvailable = NO;

#ifdef J2OBJC_EXCEPTION_PREPROCESSING
  preprocessingAvailable = InstallPreprocessor();
  if (!preprocessingAvailable) {
    // If no preprocessors ran then the safest thing we can do is omit callstacks.
    userInfo = @{
      @"NSExceptionOmitCallstacks" : @YES,
    };
  }
#endif  // J2OBJC_EXCEPTION_PREPROCESSING

  // Under ObjC initialization rules we should not be ignoring the return value
  // here because [... init...] can return a different instance than |self| in
  // class clusters and other scenarios. In practice, however, it seems that
  // NSException doesn't do that.
  [self initWithName:[[self class] description] reason:reason userInfo:userInfo];

#ifdef J2OBJC_EXCEPTION_PREPROCESSING
  if (preprocessingAvailable) {
    PrePreprocessException(self);
  }
#endif  // J2OBJC_EXCEPTION_PREPROCESSING
}
