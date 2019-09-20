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
import android.icu.lang.UCharacter;

/**
 * A transliterator that converts Unicode escape forms to the
 * characters they represent.  Escape forms have a prefix, a suffix, a
 * radix, and minimum and maximum digit counts.
 *
 * <p>This class is package private.  It registers several standard
 * variants with the system which are then accessed via their IDs.
 *
 * @author Alan Liu
 */
class UnescapeTransliterator extends Transliterator {

    /**
     * The encoded pattern specification.  The pattern consists of
     * zero or more forms.  Each form consists of a prefix, suffix,
     * radix, minimum digit count, and maximum digit count.  These
     * values are stored as a five character header.  That is, their
     * numeric values are cast to 16-bit characters and stored in the
     * string.  Following these five characters, the prefix
     * characters, then suffix characters are stored.  Each form thus
     * takes n+5 characters, where n is the total length of the prefix
     * and suffix.  The end is marked by a header of length one
     * consisting of the character END.
     */
    private char spec[];

    /**
     * Special character marking the end of the spec[] array.
     */
    private static final char END = 0xFFFF;

    /**
     * Registers standard variants with the system.  Called by
     * Transliterator during initialization.
     */
    static void register() {
        // Unicode: "U+10FFFF" hex, min=4, max=6
        Transliterator.registerFactory("Hex-Any/Unicode", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Unicode", new char[] {
                    2, 0, 16, 4, 6, 'U', '+',
                    END
                });
            }
        });

        // Java: "\\uFFFF" hex, min=4, max=4
        Transliterator.registerFactory("Hex-Any/Java", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Java", new char[] {
                    2, 0, 16, 4, 4, '\\', 'u',
                    END
                });
            }
        });

        // C: "\\uFFFF" hex, min=4, max=4; \\U0010FFFF hex, min=8, max=8
        Transliterator.registerFactory("Hex-Any/C", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/C", new char[] {
                    2, 0, 16, 4, 4, '\\', 'u',
                    2, 0, 16, 8, 8, '\\', 'U',
                    END
                });
            }
        });

        // XML: "&#x10FFFF;" hex, min=1, max=6
        Transliterator.registerFactory("Hex-Any/XML", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML", new char[] {
                    3, 1, 16, 1, 6, '&', '#', 'x', ';',
                    END
                });
            }
        });

        // XML10: "&1114111;" dec, min=1, max=7 (not really "Hex-Any")
        Transliterator.registerFactory("Hex-Any/XML10", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML10", new char[] {
                    2, 1, 10, 1, 7, '&', '#', ';',
                    END
                });
            }
        });

        // Perl: "\\x{263A}" hex, min=1, max=6
        Transliterator.registerFactory("Hex-Any/Perl", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Perl", new char[] {
                    3, 1, 16, 1, 6, '\\', 'x', '{', '}',
                    END
                });
            }
        });

        // All: Java, C, Perl, XML, XML10, Unicode
        Transliterator.registerFactory("Hex-Any", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any", new char[] {
                    2, 0, 16, 4, 6, 'U', '+',            // Unicode
                    2, 0, 16, 4, 4, '\\', 'u',           // Java
                    2, 0, 16, 8, 8, '\\', 'U',           // C (surrogates)
                    3, 1, 16, 1, 6, '&', '#', 'x', ';',  // XML
                    2, 1, 10, 1, 7, '&', '#', ';',       // XML10
                    3, 1, 16, 1, 6, '\\', 'x', '{', '}', // Perl
                    END
                });
            }
        });
    }

    /**
     * Package private constructor.  Takes the encoded spec array.
     */
    UnescapeTransliterator(String ID, char spec[]) {
        super(ID, null);
        this.spec = spec;
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position pos, boolean isIncremental) {
        int start = pos.start;
        int limit = pos.limit;
        int i, ipat;

      loop:
        while (start < limit) {
            // Loop over the forms in spec[].  Exit this loop when we
            // match one of the specs.  Exit the outer loop if a
            // partial match is detected and isIncremental is true.
            for (ipat = 0; spec[ipat] != END;) {

                // Read the header
                int prefixLen = spec[ipat++];
                int suffixLen = spec[ipat++];
                int radix     = spec[ipat++];
                int minDigits = spec[ipat++];
                int maxDigits = spec[ipat++];

                // s is a copy of start that is advanced over the
                // characters as we parse them.
                int s = start;
                boolean match = true;

                for (i=0; i<prefixLen; ++i) {
                    if (s >= limit) {
                        if (i > 0) {
                            // We've already matched a character.  This is
                            // a partial match, so we return if in
                            // incremental mode.  In non-incremental mode,
                            // go to the next spec.
                            if (isIncremental) {
                                break loop;
                            }
                            match = false;
                            break;
                        }
                    }
                    char c = text.charAt(s++);
                    if (c != spec[ipat + i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    int u = 0;
                    int digitCount = 0;
                    for (;;) {
                        if (s >= limit) {
                            // Check for partial match in incremental mode.
                            if (s > start && isIncremental) {
                                break loop;
                            }
                            break;
                        }
                        int ch = text.char32At(s);
                        int digit = UCharacter.digit(ch, radix);
                        if (digit < 0) {
                            break;
                        }
                        s += UTF16.getCharCount(ch);
                        u = (u * radix) + digit;
                        if (++digitCount == maxDigits) {
                            break;
                        }
                    }

                    match = (digitCount >= minDigits);

                    if (match) {
                        for (i=0; i<suffixLen; ++i) {
                            if (s >= limit) {
                                // Check for partial match in incremental mode.
                                if (s > start && isIncremental) {
                                    break loop;
                                }
                                match = false;
                                break;
                            }
                            char c = text.charAt(s++);
                            if (c != spec[ipat + prefixLen + i]) {
                                match = false;
                                break;
                            }
                        }

                        if (match) {
                            // At this point, we have a match
                            String str = UTF16.valueOf(u);
                            text.replace(start, s, str);
                            limit -= s - start - str.length();
                            // The following break statement leaves the
                            // loop that is traversing the forms in
                            // spec[].  We then parse the next input
                            // character.
                            break;
                        }
                    }
                }

                ipat += prefixLen + suffixLen;
            }

            if (start < limit) {
                start += UTF16.getCharCount(text.char32At(start));
            }
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
        // Each form consists of a prefix, suffix,
        // * radix, minimum digit count, and maximum digit count.  These
        // * values are stored as a five character header. ...
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        UnicodeSet items = new UnicodeSet();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; spec[i] != END;) {
            // first 5 items are header
            int end = i + spec[i] + spec[i+1] + 5;
            int radix = spec[i+2];
            for (int j = 0; j < radix; ++j) {
                Utility.appendNumber(buffer, j, radix, 0);
            }
            // then add the characters
            for (int j = i + 5; j < end; ++j) {
                items.add(spec[j]);
            }
            // and go to next block
            i = end;
        }
        items.addAll(buffer.toString());
        items.retainAll(myFilter);

        if (items.size() > 0) {
            sourceSet.addAll(items);
            targetSet.addAll(0,0x10FFFF); // assume we can produce any character
        }
    }
}
