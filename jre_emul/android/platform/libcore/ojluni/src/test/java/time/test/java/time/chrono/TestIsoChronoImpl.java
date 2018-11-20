/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package test.java.time.chrono;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;
import static org.junit.Assert.assertEquals;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test.
 */
@RunWith(DataProviderRunner.class)
public class TestIsoChronoImpl {

    @DataProvider
    public static Object[][] provider_rangeVersusCalendar() {
        return new Object[][]{
            {LocalDate.of(1583, 1, 1), LocalDate.of(2100, 1, 1)},
        };
    }

    //-----------------------------------------------------------------------
    // Verify  ISO Calendar matches java.util.Calendar for range
    //-----------------------------------------------------------------------
    @Test
    @UseDataProvider("provider_rangeVersusCalendar")
    public void test_IsoChrono_vsCalendar(LocalDate isoStartDate, LocalDate isoEndDate) {
        GregorianCalendar cal = new GregorianCalendar();
        assertEquals("Unexpected calendar type", cal.getCalendarType(), "gregory");
        LocalDate isoDate = IsoChronology.INSTANCE.date(isoStartDate);

        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        cal.set(Calendar.YEAR, isoDate.get(YEAR));
        cal.set(Calendar.MONTH, isoDate.get(MONTH_OF_YEAR) - 1);
        cal.set(Calendar.DAY_OF_MONTH, isoDate.get(DAY_OF_MONTH));

        while (isoDate.isBefore(isoEndDate)) {
            assertEquals("Day mismatch in " + isoDate + ";  cal: " + cal, isoDate.get(DAY_OF_MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
            assertEquals("Month mismatch in " + isoDate, isoDate.get(MONTH_OF_YEAR),
                cal.get(Calendar.MONTH) + 1);
            assertEquals("Year mismatch in " + isoDate, isoDate.get(YEAR_OF_ERA),
                cal.get(Calendar.YEAR));

            isoDate = isoDate.plus(1, ChronoUnit.DAYS);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    //-----------------------------------------------------------------------
    // Verify  ISO Calendar matches java.util.Calendar
    // DayOfWeek, WeekOfMonth, WeekOfYear for range
    //-----------------------------------------------------------------------
    @Test
    @UseDataProvider("provider_rangeVersusCalendar")
    public void test_DayOfWeek_IsoChronology_vsCalendar(LocalDate isoStartDate, LocalDate isoEndDate) {
        GregorianCalendar cal = new GregorianCalendar();
        assertEquals("Unexpected calendar type", cal.getCalendarType(), "gregory");
        LocalDate isoDate = IsoChronology.INSTANCE.date(isoStartDate);

        for (DayOfWeek firstDayOfWeek : DayOfWeek.values()) {
            for (int minDays = 1; minDays <= 7; minDays++) {
                WeekFields weekDef = WeekFields.of(firstDayOfWeek, minDays);
                cal.setFirstDayOfWeek(Math.floorMod(firstDayOfWeek.getValue(), 7) + 1);
                cal.setMinimalDaysInFirstWeek(minDays);

                cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
                cal.set(Calendar.YEAR, isoDate.get(YEAR));
                cal.set(Calendar.MONTH, isoDate.get(MONTH_OF_YEAR) - 1);
                cal.set(Calendar.DAY_OF_MONTH, isoDate.get(DAY_OF_MONTH));

                // For every date in the range
                while (isoDate.isBefore(isoEndDate)) {
                    assertEquals("Day mismatch in " + isoDate + ";  cal: " + cal,
                        isoDate.get(DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    assertEquals("Month mismatch in " + isoDate, isoDate.get(MONTH_OF_YEAR),
                        cal.get(Calendar.MONTH) + 1);
                    assertEquals("Year mismatch in " + isoDate, isoDate.get(YEAR_OF_ERA),
                        cal.get(Calendar.YEAR));

                    int jdow = Math.floorMod(cal.get(Calendar.DAY_OF_WEEK) - 2, 7) + 1;
                    int dow = isoDate.get(weekDef.dayOfWeek());
                    assertEquals("Calendar DayOfWeek does not match ISO DayOfWeek", jdow, dow);

                    int jweekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
                    int isoWeekOfMonth = isoDate.get(weekDef.weekOfMonth());
                    assertEquals("Calendar WeekOfMonth does not match ISO WeekOfMonth",
                        jweekOfMonth, isoWeekOfMonth);

                    int jweekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
                    int weekOfYear = isoDate.get(weekDef.weekOfWeekBasedYear());
                    assertEquals("GregorianCalendar WeekOfYear does not match WeekOfWeekBasedYear",
                        jweekOfYear, weekOfYear);

                    int jWeekYear = cal.getWeekYear();
                    int weekBasedYear = isoDate.get(weekDef.weekBasedYear());
                    assertEquals("GregorianCalendar getWeekYear does not match YearOfWeekBasedYear",
                        jWeekYear, weekBasedYear);

                    int jweeksInWeekyear = cal.getWeeksInWeekYear();
                    int weeksInWeekBasedYear = (int)isoDate.range(weekDef.weekOfWeekBasedYear()).getMaximum();
                    assertEquals("length of weekBasedYear", jweeksInWeekyear, weeksInWeekBasedYear);

                    isoDate = isoDate.plus(1, ChronoUnit.DAYS);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
    }

    /**
     * Return the ISO Day of Week from a java.util.Calendr DAY_OF_WEEK.
     * @param the java.util.Calendar day of week (1=Sunday, 7=Saturday)
     * @return the ISO DayOfWeek
     */
    private DayOfWeek toISOfromCalendarDOW(int i) {
        return DayOfWeek.of(Math.floorMod(i - 2, 7) + 1);
    }
}
