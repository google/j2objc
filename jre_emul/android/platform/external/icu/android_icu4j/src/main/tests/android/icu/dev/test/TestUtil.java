/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public final class TestUtil {
    /**
     * Path to test data in icu4jtest.jar
     */
    public static final String DATA_PATH = "/android/icu/dev/data/";

    /**
     * Return an input stream on the data file at path 'name' rooted at the data path
     */
    public static final InputStream getDataStream(String name) throws IOException {
        InputStream is = null;
        try {
            is = TestUtil.class.getResourceAsStream(DATA_PATH + name);
        } catch (Throwable t) {
            IOException ex =
                new IOException("data resource '" + name + "' not found");
            ex.initCause(t);
            throw ex;
        }
        return is;
    }

    /**
     * Return a buffered reader on the data file at path 'name' rooted at the data path.
     */
    public static final BufferedReader getDataReader(String name, String charset) throws IOException {
        InputStream is = getDataStream(name);
        InputStreamReader isr =
            charset == null
                ? new InputStreamReader(is)
                : new InputStreamReader(is, charset);
        return new BufferedReader(isr);
    }

    /**
     * Return a buffered reader on the data file at path 'name' rooted at the data path,
     * using the provided encoding.
     */
    public static final BufferedReader getDataReader(String name)
        throws IOException {
        return getDataReader(name, null);
    }

    static final char DIGITS[] =
        {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z' };
    /**
     * Return true if the character is NOT printable ASCII.  The tab,
     * newline and linefeed characters are considered unprintable.
     */
    public static boolean isUnprintable(int c) {
        return !(c >= 0x20 && c <= 0x7E);
    }
    /**
     * Escape unprintable characters using <backslash>uxxxx notation
     * for U+0000 to U+FFFF and <backslash>Uxxxxxxxx for U+10000 and
     * above.  If the character is printable ASCII, then do nothing
     * and return FALSE.  Otherwise, append the escaped notation and
     * return TRUE.
     */
    public static boolean escapeUnprintable(StringBuffer result, int c) {
        if (isUnprintable(c)) {
            result.append('\\');
            if ((c & ~0xFFFF) != 0) {
                result.append('U');
                result.append(DIGITS[0xF & (c >> 28)]);
                result.append(DIGITS[0xF & (c >> 24)]);
                result.append(DIGITS[0xF & (c >> 20)]);
                result.append(DIGITS[0xF & (c >> 16)]);
            } else {
                result.append('u');
            }
            result.append(DIGITS[0xF & (c >> 12)]);
            result.append(DIGITS[0xF & (c >> 8)]);
            result.append(DIGITS[0xF & (c >> 4)]);
            result.append(DIGITS[0xF & c]);
            return true;
        }
        return false;
    }

    static class Lock {
        private int count;

        synchronized void inc() {
            ++count;
        }

        synchronized void dec() {
            --count;
        }

        synchronized int count() {
            return count;
        }

        void go() {
            try {
                while (count() > 0) {
                    synchronized (this) {
                        notifyAll();
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    static class TestThread extends Thread {
        Lock lock;
        Runnable target;

        TestThread(Lock lock, Runnable target) {
            this.lock = lock;
            this.target = target;

            lock.inc();
        }

        public void run() {
            try {
                synchronized (lock) {
                    lock.wait();
                }
                target.run();
            } catch (InterruptedException e) {
            }

            lock.dec();
        }
    }

    public static void runUntilDone(Runnable[] targets) {
        if (targets == null) {
            throw new IllegalArgumentException("targets is null");
        }
        if (targets.length == 0) {
            return;
        }

        Lock lock = new Lock();
        for (int i = 0; i < targets.length; ++i) {
            new TestThread(lock, targets[i]).start();
        }

        lock.go();
    }
    public static BufferedReader openUTF8Reader(String dir, String filename) throws IOException {
        return openReader(dir,filename,"UTF-8");
    }
    public static BufferedReader openReader(String dir, String filename, String encoding) throws IOException {
        File file = new File(dir + filename);
        return new BufferedReader(
            new InputStreamReader(
                new FileInputStream(file),
                encoding),
            4*1024);
    }

    public enum JavaVendor {
        Unknown,
        Oracle,
        IBM,
        Android
    }

    public static JavaVendor getJavaVendor() {
        JavaVendor vendor = JavaVendor.Unknown;
        String javaVendorProp = System.getProperty("java.vendor", "").toLowerCase(Locale.US).trim();
        if (javaVendorProp.startsWith("ibm")) {
            vendor = JavaVendor.IBM;
        } else if (javaVendorProp.startsWith("sun") || javaVendorProp.startsWith("oracle")) {
            vendor = JavaVendor.Oracle;
        } else if (javaVendorProp.contains("android")) {
            vendor = JavaVendor.Android;
        }
        return vendor;
    }

    public static int getJavaVersion() {
        int ver = -1;
        String verstr = System.getProperty("java.version");
        if (verstr != null) {
            String[] numbers = verstr.split("\\.");
            try {
                ver = Integer.parseInt(numbers[1]);
            } catch (NumberFormatException e) {
                ver = -1;
            }
        }
        return ver;
    }
}
