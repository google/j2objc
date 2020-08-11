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
package libcore.java.time.zone;

import org.junit.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Additional tests for {@link ZoneRules}.
 *
 * @see tck.java.time.zone.TCKZoneRules
 */
public class ZoneRulesTest {

    @Test
    public void test_of_ZoneOffset() {
        ZoneOffset offset = ZoneOffset.MIN;
        ZoneRules zoneRules = ZoneRules.of(offset);

        assertEquals(Collections.emptyList(), zoneRules.getTransitionRules());
        assertEquals(Collections.emptyList(), zoneRules.getTransitions());
        assertNull(zoneRules.nextTransition(Instant.MIN));

        // Check various offsets at a bunch of instants, as they should be constant.
        Instant[] instants = new Instant[] {
                LocalDateTime.MIN.toInstant(offset),
                Instant.EPOCH,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1).toInstant(ZoneOffset.UTC),
                Instant.now(),
                LocalDateTime.MAX.toInstant(offset),
        };

        for (Instant instant : instants) {
            assertEquals(Duration.ZERO, zoneRules.getDaylightSavings(instant));
            assertEquals(offset, zoneRules.getOffset(instant));
            assertEquals(offset, zoneRules.getStandardOffset(instant));
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, offset);
            assertNull(zoneRules.getTransition(localDateTime));
            assertEquals(Collections.singletonList(offset),
                    zoneRules.getValidOffsets(localDateTime));
        }
    }

    @Test(expected = NullPointerException.class)
    public void test_of_ZoneOffset_null() {
        ZoneRules.of(null);
    }
}
