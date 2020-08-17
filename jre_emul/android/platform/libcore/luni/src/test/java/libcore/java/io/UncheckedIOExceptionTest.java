/*
 * Copyright (C) 2016 The Android Open Source Project
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

package libcore.java.io;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedIOExceptionTest extends TestCase {

    /**
     * java.lang.UncheckedIOException#UncheckedIOException(java.io.IOException)
     */
    public void test_ConstructorLjava_lang_IOException() {
        IOException ioException = new IOException();
        UncheckedIOException e = new UncheckedIOException(ioException);
        assertEquals("java.io.IOException", e.getMessage());
        assertSame(ioException, e.getCause());
    }

    /**
     * java.lang.UncheckedIOException#UncheckedIOException(java.lang.String, java.io.IOException)
     */
    public void test_ConstructorLjava_lang_String_IOException() {
        IOException ioException = new IOException();
        UncheckedIOException e = new UncheckedIOException("errmsg", ioException);
        assertEquals("errmsg", e.getMessage());
        assertSame(ioException, e.getCause());
    }

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new UncheckedIOException(new IOException()));
    }
}
