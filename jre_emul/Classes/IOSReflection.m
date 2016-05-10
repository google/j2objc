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
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:msg]);
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

struct objc_method_description *JreFindMethodDescFromList(
    SEL sel, struct objc_method_description *methods, unsigned int count) {
  for (unsigned int i = 0; i < count; i++) {
    if (sel == methods[i].name) {
      return &methods[i];
    }
  }
  return NULL;
}

struct objc_method_description *JreFindMethodDescFromMethodList(
    SEL sel, Method *methods, unsigned int count) {
  for (unsigned int i = 0; i < count; i++) {
    struct objc_method_description *desc = method_getDescription(methods[i]);
    if (sel == desc->name) {
      return desc;
    }
  }
  return NULL;
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

const J2ObjcFieldInfo *JreFindFieldInfo(const J2ObjcClassInfo *metadata, const char *fieldName) {
  if (metadata) {
    for (int i = 0; i < metadata->fieldCount; i++) {
      const J2ObjcFieldInfo *fieldInfo = &metadata->fields[i];
      if (fieldInfo->javaName && strcmp(fieldName, fieldInfo->javaName) == 0) {
        return fieldInfo;
      }
      if (strcmp(fieldName, fieldInfo->name) == 0) {
        return fieldInfo;
      }
      // See if field name has trailing underscore added.
      size_t max  = strlen(fieldInfo->name) - 1;
      if (fieldInfo->name[max] == '_' && strlen(fieldName) == max &&
          strncmp(fieldName, fieldInfo->name, max) == 0) {
        return fieldInfo;
      }
    }
  }
  return NULL;
}

NSString *JreClassTypeName(const J2ObjcClassInfo *metadata) {
  return metadata ? [NSString stringWithUTF8String:metadata->typeName] : nil;
}

NSString *JreClassPackageName(const J2ObjcClassInfo *metadata) {
  return metadata && metadata->packageName
      ? [NSString stringWithUTF8String:metadata->packageName] : nil;
}

NSString *JreClassEnclosingName(const J2ObjcClassInfo *metadata) {
  return metadata && metadata->enclosingName
      ? [NSString stringWithUTF8String:metadata->enclosingName] : nil;
}

NSString *JreClassGenericString(const J2ObjcClassInfo *metadata) {
  return metadata && metadata->genericSignature
      ? [NSString stringWithUTF8String:metadata->genericSignature] : nil;
}

const J2ObjcMethodInfo *JreFindMethodInfo(const J2ObjcClassInfo *metadata, NSString *methodName) {
  if (!metadata) {
    return NULL;
  }
  const char *name = [methodName UTF8String];
  for (int i = 0; i < metadata->methodCount; i++) {
    if (strcmp(name, metadata->methods[i].selector) == 0) {
      return &metadata->methods[i];
    }
  }
  return nil;
}

IOSObjectArray *JreClassInnerClasses(const J2ObjcClassInfo *metadata) {
  if (!metadata || metadata->innerClassCount == 0) {
    return nil;
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:metadata->innerClassCount
                                                      type:JavaLangReflectType_class_()];
  for (int i = 0; i < metadata->innerClassCount; i++) {
    IOSObjectArray_Set(result, i, JreTypeForString(metadata->innerClassnames[i]));
  }
  return result;
}

NSString *JreMethodName(const J2ObjcMethodInfo *metadata) {
  return metadata ? (metadata->javaName ? [NSString stringWithUTF8String:metadata->javaName]
                     : [NSString stringWithUTF8String:metadata->selector]) : nil;
}

NSString *JreMethodJavaName(const J2ObjcMethodInfo *metadata) {
  return metadata && metadata->javaName ? [NSString stringWithUTF8String:metadata->javaName] : nil;
}

NSString *JreMethodObjcName(const J2ObjcMethodInfo *metadata) {
  return metadata ? [NSString stringWithUTF8String:metadata->selector] : nil;
}

jboolean JreMethodIsConstructor(const J2ObjcMethodInfo *metadata) {
  if (!metadata) {
    return NO;
  }
  const char *name = metadata->selector;
  return strcmp(name, "init") == 0 || strstr(name, "initWith") == name;
}

IOSObjectArray *JreMethodExceptionTypes(const J2ObjcMethodInfo *metadata) {
  if (!metadata || !metadata->exceptions) {
    return nil;
  }

  const char *p = metadata->exceptions;
  int n = 0;
  while (p != NULL) {
    const char *semi = strchr(p, ';');
    if (semi != NULL) {
      ++n;
      p = semi + 1;
    } else {
      p = NULL;
    }
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:(jint)n
                                                      type:JavaLangReflectType_class_()];
  jint count = 0;
  p = metadata->exceptions;
  while (p != NULL) {
    char *semi = strchr(p, ';');
    if (semi != NULL) {
      char *exc = strndup(p, semi - p + 1);  // Include trailing ';'.
      IOSObjectArray_Set(result, count++, JreTypeForString(exc));
      free(exc);
      p = semi + 1;
    } else {
      p = NULL;
    }
  }
  return result;
}

NSString *JreMethodGenericString(const J2ObjcMethodInfo *metadata) {
  return metadata && metadata->genericSignature
      ? [NSString stringWithUTF8String:metadata->genericSignature] : nil;
}


const J2ObjCEnclosingMethodInfo *JreEnclosingMethod(const J2ObjcClassInfo *metadata) {
  return metadata ? metadata->enclosingMethod : NULL;
}

NSString *JreEnclosingMethodTypeName(const J2ObjCEnclosingMethodInfo *metadata) {
  return metadata ? [NSString stringWithUTF8String:metadata->typeName] : nil;
}

NSString *JreEnclosingMethodSelector(const J2ObjCEnclosingMethodInfo *metadata) {
  return metadata ? [NSString stringWithUTF8String:metadata->selector] : nil;
}


NSString *JreClassQualifiedName(const J2ObjcClassInfo *metadata) {
  if (!metadata) {
    return nil;
  }
  NSMutableString *qName = [NSMutableString string];
  NSString *packageName = JreClassPackageName(metadata);
  NSString *enclosingName = JreClassEnclosingName(metadata);
  if ([packageName length] > 0) {
    [qName appendString:packageName];
    [qName appendString:@"."];
  }
  if (enclosingName) {
    [qName appendString:enclosingName];
    [qName appendString:@"$"];
  }
  [qName appendString:JreClassTypeName(metadata)];
  return qName;
}
