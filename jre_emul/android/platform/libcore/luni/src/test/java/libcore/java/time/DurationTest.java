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
package libcore.java.time;

import org.junit.Test;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
/* J2ObjC removed: Only "gregorian" and "julian" calendars are supported.
import java.time.chrono.MinguoChronology; */
import java.time.temporal.Temporal;
import java.time.temporal.UnsupportedTemporalTypeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Additional tests for {@link Duration}.
 *
 * @see tck.java.time.TCKDuration
 * @see test.java.time.TestDuration
 */
public class DurationTest {

    // Hardcoded maximum representable duration.
    public static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999);

    @Test
    public void test_addTo() {
        assertSame(Instant.EPOCH, Duration.ZERO.addTo(Instant.EPOCH));

        // These tests are a little tautological, but since Duration.between is well-tested,
        // they are still valuable.
        assertEquals(Instant.MAX,
                Duration.between(Instant.EPOCH, Instant.MAX).addTo(Instant.EPOCH));
        assertEquals(Instant.MAX,
                Duration.between(Instant.MIN, Instant.MAX).addTo(Instant.MIN));
        assertEquals(Instant.EPOCH,
                Duration.between(Instant.MIN, Instant.EPOCH).addTo(Instant.MIN));
    }

    @Test
    public void test_subtractFrom() {
        assertSame(Instant.EPOCH, Duration.ZERO.subtractFrom(Instant.EPOCH));
        assertEquals(Instant.MIN,
                Duration.between(Instant.MIN, Instant.EPOCH).subtractFrom(Instant.EPOCH));
        assertEquals(Instant.MIN,
                Duration.between(Instant.MIN, Instant.MAX).subtractFrom(Instant.MAX));
        assertEquals(Instant.EPOCH,
                Duration.between(Instant.EPOCH, Instant.MAX).subtractFrom(Instant.MAX));
    }

    @Test
    public void test_addTo_exceeds() {
        Object[][] breakingValues = new Object[][] {
                { Instant.EPOCH, Duration.between(Instant.EPOCH, Instant.MAX).plusNanos(1) },
                // Adding a negative duration is the same as subtracting the negated value.
                { Instant.EPOCH, Duration.between(Instant.EPOCH, Instant.MIN).minusNanos(1) },
                { Instant.EPOCH, Duration.between(Instant.MIN, Instant.MAX) },
                { Instant.EPOCH, MAX_DURATION },
                { Instant.MIN, MAX_DURATION },
                { Instant.MAX, Duration.ofNanos(1) },
                { LocalDateTime.MAX, Duration.ofNanos(1) },
                { LocalDateTime.now(), MAX_DURATION },
                { ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC ), Duration.ofNanos(1) },
        };

        for (Object[] values : breakingValues) {
            Temporal temporal = (Temporal) values[0];
            Duration duration = (Duration) values[1];

            try {
                duration.addTo(temporal);
                fail(" Should have failed to add " + duration + " to " + temporal);
            } catch (DateTimeException expected) {
            }
        }
    }

    @Test
    public void test_subtractFrom_exceeds() {
        Object[][] breakingValues = new Object[][] {
                { Instant.EPOCH, Duration.between(Instant.MIN, Instant.EPOCH).plusNanos(1) },
                // Subtracting a negative Duration is the same as adding the negated value.
                { Instant.EPOCH, Duration.between(Instant.MAX, Instant.EPOCH).minusNanos(1) },
                { Instant.EPOCH, Duration.between(Instant.MIN, Instant.MAX) },
                { Instant.EPOCH, MAX_DURATION },
                { Instant.MAX, MAX_DURATION },
                { Instant.MIN, Duration.ofNanos(1) },
                { LocalDateTime.MIN, Duration.ofNanos(1) },
                { LocalDateTime.now(), MAX_DURATION },
                { LocalDateTime.MAX, MAX_DURATION },
                { ZonedDateTime.of(LocalDateTime.MIN, ZoneOffset.UTC ), Duration.ofNanos(1) },
        };

        for (Object[] values : breakingValues) {
            Temporal temporal = (Temporal) values[0];
            Duration duration = (Duration) values[1];

            try {
                duration.subtractFrom(temporal);
                fail("Should have failed to subtract " + duration + " from " + temporal);
            } catch (DateTimeException expected) {
            }
        }
    }

    /* J2ObjC removed: Only "gregorian" and "julian" calendars are supported.
    @Test
    public void test_addTo_subtractFrom_unsupported() {
        // These Temporal objects don't supports seconds/nanos.
        // The actual values of those Temporals don't matter, only their type is checked.
        Temporal[] unsupportedTemporals = new Temporal[] {
                Year.now(),
                YearMonth.now(),
                LocalDate.now(),
                // An arbitrary ChronoLocalDateImpl as a representative example.
                MinguoChronology.INSTANCE.dateNow(),
        };

        Duration second = Duration.ofSeconds(1);
        for (Temporal temporal : unsupportedTemporals) {
            // Adding/subtracting zero should not fail.
            assertSame(temporal, Duration.ZERO.addTo(temporal));
            assertSame(temporal, Duration.ZERO.subtractFrom(temporal));

            try {
                second.addTo(temporal);
                fail("Should not be able to add a duration  to " + temporal);
            } catch (UnsupportedTemporalTypeException expected) {
            }
            try {
                second.subtractFrom(temporal);
                fail("Should not be able to subtract a duration  from " + temporal);
            } catch (UnsupportedTemporalTypeException expected) {
            }
        }

    } */
}
