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
 * limitations under the License.
 */
package libcore.java.time.temporal;

import org.junit.Test;
import java.time.temporal.UnsupportedTemporalTypeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests for {@link UnsupportedTemporalTypeException}.
 */
public class UnsupportedTemporalTypeExceptionTest {
    @Test
    public void test_constructor_message() {
        UnsupportedTemporalTypeException ex = new UnsupportedTemporalTypeException("message");
        assertEquals("message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void test_constructor_message_cause() {
        Throwable cause = new Exception();
        UnsupportedTemporalTypeException ex =
                new UnsupportedTemporalTypeException("message", cause);
        assertEquals("message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }


}
