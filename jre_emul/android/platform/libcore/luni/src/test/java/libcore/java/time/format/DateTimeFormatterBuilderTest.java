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
package libcore.java.time.format;

import org.junit.Test;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalQueries;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Additional tests for {@link DateTimeFormatterBuilder}.
 *
 * @see tck.java.time.format.TCKDateTimeFormatterBuilder
 * @see test.java.time.format.TestDateTimeFormatterBuilder
 */
public class DateTimeFormatterBuilderTest {

    @Test
    public void test_append_DateTimeFormatter() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendLiteral('<').append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('>')
                .toFormatter(Locale.ROOT);
        assertEquals("<2000-12-31>", formatter.format(LocalDate.of(2000, Month.DECEMBER, 31)));
    }

    @Test
    public void test_appendZoneRegionId_format() {
        DateTimeFormatter formatter =
                new DateTimeFormatterBuilder().appendZoneRegionId().toFormatter();

        assertEquals("Europe/London",
                formatter.format(ZonedDateTime.now(ZoneId.of("Europe/London"))));
        assertEquals("UTC",
                formatter.format(ZonedDateTime.now(ZoneId.of("UTC"))));
    }

    @Test
    public void test_appendZoneRegionId_format_offset() {
        DateTimeFormatter formatter =
                new DateTimeFormatterBuilder().appendZoneRegionId().toFormatter();

        try {
            formatter.format(OffsetDateTime.now(ZoneOffset.UTC));
            fail("Formatted ZoneOffset using appendZoneRegionId formatter");
        } catch (DateTimeException expected) {
        }
    }

    @Test
    public void test_appendZoneRegionId_parse() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendZoneRegionId()
                .toFormatter();

        assertEquals(ZoneId.of("Europe/London"),
                formatter.parse("Europe/London").query(TemporalQueries.zoneId()));
        assertEquals(ZoneId.of("UTC"),
                formatter.parse("UTC").query(TemporalQueries.zoneId()));
        assertEquals(ZoneId.of("GMT+1"),
                formatter.parse("GMT+01:00").query(TemporalQueries.zoneId()));
        // Note that the JavaDoc for appendZoneRegionId() suggests that this should return a
        // ZoneOffset, but that documentation seems to be wrong (see http://b/35665981).
        assertEquals(ZoneId.of("UTC+01:00"),
                formatter.parse("UTC+01:00").query(TemporalQueries.zoneId()));
        // Parsing a "bare metal" offset without prefix will return a ZoneOffset.
        assertEquals(ZoneOffset.ofHours(1),
                formatter.parse("+01:00").query(TemporalQueries.zoneId()));
    }
}
