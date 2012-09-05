//
//  JUnitRunner.h
//  JreEmulation
//
//  Created by Tom Ball on 11/10/11.
//  Copyright 2011 Google Inc. All rights reserved.
//

#import "JreEmulation.h"
#import "junit/framework/TestSuite.h"

@class JunitFrameworkTest;

@interface JUnitRunner : NSObject {
}

// Executes specified tests in a JUnit class and logs the results.
// The first argument is the JUnit class, followed by a nil-terminated
// list of test names.
//
// @return the number of test failures and exceptions
+ (int)runTests:(Class)testClass, ...
    NS_REQUIRES_NIL_TERMINATION;

// Creates a test suite from a JUnit test class and a nil-terminated list 
// of its test names.
+ (JunitFrameworkTestSuite *)testSuite:(Class)testClass, ...
    NS_REQUIRES_NIL_TERMINATION;

// Creates a test suite from a JUnit test class and a va_list of its 
// test names.
+ (JunitFrameworkTestSuite *)testSuite:(Class)testClass 
                         withArguments:(va_list)args;

// Executes a unit test or test suite, and logs the results.
//
// @return the number of test failures and exceptions
+ (int)runTest:(id<JunitFrameworkTest>)test;

@end
