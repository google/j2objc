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

// Common definitions needed by all J2ObjC generated files.

#ifndef _J2OBJC_COMMON_H_
#define _J2OBJC_COMMON_H_

#import <Foundation/Foundation.h>

@class IOSClass;

#ifndef __has_feature
#define __has_feature(x) 0  // Compatibility with non-clang compilers.
#endif

# ifndef OBJC_METHOD_FAMILY_NONE
#  if __has_attribute(objc_method_family)
#   define OBJC_METHOD_FAMILY_NONE __attribute__((objc_method_family(none)))
#  else
#   define OBJC_METHOD_FAMILY_NONE
#  endif
# endif

# if __has_feature(objc_arc)
#  define ARCBRIDGE __bridge
#  define ARCBRIDGE_TRANSFER __bridge_transfer
#  define ARC_CONSUME_PARAMETER __attribute((ns_consumed))
#  define AUTORELEASE(x) x
#  define RELEASE_(x) x
#  define RETAIN_(x) x
#  define RETAIN_AND_AUTORELEASE(x) x
# else
#  define ARCBRIDGE
#  define ARCBRIDGE_TRANSFER
#  define ARC_CONSUME_PARAMETER
#  define AUTORELEASE(x) [x autorelease]
#  define RELEASE_(x) [x release]
#  define RETAIN_(x) [x retain]
#  define RETAIN_AND_AUTORELEASE(x) [[x retain] autorelease]
# endif

#define J2OBJC_COMMA() ,

#ifdef J2OBJC_DISABLE_ALL_CHECKS
 #define J2OBJC_DISABLE_NIL_CHECKS 1
 #define J2OBJC_DISABLE_CAST_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_CHECKS 1
#endif

#ifdef J2OBJC_DISABLE_ARRAY_CHECKS
 #define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_TYPE_CHECKS 1
#endif

// Typedefs for each of Java's primitive types. (as defined in jni.h)
// jboolean and jbyte are modified from jni.h to integrate better with
// Objective-C code.
typedef BOOL            jboolean;
typedef char            jbyte;          /* signed 8 bits */
typedef uint16_t        jchar;          /* unsigned 16 bits */
typedef int16_t         jshort;         /* signed 16 bits */
typedef int32_t         jint;           /* signed 32 bits */
typedef int64_t         jlong;          /* signed 64 bits */
typedef float           jfloat;         /* 32-bit IEEE 754 */
typedef double          jdouble;        /* 64-bit IEEE 754 */

CF_EXTERN_C_BEGIN

void JreThrowNullPointerException() __attribute__((noreturn));
void JreThrowClassCastException() __attribute__((noreturn));

#ifdef J2OBJC_COUNT_NIL_CHK
int j2objc_nil_chk_count;
#endif

void JrePrintNilChkCount();
void JrePrintNilChkCountAtExit();

NSString *JreStrcat(const char *types, ...);

CF_EXTERN_C_END

// Marked as unused to avoid a clang warning when this file is included
// but NIL_CHK isn't used.
__attribute__ ((unused)) static inline id nil_chk(id __unsafe_unretained p) {
#ifdef J2OBJC_COUNT_NIL_CHK
  j2objc_nil_chk_count++;
#endif
#if !defined(J2OBJC_DISABLE_NIL_CHECKS)
  if (__builtin_expect(!p, 0)) {
    JreThrowNullPointerException();
  }
#endif
  return p;
}

// Separate methods for class and protocol cast checks are used to reduce
// overhead, since the difference is statically known.
__attribute__ ((unused)) static inline id check_class_cast(id __unsafe_unretained p, Class clazz) {
#if !defined(J2OBJC_DISABLE_CAST_CHECKS)
  if (__builtin_expect(p && ![p isKindOfClass:clazz], 0)) {
    JreThrowClassCastException();
  }
#endif
  return p;
}

__attribute__ ((unused)) static inline id check_protocol_cast(id __unsafe_unretained p,
                                                              Protocol *protocol) {
#if !defined(J2OBJC_DISABLE_CAST_CHECKS)
  if (__builtin_expect(p && ![p conformsToProtocol:protocol], 0)) {
    JreThrowClassCastException();
  }
#endif
  return p;
}

// Should only be used with manual reference counting.
#if !__has_feature(objc_arc)
static inline id JreStrongAssignInner(id *pIvar, id self, NS_RELEASES_ARGUMENT id value) {
  if (*pIvar != self) {
    [*pIvar autorelease];
  }
  *pIvar = value;
  return value;
}

static inline id JreStrongAssign(id *pIvar, id self, id value) {
  if (value != self) {
    [value retain];
  }
  return JreStrongAssignInner(pIvar, self, value);
}

static inline id JreStrongAssignAndConsume(id *pIvar, id self, NS_RELEASES_ARGUMENT id value) {
  if (value == self) {
    [value autorelease];
  }
  return JreStrongAssignInner(pIvar, self, value);
}
#endif

/*!
 * Defines the initialized flag for a class.
 *
 * @define J2OBJC_INITIALIZED_DEFN
 * @param CLASS The class for which the initialized flag is defined.
 */
#define J2OBJC_INITIALIZED_DEFN(CLASS) \
  _Atomic(BOOL) CLASS##_initialized = NO;

/*!
 * Defines the code to set a class's initialized flag. This should be used at
 * the end of each class's initialize class method.
 *
 * @define J2OBJC_SET_INITIALIZED
 * @param CLASS The class who's flag is to be set.
 */
#define J2OBJC_SET_INITIALIZED(CLASS) \
  __c11_atomic_store(&CLASS##_initialized, YES, __ATOMIC_RELEASE);

/*!
 * Defines an init function for a class that will ensure that the class is
 * initialized. For class "Foo" the function will have the following signature:
 *   inline void Foo_initialize();
 *
 * @define J2OBJC_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_STATIC_INIT(CLASS) \
  FOUNDATION_EXPORT _Atomic(BOOL) CLASS##_initialized; \
  __attribute__((always_inline)) inline void CLASS##_initialize() { \
    if (__builtin_expect(!__c11_atomic_load(&CLASS##_initialized, __ATOMIC_ACQUIRE), 0)) { \
      [CLASS class]; \
    } \
  }

/*!
 * Defines an empty init function for a class that has no initialization code.
 *
 * @define J2OBJC_EMPTY_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_EMPTY_STATIC_INIT(CLASS) \
  __attribute__((always_inline)) inline void CLASS##_initialize() {}

/*!
 * Declares the type literal accessor for a type. This macro should be added to
 * the header of each generated Java type.
 *
 * @define J2OBJC_TYPE_LITERAL_HEADER
 * @param TYPE The name of the type to declare the accessor for.
 */
#define J2OBJC_TYPE_LITERAL_HEADER(TYPE) \
  FOUNDATION_EXPORT IOSClass *TYPE##_class_();

/*!
 * Defines the type literal accessor for a class or enum type. This macro should
 * be added to the implementation of each generated Java type.
 *
 * @define J2OBJC_CLASS_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_CLASS_TYPE_LITERAL_SOURCE(TYPE) \
  IOSClass *TYPE##_class_() { \
    static IOSClass *cls; \
    static dispatch_once_t token; \
    TYPE##_initialize(); \
    dispatch_once(&token, ^{ cls = IOSClass_fromClass([TYPE class]); }); \
    return cls; \
  }

/*!
 * Defines the type literal accessor for a interface or annotation type. This
 * macro should be added to the implementation of each generated Java type.
 *
 * @define J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(TYPE) \
  IOSClass *TYPE##_class_() { \
    static IOSClass *cls; \
    static dispatch_once_t token; \
    TYPE##_initialize(); \
    dispatch_once(&token, ^{ cls = IOSClass_fromProtocol(@protocol(TYPE)); }); \
    return cls; \
  }

#if __has_feature(objc_arc)
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((unused)) static inline TYPE CLASS##_set_##FIELD(CLASS *instance, TYPE value) { \
    return instance->FIELD = value; \
  }
#else
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((unused)) static inline TYPE CLASS##_set_##FIELD(CLASS *instance, TYPE value) { \
    return JreStrongAssign(&instance->FIELD, instance, value); \
  }\
  __attribute__((unused)) static inline TYPE CLASS##_setAndConsume_##FIELD( \
        CLASS *instance, NS_RELEASES_ARGUMENT TYPE value) { \
    return JreStrongAssignAndConsume(&instance->FIELD, instance, value); \
  }
#endif

/*!
 * Defines the getter for a static variable. For class "Foo" and field "bar_"
 * with type "int" the getter will have the following signature:
 *   inline int Foo_get_bar_();
 *
 * @define J2OBJC_STATIC_FIELD_GETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#define J2OBJC_STATIC_FIELD_GETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_get_##FIELD() { \
    CLASS##_initialize(); \
    return CLASS##_##FIELD; \
  }

/*!
 * Defines the reference getter for a static variable. For class "Foo" and field
 * "bar_" with type "int" the getter will have the following signature:
 *   inline int *Foo_getRef_bar_();
 *
 * @define J2OBJC_STATIC_FIELD_REF_GETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#define J2OBJC_STATIC_FIELD_REF_GETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE *CLASS##_getRef_##FIELD() { \
    CLASS##_initialize(); \
    return &CLASS##_##FIELD; \
  }

/*!
 * Defines the setter for a static variable with an object type. For class "Foo"
 * and field "bar_" with type "NSString *" the getter will have the following
 * signature:
 *   inline NSString *Foo_set_bar_(NSString *value);
 *
 * @define J2OBJC_STATIC_FIELD_SETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#if __has_feature(objc_arc)
#define J2OBJC_STATIC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_set_##FIELD(TYPE value) { \
    CLASS##_initialize(); \
    return CLASS##_##FIELD = value; \
  }
#else
#define J2OBJC_STATIC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_set_##FIELD(TYPE value) { \
    CLASS##_initialize(); \
    return JreStrongAssign(&CLASS##_##FIELD, nil, value); \
  } \
  __attribute__((always_inline)) inline TYPE CLASS##_setAndConsume_##FIELD(TYPE value) { \
    CLASS##_initialize(); \
    return JreStrongAssignAndConsume(&CLASS##_##FIELD, nil, value); \
  }
#endif

/*!
 * Defines the getter for an enum constant. For enum class "FooEnum" and constant "BAR"
 * the getter will have the following signature:
 *   inline Foo *FooEnum_BAR();
 *
 * @define J2OBJC_ENUM_CONSTANT_GETTER
 * @param CLASS The enum class (must end in "Enum").
 * @param CONSTANT The name of the enum constant.
 */
#define J2OBJC_ENUM_CONSTANT_GETTER(CLASS, CONSTANT) \
  __attribute__((always_inline)) inline CLASS *CLASS##_get_##CONSTANT() { \
    CLASS##_initialize(); \
    return CLASS##_##CONSTANT; \
  }

/*!
 * Adds noop implementations for the memory management methods. This helps to
 * avoid the cost of incrementing and decrementing the retain count for objects
 * that should never be dealloc'ed.
 *
 * @define J2OBJC_ETERNAL_SINGLETON
 */
#define J2OBJC_ETERNAL_SINGLETON \
  - (id)retain { return self; } \
  - (oneway void)release {} \
  - (id)autorelease { return self; }

#define MOD_ASSIGN_DEFN(NAME, TYPE) \
  static inline TYPE ModAssign##NAME(TYPE *pLhs, double rhs) { \
    return *pLhs = (TYPE) fmod(*pLhs, rhs); \
  }

MOD_ASSIGN_DEFN(Byte, char)
MOD_ASSIGN_DEFN(Char, unichar)
MOD_ASSIGN_DEFN(Double, double)
MOD_ASSIGN_DEFN(Float, float)
MOD_ASSIGN_DEFN(Int, int)
MOD_ASSIGN_DEFN(Long, long long)
MOD_ASSIGN_DEFN(Short, short int)

#undef MOD_ASSIGN_DEFN

#define SHIFT_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  static inline TYPE LShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs << (rhs & MASK); \
  } \
  static inline TYPE RShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs >> (rhs & MASK); \
  } \
  static inline TYPE URShift##NAME(TYPE lhs, jlong rhs) { \
    return (TYPE) (((UTYPE) lhs) >> (rhs & MASK)); \
  }

#define SHIFT_ASSIGN_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  static inline TYPE LShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs << (rhs & MASK)); \
  } \
  static inline TYPE RShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs >> (rhs & MASK)); \
  } \
  static inline TYPE URShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (((UTYPE) *pLhs) >> (rhs & MASK)); \
  }

// Shift masks are determined by the JLS spec, section 15.19.
SHIFT_OPERATORS_DEFN(32, jint, uint32_t, 0x1f)
SHIFT_OPERATORS_DEFN(64, jlong, uint64_t, 0x3f)
SHIFT_ASSIGN_OPERATORS_DEFN(Byte, jbyte, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Char, jchar, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Int, jint, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Long, jlong, uint64_t, 0x3f)
SHIFT_ASSIGN_OPERATORS_DEFN(Short, jshort, uint32_t, 0x1f)

#undef SHIFT_OPERATORS_DEFN
#undef SHIFT_ASSIGN_OPERATORS_DEFN

/*!
 * Returns correct result when casting a double to an integral type. In C, a
 * float >= Integer.MAX_VALUE (allowing for rounding) returns 0x80000000,
 * while Java requires 0x7FFFFFFF.  A double >= Long.MAX_VALUE returns
 * 0x8000000000000000L, while Java requires 0x7FFFFFFFFFFFFFFFL.
 */
__attribute__((always_inline)) inline int J2ObjCFpToInt(double d) {
  int tmp = (int) d;
  return tmp == (int) 0x80000000 ? (d >= 0 ? 0x7FFFFFFF : tmp) : tmp;
}
__attribute__((always_inline)) inline long long J2ObjCFpToLong(double d) {
  long long tmp = (long long) d;
  return (unsigned long long) tmp == 0x8000000000000000LL ?
      (d >= 0 ? 0x7FFFFFFFFFFFFFFFL : tmp) : tmp;
}
__attribute__((always_inline)) inline unichar J2ObjCFpToUnichar(double d) {
  unsigned tmp = (unsigned) d;
  return tmp > 0xFFFF || (tmp == 0 && d > 0) ? 0xFFFF : (unichar) tmp;
}

// This macro is used by the translator to add increment and decrement
// operations to the header files of the boxed types.
#define BOXED_INC_AND_DEC(CNAME, VALUE_METHOD, TYPE) \
  static inline TYPE *PreIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1); \
  } \
  static inline TYPE *PostIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1); \
    return original; \
  } \
  static inline TYPE *PreDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1); \
  } \
  static inline TYPE *PostDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1); \
    return original; \
  }

#endif // _J2OBJC_COMMON_H_
