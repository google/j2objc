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

// Common definitions needed by J2ObjC and JNI.

#ifndef _J2OBJC_TYPES_H_
#define _J2OBJC_TYPES_H_

#ifdef __OBJC__
#import <Foundation/Foundation.h>
#else
#include <stdint.h>
#endif

#if ! __has_extension(c_atomic)
#error C11 or higher language standard must be used for Java volatile support.
#endif

// Typedefs for each of Java's primitive types. The translator no longer
// generates these types, but they are defined for backwards compatibility
// native Objective-C code that uses these types.
typedef int8_t          jbyte;          /* signed 8 bits */
typedef uint16_t        jchar;          /* unsigned 16 bits */
typedef int16_t         jshort;         /* signed 16 bits */
typedef int32_t         jint;           /* signed 32 bits */
typedef int64_t         jlong;          /* signed 64 bits */
typedef float           jfloat;         /* 32-bit IEEE 754 */
typedef double          jdouble;        /* 64-bit IEEE 754 */

// The translator doesn't generate jboolean anymore, nor is it used in jre_emul.
// This typedef is for backwards-compatibility with other projects' native code.
typedef bool            jboolean;

// Typedefs for Java types declared as volatile.
typedef _Atomic(int8_t)   volatile_int8_t;
typedef _Atomic(uint16_t) volatile_uint16_t;
typedef _Atomic(int16_t)  volatile_int16_t;
typedef _Atomic(int32_t)  volatile_int32_t;
typedef _Atomic(int64_t)  volatile_int64_t;
typedef _Atomic(float)    volatile_float;
typedef _Atomic(double)   volatile_double;
typedef _Atomic(bool)     volatile_bool;

// Aliases for native references to volatile types.
typedef volatile_bool     volatile_jboolean;
typedef volatile_int8_t   volatile_jbyte;
typedef volatile_uint16_t volatile_jchar;
typedef volatile_int16_t  volatile_jshort;
typedef volatile_int64_t  volatile_jlong;
typedef volatile_int32_t  volatile_jint;
typedef volatile_float    volatile_jfloat;
typedef volatile_double   volatile_jdouble;

// bool is a macro that expands to _Bool, so add typedef for use by macros.
typedef volatile_bool     volatile__Bool;

// Volatile object access is guarded by spin locks because of reference counting
// so we don't use an atomic type. uintptr_t is used for the typedef mainly to
// prevent accidental usage as a regular id type.
typedef uintptr_t          volatile_id;

#endif // _J2OBJC_TYPES_H_
