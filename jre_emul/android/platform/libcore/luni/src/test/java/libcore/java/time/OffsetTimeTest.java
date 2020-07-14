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
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Additional tests for {@link OffsetTime}.
 *
 * @see tck.java.time.TCKOffsetTime
 * @see test.java.time.TestOffsetTime
 */
public class OffsetTimeTest {

    private static final OffsetTime NOON_UTC = OffsetTime
            .of(/* hour */ 12, /* minute */ 0, /* second */ 0, /* nano */ 0, ZoneOffset.UTC);

    @Test
    public void test_plus() {
        // Most of the logic is in LocalTime, to which OffsetTime#plus() delegates, verify only some
        // simple cases here. In-depth tests for LocalTime#plus() can be found in TCKLocalTime.
        assertEquals(OffsetTime.of(13, 0, 0, 0, ZoneOffset.UTC),
                NOON_UTC.plus(1, ChronoUnit.HOURS));
        assertEquals(OffsetTime.of(11, 0, 0, 0, ZoneOffset.UTC),
                NOON_UTC.plus(23, ChronoUnit.HOURS));
        assertEquals(OffsetTime.of(12, 1, 0, 0, ZoneOffset.UTC),
                NOON_UTC.plus(1, ChronoUnit.MINUTES));
        assertEquals(OffsetTime.of(12, 1, 0, 0, ZoneOffset.UTC),
                NOON_UTC.plus(60, ChronoUnit.SECONDS));
        assertEquals(OffsetTime.of(12, 0, 0, 1_000_000, ZoneOffset.UTC),
                NOON_UTC.plus(1, ChronoUnit.MILLIS));
        assertEquals(OffsetTime.of(12, 0, 0, 1, ZoneOffset.UTC),
                NOON_UTC.plus(1, ChronoUnit.NANOS));
    }

    @Test
    public void test_plus_noop() {
        assertPlusIsNoop(0, ChronoUnit.MINUTES);
        assertPlusIsNoop(2, ChronoUnit.HALF_DAYS);
        assertPlusIsNoop(24, ChronoUnit.HOURS);
        assertPlusIsNoop(24 * 60, ChronoUnit.MINUTES);
        assertPlusIsNoop(24 * 60 * 60, ChronoUnit.SECONDS);
        assertPlusIsNoop(24 * 60 * 60 * 1_000, ChronoUnit.MILLIS);
        assertPlusIsNoop(24 * 60 * 60 * 1_000_000_000L, ChronoUnit.NANOS);
    }

    private static void assertPlusIsNoop(long amount, TemporalUnit unit) {
        assertSame(NOON_UTC, NOON_UTC.plus(amount, unit));
    }

    @Test
    public void test_plus_minus_invalidUnits() {
        for (ChronoUnit unit : EnumSet.range(ChronoUnit.DAYS, ChronoUnit.FOREVER)) {
            try {
                NOON_UTC.plus(1, unit);
                fail("Adding 1 " + unit + " should have failed.");
            } catch (UnsupportedTemporalTypeException expected) {
            }
            try {
                NOON_UTC.minus(1, unit);
                fail("Subtracting 1 " + unit + " should have failed.");
            } catch (UnsupportedTemporalTypeException expected) {
            }
        }
    }
}
