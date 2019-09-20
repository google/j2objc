/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import java.util.Date;
import java.util.Locale;

/**
 * Implement the Coptic calendar system.
 * <p>
 * CopticCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=coptic"</code>.</p>
 *
 * @see android.icu.util.Calendar
 */
public final class CopticCalendar extends CECalendar 
{
    // jdk1.4.2 serialver
    private static final long serialVersionUID = 5903818751846742911L;

    /** 
     * Constant for ωογτ / تﻮﺗ,
     * the 1st month of the Coptic year. 
     */
    public static final int TOUT = 0;

    /** 
     * Constant for Παοπι / ﻪﺑﺎﺑ,
     * the 2nd month of the Coptic year. 
     */
    public static final int BABA = 1;

    /** 
     * Constant for Αθορ / رﻮﺗﺎﻫ,
     * the 3rd month of the Coptic year. 
     */
    public static final int HATOR = 2;

    /** 
     * Constant for Χοιακ / ﻚﻬﻴﻛ;,
     * the 4th month of the Coptic year. 
     */
    public static final int KIAHK = 3;

    /** 
     * Constant for Τωβι / طﻮﺒﻫ,
     * the 5th month of the Coptic year. 
     */
    public static final int TOBA = 4;

    /** 
     * Constant for Μεϣιρ / ﺮﻴﺸﻣأ,
     * the 6th month of the Coptic year. 
     */
    public static final int AMSHIR = 5;

    /** 
     * Constant for Παρεμϩατ / تﺎﻬﻣﺮﺑ,
     * the 7th month of the Coptic year. 
     */
    public static final int BARAMHAT = 6;

    /** 
     * Constant for Φαρμοθι / هدﻮﻣﺮﺑ, 
     * the 8th month of the Coptic year. 
     */
    public static final int BARAMOUDA = 7;

    /** 
     * Constant for Παϣαν / ﺲﻨﺸﺑ;,
     * the 9th month of the Coptic year. 
     */
    public static final int BASHANS = 8;

    /** 
     * Constant for Παωνι / ﻪﻧؤﻮﺑ,
     * the 10th month of the Coptic year. 
     */
    public static final int PAONA = 9;

    /** 
     * Constant for Επηπ / ﺐﻴﺑأ,
     * the 11th month of the Coptic year. 
     */
    public static final int EPEP = 10;

    /** 
     * Constant for Μεϲωρη / ىﺮﺴﻣ,
     * the 12th month of the Coptic year. 
     */
    public static final int MESRA = 11;

    /** 
     * Constant for Πικογϫι μαβοτ / ﺮﻴﻐﺼﻟاﺮﻬﺸﻟا,
     * the 13th month of the Coptic year. 
     */
    public static final int NASIE = 12;
  
    private static final int JD_EPOCH_OFFSET  = 1824665;

    // Eras
    private static final int BCE = 0;
    private static final int CE = 1;

    /**
     * Constructs a default <code>CopticCalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    public CopticCalendar() {
        super();
    }

    /**
     * Constructs a <code>CopticCalendar</code> based on the current time
     * in the given time zone with the default locale.
     *
     * @param zone The time zone for the new calendar.
     */
    public CopticCalendar(TimeZone zone) {
        super(zone);
    }

    /**
     * Constructs a <code>CopticCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale The locale for the new calendar.
     */
    public CopticCalendar(Locale aLocale) {
        super(aLocale);
    }

    /**
     * Constructs a <code>CopticCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale The icu locale for the new calendar.
     */
    public CopticCalendar(ULocale locale) {
        super(locale);
    }

    /**
     * Constructs a <code>CopticCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     * @param aLocale The locale for the new calendar.
     */
    public CopticCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }
    
    /**
     * Constructs a <code>CopticCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     * @param locale The icu locale for the new calendar.
     */
    public CopticCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }
    
    /**
     * Constructs a <code>CopticCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tout.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     */
    public CopticCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    /**
     * Constructs a <code>CopticCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    public CopticCalendar(Date date) {
        super(date);
    }

    /**
     * Constructs a <code>CopticCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tout.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    public CopticCalendar(int year, int month, int date, int hour,
                          int minute, int second) {
        super(year, month, date, hour, minute, second);
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "coptic";
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleGetExtendedYear() {
        int eyear;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            eyear = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else {
            // The year defaults to the epoch start, the era to AD
            int era = internalGet(ERA, CE);
            if (era == BCE) {
                eyear = 1 - internalGet(YEAR, 1); // Convert to extended year
            } else {
                eyear = internalGet(YEAR, 1); // Default to year 1
            }
        }
        return eyear;
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
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

        if (fields[0] <= 0) {
            era = BCE;
            year = 1 - fields[0];
        } else {
            era = CE;
            year = fields[0];
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
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int getJDEpochOffset() {
        return JD_EPOCH_OFFSET;
    }

    /**
     * Convert an Coptic year, month, and day to a Julian day.
     *
     * @param year the year
     * @param month the month
     * @param date the day
     * @hide draft / provisional / internal are hidden on Android
     */
    // The equivalent operation can be done by public Calendar API.
    // This API was accidentally marked as @draft, but we have no good
    // reason to keep this.  For now, we leave it as is, but may be
    // removed in future.  2008-03-21 yoshito
    public static int copticToJD(long year, int month, int date) {
        return ceToJD(year, month, date, JD_EPOCH_OFFSET);
    }
}

