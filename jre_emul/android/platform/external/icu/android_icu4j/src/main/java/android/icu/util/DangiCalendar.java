/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import java.util.Date;

import android.icu.util.ULocale.Category;

/**
 * <code>DangiCalendar</code> is a concrete subclass of {@link Calendar}
 * that implements a traditional Korean calendar.
 * 
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public class DangiCalendar extends ChineseCalendar {

    private static final long serialVersionUID = 8156297445349501985L;

    /**
     * The start year of the Korean traditional calendar (Dan-gi) is the inaugural
     * year of Dan-gun (BC 2333).
     */
    private static final int DANGI_EPOCH_YEAR = -2332;

    /**
     * The time zone used for performing astronomical computations for
     * Dangi calendar. In Korea various timezones have been used historically 
     * (cf. http://www.math.snu.ac.kr/~kye/others/lunar.html): 
     *  
     *            - 1908/04/01: GMT+8 
     * 1908/04/01 - 1911/12/31: GMT+8.5 
     * 1912/01/01 - 1954/03/20: GMT+9 
     * 1954/03/21 - 1961/08/09: GMT+8.5 
     * 1961/08/10 -           : GMT+9 
     *  
     * Note that, in 1908-1911, the government did not apply the timezone change 
     * but used GMT+8. In addition, 1954-1961's timezone change does not affect 
     * the lunar date calculation. Therefore, the following simpler rule works: 
     *   
     * -1911: GMT+8 
     * 1912-: GMT+9 
     *  
     * Unfortunately, our astronomer's approximation doesn't agree with the 
     * references (http://www.math.snu.ac.kr/~kye/others/lunar.html and 
     * http://astro.kasi.re.kr/Life/ConvertSolarLunarForm.aspx?MenuID=115) 
     * in 1897/7/30. So the following ad hoc fix is used here: 
     *  
     *     -1896: GMT+8 
     *      1897: GMT+7 
     * 1898-1911: GMT+8 
     * 1912-    : GMT+9 
     */
    private static final TimeZone KOREA_ZONE;

    static {
        InitialTimeZoneRule initialTimeZone = new InitialTimeZoneRule("GMT+8", 8 * ONE_HOUR, 0);
        long[] millis1897 = { (1897 - 1970) * 365L * ONE_DAY }; // some days of error is not a problem here
        long[] millis1898 = { (1898 - 1970) * 365L * ONE_DAY }; // some days of error is not a problem here
        long[] millis1912 = { (1912 - 1970) * 365L * ONE_DAY }; // this doesn't create an issue for 1911/12/20
        TimeZoneRule rule1897 = new TimeArrayTimeZoneRule("Korean 1897", 7 * ONE_HOUR, 0, millis1897,
                DateTimeRule.STANDARD_TIME);
        TimeZoneRule rule1898to1911 = new TimeArrayTimeZoneRule("Korean 1898-1911", 8 * ONE_HOUR, 0, millis1898,
                DateTimeRule.STANDARD_TIME);
        TimeZoneRule ruleFrom1912 = new TimeArrayTimeZoneRule("Korean 1912-", 9 * ONE_HOUR, 0, millis1912,
                DateTimeRule.STANDARD_TIME);

        RuleBasedTimeZone tz = new RuleBasedTimeZone("KOREA_ZONE", initialTimeZone);
        tz.addTransitionRule(rule1897);
        tz.addTransitionRule(rule1898to1911);
        tz.addTransitionRule(ruleFrom1912);
        tz.freeze();
        KOREA_ZONE = tz;
    };

    /**
     * Construct a <code>DangiCalendar</code> with the default time zone and locale.
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public DangiCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Construct a <code>DangiCalendar</code> with the give date set in the default time zone
     * with the default locale.
     * @param date The date to which the new calendar is set.
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public DangiCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        setTime(date);
    }

    /**
     * Construct a <code>DangiCalendar</code>  based on the current time
     * with the given time zone with the given locale.
     * @param zone the given time zone
     * @param locale the given locale
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public DangiCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale, DANGI_EPOCH_YEAR, KOREA_ZONE);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getType() {
        return "dangi";
    }
}
