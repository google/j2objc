/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 *   Copyright (C) 2009-2015, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package android.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class Normalizer2Impl {
    public static final class Hangul {
        /* Korean Hangul and Jamo constants */
        public static final int JAMO_L_BASE=0x1100;     /* "lead" jamo */
        public static final int JAMO_L_END=0x1112;
        public static final int JAMO_V_BASE=0x1161;     /* "vowel" jamo */
        public static final int JAMO_V_END=0x1175;
        public static final int JAMO_T_BASE=0x11a7;     /* "trail" jamo */
        public static final int JAMO_T_END=0x11c2;

        public static final int HANGUL_BASE=0xac00;
        public static final int HANGUL_END=0xd7a3;

        public static final int JAMO_L_COUNT=19;
        public static final int JAMO_V_COUNT=21;
        public static final int JAMO_T_COUNT=28;

        public static final int JAMO_L_LIMIT=JAMO_L_BASE+JAMO_L_COUNT;
        public static final int JAMO_V_LIMIT=JAMO_V_BASE+JAMO_V_COUNT;

        public static final int JAMO_VT_COUNT=JAMO_V_COUNT*JAMO_T_COUNT;

        public static final int HANGUL_COUNT=JAMO_L_COUNT*JAMO_V_COUNT*JAMO_T_COUNT;
        public static final int HANGUL_LIMIT=HANGUL_BASE+HANGUL_COUNT;

        public static boolean isHangul(int c) {
            return HANGUL_BASE<=c && c<HANGUL_LIMIT;
        }
        public static boolean isHangulWithoutJamoT(char c) {
            c-=HANGUL_BASE;
            return c<HANGUL_COUNT && c%JAMO_T_COUNT==0;
        }
        public static boolean isJamoL(int c) {
            return JAMO_L_BASE<=c && c<JAMO_L_LIMIT;
        }
        public static boolean isJamoV(int c) {
            return JAMO_V_BASE<=c && c<JAMO_V_LIMIT;
        }

        /**
         * Decomposes c, which must be a Hangul syllable, into buffer
         * and returns the length of the decomposition (2 or 3).
         */
        public static int decompose(int c, Appendable buffer) {
            try {
                c-=HANGUL_BASE;
                int c2=c%JAMO_T_COUNT;
                c/=JAMO_T_COUNT;
                buffer.append((char)(JAMO_L_BASE+c/JAMO_V_COUNT));
                buffer.append((char)(JAMO_V_BASE+c%JAMO_V_COUNT));
                if(c2==0) {
                    return 2;
                } else {
                    buffer.append((char)(JAMO_T_BASE+c2));
                    return 3;
                }
            } catch(IOException e) {
                // Will not occur because we do not write to I/O.
                throw new ICUUncheckedIOException(e);
            }
        }

        /**
         * Decomposes c, which must be a Hangul syllable, into buffer.
         * This is the raw, not recursive, decomposition. Its length is always 2.
         */
        public static void getRawDecomposition(int c, Appendable buffer) {
            try {
                int orig=c;
                c-=HANGUL_BASE;
                int c2=c%JAMO_T_COUNT;
                if(c2==0) {
                    c/=JAMO_T_COUNT;
                    buffer.append((char)(JAMO_L_BASE+c/JAMO_V_COUNT));
                    buffer.append((char)(JAMO_V_BASE+c%JAMO_V_COUNT));
                } else {
                    buffer.append((char)(orig-c2));  // LV syllable
                    buffer.append((char)(JAMO_T_BASE+c2));
                }
            } catch(IOException e) {
                // Will not occur because we do not write to I/O.
                throw new ICUUncheckedIOException(e);
            }
        }
    }

    /**
     * Writable buffer that takes care of canonical ordering.
     * Its Appendable methods behave like the C++ implementation's
     * appendZeroCC() methods.
     * <p>
     * If dest is a StringBuilder, then the buffer writes directly to it.
     * Otherwise, the buffer maintains a StringBuilder for intermediate text segments
     * until no further changes are necessary and whole segments are appended.
     * append() methods that take combining-class values always write to the StringBuilder.
     * Other append() methods flush and append to the Appendable.
     */
    public static final class ReorderingBuffer implements Appendable {
        public ReorderingBuffer(Normalizer2Impl ni, Appendable dest, int destCapacity) {
            impl=ni;
            app=dest;
            if(app instanceof StringBuilder) {
                appIsStringBuilder=true;
                str=(StringBuilder)dest;
                // In Java, the constructor subsumes public void init(int destCapacity) {
                str.ensureCapacity(destCapacity);
                reorderStart=0;
                if(str.length()==0) {
                    lastCC=0;
                } else {
                    setIterator();
                    lastCC=previousCC();
                    // Set reorderStart after the last code point with cc<=1 if there is one.
                    if(lastCC>1) {
                        while(previousCC()>1) {}
                    }
                    reorderStart=codePointLimit;
                }
            } else {
                appIsStringBuilder=false;
                str=new StringBuilder();
                reorderStart=0;
                lastCC=0;
            }
        }

        public boolean isEmpty() { return str.length()==0; }
        public int length() { return str.length(); }
        public int getLastCC() { return lastCC; }

        public StringBuilder getStringBuilder() { return str; }

        public boolean equals(CharSequence s, int start, int limit) {
            return UTF16Plus.equal(str, 0, str.length(), s, start, limit);
        }

        // For Hangul composition, replacing the Leading consonant Jamo with the syllable.
        public void setLastChar(char c) {
            str.setCharAt(str.length()-1, c);
        }

        public void append(int c, int cc) {
            if(lastCC<=cc || cc==0) {
                str.appendCodePoint(c);
                lastCC=cc;
                if(cc<=1) {
                    reorderStart=str.length();
                }
            } else {
                insert(c, cc);
            }
        }
        // s must be in NFD, otherwise change the implementation.
        public void append(CharSequence s, int start, int limit,
                           int leadCC, int trailCC) {
            if(start==limit) {
                return;
            }
            if(lastCC<=leadCC || leadCC==0) {
                if(trailCC<=1) {
                    reorderStart=str.length()+(limit-start);
                } else if(leadCC<=1) {
                    reorderStart=str.length()+1;  // Ok if not a code point boundary.
                }
                str.append(s, start, limit);
                lastCC=trailCC;
            } else {
                int c=Character.codePointAt(s, start);
                start+=Character.charCount(c);
                insert(c, leadCC);  // insert first code point
                while(start<limit) {
                    c=Character.codePointAt(s, start);
                    start+=Character.charCount(c);
                    if(start<limit) {
                        // s must be in NFD, otherwise we need to use getCC().
                        leadCC=getCCFromYesOrMaybe(impl.getNorm16(c));
                    } else {
                        leadCC=trailCC;
                    }
                    append(c, leadCC);
                }
            }
        }
        // The following append() methods work like C++ appendZeroCC().
        // They assume that the cc or trailCC of their input is 0.
        // Most of them implement Appendable interface methods.
        // @Override when we switch to Java 6
        @Override
        public ReorderingBuffer append(char c) {
            str.append(c);
            lastCC=0;
            reorderStart=str.length();
            return this;
        }
        public void appendZeroCC(int c) {
            str.appendCodePoint(c);
            lastCC=0;
            reorderStart=str.length();
        }
        // @Override when we switch to Java 6
        @Override
        public ReorderingBuffer append(CharSequence s) {
            if(s.length()!=0) {
                str.append(s);
                lastCC=0;
                reorderStart=str.length();
            }
            return this;
        }
        // @Override when we switch to Java 6
        @Override
        public ReorderingBuffer append(CharSequence s, int start, int limit) {
            if(start!=limit) {
                str.append(s, start, limit);
                lastCC=0;
                reorderStart=str.length();
            }
            return this;
        }
        /**
         * Flushes from the intermediate StringBuilder to the Appendable,
         * if they are different objects.
         * Used after recomposition.
         * Must be called at the end when writing to a non-StringBuilder Appendable.
         */
        public void flush() {
            if(appIsStringBuilder) {
                reorderStart=str.length();
            } else {
                try {
                    app.append(str);
                    str.setLength(0);
                    reorderStart=0;
                } catch(IOException e) {
                    throw new ICUUncheckedIOException(e);  // Avoid declaring "throws IOException".
                }
            }
            lastCC=0;
        }
        /**
         * Flushes from the intermediate StringBuilder to the Appendable,
         * if they are different objects.
         * Then appends the new text to the Appendable or StringBuilder.
         * Normally used after quick check loops find a non-empty sequence.
         */
        public ReorderingBuffer flushAndAppendZeroCC(CharSequence s, int start, int limit) {
            if(appIsStringBuilder) {
                str.append(s, start, limit);
                reorderStart=str.length();
            } else {
                try {
                    app.append(str).append(s, start, limit);
                    str.setLength(0);
                    reorderStart=0;
                } catch(IOException e) {
                    throw new ICUUncheckedIOException(e);  // Avoid declaring "throws IOException".
                }
            }
            lastCC=0;
            return this;
        }
        public void remove() {
            str.setLength(0);
            lastCC=0;
            reorderStart=0;
        }
        public void removeSuffix(int suffixLength) {
            int oldLength=str.length();
            str.delete(oldLength-suffixLength, oldLength);
            lastCC=0;
            reorderStart=str.length();
        }

        /*
         * TODO: Revisit whether it makes sense to track reorderStart.
         * It is set to after the last known character with cc<=1,
         * which stops previousCC() before it reads that character and looks up its cc.
         * previousCC() is normally only called from insert().
         * In other words, reorderStart speeds up the insertion of a combining mark
         * into a multi-combining mark sequence where it does not belong at the end.
         * This might not be worth the trouble.
         * On the other hand, it's not a huge amount of trouble.
         *
         * We probably need it for UNORM_SIMPLE_APPEND.
         */

        // Inserts c somewhere before the last character.
        // Requires 0<cc<lastCC which implies reorderStart<limit.
        private void insert(int c, int cc) {
            for(setIterator(), skipPrevious(); previousCC()>cc;) {}
            // insert c at codePointLimit, after the character with prevCC<=cc
            if(c<=0xffff) {
                str.insert(codePointLimit, (char)c);
                if(cc<=1) {
                    reorderStart=codePointLimit+1;
                }
            } else {
                str.insert(codePointLimit, Character.toChars(c));
                if(cc<=1) {
                    reorderStart=codePointLimit+2;
                }
            }
        }

        private final Normalizer2Impl impl;
        private final Appendable app;
        private final StringBuilder str;
        private final boolean appIsStringBuilder;
        private int reorderStart;
        private int lastCC;

        // private backward iterator
        private void setIterator() { codePointStart=str.length(); }
        private void skipPrevious() {  // Requires 0<codePointStart.
            codePointLimit=codePointStart;
            codePointStart=str.offsetByCodePoints(codePointStart, -1);
        }
        private int previousCC() {  // Returns 0 if there is no previous character.
            codePointLimit=codePointStart;
            if(reorderStart>=codePointStart) {
                return 0;
            }
            int c=str.codePointBefore(codePointStart);
            codePointStart-=Character.charCount(c);
            if(c<MIN_CCC_LCCC_CP) {
                return 0;
            }
            return getCCFromYesOrMaybe(impl.getNorm16(c));
        }

        private int codePointStart, codePointLimit;
    }

    // TODO: Propose as public API on the UTF16 class.
    // TODO: Propose widening UTF16 methods that take char to take int.
    // TODO: Propose widening UTF16 methods that take String to take CharSequence.
    public static final class UTF16Plus {
        /**
         * Assuming c is a surrogate code point (UTF16.isSurrogate(c)),
         * is it a lead surrogate?
         * @param c code unit or code point
         * @return true or false
         */
        public static boolean isSurrogateLead(int c) { return (c&0x400)==0; }
        /**
         * Compares two CharSequence objects for binary equality.
         * @param s1 first sequence
         * @param s2 second sequence
         * @return true if s1 contains the same text as s2
         */
        public static boolean equal(CharSequence s1,  CharSequence s2) {
            if(s1==s2) {
                return true;
            }
            int length=s1.length();
            if(length!=s2.length()) {
                return false;
            }
            for(int i=0; i<length; ++i) {
                if(s1.charAt(i)!=s2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        /**
         * Compares two CharSequence subsequences for binary equality.
         * @param s1 first sequence
         * @param start1 start offset in first sequence
         * @param limit1 limit offset in first sequence
         * @param s2 second sequence
         * @param start2 start offset in second sequence
         * @param limit2 limit offset in second sequence
         * @return true if s1.subSequence(start1, limit1) contains the same text
         *              as s2.subSequence(start2, limit2)
         */
        public static boolean equal(CharSequence s1, int start1, int limit1,
                                    CharSequence s2, int start2, int limit2) {
            if((limit1-start1)!=(limit2-start2)) {
                return false;
            }
            if(s1==s2 && start1==start2) {
                return true;
            }
            while(start1<limit1) {
                if(s1.charAt(start1++)!=s2.charAt(start2++)) {
                    return false;
                }
            }
            return true;
        }
    }

    public Normalizer2Impl() {}

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        // @Override when we switch to Java 6
        @Override
        public boolean isDataVersionAcceptable(byte version[]) {
            return version[0]==2;
        }
    }
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    private static final int DATA_FORMAT = 0x4e726d32;  // "Nrm2"

    public Normalizer2Impl load(ByteBuffer bytes) {
        try {
            dataVersion=ICUBinary.readHeaderAndDataVersion(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            int indexesLength=bytes.getInt()/4;  // inIndexes[IX_NORM_TRIE_OFFSET]/4
            if(indexesLength<=IX_MIN_MAYBE_YES) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough indexes");
            }
            int[] inIndexes=new int[indexesLength];
            inIndexes[0]=indexesLength*4;
            for(int i=1; i<indexesLength; ++i) {
                inIndexes[i]=bytes.getInt();
            }

            minDecompNoCP=inIndexes[IX_MIN_DECOMP_NO_CP];
            minCompNoMaybeCP=inIndexes[IX_MIN_COMP_NO_MAYBE_CP];

            minYesNo=inIndexes[IX_MIN_YES_NO];
            minYesNoMappingsOnly=inIndexes[IX_MIN_YES_NO_MAPPINGS_ONLY];
            minNoNo=inIndexes[IX_MIN_NO_NO];
            limitNoNo=inIndexes[IX_LIMIT_NO_NO];
            minMaybeYes=inIndexes[IX_MIN_MAYBE_YES];

            // Read the normTrie.
            int offset=inIndexes[IX_NORM_TRIE_OFFSET];
            int nextOffset=inIndexes[IX_EXTRA_DATA_OFFSET];
            normTrie=Trie2_16.createFromSerialized(bytes);
            int trieLength=normTrie.getSerializedLength();
            if(trieLength>(nextOffset-offset)) {
                throw new ICUUncheckedIOException("Normalizer2 data: not enough bytes for normTrie");
            }
            ICUBinary.skipBytes(bytes, (nextOffset-offset)-trieLength);  // skip padding after trie bytes

            // Read the composition and mapping data.
            offset=nextOffset;
            nextOffset=inIndexes[IX_SMALL_FCD_OFFSET];
            int numChars=(nextOffset-offset)/2;
            if(numChars!=0) {
                maybeYesCompositions=ICUBinary.getString(bytes, numChars, 0);
                extraData=maybeYesCompositions.substring(MIN_NORMAL_MAYBE_YES-minMaybeYes);
            }

            // smallFCD: new in formatVersion 2
            offset=nextOffset;
            smallFCD=new byte[0x100];
            bytes.get(smallFCD);

            // Build tccc180[].
            // gennorm2 enforces lccc=0 for c<MIN_CCC_LCCC_CP=U+0300.
            tccc180=new int[0x180];
            int bits=0;
            for(int c=0; c<0x180; bits>>=1) {
                if((c&0xff)==0) {
                    bits=smallFCD[c>>8];  // one byte per 0x100 code points
                }
                if((bits&1)!=0) {
                    for(int i=0; i<0x20; ++i, ++c) {
                        tccc180[c]=getFCD16FromNormData(c)&0xff;
                    }
                } else {
                    c+=0x20;
                }
            }

            return this;
        } catch(IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }
    public Normalizer2Impl load(String name) {
        return load(ICUBinary.getRequiredData(name));
    }

    private void enumLcccRange(int start, int end, int norm16, UnicodeSet set) {
        if(isAlgorithmicNoNo(norm16)) {
            // Range of code points with same-norm16-value algorithmic decompositions.
            // They might have different non-zero FCD16 values.
            do {
                int fcd16=getFCD16(start);
                if(fcd16>0xff) { set.add(start); }
            } while(++start<=end);
        } else {
            int fcd16=getFCD16(start);
            if(fcd16>0xff) { set.add(start, end); }
        }
    }

    private void enumNorm16PropertyStartsRange(int start, int end, int value, UnicodeSet set) {
        /* add the start code point to the USet */
        set.add(start);
        if(start!=end && isAlgorithmicNoNo(value)) {
            // Range of code points with same-norm16-value algorithmic decompositions.
            // They might have different non-zero FCD16 values.
            int prevFCD16=getFCD16(start);
            while(++start<=end) {
                int fcd16=getFCD16(start);
                if(fcd16!=prevFCD16) {
                    set.add(start);
                    prevFCD16=fcd16;
                }
            }
        }
    }

    public void addLcccChars(UnicodeSet set) {
        /* add the start code point of each same-value range of each trie */
        Iterator<Trie2.Range> trieIterator=normTrie.iterator();
        Trie2.Range range;
        while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
            enumLcccRange(range.startCodePoint, range.endCodePoint, range.value, set);
        }
    }

    public void addPropertyStarts(UnicodeSet set) {
        /* add the start code point of each same-value range of each trie */
        Iterator<Trie2.Range> trieIterator=normTrie.iterator();
        Trie2.Range range;
        while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
            enumNorm16PropertyStartsRange(range.startCodePoint, range.endCodePoint, range.value, set);
        }

        /* add Hangul LV syllables and LV+1 because of skippables */
        for(int c=Hangul.HANGUL_BASE; c<Hangul.HANGUL_LIMIT; c+=Hangul.JAMO_T_COUNT) {
            set.add(c);
            set.add(c+1);
        }
        set.add(Hangul.HANGUL_LIMIT); /* add Hangul+1 to continue with other properties */
    }

    public void addCanonIterPropertyStarts(UnicodeSet set) {
        /* add the start code point of each same-value range of the canonical iterator data trie */
        ensureCanonIterData();
        // currently only used for the SEGMENT_STARTER property
        Iterator<Trie2.Range> trieIterator=canonIterData.iterator(segmentStarterMapper);
        Trie2.Range range;
        while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
            /* add the start code point to the USet */
            set.add(range.startCodePoint);
        }
    }
    private static final Trie2.ValueMapper segmentStarterMapper=new Trie2.ValueMapper() {
        @Override
        public int map(int in) {
            return in&CANON_NOT_SEGMENT_STARTER;
        }
    };

    // low-level properties ------------------------------------------------ ***

    public Trie2_16 getNormTrie() { return normTrie; }

    // Note: Normalizer2Impl.java r30983 (2011-nov-27)
    // still had getFCDTrie() which built and cached an FCD trie.
    // That provided faster access to FCD data than getFCD16FromNormData()
    // but required synchronization and consumed some 10kB of heap memory
    // in any process that uses FCD (e.g., via collation).
    // tccc180[] and smallFCD[] are intended to help with any loss of performance,
    // at least for Latin & CJK.

    /**
     * Builds the canonical-iterator data for this instance.
     * This is required before any of {@link #isCanonSegmentStarter(int)} or
     * {@link #getCanonStartSet(int, UnicodeSet)} are called,
     * or else they crash.
     * @return this
     */
    public synchronized Normalizer2Impl ensureCanonIterData() {
        if(canonIterData==null) {
            Trie2Writable newData=new Trie2Writable(0, 0);
            canonStartSets=new ArrayList<UnicodeSet>();
            Iterator<Trie2.Range> trieIterator=normTrie.iterator();
            Trie2.Range range;
            while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
                final int norm16=range.value;
                if(norm16==0 || (minYesNo<=norm16 && norm16<minNoNo)) {
                    // Inert, or 2-way mapping (including Hangul syllable).
                    // We do not write a canonStartSet for any yesNo character.
                    // Composites from 2-way mappings are added at runtime from the
                    // starter's compositions list, and the other characters in
                    // 2-way mappings get CANON_NOT_SEGMENT_STARTER set because they are
                    // "maybe" characters.
                    continue;
                }
                for(int c=range.startCodePoint; c<=range.endCodePoint; ++c) {
                    final int oldValue=newData.get(c);
                    int newValue=oldValue;
                    if(norm16>=minMaybeYes) {
                        // not a segment starter if it occurs in a decomposition or has cc!=0
                        newValue|=CANON_NOT_SEGMENT_STARTER;
                        if(norm16<MIN_NORMAL_MAYBE_YES) {
                            newValue|=CANON_HAS_COMPOSITIONS;
                        }
                    } else if(norm16<minYesNo) {
                        newValue|=CANON_HAS_COMPOSITIONS;
                    } else {
                        // c has a one-way decomposition
                        int c2=c;
                        int norm16_2=norm16;
                        while(limitNoNo<=norm16_2 && norm16_2<minMaybeYes) {
                            c2=this.mapAlgorithmic(c2, norm16_2);
                            norm16_2=getNorm16(c2);
                        }
                        if(minYesNo<=norm16_2 && norm16_2<limitNoNo) {
                            // c decomposes, get everything from the variable-length extra data
                            int firstUnit=extraData.charAt(norm16_2);
                            int length=firstUnit&MAPPING_LENGTH_MASK;
                            if((firstUnit&MAPPING_HAS_CCC_LCCC_WORD)!=0) {
                                if(c==c2 && (extraData.charAt(norm16_2-1)&0xff)!=0) {
                                    newValue|=CANON_NOT_SEGMENT_STARTER;  // original c has cc!=0
                                }
                            }
                            // Skip empty mappings (no characters in the decomposition).
                            if(length!=0) {
                                ++norm16_2;  // skip over the firstUnit
                                // add c to first code point's start set
                                int limit=norm16_2+length;
                                c2=extraData.codePointAt(norm16_2);
                                addToStartSet(newData, c, c2);
                                // Set CANON_NOT_SEGMENT_STARTER for each remaining code point of a
                                // one-way mapping. A 2-way mapping is possible here after
                                // intermediate algorithmic mapping.
                                if(norm16_2>=minNoNo) {
                                    while((norm16_2+=Character.charCount(c2))<limit) {
                                        c2=extraData.codePointAt(norm16_2);
                                        int c2Value=newData.get(c2);
                                        if((c2Value&CANON_NOT_SEGMENT_STARTER)==0) {
                                            newData.set(c2, c2Value|CANON_NOT_SEGMENT_STARTER);
                                        }
                                    }
                                }
                            }
                        } else {
                            // c decomposed to c2 algorithmically; c has cc==0
                            addToStartSet(newData, c, c2);
                        }
                    }
                    if(newValue!=oldValue) {
                        newData.set(c, newValue);
                    }
                }
            }
            canonIterData=newData.toTrie2_32();
        }
        return this;
    }

    public int getNorm16(int c) { return normTrie.get(c); }

    public int getCompQuickCheck(int norm16) {
        if(norm16<minNoNo || MIN_YES_YES_WITH_CC<=norm16) {
            return 1;  // yes
        } else if(minMaybeYes<=norm16) {
            return 2;  // maybe
        } else {
            return 0;  // no
        }
    }
    public boolean isAlgorithmicNoNo(int norm16) { return limitNoNo<=norm16 && norm16<minMaybeYes; }
    public boolean isCompNo(int norm16) { return minNoNo<=norm16 && norm16<minMaybeYes; }
    public boolean isDecompYes(int norm16) { return norm16<minYesNo || minMaybeYes<=norm16; }

    public int getCC(int norm16) {
        if(norm16>=MIN_NORMAL_MAYBE_YES) {
            return norm16&0xff;
        }
        if(norm16<minNoNo || limitNoNo<=norm16) {
            return 0;
        }
        return getCCFromNoNo(norm16);
    }
    public static int getCCFromYesOrMaybe(int norm16) {
        return norm16>=MIN_NORMAL_MAYBE_YES ? norm16&0xff : 0;
    }

    /**
     * Returns the FCD data for code point c.
     * @param c A Unicode code point.
     * @return The lccc(c) in bits 15..8 and tccc(c) in bits 7..0.
     */
    public int getFCD16(int c) {
        if(c<0) {
            return 0;
        } else if(c<0x180) {
            return tccc180[c];
        } else if(c<=0xffff) {
            if(!singleLeadMightHaveNonZeroFCD16(c)) { return 0; }
        }
        return getFCD16FromNormData(c);
    }
    /** Returns the FCD data for U+0000<=c<U+0180. */
    public int getFCD16FromBelow180(int c) { return tccc180[c]; }
    /** Returns true if the single-or-lead code unit c might have non-zero FCD data. */
    public boolean singleLeadMightHaveNonZeroFCD16(int lead) {
        // 0<=lead<=0xffff
        byte bits=smallFCD[lead>>8];
        if(bits==0) { return false; }
        return ((bits>>((lead>>5)&7))&1)!=0;
    }

    /** Gets the FCD value from the regular normalization data. */
    public int getFCD16FromNormData(int c) {
        // Only loops for 1:1 algorithmic mappings.
        for(;;) {
            int norm16=getNorm16(c);
            if(norm16<=minYesNo) {
                // no decomposition or Hangul syllable, all zeros
                return 0;
            } else if(norm16>=MIN_NORMAL_MAYBE_YES) {
                // combining mark
                norm16&=0xff;
                return norm16|(norm16<<8);
            } else if(norm16>=minMaybeYes) {
                return 0;
            } else if(isDecompNoAlgorithmic(norm16)) {
                c=mapAlgorithmic(c, norm16);
            } else {
                // c decomposes, get everything from the variable-length extra data
                int firstUnit=extraData.charAt(norm16);
                if((firstUnit&MAPPING_LENGTH_MASK)==0) {
                    // A character that is deleted (maps to an empty string) must
                    // get the worst-case lccc and tccc values because arbitrary
                    // characters on both sides will become adjacent.
                    return 0x1ff;
                } else {
                    int fcd16=firstUnit>>8;  // tccc
                    if((firstUnit&MAPPING_HAS_CCC_LCCC_WORD)!=0) {
                        fcd16|=extraData.charAt(norm16-1)&0xff00;  // lccc
                    }
                    return fcd16;
                }
            }
        }
    }

    /**
     * Gets the decomposition for one code point.
     * @param c code point
     * @return c's decomposition, if it has one; returns null if it does not have a decomposition
     */
    public String getDecomposition(int c) {
        int decomp=-1;
        int norm16;
        for(;;) {
            if(c<minDecompNoCP || isDecompYes(norm16=getNorm16(c))) {
                // c does not decompose
            } else if(isHangul(norm16)) {
                // Hangul syllable: decompose algorithmically
                StringBuilder buffer=new StringBuilder();
                Hangul.decompose(c, buffer);
                return buffer.toString();
            } else if(isDecompNoAlgorithmic(norm16)) {
                decomp=c=mapAlgorithmic(c, norm16);
                continue;
            } else {
                // c decomposes, get everything from the variable-length extra data
                int length=extraData.charAt(norm16++)&MAPPING_LENGTH_MASK;
                return extraData.substring(norm16, norm16+length);
            }
            if(decomp<0) {
                return null;
            } else {
                return UTF16.valueOf(decomp);
            }
        }
    }

    /**
     * Gets the raw decomposition for one code point.
     * @param c code point
     * @return c's raw decomposition, if it has one; returns null if it does not have a decomposition
     */
    public String getRawDecomposition(int c) {
        // We do not loop in this method because an algorithmic mapping itself
        // becomes a final result rather than having to be decomposed recursively.
        int norm16;
        if(c<minDecompNoCP || isDecompYes(norm16=getNorm16(c))) {
            // c does not decompose
            return null;
        } else if(isHangul(norm16)) {
            // Hangul syllable: decompose algorithmically
            StringBuilder buffer=new StringBuilder();
            Hangul.getRawDecomposition(c, buffer);
            return buffer.toString();
        } else if(isDecompNoAlgorithmic(norm16)) {
            return UTF16.valueOf(mapAlgorithmic(c, norm16));
        } else {
            // c decomposes, get everything from the variable-length extra data
            int firstUnit=extraData.charAt(norm16);
            int mLength=firstUnit&MAPPING_LENGTH_MASK;  // length of normal mapping
            if((firstUnit&MAPPING_HAS_RAW_MAPPING)!=0) {
                // Read the raw mapping from before the firstUnit and before the optional ccc/lccc word.
                // Bit 7=MAPPING_HAS_CCC_LCCC_WORD
                int rawMapping=norm16-((firstUnit>>7)&1)-1;
                char rm0=extraData.charAt(rawMapping);
                if(rm0<=MAPPING_LENGTH_MASK) {
                    return extraData.substring(rawMapping-rm0, rawMapping);
                } else {
                    // Copy the normal mapping and replace its first two code units with rm0.
                    StringBuilder buffer=new StringBuilder(mLength-1).append(rm0);
                    norm16+=1+2;  // skip over the firstUnit and the first two mapping code units
                    return buffer.append(extraData, norm16, norm16+mLength-2).toString();
                }
            } else {
                norm16+=1;  // skip over the firstUnit
                return extraData.substring(norm16, norm16+mLength);
            }
        }
    }

    /**
     * Returns true if code point c starts a canonical-iterator string segment.
     * <b>{@link #ensureCanonIterData()} must have been called before this method,
     * or else this method will crash.</b>
     * @param c A Unicode code point.
     * @return true if c starts a canonical-iterator string segment.
     */
    public boolean isCanonSegmentStarter(int c) {
        return canonIterData.get(c)>=0;
    }
    /**
     * Returns true if there are characters whose decomposition starts with c.
     * If so, then the set is cleared and then filled with those characters.
     * <b>{@link #ensureCanonIterData()} must have been called before this method,
     * or else this method will crash.</b>
     * @param c A Unicode code point.
     * @param set A UnicodeSet to receive the characters whose decompositions
     *        start with c, if there are any.
     * @return true if there are characters whose decomposition starts with c.
     */
    public boolean getCanonStartSet(int c, UnicodeSet set) {
        int canonValue=canonIterData.get(c)&~CANON_NOT_SEGMENT_STARTER;
        if(canonValue==0) {
            return false;
        }
        set.clear();
        int value=canonValue&CANON_VALUE_MASK;
        if((canonValue&CANON_HAS_SET)!=0) {
            set.addAll(canonStartSets.get(value));
        } else if(value!=0) {
            set.add(value);
        }
        if((canonValue&CANON_HAS_COMPOSITIONS)!=0) {
            int norm16=getNorm16(c);
            if(norm16==JAMO_L) {
                int syllable=Hangul.HANGUL_BASE+(c-Hangul.JAMO_L_BASE)*Hangul.JAMO_VT_COUNT;
                set.add(syllable, syllable+Hangul.JAMO_VT_COUNT-1);
            } else {
                addComposites(getCompositionsList(norm16), set);
            }
        }
        return true;
    }

    public static final int MIN_CCC_LCCC_CP=0x300;

    public static final int MIN_YES_YES_WITH_CC=0xff01;
    public static final int JAMO_VT=0xff00;
    public static final int MIN_NORMAL_MAYBE_YES=0xfe00;
    public static final int JAMO_L=1;
    public static final int MAX_DELTA=0x40;

    // Byte offsets from the start of the data, after the generic header.
    public static final int IX_NORM_TRIE_OFFSET=0;
    public static final int IX_EXTRA_DATA_OFFSET=1;
    public static final int IX_SMALL_FCD_OFFSET=2;
    public static final int IX_RESERVED3_OFFSET=3;
    public static final int IX_TOTAL_SIZE=7;

    // Code point thresholds for quick check codes.
    public static final int IX_MIN_DECOMP_NO_CP=8;
    public static final int IX_MIN_COMP_NO_MAYBE_CP=9;

    // Norm16 value thresholds for quick check combinations and types of extra data.
    // Mappings & compositions in [minYesNo..minYesNoMappingsOnly[.
    public static final int IX_MIN_YES_NO=10;
    public static final int IX_MIN_NO_NO=11;
    public static final int IX_LIMIT_NO_NO=12;
    public static final int IX_MIN_MAYBE_YES=13;

    // Mappings only in [minYesNoMappingsOnly..minNoNo[.
    public static final int IX_MIN_YES_NO_MAPPINGS_ONLY=14;

    public static final int IX_COUNT=16;

    public static final int MAPPING_HAS_CCC_LCCC_WORD=0x80;
    public static final int MAPPING_HAS_RAW_MAPPING=0x40;
    public static final int MAPPING_NO_COMP_BOUNDARY_AFTER=0x20;
    public static final int MAPPING_LENGTH_MASK=0x1f;

    public static final int COMP_1_LAST_TUPLE=0x8000;
    public static final int COMP_1_TRIPLE=1;
    public static final int COMP_1_TRAIL_LIMIT=0x3400;
    public static final int COMP_1_TRAIL_MASK=0x7ffe;
    public static final int COMP_1_TRAIL_SHIFT=9;  // 10-1 for the "triple" bit
    public static final int COMP_2_TRAIL_SHIFT=6;
    public static final int COMP_2_TRAIL_MASK=0xffc0;

    // higher-level functionality ------------------------------------------ ***

    // NFD without an NFD Normalizer2 instance.
    public Appendable decompose(CharSequence s, StringBuilder dest) {
        decompose(s, 0, s.length(), dest, s.length());
        return dest;
    }
    /**
     * Decomposes s[src, limit[ and writes the result to dest.
     * limit can be NULL if src is NUL-terminated.
     * destLengthEstimate is the initial dest buffer capacity and can be -1.
     */
    public void decompose(CharSequence s, int src, int limit, StringBuilder dest,
                   int destLengthEstimate) {
        if(destLengthEstimate<0) {
            destLengthEstimate=limit-src;
        }
        dest.setLength(0);
        ReorderingBuffer buffer=new ReorderingBuffer(this, dest, destLengthEstimate);
        decompose(s, src, limit, buffer);
    }

    // Dual functionality:
    // buffer!=NULL: normalize
    // buffer==NULL: isNormalized/quickCheck/spanQuickCheckYes
    public int decompose(CharSequence s, int src, int limit,
                         ReorderingBuffer buffer) {
        int minNoCP=minDecompNoCP;

        int prevSrc;
        int c=0;
        int norm16=0;

        // only for quick check
        int prevBoundary=src;
        int prevCC=0;

        for(;;) {
            // count code units below the minimum or with irrelevant data for the quick check
            for(prevSrc=src; src!=limit;) {
                if( (c=s.charAt(src))<minNoCP ||
                    isMostDecompYesAndZeroCC(norm16=normTrie.getFromU16SingleLead((char)c))
                ) {
                    ++src;
                } else if(!UTF16.isSurrogate((char)c)) {
                    break;
                } else {
                    char c2;
                    if(UTF16Plus.isSurrogateLead(c)) {
                        if((src+1)!=limit && Character.isLowSurrogate(c2=s.charAt(src+1))) {
                            c=Character.toCodePoint((char)c, c2);
                        }
                    } else /* trail surrogate */ {
                        if(prevSrc<src && Character.isHighSurrogate(c2=s.charAt(src-1))) {
                            --src;
                            c=Character.toCodePoint(c2, (char)c);
                        }
                    }
                    if(isMostDecompYesAndZeroCC(norm16=getNorm16(c))) {
                        src+=Character.charCount(c);
                    } else {
                        break;
                    }
                }
            }
            // copy these code units all at once
            if(src!=prevSrc) {
                if(buffer!=null) {
                    buffer.flushAndAppendZeroCC(s, prevSrc, src);
                } else {
                    prevCC=0;
                    prevBoundary=src;
                }
            }
            if(src==limit) {
                break;
            }

            // Check one above-minimum, relevant code point.
            src+=Character.charCount(c);
            if(buffer!=null) {
                decompose(c, norm16, buffer);
            } else {
                if(isDecompYes(norm16)) {
                    int cc=getCCFromYesOrMaybe(norm16);
                    if(prevCC<=cc || cc==0) {
                        prevCC=cc;
                        if(cc<=1) {
                            prevBoundary=src;
                        }
                        continue;
                    }
                }
                return prevBoundary;  // "no" or cc out of order
            }
        }
        return src;
    }
    public void decomposeAndAppend(CharSequence s, boolean doDecompose, ReorderingBuffer buffer) {
        int limit=s.length();
        if(limit==0) {
            return;
        }
        if(doDecompose) {
            decompose(s, 0, limit, buffer);
            return;
        }
        // Just merge the strings at the boundary.
        int c=Character.codePointAt(s, 0);
        int src=0;
        int firstCC, prevCC, cc;
        firstCC=prevCC=cc=getCC(getNorm16(c));
        while(cc!=0) {
            prevCC=cc;
            src+=Character.charCount(c);
            if(src>=limit) {
                break;
            }
            c=Character.codePointAt(s, src);
            cc=getCC(getNorm16(c));
        };
        buffer.append(s, 0, src, firstCC, prevCC);
        buffer.append(s, src, limit);
    }
    // Very similar to composeQuickCheck(): Make the same changes in both places if relevant.
    // doCompose: normalize
    // !doCompose: isNormalized (buffer must be empty and initialized)
    public boolean compose(CharSequence s, int src, int limit,
                           boolean onlyContiguous,
                           boolean doCompose,
                           ReorderingBuffer buffer) {
        int minNoMaybeCP=minCompNoMaybeCP;

        /*
         * prevBoundary points to the last character before the current one
         * that has a composition boundary before it with ccc==0 and quick check "yes".
         * Keeping track of prevBoundary saves us looking for a composition boundary
         * when we find a "no" or "maybe".
         *
         * When we back out from prevSrc back to prevBoundary,
         * then we also remove those same characters (which had been simply copied
         * or canonically-order-inserted) from the ReorderingBuffer.
         * Therefore, at all times, the [prevBoundary..prevSrc[ source units
         * must correspond 1:1 to destination units at the end of the destination buffer.
         */
        int prevBoundary=src;
        int prevSrc;
        int c=0;
        int norm16=0;

        // only for isNormalized
        int prevCC=0;

        for(;;) {
            // count code units below the minimum or with irrelevant data for the quick check
            for(prevSrc=src; src!=limit;) {
                if( (c=s.charAt(src))<minNoMaybeCP ||
                    isCompYesAndZeroCC(norm16=normTrie.getFromU16SingleLead((char)c))
                ) {
                    ++src;
                } else if(!UTF16.isSurrogate((char)c)) {
                    break;
                } else {
                    char c2;
                    if(UTF16Plus.isSurrogateLead(c)) {
                        if((src+1)!=limit && Character.isLowSurrogate(c2=s.charAt(src+1))) {
                            c=Character.toCodePoint((char)c, c2);
                        }
                    } else /* trail surrogate */ {
                        if(prevSrc<src && Character.isHighSurrogate(c2=s.charAt(src-1))) {
                            --src;
                            c=Character.toCodePoint(c2, (char)c);
                        }
                    }
                    if(isCompYesAndZeroCC(norm16=getNorm16(c))) {
                        src+=Character.charCount(c);
                    } else {
                        break;
                    }
                }
            }
            // copy these code units all at once
            if(src!=prevSrc) {
                if(src==limit) {
                    if(doCompose) {
                        buffer.flushAndAppendZeroCC(s, prevSrc, src);
                    }
                    break;
                }
                // Set prevBoundary to the last character in the quick check loop.
                prevBoundary=src-1;
                if( Character.isLowSurrogate(s.charAt(prevBoundary)) && prevSrc<prevBoundary &&
                    Character.isHighSurrogate(s.charAt(prevBoundary-1))
                ) {
                    --prevBoundary;
                }
                if(doCompose) {
                    // The last "quick check yes" character is excluded from the
                    // flush-and-append call in case it needs to be modified.
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                } else {
                    prevCC=0;
                }
                // The start of the current character (c).
                prevSrc=src;
            } else if(src==limit) {
                break;
            }

            src+=Character.charCount(c);
            /*
             * isCompYesAndZeroCC(norm16) is false, that is, norm16>=minNoNo.
             * c is either a "noNo" (has a mapping) or a "maybeYes" (combines backward)
             * or has ccc!=0.
             * Check for Jamo V/T, then for regular characters.
             * c is not a Hangul syllable or Jamo L because those have "yes" properties.
             */
            if(isJamoVT(norm16) && prevBoundary!=prevSrc) {
                char prev=s.charAt(prevSrc-1);
                boolean needToDecompose=false;
                if(c<Hangul.JAMO_T_BASE) {
                    // c is a Jamo Vowel, compose with previous Jamo L and following Jamo T.
                    prev-=Hangul.JAMO_L_BASE;
                    if(prev<Hangul.JAMO_L_COUNT) {
                        if(!doCompose) {
                            return false;
                        }
                        char syllable=(char)
                            (Hangul.HANGUL_BASE+
                             (prev*Hangul.JAMO_V_COUNT+(c-Hangul.JAMO_V_BASE))*
                             Hangul.JAMO_T_COUNT);
                        char t;
                        if(src!=limit && (t=(char)(s.charAt(src)-Hangul.JAMO_T_BASE))<Hangul.JAMO_T_COUNT) {
                            ++src;
                            syllable+=t;  // The next character was a Jamo T.
                            prevBoundary=src;
                            buffer.setLastChar(syllable);
                            continue;
                        }
                        // If we see L+V+x where x!=T then we drop to the slow path,
                        // decompose and recompose.
                        // This is to deal with NFKC finding normal L and V but a
                        // compatibility variant of a T. We need to either fully compose that
                        // combination here (which would complicate the code and may not work
                        // with strange custom data) or use the slow path -- or else our replacing
                        // two input characters (L+V) with one output character (LV syllable)
                        // would violate the invariant that [prevBoundary..prevSrc[ has the same
                        // length as what we appended to the buffer since prevBoundary.
                        needToDecompose=true;
                    }
                } else if(Hangul.isHangulWithoutJamoT(prev)) {
                    // c is a Jamo Trailing consonant,
                    // compose with previous Hangul LV that does not contain a Jamo T.
                    if(!doCompose) {
                        return false;
                    }
                    buffer.setLastChar((char)(prev+c-Hangul.JAMO_T_BASE));
                    prevBoundary=src;
                    continue;
                }
                if(!needToDecompose) {
                    // The Jamo V/T did not compose into a Hangul syllable.
                    if(doCompose) {
                        buffer.append((char)c);
                    } else {
                        prevCC=0;
                    }
                    continue;
                }
            }
            /*
             * Source buffer pointers:
             *
             *  all done      quick check   current char  not yet
             *                "yes" but     (c)           processed
             *                may combine
             *                forward
             * [-------------[-------------[-------------[-------------[
             * |             |             |             |             |
             * orig. src     prevBoundary  prevSrc       src           limit
             *
             *
             * Destination buffer pointers inside the ReorderingBuffer:
             *
             *  all done      might take    not filled yet
             *                characters for
             *                reordering
             * [-------------[-------------[-------------[
             * |             |             |             |
             * start         reorderStart  limit         |
             *                             +remainingCap.+
             */
            if(norm16>=MIN_YES_YES_WITH_CC) {
                int cc=norm16&0xff;  // cc!=0
                if( onlyContiguous &&  // FCC
                    (doCompose ? buffer.getLastCC() : prevCC)==0 &&
                    prevBoundary<prevSrc &&
                    // buffer.getLastCC()==0 && prevBoundary<prevSrc tell us that
                    // [prevBoundary..prevSrc[ (which is exactly one character under these conditions)
                    // passed the quick check "yes && ccc==0" test.
                    // Check whether the last character was a "yesYes" or a "yesNo".
                    // If a "yesNo", then we get its trailing ccc from its
                    // mapping and check for canonical order.
                    // All other cases are ok.
                    getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc)>cc
                ) {
                    // Fails FCD test, need to decompose and contiguously recompose.
                    if(!doCompose) {
                        return false;
                    }
                } else if(doCompose) {
                    buffer.append(c, cc);
                    continue;
                } else if(prevCC<=cc) {
                    prevCC=cc;
                    continue;
                } else {
                    return false;
                }
            } else if(!doCompose && !isMaybeOrNonZeroCC(norm16)) {
                return false;
            }

            /*
             * Find appropriate boundaries around this character,
             * decompose the source text from between the boundaries,
             * and recompose it.
             *
             * We may need to remove the last few characters from the ReorderingBuffer
             * to account for source text that was copied or appended
             * but needs to take part in the recomposition.
             */

            /*
             * Find the last composition boundary in [prevBoundary..src[.
             * It is either the decomposition of the current character (at prevSrc),
             * or prevBoundary.
             */
            if(hasCompBoundaryBefore(c, norm16)) {
                prevBoundary=prevSrc;
            } else if(doCompose) {
                buffer.removeSuffix(prevSrc-prevBoundary);
            }

            // Find the next composition boundary in [src..limit[ -
            // modifies src to point to the next starter.
            src=findNextCompBoundary(s, src, limit);

            // Decompose [prevBoundary..src[ into the buffer and then recompose that part of it.
            int recomposeStartIndex=buffer.length();
            decomposeShort(s, prevBoundary, src, buffer);
            recompose(buffer, recomposeStartIndex, onlyContiguous);
            if(!doCompose) {
                if(!buffer.equals(s, prevBoundary, src)) {
                    return false;
                }
                buffer.remove();
                prevCC=0;
            }

            // Move to the next starter. We never need to look back before this point again.
            prevBoundary=src;
        }
        return true;
    }
    /**
     * Very similar to compose(): Make the same changes in both places if relevant.
     * doSpan: spanQuickCheckYes (ignore bit 0 of the return value)
     * !doSpan: quickCheck
     * @return bits 31..1: spanQuickCheckYes (==s.length() if "yes") and
     *         bit 0: set if "maybe"; otherwise, if the span length&lt;s.length()
     *         then the quick check result is "no"
     */
    public int composeQuickCheck(CharSequence s, int src, int limit,
                                 boolean onlyContiguous, boolean doSpan) {
        int qcResult=0;
        int minNoMaybeCP=minCompNoMaybeCP;

        /*
         * prevBoundary points to the last character before the current one
         * that has a composition boundary before it with ccc==0 and quick check "yes".
         */
        int prevBoundary=src;
        int prevSrc;
        int c=0;
        int norm16=0;
        int prevCC=0;

        for(;;) {
            // count code units below the minimum or with irrelevant data for the quick check
            for(prevSrc=src;;) {
                if(src==limit) {
                    return (src<<1)|qcResult;  // "yes" or "maybe"
                }
                if( (c=s.charAt(src))<minNoMaybeCP ||
                    isCompYesAndZeroCC(norm16=normTrie.getFromU16SingleLead((char)c))
                ) {
                    ++src;
                } else if(!UTF16.isSurrogate((char)c)) {
                    break;
                } else {
                    char c2;
                    if(UTF16Plus.isSurrogateLead(c)) {
                        if((src+1)!=limit && Character.isLowSurrogate(c2=s.charAt(src+1))) {
                            c=Character.toCodePoint((char)c, c2);
                        }
                    } else /* trail surrogate */ {
                        if(prevSrc<src && Character.isHighSurrogate(c2=s.charAt(src-1))) {
                            --src;
                            c=Character.toCodePoint(c2, (char)c);
                        }
                    }
                    if(isCompYesAndZeroCC(norm16=getNorm16(c))) {
                        src+=Character.charCount(c);
                    } else {
                        break;
                    }
                }
            }
            if(src!=prevSrc) {
                // Set prevBoundary to the last character in the quick check loop.
                prevBoundary=src-1;
                if( Character.isLowSurrogate(s.charAt(prevBoundary)) && prevSrc<prevBoundary &&
                        Character.isHighSurrogate(s.charAt(prevBoundary-1))
                ) {
                    --prevBoundary;
                }
                prevCC=0;
                // The start of the current character (c).
                prevSrc=src;
            }

            src+=Character.charCount(c);
            /*
             * isCompYesAndZeroCC(norm16) is false, that is, norm16>=minNoNo.
             * c is either a "noNo" (has a mapping) or a "maybeYes" (combines backward)
             * or has ccc!=0.
             */
            if(isMaybeOrNonZeroCC(norm16)) {
                int cc=getCCFromYesOrMaybe(norm16);
                if( onlyContiguous &&  // FCC
                    cc!=0 &&
                    prevCC==0 &&
                    prevBoundary<prevSrc &&
                    // prevCC==0 && prevBoundary<prevSrc tell us that
                    // [prevBoundary..prevSrc[ (which is exactly one character under these conditions)
                    // passed the quick check "yes && ccc==0" test.
                    // Check whether the last character was a "yesYes" or a "yesNo".
                    // If a "yesNo", then we get its trailing ccc from its
                    // mapping and check for canonical order.
                    // All other cases are ok.
                    getTrailCCFromCompYesAndZeroCC(s, prevBoundary, prevSrc)>cc
                ) {
                    // Fails FCD test.
                } else if(prevCC<=cc || cc==0) {
                    prevCC=cc;
                    if(norm16<MIN_YES_YES_WITH_CC) {
                        if(!doSpan) {
                            qcResult=1;
                        } else {
                            return prevBoundary<<1;  // spanYes does not care to know it's "maybe"
                        }
                    }
                    continue;
                }
            }
            return prevBoundary<<1;  // "no"
        }
    }
    public void composeAndAppend(CharSequence s,
                                 boolean doCompose,
                                 boolean onlyContiguous,
                                 ReorderingBuffer buffer) {
        int src=0, limit=s.length();
        if(!buffer.isEmpty()) {
            int firstStarterInSrc=findNextCompBoundary(s, 0, limit);
            if(0!=firstStarterInSrc) {
                int lastStarterInDest=findPreviousCompBoundary(buffer.getStringBuilder(),
                                                               buffer.length());
                StringBuilder middle=new StringBuilder((buffer.length()-lastStarterInDest)+
                                                       firstStarterInSrc+16);
                middle.append(buffer.getStringBuilder(), lastStarterInDest, buffer.length());
                buffer.removeSuffix(buffer.length()-lastStarterInDest);
                middle.append(s, 0, firstStarterInSrc);
                compose(middle, 0, middle.length(), onlyContiguous, true, buffer);
                src=firstStarterInSrc;
            }
        }
        if(doCompose) {
            compose(s, src, limit, onlyContiguous, true, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }
    // Dual functionality:
    // buffer!=NULL: normalize
    // buffer==NULL: isNormalized/quickCheck/spanQuickCheckYes
    public int makeFCD(CharSequence s, int src, int limit, ReorderingBuffer buffer) {
        // Note: In this function we use buffer->appendZeroCC() because we track
        // the lead and trail combining classes here, rather than leaving it to
        // the ReorderingBuffer.
        // The exception is the call to decomposeShort() which uses the buffer
        // in the normal way.

        // Tracks the last FCD-safe boundary, before lccc=0 or after properly-ordered tccc<=1.
        // Similar to the prevBoundary in the compose() implementation.
        int prevBoundary=src;
        int prevSrc;
        int c=0;
        int prevFCD16=0;
        int fcd16=0;

        for(;;) {
            // count code units with lccc==0
            for(prevSrc=src; src!=limit;) {
                if((c=s.charAt(src))<MIN_CCC_LCCC_CP) {
                    prevFCD16=~c;
                    ++src;
                } else if(!singleLeadMightHaveNonZeroFCD16(c)) {
                    prevFCD16=0;
                    ++src;
                } else {
                    if(UTF16.isSurrogate((char)c)) {
                        char c2;
                        if(UTF16Plus.isSurrogateLead(c)) {
                            if((src+1)!=limit && Character.isLowSurrogate(c2=s.charAt(src+1))) {
                                c=Character.toCodePoint((char)c, c2);
                            }
                        } else /* trail surrogate */ {
                            if(prevSrc<src && Character.isHighSurrogate(c2=s.charAt(src-1))) {
                                --src;
                                c=Character.toCodePoint(c2, (char)c);
                            }
                        }
                    }
                    if((fcd16=getFCD16FromNormData(c))<=0xff) {
                        prevFCD16=fcd16;
                        src+=Character.charCount(c);
                    } else {
                        break;
                    }
                }
            }
            // copy these code units all at once
            if(src!=prevSrc) {
                if(src==limit) {
                    if(buffer!=null) {
                        buffer.flushAndAppendZeroCC(s, prevSrc, src);
                    }
                    break;
                }
                prevBoundary=src;
                // We know that the previous character's lccc==0.
                if(prevFCD16<0) {
                    // Fetching the fcd16 value was deferred for this below-U+0300 code point.
                    int prev=~prevFCD16;
                    prevFCD16= prev<0x180 ? tccc180[prev] : getFCD16FromNormData(prev);
                    if(prevFCD16>1) {
                        --prevBoundary;
                    }
                } else {
                    int p=src-1;
                    if( Character.isLowSurrogate(s.charAt(p)) && prevSrc<p &&
                        Character.isHighSurrogate(s.charAt(p-1))
                    ) {
                        --p;
                        // Need to fetch the previous character's FCD value because
                        // prevFCD16 was just for the trail surrogate code point.
                        prevFCD16=getFCD16FromNormData(Character.toCodePoint(s.charAt(p), s.charAt(p+1)));
                        // Still known to have lccc==0 because its lead surrogate unit had lccc==0.
                    }
                    if(prevFCD16>1) {
                        prevBoundary=p;
                    }
                }
                if(buffer!=null) {
                    // The last lccc==0 character is excluded from the
                    // flush-and-append call in case it needs to be modified.
                    buffer.flushAndAppendZeroCC(s, prevSrc, prevBoundary);
                    buffer.append(s, prevBoundary, src);
                }
                // The start of the current character (c).
                prevSrc=src;
            } else if(src==limit) {
                break;
            }

            src+=Character.charCount(c);
            // The current character (c) at [prevSrc..src[ has a non-zero lead combining class.
            // Check for proper order, and decompose locally if necessary.
            if((prevFCD16&0xff)<=(fcd16>>8)) {
                // proper order: prev tccc <= current lccc
                if((fcd16&0xff)<=1) {
                    prevBoundary=src;
                }
                if(buffer!=null) {
                    buffer.appendZeroCC(c);
                }
                prevFCD16=fcd16;
                continue;
            } else if(buffer==null) {
                return prevBoundary;  // quick check "no"
            } else {
                /*
                 * Back out the part of the source that we copied or appended
                 * already but is now going to be decomposed.
                 * prevSrc is set to after what was copied/appended.
                 */
                buffer.removeSuffix(prevSrc-prevBoundary);
                /*
                 * Find the part of the source that needs to be decomposed,
                 * up to the next safe boundary.
                 */
                src=findNextFCDBoundary(s, src, limit);
                /*
                 * The source text does not fulfill the conditions for FCD.
                 * Decompose and reorder a limited piece of the text.
                 */
                decomposeShort(s, prevBoundary, src, buffer);
                prevBoundary=src;
                prevFCD16=0;
            }
        }
        return src;
    }
    public void makeFCDAndAppend(CharSequence s, boolean doMakeFCD, ReorderingBuffer buffer) {
        int src=0, limit=s.length();
        if(!buffer.isEmpty()) {
            int firstBoundaryInSrc=findNextFCDBoundary(s, 0, limit);
            if(0!=firstBoundaryInSrc) {
                int lastBoundaryInDest=findPreviousFCDBoundary(buffer.getStringBuilder(),
                                                               buffer.length());
                StringBuilder middle=new StringBuilder((buffer.length()-lastBoundaryInDest)+
                                                       firstBoundaryInSrc+16);
                middle.append(buffer.getStringBuilder(), lastBoundaryInDest, buffer.length());
                buffer.removeSuffix(buffer.length()-lastBoundaryInDest);
                middle.append(s, 0, firstBoundaryInSrc);
                makeFCD(middle, 0, middle.length(), buffer);
                src=firstBoundaryInSrc;
            }
        }
        if(doMakeFCD) {
            makeFCD(s, src, limit, buffer);
        } else {
            buffer.append(s, src, limit);
        }
    }

    // Note: hasDecompBoundary() could be implemented as aliases to
    // hasFCDBoundaryBefore() and hasFCDBoundaryAfter()
    // at the cost of building the FCD trie for a decomposition normalizer.
    public boolean hasDecompBoundary(int c, boolean before) {
        for(;;) {
            if(c<minDecompNoCP) {
                return true;
            }
            int norm16=getNorm16(c);
            if(isHangul(norm16) || isDecompYesAndZeroCC(norm16)) {
                return true;
            } else if(norm16>MIN_NORMAL_MAYBE_YES) {
                return false;  // ccc!=0
            } else if(isDecompNoAlgorithmic(norm16)) {
                c=mapAlgorithmic(c, norm16);
            } else {
                // c decomposes, get everything from the variable-length extra data
                int firstUnit=extraData.charAt(norm16);
                if((firstUnit&MAPPING_LENGTH_MASK)==0) {
                    return false;
                }
                if(!before) {
                    // decomp after-boundary: same as hasFCDBoundaryAfter(),
                    // fcd16<=1 || trailCC==0
                    if(firstUnit>0x1ff) {
                        return false;  // trailCC>1
                    }
                    if(firstUnit<=0xff) {
                        return true;  // trailCC==0
                    }
                    // if(trailCC==1) test leadCC==0, same as checking for before-boundary
                }
                // true if leadCC==0 (hasFCDBoundaryBefore())
                return (firstUnit&MAPPING_HAS_CCC_LCCC_WORD)==0 || (extraData.charAt(norm16-1)&0xff00)==0;
            }
        }
    }
    public boolean isDecompInert(int c) { return isDecompYesAndZeroCC(getNorm16(c)); }

    public boolean hasCompBoundaryBefore(int c) {
        return c<minCompNoMaybeCP || hasCompBoundaryBefore(c, getNorm16(c));
    }
    public boolean hasCompBoundaryAfter(int c, boolean onlyContiguous, boolean testInert) {
        for(;;) {
            int norm16=getNorm16(c);
            if(isInert(norm16)) {
                return true;
            } else if(norm16<=minYesNo) {
                // Hangul: norm16==minYesNo
                // Hangul LVT has a boundary after it.
                // Hangul LV and non-inert yesYes characters combine forward.
                return isHangul(norm16) && !Hangul.isHangulWithoutJamoT((char)c);
            } else if(norm16>= (testInert ? minNoNo : minMaybeYes)) {
                return false;
            } else if(isDecompNoAlgorithmic(norm16)) {
                c=mapAlgorithmic(c, norm16);
            } else {
                // c decomposes, get everything from the variable-length extra data.
                // If testInert, then c must be a yesNo character which has lccc=0,
                // otherwise it could be a noNo.
                int firstUnit=extraData.charAt(norm16);
                // true if
                //   not MAPPING_NO_COMP_BOUNDARY_AFTER
                //     (which is set if
                //       c is not deleted, and
                //       it and its decomposition do not combine forward, and it has a starter)
                //   and if FCC then trailCC<=1
                return
                    (firstUnit&MAPPING_NO_COMP_BOUNDARY_AFTER)==0 &&
                    (!onlyContiguous || firstUnit<=0x1ff);
            }
        }
    }

    public boolean hasFCDBoundaryBefore(int c) { return c<MIN_CCC_LCCC_CP || getFCD16(c)<=0xff; }
    public boolean hasFCDBoundaryAfter(int c) {
        int fcd16=getFCD16(c);
        return fcd16<=1 || (fcd16&0xff)==0;
    }
    public boolean isFCDInert(int c) { return getFCD16(c)<=1; }

    private boolean isMaybe(int norm16) { return minMaybeYes<=norm16 && norm16<=JAMO_VT; }
    private boolean isMaybeOrNonZeroCC(int norm16) { return norm16>=minMaybeYes; }
    private static boolean isInert(int norm16) { return norm16==0; }
    private static boolean isJamoL(int norm16) { return norm16==1; }
    private static boolean isJamoVT(int norm16) { return norm16==JAMO_VT; }
    private boolean isHangul(int norm16) { return norm16==minYesNo; }
    private boolean isCompYesAndZeroCC(int norm16) { return norm16<minNoNo; }
    // UBool isCompYes(uint16_t norm16) const {
    //     return norm16>=MIN_YES_YES_WITH_CC || norm16<minNoNo;
    // }
    // UBool isCompYesOrMaybe(uint16_t norm16) const {
    //     return norm16<minNoNo || minMaybeYes<=norm16;
    // }
    // private boolean hasZeroCCFromDecompYes(int norm16) {
    //     return norm16<=MIN_NORMAL_MAYBE_YES || norm16==JAMO_VT;
    // }
    private boolean isDecompYesAndZeroCC(int norm16) {
        return norm16<minYesNo ||
               norm16==JAMO_VT ||
               (minMaybeYes<=norm16 && norm16<=MIN_NORMAL_MAYBE_YES);
    }
    /**
     * A little faster and simpler than isDecompYesAndZeroCC() but does not include
     * the MaybeYes which combine-forward and have ccc=0.
     * (Standard Unicode 5.2 normalization does not have such characters.)
     */
    private boolean isMostDecompYesAndZeroCC(int norm16) {
        return norm16<minYesNo || norm16==MIN_NORMAL_MAYBE_YES || norm16==JAMO_VT;
    }
    private boolean isDecompNoAlgorithmic(int norm16) { return norm16>=limitNoNo; }

    // For use with isCompYes().
    // Perhaps the compiler can combine the two tests for MIN_YES_YES_WITH_CC.
    // static uint8_t getCCFromYes(uint16_t norm16) {
    //     return norm16>=MIN_YES_YES_WITH_CC ? (uint8_t)norm16 : 0;
    // }
    private int getCCFromNoNo(int norm16) {
        if((extraData.charAt(norm16)&MAPPING_HAS_CCC_LCCC_WORD)!=0) {
            return extraData.charAt(norm16-1)&0xff;
        } else {
            return 0;
        }
    }
    // requires that the [cpStart..cpLimit[ character passes isCompYesAndZeroCC()
    int getTrailCCFromCompYesAndZeroCC(CharSequence s, int cpStart, int cpLimit) {
        int c;
        if(cpStart==(cpLimit-1)) {
            c=s.charAt(cpStart);
        } else {
            c=Character.codePointAt(s, cpStart);
        }
        int prevNorm16=getNorm16(c);
        if(prevNorm16<=minYesNo) {
            return 0;  // yesYes and Hangul LV/LVT have ccc=tccc=0
        } else {
            return extraData.charAt(prevNorm16)>>8;  // tccc from yesNo
        }
    }

    // Requires algorithmic-NoNo.
    private int mapAlgorithmic(int c, int norm16) {
        return c+norm16-(minMaybeYes-MAX_DELTA-1);
    }

    // Requires minYesNo<norm16<limitNoNo.
    // private int getMapping(int norm16) { return /*extraData+*/norm16; }

    /**
     * @return index into maybeYesCompositions, or -1
     */
    private int getCompositionsListForDecompYes(int norm16) {
        if(norm16==0 || MIN_NORMAL_MAYBE_YES<=norm16) {
            return -1;
        } else {
            if((norm16-=minMaybeYes)<0) {
                // norm16<minMaybeYes: index into extraData which is a substring at
                //     maybeYesCompositions[MIN_NORMAL_MAYBE_YES-minMaybeYes]
                // same as (MIN_NORMAL_MAYBE_YES-minMaybeYes)+norm16
                norm16+=MIN_NORMAL_MAYBE_YES;  // for yesYes; if Jamo L: harmless empty list
            }
            return norm16;
        }
    }
    /**
     * @return index into maybeYesCompositions
     */
    private int getCompositionsListForComposite(int norm16) {
        // composite has both mapping & compositions list
        int firstUnit=extraData.charAt(norm16);
        return (MIN_NORMAL_MAYBE_YES-minMaybeYes)+norm16+  // mapping in maybeYesCompositions
            1+  // +1 to skip the first unit with the mapping lenth
            (firstUnit&MAPPING_LENGTH_MASK);  // + mapping length
    }
    /**
     * @param c code point must have compositions
     * @return index into maybeYesCompositions
     */
    private int getCompositionsList(int norm16) {
        return isDecompYes(norm16) ?
                getCompositionsListForDecompYes(norm16) :
                getCompositionsListForComposite(norm16);
    }

    // Decompose a short piece of text which is likely to contain characters that
    // fail the quick check loop and/or where the quick check loop's overhead
    // is unlikely to be amortized.
    // Called by the compose() and makeFCD() implementations.
    // Public in Java for collation implementation code.
    public void decomposeShort(CharSequence s, int src, int limit,
                               ReorderingBuffer buffer) {
        while(src<limit) {
            int c=Character.codePointAt(s, src);
            src+=Character.charCount(c);
            decompose(c, getNorm16(c), buffer);
        }
    }
    private void decompose(int c, int norm16,
                           ReorderingBuffer buffer) {
        // Only loops for 1:1 algorithmic mappings.
        for(;;) {
            // get the decomposition and the lead and trail cc's
            if(isDecompYes(norm16)) {
                // c does not decompose
                buffer.append(c, getCCFromYesOrMaybe(norm16));
            } else if(isHangul(norm16)) {
                // Hangul syllable: decompose algorithmically
                Hangul.decompose(c, buffer);
            } else if(isDecompNoAlgorithmic(norm16)) {
                c=mapAlgorithmic(c, norm16);
                norm16=getNorm16(c);
                continue;
            } else {
                // c decomposes, get everything from the variable-length extra data
                int firstUnit=extraData.charAt(norm16);
                int length=firstUnit&MAPPING_LENGTH_MASK;
                int leadCC, trailCC;
                trailCC=firstUnit>>8;
                if((firstUnit&MAPPING_HAS_CCC_LCCC_WORD)!=0) {
                    leadCC=extraData.charAt(norm16-1)>>8;
                } else {
                    leadCC=0;
                }
                ++norm16;  // skip over the firstUnit
                buffer.append(extraData, norm16, norm16+length, leadCC, trailCC);
            }
            return;
        }
    }

    /**
     * Finds the recomposition result for
     * a forward-combining "lead" character,
     * specified with a pointer to its compositions list,
     * and a backward-combining "trail" character.
     *
     * <p>If the lead and trail characters combine, then this function returns
     * the following "compositeAndFwd" value:
     * <pre>
     * Bits 21..1  composite character
     * Bit      0  set if the composite is a forward-combining starter
     * </pre>
     * otherwise it returns -1.
     *
     * <p>The compositions list has (trail, compositeAndFwd) pair entries,
     * encoded as either pairs or triples of 16-bit units.
     * The last entry has the high bit of its first unit set.
     *
     * <p>The list is sorted by ascending trail characters (there are no duplicates).
     * A linear search is used.
     *
     * <p>See normalizer2impl.h for a more detailed description
     * of the compositions list format.
     */
    private static int combine(String compositions, int list, int trail) {
        int key1, firstUnit;
        if(trail<COMP_1_TRAIL_LIMIT) {
            // trail character is 0..33FF
            // result entry may have 2 or 3 units
            key1=(trail<<1);
            while(key1>(firstUnit=compositions.charAt(list))) {
                list+=2+(firstUnit&COMP_1_TRIPLE);
            }
            if(key1==(firstUnit&COMP_1_TRAIL_MASK)) {
                if((firstUnit&COMP_1_TRIPLE)!=0) {
                    return (compositions.charAt(list+1)<<16)|compositions.charAt(list+2);
                } else {
                    return compositions.charAt(list+1);
                }
            }
        } else {
            // trail character is 3400..10FFFF
            // result entry has 3 units
            key1=COMP_1_TRAIL_LIMIT+(((trail>>COMP_1_TRAIL_SHIFT))&~COMP_1_TRIPLE);
            int key2=(trail<<COMP_2_TRAIL_SHIFT)&0xffff;
            int secondUnit;
            for(;;) {
                if(key1>(firstUnit=compositions.charAt(list))) {
                    list+=2+(firstUnit&COMP_1_TRIPLE);
                } else if(key1==(firstUnit&COMP_1_TRAIL_MASK)) {
                    if(key2>(secondUnit=compositions.charAt(list+1))) {
                        if((firstUnit&COMP_1_LAST_TUPLE)!=0) {
                            break;
                        } else {
                            list+=3;
                        }
                    } else if(key2==(secondUnit&COMP_2_TRAIL_MASK)) {
                        return ((secondUnit&~COMP_2_TRAIL_MASK)<<16)|compositions.charAt(list+2);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return -1;
    }
    /**
     * @param list some character's compositions list
     * @param set recursively receives the composites from these compositions
     */
    private void addComposites(int list, UnicodeSet set) {
        int firstUnit, compositeAndFwd;
        do {
            firstUnit=maybeYesCompositions.charAt(list);
            if((firstUnit&COMP_1_TRIPLE)==0) {
                compositeAndFwd=maybeYesCompositions.charAt(list+1);
                list+=2;
            } else {
                compositeAndFwd=((maybeYesCompositions.charAt(list+1)&~COMP_2_TRAIL_MASK)<<16)|
                                maybeYesCompositions.charAt(list+2);
                list+=3;
            }
            int composite=compositeAndFwd>>1;
            if((compositeAndFwd&1)!=0) {
                addComposites(getCompositionsListForComposite(getNorm16(composite)), set);
            }
            set.add(composite);
        } while((firstUnit&COMP_1_LAST_TUPLE)==0);
    }
    /*
     * Recomposes the buffer text starting at recomposeStartIndex
     * (which is in NFD - decomposed and canonically ordered),
     * and truncates the buffer contents.
     *
     * Note that recomposition never lengthens the text:
     * Any character consists of either one or two code units;
     * a composition may contain at most one more code unit than the original starter,
     * while the combining mark that is removed has at least one code unit.
     */
    private void recompose(ReorderingBuffer buffer, int recomposeStartIndex,
                           boolean onlyContiguous) {
        StringBuilder sb=buffer.getStringBuilder();
        int p=recomposeStartIndex;
        if(p==sb.length()) {
            return;
        }

        int starter, pRemove;
        int compositionsList;
        int c, compositeAndFwd;
        int norm16;
        int cc, prevCC;
        boolean starterIsSupplementary;

        // Some of the following variables are not used until we have a forward-combining starter
        // and are only initialized now to avoid compiler warnings.
        compositionsList=-1;  // used as indicator for whether we have a forward-combining starter
        starter=-1;
        starterIsSupplementary=false;
        prevCC=0;

        for(;;) {
            c=sb.codePointAt(p);
            p+=Character.charCount(c);
            norm16=getNorm16(c);
            cc=getCCFromYesOrMaybe(norm16);
            if( // this character combines backward and
                isMaybe(norm16) &&
                // we have seen a starter that combines forward and
                compositionsList>=0 &&
                // the backward-combining character is not blocked
                (prevCC<cc || prevCC==0)
            ) {
                if(isJamoVT(norm16)) {
                    // c is a Jamo V/T, see if we can compose it with the previous character.
                    if(c<Hangul.JAMO_T_BASE) {
                        // c is a Jamo Vowel, compose with previous Jamo L and following Jamo T.
                        char prev=(char)(sb.charAt(starter)-Hangul.JAMO_L_BASE);
                        if(prev<Hangul.JAMO_L_COUNT) {
                            pRemove=p-1;
                            char syllable=(char)
                                (Hangul.HANGUL_BASE+
                                 (prev*Hangul.JAMO_V_COUNT+(c-Hangul.JAMO_V_BASE))*
                                 Hangul.JAMO_T_COUNT);
                            char t;
                            if(p!=sb.length() && (t=(char)(sb.charAt(p)-Hangul.JAMO_T_BASE))<Hangul.JAMO_T_COUNT) {
                                ++p;
                                syllable+=t;  // The next character was a Jamo T.
                            }
                            sb.setCharAt(starter, syllable);
                            // remove the Jamo V/T
                            sb.delete(pRemove, p);
                            p=pRemove;
                        }
                    }
                    /*
                     * No "else" for Jamo T:
                     * Since the input is in NFD, there are no Hangul LV syllables that
                     * a Jamo T could combine with.
                     * All Jamo Ts are combined above when handling Jamo Vs.
                     */
                    if(p==sb.length()) {
                        break;
                    }
                    compositionsList=-1;
                    continue;
                } else if((compositeAndFwd=combine(maybeYesCompositions, compositionsList, c))>=0) {
                    // The starter and the combining mark (c) do combine.
                    int composite=compositeAndFwd>>1;

                    // Remove the combining mark.
                    pRemove=p-Character.charCount(c);  // pRemove & p: start & limit of the combining mark
                    sb.delete(pRemove, p);
                    p=pRemove;
                    // Replace the starter with the composite.
                    if(starterIsSupplementary) {
                        if(composite>0xffff) {
                            // both are supplementary
                            sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                            sb.setCharAt(starter+1, UTF16.getTrailSurrogate(composite));
                        } else {
                            sb.setCharAt(starter, (char)c);
                            sb.deleteCharAt(starter+1);
                            // The composite is shorter than the starter,
                            // move the intermediate characters forward one.
                            starterIsSupplementary=false;
                            --p;
                        }
                    } else if(composite>0xffff) {
                        // The composite is longer than the starter,
                        // move the intermediate characters back one.
                        starterIsSupplementary=true;
                        sb.setCharAt(starter, UTF16.getLeadSurrogate(composite));
                        sb.insert(starter+1, UTF16.getTrailSurrogate(composite));
                        ++p;
                    } else {
                        // both are on the BMP
                        sb.setCharAt(starter, (char)composite);
                    }

                    // Keep prevCC because we removed the combining mark.

                    if(p==sb.length()) {
                        break;
                    }
                    // Is the composite a starter that combines forward?
                    if((compositeAndFwd&1)!=0) {
                        compositionsList=
                            getCompositionsListForComposite(getNorm16(composite));
                    } else {
                        compositionsList=-1;
                    }

                    // We combined; continue with looking for compositions.
                    continue;
                }
            }

            // no combination this time
            prevCC=cc;
            if(p==sb.length()) {
                break;
            }

            // If c did not combine, then check if it is a starter.
            if(cc==0) {
                // Found a new starter.
                if((compositionsList=getCompositionsListForDecompYes(norm16))>=0) {
                    // It may combine with something, prepare for it.
                    if(c<=0xffff) {
                        starterIsSupplementary=false;
                        starter=p-1;
                    } else {
                        starterIsSupplementary=true;
                        starter=p-2;
                    }
                }
            } else if(onlyContiguous) {
                // FCC: no discontiguous compositions; any intervening character blocks.
                compositionsList=-1;
            }
        }
        buffer.flush();
    }

    public int composePair(int a, int b) {
        int norm16=getNorm16(a);  // maps an out-of-range 'a' to inert norm16=0
        int list;
        if(isInert(norm16)) {
            return -1;
        } else if(norm16<minYesNoMappingsOnly) {
            if(isJamoL(norm16)) {
                b-=Hangul.JAMO_V_BASE;
                if(0<=b && b<Hangul.JAMO_V_COUNT) {
                    return
                        (Hangul.HANGUL_BASE+
                         ((a-Hangul.JAMO_L_BASE)*Hangul.JAMO_V_COUNT+b)*
                         Hangul.JAMO_T_COUNT);
                } else {
                    return -1;
                }
            } else if(isHangul(norm16)) {
                b-=Hangul.JAMO_T_BASE;
                if(Hangul.isHangulWithoutJamoT((char)a) && 0<b && b<Hangul.JAMO_T_COUNT) {  // not b==0!
                    return a+b;
                } else {
                    return -1;
                }
            } else {
                // 'a' has a compositions list in extraData
                list=norm16;
                if(norm16>minYesNo) {  // composite 'a' has both mapping & compositions list
                    list+=  // mapping pointer
                        1+  // +1 to skip the first unit with the mapping lenth
                        (extraData.charAt(list)&MAPPING_LENGTH_MASK);  // + mapping length
                }
                // Turn the offset-into-extraData into an offset-into-maybeYesCompositions.
                list+=MIN_NORMAL_MAYBE_YES-minMaybeYes;
            }
        } else if(norm16<minMaybeYes || MIN_NORMAL_MAYBE_YES<=norm16) {
            return -1;
        } else {
            list=norm16-minMaybeYes;  // offset into maybeYesCompositions
        }
        if(b<0 || 0x10ffff<b) {  // combine(list, b) requires a valid code point b
            return -1;
        }
        return combine(maybeYesCompositions, list, b)>>1;
    }

    /**
     * Does c have a composition boundary before it?
     * True if its decomposition begins with a character that has
     * ccc=0 && NFC_QC=Yes (isCompYesAndZeroCC()).
     * As a shortcut, this is true if c itself has ccc=0 && NFC_QC=Yes
     * (isCompYesAndZeroCC()) so we need not decompose.
     */
    private boolean hasCompBoundaryBefore(int c, int norm16) {
        for(;;) {
            if(isCompYesAndZeroCC(norm16)) {
                return true;
            } else if(isMaybeOrNonZeroCC(norm16)) {
                return false;
            } else if(isDecompNoAlgorithmic(norm16)) {
                c=mapAlgorithmic(c, norm16);
                norm16=getNorm16(c);
            } else {
                // c decomposes, get everything from the variable-length extra data
                int firstUnit=extraData.charAt(norm16);
                if((firstUnit&MAPPING_LENGTH_MASK)==0) {
                    return false;
                }
                if((firstUnit&MAPPING_HAS_CCC_LCCC_WORD)!=0 && (extraData.charAt(norm16-1)&0xff00)!=0) {
                    return false;  // non-zero leadCC
                }
                return isCompYesAndZeroCC(getNorm16(Character.codePointAt(extraData, norm16+1)));
            }
        }
    }
    private int findPreviousCompBoundary(CharSequence s, int p) {
        while(p>0) {
            int c=Character.codePointBefore(s, p);
            p-=Character.charCount(c);
            if(hasCompBoundaryBefore(c)) {
                break;
            }
            // We could also test hasCompBoundaryAfter() and return iter.codePointLimit,
            // but that's probably not worth the extra cost.
        }
        return p;
    }
    private int findNextCompBoundary(CharSequence s, int p, int limit) {
        while(p<limit) {
            int c=Character.codePointAt(s, p);
            int norm16=normTrie.get(c);
            if(hasCompBoundaryBefore(c, norm16)) {
                break;
            }
            p+=Character.charCount(c);
        }
        return p;
    }

    private int findPreviousFCDBoundary(CharSequence s, int p) {
        while(p>0) {
            int c=Character.codePointBefore(s, p);
            p-=Character.charCount(c);
            if(c<MIN_CCC_LCCC_CP || getFCD16(c)<=0xff) {
                break;
            }
        }
        return p;
    }
    private int findNextFCDBoundary(CharSequence s, int p, int limit) {
        while(p<limit) {
            int c=Character.codePointAt(s, p);
            if(c<MIN_CCC_LCCC_CP || getFCD16(c)<=0xff) {
                break;
            }
            p+=Character.charCount(c);
        }
        return p;
    }

    private void addToStartSet(Trie2Writable newData, int origin, int decompLead) {
        int canonValue=newData.get(decompLead);
        if((canonValue&(CANON_HAS_SET|CANON_VALUE_MASK))==0 && origin!=0) {
            // origin is the first character whose decomposition starts with
            // the character for which we are setting the value.
            newData.set(decompLead, canonValue|origin);
        } else {
            // origin is not the first character, or it is U+0000.
            UnicodeSet set;
            if((canonValue&CANON_HAS_SET)==0) {
                int firstOrigin=canonValue&CANON_VALUE_MASK;
                canonValue=(canonValue&~CANON_VALUE_MASK)|CANON_HAS_SET|canonStartSets.size();
                newData.set(decompLead, canonValue);
                canonStartSets.add(set=new UnicodeSet());
                if(firstOrigin!=0) {
                    set.add(firstOrigin);
                }
            } else {
                set=canonStartSets.get(canonValue&CANON_VALUE_MASK);
            }
            set.add(origin);
        }
    }

    @SuppressWarnings("unused")
    private VersionInfo dataVersion;

    // Code point thresholds for quick check codes.
    private int minDecompNoCP;
    private int minCompNoMaybeCP;

    // Norm16 value thresholds for quick check combinations and types of extra data.
    private int minYesNo;
    private int minYesNoMappingsOnly;
    private int minNoNo;
    private int limitNoNo;
    private int minMaybeYes;

    private Trie2_16 normTrie;
    private String maybeYesCompositions;
    private String extraData;  // mappings and/or compositions for yesYes, yesNo & noNo characters
    private byte[] smallFCD;  // [0x100] one bit per 32 BMP code points, set if any FCD!=0
    private int[] tccc180;  // [0x180] tccc values for U+0000..U+017F

    private Trie2_32 canonIterData;
    private ArrayList<UnicodeSet> canonStartSets;

    // bits in canonIterData
    private static final int CANON_NOT_SEGMENT_STARTER = 0x80000000;
    private static final int CANON_HAS_COMPOSITIONS = 0x40000000;
    private static final int CANON_HAS_SET = 0x200000;
    private static final int CANON_VALUE_MASK = 0x1fffff;
}
