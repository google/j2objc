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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OptionalTest extends TestCase {

    public void testEmpty_sameInstance() {
        // empty() is a singleton instance.
        assertSame(Optional.<Integer>empty(), Optional.<String>empty());
        assertSame(Optional.<String>empty(), Optional.<String>empty());

        // Note that we assert here that the empty() optional is the same instance
        // as Optional.ofNullable(null). This allows us to avoid having to write tests
        // for both cases.
        assertSame(Optional.<String>empty(), Optional.ofNullable(null));
    }

    public void testGet() {
        Optional<String> empty = Optional.empty();

        try {
            empty.get();
            fail();
        } catch (NoSuchElementException expected) {
        }

        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        assertSame(foo, optionalFoo.get());
    }

    public void testIsPresent() {
        Optional<String> empty = Optional.empty();
        assertFalse(empty.isPresent());

        Optional<String> optionalFoo = Optional.of("foo");
        assertTrue(optionalFoo.isPresent());

        assertFalse(Optional.<String>ofNullable(null).isPresent());
    }

    public void testIfPresent() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        Consumer<String> alwaysFail = s -> fail();

        // alwaysFail must never be called.
        empty.ifPresent(alwaysFail);
        ofNull.ifPresent(alwaysFail);

        final AtomicReference<String> reference = new AtomicReference<>();

        String foo = "foo";
        Optional.of(foo).ifPresent(s -> reference.set(s));
        assertSame(foo, reference.get());
    }

    public void testFilter() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        Predicate<String> alwaysFail = s -> { fail(); return true; };
        // If isPresent() == false, optional always returns itself (!!).
        assertSame(empty, empty.filter(alwaysFail));
        assertSame(empty, empty.filter(alwaysFail));
        assertSame(ofNull, ofNull.filter(alwaysFail));
        assertSame(ofNull, ofNull.filter(alwaysFail));

        final String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        Predicate<String> alwaysTrue = s -> true;
        Predicate<String> alwaysFalse = s -> false;
        assertSame(empty, optionalFoo.filter(alwaysFalse));
        assertSame(optionalFoo, optionalFoo.filter(alwaysTrue));

        final AtomicReference<String> reference = new AtomicReference<>();
        optionalFoo.filter(s -> { reference.set(s); return true; });
        assertSame(foo, reference.get());
    }

    public void testMap() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        Function<String, String> alwaysFail = s -> { fail(); return ""; };
        // Should return Optional.empty() if the value isn't present.
        assertSame(empty, empty.map(alwaysFail));
        assertSame(empty, ofNull.map(alwaysFail));

        final AtomicReference<String> reference = new AtomicReference<>();
        Function<String, String> identity = (String s) -> { reference.set(s); return s; };
        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        Optional<String> mapped = optionalFoo.map(identity);
        assertSame(foo, mapped.get());
        assertSame(foo, reference.get());

        Function<String, String> alwaysNull = s -> null;
        assertSame(empty, optionalFoo.map(alwaysNull));
    }

    public void testFlatMap() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        Function<String, Optional<String>> alwaysFail = s -> { fail(); return Optional.empty(); };
        // Should return Optional.empty() if the value isn't present.
        assertSame(empty, empty.flatMap(alwaysFail));
        assertSame(empty, ofNull.flatMap(alwaysFail));

        final AtomicReference<String> reference = new AtomicReference<>();
        Function<String, Optional<String>> identity =
                s -> { reference.set(s); return Optional.of(s); };

        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        Optional<String> mapped = optionalFoo.flatMap(identity);
        assertSame(foo, mapped.get());
        assertSame(foo, reference.get());

        Function<String, Optional<String>> alwaysNull = s -> null;

        try {
            optionalFoo.flatMap(alwaysNull);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            optionalFoo.flatMap(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testOrElse() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        String bar = "bar";
        assertSame(bar, empty.orElse(bar));
        assertSame(bar, ofNull.orElse(bar));

        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        assertSame(foo, optionalFoo.orElse(bar));
    }

    public void testOrElseGet() {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        final String bar = "bar";
        Supplier<String> barSupplier = () -> bar;

        assertSame(bar, empty.orElseGet(barSupplier));
        assertSame(bar, ofNull.orElseGet(barSupplier));

        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        assertSame(foo, optionalFoo.orElseGet(barSupplier));
    }

    public void testOrElseThrow() throws Exception {
        Optional<String> empty = Optional.empty();
        Optional<String> ofNull = Optional.ofNullable(null);

        final IOException bar = new IOException("bar");
        Supplier<IOException> barSupplier = () -> bar;
        try {
            empty.orElseThrow(barSupplier);
            fail();
        } catch (IOException ioe) {
            assertSame(bar, ioe);
        }

        try {
            ofNull.orElseThrow(barSupplier);
            fail();
        } catch (IOException ioe) {
            assertSame(bar, ioe);
        }

        String foo = "foo";
        Optional<String> optionalFoo = Optional.of(foo);
        assertSame(foo, optionalFoo.orElseThrow(barSupplier));
    }

    public void testEquals() {
        assertEquals(Optional.empty(), Optional.<String>ofNullable(null));
        assertEquals(Optional.of("foo"), Optional.of("foo"));

        assertFalse(Optional.of("foo").equals(Optional.empty()));
        assertFalse(Optional.of("foo").equals(Optional.of("bar")));
    }

    public void testHashcode() {
        assertEquals("foo".hashCode(), Optional.of("foo").hashCode());
    }
}
