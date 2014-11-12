/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatSymbolsTest extends junit.framework.TestCase {
    private void assertLocaleIsEquivalentToRoot(Locale locale) {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
        assertEquals(DateFormatSymbols.getInstance(Locale.ROOT), dfs);
    }

    public void test_getInstance_unknown_locale() throws Exception {
        // TODO: we fail this test. on Android, the root locale uses GMT offsets as names.
        // see the invalid locale test below. on the RI, the root locale uses English names.
        assertLocaleIsEquivalentToRoot(new Locale("xx", "XX"));
    }

    public void test_getInstance_invalid_locale() throws Exception {
        assertLocaleIsEquivalentToRoot(new Locale("not exist language", "not exist country"));
    }

    public void testSerialization() throws Exception {
        // Set the default locale. The default locale used to determine what strings were used by
        Locale.setDefault(Locale.US);

        // The Polish language needs stand-alone month and weekday names.
        Locale pl = new Locale("pl");
        DateFormatSymbols originalDfs = new DateFormatSymbols(pl);

        // Serialize...
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(originalDfs);
        byte[] bytes = out.toByteArray();

        // Deserialize...
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        DateFormatSymbols deserializedDfs = (DateFormatSymbols) in.readObject();
        assertEquals(-1, in.read());

        // The two objects be equal.
        assertEquals(originalDfs, deserializedDfs);

        // The original differentiates between regular month names and stand-alone month names...
        assertEquals("stycznia", formatDate(pl, "MMMM", originalDfs));
        assertEquals("stycze\u0144", formatDate(pl, "LLLL", originalDfs));

        // And so does the deserialized version.
        assertEquals("stycznia", formatDate(pl, "MMMM", deserializedDfs));
        assertEquals("stycze\u0144", formatDate(pl, "LLLL", deserializedDfs));
    }

    private String formatDate(Locale l, String fmt, DateFormatSymbols dfs) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, l);
        sdf.setDateFormatSymbols(dfs);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(0));
    }

    public void test_getZoneStrings_cloning() throws Exception {
        // Check that corrupting our array doesn't affect other callers.

        // Kill a row.
        {
            String[][] originalZoneStrings = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
            assertNotNull(originalZoneStrings[0]);
            originalZoneStrings[0] = null;
            String[][] currentZoneStrings = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
            assertNotNull(currentZoneStrings[0]);
        }

        // Kill an element.
        {
            String[][] originalZoneStrings = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
            assertNotNull(originalZoneStrings[0][0]);
            originalZoneStrings[0][0] = null;
            String[][] currentZoneStrings = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
            assertNotNull(currentZoneStrings[0][0]);
        }
    }

    public void test_getZoneStrings_UTC() throws Exception {
        HashMap<String, String[]> zoneStrings = new HashMap<String, String[]>();
        for (String[] row : DateFormatSymbols.getInstance(Locale.US).getZoneStrings()) {
            zoneStrings.put(row[0], row);
        }

        assertUtc(zoneStrings.get("Etc/UCT"));
        assertUtc(zoneStrings.get("Etc/UTC"));
        assertUtc(zoneStrings.get("Etc/Universal"));
        assertUtc(zoneStrings.get("Etc/Zulu"));

        assertUtc(zoneStrings.get("UCT"));
        assertUtc(zoneStrings.get("UTC"));
        assertUtc(zoneStrings.get("Universal"));
        assertUtc(zoneStrings.get("Zulu"));
    }
    private static void assertUtc(String[] row) {
        // Element 0 is the Olson id. The short names should be "UTC".
        // On the RI, the long names are localized. ICU doesn't have those, so we just use UTC.
        assertEquals(Arrays.toString(row), "UTC", row[2]);
        assertEquals(Arrays.toString(row), "UTC", row[4]);
    }

    // If icu4c doesn't actually have a name, we arrange to return null from native code rather
    // that use icu4c's probably-out-of-date time zone transition data.
    // getZoneStrings has to paper over this.
    public void test_getZoneStrings_no_nulls() throws Exception {
        String[][] array = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
        int failCount = 0;
        for (String[] row : array) {
            for (String element : row) {
                if (element == null) {
                    System.err.println(Arrays.toString(row));
                    ++failCount;
                }
            }
        }
        assertEquals(0, failCount);
    }

    public void test_getZoneStrings_Apia() throws Exception {
        String[][] array = DateFormatSymbols.getInstance(Locale.US).getZoneStrings();
        for (int i = 0; i < array.length; ++i) {
            String[] row = array[i];
            // Pacific/Apia is somewhat arbitrary; we just want a zone we have to generate
            // "GMT" strings for the short names.
            if (row[0].equals("Pacific/Apia")) {
                assertEquals("Samoa Standard Time", row[1]);
                assertEquals("GMT+13:00", row[2]);
                assertEquals("Samoa Daylight Time", row[3]);
                assertEquals("GMT+14:00", row[4]);
            }
        }
    }
}
