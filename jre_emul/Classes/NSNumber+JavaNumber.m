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
//  NSNumber+JavaNumber.m
//  JreEmulation
//
//  Created by Tom Ball on 12/6/13.
//

#import "NSNumber+JavaNumber.h"

#import "J2ObjC_source.h"

#define NSNumber_serialVersionUID -8742448824652078965LL

@implementation NSNumber (JavaNumber)

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "init", NULL, 0x1, -1, -1, -1 },
    { "charValue", "B", 0x1, 0, -1, -1 },
    { "doubleValue", "D", 0x401, -1, -1, -1 },
    { "floatValue", "F", 0x401, -1, -1, -1 },
    { "intValue", "I", 0x401, -1, -1, -1 },
    { "longLongValue", "J", 0x401, 1, -1, -1 },
    { "shortValue", "S", 0x1, -1, -1, -1 },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "serialVersionUID", "J", .constantValue.asLong = NSNumber_serialVersionUID, 0x1a, -1, -1, -1
    },
  };
  static const void *ptrTable[] = { "byteValue", "longValue" };
  static const J2ObjcClassInfo _NSNumber = {
    3, "Number", "java.lang", NULL, 0x401, 7, methods, 1, fields, 0, NULL, 0, NULL, NULL, NULL,
    ptrTable };
  return &_NSNumber;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSNumber)

// Empty class to force category to be loaded.
@implementation JreNumberCategoryDummy
@end
