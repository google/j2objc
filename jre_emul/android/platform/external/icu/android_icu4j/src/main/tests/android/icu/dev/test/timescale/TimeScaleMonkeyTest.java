/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ****************************************************************************
 * Copyright (C) 2004-2010, International Business Machines Corporation and *
 * others. All Rights Reserved.                                             *
 ****************************************************************************
 *
 */

package android.icu.dev.test.timescale;

import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.math.BigDecimal;
import android.icu.util.UniversalTimeScale;

/**
 * This class tests the UniversalTimeScale class by
 * generating ramdon values in range and making sure
 * that they round-trip correctly.
 */
public class TimeScaleMonkeyTest extends TestFmwk
{

    /**
     * The default constructor.
     */
    public TimeScaleMonkeyTest()
    {
    }
    
    private static final int LOOP_COUNT = 1000;
    private static final BigDecimal longMax = new BigDecimal(Long.MAX_VALUE);
    
    private Random ran = null;
    
    private long ranInt;
    private long ranMin;
    private long ranMax;
    
    private void initRandom(long min, long max)
    {
        BigDecimal interval = new BigDecimal(max).subtract(new BigDecimal(min));
        
        ranMin = min;
        ranMax = max;
        ranInt = 0;
        
        if (ran == null) {
            ran = createRandom();
        }
        
        if (interval.compareTo(longMax) < 0) {
            ranInt = interval.longValue();
        }
    }
    
    private final long randomInRange()
    {
        long value;
        
        if (ranInt != 0) {
            value = ran.nextLong() % ranInt;
            
            if (value < 0) {
                value = -value;
            }
            
            value += ranMin;
        } else {
            do {
                value = ran.nextLong();
            } while (value < ranMin || value > ranMax);
        }
        
        return value;
    }
    
    @Test
    public void TestRoundTrip()
    {
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            long fromMin = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.FROM_MIN_VALUE);
            long fromMax = UniversalTimeScale.getTimeScaleValue(scale, UniversalTimeScale.FROM_MAX_VALUE);
            int i = 0;
            
            initRandom(fromMin, fromMax);
            
            while (i < LOOP_COUNT) {
                long value = randomInRange();
                                
                long rt = UniversalTimeScale.toLong(UniversalTimeScale.from(value, scale), scale);
                
                if (rt != value) {
                    errln("Round-trip error: time scale = " + scale + ", value = " + value + ", round-trip = " + rt);
                }
                
                i += 1;
            }
        }
    }
}
