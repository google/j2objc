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

import java.util.LongSummaryStatistics;

public class LongSummaryStatisticsTest extends junit.framework.TestCase {

    private static final long data1[] = {2, -5, 7, -1, 1, 0, 100};
    private static final long data2[] = {1, 3, 2, 1, 7};

    public void test_empty() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        assertEquals(0, lss.getCount());
        assertEquals(0, lss.getSum());
        assertEquals(0.0d, lss.getAverage());
        assertEquals(Long.MAX_VALUE, lss.getMin());
        assertEquals(Long.MIN_VALUE, lss.getMax());
    }

    public void test_accept() {
        LongSummaryStatistics lss = new LongSummaryStatistics();

        // For long values
        lss.accept(100L);
        assertEquals(1, lss.getCount());
        assertEquals(100L, lss.getSum());
        lss.accept(250L);
        assertEquals(2, lss.getCount());
        assertEquals(350L, lss.getSum());

        // for int values
        lss.accept(50);
        assertEquals(3, lss.getCount());
        assertEquals(400L, lss.getSum());
        lss.accept(200);
        assertEquals(4, lss.getCount());
        assertEquals(600L, lss.getSum());
    }

    public void test_combine() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData2();
        LongSummaryStatistics lssCombined = getLongSummaryStatisticsData1();
        lssCombined.combine(lss1);

        assertEquals(12, lssCombined.getCount());
        assertEquals(118L, lssCombined.getSum());
        assertEquals(100L, lssCombined.getMax());
        assertEquals(-5L, lssCombined.getMin());
        assertEquals(9.833333, lssCombined.getAverage(), 1E-6);
    }

    public void test_getCount() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData1();
        assertEquals(data1.length, lss1.getCount());
    }

    public void test_getSum() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData1();
        assertEquals(104L, lss1.getSum());
    }

    public void test_getMin() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData1();
        assertEquals(-5L, lss1.getMin());
    }

    public void test_getMax() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData1();
        assertEquals(100L, lss1.getMax());
    }

    public void test_getAverage() {
        LongSummaryStatistics lss1 = getLongSummaryStatisticsData1();
        assertEquals(14.857142, lss1.getAverage(), 1E-6);
    }

    private static LongSummaryStatistics getLongSummaryStatisticsData1() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        for (long value : data1) {
            lss.accept(value);
        }
        return lss;
    }

    private static LongSummaryStatistics getLongSummaryStatisticsData2() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        for (long value : data2) {
            lss.accept(value);
        }
        return lss;
    }
}
