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
  NSUInteger typeLen = strlen(typeStr);
  if (typeLen >= 2) {
    if (*typeStr == '[') {
      IOSClass *componentType = (IOSClass *) JreTypeForString(typeStr + 1);
      return IOSClass_arrayOf(componentType);
    }

    // Extract type from string starting with a 'L' or 'T' and ending with ';'.
    NSString *typeName = [NSString stringWithUTF8String:typeStr];
    NSString *className = [typeName substringWithRange:NSMakeRange(1, typeLen - 2)];
    className = [className stringByReplacingOccurrencesOfString:@"/" withString:@"."];
    if (*typeStr == 'L') {
      return [IOSClass forName:className];
    }
    if (*typeStr == 'T') {
      return [JavaLangReflectTypeVariableImpl typeVariableWithName:className];
    }
  }
  NSString *msg = [NSString stringWithFormat:@"invalid type from metadata %s", typeStr];
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:msg]);
}

IOSClass *TypeToClass(id<JavaLangReflectType> type) {
  if (!type) {
    return nil;
  }
  if ([type isKindOfClass:[IOSClass class]]) {
    return (IOSClass *)type;
  }
  return NSObject_class_();
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

NSMethodSignature *JreSignatureOrNull(struct objc_method_description *methodDesc) {
  const char *types = methodDesc->types;
  // Some IOS devices crash instead of throwing an exception on struct type
  // encodings.
  const char *badChar = strchr(types, '{');
  if (badChar) {
    return nil;
  }
  @try {
    // Fails when non-ObjC types are included in the type encoding.
    return [NSMethodSignature signatureWithObjCTypes:types];
  }
  @catch (NSException *e) {
    return nil;
  }
}
