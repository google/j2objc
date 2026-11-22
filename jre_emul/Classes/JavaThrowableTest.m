// Copyright 2026 Google Inc. All Rights Reserved.
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
//  JavaThrowableTest.m
//  JreEmulation
//
//  Tests for the java.lang.Throwable glue in JavaThrowable.m.
//

#import <XCTest/XCTest.h>
#import <dlfcn.h>
#import <execinfo.h>
#import <objc/objc-exception.h>
#import <objc/runtime.h>
#import <sched.h>
#import <stdatomic.h>
#import <stdlib.h>
#import <unistd.h>

#import "J2ObjC_source.h"
#import "java/lang/RuntimeException.h"

// Number of threads that throw the same exception at the same time.
static const int kThreadCount = 8;

static atomic_int gStartedThreads;
static atomic_int gCatchFailures;

// Runs |block| on kThreadCount new threads that all start it at about the same time, and waits for
// them to finish. Returns NO if they don't finish within 10 seconds.
static BOOL RunConcurrently(void (^block)(void)) {
  atomic_store(&gStartedThreads, 0);
  dispatch_group_t group = dispatch_group_create();
  for (int i = 0; i < kThreadCount; i++) {
    dispatch_group_enter(group);
    NSThread *thread = [[NSThread alloc] initWithBlock:^{
      atomic_fetch_add(&gStartedThreads, 1);
      while (atomic_load(&gStartedThreads) < kThreadCount) {
        sched_yield();
      }
      block();
      dispatch_group_leave(group);
    }];
    [thread start];
    [thread release];
  }
  long timedOut = dispatch_group_wait(group, dispatch_time(DISPATCH_TIME_NOW, 10 * NSEC_PER_SEC));
  dispatch_release(group);
  return timedOut == 0;
}

// Throws and catches |exception|, and counts it in gCatchFailures if something else was caught.
static void ThrowAndCatch(id exception) {
  @try {
    @throw exception;
  } @catch (id caught) {
    if (caught != exception) {
      atomic_fetch_add(&gCatchFailures, 1);
    }
  }
}

// Returns whether |address| is in CoreFoundation.
static BOOL IsInCoreFoundation(const void *address) {
  Dl_info image;
  return dladdr(address, &image) && image.dli_fname &&
         strstr(image.dli_fname, "/CoreFoundation.framework/") != NULL;
}

// Atomically raises |*max| to |value| if |value| is larger.
static void UpdateMax(atomic_int *max, int value) {
  int current = atomic_load(max);
  while (value > current && !atomic_compare_exchange_weak(max, &current, value)) {
  }
}

// The state of CountingCallStackSymbols() below.
static _Thread_local BOOL tCountCallStackSymbols;
static _Thread_local id tExceptionToThrowInCallStackSymbols;
static useconds_t gCallStackSymbolsDelay;
static atomic_int gCallStackSymbolsCalls;
static atomic_int gCallStackSymbolsInProgress;
static atomic_int gCallStackSymbolsMaxInProgress;
static Method gCallStackSymbolsMethod;
static IMP gOriginalCallStackSymbols;

// Replacement for +[NSThread callStackSymbols]. CoreFoundation's __exceptionPreprocess() calls it
// when it fills in the call stacks of an exception that is thrown. On the threads that set
// tCountCallStackSymbols, it counts the calls from CoreFoundation and how many of them run at the
// same time. It can also sleep, so that threads that fill in the call stacks of the same exception
// at the same time overlap here. If tExceptionToThrowInCallStackSymbols is set, it throws and
// catches that exception once, while J2ObjC's preprocessor holds the lock of the exception whose
// call stacks are being filled in.
static NSArray *CountingCallStackSymbols(id self, SEL _cmd) {
  if (!tCountCallStackSymbols || !IsInCoreFoundation(__builtin_return_address(0))) {
    return ((NSArray * (*)(id, SEL)) gOriginalCallStackSymbols)(self, _cmd);
  }
  atomic_fetch_add(&gCallStackSymbolsCalls, 1);
  UpdateMax(&gCallStackSymbolsMaxInProgress, atomic_fetch_add(&gCallStackSymbolsInProgress, 1) + 1);
  id exceptionToThrow = tExceptionToThrowInCallStackSymbols;
  if (exceptionToThrow) {
    tExceptionToThrowInCallStackSymbols = nil;
    ThrowAndCatch(exceptionToThrow);
  }
  if (gCallStackSymbolsDelay > 0) {
    usleep(gCallStackSymbolsDelay);
  }
  NSArray *symbols = ((NSArray * (*)(id, SEL)) gOriginalCallStackSymbols)(self, _cmd);
  atomic_fetch_sub(&gCallStackSymbolsInProgress, 1);
  return symbols;
}

// Restores +[NSThread callStackSymbols], if StartCountingCallStackSymbols() replaced it.
static void StopCountingCallStackSymbols(void) {
  if (gCallStackSymbolsMethod) {
    method_setImplementation(gCallStackSymbolsMethod, gOriginalCallStackSymbols);
    gCallStackSymbolsMethod = NULL;
  }
}

// Replaces +[NSThread callStackSymbols] with CountingCallStackSymbols() until
// StopCountingCallStackSymbols() is called or |testCase| ends.
static void StartCountingCallStackSymbols(XCTestCase *testCase, useconds_t delay) {
  gCallStackSymbolsDelay = delay;
  atomic_store(&gCallStackSymbolsCalls, 0);
  atomic_store(&gCallStackSymbolsMaxInProgress, 0);
  gCallStackSymbolsMethod = class_getClassMethod([NSThread class], @selector(callStackSymbols));
  // Save the original first: once the replacement is installed, any thread may call it.
  gOriginalCallStackSymbols = method_getImplementation(gCallStackSymbolsMethod);
  method_setImplementation(gCallStackSymbolsMethod, (IMP)CountingCallStackSymbols);
  [testCase addTeardownBlock:^{
    StopCountingCallStackSymbols();
  }];
}

// Throws |exception| on kThreadCount threads at the same time, counting the calls that
// CoreFoundation makes to +[NSThread callStackSymbols]. Returns NO if the threads don't finish.
static BOOL ThrowConcurrently(id exception) {
  return RunConcurrently(^{
    tCountCallStackSymbols = YES;
    ThrowAndCatch(exception);
    tCountCallStackSymbols = NO;
  });
}

// Parts of the call stack symbols of J2ObjC's exception preprocessor, objc_exception_throw() and
// FakeCrashReporterPreprocessor() below.
static NSString *const kJ2ObjCPreprocessor = @" J2ObjCThrowableExceptionPreprocessor ";
static NSString *const kThrow = @" objc_exception_throw ";
static NSString *const kFakeCrashReporter = @" FakeCrashReporterPreprocessor ";

// Returns the index of the first call stack symbol that contains |function|, or NSNotFound, also if
// |symbols| is nil.
static NSUInteger IndexOfFunction(NSArray<NSString *> *symbols, NSString *function) {
  if (!symbols) {
    return NSNotFound;
  }
  return [symbols indexOfObjectPassingTest:^BOOL(NSString *symbol, NSUInteger index, BOOL *stop) {
    return [symbol containsString:function];
  }];
}

// Returns the number of call stack symbols that contain |function|.
static NSUInteger CountOfFunction(NSArray<NSString *> *symbols, NSString *function) {
  return
      [[symbols indexesOfObjectsPassingTest:^BOOL(NSString *symbol, NSUInteger index, BOOL *stop) {
        return [symbol containsString:function];
      }] count];
}

// Returns the call stack symbol at |index|, or an empty string if there is none.
static NSString *SymbolAtIndex(NSArray<NSString *> *symbols, NSUInteger index) {
  return index < symbols.count ? symbols[index] : @"";
}

// Returns what NSException's own implementation of |selector| returns for |exception|: the call
// stack that CoreFoundation recorded, which JavaLangThrowable's override leaves J2ObjC's frame out
// of.
static NSArray *RecordedCallStack(NSException *exception, SEL selector) {
  IMP recorded = class_getMethodImplementation([NSException class], selector);
  return ((NSArray * (*)(id, SEL)) recorded)(exception, selector);
}

// Returns |array| without the element at |index|.
static NSArray *ArrayWithoutIndex(NSArray *array, NSUInteger index) {
  NSMutableArray *result = [NSMutableArray arrayWithArray:array];
  if (index < result.count) {
    [result removeObjectAtIndex:index];
  }
  return result;
}

// The state of FakeCrashReporterPreprocessor() below.
static objc_exception_preprocessor gReplacedPreprocessor;
static _Thread_local BOOL tRecordCrashReporterStack;
static NSArray<NSString *> *gCrashReporterStack;
static atomic_int gCrashReporterCalls;

// Returns the symbols of the call stack of the calling thread.
static NSArray<NSString *> *CurrentCallStack(void) {
  void *frames[128];
  int count = backtrace(frames, 128);
  char **symbols = backtrace_symbols(frames, count);
  NSMutableArray<NSString *> *stack = [NSMutableArray arrayWithCapacity:count];
  for (int i = 0; i < count; i++) {
    [stack addObject:[NSString stringWithUTF8String:symbols[i]]];
  }
  free(symbols);
  return stack;
}

// A preprocessor like a crash reporter's, which an app installs after J2ObjC's. On the threads that
// set tRecordCrashReporterStack, it records the call stack that it runs on in gCrashReporterStack.
// It calls the preprocessor that it replaced.
static id FakeCrashReporterPreprocessor(id exception) {
  if (tRecordCrashReporterStack) {
    [gCrashReporterStack release];
    gCrashReporterStack = [CurrentCallStack() retain];
  }
  objc_exception_preprocessor replaced = gReplacedPreprocessor;
  if (!replaced) {
    return exception;
  }
  id result = replaced(exception);
  // Like Crashpad's, this preprocessor stays on the stack while the one it replaced runs.
  atomic_fetch_add(&gCrashReporterCalls, 1);
  return result;
}

// Puts back the preprocessor that FakeCrashReporterPreprocessor() replaced, like Crashpad does when
// it stops handling exceptions.
static void UninstallFakeCrashReporter(void) {
  if (gReplacedPreprocessor) {
    objc_setExceptionPreprocessor(gReplacedPreprocessor);
    gReplacedPreprocessor = NULL;
  }
}

// Installs FakeCrashReporterPreprocessor() in front of the other exception preprocessors until
// UninstallFakeCrashReporter() is called or |testCase| ends.
static void InstallFakeCrashReporter(XCTestCase *testCase) {
  atomic_store(&gCrashReporterCalls, 0);
  gReplacedPreprocessor = objc_setExceptionPreprocessor(&FakeCrashReporterPreprocessor);
  [testCase addTeardownBlock:^{
    UninstallFakeCrashReporter();
    [gCrashReporterStack release];
    gCrashReporterStack = nil;
  }];
}

@interface JavaThrowableTest : XCTestCase
@end

@implementation JavaThrowableTest

- (void)setUp {
  [super setUp];
  atomic_store(&gCatchFailures, 0);
}

// Throws a new Java exception on kThreadCount threads at the same time, and checks that they took
// turns filling in its call stacks.
- (void)checkConcurrentThrowsTakeTurns {
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"shared"]);

  // Sleep in +callStackSymbols, so that threads that fill in the call stacks at the same time
  // overlap there.
  StartCountingCallStackSymbols(self, 20 * 1000);
  BOOL finished = ThrowConcurrently(exception);
  StopCountingCallStackSymbols();

  XCTAssertTrue(finished, @"The throwing threads didn't finish.");
  XCTAssertEqual(atomic_load(&gCatchFailures), 0, @"Caught a different exception than thrown.");
  XCTAssertEqual(atomic_load(&gCallStackSymbolsMaxInProgress), 1,
                 @"Threads filled in the call stacks of a Java exception at the same time.");
  XCTAssertEqual(atomic_load(&gCallStackSymbolsCalls), 1,
                 @"CoreFoundation filled in the call stacks of a Java exception more than once.");
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  XCTAssertNotEqual(IndexOfFunction(symbols, kThrow), NSNotFound, @"%@", symbols);
  XCTAssertEqual(CountOfFunction(symbols, kJ2ObjCPreprocessor), 0U, @"%@", symbols);
  // Not compared with the number of symbols, which also has the frame of
  // CountingCallStackSymbols().
  XCTAssertGreaterThan([[exception callStackReturnAddresses] count], 0U);
}

- (void)testThrownJavaExceptionsHaveCallStacks {
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"java"]);
  ThrowAndCatch(exception);

  NSArray<NSNumber *> *addresses = [exception callStackReturnAddresses];
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  XCTAssertGreaterThan(addresses.count, 0U);
  XCTAssertEqual(addresses.count, symbols.count);
  XCTAssertNotEqual(IndexOfFunction(symbols, kThrow), NSNotFound, @"%@", symbols);
  XCTAssertEqual(atomic_load(&gCatchFailures), 0, @"Caught a different exception than thrown.");
}

// Nothing runs the exception preprocessors when a Java exception is created, so like any
// NSException, it has no call stacks until it is thrown.
- (void)testJavaExceptionsThatWereNeverThrownHaveNoCallStacks {
  StartCountingCallStackSymbols(self, 0);
  tCountCallStackSymbols = YES;
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"never thrown"]);
  tCountCallStackSymbols = NO;
  StopCountingCallStackSymbols();

  XCTAssertEqual(atomic_load(&gCallStackSymbolsCalls), 0,
                 @"CoreFoundation filled in the call stacks of a Java exception that was created.");
  XCTAssertNil([exception callStackReturnAddresses]);
  XCTAssertNil([exception callStackSymbols]);
}

// Like CoreFoundation does for any exception, the call stacks are those of the first throw when an
// exception is thrown again.
- (void)testCallStacksAreThoseOfTheFirstThrow {
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"rethrown"]);
  ThrowAndCatch(exception);
  NSArray<NSNumber *> *addresses = [exception callStackReturnAddresses];
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  ThrowAndCatch(exception);

  XCTAssertEqualObjects([exception callStackReturnAddresses], addresses);
  XCTAssertEqualObjects([exception callStackSymbols], symbols);
  XCTAssertGreaterThan(addresses.count, 0U);
}

// The tests below count the calls that CoreFoundation makes to +[NSThread callStackSymbols] when
// it fills in the call stacks of an exception. Check that the count sees them.
- (void)testCountsCoreFoundationFillingInCallStacks {
  NSException *exception = [NSException exceptionWithName:@"Test" reason:nil userInfo:nil];

  StartCountingCallStackSymbols(self, 0);
  tCountCallStackSymbols = YES;
  ThrowAndCatch(exception);
  tCountCallStackSymbols = NO;
  StopCountingCallStackSymbols();

  XCTAssertGreaterThan(atomic_load(&gCallStackSymbolsCalls), 0,
                       @"Didn't see CoreFoundation fill in the call stacks of an NSException.");
}

// A failed future hands the same exception to all of its listeners, which may rethrow it on
// different threads at the same time. CoreFoundation's __exceptionPreprocess() lazily creates and
// fills a dictionary in the exception without synchronization, so this used to crash in
// -[__NSDictionaryM objectForKey:] or -[__NSDictionaryM setObject:forKey:] (b/531804243).
- (void)testConcurrentThrowsOfASharedExceptionTakeTurns {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  [self checkConcurrentThrowsTakeTurns];
}

// Many exceptions, each thrown on several threads at the same time, with no delay in
// +callStackSymbols: each exception gets its call stacks once, and no thread waits for a lock
// forever.
- (void)testConcurrentThrowsOfFreshExceptionsTakeTurns {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  const int kExceptionCount = 200;
  int unfinished = 0;
  int withoutCallStacks = 0;
  StartCountingCallStackSymbols(self, 0);
  for (int i = 0; i < kExceptionCount; i++) {
    @autoreleasepool {
      JavaLangRuntimeException *exception =
          AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"shared"]);
      if (!ThrowConcurrently(exception)) {
        unfinished++;
      }
      if (IndexOfFunction([exception callStackSymbols], kThrow) == NSNotFound) {
        withoutCallStacks++;
      }
    }
  }
  StopCountingCallStackSymbols();

  XCTAssertEqual(unfinished, 0, @"The throwing threads didn't finish for %d exceptions.",
                 unfinished);
  XCTAssertEqual(withoutCallStacks, 0, @"%d exceptions had no call stacks.", withoutCallStacks);
  XCTAssertEqual(atomic_load(&gCatchFailures), 0, @"Caught a different exception than thrown.");
  XCTAssertEqual(atomic_load(&gCallStackSymbolsMaxInProgress), 1,
                 @"Threads filled in the call stacks of a Java exception at the same time.");
  XCTAssertEqual(atomic_load(&gCallStackSymbolsCalls), kExceptionCount,
                 @"CoreFoundation didn't fill in the call stacks of each exception once.");
}

// A thread that throws a Java exception while J2ObjC's preprocessor holds that exception's lock for
// it, here from inside CoreFoundation's preprocessor, doesn't wait for the lock: os_unfair_lock
// would abort.
- (void)testThrowingWhileHoldingTheLockDoesNotTakeItAgain {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"thrown twice"]);

  StartCountingCallStackSymbols(self, 0);
  tCountCallStackSymbols = YES;
  tExceptionToThrowInCallStackSymbols = exception;
  ThrowAndCatch(exception);
  tCountCallStackSymbols = NO;
  StopCountingCallStackSymbols();

  XCTAssertNil(tExceptionToThrowInCallStackSymbols,
               @"Didn't throw while CoreFoundation filled in the call stacks.");
  XCTAssertEqual(atomic_load(&gCatchFailures), 0, @"Caught a different exception than thrown.");
  XCTAssertNotEqual(IndexOfFunction([exception callStackSymbols], kThrow), NSNotFound);
}

// For a Java exception, J2ObjC's preprocessor runs CoreFoundation's, so it is on the stack when
// CoreFoundation records the call stacks: right below __exceptionPreprocess(), and right above
// objc_exception_throw(), which called it.
- (void)testJ2ObjCPreprocessorRunsCoreFoundationsForJavaExceptions {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"java"]);
  ThrowAndCatch(exception);

  NSArray<NSString *> *symbols = RecordedCallStack(exception, @selector(callStackSymbols));
  NSUInteger preprocessorIndex = IndexOfFunction(symbols, kJ2ObjCPreprocessor);
  XCTAssertNotEqual(preprocessorIndex, NSNotFound, @"%@", symbols);
  XCTAssertEqual(CountOfFunction(symbols, kJ2ObjCPreprocessor), 1U, @"%@", symbols);
  XCTAssertTrue([SymbolAtIndex(symbols, preprocessorIndex - 1) containsString:@"CoreFoundation"],
                @"%@", symbols);
  XCTAssertEqual(IndexOfFunction(symbols, kThrow), preprocessorIndex + 1, @"%@", symbols);
}

// JavaLangThrowable returns the call stacks that CoreFoundation recorded without J2ObjC's frame, so
// that crash reporters see the same call stacks as without J2ObjC's preprocessor.
- (void)testCallStacksLeaveOutJ2ObjCPreprocessorFrame {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"java"]);
  ThrowAndCatch(exception);

  NSArray<NSString *> *recordedSymbols = RecordedCallStack(exception, @selector(callStackSymbols));
  NSArray<NSNumber *> *recordedAddresses =
      RecordedCallStack(exception, @selector(callStackReturnAddresses));
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  NSArray<NSNumber *> *addresses = [exception callStackReturnAddresses];
  NSUInteger preprocessorIndex = IndexOfFunction(recordedSymbols, kJ2ObjCPreprocessor);
  NSUInteger throwIndex = IndexOfFunction(symbols, kThrow);

  XCTAssertNotEqual(preprocessorIndex, NSNotFound, @"%@", recordedSymbols);
  XCTAssertEqualObjects(symbols, ArrayWithoutIndex(recordedSymbols, preprocessorIndex));
  XCTAssertEqualObjects(addresses, ArrayWithoutIndex(recordedAddresses, preprocessorIndex));
  XCTAssertEqual(CountOfFunction(symbols, kJ2ObjCPreprocessor), 0U, @"%@", symbols);
  XCTAssertTrue([SymbolAtIndex(symbols, throwIndex - 1) containsString:@"CoreFoundation"], @"%@",
                symbols);
  XCTAssertThrowsSpecificNamed([symbols objectAtIndex:symbols.count], NSException,
                               NSRangeException);
  XCTAssertThrowsSpecificNamed([symbols objectAtIndex:NSUIntegerMax], NSException,
                               NSRangeException);
}

// The symbols can have more frames than the return addresses, here because
// CountingCallStackSymbols() was on the stack when CoreFoundation got the symbols. Both end with
// the same outermost frames, so JavaLangThrowable finds J2ObjC's frame in the symbols by counting
// from the end.
- (void)testCallStackSymbolsLeaveOutJ2ObjCPreprocessorFrameWhenThereAreMoreSymbols {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"more symbols"]);
  StartCountingCallStackSymbols(self, 0);
  tCountCallStackSymbols = YES;
  ThrowAndCatch(exception);
  tCountCallStackSymbols = NO;
  StopCountingCallStackSymbols();

  NSArray<NSString *> *recordedSymbols = RecordedCallStack(exception, @selector(callStackSymbols));
  NSArray<NSNumber *> *recordedAddresses =
      RecordedCallStack(exception, @selector(callStackReturnAddresses));
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  NSUInteger preprocessorIndex = IndexOfFunction(recordedSymbols, kJ2ObjCPreprocessor);

  XCTAssertEqual(recordedSymbols.count, recordedAddresses.count + 1, @"%@", recordedSymbols);
  XCTAssertNotEqual(preprocessorIndex, NSNotFound, @"%@", recordedSymbols);
  XCTAssertEqualObjects(symbols, ArrayWithoutIndex(recordedSymbols, preprocessorIndex));
}

// Other exceptions go straight to CoreFoundation's preprocessor, so J2ObjC's is not on the stack
// when CoreFoundation records their call stacks.
- (void)testOtherExceptionsGoStraightToCoreFoundation {
  NSException *exception = [NSException exceptionWithName:@"Test" reason:nil userInfo:nil];
  ThrowAndCatch(exception);

  NSArray<NSString *> *symbols = [exception callStackSymbols];
  XCTAssertNotEqual(IndexOfFunction(symbols, kThrow), NSNotFound, @"%@", symbols);
  XCTAssertEqual(CountOfFunction(symbols, kJ2ObjCPreprocessor), 0U, @"%@", symbols);
  XCTAssertEqual(atomic_load(&gCatchFailures), 0, @"Caught a different exception than thrown.");
}

// Crash reporters install their preprocessors after J2ObjC's, so theirs run first: J2ObjC's is not
// on the stack when they look for the handler of an exception or record a crash.
- (void)testPreprocessorsInstalledLaterRunFirst {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  InstallFakeCrashReporter(self);
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"reported"]);
  tRecordCrashReporterStack = YES;
  ThrowAndCatch(exception);
  tRecordCrashReporterStack = NO;

  XCTAssertNotNil(gCrashReporterStack);
  XCTAssertEqual(CountOfFunction(gCrashReporterStack, kJ2ObjCPreprocessor), 0U, @"%@",
                 gCrashReporterStack);
  NSArray<NSString *> *recorded = RecordedCallStack(exception, @selector(callStackSymbols));
  NSUInteger preprocessorIndex = IndexOfFunction(recorded, kJ2ObjCPreprocessor);
  XCTAssertNotEqual(preprocessorIndex, NSNotFound, @"%@", recorded);
  XCTAssertEqual(IndexOfFunction(recorded, kFakeCrashReporter), preprocessorIndex + 1, @"%@",
                 recorded);
  XCTAssertEqual(IndexOfFunction(recorded, kThrow), preprocessorIndex + 2, @"%@", recorded);
}

// Behind a crash reporter's preprocessor, the call stacks that JavaLangThrowable returns, which the
// crash reporter reads when it reports an uncaught exception later, look the same as without
// J2ObjC's preprocessor.
- (void)testCallStacksLeaveOutJ2ObjCPreprocessorFrameBehindACrashReporter {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  InstallFakeCrashReporter(self);
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"reported"]);
  ThrowAndCatch(exception);

  NSArray<NSString *> *recorded = RecordedCallStack(exception, @selector(callStackSymbols));
  NSArray<NSString *> *symbols = [exception callStackSymbols];
  NSUInteger throwIndex = IndexOfFunction(symbols, kThrow);

  XCTAssertNotEqual(IndexOfFunction(recorded, kJ2ObjCPreprocessor), NSNotFound, @"%@", recorded);
  XCTAssertEqual(CountOfFunction(symbols, kJ2ObjCPreprocessor), 0U, @"%@", symbols);
  XCTAssertEqual(IndexOfFunction(symbols, kFakeCrashReporter), throwIndex - 1, @"%@", symbols);
  XCTAssertTrue([SymbolAtIndex(symbols, throwIndex - 2) containsString:@"CoreFoundation"], @"%@",
                symbols);
}

// Concurrent throws still take turns behind a crash reporter's preprocessor.
- (void)testConcurrentThrowsTakeTurnsBehindACrashReporter {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  InstallFakeCrashReporter(self);

  [self checkConcurrentThrowsTakeTurns];

  XCTAssertGreaterThan(atomic_load(&gCrashReporterCalls), 0);
}

// When a crash reporter stops handling exceptions, like Crashpad does, it puts back the
// preprocessor that it replaced, which is J2ObjC's.
- (void)testCrashReporterPutsBackJ2ObjCPreprocessor {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  InstallFakeCrashReporter(self);
  UninstallFakeCrashReporter();
  JavaLangRuntimeException *exception =
      AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:@"not reported"]);
  ThrowAndCatch(exception);

  NSArray<NSString *> *recorded = RecordedCallStack(exception, @selector(callStackSymbols));
  NSUInteger preprocessorIndex = IndexOfFunction(recorded, kJ2ObjCPreprocessor);
  XCTAssertNotEqual(preprocessorIndex, NSNotFound, @"%@", recorded);
  XCTAssertEqual(CountOfFunction(recorded, kFakeCrashReporter), 0U, @"%@", recorded);
  XCTAssertEqual(IndexOfFunction(recorded, kThrow), preprocessorIndex + 1, @"%@", recorded);
}

// Concurrent throws still take turns after a crash reporter puts back J2ObjC's preprocessor.
- (void)testConcurrentThrowsTakeTurnsAfterACrashReporterStops {
#ifndef J2OBJC_EXCEPTION_PREPROCESSING
  XCTSkip(@"Needs --define=j2objc_exception_preprocessing=1.");
#endif
  InstallFakeCrashReporter(self);
  UninstallFakeCrashReporter();

  [self checkConcurrentThrowsTakeTurns];

  XCTAssertEqual(atomic_load(&gCrashReporterCalls), 0);
}

@end
