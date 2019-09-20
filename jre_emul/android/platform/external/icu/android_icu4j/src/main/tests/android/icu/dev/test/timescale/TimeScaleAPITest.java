/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 */

package android.icu.dev.test.timescale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.math.BigDecimal;
import android.icu.util.UniversalTimeScale;

/**
 * Test UniversalTimeScale API
 */
public class TimeScaleAPITest extends TestFmwk
{

    /**
     * 
     */
    public TimeScaleAPITest()
    {
    }
    
    @Test
    public void TestBigDecimalFromBigDecimal()
    {
        BigDecimal bigZero = new BigDecimal(0);
        
        try {
            UniversalTimeScale.bigDecimalFrom(bigZero, -1);
            errln("bigDecimalFrom(bigZero, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.bigDecimalFrom(bigZero, scale);
            } catch (IllegalArgumentException iae) {
                errln("bigDecimalFrom(bigZero, " + scale + ") threw IllegalArgumentException.");
            }
        }
        
        try {
            UniversalTimeScale.bigDecimalFrom(bigZero, UniversalTimeScale.MAX_SCALE);
            errln("from(bigZero, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
    }
    
    @Test
    public void TestBigDecimalFromDouble()
    {
        try {
            UniversalTimeScale.bigDecimalFrom(0.0, -1);
            errln("bigDecimalFrom(0.0, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.bigDecimalFrom(0.0, scale);
            } catch (IllegalArgumentException iae) {
                errln("bigDecimalFrom(0.0, " + scale + ") threw IllegalArgumentException.");
            }
       }
        
        try {
            UniversalTimeScale.bigDecimalFrom(0.0, UniversalTimeScale.MAX_SCALE);
            errln("from(0.0, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
    }
    
    @Test
    public void TestBigDecimalFromLong()
    {
        try {
            UniversalTimeScale.bigDecimalFrom(0L, -1);
            errln("bigDecimalFrom(0L, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.bigDecimalFrom(0L, scale);
            } catch (IllegalArgumentException iae) {
                errln("bigDecimalFrom(0L, " + scale + ") threw IllegalArgumentException.");
            }
       }
        
        try {
            UniversalTimeScale.bigDecimalFrom(0L, UniversalTimeScale.MAX_SCALE);
            errln("from(0L, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.bigDecimalFrom failed as expected");
        }
    }
    
    @Test
    public void TestFromLong()
    {
        long result;
        
        try {
            result = UniversalTimeScale.from(0L, -1);
            errln("from(0L, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.from failed as expected");
        }

        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            long fromMin = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.FROM_MIN_VALUE);
            long fromMax = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.FROM_MAX_VALUE);
            
            try {
                result = UniversalTimeScale.from(0L, scale);
                logln("from(0L, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("from(0L, " + scale + ") threw IllegalArgumentException.");
            }

            try {
                result = UniversalTimeScale.from(fromMin, scale);
                logln("from(fromMin, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("from(fromMin, " + scale + ") threw IllegalArgumentException.");
            }
             
            if (fromMin > Long.MIN_VALUE) {
                try {
                    result = UniversalTimeScale.from(fromMin - 1, scale);
                    errln("from(fromMin - 1, " + scale + ") did not throw IllegalArgumentException.");
                } catch (IllegalArgumentException iae) {
                    logln("PASS: UniversalTimeScale.from failed as expected");
                }
            }
             
            try {
                result = UniversalTimeScale.from(fromMax, scale);
                logln("from(fromMax, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("from(fromMax, " + scale + ") threw IllegalArgumentException.");
            }
              
            if (fromMax < Long.MAX_VALUE) {
                try {
                    result = UniversalTimeScale.from(fromMax + 1, scale);
                    errln("from(fromMax + 1, " + scale + ") did not throw IllegalArgumentException.");
               } catch (IllegalArgumentException iae) {
                logln("PASS: UniversalTimeScale.from failed as expected");
               }
            }
       }
        
        try {
            result = UniversalTimeScale.from(0L, UniversalTimeScale.MAX_SCALE);
            errln("from(0L, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.from failed as expected");
        }
    }
    
    @Test
    public void TestGetTimeScale()
    {
        long value;
        
        try {
            value = UniversalTimeScale.getTimeScaleValue(-1, 0);
            errln("getTimeScaleValue(-1, 0) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.getTimeScaleValue failed as expected");
        }
        
        try {
            value = UniversalTimeScale.getTimeScaleValue(0, -1);
            errln("getTimeScaleValue(0, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.getTimeScaleValue failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                value = UniversalTimeScale.getTimeScaleValue(scale, 0);
                logln("getTimeScaleValue(" + scale + ", 0) returned " + value);
            } catch (IllegalArgumentException iae) {
                errln("getTimeScaleValue(" + scale + ", 0) threw IllegalArgumentException.");
            }
        }
        
        try {
            value = UniversalTimeScale.getTimeScaleValue(UniversalTimeScale.MAX_SCALE, 0);
            errln("getTimeScaleValue(MAX_SCALE, 0) did not throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.getTimeScaleValue failed as expected");
        }
        
        try {
            value = UniversalTimeScale.getTimeScaleValue(0, UniversalTimeScale.MAX_SCALE_VALUE);
            errln("getTimeScaleValue(0, MAX_SCALE_VALUE) did not throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.getTimeScaleValue failed as expected");
        }
    }
    
    @Test
    public void TestToBigDecimalFromBigDecimal()
    {
        BigDecimal bigZero = new BigDecimal(0);
        
        try {
            UniversalTimeScale.toBigDecimal(bigZero, -1);
            errln("toBigDecimal(bigZero, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimal failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.toBigDecimal(bigZero, scale);
            } catch (IllegalArgumentException iae) {
                errln("toBigDecimal(bigZero, " + scale + ") threw IllegalArgumentException.");
            }
        }
        
        try {
            UniversalTimeScale.toBigDecimal(bigZero, UniversalTimeScale.MAX_SCALE);
            errln("toBigDecimal(bigZero, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimal failed as expected");
        }
    }

    @Test
    public void TestToBigDecimalTrunc()
    {
        BigDecimal bigZero = new BigDecimal(0);
        
        try {
            UniversalTimeScale.toBigDecimalTrunc(bigZero, -1);
            errln("toBigDecimalTrunc(bigZero, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimalTrunc failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.toBigDecimalTrunc(bigZero, scale);
            } catch (IllegalArgumentException iae) {
                errln("toBigDecimalTrunc(bigZero, " + scale + ") threw IllegalArgumentException.");
            }
        }
        
        try {
            UniversalTimeScale.toBigDecimalTrunc(bigZero, UniversalTimeScale.MAX_SCALE);
            errln("toBigDecimalTrunc(bigZero, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimalTrunc failed as expected");
        }
    }
    
    @Test
    public void TestToBigDecimalFromLong()
    {
        try {
            UniversalTimeScale.toBigDecimal(0L, -1);
            errln("toBigDecimal(0L, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimal failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            try {
                UniversalTimeScale.toBigDecimal(0L, scale);
            } catch (IllegalArgumentException iae) {
                errln("toBigDecimal(0L, " + scale + ") threw IllegalArgumentException.");
            }
        }
        
        try {
            UniversalTimeScale.toBigDecimal(0L, UniversalTimeScale.MAX_SCALE);
            errln("toBigDecimal(0L, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toBigDecimal failed as expected");
        }
    }
    
    @Test
    public void TestToLong()
    {
        long result;
        
        try {
            result = UniversalTimeScale.toLong(0L, -1);
            errln("toLong(0L, -1) did not throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toLong failed as expected");
        }
        
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            long toMin = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.TO_MIN_VALUE);
            long toMax = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.TO_MAX_VALUE);
            
            try {
                result = UniversalTimeScale.toLong(0L, scale);
                logln("toLong(0L, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("toLong(0L, " + scale + ") threw IllegalArgumentException.");
            }
            
            try {
                result = UniversalTimeScale.toLong(toMin, scale);
                logln("toLong(toMin, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("toLong(toMin, " + scale + ") threw IllegalArgumentException.");
            }
             
            if (toMin > Long.MIN_VALUE) {
                try {
                    result = UniversalTimeScale.toLong(toMin - 1, scale);
                    errln("toLong(toMin - 1, " + scale + ") did not throw IllegalArgumentException.");
                } catch (IllegalArgumentException iae) {
                    logln("PASS: UniversalTimeScale.toLong failed as expected");
                }
            }
             
            try {
                result = UniversalTimeScale.toLong(toMax, scale);
                logln("toLong(toMax, " + scale + ") returned " + result);
            } catch (IllegalArgumentException iae) {
                errln("toLong(toMax, " + scale + ") threw IllegalArgumentException.");
            }
              
            if (toMax < Long.MAX_VALUE) {
                try {
                    result = UniversalTimeScale.toLong(toMax + 1, scale);
                    errln("toLong(toMax + 1, " + scale + ") did not throw IllegalArgumentException.");
               } catch (IllegalArgumentException iae) {
                logln("PASS: UniversalTimeScale.toLong failed as expected");
               }
            }
       }
        
        try {
            result = UniversalTimeScale.toLong(0L, UniversalTimeScale.MAX_SCALE);
            errln("toLong(0L, MAX_SCALE) did not throw IllegalArgumetException.");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UniversalTimeScale.toLong failed as expected");
        }
    }
}
