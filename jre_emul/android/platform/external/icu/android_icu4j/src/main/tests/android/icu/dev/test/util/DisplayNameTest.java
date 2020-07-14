/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2008, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.Calendar;
import android.icu.util.Currency;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

// TODO(junit): test is broken in main branch

public class DisplayNameTest extends TestFmwk {
    static final boolean SHOW_ALL = false;
    
    interface DisplayNameGetter {
        public String get(ULocale locale, String code, Object context);
    }

    Map[] codeToName = new Map[10];
    {
        for (int k = 0; k < codeToName.length; ++k) codeToName[k] = new HashMap();
    }
    
    static final Object[] zoneFormats = {new Integer(0), new Integer(1), new Integer(2),
        new Integer(3), new Integer(4), new Integer(5), new Integer(6), new Integer(7)};
    static final Object[] currencyFormats = {new Integer(Currency.SYMBOL_NAME), new Integer(Currency.LONG_NAME)};
    static final Object[] NO_CONTEXT = {null};
    
    static final Date JAN1;
    static final Date JULY1;

    static {
        Calendar cal = new GregorianCalendar(2004, Calendar.JANUARY, 1);
        JAN1 = cal.getTime();
        cal.set(Calendar.MONTH, Calendar.JULY);
        JULY1 = cal.getTime();
    }

    String[] countries = addUnknown(ULocale.getISOCountries(),2);
    String[] languages = addUnknown(ULocale.getISOLanguages(),2);
    String[] zones = addUnknown(getRealZoneIDs(),5);
    String[] scripts = addUnknown(getCodes(new ULocale("en","US",""), "Scripts"),4);
    // TODO fix once there is a way to get a list of all script codes
    String[] currencies = addUnknown(getCodes(new ULocale("en","",""), "Currencies"),3);
    // TODO fix once there is a way to get a list of all currency codes

    @Ignore
    @Test
    public void TestLocales() {
        ULocale[] locales = ULocale.getAvailableLocales();
        for (int i = 0; i < locales.length; ++i) {
            checkLocale(locales[i]);
        }
    }

    /**
     * @return
     */
    private String[] getRealZoneIDs() {
        Set temp = new TreeSet(Arrays.asList(TimeZone.getAvailableIDs()));
        temp.removeAll(getAliasMap().keySet());
        return (String[])temp.toArray(new String[temp.size()]);
    }

    @Ignore
    @Test
    public void TestEnglish() {
        checkLocale(ULocale.ENGLISH);
    }

    @Ignore
    @Test
    public void TestFrench() {
        checkLocale(ULocale.FRENCH);
    }

    private void checkLocale(ULocale locale) {
        logln("Checking " + locale);
        check("Language", locale, languages, null, new DisplayNameGetter() {
            public String get(ULocale loc, String code, Object context) {
                return ULocale.getDisplayLanguage(code, loc);
            }
        });
        check("Script", locale, scripts, null, new DisplayNameGetter() {
            public String get(ULocale loc, String code, Object context) {
                // TODO This is kinda a hack; ought to be direct way.
                return ULocale.getDisplayScript("en_"+code, loc);
            }
        });
        check("Country", locale, countries, null, new DisplayNameGetter() {
            public String get(ULocale loc, String code, Object context) {
                // TODO This is kinda a hack; ought to be direct way.
                return ULocale.getDisplayCountry("en_"+code, loc);
            }
        });
        check("Currencies", locale, currencies, currencyFormats, new DisplayNameGetter() {
            public String get(ULocale loc, String code, Object context) {
                Currency s = Currency.getInstance(code);
                return s.getName(loc, ((Integer)context).intValue(), new boolean[1]);
            }
        });
        // comment this out, because the zone string information is lost
        // we'd have to access the resources directly to test them

        check("Zones", locale, zones, zoneFormats, new DisplayNameGetter() {
            // TODO replace once we have real API
            public String get(ULocale loc, String code, Object context) {
                return getZoneString(loc, code, ((Integer)context).intValue());
            }
        });

    }
    
    Map zoneData = new HashMap();
    
    private String getZoneString(ULocale locale, String olsonID, int item) {
        Map data = (Map)zoneData.get(locale);
        if (data == null) {
            data = new HashMap();
            if (SHOW_ALL) System.out.println();
            if (SHOW_ALL) System.out.println("zones for " + locale);
            ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(locale);
            ICUResourceBundle table = bundle.getWithFallback("zoneStrings");
            for (int i = 0; i < table.getSize(); ++i) {
                UResourceBundle stringSet = table.get(i);
                //ICUResourceBundle stringSet = table.getWithFallback(String.valueOf(i));
                String key = stringSet.getString(0);
                if (SHOW_ALL) System.out.println("key: " + key);
                ArrayList list = new ArrayList();
                for (int j = 1; j < stringSet.getSize(); ++j) {
                    String entry = stringSet.getString(j);
                    if (SHOW_ALL) System.out.println("  entry: " + entry);
                    list.add(entry);
                }
                data.put(key, list.toArray(new String[list.size()]));
            }
            zoneData.put(locale, data);
        }
        String[] strings = (String[]) data.get(olsonID);
        if (strings == null || item >= strings.length) return olsonID;
        return strings[item];
    }
    
    static String[][] zonesAliases = {
        {"America/Atka", "America/Atka"},
        {"America/Ensenada", "America/Ensenada"},
        {"America/Fort_Wayne", "America/Fort_Wayne"},
        {"America/Indiana/Indianapolis", "America/Indiana/Indianapolis"},
        {"America/Kentucky/Louisville", "America/Kentucky/Louisville"},
        {"America/Knox_IN", "America/Knox_IN"},
        {"America/Porto_Acre", "America/Porto_Acre"},
        {"America/Rosario", "America/Rosario"},
        {"America/Shiprock", "America/Shiprock"},
        {"America/Virgin", "America/Virgin"},
        {"Antarctica/South_Pole", "Antarctica/South_Pole"},
        {"Arctic/Longyearbyen", "Arctic/Longyearbyen"},
        {"Asia/Ashkhabad", "Asia/Ashkhabad"},
        {"Asia/Chungking", "Asia/Chungking"},
        {"Asia/Dacca", "Asia/Dacca"},
        {"Asia/Istanbul", "Asia/Istanbul"},
        {"Asia/Macao", "Asia/Macao"},
        {"Asia/Tel_Aviv", "Asia/Tel_Aviv"},
        {"Asia/Thimbu", "Asia/Thimbu"},
        {"Asia/Ujung_Pandang", "Asia/Ujung_Pandang"},
        {"Asia/Ulan_Bator", "Asia/Ulan_Bator"},
        {"Australia/ACT", "Australia/ACT"},
        {"Australia/Canberra", "Australia/Canberra"},
        {"Australia/LHI", "Australia/LHI"},
        {"Australia/NSW", "Australia/NSW"},
        {"Australia/North", "Australia/North"},
        {"Australia/Queensland", "Australia/Queensland"},
        {"Australia/South", "Australia/South"},
        {"Australia/Tasmania", "Australia/Tasmania"},
        {"Australia/Victoria", "Australia/Victoria"},
        {"Australia/West", "Australia/West"},
        {"Australia/Yancowinna", "Australia/Yancowinna"},
        {"Brazil/Acre", "Brazil/Acre"},
        {"Brazil/DeNoronha", "Brazil/DeNoronha"},
        {"Brazil/East", "Brazil/East"},
        {"Brazil/West", "Brazil/West"},
        {"CST6CDT", "CST6CDT"},
        {"Canada/Atlantic", "Canada/Atlantic"},
        {"Canada/Central", "Canada/Central"},
        {"Canada/East-Saskatchewan", "Canada/East-Saskatchewan"},
        {"Canada/Eastern", "Canada/Eastern"},
        {"Canada/Mountain", "Canada/Mountain"},
        {"Canada/Newfoundland", "Canada/Newfoundland"},
        {"Canada/Pacific", "Canada/Pacific"},
        {"Canada/Saskatchewan", "Canada/Saskatchewan"},
        {"Canada/Yukon", "Canada/Yukon"},
        {"Chile/Continental", "Chile/Continental"},
        {"Chile/EasterIsland", "Chile/EasterIsland"},
        {"Cuba", "Cuba"},
        {"EST", "EST"},
        {"EST5EDT", "EST5EDT"},
        {"Egypt", "Egypt"},
        {"Eire", "Eire"},
        {"Etc/GMT+0", "Etc/GMT+0"},
        {"Etc/GMT-0", "Etc/GMT-0"},
        {"Etc/GMT0", "Etc/GMT0"},
        {"Etc/Greenwich", "Etc/Greenwich"},
        {"Etc/Universal", "Etc/Universal"},
        {"Etc/Zulu", "Etc/Zulu"},
        {"Europe/Nicosia", "Europe/Nicosia"},
        {"Europe/Tiraspol", "Europe/Tiraspol"},
        {"GB", "GB"},
        {"GB-Eire", "GB-Eire"},
        {"GMT", "GMT"},
        {"GMT+0", "GMT+0"},
        {"GMT-0", "GMT-0"},
        {"GMT0", "GMT0"},
        {"Greenwich", "Greenwich"},
        {"HST", "HST"},
        {"Hongkong", "Hongkong"},
        {"Iceland", "Iceland"},
        {"Iran", "Iran"},
        {"Israel", "Israel"},
        {"Jamaica", "Jamaica"},
        {"Japan", "Japan"},
        {"Kwajalein", "Kwajalein"},
        {"Libya", "Libya"},
        {"MST", "MST"},
        {"MST7MDT", "MST7MDT"},
        {"Mexico/BajaNorte", "Mexico/BajaNorte"},
        {"Mexico/BajaSur", "Mexico/BajaSur"},
        {"Mexico/General", "Mexico/General"},
        {"Mideast/Riyadh87", "Mideast/Riyadh87"},
        {"Mideast/Riyadh88", "Mideast/Riyadh88"},
        {"Mideast/Riyadh89", "Mideast/Riyadh89"},
        {"NZ", "NZ"},
        {"NZ-CHAT", "NZ-CHAT"},
        {"Navajo", "Navajo"},
        {"PRC", "PRC"},
        {"PST8PDT", "PST8PDT"},
        {"Pacific/Samoa", "Pacific/Samoa"},
        {"Poland", "Poland"},
        {"Portugal", "Portugal"},
        {"ROC", "ROC"},
        {"ROK", "ROK"},
        {"Singapore", "Singapore"},
        {"SystemV/AST4", "SystemV/AST4"},
        {"SystemV/AST4ADT", "SystemV/AST4ADT"},
        {"SystemV/CST6", "SystemV/CST6"},
        {"SystemV/CST6CDT", "SystemV/CST6CDT"},
        {"SystemV/EST5", "SystemV/EST5"},
        {"SystemV/EST5EDT", "SystemV/EST5EDT"},
        {"SystemV/HST10", "SystemV/HST10"},
        {"SystemV/MST7", "SystemV/MST7"},
        {"SystemV/MST7MDT", "SystemV/MST7MDT"},
        {"SystemV/PST8", "SystemV/PST8"},
        {"SystemV/PST8PDT", "SystemV/PST8PDT"},
        {"SystemV/YST9", "SystemV/YST9"},
        {"SystemV/YST9YDT", "SystemV/YST9YDT"},
        {"Turkey", "Turkey"},
        {"UCT", "UCT"},
        {"US/Alaska", "US/Alaska"},
        {"US/Aleutian", "US/Aleutian"},
        {"US/Arizona", "US/Arizona"},
        {"US/Central", "US/Central"},
        {"US/East-Indiana", "US/East-Indiana"},
        {"US/Eastern", "US/Eastern"},
        {"US/Hawaii", "US/Hawaii"},
        {"US/Indiana-Starke", "US/Indiana-Starke"},
        {"US/Michigan", "US/Michigan"},
        {"US/Mountain", "US/Mountain"},
        {"US/Pacific", "US/Pacific"},
        {"US/Pacific-New", "US/Pacific-New"},
        {"US/Samoa", "US/Samoa"},
        {"UTC", "UTC"},
        {"Universal", "Universal"},
        {"W-SU", "W-SU"},
        {"Zulu", "Zulu"},
        {"ACT", "ACT"},
        {"AET", "AET"},
        {"AGT", "AGT"},
        {"ART", "ART"},
        {"AST", "AST"},
        {"BET", "BET"},
        {"BST", "BST"},
        {"CAT", "CAT"},
        {"CNT", "CNT"},
        {"CST", "CST"},
        {"CTT", "CTT"},
        {"EAT", "EAT"},
        {"ECT", "ECT"},
        {"IET", "IET"},
        {"IST", "IST"},
        {"JST", "JST"},
        {"MIT", "MIT"},
        {"NET", "NET"},
        {"NST", "NST"},
        {"PLT", "PLT"},
        {"PNT", "PNT"},
        {"PRT", "PRT"},
        {"PST", "PST"},
        {"SST", "SST"},
        {"VST", "VST"},
    };

    /**
     * Hack to get code list
     * @return
     */
    private static String[] getCodes(ULocale locale, String tableName) {
        // TODO remove Ugly Hack
        // get stuff
        ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(locale);
        ICUResourceBundle table = bundle.getWithFallback(tableName);
        // copy into array
        ArrayList stuff = new ArrayList();
        for (Enumeration keys = table.getKeys(); keys.hasMoreElements();) {
            stuff.add(keys.nextElement());
        }
        String[] result = new String[stuff.size()];
        return (String[]) stuff.toArray(result);
        //return new String[] {"Latn", "Cyrl"};
    }

    /**
     * Add two unknown strings, just to make sure they get passed through without colliding
     * @param strings
     * @return
     */
    private String[] addUnknown(String[] strings, int len) {
        String[] result = new String[strings.length + 2];
        result[0] = "x1unknown".substring(0,len);
        result[1] = "y1nknown".substring(0,len);
        System.arraycopy(strings,0,result,2,strings.length);
        return result;
    }
    
    Map bogusZones = null;
    
    private Map getAliasMap() {
        if (bogusZones == null) {
            bogusZones = new TreeMap();
            for (int i = 0; i < zonesAliases.length; ++i) {
                bogusZones.put(zonesAliases[i][0], zonesAliases[i][1]);
            }
        }
        return bogusZones;
    }


    private void check(String type, ULocale locale, 
      String[] codes, Object[] contextList, DisplayNameGetter getter) {
        if (contextList == null) contextList = NO_CONTEXT;
        for (int k = 0; k < contextList.length; ++k) codeToName[k].clear();
        for (int j = 0; j < codes.length; ++j) {
            String code = codes[j];
            for (int k = 0; k < contextList.length; ++k) {
                Object context = contextList[k];
                String name = getter.get(locale, code, context);
                if (name == null || name.length() == 0) {
                    errln(
                        "Null or Zero-Length Display Name\t" + type
                        + "\t(" + ((context != null) ? context : "") + ")"
                        + ":\t" + locale + " [" + locale.getDisplayName(ULocale.ENGLISH) + "]"
                        + "\t" + code + " [" + getter.get(ULocale.ENGLISH, code, context) + "]"
                    );
                    continue;            
                }
                String otherCode = (String) codeToName[k].get(name);
                if (otherCode != null) {
                    errln(
                        "Display Names collide for\t" + type                        + "\t(" + ((context != null) ? context : "") + ")"
                        + ":\t" + locale + " [" + locale.getDisplayName(ULocale.ENGLISH) + "]"
                        + "\t" + code + " [" + getter.get(ULocale.ENGLISH, code, context) + "]"
                        + "\t& " + otherCode + " [" + getter.get(ULocale.ENGLISH, otherCode, context) + "]"
                        + "\t=> " + name
                    );
                } else {
                    codeToName[k].put(name, code);
                    if (SHOW_ALL) logln(
                        type 
                        + " (" + ((context != null) ? context : "") + ")"
                        + "\t" + locale + " [" + locale.getDisplayName(ULocale.ENGLISH) + "]"
                        + "\t" + code + "[" + getter.get(ULocale.ENGLISH, code, context) + "]"
                        + "\t=> " + name 
                    );
                }
            }
        }
    }
}
