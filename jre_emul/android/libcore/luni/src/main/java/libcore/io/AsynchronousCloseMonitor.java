/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import java.io.FileDescriptor;

//Native code based on Android's AsynchronousSocketCloseMonitor.cpp.

/*-[
#include "java/lang/System.h"

#include <errno.h>
#include <pthread.h>
#include <signal.h>

@class AsynchronousSocketCloseMonitor;

static const int BLOCKED_THREAD_SIGNAL = SIGUSR2;
static pthread_mutex_t blockedThreadListMutex = PTHREAD_MUTEX_INITIALIZER;
static AsynchronousSocketCloseMonitor* blockedThreadList = NULL;

static void blockedThreadSignalHandler(int unused) {
  // Do nothing. We only sent this signal for its side-effect of interrupting syscalls.
}

@interface AsynchronousSocketCloseMonitor : NSObject {
 @private
  AsynchronousSocketCloseMonitor* mPrev;
  AsynchronousSocketCloseMonitor* mNext;
  pthread_t mThread;
  int mFd;
}

- (id)initWithFileDescriptor:(int)fd;
+ (void)signalBlockedThreads:(int)fd;
@end

@implementation AsynchronousSocketCloseMonitor

- (id)initWithFileDescriptor:(int)fd {
  if ((self = [super init])) {
    pthread_mutex_lock(&blockedThreadListMutex);
    // Who are we, and what are we waiting for?
    mThread = pthread_self();
    mFd = fd;
    // Insert ourselves at the head of the intrusive doubly-linked list...
    mPrev = NULL;
    mNext = blockedThreadList;
    if (mNext != NULL) {
        mNext->mPrev = self;
    }
    blockedThreadList = self;
    pthread_mutex_unlock(&blockedThreadListMutex);
  }
  return self;
}

- (void)dealloc {
  pthread_mutex_lock(&blockedThreadListMutex);
  // Unlink ourselves from the intrusive doubly-linked list...
  if (mNext != NULL) {
      mNext->mPrev = mPrev;
  }
  if (mPrev == NULL) {
      blockedThreadList = mNext;
  } else {
      mPrev->mNext = mNext;
  }
  pthread_mutex_unlock(&blockedThreadListMutex);
#if !__has_feature(objc_arc)
  [super dealloc];
#endif
}

+ (void)signalBlockedThreads:(int)fd {
  pthread_mutex_lock(&blockedThreadListMutex);
  for (AsynchronousSocketCloseMonitor* it = blockedThreadList; it != NULL; it = it->mNext) {
    if (it->mFd == fd) {
      pthread_kill(it->mThread, BLOCKED_THREAD_SIGNAL);
      // Keep going, because there may be more than one thread...
    }
  }
  pthread_mutex_unlock(&blockedThreadListMutex);
}

+ (void)initialize {
  if (self == [AsynchronousSocketCloseMonitor class]) {
    // Ensure that the signal we send interrupts system calls but doesn't kill threads.
    // Using sigaction(2) lets us ensure that the SA_RESTART flag is not set.
    // (The whole reason we're sending this signal is to unblock system calls!)
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = blockedThreadSignalHandler;
    sa.sa_flags = 0;
    int rc = sigaction(BLOCKED_THREAD_SIGNAL, &sa, NULL);
    if (rc == -1) {
      NSString *errMsg =
          [NSString stringWithFormat:@"setting blocked thread signal handler failed: %s",
              strerror(errno)];
      JavaLangSystem_logEWithNSString_(errMsg);
    }
  }
}

@end

]-*/

public final class AsynchronousCloseMonitor {
    private AsynchronousCloseMonitor() {
    }

    public static native void signalBlockedThreads(FileDescriptor fd) /*-[
      [AsynchronousSocketCloseMonitor signalBlockedThreads:[fd getInt$]];
    ]-*/;

    public static native Object newAsynchronousSocketCloseMonitor(int fd) /*-[
      return AUTORELEASE([[AsynchronousSocketCloseMonitor alloc] initWithFileDescriptor:fd]);
    ]-*/;
}
