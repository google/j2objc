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
#import "objc/message.h"

const J2ObjcClassInfo JreEmptyClassInfo = {
    NULL, NULL, NULL, NULL, NULL, J2OBJC_METADATA_VERSION, 0x0, 0, 0, -1, -1, -1, -1, -1 };

const J2ObjcClassInfo *JreFindMetadata(Class cls) {
  // Can't use respondsToSelector here because that will search superclasses.
  Method metadataMethod = cls ? JreFindClassMethod(cls, "__metadata") : NULL;
  if (metadataMethod) {
    const J2ObjcClassInfo *metadata = (const J2ObjcClassInfo *)method_invoke(cls, metadataMethod);
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
    @throw create_JavaLangAssertionError_initWithId_(
      [NSString stringWithFormat:@"invalid type from metadata %s", str]);
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
  if (!types) {
    return nil;
  }
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

NSString *JreMetadataNameList(IOSObjectArray *classes) {
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

jboolean JreMethodIsConstructor(const J2ObjcMethodInfo *metadata) {
  if (!metadata) {
    return NO;
  }
  const char *name = metadata->selector;
  return strcmp(name, "init") == 0 || strstr(name, "initWith") == name;
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
