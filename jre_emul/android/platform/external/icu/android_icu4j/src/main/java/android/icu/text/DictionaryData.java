/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.text;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.icu.impl.Assert;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.UResourceBundle;

final class DictionaryData {
    // disallow instantiation
    private DictionaryData() { }

    public static final int TRIE_TYPE_BYTES = 0;
    public static final int TRIE_TYPE_UCHARS = 1;
    public static final int TRIE_TYPE_MASK = 7;
    public static final int TRIE_HAS_VALUES = 8;
    public static final int TRANSFORM_NONE = 0;
    public static final int TRANSFORM_TYPE_OFFSET = 0x1000000;
    public static final int TRANSFORM_TYPE_MASK = 0x7f000000;
    public static final int TRANSFORM_OFFSET_MASK = 0x1fffff;

    public static final int IX_STRING_TRIE_OFFSET = 0;
    public static final int IX_RESERVED1_OFFSET = 1;
    public static final int IX_RESERVED2_OFFSET = 2;
    public static final int IX_TOTAL_SIZE = 3;
    public static final int IX_TRIE_TYPE = 4;
    public static final int IX_TRANSFORM = 5;
    public static final int IX_RESERVED6 = 6;
    public static final int IX_RESERVED7 = 7;
    public static final int IX_COUNT = 8;

    private static final int DATA_FORMAT_ID = 0x44696374;

    public static DictionaryMatcher loadDictionaryFor(String dictType) throws IOException {
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME);
        String dictFileName = rb.getStringWithFallback("dictionaries/" + dictType);
        dictFileName = ICUData.ICU_BRKITR_NAME + '/' + dictFileName;
        ByteBuffer bytes = ICUBinary.getRequiredData(dictFileName);
        ICUBinary.readHeader(bytes, DATA_FORMAT_ID, null);
        int[] indexes = new int[IX_COUNT];
        // TODO: read indexes[IX_STRING_TRIE_OFFSET] first, then read a variable-length indexes[]
        for (int i = 0; i < IX_COUNT; i++) {
            indexes[i] = bytes.getInt();
        }
        int offset = indexes[IX_STRING_TRIE_OFFSET];
        Assert.assrt(offset >= (4 * IX_COUNT));
        if (offset > (4 * IX_COUNT)) {
            int diff = offset - (4 * IX_COUNT);
            ICUBinary.skipBytes(bytes, diff);
        }
        int trieType = indexes[IX_TRIE_TYPE] & TRIE_TYPE_MASK;
        int totalSize = indexes[IX_TOTAL_SIZE] - offset;
        DictionaryMatcher m = null;
        if (trieType == TRIE_TYPE_BYTES) {
            int transform = indexes[IX_TRANSFORM];
            byte[] data = new byte[totalSize];
            bytes.get(data);
            m = new BytesDictionaryMatcher(data, transform);
        } else if (trieType == TRIE_TYPE_UCHARS) {
            Assert.assrt(totalSize % 2 == 0);
            String data = ICUBinary.getString(bytes, totalSize / 2, totalSize & 1);
            m = new CharsDictionaryMatcher(data);
        } else {
            m = null;
        }
        return m;
    }
}
