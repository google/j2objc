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

#import <SenTestingKit/SenTestingKit.h>
#import "IOSClass.h"
#import "java/lang/Byte.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/Short.h"
#import "java/util/LinkedList.h"

static double defaultValue = 3.1416;

@interface JavaLangReflectMethodTest : SenTestCase {
 @private
  JavaUtilLinkedList *object_;
  IOSClass *class_;
}
@end


@implementation JavaLangReflectMethodTest

- (void)setUp {
  object_ = [[[JavaUtilLinkedList alloc] init] autorelease];
  class_ = [IOSClass classWithClass:[object_ class]];
}

- (void)testGetName {
  JavaLangReflectMethod *hashMethod = [class_ getMethod:@"hash"
                                         parameterTypes:nil];
  STAssertNotNil(hashMethod, @"hashMethod not found", nil);
  NSString *name = [hashMethod getName];
  STAssertEqualObjects(name, @"hash", @"wrong method name", nil);
}

- (void)testGetReturnType {
  JavaLangReflectMethod *sizeMethod = [class_ getMethod:@"size"
                                         parameterTypes:nil];
  STAssertNotNil(sizeMethod, @"sizeMethod not found", nil);
  IOSClass *returnType = [sizeMethod getReturnType];
  STAssertNotNil(returnType, @"no return type returned", nil);
  NSString *typeName = [returnType getName];
  STAssertEqualObjects(typeName, @"JavaLangInteger", 
                       @"wrong return type returned", nil);
}

- (void)testInvocation {
  JavaLangReflectMethod *sizeMethod = [class_ getMethod:@"size"
                                         parameterTypes:nil];
  IOSClass *objectType = [IOSClass classWithClass:[NSObject class]];
  IOSObjectArray *parameters =
      [[[IOSObjectArray alloc] initWithLength:0 type:objectType] autorelease];
  id result = [sizeMethod invokeWithId:object_
                           withNSObjectArray:parameters];
  STAssertTrue([result isKindOfClass:[JavaLangInteger class]],
                @"incorrect type returned", nil);
  JavaLangInteger *integer = (JavaLangInteger *) result;
  STAssertEquals([integer intValue], 0, @"invalid result", nil);
}

static id invokeValueMethod(NSString *methodName) {
  JavaLangDouble *value =
      [[[JavaLangDouble alloc] initWithDouble:defaultValue] autorelease];
  IOSClass *doubleClass = [IOSClass classWithClass:[value class]];
  JavaLangReflectMethod *method = [doubleClass getMethod:methodName
                                          parameterTypes:nil];
  IOSClass *objectType = [IOSClass classWithClass:[NSObject class]];
  IOSObjectArray *parameters =
      [[[IOSObjectArray alloc] initWithLength:0
                                         type:objectType] autorelease];
  return [method invokeWithId:value
                  withNSObjectArray:parameters];
}

- (void)testByteReturn {
  id result = invokeValueMethod(@"byteValue");
  STAssertTrue([result isKindOfClass:[JavaLangByte class]],
               @"incorrect type returned", nil);
  JavaLangByte *b = (JavaLangByte *) result;
  STAssertEquals([b byteValue], (char) 3, @"invalid result", nil);
}

- (void)testDoubleReturn {
  id result = invokeValueMethod(@"doubleValue");
  STAssertTrue([result isKindOfClass:[JavaLangDouble class]],
               @"incorrect type returned", nil);
  JavaLangDouble *d = (JavaLangDouble *) result;
  STAssertEquals([d doubleValue], 3.1416, @"invalid result", nil);
}

- (void)testFloatReturn {
  id result = invokeValueMethod(@"floatValue");
  STAssertTrue([result isKindOfClass:[JavaLangFloat class]],
               @"incorrect type returned", nil);
  JavaLangFloat *f = (JavaLangFloat *) result;
  STAssertEquals([f floatValue], 3.1416f, @"invalid result", nil);
}

- (void)testIntegerReturn {
  id result = invokeValueMethod(@"intValue");
  STAssertTrue([result isKindOfClass:[JavaLangInteger class]],
               @"incorrect type returned", nil);
  JavaLangInteger *i = (JavaLangInteger *) result;
  STAssertEquals([i intValue], 3, @"invalid result", nil);
}

- (void)testLongReturn {
  id result = invokeValueMethod(@"longLongValue");
  STAssertTrue([result isKindOfClass:[JavaLangLong class]],
               @"incorrect type returned", nil);
  JavaLangLong *l = (JavaLangLong *) result;
  STAssertEquals([l longLongValue], 3LL, @"invalid result", nil);
}

- (void)testShortReturn {
  id result = invokeValueMethod(@"shortValue");
  STAssertTrue([result isKindOfClass:[JavaLangShort class]],
               @"incorrect type returned", nil);
  JavaLangShort *s = (JavaLangShort *) result;
  STAssertEquals([s shortValue], (short) 3, @"invalid result", nil);
}

@end
