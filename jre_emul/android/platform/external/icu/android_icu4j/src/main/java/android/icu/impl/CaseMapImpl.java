/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.impl;

import java.io.IOException;

import android.icu.lang.UCharacter;
import android.icu.text.BreakIterator;
import android.icu.text.Edits;
import android.icu.util.ICUUncheckedIOException;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CaseMapImpl {
    /**
     * Implementation of UCaseProps.ContextIterator, iterates over a String.
     * See ustrcase.c/utf16_caseContextIterator().
     */
    public static final class StringContextIterator implements UCaseProps.ContextIterator {
        /**
         * Constructor.
         * @param src String to iterate over.
         */
        public StringContextIterator(CharSequence src) {
            this.s=src;
            limit=src.length();
            cpStart=cpLimit=index=0;
            dir=0;
        }

        /**
         * Set the iteration limit for nextCaseMapCP() to an index within the string.
         * If the limit parameter is negative or past the string, then the
         * string length is restored as the iteration limit.
         *
         * <p>This limit does not affect the next() function which always
         * iterates to the very end of the string.
         *
         * @param lim The iteration limit.
         */
        public void setLimit(int lim) {
            if(0<=lim && lim<=s.length()) {
                limit=lim;
            } else {
                limit=s.length();
            }
        }

        /**
         * Move to the iteration limit without fetching code points up to there.
         */
        public void moveToLimit() {
            cpStart=cpLimit=limit;
        }

        /**
         * Iterate forward through the string to fetch the next code point
         * to be case-mapped, and set the context indexes for it.
         *
         * <p>When the iteration limit is reached (and -1 is returned),
         * getCPStart() will be at the iteration limit.
         *
         * <p>Iteration with next() does not affect the position for nextCaseMapCP().
         *
         * @return The next code point to be case-mapped, or <0 when the iteration is done.
         */
        public int nextCaseMapCP() {
            cpStart=cpLimit;
            if(cpLimit<limit) {
                int c=Character.codePointAt(s, cpLimit);
                cpLimit+=Character.charCount(c);
                return c;
            } else {
                return -1;
            }
        }

        /**
         * Returns the start of the code point that was last returned
         * by nextCaseMapCP().
         */
        public int getCPStart() {
            return cpStart;
        }

        /**
         * Returns the limit of the code point that was last returned
         * by nextCaseMapCP().
         */
        public int getCPLimit() {
            return cpLimit;
        }

        public int getCPLength() {
            return cpLimit-cpStart;
        }

        // implement UCaseProps.ContextIterator
        // The following code is not used anywhere in this private class
        @Override
        public void reset(int direction) {
            if(direction>0) {
                /* reset for forward iteration */
                dir=1;
                index=cpLimit;
            } else if(direction<0) {
                /* reset for backward iteration */
                dir=-1;
                index=cpStart;
            } else {
                // not a valid direction
                dir=0;
                index=0;
            }
        }

        @Override
        public int next() {
            int c;

            if(dir>0 && index<s.length()) {
                c=Character.codePointAt(s, index);
                index+=Character.charCount(c);
                return c;
            } else if(dir<0 && index>0) {
                c=Character.codePointBefore(s, index);
                index-=Character.charCount(c);
                return c;
            }
            return -1;
        }

        // variables
        protected CharSequence s;
        protected int index, limit, cpStart, cpLimit;
        protected int dir; // 0=initial state  >0=forward  <0=backward
    }

    /**
     * Omit unchanged text when case-mapping with Edits.
     */
    public static final int OMIT_UNCHANGED_TEXT = 0x4000;

    private static int appendCodePoint(Appendable a, int c) throws IOException {
        if (c <= Character.MAX_VALUE) {
            a.append((char)c);
            return 1;
        } else {
            a.append((char)(0xd7c0 + (c >> 10)));
            a.append((char)(Character.MIN_LOW_SURROGATE + (c & 0x3ff)));
            return 2;
        }
    }

    /**
     * Appends a full case mapping result, see {@link UCaseProps#MAX_STRING_LENGTH}.
     * @throws IOException
     */
    private static void appendResult(int result, Appendable dest,
            int cpLength, int options, Edits edits) throws IOException {
        // Decode the result.
        if (result < 0) {
            // (not) original code point
            if (edits != null) {
                edits.addUnchanged(cpLength);
                if ((options & OMIT_UNCHANGED_TEXT) != 0) {
                    return;
                }
            }
            appendCodePoint(dest, ~result);
        } else if (result <= UCaseProps.MAX_STRING_LENGTH) {
            // The mapping has already been appended to result.
            if (edits != null) {
                edits.addReplace(cpLength, result);
            }
        } else {
            // Append the single-code point mapping.
            int length = appendCodePoint(dest, result);
            if (edits != null) {
                edits.addReplace(cpLength, length);
            }
        }
    }

    private static final void appendUnchanged(CharSequence src, int start, int length,
            Appendable dest, int options, Edits edits) throws IOException {
        if (length > 0) {
            if (edits != null) {
                edits.addUnchanged(length);
                if ((options & OMIT_UNCHANGED_TEXT) != 0) {
                    return;
                }
            }
            dest.append(src, start, start + length);
        }
    }

    private static void internalToLower(int caseLocale, int options, StringContextIterator iter,
            Appendable dest, Edits edits) throws IOException {
        int c;
        while ((c = iter.nextCaseMapCP()) >= 0) {
            c = UCaseProps.INSTANCE.toFullLower(c, iter, dest, caseLocale);
            appendResult(c, dest, iter.getCPLength(), options, edits);
        }
    }

    public static <A extends Appendable> A toLower(int caseLocale, int options,
            CharSequence src, A dest, Edits edits) {
        try {
            if (edits != null) {
                edits.reset();
            }
            StringContextIterator iter = new StringContextIterator(src);
            internalToLower(caseLocale, options, iter, dest, edits);
            return dest;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public static <A extends Appendable> A toUpper(int caseLocale, int options,
            CharSequence src, A dest, Edits edits) {
        try {
            if (edits != null) {
                edits.reset();
            }
            if (caseLocale == UCaseProps.LOC_GREEK) {
                return GreekUpper.toUpper(options, src, dest, edits);
            }
            StringContextIterator iter = new StringContextIterator(src);
            int c;
            while ((c = iter.nextCaseMapCP()) >= 0) {
                c = UCaseProps.INSTANCE.toFullUpper(c, iter, dest, caseLocale);
                appendResult(c, dest, iter.getCPLength(), options, edits);
            }
            return dest;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public static <A extends Appendable> A toTitle(
            int caseLocale, int options, BreakIterator titleIter,
            CharSequence src, A dest, Edits edits) {
        try {
            if (edits != null) {
                edits.reset();
            }

            /* set up local variables */
            StringContextIterator iter = new StringContextIterator(src);
            int srcLength = src.length();
            int prev=0;
            boolean isFirstIndex=true;

            /* titlecasing loop */
            while(prev<srcLength) {
                /* find next index where to titlecase */
                int index;
                if(isFirstIndex) {
                    isFirstIndex=false;
                    index=titleIter.first();
                } else {
                    index=titleIter.next();
                }
                if(index==BreakIterator.DONE || index>srcLength) {
                    index=srcLength;
                }

                /*
                 * Unicode 4 & 5 section 3.13 Default Case Operations:
                 *
                 * R3  toTitlecase(X): Find the word boundaries based on Unicode Standard Annex
                 * #29, "Text Boundaries." Between each pair of word boundaries, find the first
                 * cased character F. If F exists, map F to default_title(F); then map each
                 * subsequent character C to default_lower(C).
                 *
                 * In this implementation, segment [prev..index[ into 3 parts:
                 * a) uncased characters (copy as-is) [prev..titleStart[
                 * b) first case letter (titlecase)         [titleStart..titleLimit[
                 * c) subsequent characters (lowercase)                 [titleLimit..index[
                 */
                if(prev<index) {
                    // find and copy uncased characters [prev..titleStart[
                    int titleStart=prev;
                    iter.setLimit(index);
                    int c=iter.nextCaseMapCP();
                    if((options&UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT)==0
                            && UCaseProps.NONE==UCaseProps.INSTANCE.getType(c)) {
                        // Adjust the titlecasing index (titleStart) to the next cased character.
                        while((c=iter.nextCaseMapCP())>=0
                                && UCaseProps.NONE==UCaseProps.INSTANCE.getType(c)) {}
                        // If c<0 then we have only uncased characters in [prev..index[
                        // and stopped with titleStart==titleLimit==index.
                        titleStart=iter.getCPStart();
                        appendUnchanged(src, prev, titleStart-prev, dest, options, edits);
                    }

                    if(titleStart<index) {
                        int titleLimit=iter.getCPLimit();
                        // titlecase c which is from [titleStart..titleLimit[
                        c = UCaseProps.INSTANCE.toFullTitle(c, iter, dest, caseLocale);
                        appendResult(c, dest, iter.getCPLength(), options, edits);

                        // Special case Dutch IJ titlecasing
                        if (titleStart+1 < index && caseLocale == UCaseProps.LOC_DUTCH) {
                            char c1 = src.charAt(titleStart);
                            if ((c1 == 'i' || c1 == 'I')) {
                                char c2 = src.charAt(titleStart+1);
                                if (c2 == 'j') {
                                    dest.append('J');
                                    if (edits != null) {
                                        edits.addReplace(1, 1);
                                    }
                                    c = iter.nextCaseMapCP();
                                    titleLimit++;
                                    assert c == c2;
                                    assert titleLimit == iter.getCPLimit();
                                } else if (c2 == 'J') {
                                    // Keep the capital J from getting lowercased.
                                    appendUnchanged(src, titleStart + 1, 1, dest, options, edits);
                                    c = iter.nextCaseMapCP();
                                    titleLimit++;
                                    assert c == c2;
                                    assert titleLimit == iter.getCPLimit();
                                }
                            }
                        }

                        // lowercase [titleLimit..index[
                        if(titleLimit<index) {
                            if((options&UCharacter.TITLECASE_NO_LOWERCASE)==0) {
                                // Normal operation: Lowercase the rest of the word.
                                internalToLower(caseLocale, options, iter, dest, edits);
                            } else {
                                // Optionally just copy the rest of the word unchanged.
                                appendUnchanged(src, titleLimit, index-titleLimit, dest, options, edits);
                                iter.moveToLimit();
                            }
                        }
                    }
                }

                prev=index;
            }
            return dest;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public static <A extends Appendable> A fold(int options,
            CharSequence src, A dest, Edits edits) {
        try {
            if (edits != null) {
                edits.reset();
            }
            int length = src.length();
            for (int i = 0; i < length;) {
                int c = Character.codePointAt(src, i);
                int cpLength = Character.charCount(c);
                i += cpLength;
                c = UCaseProps.INSTANCE.toFullFolding(c, dest, options);
                appendResult(c, dest, cpLength, options, edits);
            }
            return dest;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static final class GreekUpper {
        // Data bits.
        private static final int UPPER_MASK = 0x3ff;
        private static final int HAS_VOWEL = 0x1000;
        private static final int HAS_YPOGEGRAMMENI = 0x2000;
        private static final int HAS_ACCENT = 0x4000;
        private static final int HAS_DIALYTIKA = 0x8000;
        // Further bits during data building and processing, not stored in the data map.
        private static final int HAS_COMBINING_DIALYTIKA = 0x10000;
        private static final int HAS_OTHER_GREEK_DIACRITIC = 0x20000;

        private static final int HAS_VOWEL_AND_ACCENT = HAS_VOWEL | HAS_ACCENT;
        private static final int HAS_VOWEL_AND_ACCENT_AND_DIALYTIKA =
                HAS_VOWEL_AND_ACCENT | HAS_DIALYTIKA;
        private static final int HAS_EITHER_DIALYTIKA = HAS_DIALYTIKA | HAS_COMBINING_DIALYTIKA;

        // State bits.
        private static final int AFTER_CASED = 1;
        private static final int AFTER_VOWEL_WITH_ACCENT = 2;

        // Data generated by prototype code, see
        // http://site.icu-project.org/design/case/greek-upper
        // TODO: Move this data into ucase.icu.
        private static final char[] data0370 = {
            // U+0370..03FF
            0x0370,  // Ͱ
            0x0370,  // ͱ
            0x0372,  // Ͳ
            0x0372,  // ͳ
            0,
            0,
            0x0376,  // Ͷ
            0x0376,  // ͷ
            0,
            0,
            0x037A,  // ͺ
            0x03FD,  // ͻ
            0x03FE,  // ͼ
            0x03FF,  // ͽ
            0,
            0x037F,  // Ϳ
            0,
            0,
            0,
            0,
            0,
            0,
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ά
            0,
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Έ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ή
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ί
            0,
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ό
            0,
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ύ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ώ
            0x0399 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ΐ
            0x0391 | HAS_VOWEL,  // Α
            0x0392,  // Β
            0x0393,  // Γ
            0x0394,  // Δ
            0x0395 | HAS_VOWEL,  // Ε
            0x0396,  // Ζ
            0x0397 | HAS_VOWEL,  // Η
            0x0398,  // Θ
            0x0399 | HAS_VOWEL,  // Ι
            0x039A,  // Κ
            0x039B,  // Λ
            0x039C,  // Μ
            0x039D,  // Ν
            0x039E,  // Ξ
            0x039F | HAS_VOWEL,  // Ο
            0x03A0,  // Π
            0x03A1,  // Ρ
            0,
            0x03A3,  // Σ
            0x03A4,  // Τ
            0x03A5 | HAS_VOWEL,  // Υ
            0x03A6,  // Φ
            0x03A7,  // Χ
            0x03A8,  // Ψ
            0x03A9 | HAS_VOWEL,  // Ω
            0x0399 | HAS_VOWEL | HAS_DIALYTIKA,  // Ϊ
            0x03A5 | HAS_VOWEL | HAS_DIALYTIKA,  // Ϋ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ά
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // έ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ή
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ί
            0x03A5 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ΰ
            0x0391 | HAS_VOWEL,  // α
            0x0392,  // β
            0x0393,  // γ
            0x0394,  // δ
            0x0395 | HAS_VOWEL,  // ε
            0x0396,  // ζ
            0x0397 | HAS_VOWEL,  // η
            0x0398,  // θ
            0x0399 | HAS_VOWEL,  // ι
            0x039A,  // κ
            0x039B,  // λ
            0x039C,  // μ
            0x039D,  // ν
            0x039E,  // ξ
            0x039F | HAS_VOWEL,  // ο
            0x03A0,  // π
            0x03A1,  // ρ
            0x03A3,  // ς
            0x03A3,  // σ
            0x03A4,  // τ
            0x03A5 | HAS_VOWEL,  // υ
            0x03A6,  // φ
            0x03A7,  // χ
            0x03A8,  // ψ
            0x03A9 | HAS_VOWEL,  // ω
            0x0399 | HAS_VOWEL | HAS_DIALYTIKA,  // ϊ
            0x03A5 | HAS_VOWEL | HAS_DIALYTIKA,  // ϋ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ό
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ύ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ώ
            0x03CF,  // Ϗ
            0x0392,  // ϐ
            0x0398,  // ϑ
            0x03D2,  // ϒ
            0x03D2 | HAS_ACCENT,  // ϓ
            0x03D2 | HAS_DIALYTIKA,  // ϔ
            0x03A6,  // ϕ
            0x03A0,  // ϖ
            0x03CF,  // ϗ
            0x03D8,  // Ϙ
            0x03D8,  // ϙ
            0x03DA,  // Ϛ
            0x03DA,  // ϛ
            0x03DC,  // Ϝ
            0x03DC,  // ϝ
            0x03DE,  // Ϟ
            0x03DE,  // ϟ
            0x03E0,  // Ϡ
            0x03E0,  // ϡ
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0x039A,  // ϰ
            0x03A1,  // ϱ
            0x03F9,  // ϲ
            0x037F,  // ϳ
            0x03F4,  // ϴ
            0x0395 | HAS_VOWEL,  // ϵ
            0,
            0x03F7,  // Ϸ
            0x03F7,  // ϸ
            0x03F9,  // Ϲ
            0x03FA,  // Ϻ
            0x03FA,  // ϻ
            0x03FC,  // ϼ
            0x03FD,  // Ͻ
            0x03FE,  // Ͼ
            0x03FF,  // Ͽ
        };

        private static final char[] data1F00 = {
            // U+1F00..1FFF
            0x0391 | HAS_VOWEL,  // ἀ
            0x0391 | HAS_VOWEL,  // ἁ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἂ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἃ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἄ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἅ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἆ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ἇ
            0x0391 | HAS_VOWEL,  // Ἀ
            0x0391 | HAS_VOWEL,  // Ἁ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἂ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἃ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἄ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἅ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἆ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ἇ
            0x0395 | HAS_VOWEL,  // ἐ
            0x0395 | HAS_VOWEL,  // ἑ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // ἒ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // ἓ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // ἔ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // ἕ
            0,
            0,
            0x0395 | HAS_VOWEL,  // Ἐ
            0x0395 | HAS_VOWEL,  // Ἑ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Ἒ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Ἓ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Ἔ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Ἕ
            0,
            0,
            0x0397 | HAS_VOWEL,  // ἠ
            0x0397 | HAS_VOWEL,  // ἡ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἢ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἣ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἤ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἥ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἦ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ἧ
            0x0397 | HAS_VOWEL,  // Ἠ
            0x0397 | HAS_VOWEL,  // Ἡ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἢ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἣ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἤ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἥ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἦ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ἧ
            0x0399 | HAS_VOWEL,  // ἰ
            0x0399 | HAS_VOWEL,  // ἱ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἲ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἳ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἴ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἵ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἶ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ἷ
            0x0399 | HAS_VOWEL,  // Ἰ
            0x0399 | HAS_VOWEL,  // Ἱ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἲ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἳ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἴ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἵ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἶ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ἷ
            0x039F | HAS_VOWEL,  // ὀ
            0x039F | HAS_VOWEL,  // ὁ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ὂ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ὃ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ὄ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ὅ
            0,
            0,
            0x039F | HAS_VOWEL,  // Ὀ
            0x039F | HAS_VOWEL,  // Ὁ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ὂ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ὃ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ὄ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ὅ
            0,
            0,
            0x03A5 | HAS_VOWEL,  // ὐ
            0x03A5 | HAS_VOWEL,  // ὑ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὒ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὓ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὔ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὕ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὖ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὗ
            0,
            0x03A5 | HAS_VOWEL,  // Ὑ
            0,
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ὓ
            0,
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ὕ
            0,
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ὗ
            0x03A9 | HAS_VOWEL,  // ὠ
            0x03A9 | HAS_VOWEL,  // ὡ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὢ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὣ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὤ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὥ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὦ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὧ
            0x03A9 | HAS_VOWEL,  // Ὠ
            0x03A9 | HAS_VOWEL,  // Ὡ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὢ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὣ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὤ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὥ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὦ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὧ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ὰ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ά
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // ὲ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // έ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ὴ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ή
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ὶ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ί
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ὸ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // ό
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ὺ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ύ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ὼ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ώ
            0,
            0,
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾀ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾁ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾂ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾃ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾄ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾅ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾆ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾇ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾈ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾉ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾊ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾋ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾌ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾍ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾎ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾏ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾐ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾑ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾒ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾓ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾔ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾕ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾖ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾗ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾘ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾙ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾚ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾛ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾜ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾝ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾞ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾟ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾠ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾡ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾢ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾣ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾤ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾥ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾦ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾧ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾨ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾩ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾪ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾫ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾬ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾭ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾮ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾯ
            0x0391 | HAS_VOWEL,  // ᾰ
            0x0391 | HAS_VOWEL,  // ᾱ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾲ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾳ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾴ
            0,
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // ᾶ
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ᾷ
            0x0391 | HAS_VOWEL,  // Ᾰ
            0x0391 | HAS_VOWEL,  // Ᾱ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ὰ
            0x0391 | HAS_VOWEL | HAS_ACCENT,  // Ά
            0x0391 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ᾼ
            0,
            0x0399 | HAS_VOWEL,  // ι
            0,
            0,
            0,
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῂ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ῃ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῄ
            0,
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // ῆ
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῇ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Ὲ
            0x0395 | HAS_VOWEL | HAS_ACCENT,  // Έ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ὴ
            0x0397 | HAS_VOWEL | HAS_ACCENT,  // Ή
            0x0397 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ῌ
            0,
            0,
            0,
            0x0399 | HAS_VOWEL,  // ῐ
            0x0399 | HAS_VOWEL,  // ῑ
            0x0399 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ῒ
            0x0399 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ΐ
            0,
            0,
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // ῖ
            0x0399 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ῗ
            0x0399 | HAS_VOWEL,  // Ῐ
            0x0399 | HAS_VOWEL,  // Ῑ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ὶ
            0x0399 | HAS_VOWEL | HAS_ACCENT,  // Ί
            0,
            0,
            0,
            0,
            0x03A5 | HAS_VOWEL,  // ῠ
            0x03A5 | HAS_VOWEL,  // ῡ
            0x03A5 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ῢ
            0x03A5 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ΰ
            0x03A1,  // ῤ
            0x03A1,  // ῥ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // ῦ
            0x03A5 | HAS_VOWEL | HAS_ACCENT | HAS_DIALYTIKA,  // ῧ
            0x03A5 | HAS_VOWEL,  // Ῠ
            0x03A5 | HAS_VOWEL,  // Ῡ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ὺ
            0x03A5 | HAS_VOWEL | HAS_ACCENT,  // Ύ
            0x03A1,  // Ῥ
            0,
            0,
            0,
            0,
            0,
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῲ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ῳ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῴ
            0,
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // ῶ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI | HAS_ACCENT,  // ῷ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ὸ
            0x039F | HAS_VOWEL | HAS_ACCENT,  // Ό
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ὼ
            0x03A9 | HAS_VOWEL | HAS_ACCENT,  // Ώ
            0x03A9 | HAS_VOWEL | HAS_YPOGEGRAMMENI,  // ῼ
            0,
            0,
            0,
        };

        // U+2126 Ohm sign
        private static final char data2126 = 0x03A9 | HAS_VOWEL;  // Ω

        private static final int getLetterData(int c) {
            if (c < 0x370 || 0x2126 < c || (0x3ff < c && c < 0x1f00)) {
                return 0;
            } else if (c <= 0x3ff) {
                return data0370[c - 0x370];
            } else if (c <= 0x1fff) {
                return data1F00[c - 0x1f00];
            } else if (c == 0x2126) {
                return data2126;
            } else {
                return 0;
            }
        }

        /**
         * Returns a non-zero value for each of the Greek combining diacritics
         * listed in The Unicode Standard, version 8, chapter 7.2 Greek,
         * plus some perispomeni look-alikes.
         */
        private static final int getDiacriticData(int c) {
            switch (c) {
            case '\u0300':  // varia
            case '\u0301':  // tonos = oxia
            case '\u0342':  // perispomeni
            case '\u0302':  // circumflex can look like perispomeni
            case '\u0303':  // tilde can look like perispomeni
            case '\u0311':  // inverted breve can look like perispomeni
                return HAS_ACCENT;
            case '\u0308':  // dialytika = diaeresis
                return HAS_COMBINING_DIALYTIKA;
            case '\u0344':  // dialytika tonos
                return HAS_COMBINING_DIALYTIKA | HAS_ACCENT;
            case '\u0345':  // ypogegrammeni = iota subscript
                return HAS_YPOGEGRAMMENI;
            case '\u0304':  // macron
            case '\u0306':  // breve
            case '\u0313':  // comma above
            case '\u0314':  // reversed comma above
            case '\u0343':  // koronis
                return HAS_OTHER_GREEK_DIACRITIC;
            default:
                return 0;
            }
        }

        private static boolean isFollowedByCasedLetter(CharSequence s, int i) {
            while (i < s.length()) {
                int c = Character.codePointAt(s, i);
                int type = UCaseProps.INSTANCE.getTypeOrIgnorable(c);
                if ((type & UCaseProps.IGNORABLE) != 0) {
                    // Case-ignorable, continue with the loop.
                } else if (type != UCaseProps.NONE) {
                    return true;  // Followed by cased letter.
                } else {
                    return false;  // Uncased and not case-ignorable.
                }
            }
            return false;  // Not followed by cased letter.
        }

        /**
         * Greek string uppercasing with a state machine.
         * Probably simpler than a stateless function that has to figure out complex context-before
         * for each character.
         * TODO: Try to re-consolidate one way or another with the non-Greek function.
         *
         * <p>Keep this consistent with the C++ versions in ustrcase.cpp (UTF-16) and ucasemap.cpp (UTF-8).
         * @throws IOException
         */
        private static <A extends Appendable> A toUpper(int options,
                CharSequence src, A dest, Edits edits) throws IOException {
            int state = 0;
            for (int i = 0; i < src.length();) {
                int c = Character.codePointAt(src, i);
                int nextIndex = i + Character.charCount(c);
                int nextState = 0;
                int type = UCaseProps.INSTANCE.getTypeOrIgnorable(c);
                if ((type & UCaseProps.IGNORABLE) != 0) {
                    // c is case-ignorable
                    nextState |= (state & AFTER_CASED);
                } else if (type != UCaseProps.NONE) {
                    // c is cased
                    nextState |= AFTER_CASED;
                }
                int data = getLetterData(c);
                if (data > 0) {
                    int upper = data & UPPER_MASK;
                    // Add a dialytika to this iota or ypsilon vowel
                    // if we removed a tonos from the previous vowel,
                    // and that previous vowel did not also have (or gain) a dialytika.
                    // Adding one only to the final vowel in a longer sequence
                    // (which does not occur in normal writing) would require lookahead.
                    // Set the same flag as for preserving an existing dialytika.
                    if ((data & HAS_VOWEL) != 0 && (state & AFTER_VOWEL_WITH_ACCENT) != 0 &&
                            (upper == 'Ι' || upper == 'Υ')) {
                        data |= HAS_DIALYTIKA;
                    }
                    int numYpogegrammeni = 0;  // Map each one to a trailing, spacing, capital iota.
                    if ((data & HAS_YPOGEGRAMMENI) != 0) {
                        numYpogegrammeni = 1;
                    }
                    // Skip combining diacritics after this Greek letter.
                    while (nextIndex < src.length()) {
                        int diacriticData = getDiacriticData(src.charAt(nextIndex));
                        if (diacriticData != 0) {
                            data |= diacriticData;
                            if ((diacriticData & HAS_YPOGEGRAMMENI) != 0) {
                                ++numYpogegrammeni;
                            }
                            ++nextIndex;
                        } else {
                            break;  // not a Greek diacritic
                        }
                    }
                    if ((data & HAS_VOWEL_AND_ACCENT_AND_DIALYTIKA) == HAS_VOWEL_AND_ACCENT) {
                        nextState |= AFTER_VOWEL_WITH_ACCENT;
                    }
                    // Map according to Greek rules.
                    boolean addTonos = false;
                    if (upper == 'Η' &&
                            (data & HAS_ACCENT) != 0 &&
                            numYpogegrammeni == 0 &&
                            (state & AFTER_CASED) == 0 &&
                            !isFollowedByCasedLetter(src, nextIndex)) {
                        // Keep disjunctive "or" with (only) a tonos.
                        // We use the same "word boundary" conditions as for the Final_Sigma test.
                        if (i == nextIndex) {
                            upper = 'Ή';  // Preserve the precomposed form.
                        } else {
                            addTonos = true;
                        }
                    } else if ((data & HAS_DIALYTIKA) != 0) {
                        // Preserve a vowel with dialytika in precomposed form if it exists.
                        if (upper == 'Ι') {
                            upper = 'Ϊ';
                            data &= ~HAS_EITHER_DIALYTIKA;
                        } else if (upper == 'Υ') {
                            upper = 'Ϋ';
                            data &= ~HAS_EITHER_DIALYTIKA;
                        }
                    }

                    boolean change;
                    if (edits == null) {
                        change = true;  // common, simple usage
                    } else {
                        // Find out first whether we are changing the text.
                        change = src.charAt(i) != upper || numYpogegrammeni > 0;
                        int i2 = i + 1;
                        if ((data & HAS_EITHER_DIALYTIKA) != 0) {
                            change |= i2 >= nextIndex || src.charAt(i2) != 0x308;
                            ++i2;
                        }
                        if (addTonos) {
                            change |= i2 >= nextIndex || src.charAt(i2) != 0x301;
                            ++i2;
                        }
                        int oldLength = nextIndex - i;
                        int newLength = (i2 - i) + numYpogegrammeni;
                        change |= oldLength != newLength;
                        if (change) {
                            if (edits != null) {
                                edits.addReplace(oldLength, newLength);
                            }
                        } else {
                            if (edits != null) {
                                edits.addUnchanged(oldLength);
                            }
                            // Write unchanged text?
                            change = (options & OMIT_UNCHANGED_TEXT) == 0;
                        }
                    }

                    if (change) {
                        dest.append((char)upper);
                        if ((data & HAS_EITHER_DIALYTIKA) != 0) {
                            dest.append('\u0308');  // restore or add a dialytika
                        }
                        if (addTonos) {
                            dest.append('\u0301');
                        }
                        while (numYpogegrammeni > 0) {
                            dest.append('Ι');
                            --numYpogegrammeni;
                        }
                    }
                } else {
                    c = UCaseProps.INSTANCE.toFullUpper(c, null, dest, UCaseProps.LOC_GREEK);
                    appendResult(c, dest, nextIndex - i, options, edits);
                }
                i = nextIndex;
                state = nextState;
            }
            return dest;
        }
    }
}
