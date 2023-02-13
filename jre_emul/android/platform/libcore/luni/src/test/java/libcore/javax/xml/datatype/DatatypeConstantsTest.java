/*
 * Copyright (C) 2021 The Android Open Source Project
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
package libcore.javax.xml.datatype;

import javax.xml.datatype.DatatypeConstants;
import junit.framework.TestCase;

public class DatatypeConstantsTest extends TestCase {

    public void testField_getId() {
        assertEquals(0, DatatypeConstants.YEARS.getId());
        assertEquals(1, DatatypeConstants.MONTHS.getId());
        assertEquals(2, DatatypeConstants.DAYS.getId());
        assertEquals(3, DatatypeConstants.HOURS.getId());
        assertEquals(4, DatatypeConstants.MINUTES.getId());
        assertEquals(5, DatatypeConstants.SECONDS.getId());
    }
}
