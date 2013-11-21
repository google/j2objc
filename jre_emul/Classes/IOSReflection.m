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
//  Created by Keith Stanger on Nov 12, 2013.
//

#import "IOSReflection.h"

#import "IOSClass.h"
#import "java/lang/AssertionError.h"
#import "java/lang/reflect/TypeVariableImpl.h"

id<JavaLangReflectType> JreTypeForString(const char *typeStr) {
  if (strlen(typeStr) == 1) {
    IOSClass *primitiveType = [IOSClass primitiveClassForChar:*typeStr];
    if (primitiveType) {
      return primitiveType;
    }
  }
  if (*typeStr == 'L') {
    return [IOSClass classForIosName:[NSString stringWithUTF8String:&typeStr[1]]];
  }
  if (*typeStr == 'T') {
    return [JavaLangReflectTypeVariableImpl typeVariableWithName:
        [NSString stringWithUTF8String:&typeStr[1]]];
  }
  NSString *msg = [NSString stringWithFormat:@"invalid type from metadata %s", typeStr];
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:msg]);
}

Method JreFindInstanceMethod(Class cls, const char *name) {
  unsigned int count;
  Method result = nil;
  Method *methods = class_copyMethodList(cls, &count);
  for (NSUInteger i = 0; i < count; i++) {
    if (strcmp(name, sel_getName(method_getName(methods[i]))) == 0) {
      result = methods[i];
      break;
    }
  }
  free(methods);
  return result;
}

Method JreFindClassMethod(Class cls, const char *name) {
  return JreFindInstanceMethod(object_getClass(cls), name);
}
