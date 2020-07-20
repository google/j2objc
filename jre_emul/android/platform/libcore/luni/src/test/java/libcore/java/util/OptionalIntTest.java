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
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class OptionalIntTest extends TestCase {
    public void testEmpty_sameInstance() {
        assertSame(OptionalInt.empty(), OptionalInt.empty());
    }

    public void testGet() {
        assertEquals(56, OptionalInt.of(56).getAsInt());

        try {
            OptionalInt.empty().getAsInt();
            fail();
        } catch (NoSuchElementException nsee) {
        }
    }

    public void testIsPresent() {
        assertTrue(OptionalInt.of(56).isPresent());
        assertFalse(OptionalInt.empty().isPresent());
    }

    public void testIfPresent() {
        IntConsumer alwaysFails = value -> fail();
        OptionalInt.empty().ifPresent(alwaysFails);

        final AtomicInteger reference = new AtomicInteger();
        IntConsumer recorder = value -> reference.set(value);;
        OptionalInt.of(56).ifPresent(recorder);
        assertEquals(56, reference.get());
    }

    public void testOrElse() {
        assertEquals(57, OptionalInt.empty().orElse(57));
        assertEquals(56, OptionalInt.of(56).orElse(57));
    }

    public void testOrElseGet() {
        IntSupplier alwaysFails = () -> { fail(); return 57; };
        assertEquals(56, OptionalInt.of(56).orElseGet(alwaysFails));

        IntSupplier supplies57 = () -> 57;
        assertEquals(57, OptionalInt.empty().orElseGet(supplies57));
    }

    public void testOrElseThrow() throws IOException {
        final IOException bar = new IOException("bar");

        Supplier<IOException> barSupplier = () -> bar;
        assertEquals(57, OptionalInt.of(57).orElseThrow(barSupplier));

        try {
            OptionalInt.empty().orElseThrow(barSupplier);
            fail();
        } catch (IOException expected) {
            assertSame(bar, expected);
        }
    }

    public void testEquals() {
        assertEquals(OptionalInt.empty(), OptionalInt.empty());
        assertEquals(OptionalInt.of(56), OptionalInt.of(56));
        assertFalse(OptionalInt.empty().equals(OptionalInt.of(56)));
        assertFalse(OptionalInt.of(57).equals(OptionalInt.of(56)));
    }

    public void testHashCode() {
        assertEquals(Integer.hashCode(57), OptionalInt.of(57).hashCode());
    }
}
