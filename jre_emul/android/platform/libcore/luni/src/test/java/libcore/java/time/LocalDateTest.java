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
import java.time.chrono.IsoChronology;

import static org.junit.Assert.assertSame;

/**
 * Additional tests for {@link LocalDate}.
 *
 * @see tck.java.time.TCKLocalDate
 * @see test.java.time.TestLocalDate
 */
public class LocalDateTest {

    @Test
    public void test_getChronology() {
        // LocalDate always uses the IsoChronology.
        assertSame(IsoChronology.INSTANCE, LocalDate.MIN.getChronology());
    }
}
