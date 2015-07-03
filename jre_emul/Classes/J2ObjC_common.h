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

#import "J2ObjC_types.h"

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

FOUNDATION_EXPORT id JreStrongAssign(id *pIvar, id value);
FOUNDATION_EXPORT id JreStrongAssignAndConsume(id *pIvar, NS_RELEASES_ARGUMENT id value);

/*!
 * Macros that simplify the syntax for loading of static fields.
 *
 * @define JreLoadStatic
 * @define JreLoadStaticRef
 * @param CLASS The Objective-C class name of the containing class.
 * @param FIELD The name of the static field.
 */
#define JreLoadStatic(CLASS, FIELD) (CLASS##_initialize(), CLASS##_##FIELD)
#define JreLoadStaticRef(CLASS, FIELD) (CLASS##_initialize(), &CLASS##_##FIELD)

/*!
 * Defines the initialized flag for a class.
 *
 * @define J2OBJC_INITIALIZED_DEFN
 * @param CLASS The class for which the initialized flag is defined.
 */
#define J2OBJC_INITIALIZED_DEFN(CLASS) \
  _Atomic(BOOL) CLASS##__initialized = NO;

/*!
 * Defines the code to set a class's initialized flag. This should be used at
 * the end of each class's initialize class method.
 *
 * @define J2OBJC_SET_INITIALIZED
 * @param CLASS The class who's flag is to be set.
 */
#define J2OBJC_SET_INITIALIZED(CLASS) \
  __c11_atomic_store(&CLASS##__initialized, YES, __ATOMIC_RELEASE);

/*!
 * Defines an init function for a class that will ensure that the class is
 * initialized. For class "Foo" the function will have the following signature:
 *   inline void Foo_initialize();
 *
 * @define J2OBJC_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_STATIC_INIT(CLASS) \
  FOUNDATION_EXPORT _Atomic(BOOL) CLASS##__initialized; \
  __attribute__((always_inline)) inline void CLASS##_initialize() { \
    if (__builtin_expect(!__c11_atomic_load(&CLASS##__initialized, __ATOMIC_ACQUIRE), 0)) { \
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
    return JreStrongAssign(&instance->FIELD, value); \
  }\
  __attribute__((unused)) static inline TYPE CLASS##_setAndConsume_##FIELD( \
        CLASS *instance, NS_RELEASES_ARGUMENT TYPE value) { \
    return JreStrongAssignAndConsume(&instance->FIELD, value); \
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
    return JreStrongAssign(&CLASS##_##FIELD, value); \
  } \
  __attribute__((always_inline)) inline TYPE CLASS##_setAndConsume_##FIELD(TYPE value) { \
    CLASS##_initialize(); \
    return JreStrongAssignAndConsume(&CLASS##_##FIELD, value); \
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

/*!
 * Defines increment and decrement operators on boxed types. The translator will
 * use this macro to add these operators to the headers of the boxed types.
 *
 * @define BOXED_INC_AND_DEC
 * @param CNAME The capitalized name of the primitive type (eg. "Int").
 * @param VALUE_METHOD The method on the boxed type that returns the value.
 * @param TYPE The boxed type name (eg. "JavaLangInteger").
 */
#define BOXED_INC_AND_DEC(CNAME, VALUE_METHOD, TYPE) \
  __attribute__((always_inline)) inline TYPE *BoxedPreIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPreIncrStrong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1)); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPostIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1); \
    return original; \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPostIncrStrong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] + 1)); \
    return original; \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPreDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPreDecrStrong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1)); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPostDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1); \
    return original; \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPostDecrStrong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] - 1)); \
    return original; \
  }

#define ADD_OP(a, b) a + b
#define MINUS_OP(a, b) a - b
#define TIMES_OP(a, b) a * b
#define DIVIDE_OP(a, b) a / b
#define REMAINDER_OP(a, b) a % b
#define FREMAINDER_OP(a, b) fmod(a, b)
#define BITAND_OP(a, b) a & b
#define BITOR_OP(a, b) a | b
#define BITXOR_OP(a, b) a ^ b
#define LSHIFT_32_OP(a, b) a << (b & 0x1f)
#define RSHIFT_32_OP(a, b) a >> (b & 0x1f)
#define LSHIFT_64_OP(a, b) a << (b & 0x3f)
#define RSHIFT_64_OP(a, b) a >> (b & 0x3f)

/*!
 * Template for compound assign operators on boxed types.
 *
 * @define BOXED_COMPOUND_ASSIGN
 * @param CNAME The capitalized name of the primitive type (eg. "Int").
 * @param VALUE_METHOD The method on the boxed type that returns the value.
 * @param TYPE The primitive type name (eg. "jint").
 * @param BOXED_TYPE The boxed type name (eg. "JavaLangInteger").
 * @param RTYPE The type of the right hand side of the assignment.
 * @param OPNAME The name of the operator, used to construct the function name.
 * @param OP A macro that takes two parameters and prints the operation.
 * @param OP_LTYPE The cast type for the left hand side of the operation.
 */
#define BOXED_COMPOUND_ASSIGN( \
    CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, RTYPE, OPNAME, OP, OP_LTYPE) \
  __attribute__((always_inline)) inline BOXED_TYPE *Boxed##OPNAME##Assign##CNAME( \
      BOXED_TYPE **lhs, RTYPE rhs) { \
    nil_chk(*lhs); \
    return *lhs = BOXED_TYPE##_valueOfWith##CNAME##_( \
        (TYPE)(OP((OP_LTYPE)[*lhs VALUE_METHOD], rhs))); \
  } \
  __attribute__((always_inline)) inline BOXED_TYPE *Boxed##OPNAME##AssignStrong##CNAME( \
      BOXED_TYPE **lhs, RTYPE rhs) { \
    nil_chk(*lhs); \
    return JreStrongAssign(lhs, \
        BOXED_TYPE##_valueOfWith##CNAME##_((TYPE)(OP((OP_LTYPE)[*lhs VALUE_METHOD], rhs)))); \
  }

// This macros are used in the boxed primitive header files.
#define BOXED_COMPOUND_ASSIGN_ARITHMETIC(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Plus, ADD_OP, TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Minus, MINUS_OP, TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Times, TIMES_OP, TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Divide, DIVIDE_OP, TYPE)
#define BOXED_COMPOUND_ASSIGN_MOD(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Mod, REMAINDER_OP, TYPE)
#define BOXED_COMPOUND_ASSIGN_FPMOD(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, Mod, FREMAINDER_OP, TYPE)
#define BOXED_COMPOUND_ASSIGN_BITWISE(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, BitAnd, BITAND_OP, TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, BitOr, BITOR_OP, TYPE) \
    BOXED_COMPOUND_ASSIGN(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, TYPE, BitXor, BITXOR_OP, TYPE)
#define BOXED_SHIFT_ASSIGN_32(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, LShift, LSHIFT_32_OP, jint) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, RShift, RSHIFT_32_OP, jint) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, URShift, RSHIFT_32_OP, uint32_t)
#define BOXED_SHIFT_ASSIGN_64(CNAME, VALUE_METHOD, TYPE, BOXED_TYPE) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, LShift, LSHIFT_64_OP, jlong) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, RShift, RSHIFT_64_OP, jlong) \
    BOXED_COMPOUND_ASSIGN( \
        CNAME, VALUE_METHOD, TYPE, BOXED_TYPE, jlong, URShift, RSHIFT_64_OP, uint64_t)

#endif // _J2OBJC_COMMON_H_
