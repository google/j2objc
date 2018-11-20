/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/*
 * New added, 2005-5-10 [Terry/SGL]
 * Major modification by Ram
 */

package android.icu.dev.test.util;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUData;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

public class LocaleAliasTest extends TestFmwk {
    private static final ULocale[][] _LOCALES = {

            {new ULocale("en", "RH"), new ULocale("en", "ZW")},
            {new ULocale("in"), new ULocale("id")},
            {new ULocale("in", "ID"), new ULocale("id", "ID")},
            {new ULocale("iw"), new ULocale("he")},
            {new ULocale("iw", "IL"), new ULocale("he", "IL")},
            {new ULocale("ji"), new ULocale("yi")},
            
            {new ULocale("en", "BU"), new ULocale("en", "MM")},
            {new ULocale("en", "DY"), new ULocale("en", "BJ")},
            {new ULocale("en", "HV"), new ULocale("en", "BF")},
            {new ULocale("en", "NH"), new ULocale("en", "VU")},
            {new ULocale("en", "TP"), new ULocale("en", "TL")},
            {new ULocale("en", "ZR"), new ULocale("en", "CD")}
    };
    
    private static final int _LOCALE_NUMBER = _LOCALES.length;
    private static ULocale[] available = null;
    private HashMap availableMap = new HashMap();
    private static final ULocale _DEFAULT_LOCALE = ULocale.US;
    
    public LocaleAliasTest() {
    }
    
    @Before
    public void init() {
        available = ULocale.getAvailableLocales();
        for(int i=0; i<available.length;i++){
            availableMap.put(available[i].toString(),"");
        }
    }

    @Test
    public void TestCalendar() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];
            if(availableMap.get(_LOCALES[i][1])==null){
                logln(_LOCALES[i][1]+" is not available. Skipping!");
                continue;
            }
            Calendar c1 = Calendar.getInstance(oldLoc);
            Calendar c2 = Calendar.getInstance(newLoc);
            c1.setTime(c2.getTime());
            //Test function "getFirstDayOfWeek"
    //        int firstDayOfWeek1 = c1.getFirstDayOfWeek();
    //        int firstDayOfWeek2 = c2.getFirstDayOfWeek();
    //        if (firstDayOfWeek1 != firstDayOfWeek2) {
    //            this.logln("Calendar(getFirstDayOfWeek) old:"
    //                    +firstDayOfWeek1+"   new:"+firstDayOfWeek2);
    //            pass = false;
    //        }
                    
            //Test function "getLocale(ULocale.VALID_LOCALE)"
            ULocale l1 = c1.getLocale(ULocale.VALID_LOCALE);
            ULocale l2 = c2.getLocale(ULocale.VALID_LOCALE);
            if (!newLoc.equals(l1)) {
                errln("CalendarTest: newLoc!=l1: newLoc= "+newLoc +" l1= "+l1);
            }
            if (!l1.equals(l2)) {
                errln("CalendarTest: l1!=l2: l1= "+l1 +" l2= "+l2);
            }
            if(!c1.equals(c2)){
                errln("CalendarTest: c1!=c2.  newLoc= "+newLoc +" oldLoc= "+oldLoc);
            }
            logln("Calendar(getLocale) old:"+l1+"   new:"+l2);    
        }
        ULocale.setDefault(defLoc);
    }
    
    @Test
    public void  TestDateFormat() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];
            if(availableMap.get(_LOCALES[i][1])==null){
                logln(_LOCALES[i][1]+" is not available. Skipping!");
                continue;
            }
            DateFormat df1 = DateFormat.getDateInstance(DateFormat.FULL, oldLoc);
            DateFormat df2 = DateFormat.getDateInstance(DateFormat.FULL, newLoc);
            
            //Test function "getLocale"
            ULocale l1 = df1.getLocale(ULocale.VALID_LOCALE);
            ULocale l2 = df2.getLocale(ULocale.VALID_LOCALE);
            if (!newLoc.equals(l1)) {
                errln("DateFormatTest: newLoc!=l1: newLoc= "+newLoc +" l1= "+l1);
            }
            if (!l1.equals(l2)) {
                errln("DateFormatTest: l1!=l2: l1= "+l1 +" l2= "+l2);
            }
            if (!df1.equals(df2)) {
                errln("DateFormatTest: df1!=df2: newLoc= "+newLoc +" oldLoc= "+oldLoc);
            }
            TestFmwk.logln("DateFormat(getLocale) old:"+l1+"   new:"+l2);
            
            //Test function "format"
    //        Date d = new Date();
    //        String d1 = df1.format(d);
    //        String d2 = df2.format(d);
    //        if (!d1.equals(d2)) {
    //            pass = false;
    //        }
    //        this.logln("DateFormat(format) old:"+d1+"   new:"+d2);
        }
        ULocale.setDefault(defLoc);
    }
    
    @Test
    public void TestULocale() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];
            if(availableMap.get(_LOCALES[i][1])==null){
                logln(_LOCALES[i][1]+" is not available. Skipping!");
                continue;
            }
            ULocale ul1 = new ULocale(oldLoc.toString());
            ULocale ul2 = new ULocale(newLoc.toString());
            
            String name1 = ul1.getDisplayName();
            String name2 = ul2.getDisplayName();
            if (!name1.equals(name2)) {
                errln("name1!=name2. name1 = " + name1 +" name2 = " +name2);
            }
            logln("ULocale(getDisplayName) old:"+name1+"   new:"+name2);
        }
        ULocale.setDefault(defLoc);
    }
    
    @Test
    public void TestDisplayName() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];

            for(int j=0; j<available.length; j++){
               String oldCountry = oldLoc.getDisplayCountry(available[j]);
               String newCountry = newLoc.getDisplayCountry(available[j]);
               String oldLang = oldLoc.getDisplayLanguage(available[j]);
               String newLang = newLoc.getDisplayLanguage(available[j]);
               
               // is  there  display name for the current country ID               
               if(!newCountry.equals(newLoc.getCountry())){
                   if(!oldCountry.equals(newCountry)){
                       errln("getCountry() failed for "+ oldLoc +" oldCountry= "+ prettify(oldCountry) +" newCountry = "+prettify(newCountry)+ " in display locale "+ available[j].toString());
                   }
               }
               //there is a display name for the current lang ID               
               if(!newLang.equals(newLoc.getLanguage())){
                   if(!oldLang.equals(newLang)){
                       errln("getLanguage() failed for " + oldLoc + " oldLang = "+ prettify(oldLang) +" newLang = "+prettify(newLang)+ " in display locale "+ available[j].toString());
                   }
               }
            }
        }
        ULocale.setDefault(defLoc);
    }

    @Test
    public void TestUResourceBundle() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            if(availableMap.get(_LOCALES[i][1])==null){
                logln(_LOCALES[i][1]+" is not available. Skipping!");
                continue;
            }
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];
            UResourceBundle urb1 = null;
            UResourceBundle urb2 = null;
            
            urb1 = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, oldLoc);
            urb2 = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, newLoc);
            ULocale l1 = urb1.getULocale();
            ULocale l2 = urb2.getULocale();        
            if (!newLoc.equals(l1)) {
                errln("ResourceBundleTest: newLoc!=l1: newLoc= "+newLoc +" l1= "+l1);
            }
            if (!l1.equals(l2)) {
                errln("ResourceBundleTest: l1!=l2: l1= "+l1 +" l2= "+l2);
            }
            TestFmwk.logln("UResourceBundle old:"+l1+"   new:"+l2);
        }
        ULocale.setDefault(defLoc);
    }
}
