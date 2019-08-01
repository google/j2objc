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
 * limitations under the License
 */

package libcore.java.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class OptionalLongTest extends TestCase {
    public void testEmpty_sameInstance() {
        assertSame(OptionalLong.empty(), OptionalLong.empty());
    }

    public void testGet() {
        assertEquals(56, OptionalLong.of(56).getAsLong());

        try {
            OptionalLong.empty().getAsLong();
            fail();
        } catch (NoSuchElementException nsee) {
        }
    }

    public void testIsPresent() {
        assertTrue(OptionalLong.of(56).isPresent());
        assertFalse(OptionalLong.empty().isPresent());
    }

    public void testIfPresent() {
        LongConsumer alwaysFails = value -> fail();
        OptionalLong.empty().ifPresent(alwaysFails);

        final AtomicLong reference = new AtomicLong();
        LongConsumer recorder = (long value) -> reference.set(value);
        OptionalLong.of(56).ifPresent(recorder);
        assertEquals(56, reference.get());
    }

    public void testOrElse() {
        assertEquals(57, OptionalLong.empty().orElse(57));
        assertEquals(56, OptionalLong.of(56).orElse(57));
    }

    public void testOrElseGet() {
        LongSupplier alwaysFails = () -> { fail(); return 57; };
        assertEquals(56, OptionalLong.of(56).orElseGet(alwaysFails));

        LongSupplier supplies57 = () -> 57;
        assertEquals(57, OptionalLong.empty().orElseGet(supplies57));
    }

    public void testOrElseThrow() throws IOException {
        final IOException bar = new IOException("bar");

        Supplier<IOException> barSupplier = () -> bar;
        assertEquals(57, OptionalLong.of(57).orElseThrow(barSupplier));

        try {
            OptionalLong.empty().orElseThrow(barSupplier);
            fail();
        } catch (IOException expected) {
            assertSame(bar, expected);
        }
    }

    public void testEquals() {
        assertEquals(OptionalLong.empty(), OptionalLong.empty());
        assertEquals(OptionalLong.of(56), OptionalLong.of(56));
        assertFalse(OptionalLong.empty().equals(OptionalLong.of(56)));
        assertFalse(OptionalLong.of(57).equals(OptionalLong.of(56)));
    }

    public void testHashCode() {
        assertEquals(Long.hashCode(57), OptionalLong.of(57).hashCode());
    }
}
