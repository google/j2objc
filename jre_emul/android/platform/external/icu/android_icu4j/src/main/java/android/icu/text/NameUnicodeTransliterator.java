/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 * Copyright (C) 1996-2011, International Business Machines Corporation and
 * others. All Rights Reserved.
 */
package android.icu.text;
import android.icu.impl.PatternProps;
import android.icu.impl.UCharacterName;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;

/**
 * A transliterator that performs name to character mapping.
 * @author Alan Liu
 */
class NameUnicodeTransliterator extends Transliterator {

    static final String _ID = "Name-Any";

    static final String OPEN_PAT    = "\\N~{~";
    static final char   OPEN_DELIM  = '\\'; // first char of OPEN_PAT
    static final char   CLOSE_DELIM = '}';
    static final char   SPACE       = ' ';


    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NameUnicodeTransliterator(null);
            }
        });
    }

    /**
     * Constructs a transliterator.
     */
    public NameUnicodeTransliterator(UnicodeFilter filter) {
        super(_ID, filter);
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position offsets, boolean isIncremental) {

        int maxLen = UCharacterName.INSTANCE.getMaxCharNameLength() + 1; // allow for temporary trailing space

        StringBuffer name = new StringBuffer(maxLen);

        // Get the legal character set
        UnicodeSet legal = new UnicodeSet();
        UCharacterName.INSTANCE.getCharNameCharacters(legal);

        int cursor = offsets.start;
        int limit = offsets.limit;

        // Modes:
        // 0 - looking for open delimiter
        // 1 - after open delimiter
        int mode = 0;
        int openPos = -1; // open delim candidate pos

        int c;
        while (cursor < limit) {
            c = text.char32At(cursor);

            switch (mode) {
            case 0: // looking for open delimiter
                if (c == OPEN_DELIM) { // quick check first
                    openPos = cursor;
                    int i = Utility.parsePattern(OPEN_PAT, text, cursor, limit);
                    if (i >= 0 && i < limit) {
                        mode = 1;
                        name.setLength(0);
                        cursor = i;
                        continue; // *** reprocess char32At(cursor)
                    }
                }
                break;

            case 1: // after open delimiter
                // Look for legal chars.  If \s+ is found, convert it
                // to a single space.  If closeDelimiter is found, exit
                // the loop.  If any other character is found, exit the
                // loop.  If the limit is reached, exit the loop.

                // Convert \s+ => SPACE.  This assumes there are no
                // runs of >1 space characters in names.
                if (PatternProps.isWhiteSpace(c)) {
                    // Ignore leading whitespace
                    if (name.length() > 0 &&
                        name.charAt(name.length()-1) != SPACE) {
                        name.append(SPACE);
                        // If we are too long then abort.  maxLen includes
                        // temporary trailing space, so use '>'.
                        if (name.length() > maxLen) {
                            mode = 0;
                        }
                    }
                    break;
                }

                if (c == CLOSE_DELIM) {

                    int len = name.length();

                    // Delete trailing space, if any
                    if (len > 0 &&
                        name.charAt(len-1) == SPACE) {
                        name.setLength(--len);
                    }

                    c = UCharacter.getCharFromExtendedName(name.toString());
                    if (c != -1) {
                        // Lookup succeeded

                        // assert(UTF16.getCharCount(CLOSE_DELIM) == 1);
                        cursor++; // advance over CLOSE_DELIM

                        String str = UTF16.valueOf(c);
                        text.replace(openPos, cursor, str);

                        // Adjust indices for the change in the length of
                        // the string.  Do not assume that str.length() ==
                        // 1, in case of surrogates.
                        int delta = cursor - openPos - str.length();
                        cursor -= delta;
                        limit -= delta;
                        // assert(cursor == openPos + str.length());
                    }
                    // If the lookup failed, we leave things as-is and
                    // still switch to mode 0 and continue.
                    mode = 0;
                    openPos = -1; // close off candidate
                    continue; // *** reprocess char32At(cursor)
                }

                if (legal.contains(c)) {
                    UTF16.append(name, c);
                    // If we go past the longest possible name then abort.
                    // maxLen includes temporary trailing space, so use '>='.
                    if (name.length() >= maxLen) {
                        mode = 0;
                    }
                }

                // Invalid character
                else {
                    --cursor; // Backup and reprocess this character
                    mode = 0;
                }

                break;
            }

            cursor += UTF16.getCharCount(c);
        }

        offsets.contextLimit += limit - offsets.limit;
        offsets.limit = limit;
        // In incremental mode, only advance the cursor up to the last
        // open delimiter candidate.
        offsets.start = (isIncremental && openPos >= 0) ? openPos : cursor;
    }

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(android.icu.text.UnicodeSet, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        if (!myFilter.containsAll(UnicodeNameTransliterator.OPEN_DELIM) || !myFilter.contains(CLOSE_DELIM)) {
            return; // we have to contain both prefix and suffix
        }
        UnicodeSet items = new UnicodeSet()
        .addAll('0', '9')
        .addAll('A', 'F')
        .addAll('a', 'z') // for controls
        .add('<').add('>') // for controls
        .add('(').add(')') // for controls
        .add('-')
        .add(' ')
        .addAll(UnicodeNameTransliterator.OPEN_DELIM)
        .add(CLOSE_DELIM);
        items.retainAll(myFilter);
        if (items.size() > 0) {
            sourceSet.addAll(items);
            // could produce any character
            targetSet.addAll(0, 0x10FFFF);
        }
    }
}
