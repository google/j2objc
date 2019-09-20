/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationTailoring.java, ported from collationtailoring.h/.cpp
*
* C++ version created on: 2013mar12
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.Map;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;

/**
 * Collation tailoring data & settings.
 * This is a container of values for a collation tailoring
 * built from rules or deserialized from binary data.
 *
 * It is logically immutable: Do not modify its values.
 * The fields are public for convenience.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationTailoring {
    CollationTailoring(SharedObject.Reference<CollationSettings> baseSettings) {
        if(baseSettings != null) {
            assert(baseSettings.readOnly().reorderCodes.length == 0);
            assert(baseSettings.readOnly().reorderTable == null);
            assert(baseSettings.readOnly().minHighNoReorder == 0);
            settings = baseSettings.clone();
        } else {
            settings = new SharedObject.Reference<CollationSettings>(new CollationSettings());
        }
    }

    void ensureOwnedData() {
        if(ownedData == null) {
            Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
            ownedData = new CollationData(nfcImpl);
        }
        data = ownedData;
    }

    /** Not thread-safe, call only before sharing. */
    void setRules(String r) {
        assert rules == null && rulesResource == null;
        rules = r;
    }
    /** Not thread-safe, call only before sharing. */
    void setRulesResource(UResourceBundle res) {
        assert rules == null && rulesResource == null;
        rulesResource = res;
    }
    public String getRules() {
        if (rules != null) {
            return rules;
        }
        if (rulesResource != null) {
            return rulesResource.getString();
        }
        return "";
    }

    static VersionInfo makeBaseVersion(VersionInfo ucaVersion) {
        return VersionInfo.getInstance(
                VersionInfo.UCOL_BUILDER_VERSION.getMajor(),
                (ucaVersion.getMajor() << 3) + ucaVersion.getMinor(),
                ucaVersion.getMilli() << 6,
                0);
    }
    void setVersion(int baseVersion, int rulesVersion) {
        // See comments for version field.
        int r = (rulesVersion >> 16) & 0xff00;
        int s = (rulesVersion >> 16) & 0xff;
        int t = (rulesVersion >> 8) & 0xff;
        int q = rulesVersion & 0xff;
        version = (VersionInfo.UCOL_BUILDER_VERSION.getMajor() << 24) |
                (baseVersion & 0xffc000) |  // UCA version u.v.w
                ((r + (r >> 6)) & 0x3f00) |
                (((s << 3) + (s >> 5) + t + (q << 4) + (q >> 4)) & 0xff);
    }
    int getUCAVersion() {
        // Version second byte/bits 23..16 to bits 11..4,
        // third byte/bits 15..14 to bits 1..0.
        return ((version >> 12) & 0xff0) | ((version >> 14) & 3);
    }

    // data for sorting etc.
    public CollationData data;  // == base data or ownedData
    public SharedObject.Reference<CollationSettings> settings;  // reference-counted
    // In Java, deserialize the rules string from the resource bundle
    // only when it is used. (It can be large and is rarely used.)
    private String rules;
    private UResourceBundle rulesResource;
    // The locale is null (C++: bogus) when built from rules or constructed from a binary blob.
    // It can then be set by the service registration code which is thread-safe.
    public ULocale actualLocale = ULocale.ROOT;
    // UCA version u.v.w & rules version r.s.t.q:
    // version[0]: builder version (runtime version is mixed in at runtime)
    // version[1]: bits 7..3=u, bits 2..0=v
    // version[2]: bits 7..6=w, bits 5..0=r
    // version[3]= (s<<5)+(s>>3)+t+(q<<4)+(q>>4)
    public int version = 0;

    // owned objects
    CollationData ownedData;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    public Map<Integer, Integer> maxExpansions;

    /*
     * Not Cloneable: A CollationTailoring cannot be copied.
     * It is immutable, and the data trie cannot be copied either.
     */
}
