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

#import "J2ObjC_common.h"
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

// Use same data types that the translator generates.
typedef union J2ObjcConstantValue {
  jboolean boolean;
  char byte;
  unichar char_;
  double double_;
  float float_;
  int int_;
  long long long_;
  short short_;
  const char *string;
} J2ObjcConstantValue;

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
extern NSMethodSignature *JreSignatureOrNull(struct objc_method_description *methodDesc);

#endif // JreEmulation_IOSReflection_h
