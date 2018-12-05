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

package libcore.java.lang.annotation;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Method;

public class AnnotationTypeMismatchExceptionTest extends junit.framework.TestCase {
    public void testGetters() throws Exception {
        Method m = String.class.getMethod("length");
        AnnotationTypeMismatchException ex = new AnnotationTypeMismatchException(m, "poop");
        assertSame(m, ex.element());
        assertEquals("poop", ex.foundType());
    }

    public void testSerialization() throws Exception {
        Method m = String.class.getMethod("length");
        AnnotationTypeMismatchException original = new AnnotationTypeMismatchException(m, "poop");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // AnnotationTypeMismatchException is broken: it's Serializable but has a non-transient
            // non-serializable field of type Method.
            new ObjectOutputStream(out).writeObject(original);
            fail();
        } catch (NotSerializableException expected) {
        }
    }
}
