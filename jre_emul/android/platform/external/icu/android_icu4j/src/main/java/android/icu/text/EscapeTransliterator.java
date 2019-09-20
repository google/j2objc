/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2001-2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   11/19/2001  aliu        Creation.
**********************************************************************
*/
package android.icu.text;
import android.icu.impl.Utility;

/**
 * A transliterator that converts Unicode characters to an escape
 * form.  Examples of escape forms are "U+4E01" and "&#x10FFFF;".
 * Escape forms have a prefix and suffix, either of which may be
 * empty, a radix, typically 16 or 10, a minimum digit count,
 * typically 1, 4, or 8, and a boolean that specifies whether
 * supplemental characters are handled as 32-bit code points or as two
 * 16-bit code units.  Most escape forms handle 32-bit code points,
 * but some, such as the Java form, intentionally break them into two
 * surrogate pairs, for backward compatibility.
 *
 * <p>Some escape forms actually have two different patterns, one for
 * BMP characters (0..FFFF) and one for supplements (>FFFF).  To
 * handle this, a second EscapeTransliterator may be defined that
 * specifies the pattern to be produced for supplementals.  An example
 * of a form that requires this is the C form, which uses "\\uFFFF"
 * for BMP characters and "\\U0010FFFF" for supplementals.
 *
 * <p>This class is package private.  It registers several standard
 * variants with the system which are then accessed via their IDs.
 *
 * @author Alan Liu
 */
class EscapeTransliterator extends Transliterator {

    /**
     * The prefix of the escape form; may be empty, but usually isn't.
     * May not be null.
     */
    private String prefix;

    /**
     * The prefix of the escape form; often empty.  May not be null.
     */
    private String suffix;

    /**
     * The radix to display the number in.  Typically 16 or 10.  Must
     * be in the range 2 to 36.
     */
    private int radix;

    /**
     * The minimum number of digits.  Typically 1, 4, or 8.  Values
     * less than 1 are equivalent to 1.
     */
    private int minDigits;

    /**
     * If true, supplementals are handled as 32-bit code points.  If
     * false, they are handled as two 16-bit code units.
     */
    private boolean grokSupplementals;

    /**
     * The form to be used for supplementals.  If this is null then
     * the same form is used for BMP characters and supplementals.  If
     * this is not null and if grokSupplementals is true then the
     * prefix, suffix, radix, and minDigits of this object are used
     * for supplementals.
     */
    private EscapeTransliterator supplementalHandler;

    /**
     * Registers standard variants with the system.  Called by
     * Transliterator during initialization.
     */
    static void register() {
        // Unicode: "U+10FFFF" hex, min=4, max=6
        Transliterator.registerFactory("Any-Hex/Unicode", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/Unicode",
                                                "U+", "", 16, 4, true, null);
            }
        });

        // Java: "\\uFFFF" hex, min=4, max=4
        Transliterator.registerFactory("Any-Hex/Java", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/Java",
                                                "\\u", "", 16, 4, false, null);
            }
        });

        // C: "\\uFFFF" hex, min=4, max=4; \\U0010FFFF hex, min=8, max=8
        Transliterator.registerFactory("Any-Hex/C", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/C",
                                                "\\u", "", 16, 4, true,
                       new EscapeTransliterator("", "\\U", "", 16, 8, true, null));
            }
        });

        // XML: "&#x10FFFF;" hex, min=1, max=6
        Transliterator.registerFactory("Any-Hex/XML", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/XML",
                                                "&#x", ";", 16, 1, true, null);
            }
        });

        // XML10: "&1114111;" dec, min=1, max=7 (not really "Any-Hex")
        Transliterator.registerFactory("Any-Hex/XML10", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/XML10",
                                                "&#", ";", 10, 1, true, null);
            }
        });

        // Perl: "\\x{263A}" hex, min=1, max=6
        Transliterator.registerFactory("Any-Hex/Perl", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/Perl",
                                                "\\x{", "}", 16, 1, true, null);
            }
        });

        // Plain: "FFFF" hex, min=4, max=6
        Transliterator.registerFactory("Any-Hex/Plain", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex/Plain",
                                                "", "", 16, 4, true, null);
            }
        });

        // Generic
        Transliterator.registerFactory("Any-Hex", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new EscapeTransliterator("Any-Hex",
                                                "\\u", "", 16, 4, false, null);
            }
        });
    }

    /**
     * Constructs an escape transliterator with the given ID and
     * parameters.  See the class member documentation for details.
     */
    EscapeTransliterator(String ID, String prefix, String suffix,
                         int radix, int minDigits,
                         boolean grokSupplementals,
                         EscapeTransliterator supplementalHandler) {
        super(ID, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.radix = radix;
        this.minDigits = minDigits;
        this.grokSupplementals = grokSupplementals;
        this.supplementalHandler = supplementalHandler;
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position pos, boolean incremental) {
        int start = pos.start;
        int limit = pos.limit;

        StringBuilder buf = new StringBuilder(prefix);
        int prefixLen = prefix.length();
        boolean redoPrefix = false;

        while (start < limit) {
            int c = grokSupplementals ? text.char32At(start) : text.charAt(start);
            int charLen = grokSupplementals ? UTF16.getCharCount(c) : 1;

            if ((c & 0xFFFF0000) != 0 && supplementalHandler != null) {
                buf.setLength(0);
                buf.append(supplementalHandler.prefix);
                Utility.appendNumber(buf, c, supplementalHandler.radix,
                                     supplementalHandler.minDigits);
                buf.append(supplementalHandler.suffix);
                redoPrefix = true;
            } else {
                if (redoPrefix) {
                    buf.setLength(0);
                    buf.append(prefix);
                    redoPrefix = false;
                } else {
                    buf.setLength(prefixLen);
                }
                Utility.appendNumber(buf, c, radix, minDigits);
                buf.append(suffix);
            }

            text.replace(start, start + charLen, buf.toString());
            start += buf.length();
            limit += buf.length() - charLen;
        }

        pos.contextLimit += limit - pos.limit;
        pos.limit = limit;
        pos.start = start;
    }

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(android.icu.text.UnicodeSet, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        sourceSet.addAll(getFilterAsUnicodeSet(inputFilter));
        for (EscapeTransliterator it = this; it != null ; it = it.supplementalHandler) {
            if (inputFilter.size() != 0) {
                targetSet.addAll(it.prefix);
                targetSet.addAll(it.suffix);
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < it.radix; ++i) {
                    Utility.appendNumber(buffer, i, it.radix, it.minDigits);
                }
                targetSet.addAll(buffer.toString()); // TODO drop once String is changed to CharSequence in UnicodeSet
            }
        }
    }
}
