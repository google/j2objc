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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;


/**
 * Additional tets for {@link ZonedDateTime}.
 *
 * @see tck.java.time.TCKZonedDateTime
 * @see test.java.time.TestZonedDateTime
 */
public class ZonedDateTimeTest {

    // Europe/Vienna is UTC+2 during summer, UTC+1 during winter.
    private static final ZoneId ZONE_VIENNA = ZoneId.of("Europe/Vienna");

    // UTC+1, the offset during winter time.
    private static final ZoneOffset OFFSET_P1 = ZoneOffset.ofHours(1);

    // UTC+2, the offset during summer time.
    private static final ZoneOffset OFFSET_P2 = ZoneOffset.ofHours(2);

    // LocalDateTime during winter time (OFFSET_P1 in ZONE_VIENNA).
    private static final LocalDateTime LDT_P1 = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0);

    // LocalDateTime during summer time (OFFSET_P2 in ZONE_VIENNA).
    private static final LocalDateTime LDT_P2 = LocalDateTime.of(2000, Month.JUNE, 1, 0, 0);

    // LocalDateTime that is in a gap that occurs at the switch from winter time to summer time.
    // This is not a valid local time in ZONE_VIENNA.
    private static final LocalDateTime LDT_IN_GAP = LocalDateTime.of(2000, Month.MARCH, 26, 2, 30);

    // LocalDateTime that is in an overlap that occurs at the switch from summer time to winter
    // time. This LDT actually occurs twice in ZONE_VIENNA.
    private static final LocalDateTime LDT_IN_OVERLAP =
            LocalDateTime.of(2000, Month.OCTOBER, 29, 2, 30);

    @Test
    public void test_ofInstant() {
        // ofInstant behaves as if it calculated an Instant from the LocalDateTime/ZoneOffset
        // and then calling ofInstant(Instant, ZoneId). That's why "invalid" zone offsets are
        // tolerated and basically just change how the LocalDateTime is interpreted.

        // checkOfInstant(localDateTime, offset, zone, expectedDateTime, expectedOffset)

        // Correct offset in summer.
        checkOfInstant(LDT_P1, OFFSET_P1, ZONE_VIENNA, LDT_P1, OFFSET_P1);
        // Correct offset in winter.
        checkOfInstant(LDT_P2, OFFSET_P2, ZONE_VIENNA, LDT_P2, OFFSET_P2);
        // "Wrong" offset in winter.
        checkOfInstant(LDT_P1, OFFSET_P2, ZONE_VIENNA, LDT_P1.minusDays(1).withHour(23), OFFSET_P1);
        // "Wrong" offset in summer.
        checkOfInstant(LDT_P2, OFFSET_P1, ZONE_VIENNA, LDT_P2.withHour(1), OFFSET_P2);

        // Very wrong offset in winter.
        checkOfInstant(LDT_P1, ZoneOffset.ofHours(-10), ZONE_VIENNA, LDT_P1.withHour(11),
                OFFSET_P1);

        // Neither of those combinations exist, so they are interpreted as either before or after
        // the gap, depending on the offset.
        checkOfInstant(LDT_IN_GAP, OFFSET_P1, ZONE_VIENNA, LDT_IN_GAP.plusHours(1), OFFSET_P2);
        checkOfInstant(LDT_IN_GAP, OFFSET_P2, ZONE_VIENNA, LDT_IN_GAP.minusHours(1), OFFSET_P1);

        // Both combinations exist and are valid, so they produce exactly the input.
        checkOfInstant(LDT_IN_OVERLAP, OFFSET_P1, ZONE_VIENNA, LDT_IN_OVERLAP, OFFSET_P1);
        checkOfInstant(LDT_IN_OVERLAP, OFFSET_P2, ZONE_VIENNA, LDT_IN_OVERLAP, OFFSET_P2);
    }

    /**
     * Assert that calling {@link ZonedDateTime#ofInstant(LocalDateTime, ZoneOffset, ZoneId)} with
     * the first three parameters produces a sane result with the localDateTime and offset equal to
     * the last two.
     */
    private static void checkOfInstant(LocalDateTime localDateTime, ZoneOffset offset,
            ZoneId zone, LocalDateTime expectedDateTime, ZoneOffset expectedOffset) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(localDateTime, offset, zone);
        String message = String.format(" for ofInstant(%s, %s, %s) = %s, ",
                localDateTime, offset, zone, zonedDateTime);
        // Note that localDateTime doesn't necessarily equal zoneDateTime.toLocalDateTime(),
        // specifically when offset is not a valid offset for zone at localDateTime (or ever).
        assertEquals("zone" + message, zone, zonedDateTime.getZone());

        assertEquals("offset" + message, expectedOffset, zonedDateTime.getOffset());
        assertEquals("localDateTime" + message, expectedDateTime, zonedDateTime.toLocalDateTime());
        if (offset.equals(expectedOffset)) {
            // When we get same offset, the localDateTime must be the same as the input. This
            // assert basically just verifies that the test is written correctly.
            assertEquals("expected localDateTime" + message,
                    expectedDateTime, zonedDateTime.toLocalDateTime());
        }
    }

    @Test(expected = NullPointerException.class)
    public void test_ofInstant_localDateTime_null() {
        ZonedDateTime.ofInstant(null, OFFSET_P1, ZONE_VIENNA);
    }

    @Test(expected = NullPointerException.class)
    public void test_ofInstant_offset_null() {
        ZonedDateTime.ofInstant(LDT_P1, null, ZONE_VIENNA);
    }

    @Test(expected = NullPointerException.class)
    public void test_ofInstant_zone_null() {
        ZonedDateTime.ofInstant(LDT_P1, OFFSET_P1, null);
    }

    @Test
    public void test_ofLocal() {
        // checkOfLocal(localDateTime, zone, preferredOffset, expectedDateTime, expectedOffset)

        // Correct offset in summer.
        checkOfLocal(LDT_P1, ZONE_VIENNA, OFFSET_P1, LDT_P1, OFFSET_P1);
        // Correct offset in winter.
        checkOfLocal(LDT_P2, ZONE_VIENNA, OFFSET_P2, LDT_P2, OFFSET_P2);
        // "Wrong" offset in winter.
        checkOfLocal(LDT_P1, ZONE_VIENNA, OFFSET_P2, LDT_P1, OFFSET_P1);
        // "Wrong" offset in summer.
        checkOfLocal(LDT_P2, ZONE_VIENNA, OFFSET_P1, LDT_P2, OFFSET_P2);
        // Very wrong offset in winter.
        checkOfLocal(LDT_P1, ZONE_VIENNA, ZoneOffset.ofHours(-10), LDT_P1, OFFSET_P1);

        // Neither of those combinations exist, so they are interpreted as after the gap.
        checkOfLocal(LDT_IN_GAP, ZONE_VIENNA, OFFSET_P1, LDT_IN_GAP.plusHours(1), OFFSET_P2);
        checkOfLocal(LDT_IN_GAP, ZONE_VIENNA, OFFSET_P2, LDT_IN_GAP.plusHours(1), OFFSET_P2);

        // Both combinations exist and are valid, so they produce exactly the input.
        checkOfLocal(LDT_IN_OVERLAP, ZONE_VIENNA, OFFSET_P1, LDT_IN_OVERLAP, OFFSET_P1);
        checkOfLocal(LDT_IN_OVERLAP, ZONE_VIENNA, OFFSET_P2, LDT_IN_OVERLAP, OFFSET_P2);

        // Passing in null for preferredOffset will be biased to the offset before the transition.
        checkOfLocal(LDT_IN_OVERLAP, ZONE_VIENNA, /* preferredOffset */ null,
                LDT_IN_OVERLAP, OFFSET_P2);
        // Passing in an invalid offset will be biased to the offset before the transition.
        checkOfLocal(LDT_IN_OVERLAP, ZONE_VIENNA, ZoneOffset.ofHours(10),
                LDT_IN_OVERLAP, OFFSET_P2);
    }

    /**
     * Assert that calling {@link ZonedDateTime#ofLocal(LocalDateTime, ZoneId, ZoneOffset)} with
     * the first three parameters produces a sane result with the localDateTime, and offset equal
     * to the last two.
     */
    private static void checkOfLocal(LocalDateTime localDateTime, ZoneId zone,
            ZoneOffset preferredOffset, LocalDateTime expectedDateTime, ZoneOffset expectedOffset) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofLocal(localDateTime, zone, preferredOffset);
        String message = String.format(" for ofLocal(%s, %s, %s) = %s, ",
                localDateTime, zone, preferredOffset, zonedDateTime);
        // Note that localDateTime doesn't necessarily equal zoneDateTime.toLocalDateTime(),
        // specifically when offset is not a valid offset for zone at localDateTime (or ever).
        assertEquals("zone" + message, zone, zonedDateTime.getZone());
        assertEquals("offset" + message, expectedOffset, zonedDateTime.getOffset());
        assertEquals("localDateTime" + message, expectedDateTime, zonedDateTime.toLocalDateTime());
    }

    @Test(expected = NullPointerException.class)
    public void test_ofLocal_localDateTime_null() {
        ZonedDateTime.ofLocal(null, ZONE_VIENNA, OFFSET_P1);
    }

    @Test(expected = NullPointerException.class)
    public void test_ofLocal_zone_null() {
        ZonedDateTime.ofLocal(LDT_P1, null, OFFSET_P1);
    }
}
