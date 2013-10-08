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
//  IOSReflection.m
//  JreEmulation
//
//  Created by Tom Ball on 10/8/13.
//

#import "IOSReflection.h"
#import "IOSClass.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/Short.h"
#import "java/lang/Void.h"

// Return a wrapper object for a value with a specified Obj-C type encoding.
id J2ObjcBoxValue(J2ObjcRawValue *value, const char *type) {
  if (strlen(type) == 1) {
    char typeChar = *type;
    switch (typeChar) {
      case '@':
        return (ARCBRIDGE id)value->asId;
      case '#':
        return [IOSClass classWithClass:(ARCBRIDGE Class)value];
      case 'c':
        return [JavaLangByte valueOfWithByte:value->asChar];
      case 'S':
        // A Java character is an unsigned two-byte int; in other words,
        // an unsigned short with an encoding of 'S'.
        return [JavaLangCharacter valueOfWithChar:value->asUnichar];
      case 's':
        return [JavaLangShort valueOfWithShort:value->asShort];
      case 'i':
        return [JavaLangInteger valueOfWithInt:value->asInt];
      case 'l':
      case 'L':
      case 'q':
      case 'Q':
        return [JavaLangLong valueOfWithLong:value->asLong];
      case 'f':
        return [JavaLangFloat valueOfWithFloat:value->asFloat];
      case 'd':
        return [JavaLangDouble valueOfWithDouble:value->asDouble];
      case 'B':
        return [JavaLangBoolean valueOfWithBoolean:value->asBOOL];
    }
  }
  id exception =
  [[JavaLangAssertionError alloc] initWithNSString:
   [NSString stringWithFormat:@"unknown Java type encoding: %s", type]];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
}

// Returns the native value from a possible wrapper object. The type
// parameter is used to differentiate from cases where a wrapper object
// is the expected type. For example, some methods may return a float
// and some may return a java.lang.Float -- the reflection code needs
// to work with both as java.lang.Float instances until the result is
// unboxed to the expected type.
void J2ObjcUnboxValue(id value, const char *type, J2ObjcRawValue *result) {
  char typeChar = *type;
  if (typeChar == 'B' && [value isKindOfClass:[JavaLangBoolean class]]) {
    result->asBOOL = [(JavaLangBoolean *) value booleanValue];
  } else if (typeChar == 'c' && [value isKindOfClass:[JavaLangByte class]]) {
    result->asChar = [(JavaLangByte *) value charValue];
  } else if (typeChar == 'S' && [value isKindOfClass:[JavaLangCharacter class]]) {
    result->asUnichar = [(JavaLangCharacter *) value charValue];
  } else if (typeChar == 's' && [value isKindOfClass:[JavaLangShort class]]) {
    result->asShort = [(JavaLangShort *) value shortValue];
  } else if (typeChar == 'i' && [value isKindOfClass:[JavaLangInteger class]]) {
    result->asInt = [(JavaLangInteger *) value intValue];
  } else if ((typeChar == 'l' || typeChar == 'L' || typeChar == 'q' || typeChar == 'Q') &&
             [value isKindOfClass:[JavaLangLong class]]) {
    result->asLong = [(JavaLangLong *) value longValue];
  } else if (typeChar == 'f' && [value isKindOfClass:[JavaLangFloat class]]) {
    result->asFloat = [(JavaLangFloat *) value floatValue];
  } else if (typeChar == 'd' && [value isKindOfClass:[JavaLangDouble class]]) {
    result->asDouble = [(JavaLangDouble *) value doubleValue];
  } else {
    // No unboxing needed.
    result->asId = value;
  }
}
