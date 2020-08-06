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
package libcore.java.time.zone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import android.icu.util.BasicTimeZone;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesProvider;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Test the {@link java.time.zone.IcuZoneRulesProvider}.
 *
 * It is indirectly tested via static methods in {@link ZoneRulesProvider} as all the relevant
 * methods are protected. This test verifies that the rules returned by that provider behave
 * equivalently to the ICU rules from which they are created.
 */
@RunWith(Parameterized.class)
public class IcuZoneRulesProviderTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<String> getZoneIds() {
        Set<String> availableZoneIds = ZoneRulesProvider.getAvailableZoneIds();
        assertFalse("no zones returned", availableZoneIds.isEmpty());
        return availableZoneIds;
    }

    private final String zoneId;

    public IcuZoneRulesProviderTest(final String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Verifies that ICU and java.time return the same transitions before and after a pre-selected
     * set of instants in time.
     */
    @Test
    public void testTransitionsNearInstants() {
        // An arbitrary set of instants at which to test the offsets in both implementations.
        Instant[] instants = new Instant[] {
                LocalDateTime.of(1900, Month.DECEMBER, 24, 12, 0).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(1970, Month.JANUARY, 1, 2, 3).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(1980, Month.FEBRUARY, 4, 5, 6).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(1990, Month.MARCH, 7, 8, 9).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2000, Month.APRIL, 10, 11, 12).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2016, Month.MAY, 13, 14, 15).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2020, Month.JUNE, 16, 17, 18).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2100, Month.JULY, 19, 20, 21).toInstant(ZoneOffset.UTC),
                // yes, adding "now" makes the test time-dependent, but it also ensures that future
                // updates don't break on the then-current date.
                Instant.now()
        };
        // Coincidentally this test verifies that all zones can be converted to ZoneRules and
        // don't violate any of the assumptions of IcuZoneRulesProvider.
        ZoneRules rules = ZoneRulesProvider.getRules(zoneId, false);
        BasicTimeZone timeZone = (BasicTimeZone) TimeZone.getTimeZone(zoneId);

        int[] icuOffsets = new int[2];
        for (Instant instant : instants) {
            ZoneOffset offset = rules.getOffset(instant);
            Duration daylightSavings = rules.getDaylightSavings(instant);
            timeZone.getOffset(instant.toEpochMilli(), false, icuOffsets);

            assertEquals("total offset for " + zoneId + " at " + instant,
                    icuOffsets[1] + icuOffsets[0], offset.getTotalSeconds() * 1000);
            assertEquals("dst offset for " + zoneId + " at " + instant,
                    icuOffsets[1], daylightSavings.toMillis());

            ZoneOffsetTransition jtTrans;
            TimeZoneTransition icuTrans;

            jtTrans = rules.nextTransition(instant);
            icuTrans = timeZone.getNextTransition(instant.toEpochMilli(), false);
            while (isIcuOnlyTransition(icuTrans)) {
                icuTrans = timeZone.getNextTransition(icuTrans.getTime(), false);
            }
            assertEquivalent(icuTrans, jtTrans);

            jtTrans = rules.previousTransition(instant);
            icuTrans = timeZone.getPreviousTransition(instant.toEpochMilli(), false);
            // Find previous "real" transition.
            while (isIcuOnlyTransition(icuTrans)) {
                icuTrans = timeZone.getPreviousTransition(icuTrans.getTime(), false);
            }
            assertEquivalent(icuTrans, jtTrans);
        }
    }

    /**
     * Verifies that ICU and java.time rules return the same transitions between 1900 and 2100.
     */
    @Test
    public void testAllTransitions() {
        final Instant start = LocalDateTime.of(1900, Month.JANUARY, 1, 12, 0)
                .toInstant(ZoneOffset.UTC);
        // Many timezones have ongoing DST changes, so they would generate transitions endlessly.
        // Pick a far-future end date to stop comparing in that case.
        final Instant end = LocalDateTime.of(2100, Month.DECEMBER, 31, 12, 0)
                .toInstant(ZoneOffset.UTC);

        ZoneRules rules = ZoneRulesProvider.getRules(zoneId, false);
        BasicTimeZone timeZone = (BasicTimeZone) TimeZone.getTimeZone(zoneId);

        Instant instant = start;
        while (instant.isBefore(end)) {
            ZoneOffsetTransition jtTrans;
            TimeZoneTransition icuTrans;

            jtTrans = rules.nextTransition(instant);
            icuTrans = timeZone.getNextTransition(instant.toEpochMilli(), false);
            while (isIcuOnlyTransition(icuTrans)) {
                icuTrans = timeZone.getNextTransition(icuTrans.getTime(), false);
            }
            assertEquivalent(icuTrans, jtTrans);
            if (jtTrans == null) {
                break;
            }
            instant = jtTrans.getInstant();
        }
    }

    /**
     * Returns {@code true} iff this transition will only be returned by ICU code.
     * ICU reports "no-op" transitions where the raw offset and the dst savings
     * change by the same absolute value in opposite directions, java.time doesn't
     * return them, so find the next "real" transition.
     */
    private static boolean isIcuOnlyTransition(TimeZoneTransition transition) {
        if (transition == null) {
            return false;
        }
        return transition.getFrom().getRawOffset() + transition.getFrom().getDSTSavings()
                == transition.getTo().getRawOffset() + transition.getTo().getDSTSavings();
    }

    /**
     * Asserts that the ICU {@link TimeZoneTransition} is equivalent to the java.time {@link
     * ZoneOffsetTransition}.
     */
    private static void assertEquivalent(
            TimeZoneTransition icuTransition, ZoneOffsetTransition jtTransition) {
        if (icuTransition == null) {
            assertNull(jtTransition);
            return;
        }
        assertEquals("time of transition",
                Instant.ofEpochMilli(icuTransition.getTime()), jtTransition.getInstant());
        TimeZoneRule from = icuTransition.getFrom();
        TimeZoneRule to = icuTransition.getTo();
        assertEquals("offset before",
                (from.getDSTSavings() + from.getRawOffset()) / 1000,
                jtTransition.getOffsetBefore().getTotalSeconds());
        assertEquals("offset after",
                (to.getDSTSavings() + to.getRawOffset()) / 1000,
                jtTransition.getOffsetAfter().getTotalSeconds());
    }
}
