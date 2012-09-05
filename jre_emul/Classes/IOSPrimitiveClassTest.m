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
//  IOSPrimitiveClassTest.m
//  JreEmulation
//
//  Created by Tom Ball on 1/23/12.
//

#import "IOSPrimitiveClass.h"
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSShortArray.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/Short.h"
#import <SenTestingKit/SenTestingKit.h>

// Unit tests for IOSPrimitiveClass.
@interface IOSPrimitiveClassTest : SenTestCase {
}

@end

@implementation IOSPrimitiveClassTest

- (void)testBooleanType {
  NSString *objectTypeName = [[[JavaLangBoolean getTRUE] getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangBoolean", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangBoolean TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"boolean", 
                       @"incorrect primitive type name");
  NSString *arrayTypeName =
      [[[[IOSBooleanArray alloc] initWithLength:0] getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[Z", @"incorrect array type name");
}

- (void)testByteType {
  IOSClass *javaByteClass = [[JavaLangByte valueOfWithChar:42] getClass];
  NSString *objectTypeName = [javaByteClass getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangByte", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangByte TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"byte", 
                       @"incorrect primitive type name");
  IOSByteArray *byteArray = [[IOSByteArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[byteArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[B", @"incorrect array type name");
}

- (void)testCharType {
  JavaLangCharacter *javaCharacter = [JavaLangCharacter valueOfWithUnichar:'x'];
  NSString *objectTypeName = [[javaCharacter getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangCharacter", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangCharacter TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"char", 
                       @"incorrect primitive type name");
  IOSCharArray *charArray = [[IOSCharArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[charArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[C", @"incorrect array type name");
}

- (void)testDoubleType {
  JavaLangDouble *javaDouble = [JavaLangDouble valueOfWithDouble:1.2];
  NSString *objectTypeName = [[javaDouble getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangDouble", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangDouble TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"double", 
                       @"incorrect primitive type name");
  IOSDoubleArray *doubleArray = [[IOSDoubleArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[doubleArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[D", @"incorrect array type name");
}

- (void)testFloatType {
  JavaLangFloat *javaFloat = [JavaLangFloat valueOfWithFloat:3.4f];
  NSString *objectTypeName = [[javaFloat getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangFloat", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangFloat TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"float", 
                       @"incorrect primitive type name");
  IOSFloatArray *floatArray = [[IOSFloatArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[floatArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[F", @"incorrect array type name");
}

- (void)testIntType {
  JavaLangInteger *javaInteger = [JavaLangInteger valueOfWithInt:42];
  NSString *objectTypeName = [[javaInteger getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangInteger", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangInteger TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"int", 
                       @"incorrect primitive type name");
  IOSIntArray *intArray = [[IOSIntArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[intArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[I", @"incorrect array type name");
}

- (void)testLongType {
  JavaLangLong *javaLong = [JavaLangLong valueOfWithLongInt:42LL];
  NSString *objectTypeName = [[javaLong getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangLong", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangLong TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"long", 
                       @"incorrect primitive type name");
  IOSLongArray *longArray = [[IOSLongArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[longArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[J", @"incorrect array type name");
}

- (void)testShortType {
  JavaLangShort *javaShort = [JavaLangShort valueOfWithShortInt:42];
  NSString *objectTypeName = [[javaShort getClass] getName];
  STAssertEqualObjects(objectTypeName, @"JavaLangShort", 
                       @"incorrect object type name");
  NSString *primitiveTypeName = [[JavaLangShort TYPE] getName];
  STAssertEqualObjects(primitiveTypeName, @"short", 
                       @"incorrect primitive type name");
  IOSShortArray *shortArray = [[IOSShortArray alloc] initWithLength:0];
  NSString *arrayTypeName = [[shortArray getClass] getName];
  STAssertEqualObjects(arrayTypeName, @"[S", @"incorrect array type name");
}

@end
