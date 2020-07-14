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
//  JavaLangReflectMethodTest.m
//  JreEmulation
//
//  Created by Tom Ball on 12/7/11.
//

#import <XCTest/XCTest.h>
#import "IOSClass.h"
#import "com/google/j2objc/util/ReflectionUtil.h"
#import "java/lang/Byte.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/Short.h"
#import "java/util/LinkedList.h"

static double defaultValue = 3.1416;

@interface JavaLangReflectMethodTest : XCTestCase {
 @private
  JavaUtilLinkedList *object_;
  IOSClass *class_;
}
@end


@implementation JavaLangReflectMethodTest

- (void)setUp {
  object_ = AUTORELEASE([[JavaUtilLinkedList alloc] init]);
  class_ = JavaUtilLinkedList_class_();
}

- (void)testGetName {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  JavaLangReflectMethod *hashMethod = [class_ getMethod:@"hashCode"
                                         parameterTypes:nil];
  XCTAssertNotNil(hashMethod, @"hashMethod not found", nil);
  NSString *name = [hashMethod getName];
  XCTAssertEqualObjects(name, @"hashCode", @"wrong method name", nil);
}

- (void)testGetReturnType {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  JavaLangReflectMethod *sizeMethod = [class_ getMethod:@"size"
                                         parameterTypes:nil];
  XCTAssertNotNil(sizeMethod, @"sizeMethod not found", nil);
  IOSClass *returnType = [sizeMethod getReturnType];
  XCTAssertNotNil(returnType, @"no return type returned", nil);
  NSString *typeName = [returnType getName];
  XCTAssertEqualObjects(typeName, @"int", @"wrong return type returned", nil);
}

- (void)testInvocation {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  JavaLangReflectMethod *sizeMethod = [class_ getMethod:@"size"
                                         parameterTypes:nil];
  IOSObjectArray *parameters = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];
  id result = [sizeMethod invokeWithId:object_
                           withNSObjectArray:parameters];
  XCTAssertTrue([result isKindOfClass:[JavaLangInteger class]],
                @"incorrect type returned", nil);
  JavaLangInteger *integer = (JavaLangInteger *) result;
  XCTAssertEqual([integer intValue], 0, @"invalid result", nil);
}

static id invokeValueMethod(NSString *methodName) {
  JavaLangDouble *value =
      AUTORELEASE([[JavaLangDouble alloc] initWithDouble:defaultValue]);
  JavaLangReflectMethod *method = [JavaLangDouble_class_() getMethod:methodName parameterTypes:nil];
  IOSObjectArray *parameters = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];
  return [method invokeWithId:value
                  withNSObjectArray:parameters];
}

- (void)testByteReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"byteValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangByte class]],
               @"incorrect type returned", nil);
  JavaLangByte *b = (JavaLangByte *) result;
  XCTAssertEqual([b charValue], (char) 3, @"invalid result", nil);
}

- (void)testDoubleReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"doubleValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangDouble class]],
               @"incorrect type returned", nil);
  JavaLangDouble *d = (JavaLangDouble *) result;
  XCTAssertEqual([d doubleValue], 3.1416, @"invalid result", nil);
}

- (void)testFloatReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"floatValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangFloat class]],
               @"incorrect type returned", nil);
  JavaLangFloat *f = (JavaLangFloat *) result;
  XCTAssertEqual([f floatValue], 3.1416f, @"invalid result", nil);
}

- (void)testIntegerReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"intValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangInteger class]],
               @"incorrect type returned", nil);
  JavaLangInteger *i = (JavaLangInteger *) result;
  XCTAssertEqual([i intValue], 3, @"invalid result", nil);
}

- (void)testLongReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"longValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangLong class]],
               @"incorrect type returned", nil);
  JavaLangLong *l = (JavaLangLong *) result;
  XCTAssertEqual([l longLongValue], 3LL, @"invalid result", nil);
}

- (void)testShortReturn {
  if (ComGoogleJ2objcUtilReflectionUtil_isJreReflectionStripped()) {
    return;
  }
  id result = invokeValueMethod(@"shortValue");
  XCTAssertTrue([result isKindOfClass:[JavaLangShort class]],
               @"incorrect type returned", nil);
  JavaLangShort *s = (JavaLangShort *) result;
  XCTAssertEqual([s shortValue], (short) 3, @"invalid result", nil);
}

@end
