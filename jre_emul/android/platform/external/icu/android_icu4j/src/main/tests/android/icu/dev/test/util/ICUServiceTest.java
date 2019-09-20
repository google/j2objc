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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICULocaleService;
import android.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import android.icu.impl.ICULocaleService.LocaleKey;
import android.icu.impl.ICULocaleService.LocaleKeyFactory;
import android.icu.impl.ICUNotifier;
import android.icu.impl.ICURWLock;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUService;
import android.icu.impl.ICUService.Factory;
import android.icu.impl.ICUService.Key;
import android.icu.impl.ICUService.ServiceListener;
import android.icu.impl.ICUService.SimpleFactory;
import android.icu.impl.LocaleUtility;
import android.icu.util.ULocale;

public class ICUServiceTest extends TestFmwk
{
    private String lrmsg(String message, Object lhs, Object rhs) {
    return message + " lhs: " + lhs + " rhs: " + rhs;
    }

    public void confirmBoolean(String message, boolean val) {
    msg(message, val ? LOG : ERR, !val, true);
    }

    public void confirmEqual(String message, Object lhs, Object rhs) {
        msg(lrmsg(message, lhs, rhs), (lhs == null ? rhs == null : lhs.equals(rhs)) ? LOG : ERR, true, true);
    }

    public void confirmIdentical(String message, Object lhs, Object rhs) {
    msg(lrmsg(message, lhs, rhs), lhs == rhs ? LOG : ERR, true, true);
    }

    public void confirmIdentical(String message, int lhs, int rhs) {
    msg(message + " lhs: " + lhs + " rhs: " + rhs, lhs == rhs ? LOG : ERR, true, true);
    }

    /**
     * Convenience override of getDisplayNames(ULocale, Comparator, String) that
     * uses the current default ULocale as the locale, the default collator for
     * the locale as the comparator to sort the display names, and null for
     * the matchID.
     */
    public SortedMap getDisplayNames(ICUService service) {
        ULocale locale = ULocale.getDefault();
        Collator col = Collator.getInstance(locale.toLocale());
        return service.getDisplayNames(locale, col, null);
    }

    /**
     * Convenience override of getDisplayNames(ULocale, Comparator, String) that
     * uses the default collator for the locale as the comparator to
     * sort the display names, and null for the matchID.
     */
    public SortedMap getDisplayNames(ICUService service, ULocale locale) {
        Collator col = Collator.getInstance(locale.toLocale());
        return service.getDisplayNames(locale, col, null);
    }
    /**
     * Convenience override of getDisplayNames(ULocale, Comparator, String) that
     * uses the default collator for the locale as the comparator to
     * sort the display names.
     */
    public SortedMap getDisplayNames(ICUService service, ULocale locale, String matchID) {
        Collator col = Collator.getInstance(locale.toLocale());
        return service.getDisplayNames(locale, col, matchID);
    }

    // use locale keys
    static final class TestService extends ICUService {
        public TestService() {
            super("Test Service");
        }

    public Key createKey(String id) {
        return LocaleKey.createWithCanonicalFallback(id, null); // no fallback locale
    }
    }

    @Test
    public void TestAPI() {
    // create a service using locale keys,
    ICUService service = new TestService();

        logln("service name:" + service.getName());

    // register an object with one locale,
    // search for an object with a more specific locale
    // should return the original object
    Integer singleton0 = new Integer(0);
    service.registerObject(singleton0, "en_US");
    Object result = service.get("en_US_FOO");
    confirmIdentical("1) en_US_FOO -> en_US", result, singleton0);

    // register a new object with the more specific locale
    // search for an object with that locale
    // should return the new object
    Integer singleton1 = new Integer(1);
    service.registerObject(singleton1, "en_US_FOO");
    result = service.get("en_US_FOO");
    confirmIdentical("2) en_US_FOO -> en_US_FOO", result, singleton1);

    // search for an object that falls back to the first registered locale
    result = service.get("en_US_BAR");
    confirmIdentical("3) en_US_BAR -> en_US", result, singleton0);

    // get a list of the factories, should be two
    List factories = service.factories();
    confirmIdentical("4) factory size", factories.size(), 2);

    // register a new object with yet another locale
    // original factory list is unchanged
    Integer singleton2 = new Integer(2);
    service.registerObject(singleton2, "en");
    confirmIdentical("5) factory size", factories.size(), 2);

    // search for an object with the new locale
    // stack of factories is now en, en_US_FOO, en_US
    // search for en_US should still find en_US object
    result = service.get("en_US_BAR");
    confirmIdentical("6) en_US_BAR -> en_US", result, singleton0);

    // register a new object with an old id, should hide earlier factory using this id, but leave it there
    Integer singleton3 = new Integer(3);
    service.registerObject(singleton3, "en_US");
    factories = service.factories();
    confirmIdentical("9) factory size", factories.size(), 4);

    // should get data from that new factory
    result = service.get("en_US_BAR");
    confirmIdentical("10) en_US_BAR -> (3)", result, singleton3);

    // remove new factory
    // should have fewer factories again
    service.unregisterFactory((Factory)factories.get(0));
    factories = service.factories();
    confirmIdentical("11) factory size", factories.size(), 3);

    // should get original data again after remove factory
    result = service.get("en_US_BAR");
    confirmIdentical("12) en_US_BAR -> 0", result, singleton0);

    // shouldn't find unregistered ids
    result = service.get("foo");
    confirmIdentical("13) foo -> null", result, null);

    // should find non-canonical strings
    String[] resultID = new String[1];
    result = service.get("EN_us_fOo", resultID);
    confirmEqual("14) find non-canonical", resultID[0], "en_US_FOO");

    // should be able to register non-canonical strings and get them canonicalized
    service.registerObject(singleton3, "eN_ca_dUde");
    result = service.get("En_Ca_DuDe", resultID);
    confirmEqual("15) register non-canonical", resultID[0], "en_CA_DUDE");

    // should be able to register invisible factories, these will not
    // be visible by default, but if you know the secret password you
    // can still access these services...
    Integer singleton4 = new Integer(4);
    service.registerObject(singleton4, "en_US_BAR", false);
    result = service.get("en_US_BAR");
    confirmIdentical("17) get invisible", result, singleton4);

    // should not be able to locate invisible services
    Set ids = service.getVisibleIDs();
    confirmBoolean("18) find invisible", !ids.contains("en_US_BAR"));

    service.reset();
    // an anonymous factory than handles all ids
    {
        Factory factory = new Factory() {
            public Object create(Key key, ICUService unusedService) {
                return new ULocale(key.currentID());
            }

            public void updateVisibleIDs(Map unusedResult) {
            }

            public String getDisplayName(String id, ULocale l) {
                return null;
            }
        };
        service.registerFactory(factory);

        // anonymous factory will still handle the id
        result = service.get(ULocale.US.toString());
        confirmEqual("21) locale", result, ULocale.US);

        // still normalizes id
        result = service.get("EN_US_BAR");
        confirmEqual("22) locale", result, new ULocale("en_US_BAR"));

        // we can override for particular ids
        service.registerObject(singleton3, "en_US_BAR");
        result = service.get("en_US_BAR");
        confirmIdentical("23) override super", result, singleton3);

    }

    // empty service should not recognize anything
    service.reset();
    result = service.get("en_US");
    confirmIdentical("24) empty", result, null);

    // create a custom multiple key factory
    {
        String[] xids = { "en_US_VALLEY_GIRL",
                  "en_US_VALLEY_BOY",
                  "en_US_SURFER_GAL",
                  "en_US_SURFER_DUDE"
        };
        service.registerFactory(new TestLocaleKeyFactory(xids, "Later"));
    }

    // iterate over the visual ids returned by the multiple factory
    {
        Set vids = service.getVisibleIDs();
        Iterator iter = vids.iterator();
        int count = 0;
        while (iter.hasNext()) {
        ++count;
                String id = (String)iter.next();
        logln("  " + id + " --> " + service.get(id));
        }
        // four visible ids
        confirmIdentical("25) visible ids", count, 4);
    }

    // iterate over the display names
    {
        Map dids = getDisplayNames(service, ULocale.GERMANY);
        Iterator iter = dids.entrySet().iterator();
        int count = 0;
        while (iter.hasNext()) {
        ++count;
        Entry e = (Entry)iter.next();
        logln("  " + e.getKey() + " -- > " + e.getValue());
        }
        // four display names, in german
        confirmIdentical("26) display names", count, 4);
    }

    // no valid display name
    confirmIdentical("27) get display name", service.getDisplayName("en_US_VALLEY_GEEK"), null);

    {
        String name = service.getDisplayName("en_US_SURFER_DUDE", ULocale.US);
        confirmEqual("28) get display name", name, "English (United States, SURFER_DUDE)");
    }

    // register another multiple factory
    {
        String[] xids = {
        "en_US_SURFER", "en_US_SURFER_GAL", "en_US_SILICON", "en_US_SILICON_GEEK"
        };
        service.registerFactory(new TestLocaleKeyFactory(xids, "Rad dude"));
    }

    // this time, we have seven display names
        // Rad dude's surfer gal 'replaces' later's surfer gal
    {
        Map dids = getDisplayNames(service);
        Iterator iter = dids.entrySet().iterator();
        int count = 0;
        while (iter.hasNext()) {
        ++count;
        Entry e = (Entry)iter.next();
        logln("  " + e.getKey() + " --> " + e.getValue());
        }
        // seven display names, in spanish
        confirmIdentical("29) display names", count, 7);
    }

    // we should get the display name corresponding to the actual id
    // returned by the id we used.
    {
        String[] actualID = new String[1];
        String id = "en_us_surfer_gal";
        String gal = (String)service.get(id, actualID);
        if (gal != null) {
                logln("actual id: " + actualID[0]);
        String displayName = service.getDisplayName(actualID[0], ULocale.US);
        logln("found actual: " + gal + " with display name: " + displayName);
        confirmBoolean("30) found display name for actual", displayName != null);

        displayName = service.getDisplayName(id, ULocale.US);
        logln("found query: " + gal + " with display name: " + displayName);
        // this is no longer a bug, we want to return display names for anything
        // that a factory handles.  since we handle it, we should return a display
        // name.  see jb3549
        // confirmBoolean("31) found display name for query", displayName == null);
        } else {
        errln("30) service could not find entry for " + id);
        }

            // this should be handled by the 'dude' factory, since it overrides en_US_SURFER.
        id = "en_US_SURFER_BOZO";
        String bozo = (String)service.get(id, actualID);
        if (bozo != null) {
        String displayName = service.getDisplayName(actualID[0], ULocale.US);
        logln("found actual: " + bozo + " with display name: " + displayName);
        confirmBoolean("32) found display name for actual", displayName != null);

        displayName = service.getDisplayName(id, ULocale.US);
        logln("found actual: " + bozo + " with display name: " + displayName);
        // see above and jb3549
        // confirmBoolean("33) found display name for query", displayName == null);
        } else {
        errln("32) service could not find entry for " + id);
        }

            confirmBoolean("34) is default ", !service.isDefault());
    }

        /*
      // disallow hiding for now

      // hiding factory should obscure 'sublocales'
      {
      String[] xids = {
      "en_US_VALLEY", "en_US_SILICON"
      };
      service.registerFactory(new TestHidingFactory(xids, "hiding"));
      }

      {
      Map dids = service.getDisplayNames();
      Iterator iter = dids.entrySet().iterator();
      int count = 0;
      while (iter.hasNext()) {
      ++count;
      Entry e = (Entry)iter.next();
      logln("  " + e.getKey() + " -- > " + e.getValue());
      }
      confirmIdentical("35) hiding factory", count, 5);
      }
        */

    {
        Set xids = service.getVisibleIDs();
        Iterator iter = xids.iterator();
        while (iter.hasNext()) {
        String xid = (String)iter.next();
        logln(xid + "?  " + service.get(xid));
        }

        logln("valleygirl?  " + service.get("en_US_VALLEY_GIRL"));
        logln("valleyboy?   " + service.get("en_US_VALLEY_BOY"));
        logln("valleydude?  " + service.get("en_US_VALLEY_DUDE"));
        logln("surfergirl?  " + service.get("en_US_SURFER_GIRL"));
    }

    // resource bundle factory.
    service.reset();
    service.registerFactory(new ICUResourceBundleFactory());

    // list all of the resources
    {
            logln("all visible ids: " + service.getVisibleIDs());
            /*
          Set xids = service.getVisibleIDs();
          StringBuffer buf = new StringBuffer("{");
          boolean notfirst = false;
          Iterator iter = xids.iterator();
          while (iter.hasNext()) {
          String xid = (String)iter.next();
          if (notfirst) {
          buf.append(", ");
          } else {
          notfirst = true;
          }
          buf.append(xid);
          }
          buf.append("}");
          logln(buf.toString());
            */
    }

        // list only the resources for es, default locale
        // since we're using the default Key, only "es" is matched
        {
            logln("visible ids for es locale: " + service.getVisibleIDs("es"));
        }

        // list only the spanish display names for es, spanish collation order
        // since we're using the default Key, only "es" is matched
        {
            logln("display names: " + getDisplayNames(service, new ULocale("es"), "es"));
        }

        // list the display names in reverse order
        {
            logln("display names in reverse order: " +
                  service.getDisplayNames(ULocale.US, new Comparator() {
                          public int compare(Object lhs, Object rhs) {
                              return -String.CASE_INSENSITIVE_ORDER.compare((String)lhs, (String)rhs);
                          }
                      }));
        }

    // get all the display names of these resources
    // this should be fast since the display names were cached.
    {
            logln("service display names for de_DE");
        Map names = getDisplayNames(service, new ULocale("de_DE"));
        StringBuffer buf = new StringBuffer("{");
        Iterator iter = names.entrySet().iterator();
        while (iter.hasNext()) {
        Entry e = (Entry)iter.next();
        String name = (String)e.getKey();
        String id = (String)e.getValue();
        buf.append("\n   " + name + " --> " + id);
        }
        buf.append("\n}");
        logln(buf.toString());
    }

        CalifornioLanguageFactory califactory = new CalifornioLanguageFactory();
        service.registerFactory(califactory);
    // get all the display names of these resources
    {
            logln("californio language factory");
        StringBuffer buf = new StringBuffer("{");
            String[] idNames = {
                CalifornioLanguageFactory.californio,
        CalifornioLanguageFactory.valley,
        CalifornioLanguageFactory.surfer,
        CalifornioLanguageFactory.geek
            };
            for (int i = 0; i < idNames.length; ++i) {
                String idName = idNames[i];
                buf.append("\n  --- " + idName + " ---");
                Map names = getDisplayNames(service, new ULocale(idName));
                Iterator iter = names.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry e = (Entry)iter.next();
                    String name = (String)e.getKey();
                    String id = (String)e.getValue();
                    buf.append("\n    " + name + " --> " + id);
                }
        }
        buf.append("\n}");
        logln(buf.toString());
    }

    // test notification
    // simple registration
    {
            logln("simple registration notification");
        ICULocaleService ls = new ICULocaleService();
        ServiceListener l1 = new ServiceListener() {
            private int n;
            public void serviceChanged(ICUService s) {
            logln("listener 1 report " + n++ + " service changed: " + s);
            }
        };
        ls.addListener(l1);
        ServiceListener l2 = new ServiceListener() {
            private int n;
            public void serviceChanged(ICUService s) {
            logln("listener 2 report " + n++ + " service changed: " + s);
            }
        };
        ls.addListener(l2);
        logln("registering foo... ");
        ls.registerObject("Foo", "en_FOO");
        logln("registering bar... ");
        ls.registerObject("Bar", "en_BAR");
        logln("getting foo...");
        logln((String)ls.get("en_FOO"));
        logln("removing listener 2...");
        ls.removeListener(l2);
        logln("registering baz...");
        ls.registerObject("Baz", "en_BAZ");
        logln("removing listener 1");
        ls.removeListener(l1);
        logln("registering burp...");
        ls.registerObject("Burp", "en_BURP");

        // should only get one notification even if register multiple times
        logln("... trying multiple registration");
        ls.addListener(l1);
        ls.addListener(l1);
        ls.addListener(l1);
        ls.addListener(l2);
        ls.registerObject("Foo", "en_FOO");
        logln("... registered foo");

        // since in a separate thread, we can callback and not deadlock
        ServiceListener l3 = new ServiceListener() {
            private int n;
            public void serviceChanged(ICUService s) {
            logln("listener 3 report " + n++ + " service changed...");
            if (s.get("en_BOINK") == null) { // don't recurse on ourselves!!!
                logln("registering boink...");
                s.registerObject("boink", "en_BOINK");
            }
            }
        };
        ls.addListener(l3);
        logln("registering boo...");
        ls.registerObject("Boo", "en_BOO");
        logln("...done");

        try {
        Thread.sleep(100);
        }
        catch (InterruptedException e) {
        }
    }
    }

    static class TestLocaleKeyFactory extends LocaleKeyFactory {
    protected final Set ids;
    protected final String factoryID;

    public TestLocaleKeyFactory(String[] ids, String factoryID) {
            super(VISIBLE, factoryID);

        this.ids = Collections.unmodifiableSet(new HashSet(Arrays.asList(ids)));
            this.factoryID = factoryID + ": ";
    }

    protected Object handleCreate(ULocale loc, int kind, ICUService service) {
            return factoryID + loc.toString();
    }

    protected Set getSupportedIDs() {
            return ids;
    }
    }

    /*
      // Disallow hiding for now since it causes gnarly problems, like
      // how do you localize the hidden (but still exported) names.

      static class TestHidingFactory implements ICUService.Factory {
      protected final String[] ids;
      protected final String factoryID;

      public TestHidingFactory(String[] ids) {
      this(ids, "Hiding");
      }

      public TestHidingFactory(String[] ids, String factoryID) {
      this.ids = (String[])ids.clone();

      if (factoryID == null || factoryID.length() == 0) {
      this.factoryID = "";
      } else {
      this.factoryID = factoryID + ": ";
      }
      }

      public Object create(Key key, ICUService service) {
      for (int i = 0; i < ids.length; ++i) {
      if (LocaleUtility.isFallbackOf(ids[i], key.currentID())) {
      return factoryID + key.canonicalID();
      }
      }
      return null;
      }

      public void updateVisibleIDs(Map result) {
      for (int i = 0; i < ids.length; ++i) {
      String id = ids[i];
      Iterator iter = result.keySet().iterator();
      while (iter.hasNext()) {
      if (LocaleUtility.isFallbackOf(id, (String)iter.next())) {
      iter.remove();
      }
      }
      result.put(id, this);
      }
      }

      public String getDisplayName(String id, ULocale locale) {
      return factoryID + new ULocale(id).getDisplayName(locale);
      }
      }
    */

    static class CalifornioLanguageFactory extends ICUResourceBundleFactory {
    public static String californio = "en_US_CA";
    public static String valley = californio + "_VALLEY";
    public static String surfer = californio + "_SURFER";
    public static String geek = californio + "_GEEK";
        public static Set supportedIDs;
        static {
            HashSet result = new HashSet();
            result.addAll(ICUResourceBundle.getAvailableLocaleNameSet());
        result.add(californio);
        result.add(valley);
        result.add(surfer);
        result.add(geek);
            supportedIDs = Collections.unmodifiableSet(result);
        }

    public Set getSupportedIDs() {
            return supportedIDs;
    }

    public String getDisplayName(String id, ULocale locale) {
        String prefix = "";
        String suffix = "";
        String ls = locale.toString();
        if (LocaleUtility.isFallbackOf(californio, ls)) {
        if (ls.equalsIgnoreCase(valley)) {
            prefix = "Like, you know, it's so totally ";
        } else if (ls.equalsIgnoreCase(surfer)) {
            prefix = "Dude, its ";
        } else if (ls.equalsIgnoreCase(geek)) {
            prefix = "I'd estimate it's approximately ";
        } else {
            prefix = "Huh?  Maybe ";
        }
        }
        if (LocaleUtility.isFallbackOf(californio, id)) {
        if (id.equalsIgnoreCase(valley)) {
            suffix = "like the Valley, you know?  Let's go to the mall!";
        } else if (id.equalsIgnoreCase(surfer)) {
            suffix = "time to hit those gnarly waves, Dude!!!";
        } else if (id.equalsIgnoreCase(geek)) {
            suffix = "all systems go.  T-Minus 9, 8, 7...";
        } else {
            suffix = "No Habla Englais";
        }
        } else {
        suffix = super.getDisplayName(id, locale);
        }

        return prefix + suffix;
    }
    }

    @Test
    public void TestLocale() {
    ICULocaleService service = new ICULocaleService("test locale");
    service.registerObject("root", ULocale.ROOT);
    service.registerObject("german", "de");
    service.registerObject("german_Germany", ULocale.GERMANY);
    service.registerObject("japanese", "ja");
    service.registerObject("japanese_Japan", ULocale.JAPAN);

    Object target = service.get("de_US");
    confirmEqual("test de_US", "german", target);

        ULocale de = new ULocale("de");
        ULocale de_US = new ULocale("de_US");

        target = service.get(de_US);
    confirmEqual("test de_US 2", "german", target);

        target = service.get(de_US, LocaleKey.KIND_ANY);
    confirmEqual("test de_US 3", "german", target);

        target = service.get(de_US, 1234);
    confirmEqual("test de_US 4", "german", target);

        ULocale[] actualReturn = new ULocale[1];
        target = service.get(de_US, actualReturn);
        confirmEqual("test de_US 5", "german", target);
        confirmEqual("test de_US 6", actualReturn[0], de);

        actualReturn[0] = null;
        target = service.get(de_US, LocaleKey.KIND_ANY, actualReturn);
        confirmEqual("test de_US 7", actualReturn[0], de);

        actualReturn[0] = null;
        target = service.get(de_US, 1234, actualReturn);
    confirmEqual("test de_US 8", "german", target);
        confirmEqual("test de_US 9", actualReturn[0], de);

        service.registerObject("one/de_US", de_US, 1);
        service.registerObject("two/de_US", de_US, 2);

        target = service.get(de_US, 1);
        confirmEqual("test de_US kind 1", "one/de_US", target);

        target = service.get(de_US, 2);
        confirmEqual("test de_US kind 2", "two/de_US", target);

        target = service.get(de_US);
        confirmEqual("test de_US kind 3", "german", target);

        LocaleKey lkey = LocaleKey.createWithCanonicalFallback("en", null, 1234);
        logln("lkey prefix: " + lkey.prefix());
        logln("lkey descriptor: " + lkey.currentDescriptor());
        logln("lkey current locale: " + lkey.currentLocale());

        lkey.fallback();
        logln("lkey descriptor 2: " + lkey.currentDescriptor());

        lkey.fallback();
        logln("lkey descriptor 3: " + lkey.currentDescriptor());

    target = service.get("za_PPP");
    confirmEqual("test zappp", "root", target);

    ULocale loc = ULocale.getDefault();
    ULocale.setDefault(ULocale.JAPANESE);
    target = service.get("za_PPP");
    confirmEqual("test with ja locale", "japanese", target);

    Set ids = service.getVisibleIDs();
    for (Iterator iter = ids.iterator(); iter.hasNext();) {
        logln("id: " + iter.next());
    }

    ULocale.setDefault(loc);
    ids = service.getVisibleIDs();
    for (Iterator iter = ids.iterator(); iter.hasNext();) {
        logln("id: " + iter.next());
    }

    target = service.get("za_PPP");
    confirmEqual("test with en locale", "root", target);

        ULocale[] locales = service.getAvailableULocales();
        confirmIdentical("test available locales", locales.length, 6);
        logln("locales: ");
        for (int i = 0; i < locales.length; ++i) {
            log("\n  [" + i + "] " + locales[i]);
        }
        logln(" ");

        service.registerFactory(new ICUResourceBundleFactory());
        target = service.get(ULocale.JAPAN);

        {
            int n = 0;
            List factories = service.factories();
            Iterator iter = factories.iterator();
            while (iter.hasNext()) {
                logln("[" + n++ + "] " + iter.next());
            }
        }

        // list only the english display names for es, in reverse order
        // since we're using locale keys, we should get all and only the es locales
        // hmmm, the default toString function doesn't print in sorted order for TreeMap
        {
            SortedMap map = service.getDisplayNames(ULocale.US,
                            new Comparator() {
                                public int compare(Object lhs, Object rhs) {
                                return -String.CASE_INSENSITIVE_ORDER.compare((String)lhs, (String)rhs);
                                }
                            },
                            "es");

            logln("es display names in reverse order " + map);
        }
    }

    @Test
    public void TestWrapFactory() {
        final String greeting = "Hello There";
        final String greetingID = "greeting";

        ICUService service = new ICUService("wrap");
        service.registerObject(greeting, greetingID);

        logln("test one: " + service.get(greetingID));

        class WrapFactory implements Factory {
            public Object create(Key key, ICUService serviceArg) {
                if (key.currentID().equals(greetingID)) {
                    Object previous = serviceArg.getKey(key, null, this);
                    return "A different greeting: \"" + previous + "\"";
                }
                return null;
            }

            public void updateVisibleIDs(Map result) {
                result.put("greeting", this);
            }

            public String getDisplayName(String id, ULocale locale) {
                return "wrap '" + id + "'";
            }
        }
        service.registerFactory(new WrapFactory());

        confirmEqual("wrap test: ", service.get(greetingID), "A different greeting: \"" + greeting + "\"");
    }

    // misc coverage tests
    @Test
    public void TestCoverage() {
    // Key
    Key key = new Key("foobar");
    logln("ID: " + key.id());
    logln("canonicalID: " + key.canonicalID());
    logln("currentID: " + key.currentID());
    logln("has fallback: " + key.fallback());

    // SimpleFactory
    Object obj = new Object();
    SimpleFactory sf = new SimpleFactory(obj, "object");
    try {
        sf = new SimpleFactory(null, null);
        errln("didn't throw exception");
    }
    catch (IllegalArgumentException e) {
        logln("OK: " + e.getMessage());
    }
    catch (Exception e) {
        errln("threw wrong exception" + e);
    }
    logln(sf.getDisplayName("object", null));

    // ICUService
    ICUService service = new ICUService();
    service.registerFactory(sf);

    try {
        service.get(null, null);
        errln("didn't throw exception");
    }
    catch (NullPointerException e) {
        logln("OK: " + e.getMessage());
    }
        /*
      catch (Exception e) {
      errln("threw wrong exception" + e);
      }
        */
    try {
        service.registerFactory(null);
        errln("didn't throw exception");
    }
    catch (NullPointerException e) {
        logln("OK: " + e.getMessage());
    }
    catch (Exception e) {
        errln("threw wrong exception" + e);
    }

    try {
        service.unregisterFactory(null);
        errln("didn't throw exception");
    }
    catch (NullPointerException e) {
        logln("OK: " + e.getMessage());
    }
    catch (Exception e) {
        errln("threw wrong exception" + e);
    }

    logln("object is: " + service.get("object"));

    logln("stats: " + service.stats());

    // ICURWLock

    ICURWLock rwlock = new ICURWLock();
    rwlock.resetStats();

    rwlock.acquireRead();
    rwlock.releaseRead();

    rwlock.acquireWrite();
    rwlock.releaseWrite();
    logln("stats: " + rwlock.getStats());
    logln("stats: " + rwlock.clearStats());
    rwlock.acquireRead();
    rwlock.releaseRead();
    rwlock.acquireWrite();
    rwlock.releaseWrite();
    logln("stats: " + rwlock.getStats());

    try {
        rwlock.releaseRead();
        errln("no error thrown");
    }
    catch (Exception e) {
        logln("OK: " + e.getMessage());
    }

    try {
        rwlock.releaseWrite();
        errln("no error thrown");
    }
    catch (Exception e) {
        logln("OK: " + e.getMessage());
    }

        // ICULocaleService

    // LocaleKey

    // LocaleKey lkey = LocaleKey.create("en_US", "ja_JP");
    // lkey = LocaleKey.create(null, null);
    LocaleKey lkey = LocaleKey.createWithCanonicalFallback("en_US", "ja_JP");
        logln("lkey: " + lkey);

        lkey = LocaleKey.createWithCanonicalFallback(null, null);
        logln("lkey from null,null: " + lkey);

    // LocaleKeyFactory
    LocaleKeyFactory lkf = new LKFSubclass(false);
        logln("lkf: " + lkf);
    logln("obj: " + lkf.create(lkey, null));
    logln(lkf.getDisplayName("foo", null));
    logln(lkf.getDisplayName("bar", null));
    lkf.updateVisibleIDs(new HashMap());

    LocaleKeyFactory invisibleLKF = new LKFSubclass(false);
    logln("obj: " + invisibleLKF.create(lkey, null));
    logln(invisibleLKF.getDisplayName("foo", null));
    logln(invisibleLKF.getDisplayName("bar", null));
    invisibleLKF.updateVisibleIDs(new HashMap());

    // ResourceBundleFactory
    ICUResourceBundleFactory rbf = new ICUResourceBundleFactory();
    logln("RB: " + rbf.create(lkey, null));

    // ICUNotifier
    ICUNotifier nf = new ICUNSubclass();
    try {
        nf.addListener(null);
        errln("added null listener");
    }
    catch (NullPointerException e) {
        logln(e.getMessage());
    }
    catch (Exception e) {
        errln("got wrong exception");
    }

    try {
        nf.addListener(new WrongListener());
        errln("added wrong listener");
    }
    catch (IllegalStateException e) {
        logln(e.getMessage());
    }
    catch (Exception e) {
        errln("got wrong exception");
    }

    try {
        nf.removeListener(null);
        errln("removed null listener");
    }
    catch (NullPointerException e) {
        logln(e.getMessage());
    }
    catch (Exception e) {
        errln("got wrong exception");
    }

    nf.removeListener(new MyListener());
    nf.notifyChanged();
    nf.addListener(new MyListener());
    nf.removeListener(new MyListener());
    }

    static class MyListener implements EventListener {
    }

    static class WrongListener implements EventListener {
    }

    static class ICUNSubclass extends ICUNotifier {
        public boolean acceptsListener(EventListener l) {
            return l instanceof MyListener;
        }
    
        // not used, just needed to implement abstract base
        public void notifyListener(EventListener l) {
        }
    }

    static class LKFSubclass extends LocaleKeyFactory {
    LKFSubclass(boolean visible) {
        super(visible ? VISIBLE : INVISIBLE);
    }

    protected Set getSupportedIDs() {
            return Collections.EMPTY_SET;
    }
    }
}
