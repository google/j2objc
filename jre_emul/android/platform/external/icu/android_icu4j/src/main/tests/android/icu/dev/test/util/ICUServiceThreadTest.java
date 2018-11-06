/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestLog;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.ICUService.SimpleFactory;
import android.icu.util.ULocale;

public class ICUServiceThreadTest extends TestFmwk
{
    private static final boolean PRINTSTATS = false;

    private static final String[] countries = {
        "ab", "bc", "cd", "de", "ef", "fg", "gh", "ji", "ij", "jk"
    };
    private static final String[] languages = {
        "", "ZY", "YX", "XW", "WV", "VU", "UT", "TS", "SR", "RQ", "QP"
    };
    private static final String[] variants = {
        "", "", "", "GOLD", "SILVER", "BRONZE"
    };

    private static class TestFactory extends SimpleFactory {
        TestFactory(String id) {
            super(new ULocale(id), id, true);
        }

        public String getDisplayName(String idForDisplay, ULocale locale) {
            return (visible && idForDisplay.equals(this.id)) ? "(" + locale.toString() + ") " + idForDisplay : null;
        }

        public String toString() {
            return "Factory_" + id;
        }
    }
    /**
     * Convenience override of getDisplayNames(ULocale, Comparator, String) that
     * uses the default collator for the locale as the comparator to
     * sort the display names, and null for the matchID.
     */
    public static SortedMap getDisplayNames(ICUService service, ULocale locale) {
        Collator col;
        try {
            col = Collator.getInstance(locale.toLocale());
        }
        catch (MissingResourceException e) {
            // if no collator resources, we can't collate
            col = null;
        }
        return service.getDisplayNames(locale, col, null);
    }
    private static final Random r = new Random(); // this is a multi thread test, can't 'unrandomize'

    private static String getCLV() {
        String c = countries[r.nextInt(countries.length)];
        String l = languages[r.nextInt(languages.length)];
        String v = variants[r.nextInt(variants.length)];
        return new Locale(c, l, v).toString();
    }

    private static boolean WAIT = true;
    private static boolean GO = false;
    private static long TIME = 5000;

    public static void runThreads() {
        runThreads(TIME);
    }

    public static void runThreads(long time) {
        try {
            GO = true;
            WAIT = false;

            Thread.sleep(time);

            WAIT = true;
            GO = false;

            Thread.sleep(300);
        }
        catch (InterruptedException e) {
        }
    }

    static class TestThread extends Thread {
        //private final String name;
        protected ICUService service;
        private final long delay;
        protected final TestLog log;

        public TestThread(String name, ICUService service, long delay, TestLog log) {
            //this.name = name + " ";
            this.service = service;
            this.delay = delay;
            this.log = new DelegatingLog(log);
            this.setDaemon(true);
        }

        public void run() {
            while (WAIT) {
                Thread.yield();
            }

            try {
                while (GO) {
                    iterate();
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
            }
            catch (InterruptedException e) {
            }
        }

        protected void iterate() {
        }

        /*
          public boolean logging() {
          return log != null;
          }

          public void log(String msg) {
          if (logging()) {
          log.log(name + msg);
          }
          }

          public void logln(String msg) {
          if (logging()) {
          log.logln(name + msg);
          }
          }

          public void err(String msg) {
          if (logging()) {
          log.err(name + msg);
          }
          }

          public void errln(String msg) {
          if (logging()) {
          log.errln(name + msg);
          }
          }

          public void warn(String msg) {
          if (logging()) {
          log.info(name + msg);
          }
          }

          public void warnln(String msg) {
          if (logging()) {
          log.infoln(name + msg);
          }
          }
        */
    }

    static class RegisterFactoryThread extends TestThread {
        RegisterFactoryThread(String name, ICUService service, long delay, TestLog log) {
            super("REG " + name, service, delay, log);
        }

        protected void iterate() {
            Factory f = new TestFactory(getCLV());
            service.registerFactory(f);
            //log.logln(f.toString());
            TestFmwk.logln(f.toString());
        }
    }

    static class UnregisterFactoryThread extends TestThread {
        private Random r;
        List factories;

        UnregisterFactoryThread(String name, ICUService service, long delay, TestLog log) {
            super("UNREG " + name, service, delay, log);

            r = new Random();
            factories = service.factories();
        }

        public void iterate() {
            int s = factories.size();
            if (s == 0) {
                factories = service.factories();
            } else {
                int n = r.nextInt(s);
                Factory f = (Factory)factories.remove(n);
                boolean success = service.unregisterFactory(f);
                //log.logln("factory: " + f + (success ? " succeeded." : " *** failed."));
                TestFmwk.logln("factory: " + f + (success ? " succeeded." : " *** failed."));
            }
        }
    }

    static class UnregisterFactoryListThread extends TestThread {
        Factory[] factories;
        int n;

        UnregisterFactoryListThread(String name, ICUService service, long delay, Factory[] factories, TestLog log) {
            super("UNREG " + name, service, delay, log);

            this.factories = factories;
        }

        public void iterate() {
            if (n < factories.length) {
                Factory f = factories[n++];
                boolean success = service.unregisterFactory(f);
                //log.logln("factory: " + f + (success ? " succeeded." : " *** failed."));
                TestFmwk.logln("factory: " + f + (success ? " succeeded." : " *** failed."));
            }
        }
    }


    static class GetVisibleThread extends TestThread {
        GetVisibleThread(String name, ICUService service, long delay, TestLog log) {
            super("VIS " + name, service, delay, log);
        }

        protected void iterate() {
            Set ids = service.getVisibleIDs();
            Iterator iter = ids.iterator();
            int n = 10;
            while (--n >= 0 && iter.hasNext()) {
                String id = (String)iter.next();
                Object result = service.get(id);
                //log.logln("iter: " + n + " id: " + id + " result: " + result);
                TestFmwk.logln("iter: " + n + " id: " + id + " result: " + result);
            }
        }
    }

    static class GetDisplayThread extends TestThread {
        ULocale locale;

        GetDisplayThread(String name, ICUService service, long delay, ULocale locale, TestLog log) {
            super("DIS " + name, service, delay, log);

            this.locale = locale;
        }

        protected void iterate() {
            Map names = getDisplayNames(service,locale);
            Iterator iter = names.entrySet().iterator();
            int n = 10;
            while (--n >= 0 && iter.hasNext()) {
                Entry e = (Entry)iter.next();
                String dname = (String)e.getKey();
                String id = (String)e.getValue();
                Object result = service.get(id);

                // Note: IllegalMonitorStateException is thrown by the code
                // below on IBM JRE5 for AIX 64bit.  For some reason, converting
                // int to String out of this statement resolves the issue.

                //log.logln(" iter: " + n +
                String num = Integer.toString(n);
//                log.logln(" iter: " + num +
//                        " dname: " + dname +
//                        " id: " + id +
//                        " result: " + result);
                TestFmwk.logln(" iter: " + num +
                        " dname: " + dname +
                        " id: " + id +
                        " result: " + result);
            }
        }
    }

    static class GetThread extends TestThread {
        private String[] actualID;

        GetThread(String name, ICUService service, long delay, TestLog log) {
            super("GET " + name, service, delay, log);

            actualID = new String[1];
        }

        protected void iterate() {
            String id = getCLV();
            Object o = service.get(id, actualID);
            if (o != null) {
                //log.logln(" id: " + id + " actual: " + actualID[0] + " result: " + o);
                TestFmwk.logln(" id: " + id + " actual: " + actualID[0] + " result: " + o);
            }
        }
    }

    static class GetListThread extends TestThread {
        private final String[] list;
        private int n;

        GetListThread(String name, ICUService service, long delay, String[] list, TestLog log) {
            super("GETL " + name, service, delay, log);

            this.list = list;
        }

        protected void iterate() {
            if (--n < 0) {
                n = list.length - 1;
            }
            String id = list[n];
            Object o = service.get(id);
            //log.logln(" id: " + id + " result: " + o);
            TestFmwk.logln(" id: " + id + " result: " + o);
        }
    }

    // return a collection of unique factories, might be fewer than requested
    Collection getFactoryCollection(int requested) {
        Set locales = new HashSet();
        for (int i = 0; i < requested; ++i) {
            locales.add(getCLV());
        }
        List factories = new ArrayList(locales.size());
        Iterator iter = locales.iterator();
        while (iter.hasNext()) {
            factories.add(new TestFactory((String)iter.next()));
        }
        return factories;
    }

    void registerFactories(ICUService service, Collection c) {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            service.registerFactory((Factory)iter.next());
        }
    }

    ICUService stableService() {
        if (stableService == null) {
            stableService = new ICULocaleService();
            registerFactories(stableService, getFactoryCollection(50));
        }
        if (PRINTSTATS) stableService.stats();  // Enable the stats collection
        return stableService;
    }
    private ICUService stableService;

    // run multiple get on a stable service
    @Test
    public void Test00_ConcurrentGet() {
        for(int i = 0; i < 10; ++i) {
            new GetThread("[" + Integer.toString(i) + "]",  stableService(), 0, this).start();
        }
        runThreads();
        if (PRINTSTATS) System.out.println(stableService.stats());
    }

    // run multiple getVisibleID on a stable service
    @Test
    public void Test01_ConcurrentGetVisible() {
        for(int i = 0; i < 10; ++i) {
            new GetVisibleThread("[" + Integer.toString(i) + "]",  stableService(), 0, this).start();
        }
        runThreads();
        if (PRINTSTATS) System.out.println(stableService.stats());
    }

    // run multiple getDisplayName on a stable service
    @Test
    public void Test02_ConcurrentGetDisplay() {
        String[] localeNames = {
            "en", "es", "de", "fr", "zh", "it", "no", "sv"
        };
        for(int i = 0; i < localeNames.length; ++i) {
            String locale = localeNames[i];
            new GetDisplayThread("[" + locale + "]",
                                 stableService(),
                                 0,
                                 new ULocale(locale),
                                 this).start();
        }
        runThreads();
        if (PRINTSTATS) System.out.println(stableService.stats());
    }

    // run register/unregister on a service
    @Test
    public void Test03_ConcurrentRegUnreg() {
        ICUService service = new ICULocaleService();
        if (PRINTSTATS) service.stats();    // Enable the stats collection
        for (int i = 0; i < 5; ++i) {
            new RegisterFactoryThread("[" + i + "]", service, 0, this).start();
        }
        for (int i = 0; i < 5; ++i) {
            new UnregisterFactoryThread("[" + i + "]", service, 0, this).start();
        }
        runThreads();
        if (PRINTSTATS) System.out.println(service.stats());
    }

    @Test
    public void Test04_WitheringService() {
        ICUService service = new ICULocaleService();
        if (PRINTSTATS) service.stats();    // Enable the stats collection

        Collection fc = getFactoryCollection(50);
        registerFactories(service, fc);

        Factory[] factories = (Factory[])fc.toArray(new Factory[fc.size()]);
        Comparator comp = new Comparator() {
                public int compare(Object lhs, Object rhs) {
                    return lhs.toString().compareTo(rhs.toString());
                }
            };
        Arrays.sort(factories, comp);

        new GetThread("", service, 0, this).start();
        new UnregisterFactoryListThread("", service, 3, factories, this).start();

        runThreads(2000);
        if (PRINTSTATS) System.out.println(service.stats());
    }

    // "all hell breaks loose"
    // one register and one unregister thread, delay 500ms
    // two display threads with different locales, delay 500ms;
    // one visible id thread, delay 50ms
    // fifteen get threads, delay 0
    // run for ten seconds
    @Test
    public void Test05_ConcurrentEverything() {
        ICUService service = new ICULocaleService();
        if (PRINTSTATS) service.stats();    // Enable the stats collection

        new RegisterFactoryThread("", service, 500, this).start();

        for(int i = 0; i < 15; ++i) {
            new GetThread("[" + Integer.toString(i) + "]", service, 0, this).start();
        }

        new GetVisibleThread("",  service, 50, this).start();

        String[] localeNames = {
            "en", "de"
        };
        for(int i = 0; i < localeNames.length; ++i) {
            String locale = localeNames[i];
            new GetDisplayThread("[" + locale + "]",
                                 stableService(),
                                 500,
                                 new ULocale(locale),
                                 this).start();
        }

        new UnregisterFactoryThread("", service, 500, this).start();

        // yoweee!!!
        runThreads(9500);
        if (PRINTSTATS) System.out.println(service.stats());
    }
}
