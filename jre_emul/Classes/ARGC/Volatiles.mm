
#import "J2ObjC_common.h"

#if J2OBJC_USE_GC

#define GC_DEBUG 0

#include "NSObject+ARGC.h"
#include <pthread.h>

extern "C" {
void ARGC_lock_volatile();
void ARGC_unlock_volatile();
}

static pthread_t g_lockThread = NULL;
static int g_cntLock = 0;

void ARGC_lock_volatile() {
  pthread_t curr_thread = pthread_self();
  if (curr_thread != g_lockThread) {
    while (!__sync_bool_compare_and_swap(&g_lockThread, NULL, curr_thread)) {}
  }
  g_cntLock ++;
}

void ARGC_unlock_volatile() {
  if (GC_DEBUG) {
    assert(pthread_self() == g_lockThread);
  }
  if (--g_cntLock == 0) {
    g_lockThread = NULL;
  }
}

id JreLoadVolatileId(volatile_id *pVar) {
  ARGC_lock_volatile();
  id oid = *(std::atomic<id>*)pVar;
  if (oid != NULL) {
    [oid retain];
    [oid autorelease];
  };
  ARGC_unlock_volatile();
  return oid;
}

id JreAssignVolatileId(volatile_id *pVar, id newValue) {
  ARGC_lock_volatile();
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, newValue);
  ARGC_unlock_volatile();
  
  return newValue;
}

void JreReleaseVolatile(volatile_id *pVar) {
  ARGC_lock_volatile();
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, NULL);
  ARGC_unlock_volatile();
}

id JreVolatileStrongAssign(volatile_id *pVar, id newValue) {
  ARGC_lock_volatile();
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, newValue);
  ARGC_unlock_volatile();
  return newValue;
}


id JreVolatileNativeAssign(volatile_id *pVar, id newValue) {
  ARGC_lock_volatile();
  ARGC_assignStrongObject((ARGC_FIELD_REF id*)pVar, newValue);
  ARGC_unlock_volatile();
  return newValue;
}

bool JreCompareAndSwapVolatileStrongId(volatile_id *ptr, id expected, id newValue) {
  std::atomic<id>* field = (std::atomic<id>*)ptr;
  ARGC_lock_volatile();
  bool res =  field->compare_exchange_strong(expected, newValue);
  if (res) {
    if (newValue) ARGC_genericRetain(newValue);
    if (expected) ARGC_genericRelease(expected);
  }
  ARGC_unlock_volatile();
  return res;
}

id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue) {
  std::atomic<id>* field = (std::atomic<id>*)pVar;
  ARGC_lock_volatile();
  id oldValue = field->exchange(newValue);
  if (oldValue != newValue) {
    if (newValue) {
      [newValue retain];
    }
    if (oldValue) {
      //            if (GC_DEBUG && GC_LOG_ALLOC) {
      //                if ([oldValue toJObject] == NULL) {
      //                    NSLog(@"--nstr %p #%d %@", oldValue, (int)NSExtraRefCount(oldValue), [oldValue class]);
      //                }
      //            }
      [oldValue autorelease];
    }
  }
  ARGC_unlock_volatile();
  return oldValue;
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
  ARGC_lock_volatile();
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, *(id*)pOther);
  ARGC_unlock_volatile();
}

void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
  ARGC_lock_volatile();
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, *(id*)pOther);
  ARGC_unlock_volatile();
}

#endif // J2OBJC_USE_GC
