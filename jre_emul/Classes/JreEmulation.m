// Copyright 2011 Google Inc. All Rights Reserved.
//
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
//  JreEmulation.m
//  J2ObjC
//
//  Created by Tom Ball on 4/23/12.
//
//  Implements definitions from both J2ObjC_common.h and JreEmulation.h.

#import "JreEmulation.h"

#import "IOSClass.h"
#import "java/lang/AbstractStringBuilder.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/NullPointerException.h"
#import "java_lang_IntegralToString.h"
#import "java_lang_RealToString.h"

void JreThrowNullPointerException() {
  @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
}

void JreThrowClassCastException() {
  @throw AUTORELEASE([[JavaLangClassCastException alloc] init]);
}

#ifdef J2OBJC_COUNT_NIL_CHK
int j2objc_nil_chk_count = 0;
#endif

void JrePrintNilChkCount() {
#ifdef J2OBJC_COUNT_NIL_CHK
  printf("nil_chk count: %d\n", j2objc_nil_chk_count);
#endif
}

void JrePrintNilChkCountAtExit() {
  atexit(JrePrintNilChkCount);
}

static inline id JreStrongAssignInner(id *pIvar, NS_RELEASES_ARGUMENT id value) {
  [*pIvar autorelease];
  return *pIvar = value;
}

id JreStrongAssign(id *pIvar, id value) {
  return JreStrongAssignInner(pIvar, [value retain]);
}

id JreStrongAssignAndConsume(id *pIvar, NS_RELEASES_ARGUMENT id value) {
  return JreStrongAssignInner(pIvar, value);
}

// Method to handle dynamic creation of class wrappers surrounding blocks which come from lambdas
// not requiring a capture.
id GetNonCapturingLambda(Class baseClass, NSString *blockClassName, SEL methodSelector, id block) {
  // Ideally we would solve this with a singleton instead of a lookup, but Objective-C doesn't have
  // class variables, and we can't define static C variables dynamically.
  // TODO(kirbs): Refactor GetNonCapturingLambda for thread safety.  Right now dictionary
  // access is not threadsafe, but we will probably be moving away from a dictionary for lambda
  // lookup and storage.
  static NSMutableDictionary *nonCapturingBlockLookup = nil;
  static dispatch_once_t once;
  dispatch_once(&once, ^{
    nonCapturingBlockLookup = [[NSMutableDictionary alloc] init];
  });
  id lambdaObj = [nonCapturingBlockLookup objectForKey:blockClassName];
  if (lambdaObj != nil) {
    return lambdaObj;
  }
  Class blockClass = objc_allocateClassPair(baseClass, [blockClassName UTF8String], 0);
  Method method = class_getInstanceMethod(baseClass, methodSelector);
  const char *types = method_getTypeEncoding(method);
  IMP block_implementation = imp_implementationWithBlock(block);
  class_addMethod(blockClass, methodSelector, block_implementation, types);
  objc_registerClassPair(blockClass);
  lambdaObj = [[blockClass alloc] init];
  [nonCapturingBlockLookup setObject:lambdaObj forKeyedSubscript:blockClassName];
  return lambdaObj;
}

// Method to handle dynamic creation of class wrappers surrounding blocks from lambdas requiring
// a capture.
id GetCapturingLambda(Class baseClass, NSString *blockClassName, SEL methodSelector, id block) {
  // Ideally we would solve this by creating a variadic block or method which wraps an
  // underlying block instance variable, and creating instances of classes rather than entirely
  // new classes.  There are workarounds, but as far as I can tell this is going to have the
  // least code footprint, though it may be less performant.
  //
  // Alternatives
  // Create methods for each selector we need to use from a lambda.
  //  Comes with code cruft in the generated code.
  // Call all blocks with one array arg, and then unpack the array into variables.
  //  Comes with cruft in the generated blocks.
  //  Comes with memory overhead.
  //  Awkward handling of non-id types.
  // We could lookup the number of arguments in the functional method, and have an array of
  // matching block wrappers.
  //  Need explicit blocks for all distinct argument lengths we support.
  //  Probably more performant than creating a new class for every lambda instance.
  static int lambdaCount = 0;
  blockClassName = [NSString stringWithFormat:@"%@_%d",blockClassName,lambdaCount++];
  Class blockClass = objc_allocateClassPair(baseClass, [blockClassName UTF8String], 0);
  IMP block_implementation = imp_implementationWithBlock(block);
  Method method = class_getInstanceMethod(baseClass, methodSelector);
  const char *types = method_getTypeEncoding(method);
  class_addMethod(blockClass, methodSelector, block_implementation, types);
  objc_registerClassPair(blockClass);
  return [[blockClass alloc] init];
}

// Converts main() arguments into an IOSObjectArray of NSStrings.  The first
// argument, the program name, is skipped so the returned array matches what
// is passed to a Java main method.
FOUNDATION_EXPORT
    IOSObjectArray *JreEmulationMainArguments(int argc, const char *argv[]) {
  IOSClass *stringType = NSString_class_();
  if (argc <= 1) {
    return [IOSObjectArray arrayWithLength:0 type:stringType];
  }
  IOSObjectArray *args = [IOSObjectArray arrayWithLength:argc - 1 type:stringType];
  for (int i = 1; i < argc; i++) {
    NSString *arg =
        [NSString stringWithCString:argv[i]
                           encoding:[NSString defaultCStringEncoding]];
    IOSObjectArray_Set(args, i - 1, arg);
  }
  return args;
}

// Counts the number of object types in a string concatenation.
static NSUInteger CountObjectArgs(const char *types) {
  NSUInteger numObjs = 0;
  while (*types) {
    if (*(types++) == '@') numObjs++;
  }
  return numObjs;
}

// Computes the capacity for the buffer.
static jint ComputeCapacity(const char *types, va_list va, NSString **objDescriptions) {
  jint capacity = 0;
  while (*types) {
    switch(*types) {
      case 'C':
        capacity++;
        va_arg(va, jint);
        break;
      case 'D':
        capacity += 24;  // Determined experimentally.
        va_arg(va, jdouble);
        break;
      case 'F':
        capacity += 15;  // Determined experimentally.
        va_arg(va, jdouble);
        break;
      case 'B':
        capacity += 4;
        va_arg(va, jint);
        break;
      case 'S':
        capacity += 6;
        va_arg(va, jint);
        break;
      case 'I':
        capacity += 11;
        va_arg(va, jint);
        break;
      case 'J':
        capacity += 20;
        va_arg(va, jlong);
        break;
      case 'Z':
        capacity += (jboolean)va_arg(va, jint) ? 4 : 5;
        break;
      case '$':
        {
          NSString *str = va_arg(va, NSString *);
          capacity += str ? CFStringGetLength((CFStringRef)str) : 4;
        }
        break;
      case '@':
        {
          id obj = va_arg(va, id);
          if (obj) {
            NSString *description = [obj description];
            *(objDescriptions++) = description;
            capacity += CFStringGetLength((CFStringRef)description);
          } else {
            *(objDescriptions++) = nil;
            capacity += 4;
          }
        }
        break;
    }
    types++;
  }
  return capacity;
}

static void AppendArgs(
    const char *types, va_list va, NSString **objDescriptions, JreStringBuilder *sb) {
  while (*types) {
    switch (*types) {
      case 'C':
        JreStringBuilder_appendChar(sb, (jchar)va_arg(va, jint));
        break;
      case 'D':
        RealToString_appendDouble(sb, va_arg(va, jdouble));
        break;
      case 'F':
        RealToString_appendFloat(sb, (jfloat)va_arg(va, jdouble));
        break;
      case 'B':
      case 'I':
      case 'S':
        IntegralToString_convertInt(sb, va_arg(va, jint));
        break;
      case 'J':
        IntegralToString_convertLong(sb, va_arg(va, jlong));
        break;
      case 'Z':
        JreStringBuilder_appendString(sb, (jboolean)va_arg(va, jint) ? @"true" : @"false");
        break;
      case '$':
        JreStringBuilder_appendString(sb, va_arg(va, NSString *));
        break;
      case '@':
        va_arg(va, id);
        JreStringBuilder_appendString(sb, *(objDescriptions++));
        break;
    }
    types++;
  }
}

NSString *JreStrcat(const char *types, ...) {
  NSString *objDescriptions[CountObjectArgs(types)];
  va_list va;
  va_start(va, types);
  jint capacity = ComputeCapacity(types, va, objDescriptions);
  va_end(va);

  // Create a string builder and fill it.
  JreStringBuilder sb;
  JreStringBuilder_initWithCapacity(&sb, capacity);
  va_start(va, types);
  AppendArgs(types, va, objDescriptions, &sb);
  va_end(va);
  return JreStringBuilder_toStringAndDealloc(&sb);
}

id JreStrAppendInner(id lhs, const char *types, va_list va) {
  va_list va_capacity;
  va_copy(va_capacity, va);
  NSString *objDescriptions[CountObjectArgs(types)];

  jint capacity = ComputeCapacity(types, va_capacity, objDescriptions);
  va_end(va_capacity);

  NSString *lhsDescription = nil;
  if (lhs) {
    lhsDescription = [lhs description];
    capacity += CFStringGetLength((CFStringRef)lhsDescription);
  } else {
    capacity += 4;
  }

  JreStringBuilder sb;
  JreStringBuilder_initWithCapacity(&sb, capacity);
  JreStringBuilder_appendString(&sb, lhsDescription);
  AppendArgs(types, va, objDescriptions, &sb);

  return JreStringBuilder_toStringAndDealloc(&sb);
}

id JreStrAppend(id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(*lhs, types, va);
  va_end(va);
  return *lhs = result;
}

id JreStrAppendStrong(id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(*lhs, types, va);
  va_end(va);
  return JreStrongAssign(lhs, result);
}

id JreStrAppendArray(JreArrayRef lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(*lhs.pValue, types, va);
  va_end(va);
  return IOSObjectArray_SetRef(lhs, result);
}

FOUNDATION_EXPORT void JreRelease(id obj) {
  [obj release];
}
