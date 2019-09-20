/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.icu.impl.Grego;
import android.icu.impl.Utility;
import android.icu.util.Currency.CurrencyUsage;

/**
 * Provides information about currencies that is not specific to a locale.
 *
 * A note about currency dates.  The CLDR data provides data to the day,
 * inclusive.  The date information used by CurrencyInfo and CurrencyFilter
 * is represented by milliseconds, which is overly precise.  These times are
 * in GMT, so queries involving dates should use GMT times, but more generally
 * you should avoid relying on time of day in queries.
 *
 * This class is not intended for public subclassing.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class CurrencyMetaInfo {
    private static final CurrencyMetaInfo impl;
    private static final boolean hasData;

    /**
     * Returns the unique instance of the currency meta info.
     * @return the meta info
     */
    public static CurrencyMetaInfo getInstance() {
        return impl;
    }

    /**
     * Returns the unique instance of the currency meta info, or null if
     * noSubstitute is true and there is no data to support this API.
     * @param noSubstitute true if no substitute data should be used
     * @return the meta info, or null
     */
    public static CurrencyMetaInfo getInstance(boolean noSubstitute) {
        return hasData ? impl : null;
    }

    /**
     * Returns true if there is data for the currency meta info.
     * @return true if there is actual data
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static boolean hasData() {
        return hasData;
    }

    /**
     * Subclass constructor.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected CurrencyMetaInfo() {
    }

    /**
     * A filter used to select which currency info is returned.
     */
    public static final class CurrencyFilter {
        /**
         * The region to filter on.  If null, accepts any region.
         */
        public final String region;

        /**
         * The currency to filter on.  If null, accepts any currency.
         */
        public final String currency;

        /**
         * The from date to filter on (as milliseconds).  Accepts any currency on or after this date.
         */
        public final long from;

        /**
         * The to date to filter on (as milliseconds).  Accepts any currency on or before this date.
         */
        public final long to;

        /**
         * true if we are filtering only for currencies used as legal tender.
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public final boolean tenderOnly;

        private CurrencyFilter(String region, String currency, long from, long to, boolean tenderOnly) {
            this.region = region;
            this.currency = currency;
            this.from = from;
            this.to = to;
            this.tenderOnly = tenderOnly;

        }

        private static final CurrencyFilter ALL = new CurrencyFilter(
                null, null, Long.MIN_VALUE, Long.MAX_VALUE, false);

        /**
         * Returns a filter that accepts all currency data.
         * @return a filter
         */
        public static CurrencyFilter all() {
            return ALL;
        }

        /**
         * Returns a filter that accepts all currencies in use as of the current date.
         * @return a filter
         * @see #withDate(Date)
         */
        public static CurrencyFilter now() {
            return ALL.withDate(new Date());
        }

        /**
         * Returns a filter that accepts all currencies ever used in the given region.
         * @param region the region code
         * @return a filter
         * @see #withRegion(String)
         */
        public static CurrencyFilter onRegion(String region) {
            return ALL.withRegion(region);
        }

        /**
         * Returns a filter that accepts the given currency.
         * @param currency the currency code
         * @return a filter
         * @see #withCurrency(String)
         */
        public static CurrencyFilter onCurrency(String currency) {
            return ALL.withCurrency(currency);
        }

        /**
         * Returns a filter that accepts all currencies in use on the given date.
         * @param date the date
         * @return a filter
         * @see #withDate(Date)
         */
        public static CurrencyFilter onDate(Date date) {
            return ALL.withDate(date);
        }

        /**
         * Returns a filter that accepts all currencies that were in use at some point between
         * the given dates, or if dates are equal, currencies in use on that date.
         * @param from date on or after a currency must have been in use
         * @param to date on or before which a currency must have been in use,
         * or if equal to from, the date on which a currency must have been in use
         * @return a filter
         * @see #withDateRange(Date, Date)
         */
        public static CurrencyFilter onDateRange(Date from, Date to) {
            return ALL.withDateRange(from, to);
        }

        /**
         * Returns a filter that accepts all currencies in use on the given date.
         * @param date the date as milliseconds after Jan 1, 1970
         */
        public static CurrencyFilter onDate(long date) {
            return ALL.withDate(date);
        }

        /**
         * Returns a filter that accepts all currencies that were in use at some
         * point between the given dates, or if dates are equal, currencies in
         * use on that date.
         * @param from The date on or after a currency must have been in use.
         *   Measured in milliseconds since Jan 1, 1970 GMT.
         * @param to The date on or before which a currency must have been in use.
         *   Measured in milliseconds since Jan 1, 1970 GMT.
         */
        public static CurrencyFilter onDateRange(long from, long to) {
            return ALL.withDateRange(from, to);
        }

        /**
         * Returns a CurrencyFilter for finding currencies that were either once used,
         * are used, or will be used as tender.
         */
        public static CurrencyFilter onTender() {
            return ALL.withTender();
        }

        /**
         * Returns a copy of this filter, with the specified region.  Region can be null to
         * indicate no filter on region.
         * @param region the region code
         * @return the filter
         * @see #onRegion(String)
         */
        public CurrencyFilter withRegion(String region) {
            return new CurrencyFilter(region, this.currency, this.from, this.to, this.tenderOnly);
        }

        /**
         * Returns a copy of this filter, with the specified currency.  Currency can be null to
         * indicate no filter on currency.
         * @param currency the currency code
         * @return the filter
         * @see #onCurrency(String)
         */
        public CurrencyFilter withCurrency(String currency) {
            return new CurrencyFilter(this.region, currency, this.from, this.to, this.tenderOnly);
        }

        /**
         * Returns a copy of this filter, with from and to set to the given date.
         * @param date the date on which the currency must have been in use
         * @return the filter
         * @see #onDate(Date)
         */
        public CurrencyFilter withDate(Date date) {
            return new CurrencyFilter(this.region, this.currency, date.getTime(), date.getTime(), this.tenderOnly);
        }

        /**
         * Returns a copy of this filter, with from and to set to the given dates.
         * @param from date on or after which the currency must have been in use
         * @param to date on or before which the currency must have been in use
         * @return the filter
         * @see #onDateRange(Date, Date)
         */
        public CurrencyFilter withDateRange(Date from, Date to) {
            long fromLong = from == null ? Long.MIN_VALUE : from.getTime();
            long toLong = to == null ? Long.MAX_VALUE : to.getTime();
            return new CurrencyFilter(this.region, this.currency, fromLong, toLong, this.tenderOnly);
        }

        /**
         * Returns a copy of this filter that accepts all currencies in use on
         * the given date.
         * @param date the date as milliseconds after Jan 1, 1970
         */
        public CurrencyFilter withDate(long date) {
            return new CurrencyFilter(this.region, this.currency, date, date, this.tenderOnly);
        }

        /**
         * Returns a copy of this filter that accepts all currencies that were
         * in use at some point between the given dates, or if dates are equal,
         * currencies in use on that date.
         * @param from The date on or after a currency must have been in use.
         *   Measured in milliseconds since Jan 1, 1970 GMT.
         * @param to The date on or before which a currency must have been in use.
         *   Measured in milliseconds since Jan 1, 1970 GMT.
         */
        public CurrencyFilter withDateRange(long from, long to) {
            return new CurrencyFilter(this.region, this.currency, from, to, this.tenderOnly);
        }

        /**
         * Returns a copy of this filter that filters for currencies that were
         * either once used, are used, or will be used as tender.
         */
        public CurrencyFilter withTender() {
            return new CurrencyFilter(this.region, this.currency, this.from, this.to, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object rhs) {
            return rhs instanceof CurrencyFilter &&
                equals((CurrencyFilter) rhs);
        }

        /**
         * Type-safe override of {@link #equals(Object)}.
         * @param rhs the currency filter to compare to
         * @return true if the filters are equal
         */
        public boolean equals(CurrencyFilter rhs) {
          return Utility.sameObjects(this, rhs) || (rhs != null &&
                    equals(this.region, rhs.region) &&
                    equals(this.currency, rhs.currency) &&
                    this.from == rhs.from &&
                    this.to == rhs.to &&
                    this.tenderOnly == rhs.tenderOnly);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int hc = 0;
            if (region != null) {
                hc = region.hashCode();
            }
            if (currency != null) {
                hc = hc * 31 + currency.hashCode();
            }
            hc = hc * 31 + (int) from;
            hc = hc * 31 + (int) (from >>> 32);
            hc = hc * 31 + (int) to;
            hc = hc * 31 + (int) (to >>> 32);
            hc = hc * 31 + (tenderOnly ? 1 : 0);
            return hc;
        }

        /**
         * Returns a string representing the filter, for debugging.
         * @return A string representing the filter.
         */
        @Override
        public String toString() {
            return debugString(this);
        }

        private static boolean equals(String lhs, String rhs) {
            return (Utility.sameObjects(lhs, rhs) ||
                    (lhs != null && lhs.equals(rhs)));
        }
    }

    /**
     * Represents the raw information about fraction digits and rounding increment.
     */
    public static final class CurrencyDigits {
        /**
         * Number of fraction digits used to display this currency.
         */
        public final int fractionDigits;
        /**
         * Rounding increment used when displaying this currency.
         */
        public final int roundingIncrement;

        /**
         * Constructor for CurrencyDigits.
         * @param fractionDigits the fraction digits
         * @param roundingIncrement the rounding increment
         */
        public CurrencyDigits(int fractionDigits, int roundingIncrement) {
            this.fractionDigits = fractionDigits;
            this.roundingIncrement = roundingIncrement;
        }

        /**
         * Returns a string representing the currency digits, for debugging.
         * @return A string representing the currency digits.
         */
        @Override
        public String toString() {
            return debugString(this);
        }
    }

    /**
     * Represents a complete currency info record listing the region, currency, from and to dates,
     * and priority.
     * Use {@link CurrencyMetaInfo#currencyInfo(CurrencyFilter)}
     * for a list of info objects matching the filter.
     */
    public static final class CurrencyInfo {
        /**
         * Region code where currency is used.
         */
        public final String region;

        /**
         * The three-letter ISO currency code.
         */
        public final String code;

        /**
         * Date on which the currency was first officially used in the region.
         * This is midnight at the start of the first day on which the currency was used, GMT.
         * If there is no date, this is Long.MIN_VALUE;
         */
        public final long from;

        /**
         * Date at which the currency stopped being officially used in the region.
         * This is one millisecond before midnight at the end of the last day on which the currency was used, GMT.
         * If there is no date, this is Long.MAX_VALUE.
         */
        public final long to;

        /**
         * Preference order of currencies being used at the same time in the region.  Lower
         * values are preferred (generally, this is a transition from an older to a newer
         * currency).  Priorities within a single country are unique.
         */
        public final int priority;


        private final boolean tender;

        /**
         * @deprecated ICU 51 Use {@link CurrencyMetaInfo#currencyInfo(CurrencyFilter)} instead.
         */
        @Deprecated
        public CurrencyInfo(String region, String code, long from, long to, int priority) {
            this(region, code, from, to, priority, true);
        }

        /**
         * Constructs a currency info.
         *
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public CurrencyInfo(String region, String code, long from, long to, int priority, boolean tender) {
            this.region = region;
            this.code = code;
            this.from = from;
            this.to = to;
            this.priority = priority;
            this.tender = tender;
        }

        /**
         * Returns a string representation of this object, useful for debugging.
         * @return A string representation of this object.
         */
        @Override
        public String toString() {
            return debugString(this);
        }

        /**
         * Determine whether or not this currency was once used, is used,
         * or will be used as tender in this region.
         */
        public boolean isTender() {
            return tender;
        }
    }

///CLOVER:OFF
    /**
     * Returns the list of CurrencyInfos matching the provided filter.  Results
     * are ordered by country code, then by highest to lowest priority (0 is highest).
     * The returned list is unmodifiable.
     * @param filter the filter to control which currency info to return
     * @return the matching information
     */
    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    /**
     * Returns the list of currency codes matching the provided filter.
     * Results are ordered as in {@link #currencyInfo(CurrencyFilter)}.
     * The returned list is unmodifiable.
     * @param filter the filter to control which currencies to return.  If filter is null,
     * returns all currencies for which information is available.
     * @return the matching currency codes
     */
    public List<String> currencies(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    /**
     * Returns the list of region codes matching the provided filter.
     * Results are ordered as in {@link #currencyInfo(CurrencyFilter)}.
     * The returned list is unmodifiable.
     * @param filter the filter to control which regions to return.  If filter is null,
     * returns all regions for which information is available.
     * @return the matching region codes
     */
    public List<String> regions(CurrencyFilter filter) {
        return Collections.emptyList();
    }
///CLOVER:ON

    /**
     * Returns the CurrencyDigits for the currency code.
     * This is equivalent to currencyDigits(isoCode, CurrencyUsage.STANDARD);
     * @param isoCode the currency code
     * @return the CurrencyDigits
     */
    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    /**
     * Returns the CurrencyDigits for the currency code with Context Usage.
     * @param isoCode the currency code
     * @param currencyUsage the currency usage
     * @return the CurrencyDigits
     */
    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyUsage) {
        return defaultDigits;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected static final CurrencyDigits defaultDigits = new CurrencyDigits(2, 0);

    static {
        CurrencyMetaInfo temp = null;
        boolean tempHasData = false;
        try {
            Class<?> clzz = Class.forName("android.icu.impl.ICUCurrencyMetaInfo");
            temp = (CurrencyMetaInfo) clzz.newInstance();
            tempHasData = true;
        } catch (Throwable t) {
            temp = new CurrencyMetaInfo();
        }
        impl = temp;
        hasData = tempHasData;
    }

    private static String dateString(long date) {
        if (date == Long.MAX_VALUE || date == Long.MIN_VALUE) {
            return null;
        }
        return Grego.timeToString(date);
    }

    private static String debugString(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field f : o.getClass().getFields()) {
                Object v = f.get(o);
                if (v != null) {
                    String s;
                    if (v instanceof Date) {
                        s = dateString(((Date)v).getTime());
                    } else if (v instanceof Long) {
                        s = dateString(((Long)v).longValue());
                    } else {
                        s = String.valueOf(v);
                    }
                    if (s == null) {
                        continue;
                    }
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(f.getName())
                        .append("='")
                        .append(s)
                        .append("'");
                }
            }
        } catch (Throwable t) {
        }
        sb.insert(0, o.getClass().getSimpleName() + "(");
        sb.append(")");
        return sb.toString();
    }
}
