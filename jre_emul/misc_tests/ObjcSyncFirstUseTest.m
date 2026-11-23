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
#import <XCTest/XCTest.h>
#include <pthread.h>
#include <stdatomic.h>
#include <stdint.h>

// Reproduces the process first-use data race inside recursiveAttributes() in
// apple_apsl/objc-sync.m when two threads enter @synchronized on objects that
// hash to different sDataLists buckets before any prior :jre_core
// objc_sync_enter call has initialized sRecursiveLockAttr:
//
//   (no prior @synchronized against :jre_core in the test process:
//    sRecursiveLockAttrIntialized == false)
//
//   Thread A (@synchronized(lockBucketA))   Thread B (@synchronized(lockBucketB))
//     |                                       |
//     v  (atomic spin barrier: readyCount==2) v
//   objc_sync_enter(lockBucketA)            objc_sync_enter(lockBucketB)
//     | locks sDataLists[bucketA].lock        | locks sDataLists[bucketB].lock
//     | (bucketA != bucketB -> NO HB edge)    |
//     v                                       v
//   recursiveAttributes()                   recursiveAttributes()
//     if (!sRecursiveLockAttrIntialized)      if (!sRecursiveLockAttrIntialized)
//     pthread_mutexattr_init(&sRecursiveLockAttr)  <=== TSan DATA RACE ===>

typedef struct {
  __unsafe_unretained NSObject *targetObject;
  atomic_int *readyCount;
  atomic_int *completedCount;
  int sink;
} ThreadSyncArgs;

static BOOL gFoundDistinctBucket = NO;
static int gCreateErrorA = -1;
static int gCreateErrorB = -1;
static int gJoinErrorA = -1;
static int gJoinErrorB = -1;
static int gCompletedCount = 0;
static int gSinkA = 0;
static int gSinkB = 0;
static NSUInteger gRetainedCandidateCount = 0;

static uintptr_t SyncBucketIndex(NSObject *targetObject) {
  // Matches HASH(obj) = ((((uintptr_t)(obj)) >> 5) & (COUNT - 1)) with
  // COUNT == 16 in apple_apsl/objc-sync.m.
  return (((uintptr_t)(__bridge const void *)targetObject) >> 5) & 15U;
}

static void *RunSynchronized(void *context) {
  @autoreleasepool {
    ThreadSyncArgs *threadArgs = (ThreadSyncArgs *)context;
    atomic_fetch_add_explicit(threadArgs->readyCount, 1, memory_order_acq_rel);
    while (atomic_load_explicit(threadArgs->readyCount, memory_order_acquire) < 2) {
    }
    @synchronized(threadArgs->targetObject) {
      threadArgs->sink += 1;
    }
    atomic_fetch_add_explicit(threadArgs->completedCount, 1, memory_order_relaxed);
  }
  return NULL;
}

@interface ObjcSyncFirstUseTest : XCTestCase
@end

@implementation ObjcSyncFirstUseTest

+ (void)load {
  // Trigger the two-thread @synchronized race during image load before XCTest
  // test discovery or Foundation runtime observers call objc_sync_enter.
  @autoreleasepool {
    // Keep all candidate allocations alive so addresses cannot be reused, and
    // do not invoke @synchronized before spawning the worker pthreads.
    NSMutableArray<NSObject *> *retainedCandidates = [[NSMutableArray alloc] init];
    NSObject *lockBucketA = [[NSObject alloc] init];
    [retainedCandidates addObject:lockBucketA];
    uintptr_t bucketA = SyncBucketIndex(lockBucketA);

    NSObject *lockBucketB = nil;
    for (int attempt = 0; attempt < 4096; ++attempt) {
      NSObject *candidate = [[NSObject alloc] init];
      [retainedCandidates addObject:candidate];
      if (SyncBucketIndex(candidate) != bucketA) {
        lockBucketB = candidate;
        break;
      }
    }
    gFoundDistinctBucket = (lockBucketB != nil);
    if (!gFoundDistinctBucket) {
      return;
    }

    atomic_int readyCount;
    atomic_init(&readyCount, 0);
    atomic_int completedCount;
    atomic_init(&completedCount, 0);

    ThreadSyncArgs argsA = {
        .targetObject = lockBucketA,
        .readyCount = &readyCount,
        .completedCount = &completedCount,
        .sink = 0,
    };
    ThreadSyncArgs argsB = {
        .targetObject = lockBucketB,
        .readyCount = &readyCount,
        .completedCount = &completedCount,
        .sink = 0,
    };

    pthread_t threadA;
    pthread_t threadB;
    gCreateErrorA = pthread_create(&threadA, NULL, RunSynchronized, &argsA);
    if (gCreateErrorA != 0) {
      return;
    }
    gCreateErrorB = pthread_create(&threadB, NULL, RunSynchronized, &argsB);
    if (gCreateErrorB != 0) {
      // Release threadA from the start barrier before joining so it cannot spin indefinitely.
      atomic_fetch_add_explicit(&readyCount, 1, memory_order_acq_rel);
      pthread_join(threadA, NULL);
      return;
    }

    gJoinErrorA = pthread_join(threadA, NULL);
    gJoinErrorB = pthread_join(threadB, NULL);
    gCompletedCount = atomic_load_explicit(&completedCount, memory_order_relaxed);
    gSinkA = argsA.sink;
    gSinkB = argsB.sink;
    gRetainedCandidateCount = [retainedCandidates count];
  }
}

- (void)testConcurrentFirstUseAcrossDistinctBuckets {
  XCTAssertTrue(gFoundDistinctBucket);
  XCTAssertEqual(gCreateErrorA, 0);
  XCTAssertEqual(gCreateErrorB, 0);
  XCTAssertEqual(gJoinErrorA, 0);
  XCTAssertEqual(gJoinErrorB, 0);
  XCTAssertEqual(gCompletedCount, 2);
  XCTAssertEqual(gSinkA, 1);
  XCTAssertEqual(gSinkB, 1);
  XCTAssertGreaterThan(gRetainedCandidateCount, (NSUInteger)0);
}

@end
