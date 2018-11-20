/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl.locale;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import android.icu.impl.ValidIdentifiers;
import android.icu.impl.ValidIdentifiers.Datasubtype;
import android.icu.impl.ValidIdentifiers.Datatype;
import android.icu.impl.locale.KeyTypeData.ValueType;
import android.icu.util.IllformedLocaleException;
import android.icu.util.Output;
import android.icu.util.ULocale;

/**
 * @author markdavis
 * @hide Only a subset of ICU is exposed in Android
 *
 */
public class LocaleValidityChecker {
    private final Set<Datasubtype> datasubtypes;
    private final boolean allowsDeprecated;
    public static class Where {
        public Datatype fieldFailure;
        public String codeFailure;

        public boolean set(Datatype datatype, String code) {
            fieldFailure = datatype;
            codeFailure = code;
            return false;
        }
        @Override
        public String toString() {
            return fieldFailure == null ? "OK" : "{" + fieldFailure + ", " + codeFailure + "}";
        }
    }

    public LocaleValidityChecker(Set<Datasubtype> datasubtypes) {
        this.datasubtypes = EnumSet.copyOf(datasubtypes);
        allowsDeprecated = datasubtypes.contains(Datasubtype.deprecated);
    }

    public LocaleValidityChecker(Datasubtype... datasubtypes) {
        this.datasubtypes = EnumSet.copyOf(Arrays.asList(datasubtypes));
        allowsDeprecated = this.datasubtypes.contains(Datasubtype.deprecated);
    }

    /**
     * @return the datasubtypes
     */
    public Set<Datasubtype> getDatasubtypes() {
        return EnumSet.copyOf(datasubtypes);
    }

    static Pattern SEPARATOR = Pattern.compile("[-_]");

    @SuppressWarnings("unused")
    private static final Pattern VALID_X = Pattern.compile("[a-zA-Z0-9]{2,8}(-[a-zA-Z0-9]{2,8})*");

    public boolean isValid(ULocale locale, Where where) {
        where.set(null, null);
        final String language = locale.getLanguage();
        final String script = locale.getScript();
        final String region = locale.getCountry();
        final String variantString = locale.getVariant();
        final Set<Character> extensionKeys = locale.getExtensionKeys();
        //        if (language.isEmpty()) {
        //            // the only case where this is valid is if there is only an 'x' extension string
        //            if (!script.isEmpty() || !region.isEmpty() || variantString.isEmpty() 
        //                    || extensionKeys.size() != 1 || !extensionKeys.contains('x')) {
        //                return where.set(Datatype.x, "Null language only with x-...");
        //            }
        //            return true; // for x string, wellformedness = valid
        //        }
        if (!isValid(Datatype.language, language, where)) {
            // special case x
            if (language.equals("x")) {
                where.set(null, null); // for x, well-formed == valid
                return true;
            }
            return false;
        }
        if (!isValid(Datatype.script, script, where)) return false;
        if (!isValid(Datatype.region, region, where)) return false;
        if (!variantString.isEmpty()) {
            for (String variant : SEPARATOR.split(variantString)) {
                if (!isValid(Datatype.variant, variant, where)) return false;
            }
        }
        for (Character c : extensionKeys) {
            try {
                Datatype datatype = Datatype.valueOf(c+"");
                switch (datatype) {
                case x:
                    return true; // if it is syntactic (checked by ULocale) it is valid
                case t:
                case u:
                    if (!isValidU(locale, datatype, locale.getExtension(c), where)) return false;
                    break;
                }
            } catch (Exception e) {
                return where.set(Datatype.illegal, c+"");
            }
        }
        return true;
    }

    // TODO combine this with the KeyTypeData.SpecialType, and get it from the type, not the key
    enum SpecialCase {
        normal, anything, reorder, codepoints, subdivision, rgKey;
        static SpecialCase get(String key) {
            if (key.equals("kr")) {
                return reorder;
            } else if (key.equals("vt")) {
                return codepoints;
            } else if (key.equals("sd")) {
                return subdivision;
            } else if (key.equals("rg")) {
                return rgKey;
            } else if (key.equals("x0")) {
                return anything;
            } else {
                return normal;
            }
        }
    }

    /**
     * @param locale 
     * @param datatype 
     * @param extension
     * @param where
     * @return
     */
    private boolean isValidU(ULocale locale, Datatype datatype, String extensionString, Where where) {
        String key = "";
        int typeCount = 0;
        ValueType valueType = null;
        SpecialCase specialCase = null;
        StringBuilder prefix = new StringBuilder();
        Set<String> seen = new HashSet<String>();

        StringBuilder tBuffer = datatype == Datatype.t ? new StringBuilder() : null;

        // TODO: is empty -u- valid?

        for (String subtag : SEPARATOR.split(extensionString)) {
            if (subtag.length() == 2 
                    && (tBuffer == null || subtag.charAt(1) <= '9')) {
                // if we have accumulated a t buffer, check that first
                if (tBuffer != null) {
                    // Check t buffer. Empty after 't' is ok.
                    if (tBuffer.length() != 0 && !isValidLocale(tBuffer.toString(),where)) {
                        return false;
                    }
                    tBuffer = null;
                }
                key = KeyTypeData.toBcpKey(subtag);
                if (key == null) {
                    return where.set(datatype, subtag);
                }
                if (!allowsDeprecated && KeyTypeData.isDeprecated(key)) {
                    return where.set(datatype, key);
                }
                valueType = KeyTypeData.getValueType(key);
                specialCase = SpecialCase.get(key);
                typeCount = 0;
            } else if (tBuffer != null) {
                if (tBuffer.length() != 0) {
                    tBuffer.append('-');
                }
                tBuffer.append(subtag);
            } else {
                ++typeCount;
                switch (valueType) {
                case single: 
                    if (typeCount > 1) {
                        return where.set(datatype, key+"-"+subtag);
                    }
                    break;
                case incremental:
                    if (typeCount == 1) {
                        prefix.setLength(0);
                        prefix.append(subtag);
                    } else {
                        prefix.append('-').append(subtag);
                        subtag = prefix.toString();
                    }
                    break;
                case multiple:
                    if (typeCount == 1) {
                        seen.clear();
                    }
                    break;
                }
                switch (specialCase) {
                case anything: 
                    continue;
                case codepoints: 
                    try {
                        if (Integer.parseInt(subtag,16) > 0x10FFFF) {
                            return where.set(datatype, key+"-"+subtag);
                        }
                    } catch (NumberFormatException e) {
                        return where.set(datatype, key+"-"+subtag);
                    }
                    continue;
                case reorder:
                    boolean newlyAdded = seen.add(subtag.equals("zzzz") ? "others" : subtag);
                    if (!newlyAdded || !isScriptReorder(subtag)) {
                        return where.set(datatype, key+"-"+subtag);
                    }
                    continue;
                case subdivision:
                    if (!isSubdivision(locale, subtag)) {
                        return where.set(datatype, key+"-"+subtag);
                    }
                    continue;
                case rgKey:
                    if (subtag.length() < 6 || !subtag.endsWith("zzzz")) {
                        return where.set(datatype, subtag);
                    }
                    if (!isValid(Datatype.region, subtag.substring(0,subtag.length()-4), where)) {
                        return false;
                    }
                    continue;
                }

                // en-u-sd-usca
                // en-US-u-sd-usca
                Output<Boolean> isKnownKey = new Output<Boolean>();
                Output<Boolean> isSpecialType = new Output<Boolean>();
                String type = KeyTypeData.toBcpType(key, subtag, isKnownKey, isSpecialType);
                if (type == null) {
                    return where.set(datatype, key+"-"+subtag);
                }
                if (!allowsDeprecated && KeyTypeData.isDeprecated(key, subtag)) {
                    return where.set(datatype, key+"-"+subtag);
                }
            }
        }
        // Check t buffer. Empty after 't' is ok.
        if (tBuffer != null && tBuffer.length() != 0 && !isValidLocale(tBuffer.toString(),where)) {
            return false;
        }
        return true;
    }

    /**
     * @param locale
     * @param subtag
     * @return
     */
    private boolean isSubdivision(ULocale locale, String subtag) {
        // First check if the subtag is valid
        if (subtag.length() < 3) {
            return false;
        }
        String region = subtag.substring(0, subtag.charAt(0) <= '9' ? 3 : 2);
        String subdivision = subtag.substring(region.length());
        if (ValidIdentifiers.isValid(Datatype.subdivision, datasubtypes, region, subdivision) == null) {
            return false;
        }
        // Then check for consistency with the locale's region
        String localeRegion = locale.getCountry();
        if (localeRegion.isEmpty()) {
            ULocale max = ULocale.addLikelySubtags(locale);
            localeRegion = max.getCountry();
        }
        if (!region.equalsIgnoreCase(localeRegion)) {
            return false;
        }
        return true;
    }

    static final Set<String> REORDERING_INCLUDE = new HashSet<String>(Arrays.asList("space", "punct", "symbol", "currency", "digit", "others", "zzzz"));
    static final Set<String> REORDERING_EXCLUDE = new HashSet<String>(Arrays.asList("zinh", "zyyy"));
    static final Set<Datasubtype> REGULAR_ONLY = EnumSet.of(Datasubtype.regular);
    /**
     * @param subtag
     * @return
     */
    private boolean isScriptReorder(String subtag) {
        subtag = AsciiUtil.toLowerString(subtag);
        if (REORDERING_INCLUDE.contains(subtag)) {
            return true;
        } else if (REORDERING_EXCLUDE.contains(subtag)) {
            return false;
        }
        return ValidIdentifiers.isValid(Datatype.script, REGULAR_ONLY, subtag) != null;
        //        space, punct, symbol, currency, digit - core groups of characters below 'a'
        //        any script code except Common and Inherited.
        //      sc ; Zinh                             ; Inherited                        ; Qaai
        //      sc ; Zyyy                             ; Common
        //        Some pairs of scripts sort primary-equal and always reorder together. For example, Katakana characters are are always reordered with Hiragana.
        //        others - where all codes not explicitly mentioned should be ordered. The script code Zzzz (Unknown Script) is a synonym for others.        return false;
    }

    /**
     * @param extensionString
     * @param where
     * @return
     */
    private boolean isValidLocale(String extensionString, Where where) {
        try {
            ULocale locale = new ULocale.Builder().setLanguageTag(extensionString).build();
            return isValid(locale, where);
        } catch (IllformedLocaleException e) {
            int startIndex = e.getErrorIndex();
            String[] list = SEPARATOR.split(extensionString.substring(startIndex));
            return where.set(Datatype.t, list[0]);
        } catch (Exception e) {
            return where.set(Datatype.t, e.getMessage());
        }
    }

    /**
     * @param language
     * @param language2
     * @return
     */
    private boolean isValid(Datatype datatype, String code, Where where) {
        return code.isEmpty() ? true :
            ValidIdentifiers.isValid(datatype, datasubtypes, code) != null ? true : 
                where == null ? false 
                        : where.set(datatype, code);
    }
}
