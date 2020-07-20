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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

/**
 * Additional tests for {@link OffsetDateTime}.
 *
 * @see tck.java.time.TCKOffsetDateTime
 * @see test.java.time.TestOffsetDateTime
 * @see test.java.time.TestOffsetDateTime_instants
 */
public class OffsetDateTimeTest {

    private static final OffsetDateTime ODT =
            OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);
    //                        2000-01-02T03:04:05.000000006 UTC

    @Test
    public void test_plus() {
        // Most of the logic is in LocalDateTime, to which OffsetDateTime#plus() delegates, verify
        // only some simple cases here. In-depth tests for LocalDateTime#plus() can be found in
        // TCKLocalDateTime.
        assertEquals(OffsetDateTime.of(2000, 1, 2, 4, 4, 5, 6, ZoneOffset.UTC),
                ODT.plus(1, ChronoUnit.HOURS));
        assertEquals(OffsetDateTime.of(2000, 1, 3, 2, 4, 5, 6, ZoneOffset.UTC),
                ODT.plus(23, ChronoUnit.HOURS));
        assertEquals(OffsetDateTime.of(2000, 1, 2, 3, 5, 5, 6, ZoneOffset.UTC),
                ODT.plus(1, ChronoUnit.MINUTES));
        assertEquals(OffsetDateTime.of(2000, 1, 2, 3, 5, 5, 6, ZoneOffset.UTC),
                ODT.plus(60, ChronoUnit.SECONDS));
        assertEquals(OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 1_000_006, ZoneOffset.UTC),
                ODT.plus(1, ChronoUnit.MILLIS));
        assertEquals(OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 7, ZoneOffset.UTC),
                ODT.plus(1, ChronoUnit.NANOS));
    }
}
