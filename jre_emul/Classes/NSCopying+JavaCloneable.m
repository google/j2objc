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
//  NSCopying+JavaCloneable.m
//  JreEmulation
//
//  Created by Keith Stanger on Jan 13, 2015.
//

#include "NSCopying+JavaCloneable.h"

#include "J2ObjC_source.h"

@implementation NSCopying

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcClassInfo _NSCopying = {
    1, "Cloneable", "java.lang", NULL, 0x201, 0, NULL, 0, NULL, 0, NULL
  };
  return &_NSCopying;
}

@end

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(NSCopying)
