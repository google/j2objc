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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class OptionalDoubleTest extends TestCase {
    public void testEmpty_sameInstance() {
        assertSame(OptionalDouble.empty(), OptionalDouble.empty());
    }

    public void testGet() {
        assertEquals(56.0, OptionalDouble.of(56.0).getAsDouble());

        try {
            OptionalDouble.empty().getAsDouble();
            fail();
        } catch (NoSuchElementException nsee) {
        }
    }

    public void testIsPresent() {
        assertTrue(OptionalDouble.of(56.0).isPresent());
        assertFalse(OptionalDouble.empty().isPresent());
    }

    public void testIfPresent() {
        DoubleConsumer alwaysFails = value -> fail();
        OptionalDouble.empty().ifPresent(alwaysFails);

        final AtomicReference<Double> reference = new AtomicReference<>();
        DoubleConsumer recorder = value -> reference.set(value);
        OptionalDouble.of(56.0).ifPresent(recorder);
        assertEquals(56.0, reference.get().doubleValue());
    }

    public void testOrElse() {
        assertEquals(57.0, OptionalDouble.empty().orElse(57.0));
        assertEquals(56.0, OptionalDouble.of(56.0).orElse(57.0));
    }

    public void testOrElseGet() {
        DoubleSupplier alwaysFails = () -> { fail(); return 57.0; };
        assertEquals(56.0, OptionalDouble.of(56.0).orElseGet(alwaysFails));

        DoubleSupplier supplies57 = () -> 57.0;
        assertEquals(57.0, OptionalDouble.empty().orElseGet(supplies57));
    }

    public void testOrElseThrow() throws IOException {
        final IOException bar = new IOException("bar");

        Supplier<IOException> barSupplier = () -> bar;
        assertEquals(57.0, OptionalDouble.of(57.0).orElseThrow(barSupplier));

        try {
            OptionalDouble.empty().orElseThrow(barSupplier);
            fail();
        } catch (IOException expected) {
            assertSame(bar, expected);
        }
    }

    public void testEquals() {
        assertEquals(OptionalDouble.empty(), OptionalDouble.empty());
        assertEquals(OptionalDouble.of(56.0), OptionalDouble.of(56.0));
        assertFalse(OptionalDouble.empty().equals(OptionalDouble.of(56.0)));
        assertFalse(OptionalDouble.of(57.0).equals(OptionalDouble.of(56.0)));
    }

    public void testHashCode() {
        assertEquals(Double.hashCode(57.0), OptionalDouble.of(57.0).hashCode());
    }
}
