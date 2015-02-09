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

#define JavaLangNumber_serialVersionUID -8742448824652078965LL

@implementation NSNumber (JavaNumber)

+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { "charValue", "byteValue", "B", 0x1, NULL },
    { "doubleValue", NULL, "D", 0x401, NULL },
    { "floatValue", NULL, "F", 0x401, NULL },
    { "intValue", NULL, "I", 0x401, NULL },
    { "longLongValue", "longValue", "J", 0x401, NULL },
    { "shortValue", NULL, "S", 0x401, NULL },
  };
  static J2ObjcFieldInfo fields[] = {
    { "serialVersionUID_", NULL, 0x1a, "J", NULL,
      .constantValue.asLong = JavaLangNumber_serialVersionUID },
  };
  static J2ObjcClassInfo _JavaLangNumber = {
    1, "Number", "java.lang", NULL, 0x401, 6, methods, 1, fields, 0, NULL
  };
  return &_JavaLangNumber;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSNumber)

// Empty class to force category to be loaded.
@implementation JreNumberCategoryDummy
@end
