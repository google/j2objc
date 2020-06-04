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
import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Additional tests for {@link YearMonth}.
 *
 * @see tck.java.time.TCKYearMonth
 * @see test.java.time.TestYearMonth
 */
public class YearMonthTest {

    @Test
    public void test_with_TemporalField_long() {
        YearMonth ym = YearMonth.of(2000, Month.JANUARY);
        // -1999 is actually 2000 BCE (and 0 is 1 BCE).
        YearMonth bceYm = YearMonth.of(-1999, Month.JANUARY);

        assertEquals(YearMonth.of(1000, Month.JANUARY), ym.with(ChronoField.YEAR, 1000));
        assertEquals(YearMonth.of(-1, Month.JANUARY), ym.with(ChronoField.YEAR, -1));
        assertEquals(YearMonth.of(2000, Month.FEBRUARY), ym.with(ChronoField.MONTH_OF_YEAR, 2));
        assertEquals(YearMonth.of(-1999, Month.DECEMBER),
                bceYm.with(ChronoField.MONTH_OF_YEAR, 12));
        assertSame(ym, ym.with(ChronoField.ERA, IsoEra.CE.getValue()));
        assertSame(bceYm, bceYm.with(ChronoField.ERA, IsoEra.BCE.getValue()));

        assertEquals(bceYm, ym.with(ChronoField.ERA, IsoEra.BCE.getValue()));
        assertEquals(ym, bceYm.with(ChronoField.ERA, IsoEra.CE.getValue()));
        assertEquals(YearMonth.of(1, Month.JANUARY), ym.with(ChronoField.YEAR_OF_ERA, 1));
        // Proleptic year 0 is 1 BCE.
        assertEquals(YearMonth.of(0, Month.JANUARY), bceYm.with(ChronoField.YEAR_OF_ERA, 1));
        assertEquals(YearMonth.of(0, Month.JANUARY), ym.with(ChronoField.PROLEPTIC_MONTH, 0));
        assertEquals(YearMonth.of(Year.MAX_VALUE, Month.DECEMBER), ym.with(ChronoField.PROLEPTIC_MONTH, Year.MAX_VALUE * 12L + 11));
        assertEquals(YearMonth.of(Year.MIN_VALUE, Month.JANUARY), ym.with(ChronoField.PROLEPTIC_MONTH, Year.MIN_VALUE * 12L));
    }

    @Test
    public void test_with_TemporalField_long_invalidValue() {
        Object[][] invalidValues = new Object[][] {
                { ChronoField.YEAR_OF_ERA, 0 },
                { ChronoField.YEAR_OF_ERA, Year.MAX_VALUE + 1 },
                { ChronoField.YEAR, Year.MIN_VALUE - 1 },
                { ChronoField.YEAR, Year.MAX_VALUE + 1 },
                { ChronoField.ERA, -1 },
                { ChronoField.ERA, 2 },
                { ChronoField.MONTH_OF_YEAR, -1 },
                { ChronoField.MONTH_OF_YEAR, 0 },
                { ChronoField.MONTH_OF_YEAR, 13 },
                { ChronoField.PROLEPTIC_MONTH, Year.MAX_VALUE * 12L + 12 },
                { ChronoField.PROLEPTIC_MONTH, Year.MIN_VALUE * 12L - 1 },
        };

        YearMonth ym = YearMonth.of(2000, Month.JANUARY);
        for (Object[] values : invalidValues) {
            ChronoField field = (ChronoField) values[0];
            long value = ((Number) values[1]).longValue();
            try {
                ym.with(field, value);
                fail("ym.with(" + field + ", " + value + ") should have failed.");
            } catch (DateTimeException expected) {
            }
        }

    }

    @Test
    public void test_with_TemporalField_long_invalidField() {
        ChronoField[] invalidFields = new ChronoField[] {
                ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH,
                ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR,
                ChronoField.ALIGNED_WEEK_OF_MONTH,
                ChronoField.ALIGNED_WEEK_OF_YEAR,
                ChronoField.AMPM_OF_DAY,
                ChronoField.CLOCK_HOUR_OF_AMPM,
                ChronoField.CLOCK_HOUR_OF_DAY,
                ChronoField.DAY_OF_MONTH,
                ChronoField.DAY_OF_WEEK,
                ChronoField.DAY_OF_YEAR,
                ChronoField.EPOCH_DAY,
                ChronoField.HOUR_OF_AMPM,
                ChronoField.HOUR_OF_DAY,
                ChronoField.INSTANT_SECONDS,
                ChronoField.MICRO_OF_DAY,
                ChronoField.MICRO_OF_SECOND,
                ChronoField.MILLI_OF_DAY,
                ChronoField.MILLI_OF_SECOND,
                ChronoField.MINUTE_OF_DAY,
                ChronoField.MINUTE_OF_HOUR,
                ChronoField.NANO_OF_DAY,
                ChronoField.NANO_OF_SECOND,
                ChronoField.OFFSET_SECONDS,
                ChronoField.SECOND_OF_DAY,
                ChronoField.SECOND_OF_MINUTE,
        };

        YearMonth ym = YearMonth.of(2000, Month.JANUARY);
        for (ChronoField invalidField : invalidFields) {
            // Get a valid value to ensure we fail to due invalid field, not due to invalid value.
            long value = invalidField.range().getMinimum();
            try {
                ym.with(invalidField, value);
                fail("TemporalField.with() should not accept " + invalidField);
            } catch (UnsupportedTemporalTypeException expected) {
            }
        }

    }
}
