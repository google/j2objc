/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.impl;

import java.util.Date;
import java.util.TimeZone;

/**
 * <code>CalendarAstronomer</code> is a class that can perform the calculations to
 * determine the positions of the sun and moon, the time of sunrise and
 * sunset, and other astronomy-related data.  The calculations it performs
 * are in some cases quite complicated, and this utility class saves you
 * the trouble of worrying about them.
 * <p>
 * The measurement of time is a very important part of astronomy.  Because
 * astronomical bodies are constantly in motion, observations are only valid
 * at a given moment in time.  Accordingly, each <code>CalendarAstronomer</code>
 * object has a <code>time</code> property that determines the date
 * and time for which its calculations are performed.  You can set and
 * retrieve this property with {@link #setDate setDate}, {@link #getDate getDate}
 * and related methods.
 * <p>
 * Almost all of the calculations performed by this class, or by any
 * astronomer, are approximations to various degrees of accuracy.  The
 * calculations in this class are mostly modelled after those described
 * in the book
 * <a href="http://www.amazon.com/exec/obidos/ISBN=0521356997" target="_top">
 * Practical Astronomy With Your Calculator</a>, by Peter J.
 * Duffett-Smith, Cambridge University Press, 1990.  This is an excellent
 * book, and if you want a greater understanding of how these calculations
 * are performed it a very good, readable starting point.
 * <p>
 * <strong>WARNING:</strong> This class is very early in its development, and
 * it is highly likely that its API will change to some degree in the future.
 * At the moment, it basically does just enough to support {@link android.icu.util.IslamicCalendar}
 * and {@link android.icu.util.ChineseCalendar}.
 *
 * @author Laura Werner
 * @author Alan Liu
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class CalendarAstronomer {

    //-------------------------------------------------------------------------
    // Astronomical constants
    //-------------------------------------------------------------------------

    /**
     * The number of standard hours in one sidereal day.
     * Approximately 24.93.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double SIDEREAL_DAY = 23.93446960027;

    /**
     * The number of sidereal hours in one mean solar day.
     * Approximately 24.07.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double SOLAR_DAY =  24.065709816;

    /**
     * The average number of solar days from one new moon to the next.  This is the time
     * it takes for the moon to return the same ecliptic longitude as the sun.
     * It is longer than the sidereal month because the sun's longitude increases
     * during the year due to the revolution of the earth around the sun.
     * Approximately 29.53.
     *
     * @see #SIDEREAL_MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double SYNODIC_MONTH = 29.530588853;

    /**
     * The average number of days it takes
     * for the moon to return to the same ecliptic longitude relative to the
     * stellar background.  This is referred to as the sidereal month.
     * It is shorter than the synodic month due to
     * the revolution of the earth around the sun.
     * Approximately 27.32.
     *
     * @see #SYNODIC_MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double SIDEREAL_MONTH = 27.32166;

    /**
     * The average number number of days between successive vernal equinoxes.
     * Due to the precession of the earth's
     * axis, this is not precisely the same as the sidereal year.
     * Approximately 365.24
     *
     * @see #SIDEREAL_YEAR
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double TROPICAL_YEAR = 365.242191;

    /**
     * The average number of days it takes
     * for the sun to return to the same position against the fixed stellar
     * background.  This is the duration of one orbit of the earth about the sun
     * as it would appear to an outside observer.
     * Due to the precession of the earth's
     * axis, this is not precisely the same as the tropical year.
     * Approximately 365.25.
     *
     * @see #TROPICAL_YEAR
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final double SIDEREAL_YEAR = 365.25636;

    //-------------------------------------------------------------------------
    // Time-related constants
    //-------------------------------------------------------------------------

    /**
     * The number of milliseconds in one second.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int  SECOND_MS = 1000;

    /**
     * The number of milliseconds in one minute.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int  MINUTE_MS = 60*SECOND_MS;

    /**
     * The number of milliseconds in one hour.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int  HOUR_MS   = 60*MINUTE_MS;

    /**
     * The number of milliseconds in one day.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final long DAY_MS    = 24*HOUR_MS;

    /**
     * The start of the julian day numbering scheme used by astronomers, which
     * is 1/1/4713 BC (Julian), 12:00 GMT.  This is given as the number of milliseconds
     * since 1/1/1970 AD (Gregorian), a negative number.
     * Note that julian day numbers and
     * the Julian calendar are <em>not</em> the same thing.  Also note that
     * julian days start at <em>noon</em>, not midnight.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final long JULIAN_EPOCH_MS = -210866760000000L;

//  static {
//      Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
//      cal.clear();
//      cal.set(cal.ERA, 0);
//      cal.set(cal.YEAR, 4713);
//      cal.set(cal.MONTH, cal.JANUARY);
//      cal.set(cal.DATE, 1);
//      cal.set(cal.HOUR_OF_DAY, 12);
//      System.out.println("1.5 Jan 4713 BC = " + cal.getTime().getTime());

//      cal.clear();
//      cal.set(cal.YEAR, 2000);
//      cal.set(cal.MONTH, cal.JANUARY);
//      cal.set(cal.DATE, 1);
//      cal.add(cal.DATE, -1);
//      System.out.println("0.0 Jan 2000 = " + cal.getTime().getTime());
//  }

    /**
     * Milliseconds value for 0.0 January 2000 AD.
     */
    static final long EPOCH_2000_MS = 946598400000L;

    //-------------------------------------------------------------------------
    // Assorted private data used for conversions
    //-------------------------------------------------------------------------

    // My own copies of these so compilers are more likely to optimize them away
    static private final double PI = 3.14159265358979323846;
    static private final double PI2 = PI * 2.0;

    static private final double RAD_HOUR = 12 / PI;        // radians -> hours
    static private final double DEG_RAD  = PI / 180;        // degrees -> radians
    static private final double RAD_DEG  = 180 / PI;        // radians -> degrees

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a new <code>CalendarAstronomer</code> object that is initialized to
     * the current date and time.
     * @hide draft / provisional / internal are hidden on Android
     */
    public CalendarAstronomer() {
        this(System.currentTimeMillis());
    }

    /**
     * Construct a new <code>CalendarAstronomer</code> object that is initialized to
     * the specified date and time.
     * @hide draft / provisional / internal are hidden on Android
     */
    public CalendarAstronomer(Date d) {
        this(d.getTime());
    }

    /**
     * Construct a new <code>CalendarAstronomer</code> object that is initialized to
     * the specified time.  The time is expressed as a number of milliseconds since
     * January 1, 1970 AD (Gregorian).
     *
     * @see java.util.Date#getTime()
     * @hide draft / provisional / internal are hidden on Android
     */
    public CalendarAstronomer(long aTime) {
        time = aTime;
    }

    /**
     * Construct a new <code>CalendarAstronomer</code> object with the given
     * latitude and longitude.  The object's time is set to the current
     * date and time.
     * <p>
     * @param longitude The desired longitude, in <em>degrees</em> east of
     *                  the Greenwich meridian.
     *
     * @param latitude  The desired latitude, in <em>degrees</em>.  Positive
     *                  values signify North, negative South.
     *
     * @see java.util.Date#getTime()
     * @hide draft / provisional / internal are hidden on Android
     */
    public CalendarAstronomer(double longitude, double latitude) {
        this();
        fLongitude = normPI(longitude * DEG_RAD);
        fLatitude  = normPI(latitude  * DEG_RAD);
        fGmtOffset = (long)(fLongitude * 24 * HOUR_MS / PI2);
    }


    //-------------------------------------------------------------------------
    // Time and date getters and setters
    //-------------------------------------------------------------------------

    /**
     * Set the current date and time of this <code>CalendarAstronomer</code> object.  All
     * astronomical calculations are performed based on this time setting.
     *
     * @param aTime the date and time, expressed as the number of milliseconds since
     *              1/1/1970 0:00 GMT (Gregorian).
     *
     * @see #setDate
     * @see #getTime
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setTime(long aTime) {
        time = aTime;
        clearCache();
    }

    /**
     * Set the current date and time of this <code>CalendarAstronomer</code> object.  All
     * astronomical calculations are performed based on this time setting.
     *
     * @param date the time and date, expressed as a <code>Date</code> object.
     *
     * @see #setTime
     * @see #getDate
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setDate(Date date) {
        setTime(date.getTime());
    }

    /**
     * Set the current date and time of this <code>CalendarAstronomer</code> object.  All
     * astronomical calculations are performed based on this time setting.
     *
     * @param jdn   the desired time, expressed as a "julian day number",
     *              which is the number of elapsed days since
     *              1/1/4713 BC (Julian), 12:00 GMT.  Note that julian day
     *              numbers start at <em>noon</em>.  To get the jdn for
     *              the corresponding midnight, subtract 0.5.
     *
     * @see #getJulianDay
     * @see #JULIAN_EPOCH_MS
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setJulianDay(double jdn) {
        time = (long)(jdn * DAY_MS) + JULIAN_EPOCH_MS;
        clearCache();
        julianDay = jdn;
    }

    /**
     * Get the current time of this <code>CalendarAstronomer</code> object,
     * represented as the number of milliseconds since
     * 1/1/1970 AD 0:00 GMT (Gregorian).
     *
     * @see #setTime
     * @see #getDate
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getTime() {
        return time;
    }

    /**
     * Get the current time of this <code>CalendarAstronomer</code> object,
     * represented as a <code>Date</code> object.
     *
     * @see #setDate
     * @see #getTime
     * @hide draft / provisional / internal are hidden on Android
     */
    public Date getDate() {
        return new Date(time);
    }

    /**
     * Get the current time of this <code>CalendarAstronomer</code> object,
     * expressed as a "julian day number", which is the number of elapsed
     * days since 1/1/4713 BC (Julian), 12:00 GMT.
     *
     * @see #setJulianDay
     * @see #JULIAN_EPOCH_MS
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getJulianDay() {
        if (julianDay == INVALID) {
            julianDay = (double)(time - JULIAN_EPOCH_MS) / (double)DAY_MS;
        }
        return julianDay;
    }

    /**
     * Return this object's time expressed in julian centuries:
     * the number of centuries after 1/1/1900 AD, 12:00 GMT
     *
     * @see #getJulianDay
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getJulianCentury() {
        if (julianCentury == INVALID) {
            julianCentury = (getJulianDay() - 2415020.0) / 36525;
        }
        return julianCentury;
    }

    /**
     * Returns the current Greenwich sidereal time, measured in hours
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getGreenwichSidereal() {
        if (siderealTime == INVALID) {
            // See page 86 of "Practial Astronomy with your Calculator",
            // by Peter Duffet-Smith, for details on the algorithm.

            double UT = normalize((double)time/HOUR_MS, 24);

            siderealTime = normalize(getSiderealOffset() + UT*1.002737909, 24);
        }
        return siderealTime;
    }

    private double getSiderealOffset() {
        if (siderealT0 == INVALID) {
            double JD  = Math.floor(getJulianDay() - 0.5) + 0.5;
            double S   = JD - 2451545.0;
            double T   = S / 36525.0;
            siderealT0 = normalize(6.697374558 + 2400.051336*T + 0.000025862*T*T, 24);
        }
        return siderealT0;
    }

    /**
     * Returns the current local sidereal time, measured in hours
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getLocalSidereal() {
        return normalize(getGreenwichSidereal() + (double)fGmtOffset/HOUR_MS, 24);
    }

    /**
     * Converts local sidereal time to Universal Time.
     *
     * @param lst   The Local Sidereal Time, in hours since sidereal midnight
     *              on this object's current date.
     *
     * @return      The corresponding Universal Time, in milliseconds since
     *              1 Jan 1970, GMT.
     */
    private long lstToUT(double lst) {
        // Convert to local mean time
        double lt = normalize((lst - getSiderealOffset()) * 0.9972695663, 24);

        // Then find local midnight on this day
        long base = DAY_MS * ((time + fGmtOffset)/DAY_MS) - fGmtOffset;

        //out("    lt  =" + lt + " hours");
        //out("    base=" + new Date(base));

        return base + (long)(lt * HOUR_MS);
    }


    //-------------------------------------------------------------------------
    // Coordinate transformations, all based on the current time of this object
    //-------------------------------------------------------------------------

    /**
     * Convert from ecliptic to equatorial coordinates.
     *
     * @param ecliptic  A point in the sky in ecliptic coordinates.
     * @return          The corresponding point in equatorial coordinates.
     * @hide draft / provisional / internal are hidden on Android
     */
    public final Equatorial eclipticToEquatorial(Ecliptic ecliptic)
    {
        return eclipticToEquatorial(ecliptic.longitude, ecliptic.latitude);
    }

    /**
     * Convert from ecliptic to equatorial coordinates.
     *
     * @param eclipLong     The ecliptic longitude
     * @param eclipLat      The ecliptic latitude
     *
     * @return              The corresponding point in equatorial coordinates.
     * @hide draft / provisional / internal are hidden on Android
     */
    public final Equatorial eclipticToEquatorial(double eclipLong, double eclipLat)
    {
        // See page 42 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.

        double obliq = eclipticObliquity();
        double sinE = Math.sin(obliq);
        double cosE = Math.cos(obliq);

        double sinL = Math.sin(eclipLong);
        double cosL = Math.cos(eclipLong);

        double sinB = Math.sin(eclipLat);
        double cosB = Math.cos(eclipLat);
        double tanB = Math.tan(eclipLat);

        return new Equatorial(Math.atan2(sinL*cosE - tanB*sinE, cosL),
                               Math.asin(sinB*cosE + cosB*sinE*sinL) );
    }

    /**
     * Convert from ecliptic longitude to equatorial coordinates.
     *
     * @param eclipLong     The ecliptic longitude
     *
     * @return              The corresponding point in equatorial coordinates.
     * @hide draft / provisional / internal are hidden on Android
     */
    public final Equatorial eclipticToEquatorial(double eclipLong)
    {
        return eclipticToEquatorial(eclipLong, 0);  // TODO: optimize
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public Horizon eclipticToHorizon(double eclipLong)
    {
        Equatorial equatorial = eclipticToEquatorial(eclipLong);

        double H = getLocalSidereal()*PI/12 - equatorial.ascension;     // Hour-angle

        double sinH = Math.sin(H);
        double cosH = Math.cos(H);
        double sinD = Math.sin(equatorial.declination);
        double cosD = Math.cos(equatorial.declination);
        double sinL = Math.sin(fLatitude);
        double cosL = Math.cos(fLatitude);

        double altitude = Math.asin(sinD*sinL + cosD*cosL*cosH);
        double azimuth  = Math.atan2(-cosD*cosL*sinH, sinD - sinL * Math.sin(altitude));

        return new Horizon(azimuth, altitude);
    }


    //-------------------------------------------------------------------------
    // The Sun
    //-------------------------------------------------------------------------

    //
    // Parameters of the Sun's orbit as of the epoch Jan 0.0 1990
    // Angles are in radians (after multiplying by PI/180)
    //
    static final double JD_EPOCH = 2447891.5; // Julian day of epoch

    static final double SUN_ETA_G   = 279.403303 * PI/180; // Ecliptic longitude at epoch
    static final double SUN_OMEGA_G = 282.768422 * PI/180; // Ecliptic longitude of perigee
    static final double SUN_E      =   0.016713;          // Eccentricity of orbit
    //double sunR0     =   1.495585e8;        // Semi-major axis in KM
    //double sunTheta0 =   0.533128 * PI/180; // Angular diameter at R0

    // The following three methods, which compute the sun parameters
    // given above for an arbitrary epoch (whatever time the object is
    // set to), make only a small difference as compared to using the
    // above constants.  E.g., Sunset times might differ by ~12
    // seconds.  Furthermore, the eta-g computation is befuddled by
    // Duffet-Smith's incorrect coefficients (p.86).  I've corrected
    // the first-order coefficient but the others may be off too - no
    // way of knowing without consulting another source.

//  /**
//   * Return the sun's ecliptic longitude at perigee for the current time.
//   * See Duffett-Smith, p. 86.
//   * @return radians
//   */
//  private double getSunOmegaG() {
//      double T = getJulianCentury();
//      return (281.2208444 + (1.719175 + 0.000452778*T)*T) * DEG_RAD;
//  }

//  /**
//   * Return the sun's ecliptic longitude for the current time.
//   * See Duffett-Smith, p. 86.
//   * @return radians
//   */
//  private double getSunEtaG() {
//      double T = getJulianCentury();
//      //return (279.6966778 + (36000.76892 + 0.0003025*T)*T) * DEG_RAD;
//      //
//      // The above line is from Duffett-Smith, and yields manifestly wrong
//      // results.  The below constant is derived empirically to match the
//      // constant he gives for the 1990 EPOCH.
//      //
//      return (279.6966778 + (-0.3262541582718024 + 0.0003025*T)*T) * DEG_RAD;
//  }

//  /**
//   * Return the sun's eccentricity of orbit for the current time.
//   * See Duffett-Smith, p. 86.
//   * @return double
//   */
//  private double getSunE() {
//      double T = getJulianCentury();
//      return 0.01675104 - (0.0000418 + 0.000000126*T)*T;
//  }

    /**
     * The longitude of the sun at the time specified by this object.
     * The longitude is measured in radians along the ecliptic
     * from the "first point of Aries," the point at which the ecliptic
     * crosses the earth's equatorial plane at the vernal equinox.
     * <p>
     * Currently, this method uses an approximation of the two-body Kepler's
     * equation for the earth and the sun.  It does not take into account the
     * perturbations caused by the other planets, the moon, etc.
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getSunLongitude()
    {
        // See page 86 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.

        if (sunLongitude == INVALID) {
            double[] result = getSunLongitude(getJulianDay());
            sunLongitude = result[0];
            meanAnomalySun = result[1];
        }
        return sunLongitude;
    }

    /**
     * TODO Make this public when the entire class is package-private.
     */
    /*public*/ double[] getSunLongitude(double julian)
    {
        // See page 86 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.

        double day = julian - JD_EPOCH;       // Days since epoch

        // Find the angular distance the sun in a fictitious
        // circular orbit has travelled since the epoch.
        double epochAngle = norm2PI(PI2/TROPICAL_YEAR*day);

        // The epoch wasn't at the sun's perigee; find the angular distance
        // since perigee, which is called the "mean anomaly"
        double meanAnomaly = norm2PI(epochAngle + SUN_ETA_G - SUN_OMEGA_G);

        // Now find the "true anomaly", e.g. the real solar longitude
        // by solving Kepler's equation for an elliptical orbit
        // NOTE: The 3rd ed. of the book lists omega_g and eta_g in different
        // equations; omega_g is to be correct.
        return new double[] {
            norm2PI(trueAnomaly(meanAnomaly, SUN_E) + SUN_OMEGA_G),
            meanAnomaly
        };
    }

    /**
     * The position of the sun at this object's current date and time,
     * in equatorial coordinates.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Equatorial getSunPosition() {
        return eclipticToEquatorial(getSunLongitude(), 0);
    }

    private static class SolarLongitude {
        double value;
        SolarLongitude(double val) { value = val; }
    }

    /**
     * Constant representing the vernal equinox.
     * For use with {@link #getSunTime(SolarLongitude, boolean) getSunTime}.
     * Note: In this case, "vernal" refers to the northern hemisphere's seasons.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final SolarLongitude VERNAL_EQUINOX  = new SolarLongitude(0);

    /**
     * Constant representing the summer solstice.
     * For use with {@link #getSunTime(SolarLongitude, boolean) getSunTime}.
     * Note: In this case, "summer" refers to the northern hemisphere's seasons.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final SolarLongitude SUMMER_SOLSTICE = new SolarLongitude(PI/2);

    /**
     * Constant representing the autumnal equinox.
     * For use with {@link #getSunTime(SolarLongitude, boolean) getSunTime}.
     * Note: In this case, "autumn" refers to the northern hemisphere's seasons.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final SolarLongitude AUTUMN_EQUINOX  = new SolarLongitude(PI);

    /**
     * Constant representing the winter solstice.
     * For use with {@link #getSunTime(SolarLongitude, boolean) getSunTime}.
     * Note: In this case, "winter" refers to the northern hemisphere's seasons.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final SolarLongitude WINTER_SOLSTICE = new SolarLongitude((PI*3)/2);

    /**
     * Find the next time at which the sun's ecliptic longitude will have
     * the desired value.
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getSunTime(double desired, boolean next)
    {
        return timeOfAngle( new AngleFunc() { @Override
        public double eval() { return getSunLongitude(); } },
                            desired,
                            TROPICAL_YEAR,
                            MINUTE_MS,
                            next);
    }

    /**
     * Find the next time at which the sun's ecliptic longitude will have
     * the desired value.
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getSunTime(SolarLongitude desired, boolean next) {
        return getSunTime(desired.value, next);
    }

    /**
     * Returns the time (GMT) of sunrise or sunset on the local date to which
     * this calendar is currently set.
     *
     * NOTE: This method only works well if this object is set to a
     * time near local noon.  Because of variations between the local
     * official time zone and the geographic longitude, the
     * computation can flop over into an adjacent day if this object
     * is set to a time near local midnight.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getSunRiseSet(boolean rise) {
        long t0 = time;

        // Make a rough guess: 6am or 6pm local time on the current day
        long noon = ((time + fGmtOffset)/DAY_MS)*DAY_MS - fGmtOffset + 12*HOUR_MS;

        setTime(noon + (rise ? -6L : 6L) * HOUR_MS);

        long t = riseOrSet(new CoordFunc() {
            @Override
            public Equatorial eval() { return getSunPosition(); }
            },
                rise,
                .533 * DEG_RAD,        // Angular Diameter
                34 /60.0 * DEG_RAD,    // Refraction correction
                MINUTE_MS / 12);       // Desired accuracy

            setTime(t0);
            return t;
        }

// Commented out - currently unused. ICU 2.6, Alan
//    //-------------------------------------------------------------------------
//    // Alternate Sun Rise/Set
//    // See Duffett-Smith p.93
//    //-------------------------------------------------------------------------
//
//    // This yields worse results (as compared to USNO data) than getSunRiseSet().
//    /**
//     * TODO Make this public when the entire class is package-private.
//     */
//    /*public*/ long getSunRiseSet2(boolean rise) {
//        // 1. Calculate coordinates of the sun's center for midnight
//        double jd = Math.floor(getJulianDay() - 0.5) + 0.5;
//        double[] sl = getSunLongitude(jd);
//        double lambda1 = sl[0];
//        Equatorial pos1 = eclipticToEquatorial(lambda1, 0);
//
//        // 2. Add ... to lambda to get position 24 hours later
//        double lambda2 = lambda1 + 0.985647*DEG_RAD;
//        Equatorial pos2 = eclipticToEquatorial(lambda2, 0);
//
//        // 3. Calculate LSTs of rising and setting for these two positions
//        double tanL = Math.tan(fLatitude);
//        double H = Math.acos(-tanL * Math.tan(pos1.declination));
//        double lst1r = (PI2 + pos1.ascension - H) * 24 / PI2;
//        double lst1s = (pos1.ascension + H) * 24 / PI2;
//               H = Math.acos(-tanL * Math.tan(pos2.declination));
//        double lst2r = (PI2-H + pos2.ascension ) * 24 / PI2;
//        double lst2s = (H + pos2.ascension ) * 24 / PI2;
//        if (lst1r > 24) lst1r -= 24;
//        if (lst1s > 24) lst1s -= 24;
//        if (lst2r > 24) lst2r -= 24;
//        if (lst2s > 24) lst2s -= 24;
//
//        // 4. Convert LSTs to GSTs.  If GST1 > GST2, add 24 to GST2.
//        double gst1r = lstToGst(lst1r);
//        double gst1s = lstToGst(lst1s);
//        double gst2r = lstToGst(lst2r);
//        double gst2s = lstToGst(lst2s);
//        if (gst1r > gst2r) gst2r += 24;
//        if (gst1s > gst2s) gst2s += 24;
//
//        // 5. Calculate GST at 0h UT of this date
//        double t00 = utToGst(0);
//
//        // 6. Calculate GST at 0h on the observer's longitude
//        double offset = Math.round(fLongitude*12/PI); // p.95 step 6; he _rounds_ to nearest 15 deg.
//        double t00p = t00 - offset*1.002737909;
//        if (t00p < 0) t00p += 24; // do NOT normalize
//
//        // 7. Adjust
//        if (gst1r < t00p) {
//            gst1r += 24;
//            gst2r += 24;
//        }
//        if (gst1s < t00p) {
//            gst1s += 24;
//            gst2s += 24;
//        }
//
//        // 8.
//        double gstr = (24.07*gst1r-t00*(gst2r-gst1r))/(24.07+gst1r-gst2r);
//        double gsts = (24.07*gst1s-t00*(gst2s-gst1s))/(24.07+gst1s-gst2s);
//
//        // 9. Correct for parallax, refraction, and sun's diameter
//        double dec = (pos1.declination + pos2.declination) / 2;
//        double psi = Math.acos(Math.sin(fLatitude) / Math.cos(dec));
//        double x = 0.830725 * DEG_RAD; // parallax+refraction+diameter
//        double y = Math.asin(Math.sin(x) / Math.sin(psi)) * RAD_DEG;
//        double delta_t = 240 * y / Math.cos(dec) / 3600; // hours
//
//        // 10. Add correction to GSTs, subtract from GSTr
//        gstr -= delta_t;
//        gsts += delta_t;
//
//        // 11. Convert GST to UT and then to local civil time
//        double ut = gstToUt(rise ? gstr : gsts);
//        //System.out.println((rise?"rise=":"set=") + ut + ", delta_t=" + delta_t);
//        long midnight = DAY_MS * (time / DAY_MS); // Find UT midnight on this day
//        return midnight + (long) (ut * 3600000);
//    }

// Commented out - currently unused. ICU 2.6, Alan
//    /**
//     * Convert local sidereal time to Greenwich sidereal time.
//     * Section 15.  Duffett-Smith p.21
//     * @param lst in hours (0..24)
//     * @return GST in hours (0..24)
//     */
//    double lstToGst(double lst) {
//        double delta = fLongitude * 24 / PI2;
//        return normalize(lst - delta, 24);
//    }

// Commented out - currently unused. ICU 2.6, Alan
//    /**
//     * Convert UT to GST on this date.
//     * Section 12.  Duffett-Smith p.17
//     * @param ut in hours
//     * @return GST in hours
//     */
//    double utToGst(double ut) {
//        return normalize(getT0() + ut*1.002737909, 24);
//    }

// Commented out - currently unused. ICU 2.6, Alan
//    /**
//     * Convert GST to UT on this date.
//     * Section 13.  Duffett-Smith p.18
//     * @param gst in hours
//     * @return UT in hours
//     */
//    double gstToUt(double gst) {
//        return normalize(gst - getT0(), 24) * 0.9972695663;
//    }

// Commented out - currently unused. ICU 2.6, Alan
//    double getT0() {
//        // Common computation for UT <=> GST
//
//        // Find JD for 0h UT
//        double jd = Math.floor(getJulianDay() - 0.5) + 0.5;
//
//        double s = jd - 2451545.0;
//        double t = s / 36525.0;
//        double t0 = 6.697374558 + (2400.051336 + 0.000025862*t)*t;
//        return t0;
//    }

// Commented out - currently unused. ICU 2.6, Alan
//    //-------------------------------------------------------------------------
//    // Alternate Sun Rise/Set
//    // See sci.astro FAQ
//    // http://www.faqs.org/faqs/astronomy/faq/part3/section-5.html
//    //-------------------------------------------------------------------------
//
//    // Note: This method appears to produce inferior accuracy as
//    // compared to getSunRiseSet().
//
//    /**
//     * TODO Make this public when the entire class is package-private.
//     */
//    /*public*/ long getSunRiseSet3(boolean rise) {
//
//        // Compute day number for 0.0 Jan 2000 epoch
//        double d = (double)(time - EPOCH_2000_MS) / DAY_MS;
//
//        // Now compute the Local Sidereal Time, LST:
//        //
//        double LST  =  98.9818  +  0.985647352 * d  +  /*UT*15  +  long*/
//            fLongitude*RAD_DEG;
//        //
//        // (east long. positive).  Note that LST is here expressed in degrees,
//        // where 15 degrees corresponds to one hour.  Since LST really is an angle,
//        // it's convenient to use one unit---degrees---throughout.
//
//        //     COMPUTING THE SUN'S POSITION
//        //     ----------------------------
//        //
//        // To be able to compute the Sun's rise/set times, you need to be able to
//        // compute the Sun's position at any time.  First compute the "day
//        // number" d as outlined above, for the desired moment.  Next compute:
//        //
//        double oblecl = 23.4393 - 3.563E-7 * d;
//        //
//        double w  =  282.9404  +  4.70935E-5   * d;
//        double M  =  356.0470  +  0.9856002585 * d;
//        double e  =  0.016709  -  1.151E-9     * d;
//        //
//        // This is the obliquity of the ecliptic, plus some of the elements of
//        // the Sun's apparent orbit (i.e., really the Earth's orbit): w =
//        // argument of perihelion, M = mean anomaly, e = eccentricity.
//        // Semi-major axis is here assumed to be exactly 1.0 (while not strictly
//        // true, this is still an accurate approximation).  Next compute E, the
//        // eccentric anomaly:
//        //
//        double E = M + e*(180/PI) * Math.sin(M*DEG_RAD) * ( 1.0 + e*Math.cos(M*DEG_RAD) );
//        //
//        // where E and M are in degrees.  This is it---no further iterations are
//        // needed because we know e has a sufficiently small value.  Next compute
//        // the true anomaly, v, and the distance, r:
//        //
//        /*      r * cos(v)  =  */ double A  =  Math.cos(E*DEG_RAD) - e;
//        /*      r * sin(v)  =  */ double B  =  Math.sqrt(1 - e*e) * Math.sin(E*DEG_RAD);
//        //
//        // and
//        //
//        //      r  =  sqrt( A*A + B*B )
//        double v  =  Math.atan2( B, A )*RAD_DEG;
//        //
//        // The Sun's true longitude, slon, can now be computed:
//        //
//        double slon  =  v + w;
//        //
//        // Since the Sun is always at the ecliptic (or at least very very close to
//        // it), we can use simplified formulae to convert slon (the Sun's ecliptic
//        // longitude) to sRA and sDec (the Sun's RA and Dec):
//        //
//        //                   sin(slon) * cos(oblecl)
//        //     tan(sRA)  =  -------------------------
//        //             cos(slon)
//        //
//        //     sin(sDec) =  sin(oblecl) * sin(slon)
//        //
//        // As was the case when computing az, the Azimuth, if possible use an
//        // atan2() function to compute sRA.
//
//        double sRA = Math.atan2(Math.sin(slon*DEG_RAD) * Math.cos(oblecl*DEG_RAD), Math.cos(slon*DEG_RAD))*RAD_DEG;
//
//        double sin_sDec = Math.sin(oblecl*DEG_RAD) * Math.sin(slon*DEG_RAD);
//        double sDec = Math.asin(sin_sDec)*RAD_DEG;
//
//        //     COMPUTING RISE AND SET TIMES
//        //     ----------------------------
//        //
//        // To compute when an object rises or sets, you must compute when it
//        // passes the meridian and the HA of rise/set.  Then the rise time is
//        // the meridian time minus HA for rise/set, and the set time is the
//        // meridian time plus the HA for rise/set.
//        //
//        // To find the meridian time, compute the Local Sidereal Time at 0h local
//        // time (or 0h UT if you prefer to work in UT) as outlined above---name
//        // that quantity LST0.  The Meridian Time, MT, will now be:
//        //
//        //     MT  =  RA - LST0
//        double MT = normalize(sRA - LST, 360);
//        //
//        // where "RA" is the object's Right Ascension (in degrees!).  If negative,
//        // add 360 deg to MT.  If the object is the Sun, leave the time as it is,
//        // but if it's stellar, multiply MT by 365.2422/366.2422, to convert from
//        // sidereal to solar time.  Now, compute HA for rise/set, name that
//        // quantity HA0:
//        //
//        //                 sin(h0)  -  sin(lat) * sin(Dec)
//        // cos(HA0)  =  ---------------------------------
//        //                      cos(lat) * cos(Dec)
//        //
//        // where h0 is the altitude selected to represent rise/set.  For a purely
//        // mathematical horizon, set h0 = 0 and simplify to:
//        //
//        //     cos(HA0)  =  - tan(lat) * tan(Dec)
//        //
//        // If you want to account for refraction on the atmosphere, set h0 = -35/60
//        // degrees (-35 arc minutes), and if you want to compute the rise/set times
//        // for the Sun's upper limb, set h0 = -50/60 (-50 arc minutes).
//        //
//        double h0 = -50/60 * DEG_RAD;
//
//        double HA0 = Math.acos(
//          (Math.sin(h0) - Math.sin(fLatitude) * sin_sDec) /
//          (Math.cos(fLatitude) * Math.cos(sDec*DEG_RAD)))*RAD_DEG;
//
//        // When HA0 has been computed, leave it as it is for the Sun but multiply
//        // by 365.2422/366.2422 for stellar objects, to convert from sidereal to
//        // solar time.  Finally compute:
//        //
//        //    Rise time  =  MT - HA0
//        //    Set  time  =  MT + HA0
//        //
//        // convert the times from degrees to hours by dividing by 15.
//        //
//        // If you'd like to check that your calculations are accurate or just
//        // need a quick result, check the USNO's Sun or Moon Rise/Set Table,
//        // <URL:http://aa.usno.navy.mil/AA/data/docs/RS_OneYear.html>.
//
//        double result = MT + (rise ? -HA0 : HA0); // in degrees
//
//        // Find UT midnight on this day
//        long midnight = DAY_MS * (time / DAY_MS);
//
//        return midnight + (long) (result * 3600000 / 15);
//    }

    //-------------------------------------------------------------------------
    // The Moon
    //-------------------------------------------------------------------------

    static final double moonL0 = 318.351648 * PI/180;   // Mean long. at epoch
    static final double moonP0 =  36.340410 * PI/180;   // Mean long. of perigee
    static final double moonN0 = 318.510107 * PI/180;   // Mean long. of node
    static final double moonI  =   5.145366 * PI/180;   // Inclination of orbit
    static final double moonE  =   0.054900;            // Eccentricity of orbit

    // These aren't used right now
    static final double moonA  =   3.84401e5;           // semi-major axis (km)
    static final double moonT0 =   0.5181 * PI/180;     // Angular size at distance A
    static final double moonPi =   0.9507 * PI/180;     // Parallax at distance A

    /**
     * The position of the moon at the time set on this
     * object, in equatorial coordinates.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Equatorial getMoonPosition()
    {
        //
        // See page 142 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.
        //
        if (moonPosition == null) {
            // Calculate the solar longitude.  Has the side effect of
            // filling in "meanAnomalySun" as well.
            double sunLong = getSunLongitude();

            //
            // Find the # of days since the epoch of our orbital parameters.
            // TODO: Convert the time of day portion into ephemeris time
            //
            double day = getJulianDay() - JD_EPOCH;       // Days since epoch

            // Calculate the mean longitude and anomaly of the moon, based on
            // a circular orbit.  Similar to the corresponding solar calculation.
            double meanLongitude = norm2PI(13.1763966*PI/180*day + moonL0);
            double meanAnomalyMoon = norm2PI(meanLongitude - 0.1114041*PI/180 * day - moonP0);

            //
            // Calculate the following corrections:
            //  Evection:   the sun's gravity affects the moon's eccentricity
            //  Annual Eqn: variation in the effect due to earth-sun distance
            //  A3:         correction factor (for ???)
            //
            double evection = 1.2739*PI/180 * Math.sin(2 * (meanLongitude - sunLong)
                                                - meanAnomalyMoon);
            double annual   = 0.1858*PI/180 * Math.sin(meanAnomalySun);
            double a3       = 0.3700*PI/180 * Math.sin(meanAnomalySun);

            meanAnomalyMoon += evection - annual - a3;

            //
            // More correction factors:
            //  center  equation of the center correction
            //  a4      yet another error correction (???)
            //
            // TODO: Skip the equation of the center correction and solve Kepler's eqn?
            //
            double center = 6.2886*PI/180 * Math.sin(meanAnomalyMoon);
            double a4 =     0.2140*PI/180 * Math.sin(2 * meanAnomalyMoon);

            // Now find the moon's corrected longitude
            moonLongitude = meanLongitude + evection + center - annual + a4;

            //
            // And finally, find the variation, caused by the fact that the sun's
            // gravitational pull on the moon varies depending on which side of
            // the earth the moon is on
            //
            double variation = 0.6583*PI/180 * Math.sin(2*(moonLongitude - sunLong));

            moonLongitude += variation;

            //
            // What we've calculated so far is the moon's longitude in the plane
            // of its own orbit.  Now map to the ecliptic to get the latitude
            // and longitude.  First we need to find the longitude of the ascending
            // node, the position on the ecliptic where it is crossed by the moon's
            // orbit as it crosses from the southern to the northern hemisphere.
            //
            double nodeLongitude = norm2PI(moonN0 - 0.0529539*PI/180 * day);

            nodeLongitude -= 0.16*PI/180 * Math.sin(meanAnomalySun);

            double y = Math.sin(moonLongitude - nodeLongitude);
            double x = Math.cos(moonLongitude - nodeLongitude);

            moonEclipLong = Math.atan2(y*Math.cos(moonI), x) + nodeLongitude;
            double moonEclipLat = Math.asin(y * Math.sin(moonI));

            moonPosition = eclipticToEquatorial(moonEclipLong, moonEclipLat);
        }
        return moonPosition;
    }

    /**
     * The "age" of the moon at the time specified in this object.
     * This is really the angle between the
     * current ecliptic longitudes of the sun and the moon,
     * measured in radians.
     *
     * @see #getMoonPhase
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getMoonAge() {
        // See page 147 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.
        //
        // Force the moon's position to be calculated.  We're going to use
        // some the intermediate results cached during that calculation.
        //
        getMoonPosition();

        return norm2PI(moonEclipLong - sunLongitude);
    }

    /**
     * Calculate the phase of the moon at the time set in this object.
     * The returned phase is a <code>double</code> in the range
     * <code>0 <= phase < 1</code>, interpreted as follows:
     * <ul>
     * <li>0.00: New moon
     * <li>0.25: First quarter
     * <li>0.50: Full moon
     * <li>0.75: Last quarter
     * </ul>
     *
     * @see #getMoonAge
     * @hide draft / provisional / internal are hidden on Android
     */
    public double getMoonPhase() {
        // See page 147 of "Practial Astronomy with your Calculator",
        // by Peter Duffet-Smith, for details on the algorithm.
        return 0.5 * (1 - Math.cos(getMoonAge()));
    }

    private static class MoonAge {
        double value;
        MoonAge(double val) { value = val; }
    }

    /**
     * Constant representing a new moon.
     * For use with {@link #getMoonTime(MoonAge, boolean) getMoonTime}
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final MoonAge NEW_MOON      = new MoonAge(0);

    /**
     * Constant representing the moon's first quarter.
     * For use with {@link #getMoonTime(MoonAge, boolean) getMoonTime}
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final MoonAge FIRST_QUARTER = new MoonAge(PI/2);

    /**
     * Constant representing a full moon.
     * For use with {@link #getMoonTime(MoonAge, boolean) getMoonTime}
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final MoonAge FULL_MOON     = new MoonAge(PI);

    /**
     * Constant representing the moon's last quarter.
     * For use with {@link #getMoonTime(MoonAge, boolean) getMoonTime}
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final MoonAge LAST_QUARTER  = new MoonAge((PI*3)/2);

    /**
     * Find the next or previous time at which the Moon's ecliptic
     * longitude will have the desired value.
     * <p>
     * @param desired   The desired longitude.
     * @param next      <tt>true</tt> if the next occurrance of the phase
     *                  is desired, <tt>false</tt> for the previous occurrance.
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getMoonTime(double desired, boolean next)
    {
        return timeOfAngle( new AngleFunc() {
                            @Override
                            public double eval() { return getMoonAge(); } },
                            desired,
                            SYNODIC_MONTH,
                            MINUTE_MS,
                            next);
    }

    /**
     * Find the next or previous time at which the moon will be in the
     * desired phase.
     * <p>
     * @param desired   The desired phase of the moon.
     * @param next      <tt>true</tt> if the next occurrance of the phase
     *                  is desired, <tt>false</tt> for the previous occurrance.
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getMoonTime(MoonAge desired, boolean next) {
        return getMoonTime(desired.value, next);
    }

    /**
     * Returns the time (GMT) of sunrise or sunset on the local date to which
     * this calendar is currently set.
     * @hide draft / provisional / internal are hidden on Android
     */
    public long getMoonRiseSet(boolean rise)
    {
        return riseOrSet(new CoordFunc() {
                            @Override
                            public Equatorial eval() { return getMoonPosition(); }
                         },
                         rise,
                         .533 * DEG_RAD,        // Angular Diameter
                         34 /60.0 * DEG_RAD,    // Refraction correction
                         MINUTE_MS);            // Desired accuracy
    }

    //-------------------------------------------------------------------------
    // Interpolation methods for finding the time at which a given event occurs
    //-------------------------------------------------------------------------

    private interface AngleFunc {
        public double eval();
    }

    private long timeOfAngle(AngleFunc func, double desired,
                             double periodDays, long epsilon, boolean next)
    {
        // Find the value of the function at the current time
        double lastAngle = func.eval();

        // Find out how far we are from the desired angle
        double deltaAngle = norm2PI(desired - lastAngle) ;

        // Using the average period, estimate the next (or previous) time at
        // which the desired angle occurs.
        double deltaT =  (deltaAngle + (next ? 0 : -PI2)) * (periodDays*DAY_MS) / PI2;

        double lastDeltaT = deltaT; // Liu
        long startTime = time; // Liu

        setTime(time + (long)deltaT);

        // Now iterate until we get the error below epsilon.  Throughout
        // this loop we use normPI to get values in the range -Pi to Pi,
        // since we're using them as correction factors rather than absolute angles.
        do {
            // Evaluate the function at the time we've estimated
            double angle = func.eval();

            // Find the # of milliseconds per radian at this point on the curve
            double factor = Math.abs(deltaT / normPI(angle-lastAngle));

            // Correct the time estimate based on how far off the angle is
            deltaT = normPI(desired - angle) * factor;

            // HACK:
            //
            // If abs(deltaT) begins to diverge we need to quit this loop.
            // This only appears to happen when attempting to locate, for
            // example, a new moon on the day of the new moon.  E.g.:
            //
            // This result is correct:
            // newMoon(7508(Mon Jul 23 00:00:00 CST 1990,false))=
            //   Sun Jul 22 10:57:41 CST 1990
            //
            // But attempting to make the same call a day earlier causes deltaT
            // to diverge:
            // CalendarAstronomer.timeOfAngle() diverging: 1.348508727575625E9 ->
            //   1.3649828540224032E9
            // newMoon(7507(Sun Jul 22 00:00:00 CST 1990,false))=
            //   Sun Jul 08 13:56:15 CST 1990
            //
            // As a temporary solution, we catch this specific condition and
            // adjust our start time by one eighth period days (either forward
            // or backward) and try again.
            // Liu 11/9/00
            if (Math.abs(deltaT) > Math.abs(lastDeltaT)) {
                long delta = (long) (periodDays * DAY_MS / 8);
                setTime(startTime + (next ? delta : -delta));
                return timeOfAngle(func, desired, periodDays, epsilon, next);
            }

            lastDeltaT = deltaT;
            lastAngle = angle;

            setTime(time + (long)deltaT);
        }
        while (Math.abs(deltaT) > epsilon);

        return time;
    }

    private interface CoordFunc {
        public Equatorial eval();
    }

    private long riseOrSet(CoordFunc func, boolean rise,
                           double diameter, double refraction,
                           long epsilon)
    {
        Equatorial  pos = null;
        double      tanL   = Math.tan(fLatitude);
        long        deltaT = Long.MAX_VALUE;
        int         count = 0;

        //
        // Calculate the object's position at the current time, then use that
        // position to calculate the time of rising or setting.  The position
        // will be different at that time, so iterate until the error is allowable.
        //
        do {
            // See "Practical Astronomy With Your Calculator, section 33.
            pos = func.eval();
            double angle = Math.acos(-tanL * Math.tan(pos.declination));
            double lst = ((rise ? PI2-angle : angle) + pos.ascension ) * 24 / PI2;

            // Convert from LST to Universal Time.
            long newTime = lstToUT( lst );

            deltaT = newTime - time;
            setTime(newTime);
        }
        while (++ count < 5 && Math.abs(deltaT) > epsilon);

        // Calculate the correction due to refraction and the object's angular diameter
        double cosD  = Math.cos(pos.declination);
        double psi   = Math.acos(Math.sin(fLatitude) / cosD);
        double x     = diameter / 2 + refraction;
        double y     = Math.asin(Math.sin(x) / Math.sin(psi));
        long  delta  = (long)((240 * y * RAD_DEG / cosD)*SECOND_MS);

        return time + (rise ? -delta : delta);
    }

    //-------------------------------------------------------------------------
    // Other utility methods
    //-------------------------------------------------------------------------

    /***
     * Given 'value', add or subtract 'range' until 0 <= 'value' < range.
     * The modulus operator.
     */
    private static final double normalize(double value, double range) {
        return value - range * Math.floor(value / range);
    }

    /**
     * Normalize an angle so that it's in the range 0 - 2pi.
     * For positive angles this is just (angle % 2pi), but the Java
     * mod operator doesn't work that way for negative numbers....
     */
    private static final double norm2PI(double angle) {
        return normalize(angle, PI2);
    }

    /**
     * Normalize an angle into the range -PI - PI
     */
    private static final double normPI(double angle) {
        return normalize(angle + PI, PI2) - PI;
    }

    /**
     * Find the "true anomaly" (longitude) of an object from
     * its mean anomaly and the eccentricity of its orbit.  This uses
     * an iterative solution to Kepler's equation.
     *
     * @param meanAnomaly   The object's longitude calculated as if it were in
     *                      a regular, circular orbit, measured in radians
     *                      from the point of perigee.
     *
     * @param eccentricity  The eccentricity of the orbit
     *
     * @return The true anomaly (longitude) measured in radians
     */
    private double trueAnomaly(double meanAnomaly, double eccentricity)
    {
        // First, solve Kepler's equation iteratively
        // Duffett-Smith, p.90
        double delta;
        double E = meanAnomaly;
        do {
            delta = E - eccentricity * Math.sin(E) - meanAnomaly;
            E = E - delta / (1 - eccentricity * Math.cos(E));
        }
        while (Math.abs(delta) > 1e-5); // epsilon = 1e-5 rad

        return 2.0 * Math.atan( Math.tan(E/2) * Math.sqrt( (1+eccentricity)
                                                          /(1-eccentricity) ) );
    }

    /**
     * Return the obliquity of the ecliptic (the angle between the ecliptic
     * and the earth's equator) at the current time.  This varies due to
     * the precession of the earth's axis.
     *
     * @return  the obliquity of the ecliptic relative to the equator,
     *          measured in radians.
     */
    private double eclipticObliquity() {
        if (eclipObliquity == INVALID) {
            final double epoch = 2451545.0;     // 2000 AD, January 1.5

            double T = (getJulianDay() - epoch) / 36525;

            eclipObliquity = 23.439292
                           - 46.815/3600 * T
                           - 0.0006/3600 * T*T
                           + 0.00181/3600 * T*T*T;

            eclipObliquity *= DEG_RAD;
        }
        return eclipObliquity;
    }


    //-------------------------------------------------------------------------
    // Private data
    //-------------------------------------------------------------------------

    /**
     * Current time in milliseconds since 1/1/1970 AD
     * @see java.util.Date#getTime
     */
    private long time;

    /* These aren't used yet, but they'll be needed for sunset calculations
     * and equatorial to horizon coordinate conversions
     */
    private double fLongitude = 0.0;
    private double fLatitude  = 0.0;
    private long   fGmtOffset = 0;

    //
    // The following fields are used to cache calculated results for improved
    // performance.  These values all depend on the current time setting
    // of this object, so the clearCache method is provided.
    //
    static final private double INVALID = Double.MIN_VALUE;

    private transient double    julianDay       = INVALID;
    private transient double    julianCentury   = INVALID;
    private transient double    sunLongitude    = INVALID;
    private transient double    meanAnomalySun  = INVALID;
    private transient double    moonLongitude   = INVALID;
    private transient double    moonEclipLong   = INVALID;
    //private transient double    meanAnomalyMoon = INVALID;
    private transient double    eclipObliquity  = INVALID;
    private transient double    siderealT0      = INVALID;
    private transient double    siderealTime    = INVALID;

    private transient Equatorial  moonPosition = null;

    private void clearCache() {
        julianDay       = INVALID;
        julianCentury   = INVALID;
        sunLongitude    = INVALID;
        meanAnomalySun  = INVALID;
        moonLongitude   = INVALID;
        moonEclipLong   = INVALID;
        //meanAnomalyMoon = INVALID;
        eclipObliquity  = INVALID;
        siderealTime    = INVALID;
        siderealT0      = INVALID;
        moonPosition    = null;
    }

    //private static void out(String s) {
    //    System.out.println(s);
    //}

    //private static String deg(double rad) {
    //    return Double.toString(rad * RAD_DEG);
    //}

    //private static String hours(long ms) {
    //    return Double.toString((double)ms / HOUR_MS) + " hours";
    //}

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public String local(long localMillis) {
        return new Date(localMillis - TimeZone.getDefault().getRawOffset()).toString();
    }


    /**
     * Represents the position of an object in the sky relative to the ecliptic,
     * the plane of the earth's orbit around the Sun.
     * This is a spherical coordinate system in which the latitude
     * specifies the position north or south of the plane of the ecliptic.
     * The longitude specifies the position along the ecliptic plane
     * relative to the "First Point of Aries", which is the Sun's position in the sky
     * at the Vernal Equinox.
     * <p>
     * Note that Ecliptic objects are immutable and cannot be modified
     * once they are constructed.  This allows them to be passed and returned by
     * value without worrying about whether other code will modify them.
     *
     * @see CalendarAstronomer.Equatorial
     * @see CalendarAstronomer.Horizon
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Ecliptic {
        /**
         * Constructs an Ecliptic coordinate object.
         * <p>
         * @param lat The ecliptic latitude, measured in radians.
         * @param lon The ecliptic longitude, measured in radians.
         * @hide draft / provisional / internal are hidden on Android
         */
        public Ecliptic(double lat, double lon) {
            latitude = lat;
            longitude = lon;
        }

        /**
         * Return a string representation of this object
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public String toString() {
            return Double.toString(longitude*RAD_DEG) + "," + (latitude*RAD_DEG);
        }

        /**
         * The ecliptic latitude, in radians.  This specifies an object's
         * position north or south of the plane of the ecliptic,
         * with positive angles representing north.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double latitude;

        /**
         * The ecliptic longitude, in radians.
         * This specifies an object's position along the ecliptic plane
         * relative to the "First Point of Aries", which is the Sun's position
         * in the sky at the Vernal Equinox,
         * with positive angles representing east.
         * <p>
         * A bit of trivia: the first point of Aries is currently in the
         * constellation Pisces, due to the precession of the earth's axis.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double longitude;
    }

    /**
     * Represents the position of an
     * object in the sky relative to the plane of the earth's equator.
     * The <i>Right Ascension</i> specifies the position east or west
     * along the equator, relative to the sun's position at the vernal
     * equinox.  The <i>Declination</i> is the position north or south
     * of the equatorial plane.
     * <p>
     * Note that Equatorial objects are immutable and cannot be modified
     * once they are constructed.  This allows them to be passed and returned by
     * value without worrying about whether other code will modify them.
     *
     * @see CalendarAstronomer.Ecliptic
     * @see CalendarAstronomer.Horizon
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Equatorial {
        /**
         * Constructs an Equatorial coordinate object.
         * <p>
         * @param asc The right ascension, measured in radians.
         * @param dec The declination, measured in radians.
         * @hide draft / provisional / internal are hidden on Android
         */
        public Equatorial(double asc, double dec) {
            ascension = asc;
            declination = dec;
        }

        /**
         * Return a string representation of this object, with the
         * angles measured in degrees.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public String toString() {
            return Double.toString(ascension*RAD_DEG) + "," + (declination*RAD_DEG);
        }

        /**
         * Return a string representation of this object with the right ascension
         * measured in hours, minutes, and seconds.
         * @hide draft / provisional / internal are hidden on Android
         */
        public String toHmsString() {
            return radToHms(ascension) + "," + radToDms(declination);
        }

        /**
         * The right ascension, in radians.
         * This is the position east or west along the equator
         * relative to the sun's position at the vernal equinox,
         * with positive angles representing East.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double ascension;

        /**
         * The declination, in radians.
         * This is the position north or south of the equatorial plane,
         * with positive angles representing north.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double declination;
    }

    /**
     * Represents the position of an  object in the sky relative to
     * the local horizon.
     * The <i>Altitude</i> represents the object's elevation above the horizon,
     * with objects below the horizon having a negative altitude.
     * The <i>Azimuth</i> is the geographic direction of the object from the
     * observer's position, with 0 representing north.  The azimuth increases
     * clockwise from north.
     * <p>
     * Note that Horizon objects are immutable and cannot be modified
     * once they are constructed.  This allows them to be passed and returned by
     * value without worrying about whether other code will modify them.
     *
     * @see CalendarAstronomer.Ecliptic
     * @see CalendarAstronomer.Equatorial
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Horizon {
        /**
         * Constructs a Horizon coordinate object.
         * <p>
         * @param alt  The altitude, measured in radians above the horizon.
         * @param azim The azimuth, measured in radians clockwise from north.
         * @hide draft / provisional / internal are hidden on Android
         */
        public Horizon(double alt, double azim) {
            altitude = alt;
            azimuth = azim;
        }

        /**
         * Return a string representation of this object, with the
         * angles measured in degrees.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public String toString() {
            return Double.toString(altitude*RAD_DEG) + "," + (azimuth*RAD_DEG);
        }

        /**
         * The object's altitude above the horizon, in radians.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double altitude;

        /**
         * The object's direction, in radians clockwise from north.
         * @hide draft / provisional / internal are hidden on Android
         */
        public final double azimuth;
    }

    static private String radToHms(double angle) {
        int hrs = (int) (angle*RAD_HOUR);
        int min = (int)((angle*RAD_HOUR - hrs) * 60);
        int sec = (int)((angle*RAD_HOUR - hrs - min/60.0) * 3600);

        return Integer.toString(hrs) + "h" + min + "m" + sec + "s";
    }

    static private String radToDms(double angle) {
        int deg = (int) (angle*RAD_DEG);
        int min = (int)((angle*RAD_DEG - deg) * 60);
        int sec = (int)((angle*RAD_DEG - deg - min/60.0) * 3600);

        return Integer.toString(deg) + "\u00b0" + min + "'" + sec + "\"";
    }
}
