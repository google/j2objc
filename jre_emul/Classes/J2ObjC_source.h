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

// Common defines and includes needed by all J2ObjC source files.

#ifndef _J2OBJC_SOURCE_H_
#define _J2OBJC_SOURCE_H_

#import "J2ObjC_common.h"
#import "JavaObject.h"
#import "IOSClass.h"  // Type literal accessors.
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "IOSReflection.h"  // Metadata methods.
#import "NSCopying+JavaCloneable.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "jni.h"
#import <libkern/OSAtomic.h>  // OSMemoryBarrier used in initialize methods.

// Only expose this function to ARC generated code.
#if __has_feature(objc_arc)
FOUNDATION_EXPORT void JreRelease(id obj);
#endif

// Defined in JreEmulation.m
FOUNDATION_EXPORT id GetNonCapturingLambda(Class baseClass, NSString * blockClassName,
    SEL methodSelector, id block);
FOUNDATION_EXPORT id GetCapturingLambda(Class baseClass, NSString *blockClassName,
    SEL methodSelector, id block);

#define MOD_ASSIGN_DEFN(NAME, TYPE) \
  __attribute__((always_inline)) inline TYPE JreModAssign##NAME(TYPE *pLhs, jdouble rhs) { \
    return *pLhs = (TYPE) fmod(*pLhs, rhs); \
  }

MOD_ASSIGN_DEFN(Char, jchar)
MOD_ASSIGN_DEFN(Byte, jbyte)
MOD_ASSIGN_DEFN(Short, jshort)
MOD_ASSIGN_DEFN(Int, jint)
MOD_ASSIGN_DEFN(Long, jlong)
MOD_ASSIGN_DEFN(Float, jfloat)
MOD_ASSIGN_DEFN(Double, jdouble)
#undef MOD_ASSIGN_DEFN

#define SHIFT_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs << (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs >> (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShift##NAME(TYPE lhs, jlong rhs) { \
    return (TYPE) (((UTYPE) lhs) >> (rhs & MASK)); \
  }

#define SHIFT_ASSIGN_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs << (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs >> (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (((UTYPE) *pLhs) >> (rhs & MASK)); \
  }

// Shift masks are determined by the JLS spec, section 15.19.
SHIFT_OPERATORS_DEFN(32, jint, uint32_t, 0x1f)
SHIFT_OPERATORS_DEFN(64, jlong, uint64_t, 0x3f)
SHIFT_ASSIGN_OPERATORS_DEFN(Char, jchar, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Byte, jbyte, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Short, jshort, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Int, jint, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Long, jlong, uint64_t, 0x3f)
#undef SHIFT_OPERATORS_DEFN
#undef SHIFT_ASSIGN_OPERATORS_DEFN

/*!
 * Returns correct result when casting a double to an integral type. In C, a
 * float >= Integer.MAX_VALUE (allowing for rounding) returns 0x80000000,
 * while Java requires 0x7FFFFFFF.  A double >= Long.MAX_VALUE returns
 * 0x8000000000000000L, while Java requires 0x7FFFFFFFFFFFFFFFL.
 */
__attribute__((always_inline)) inline jint JreFpToInt(jdouble d) {
  jint tmp = (jint)d;
  return tmp == (jint)0x80000000 ? (d >= 0 ? 0x7FFFFFFF : tmp) : tmp;
}
__attribute__((always_inline)) inline jlong JreFpToLong(jdouble d) {
  jlong tmp = (jlong)d;
  return tmp == (jlong)0x8000000000000000LL ? (d >= 0 ? 0x7FFFFFFFFFFFFFFFL : tmp) : tmp;
}
__attribute__((always_inline)) inline jchar JreFpToChar(jdouble d) {
  unsigned tmp = (unsigned)d;
  return tmp > 0xFFFF || (tmp == 0 && d > 0) ? 0xFFFF : (jchar)tmp;
}

#endif  // _J2OBJC_SOURCE_H_
