// Copyright 2011 Google Inc. All Rights Reserved.
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

//
// Transpiler Memory Debug.
//

#ifndef _JreMemDebug_H_
#define _JreMemDebug_H_

// Set JREMEMDEBUG_OVERHEAD_ENABLED to 1 to allow overhead of memory debugging.
// Keeping this value to 0 will completely disable it at compile time
// And will avoid any (even unsignificant) overhead.
#define JREMEMDEBUG_OVERHEAD_ENABLED 1

#ifdef __OBJC__
# import <Foundation/Foundation.h>
# import "JreMemDebugStrongReference.h"

    // Predeclare private functions.
    FOUNDATION_EXPORT id JreMemDebugAddInternal(id obj);
    FOUNDATION_EXPORT void JreMemDebugRemoveInternal(id obj);
    FOUNDATION_EXPORT void JreMemDebugLockInternal(void);
    FOUNDATION_EXPORT void JreMemDebugUnlockInternal(void);

    // JreMemDebugEnabled should be set to YES at the beginning of main()
    // to enable the debug mode.
    //
    // After it has been enabled, it should not be set back to NO.
    // Unexpected behavior might occur.
    FOUNDATION_EXPORT BOOL JreMemDebugEnabled;

    // This function add the given transpiled java object to the set of
    // allocated objects.
    static inline id JreMemDebugAdd(id obj) {
# if JREMEMDEBUG_OVERHEAD_ENABLED
      if (!JreMemDebugEnabled)
        return obj;

      return JreMemDebugAddInternal(obj);
# else
      return obj;
# endif
    }

    // This function remove the given transpiled java object from the set of
    // allocated objects.
    static inline void JreMemDebugRemove(id obj) {
# if JREMEMDEBUG_OVERHEAD_ENABLED
      if (!JreMemDebugEnabled)
        return;

      JreMemDebugRemoveInternal(obj);
# endif
    }

    // This function is a global lock on the memory debugging tool.
    static inline void JreMemDebugLock(void) {
# if JREMEMDEBUG_OVERHEAD_ENABLED
      if (!JreMemDebugEnabled)
        return;

      JreMemDebugLockInternal();
# endif
    }

    // This function is a global unlock on the memory debugging tool.
    static inline void JreMemDebugUnlock(void) {
# if JREMEMDEBUG_OVERHEAD_ENABLED
      if (!JreMemDebugEnabled)
        return;

      JreMemDebugUnlockInternal();
# endif
    }

    // Analysis will ignore all previous allocations from the
    // time this function is called.
    FOUNDATION_EXPORT void JreMemDebugMarkAllocations(void);

    // This function will generate an analysis report in
    // ~/Library/Logs/J2Objc
    // A .dot file (graphviz format) and a .log file will be generated.
    //
    // The following command line can be used to generate visual output.
    // $ dot -Tsvg -ooutput.svg input.dot
    // See http://www.graphviz.org/ for more information.
    FOUNDATION_EXPORT void JreMemDebugGenerateAllocationsReport(void);

#endif

#endif // _JreMemDebug_H_
