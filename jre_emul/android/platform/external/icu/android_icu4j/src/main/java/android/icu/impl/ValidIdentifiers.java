/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.icu.impl.locale.AsciiUtil;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;

/**
 * @author markdavis
 * @hide Only a subset of ICU is exposed in Android
 *
 */
public class ValidIdentifiers {

    public enum Datatype {
        currency,
        language,
        region,
        script,
        subdivision,
        unit,
        variant,
        u,
        t,
        x,
        illegal
    }

    public enum Datasubtype {
        deprecated,
        private_use,
        regular,
        special,
        unknown,
        macroregion,
    }

    public static class ValiditySet {
        public final Set<String> regularData;
        public final Map<String,Set<String>> subdivisionData;
        public ValiditySet(Set<String> plainData, boolean makeMap) {
            if (makeMap) {
                HashMap<String,Set<String>> _subdivisionData = new HashMap<String,Set<String>>();
                for (String s : plainData) {
                    int pos = s.indexOf('-'); // read v28 data also
                    int pos2 = pos+1;
                    if (pos < 0) {
                        pos2 = pos = s.charAt(0) < 'A' ? 3 : 2;
                    }
                    final String key = s.substring(0, pos);
                    final String subdivision = s.substring(pos2);

                    Set<String> oldSet = _subdivisionData.get(key);
                    if (oldSet == null) {
                        _subdivisionData.put(key, oldSet = new HashSet<String>());
                    }
                    oldSet.add(subdivision);
                }
                this.regularData = null;
                HashMap<String,Set<String>> _subdivisionData2 = new HashMap<String,Set<String>>();
                // protect the sets
                for (Entry<String, Set<String>> e : _subdivisionData.entrySet()) {
                    Set<String> value = e.getValue();
                    // optimize a bit by using singleton
                    Set<String> set = value.size() == 1 ? Collections.singleton(value.iterator().next()) 
                            : Collections.unmodifiableSet(value);
                    _subdivisionData2.put(e.getKey(), set);
                }

                this.subdivisionData = Collections.unmodifiableMap(_subdivisionData2);
            } else {
                this.regularData = Collections.unmodifiableSet(plainData);
                this.subdivisionData = null;
            }
        }

        public boolean contains(String code) {
            if (regularData != null) {
                return regularData.contains(code);
            } else {
                int pos = code.indexOf('-');
                String key = code.substring(0,pos);
                final String value = code.substring(pos+1);
                return contains(key, value);
            }
        }
        
        public boolean contains(String key, String value) {
            Set<String> oldSet = subdivisionData.get(key);
            return oldSet != null && oldSet.contains(value);
        }
        
        @Override
        public String toString() {
            if (regularData != null) {
                return regularData.toString();
            } else {
                return subdivisionData.toString();
            }
        }
    }

    private static class ValidityData {
        static final Map<Datatype,Map<Datasubtype,ValiditySet>> data;
        static {
            Map<Datatype, Map<Datasubtype, ValiditySet>> _data = new EnumMap<Datatype,Map<Datasubtype,ValiditySet>>(Datatype.class);
            UResourceBundle suppData = UResourceBundle.getBundleInstance(
                    ICUData.ICU_BASE_NAME,
                    "supplementalData",
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle validityInfo = suppData.get("idValidity");
            for(UResourceBundleIterator datatypeIterator = validityInfo.getIterator(); 
                    datatypeIterator.hasNext();) {
                UResourceBundle datatype = datatypeIterator.next();
                String rawKey = datatype.getKey();
                Datatype key = Datatype.valueOf(rawKey);
                Map<Datasubtype,ValiditySet> values = new EnumMap<Datasubtype,ValiditySet>(Datasubtype.class);
                for(UResourceBundleIterator datasubtypeIterator = datatype.getIterator(); 
                        datasubtypeIterator.hasNext();) {
                    UResourceBundle datasubtype = datasubtypeIterator.next();
                    String rawsubkey = datasubtype.getKey();
                    Datasubtype subkey = Datasubtype.valueOf(rawsubkey);
                    // handle single value specially
                    Set<String> subvalues = new HashSet<String>();
                    if (datasubtype.getType() == UResourceBundle.STRING) {
                        addRange(datasubtype.getString(), subvalues);
                    } else {
                        for (String string : datasubtype.getStringArray()) {
                            addRange(string, subvalues);
                        }
                    }
                    values.put(subkey, new ValiditySet(subvalues, key == Datatype.subdivision));
                }
                _data.put(key, Collections.unmodifiableMap(values));
            }
            data = Collections.unmodifiableMap(_data);
        }
        private static void addRange(String string, Set<String> subvalues) {
            string = AsciiUtil.toLowerString(string);
            int pos = string.indexOf('~');
            if (pos < 0) {
                subvalues.add(string);
            } else {
                StringRange.expand(string.substring(0,pos), string.substring(pos+1), false, subvalues);
            }
        }
    }
    
    public static Map<Datatype, Map<Datasubtype, ValiditySet>> getData() {
        return ValidityData.data;
    }

    /**
     * Returns the Datasubtype containing the code, or null if there is none.
     */
    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> datasubtypes, String code) {
        Map<Datasubtype, ValiditySet> subtable = ValidityData.data.get(datatype);
        if (subtable != null) {
            for (Datasubtype datasubtype : datasubtypes) {
                ValiditySet validitySet = subtable.get(datasubtype);
                if (validitySet != null) {
                    if (validitySet.contains(AsciiUtil.toLowerString(code))) {
                        return datasubtype;
                    }
                }
            }
        }
        return null;
    }
    
    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> datasubtypes, String code, String value) {
        Map<Datasubtype, ValiditySet> subtable = ValidityData.data.get(datatype);
        if (subtable != null) {
            code = AsciiUtil.toLowerString(code);
            value = AsciiUtil.toLowerString(value);
            for (Datasubtype datasubtype : datasubtypes) {
                ValiditySet validitySet = subtable.get(datasubtype);
                if (validitySet != null) {
                    if (validitySet.contains(code, value)) {
                        return datasubtype;
                    }
                }
            }
        }
        return null;
    }
}
