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
import java.time.Year;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Additional tests for {@link Year}.
 *
 * @see tck.java.time.TCKYear
 * @see test.java.time.TestYear
 */
public class YearTest {
    @Test
    public void test_isLeap() {
        // More extensive tests for Year.isLeap() (which delegates to this static method) can be
        // found in tck.java.time.TCKYear.test_isLeap()
        assertFalse(Year.isLeap(1900));
        assertFalse(Year.isLeap(1999));
        assertTrue(Year.isLeap(2000));
        assertFalse(Year.isLeap(2001));
        assertFalse(Year.isLeap(2002));
        assertFalse(Year.isLeap(2003));
        assertTrue(Year.isLeap(2004));
        assertFalse(Year.isLeap(2005));
    }
}
