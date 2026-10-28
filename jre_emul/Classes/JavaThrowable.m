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
#import "java/lang/Throwable.h"
#import "jni.h"

#import <dlfcn.h>
#import <execinfo.h>
#import <objc/objc-exception.h>
#import <objc/runtime.h>
#import <os/lock.h>
#import <stdatomic.h>

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
// There is no public API to make __exceptionPreprocess() thread-safe, but there is one to chain
// exception preprocessors. When the J2ObjC runtime is loaded, it installs its own preprocessor
// directly in front of CoreFoundation's, and runs CoreFoundation's preprocessor for a Java
// exception while holding a lock for that exception. Threads that throw the same exception at the
// same time then take turns in __exceptionPreprocess(): the first one fills in the call stacks,
// and the others only read them. CoreFoundation still records the call stacks, and NSException
// still returns them. Other exceptions go straight to CoreFoundation's preprocessor.
//
// Preprocessors that are installed later, such as a crash reporter's, run before ours. Ours is not
// on the stack when they look for the handler of an exception or record a crash, and if they put
// back the preprocessor that they replaced, they put back ours. The call stacks that CoreFoundation
// records for a Java exception include our frame, right below __exceptionPreprocess(), but
// JavaLangThrowable leaves it out of the call stacks that it returns. We only hold the lock around
// CoreFoundation's preprocessor: if ours is not directly in front of it, ours passes exceptions on
// without locking.
//
// Running the preprocessors when the exception is created instead does not work. A crash
// reporter's preprocessor looks for the handler that will catch the exception, and for an
// exception that is only being created, it may find a catch-all handler, such as libdispatch's,
// and record the exception as uncaught.
//
// A Java exception that was never thrown has no call stacks. That is also true of the base class,
// and so must be something exception handlers and other preprocessors must already deal with in
// practice:
//
// NSLog(@"%@", [[[NSException alloc] initWithName:@"foo" reason:nil userInfo:nil]
//                  callStackReturnAddresses]);
//
#ifdef J2OBJC_EXCEPTION_PREPROCESSING
static dispatch_once_t gExceptionPreprocessorOnce = 0;
static objc_exception_preprocessor gNextExceptionPreprocessor = nullptr;

// Whether gNextExceptionPreprocessor is CoreFoundation's, the only one that we run while holding a
// lock.
static BOOL gNextExceptionPreprocessorIsCoreFoundation = NO;

static Class gJavaLangThrowableClass = Nil;

// The locks that serialize CoreFoundation's preprocessing of Java exceptions. An exception uses the
// lock picked by its address, so different exceptions rarely wait for each other. Zero filled is
// sufficient initialization.
enum { kPreprocessingLockCount = 64 };
static os_unfair_lock gPreprocessingLocks[kPreprocessingLockCount];

// Whether this thread holds one of gPreprocessingLocks. Exceptions that it throws while it does
// don't take a lock: os_unfair_lock aborts if a thread locks a lock that it already holds, and a
// thread that held two of the locks could deadlock with another one.
static _Thread_local BOOL tPreprocessingJavaException;

// The return address of the call from J2ObjCThrowableExceptionPreprocessor() to CoreFoundation's
// preprocessor, which is our frame in the call stacks that CoreFoundation records for Java
// exceptions. Set the first time that CoreFoundation records the call stacks of a Java exception.
static _Atomic(uintptr_t) gPreprocessorFrameAddress;

static void InstallPreprocessor(void);
static NSArray<NSNumber *> *RecordedCallStackReturnAddresses(id exception);
static void FindPreprocessorFrameAddress(id exception, uintptr_t callerAddress);

// Returns whether |object| is a Java exception. Unlike -isKindOfClass:, this checks the object's
// actual class, which a proxy can't misreport, and it doesn't send the object any messages.
static BOOL IsJavaThrowable(id object) {
  for (Class cls = object_getClass(object); cls; cls = class_getSuperclass(cls)) {
    if (cls == gJavaLangThrowableClass) {
      return YES;
    }
  }
  return NO;
}

// Returns the lock in gPreprocessingLocks for |exception|.
static os_unfair_lock *PreprocessingLock(id exception) {
  // Objects are 16-byte aligned, so the lowest 4 bits of their addresses are always 0.
  return &gPreprocessingLocks[((uintptr_t)exception >> 4) % kPreprocessingLockCount];
}

// Static but named to make it clearer in stacktraces.
static id J2ObjCThrowableExceptionPreprocessor(id exception) {
  // If another thread just installed this preprocessor, waits until it has also set
  // gNextExceptionPreprocessor.
  InstallPreprocessor();
  objc_exception_preprocessor next = gNextExceptionPreprocessor;
  if (!next) {
    return exception;
  }
  if (!gNextExceptionPreprocessorIsCoreFoundation || !IsJavaThrowable(exception) ||
      tPreprocessingJavaException) {
    // A tail call, so that our frame is not on the stack while the rest of the chain runs.
    __attribute__((musttail)) return next(exception);
  }
  os_unfair_lock *lock = PreprocessingLock(exception);
  os_unfair_lock_lock(lock);
  tPreprocessingJavaException = YES;
  id result = nil;
  @try {
    // Until we know the address of our frame, look for it when CoreFoundation records the call
    // stacks of a Java exception during this call, which it only does if the exception has none.
    BOOL findFrameAddress =
        !atomic_load_explicit(&gPreprocessorFrameAddress, memory_order_relaxed) &&
        !RecordedCallStackReturnAddresses(exception);
    result = next(exception);
    if (findFrameAddress) {
      FindPreprocessorFrameAddress(exception, (uintptr_t)__builtin_return_address(0));
    }
  } @finally {
    tPreprocessingJavaException = NO;
    os_unfair_lock_unlock(lock);
  }
  return result;
}

// Returns the call stack return addresses that CoreFoundation recorded for |exception|, from
// NSException's own method, which JavaLangThrowable overrides below.
static NSArray<NSNumber *> *RecordedCallStackReturnAddresses(id exception) {
  SEL selector = @selector(callStackReturnAddresses);
  IMP recorded = class_getMethodImplementation([NSException class], selector);
  return ((NSArray<NSNumber *> * (*)(id, SEL)) recorded)(exception, selector);
}

// Sets gPreprocessorFrameAddress from the call stacks that CoreFoundation recorded for |exception|
// while our preprocessor called it. Our frame is right above the frame of |callerAddress|, the
// return address into the function that called our preprocessor.
static void FindPreprocessorFrameAddress(id exception, uintptr_t callerAddress) {
  NSArray<NSNumber *> *addresses = RecordedCallStackReturnAddresses(exception);
  NSUInteger count = [addresses count];
  for (NSUInteger i = 1; i < count; i++) {
    if ([[addresses objectAtIndex:i] unsignedLongLongValue] == callerAddress) {
      atomic_store_explicit(&gPreprocessorFrameAddress,
                            (uintptr_t)[[addresses objectAtIndex:i - 1] unsignedLongLongValue],
                            memory_order_relaxed);
      return;
    }
  }
}

// Returns the index of our frame in |addresses|, call stack return addresses that CoreFoundation
// recorded, or NSNotFound.
static NSUInteger IndexOfPreprocessorFrame(NSArray<NSNumber *> *addresses) {
  uintptr_t frameAddress = atomic_load_explicit(&gPreprocessorFrameAddress, memory_order_relaxed);
  if (!frameAddress) {
    return NSNotFound;
  }
  NSUInteger count = [addresses count];
  for (NSUInteger i = 0; i < count; i++) {
    if ([[addresses objectAtIndex:i] unsignedLongLongValue] == frameAddress) {
      return i;
    }
  }
  return NSNotFound;
}

// A call stack without one of its frames. It doesn't copy the call stack, so that symbols, which
// +[NSThread callStackSymbols] looks up when they are read, aren't all looked up at once.
@interface JreCallStackWithoutFrame : NSArray {
  NSArray *callStack_;
  NSUInteger frameIndex_;
}
// |frameIndex| must be the index of a frame in |callStack|.
- (instancetype)initWithCallStack:(NSArray *)callStack frameIndex:(NSUInteger)frameIndex;
@end

@implementation JreCallStackWithoutFrame

- (instancetype)initWithCallStack:(NSArray *)callStack frameIndex:(NSUInteger)frameIndex {
  if ((self = [super init])) {
    callStack_ = [callStack retain];
    frameIndex_ = frameIndex;
  }
  return self;
}

- (NSUInteger)count {
  return [callStack_ count] - 1;
}

- (id)objectAtIndex:(NSUInteger)index {
  NSUInteger count = [self count];
  if (index >= count) {
    [NSException raise:NSRangeException
                format:@"index %lu beyond bounds of call stack with %lu frames",
                       (unsigned long)index, (unsigned long)count];
  }
  return [callStack_ objectAtIndex:index < frameIndex_ ? index : index + 1];
}

- (void)dealloc {
  [callStack_ release];
  [super dealloc];
}

@end

// JavaLangThrowable leaves our frame out of the call stacks that CoreFoundation records, so that
// crash reporters, and everything else that reads them, see the same call stacks as they would
// without our preprocessor.
@implementation JavaLangThrowable (J2ObjCExceptionPreprocessing)

- (NSArray<NSNumber *> *)callStackReturnAddresses {
  NSArray<NSNumber *> *addresses = [super callStackReturnAddresses];
  NSUInteger frameIndex = IndexOfPreprocessorFrame(addresses);
  if (frameIndex == NSNotFound) {
    return addresses;
  }
  return AUTORELEASE([[JreCallStackWithoutFrame alloc] initWithCallStack:addresses
                                                              frameIndex:frameIndex]);
}

- (NSArray<NSString *> *)callStackSymbols {
  NSArray<NSString *> *symbols = [super callStackSymbols];
  NSArray<NSNumber *> *addresses = [super callStackReturnAddresses];
  NSUInteger frameIndex = IndexOfPreprocessorFrame(addresses);
  NSUInteger symbolCount = [symbols count];
  NSUInteger addressCount = [addresses count];
  if (frameIndex == NSNotFound || symbolCount < addressCount) {
    return symbols;
  }
  // Both arrays end with the same outermost frames.
  return AUTORELEASE([[JreCallStackWithoutFrame alloc]
      initWithCallStack:symbols
             frameIndex:frameIndex + (symbolCount - addressCount)]);
}

@end

// Returns whether |address| is in CoreFoundation.
static BOOL IsInCoreFoundation(const void *address) {
  Dl_info coreFoundation;
  Dl_info image;
  return dladdr((const void *)&CFRetain, &coreFoundation) && dladdr(address, &image) &&
         image.dli_fbase == coreFoundation.dli_fbase;
}

static void InstallPreprocessor(void) {
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

    // Unlike [JavaLangThrowable class], this doesn't initialize the class.
    gJavaLangThrowableClass = objc_lookUpClass("JavaLangThrowable");
    gNextExceptionPreprocessor =
        objc_setExceptionPreprocessor(&J2ObjCThrowableExceptionPreprocessor);
    gNextExceptionPreprocessorIsCoreFoundation =
        gNextExceptionPreprocessor && IsInCoreFoundation((const void *)gNextExceptionPreprocessor);
  });
}

// Installs our preprocessor when the J2ObjC runtime is loaded, before the app can install others,
// such as a crash reporter's, so that those run before ours.
__attribute__((constructor)) static void InstallPreprocessorWhenLoaded(void) {
  InstallPreprocessor();
}

#endif  // J2OBJC_EXCEPTION_PREPROCESSING

void NSException_initWithNSString_(NSException *self, NSString *message) {
  // The NSException reason string is based on java.lang.Throwable.toString():
  //   . if there is a message, then the reason is "class-name: message",
  //   . otherwise, it's "class-name".
  NSString *clsName = [[self java_getClass] getName];
  NSString *reason = message ? [NSString stringWithFormat:@"%@: %@", clsName, message] : clsName;

  // Under ObjC initialization rules we should not be ignoring the return value
  // here because [... init...] can return a different instance than |self| in
  // class clusters and other scenarios. In practice, however, it seems that
  // NSException doesn't do that.
  [self initWithName:[[self class] description] reason:reason userInfo:nil];
}
