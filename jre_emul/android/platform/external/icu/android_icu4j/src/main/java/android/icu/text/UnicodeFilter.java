/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

/**
 * <code>UnicodeFilter</code> defines a protocol for selecting a
 * subset of the full range (U+0000 to U+FFFF) of Unicode characters.
 */
@SuppressWarnings("javadoc")    // com.imb.icu.text.Transliterator is in another project
public abstract class UnicodeFilter implements UnicodeMatcher {

    /**
     * Returns <tt>true</tt> for characters that are in the selected
     * subset.  In other words, if a character is <b>to be
     * filtered</b>, then <tt>contains()</tt> returns
     * <b><tt>false</tt></b>.
     */
    public abstract boolean contains(int c);

    /**
     * Default implementation of UnicodeMatcher::matches() for Unicode
     * filters.  Matches a single 16-bit code unit at offset.
     */
    @Override
    public int matches(Replaceable text,
                       int[] offset,
                       int limit,
                       boolean incremental) {
        int c;
        if (offset[0] < limit &&
            contains(c = text.char32At(offset[0]))) {
            offset[0] += UTF16.getCharCount(c);
            return U_MATCH;
        }
        if (offset[0] > limit && contains(text.char32At(offset[0]))) {
            // Backup offset by 1, unless the preceding character is a
            // surrogate pair -- then backup by 2 (keep offset pointing at
            // the lead surrogate).
            --offset[0];
            if (offset[0] >= 0) {
                offset[0] -= UTF16.getCharCount(text.char32At(offset[0])) - 1;
            }
            return U_MATCH;
        }
        if (incremental && offset[0] == limit) {
            return U_PARTIAL_MATCH;
        }
        return U_MISMATCH;
    }

    // TODO Remove this when the JDK property implements MemberDoc.isSynthetic
    /**
     * (This should not be here; it is declared to make CheckTags
     * happy.  Java inserts a synthetic constructor and CheckTags
     * can't tell that it's synthetic.)
     *
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected UnicodeFilter() {}
}
