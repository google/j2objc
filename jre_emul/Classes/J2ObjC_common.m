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
//  J2ObjC_common.m
//  J2ObjC
//
//  Implements definitions from J2ObjC_common.h.

#import "J2ObjC_common.h"

#if __has_feature(objc_arc)
#error "J2ObjC_common cannot be built with ARC"
#endif

#import "FastPointerLookup.h"
#import "IOSClass.h"
#import "JreRetainedWith.h"
#import "java/lang/AbstractStringBuilder.h"
#import "java/lang/ArithmeticException.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/Iterable.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Throwable.h"
#import "java/util/logging/Level.h"
#import "java/util/logging/Logger.h"
#import "objc/runtime.h"
#import "os/lock.h"

id JreThrowNullPointerException() {
  @throw create_JavaLangNullPointerException_init(); // NOLINT
}

void JreThrowClassCastException(id obj, Class cls) {
  @throw create_JavaLangClassCastException_initWithNSString_(  // NOLINT
      [NSString stringWithFormat:@"Cannot cast object of type %@ to %@",
          [[obj java_getClass] getName], NSStringFromClass(cls)]);
}

void JreThrowClassCastExceptionWithIOSClass(id obj, IOSClass *cls) {
  @throw create_JavaLangClassCastException_initWithNSString_(  // NOLINT
      [NSString stringWithFormat:@"Cannot cast object of type %@ to %@",
          [[obj java_getClass] getName], [cls getName]]);
}

void JreThrowArithmeticExceptionWithNSString(NSString *msg) {
  @throw create_JavaLangArithmeticException_initWithNSString_(msg);  // NOLINT
}

void JreThrowAssertionError(id __unsafe_unretained msg) {
  @throw [[[JavaLangAssertionError alloc] initWithId:[msg description]] autorelease];  // NOLINT
}

void JreFinalize(id self) {
  @try {
    [self java_finalize];
  } @catch (JavaLangThrowable *e) {
    [JavaUtilLoggingLogger_getLoggerWithNSString_([[self java_getClass] getName])
        logWithJavaUtilLoggingLevel:JavaUtilLoggingLevel_get_WARNING()
                       withNSString:@"Uncaught exception in finalizer"
              withJavaLangThrowable:e];
  }
}

id JreStrongAssign(__strong id *pIvar, id value) {
  return JreAutoreleasedAssign(pIvar, [value retain]);
}

id JreStrongAssignAndConsume(__strong id *pIvar, NS_RELEASES_ARGUMENT id value) {
  return JreAutoreleasedAssign(pIvar, value);
}

// Declare a pool of os_unfair_lock for volatile variable access. The use
// of os_unfair_lock is consistent with Apple's implementation of atomic
// properties in current runtimes, but since updated runtime source is not
// available this can only be verified by binary inspection (verified iOS
// 15.4 runtime).
//
// Unfair locks are preferred because while they do not enforce lock ordering
// or fairness they do provide ownership information to resolve priority
// inversions:
// NOLINTNEXTLINE
// https://developer.apple.com/documentation/os/1646466-os_unfair_lock_lock?language=objc
//
// The hashing used here is inspired by Apple's older implementation:
// NOLINTNEXTLINE
// http://www.opensource.apple.com/source/objc4/objc4-532.2/runtime/Accessors.subproj/objc-accessors.mm
//
#define VOLATILE_POWER 7
#define VOLATILE_NLOCKS (1 << VOLATILE_POWER)
#define VOLATILE_MASK (VOLATILE_NLOCKS - 1)
#define VOLATILE_HASH(x) (((long)x >> 5) & VOLATILE_MASK)
#define VOLATILE_GETLOCK(ptr) &volatile_locks[VOLATILE_HASH(ptr)]
#define VOLATILE_LOCK(l) os_unfair_lock_lock(l)
#define VOLATILE_UNLOCK(l) os_unfair_lock_unlock(l)
typedef os_unfair_lock_t volatile_lock_t;
// Zero filled is sufficient initialization.
static os_unfair_lock volatile_locks[VOLATILE_NLOCKS];

id JreLoadVolatileId(volatile_id *pVar) {
  volatile_lock_t lock = VOLATILE_GETLOCK(pVar);
  VOLATILE_LOCK(lock);
  id value = [*(id *)pVar retain];
  VOLATILE_UNLOCK(lock);
  return [value autorelease];
}

id JreAssignVolatileId(volatile_id *pVar, id value) {
  volatile_lock_t lock = VOLATILE_GETLOCK(pVar);
  VOLATILE_LOCK(lock);
  *(id *)pVar = value;
  VOLATILE_UNLOCK(lock);
  return value;
}

id JreVolatileStrongAssign(volatile_id *pIvar, id value) {
  volatile_lock_t lock = VOLATILE_GETLOCK(pIvar);
  [value retain];
  VOLATILE_LOCK(lock);
  id oldValue = *(id *)pIvar;
  *(id *)pIvar = value;
  VOLATILE_UNLOCK(lock);
  [oldValue autorelease];
  return value;
}

jboolean JreCompareAndSwapVolatileStrongId(volatile_id *pVar, id expected, id newValue) {
  volatile_lock_t lock = VOLATILE_GETLOCK(pVar);
  VOLATILE_LOCK(lock);
  jboolean result = *(id *)pVar == expected;
  if (result) {
    *(id *)pVar = [newValue retain];
  }
  VOLATILE_UNLOCK(lock);
  if (result) {
    [expected autorelease];
  }
  return result;
}

id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue) {
  [newValue retain];
  volatile_lock_t lock = VOLATILE_GETLOCK(pVar);
  VOLATILE_LOCK(lock);
  id oldValue = *(id *)pVar;
  *(id *)pVar = newValue;
  VOLATILE_UNLOCK(lock);
  return [oldValue autorelease];
}

void JreReleaseVolatile(volatile_id *pVar) {
  // This is only called from a dealloc method, so we can assume there are no
  // concurrent threads with access to this address. Therefore, synchronization
  // is unnecessary.
  [*(id *)pVar release];
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
  volatile_lock_t lock = VOLATILE_GETLOCK(pOther);
  VOLATILE_LOCK(lock);
  *(id *)pVar = *(id *)pOther;
  VOLATILE_UNLOCK(lock);
}

void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
  // We lock on pOther because it may be visible to other threads. Since we are
  // still within Object.clone() we know that pVar isn't visible to other
  // threads yet, so we don't need to use it's lock. However, we do the
  // assignment within pOther's lock to provide sequencial consistency.
  volatile_lock_t lock = VOLATILE_GETLOCK(pOther);
  VOLATILE_LOCK(lock);
  *(id *)pVar = [*(id *)pOther retain];
  VOLATILE_UNLOCK(lock);
}

id JreStrictFieldStrongAssign(__strong id *pIvar, id value) {
  // Reuse the volatile locks for strict field assignments.
  volatile_lock_t lock = VOLATILE_GETLOCK(pIvar);
  [value retain];
  VOLATILE_LOCK(lock);
  id oldValue = *(id *)pIvar;
  *(id *)pIvar = value;
  VOLATILE_UNLOCK(lock);
  [oldValue autorelease];
  return value;
}

id JreStrictFieldStrongLoad(__strong id *pIvar) {
  // Reuse the volatile locks for strict field assignments.
  volatile_lock_t lock = VOLATILE_GETLOCK(pIvar);
  VOLATILE_LOCK(lock);
  id value = [*(id *)pIvar retain];
  VOLATILE_UNLOCK(lock);
  return [value autorelease];
}

void JreStrictFieldStrongRelease(__strong id *pIvar) {
  // This is only called from a dealloc method, so we can assume there are no
  // concurrent threads with access to this address. Therefore, synchronization
  // is unnecessary.
  [*(id *)pIvar release];
  // Unlike volatile fields, these fields are types also managed by ARC, so we need to nil
  // to prevent subsequent duplicate release by ARC.
  *(id *)pIvar = nil;
}

id JreRetainedWithAssign(id parent, __strong id *pIvar, id value) {
  if (*pIvar) {
    JreRetainedWithHandlePreviousValue(parent, *pIvar);
    [*pIvar autorelease];
  }
  // This retain makes sure that the child object has a retain count of at
  // least 2 which is required by JreRetainedWithInitialize.
  [value retain];
  JreRetainedWithInitialize(parent, value);
  return *pIvar = value;
}

id JreVolatileRetainedWithAssign(id parent, volatile_id *pIvar, id value) {
  // This retain makes sure that the child object has a retain count of at
  // least 2 which is required by JreRetainedWithInitialize.
  [value retain];
  JreRetainedWithInitialize(parent, value);
  volatile_lock_t lock = VOLATILE_GETLOCK(pIvar);
  VOLATILE_LOCK(lock);
  id oldValue = *(id *)pIvar;
  *(id *)pIvar = value;
  VOLATILE_UNLOCK(lock);
  if (oldValue) {
    JreRetainedWithHandlePreviousValue(parent, oldValue);
    [oldValue release];
  }
  return value;
}

void JreRetainedWithRelease(id parent, id value) {
  JreRetainedWithHandleDealloc(parent, value);
  [value release];
}

void JreVolatileRetainedWithRelease(id parent, volatile_id *pVar) {
  JreRetainedWithHandleDealloc(parent, *(id *)pVar);
  // This is only called from a dealloc method, so we can assume there are no
  // concurrent threads with access to this address. Therefore, synchronization
  // is unnecessary.
  [*(id *)pVar release];
}

void JreStrictFieldRetainedWithRelease(id parent, id *pVar) {
  JreRetainedWithHandleDealloc(parent, *(id *)pVar);
  // This is only called from a dealloc method, so we can assume there are no
  // concurrent threads with access to this address. Therefore, synchronization
  // is unnecessary. Also, no need to nil out, RetainedWith is manual memory
  // management only.
  [*(id *)pVar release];
}

jint JreIndexOfStr(NSString *str, NSString **values, jint size) {
  for (int i = 0; i < size; i++) {
    if ([str isEqualToString:values[i]]) {
      return i;
    }
  }
  return -1;
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
          NSString *description = [va_arg(va, id) description];
          if (description) {
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
        JreStringBuilder_appendDouble(sb, va_arg(va, jdouble));
        break;
      case 'F':
        JreStringBuilder_appendFloat(sb, (jfloat)va_arg(va, jdouble));
        break;
      case 'B':
      case 'I':
      case 'S':
        JreStringBuilder_appendInt(sb, va_arg(va, jint));
        break;
      case 'J':
        JreStringBuilder_appendLong(sb, va_arg(va, jlong));
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

id JreStrAppend(__unsafe_unretained id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(*lhs, types, va);
  va_end(va);
  return *lhs = result;
}

id JreStrAppendStrong(__strong id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(*lhs, types, va);
  va_end(va);
  return JreStrongAssign(lhs, result);
}

id JreStrAppendVolatile(volatile_id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(JreLoadVolatileId(lhs), types, va);
  va_end(va);
  return JreAssignVolatileId(lhs, result);
}

id JreStrAppendVolatileStrong(volatile_id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(JreLoadVolatileId(lhs), types, va);
  va_end(va);
  return JreVolatileStrongAssign(lhs, result);
}

id JreStrAppendStrictFieldStrong(__strong id *lhs, const char *types, ...) {
  va_list va;
  va_start(va, types);
  NSString *result = JreStrAppendInner(JreStrictFieldStrongLoad(lhs), types, va);
  va_end(va);
  return JreStrictFieldStrongAssign(lhs, result);
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

FOUNDATION_EXPORT NSString *JreEnumConstantName(IOSClass *enumClass, jint ordinal) {
  const J2ObjcClassInfo *metadata = [enumClass getMetadata];
  if (metadata) {
    return [NSString stringWithUTF8String:metadata->fields[ordinal].name];
  } else {
    return [NSString stringWithFormat:@"%@_%d", NSStringFromClass(enumClass.objcClass), ordinal];
  }
}

NSUInteger JreDefaultFastEnumeration(
    __unsafe_unretained id<JavaLangIterable> obj, NSFastEnumerationState *state,
    __unsafe_unretained id *stackbuf) {
  SEL hasNextSel = sel_registerName("hasNext");
  SEL nextSel = sel_registerName("next");
  __unsafe_unretained id iter = (ARCBRIDGE id) (void *) state->extra[0];
  if (!iter) {
    static unsigned long no_mutation = 1;
    state->mutationsPtr = &no_mutation;
    // The for/in loop could break early so we have no guarantee of being able
    // to release the iterator. As long as the current autorelease pool is not
    // cleared within the loop, this should be fine.
    iter = nil_chk([obj iterator]);
    state->extra[0] = (unsigned long) iter;
    state->extra[1] = (unsigned long) [iter methodForSelector:hasNextSel];
    state->extra[2] = (unsigned long) [iter methodForSelector:nextSel];
  }
  jboolean (*hasNextImpl)(id, SEL) = (jboolean (*)(id, SEL)) state->extra[1];
  id (*nextImpl)(id, SEL) = (id (*)(id, SEL)) state->extra[2];
  NSUInteger objCount = 0;
  state->itemsPtr = stackbuf;
  if (hasNextImpl(iter, hasNextSel)) {
    *stackbuf++ = nextImpl(iter, nextSel);
    objCount++;
  }
  return objCount;
}
