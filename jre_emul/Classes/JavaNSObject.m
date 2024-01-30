// Copyright 2024 Google Inc. All Rights Reserved.
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

#import <Foundation/Foundation.h>

#import "J2ObjC_source.h"
#import "JavaNSObject.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/InternalError.h"

@interface JavaNSObject () {
  pthread_mutex_t mutex_;
  pthread_cond_t cond_;
  int lockCount_;
}

@end

@implementation JavaNSObject

- (instancetype)init {
  self = [super init];
  if (self) {
    pthread_mutexattr_t mutexAttr;
    pthread_mutexattr_init(&mutexAttr);
    pthread_mutexattr_settype(&mutexAttr, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&mutex_, &mutexAttr);
    pthread_cond_init(&cond_, nullptr);
  }
  return self;
}

- (BOOL)java_currentThreadHoldsLock {
  int result = pthread_mutex_trylock(&mutex_);
  if (result == EBUSY) {
    // Another thread has the lock.
    return NO;
  }
  if (result != 0) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }

  // This thread now has the lock (and no other thread had the lock previously).
  // See if it previously had the lock.
  BOOL lockHeld = lockCount_ > 0;

  result = pthread_mutex_unlock(&mutex_);
  if (result != 0) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }

  return lockHeld;
}

- (void)java_lock {
  int result;
  JavaLangThread *javaThread = getCurrentJavaThreadOrNull();
  if (javaThread) {
    result = pthread_mutex_trylock(&mutex_);
    if (result == EBUSY) {
      JreAssignVolatileInt(&javaThread->state_, JavaLangThread_STATE_BLOCKED);
      result = pthread_mutex_lock(&data->mutex);
      JreAssignVolatileInt(&javaThread->state_, JavaLangThread_STATE_RUNNABLE);
    }
  } else {
    result = pthread_mutex_lock(&mutex_);
  }
  if (result != 0) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
  lockCount_++;
  JavaLangThread *javaThread = getCurrentJavaThreadOrNull();
}

- (void)java_unlock {
  int result = pthread_mutex_unlock(&mutex_);
  if (!result) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
  lockCount_--;
}

- (void)java_notify {
  int result = pthread_cond_signal(&cond_);
  if (!result) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

- (void)java_notifyAll {
  int result = pthread_cond_broadcast(&cond_);
  if (!result) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

static void doWait(JavaNSObject *obj, uint64_t timeoutNanos) {
  // This must be called with the mutex held, so the lock count must be greater than 0.
  assert(objc->lockCount_ > 0);

  // Temporarily bring the lock count to 1 so pthread_cond_wait can fully unlock the recursive lock.
  int originalLockCount = obj->lockCount_;
  assert(originalLockCount > 0);

  for (int i = 0; i < originalLockCount - 1; i++) {
    pthread_mutex_unlock(obj->mutex_);
  }

  JavaLangThread *javaThread = getCurrentJavaThreadOrNull();

  if (javaThread) {
    assert(javaThread->blocker_ == nil);
    javaThread->blocker_ = obj;
  }

  if (timeoutNanos == 0) {
    if (javaThread) {
      JreAssignVolatileInt(&javaThread->state_, JavaLangThread_STATE_WAITING);
    }
    result = pthread_cond_wait(&obj->cond_, &obj->mutex_);
  } else {
    struct timespec timeout = {
        .tv_sec = remainingTimeoutNanos / 1000000000ULL,
        .tv_nsec = remainingTimeoutNanos % 1000000000ULL,
    };

    if (javaThread) {
      JreAssignVolatileInt(&javaThread->state_, JavaLangThread_STATE_TIMED_WAITING);
    }
    result = pthread_cond_timedwait_relative_np(&obj->cond_, &obj->mutex_, &t);
  }

  if (javaThread) {
    javaThread->blocker_ = nil;
    JreAssignVolatileInt(&javaThread->state_, JavaLangThread_STATE_RUNNABLE);
  }

  // Now bring the lock count back to where it was.
  for (int i = 0; i < originalLockCount - 1; i++) {
    pthread_mutex_lock(obj->mutex_);
  }

  if (javaThread) {
    jboolean wasInterrupted = javaThread->interrupted_;
    javaThread->interrupted_ = false;
    if (wasInterrupted) {
      @throw AUTORELEASE([[JavaLangInterruptedException alloc] init]);
    }
  }

  if (result != 0 && result != ETIMEDOUT) {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

- (void)java_wait {
  doWait(self, cond_, mutex_, /*timeoutNanos=*/0);
}

- (void)java_waitWithLong:(long long)timeoutMillis {
  if (timeoutMillis < 0) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  doWait(self, cond_, mutex_, timeoutMillis * 1000000);
}

- (void)java_waitWithLong:(long long)timeoutMillis withInt:(int)timeoutNanos {
  if (timeoutMillis < 0 || timeoutNanos < 0) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  doWait(self, cond_, mutex_, timeoutMillis * 1000000 + timeoutNanos);
}

@end

#endif  // _JavaObject_H_
