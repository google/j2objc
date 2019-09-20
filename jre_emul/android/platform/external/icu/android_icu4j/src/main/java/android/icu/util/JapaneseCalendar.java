/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;
import java.util.Date;
import java.util.Locale;

/**
 * <code>JapaneseCalendar</code> is a subclass of <code>GregorianCalendar</code>
 * that numbers years and eras based on the reigns of the Japanese emperors.
 * The Japanese calendar is identical to the Gregorian calendar in all respects
 * except for the year and era.  The ascension of each  emperor to the throne
 * begins a new era, and the years of that era are numbered starting with the
 * year of ascension as year 1.
 * <p>
 * Note that in the year of an imperial ascension, there are two possible sets
 * of year and era values: that for the old era and for the new.  For example, a
 * new era began on January 7, 1989 AD.  Strictly speaking, the first six days
 * of that year were in the Showa era, e.g. "January 6, 64 Showa", while the rest
 * of the year was in the Heisei era, e.g. "January 7, 1 Heisei".  This class
 * handles this distinction correctly when computing dates.  However, in lenient
 * mode either form of date is acceptable as input. 
 * <p>
 * In modern times, eras have started on January 8, 1868 AD, Gregorian (Meiji),
 * July 30, 1912 (Taisho), December 25, 1926 (Showa), and January 7, 1989 (Heisei).  Constants
 * for these eras, suitable for use in the <code>ERA</code> field, are provided
 * in this class.  Note that the <em>number</em> used for each era is more or
 * less arbitrary.  Currently, the era starting in 1053 AD is era #0; however this
 * may change in the future as we add more historical data.  Use the predefined
 * constants rather than using actual, absolute numbers.
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * JapaneseCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=japanese"</code>.</p>
 *
 * @see android.icu.util.GregorianCalendar
 * @see android.icu.util.Calendar
 *
 * @author Laura Werner
 * @author Alan Liu
 */
public class JapaneseCalendar extends GregorianCalendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = -2977189902603704691L;

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>JapaneseCalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    public JapaneseCalendar() {
        super();
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the given time zone with the default locale.
     * @param zone the given time zone.
     */
    public JapaneseCalendar(TimeZone zone) {
        super(zone);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the default time zone with the given locale.
     * @param aLocale the given locale.
     */
    public JapaneseCalendar(Locale aLocale) {
        super(aLocale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the default time zone with the given locale.
     * @param locale the given ulocale.
     */
    public JapaneseCalendar(ULocale locale) {
        super(locale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     *
     * @param aLocale the given locale.
     */
    public JapaneseCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     *
     * @param locale the given ulocale.
     */
    public JapaneseCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    public JapaneseCalendar(Date date) {
        this();
        setTime(date);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param era       The imperial era used to set the calendar's {@link #ERA ERA} field.
     *                  Eras are numbered starting with the Tenki era, which
     *                  began in 1053 AD Gregorian, as era zero.  Recent
     *                  eras can be specified using the constants
     *                  {@link #MEIJI} (which started in 1868 AD),
     *                  {@link #TAISHO} (1912 AD),
     *                  {@link #SHOWA} (1926 AD), and
     *                  {@link #HEISEI} (1989 AD).
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} field,
     *                  in terms of the era.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's DATE field.
     */
    public JapaneseCalendar(int era, int year, int month, int date) {
        super(year, month, date);
        set(ERA, era);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} field,
     *                  in the era Heisei, the most current at the time this
     *                  class was last updated.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} field.
     */
    public JapaneseCalendar(int year, int month, int date) {
        super(year, month, date);
        set(ERA, CURRENT_ERA);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field,
     *                  in the era Heisei, the most current at the time of this
     *                  writing.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     *
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     *
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     *
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    public JapaneseCalendar(int year, int month, int date, int hour,
                             int minute, int second)
    {
        super(year, month, date, hour, minute, second);
        set(ERA, CURRENT_ERA);
    }

    //-------------------------------------------------------------------------

    // Use 1970 as the default value of EXTENDED_YEAR
    private static final int GREGORIAN_EPOCH = 1970;

    /**
     */
    protected int handleGetExtendedYear() {
        // EXTENDED_YEAR in JapaneseCalendar is a Gregorian year
        // The default value of EXTENDED_YEAR is 1970 (Showa 45)
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR &&
            newerField(EXTENDED_YEAR, ERA) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, GREGORIAN_EPOCH);
        } else {
            // extended year is a gregorian year, where 1 = 1AD,  0 = 1BC, -1 = 2BC, etc 
            year = internalGet(YEAR, 1)                       // pin to minimum of year 1 (first year)
                    + ERAS[internalGet(ERA, CURRENT_ERA) * 3] // add gregorian starting year
                    - 1;                                      // Subtract one because year starts at 1
        }
        return year;
    }
    
    /**
     * Called by handleComputeJulianDay.  Returns the default month (0-based) for the year,
     * taking year and era into account.  Defaults to 0 (JANUARY) for Gregorian.
     * @param extendedYear the extendedYear, as returned by handleGetExtendedYear
     * @return the default month
     * @see #MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    protected int getDefaultMonthInYear(int extendedYear)
    {
      int era = internalGet(ERA, CURRENT_ERA);
      //computeFields(status); // No need to compute fields here - expect the caller already did so.

      // Find out if we are at the edge of an era
      if(extendedYear == ERAS[era*3]) {
        return ERAS[(era*3)+1] // month..
            -1; // return 0-based month
      } else {
        return super.getDefaultMonthInYear(extendedYear);
      }
    }

    /**
     * Called by handleComputeJulianDay.  Returns the default day (1-based) for the month,
     * taking currently-set year and era into account.  Defaults to 1 for Gregorian.
     * @param extendedYear the extendedYear, as returned by handleGetExtendedYear
     * @param month the month, as returned by getDefaultMonthInYear
     * @return the default day of the month
     * @see #DAY_OF_MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    protected int getDefaultDayInMonth(int extendedYear, int month) {
      int era = internalGet(ERA, CURRENT_ERA);
          
      if(extendedYear == ERAS[era*3]) { // if it is year 1..
        if(month == ((ERAS[(era*3)+1])-1)) { // if it is the emperor's first month.. 
          return ERAS[(era*3)+2]; // return the D_O_M of acession
        }
      }

      return super.getDefaultDayInMonth(extendedYear, month);
    }

    /**
     */
    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int year = internalGet(EXTENDED_YEAR);

        int low = 0;

        // Short circuit for recent years.  Most modern computations will
        // occur in the current era and won't require the binary search.
        // Note that if the year is == the current era year, then we use
        // the binary search to handle the month/dom comparison.
        if (year > ERAS[ERAS.length - 3]) {
            low = CURRENT_ERA;
        } else {
            // Binary search
            int high = ERAS.length / 3;
        
            while (low < high - 1) {
                int i = (low + high) / 2;
                int diff = year - ERAS[i*3];

                // If years are the same, then compare the months, and if those
                // are the same, compare days of month.  In the ERAS array
                // months are 1-based for easier maintenance.
                if (diff == 0) {
                    diff = internalGet(MONTH) - (ERAS[i*3 + 1] - 1);
                    if (diff == 0) {
                        diff = internalGet(DAY_OF_MONTH) - ERAS[i*3 + 2];
                    }
                }
                if (diff >= 0) {
                    low = i;
                } else {
                    high = i;
                }
            }
        }

        // Now we've found the last era that starts before this date, so
        // adjust the year to count from the start of that era.  Note that
        // all dates before the first era will fall into the first era by
        // the algorithm.
        internalSet(ERA, low);
        internalSet(YEAR, year - ERAS[low*3] + 1);
    }

    private static final int[] ERAS = {
    //  Gregorian date of each emperor's ascension
    //  Years are AD, months are 1-based.
    //  Year  Month Day
         645,    6, 19,     // Taika
         650,    2, 15,     // Hakuchi
         672,    1,  1,     // Hakuho
         686,    7, 20,     // Shucho
         701,    3, 21,     // Taiho
         704,    5, 10,     // Keiun
         708,    1, 11,     // Wado
         715,    9,  2,     // Reiki
         717,   11, 17,     // Yoro
         724,    2,  4,     // Jinki
         729,    8,  5,     // Tempyo
         749,    4, 14,     // Tempyo-kampo
         749,    7,  2,     // Tempyo-shoho
         757,    8, 18,     // Tempyo-hoji
         765,    1,  7,     // Tempho-jingo
         767,    8, 16,     // Jingo-keiun
         770,   10,  1,     // Hoki
         781,    1,  1,     // Ten-o
         782,    8, 19,     // Enryaku
         806,    5, 18,     // Daido
         810,    9, 19,     // Konin
         824,    1,  5,     // Tencho
         834,    1,  3,     // Showa
         848,    6, 13,     // Kajo
         851,    4, 28,     // Ninju
         854,   11, 30,     // Saiko
         857,    2, 21,     // Tennan
         859,    4, 15,     // Jogan
         877,    4, 16,     // Genkei
         885,    2, 21,     // Ninna
         889,    4, 27,     // Kampyo
         898,    4, 26,     // Shotai
         901,    7, 15,     // Engi
         923,    4, 11,     // Encho
         931,    4, 26,     // Shohei
         938,    5, 22,     // Tengyo
         947,    4, 22,     // Tenryaku
         957,   10, 27,     // Tentoku
         961,    2, 16,     // Owa
         964,    7, 10,     // Koho
         968,    8, 13,     // Anna
         970,    3, 25,     // Tenroku
         973,   12, 20,     // Ten-en
         976,    7, 13,     // Jogen
         978,   11, 29,     // Tengen
         983,    4, 15,     // Eikan
         985,    4, 27,     // Kanna
         987,    4,  5,     // Ei-en
         989,    8,  8,     // Eiso
         990,   11,  7,     // Shoryaku
         995,    2, 22,     // Chotoku
         999,    1, 13,     // Choho
        1004,    7, 20,     // Kanko
        1012,   12, 25,     // Chowa
        1017,    4, 23,     // Kannin
        1021,    2,  2,     // Jian
        1024,    7, 13,     // Manju
        1028,    7, 25,     // Chogen
        1037,    4, 21,     // Choryaku
        1040,   11, 10,     // Chokyu
        1044,   11, 24,     // Kantoku
        1046,    4, 14,     // Eisho
        1053,    1, 11,     // Tengi
        1058,    8, 29,     // Kohei
        1065,    8,  2,     // Jiryaku
        1069,    4, 13,     // Enkyu
        1074,    8, 23,     // Shoho
        1077,   11, 17,     // Shoryaku
        1081,    2, 10,     // Eiho
        1084,    2,  7,     // Otoku
        1087,    4,  7,     // Kanji
        1094,   12, 15,     // Kaho
        1096,   12, 17,     // Eicho
        1097,   11, 21,     // Shotoku
        1099,    8, 28,     // Kowa
        1104,    2, 10,     // Choji
        1106,    4,  9,     // Kasho
        1108,    8,  3,     // Tennin
        1110,    7, 13,     // Ten-ei
        1113,    7, 13,     // Eikyu
        1118,    4,  3,     // Gen-ei
        1120,    4, 10,     // Hoan
        1124,    4,  3,     // Tenji
        1126,    1, 22,     // Daiji
        1131,    1, 29,     // Tensho
        1132,    8, 11,     // Chosho
        1135,    4, 27,     // Hoen
        1141,    7, 10,     // Eiji
        1142,    4, 28,     // Koji
        1144,    2, 23,     // Tenyo
        1145,    7, 22,     // Kyuan
        1151,    1, 26,     // Ninpei
        1154,   10, 28,     // Kyuju
        1156,    4, 27,     // Hogen
        1159,    4, 20,     // Heiji
        1160,    1, 10,     // Eiryaku
        1161,    9,  4,     // Oho
        1163,    3, 29,     // Chokan
        1165,    6,  5,     // Eiman
        1166,    8, 27,     // Nin-an
        1169,    4,  8,     // Kao
        1171,    4, 21,     // Shoan
        1175,    7, 28,     // Angen
        1177,    8,  4,     // Jisho
        1181,    7, 14,     // Yowa
        1182,    5, 27,     // Juei
        1184,    4, 16,     // Genryuku
        1185,    8, 14,     // Bunji
        1190,    4, 11,     // Kenkyu
        1199,    4, 27,     // Shoji
        1201,    2, 13,     // Kennin
        1204,    2, 20,     // Genkyu
        1206,    4, 27,     // Ken-ei
        1207,   10, 25,     // Shogen
        1211,    3,  9,     // Kenryaku
        1213,   12,  6,     // Kenpo
        1219,    4, 12,     // Shokyu
        1222,    4, 13,     // Joo
        1224,   11, 20,     // Gennin
        1225,    4, 20,     // Karoku
        1227,   12, 10,     // Antei
        1229,    3,  5,     // Kanki
        1232,    4,  2,     // Joei
        1233,    4, 15,     // Tempuku
        1234,   11,  5,     // Bunryaku
        1235,    9, 19,     // Katei
        1238,   11, 23,     // Ryakunin
        1239,    2,  7,     // En-o
        1240,    7, 16,     // Ninji
        1243,    2, 26,     // Kangen
        1247,    2, 28,     // Hoji
        1249,    3, 18,     // Kencho
        1256,   10,  5,     // Kogen
        1257,    3, 14,     // Shoka
        1259,    3, 26,     // Shogen
        1260,    4, 13,     // Bun-o
        1261,    2, 20,     // Kocho
        1264,    2, 28,     // Bun-ei
        1275,    4, 25,     // Kenji
        1278,    2, 29,     // Koan
        1288,    4, 28,     // Shoo
        1293,    8, 55,     // Einin
        1299,    4, 25,     // Shoan
        1302,   11, 21,     // Kengen
        1303,    8,  5,     // Kagen
        1306,   12, 14,     // Tokuji
        1308,   10,  9,     // Enkei
        1311,    4, 28,     // Ocho
        1312,    3, 20,     // Showa
        1317,    2,  3,     // Bunpo
        1319,    4, 28,     // Geno
        1321,    2, 23,     // Genkyo
        1324,   12,  9,     // Shochu
        1326,    4, 26,     // Kareki
        1329,    8, 29,     // Gentoku
        1331,    8,  9,     // Genko
        1334,    1, 29,     // Kemmu
        1336,    2, 29,     // Engen
        1340,    4, 28,     // Kokoku
        1346,   12,  8,     // Shohei
        1370,    7, 24,     // Kentoku
        1372,    4,  1,     // Bunch\u0169
        1375,    5, 27,     // Tenju
        1379,    3, 22,     // Koryaku
        1381,    2, 10,     // Kowa
        1384,    4, 28,     // Gench\u0169
        1384,    2, 27,     // Meitoku
        1387,    8, 23,     // Kakei
        1389,    2,  9,     // Koo
        1390,    3, 26,     // Meitoku
        1394,    7,  5,     // Oei
        1428,    4, 27,     // Shocho
        1429,    9,  5,     // Eikyo
        1441,    2, 17,     // Kakitsu
        1444,    2,  5,     // Bun-an
        1449,    7, 28,     // Hotoku
        1452,    7, 25,     // Kyotoku
        1455,    7, 25,     // Kosho
        1457,    9, 28,     // Choroku
        1460,   12, 21,     // Kansho
        1466,    2, 28,     // Bunsho
        1467,    3,  3,     // Onin
        1469,    4, 28,     // Bunmei
        1487,    7, 29,     // Chokyo
        1489,    8, 21,     // Entoku
        1492,    7, 19,     // Meio
        1501,    2, 29,     // Bunki
        1504,    2, 30,     // Eisho
        1521,    8, 23,     // Taiei
        1528,    8, 20,     // Kyoroku
        1532,    7, 29,     // Tenmon
        1555,   10, 23,     // Koji
        1558,    2, 28,     // Eiroku
        1570,    4, 23,     // Genki
        1573,    7, 28,     // Tensho
        1592,   12,  8,     // Bunroku
        1596,   10, 27,     // Keicho
        1615,    7, 13,     // Genwa
        1624,    2, 30,     // Kan-ei
        1644,   12, 16,     // Shoho
        1648,    2, 15,     // Keian
        1652,    9, 18,     // Shoo
        1655,    4, 13,     // Meiryaku
        1658,    7, 23,     // Manji
        1661,    4, 25,     // Kanbun
        1673,    9, 21,     // Enpo
        1681,    9, 29,     // Tenwa
        1684,    2, 21,     // Jokyo
        1688,    9, 30,     // Genroku
        1704,    3, 13,     // Hoei
        1711,    4, 25,     // Shotoku
        1716,    6, 22,     // Kyoho
        1736,    4, 28,     // Genbun
        1741,    2, 27,     // Kanpo
        1744,    2, 21,     // Enkyo
        1748,    7, 12,     // Kan-en
        1751,   10, 27,     // Horyaku
        1764,    6,  2,     // Meiwa
        1772,   11, 16,     // An-ei
        1781,    4,  2,     // Tenmei
        1789,    1, 25,     // Kansei
        1801,    2,  5,     // Kyowa
        1804,    2, 11,     // Bunka
        1818,    4, 22,     // Bunsei
        1830,   12, 10,     // Tenpo
        1844,   12,  2,     // Koka
        1848,    2, 28,     // Kaei
        1854,   11, 27,     // Ansei
        1860,    3, 18,     // Man-en
        1861,    2, 19,     // Bunkyu
        1864,    2, 20,     // Genji
        1865,    4,  7,     // Keio
        1868,    9,  8,     // Meiji
        1912,    7, 30,     // Taisho
        1926,   12, 25,     // Showa
        1989,    1,  8,     // Heisei
    };

    //-------------------------------------------------------------------------
    // Public constants for some of the recent eras that folks might use...
    //-------------------------------------------------------------------------

    // Constant for the current era.  This must be regularly updated.
    /**
     */
    static public final int CURRENT_ERA = (ERAS.length / 3) - 1;
    
    /** 
     * Constant for the era starting on Sept. 8, 1868 AD.
     */
    static public final int MEIJI = CURRENT_ERA - 3;

    /** 
     * Constant for the era starting on July 30, 1912 AD. 
     */
    static public final int TAISHO = CURRENT_ERA - 2;
    
    /** 
     * Constant for the era starting on Dec. 25, 1926 AD. 
     */
    static public final int SHOWA = CURRENT_ERA - 1;

    /** 
     * Constant for the era starting on Jan. 7, 1989 AD. 
     */
    static public final int HEISEI = CURRENT_ERA;

    /**
     * Override GregorianCalendar.  We should really handle YEAR_WOY and
     * EXTENDED_YEAR here too to implement the 1..5000000 range, but it's
     * not critical.
     */
    @SuppressWarnings("fallthrough")
    protected int handleGetLimit(int field, int limitType) {
        switch (field) {
        case ERA:
            if (limitType == MINIMUM || limitType == GREATEST_MINIMUM) {
                return 0;
            }
            return CURRENT_ERA;
        case YEAR:
        {
            switch (limitType) {
            case MINIMUM:
            case GREATEST_MINIMUM:
                return 1;
            case LEAST_MAXIMUM:
                return 1;
            case MAXIMUM:
                return super.handleGetLimit(field, MAXIMUM) - ERAS[CURRENT_ERA*3];
            }
            //Fall through to the default if not handled above
        }
        default:
            return super.handleGetLimit(field, limitType);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "japanese";
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getActualMaximum(int field) {
        if (field == YEAR) {
            int era = get(Calendar.ERA);
            if (era == CURRENT_ERA) {
                // TODO: Investigate what value should be used here - revisit after 4.0.
                return handleGetLimit(YEAR, MAXIMUM);
            } else {
                int nextEraYear = ERAS[(era+1)*3];
                int nextEraMonth = ERAS[(era+1)*3 + 1];
                int nextEraDate = ERAS[(era+1)*3 + 2];

                int maxYear = nextEraYear - ERAS[era*3] + 1; // 1-base
                if (nextEraMonth == 1 && nextEraDate == 1) {
                    // Substract 1, because the next era starts at Jan 1
                    maxYear--;
                }
                return maxYear;
            }
        }
        return super.getActualMaximum(field);
    }

}
