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
#import "java/lang/ClassNotFoundException.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Method.h"
#import "objc/message.h"

// Suppress undeclared-selector warning to avoid incurring sel_registerName()
// overhead with every metadata lookup.
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"

const J2ObjcClassInfo JreEmptyClassInfo = {
    NULL, NULL, NULL, NULL, NULL, J2OBJC_METADATA_VERSION, 0x0, 0, 0, -1, -1, -1, -1, -1 };

const J2ObjcClassInfo *JreFindMetadata(Class cls) {
  // Can't use respondsToSelector here because that will search superclasses.
  Method metadataMethod = cls ? JreFindClassMethod(cls, @selector(__metadata)) : NULL;
  if (metadataMethod) {
    static J2ObjcClassInfo *(*method_invoke_metadata)(Class, Method) =
        (J2ObjcClassInfo * (*)(Class, Method)) method_invoke;
    const J2ObjcClassInfo *metadata = method_invoke_metadata(cls, metadataMethod);
    // We don't use any Java based assert or throwables here because this function is called during
    // IOSClass construction under mutual exclusion so causing any other IOSClass to be initialized
    // would result in deadlock.
    NSCAssert(metadata->version == J2OBJC_METADATA_VERSION,
        @"J2ObjC metadata is out-of-date, source must be re-translated.");
    return metadata;
  }
  return NULL;
}

// Parses the next IOSClass from the delimited string, advancing the c-string pointer past the
// parsed type.
static IOSClass *ParseNextClass(const char **strPtr) {
  const char c = *(*strPtr)++;
  if (c == '[') {
    return IOSClass_arrayOf(ParseNextClass(strPtr));
  } else if (c == 'L') {
    const char *delimitor = strchr(*strPtr, ';');
    NSString *name = [[NSString alloc] initWithBytes:*strPtr
                                              length:delimitor - *strPtr
                                             encoding:NSUTF8StringEncoding];
    *strPtr = delimitor + 1;
    IOSClass *result = [IOSClass classForIosName:name];
    [name release];
    return result;
  }
  IOSClass *primitiveType = [IOSClass primitiveClassForChar:c];
  if (primitiveType) {
    return primitiveType;
  }
  // Bad reflection data. Caller should throw AssertionError.
  return nil;
}

IOSClass *JreClassForString(const char * const str) {
  const char *ptr = str;
  IOSClass *result = ParseNextClass(&ptr);
  if (!result) {
    NSString *type = [NSString stringWithCString:str encoding:NSUTF8StringEncoding];
    size_t len = strlen(str);
    if (len > 2 && *str == 'L' && str[len - 1] == ';') {
      type = [type substringWithRange:NSMakeRange(1, len - 2)];
      @throw create_JavaLangClassNotFoundException_initWithNSString_(type);
    } else {
      @throw create_JavaLangAssertionError_initWithId_(
        [NSString stringWithFormat:@"invalid type from metadata %@", type]);
    }
  }
  return result;
}

IOSObjectArray *JreParseClassList(const char * const listStr) {
  if (!listStr) {
    return [IOSObjectArray arrayWithLength:0 type:IOSClass_class_()];
  }
  const char *ptr = listStr;
  NSMutableArray *builder = [NSMutableArray array];
  while (*ptr) {
    IOSClass *nextClass = ParseNextClass(&ptr);
    if (!nextClass) {
      @throw create_JavaLangAssertionError_initWithId_(
          [NSString stringWithFormat:@"invalid type list from metadata %s", listStr]);
    }
    [builder addObject:nextClass];
  }
  return [IOSObjectArray arrayWithNSArray:builder type:IOSClass_class_()];
}

Method JreFindInstanceMethod(Class cls, SEL selector) {
  unsigned int count;
  Method result = nil;
  Method *methods = class_copyMethodList(cls, &count);
  for (NSUInteger i = 0; i < count; i++) {
    if (selector == method_getName(methods[i])) {
      result = methods[i];
      break;
    }
  }
  free(methods);
  return result;
}

Method JreFindClassMethod(Class cls, SEL selector) {
  return JreFindInstanceMethod(object_getClass(cls), selector);
}

static NSString *MetadataNameList(IOSObjectArray *classes) {
  if (!classes || classes->size_ == 0) {
    return nil;
  }
  NSMutableString *str = [NSMutableString string];
  for (IOSClass *cls in classes) {
    if (!cls) {
      return @"";  // Won't match anything.
    }
    [cls appendMetadataName:str];
  }
  return str;
}

const J2ObjcFieldInfo *JreFindFieldInfo(const J2ObjcClassInfo *metadata, const char *fieldName) {
  if (metadata) {
    for (int i = 0; i < metadata->fieldCount; i++) {
      const J2ObjcFieldInfo *fieldInfo = &metadata->fields[i];
      const char *javaName = JrePtrAtIndex(metadata->ptrTable, fieldInfo->javaNameIdx);
      if (javaName && strcmp(fieldName, javaName) == 0) {
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

static bool NullableCStrEquals(const char *a, const char *b) {
  return (a == NULL && b == NULL) || (a != NULL && b != NULL && strcmp(a, b) == 0);
}

JavaLangReflectMethod *JreMethodWithNameAndParamTypes(
    IOSClass *iosClass, NSString *name, IOSObjectArray *paramTypes) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  const void **ptrTable = metadata->ptrTable;
  const char *cname = [name UTF8String];
  const char *cparams = [MetadataNameList(paramTypes) UTF8String];
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (methodInfo->returnType && strcmp(JreMethodJavaName(methodInfo, ptrTable), cname) == 0
        && NullableCStrEquals(JrePtrAtIndex(ptrTable, methodInfo->paramsIdx), cparams)) {
      return [JavaLangReflectMethod methodWithDeclaringClass:iosClass metadata:methodInfo];
    }
  }
  return nil;
}

JavaLangReflectConstructor *JreConstructorWithParamTypes(
    IOSClass *iosClass, IOSObjectArray *paramTypes) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  const void **ptrTable = metadata->ptrTable;
  const char *cparams = [MetadataNameList(paramTypes) UTF8String];
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (!methodInfo->returnType
        && NullableCStrEquals(JrePtrAtIndex(ptrTable, methodInfo->paramsIdx), cparams)) {
      return [JavaLangReflectConstructor constructorWithDeclaringClass:iosClass
                                                              metadata:methodInfo];
    }
  }
  return nil;
}

JavaLangReflectMethod *JreMethodForSelector(IOSClass *iosClass, SEL selector) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (selector == methodInfo->selector && methodInfo->returnType) {
      return [JavaLangReflectMethod methodWithDeclaringClass:iosClass metadata:methodInfo];
    }
  }
  return nil;
}

JavaLangReflectConstructor *JreConstructorForSelector(IOSClass *iosClass, SEL selector) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (selector == methodInfo->selector && !methodInfo->returnType) {
      return [JavaLangReflectConstructor constructorWithDeclaringClass:iosClass
                                                              metadata:methodInfo];
    }
  }
  return nil;
}

JavaLangReflectMethod *JreMethodWithNameAndParamTypesInherited(
    IOSClass *iosClass, NSString *name, IOSObjectArray *types) {
  JavaLangReflectMethod *method = JreMethodWithNameAndParamTypes(iosClass, name, types);
  if (method) {
    return method;
  }
  for (IOSClass *p in [iosClass getInterfacesInternal]) {
    method = JreMethodWithNameAndParamTypesInherited(p, name, types);
    if (method) {
      return method;
    }
  }
  IOSClass *superclass = [iosClass getSuperclass];
  return superclass ? JreMethodWithNameAndParamTypesInherited(superclass, name, types) : nil;
}

JavaLangReflectMethod *JreMethodForSelectorInherited(IOSClass *iosClass, SEL selector) {
  JavaLangReflectMethod *method = JreMethodForSelector(iosClass, selector);
  if (method) {
    return method;
  }
  for (IOSClass *p in [iosClass getInterfacesInternal]) {
    method = JreMethodForSelectorInherited(p, selector);
    if (method) {
      return method;
    }
  }
  IOSClass *superclass = [iosClass getSuperclass];
  return superclass ? JreMethodForSelectorInherited(superclass, selector) : nil;
}

NSString *JreMethodGenericString(const J2ObjcMethodInfo *metadata, const void **ptrTable) {
  const char *genericSig = metadata ? JrePtrAtIndex(ptrTable, metadata->genericSignatureIdx) : NULL;
  return genericSig ? [NSString stringWithUTF8String:genericSig] : nil;
}

static NSMutableString *BuildQualifiedName(const J2ObjcClassInfo *metadata) {
  if (!metadata) {
    return nil;
  }
  const char *enclosingClass = JrePtrAtIndex(metadata->ptrTable, metadata->enclosingClassIdx);
  if (enclosingClass) {
    NSMutableString *qName = BuildQualifiedName([JreClassForString(enclosingClass) getMetadata]);
    if (!qName) {
      return nil;
    }
    [qName appendString:@"$"];
    [qName appendString:[NSString stringWithUTF8String:metadata->typeName]];
    return qName;
  } else if (metadata->packageName) {
    NSMutableString *qName = [NSMutableString stringWithUTF8String:metadata->packageName];
    [qName appendString:@"."];
    [qName appendString:[NSString stringWithUTF8String:metadata->typeName]];
    return qName;
  } else {
    return [NSMutableString stringWithUTF8String:metadata->typeName];
  }
}

NSString *JreClassQualifiedName(const J2ObjcClassInfo *metadata) {
  return BuildQualifiedName(metadata);
}

JavaLangReflectField *FindDeclaredField(IOSClass *iosClass, NSString *name, jboolean publicOnly) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  const J2ObjcFieldInfo *fieldMeta = JreFindFieldInfo(metadata, [name UTF8String]);
  if (fieldMeta && (!publicOnly || (fieldMeta->modifiers & JavaLangReflectModifier_PUBLIC) != 0)) {
    Ivar ivar = class_getInstanceVariable(iosClass.objcClass, fieldMeta->name);
    return [JavaLangReflectField fieldWithIvar:ivar
                                     withClass:iosClass
                                  withMetadata:fieldMeta];
  }
  return nil;
}

JavaLangReflectField *FindField(IOSClass *iosClass, NSString *name, jboolean publicOnly) {
  while (iosClass) {
    JavaLangReflectField *field = FindDeclaredField(iosClass, name, publicOnly);
    if (field) {
      return field;
    }
    for (IOSClass *p in [iosClass getInterfacesInternal]) {
      JavaLangReflectField *field = FindField(p, name, publicOnly);
      if (field) {
        return field;
      }
    }
    iosClass = [iosClass getSuperclass];
  }
  return nil;
}

NSString *JreMetadataToString(const J2ObjcClassInfo *metadata) {
  NSMutableString *str = BuildQualifiedName(metadata);

  [str appendString:@" Fields:"];
  for (int i = 0; i < metadata->fieldCount; i++) {
    const J2ObjcFieldInfo *fieldInfo = &metadata->fields[i];
    const char *javaName = JrePtrAtIndex(metadata->ptrTable, fieldInfo->javaNameIdx);
    [str appendString:@" "];
    if (javaName) {
      [str appendString:[NSString stringWithUTF8String:javaName]];
    } else {
      [str appendString:[NSString stringWithUTF8String:fieldInfo->name]];
    }
  }

  [str appendString:@" Methods:"];
  const void **ptrTable = metadata->ptrTable;
  for (int i = 0; i < metadata->methodCount; i++) {
    [str appendString:@" "];
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    [str appendString:[NSString stringWithUTF8String:JreMethodJavaName(methodInfo, ptrTable)]];
  }

  return str;
}

#pragma clang diagnostic pop
