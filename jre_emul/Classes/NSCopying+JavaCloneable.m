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

void NSCopying__init_class__() {
  static const J2ObjcClassInfo _NSCopying = {
    empty_static_initialize,
    NULL, NULL, NULL, 7, 0x609, 0, 0, -1, -1, -1, -1, -1 };
  
  JreBindIOSProtocol(@protocol(NSCopying), &_NSCopying, @"java.lang.Cloneable", 10);
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(NSCopying)

J2OBJC_NAME_MAPPING(NSCopying, "java.lang.Cloneable", "NSCopying")
