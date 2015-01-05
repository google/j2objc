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

// C11 atomic operations library subset to support Java volatiles.
// Definitions are from http://en.cppreference.com/w/c/atomic and
// http://clang.llvm.org/docs/LanguageExtensions.html#c11-atomic-builtins

#ifndef _VOLATILE_H_
#define _VOLATILE_H_

#if defined(_LIBCPP_VERSION) && __has_include( <atomic> )
#warning "Has C++ atomic support"
#elseif ! __has_feature(c_atomic)
#error "Must use -std=c11 or -std=c++11 flag to translate Java volatile fields"
#else

enum memory_order {
  memory_order_relaxed,
  memory_order_consume,
  memory_order_acquire,
  memory_order_release,
  memory_order_acq_rel,
  memory_order_seq_cst
};

#define _Volatile(T)          _Atomic(T)

typedef _Volatile(bool)       volatile_bool;
typedef _Volatile(char)       volatile_char;
typedef _Volatile(short)      volatile_short;
typedef _Volatile(int)        volatile_int;
typedef _Volatile(long long)  volatile_long;
typedef _Volatile(intptr_t)   volatile_intptr_t;

#define volatile_init(obj, value)       (__c11_atomic_init(obj, value), volatile_load(obj))
#define volatile_load(object)           __c11_atomic_load(object, memory_order_seq_cst)
#define volatile_store(object, desired) \
    (__c11_atomic_store(object, desired, memory_order_seq_cst), volatile_load(object))
#define volatile_compare_exchange_strong(object, expected, desired) \
    __c11_atomic_compare_exchange_strong(object, expected, desired, \
        memory_order_seq_cst, memory_order_seq_cst)

#endif /* if ! __has_feature(c_atomic) */
#endif /* _VOLATILE_H_ */
