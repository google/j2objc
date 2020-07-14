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

import java.util.IntSummaryStatistics;

public class IntSummaryStatisticsTest extends junit.framework.TestCase {

    private static final int data1[] = {2, -5, 7, -1, 1, 0, 100};
    private static final int data2[] = {1, 3, 2, 1, 7};

    public void test_empty() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        assertEquals(0, iss.getCount());
        assertEquals(0, iss.getSum());
        assertEquals(0.0d, iss.getAverage());
        assertEquals(Integer.MAX_VALUE, iss.getMin());
        assertEquals(Integer.MIN_VALUE, iss.getMax());
    }

    public void test_accept() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        iss.accept(5);
        assertEquals(1, iss.getCount());
        assertEquals(5, iss.getSum());
        iss.accept(10);
        assertEquals(2, iss.getCount());
        assertEquals(15, iss.getSum());
    }

    public void test_combine() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData2();
        IntSummaryStatistics issCombined = getIntSummaryStatisticsData1();
        issCombined.combine(iss1);

        assertEquals(12, issCombined.getCount());
        assertEquals(118, issCombined.getSum());
        assertEquals(100, issCombined.getMax());
        assertEquals(-5, issCombined.getMin());
        assertEquals(9.833333d, issCombined.getAverage(), 1E-6);
    }

    public void test_getCount() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData1();
        assertEquals(data1.length, iss1.getCount());
    }

    public void test_getSum() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData1();
        assertEquals(104, iss1.getSum());
    }

    public void test_getMin() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData1();
        assertEquals(-5, iss1.getMin());
    }

    public void test_getMax() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData1();
        assertEquals(100, iss1.getMax());
    }

    public void test_getAverage() {
        IntSummaryStatistics iss1 = getIntSummaryStatisticsData1();
        assertEquals(14.857142, iss1.getAverage(), 1E-6);
    }

    private static IntSummaryStatistics getIntSummaryStatisticsData1() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (int value : data1) {
            iss.accept(value);
        }
        return iss;
    }

    private static IntSummaryStatistics getIntSummaryStatisticsData2() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (int value : data2) {
            iss.accept(value);
        }
        return iss;
    }
}
