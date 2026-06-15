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
//  NSObject+CloneTest.m
//  JreEmulation
//
//  Created by Tom Ball on 8/25/11.
//

#import "NSObject+JavaObject.h"
#import "IOSClass.h"
#import "com/google/j2objc/util/ReflectionUtil.h"
#import "java/lang/CloneNotSupportedException.h"
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import <XCTest/XCTest.h>

#import "java/lang/Integer.h"
#import "java/lang/Double.h"
#import "java/lang/Long.h"
#import "java/lang/Float.h"
#import "java/lang/Short.h"
#import "java/lang/Byte.h"

// Unit tests for NSObject+Clone.
@interface NSObject_JavaObjectTest : XCTestCase
@end

@implementation NSObject_JavaObjectTest

- (void)testResponds {
  XCTAssertTrue([@"tester" respondsToSelector: @selector(compareToWithId:)],
               @"NSObject+Clone category not loaded", nil);
}

- (void)testGetClass {
  // Test with class.
  JavaUtilArrayList *one = AUTORELEASE([[JavaUtilArrayList alloc] init]);
  IOSClass *clazz = [one java_getClass];
  bool result = ComGoogleJ2objcUtilReflectionUtil_matchClassNamePrefixWithNSString_withNSString_(
      [clazz getName], @"java.util.ArrayList");
  XCTAssertTrue(result, @"incorrect class name");

  // Now with a protocol.
  id<JavaUtilList> two = AUTORELEASE([[JavaUtilArrayList alloc] init]);
  clazz = [(id<JavaObject>) two java_getClass];
  result =
      ComGoogleJ2objcUtilReflectionUtil_matchClassNamePrefixWithNSString_withNSString_(
          [clazz getName], @"java.util.ArrayList");
  XCTAssertTrue(result, @"incorrect class name");
}

- (void)testBoxedNumberCopy {
  JavaLangInteger *valInt = JavaLangInteger_valueOfWithInt_(42);
  JavaLangInteger *copyInt = [valInt copy];
  XCTAssertTrue(valInt == copyInt, @"copy on JavaLangInteger should return self");
  XCTAssertTrue([copyInt isKindOfClass:[JavaLangInteger class]], @"copied type must be JavaLangInteger");
  RELEASE_(copyInt);

  JavaLangDouble *valDouble = JavaLangDouble_valueOfWithDouble_(3.14);
  JavaLangDouble *copyDouble = [valDouble copy];
  XCTAssertTrue(valDouble == copyDouble, @"copy on JavaLangDouble should return self");
  XCTAssertTrue([copyDouble isKindOfClass:[JavaLangDouble class]], @"copied type must be JavaLangDouble");
  RELEASE_(copyDouble);

  JavaLangLong *valLong = JavaLangLong_valueOfWithLong_(123456789L);
  JavaLangLong *copyLong = [valLong copy];
  XCTAssertTrue(valLong == copyLong, @"copy on JavaLangLong should return self");
  XCTAssertTrue([copyLong isKindOfClass:[JavaLangLong class]], @"copied type must be JavaLangLong");
  RELEASE_(copyLong);

  JavaLangFloat *valFloat = JavaLangFloat_valueOfWithFloat_(2.71f);
  JavaLangFloat *copyFloat = [valFloat copy];
  XCTAssertTrue(valFloat == copyFloat, @"copy on JavaLangFloat should return self");
  XCTAssertTrue([copyFloat isKindOfClass:[JavaLangFloat class]], @"copied type must be JavaLangFloat");
  RELEASE_(copyFloat);

  JavaLangShort *valShort = JavaLangShort_valueOfWithShort_(12);
  JavaLangShort *copyShort = [valShort copy];
  XCTAssertTrue(valShort == copyShort, @"copy on JavaLangShort should return self");
  XCTAssertTrue([copyShort isKindOfClass:[JavaLangShort class]], @"copied type must be JavaLangShort");
  RELEASE_(copyShort);

  JavaLangByte *valByte = JavaLangByte_valueOfWithByte_(5);
  JavaLangByte *copyByte = [valByte copy];
  XCTAssertTrue(valByte == copyByte, @"copy on JavaLangByte should return self");
  XCTAssertTrue([copyByte isKindOfClass:[JavaLangByte class]], @"copied type must be JavaLangByte");
  RELEASE_(copyByte);
}

@end
