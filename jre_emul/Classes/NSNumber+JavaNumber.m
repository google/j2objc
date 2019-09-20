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
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "B", 0x1, 0, -1, -1, -1, -1, -1 },
    { NULL, "D", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "F", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "J", 0x401, 1, -1, -1, -1, -1, -1 },
    { NULL, "S", 0x1, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(charValue);
  methods[2].selector = @selector(doubleValue);
  methods[3].selector = @selector(floatValue);
  methods[4].selector = @selector(intValue);
  methods[5].selector = @selector(longLongValue);
  methods[6].selector = @selector(shortValue);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "serialVersionUID", "J", .constantValue.asLong = NSNumber_serialVersionUID, 0x1a, -1, -1, -1,
      -1 },
  };
  static const void *ptrTable[] = { "byteValue", "longValue" };
  static const J2ObjcClassInfo _NSNumber = {
    "Number", "java.lang", ptrTable, methods, fields, 7, 0x401, 7, 1, -1, -1, -1, -1, -1 };
  return &_NSNumber;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSNumber)

J2OBJC_NAME_MAPPING(NSNumber, "java.lang.Number", "NSNumber")

// Empty class to force category to be loaded.
@implementation JreNumberCategoryDummy
@end
