/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

/**
 * <b>Note:</b> The Holiday framework is a technology preview.
 * Despite its age, is still draft API, and clients should treat it as such.
 * 
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class HebrewHoliday extends Holiday
{
    private static final HebrewCalendar gCalendar = new HebrewCalendar();

    /**
     * Construct a holiday defined in reference to the Hebrew calendar.
     *
     * @param name The name of the holiday
     * @hide draft / provisional / internal are hidden on Android
     */
    public HebrewHoliday(int month, int date, String name)
    {
        this(month, date, 1, name);
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public HebrewHoliday(int month, int date, int length, String name)
    {
        super(name, new SimpleDateRule(month, date, gCalendar));
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday ROSH_HASHANAH   = new HebrewHoliday(HebrewCalendar.TISHRI,  1,  2,  "Rosh Hashanah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday GEDALIAH        = new HebrewHoliday(HebrewCalendar.TISHRI,  3,      "Fast of Gedaliah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday YOM_KIPPUR      = new HebrewHoliday(HebrewCalendar.TISHRI, 10,      "Yom Kippur");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SUKKOT          = new HebrewHoliday(HebrewCalendar.TISHRI, 15,  6,  "Sukkot");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday HOSHANAH_RABBAH = new HebrewHoliday(HebrewCalendar.TISHRI, 21,      "Hoshanah Rabbah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SHEMINI_ATZERET = new HebrewHoliday(HebrewCalendar.TISHRI, 22,      "Shemini Atzeret");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SIMCHAT_TORAH   = new HebrewHoliday(HebrewCalendar.TISHRI, 23,      "Simchat Torah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday HANUKKAH        = new HebrewHoliday(HebrewCalendar.KISLEV, 25,      "Hanukkah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday TEVET_10        = new HebrewHoliday(HebrewCalendar.TEVET,  10,      "Fast of Tevet 10");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday TU_BSHEVAT      = new HebrewHoliday(HebrewCalendar.SHEVAT, 15,      "Tu B'Shevat");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday ESTHER          = new HebrewHoliday(HebrewCalendar.ADAR,   13,      "Fast of Esther");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday PURIM           = new HebrewHoliday(HebrewCalendar.ADAR,   14,      "Purim");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SHUSHAN_PURIM   = new HebrewHoliday(HebrewCalendar.ADAR,   15,      "Shushan Purim");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday PASSOVER        = new HebrewHoliday(HebrewCalendar.NISAN,  15,  8,  "Passover");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday YOM_HASHOAH     = new HebrewHoliday(HebrewCalendar.NISAN,  27,      "Yom Hashoah");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday YOM_HAZIKARON   = new HebrewHoliday(HebrewCalendar.IYAR,    4,      "Yom Hazikaron");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday YOM_HAATZMAUT   = new HebrewHoliday(HebrewCalendar.IYAR,    5,      "Yom Ha'Atzmaut");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday PESACH_SHEINI   = new HebrewHoliday(HebrewCalendar.IYAR,   14,      "Pesach Sheini");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday LAG_BOMER       = new HebrewHoliday(HebrewCalendar.IYAR,   18,      "Lab B'Omer");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday YOM_YERUSHALAYIM = new HebrewHoliday(HebrewCalendar.IYAR,   28,      "Yom Yerushalayim");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SHAVUOT         = new HebrewHoliday(HebrewCalendar.SIVAN,   6,  2,  "Shavuot");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday TAMMUZ_17       = new HebrewHoliday(HebrewCalendar.TAMUZ,  17,      "Fast of Tammuz 17");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday TISHA_BAV       = new HebrewHoliday(HebrewCalendar.AV,      9,      "Fast of Tisha B'Av");

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public static HebrewHoliday SELIHOT         = new HebrewHoliday(HebrewCalendar.ELUL,   21,      "Selihot");
}
