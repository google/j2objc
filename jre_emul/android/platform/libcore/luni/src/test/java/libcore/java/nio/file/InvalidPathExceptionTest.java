/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.nio.file.InvalidPathException;

public class InvalidPathExceptionTest extends TestCase {

    public void test_Constructor$String$String$Int() {
        String reason = "reason";
        String input = "input";
        int index = 0;

        InvalidPathException exception = new InvalidPathException(input, reason, index);
        assertEquals(index, exception.getIndex());
        assertEquals(reason, exception.getReason());
        assertEquals(input, exception.getInput());

        // Test the case where index = -1.
        index = -1;
        exception = new InvalidPathException(input, reason, index);
        assertEquals(index, exception.getIndex());
        assertEquals(reason, exception.getReason());
        assertEquals(input, exception.getInput());

        // Test the case where index < -1;
        index = -2;
        try {
            new InvalidPathException(input, reason, index);
            fail();
        } catch (IllegalArgumentException expected) {}

        // Test the case where input is null, reason is not null and index >= -1.
        try {
            index = 0;
            new InvalidPathException(null, reason, index);
            fail();
        } catch (NullPointerException expected) {}

        // Test the case where input is null, reason is not null and index < -1.
        try {
            index = -1;
            new InvalidPathException(null, reason, index);
            fail();
        } catch (NullPointerException expected) {}

        // Test the case where reason is null, input is not null and index >= -1.
        try {
            index = 0;
            new InvalidPathException(input, null, index);
            fail();
        } catch (NullPointerException expected) {}

        // Test the case where input is not null, reason is null and index < -1.
        try {
            index = -1;
            new InvalidPathException(input, null, index);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_Constructor$String$String() {
        String reason = "reason";
        String input = "input";

        InvalidPathException exception = new InvalidPathException(input, reason);
        assertEquals(-1, exception.getIndex());
        assertEquals(reason, exception.getReason());
        assertEquals(input, exception.getInput());

        // Test the case where input is null and reason is not null.
        try {
            new InvalidPathException(null, reason);
            fail();
        } catch (NullPointerException expected) {}

        // Test the case where reason is null and input is not null.
        try {
            new InvalidPathException(input, null);
            fail();
        } catch (NullPointerException expected) {}
    }
}
