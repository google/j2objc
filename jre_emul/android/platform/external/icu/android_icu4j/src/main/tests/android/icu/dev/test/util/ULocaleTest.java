/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **********************************************************************
 * Copyright (c) 2004-2016, International Business Machines
 * Corporation and others.  All Rights Reserved.
 **********************************************************************
 * Author: Alan Liu
 * Created: January 14 2004
 * Since: ICU 2.8
 **********************************************************************
 */
package android.icu.dev.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.TestUtil.JavaVendor;
import android.icu.lang.UCharacter;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.DisplayContext;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import android.icu.text.NumberFormat;
import android.icu.text.NumberFormat.SimpleNumberFormatFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.IllformedLocaleException;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Minimize;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;

public class ULocaleTest extends TestFmwk {

    // Ticket #8078 and #11674
    private static final boolean JAVA7_OR_LATER =
            TestUtil.getJavaVendor() == JavaVendor.Android || TestUtil.getJavaVersion() >= 7;

    @Test
    public void TestCalendar() {
        // TODO The CalendarFactory mechanism is not public,
        // so we can't test it yet.  If it becomes public,
        // enable this code.

        // class CFactory implements CalendarFactory {
        //     Locale loc;
        //     Calendar proto;
        //     public CFactory(Locale locale, Calendar prototype) {
        //         loc = locale;
        //         proto = prototype;
        //     }
        //     public Calendar create(TimeZone tz, Locale locale) {
        //         // ignore tz -- not relevant to this test
        //         return locale.equals(loc) ?
        //             (Calendar) proto.clone() : null;
        //     }
        //     public String factoryName() {
        //         return "CFactory";
        //     }
        // };

        checkService("en_US_BROOKLYN", new ServiceFacade() {
            @Override
            public Object create(ULocale req) {
                return Calendar.getInstance(req);
            }
            // }, null, new Registrar() {
            //     public Object register(ULocale loc, Object prototype) {
            //         CFactory f = new CFactory(loc, (Calendar) prototype);
            //         return Calendar.register(f, loc);
            //     }
            //     public boolean unregister(Object key) {
            //         return Calendar.unregister(key);
            //     }
        });
    }

    // Currency getLocale API is obsolete in 3.2.  Since it now returns ULocale.ROOT,
    // and this is not equal to the requested locale zh_TW_TAIPEI, the
    // checkService call would always fail.  So we now omit the test.
    /*
    @Test
    public void TestCurrency() {
        checkService("zh_TW_TAIPEI", new ServiceFacade() {
                public Object create(ULocale req) {
                    return Currency.getInstance(req);
                }
            }, null, new Registrar() {
                    public Object register(ULocale loc, Object prototype) {
                        return Currency.registerInstance((Currency) prototype, loc);
                    }
                    public boolean unregister(Object key) {
                        return Currency.unregister(key);
                    }
                });
    }
     */

    @Test
    public void TestDateFormat() {
        checkService("de_CH_ZURICH", new ServiceFacade() {
            @Override
            public Object create(ULocale req) {
                return DateFormat.getDateInstance(DateFormat.DEFAULT, req);
            }
        }, new Subobject() {
            @Override
            public Object get(Object parent) {
                return ((SimpleDateFormat) parent).getDateFormatSymbols();
            }
        }, null);
    }

    @Test
    public void TestNumberFormat() {
        class NFactory extends SimpleNumberFormatFactory {
            NumberFormat proto;
            ULocale locale;
            public NFactory(ULocale loc, NumberFormat fmt) {
                super(loc);
                this.locale = loc;
                this.proto = fmt;
            }
            @Override
            public NumberFormat createFormat(ULocale loc, int formatType) {
                return (NumberFormat) (locale.equals(loc) ?
                        proto.clone() : null);
            }
        }

        checkService("fr_FR_NICE", new ServiceFacade() {
            @Override
            public Object create(ULocale req) {
                return NumberFormat.getInstance(req);
            }
        }, new Subobject() {
            @Override
            public Object get(Object parent) {
                return ((DecimalFormat) parent).getDecimalFormatSymbols();
            }
        }, new Registrar() {
            @Override
            public Object register(ULocale loc, Object prototype) {
                NFactory f = new NFactory(loc, (NumberFormat) prototype);
                return NumberFormat.registerFactory(f);
            }
            @Override
            public boolean unregister(Object key) {
                return NumberFormat.unregister(key);
            }
        });
    }

    @Test
    public void TestSetULocaleKeywords() {
        ULocale uloc = new ULocale("en_Latn_US");
        uloc = uloc.setKeywordValue("Foo", "FooValue");
        if (!"en_Latn_US@foo=FooValue".equals(uloc.getName())) {
            errln("failed to add foo keyword, got: " + uloc.getName());
        }
        uloc = uloc.setKeywordValue("Bar", "BarValue");
        if (!"en_Latn_US@bar=BarValue;foo=FooValue".equals(uloc.getName())) {
            errln("failed to add bar keyword, got: " + uloc.getName());
        }
        uloc = uloc.setKeywordValue("BAR", "NewBarValue");
        if (!"en_Latn_US@bar=NewBarValue;foo=FooValue".equals(uloc.getName())) {
            errln("failed to change bar keyword, got: " + uloc.getName());
        }
        uloc = uloc.setKeywordValue("BaR", null);
        if (!"en_Latn_US@foo=FooValue".equals(uloc.getName())) {
            errln("failed to delete bar keyword, got: " + uloc.getName());
        }
        uloc = uloc.setKeywordValue(null, null);
        if (!"en_Latn_US".equals(uloc.getName())) {
            errln("failed to delete all keywords, got: " + uloc.getName());
        }
    }

    /*
     * ticket#5060
     */
    @Test
    public void TestJavaLocaleCompatibility() {
        Locale backupDefault = Locale.getDefault();
        ULocale orgUlocDefault = ULocale.getDefault();

        // Java Locale for ja_JP with Japanese calendar
        Locale jaJPJP = new Locale("ja", "JP", "JP");
        Locale jaJP = new Locale("ja", "JP");
        // Java Locale for th_TH with Thai digits
        Locale thTHTH = new Locale("th", "TH", "TH");

        Calendar cal = Calendar.getInstance(jaJPJP);
        String caltype = cal.getType();
        if (!caltype.equals("japanese")) {
            errln("FAIL: Invalid calendar type: " + caltype + " /expected: japanese");
        }

        cal = Calendar.getInstance(jaJP);
        caltype = cal.getType();
        if (!caltype.equals("gregorian")) {
            errln("FAIL: Invalid calendar type: " + caltype + " /expected: gregorian");
        }

        // Default locale
        Locale.setDefault(jaJPJP);
        ULocale defUloc = ULocale.getDefault();
        if (JAVA7_OR_LATER) {
            if (!defUloc.toString().equals("ja_JP_JP@calendar=japanese")) {
                errln("FAIL: Invalid default ULocale: " + defUloc + " /expected: ja_JP_JP@calendar=japanese");
            }
        } else {
            if (!defUloc.toString().equals("ja_JP@calendar=japanese")) {
                errln("FAIL: Invalid default ULocale: " + defUloc + " /expected: ja_JP@calendar=japanese");
            }
        }
        // Check calendar type
        cal = Calendar.getInstance();
        caltype = cal.getType();
        if (!caltype.equals("japanese")) {
            errln("FAIL: Invalid calendar type: " + caltype + " /expected: japanese");
        }
        Locale.setDefault(backupDefault);

        // Set default via ULocale
        ULocale ujaJP_calJP = new ULocale("ja_JP@calendar=japanese");
        ULocale.setDefault(ujaJP_calJP);
        if (!JAVA7_OR_LATER && !Locale.getDefault().equals(jaJPJP)) {
            errln("FAIL: ULocale#setDefault failed to set Java Locale ja_JP_JP /actual: " + Locale.getDefault());
        }
        // Ticket#6672 - missing keywords
        defUloc = ULocale.getDefault();
        if (!defUloc.equals(ujaJP_calJP)) {
            errln("FAIL: ULocale#getDefault returned " + defUloc + " /expected: ja_JP@calendar=japanese");
        }
        // Set a incompatible base locale via Locale#setDefault
        Locale.setDefault(Locale.US);
        defUloc = ULocale.getDefault();
        if (defUloc.equals(ujaJP_calJP)) {
            errln("FAIL: ULocale#getDefault returned " + defUloc + " /expected: " + ULocale.forLocale(Locale.US));
        }

        Locale.setDefault(backupDefault);

        // We also want to map ICU locale ja@calendar=japanese to Java ja_JP_JP
        ULocale.setDefault(new ULocale("ja@calendar=japanese"));
        if (!JAVA7_OR_LATER && !Locale.getDefault().equals(jaJPJP)) {
            errln("FAIL: ULocale#setDefault failed to set Java Locale ja_JP_JP /actual: " + Locale.getDefault());
        }
        Locale.setDefault(backupDefault);

        // Java no_NO_NY
        Locale noNONY = new Locale("no", "NO", "NY");
        Locale.setDefault(noNONY);
        defUloc = ULocale.getDefault();
        if (!defUloc.toString().equals("nn_NO")) {
            errln("FAIL: Invalid default ULocale: " + defUloc + " /expected: nn_NO");
        }
        Locale.setDefault(backupDefault);

        // Java th_TH_TH -> ICU th_TH@numbers=thai
        ULocale.setDefault(new ULocale("th@numbers=thai"));
        if (!JAVA7_OR_LATER && !Locale.getDefault().equals(thTHTH)) {
            errln("FAIL: ULocale#setDefault failed to set Java Locale th_TH_TH /actual: " + Locale.getDefault());
        }
        Locale.setDefault(backupDefault);

        // Set default via ULocale
        ULocale.setDefault(new ULocale("nn_NO"));
        if (!JAVA7_OR_LATER && !Locale.getDefault().equals(noNONY)) {
            errln("FAIL: ULocale#setDefault failed to set Java Locale no_NO_NY /actual: " + Locale.getDefault());
        }
        Locale.setDefault(backupDefault);

        // We also want to map ICU locale nn to Java no_NO_NY
        ULocale.setDefault(new ULocale("nn"));
        if (!JAVA7_OR_LATER && !Locale.getDefault().equals(noNONY)) {
            errln("FAIL: ULocale#setDefault failed to set Java Locale no_NO_NY /actual: " + Locale.getDefault());
        }
        Locale.setDefault(backupDefault);

        // Make sure default ULocale is restored
        if (!ULocale.getDefault().equals(orgUlocDefault)) {
            errln("FAIL: Original default ULocale is not restored - " + ULocale.getDefault() + ", expected(orginal) - " + orgUlocDefault);
        }
    }

    // ================= Infrastructure =================

    /**
     * Compare two locale IDs.  If they are equal, return 0.  If `string'
     * starts with `prefix' plus an additional element, that is, string ==
     * prefix + '_' + x, then return 1.  Otherwise return a value < 0.
     */
    static int loccmp(String string, String prefix) {
        int slen = string.length(),
                plen = prefix.length();
        /* 'root' is "less than" everything */
        if (prefix.equals("root")) {
            return string.equals("root") ? 0 : 1;
        }
        // ON JAVA (only -- not on C -- someone correct me if I'm wrong)
        // consider "" to be an alternate name for "root".
        if (plen == 0) {
            return slen == 0 ? 0 : 1;
        }
        if (!string.startsWith(prefix)) return -1; /* mismatch */
        if (slen == plen) return 0;
        if (string.charAt(plen) == '_') return 1;
        return -2; /* false match, e.g. "en_USX" cmp "en_US" */
    }

    /**
     * Check the relationship between requested locales, and report problems.
     * The caller specifies the expected relationships between requested
     * and valid (expReqValid) and between valid and actual (expValidActual).
     * Possible values are:
     * "gt" strictly greater than, e.g., en_US > en
     * "ge" greater or equal,      e.g., en >= en
     * "eq" equal,                 e.g., en == en
     */
    void checklocs(String label,
            String req,
            Locale validLoc,
            Locale actualLoc,
            String expReqValid,
            String expValidActual) {
        String valid = validLoc.toString();
        String actual = actualLoc.toString();
        int reqValid = loccmp(req, valid);
        int validActual = loccmp(valid, actual);
        boolean reqOK = (expReqValid.equals("gt") && reqValid > 0) ||
                (expReqValid.equals("ge") && reqValid >= 0) ||
                (expReqValid.equals("eq") && reqValid == 0);
        boolean valOK = (expValidActual.equals("gt") && validActual > 0) ||
                (expValidActual.equals("ge") && validActual >= 0) ||
                (expValidActual.equals("eq") && validActual == 0);
        if (reqOK && valOK) {
            logln("Ok: " + label + "; req=" + req + ", valid=" + valid +
                    ", actual=" + actual);
        } else {
            errln("FAIL: " + label + "; req=" + req + ", valid=" + valid +
                    ", actual=" + actual +
                    (reqOK ? "" : "\n  req !" + expReqValid + " valid") +
                    (valOK ? "" : "\n  val !" + expValidActual + " actual"));
        }
    }

    /**
     * Interface used by checkService defining a protocol to create an
     * object, given a requested locale.
     */
    interface ServiceFacade {
        Object create(ULocale requestedLocale);
    }

    /**
     * Interface used by checkService defining a protocol to get a
     * contained subobject, given its parent object.
     */
    interface Subobject {
        Object get(Object parent);
    }

    /**
     * Interface used by checkService defining a protocol to register
     * and unregister a service object prototype.
     */
    interface Registrar {
        Object register(ULocale loc, Object prototype);
        boolean unregister(Object key);
    }

    /**
     * Use reflection to call getLocale() on the given object to
     * determine both the valid and the actual locale.  Verify these
     * for correctness.
     */
    void checkObject(String requestedLocale, Object obj,
            String expReqValid, String expValidActual) {
        Class[] getLocaleParams = new Class[] { ULocale.Type.class };
        try {
            Class cls = obj.getClass();
            Method getLocale = cls.getMethod("getLocale", getLocaleParams);
            ULocale valid = (ULocale) getLocale.invoke(obj, new Object[] {
                    ULocale.VALID_LOCALE });
            ULocale actual = (ULocale) getLocale.invoke(obj, new Object[] {
                    ULocale.ACTUAL_LOCALE });
            checklocs(cls.getName(), requestedLocale,
                    valid.toLocale(), actual.toLocale(),
                    expReqValid, expValidActual);
        }

        // Make the following exceptions _specific_ -- do not
        // catch(Exception), since that will catch the exception
        // that errln throws.
        catch(NoSuchMethodException e1) {
            // no longer an error, Currency has no getLocale
            // errln("FAIL: reflection failed: " + e1);
        } catch(SecurityException e2) {
            errln("FAIL: reflection failed: " + e2);
        } catch(IllegalAccessException e3) {
            errln("FAIL: reflection failed: " + e3);
        } catch(IllegalArgumentException e4) {
            errln("FAIL: reflection failed: " + e4);
        } catch(InvocationTargetException e5) {
            // no longer an error, Currency has no getLocale
            // errln("FAIL: reflection failed: " + e5);
        }
    }

    /**
     * Verify the correct getLocale() behavior for the given service.
     * @param requestedLocale the locale to request.  This MUST BE
     * FAKE.  In other words, it should be something like
     * en_US_FAKEVARIANT so this method can verify correct fallback
     * behavior.
     * @param svc a factory object that can create the object to be
     * tested.  This isn't necessary here (one could just pass in the
     * object) but is required for the overload of this method that
     * takes a Registrar.
     */
    void checkService(String requestedLocale, ServiceFacade svc) {
        checkService(requestedLocale, svc, null, null);
    }

    /**
     * Verify the correct getLocale() behavior for the given service.
     * @param requestedLocale the locale to request.  This MUST BE
     * FAKE.  In other words, it should be something like
     * en_US_FAKEVARIANT so this method can verify correct fallback
     * behavior.
     * @param svc a factory object that can create the object to be
     * tested.
     * @param sub an object that can be used to retrieve a subobject
     * which should also be tested.  May be null.
     * @param reg an object that supplies the registration and
     * unregistration functionality to be tested.  May be null.
     */
    void checkService(String requestedLocale, ServiceFacade svc,
            Subobject sub, Registrar reg) {
        ULocale req = new ULocale(requestedLocale);
        Object obj = svc.create(req);
        checkObject(requestedLocale, obj, "gt", "ge");
        if (sub != null) {
            Object subobj = sub.get(obj);
            checkObject(requestedLocale, subobj, "gt", "ge");
        }
        if (reg != null) {
            logln("Info: Registering service");
            Object key = reg.register(req, obj);
            Object objReg = svc.create(req);
            checkObject(requestedLocale, objReg, "eq", "eq");
            if (sub != null) {
                Object subobj = sub.get(obj);
                // Assume subobjects don't come from services, so
                // their metadata should be structured normally.
                checkObject(requestedLocale, subobj, "gt", "ge");
            }
            logln("Info: Unregistering service");
            if (!reg.unregister(key)) {
                errln("FAIL: unregister failed");
            }
            Object objUnreg = svc.create(req);
            checkObject(requestedLocale, objUnreg, "gt", "ge");
        }
    }
    private static final int LOCALE_SIZE = 9;
    private static final String[][] rawData2 = new String[][]{
        /* language code */
        {   "en",   "fr",   "ca",   "el",   "no",   "zh",   "de",   "es",  "ja"    },
        /* script code */
        {   "",     "",     "",     "",     "",     "Hans", "", "", ""  },
        /* country code */
        {   "US",   "FR",   "ES",   "GR",   "NO",   "CN", "DE", "", "JP"    },
        /* variant code */
        {   "",     "",     "",     "",     "NY",   "", "", "", ""      },
        /* full name */
        {   "en_US",    "fr_FR",    "ca_ES",
            "el_GR",    "no_NO_NY", "zh_Hans_CN",
            "de_DE@collation=phonebook", "es@collation=traditional",  "ja_JP@calendar=japanese" },
            /* ISO-3 language */
            {   "eng",  "fra",  "cat",  "ell",  "nor",  "zho", "deu", "spa", "jpn"   },
            /* ISO-3 country */
            {   "USA",  "FRA",  "ESP",  "GRC",  "NOR",  "CHN", "DEU", "", "JPN"   },
            /* LCID */
            {   "409", "40c", "403", "408", "814",  "804", "407", "a", "411"     },

            /* display language (English) */
            {   "English",  "French",   "Catalan", "Greek",    "Norwegian", "Chinese", "German", "Spanish", "Japanese"    },
            /* display script code (English) */
            {   "",     "",     "",     "",     "",     "Simplified Han", "", "", ""       },
            /* display country (English) */
            {   "United States",    "France",   "Spain",  "Greece",   "Norway", "China", "Germany", "", "Japan"       },
            /* display variant (English) */
            {   "",     "",     "",     "",     "NY",  "", "", "", ""       },
            /* display name (English) */
            {   "English (United States)", "French (France)", "Catalan (Spain)",
                "Greek (Greece)", "Norwegian (Norway, NY)", "Chinese (Simplified Han, China)",
                "German (Germany, Collation=Phonebook Sort Order)", "Spanish (Collation=Traditional)", "Japanese (Japan, Calendar=Japanese Calendar)" },

                /* display language (French) */
                {   "anglais",  "fran\\u00E7ais",   "catalan", "grec",    "norv\\u00E9gien",    "chinois", "allemand", "espagnol", "japonais"     },
                /* display script code (French) */
                {   "",     "",     "",     "",     "",     "Hans", "", "", ""         },
                /* display country (French) */
                {   "\\u00C9tats-Unis",    "France",   "Espagne",  "Gr\\u00E8ce",   "Norv\\u00E8ge",    "Chine", "Allemagne", "", "Japon"       },
                /* display variant (French) */
                {   "",     "",     "",     "",     "NY",   "", "", "", ""       },
                /* display name (French) */
                {   "anglais (\\u00C9tats-Unis)", "fran\\u00E7ais (France)", "catalan (Espagne)",
                    "grec (Gr\\u00E8ce)", "norv\\u00E9gien (Norv\\u00E8ge, NY)",  "chinois (Hans, Chine)",
                    "allemand (Allemagne, Ordonnancement=Ordre de l'annuaire)", "espagnol (Ordonnancement=Ordre traditionnel)", "japonais (Japon, Calendrier=Calendrier japonais)" },

                    /* display language (Catalan) */
                    {   "angl\\u00E8s", "franc\\u00E8s", "catal\\u00E0", "grec",  "noruec", "xin\\u00E9s", "alemany", "espanyol", "japon\\u00E8s"    },
                    /* display script code (Catalan) */
                    {   "",     "",     "",     "",     "",     "Hans", "", "", ""         },
                    /* display country (Catalan) */
                    {   "Estats Units", "Fran\\u00E7a", "Espanya",  "Gr\\u00E8cia", "Noruega",  "Xina", "Alemanya", "", "Jap\\u00F3"    },
                    /* display variant (Catalan) */
                    {   "", "", "",                    "", "NY",    "", "", "", ""    },
                    /* display name (Catalan) */
                    {   "angl\\u00E8s (Estats Units)", "franc\\u00E8s (Fran\\u00E7a)", "catal\\u00E0 (Espanya)",
                        "grec (Gr\\u00E8cia)", "noruec (Noruega, NY)", "xin\\u00E9s (Hans, Xina)",
                        "alemany (Alemanya, COLLATION=PHONEBOOK)", "espanyol (COLLATION=TRADITIONAL)", "japon\\u00E8s (Jap\\u00F3, CALENDAR=JAPANESE)" },

                        /* display language (Greek) */
                        {
                            "\\u0391\\u03b3\\u03b3\\u03bb\\u03b9\\u03ba\\u03ac",
                            "\\u0393\\u03b1\\u03bb\\u03bb\\u03b9\\u03ba\\u03ac",
                            "\\u039a\\u03b1\\u03c4\\u03b1\\u03bb\\u03b1\\u03bd\\u03b9\\u03ba\\u03ac",
                            "\\u0395\\u03bb\\u03bb\\u03b7\\u03bd\\u03b9\\u03ba\\u03ac",
                            "\\u039d\\u03bf\\u03c1\\u03b2\\u03b7\\u03b3\\u03b9\\u03ba\\u03ac",
                            "\\u039A\\u03B9\\u03BD\\u03B5\\u03B6\\u03B9\\u03BA\\u03AC",
                            "\\u0393\\u03B5\\u03C1\\u03BC\\u03B1\\u03BD\\u03B9\\u03BA\\u03AC",
                            "\\u0399\\u03C3\\u03C0\\u03B1\\u03BD\\u03B9\\u03BA\\u03AC",
                            "\\u0399\\u03B1\\u03C0\\u03C9\\u03BD\\u03B9\\u03BA\\u03AC"
                        },
                        /* display script code (Greek) */
                        {   "",     "",     "",     "",     "",     "Hans", "", "", ""         },
                        /* display country (Greek) */
                        {
                            "\\u0397\\u03bd\\u03c9\\u03bc\\u03ad\\u03bd\\u03b5\\u03c2 \\u03a0\\u03bf\\u03bb\\u03b9\\u03c4\\u03b5\\u03af\\u03b5\\u03c2",
                            "\\u0393\\u03b1\\u03bb\\u03bb\\u03af\\u03b1",
                            "\\u0399\\u03c3\\u03c0\\u03b1\\u03bd\\u03af\\u03b1",
                            "\\u0395\\u03bb\\u03bb\\u03ac\\u03b4\\u03b1",
                            "\\u039d\\u03bf\\u03c1\\u03b2\\u03b7\\u03b3\\u03af\\u03b1",
                            "\\u039A\\u03AF\\u03BD\\u03B1",
                            "\\u0393\\u03B5\\u03C1\\u03BC\\u03B1\\u03BD\\u03AF\\u03B1",
                            "",
                            "\\u0399\\u03B1\\u03C0\\u03C9\\u03BD\\u03AF\\u03B1"
                        },
                        /* display variant (Greek) */
                        {   "", "", "", "", "NY", "", "", "", ""    }, /* TODO: currently there is no translation for NY in Greek fix this test when we have it */
                        /* display name (Greek) */
                        {
                            "\\u0391\\u03b3\\u03b3\\u03bb\\u03b9\\u03ba\\u03ac (\\u0397\\u03bd\\u03c9\\u03bc\\u03ad\\u03bd\\u03b5\\u03c2 \\u03a0\\u03bf\\u03bb\\u03b9\\u03c4\\u03b5\\u03af\\u03b5\\u03c2)",
                            "\\u0393\\u03b1\\u03bb\\u03bb\\u03b9\\u03ba\\u03ac (\\u0393\\u03b1\\u03bb\\u03bb\\u03af\\u03b1)",
                            "\\u039a\\u03b1\\u03c4\\u03b1\\u03bb\\u03b1\\u03bd\\u03b9\\u03ba\\u03ac (\\u0399\\u03c3\\u03c0\\u03b1\\u03bd\\u03af\\u03b1)",
                            "\\u0395\\u03bb\\u03bb\\u03b7\\u03bd\\u03b9\\u03ba\\u03ac (\\u0395\\u03bb\\u03bb\\u03ac\\u03b4\\u03b1)",
                            "\\u039d\\u03bf\\u03c1\\u03b2\\u03b7\\u03b3\\u03b9\\u03ba\\u03ac (\\u039d\\u03bf\\u03c1\\u03b2\\u03b7\\u03b3\\u03af\\u03b1, NY)",
                            "\\u039A\\u03B9\\u03BD\\u03B5\\u03B6\\u03B9\\u03BA\\u03AC (Hans, \\u039A\\u03AF\\u03BD\\u03B1)",
                            "\\u0393\\u03B5\\u03C1\\u03BC\\u03B1\\u03BD\\u03B9\\u03BA\\u03AC (\\u0393\\u03B5\\u03C1\\u03BC\\u03B1\\u03BD\\u03AF\\u03B1, COLLATION=PHONEBOOK)",
                            "\\u0399\\u03C3\\u03C0\\u03B1\\u03BD\\u03B9\\u03BA\\u03AC (COLLATION=TRADITIONAL)",
                            "\\u0399\\u03B1\\u03C0\\u03C9\\u03BD\\u03B9\\u03BA\\u03AC (\\u0399\\u03B1\\u03C0\\u03C9\\u03BD\\u03AF\\u03B1, CALENDAR=JAPANESE)"
                        }
    };
    //    private static final int ENGLISH = 0;
    //    private static final int FRENCH = 1;
    //    private static final int CATALAN = 2;
    //    private static final int GREEK = 3;
    //    private static final int NORWEGIAN = 4;
    private static final int LANG = 0;
    private static final int SCRIPT = 1;
    private static final int CTRY = 2;
    private static final int VAR = 3;
    private static final int NAME = 4;
    //    private static final int LANG3 = 5;
    //    private static final int CTRY3 = 6;
    //    private static final int LCID = 7;
    //    private static final int DLANG_EN = 8;
    //    private static final int DSCRIPT_EN = 9;
    //    private static final int DCTRY_EN = 10;
    //    private static final int DVAR_EN = 11;
    //    private static final int DNAME_EN = 12;
    //    private static final int DLANG_FR = 13;
    //    private static final int DSCRIPT_FR = 14;
    //    private static final int DCTRY_FR = 15;
    //    private static final int DVAR_FR = 16;
    //    private static final int DNAME_FR = 17;
    //    private static final int DLANG_CA = 18;
    //    private static final int DSCRIPT_CA = 19;
    //    private static final int DCTRY_CA = 20;
    //    private static final int DVAR_CA = 21;
    //    private static final int DNAME_CA = 22;
    //    private static final int DLANG_EL = 23;
    //    private static final int DSCRIPT_EL = 24;
    //    private static final int DCTRY_EL = 25;
    //    private static final int DVAR_EL = 26;
    //    private static final int DNAME_EL = 27;

    @Test
    public void TestBasicGetters() {
        int i;
        logln("Testing Basic Getters\n");
        for (i = 0; i < LOCALE_SIZE; i++) {
            String testLocale=(rawData2[NAME][i]);
            logln("Testing "+ testLocale+".....\n");

            String lang =ULocale.getLanguage(testLocale);
            if (0 !=lang.compareTo(rawData2[LANG][i]))    {
                errln("  Language code mismatch: "+lang+" versus "+  rawData2[LANG][i]);
            }

            String ctry=ULocale.getCountry(testLocale);
            if (0 !=ctry.compareTo(rawData2[CTRY][i]))    {
                errln("  Country code mismatch: "+ctry+" versus "+  rawData2[CTRY][i]);
            }

            String var=ULocale.getVariant(testLocale);
            if (0 !=var.compareTo(rawData2[VAR][i]))    {
                errln("  Variant code mismatch: "+var+" versus "+  rawData2[VAR][i]);
            }

            String name = ULocale.getName(testLocale);
            if (0 !=name.compareTo(rawData2[NAME][i]))    {
                errln("  Name mismatch: "+name+" versus "+  rawData2[NAME][i]);
            }

        }
    }

    @Test
    public void TestPrefixes() {
        // POSIX ids are no longer handled by getName, so POSIX failures are ignored
        final String [][] testData = new String[][]{
                /* null canonicalize() column means "expect same as getName()" */
                {"sv", "", "FI", "AL", "sv-fi-al", "sv_FI_AL", null},
                {"en", "", "GB", "", "en-gb", "en_GB", null},
                {"i-hakka", "", "MT", "XEMXIJA", "i-hakka_MT_XEMXIJA", "i-hakka_MT_XEMXIJA", null},
                {"i-hakka", "", "CN", "", "i-hakka_CN", "i-hakka_CN", null},
                {"i-hakka", "", "MX", "", "I-hakka_MX", "i-hakka_MX", null},
                {"x-klingon", "", "US", "SANJOSE", "X-KLINGON_us_SANJOSE", "x-klingon_US_SANJOSE", null},

                {"de", "", "", "1901", "de-1901", "de__1901", null},
                {"mr", "", "", "", "mr.utf8", "mr.utf8", "mr"},
                {"de", "", "TV", "", "de-tv.koi8r", "de_TV.koi8r", "de_TV"},
                {"x-piglatin", "", "ML", "", "x-piglatin_ML.MBE", "x-piglatin_ML.MBE", "x-piglatin_ML"},  /* Multibyte English */
                {"i-cherokee", "","US", "", "i-Cherokee_US.utf7", "i-cherokee_US.utf7", "i-cherokee_US"},
                {"x-filfli", "", "MT", "FILFLA", "x-filfli_MT_FILFLA.gb-18030", "x-filfli_MT_FILFLA.gb-18030", "x-filfli_MT_FILFLA"},
                {"no", "", "NO", "NY_B", "no-no-ny.utf32@B", "no_NO_NY.utf32@B", "no_NO_NY_B"},
                {"no", "", "NO", "B",  "no-no.utf32@B", "no_NO.utf32@B", "no_NO_B"},
                {"no", "", "",   "NY", "no__ny", "no__NY", null},
                {"no", "", "",   "NY", "no@ny", "no@ny", "no__NY"},
                {"el", "Latn", "", "", "el-latn", "el_Latn", null},
                {"en", "Cyrl", "RU", "", "en-cyrl-ru", "en_Cyrl_RU", null},
                {"zh", "Hant", "TW", "STROKE", "zh-hant_TW_STROKE", "zh_Hant_TW_STROKE", "zh_Hant_TW@collation=stroke"},
                {"zh", "Hant", "CN", "STROKE", "zh-hant_CN_STROKE", "zh_Hant_CN_STROKE", "zh_Hant_CN@collation=stroke"},
                {"zh", "Hant", "TW", "PINYIN", "zh-hant_TW_PINYIN", "zh_Hant_TW_PINYIN", "zh_Hant_TW@collation=pinyin"},
                {"qq", "Qqqq", "QQ", "QQ", "qq_Qqqq_QQ_QQ", "qq_Qqqq_QQ_QQ", null},
                {"qq", "Qqqq", "", "QQ", "qq_Qqqq__QQ", "qq_Qqqq__QQ", null},
                {"ab", "Cdef", "GH", "IJ", "ab_cdef_gh_ij", "ab_Cdef_GH_IJ", null}, /* total garbage */

                // odd cases
                {"", "", "", "", "@FOO=bar", "@foo=bar", null},
                {"", "", "", "", "_@FOO=bar", "@foo=bar", null},
                {"", "", "", "", "__@FOO=bar", "@foo=bar", null},
                {"", "", "", "FOO", "__foo@FOO=bar", "__FOO@foo=bar", null}, // we have some of these prefixes
        };

        String loc, buf,buf1;
        final String [] testTitles = {
                "ULocale.getLanguage()",
                "ULocale.getScript()",
                "ULocale.getCountry()",
                "ULocale.getVariant()",
                "name",
                "ULocale.getName()",
                "canonicalize()",
        };
        ULocale uloc;

        for(int row=0;row<testData.length;row++) {
            loc = testData[row][NAME];
            logln("Test #"+row+": "+loc);

            uloc = new ULocale(loc);

            for(int n=0;n<=(NAME+2);n++) {
                if(n==NAME) continue;

                switch(n) {
                case LANG:
                    buf  = ULocale.getLanguage(loc);
                    buf1 = uloc.getLanguage();
                    break;

                case SCRIPT:
                    buf  = ULocale.getScript(loc);
                    buf1 = uloc.getScript();
                    break;

                case CTRY:
                    buf  = ULocale.getCountry(loc);
                    buf1 = uloc.getCountry();
                    break;

                case VAR:
                    buf  = ULocale.getVariant(loc);
                    buf1 = buf;
                    break;

                case NAME+1:
                    buf  = ULocale.getName(loc);
                buf1 = uloc.getName();
                break;

                case NAME+2:
                    buf = ULocale.canonicalize(loc);
                buf1 = ULocale.createCanonical(loc).getName();
                break;

                default:
                    buf = "**??";
                    buf1 = buf;
                }

                logln("#"+row+": "+testTitles[n]+" on "+loc+": -> ["+buf+"]");

                String expected = testData[row][n];
                if (expected == null && n == (NAME+2)) {
                    expected = testData[row][NAME+1];
                }

                // ignore POSIX failures in getName, we don't spec behavior in this case
                if (n == NAME+1 &&
                        (expected.indexOf('.') != -1 ||
                        expected.indexOf('@') != -1)) {
                    continue;
                }

                if(buf.compareTo(expected)!=0) {
                    errln("#"+row+": "+testTitles[n]+" on "+loc+": -> ["+buf+"] (expected '"+expected+"'!)");
                }
                if(buf1.compareTo(expected)!=0) {
                    errln("#"+row+": "+testTitles[n]+" on ULocale object "+loc+": -> ["+buf1+"] (expected '"+expected+"'!)");
                }
            }
        }
    }

    @Test
    public void TestUldnWithGarbage(){
        LocaleDisplayNames ldn = LocaleDisplayNames.getInstance(Locale.US, DisplayContext.DIALECT_NAMES);
        String badLocaleID = "english (United States) [w";
        String expectedResult = "english [united states] [w"; // case changed from input
        String result = ldn.localeDisplayName(badLocaleID);
        if (result.compareTo(expectedResult) != 0) {
            errln("FAIL: LocaleDisplayNames.localeDisplayName(String) for bad locale ID \"" + badLocaleID + "\", expected \"" + expectedResult + "\", got \"" + result + "\"");
        }
        ULocale badLocale = new ULocale(badLocaleID);
        result = ldn.localeDisplayName(badLocale);
        if (result.compareTo(expectedResult) != 0) {
            errln("FAIL: LocaleDisplayNames.localeDisplayName(ULocale) for bad locale ID \"" + badLocaleID + "\", expected \"" + expectedResult + "\", got \"" + result + "\"");
        }
    }

    @Test
    public void TestObsoleteNames(){
        final String[][] tests = new String[][]{
                /* locale, language3, language2, Country3, country2 */
                { "eng_USA", "eng", "en", "USA", "US" },
                { "kok",  "kok", "kok", "", "" },
                { "in",  "ind", "in", "", "" },
                { "id",  "ind", "id", "", "" }, /* NO aliasing */
                { "sh",  "srp", "sh", "", "" },
                { "zz_CS",  "", "zz", "SCG", "CS" },
                { "zz_FX",  "", "zz", "FXX", "FX" },
                { "zz_RO",  "", "zz", "ROU", "RO" },
                { "zz_TP",  "", "zz", "TMP", "TP" },
                { "zz_TL",  "", "zz", "TLS", "TL" },
                { "zz_ZR",  "", "zz", "ZAR", "ZR" },
                { "zz_FXX",  "", "zz", "FXX", "FX" }, /* no aliasing. Doesn't go to PS(PSE). */
                { "zz_ROM",  "", "zz", "ROU", "RO" },
                { "zz_ROU",  "", "zz", "ROU", "RO" },
                { "zz_ZAR",  "", "zz", "ZAR", "ZR" },
                { "zz_TMP",  "", "zz", "TMP", "TP" },
                { "zz_TLS",  "", "zz", "TLS", "TL" },
                { "zz_YUG",  "", "zz", "YUG", "YU" },
                { "mlt_PSE", "mlt", "mt", "PSE", "PS" },
                { "iw", "heb", "iw", "", "" },
                { "ji", "yid", "ji", "", "" },
                { "jw", "jaw", "jw", "", "" },
                { "sh", "srp", "sh", "", "" },
                { "", "", "", "", "" }
        };

        for(int i=0;i<tests.length;i++){
            String locale = tests[i][0];
            logln("** Testing : "+ locale);
            String buff, buff1;
            ULocale uloc  = new ULocale(locale);

            buff = ULocale.getISO3Language(locale);
            if(buff.compareTo(tests[i][1])!=0){
                errln("FAIL: ULocale.getISO3Language("+locale+")=="+
                        buff+",\t expected "+tests[i][1]);
            }else{
                logln("   ULocale.getISO3Language("+locale+")=="+buff);
            }

            buff1 = uloc.getISO3Language();
            if(buff1.compareTo(tests[i][1])!=0){
                errln("FAIL: ULocale.getISO3Language("+locale+")=="+
                        buff+",\t expected "+tests[i][1]);
            }else{
                logln("   ULocale.getISO3Language("+locale+")=="+buff);
            }

            buff = ULocale.getLanguage(locale);
            if(buff.compareTo(tests[i][2])!=0){
                errln("FAIL: ULocale.getLanguage("+locale+")=="+
                        buff+",\t expected "+tests[i][2]);
            }else{
                logln("   ULocale.getLanguage("+locale+")=="+buff);
            }

            buff = ULocale.getISO3Country(locale);
            if(buff.compareTo(tests[i][3])!=0){
                errln("FAIL: ULocale.getISO3Country("+locale+")=="+
                        buff+",\t expected "+tests[i][3]);
            }else{
                logln("   ULocale.getISO3Country("+locale+")=="+buff);
            }

            buff1 = uloc.getISO3Country();
            if(buff1.compareTo(tests[i][3])!=0){
                errln("FAIL: ULocale.getISO3Country("+locale+")=="+
                        buff+",\t expected "+tests[i][3]);
            }else{
                logln("   ULocale.getISO3Country("+locale+")=="+buff);
            }

            buff = ULocale.getCountry(locale);
            if(buff.compareTo(tests[i][4])!=0){
                errln("FAIL: ULocale.getCountry("+locale+")=="+
                        buff+",\t expected "+tests[i][4]);
            }else{
                logln("   ULocale.getCountry("+locale+")=="+buff);
            }
        }

        if (ULocale.getLanguage("iw_IL").compareTo( ULocale.getLanguage("he_IL"))==0) {
            errln("he,iw ULocale.getLanguage mismatch");
        }

        String buff = ULocale.getLanguage("kok_IN");
        if(buff.compareTo("kok")!=0){
            errln("ULocale.getLanguage(\"kok\") failed. Expected: kok Got: "+buff);
        }
    }

    @Test
    public void TestCanonicalization(){
        final String[][]testCases = new String[][]{
                { "ca_ES_PREEURO", "ca_ES_PREEURO", "ca_ES@currency=ESP" },
                { "de_AT_PREEURO", "de_AT_PREEURO", "de_AT@currency=ATS" },
                { "de_DE_PREEURO", "de_DE_PREEURO", "de_DE@currency=DEM" },
                { "de_LU_PREEURO", "de_LU_PREEURO", "de_LU@currency=EUR" },
                { "el_GR_PREEURO", "el_GR_PREEURO", "el_GR@currency=GRD" },
                { "en_BE_PREEURO", "en_BE_PREEURO", "en_BE@currency=BEF" },
                { "en_IE_PREEURO", "en_IE_PREEURO", "en_IE@currency=IEP" },
                { "es_ES_PREEURO", "es_ES_PREEURO", "es_ES@currency=ESP" },
                { "eu_ES_PREEURO", "eu_ES_PREEURO", "eu_ES@currency=ESP" },
                { "fi_FI_PREEURO", "fi_FI_PREEURO", "fi_FI@currency=FIM" },
                { "fr_BE_PREEURO", "fr_BE_PREEURO", "fr_BE@currency=BEF" },
                { "fr_FR_PREEURO", "fr_FR_PREEURO", "fr_FR@currency=FRF" },
                { "fr_LU_PREEURO", "fr_LU_PREEURO", "fr_LU@currency=LUF" },
                { "ga_IE_PREEURO", "ga_IE_PREEURO", "ga_IE@currency=IEP" },
                { "gl_ES_PREEURO", "gl_ES_PREEURO", "gl_ES@currency=ESP" },
                { "it_IT_PREEURO", "it_IT_PREEURO", "it_IT@currency=ITL" },
                { "nl_BE_PREEURO", "nl_BE_PREEURO", "nl_BE@currency=BEF" },
                { "nl_NL_PREEURO", "nl_NL_PREEURO", "nl_NL@currency=NLG" },
                { "pt_PT_PREEURO", "pt_PT_PREEURO", "pt_PT@currency=PTE" },
                { "de__PHONEBOOK", "de__PHONEBOOK", "de@collation=phonebook" },
                { "de_PHONEBOOK", "de__PHONEBOOK", "de@collation=phonebook" },
                { "en_GB_EURO", "en_GB_EURO", "en_GB@currency=EUR" },
                { "en_GB@EURO", null, "en_GB@currency=EUR" }, /* POSIX ID */
                { "es__TRADITIONAL", "es__TRADITIONAL", "es@collation=traditional" },
                { "hi__DIRECT", "hi__DIRECT", "hi@collation=direct" },
                { "ja_JP_TRADITIONAL", "ja_JP_TRADITIONAL", "ja_JP@calendar=japanese" },
                { "th_TH_TRADITIONAL", "th_TH_TRADITIONAL", "th_TH@calendar=buddhist" },
                { "zh_TW_STROKE", "zh_TW_STROKE", "zh_TW@collation=stroke" },
                { "zh__PINYIN", "zh__PINYIN", "zh@collation=pinyin" },
                { "zh@collation=pinyin", "zh@collation=pinyin", "zh@collation=pinyin" },
                { "zh_CN@collation=pinyin", "zh_CN@collation=pinyin", "zh_CN@collation=pinyin" },
                { "zh_CN_CA@collation=pinyin", "zh_CN_CA@collation=pinyin", "zh_CN_CA@collation=pinyin" },
                { "en_US_POSIX", "en_US_POSIX", "en_US_POSIX" },
                { "hy_AM_REVISED", "hy_AM_REVISED", "hy_AM_REVISED" },
                { "no_NO_NY", "no_NO_NY", "no_NO_NY" /* not: "nn_NO" [alan ICU3.0] */ },
                { "no@ny", null, "no__NY" /* not: "nn" [alan ICU3.0] */ }, /* POSIX ID */
                { "no-no.utf32@B", null, "no_NO_B" /* not: "nb_NO_B" [alan ICU3.0] */ }, /* POSIX ID */
                { "qz-qz@Euro", null, "qz_QZ@currency=EUR" }, /* qz-qz uses private use iso codes */
                { "en-BOONT", "en__BOONT", "en__BOONT" }, /* registered name */
                { "de-1901", "de__1901", "de__1901" }, /* registered name */
                { "de-1906", "de__1906", "de__1906" }, /* registered name */
                { "sr-SP-Cyrl", "sr_SP_CYRL", "sr_Cyrl_RS" }, /* .NET name */
                { "sr-SP-Latn", "sr_SP_LATN", "sr_Latn_RS" }, /* .NET name */
                { "sr_YU_CYRILLIC", "sr_YU_CYRILLIC", "sr_Cyrl_RS" }, /* Linux name */
                { "uz-UZ-Cyrl", "uz_UZ_CYRL", "uz_Cyrl_UZ" }, /* .NET name */
                { "uz-UZ-Latn", "uz_UZ_LATN", "uz_Latn_UZ" }, /* .NET name */
                { "zh-CHS", "zh_CHS", "zh_Hans" }, /* .NET name */
                { "zh-CHT", "zh_CHT", "zh_Hant" }, /* .NET name This may change back to zh_Hant */

                /* posix behavior that used to be performed by getName */
                { "mr.utf8", null, "mr" },
                { "de-tv.koi8r", null, "de_TV" },
                { "x-piglatin_ML.MBE", null, "x-piglatin_ML" },
                { "i-cherokee_US.utf7", null, "i-cherokee_US" },
                { "x-filfli_MT_FILFLA.gb-18030", null, "x-filfli_MT_FILFLA" },
                { "no-no-ny.utf8@B", null, "no_NO_NY_B" /* not: "nn_NO" [alan ICU3.0] */ }, /* @ ignored unless variant is empty */

                /* fleshing out canonicalization */
                /* sort keywords, ';' is separator so not present at end in canonical form */
                { "en_Hant_IL_VALLEY_GIRL@currency=EUR;calendar=Japanese;", "en_Hant_IL_VALLEY_GIRL@calendar=Japanese;currency=EUR", "en_Hant_IL_VALLEY_GIRL@calendar=Japanese;currency=EUR" },
                /* already-canonical ids are not changed */
                { "en_Hant_IL_VALLEY_GIRL@calendar=Japanese;currency=EUR", "en_Hant_IL_VALLEY_GIRL@calendar=Japanese;currency=EUR", "en_Hant_IL_VALLEY_GIRL@calendar=Japanese;currency=EUR" },
                /* PRE_EURO and EURO conversions don't affect other keywords */
                /* not in spec
               { "es_ES_PREEURO@CALendar=Japanese", "es_ES_PREEURO@calendar=Japanese", "es_ES@calendar=Japanese;currency=ESP" },
               { "es_ES_EURO@SHOUT=zipeedeedoodah", "es_ES_EURO@shout=zipeedeedoodah", "es_ES@currency=EUR;shout=zipeedeedoodah" },
                 */
                /* currency keyword overrides PRE_EURO and EURO currency */
                /* not in spec
               { "es_ES_PREEURO@currency=EUR", "es_ES_PREEURO@currency=EUR", "es_ES@currency=EUR" },
               { "es_ES_EURO@currency=ESP", "es_ES_EURO@currency=ESP", "es_ES@currency=ESP" },
                 */
                /* norwegian is just too weird, if we handle things in their full generality */
                /* this is a negative test to show that we DO NOT handle 'lang=no,var=NY' specially. */
                { "no-Hant-GB_NY@currency=$$$", "no_Hant_GB_NY@currency=$$$", "no_Hant_GB_NY@currency=$$$" /* not: "nn_Hant_GB@currency=$$$" [alan ICU3.0] */ },

                /* test cases reflecting internal resource bundle usage */
                /* root is just a language */
                { "root@kw=foo", "root@kw=foo", "root@kw=foo" },
                /* level 2 canonicalization should not touch basename when there are keywords and it is null */
                { "@calendar=gregorian", "@calendar=gregorian", "@calendar=gregorian" },
        };

        for(int i = 0; i< testCases.length;i++){
            String[] testCase = testCases[i];
            String source = testCase[0];
            String level1Expected = testCase[1];
            String level2Expected = testCase[2];

            if (level1Expected != null) { // null means we have no expectations for how this case is handled
                String level1 = ULocale.getName(source);
                if (!level1.equals(level1Expected)) {
                    errln("ULocale.getName error for: '" + source +
                            "' expected: '" + level1Expected + "' but got: '" + level1 + "'");
                } else {
                    logln("Ulocale.getName for: '" + source + "' returned: '" + level1 + "'");
                }
            } else {
                logln("ULocale.getName skipped: '" + source + "'");
            }

            if (level2Expected != null) {
                String level2 = ULocale.canonicalize(source);
                if(!level2.equals(level2Expected)){
                    errln("ULocale.getName error for: '" + source +
                            "' expected: '" + level2Expected + "' but got: '" + level2 + "'");
                } else {
                    logln("Ulocale.canonicalize for: '" + source + "' returned: '" + level2 + "'");
                }
            } else {
                logln("ULocale.canonicalize skipped: '" + source + "'");
            }
        }
    }

    @Test
    public void TestGetAvailable(){
        ULocale[] locales = ULocale.getAvailableLocales();
        if(locales.length<10){
            errln("Did not get the correct result from getAvailableLocales");
        }
        // Android patch (http://b/31841293) start.
        // Accept locales other than zu_ZA at the end, as some OEMs add locales. Any locale added
        // after the original zu_ZA has to start with "z", as that's the last acceptable letter.
        if(!locales[locales.length-1].getName().startsWith("z")){
        // Android patch (http://b/31841293) end.
            errln("Did not get the expected result");
        }
    }

    @Test
    public void TestDisplayNames() {
        // consistency check, also check that all data is available
        {
            ULocale[] locales = ULocale.getAvailableLocales();
            for (int i = 0; i < locales.length; ++i) {
                ULocale l = locales[i];
                String name = l.getDisplayName();

                logln(l + " --> " + name +
                        ", " + l.getDisplayName(ULocale.GERMAN) +
                        ", " + l.getDisplayName(ULocale.FRANCE));

                String language = l.getDisplayLanguage();
                String script = l.getDisplayScriptInContext();
                String country = l.getDisplayCountry();
                String variant = l.getDisplayVariant();

                checkName(name, language, script, country, variant, ULocale.getDefault());

                for (int j = 0; j < locales.length; ++j) {
                    ULocale dl = locales[j];

                    name = l.getDisplayName(dl);
                    language = l.getDisplayLanguage(dl);
                    script = l.getDisplayScriptInContext(dl);
                    country = l.getDisplayCountry(dl);
                    variant = l.getDisplayVariant(dl);

                    if (!checkName(name, language, script, country, variant, dl)) {
                        break;
                    }
                }
            }
        }
        // spot check
        {
            ULocale[] locales = {
                    ULocale.US, ULocale.GERMANY, ULocale.FRANCE
            };
            String[] names = {
                    "Chinese (China)", "Chinesisch (China)", "chinois (Chine)"
            };
            String[] names2 = {
                    "Simplified Chinese (China)", "Chinesisch (vereinfacht) (China)", "chinois simplifi\u00E9 (Chine)"
            };
            ULocale locale = new ULocale("zh_CN");
            ULocale locale2 = new ULocale("zh_Hans_CN");

            for (int i = 0; i < locales.length; ++i) {
                String name = locale.getDisplayName(locales[i]);
                if (!names[i].equals(name)) {
                    errln("expected '" + names[i] + "' but got '" + name + "'");
                }
            }
            for (int i = 0; i < locales.length; ++i) {
                String name = locale2.getDisplayNameWithDialect(locales[i]);
                if (!names2[i].equals(name)) {
                    errln("expected '" + names2[i] + "' but got '" + name + "'");
                }
            }
        }
        // test use of context
        {
            final DisplayContext NM_STD = DisplayContext.STANDARD_NAMES;
            final DisplayContext NM_DIA = DisplayContext.DIALECT_NAMES;
            final DisplayContext CAP_BEG = DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE;
            final DisplayContext CAP_MID = DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE;
            final DisplayContext CAP_UIL = DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU;
            final DisplayContext CAP_STA = DisplayContext.CAPITALIZATION_FOR_STANDALONE;
            final DisplayContext CAP_NON = DisplayContext.CAPITALIZATION_NONE;
            final DisplayContext LEN_FU = DisplayContext.LENGTH_FULL;
            final DisplayContext LEN_SH = DisplayContext.LENGTH_SHORT;
            final DisplayContext SUB_SU = DisplayContext.SUBSTITUTE;
            final DisplayContext SUB_NO = DisplayContext.NO_SUBSTITUTE;

            class Item {
                public String displayLocale;
                public DisplayContext dialectHandling;
                public DisplayContext capitalization;
                public DisplayContext nameLength;
                public DisplayContext substituteHandling;
                public String localeToBeNamed;
                public String result;
                public Item(String dLoc, DisplayContext dia, DisplayContext cap, DisplayContext nameLen, DisplayContext sub, String locToName, String res) {
                    displayLocale = dLoc;
                    dialectHandling = dia;
                    capitalization = cap;
                    nameLength = nameLen;
                    substituteHandling = sub;
                    localeToBeNamed = locToName;
                    result = res;
                }
            };
            final Item[] items = {
                new Item("da", NM_STD, CAP_MID, LEN_FU, SUB_SU, "en", "engelsk"),
                new Item("da", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "en", "Engelsk"),
                new Item("da", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "en", "Engelsk"),
                new Item("da", NM_STD, CAP_STA, LEN_FU, SUB_SU, "en", "engelsk"),
                new Item("da", NM_STD, CAP_MID, LEN_FU, SUB_SU, "en@calendar=buddhist", "engelsk (buddhistisk kalender)"),
                new Item("da", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "en@calendar=buddhist", "Engelsk (buddhistisk kalender)"),
                new Item("da", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "en@calendar=buddhist", "Engelsk (buddhistisk kalender)"),
                new Item("da", NM_STD, CAP_STA, LEN_FU, SUB_SU, "en@calendar=buddhist", "engelsk (buddhistisk kalender)"),
                new Item("da", NM_STD, CAP_MID, LEN_FU, SUB_SU, "en_GB", "engelsk (Storbritannien)"),
                new Item("da", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "en_GB", "Engelsk (Storbritannien)"),
                new Item("da", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "en_GB", "Engelsk (Storbritannien)"),
                new Item("da", NM_STD, CAP_STA, LEN_FU, SUB_SU, "en_GB", "engelsk (Storbritannien)"),
                new Item("da", NM_STD, CAP_MID, LEN_SH, SUB_SU, "en_GB", "engelsk (UK)"),
                new Item("da", NM_STD, CAP_BEG, LEN_SH, SUB_SU, "en_GB", "Engelsk (UK)"),
                new Item("da", NM_STD, CAP_UIL, LEN_SH, SUB_SU, "en_GB", "Engelsk (UK)"),
                new Item("da", NM_STD, CAP_STA, LEN_SH, SUB_SU, "en_GB", "engelsk (UK)"),
                new Item("da", NM_DIA, CAP_MID, LEN_FU, SUB_SU, "en_GB", "britisk engelsk"),
                new Item("da", NM_DIA, CAP_BEG, LEN_FU, SUB_SU, "en_GB", "Britisk engelsk"),
                new Item("da", NM_DIA, CAP_UIL, LEN_FU, SUB_SU, "en_GB", "Britisk engelsk"),
                new Item("da", NM_DIA, CAP_STA, LEN_FU, SUB_SU, "en_GB", "britisk engelsk"),
                new Item("es", NM_STD, CAP_MID, LEN_FU, SUB_SU, "en", "ingl\u00E9s"),
                new Item("es", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "en", "Ingl\u00E9s"),
                new Item("es", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "en", "Ingl\u00E9s"),
                new Item("es", NM_STD, CAP_STA, LEN_FU, SUB_SU, "en", "Ingl\u00E9s"),
                new Item("es", NM_STD, CAP_MID, LEN_FU, SUB_SU, "en_GB", "ingl\u00E9s (Reino Unido)"),
                new Item("es", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s (Reino Unido)"),
                new Item("es", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s (Reino Unido)"),
                new Item("es", NM_STD, CAP_STA, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s (Reino Unido)"),
                new Item("es", NM_STD, CAP_MID, LEN_SH, SUB_SU, "en_GB", "ingl\u00E9s (RU)"),
                new Item("es", NM_STD, CAP_BEG, LEN_SH, SUB_SU, "en_GB", "Ingl\u00E9s (RU)"),
                new Item("es", NM_STD, CAP_UIL, LEN_SH, SUB_SU, "en_GB", "Ingl\u00E9s (RU)"),
                new Item("es", NM_STD, CAP_STA, LEN_SH, SUB_SU, "en_GB", "Ingl\u00E9s (RU)"),
                new Item("es", NM_DIA, CAP_MID, LEN_FU, SUB_SU, "en_GB", "ingl\u00E9s brit\u00E1nico"),
                new Item("es", NM_DIA, CAP_BEG, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s brit\u00E1nico"),
                new Item("es", NM_DIA, CAP_UIL, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s brit\u00E1nico"),
                new Item("es", NM_DIA, CAP_STA, LEN_FU, SUB_SU, "en_GB", "Ingl\u00E9s brit\u00E1nico"),
                new Item("ru", NM_STD, CAP_MID, LEN_FU, SUB_SU, "uz_Latn", "\u0443\u0437\u0431\u0435\u043A\u0441\u043A\u0438\u0439 (\u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430)"),
                new Item("ru", NM_STD, CAP_BEG, LEN_FU, SUB_SU, "uz_Latn", "\u0423\u0437\u0431\u0435\u043A\u0441\u043A\u0438\u0439 (\u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430)"),
                new Item("ru", NM_STD, CAP_UIL, LEN_FU, SUB_SU, "uz_Latn", "\u0423\u0437\u0431\u0435\u043A\u0441\u043A\u0438\u0439 (\u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430)"),
                new Item("ru", NM_STD, CAP_STA, LEN_FU, SUB_SU, "uz_Latn", "\u0423\u0437\u0431\u0435\u043A\u0441\u043A\u0438\u0439 (\u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430)"),
                new Item("en", NM_STD, CAP_MID, LEN_FU, SUB_SU, "ur@numbers=latn", "Urdu (Western Digits)"),
                new Item("en", NM_STD, CAP_MID, LEN_FU, SUB_SU, "ur@numbers=arabext", "Urdu (Extended Arabic-Indic Digits)"),
                new Item("en", NM_STD, CAP_MID, LEN_SH, SUB_SU, "ur@numbers=arabext", "Urdu (X Arabic-Indic Digits)"),
                new Item("af", NM_STD, CAP_NON, LEN_FU, SUB_NO, "aa", "Afar"),
                new Item("cs", NM_STD, CAP_NON, LEN_FU, SUB_NO, "vai", "vai"),
            };
            for (Item item: items) {
                ULocale locale = new ULocale(item.displayLocale);
                LocaleDisplayNames ldn = LocaleDisplayNames.getInstance(locale, item.dialectHandling, item.capitalization, item.nameLength, item.substituteHandling);
                DisplayContext dialectHandling = ldn.getContext(DisplayContext.Type.DIALECT_HANDLING);
                assertEquals("consistent dialect handling",
                        dialectHandling == DisplayContext.DIALECT_NAMES,
                        ldn.getDialectHandling() == LocaleDisplayNames.DialectHandling.DIALECT_NAMES);
                DisplayContext capitalization = ldn.getContext(DisplayContext.Type.CAPITALIZATION);
                DisplayContext nameLength = ldn.getContext(DisplayContext.Type.DISPLAY_LENGTH);
                DisplayContext substituteHandling = ldn.getContext(DisplayContext.Type.SUBSTITUTE_HANDLING);
                if (dialectHandling != item.dialectHandling || capitalization != item.capitalization || nameLength != item.nameLength || substituteHandling != item.substituteHandling) {
                    errln("FAIL: displayLoc: " + item.displayLocale + ", dialectNam?: " + item.dialectHandling +
                            ", capitalize: " + item.capitalization + ", nameLen: " + item.nameLength + ", substituteHandling: " + item.substituteHandling + ", locToName: " + item.localeToBeNamed +
                            ", => read back dialectNam?: " + dialectHandling + ", capitalize: " + capitalization + ", nameLen: " + nameLength + ", substituteHandling: " + substituteHandling);
                } else {
                    String result = ldn.localeDisplayName(item.localeToBeNamed);
                    if (!(item.result == null && result == null) && !(result != null && result.equals(item.result))) {
                        errln("FAIL: displayLoc: " + item.displayLocale + ", dialectNam?: " + item.dialectHandling +
                                ", capitalize: " + item.capitalization + ", nameLen: " + item.nameLength + ", substituteHandling: " + item.substituteHandling + ", locToName: " + item.localeToBeNamed +
                                ", => expected result: " + item.result + ", got: " + result);
                    }
                }
            }
        }
    }

    public void TestDisplayLanguageWithDialectCoverage() {
        // Coverage test. Implementation is in class LocaleDisplayNames.
        assertFalse("en in system default locale: anything but empty",
                ULocale.ENGLISH.getDisplayLanguageWithDialect().isEmpty());
        assertEquals("en in de", "Englisch",
                ULocale.ENGLISH.getDisplayLanguageWithDialect(ULocale.GERMAN));
        assertEquals("en (string) in de", "Englisch",
                ULocale.getDisplayLanguageWithDialect("en", ULocale.GERMAN));
        assertEquals("en (string) in de (string)", "Englisch",
                ULocale.getDisplayLanguageWithDialect("en", "de"));
    }

    public void TestDisplayNameWithDialectCoverage() {
        // Coverage test. Implementation is in class LocaleDisplayNames.
        assertFalse("en-GB in system default locale: anything but empty",
                ULocale.UK.getDisplayNameWithDialect().isEmpty());
        assertEquals("en-GB in de", "Britisches Englisch",
                ULocale.UK.getDisplayNameWithDialect(ULocale.GERMAN));
        assertEquals("en-GB (string) in de", "Britisches Englisch",
                ULocale.getDisplayNameWithDialect("en-GB", ULocale.GERMAN));
        assertEquals("en-GB (string) in de (string)", "Britisches Englisch",
                ULocale.getDisplayNameWithDialect("en-GB", "de"));
    }

    public void TestDisplayScriptCoverage() {
        // Coverage test. Implementation is in class LocaleDisplayNames.
        assertFalse("zh-Hans in system default locale: anything but empty",
                ULocale.SIMPLIFIED_CHINESE.getDisplayScript().isEmpty());
        // Stand-alone script name, so not just "Vereinfacht".
        assertEquals("zh-Hans in de", "Vereinfachtes Chinesisch",
                ULocale.SIMPLIFIED_CHINESE.getDisplayScript(ULocale.GERMAN));
        assertEquals("zh-Hans (string) in de", "Vereinfachtes Chinesisch",
                ULocale.getDisplayScript("zh-Hans", ULocale.GERMAN));
        assertEquals("zh-Hans (string) in de (string)", "Vereinfachtes Chinesisch",
                ULocale.getDisplayScript("zh-Hans", "de"));
    }

    private boolean checkName(String name, String language, String script, String country, String variant, ULocale dl) {
        if (!checkInclusion(dl, name, language, "language")) {
            return false;
        }
        if (!checkInclusion(dl, name, script, "script")) {
            return false;
        }
        if (!checkInclusion(dl, name, country, "country")) {
            return false;
        }
        if (!checkInclusion(dl, name, variant, "variant")) {
            return false;
        }
        return true;
    }

    private boolean checkInclusion(ULocale dl, String name, String substring, String substringName) {
        if (substring.length() > 0 && name.indexOf(substring) == -1) {
            String country2 = substring.replace('(', '[').replace(')',']').replace('（', '［').replace('）','］');
            if (name.indexOf(country2) == -1) {
                errln("loc: " + dl + " name '" + name + "' does not contain " +
                        substringName +
                        " '" + substring + "'");
                return false;
            }
        }
        return true;
    }

    @Test
    public void TestCoverage() {
        {
            //Cover displayXXX
            int i, j;
            String localeID="zh_CN";
            String name, language, script, country, variant;
            logln("Covering APIs with signature displayXXX(String, String)");
            for (i = 0; i < LOCALE_SIZE; i++) {
                //localeID String
                String testLocale=(rawData2[NAME][i]);

                logln("Testing "+ testLocale+".....");
                name = ULocale.getDisplayName(localeID, testLocale);
                language = ULocale.getDisplayLanguage(localeID, testLocale);
                script = ULocale.getDisplayScriptInContext(localeID, testLocale);
                country = ULocale.getDisplayCountry(localeID, testLocale);
                variant = ULocale.getDisplayVariant(localeID, testLocale);
                if (!checkName(name, language, script, country, variant, new ULocale(testLocale))) {
                    break;
                }
            }

            logln("Covering APIs with signature displayXXX(String, ULocale)\n");
            for (j = 0; j < LOCALE_SIZE; j++) {
                String testLocale=(rawData2[NAME][j]);
                ULocale loc = new ULocale(testLocale);

                logln("Testing "+ testLocale+".....");
                name = ULocale.getDisplayName(localeID, loc);
                language = ULocale.getDisplayLanguage(localeID, loc);
                script = ULocale.getDisplayScriptInContext(localeID, loc);
                country = ULocale.getDisplayCountry(localeID, loc);
                variant = ULocale.getDisplayVariant(localeID, loc);

                if (!checkName(name, language, script, country, variant, loc)) {
                    break;
                }
            }
        }
        ULocale loc1 = new ULocale("en_US_BROOKLYN");
        ULocale loc2 = new ULocale("en","US","BROOKLYN");
        if (!loc2.equals(loc1)){
            errln("ULocale.ULocale(String a, String b, String c)");
        }

        ULocale loc3 = new ULocale("en_US");
        ULocale loc4 = new ULocale("en","US");
        if (!loc4.equals(loc3)){
            errln("ULocale.ULocale(String a, String b)");
        }

        ULocale loc5 = (ULocale) loc4.clone();
        if (!loc5.equals(loc4)){
            errln("ULocale.clone should get the same ULocale");
        }
        ULocale.getISOCountries(); // To check the result ?!
    }

    @Test
    public void TestBamBm() {
        // "bam" shouldn't be there since the official code is 'bm'
        String[] isoLanguages = ULocale.getISOLanguages();
        for (int i = 0; i < isoLanguages.length; ++i) {
            if ("bam".equals(isoLanguages[i])) {
                errln("found bam");
            }
            if (i > 0 && isoLanguages[i].compareTo(isoLanguages[i-1]) <= 0) {
                errln("language list out of order: '" + isoLanguages[i] + " <= " + isoLanguages[i-1]);
            }
        }
    }

    @Test
    public void TestDisplayKeyword() {
        //prepare testing data
        initHashtable();
        String[] data = {"en_US@collation=phonebook;calendar=islamic-civil",
                "zh_Hans@collation=pinyin;calendar=chinese",
        "foo_Bar_BAZ@collation=traditional;calendar=buddhist"};

        for (int i = 0; i < data.length; i++) {
            String localeID = data[i];
            logln("");
            logln("Testing locale " + localeID + " ...");
            ULocale loc = new ULocale(localeID);

            Iterator it = loc.getKeywords();
            Iterator it2 = ULocale.getKeywords(localeID);
            //it and it2 are not equal here. No way to verify their equivalence yet.
            while(it.hasNext()) {
                String key = (String)it.next();
                String key2 = (String)it2.next();
                if (!key.equals(key2)) {
                    errln("FAIL: static and non-static getKeywords returned different results.");
                }

                //To verify display of Keyword
                // display the above key in English
                String s0 = ULocale.getDisplayKeyword(key); //display in default locale
                String s1 = ULocale.getDisplayKeyword(key, ULocale.US);
                String s2 = ULocale.getDisplayKeyword(key, "en_US");
                if (!s1.equals(s2)) {
                    errln ("FAIL: one of the getDisplayKeyword methods failed.");
                }
                if (ULocale.getDefault().equals(ULocale.US) && !s1.equals(s0)) {
                    errln ("FAIL: getDisplayKeyword methods failed for the default locale.");
                }
                if (!s1.equals(h[0].get(key))) {
                    errln("Locale " + localeID + " getDisplayKeyword for key: " + key +
                            " in English expected \"" + h[0].get(key) + "\" saw \"" + s1 + "\" instead");
                } else {
                    logln("OK: getDisplayKeyword for key: " + key + " in English got " + s1);
                }

                // display the key in S-Chinese
                s1 = ULocale.getDisplayKeyword(key, ULocale.CHINA);
                s2 = ULocale.getDisplayKeyword(key, "zh_Hans");
                if (!s1.equals(s2)) {
                    errln ("one of the getDisplayKeyword methods failed.");
                }
                if (!s1.equals(h[1].get(key))) {
                    errln("Locale " + localeID + " getDisplayKeyword for key: " + key +
                            " in Chinese expected \"" + h[1].get(key) + "\" saw \"" + s1 + "\" instead");
                } else {
                    logln("OK: getDisplayKeyword for key: " + key + " in Chinese got " + s1);
                }

                //To verify display of Keyword values
                String type = loc.getKeywordValue(key);
                // display type in English
                String ss0 = loc.getDisplayKeywordValue(key);
                String ss1 = loc.getDisplayKeywordValue(key, ULocale.US);
                String ss2 = ULocale.getDisplayKeywordValue(localeID, key, "en_US");
                String ss3 = ULocale.getDisplayKeywordValue(localeID, key, ULocale.US);
                if (!ss1.equals(ss2) || !ss1.equals(ss3)) {
                    errln ("FAIL: one of the getDisplayKeywordValue methods failed.");
                }
                if (ULocale.getDefault().equals(ULocale.US) && !ss1.equals(ss0)) {
                    errln ("FAIL: getDisplayKeyword methods failed for the default locale.");
                }
                if (!ss1.equals(h[0].get(type))) {
                    errln(" Locale " + localeID + " getDisplayKeywordValue for key: " + key +
                            " in English expected \"" + h[0].get(type) + "\" saw \"" + ss1 + "\" instead");
                } else {
                    logln("OK: getDisplayKeywordValue for key: " + key + " in English got " + ss1);
                }

                // display type in Chinese
                ss0 = loc.getDisplayKeywordValue(key);
                ss1 = loc.getDisplayKeywordValue(key, ULocale.CHINA);
                ss2 = ULocale.getDisplayKeywordValue(localeID, key, "zh_Hans");
                ss3 = ULocale.getDisplayKeywordValue(localeID, key, ULocale.CHINA);
                if (!ss1.equals(ss2) || !ss1.equals(ss3)) {
                    errln ("one of the getDisplayKeywordValue methods failed.");
                }
                if (!ss1.equals(h[1].get(type))) {
                    errln("Locale " + localeID + " getDisplayKeywordValue for key: " + key +
                            " in Chinese expected \"" + h[1].get(type) + "\" saw \"" + ss1 + "\" instead");
                } else {
                    logln("OK: getDisplayKeywordValue for key: " + key + " in Chinese got " + ss1);
                }
            }
        }
    }

    @Test
    public void TestDisplayWithKeyword() {
        // Note, this test depends on locale display data for the U.S. and Taiwan.
        // If the data changes (in particular, the keyTypePattern may change for Taiwan),
        // this test will break.
        LocaleDisplayNames dn = LocaleDisplayNames.getInstance(ULocale.US,
                DialectHandling.DIALECT_NAMES);
        LocaleDisplayNames tdn = LocaleDisplayNames.getInstance(ULocale.TAIWAN,
                DialectHandling.DIALECT_NAMES);
        String name = dn.localeDisplayName("de@collation=phonebook");
        String target = "German (Phonebook Sort Order)";
        assertEquals("collation", target, name);

        name = tdn.localeDisplayName("de@collation=phonebook");
        target = "德文（電話簿排序）"; // \u5FB7\u6587\uFF08\u96FB\u8A71\u7C3F\u6392\u5E8F\uFF09
        assertEquals("collation", target, name);

        name = dn.localeDisplayName("ja@currency=JPY");
        target = "Japanese (Japanese Yen)";
        assertEquals("currency (JPY)", target, name);

        name = tdn.localeDisplayName("ja@currency=JPY");
        target = "日文（日圓）"; // \u65E5\u6587\uFF08\u65E5\u5713\uFF09
        assertEquals("currency (JPY)", target, name);

        name = dn.localeDisplayName("de@currency=XYZ");
        target = "German (Currency: XYZ)";
        assertEquals("currency (XYZ)", target, name);

        name = dn.localeDisplayName("de@collation=phonebook;currency=XYZ");
        target = "German (Phonebook Sort Order, Currency: XYZ)";
        assertEquals("currency", target, name);

        name = dn.localeDisplayName("de_Latn_DE");
        target = "German (Latin, Germany)";
        assertEquals("currency", target, name);

        name = tdn.localeDisplayName("de@currency=XYZ");
        target = "德文（貨幣：XYZ）";  // \u5FB7\u6587\uFF08\u8CA8\u5E63: XYZ\uFF09
        assertEquals("currency", target, name);

        name = tdn.localeDisplayName("de@collation=phonebook;currency=XYZ");
        target = "德文（電話簿排序，貨幣：XYZ）"; // \u5FB7\u6587\uFF08\u96FB\u8A71\u7C3F\u6392\u5E8F\uFF09，\u5FB7\u6587\uFF08\u8CA8\u5E63: XYZ\uFF09
        assertEquals("collation", target, name);

        name = dn.localeDisplayName("de@foo=bar");
        target = "German (foo=bar)";
        assertEquals("foo", target, name);

        name = tdn.localeDisplayName("de@foo=bar");
        target = "德文（foo=bar）"; // \u5FB7\u6587\uFF08foo=bar\uFF09
        assertEquals("foo", target, name);

        ULocale locale = ULocale.forLanguageTag("de-x-foobar");
        name = dn.localeDisplayName(locale);
        target = "German (Private-Use: foobar)";
        assertEquals("foobar", target, name);

        name = tdn.localeDisplayName(locale);
        target = "德文（私人使用：foobar）"; // \u5FB7\u6587\uFF08\u79C1\u4EBA\u4F7F\u7528: foobar\uFF09
        assertEquals("foobar", target, name);
    }

    private void initHashtable() {
        h[0] = new HashMap<String, String>();
        h[1] = new HashMap<String, String>();

        //display in English
        h[0].put("collation", "Sort Order");
        h[0].put("calendar", "Calendar");
        h[0].put("currency", "Currency");
        h[0].put("phonebook", "Phonebook Sort Order");
        h[0].put("pinyin", "Pinyin Sort Order");
        h[0].put("traditional", "Traditional Sort Order");
        h[0].put("stroke", "Stroke Order");
        h[0].put("japanese", "Japanese Calendar");
        h[0].put("buddhist", "Buddhist Calendar");
        h[0].put("islamic", "Islamic Calendar");
        h[0].put("islamic-civil", "Islamic Calendar (tabular, civil epoch)" );
        h[0].put("hebrew", "Hebrew Calendar");
        h[0].put("chinese", "Chinese Calendar");
        h[0].put("gregorian", "Gregorian Calendar" );

        //display in S-Chinese
        h[1].put("collation", "\u6392\u5E8F");
        h[1].put("calendar", "\u65E5\u5386");
        h[1].put("currency", "\u8D27\u5E01");
        h[1].put("phonebook", "\u7535\u8BDD\u7C3F\u6392\u5E8F");
        h[1].put("pinyin", "\u62FC\u97F3\u6392\u5E8F");
        h[1].put("stroke", "\u7B14\u5212\u987A\u5E8F");
        h[1].put("traditional", "\u4F20\u7EDF\u6392\u5E8F");
        h[1].put("japanese", "\u65E5\u672C\u65E5\u5386");
        h[1].put("buddhist", "\u4F5B\u5386");
        h[1].put("islamic", "\u4F0A\u65AF\u5170\u65E5\u5386");
        h[1].put("islamic-civil", "\u4F0A\u65AF\u5170\u5E0C\u5409\u6765\u65E5\u5386");
        h[1].put("hebrew", "\u5E0C\u4F2F\u6765\u65E5\u5386");
        h[1].put("chinese", "\u519C\u5386");
        h[1].put("gregorian", "\u516C\u5386");
    }

    //Hashtables for storing expected display of keys/types of locale in English and Chinese
    private static Map[] h = new Map[2];

    private static final String ACCEPT_LANGUAGE_TESTS[][]  =  {
        /*#      result  fallback? */
        /*0*/ { "mt_MT", "false" },
        /*1*/ { "en", "false" },
        /*2*/ { "en", "true" }, // fell back from en-zzz to en
        /*3*/ { null, "true" },
        /*4*/ { "es", "false" },
        /*5*/ { "de", "false" },
        /*6*/ { "zh_TW", "false" },
        /*7*/ { "zh", "true" },
    };

    private static final String ACCEPT_LANGUAGE_HTTP[] = {
        /*0*/ "mt-mt, ja;q=0.76, en-us;q=0.95, en;q=0.92, en-gb;q=0.89, fr;q=0.87, iu-ca;q=0.84, iu;q=0.82, ja-jp;q=0.79, mt;q=0.97, de-de;q=0.74, de;q=0.71, es;q=0.68, it-it;q=0.66, it;q=0.63, vi-vn;q=0.61, vi;q=0.58, nl-nl;q=0.55, nl;q=0.53, th-th-traditional;q=.01",
        /*1*/ "ja;q=0.5, en;q=0.8, tlh",
        /*2*/ "en-zzz, de-lx;q=0.8",
        /*3*/ "mga-ie;q=0.9, tlh",
        /*4*/ "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, xxx-yyy;q=.01, "+
                "es",
                /*5*/ "de;q=.9, fr;q=.9, xxx-yyy, sr;q=.8",
                /*6*/ "zh-tw",
                /*7*/ "zh-hant-cn",
    };


    @Test
    public void TestAcceptLanguage() {
        for(int i = 0 ; i < (ACCEPT_LANGUAGE_HTTP.length); i++) {
            Boolean expectBoolean = new Boolean(ACCEPT_LANGUAGE_TESTS[i][1]);
            String expectLocale=ACCEPT_LANGUAGE_TESTS[i][0];

            logln("#" + i + ": expecting: " + expectLocale + " (" + expectBoolean + ")");

            boolean r[] = { false };
            ULocale n = ULocale.acceptLanguage(ACCEPT_LANGUAGE_HTTP[i], r);
            if((n==null)&&(expectLocale!=null)) {
                errln("result was null! line #" + i);
                continue;
            }
            if(((n==null)&&(expectLocale==null)) || (n.toString().equals(expectLocale))) {
                logln(" locale: OK." );
            } else {
                errln("expected " + expectLocale + " but got " + n.toString());
            }
            if(expectBoolean.equals(new Boolean(r[0]))) {
                logln(" bool: OK.");
            } else {
                errln("bool: not OK, was " + new Boolean(r[0]).toString() + " expected " + expectBoolean.toString());
            }
        }
    }

    private ULocale[] StringToULocaleArray(String acceptLanguageList){
        //following code is copied from
        //ULocale.acceptLanguage(String acceptLanguageList, ULocale[] availableLocales, boolean[] fallback)
        class ULocaleAcceptLanguageQ implements Comparable {
            private double q;
            private double serial;
            public ULocaleAcceptLanguageQ(double theq, int theserial) {
                q = theq;
                serial = theserial;
            }
            @Override
            public int compareTo(Object o) {
                ULocaleAcceptLanguageQ other = (ULocaleAcceptLanguageQ) o;
                if(q > other.q) { // reverse - to sort in descending order
                    return -1;
                } else if(q < other.q) {
                    return 1;
                }
                if(serial < other.serial) {
                    return -1;
                } else if(serial > other.serial) {
                    return 1;
                } else {
                    return 0; // same object
                }
            }
        }

        // 1st: parse out the acceptLanguageList into an array

        TreeMap map = new TreeMap();

        final int l = acceptLanguageList.length();
        int n;
        for(n=0;n<l;n++) {
            int itemEnd = acceptLanguageList.indexOf(',',n);
            if(itemEnd == -1) {
                itemEnd = l;
            }
            int paramEnd = acceptLanguageList.indexOf(';',n);
            double q = 1.0;

            if((paramEnd != -1) && (paramEnd < itemEnd)) {
                /* semicolon (;) is closer than end (,) */
                int t = paramEnd + 1;
                while(UCharacter.isWhitespace(acceptLanguageList.charAt(t))) {
                    t++;
                }
                if(acceptLanguageList.charAt(t)=='q') {
                    t++;
                }
                while(UCharacter.isWhitespace(acceptLanguageList.charAt(t))) {
                    t++;
                }
                if(acceptLanguageList.charAt(t)=='=') {
                    t++;
                }
                while(UCharacter.isWhitespace(acceptLanguageList.charAt(t))) {
                    t++;
                }
                try {
                    String val = acceptLanguageList.substring(t,itemEnd).trim();
                    q = Double.parseDouble(val);
                } catch (NumberFormatException nfe) {
                    q = 1.0;
                }
            } else {
                q = 1.0; //default
                paramEnd = itemEnd;
            }

            String loc = acceptLanguageList.substring(n,paramEnd).trim();
            int serial = map.size();
            ULocaleAcceptLanguageQ entry = new ULocaleAcceptLanguageQ(q,serial);
            map.put(entry, new ULocale(ULocale.canonicalize(loc))); // sort in reverse order..   1.0, 0.9, 0.8 .. etc
            n = itemEnd; // get next item. (n++ will skip over delimiter)
        }

        // 2. pull out the map
        ULocale acceptList[] = (ULocale[])map.values().toArray(new ULocale[map.size()]);
        return acceptList;
    }

    @Test
    public void TestAcceptLanguage2() {
        for(int i = 0 ; i < (ACCEPT_LANGUAGE_HTTP.length); i++) {
            Boolean expectBoolean = new Boolean(ACCEPT_LANGUAGE_TESTS[i][1]);
            String expectLocale=ACCEPT_LANGUAGE_TESTS[i][0];

            logln("#" + i + ": expecting: " + expectLocale + " (" + expectBoolean + ")");

            boolean r[] = { false };
            ULocale n = ULocale.acceptLanguage(StringToULocaleArray(ACCEPT_LANGUAGE_HTTP[i]), r);
            if((n==null)&&(expectLocale!=null)) {
                errln("result was null! line #" + i);
                continue;
            }
            if(((n==null)&&(expectLocale==null)) || (n.toString().equals(expectLocale))) {
                logln(" locale: OK." );
            } else {
                errln("expected " + expectLocale + " but got " + n.toString());
            }
            if(expectBoolean.equals(new Boolean(r[0]))) {
                logln(" bool: OK.");
            } else {
                errln("bool: not OK, was " + new Boolean(r[0]).toString() + " expected " + expectBoolean.toString());
            }
        }
    }

    @Test
    public void TestOrientation() {
        {
            String toTest [][] = {
                    { "ar", "right-to-left", "top-to-bottom" },
                    { "ar_Arab", "right-to-left", "top-to-bottom" },
                    { "fa", "right-to-left", "top-to-bottom" },
                    { "he", "right-to-left", "top-to-bottom" },
                    { "ps", "right-to-left", "top-to-bottom" },
                    { "ur", "right-to-left", "top-to-bottom" },
                    { "en", "left-to-right", "top-to-bottom" }
            };

            for (int i = 0; i < toTest.length; ++i) {
                ULocale loc = new ULocale(toTest[i][0]);
                String co = loc.getCharacterOrientation();
                String lo = loc.getLineOrientation();
                if (!co.equals(toTest[i][1])) {
                    errln("Locale \"" + toTest[i][0] + "\" should have \"" + toTest[i][1] + "\" character orientation, but got \'" + co + "\"");
                }
                else if (!lo.equals(toTest[i][2])) {
                    errln("Locale \"" + toTest[i][0] + "\" should have \"" + toTest[i][2] + "\" line orientation, but got \'" + lo + "\"");
                }
            }
        }
    }

    @Test
    public void TestJB3962(){
        ULocale loc = new ULocale("de_CH");
        String disp = loc.getDisplayName(ULocale.GERMAN);
        if(!disp.equals("Deutsch (Schweiz)")){
            errln("Did not get the expected display name for de_CH locale. Got: "+ prettify(disp));
        }
    }

    @Test
    public void TestMinimize() {
        String[][] data = {
                // source, favorRegion, favorScript
                {"zh-Hans-CN", "zh", "zh"},
                {"zh-Hant-TW", "zh-TW", "zh-Hant"},
                {"zh-Hant-SG", "zh-Hant-SG", "zh-Hant-SG"},
                {"zh-Hans-SG", "zh-SG", "zh-SG"},
                {"zh-Hant-HK", "zh-HK", "zh-HK"},
                {"en_Latn_US", "en", "en"},
                {"en_Cyrl-US", "en-Cyrl", "en-Cyrl"},
                {"en_Cyrl-RU", "en-Cyrl-RU", "en-Cyrl-RU"},
                {"en_Latn-RU", "en-RU", "en-RU"},
                {"sr_Cyrl-US", "sr-US", "sr-US"},
                {"sr_Cyrl-RU", "sr-Cyrl-RU", "sr-Cyrl-RU"},
                {"sr_Latn-RU", "sr-RU", "sr-RU"},
        };
        for (String[] test : data) {
            ULocale source = new ULocale(test[0]);
            ULocale expectedFavorRegion = new ULocale(test[1]);
            ULocale expectedFavorScript = new ULocale(test[2]);
            assertEquals("favor region:\t" + Arrays.asList(test).toString(), expectedFavorRegion,
                    ULocale.minimizeSubtags(source, Minimize.FAVOR_REGION));
            assertEquals("favor script:\t" + Arrays.asList(test).toString(), expectedFavorScript,
                    ULocale.minimizeSubtags(source, Minimize.FAVOR_SCRIPT));
        }
    }

    @Test
    public void TestAddLikelySubtags() {
        String[][] data = {
                {"en", "en_Latn_US"},
                {"en_US_BOSTON", "en_Latn_US_BOSTON"},
                {"th@calendar=buddhist", "th_Thai_TH@calendar=buddhist"},
                {"ar_ZZ", "ar_Arab_EG"},
                {"zh", "zh_Hans_CN"},
                {"zh_TW", "zh_Hant_TW"},
                {"zh_HK", "zh_Hant_HK"},
                {"zh_Hant", "zh_Hant_TW"},
                {"zh_Zzzz_CN", "zh_Hans_CN"},
                {"und_US", "en_Latn_US"},
                {"und_HK", "zh_Hant_HK"},
                /* Not yet implemented
            {"art_lojban", "arg_lojban"},
            {"zh_cmn_Hans", "zh_cmn_Hans"},
                 */
        };
        for (int i = 0; i < data.length; i++) {
            ULocale org = new ULocale(data[i][0]);
            ULocale res = ULocale.addLikelySubtags(org);
            if (!res.toString().equals(data[i][1])) {
                errln("Original: " + data[i][0] + " Expected: " + data[i][1] + " - but got " + res.toString());
            }
        }

        String[][] basic_maximize_data = {
                {
                    "zu_Zzzz_Zz",
                    "zu_Latn_ZA",
                }, {
                    "zu_Zz",
                    "zu_Latn_ZA"
                }, {
                    "en_Zz",
                    "en_Latn_US"
                }, {
                    "en_Kore",
                    "en_Kore_US"
                }, {
                    "en_Kore_Zz",
                    "en_Kore_US"
                }, {
                    "en_Kore_ZA",
                    "en_Kore_ZA"
                }, {
                    "en_Kore_ZA_POSIX",
                    "en_Kore_ZA_POSIX"
                }, {
                    "en_Gujr",
                    "en_Gujr_US"
                }, {
                    "en_ZA",
                    "en_Latn_ZA"
                }, {
                    "en_Gujr_Zz",
                    "en_Gujr_US"
                }, {
                    "en_Gujr_ZA",
                    "en_Gujr_ZA"
                }, {
                    "en_Gujr_ZA_POSIX",
                    "en_Gujr_ZA_POSIX"
                }, {
                    "en_US_POSIX_1901",
                    "en_Latn_US_POSIX_1901"
                }, {
                    "en_Latn__POSIX_1901",
                    "en_Latn_US_POSIX_1901"
                }, {
                    "en__POSIX_1901",
                    "en_Latn_US_POSIX_1901"
                }, {
                    "de__POSIX_1901",
                    "de_Latn_DE_POSIX_1901"
                }, {
                    "zzz",
                    ""
                }
        };

        for (int i = 0; i < basic_maximize_data.length; i++) {
            ULocale org = new ULocale(basic_maximize_data[i][0]);
            ULocale res = ULocale.addLikelySubtags(org);
            String exp = basic_maximize_data[i][1];
            if (exp.length() == 0) {
                if (!org.equals(res)) {
                    errln("Original: " + basic_maximize_data[i][0] + " expected: " + exp + " - but got " + res.toString());
                }
            }
            else if (!res.toString().equals(exp)) {
                errln("Original: " + basic_maximize_data[i][0] + " expected: " + exp + " - but got " + res.toString());
            }
        }

        String[][] basic_minimize_data = {
                {
                    "en_Latn_US",
                    "en"
                }, {
                    "en_Latn_US_POSIX_1901",
                    "en__POSIX_1901"
                }, {
                    "en_Zzzz_US_POSIX_1901",
                    "en__POSIX_1901"
                }, {
                    "de_Latn_DE_POSIX_1901",
                    "de__POSIX_1901"
                }, {
                    "und",
                    ""
                }
        };

        for (int i = 0; i < basic_minimize_data.length; i++) {
            ULocale org = new ULocale(basic_minimize_data[i][0]);
            ULocale res = ULocale.minimizeSubtags(org);
            String exp = basic_minimize_data[i][1];
            if (exp.length() == 0) {
                if (!org.equals(res)) {
                    errln("Original: " + basic_minimize_data[i][0] + " expected: " + exp + " - but got " + res.toString());
                }
            }
            else if (!res.toString().equals(exp)) {
                errln("Original: " + basic_minimize_data[i][0] + " expected: " + exp + " - but got " + res.toString());
            }
        }

        String[][] full_data = {
                {
                    /*   "FROM", */
                    /*   "ADD-LIKELY", */
                    /*   "REMOVE-LIKELY" */
                    /* }, { */
                    "aa",
                    "aa_Latn_ET",
                    "aa"
                }, {
                    "af",
                    "af_Latn_ZA",
                    "af"
                }, {
                    "ak",
                    "ak_Latn_GH",
                    "ak"
                }, {
                    "am",
                    "am_Ethi_ET",
                    "am"
                }, {
                    "ar",
                    "ar_Arab_EG",
                    "ar"
                }, {
                    "as",
                    "as_Beng_IN",
                    "as"
                }, {
                    "az",
                    "az_Latn_AZ",
                    "az"
                }, {
                    "be",
                    "be_Cyrl_BY",
                    "be"
                }, {
                    "bg",
                    "bg_Cyrl_BG",
                    "bg"
                }, {
                    "bn",
                    "bn_Beng_BD",
                    "bn"
                }, {
                    "bo",
                    "bo_Tibt_CN",
                    "bo"
                }, {
                    "bs",
                    "bs_Latn_BA",
                    "bs"
                }, {
                    "ca",
                    "ca_Latn_ES",
                    "ca"
                }, {
                    "ch",
                    "ch_Latn_GU",
                    "ch"
                }, {
                    "chk",
                    "chk_Latn_FM",
                    "chk"
                }, {
                    "cs",
                    "cs_Latn_CZ",
                    "cs"
                }, {
                    "cy",
                    "cy_Latn_GB",
                    "cy"
                }, {
                    "da",
                    "da_Latn_DK",
                    "da"
                }, {
                    "de",
                    "de_Latn_DE",
                    "de"
                }, {
                    "dv",
                    "dv_Thaa_MV",
                    "dv"
                }, {
                    "dz",
                    "dz_Tibt_BT",
                    "dz"
                }, {
                    "ee",
                    "ee_Latn_GH",
                    "ee"
                }, {
                    "el",
                    "el_Grek_GR",
                    "el"
                }, {
                    "en",
                    "en_Latn_US",
                    "en"
                }, {
                    "es",
                    "es_Latn_ES",
                    "es"
                }, {
                    "et",
                    "et_Latn_EE",
                    "et"
                }, {
                    "eu",
                    "eu_Latn_ES",
                    "eu"
                }, {
                    "fa",
                    "fa_Arab_IR",
                    "fa"
                }, {
                    "fi",
                    "fi_Latn_FI",
                    "fi"
                }, {
                    "fil",
                    "fil_Latn_PH",
                    "fil"
                }, {
                    "fj",
                    "fj_Latn_FJ",
                    "fj"
                }, {
                    "fo",
                    "fo_Latn_FO",
                    "fo"
                }, {
                    "fr",
                    "fr_Latn_FR",
                    "fr"
                }, {
                    "fur",
                    "fur_Latn_IT",
                    "fur"
                }, {
                    "ga",
                    "ga_Latn_IE",
                    "ga"
                }, {
                    "gaa",
                    "gaa_Latn_GH",
                    "gaa"
                }, {
                    "gl",
                    "gl_Latn_ES",
                    "gl"
                }, {
                    "gn",
                    "gn_Latn_PY",
                    "gn"
                }, {
                    "gu",
                    "gu_Gujr_IN",
                    "gu"
                }, {
                    "ha",
                    "ha_Latn_NG",
                    "ha"
                }, {
                    "haw",
                    "haw_Latn_US",
                    "haw"
                }, {
                    "he",
                    "he_Hebr_IL",
                    "he"
                }, {
                    "hi",
                    "hi_Deva_IN",
                    "hi"
                }, {
                    "hr",
                    "hr_Latn_HR",
                    "hr"
                }, {
                    "ht",
                    "ht_Latn_HT",
                    "ht"
                }, {
                    "hu",
                    "hu_Latn_HU",
                    "hu"
                }, {
                    "hy",
                    "hy_Armn_AM",
                    "hy"
                }, {
                    "id",
                    "id_Latn_ID",
                    "id"
                }, {
                    "ig",
                    "ig_Latn_NG",
                    "ig"
                }, {
                    "ii",
                    "ii_Yiii_CN",
                    "ii"
                }, {
                    "is",
                    "is_Latn_IS",
                    "is"
                }, {
                    "it",
                    "it_Latn_IT",
                    "it"
                }, {
                    "ja",
                    "ja_Jpan_JP",
                    "ja"
                }, {
                    "ka",
                    "ka_Geor_GE",
                    "ka"
                }, {
                    "kaj",
                    "kaj_Latn_NG",
                    "kaj"
                }, {
                    "kam",
                    "kam_Latn_KE",
                    "kam"
                }, {
                    "kk",
                    "kk_Cyrl_KZ",
                    "kk"
                }, {
                    "kl",
                    "kl_Latn_GL",
                    "kl"
                }, {
                    "km",
                    "km_Khmr_KH",
                    "km"
                }, {
                    "kn",
                    "kn_Knda_IN",
                    "kn"
                }, {
                    "ko",
                    "ko_Kore_KR",
                    "ko"
                }, {
                    "kok",
                    "kok_Deva_IN",
                    "kok"
                }, {
                    "kpe",
                    "kpe_Latn_LR",
                    "kpe"
                }, {
                    "ku",
                    "ku_Latn_TR",
                    "ku"
                }, {
                    "ky",
                    "ky_Cyrl_KG",
                    "ky"
                }, {
                    "la",
                    "la_Latn_VA",
                    "la"
                }, {
                    "ln",
                    "ln_Latn_CD",
                    "ln"
                }, {
                    "lo",
                    "lo_Laoo_LA",
                    "lo"
                }, {
                    "lt",
                    "lt_Latn_LT",
                    "lt"
                }, {
                    "lv",
                    "lv_Latn_LV",
                    "lv"
                }, {
                    "mg",
                    "mg_Latn_MG",
                    "mg"
                }, {
                    "mh",
                    "mh_Latn_MH",
                    "mh"
                }, {
                    "mk",
                    "mk_Cyrl_MK",
                    "mk"
                }, {
                    "ml",
                    "ml_Mlym_IN",
                    "ml"
                }, {
                    "mn",
                    "mn_Cyrl_MN",
                    "mn"
                }, {
                    "mr",
                    "mr_Deva_IN",
                    "mr"
                }, {
                    "ms",
                    "ms_Latn_MY",
                    "ms"
                }, {
                    "mt",
                    "mt_Latn_MT",
                    "mt"
                }, {
                    "my",
                    "my_Mymr_MM",
                    "my"
                }, {
                    "na",
                    "na_Latn_NR",
                    "na"
                }, {
                    "ne",
                    "ne_Deva_NP",
                    "ne"
                }, {
                    "niu",
                    "niu_Latn_NU",
                    "niu"
                }, {
                    "nl",
                    "nl_Latn_NL",
                    "nl"
                }, {
                    "nn",
                    "nn_Latn_NO",
                    "nn"
                }, {
                    "nr",
                    "nr_Latn_ZA",
                    "nr"
                }, {
                    "nso",
                    "nso_Latn_ZA",
                    "nso"
                }, {
                    "om",
                    "om_Latn_ET",
                    "om"
                }, {
                    "or",
                    "or_Orya_IN",
                    "or"
                }, {
                    "pa",
                    "pa_Guru_IN",
                    "pa"
                }, {
                    "pa_Arab",
                    "pa_Arab_PK",
                    "pa_PK"
                }, {
                    "pa_PK",
                    "pa_Arab_PK",
                    "pa_PK"
                }, {
                    "pap",
                    "pap_Latn_AW",
                    "pap"
                }, {
                    "pau",
                    "pau_Latn_PW",
                    "pau"
                }, {
                    "pl",
                    "pl_Latn_PL",
                    "pl"
                }, {
                    "ps",
                    "ps_Arab_AF",
                    "ps"
                }, {
                    "pt",
                    "pt_Latn_BR",
                    "pt"
                }, {
                    "rn",
                    "rn_Latn_BI",
                    "rn"
                }, {
                    "ro",
                    "ro_Latn_RO",
                    "ro"
                }, {
                    "ru",
                    "ru_Cyrl_RU",
                    "ru"
                }, {
                    "rw",
                    "rw_Latn_RW",
                    "rw"
                }, {
                    "sa",
                    "sa_Deva_IN",
                    "sa"
                }, {
                    "se",
                    "se_Latn_NO",
                    "se"
                }, {
                    "sg",
                    "sg_Latn_CF",
                    "sg"
                }, {
                    "si",
                    "si_Sinh_LK",
                    "si"
                }, {
                    "sid",
                    "sid_Latn_ET",
                    "sid"
                }, {
                    "sk",
                    "sk_Latn_SK",
                    "sk"
                }, {
                    "sl",
                    "sl_Latn_SI",
                    "sl"
                }, {
                    "sm",
                    "sm_Latn_WS",
                    "sm"
                }, {
                    "so",
                    "so_Latn_SO",
                    "so"
                }, {
                    "sq",
                    "sq_Latn_AL",
                    "sq"
                }, {
                    "sr",
                    "sr_Cyrl_RS",
                    "sr"
                }, {
                    "ss",
                    "ss_Latn_ZA",
                    "ss"
                }, {
                    "st",
                    "st_Latn_ZA",
                    "st"
                }, {
                    "sv",
                    "sv_Latn_SE",
                    "sv"
                }, {
                    "sw",
                    "sw_Latn_TZ",
                    "sw"
                }, {
                    "ta",
                    "ta_Taml_IN",
                    "ta"
                }, {
                    "te",
                    "te_Telu_IN",
                    "te"
                }, {
                    "tet",
                    "tet_Latn_TL",
                    "tet"
                }, {
                    "tg",
                    "tg_Cyrl_TJ",
                    "tg"
                }, {
                    "th",
                    "th_Thai_TH",
                    "th"
                }, {
                    "ti",
                    "ti_Ethi_ET",
                    "ti"
                }, {
                    "tig",
                    "tig_Ethi_ER",
                    "tig"
                }, {
                    "tk",
                    "tk_Latn_TM",
                    "tk"
                }, {
                    "tkl",
                    "tkl_Latn_TK",
                    "tkl"
                }, {
                    "tn",
                    "tn_Latn_ZA",
                    "tn"
                }, {
                    "to",
                    "to_Latn_TO",
                    "to"
                }, {
                    "tpi",
                    "tpi_Latn_PG",
                    "tpi"
                }, {
                    "tr",
                    "tr_Latn_TR",
                    "tr"
                }, {
                    "ts",
                    "ts_Latn_ZA",
                    "ts"
                }, {
                    "tt",
                    "tt_Cyrl_RU",
                    "tt"
                }, {
                    "tvl",
                    "tvl_Latn_TV",
                    "tvl"
                }, {
                    "ty",
                    "ty_Latn_PF",
                    "ty"
                }, {
                    "uk",
                    "uk_Cyrl_UA",
                    "uk"
                }, {
                    "und",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_AD",
                    "ca_Latn_AD",
                    "ca_AD"
                }, {
                    "und_AE",
                    "ar_Arab_AE",
                    "ar_AE"
                }, {
                    "und_AF",
                    "fa_Arab_AF",
                    "fa_AF"
                }, {
                    "und_AL",
                    "sq_Latn_AL",
                    "sq"
                }, {
                    "und_AM",
                    "hy_Armn_AM",
                    "hy"
                }, {
                    "und_AO",
                    "pt_Latn_AO",
                    "pt_AO"
                }, {
                    "und_AR",
                    "es_Latn_AR",
                    "es_AR"
                }, {
                    "und_AS",
                    "sm_Latn_AS",
                    "sm_AS"
                }, {
                    "und_AT",
                    "de_Latn_AT",
                    "de_AT"
                }, {
                    "und_AW",
                    "nl_Latn_AW",
                    "nl_AW"
                }, {
                    "und_AX",
                    "sv_Latn_AX",
                    "sv_AX"
                }, {
                    "und_AZ",
                    "az_Latn_AZ",
                    "az"
                }, {
                    "und_Arab",
                    "ar_Arab_EG",
                    "ar"
                }, {
                    "und_Arab_IN",
                    "ur_Arab_IN",
                    "ur_IN"
                }, {
                    "und_Arab_PK",
                    "ur_Arab_PK",
                    "ur"
                }, {
                    "und_Arab_SN",
                    "ar_Arab_SN",
                    "ar_SN"
                }, {
                    "und_Armn",
                    "hy_Armn_AM",
                    "hy"
                }, {
                    "und_BA",
                    "bs_Latn_BA",
                    "bs"
                }, {
                    "und_BD",
                    "bn_Beng_BD",
                    "bn"
                }, {
                    "und_BE",
                    "nl_Latn_BE",
                    "nl_BE"
                }, {
                    "und_BF",
                    "fr_Latn_BF",
                    "fr_BF"
                }, {
                    "und_BG",
                    "bg_Cyrl_BG",
                    "bg"
                }, {
                    "und_BH",
                    "ar_Arab_BH",
                    "ar_BH"
                }, {
                    "und_BI",
                    "rn_Latn_BI",
                    "rn"
                }, {
                    "und_BJ",
                    "fr_Latn_BJ",
                    "fr_BJ"
                }, {
                    "und_BN",
                    "ms_Latn_BN",
                    "ms_BN"
                }, {
                    "und_BO",
                    "es_Latn_BO",
                    "es_BO"
                }, {
                    "und_BR",
                    "pt_Latn_BR",
                    "pt"
                }, {
                    "und_BT",
                    "dz_Tibt_BT",
                    "dz"
                }, {
                    "und_BY",
                    "be_Cyrl_BY",
                    "be"
                }, {
                    "und_Beng",
                    "bn_Beng_BD",
                    "bn"
                }, {
                    "und_Beng_IN",
                    "bn_Beng_IN",
                    "bn_IN"
                }, {
                    "und_CD",
                    "sw_Latn_CD",
                    "sw_CD"
                }, {
                    "und_CF",
                    "fr_Latn_CF",
                    "fr_CF"
                }, {
                    "und_CG",
                    "fr_Latn_CG",
                    "fr_CG"
                }, {
                    "und_CH",
                    "de_Latn_CH",
                    "de_CH"
                }, {
                    "und_CI",
                    "fr_Latn_CI",
                    "fr_CI"
                }, {
                    "und_CL",
                    "es_Latn_CL",
                    "es_CL"
                }, {
                    "und_CM",
                    "fr_Latn_CM",
                    "fr_CM"
                }, {
                    "und_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_CO",
                    "es_Latn_CO",
                    "es_CO"
                }, {
                    "und_CR",
                    "es_Latn_CR",
                    "es_CR"
                }, {
                    "und_CU",
                    "es_Latn_CU",
                    "es_CU"
                }, {
                    "und_CV",
                    "pt_Latn_CV",
                    "pt_CV"
                }, {
                    "und_CY",
                    "el_Grek_CY",
                    "el_CY"
                }, {
                    "und_CZ",
                    "cs_Latn_CZ",
                    "cs"
                }, {
                    "und_Cyrl",
                    "ru_Cyrl_RU",
                    "ru"
                }, {
                    "und_Cyrl_KZ",
                    "ru_Cyrl_KZ",
                    "ru_KZ"
                }, {
                    "und_DE",
                    "de_Latn_DE",
                    "de"
                }, {
                    "und_DJ",
                    "aa_Latn_DJ",
                    "aa_DJ"
                }, {
                    "und_DK",
                    "da_Latn_DK",
                    "da"
                }, {
                    "und_DO",
                    "es_Latn_DO",
                    "es_DO"
                }, {
                    "und_DZ",
                    "ar_Arab_DZ",
                    "ar_DZ"
                }, {
                    "und_Deva",
                    "hi_Deva_IN",
                    "hi"
                }, {
                    "und_EC",
                    "es_Latn_EC",
                    "es_EC"
                }, {
                    "und_EE",
                    "et_Latn_EE",
                    "et"
                }, {
                    "und_EG",
                    "ar_Arab_EG",
                    "ar"
                }, {
                    "und_EH",
                    "ar_Arab_EH",
                    "ar_EH"
                }, {
                    "und_ER",
                    "ti_Ethi_ER",
                    "ti_ER"
                }, {
                    "und_ES",
                    "es_Latn_ES",
                    "es"
                }, {
                    "und_ET",
                    "am_Ethi_ET",
                    "am"
                }, {
                    "und_Ethi",
                    "am_Ethi_ET",
                    "am"
                }, {
                    "und_Ethi_ER",
                    "am_Ethi_ER",
                    "am_ER"
                }, {
                    "und_FI",
                    "fi_Latn_FI",
                    "fi"
                }, {
                    "und_FM",
                    "en_Latn_FM",
                    "en_FM"
                }, {
                    "und_FO",
                    "fo_Latn_FO",
                    "fo"
                }, {
                    "und_FR",
                    "fr_Latn_FR",
                    "fr"
                }, {
                    "und_GA",
                    "fr_Latn_GA",
                    "fr_GA"
                }, {
                    "und_GE",
                    "ka_Geor_GE",
                    "ka"
                }, {
                    "und_GF",
                    "fr_Latn_GF",
                    "fr_GF"
                }, {
                    "und_GL",
                    "kl_Latn_GL",
                    "kl"
                }, {
                    "und_GN",
                    "fr_Latn_GN",
                    "fr_GN"
                }, {
                    "und_GP",
                    "fr_Latn_GP",
                    "fr_GP"
                }, {
                    "und_GQ",
                    "es_Latn_GQ",
                    "es_GQ"
                }, {
                    "und_GR",
                    "el_Grek_GR",
                    "el"
                }, {
                    "und_GT",
                    "es_Latn_GT",
                    "es_GT"
                }, {
                    "und_GU",
                    "en_Latn_GU",
                    "en_GU"
                }, {
                    "und_GW",
                    "pt_Latn_GW",
                    "pt_GW"
                }, {
                    "und_Geor",
                    "ka_Geor_GE",
                    "ka"
                }, {
                    "und_Grek",
                    "el_Grek_GR",
                    "el"
                }, {
                    "und_Gujr",
                    "gu_Gujr_IN",
                    "gu"
                }, {
                    "und_Guru",
                    "pa_Guru_IN",
                    "pa"
                }, {
                    "und_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "und_HN",
                    "es_Latn_HN",
                    "es_HN"
                }, {
                    "und_HR",
                    "hr_Latn_HR",
                    "hr"
                }, {
                    "und_HT",
                    "ht_Latn_HT",
                    "ht"
                }, {
                    "und_HU",
                    "hu_Latn_HU",
                    "hu"
                }, {
                    "und_Hani",
                    "zh_Hani_CN",
                    "zh_Hani"
                }, {
                    "und_Hans",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_Hant",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Hebr",
                    "he_Hebr_IL",
                    "he"
                }, {
                    "und_ID",
                    "id_Latn_ID",
                    "id"
                }, {
                    "und_IL",
                    "he_Hebr_IL",
                    "he"
                }, {
                    "und_IN",
                    "hi_Deva_IN",
                    "hi"
                }, {
                    "und_IQ",
                    "ar_Arab_IQ",
                    "ar_IQ"
                }, {
                    "und_IR",
                    "fa_Arab_IR",
                    "fa"
                }, {
                    "und_IS",
                    "is_Latn_IS",
                    "is"
                }, {
                    "und_IT",
                    "it_Latn_IT",
                    "it"
                }, {
                    "und_JO",
                    "ar_Arab_JO",
                    "ar_JO"
                }, {
                    "und_JP",
                    "ja_Jpan_JP",
                    "ja"
                }, {
                    "und_Jpan",
                    "ja_Jpan_JP",
                    "ja"
                }, {
                    "und_KG",
                    "ky_Cyrl_KG",
                    "ky"
                }, {
                    "und_KH",
                    "km_Khmr_KH",
                    "km"
                }, {
                    "und_KM",
                    "ar_Arab_KM",
                    "ar_KM"
                }, {
                    "und_KP",
                    "ko_Kore_KP",
                    "ko_KP"
                }, {
                    "und_KR",
                    "ko_Kore_KR",
                    "ko"
                }, {
                    "und_KW",
                    "ar_Arab_KW",
                    "ar_KW"
                }, {
                    "und_KZ",
                    "ru_Cyrl_KZ",
                    "ru_KZ"
                }, {
                    "und_Khmr",
                    "km_Khmr_KH",
                    "km"
                }, {
                    "und_Knda",
                    "kn_Knda_IN",
                    "kn"
                }, {
                    "und_Kore",
                    "ko_Kore_KR",
                    "ko"
                }, {
                    "und_LA",
                    "lo_Laoo_LA",
                    "lo"
                }, {
                    "und_LB",
                    "ar_Arab_LB",
                    "ar_LB"
                }, {
                    "und_LI",
                    "de_Latn_LI",
                    "de_LI"
                }, {
                    "und_LK",
                    "si_Sinh_LK",
                    "si"
                }, {
                    "und_LS",
                    "st_Latn_LS",
                    "st_LS"
                }, {
                    "und_LT",
                    "lt_Latn_LT",
                    "lt"
                }, {
                    "und_LU",
                    "fr_Latn_LU",
                    "fr_LU"
                }, {
                    "und_LV",
                    "lv_Latn_LV",
                    "lv"
                }, {
                    "und_LY",
                    "ar_Arab_LY",
                    "ar_LY"
                }, {
                    "und_Laoo",
                    "lo_Laoo_LA",
                    "lo"
                }, {
                    "und_Latn_ES",
                    "es_Latn_ES",
                    "es"
                }, {
                    "und_Latn_ET",
                    "en_Latn_ET",
                    "en_ET"
                }, {
                    "und_Latn_GB",
                    "en_Latn_GB",
                    "en_GB"
                }, {
                    "und_Latn_GH",
                    "ak_Latn_GH",
                    "ak"
                }, {
                    "und_Latn_ID",
                    "id_Latn_ID",
                    "id"
                }, {
                    "und_Latn_IT",
                    "it_Latn_IT",
                    "it"
                }, {
                    "und_Latn_NG",
                    "en_Latn_NG",
                    "en_NG"
                }, {
                    "und_Latn_TR",
                    "tr_Latn_TR",
                    "tr"
                }, {
                    "und_Latn_ZA",
                    "en_Latn_ZA",
                    "en_ZA"
                }, {
                    "und_MA",
                    "ar_Arab_MA",
                    "ar_MA"
                }, {
                    "und_MC",
                    "fr_Latn_MC",
                    "fr_MC"
                }, {
                    "und_MD",
                    "ro_Latn_MD",
                    "ro_MD"
                }, {
                    "und_ME",
                    "sr_Latn_ME",
                    "sr_ME"
                }, {
                    "und_MG",
                    "mg_Latn_MG",
                    "mg"
                }, {
                    "und_MK",
                    "mk_Cyrl_MK",
                    "mk"
                }, {
                    "und_ML",
                    "bm_Latn_ML",
                    "bm"
                }, {
                    "und_MM",
                    "my_Mymr_MM",
                    "my"
                }, {
                    "und_MN",
                    "mn_Cyrl_MN",
                    "mn"
                }, {
                    "und_MO",
                    "zh_Hant_MO",
                    "zh_MO"
                }, {
                    "und_MQ",
                    "fr_Latn_MQ",
                    "fr_MQ"
                }, {
                    "und_MR",
                    "ar_Arab_MR",
                    "ar_MR"
                }, {
                    "und_MT",
                    "mt_Latn_MT",
                    "mt"
                }, {
                    "und_MV",
                    "dv_Thaa_MV",
                    "dv"
                }, {
                    "und_MX",
                    "es_Latn_MX",
                    "es_MX"
                }, {
                    "und_MY",
                    "ms_Latn_MY",
                    "ms"
                }, {
                    "und_MZ",
                    "pt_Latn_MZ",
                    "pt_MZ"
                }, {
                    "und_Mlym",
                    "ml_Mlym_IN",
                    "ml"
                }, {
                    "und_Mymr",
                    "my_Mymr_MM",
                    "my"
                }, {
                    "und_NC",
                    "fr_Latn_NC",
                    "fr_NC"
                }, {
                    "und_NE",
                    "ha_Latn_NE",
                    "ha_NE"
                }, {
                    "und_NG",
                    "en_Latn_NG",
                    "en_NG"
                }, {
                    "und_NI",
                    "es_Latn_NI",
                    "es_NI"
                }, {
                    "und_NL",
                    "nl_Latn_NL",
                    "nl"
                }, {
                    "und_NO",
                    "no_Latn_NO",  // Android patch: Replace nb with no.
                    "no"
                }, {
                    "und_NP",
                    "ne_Deva_NP",
                    "ne"
                }, {
                    "und_NR",
                    "en_Latn_NR",
                    "en_NR"
                }, {
                    "und_OM",
                    "ar_Arab_OM",
                    "ar_OM"
                }, {
                    "und_Orya",
                    "or_Orya_IN",
                    "or"
                }, {
                    "und_PA",
                    "es_Latn_PA",
                    "es_PA"
                }, {
                    "und_PE",
                    "es_Latn_PE",
                    "es_PE"
                }, {
                    "und_PF",
                    "fr_Latn_PF",
                    "fr_PF"
                }, {
                    "und_PG",
                    "tpi_Latn_PG",
                    "tpi"
                }, {
                    "und_PH",
                    "fil_Latn_PH",
                    "fil"
                }, {
                    "und_PL",
                    "pl_Latn_PL",
                    "pl"
                }, {
                    "und_PM",
                    "fr_Latn_PM",
                    "fr_PM"
                }, {
                    "und_PR",
                    "es_Latn_PR",
                    "es_PR"
                }, {
                    "und_PS",
                    "ar_Arab_PS",
                    "ar_PS"
                }, {
                    "und_PT",
                    "pt_Latn_PT",
                    "pt_PT"
                }, {
                    "und_PW",
                    "pau_Latn_PW",
                    "pau"
                }, {
                    "und_PY",
                    "gn_Latn_PY",
                    "gn"
                }, {
                    "und_QA",
                    "ar_Arab_QA",
                    "ar_QA"
                }, {
                    "und_RE",
                    "fr_Latn_RE",
                    "fr_RE"
                }, {
                    "und_RO",
                    "ro_Latn_RO",
                    "ro"
                }, {
                    "und_RS",
                    "sr_Cyrl_RS",
                    "sr"
                }, {
                    "und_RU",
                    "ru_Cyrl_RU",
                    "ru"
                }, {
                    "und_RW",
                    "rw_Latn_RW",
                    "rw"
                }, {
                    "und_SA",
                    "ar_Arab_SA",
                    "ar_SA"
                }, {
                    "und_SD",
                    "ar_Arab_SD",
                    "ar_SD"
                }, {
                    "und_SE",
                    "sv_Latn_SE",
                    "sv"
                }, {
                    "und_SG",
                    "en_Latn_SG",
                    "en_SG"
                }, {
                    "und_SI",
                    "sl_Latn_SI",
                    "sl"
                }, {
                    "und_SJ",
                    "no_Latn_SJ",  // Android patch: Replace nb with no.
                    "no_SJ"
                }, {
                    "und_SK",
                    "sk_Latn_SK",
                    "sk"
                }, {
                    "und_SM",
                    "it_Latn_SM",
                    "it_SM"
                }, {
                    "und_SN",
                    "fr_Latn_SN",
                    "fr_SN"
                }, {
                    "und_SO",
                    "so_Latn_SO",
                    "so"
                }, {
                    "und_SR",
                    "nl_Latn_SR",
                    "nl_SR"
                }, {
                    "und_ST",
                    "pt_Latn_ST",
                    "pt_ST"
                }, {
                    "und_SV",
                    "es_Latn_SV",
                    "es_SV"
                }, {
                    "und_SY",
                    "ar_Arab_SY",
                    "ar_SY"
                }, {
                    "und_Sinh",
                    "si_Sinh_LK",
                    "si"
                }, {
                    "und_Syrc",
                    "syr_Syrc_IQ",
                    "syr"
                }, {
                    "und_TD",
                    "fr_Latn_TD",
                    "fr_TD"
                }, {
                    "und_TG",
                    "fr_Latn_TG",
                    "fr_TG"
                }, {
                    "und_TH",
                    "th_Thai_TH",
                    "th"
                }, {
                    "und_TJ",
                    "tg_Cyrl_TJ",
                    "tg"
                }, {
                    "und_TK",
                    "tkl_Latn_TK",
                    "tkl"
                }, {
                    "und_TL",
                    "pt_Latn_TL",
                    "pt_TL"
                }, {
                    "und_TM",
                    "tk_Latn_TM",
                    "tk"
                }, {
                    "und_TN",
                    "ar_Arab_TN",
                    "ar_TN"
                }, {
                    "und_TO",
                    "to_Latn_TO",
                    "to"
                }, {
                    "und_TR",
                    "tr_Latn_TR",
                    "tr"
                }, {
                    "und_TV",
                    "tvl_Latn_TV",
                    "tvl"
                }, {
                    "und_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Taml",
                    "ta_Taml_IN",
                    "ta"
                }, {
                    "und_Telu",
                    "te_Telu_IN",
                    "te"
                }, {
                    "und_Thaa",
                    "dv_Thaa_MV",
                    "dv"
                }, {
                    "und_Thai",
                    "th_Thai_TH",
                    "th"
                }, {
                    "und_Tibt",
                    "bo_Tibt_CN",
                    "bo"
                }, {
                    "und_UA",
                    "uk_Cyrl_UA",
                    "uk"
                }, {
                    "und_UY",
                    "es_Latn_UY",
                    "es_UY"
                }, {
                    "und_UZ",
                    "uz_Latn_UZ",
                    "uz"
                }, {
                    "und_VA",
                    "it_Latn_VA",
                    "it_VA"
                }, {
                    "und_VE",
                    "es_Latn_VE",
                    "es_VE"
                }, {
                    "und_VN",
                    "vi_Latn_VN",
                    "vi"
                }, {
                    "und_VU",
                    "bi_Latn_VU",
                    "bi"
                }, {
                    "und_WF",
                    "fr_Latn_WF",
                    "fr_WF"
                }, {
                    "und_WS",
                    "sm_Latn_WS",
                    "sm"
                }, {
                    "und_YE",
                    "ar_Arab_YE",
                    "ar_YE"
                }, {
                    "und_YT",
                    "fr_Latn_YT",
                    "fr_YT"
                }, {
                    "und_Yiii",
                    "ii_Yiii_CN",
                    "ii"
                }, {
                    "ur",
                    "ur_Arab_PK",
                    "ur"
                }, {
                    "uz",
                    "uz_Latn_UZ",
                    "uz"
                }, {
                    "uz_AF",
                    "uz_Arab_AF",
                    "uz_AF"
                }, {
                    "uz_Arab",
                    "uz_Arab_AF",
                    "uz_AF"
                }, {
                    "ve",
                    "ve_Latn_ZA",
                    "ve"
                }, {
                    "vi",
                    "vi_Latn_VN",
                    "vi"
                }, {
                    "wal",
                    "wal_Ethi_ET",
                    "wal"
                }, {
                    "wo",
                    "wo_Latn_SN",
                    "wo"
                }, {
                    "wo_SN",
                    "wo_Latn_SN",
                    "wo"
                }, {
                    "xh",
                    "xh_Latn_ZA",
                    "xh"
                }, {
                    "yo",
                    "yo_Latn_NG",
                    "yo"
                }, {
                    "zh",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "zh_Hani",
                    "zh_Hani_CN",
                    "zh_Hani"
                }, {
                    "zh_Hant",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zh_MO",
                    "zh_Hant_MO",
                    "zh_MO"
                }, {
                    "zh_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zu",
                    "zu_Latn_ZA",
                    "zu"
                }, {
                    "und",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_ZZ",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "und_AQ",
                    "und_Latn_AQ",
                    "und_AQ"
                }, {
                    "und_Zzzz",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_Zzzz_ZZ",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_Zzzz_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_Zzzz_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Zzzz_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "und_Zzzz_AQ",
                    "und_Latn_AQ",
                    "und_AQ"
                }, {
                    "und_Latn",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_Latn_ZZ",
                    "en_Latn_US",
                    "en"
                }, {
                    "und_Latn_CN",
                    "za_Latn_CN",
                    "za"
                }, {
                    "und_Latn_TW",
                    "trv_Latn_TW",
                    "trv"
                }, {
                    "und_Latn_HK",
                    "zh_Latn_HK",
                    "zh_Latn_HK"
                }, {
                    "und_Latn_AQ",
                    "und_Latn_AQ",
                    "und_AQ"
                }, {
                    "und_Hans",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_Hans_ZZ",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_Hans_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "und_Hans_TW",
                    "zh_Hans_TW",
                    "zh_Hans_TW"
                }, {
                    "und_Hans_HK",
                    "zh_Hans_HK",
                    "zh_Hans_HK"
                }, {
                    "und_Hans_AQ",
                    "zh_Hans_AQ",
                    "zh_AQ"
                }, {
                    "und_Hant",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Hant_ZZ",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Hant_CN",
                    "yue_Hant_CN",
                    "yue_Hant_CN"
                }, {
                    "und_Hant_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "und_Hant_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "und_Hant_AQ",
                    "zh_Hant_AQ",
                    "zh_Hant_AQ"
                }, {
                    "und_Moon",
                    "en_Moon_US",
                    "en_Moon"
                }, {
                    "und_Moon_ZZ",
                    "en_Moon_US",
                    "en_Moon"
                }, {
                    "und_Moon_CN",
                    "zh_Moon_CN",
                    "zh_Moon"
                }, {
                    "und_Moon_TW",
                    "zh_Moon_TW",
                    "zh_Moon_TW"
                }, {
                    "und_Moon_HK",
                    "zh_Moon_HK",
                    "zh_Moon_HK"
                }, {
                    "und_Moon_AQ",
                    "und_Moon_AQ",
                    "und_Moon_AQ"
                }, {
                    "es",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_ZZ",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_CN",
                    "es_Latn_CN",
                    "es_CN"
                }, {
                    "es_TW",
                    "es_Latn_TW",
                    "es_TW"
                }, {
                    "es_HK",
                    "es_Latn_HK",
                    "es_HK"
                }, {
                    "es_AQ",
                    "es_Latn_AQ",
                    "es_AQ"
                }, {
                    "es_Zzzz",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_Zzzz_ZZ",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_Zzzz_CN",
                    "es_Latn_CN",
                    "es_CN"
                }, {
                    "es_Zzzz_TW",
                    "es_Latn_TW",
                    "es_TW"
                }, {
                    "es_Zzzz_HK",
                    "es_Latn_HK",
                    "es_HK"
                }, {
                    "es_Zzzz_AQ",
                    "es_Latn_AQ",
                    "es_AQ"
                }, {
                    "es_Latn",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_Latn_ZZ",
                    "es_Latn_ES",
                    "es"
                }, {
                    "es_Latn_CN",
                    "es_Latn_CN",
                    "es_CN"
                }, {
                    "es_Latn_TW",
                    "es_Latn_TW",
                    "es_TW"
                }, {
                    "es_Latn_HK",
                    "es_Latn_HK",
                    "es_HK"
                }, {
                    "es_Latn_AQ",
                    "es_Latn_AQ",
                    "es_AQ"
                }, {
                    "es_Hans",
                    "es_Hans_ES",
                    "es_Hans"
                }, {
                    "es_Hans_ZZ",
                    "es_Hans_ES",
                    "es_Hans"
                }, {
                    "es_Hans_CN",
                    "es_Hans_CN",
                    "es_Hans_CN"
                }, {
                    "es_Hans_TW",
                    "es_Hans_TW",
                    "es_Hans_TW"
                }, {
                    "es_Hans_HK",
                    "es_Hans_HK",
                    "es_Hans_HK"
                }, {
                    "es_Hans_AQ",
                    "es_Hans_AQ",
                    "es_Hans_AQ"
                }, {
                    "es_Hant",
                    "es_Hant_ES",
                    "es_Hant"
                }, {
                    "es_Hant_ZZ",
                    "es_Hant_ES",
                    "es_Hant"
                }, {
                    "es_Hant_CN",
                    "es_Hant_CN",
                    "es_Hant_CN"
                }, {
                    "es_Hant_TW",
                    "es_Hant_TW",
                    "es_Hant_TW"
                }, {
                    "es_Hant_HK",
                    "es_Hant_HK",
                    "es_Hant_HK"
                }, {
                    "es_Hant_AQ",
                    "es_Hant_AQ",
                    "es_Hant_AQ"
                }, {
                    "es_Moon",
                    "es_Moon_ES",
                    "es_Moon"
                }, {
                    "es_Moon_ZZ",
                    "es_Moon_ES",
                    "es_Moon"
                }, {
                    "es_Moon_CN",
                    "es_Moon_CN",
                    "es_Moon_CN"
                }, {
                    "es_Moon_TW",
                    "es_Moon_TW",
                    "es_Moon_TW"
                }, {
                    "es_Moon_HK",
                    "es_Moon_HK",
                    "es_Moon_HK"
                }, {
                    "es_Moon_AQ",
                    "es_Moon_AQ",
                    "es_Moon_AQ"
                }, {
                    "zh",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_ZZ",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zh_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "zh_AQ",
                    "zh_Hans_AQ",
                    "zh_AQ"
                }, {
                    "zh_Zzzz",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_Zzzz_ZZ",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_Zzzz_CN",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_Zzzz_TW",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zh_Zzzz_HK",
                    "zh_Hant_HK",
                    "zh_HK"
                }, {
                    "zh_Zzzz_AQ",
                    "zh_Hans_AQ",
                    "zh_AQ"
                }, {
                    "zh_Latn",
                    "zh_Latn_CN",
                    "zh_Latn"
                }, {
                    "zh_Latn_ZZ",
                    "zh_Latn_CN",
                    "zh_Latn"
                }, {
                    "zh_Latn_CN",
                    "zh_Latn_CN",
                    "zh_Latn"
                }, {
                    "zh_Latn_TW",
                    "zh_Latn_TW",
                    "zh_Latn_TW"
                }, {
                    "zh_Latn_HK",
                    "zh_Latn_HK",
                    "zh_Latn_HK"
                }, {
                    "zh_Latn_AQ",
                    "zh_Latn_AQ",
                    "zh_Latn_AQ"
                }, {
                    "zh_Hans",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_Hans_ZZ",
                    "zh_Hans_CN",
                    "zh"
                }, {
                    "zh_Hans_TW",
                    "zh_Hans_TW",
                    "zh_Hans_TW"
                }, {
                    "zh_Hans_HK",
                    "zh_Hans_HK",
                    "zh_Hans_HK"
                }, {
                    "zh_Hans_AQ",
                    "zh_Hans_AQ",
                    "zh_AQ"
                }, {
                    "zh_Hant",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zh_Hant_ZZ",
                    "zh_Hant_TW",
                    "zh_TW"
                }, {
                    "zh_Hant_CN",
                    "zh_Hant_CN",
                    "zh_Hant_CN"
                }, {
                    "zh_Hant_AQ",
                    "zh_Hant_AQ",
                    "zh_Hant_AQ"
                }, {
                    "zh_Moon",
                    "zh_Moon_CN",
                    "zh_Moon"
                }, {
                    "zh_Moon_ZZ",
                    "zh_Moon_CN",
                    "zh_Moon"
                }, {
                    "zh_Moon_CN",
                    "zh_Moon_CN",
                    "zh_Moon"
                }, {
                    "zh_Moon_TW",
                    "zh_Moon_TW",
                    "zh_Moon_TW"
                }, {
                    "zh_Moon_HK",
                    "zh_Moon_HK",
                    "zh_Moon_HK"
                }, {
                    "zh_Moon_AQ",
                    "zh_Moon_AQ",
                    "zh_Moon_AQ"
                }, {
                    "art",
                    "",
                    ""
                }, {
                    "art_ZZ",
                    "",
                    ""
                }, {
                    "art_CN",
                    "",
                    ""
                }, {
                    "art_TW",
                    "",
                    ""
                }, {
                    "art_HK",
                    "",
                    ""
                }, {
                    "art_AQ",
                    "",
                    ""
                }, {
                    "art_Zzzz",
                    "",
                    ""
                }, {
                    "art_Zzzz_ZZ",
                    "",
                    ""
                }, {
                    "art_Zzzz_CN",
                    "",
                    ""
                }, {
                    "art_Zzzz_TW",
                    "",
                    ""
                }, {
                    "art_Zzzz_HK",
                    "",
                    ""
                }, {
                    "art_Zzzz_AQ",
                    "",
                    ""
                }, {
                    "art_Latn",
                    "",
                    ""
                }, {
                    "art_Latn_ZZ",
                    "",
                    ""
                }, {
                    "art_Latn_CN",
                    "",
                    ""
                }, {
                    "art_Latn_TW",
                    "",
                    ""
                }, {
                    "art_Latn_HK",
                    "",
                    ""
                }, {
                    "art_Latn_AQ",
                    "",
                    ""
                }, {
                    "art_Hans",
                    "",
                    ""
                }, {
                    "art_Hans_ZZ",
                    "",
                    ""
                }, {
                    "art_Hans_CN",
                    "",
                    ""
                }, {
                    "art_Hans_TW",
                    "",
                    ""
                }, {
                    "art_Hans_HK",
                    "",
                    ""
                }, {
                    "art_Hans_AQ",
                    "",
                    ""
                }, {
                    "art_Hant",
                    "",
                    ""
                }, {
                    "art_Hant_ZZ",
                    "",
                    ""
                }, {
                    "art_Hant_CN",
                    "",
                    ""
                }, {
                    "art_Hant_TW",
                    "",
                    ""
                }, {
                    "art_Hant_HK",
                    "",
                    ""
                }, {
                    "art_Hant_AQ",
                    "",
                    ""
                }, {
                    "art_Moon",
                    "",
                    ""
                }, {
                    "art_Moon_ZZ",
                    "",
                    ""
                }, {
                    "art_Moon_CN",
                    "",
                    ""
                }, {
                    "art_Moon_TW",
                    "",
                    ""
                }, {
                    "art_Moon_HK",
                    "",
                    ""
                }, {
                    "art_Moon_AQ",
                    "",
                    ""
                }
        };

        for (int i = 0; i < full_data.length; i++) {
            ULocale org = new ULocale(full_data[i][0]);
            ULocale res = ULocale.addLikelySubtags(org);
            String exp = full_data[i][1];
            if (exp.length() == 0) {
                if (!org.equals(res)) {
                    errln("Original: " + full_data[i][0] + " expected: " + exp + " - but got " + res.toString());
                }
            }
            else if (!res.toString().equals(exp)) {
                errln("Original: " + full_data[i][0] + " expected: " + exp + " - but got " + res.toString());
            }
        }

        for (int i = 0; i < full_data.length; i++) {
            String maximal = full_data[i][1];

            if (maximal.length() > 0) {
                ULocale org = new ULocale(maximal);
                ULocale res = ULocale.minimizeSubtags(org);
                String exp = full_data[i][2];
                if (exp.length() == 0) {
                    if (!org.equals(res)) {
                        errln("Original: " + full_data[i][1] + " expected: " + exp + " - but got " + res.toString());
                    }
                }
                else if (!res.toString().equals(exp)) {
                    errln("Original: " + full_data[i][1] + " expected: " + exp + " - but got " + res.toString());
                }
            }
        }
    }

    @Test
    public void TestCLDRVersion() {
        //VersionInfo zeroVersion = VersionInfo.getInstance(0, 0, 0, 0);
        VersionInfo testExpect;
        VersionInfo testCurrent;
        VersionInfo cldrVersion;

        cldrVersion = LocaleData.getCLDRVersion();

        TestFmwk.logln("uloc_getCLDRVersion() returned: '"+cldrVersion+"'");

        // why isn't this public for tests somewhere?
        final ClassLoader testLoader = ICUResourceBundleTest.class.getClassLoader();
        UResourceBundle bundle = UResourceBundle.getBundleInstance("android/icu/dev/data/testdata", ULocale.ROOT, testLoader);

        testExpect = VersionInfo.getInstance(bundle.getString("ExpectCLDRVersionAtLeast"));
        testCurrent = VersionInfo.getInstance(bundle.getString("CurrentCLDRVersion"));


        logln("(data) ExpectCLDRVersionAtLeast { "+testExpect+"");
        if(cldrVersion.compareTo(testExpect)<0) {
            errln("CLDR version is too old, expect at least "+testExpect+".");
        }

        int r = cldrVersion.compareTo(testCurrent);
        if ( r < 0 ) {
            logln("CLDR version is behind 'current' (for testdata/root.txt) "+testCurrent+". Some things may fail.\n");
        } else if ( r > 0) {
            logln("CLDR version is ahead of 'current' (for testdata/root.txt) "+testCurrent+". Some things may fail.\n");
        } else {
            // CLDR version is OK.
        }
    }

    @Test
    public void TestToLanguageTag() {
        final String[][] locale_to_langtag = {
                {"",            "und"},
                {"en",          "en"},
                {"en_US",       "en-US"},
                {"iw_IL",       "he-IL"},
                {"sr_Latn_SR",  "sr-Latn-SR"},
                {"en_US_POSIX@ca=japanese", "en-US-u-ca-japanese-va-posix"},
                {"en__POSIX",   "en-u-va-posix"},
                {"en_US_POSIX_VAR", "en-US-posix-x-lvariant-var"},  // variant POSIX_VAR is processed as regular variant
                {"en_US_VAR_POSIX", "en-US-x-lvariant-var-posix"},  // variant VAR_POSIX is processed as regular variant
                {"en_US_POSIX@va=posix2",   "en-US-u-va-posix2"},   // if keyword va=xxx already exists, variant POSIX is simply dropped
                {"und_555",     "und-555"},
                {"123",         "und"},
                {"%$#&",        "und"},
                {"_Latn",       "und-Latn"},
                {"_DE",         "und-DE"},
                {"und_FR",      "und-FR"},
                {"th_TH_TH",    "th-TH-x-lvariant-th"},
                {"bogus",       "bogus"},
                {"foooobarrr",  "und"},
                {"aa_BB_CYRL",  "aa-BB-x-lvariant-cyrl"},
                {"en_US_1234",  "en-US-1234"},
                {"en_US_VARIANTA_VARIANTB", "en-US-varianta-variantb"},
                {"en_US_VARIANTB_VARIANTA", "en-US-variantb-varianta"},
                {"ja__9876_5432",   "ja-9876-5432"},
                {"zh_Hant__VAR",    "zh-Hant-x-lvariant-var"},
                {"es__BADVARIANT_GOODVAR",  "es"},
                {"es__GOODVAR_BAD_BADVARIANT",  "es-goodvar-x-lvariant-bad"},
                {"en@calendar=gregorian",   "en-u-ca-gregory"},
                {"de@collation=phonebook;calendar=gregorian",   "de-u-ca-gregory-co-phonebk"},
                {"th@numbers=thai;z=extz;x=priv-use;a=exta",   "th-a-exta-u-nu-thai-z-extz-x-priv-use"},
                {"en@timezone=America/New_York;calendar=japanese",    "en-u-ca-japanese-tz-usnyc"},
                {"en@timezone=US/Eastern",    "en-u-tz-usnyc"},
                {"en@x=x-y-z;a=a-b-c",  "en-x-x-y-z"},
                {"it@collation=badcollationtype;colStrength=identical;cu=usd-eur", "it-u-cu-usd-eur-ks-identic"},
                {"en_US_POSIX", "en-US-u-va-posix"},
                {"en_US_POSIX@calendar=japanese;currency=EUR","en-US-u-ca-japanese-cu-eur-va-posix"},
                {"@x=elmer",    "x-elmer"},
                {"_US@x=elmer", "und-US-x-elmer"},
                /* #12671 */
                {"en@a=bar;attribute=baz",  "en-a-bar-u-baz"},
                {"en@a=bar;attribute=baz;x=u-foo",  "en-a-bar-u-baz-x-u-foo"},
                {"en@attribute=baz",    "en-u-baz"},
                {"en@attribute=baz;calendar=islamic-civil", "en-u-baz-ca-islamic-civil"},
                {"en@a=bar;calendar=islamic-civil;x=u-foo", "en-a-bar-u-ca-islamic-civil-x-u-foo"},
                {"en@a=bar;attribute=baz;calendar=islamic-civil;x=u-foo",   "en-a-bar-u-baz-ca-islamic-civil-x-u-foo"},
        };

        for (int i = 0; i < locale_to_langtag.length; i++) {
            ULocale loc = new ULocale(locale_to_langtag[i][0]);
            String langtag = loc.toLanguageTag();
            if (!langtag.equals(locale_to_langtag[i][1])) {
                errln("FAIL: toLanguageTag returned language tag [" + langtag + "] for locale ["
                        + loc + "] - expected: [" + locale_to_langtag[i][1] + "]");
            }
        }
    }

    @Test
    public void TestForLanguageTag() {
        final Integer NOERROR = Integer.valueOf(-1);

        final Object[][] langtag_to_locale = {
                {"en",                  "en",                   NOERROR},
                {"en-us",               "en_US",                NOERROR},
                {"und-us",              "_US",                  NOERROR},
                {"und-latn",            "_Latn",                NOERROR},
                {"en-us-posix",         "en_US_POSIX",          NOERROR},
                {"de-de_euro",          "de",                   Integer.valueOf(3)},
                {"kok-in",              "kok_IN",               NOERROR},
                {"123",                 "",                     Integer.valueOf(0)},
                {"en_us",               "",                     Integer.valueOf(0)},
                {"en-latn-x",           "en_Latn",              Integer.valueOf(8)},
                {"art-lojban",          "jbo",                  NOERROR},
                {"zh-hakka",            "hak",                  NOERROR},
                {"zh-cmn-CH",           "cmn_CH",               NOERROR},
                {"xxx-yy",              "xxx_YY",               NOERROR},
                {"fr-234",              "fr_234",               NOERROR},
                {"i-default",           "en@x=i-default",       NOERROR},
                {"i-test",              "",                     Integer.valueOf(0)},
                {"ja-jp-jp",            "ja_JP",                Integer.valueOf(6)},
                {"bogus",               "bogus",                NOERROR},
                {"boguslang",           "",                     Integer.valueOf(0)},
                {"EN-lATN-us",          "en_Latn_US",           NOERROR},
                {"und-variant-1234",    "__VARIANT_1234",       NOERROR},
                {"und-varzero-var1-vartwo", "__VARZERO",        Integer.valueOf(12)},
                {"en-u-ca-gregory",     "en@calendar=gregorian",    NOERROR},
                {"en-U-cu-USD",         "en@currency=usd",      NOERROR},
                {"en-us-u-va-posix",    "en_US_POSIX",          NOERROR},
                {"en-us-u-ca-gregory-va-posix", "en_US_POSIX@calendar=gregorian",   NOERROR},
                {"en-us-posix-u-va-posix",  "en_US_POSIX@va=posix", NOERROR},
                {"en-us-u-va-posix2",   "en_US@va=posix2",      NOERROR},
                {"en-us-vari1-u-va-posix",   "en_US_VARI1@va=posix",  NOERROR},
                {"ar-x-1-2-3",          "ar@x=1-2-3",           NOERROR},
                {"fr-u-nu-latn-cu-eur", "fr@currency=eur;numbers=latn", NOERROR},
                {"de-k-kext-u-co-phonebk-nu-latn",  "de@collation=phonebook;k=kext;numbers=latn",   NOERROR},
                {"ja-u-cu-jpy-ca-jp",   "ja@calendar=yes;currency=jpy;jp=yes",  NOERROR},
                {"en-us-u-tz-usnyc",    "en_US@timezone=America/New_York",      NOERROR},
                {"und-a-abc-def",       "@a=abc-def",           NOERROR},
                {"zh-u-ca-chinese-x-u-ca-chinese",  "zh@calendar=chinese;x=u-ca-chinese",   NOERROR},
                {"fr--FR",              "fr",                   Integer.valueOf(3)},
                {"fr-",                 "fr",                   Integer.valueOf(3)},
                {"x-elmer",             "@x=elmer",             NOERROR},
                {"en-US-u-attr1-attr2-ca-gregory", "en_US@attribute=attr1-attr2;calendar=gregorian",    NOERROR},
                {"sr-u-kn",             "sr@colnumeric=yes",    NOERROR},
                {"de-u-kn-co-phonebk",  "de@collation=phonebook;colnumeric=yes",    NOERROR},
                {"en-u-attr2-attr1-kn-kb",  "en@attribute=attr1-attr2;colbackwards=yes;colnumeric=yes", NOERROR},
                {"ja-u-ijkl-efgh-abcd-ca-japanese-xx-yyy-zzz-kn",   "ja@attribute=abcd-efgh-ijkl;calendar=japanese;colnumeric=yes;xx=yyy-zzz",  NOERROR},
                {"de-u-xc-xphonebk-co-phonebk-ca-buddhist-mo-very-lo-extensi-xd-that-de-should-vc-probably-xz-killthebuffer",
                    "de@calendar=buddhist;collation=phonebook;de=should;lo=extensi;mo=very;vc=probably;xc=xphonebk;xd=that;xz=yes", Integer.valueOf(92)},
                /* #12761 */
                {"en-a-bar-u-baz",      "en@a=bar;attribute=baz",   NOERROR},
                {"en-a-bar-u-baz-x-u-foo",  "en@a=bar;attribute=baz;x=u-foo",   NOERROR},
                {"en-u-baz",            "en@attribute=baz",     NOERROR},
                {"en-u-baz-ca-islamic-civil",   "en@attribute=baz;calendar=islamic-civil",  NOERROR},
                {"en-a-bar-u-ca-islamic-civil-x-u-foo", "en@a=bar;calendar=islamic-civil;x=u-foo",  NOERROR},
                {"en-a-bar-u-baz-ca-islamic-civil-x-u-foo", "en@a=bar;attribute=baz;calendar=islamic-civil;x=u-foo",    NOERROR},

        };

        for (int i = 0; i < langtag_to_locale.length; i++) {
            String tag = (String)langtag_to_locale[i][0];
            ULocale expected = new ULocale((String)langtag_to_locale[i][1]);
            ULocale loc = ULocale.forLanguageTag(tag);

            if (!loc.equals(expected)) {
                errln("FAIL: forLanguageTag returned locale [" + loc + "] for language tag [" + tag
                        + "] - expected: [" + expected + "]");
            }
        }

        // Use locale builder to check errors
        for (int i = 0; i < langtag_to_locale.length; i++) {
            String tag = (String)langtag_to_locale[i][0];
            ULocale expected = new ULocale((String)langtag_to_locale[i][1]);
            int errorIdx = ((Integer)langtag_to_locale[i][2]).intValue();

            try {
                Builder bld = new Builder();
                bld.setLanguageTag(tag);
                ULocale loc = bld.build();

                if (!loc.equals(expected)) {
                    errln("FAIL: forLanguageTag returned locale [" + loc + "] for language tag [" + tag
                            + "] - expected: [" + expected + "]");
                }
                if (errorIdx != NOERROR.intValue()) {
                    errln("FAIL: Builder.setLanguageTag should throw an exception for input tag [" + tag + "]");
                }
            } catch (IllformedLocaleException ifle) {
                if (ifle.getErrorIndex() != errorIdx) {
                    errln("FAIL: Builder.setLanguageTag returned error index " + ifle.getErrorIndex()
                            + " for input language tag [" + tag + "] expected: " + errorIdx);
                }
            }
        }
    }

    /*
     * Test that if you use any locale without keyword that you will get a NULL
     * string returned and not throw and exception.
     */
    @Test
    public void Test4735()
    {
        try {
            new ULocale("und").getDisplayKeywordValue("calendar",ULocale.GERMAN);
            new ULocale("en").getDisplayKeywordValue("calendar",ULocale.GERMAN);
        } catch (Exception e) {
            errln("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void TestGetFallback() {
        // Testing static String getFallback(String)
        final String[][] TESTIDS =
            {
                {"en_US", "en", "", ""},    // ULocale.getFallback("") should return ""
                {"EN_us_Var", "en_US", "en", ""},   // Case is always normalized
                {"de_DE@collation=phonebook", "de@collation=phonebook", "@collation=phonebook", "@collation=phonebook"},    // Keyword is preserved
                {"en__POSIX", "en", ""},    // Trailing empty segment should be truncated
                {"_US_POSIX", "_US", ""},   // Same as above
                {"root", ""},               // No canonicalization
            };

        for (String[] chain : TESTIDS) {
            for (int i = 1; i < chain.length; i++) {
                String fallback = ULocale.getFallback(chain[i-1]);
                assertEquals("getFallback(\"" + chain[i-1] + "\")", chain[i], fallback);
            }
        }

        // Testing ULocale getFallback()
        final ULocale[][] TESTLOCALES =
            {
                {new ULocale("en_US"), new ULocale("en"), ULocale.ROOT, null},
                {new ULocale("en__POSIX"), new ULocale("en"), ULocale.ROOT, null},
                {new ULocale("de_DE@collation=phonebook"), new ULocale("de@collation=phonebook"), new ULocale("@collation=phonebook"), null},
                {new ULocale("_US_POSIX"), new ULocale("_US"), ULocale.ROOT, null},
                {new ULocale("root"), ULocale.ROOT, null},
            };

        for(ULocale[] chain : TESTLOCALES) {
            for (int i = 1; i < chain.length; i++) {
                ULocale fallback = chain[i-1].getFallback();
                assertEquals("ULocale(" + chain[i-1] + ").getFallback()", chain[i], fallback);
            }
        }
    }

    @Test
    public void TestExtension() {
        String[][] TESTCASES = {
                // {"<langtag>", "<ext key1>", "<ext val1>", "<ext key2>", "<ext val2>", ....},
                {"en"},
                {"en-a-exta-b-extb", "a", "exta", "b", "extb"},
                {"en-b-extb-a-exta", "a", "exta", "b", "extb"},
                {"de-x-a-bc-def", "x", "a-bc-def"},
                {"ja-JP-u-cu-jpy-ca-japanese-x-java", "u", "ca-japanese-cu-jpy", "x", "java"},
        };

        for (String[] testcase : TESTCASES) {
            ULocale loc = ULocale.forLanguageTag(testcase[0]);

            int nExtensions = (testcase.length - 1) / 2;

            Set<Character> keys = loc.getExtensionKeys();
            if (keys.size() != nExtensions) {
                errln("Incorrect number of extensions: returned="
                        + keys.size() + ", expected=" + nExtensions
                        + ", locale=" + testcase[0]);
            }

            for (int i = 0; i < nExtensions; i++) {
                String kstr = testcase[i/2 + 1];
                String ext = loc.getExtension(Character.valueOf(kstr.charAt(0)));
                if (ext == null || !ext.equals(testcase[i/2 + 2])) {
                    errln("Incorrect extension value: key="
                            + kstr + ", returned=" + ext + ", expected=" + testcase[i/2 + 2]
                                    + ", locale=" + testcase[0]);
                }
            }
        }

        // Exception handling
        boolean sawException = false;
        try {
            ULocale l = ULocale.forLanguageTag("en-US-a-exta");
            l.getExtension('$');
        } catch (IllegalArgumentException e) {
            sawException = true;
        }
        if (!sawException) {
            errln("getExtension must throw an exception on illegal input key");
        }
    }

    @Test
    public void TestUnicodeLocaleExtension() {
        String[][] TESTCASES = {
                //"<langtag>", "<attr1>,<attr2>,...", "<key1>,<key2>,...", "<type1>", "<type2>", ...},
                {"en", null, null},
                {"en-a-ext1-x-privuse", null, null},
                {"en-u-attr1-attr2", "attr1,attr2", null},
                {"ja-u-ca-japanese-cu-jpy", null, "ca,cu", "japanese", "jpy"},
                {"th-TH-u-number-attr-nu-thai-ca-buddhist", "attr,number", "ca,nu", "buddhist", "thai"},
        };

        for (String[] testcase : TESTCASES) {
            ULocale loc = ULocale.forLanguageTag(testcase[0]);

            Set<String> expectedAttributes = new HashSet<String>();
            if (testcase[1] != null) {
                String[] attrs = testcase[1].split(",");
                for (String s : attrs) {
                    expectedAttributes.add(s);
                }
            }

            Map<String, String> expectedKeywords = new HashMap<String, String>();
            if (testcase[2] != null) {
                String[] ukeys = testcase[2].split(",");
                for (int i = 0; i < ukeys.length; i++) {
                    expectedKeywords.put(ukeys[i], testcase[i + 3]);
                }
            }

            // Check attributes
            Set<String> attributes = loc.getUnicodeLocaleAttributes();
            if (attributes.size() != expectedAttributes.size()) {
                errln("Incorrect number for Unicode locale attributes: returned="
                        + attributes.size() + ", expected=" + expectedAttributes.size()
                        + ", locale=" + testcase[0]);
            }
            if (!attributes.containsAll(expectedAttributes) || !expectedAttributes.containsAll(attributes)) {
                errln("Incorrect set of attributes for locale " + testcase[0]);
            }

            // Check keywords
            Set<String> keys = loc.getUnicodeLocaleKeys();
            Set<String> expectedKeys = expectedKeywords.keySet();
            if (keys.size() != expectedKeys.size()) {
                errln("Incorrect number for Unicode locale keys: returned="
                        + keys.size() + ", expected=" + expectedKeys.size()
                        + ", locale=" + testcase[0]);
            }

            for (String expKey : expectedKeys) {
                String type = loc.getUnicodeLocaleType(expKey);
                String expType = expectedKeywords.get(expKey);

                if (type == null || !expType.equals(type)) {
                    errln("Incorrect Unicode locale type: key="
                            + expKey + ", returned=" + type + ", expected=" + expType
                            + ", locale=" + testcase[0]);
                }
            }
        }

        // Exception handling
        boolean sawException = false;
        try {
            ULocale l = ULocale.forLanguageTag("en-US-u-ca-gregory");
            l.getUnicodeLocaleType("$%");
        } catch (IllegalArgumentException e) {
            sawException = true;
        }
        if (!sawException) {
            errln("getUnicodeLocaleType must throw an exception on illegal input key");
        }
    }

    @Test
    public void TestForLocale() {
        Object[][] DATA = {
                {new Locale(""),                    ""},
                {new Locale("en", "US"),            "en_US"},
                {new Locale("en", "US", "POSIX"),   "en_US_POSIX"},
                {new Locale("", "US"),              "_US"},
                {new Locale("en", "", "POSIX"),     "en__POSIX"},
                {new Locale("no", "NO", "NY"),      "nn_NO"},
                {new Locale("en", "BOGUS"),         "en__BOGUS"}, // ill-formed country is mapped to variant - see #8383 and #8384
        };

        for (int i = 0; i < DATA.length; i++) {
            ULocale uloc = ULocale.forLocale((Locale) DATA[i][0]);
            assertEquals("forLocale with " + DATA[i][0], DATA[i][1], uloc.getName());
        }

        if (JAVA7_OR_LATER) {
            Object[][] DATA7 = {
                    {new Locale("ja", "JP", "JP"),      "ja_JP_JP@calendar=japanese"},
                    {new Locale("th", "TH", "TH"),      "th_TH_TH@numbers=thai"},
            };
            for (int i = 0; i < DATA7.length; i++) {
                ULocale uloc = ULocale.forLocale((Locale) DATA7[i][0]);
                assertEquals("forLocale with " + DATA7[i][0], DATA7[i][1], uloc.getName());
            }

            try {
                Method localeForLanguageTag = Locale.class.getMethod("forLanguageTag", String.class);

                String[][] DATA7EXT = {
                        {"en-Latn-US",                  "en_Latn_US"},
                        {"zh-Hant-TW",                  "zh_Hant_TW"},
                        {"und-US-u-cu-usd",             "_US@currency=usd"},
                        {"th-TH-u-ca-buddhist-nu-thai", "th_TH@calendar=buddhist;numbers=thai"},
                        {"en-US-u-va-POSIX",            "en_US_POSIX"},
                        {"de-DE-u-co-phonebk",          "de_DE@collation=phonebook"},
                        {"en-a-exta-b-extb-x-privu",    "en@a=exta;b=extb;x=privu"},
                        {"fr-u-attr1-attr2-cu-eur",     "fr@attribute=attr1-attr2;currency=eur"},
                };

                for (int i = 0; i < DATA7EXT.length; i++) {
                    Locale loc = (Locale) localeForLanguageTag.invoke(null, DATA7EXT[i][0]);
                    ULocale uloc = ULocale.forLocale(loc);
                    assertEquals("forLocale with " + loc, DATA7EXT[i][1], uloc.getName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            Object[][] DATA6 = {
                    {new Locale("ja", "JP", "JP"),      "ja_JP@calendar=japanese"},
                    {new Locale("th", "TH", "TH"),      "th_TH@numbers=thai"},
            };
            for (int i = 0; i < DATA6.length; i++) {
                ULocale uloc = ULocale.forLocale((Locale) DATA6[i][0]);
                assertEquals("forLocale with " + DATA6[i][0], DATA6[i][1], uloc.getName());
            }
        }
    }

    @Test
    public void TestToLocale() {
        Object[][] DATA = {
                {"",                new Locale("")},
                {"en_US",           new Locale("en", "US")},
                {"_US",             new Locale("", "US")},
                {"en__POSIX",       new Locale("en", "", "POSIX")},
        };

        for (int i = 0; i < DATA.length; i++) {
            Locale loc = new ULocale((String) DATA[i][0]).toLocale();
            assertEquals("toLocale with " + DATA[i][0], DATA[i][1], loc);
        }

        if (JAVA7_OR_LATER) {
            Object[][] DATA7 = {
                    {"nn_NO",                       new Locale("nn", "NO")},
                    {"no_NO_NY",                    new Locale("no", "NO", "NY")},
            };
            for (int i = 0; i < DATA7.length; i++) {
                Locale loc = new ULocale((String) DATA7[i][0]).toLocale();
                assertEquals("toLocale with " + DATA7[i][0], DATA7[i][1], loc);
            }

            try {
                Method localeForLanguageTag = Locale.class.getMethod("forLanguageTag", String.class);

                String[][] DATA7EXT = {
                        {"en_Latn_US",                  "en-Latn-US"},
                        {"zh_Hant_TW",                  "zh-Hant-TW"},
                        {"ja_JP@calendar=japanese",     "ja-JP-u-ca-japanese"},
                        {"ja_JP_JP@calendar=japanese",  "ja-JP-u-ca-japanese-x-lvariant-JP"},
                        {"th_TH@numbers=thai",          "th-TH-u-nu-thai"},
                        {"th_TH_TH@numbers=thai",       "th-TH-u-nu-thai-x-lvariant-TH"},
                        {"de@collation=phonebook",      "de-u-co-phonebk"},
                        {"en@a=exta;b=extb;x=privu",    "en-a-exta-b-extb-x-privu"},
                        {"fr@attribute=attr1-attr2;currency=eur",   "fr-u-attr1-attr2-cu-eur"},
                };

                for (int i = 0; i < DATA7EXT.length; i++) {
                    Locale loc = new ULocale(DATA7EXT[i][0]).toLocale();
                    Locale expected = (Locale) localeForLanguageTag.invoke(null, DATA7EXT[i][1]);
                    assertEquals("toLocale with " + DATA7EXT[i][0], expected, loc);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            Object[][] DATA6 = {
                    {"nn_NO",                       new Locale("no", "NO", "NY")},
                    {"no_NO_NY",                    new Locale("no", "NO", "NY")},
                    {"ja_JP@calendar=japanese",     new Locale("ja", "JP", "JP")},
                    {"th_TH@numbers=thai",          new Locale("th", "TH", "TH")},
            };
            for (int i = 0; i < DATA6.length; i++) {
                Locale loc = new ULocale((String) DATA6[i][0]).toLocale();
                assertEquals("toLocale with " + DATA6[i][0], DATA6[i][1], loc);
            }
        }
    }

    @Test
    public void TestCategoryDefault() {
        Locale backupDefault = Locale.getDefault();

        ULocale orgDefault = ULocale.getDefault();

        // Setting a category default won't change default ULocale
        ULocale uJaJp = new ULocale("ja_JP");
        ULocale uDeDePhonebook = new ULocale("de_DE@collation=phonebook");

        ULocale.setDefault(Category.DISPLAY, uJaJp);
        ULocale.setDefault(Category.FORMAT, uDeDePhonebook);

        if (!ULocale.getDefault().equals(orgDefault)) {
            errln("FAIL: Default ULocale is " + ULocale.getDefault() + ", expected: " + orgDefault);
        }

        if (!ULocale.getDefault(Category.DISPLAY).equals(uJaJp)) {
            errln("FAIL: DISPLAY ULocale is " + ULocale.getDefault(Category.DISPLAY) + ", expected: " + uJaJp);
        }

        if (!ULocale.getDefault(Category.FORMAT).equals(uDeDePhonebook)) {
            errln("FAIL: FORMAT ULocale is " + ULocale.getDefault(Category.FORMAT) + ", expected: " + uDeDePhonebook);
        }

        // Setting ULocale default will overrides category defaults
        ULocale uFrFr = new ULocale("fr_FR");

        ULocale.setDefault(uFrFr);

        if (!ULocale.getDefault(Category.DISPLAY).equals(uFrFr)) {
            errln("FAIL: DISPLAY ULocale is " + ULocale.getDefault(Category.DISPLAY) + ", expected: " + uFrFr);
        }

        if (!ULocale.getDefault(Category.FORMAT).equals(uFrFr)) {
            errln("FAIL: FORMAT ULocale is " + ULocale.getDefault(Category.FORMAT) + ", expected: " + uFrFr);
        }

        // Setting Locale default will updates ULocale default and category defaults
        Locale arEg = new Locale("ar", "EG");
        ULocale uArEg = ULocale.forLocale(arEg);

        Locale.setDefault(arEg);

        if (!ULocale.getDefault().equals(uArEg)) {
            errln("FAIL: Default ULocale is " + ULocale.getDefault() + ", expected: " + uArEg);
        }

        if (!ULocale.getDefault(Category.DISPLAY).equals(uArEg)) {
            errln("FAIL: DISPLAY ULocale is " + ULocale.getDefault(Category.DISPLAY) + ", expected: " + uArEg);
        }

        if (!ULocale.getDefault(Category.FORMAT).equals(uArEg)) {
            errln("FAIL: FORMAT ULocale is " + ULocale.getDefault(Category.FORMAT) + ", expected: " + uArEg);
        }

        // Restore back up
        Locale.setDefault(backupDefault);
    }

    //
    // Test case for the behavior of Comparable implementation.
    //
    @Test
    public void TestComparable() {
        // Test strings used for creating ULocale objects.
        // This list contains multiple different strings creating
        // multiple equivalent locales.
        final String[] localeStrings = {
                "en",
                "EN",
                "en_US",
                "en_GB",
                "en_US_POSIX",
                "en_us_posix",
                "ar_EG",
                "zh_Hans_CN",
                "zh_Hant_TW",
                "zh_Hans",
                "zh_CN",
                "zh_TW",
                "th_TH@calendar=buddhist;numbers=thai",
                "TH_TH@NUMBERS=thai;CALENDAR=buddhist",
                "th_TH@calendar=buddhist",
                "th_TH@calendar=gergorian",
                "th_TH@numbers=latn",
                "abc_def_ghi_jkl_opq",
                "abc_DEF_ghi_JKL_opq",
                "",
                "und",
                "This is a bogus locale ID",
                "This is a BOGUS locale ID",
                "en_POSIX",
                "en__POSIX",
        };

        ULocale[] locales = new ULocale[localeStrings.length];
        for (int i = 0; i < locales.length; i++) {
            locales[i] = new ULocale(localeStrings[i]);
        }

        // compares all permutations
        for (int i = 0; i < locales.length; i++) {
            for (int j = i /* including the locale itself */; j < locales.length; j++) {
                boolean eqls1 = locales[i].equals(locales[j]);
                boolean eqls2 = locales[i].equals(locales[j]);

                if (eqls1 != eqls2) {
                    errln("FAILED: loc1.equals(loc2) and loc2.equals(loc1) return different results: loc1="
                            + locales[i] + ", loc2=" + locales[j]);
                }

                int cmp1 = locales[i].compareTo(locales[j]);
                int cmp2 = locales[j].compareTo(locales[i]);

                if ((cmp1 == 0) != eqls1) {
                    errln("FAILED: inconsistent equals and compareTo: loc1="
                            + locales[i] + ", loc2=" + locales[j]);
                }
                if (cmp1 < 0 && cmp2 <= 0 || cmp1 > 0 && cmp2 >= 0 || cmp1 == 0 && cmp2 != 0) {
                    errln("FAILED: loc1.compareTo(loc2) is inconsistent with loc2.compareTo(loc1): loc1="
                            + locales[i] + ", loc2=" + locales[j]);
                }
            }
        }

        // Make sure ULocale objects can be sorted by the Java collection
        // framework class without providing a Comparator, and equals/compareTo
        // are consistent.

        // The sorted locale list created from localeStrings above.
        // Duplicated locales are removed and locale string is normalized
        // (by the ULocale constructor).
        final String[] sortedLocaleStrings = {
                "",
                "abc_DEF_GHI_JKL_OPQ",
                "ar_EG",
                "en",
                "en__POSIX",
                "en_GB",
                "en_US",
                "en_US_POSIX",
                "th_TH@calendar=buddhist",
                "th_TH@calendar=buddhist;numbers=thai",
                "th_TH@calendar=gergorian",
                "th_TH@numbers=latn",
                "this is a bogus locale id",
                "und",
                "zh_CN",
                "zh_TW",
                "zh_Hans",
                "zh_Hans_CN",
                "zh_Hant_TW",
        };

        TreeSet<ULocale> sortedLocales = new TreeSet<ULocale>();
        for (ULocale locale : locales) {
            sortedLocales.add(locale);
        }

        // Check the number of unique locales
        if (sortedLocales.size() != sortedLocaleStrings.length) {
            errln("FAILED: Number of unique locales: " + sortedLocales.size() + ", expected: " + sortedLocaleStrings.length);
        }

        // Check the order
        int i = 0;
        for (ULocale loc : sortedLocales) {
            if (!loc.toString().equals(sortedLocaleStrings[i++])) {
                errln("FAILED: Sort order is incorrect for " + loc.toString());
                break;
            }
        }
    }

    @Test
    public void TestToUnicodeLocaleKey() {
        String[][] DATA = {
                {"calendar",    "ca"},
                {"CALEndar",    "ca"},  // difference casing
                {"ca",          "ca"},  // bcp key itself
                {"kv",          "kv"},  // no difference between legacy and bcp
                {"foo",         null},  // unknown, bcp ill-formed
                {"ZZ",          "zz"},  // unknown, bcp well-formed
        };

        for (String[] d : DATA) {
            String keyword = d[0];
            String expected = d[1];

            String bcpKey = ULocale.toUnicodeLocaleKey(keyword);
            assertEquals("keyword=" + keyword, expected, bcpKey);
        }
    }

    @Test
    public void TestToLegacyKey() {
        String[][] DATA = {
                {"kb",          "colbackwards"},
                {"kB",          "colbackwards"},    // different casing
                {"Collation",   "collation"},   // keyword itself with different casing
                {"kv",          "kv"},  // no difference between legacy and bcp
                {"foo",         "foo"}, // unknown, bcp ill-formed
                {"ZZ",          "zz"},  // unknown, bcp well-formed
                {"e=mc2",       null},  // unknown, bcp/legacy ill-formed
        };

        for (String[] d : DATA) {
            String keyword = d[0];
            String expected = d[1];

            String legacyKey = ULocale.toLegacyKey(keyword);
            assertEquals("bcpKey=" + keyword, expected, legacyKey);
        }
    }

    @Test
    public void TestToUnicodeLocaleType() {
        String[][] DATA = {
                {"tz",              "Asia/Kolkata",     "inccu"},
                {"calendar",        "gregorian",        "gregory"},
                {"ca",              "gregorian",        "gregory"},
                {"ca",              "Gregorian",        "gregory"},
                {"ca",              "buddhist",         "buddhist"},
                {"Calendar",        "Japanese",         "japanese"},
                {"calendar",        "Islamic-Civil",    "islamic-civil"},
                {"calendar",        "islamicc",         "islamic-civil"},   // bcp type alias
                {"colalternate",    "NON-IGNORABLE",    "noignore"},
                {"colcaselevel",    "yes",              "true"},
                {"rg",              "GBzzzz",           "gbzzzz"},
                {"tz",              "america/new_york", "usnyc"},
                {"tz",              "Asia/Kolkata",     "inccu"},
                {"timezone",        "navajo",           "usden"},
                {"ca",              "aaaa",             "aaaa"},    // unknown type, well-formed type
                {"ca",              "gregory-japanese-islamic", "gregory-japanese-islamic"},    // unknown type, well-formed type
                {"zz",              "gregorian",        null},      // unknown key, ill-formed type
                {"co",              "foo-",             null},      // unknown type, ill-formed type
                {"variableTop",     "00A0",             "00a0"},    // valid codepoints type
                {"variableTop",     "wxyz",             "wxyz"},      // invalid codepoints type - return as is for now
                {"kr",              "space-punct",      "space-punct"}, // valid reordercode type
                {"kr",              "digit-spacepunct", null},      // invalid reordercode type
        };

        for (String[] d : DATA) {
            String keyword = d[0];
            String value = d[1];
            String expected = d[2];

            String bcpType = ULocale.toUnicodeLocaleType(keyword, value);
            assertEquals("keyword=" + keyword + ", value=" + value, expected, bcpType);
        }

    }

    @Test
    public void TestToLegacyType() {
        String[][] DATA = {
                {"calendar",        "gregory",          "gregorian"},
                {"ca",              "gregory",          "gregorian"},
                {"ca",              "Gregory",          "gregorian"},
                {"ca",              "buddhist",         "buddhist"},
                {"Calendar",        "Japanese",         "japanese"},
                {"calendar",        "Islamic-Civil",    "islamic-civil"},
                {"calendar",        "islamicc",         "islamic-civil"},   // bcp type alias
                {"colalternate",    "noignore",         "non-ignorable"},
                {"colcaselevel",    "true",             "yes"},
                {"rg",              "gbzzzz",           "gbzzzz"},
                {"tz",              "usnyc",            "America/New_York"},
                {"tz",              "inccu",            "Asia/Calcutta"},
                {"timezone",        "usden",            "America/Denver"},
                {"timezone",        "usnavajo",         "America/Denver"},  // bcp type alias
                {"colstrength",     "quarternary",      "quaternary"},  // type alias
                {"ca",              "aaaa",             "aaaa"},    // unknown type
                {"calendar",        "gregory-japanese-islamic", "gregory-japanese-islamic"},    // unknown type, well-formed type
                {"zz",              "gregorian",        "gregorian"},   // unknown key, bcp ill-formed type
                {"ca",              "gregorian-calendar",   "gregorian-calendar"},  // known key, bcp ill-formed type
                {"co",              "e=mc2",            null},  // known key, ill-formed bcp/legacy type
                {"variableTop",     "00A0",             "00a0"},        // valid codepoints type
                {"variableTop",     "wxyz",             "wxyz"},        // invalid codepoints type - return as is for now
                {"kr",              "space-punct",      "space-punct"}, // valid reordercode type
                {"kr",              "digit-spacepunct", "digit-spacepunct"},    // invalid reordercode type, but ok for legacy syntax
        };

        for (String[] d : DATA) {
            String keyword = d[0];
            String value = d[1];
            String expected = d[2];

            String legacyType = ULocale.toLegacyType(keyword, value);
            assertEquals("keyword=" + keyword + ", value="  + value, expected, legacyType);
        }
    }

    @Test
    public void TestIsRightToLeft() {
        assertFalse("root LTR", ULocale.ROOT.isRightToLeft());
        assertFalse("zh LTR", ULocale.CHINESE.isRightToLeft());
        assertTrue("ar RTL", new ULocale("ar").isRightToLeft());
        assertTrue("und-EG RTL", new ULocale("und-EG").isRightToLeft());
        assertFalse("fa-Cyrl LTR", new ULocale("fa-Cyrl").isRightToLeft());
        assertTrue("en-Hebr RTL", new ULocale("en-Hebr").isRightToLeft());
        assertTrue("ckb RTL", new ULocale("ckb").isRightToLeft());  // Sorani Kurdish
        assertFalse("fil LTR", new ULocale("fil").isRightToLeft());
        assertFalse("he-Zyxw LTR", new ULocale("he-Zyxw").isRightToLeft());
    }

    @Test
    public void TestChineseToLocale() {
        final ULocale[][] LOCALES = {
                {ULocale.CHINESE,               new ULocale("zh")},
                {ULocale.SIMPLIFIED_CHINESE,    new ULocale("zh_Hans")},
                {ULocale.TRADITIONAL_CHINESE,   new ULocale("zh_Hant")},
                {ULocale.CHINA,                 new ULocale("zh_Hans_CN")},
                {ULocale.PRC,                   new ULocale("zh_Hans_CN")},
                {ULocale.TAIWAN,                new ULocale("zh_Hant_TW")},
        };

        // When two ULocales are equal, results of ULocale#toLocale() must be
        // also equal.
        for (ULocale[] pair : LOCALES) {
            if (pair[0].equals(pair[1])) {
                assertEquals(pair[0].toString(), pair[0].toLocale(), pair[1].toLocale());
            } else {
                // This could happen when the definition of ULocale constant is changed.
                // When it happens, it could be a mistake. So we use errln below.
                // If we change the definitioin for a legitimate reason, then the hardcoded
                // test data above should be reviewed and updated.
                errln("Error: " + pair[0] + " is not equal to " + pair[1]);
            }
        }
    }
}
