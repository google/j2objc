/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2004-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 20, 2004
* Since: ICU 3.0
**********************************************************************
*/
package android.icu.util;


/**
 * An amount of a specified unit, consisting of a Number and a Unit.
 * For example, a length measure consists of a Number and a length
 * unit, such as feet or meters.
 *
 * <p>Measure objects are parsed and formatted by subclasses of
 * MeasureFormat.
 *
 * <p>Measure objects are immutable. All subclasses must guarantee that.
 * (However, subclassing is discouraged.)
 *
 * @see java.lang.Number
 * @see android.icu.util.MeasureUnit
 * @see android.icu.text.MeasureFormat
 * @author Alan Liu
 */
public class Measure {

    private final Number number;
    private final MeasureUnit unit;

    /**
     * Constructs a new object given a number and a unit.
     * @param number the number
     * @param unit the unit
     */
    public Measure(Number number, MeasureUnit unit) {
        if (number == null || unit == null) {
            throw new NullPointerException();
        }
        this.number = number;
        this.unit = unit;
    }

    /**
     * Returns true if the given object is equal to this object.
     * @return true if this object is equal to the given object
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Measure)) {
            return false;
        }
        Measure m = (Measure) obj;
        return unit.equals(m.unit) && numbersEqual(number, m.number);
    }

    /*
     * See if two numbers are identical or have the same double value.
     * @param a A number
     * @param b Another number to be compared with
     * @return Returns true if two numbers are identical or have the same double value.
     */
    // TODO improve this to catch more cases (two different longs that have same double values, BigDecimals, etc)
    private static boolean numbersEqual(Number a, Number b) {
        if (a.equals(b)) {
            return true;
        }
        if (a.doubleValue() == b.doubleValue()) {
            return true;
        }
        return false;
    }

    /**
     * Returns a hashcode for this object.
     * @return a 32-bit hash
     */
    @Override
    public int hashCode() {
        return 31 * Double.valueOf(number.doubleValue()).hashCode() + unit.hashCode();
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation consisting of the ISO currency
     * code together with the numeric amount
     */
    @Override
    public String toString() {
        return number.toString() + ' ' + unit.toString();
    }

    /**
     * Returns the numeric value of this object.
     * @return this object's Number
     */
    public Number getNumber() {
        return number;
    }

    /**
     * Returns the unit of this object.
     * @return this object's Unit
     */
    public MeasureUnit getUnit() {
        return unit;
    }
}
