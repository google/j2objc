/*
 * Copyright (C) 2016 The Android Open Source Project
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
package libcore.java.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Test that Calendar.get(WEEK_OF_MONTH) works as expected.
 */
@RunWith(Parameterized.class)
public class CalendarWeekOfMonthTest {

    private final long timeInMillis;

    private final String date;

    private final int firstDayOfWeek;

    private final int minimalDaysInFirstWeek;

    private final int expectedWeekOfMonth;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // MinimalDaysInFirstWeek = 4, FirstDayOfWeek MONDAY
                { 1462107600000L, "01 May 2016", Calendar.MONDAY, 4, 0 },
                { 1462194000000L, "02 May 2016", Calendar.MONDAY, 4, 1 },
                { 1462798800000L, "09 May 2016", Calendar.MONDAY, 4, 2 },
                { 1463403600000L, "16 May 2016", Calendar.MONDAY, 4, 3 },
                { 1464008400000L, "23 May 2016", Calendar.MONDAY, 4, 4 },
                { 1464613200000L, "30 May 2016", Calendar.MONDAY, 4, 5 },
                { 1464786000000L, "01 Jun 2016", Calendar.MONDAY, 4, 1 },
                { 1465218000000L, "06 Jun 2016", Calendar.MONDAY, 4, 2 },
                { 1465822800000L, "13 Jun 2016", Calendar.MONDAY, 4, 3 },
                { 1466427600000L, "20 Jun 2016", Calendar.MONDAY, 4, 4 },
                { 1467032400000L, "27 Jun 2016", Calendar.MONDAY, 4, 5 },
                { 1467378000000L, "01 Jul 2016", Calendar.MONDAY, 4, 0 },
                { 1467637200000L, "04 Jul 2016", Calendar.MONDAY, 4, 1 },
                { 1468242000000L, "11 Jul 2016", Calendar.MONDAY, 4, 2 },
                { 1468846800000L, "18 Jul 2016", Calendar.MONDAY, 4, 3 },
                { 1469451600000L, "25 Jul 2016", Calendar.MONDAY, 4, 4 },
                { 1470056400000L, "01 Aug 2016", Calendar.MONDAY, 4, 1 },
                { 1470661200000L, "08 Aug 2016", Calendar.MONDAY, 4, 2 },
                { 1471266000000L, "15 Aug 2016", Calendar.MONDAY, 4, 3 },
                { 1471870800000L, "22 Aug 2016", Calendar.MONDAY, 4, 4 },
                { 1472475600000L, "29 Aug 2016", Calendar.MONDAY, 4, 5 },
                { 1472734800000L, "01 Sep 2016", Calendar.MONDAY, 4, 1 },
                { 1473080400000L, "05 Sep 2016", Calendar.MONDAY, 4, 2 },
                { 1473685200000L, "12 Sep 2016", Calendar.MONDAY, 4, 3 },
                { 1474290000000L, "19 Sep 2016", Calendar.MONDAY, 4, 4 },
                { 1474894800000L, "26 Sep 2016", Calendar.MONDAY, 4, 5 },
                { 1475326800000L, "01 Oct 2016", Calendar.MONDAY, 4, 0 },
                { 1475499600000L, "03 Oct 2016", Calendar.MONDAY, 4, 1 },
                { 1476104400000L, "10 Oct 2016", Calendar.MONDAY, 4, 2 },
                { 1476709200000L, "17 Oct 2016", Calendar.MONDAY, 4, 3 },
                { 1477314000000L, "24 Oct 2016", Calendar.MONDAY, 4, 4 },
                { 1477918800000L, "31 Oct 2016", Calendar.MONDAY, 4, 5 },
                { 1478005200000L, "01 Nov 2016", Calendar.MONDAY, 4, 1 },
                { 1478523600000L, "07 Nov 2016", Calendar.MONDAY, 4, 2 },
                { 1479128400000L, "14 Nov 2016", Calendar.MONDAY, 4, 3 },
                { 1479733200000L, "21 Nov 2016", Calendar.MONDAY, 4, 4 },
                { 1480338000000L, "28 Nov 2016", Calendar.MONDAY, 4, 5 },

                // MinimalDaysInFirstWeek = 1, FirstDayOfWeek MONDAY
                { 1462107600000L, "01 May 2016", Calendar.MONDAY, 1, 1 },
                { 1462194000000L, "02 May 2016", Calendar.MONDAY, 1, 2 },
                { 1462798800000L, "09 May 2016", Calendar.MONDAY, 1, 3 },
                { 1463403600000L, "16 May 2016", Calendar.MONDAY, 1, 4 },
                { 1464008400000L, "23 May 2016", Calendar.MONDAY, 1, 5 },
                { 1464613200000L, "30 May 2016", Calendar.MONDAY, 1, 6 },
                { 1464786000000L, "01 Jun 2016", Calendar.MONDAY, 1, 1 },
                { 1465218000000L, "06 Jun 2016", Calendar.MONDAY, 1, 2 },
                { 1465822800000L, "13 Jun 2016", Calendar.MONDAY, 1, 3 },
                { 1466427600000L, "20 Jun 2016", Calendar.MONDAY, 1, 4 },
                { 1467032400000L, "27 Jun 2016", Calendar.MONDAY, 1, 5 },
                { 1467378000000L, "01 Jul 2016", Calendar.MONDAY, 1, 1 },
                { 1467637200000L, "04 Jul 2016", Calendar.MONDAY, 1, 2 },
                { 1468242000000L, "11 Jul 2016", Calendar.MONDAY, 1, 3 },
                { 1468846800000L, "18 Jul 2016", Calendar.MONDAY, 1, 4 },
                { 1469451600000L, "25 Jul 2016", Calendar.MONDAY, 1, 5 },
                { 1470056400000L, "01 Aug 2016", Calendar.MONDAY, 1, 1 },
                { 1470661200000L, "08 Aug 2016", Calendar.MONDAY, 1, 2 },
                { 1471266000000L, "15 Aug 2016", Calendar.MONDAY, 1, 3 },
                { 1471870800000L, "22 Aug 2016", Calendar.MONDAY, 1, 4 },
                { 1472475600000L, "29 Aug 2016", Calendar.MONDAY, 1, 5 },
                { 1472734800000L, "01 Sep 2016", Calendar.MONDAY, 1, 1 },
                { 1473080400000L, "05 Sep 2016", Calendar.MONDAY, 1, 2 },
                { 1473685200000L, "12 Sep 2016", Calendar.MONDAY, 1, 3 },
                { 1474290000000L, "19 Sep 2016", Calendar.MONDAY, 1, 4 },
                { 1474894800000L, "26 Sep 2016", Calendar.MONDAY, 1, 5 },
                { 1475326800000L, "01 Oct 2016", Calendar.MONDAY, 1, 1 },
                { 1475499600000L, "03 Oct 2016", Calendar.MONDAY, 1, 2 },
                { 1476104400000L, "10 Oct 2016", Calendar.MONDAY, 1, 3 },
                { 1476709200000L, "17 Oct 2016", Calendar.MONDAY, 1, 4 },
                { 1477314000000L, "24 Oct 2016", Calendar.MONDAY, 1, 5 },
                { 1477918800000L, "31 Oct 2016", Calendar.MONDAY, 1, 6 },
                { 1478005200000L, "01 Nov 2016", Calendar.MONDAY, 1, 1 },
                { 1478523600000L, "07 Nov 2016", Calendar.MONDAY, 1, 2 },
                { 1479128400000L, "14 Nov 2016", Calendar.MONDAY, 1, 3 },
                { 1479733200000L, "21 Nov 2016", Calendar.MONDAY, 1, 4 },
                { 1480338000000L, "28 Nov 2016", Calendar.MONDAY, 1, 5 },

                // MinimalDaysInFirstWeek = 4, FirstDayOfWeek SUNDAY
                { 1462107600000L, "01 May 2016", Calendar.SUNDAY, 4, 1 },
                { 1462712400000L, "08 May 2016", Calendar.SUNDAY, 4, 2 },
                { 1463317200000L, "15 May 2016", Calendar.SUNDAY, 4, 3 },
                { 1463922000000L, "22 May 2016", Calendar.SUNDAY, 4, 4 },
                { 1464526800000L, "29 May 2016", Calendar.SUNDAY, 4, 5 },
                { 1464786000000L, "01 Jun 2016", Calendar.SUNDAY, 4, 1 },
                { 1465131600000L, "05 Jun 2016", Calendar.SUNDAY, 4, 2 },
                { 1465736400000L, "12 Jun 2016", Calendar.SUNDAY, 4, 3 },
                { 1466341200000L, "19 Jun 2016", Calendar.SUNDAY, 4, 4 },
                { 1466946000000L, "26 Jun 2016", Calendar.SUNDAY, 4, 5 },
                { 1467378000000L, "01 Jul 2016", Calendar.SUNDAY, 4, 0 },
                { 1467550800000L, "03 Jul 2016", Calendar.SUNDAY, 4, 1 },
                { 1468155600000L, "10 Jul 2016", Calendar.SUNDAY, 4, 2 },
                { 1468760400000L, "17 Jul 2016", Calendar.SUNDAY, 4, 3 },
                { 1469365200000L, "24 Jul 2016", Calendar.SUNDAY, 4, 4 },
                { 1469970000000L, "31 Jul 2016", Calendar.SUNDAY, 4, 5 },
                { 1470056400000L, "01 Aug 2016", Calendar.SUNDAY, 4, 1 },
                { 1470574800000L, "07 Aug 2016", Calendar.SUNDAY, 4, 2 },
                { 1471179600000L, "14 Aug 2016", Calendar.SUNDAY, 4, 3 },
                { 1471784400000L, "21 Aug 2016", Calendar.SUNDAY, 4, 4 },
                { 1472389200000L, "28 Aug 2016", Calendar.SUNDAY, 4, 5 },
                { 1472734800000L, "01 Sep 2016", Calendar.SUNDAY, 4, 0 },
                { 1472994000000L, "04 Sep 2016", Calendar.SUNDAY, 4, 1 },
                { 1473598800000L, "11 Sep 2016", Calendar.SUNDAY, 4, 2 },
                { 1474203600000L, "18 Sep 2016", Calendar.SUNDAY, 4, 3 },
                { 1474808400000L, "25 Sep 2016", Calendar.SUNDAY, 4, 4 },
                { 1475326800000L, "01 Oct 2016", Calendar.SUNDAY, 4, 0 },
                { 1475413200000L, "02 Oct 2016", Calendar.SUNDAY, 4, 1 },
                { 1476018000000L, "09 Oct 2016", Calendar.SUNDAY, 4, 2 },
                { 1476622800000L, "16 Oct 2016", Calendar.SUNDAY, 4, 3 },
                { 1477227600000L, "23 Oct 2016", Calendar.SUNDAY, 4, 4 },
                { 1477832400000L, "30 Oct 2016", Calendar.SUNDAY, 4, 5 },
                { 1478005200000L, "01 Nov 2016", Calendar.SUNDAY, 4, 1 },
                { 1478437200000L, "06 Nov 2016", Calendar.SUNDAY, 4, 2 },
                { 1479042000000L, "13 Nov 2016", Calendar.SUNDAY, 4, 3 },
                { 1479646800000L, "20 Nov 2016", Calendar.SUNDAY, 4, 4 },
                { 1480251600000L, "27 Nov 2016", Calendar.SUNDAY, 4, 5 },

                // MinimalDaysInFirstWeek = 1, FirstDayOfWeek SUNDAY
                { 1462107600000L, "01 May 2016", Calendar.SUNDAY, 1, 1 },
                { 1462712400000L, "08 May 2016", Calendar.SUNDAY, 1, 2 },
                { 1463317200000L, "15 May 2016", Calendar.SUNDAY, 1, 3 },
                { 1463922000000L, "22 May 2016", Calendar.SUNDAY, 1, 4 },
                { 1464526800000L, "29 May 2016", Calendar.SUNDAY, 1, 5 },
                { 1464786000000L, "01 Jun 2016", Calendar.SUNDAY, 1, 1 },
                { 1465131600000L, "05 Jun 2016", Calendar.SUNDAY, 1, 2 },
                { 1465736400000L, "12 Jun 2016", Calendar.SUNDAY, 1, 3 },
                { 1466341200000L, "19 Jun 2016", Calendar.SUNDAY, 1, 4 },
                { 1466946000000L, "26 Jun 2016", Calendar.SUNDAY, 1, 5 },
                { 1467378000000L, "01 Jul 2016", Calendar.SUNDAY, 1, 1 },
                { 1467550800000L, "03 Jul 2016", Calendar.SUNDAY, 1, 2 },
                { 1468155600000L, "10 Jul 2016", Calendar.SUNDAY, 1, 3 },
                { 1468760400000L, "17 Jul 2016", Calendar.SUNDAY, 1, 4 },
                { 1469365200000L, "24 Jul 2016", Calendar.SUNDAY, 1, 5 },
                { 1469970000000L, "31 Jul 2016", Calendar.SUNDAY, 1, 6 },
                { 1470056400000L, "01 Aug 2016", Calendar.SUNDAY, 1, 1 },
                { 1470574800000L, "07 Aug 2016", Calendar.SUNDAY, 1, 2 },
                { 1471179600000L, "14 Aug 2016", Calendar.SUNDAY, 1, 3 },
                { 1471784400000L, "21 Aug 2016", Calendar.SUNDAY, 1, 4 },
                { 1472389200000L, "28 Aug 2016", Calendar.SUNDAY, 1, 5 },
                { 1472734800000L, "01 Sep 2016", Calendar.SUNDAY, 1, 1 },
                { 1472994000000L, "04 Sep 2016", Calendar.SUNDAY, 1, 2 },
                { 1473598800000L, "11 Sep 2016", Calendar.SUNDAY, 1, 3 },
                { 1474203600000L, "18 Sep 2016", Calendar.SUNDAY, 1, 4 },
                { 1474808400000L, "25 Sep 2016", Calendar.SUNDAY, 1, 5 },
                { 1475326800000L, "01 Oct 2016", Calendar.SUNDAY, 1, 1 },
                { 1475413200000L, "02 Oct 2016", Calendar.SUNDAY, 1, 2 },
                { 1476018000000L, "09 Oct 2016", Calendar.SUNDAY, 1, 3 },
                { 1476622800000L, "16 Oct 2016", Calendar.SUNDAY, 1, 4 },
                { 1477227600000L, "23 Oct 2016", Calendar.SUNDAY, 1, 5 },
                { 1477832400000L, "30 Oct 2016", Calendar.SUNDAY, 1, 6 },
                { 1478005200000L, "01 Nov 2016", Calendar.SUNDAY, 1, 1 },
                { 1478437200000L, "06 Nov 2016", Calendar.SUNDAY, 1, 2 },
                { 1479042000000L, "13 Nov 2016", Calendar.SUNDAY, 1, 3 },
                { 1479646800000L, "20 Nov 2016", Calendar.SUNDAY, 1, 4 },
                { 1480251600000L, "27 Nov 2016", Calendar.SUNDAY, 1, 5 },

        });
    }

    public CalendarWeekOfMonthTest(long timeInMillis, String date, int firstDayOfWeek,
            int minimalDaysInFirstWeek, int expectedWeekOfMonth) {
        this.timeInMillis = timeInMillis;
        this.date = date;
        this.firstDayOfWeek = firstDayOfWeek;
        this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
        this.expectedWeekOfMonth = expectedWeekOfMonth;
    }

    @Test
    public void test() {
        Calendar calendar = new GregorianCalendar(
                TimeZone.getTimeZone("America/Los_Angeles"), Locale.US);
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        calendar.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
        calendar.setTimeInMillis(timeInMillis);

        assertEquals(toString(), expectedWeekOfMonth, calendar.get(Calendar.WEEK_OF_MONTH));
    }

    @Override
    public String toString() {
        return "CalendarWeekOfMonthTest{" + "timeInMillis=" + timeInMillis
                + ", date='" + date + '\'' + ", firstDayOfWeek=" + firstDayOfWeek
                + ", minimalDaysInFirstWeek=" + minimalDaysInFirstWeek
                + ", expectedWeekOfMonth=" + expectedWeekOfMonth + '}';
    }
}
