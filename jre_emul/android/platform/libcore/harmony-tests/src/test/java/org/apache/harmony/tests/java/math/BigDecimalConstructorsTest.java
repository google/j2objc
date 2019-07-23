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
/**
 * @author Elena Semukhina
 */

package org.apache.harmony.tests.java.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import junit.framework.TestCase;

/**
 * Class:  java.math.BigDecimal
 * Methods: constructors and fields
 */
public class BigDecimalConstructorsTest extends TestCase {
    /**
     * check ONE
     */
    public void testFieldONE() {
        String oneS = "1";
        double oneD = 1.0;
        assertEquals("incorrect string value", oneS, BigDecimal.ONE.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.ONE.doubleValue(), 0);
    }

    /**
     * check TEN
     */
    public void testFieldTEN() {
        String oneS = "10";
        double oneD = 10.0;
        assertEquals("incorrect string value", oneS, BigDecimal.TEN.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.TEN.doubleValue(), 0);
    }

    /**
     * check ZERO
     */
    public void testFieldZERO() {
        String oneS = "0";
        double oneD = 0.0;
        assertEquals("incorrect string value", oneS, BigDecimal.ZERO.toString());
        assertEquals("incorrect double value", oneD, BigDecimal.ZERO.doubleValue(), 0);
    }

    /**
     * new BigDecimal(BigInteger value)
     */
    public void testConstrBI() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        BigDecimal aNumber = new BigDecimal(bA);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", 0, aNumber.scale());

        try {
            new BigDecimal((BigInteger) null);
        	fail("No NullPointerException");
        } catch (NullPointerException e) {
        	//expected
        }
    }
     
    /**
     * new BigDecimal(BigInteger value, int scale)
     */
    public void testConstrBIScale() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int aScale = 10;
        BigDecimal aNumber = new BigDecimal(bA, aScale);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(BigInteger value, MathContext)
     */
    public void testConstrBigIntegerMathContext() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int precision = 46;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "1231212478987482988429808779810457634781384757";
        int resScale = -6;
        BigDecimal result = new BigDecimal(bA, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(BigInteger value, int scale, MathContext)
     */
    public void testConstrBigIntegerScaleMathContext() {
        String a = "1231212478987482988429808779810457634781384756794987";
        BigInteger bA = new BigInteger(a);
        int aScale = 10;
        int precision = 46;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "1231212478987482988429808779810457634781384757";
        int resScale = 4;
        BigDecimal result = new BigDecimal(bA, aScale, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(char[] value); 
     */
    public void testConstrChar() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        BigDecimal result = new BigDecimal(value);
        String res = "-1.23804738E-419";
        int resScale = 427;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        
        try {
            // Regression for HARMONY-783
            new BigDecimal(new char[] {});
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    
    /**
     * new BigDecimal(char[] value, int offset, int len); 
     */
    public void testConstrCharIntInt() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 12;
        BigDecimal result = new BigDecimal(value, offset, len);
        String res = "3.804738E-40";
        int resScale = 46;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        
        try {
            // Regression for HARMONY-783
            new BigDecimal(new char[] {}, 0, 0);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    
    /**
     * new BigDecimal(char[] value, int offset, int len, MathContext mc); 
     */
    public void testConstrCharIntIntMathContext() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 12;
        int precision = 4;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        BigDecimal result = new BigDecimal(value, offset, len, mc);
        String res = "3.805E-40";
        int resScale = 43;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
        
        try {
            // Regression for HARMONY-783
            new BigDecimal(new char[] {}, 0, 0, MathContext.DECIMAL32);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    
    /**
     * new BigDecimal(char[] value, int offset, int len, MathContext mc); 
     */
    public void testConstrCharIntIntMathContextException1() {
        char value[] = {'-', '1', '2', '3', '8', '0', '.', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 120;
        int precision = 4;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        try {
            new BigDecimal(value, offset, len, mc);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }
    
    /**
     * new BigDecimal(char[] value, int offset, int len, MathContext mc); 
     */
    public void testConstrCharIntIntMathContextException2() {
        char value[] = {'-', '1', '2', '3', '8', '0', ',', '4', '7', '3', '8', 'E', '-', '4', '2', '3'};
        int offset = 3;
        int len = 120;
        int precision = 4;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        try {
            new BigDecimal(value, offset, len, mc);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
     }

    /**
     * new BigDecimal(char[] value, MathContext mc);
     */
    public void testConstrCharMathContext() {
        try {
            // Regression for HARMONY-783
            new BigDecimal(new char[] {}, MathContext.DECIMAL32);
            fail("NumberFormatException has not been thrown");
        } catch (NumberFormatException e) {
        }
    }
    
    /**
     * new BigDecimal(double value) when value is NaN
     */
    public void testConstrDoubleNaN() {
        double a = Double.NaN;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN", e
                    .getMessage());
        }
    }

    /**
     * new BigDecimal(double value) when value is positive infinity
     */
    public void testConstrDoublePosInfinity() {
        double a = Double.POSITIVE_INFINITY;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN",
                    e.getMessage());
        }
    }

    /**
     * new BigDecimal(double value) when value is positive infinity
     */
    public void testConstrDoubleNegInfinity() {
        double a = Double.NEGATIVE_INFINITY;
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
            assertEquals("Improper exception message", "Infinite or NaN",
                    e.getMessage());
        }
    }

    /**
     * new BigDecimal(double value)
     */
    public void testConstrDouble() {
        double a = 732546982374982347892379283571094797.287346782359284756;
        int aScale = 0;
        BigInteger bA = new BigInteger("732546982374982285073458350476230656");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(double, MathContext)
     */
    public void testConstrDoubleMathContext() {
        double a = 732546982374982347892379283571094797.287346782359284756;
        int precision = 21;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "732546982374982285074";
        int resScale = -15;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(0.1)
     */
    public void testConstrDouble01() {
        double a = 1.E-1;
        int aScale = 55;
        BigInteger bA = new BigInteger("1000000000000000055511151231257827021181583404541015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(0.555)
     */
    public void testConstrDouble02() {
        double a = 0.555;
        int aScale = 53;
        BigInteger bA = new BigInteger("55500000000000004884981308350688777863979339599609375");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(-0.1)
     */
    public void testConstrDoubleMinus01() {
        double a = -1.E-1;
        int aScale = 55;
        BigInteger bA = new BigInteger("-1000000000000000055511151231257827021181583404541015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(int value)
     */
    public void testConstrInt() {
        int a = 732546982;
        String res = "732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(int, MathContext)
     */
    public void testConstrIntMathContext() {
        int a = 732546982;
        int precision = 21;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(long value)
     */
    public void testConstrLong() {
        long a = 4576578677732546982L;
        String res = "4576578677732546982";
        int resScale = 0;
        BigDecimal result = new BigDecimal(a);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(long, MathContext)
     */
    public void testConstrLongMathContext() {
        long a = 4576578677732546982L;
        int precision = 5;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "45766";
        int resScale = -14;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * new BigDecimal(double value) when value is denormalized
     */
    public void testConstrDoubleDenormalized() {
        double a = 2.274341322658976E-309;
        int aScale = 1073;
        BigInteger bA = new BigInteger("227434132265897633950269241702666687639731047124115603942986140264569528085692462493371029187342478828091760934014851133733918639492582043963243759464684978401240614084312038547315281016804838374623558434472007664427140169018817050565150914041833284370702366055678057809362286455237716100382057360123091641959140448783514464639706721250400288267372238950016114583259228262046633530468551311769574111763316146065958042194569102063373243372766692713192728878701004405568459288708477607744497502929764155046100964958011009313090462293046650352146796805866786767887226278836423536035611825593567576424943331337401071583562754098901412372708947790843318760718495117047155597276492717187936854356663665005157041552436478744491526494952982062613955349661409854888916015625");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
     
    /**
     * new BigDecimal(String value)
     * when value is not a valid representation of BigDecimal.
     */
    public void testConstrStringException() {
        String a = "-238768.787678287a+10";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {}
    }

    /**
     * new BigDecimal(String value) when exponent is empty.
     */
    public void testConstrStringExceptionEmptyExponent1() {
        String a = "-238768.787678287e";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * new BigDecimal(String value) when exponent is empty.
     */
    public void testConstrStringExceptionEmptyExponent2() {
        String a = "-238768.787678287e-";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * new BigDecimal(String value) when exponent is greater than
     * Integer.MAX_VALUE.
     */
    public void testConstrStringExceptionExponentGreaterIntegerMax() {
        String a = "-238768.787678287e214748364767876";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * new BigDecimal(String value) when exponent is less than
     * Integer.MIN_VALUE.
     */
    public void testConstrStringExceptionExponentLessIntegerMin() {
        String a = "-238768.787678287e-214748364767876";
        try {
            new BigDecimal(a);
            fail("NumberFormatException has not been caught");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * new BigDecimal(String value)
     * when exponent is Integer.MAX_VALUE.
     */
    public void testConstrStringExponentIntegerMax() {
        String a = "-238768.787678287e2147483647";
        int aScale = -2147483638;
        BigInteger bA = new BigInteger("-238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value)
     * when exponent is Integer.MIN_VALUE.
     */
    public void testConstrStringExponentIntegerMin() {
        String a = ".238768e-2147483648";
        try {
           new BigDecimal(a);
           fail("NumberFormatException expected");
       } catch (NumberFormatException e) {
           assertEquals("Improper exception message","Scale out of range.", 
               e.getMessage());
       }
    }

    /**
     * new BigDecimal(String value); value does not contain exponent
     */
      public void testConstrStringWithoutExpPos1() {
        String a = "732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); value does not contain exponent
     */
      public void testConstrStringWithoutExpPos2() {
        String a = "+732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
       
    /**
     * new BigDecimal(String value); value does not contain exponent
     */
      public void testConstrStringWithoutExpNeg() {
        String a = "-732546982374982347892379283571094797.287346782359284756";
        int aScale = 18;
        BigInteger bA = new BigInteger("-732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
       
    /**
     * new BigDecimal(String value); value does not contain exponent
     * and decimal point
     */
      public void testConstrStringWithoutExpWithoutPoint() {
        String a = "-732546982374982347892379283571094797287346782359284756";
        int aScale = 0;
        BigInteger bA = new BigInteger("-732546982374982347892379283571094797287346782359284756");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
       
      /**
       * new BigDecimal(String value); value contains exponent
       * and does not contain decimal point
       */
      public void testConstrStringWithExponentWithoutPoint1() {
          String a = "-238768787678287e214";
          int aScale = -214;
          BigInteger bA = new BigInteger("-238768787678287");
          BigDecimal aNumber = new BigDecimal(a);
          assertEquals("incorrect value", bA, aNumber.unscaledValue());
          assertEquals("incorrect scale", aScale, aNumber.scale());
      }

    /**
     * new BigDecimal(String value); value contains exponent
     * and does not contain decimal point
     */
    public void testConstrStringWithExponentWithoutPoint2() {
        String a = "-238768787678287e-214";
        int aScale = 214;
        BigInteger bA = new BigInteger("-238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
     
    /**
     * new BigDecimal(String value); value contains exponent
     * and does not contain decimal point
     */
    public void testConstrStringWithExponentWithoutPoint3() {
        String a = "238768787678287e-214";
        int aScale = 214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); value contains exponent
     * and does not contain decimal point
     */
    public void testConstrStringWithExponentWithoutPoint4() {
        String a = "238768787678287e+214";
        int aScale = -214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); value contains exponent
     * and does not contain decimal point
     */
    public void testConstrStringWithExponentWithoutPoint5() {
        String a = "238768787678287E214";
        int aScale = -214;
        BigInteger bA = new BigInteger("238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); 
     * value contains both exponent and decimal point
     */
    public void testConstrStringWithExponentWithPoint1() {
        String a = "23985439837984782435652424523876878.7678287e+214";
        int aScale = -207;
        BigInteger bA = new BigInteger("239854398379847824356524245238768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); 
     * value contains both exponent and decimal point
     */
    public void testConstrStringWithExponentWithPoint2() {
        String a = "238096483923847545735673567457356356789029578490276878.7678287e-214";
        int aScale = 221;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }

    /**
     * new BigDecimal(String value); 
     * value contains both exponent and decimal point
     */
    public void testConstrStringWithExponentWithPoint3() {
        String a = "2380964839238475457356735674573563567890.295784902768787678287E+21";
        int aScale = 0;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
     
    /**
     * new BigDecimal(String value); 
     * value contains both exponent and decimal point
     */
    public void testConstrStringWithExponentWithPoint4() {
        String a = "23809648392384754573567356745735635678.90295784902768787678287E+21";
        int aScale = 2;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
     
    /**
     * new BigDecimal(String value); 
     * value contains both exponent and decimal point
     */
    public void testConstrStringWithExponentWithPoint5() {
        String a = "238096483923847545735673567457356356789029.5784902768787678287E+21";
        int aScale = -2;
        BigInteger bA = new BigInteger("2380964839238475457356735674573563567890295784902768787678287");
        BigDecimal aNumber = new BigDecimal(a);
        assertEquals("incorrect value", bA, aNumber.unscaledValue());
        assertEquals("incorrect scale", aScale, aNumber.scale());
    }
    
    /**
     * new BigDecimal(String value, MathContext)
     */
    public void testConstrStringMathContext() {
        String a = "-238768787678287e214";
        int precision = 5;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String res = "-23876";
        int resScale = -224;
        BigDecimal result = new BigDecimal(a, mc);
        assertEquals("incorrect value", res, result.unscaledValue().toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
}
