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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Additional tests for {@link DateTimeFormatter}.
 *
 * @see tck.java.time.format.TCKDateTimeFormatter
 * @see test.java.time.format.TestDateTimeFormatter
 */
public class DateTimeFormatterTest {

    @Test
    public void test_getDecimalStyle() {
        Locale arLocale = Locale.forLanguageTag("ar");
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_DATE,
                DateTimeFormatter.RFC_1123_DATE_TIME,
                new DateTimeFormatterBuilder().toFormatter(),
                new DateTimeFormatterBuilder().toFormatter(Locale.ROOT),
                new DateTimeFormatterBuilder().toFormatter(Locale.ENGLISH),
                new DateTimeFormatterBuilder().toFormatter(arLocale),
        };

        DecimalStyle arDecimalStyle = DecimalStyle.of(arLocale);
        // Verify that the Locale ar returns a DecimalStyle other than STANDARD.
        assertNotEquals(DecimalStyle.STANDARD, arDecimalStyle);

        for (DateTimeFormatter formatter : formatters) {
            // All DateTimeFormatters should use the standard style, unless explicitly changed.
            assertEquals(formatter.toString(), DecimalStyle.STANDARD, formatter.getDecimalStyle());

            DateTimeFormatter arStyleFormatter = formatter.withDecimalStyle(arDecimalStyle);
            assertEquals(arStyleFormatter.toString(),
                    arDecimalStyle, arStyleFormatter.getDecimalStyle());

            // Verify that calling withDecimalStyle() doesn't modify the original formatter.
            assertEquals(formatter.toString(), DecimalStyle.STANDARD, formatter.getDecimalStyle());
        }
    }
}
