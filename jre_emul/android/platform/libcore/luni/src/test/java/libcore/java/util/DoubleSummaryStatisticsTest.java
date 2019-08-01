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

import java.util.DoubleSummaryStatistics;

public class DoubleSummaryStatisticsTest extends junit.framework.TestCase {

    private static final double data1[] = {22.4, -53.4, 74.8, -12.4, 17, 0, 100};
    private static final double data2[] = {1.2, 3.5, 2.7, 1, 7.6};

    public void test_empty() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        assertEquals(0, dss.getCount());
        assertEquals(0.0d, dss.getSum());
        assertEquals(0.0d, dss.getAverage());
        assertEquals(Double.POSITIVE_INFINITY, dss.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, dss.getMax());
    }

    public void test_accept() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        dss.accept(100.5d);
        assertEquals(1, dss.getCount());
        assertEquals(100.5d, dss.getSum());
        dss.accept(45.0d);
        assertEquals(2, dss.getCount());
        assertEquals(145.5d, dss.getSum());
    }

    public void test_combine() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData2();
        DoubleSummaryStatistics dssCombined = getDoubleSummaryStatisticsData1();
        dssCombined.combine(dss1);
        assertEquals(12, dssCombined.getCount());
        assertEquals(164.4d, dssCombined.getSum());
        assertEquals(100.0d, dssCombined.getMax());
        assertEquals(-53.4d, dssCombined.getMin());
        assertEquals(13.7, dssCombined.getAverage(), 1E-6);
    }

    public void test_getCount() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData1();
        assertEquals(data1.length, dss1.getCount());
    }

    public void test_getSum() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData1();
        assertEquals(148.4, dss1.getSum());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getSum());
    }

    public void test_getMin() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData1();
        assertEquals(-53.4d, dss1.getMin());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getMin());
    }

    public void test_getMax() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData1();
        assertEquals(100.0d, dss1.getMax());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getMax());
    }

    public void test_getAverage() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatisticsData1();
        assertEquals(21.2, dss1.getAverage(), 1E-6);

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getAverage());
    }

    private static DoubleSummaryStatistics getDoubleSummaryStatisticsData1() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        for (double value : data1) {
            dss.accept(value);
        }
        return dss;
    }

    private static DoubleSummaryStatistics getDoubleSummaryStatisticsData2() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        for (double value : data2) {
            dss.accept(value);
        }
        return dss;
    }
}
