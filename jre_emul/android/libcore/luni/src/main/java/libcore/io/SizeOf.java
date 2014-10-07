/*
 * Copyright (C) 2010 The Android Open Source Project
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

/*-[
#import "java/lang/AssertionError.h"
]-*/

public final class SizeOf {
    public static final int CHAR = sizeOf('C');
    public static final int DOUBLE = sizeOf('D');
    public static final int FLOAT = sizeOf('F');
    public static final int INT = sizeOf('I');
    public static final int LONG = sizeOf('J');
    public static final int SHORT = sizeOf('S');

    private static native int sizeOf(char type) /*-[
      switch (type) {
        case 'C': return sizeof(unichar);
        case 'D': return sizeof(double);
        case 'F': return sizeof(float);
        case 'I': return sizeof(int);
        case 'J': return sizeof(long long);
        case 'S': return sizeof(short);
        default:
          @throw AUTORELEASE([[JavaLangAssertionError alloc] init]);
          return -1;  // never returned
      }
    ]-*/;

    private SizeOf() {
    }
}
