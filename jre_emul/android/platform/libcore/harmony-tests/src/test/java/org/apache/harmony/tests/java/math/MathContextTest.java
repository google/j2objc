/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathContextTest extends junit.framework.TestCase {

    /**
     * java.math.MathContext#MathContext(...)
     */
    public void test_MathContextConstruction() {
        String a = "-12380945E+61";
        BigDecimal aNumber = new BigDecimal(a);
        MathContext mcIntRm6hd = new MathContext(6, RoundingMode.HALF_DOWN);
        MathContext mcStr6hd = new MathContext("precision=6 roundingMode=HALF_DOWN");
        MathContext mcInt6 = new MathContext(6);
        MathContext mcInt134 = new MathContext(134);

        // getPrecision()
        assertEquals("MathContext.getPrecision() returns incorrect value",
                6, mcIntRm6hd.getPrecision() );
        assertEquals("MathContext.getPrecision() returns incorrect value",
                134, mcInt134.getPrecision() );

        // getRoundingMode()
        assertEquals("MathContext.getRoundingMode() returns incorrect value",
                RoundingMode.HALF_UP,
                mcInt6.getRoundingMode());
        assertEquals("MathContext.getRoundingMode() returns incorrect value",
                RoundingMode.HALF_DOWN, mcIntRm6hd.getRoundingMode() );

        // toString()
        assertEquals("MathContext.toString() returning incorrect value",
                "precision=6 roundingMode=HALF_DOWN", mcIntRm6hd.toString() );
        assertEquals("MathContext.toString() returning incorrect value",
                "precision=6 roundingMode=HALF_UP", mcInt6.toString() );

        // equals(.)
        assertEquals("Equal MathContexts are not equal ",
                mcIntRm6hd, mcStr6hd );
        assertFalse("Different MathContexts are reported as equal ",
                mcInt6.equals(mcStr6hd) );
        assertFalse("Different MathContexts are reported as equal ",
                mcInt6.equals(mcInt134) );

        // hashCode(.)
        assertEquals("Equal MathContexts have different hashcodes ",
                mcIntRm6hd.hashCode(), mcStr6hd.hashCode() );
        assertFalse("Different MathContexts have equal hashcodes ",
                mcInt6.hashCode() == mcStr6hd.hashCode() );
        assertFalse("Different MathContexts have equal hashcodes ",
                mcInt6.hashCode() == mcInt134.hashCode() );

        // other:
        BigDecimal res = aNumber.abs(mcInt6);
        assertEquals("MathContext Constructor with int precision failed",
                new BigDecimal("1.23809E+68"),
                res);
    }

}
