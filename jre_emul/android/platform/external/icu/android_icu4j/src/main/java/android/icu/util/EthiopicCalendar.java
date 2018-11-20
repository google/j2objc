/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.util;

import java.util.Date;
import java.util.Locale;

import android.icu.impl.CalendarUtil;
import android.icu.util.ULocale.Category;

/**
 * Implement the Ethiopic calendar system.
 * <p>
 * EthiopicCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=ethiopic"</code>.</p>
 *
 * @see android.icu.util.Calendar
 */
public final class EthiopicCalendar extends CECalendar 
{
    //jdk1.4.2 serialver
    private static final long serialVersionUID = -2438495771339315608L;

    /** 
     * Constant for መስከረም, the 1st month of the Ethiopic year.
     */
    public static final int MESKEREM = 0;

    /** 
     * Constant for ጥቅምት, the 2nd month of the Ethiopic year. 
     */
    public static final int TEKEMT = 1;

    /** 
     * Constant for ኅዳር, the 3rd month of the Ethiopic year. 
     */
    public static final int HEDAR = 2;

    /** 
     * Constant for ታኅሣሥ, the 4th month of the Ethiopic year. 
     */
    public static final int TAHSAS = 3;

    /** 
     * Constant for ጥር, the 5th month of the Ethiopic year. 
     */
    public static final int TER = 4;

    /** 
     * Constant for የካቲት, the 6th month of the Ethiopic year. 
     */
    public static final int YEKATIT = 5;

    /** 
     * Constant for መጋቢት, the 7th month of the Ethiopic year. 
     */
    public static final int MEGABIT = 6;

    /** 
     * Constant for ሚያዝያ, the 8th month of the Ethiopic year. 
     */
    public static final int MIAZIA = 7;

    /** 
     * Constant for ግንቦት, the 9th month of the Ethiopic year. 
     */
    public static final int GENBOT = 8;

    /** 
     * Constant for ሰኔ, the 10th month of the Ethiopic year. 
     */
    public static final int SENE = 9;

    /** 
     * Constant for ሐምሌ, the 11th month of the Ethiopic year. 
     */
    public static final int HAMLE = 10;

    /** 
     * Constant for ነሐሴ, the 12th month of the Ethiopic year. 
     */
    public static final int NEHASSE = 11;

    /** 
     * Constant for ጳጉሜን, the 13th month of the Ethiopic year. 
     */
    public static final int PAGUMEN = 12;
 
    // Up until the end of the 19th century the prevailant convention was to
    // reference the Ethiopic Calendar from the creation of the world, 
    // \u12d3\u1218\u1270\u1361\u12d3\u1208\u121d
    // (Amete Alem 5500 BC).  As Ethiopia modernized the reference epoch from
    // the birth of Christ (\u12d3\u1218\u1270\u1361\u121d\u1215\u1228\u1275) 
    // began to displace the creation of the
    // world reference point.  However, years before the birth of Christ are
    // still referenced in the creation of the world system.   
    // Thus -100 \u12d3/\u121d
    // would be rendered as 5400  \u12d3/\u12d3.
    //
    // The creation of the world in Ethiopic cannon was 
    // Meskerem 1, -5500  \u12d3/\u121d 00:00:00
    // applying the birth of Christ reference and Ethiopian time conventions.  This is
    // 6 hours less than the Julian epoch reference point (noon).  In Gregorian
    // the date and time was July 18th -5493 BC 06:00 AM.

    // Julian Days relative to the 
    // \u12d3\u1218\u1270\u1361\u121d\u1215\u1228\u1275 epoch
    // Note: we no longer use this constant
    //private static final int JD_EPOCH_OFFSET_AMETE_ALEM = -285019;

    // Julian Days relative to the 
    // \u12d3\u1218\u1270\u1361\u12d3\u1208\u121d epoch
    private static final int JD_EPOCH_OFFSET_AMETE_MIHRET = 1723856;

    // The delta between Amete Alem 1 and Amete Mihret 1
    // AA 5501 = AM 1
    private static final int AMETE_MIHRET_DELTA = 5500;

    // Eras
    private static final int AMETE_ALEM = 0;
    private static final int AMETE_MIHRET = 1;

    // Era mode.  When eraType is AMETE_ALEM_ERA,
    // Amete Mihret won't be used for the ERA field.
    private static final int AMETE_MIHRET_ERA = 0;
    private static final int AMETE_ALEM_ERA = 1;

    private int eraType = AMETE_MIHRET_ERA;

    /**
     * Constructs a default <code>EthiopicCalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    public EthiopicCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> based on the current time
     * in the given time zone with the default locale.
     *
     * @param zone The time zone for the new calendar.
     */
    public EthiopicCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale The locale for the new calendar.
     */
    public EthiopicCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale The icu locale for the new calendar.
     */
    public EthiopicCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     * @param aLocale The locale for the new calendar.
     */
    public EthiopicCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale));
    }
    
    /**
     * Constructs a <code>EthiopicCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     * @param locale The icu locale for the new calendar.
     */
    public EthiopicCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setCalcTypeForLocale(locale);
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Meskerem.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     */
    public EthiopicCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    public EthiopicCalendar(Date date) {
        super(date);
    }

    /**
     * Constructs a <code>EthiopicCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Meskerem.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    public EthiopicCalendar(int year, int month, int date, int hour,
                            int minute, int second)
    {
        super(year, month, date, hour, minute, second);
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        if (isAmeteAlemEra()) {
            return "ethiopic-amete-alem";
        }
        return "ethiopic";
    }

    /**
     * Set Alem or Mihret era.
     *
     * @param onOff Set Amete Alem era if true, otherwise set Amete Mihret era.
     */
    public void setAmeteAlemEra(boolean onOff) {
        eraType = onOff ? AMETE_ALEM_ERA : AMETE_MIHRET_ERA;
    }
    
    /**
     * Return true if this calendar is set to the Amete Alem era.
     *
     * @return true if set to the Amete Alem era.
     */
    public boolean isAmeteAlemEra() {
        return (eraType == AMETE_ALEM_ERA);
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleGetExtendedYear() {
        // Ethiopic calendar uses EXTENDED_YEAR aligned to
        // Amelete Mihret year always.
        int eyear;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            eyear = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else if (isAmeteAlemEra()){
            eyear = internalGet(YEAR, 1 + AMETE_MIHRET_DELTA)
                    - AMETE_MIHRET_DELTA; // Default to year 1 of Amelete Mihret
        } else {
            // The year defaults to the epoch start, the era to AMETE_MIHRET
            int era = internalGet(ERA, AMETE_MIHRET);
            if (era == AMETE_MIHRET) {
                eyear = internalGet(YEAR, 1); // Default to year 1
            } else {
                eyear = internalGet(YEAR, 1) - AMETE_MIHRET_DELTA;
            }
        }
        return eyear;
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void handleComputeFields(int julianDay) {
        int era, year;
        int[] fields = new int[3];
        jdToCE(julianDay, getJDEpochOffset(), fields);

        // fields[0] eyear
        // fields[1] month
        // fields[2] day

        if (isAmeteAlemEra()) {
            era = AMETE_ALEM;
            year = fields[0] + AMETE_MIHRET_DELTA;
        } else {
            if (fields[0] > 0) {
                era = AMETE_MIHRET;
                year = fields[0];
            } else {
                era = AMETE_ALEM;
                year = fields[0] + AMETE_MIHRET_DELTA;
            }
        }

        internalSet(EXTENDED_YEAR, fields[0]);
        internalSet(ERA, era);
        internalSet(YEAR, year);
        internalSet(MONTH, fields[1]);
        internalSet(DAY_OF_MONTH, fields[2]);
        internalSet(DAY_OF_YEAR, (30 * fields[1]) + fields[2]);
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleGetLimit(int field, int limitType) {
        if (isAmeteAlemEra() && field == ERA) {
            return 0; // Only one era in this mode, era is always 0
        }
        return super.handleGetLimit(field, limitType);
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int getJDEpochOffset() {
        return JD_EPOCH_OFFSET_AMETE_MIHRET;
    }

    /**
     * Convert an Ethiopic year, month, and day to a Julian day.
     *
     * @param year the year
     * @param month the month
     * @param date the day
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    // The equivalent operation can be done by public Calendar API.
    // This API was accidentally marked as @draft, but we have no good
    // reason to keep this.  For now, we leave it as is, but may be
    // removed in future.  2008-03-21 yoshito
    public static int EthiopicToJD(long year, int month, int date) {
        return ceToJD(year, month, date, JD_EPOCH_OFFSET_AMETE_MIHRET);
    }

    /**
     * set type based on locale
     */
    private void setCalcTypeForLocale(ULocale locale) {
        String localeCalType = CalendarUtil.getCalendarType(locale);
        if("ethiopic-amete-alem".equals(localeCalType)) { 
            setAmeteAlemEra(true);
        } else {
            setAmeteAlemEra(false); // default - Amete Mihret
        }
    }
}

