/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2004-2010, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 12, 2004
* Since: ICU 3.0
**********************************************************************
*/
package android.icu.util;


/**
 * An amount of currency, consisting of a Number and a Currency.
 * CurrencyAmount objects are immutable.
 *
 * @see java.lang.Number
 * @see Currency
 * @author Alan Liu
 */
public class CurrencyAmount extends Measure {
    
    /**
     * Constructs a new object given a number and a currency.
     * @param number the number
     * @param currency the currency
     */
    public CurrencyAmount(Number number, Currency currency) {
        super(number, currency);
    }

    /**
     * Constructs a new object given a double value and a currency.
     * @param number a double value
     * @param currency the currency
     */
    public CurrencyAmount(double number, Currency currency) {
        super(new Double(number), currency);
    }    
    
    /**
     * Returns the currency of this object.
     * @return this object's Currency
     */
    public Currency getCurrency() {
        return (Currency) getUnit();
    }
}
