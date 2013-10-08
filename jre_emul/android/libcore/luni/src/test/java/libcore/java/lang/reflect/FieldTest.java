/*
 * Copyright (C) 2008 The Android Open Source Project
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

package libcore.java.lang.reflect;

import java.lang.reflect.Field;
import junit.framework.TestCase;

public final class FieldTest extends TestCase {
    private static final long MY_LONG = 5073258162644648461L;

    // Reflection for static long fields was broken http://b/1120750
    public void testLongFieldReflection() throws Exception {
        Field field = getClass().getDeclaredField("MY_LONG");
        assertEquals(5073258162644648461L, field.getLong(null));
    }
}
