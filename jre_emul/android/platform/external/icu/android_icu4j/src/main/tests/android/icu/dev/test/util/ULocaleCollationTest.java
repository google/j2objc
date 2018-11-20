/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **********************************************************************
 * Copyright (c) 2015, International Business Machines
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.Collator;
import android.icu.text.DisplayContext;
import android.icu.text.DisplayContext.Type;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.UiListItem;
import android.icu.util.IllformedLocaleException;
import android.icu.util.ULocale;

public class ULocaleCollationTest extends TestFmwk {
    @Test
    public void TestCollator() {
        checkService("ja_JP_YOKOHAMA", new ServiceFacade() {
            public Object create(ULocale req) {
                return Collator.getInstance(req);
            }
        }, null, new Registrar() {
            public Object register(ULocale loc, Object prototype) {
                return Collator.registerInstance((Collator) prototype, loc);
            }
            public boolean unregister(Object key) {
                return Collator.unregister(key);
            }
        });
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

    @Test
    public void TestNameList() { 
        String[][][] tests = { 
                /* name in French, name in self, minimized, modified */
                {{"fr-Cyrl-BE", "fr-Cyrl-CA"}, 
                    {"Français (cyrillique, Belgique)", "Français (cyrillique, Belgique)", "fr_Cyrl_BE", "fr_Cyrl_BE"}, 
                    {"Français (cyrillique, Canada)", "Français (cyrillique, Canada)", "fr_Cyrl_CA", "fr_Cyrl_CA"}, 
                }, 
                {{"en", "de", "fr", "zh"}, 
                    {"Allemand", "Deutsch", "de", "de"}, 
                    {"Anglais", "English", "en", "en"}, 
                    {"Chinois", "中文", "zh", "zh"}, 
                    {"Français", "Français", "fr", "fr"}, 
                }, 
                // some non-canonical names
                {{"iw", "iw-US", "no", "no-Cyrl", "in", "in-YU"}, 
                    {"Hébreu (États-Unis)", "עברית (ארצות הברית)", "iw_US", "iw_US"}, 
                    {"Hébreu (Israël)", "עברית (ישראל)", "iw", "iw_IL"}, 
                    {"Indonésien (Indonésie)", "Indonesia (Indonesia)", "in", "in_ID"}, 
                    {"Indonésien (Serbie)", "Indonesia (Serbia)", "in_YU", "in_YU"}, 
                    {"Norvégien (cyrillique)", "Norsk (kyrillisk)", "no_Cyrl", "no_Cyrl"}, 
                    {"Norvégien (latin)", "Norsk (latinsk)", "no", "no_Latn"}, 
                }, 
                {{"zh-Hant-TW", "en", "en-gb", "fr", "zh-Hant", "de", "de-CH", "zh-TW"}, 
                    {"Allemand (Allemagne)", "Deutsch (Deutschland)", "de", "de_DE"}, 
                    {"Allemand (Suisse)", "Deutsch (Schweiz)", "de_CH", "de_CH"}, 
                    {"Anglais (États-Unis)", "English (United States)", "en", "en_US"}, 
                    {"Anglais (Royaume-Uni)", "English (United Kingdom)", "en_GB", "en_GB"}, 
                    {"Chinois (traditionnel)", "中文（繁體）", "zh_Hant", "zh_Hant"}, 
                    {"Français", "Français", "fr", "fr"}, 
                }, 
                {{"zh", "en-gb", "en-CA", "fr-Latn-FR"}, 
                    {"Anglais (Canada)", "English (Canada)", "en_CA", "en_CA"}, 
                    {"Anglais (Royaume-Uni)", "English (United Kingdom)", "en_GB", "en_GB"}, 
                    {"Chinois", "中文", "zh", "zh"}, 
                    {"Français", "Français", "fr", "fr"}, 
                }, 
                {{"en-gb", "fr", "zh-Hant", "zh-SG", "sr", "sr-Latn"}, 
                    {"Anglais (Royaume-Uni)", "English (United Kingdom)", "en_GB", "en_GB"}, 
                    {"Chinois (simplifié, Singapour)", "中文（简体，新加坡）", "zh_SG", "zh_Hans_SG"}, 
                    {"Chinois (traditionnel, Taïwan)", "中文（繁體，台灣）", "zh_Hant", "zh_Hant_TW"}, 
                    {"Français", "Français", "fr", "fr"}, 
                    {"Serbe (cyrillique)", "Српски (ћирилица)", "sr", "sr_Cyrl"}, 
                    {"Serbe (latin)", "Srpski (latinica)", "sr_Latn", "sr_Latn"}, 
                }, 
                {{"fr-Cyrl", "fr-Arab"}, 
                    {"Français (arabe)", "Français (arabe)", "fr_Arab", "fr_Arab"}, 
                    {"Français (cyrillique)", "Français (cyrillique)", "fr_Cyrl", "fr_Cyrl"}, 
                }, 
                {{"fr-Cyrl-BE", "fr-Arab-CA"}, 
                    {"Français (arabe, Canada)", "Français (arabe, Canada)", "fr_Arab_CA", "fr_Arab_CA"}, 
                    {"Français (cyrillique, Belgique)", "Français (cyrillique, Belgique)", "fr_Cyrl_BE", "fr_Cyrl_BE"}, 
                } 
        }; 
        ULocale french = ULocale.FRENCH; 
        LocaleDisplayNames names = LocaleDisplayNames.getInstance(french,  
                DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU); 
        for (Type type : DisplayContext.Type.values()) { 
            logln("Contexts: " + names.getContext(type).toString()); 
        } 
        Collator collator = Collator.getInstance(french); 

        for (String[][] test : tests) { 
            Set<ULocale> list = new LinkedHashSet<ULocale>(); 
            List<UiListItem> expected = new ArrayList<UiListItem>(); 
            for (String item : test[0]) { 
                list.add(new ULocale(item)); 
            } 
            for (int i = 1; i < test.length; ++i) { 
                String[] rawRow = test[i]; 
                expected.add(new UiListItem(new ULocale(rawRow[2]), new ULocale(rawRow[3]), rawRow[0], rawRow[1])); 
            } 
            List<UiListItem> newList = names.getUiList(list, false, collator); 
            if (!expected.equals(newList)) { 
                if (expected.size() != newList.size()) { 
                    errln(list.toString() + ": wrong size" + expected + ", " + newList); 
                } else { 
                    errln(list.toString()); 
                    for (int i = 0; i < expected.size(); ++i) { 
                        assertEquals(i+"", expected.get(i), newList.get(i)); 
                    } 
                } 
            } else { 
                assertEquals(list.toString(), expected, newList); 
            } 
        } 
    } 

    @Test
    public void TestIllformedLocale() {
        ULocale french = ULocale.FRENCH; 
        Collator collator = Collator.getInstance(french); 
        LocaleDisplayNames names = LocaleDisplayNames.getInstance(french,  
                DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU); 
        for (String malformed : Arrays.asList("en-a", "$", "ü--a", "en--US")) {
            try {
                Set<ULocale> supported = Collections.singleton(new ULocale(malformed));
                names.getUiList(supported, false, collator);
                assertNull("Failed to detect bogus locale «" + malformed + "»", supported);
            } catch (IllformedLocaleException e) {
                logln("Successfully detected ill-formed locale «" + malformed + "»:" + e.getMessage());
            } 
        }
    }
}
