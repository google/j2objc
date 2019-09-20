/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *********************************************************************************
 * Copyright (C) 2004-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                  *
 *********************************************************************************
 *
 */

package android.icu.util;

import android.icu.math.BigDecimal;

/** 
 * There are quite a few different conventions for binary datetime, depending on different
 * platforms and protocols. Some of these have severe drawbacks. For example, people using
 * Unix time (seconds since Jan 1, 1970, usually in a 32-bit integer)
 * think that they are safe until near the year 2038.
 * But cases can and do arise where arithmetic manipulations causes serious problems. Consider
 * the computation of the average of two datetimes, for example: if one calculates them with
 * <code>averageTime = (time1 + time2)/2</code>, there will be overflow even with dates
 * beginning in 2004. Moreover, even if these problems don't occur, there is the issue of
 * conversion back and forth between different systems.
 *
 * <p>Binary datetimes differ in a number of ways: the datatype, the unit,
 * and the epoch (origin). We refer to these as time scales.
 *
 * <p>ICU implements a universal time scale that is similar to the
 * .NET framework's System.DateTime. The universal time scale is a
 * 64-bit integer that holds ticks since midnight, January 1st, 0001.
 * (One tick is 100 nanoseconds.)
 * Negative values are supported. This has enough range to guarantee that
 * calculations involving dates around the present are safe.
 *
 * <p>The universal time scale always measures time according to the
 * proleptic Gregorian calendar. That is, the Gregorian calendar's
 * leap year rules are used for all times, even before 1582 when it was
 * introduced. (This is different from the default ICU calendar which
 * switches from the Julian to the Gregorian calendar in 1582.
 * See GregorianCalendar.setGregorianChange() and ucal_setGregorianChange().)
 *
 * ICU provides conversion functions to and from all other major time
 * scales, allowing datetimes in any time scale to be converted to the
 * universal time scale, safely manipulated, and converted back to any other
 * datetime time scale.
 *
 * <p>For more details and background, see the
 * <a href="http://www.icu-project.org/userguide/universalTimeScale.html">Universal Time Scale</a>
 * chapter in the ICU User Guide.
 */

public final class UniversalTimeScale
{
    /**
     * Used in the JDK. Data is a <code>long</code>. Value
     * is milliseconds since January 1, 1970.
     */
    public static final int JAVA_TIME = 0;

    /**
     * Used in Unix systems. Data is an <code>int</code> or a <code>long</code>. Value
     * is seconds since January 1, 1970.
     */
    public static final int UNIX_TIME = 1;

    /**
     * Used in the ICU4C. Data is a <code>double</code>. Value
     * is milliseconds since January 1, 1970.
     */
    public static final int ICU4C_TIME = 2;

    /**
     * Used in Windows for file times. Data is a <code>long</code>. Value
     * is ticks (1 tick == 100 nanoseconds) since January 1, 1601.
     */
    public static final int WINDOWS_FILE_TIME = 3;

    /**
     * Used in the .NET framework's <code>System.DateTime</code> structure.
     * Data is a <code>long</code>. Value is ticks (1 tick == 100 nanoseconds) since January 1, 0001.
     */
    public static final int DOTNET_DATE_TIME = 4;

    /**
     * Used in older Macintosh systems. Data is an <code>int</code>. Value
     * is seconds since January 1, 1904.
     */
    public static final int MAC_OLD_TIME = 5;

    /**
     * Used in the JDK. Data is a <code>double</code>. Value
     * is milliseconds since January 1, 2001.
     */
    public static final int MAC_TIME = 6;

    /**
     * Used in Excel. Data is a <code>?unknown?</code>. Value
     * is days since December 31, 1899.
     */
    public static final int EXCEL_TIME = 7;

    /**
     * Used in DB2. Data is a <code>?unknown?</code>. Value
     * is days since December 31, 1899.
     */
    public static final int DB2_TIME = 8;

    /**
     * Data is a <code>long</code>. Value is microseconds since January 1, 1970.
     * Similar to Unix time (linear value from 1970) and struct timeval
     * (microseconds resolution).
     */
    public static final int UNIX_MICROSECONDS_TIME = 9;
    
    /**
     * This is the first unused time scale value.
     */
    public static final int MAX_SCALE = 10;
    
    /**
     * The constant used to select the units value
     * for a time scale.
     */
    public static final int UNITS_VALUE = 0;
    
    /**
     * The constant used to select the epoch offset value
     * for a time scale.
     * 
     * @see #getTimeScaleValue
     */
    public static final int EPOCH_OFFSET_VALUE = 1;
    
    /**
     * The constant used to select the minimum from value
     * for a time scale.
     * 
     * @see #getTimeScaleValue
     */
    public static final int FROM_MIN_VALUE = 2;
    
    /**
     * The constant used to select the maximum from value
     * for a time scale.
     * 
     * @see #getTimeScaleValue
     */
    public static final int FROM_MAX_VALUE = 3;
    
    /**
     * The constant used to select the minimum to value
     * for a time scale.
     * 
     * @see #getTimeScaleValue
     */
    public static final int TO_MIN_VALUE = 4;
    
    /**
     * The constant used to select the maximum to value
     * for a time scale.
     * 
     * @see #getTimeScaleValue
     */
    public static final int TO_MAX_VALUE = 5;
    
    /**
     * The constant used to select the epoch plus one value
     * for a time scale.
     * 
     * NOTE: This is an internal value. DO NOT USE IT. May not
     * actually be equal to the epoch offset value plus one.
     * 
     * @see #getTimeScaleValue
     */
    public static final int EPOCH_OFFSET_PLUS_1_VALUE = 6;
    
    /**
     * The constant used to select the epoch offset minus one value
     * for a time scale.
     * 
     * NOTE: This is an internal value. DO NOT USE IT. May not
     * actually be equal to the epoch offset value minus one.
     * 
     * @see #getTimeScaleValue
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int EPOCH_OFFSET_MINUS_1_VALUE = 7;
    
    /**
     * The constant used to select the units round value
     * for a time scale.
     * 
     * NOTE: This is an internal value. DO NOT USE IT.
     * 
     * @see #getTimeScaleValue
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int UNITS_ROUND_VALUE = 8;
    
    /**
     * The constant used to select the minimum safe rounding value
     * for a time scale.
     * 
     * NOTE: This is an internal value. DO NOT USE IT.
     * 
     * @see #getTimeScaleValue
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int MIN_ROUND_VALUE = 9;
    
    /**
     * The constant used to select the maximum safe rounding value
     * for a time scale.
     * 
     * NOTE: This is an internal value. DO NOT USE IT.
     * 
     * @see #getTimeScaleValue
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int MAX_ROUND_VALUE = 10;
    
    /**
     * The number of time scale values.
     * 
     * NOTE: This is an internal value. DO NOT USE IT.
     * 
     * @see #getTimeScaleValue
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int MAX_SCALE_VALUE = 11;
    
    private static final long ticks        = 1;
    private static final long microseconds = ticks * 10;
    private static final long milliseconds = microseconds * 1000;
    private static final long seconds      = milliseconds * 1000;
    private static final long minutes      = seconds * 60;
    private static final long hours        = minutes * 60;
    private static final long days         = hours * 24;
    
    /**
     * This class holds the data that describes a particular
     * time scale.
     */
    private static final class TimeScaleData
    {
        TimeScaleData(long theUnits, long theEpochOffset,
                       long theToMin, long theToMax,
                       long theFromMin, long theFromMax)
        {
            units      = theUnits;
            unitsRound = theUnits / 2;
            
            minRound = Long.MIN_VALUE + unitsRound;
            maxRound = Long.MAX_VALUE - unitsRound;
                        
            epochOffset   = theEpochOffset / theUnits;
            
            if (theUnits == 1) {
                epochOffsetP1 = epochOffsetM1 = epochOffset;
            } else {
                epochOffsetP1 = epochOffset + 1;
                epochOffsetM1 = epochOffset - 1;
            }
            
            toMin = theToMin;
            toMax = theToMax;
            
            fromMin = theFromMin;
            fromMax = theFromMax;
        }
        
        long units;
        long epochOffset;
        long fromMin;
        long fromMax;
        long toMin;
        long toMax;
        
        long epochOffsetP1;
        long epochOffsetM1;
        long unitsRound;
        long minRound;
        long maxRound;
    }
    
    private static final TimeScaleData[] timeScaleTable = {
        new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L,         860201606885477L), // JAVA_TIME
        new TimeScaleData(seconds,      621355968000000000L, -9223372036854775808L, 9223372036854775807L, -984472800485L,               860201606885L), // UNIX_TIME
        new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L,         860201606885477L), // ICU4C_TIME
        new TimeScaleData(ticks,        504911232000000000L, -8718460804854775808L, 9223372036854775807L, -9223372036854775808L, 8718460804854775807L), // WINDOWS_FILE_TIME
        new TimeScaleData(ticks,        000000000000000000L, -9223372036854775808L, 9223372036854775807L, -9223372036854775808L, 9223372036854775807L), // DOTNET_DATE_TIME
        new TimeScaleData(seconds,      600527520000000000L, -9223372036854775808L, 9223372036854775807L, -982389955685L,               862284451685L), // MAC_OLD_TIME
        new TimeScaleData(seconds,      631139040000000000L, -9223372036854775808L, 9223372036854775807L, -985451107685L,               859223299685L), // MAC_TIME
        new TimeScaleData(days,         599265216000000000L, -9223372036854775808L, 9223372036854775807L, -11368793L,                        9981605L), // EXCEL_TIME
        new TimeScaleData(days,         599265216000000000L, -9223372036854775808L, 9223372036854775807L, -11368793L,                        9981605L), // DB2_TIME
        new TimeScaleData(microseconds, 621355968000000000L, -9223372036854775804L, 9223372036854775804L, -984472800485477580L,   860201606885477580L)  // UNIX_MICROSECONDS_TIME
    };
    
    
    /*
     * Prevent construction of this class.
     */
    ///CLOVER:OFF
    private UniversalTimeScale()
    {
        // nothing to do
    }
    ///CLOVER:ON
    
    /**
     * Convert a <code>long</code> datetime from the given time scale to the universal time scale.
     *
     * @param otherTime The <code>long</code> datetime
     * @param timeScale The time scale to convert from
     * 
     * @return The datetime converted to the universal time scale
     */
    public static long from(long otherTime, int timeScale)
    {
        TimeScaleData data = fromRangeCheck(otherTime, timeScale);
                
        return (otherTime + data.epochOffset) * data.units;
    }

    /**
     * Convert a <code>double</code> datetime from the given time scale to the universal time scale.
     * All calculations are done using <code>BigDecimal</code> to guarantee that the value
     * does not go out of range.
     *
     * @param otherTime The <code>double</code> datetime
     * @param timeScale The time scale to convert from
     * 
     * @return The datetime converted to the universal time scale
     */
    public static BigDecimal bigDecimalFrom(double otherTime, int timeScale)
    {
        TimeScaleData data     = getTimeScaleData(timeScale);
        BigDecimal other       = new BigDecimal(String.valueOf(otherTime));
        BigDecimal units       = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return other.add(epochOffset).multiply(units);
    }

    /**
     * Convert a <code>long</code> datetime from the given time scale to the universal time scale.
     * All calculations are done using <code>BigDecimal</code> to guarantee that the value
     * does not go out of range.
     *
     * @param otherTime The <code>long</code> datetime
     * @param timeScale The time scale to convert from
     * 
     * @return The datetime converted to the universal time scale
     */
    public static BigDecimal bigDecimalFrom(long otherTime, int timeScale)
    {
        TimeScaleData data     = getTimeScaleData(timeScale);
        BigDecimal other       = new BigDecimal(otherTime);
        BigDecimal units       = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return other.add(epochOffset).multiply(units);
    }

    /**
     * Convert a <code>BigDecimal</code> datetime from the given time scale to the universal time scale.
     * All calculations are done using <code>BigDecimal</code> to guarantee that the value
     * does not go out of range.
     *
     * @param otherTime The <code>BigDecimal</code> datetime
     * @param timeScale The time scale to convert from
     * 
     * @return The datetime converted to the universal time scale
     */
    public static BigDecimal bigDecimalFrom(BigDecimal otherTime, int timeScale)
    {
        TimeScaleData data = getTimeScaleData(timeScale);
        
        BigDecimal units = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return otherTime.add(epochOffset).multiply(units);
    }

    /**
     * Convert a datetime from the universal time scale stored as a <code>BigDecimal</code> to a
     * <code>long</code> in the given time scale.
     *
     * Since this calculation requires a divide, we must round. The straight forward
     * way to round by adding half of the divisor will push the sum out of range for values
     * within have the divisor of the limits of the precision of a <code>long</code>. To get around this, we do
     * the rounding like this:
     * 
     * <p><code>
     * (universalTime - units + units/2) / units + 1
     * </code>
     * 
     * <p>
     * (i.e. we subtract units first to guarantee that we'll still be in range when we
     * add <code>units/2</code>. We then need to add one to the quotent to make up for the extra subtraction.
     * This simplifies to:
     * 
     * <p><code>
     * (universalTime - units/2) / units - 1
     * </code>
     * 
     * <p>
     * For negative values to round away from zero, we need to flip the signs:
     * 
     * <p><code>
     * (universalTime + units/2) / units + 1
     * </code>
     * 
     * <p>
     * Since we also need to subtract the epochOffset, we fold the <code>+/- 1</code>
     * into the offset value. (i.e. <code>epochOffsetP1</code>, <code>epochOffsetM1</code>.)
     * 
     * @param universalTime The datetime in the universal time scale
     * @param timeScale The time scale to convert to
     * 
     * @return The datetime converted to the given time scale
     */
    public static long toLong(long universalTime, int timeScale)
    {
        TimeScaleData data = toRangeCheck(universalTime, timeScale);
        
        if (universalTime < 0) {
            if (universalTime < data.minRound) {
                return (universalTime + data.unitsRound) / data.units - data.epochOffsetP1;
            }
            
            return (universalTime - data.unitsRound) / data.units - data.epochOffset;
        }
        
        if (universalTime > data.maxRound) {
            return (universalTime - data.unitsRound) / data.units - data.epochOffsetM1;
        }
        
        return (universalTime + data.unitsRound) / data.units - data.epochOffset;
    }
    
    /**
     * Convert a datetime from the universal time scale to a <code>BigDecimal</code> in the given time scale.
     *
     * @param universalTime The datetime in the universal time scale
     * @param timeScale The time scale to convert to
     * 
     * @return The datetime converted to the given time scale
     */
    public static BigDecimal toBigDecimal(long universalTime, int timeScale)
    {
        TimeScaleData data     = getTimeScaleData(timeScale);
        BigDecimal universal   = new BigDecimal(universalTime);
        BigDecimal units       = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return universal.divide(units, BigDecimal.ROUND_HALF_UP).subtract(epochOffset);
    }
    
    /**
     * Convert a datetime from the universal time scale to a <code>BigDecimal</code> in the given time scale.
     *
     * @param universalTime The datetime in the universal time scale
     * @param timeScale The time scale to convert to
     * 
     * @return The datetime converted to the given time scale
     */
    public static BigDecimal toBigDecimal(BigDecimal universalTime, int timeScale)
    {
        TimeScaleData data     = getTimeScaleData(timeScale);
        BigDecimal units       = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return universalTime.divide(units, BigDecimal.ROUND_HALF_UP).subtract(epochOffset);
    }
    
    /**
     * Return the <code>TimeScaleData</code> object for the given time
     * scale.
     * 
     * @param scale - the time scale
     * @return the <code>TimeScaleData</code> object for the given time scale
     */
    private static TimeScaleData getTimeScaleData(int scale)
    {
        if (scale < 0 || scale >= MAX_SCALE) {
            throw new IllegalArgumentException("scale out of range: " + scale);
        }
        
        return timeScaleTable[scale];
    }
    
    /**
     * Get a value associated with a particular time scale.
     * 
     * @param scale - the time scale
     * @param value - a constant representing the value to get
     * 
     * @return - the value.
     */
    public static long getTimeScaleValue(int scale, int value)
    {
        TimeScaleData data = getTimeScaleData(scale);
        
        switch (value)
        {
        case UNITS_VALUE:
            return data.units;
            
        case EPOCH_OFFSET_VALUE:
            return data.epochOffset;
        
        case FROM_MIN_VALUE:
            return data.fromMin;
            
        case FROM_MAX_VALUE:
            return data.fromMax;
            
        case TO_MIN_VALUE:
            return data.toMin;
            
        case TO_MAX_VALUE:
            return data.toMax;
            
        case EPOCH_OFFSET_PLUS_1_VALUE:
            return data.epochOffsetP1;
            
        case EPOCH_OFFSET_MINUS_1_VALUE:
            return data.epochOffsetM1;
            
        case UNITS_ROUND_VALUE:
            return data.unitsRound;
        
        case MIN_ROUND_VALUE:
            return data.minRound;
            
        case MAX_ROUND_VALUE:
            return data.maxRound;
            
        default:
            throw new IllegalArgumentException("value out of range: " + value);
        }
    }
    
    private static TimeScaleData toRangeCheck(long universalTime, int scale)
    {
        TimeScaleData data = getTimeScaleData(scale);
          
        if (universalTime >= data.toMin && universalTime <= data.toMax) {
            return data;
        }
        
        throw new IllegalArgumentException("universalTime out of range:" + universalTime);
    }
    
    private static TimeScaleData fromRangeCheck(long otherTime, int scale)
    {
        TimeScaleData data = getTimeScaleData(scale);
          
        if (otherTime >= data.fromMin && otherTime <= data.fromMax) {
            return data;
        }
        
        throw new IllegalArgumentException("otherTime out of range:" + otherTime);
    }
    
    /**
     * Convert a time in the Universal Time Scale into another time
     * scale. The division used to do the conversion rounds down.
     * 
     * NOTE: This is an internal routine used by the tool that
     * generates the to and from limits. Use it at your own risk.
     * 
     * @param universalTime the time in the Universal Time scale
     * @param timeScale the time scale to convert to
     * @return the time in the given time scale
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static BigDecimal toBigDecimalTrunc(BigDecimal universalTime, int timeScale)
    {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal units = new BigDecimal(data.units);
        BigDecimal epochOffset = new BigDecimal(data.epochOffset);
        
        return universalTime.divide(units, BigDecimal.ROUND_DOWN).subtract(epochOffset);
    }
}
