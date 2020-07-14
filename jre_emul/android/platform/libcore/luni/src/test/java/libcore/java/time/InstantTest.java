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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.junit.Assert.assertEquals;

/**
 * Additional tests for {@link Instant}.
 *
 * @see tck.java.time.TCKInstant
 * @see test.java.time.TestInstant
 */
public class InstantTest {

    @Test
    public void test_isSupported_TemporalUnit() {
        assertEquals(false, Instant.EPOCH.isSupported((TemporalUnit) null));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.NANOS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.MICROS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.MILLIS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.SECONDS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.MINUTES));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.HOURS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.HALF_DAYS));
        assertEquals(true, Instant.EPOCH.isSupported(ChronoUnit.DAYS));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.WEEKS));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.MONTHS));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.YEARS));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.DECADES));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.CENTURIES));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.MILLENNIA));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.ERAS));
        assertEquals(false, Instant.EPOCH.isSupported(ChronoUnit.FOREVER));
    }
}
