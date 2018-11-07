/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2008-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.TreeSet;

import android.icu.util.TimeZone;

/**
 * JavaTimeZone inherits android.icu.util.TimeZone and wraps java.util.TimeZone.
 * We used to have JDKTimeZone which wrapped Java TimeZone and used it as primary
 * TimeZone implementation until ICU4J 3.4.1.  This class works exactly like
 * JDKTimeZone and allows ICU users who use ICU4J and JDK date/time/calendar
 * services in mix to maintain only JDK timezone rules.
 *
 * This TimeZone subclass is returned by the TimeZone factory method getTimeZone(String)
 * when the default timezone type in TimeZone class is TimeZone.TIMEZONE_JDK.
 * @hide Only a subset of ICU is exposed in Android
 */
public class JavaTimeZone extends TimeZone {

    private static final long serialVersionUID = 6977448185543929364L;

    private static final TreeSet<String> AVAILABLESET;

    private java.util.TimeZone javatz;
    private transient java.util.Calendar javacal;
    /* J2ObjC removed: use of reflection.
    private static Method mObservesDaylightTime; */

    static {
        AVAILABLESET = new TreeSet<String>();
        String[] availableIds = java.util.TimeZone.getAvailableIDs();
        for (int i = 0; i < availableIds.length; i++) {
            AVAILABLESET.add(availableIds[i]);
        }

        /* J2ObjC removed: use of reflection.
        try {
            mObservesDaylightTime = java.util.TimeZone.class.getMethod("observesDaylightTime", (Class[]) null);
        } catch (NoSuchMethodException e) {
            // Java 6 or older
        } catch (SecurityException e) {
            // not visible
        } */
    }

    /**
     * Constructs a JavaTimeZone with the default Java TimeZone
     */
    public JavaTimeZone() {
        this(java.util.TimeZone.getDefault(), null);
    }

    /**
     * Constructs a JavaTimeZone with the specified Java TimeZone and ID.
     * @param jtz the Java TimeZone
     * @param id the ID of the zone. if null, the zone ID is initialized
     * by the given Java TimeZone's ID.
     */
    public JavaTimeZone(java.util.TimeZone jtz, String id) {
        if (id == null) {
            id = jtz.getID();
        }
        javatz = jtz;
        setID(id);
        javacal = new java.util.GregorianCalendar(javatz);
    }

    /**
     * Creates an instance of JavaTimeZone with the given timezone ID.
     * @param id A timezone ID, either a system ID or a custom ID.
     * @return An instance of JavaTimeZone for the given ID, or null
     * when the ID cannot be understood.
     */
    public static JavaTimeZone createTimeZone(String id) {
        java.util.TimeZone jtz = null;

        if (AVAILABLESET.contains(id)) {
            jtz = java.util.TimeZone.getTimeZone(id);
        }

        if (jtz == null) {
            // Use ICU's canonical ID mapping
            boolean[] isSystemID = new boolean[1];
            String canonicalID = TimeZone.getCanonicalID(id, isSystemID);
            if (isSystemID[0] && AVAILABLESET.contains(canonicalID)) {
                jtz = java.util.TimeZone.getTimeZone(canonicalID);
            }
        }

        if (jtz == null) {
            return null;
        }

        return new JavaTimeZone(jtz, id);
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#getOffset(int, int, int, int, int, int)
     */
    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        return javatz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#getOffset(long, boolean, int[])
     */
    @Override
    public void getOffset(long date, boolean local, int[] offsets) {
        synchronized (javacal) {
            if (local) {
                int fields[] = new int[6];
                Grego.timeToFields(date, fields);
                int hour, min, sec, mil;
                int tmp = fields[5];
                mil = tmp % 1000;
                tmp /= 1000;
                sec = tmp % 60;
                tmp /= 60;
                min = tmp % 60;
                hour = tmp / 60;
                javacal.clear();
                javacal.set(fields[0], fields[1], fields[2], hour, min, sec);
                javacal.set(java.util.Calendar.MILLISECOND, mil);

                int doy1, hour1, min1, sec1, mil1;
                doy1 = javacal.get(java.util.Calendar.DAY_OF_YEAR);
                hour1 = javacal.get(java.util.Calendar.HOUR_OF_DAY);
                min1 = javacal.get(java.util.Calendar.MINUTE);
                sec1 = javacal.get(java.util.Calendar.SECOND);
                mil1 = javacal.get(java.util.Calendar.MILLISECOND);

                if (fields[4] != doy1 || hour != hour1 || min != min1 || sec != sec1 || mil != mil1) {
                    // Calendar field(s) were changed due to the adjustment for non-existing time
                    // Note: This code does not support non-existing local time at year boundary properly.
                    // But, it should work fine for real timezones.
                    int dayDelta = Math.abs(doy1 - fields[4]) > 1 ? 1 : doy1 - fields[4];
                    int delta = ((((dayDelta * 24) + hour1 - hour) * 60 + min1 - min) * 60 + sec1 - sec) * 1000 + mil1 - mil;

                    // In this case, we use the offsets before the transition
                   javacal.setTimeInMillis(javacal.getTimeInMillis() - delta - 1);
                }
            } else {
                javacal.setTimeInMillis(date);
            }
            offsets[0] = javacal.get(java.util.Calendar.ZONE_OFFSET);
            offsets[1] = javacal.get(java.util.Calendar.DST_OFFSET);
        }
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#getRawOffset()
     */
    @Override
    public int getRawOffset() {
        return javatz.getRawOffset();
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#inDaylightTime(java.util.Date)
     */
    @Override
    public boolean inDaylightTime(Date date) {
        return javatz.inDaylightTime(date);
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#setRawOffset(int)
     */
    @Override
    public void setRawOffset(int offsetMillis) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen JavaTimeZone instance.");
        }
        javatz.setRawOffset(offsetMillis);
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#useDaylightTime()
     */
    @Override
    public boolean useDaylightTime() {
        return javatz.useDaylightTime();
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#observesDaylightTime()
     */
    @Override
    public boolean observesDaylightTime() {
        /* J2ObjC removed: use of reflection.
        if (mObservesDaylightTime != null) {
            // Java 7+
            try {
                return (Boolean)mObservesDaylightTime.invoke(javatz, (Object[]) null);
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return super.observesDaylightTime(); */
        return javatz.observesDaylightTime();
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#getDSTSavings()
     */
    @Override
    public int getDSTSavings() {
        return javatz.getDSTSavings();
    }

    public java.util.TimeZone unwrap() {
        return javatz;
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#clone()
     */
    @Override
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode() + javatz.hashCode();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        javacal = new java.util.GregorianCalendar(javatz);
    }

    // Freezable stuffs
    private transient volatile boolean isFrozen = false;

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#isFrozen()
     */
    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#freeze()
     */
    @Override
    public TimeZone freeze() {
        isFrozen = true;
        return this;
    }

    /* (non-Javadoc)
     * @see android.icu.util.TimeZone#cloneAsThawed()
     */
    @Override
    public TimeZone cloneAsThawed() {
        JavaTimeZone tz = (JavaTimeZone)super.cloneAsThawed();
        tz.javatz = (java.util.TimeZone)javatz.clone();
        tz.javacal = new java.util.GregorianCalendar(javatz);  // easier than synchronized javacal.clone()
        tz.isFrozen = false;
        return tz;
    }

}
