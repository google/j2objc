/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2003-2011, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;

import android.icu.util.VersionInfo;

public abstract class AbstractTestLog implements TestLog {
    /**
     * Returns true if ICU_Version < major.minor.
     */
    static public boolean isICUVersionBefore(int major, int minor) {
        return isICUVersionBefore(major, minor, 0);
    }

    /**
     * Returns true if ICU_Version < major.minor.milli.
     */
    static public boolean isICUVersionBefore(int major, int minor, int milli) {
        return VersionInfo.ICU_VERSION.compareTo(VersionInfo.getInstance(major, minor, milli)) < 0;
    }

    /**
     * Returns true if ICU_Version >= major.minor.
     */
    static public boolean isICUVersionAtLeast(int major, int minor) {
        return isICUVersionAtLeast(major, minor, 0);
    }

    /**
     * Returns true if ICU_Version >= major.minor.milli.
     */
    static public boolean isICUVersionAtLeast(int major, int minor, int milli) {
        return !isICUVersionBefore(major, minor, milli);
    }

    /**
     * Add a message.
     */
    public static final void log(String message) {
        // TODO(stuartg): turned off - causing OOM running under ant
        // Probably temporary - must decide what to do with these
        //System.out.print(message);
        //msg(message, LOG, true, false);
    }

    /**
     * Add a message and newline.
     */
    public static final void logln(String message) {
        // TODO(stuartg): turned off - causing OOM running under ant
        // Probably temporary - must decide what to do with these
        //System.out.println(message);
        //msg(message, LOG, true, true);
    }

    /**
     * Report an error.
     */
    public static final void err(String message) {
        Assert.fail(message);
        //msg(message, ERR, true, false);
    }

    /**
     * Report an error and newline.
     */
    public static final void errln(String message) {
        Assert.fail(message);
        //msg(message, ERR, true, true);
    }

    /**
     * Report a warning (generally missing tests or data).
     */
    public static final void warn(String message) {
        Assert.fail(message);
        // TODO(stuartg): turned off - causing OOM running under ant
        //System.out.print(message);
        //msg(message, WARN, true, false);
    }

    /**
     * Report a warning (generally missing tests or data) and newline.
     */
    public static final void warnln(String message) {
        Assert.fail(message);
        // TODO(stuartg): turned off - causing OOM running under ant
        //System.out.println(message);
        //msg(message, WARN, true, true);
    }

    /**
     * Vector for logging.  Callers can force the logging system to
     * not increment the error or warning level by passing false for incCount.
     *
     * @param message the message to output.
     * @param level the message level, either LOG, WARN, or ERR.
     * @param incCount if true, increments the warning or error count
     * @param newln if true, forces a newline after the message
     */
    //public abstract void msg(String message, int level, boolean incCount, boolean newln);

    /**
     * Not sure if this class is useful.  This lets you log without first testing
     * if logging is enabled.  The Delegating log will either silently ignore the
     * message, if the delegate is null, or forward it to the delegate.
     */
    public static final class DelegatingLog extends AbstractTestLog {
        private TestLog delegate;

        public DelegatingLog(TestLog delegate) {
            this.delegate = delegate;
        }

        public void msg(String message, int level, boolean incCount, boolean newln) {
            if (delegate != null) {
                // TODO(junit): figure out what to do with this message call
                TestFmwk.msg(message, level, incCount, newln);
                //delegate.msg(message, level, incCount, newln);
            }
        }
    }
    public boolean isDateAtLeast(int year, int month, int day){
        Date now = new Date();
        Calendar c = new GregorianCalendar(year, month, day);
        Date dt = c.getTime();
        if(now.compareTo(dt)>=0){
            return true;
        }
        return false;
    }
}
