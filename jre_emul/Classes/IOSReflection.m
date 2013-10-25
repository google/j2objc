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
