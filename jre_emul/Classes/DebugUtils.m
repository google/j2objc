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
//  DebugUtils.m: low-level functions to aid debugging.
//  JreEmulation
//
//  Created by Tom Ball on 3/4/14.
//

#include "DebugUtils.h"

#include <execinfo.h>

#define FRAMES_TO_IGNORE 2

@implementation DebugUtils

static void LogStack(NSUInteger nFrames) {
  if (nFrames == 0) {
    return;
  }

  void *callStack[nFrames + FRAMES_TO_IGNORE];
  NSUInteger callFrames = backtrace(callStack, (int) nFrames + FRAMES_TO_IGNORE);

  for (NSUInteger i = FRAMES_TO_IGNORE; i <= callFrames; i++) {
    void *shortStack[1];
    shortStack[0] = callStack[i];
    char **stackSymbol = backtrace_symbols(shortStack, 1);
    NSLog(@"  %s", *stackSymbol);
    free(stackSymbol);
  }
}

+ (void)logStack:(NSUInteger)nFrames {
  @synchronized (self) {
    LogStack(nFrames);
  }
}

+ (void)logStack:(NSUInteger)nFrames withMessage:(NSString *)msg {
  @synchronized (self) {
    NSLog(@"%@", msg);
    LogStack(nFrames);
  }
}

@end

