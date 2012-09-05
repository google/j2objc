//
//  JUnitRunner.m
//  JreEmulation
//
//  Created by Tom Ball on 11/10/11.
//  Copyright 2011 Google Inc. All rights reserved.
//

#import "JUnitRunner.h"
#import "junit/framework/TestFailure.h"
#import "junit/framework/TestResult.h"

#import "java/lang/Throwable.h"
#import "java/util/Enumeration.h"

@implementation JUnitRunner

void listFailedTests(id<JavaUtilEnumeration> problems);

+ (int)runTests:(Class)testClass, ... {
  va_list args;
  va_start(args, testClass);
  JunitFrameworkTestSuite *suite =
      [JUnitRunner testSuite:testClass withArguments:args];
  return [JUnitRunner runTest:suite];
}

+ (JunitFrameworkTestSuite *)testSuite:(Class)testClass, ... {
  va_list args;
  va_start(args, testClass);
  return [JUnitRunner testSuite:testClass withArguments:args];
}
                                      
+ (JunitFrameworkTestSuite *)testSuite:(Class)testClass
                         withArguments:(va_list)args {
  JunitFrameworkTestSuite *suite = 
      [[JunitFrameworkTestSuite alloc]
       initWithNSString:NSStringFromClass(testClass)];
  NSString *name;
  while ((name = va_arg(args, NSString *)) != nil) {
    JunitFrameworkTestCase *test = [testClass new];
#if ! __has_feature(objc_arc)
    [test autorelease];
#endif
    [test setNameWithNSString:name];
    [suite addTestWithJunitFrameworkTest:test];
  }
#if ! __has_feature(objc_arc)
	[suite autorelease];
#endif
  return suite;
}

+ (int)runTest:(id<JunitFrameworkTest>)test {
  JunitFrameworkTestResult *result =
		[[JunitFrameworkTestResult alloc] init];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  [test runWithJunitFrameworkTestResult:result];
  if ([result wasSuccessful]) {
    NSLog(@"OK (%d test%@)", [result runCount], 
          [result runCount] == 1 ? @"" : @"s");
  }
  else {
    listFailedTests([result failures]);
    listFailedTests([result errors]);
    NSLog(@"FAILURES!!!\nTests run: %d,  Failures: %d,  Errors: %d",
        [result runCount], [result failureCount], [result errorCount]);
  }
  return [result failureCount] + [result errorCount];
}

void listFailedTests(id<JavaUtilEnumeration> problems) {
  while ([problems hasMoreElements]) {
    JunitFrameworkTestFailure *problem =
        (JunitFrameworkTestFailure *) [problems nextElement];
    NSLog(@"%@", problem);
    JavaLangThrowable *exception = [problem thrownException];
    if (exception) {
      [exception printStackTrace];
    }
  }
}

@end
