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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;

import static org.junit.Assert.assertEquals;

/**
 * Additional tests for {@link ZoneOffsetTransition}.
 *
 * @see tck.java.time.zone.TCKZoneOffsetTransition
 */
public class ZoneOffsetTransitionTest {

    @Test
    public void test_toEpochSeconds() {
        LocalDateTime time = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0);
        ZoneOffset offsetP1 = ZoneOffset.ofHours(1);
        ZoneOffset offsetP2 = ZoneOffset.ofHours(2);
        ZoneOffsetTransition transition = ZoneOffsetTransition.of(time,
                /* offsetBefore */ offsetP1, /* offsetAfter */ offsetP2);
        // toEpochSeconds must match the toEpochSeconds of the original time at the "offset before".
        assertEquals(
                OffsetDateTime.of(time, offsetP1).toEpochSecond(),
                transition.toEpochSecond());

    }

}
