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
//  IOSReflection.h
//  JreEmulation
//
//  Created by Tom Ball on 9/23/13.
//

#ifndef JreEmulation_IOSReflection_h
#define JreEmulation_IOSReflection_h

#include "J2ObjC_common.h"
#include "java/lang/reflect/Modifier.h"
#include "java/lang/reflect/Type.h"

#import "objc/runtime.h"

@protocol JavaLangReflectType;
@class IOSClass;

// Current metadata structure version
#define J2OBJC_METADATA_VERSION 7

// A raw value is the union of all possible native types.
typedef union {
  void *asId;
  char asChar;
  unichar asUnichar;
  short asShort;
  int asInt;
  long long asLong;
  float asFloat;
  double asDouble;
  jboolean asBOOL;
} J2ObjcRawValue;

// C data structures that hold "raw" metadata for use by the methods that
// implement Java reflection. This information is necessary because not
// all information provided by the reflection API is discoverable via the
// Objective-C runtime.

typedef int16_t ptr_idx;

typedef struct J2ObjcMethodInfo {
  const char *selector;
  const char *returnType;
  uint16_t modifiers;
  ptr_idx javaNameIdx;
  ptr_idx paramsIdx;
  ptr_idx exceptionsIdx;
  ptr_idx genericSignatureIdx;
  ptr_idx annotationsIdx;
  ptr_idx paramAnnotationsIdx;
} J2ObjcMethodInfo;

typedef struct J2ObjcFieldInfo {
  const char *name;
  const char *type;
  J2ObjcRawValue constantValue;
  uint16_t modifiers;
  ptr_idx javaNameIdx;
  ptr_idx staticRefIdx;
  ptr_idx genericSignatureIdx;
  ptr_idx annotationsIdx;
} J2ObjcFieldInfo;

typedef struct J2ObjcClassInfo {
  const char *typeName;
  const char *packageName;
  const void **ptrTable;
  const J2ObjcMethodInfo *methods;
  const J2ObjcFieldInfo *fields;
  // Pointer types are above version for better packing.
  const uint16_t version;
  uint16_t modifiers;
  uint16_t methodCount;
  uint16_t fieldCount;
  ptr_idx enclosingClassIdx;
  ptr_idx innerClassesIdx;
  ptr_idx enclosingMethodIdx;
  ptr_idx genericSignatureIdx;
  ptr_idx annotationsIdx;
} J2ObjcClassInfo;

// An empty class info struct to be used by certain kinds of class objects like
// arrays and proxies.
FOUNDATION_EXPORT const J2ObjcClassInfo JreEmptyClassInfo;

CF_EXTERN_C_BEGIN

const J2ObjcClassInfo *JreFindMetadata(Class cls);
IOSClass *JreClassForString(const char *str);
IOSObjectArray *JreParseClassList(const char *listStr);
IOSClass *TypeToClass(id<JavaLangReflectType>);
Method JreFindInstanceMethod(Class cls, const char *name);
Method JreFindClassMethod(Class cls, const char *name);
struct objc_method_description *JreFindMethodDescFromList(
    SEL sel, struct objc_method_description *methods, unsigned int count);
struct objc_method_description *JreFindMethodDescFromMethodList(
    SEL sel, Method *methods, unsigned int count);
NSMethodSignature *JreSignatureOrNull(struct objc_method_description *methodDesc);
NSString *JreMetadataNameList(IOSObjectArray *classes);

__attribute__((always_inline)) inline const void *JrePtrAtIndex(const void **ptrTable, ptr_idx i) {
  return i < 0 ? NULL : ptrTable[i];
}

// J2ObjcClassInfo accessor functions.
NSString *JreClassTypeName(const J2ObjcClassInfo *metadata);
NSString *JreClassQualifiedName(const J2ObjcClassInfo *metadata);
NSString *JreClassPackageName(const J2ObjcClassInfo *metadata);

// Field and method lookup functions.
const J2ObjcFieldInfo *JreFindFieldInfo(const J2ObjcClassInfo *metadata, const char *fieldName);
const J2ObjcMethodInfo *JreFindMethodInfo(const J2ObjcClassInfo *metadata, NSString *methodName);

// J2ObjcMethodInfo accessor functions.
jboolean JreMethodIsConstructor(const J2ObjcMethodInfo *metadata);
NSString *JreMethodGenericString(const J2ObjcMethodInfo *metadata, const void **ptrTable);

__attribute__((always_inline)) inline const char *JreMethodJavaName(
    const J2ObjcMethodInfo *metadata, const void **ptrTable) {
  const char *javaName = (const char *)JrePtrAtIndex(ptrTable, metadata->javaNameIdx);
  return javaName ? javaName : metadata->selector;
}

__attribute__((always_inline)) inline jint JreMethodModifiers(const J2ObjcMethodInfo *metadata) {
  return metadata ? metadata->modifiers : JavaLangReflectModifier_PUBLIC;
}

// metadata must not be NULL.
__attribute__((always_inline)) inline SEL JreMethodSelector(const J2ObjcMethodInfo *metadata) {
  return sel_registerName(metadata->selector);
}

__attribute__((always_inline)) inline bool JreNullableCStrEquals(const char *a, const char *b) {
  return (a == NULL && b == NULL) || (a != NULL && b != NULL && strcmp(a, b) == 0);
}

CF_EXTERN_C_END

#endif // JreEmulation_IOSReflection_h
