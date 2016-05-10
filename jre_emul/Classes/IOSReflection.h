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
#define J2OBJC_METADATA_VERSION 2

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

typedef struct J2ObjcMethodInfo {
  const char *selector;
  const char *javaName;
  const char *returnType;
  uint16_t modifiers;
  const char *exceptions;
  const char *genericSignature;
} J2ObjcMethodInfo;

typedef struct J2ObjcFieldInfo {
  const char *name;
  const char *javaName;
  uint16_t modifiers;
  const char *type;
  const void *staticRef;
  const char *genericSignature;
  J2ObjcRawValue constantValue;
} J2ObjcFieldInfo;

typedef struct J2ObjCEnclosingMethodInfo {
  const char *typeName;
  const char *selector;
} J2ObjCEnclosingMethodInfo;

typedef struct J2ObjcClassInfo {
  const unsigned version;
  const char *typeName;
  const char *packageName;
  const char *enclosingName;
  uint16_t modifiers;
  uint16_t methodCount;
  const J2ObjcMethodInfo *methods;
  uint16_t fieldCount;
  const J2ObjcFieldInfo *fields;
  uint16_t superclassTypeArgsCount;
  const char **superclassTypeArgs;
  uint16_t innerClassCount;
  const char **innerClassnames;
  const J2ObjCEnclosingMethodInfo *enclosingMethod;
  const char *genericSignature;
} J2ObjcClassInfo;

// Autoboxing support.

extern id<JavaLangReflectType> JreTypeForString(const char *typeStr);
extern IOSClass *TypeToClass(id<JavaLangReflectType>);
extern Method JreFindInstanceMethod(Class cls, const char *name);
extern Method JreFindClassMethod(Class cls, const char *name);
extern struct objc_method_description *JreFindMethodDescFromList(
    SEL sel, struct objc_method_description *methods, unsigned int count);
extern struct objc_method_description *JreFindMethodDescFromMethodList(
    SEL sel, Method *methods, unsigned int count);
extern NSMethodSignature *JreSignatureOrNull(struct objc_method_description *methodDesc);

// J2ObjcClassInfo accessor functions.
extern NSString *JreClassTypeName(const J2ObjcClassInfo *metadata);
extern NSString *JreClassQualifiedName(const J2ObjcClassInfo *metadata);
extern NSString *JreClassPackageName(const J2ObjcClassInfo *metadata);
extern NSString *JreClassEnclosingName(const J2ObjcClassInfo *metadata);
extern NSString *JreClassGenericString(const J2ObjcClassInfo *metadata);
extern const J2ObjCEnclosingMethodInfo *JreEnclosingMethod(const J2ObjcClassInfo *metadata);
extern IOSObjectArray *JreClassInnerClasses(const J2ObjcClassInfo *metadata);

// Field and method lookup functions.
extern const J2ObjcFieldInfo *JreFindFieldInfo(
    const J2ObjcClassInfo *metadata, const char *fieldName);
extern const J2ObjcMethodInfo *JreFindMethodInfo(
    const J2ObjcClassInfo *metadata, NSString *methodName);

// J2ObjcMethodInfo accessor functions.
extern NSString *JreMethodName(const J2ObjcMethodInfo *metadata);
extern NSString *JreMethodJavaName(const J2ObjcMethodInfo *metadata);
extern NSString *JreMethodObjcName(const J2ObjcMethodInfo *metadata);
extern IOSObjectArray *JreMethodExceptionTypes(const J2ObjcMethodInfo *metadata);
extern jboolean JreMethodIsConstructor(const J2ObjcMethodInfo *metadata);
extern NSString *JreMethodGenericString(const J2ObjcMethodInfo *metadata);

__attribute__((always_inline)) inline jint JreMethodModifiers(const J2ObjcMethodInfo *metadata) {
  return metadata ? metadata->modifiers : JavaLangReflectModifier_PUBLIC;
}

// metadata must not be NULL.
__attribute__((always_inline)) inline SEL JreMethodSelector(const J2ObjcMethodInfo *metadata) {
  return sel_registerName(metadata->selector ? metadata->selector : metadata->javaName);
}

__attribute__((always_inline)) inline
id<JavaLangReflectType> JreMethodReturnType(const J2ObjcMethodInfo *metadata) {
  return metadata ? JreTypeForString(metadata->returnType) : nil;
}

// J2ObjCEnclosingMethodInfo accessor functions.
extern NSString *JreEnclosingMethodTypeName(const J2ObjCEnclosingMethodInfo *metadata);
extern NSString *JreEnclosingMethodSelector(const J2ObjCEnclosingMethodInfo *metadata);

#endif // JreEmulation_IOSReflection_h
