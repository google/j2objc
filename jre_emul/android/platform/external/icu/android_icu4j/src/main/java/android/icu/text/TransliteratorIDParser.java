/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2002-2011, International Business Machines Corporation
*   and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   01/14/2002  aliu        Creation.
**********************************************************************
*/

package android.icu.text;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.util.CaseInsensitiveString;

/**
 * Parsing component for transliterator IDs.  This class contains only
 * static members; it cannot be instantiated.  Methods in this class
 * parse various ID formats, including the following:
 *
 * A basic ID, which contains source, target, and variant, but no
 * filter and no explicit inverse.  Examples include
 * "Latin-Greek/UNGEGN" and "Null".
 *
 * A single ID, which is a basic ID plus optional filter and optional
 * explicit inverse.  Examples include "[a-zA-Z] Latin-Greek" and
 * "Lower (Upper)".
 *
 * A compound ID, which is a sequence of one or more single IDs,
 * separated by semicolons, with optional forward and reverse global
 * filters.  The global filters are UnicodeSet patterns prepended or
 * appended to the IDs, separated by semicolons.  An appended filter
 * must be enclosed in parentheses and applies in the reverse
 * direction.
 *
 * @author Alan Liu
 */
class TransliteratorIDParser {

    private static final char ID_DELIM = ';';

    private static final char TARGET_SEP = '-';

    private static final char VARIANT_SEP = '/';

    private static final char OPEN_REV = '(';

    private static final char CLOSE_REV = ')';

    private static final String ANY = "Any";

    private static final int FORWARD = Transliterator.FORWARD;

    private static final int REVERSE = Transliterator.REVERSE;

    private static final Map<CaseInsensitiveString, String> SPECIAL_INVERSES =
        Collections.synchronizedMap(new HashMap<CaseInsensitiveString, String>());

    /**
     * A structure containing the parsed data of a filtered ID, that
     * is, a basic ID optionally with a filter.
     *
     * 'source' and 'target' will always be non-null.  The 'variant'
     * will be non-null only if a non-empty variant was parsed.
     *
     * 'sawSource' is true if there was an explicit source in the
     * parsed id.  If there was no explicit source, then an implied
     * source of ANY is returned and 'sawSource' is set to false.
     * 
     * 'filter' is the parsed filter pattern, or null if there was no
     * filter.
     */
    private static class Specs {
        public String source; // not null
        public String target; // not null
        public String variant; // may be null
        public String filter; // may be null
        public boolean sawSource;
        Specs(String s, String t, String v, boolean sawS, String f) {
            source = s;
            target = t;
            variant = v;
            sawSource = sawS;
            filter = f;
        }
    }

    /**
     * A structure containing the canonicalized data of a filtered ID,
     * that is, a basic ID optionally with a filter.
     *
     * 'canonID' is always non-null.  It may be the empty string "".
     * It is the id that should be assigned to the created
     * transliterator.  It _cannot_ be instantiated directly.
     *
     * 'basicID' is always non-null and non-empty.  It is always of
     * the form S-T or S-T/V.  It is designed to be fed to low-level
     * instantiation code that only understands these two formats.
     *
     * 'filter' may be null, if there is none, or non-null and
     * non-empty.
     */
    static class SingleID {
        public String canonID;
        public String basicID;
        public String filter;
        SingleID(String c, String b, String f) {
            canonID = c;
            basicID = b;
            filter = f;
        }
        SingleID(String c, String b) {
            this(c, b, null);
        }
        Transliterator getInstance() {
            Transliterator t;
            if (basicID == null || basicID.length() == 0) {
                t = Transliterator.getBasicInstance("Any-Null", canonID);
            } else {
                t = Transliterator.getBasicInstance(basicID, canonID);
            }
            if (t != null) {
                if (filter != null) {
                    t.setFilter(new UnicodeSet(filter));
                }
            }
            return t;
        }
    }

    /**
     * Parse a filter ID, that is, an ID of the general form
     * "[f1] s1-t1/v1", with the filters optional, and the variants optional.
     * @param id the id to be parsed
     * @param pos INPUT-OUTPUT parameter.  On input, the position of
     * the first character to parse.  On output, the position after
     * the last character parsed.
     * @return a SingleID object or null if the parse fails
     */
    public static SingleID parseFilterID(String id, int[] pos) {

        int start = pos[0];
        Specs specs = parseFilterID(id, pos, true);
        if (specs == null) {
            pos[0] = start;
            return null;
        }

        // Assemble return results
        SingleID single = specsToID(specs, FORWARD);
        single.filter = specs.filter;
        return single;
    }

    /**
     * Parse a single ID, that is, an ID of the general form
     * "[f1] s1-t1/v1 ([f2] s2-t3/v2)", with the parenthesized element
     * optional, the filters optional, and the variants optional.
     * @param id the id to be parsed
     * @param pos INPUT-OUTPUT parameter.  On input, the position of
     * the first character to parse.  On output, the position after
     * the last character parsed.
     * @param dir the direction.  If the direction is REVERSE then the
     * SingleID is constructed for the reverse direction.
     * @return a SingleID object or null
     */
    public static SingleID parseSingleID(String id, int[] pos, int dir) {

        int start = pos[0];

        // The ID will be of the form A, A(), A(B), or (B), where
        // A and B are filter IDs.
        Specs specsA = null;
        Specs specsB = null;
        boolean sawParen = false;

        // On the first pass, look for (B) or ().  If this fails, then
        // on the second pass, look for A, A(B), or A().
        for (int pass=1; pass<=2; ++pass) {
            if (pass == 2) {
                specsA = parseFilterID(id, pos, true);
                if (specsA == null) {
                    pos[0] = start;
                    return null;
                }
            }
            if (Utility.parseChar(id, pos, OPEN_REV)) {
                sawParen = true;
                if (!Utility.parseChar(id, pos, CLOSE_REV)) {
                    specsB = parseFilterID(id, pos, true);
                    // Must close with a ')'
                    if (specsB == null || !Utility.parseChar(id, pos, CLOSE_REV)) {
                        pos[0] = start;
                        return null;
                    }
                }
                break;
            }
        }

        // Assemble return results
        SingleID single;
        if (sawParen) {
            if (dir == FORWARD) {
                single = specsToID(specsA, FORWARD);
                single.canonID = single.canonID +
                    OPEN_REV + specsToID(specsB, FORWARD).canonID + CLOSE_REV;
                if (specsA != null) {
                    single.filter = specsA.filter;
                }
            } else {
                single = specsToID(specsB, FORWARD);
                single.canonID = single.canonID +
                    OPEN_REV + specsToID(specsA, FORWARD).canonID + CLOSE_REV;
                if (specsB != null) {
                    single.filter = specsB.filter;
                }
            }
        } else {
            // assert(specsA != null);
            if (dir == FORWARD) {
                single = specsToID(specsA, FORWARD);
            } else {
                single = specsToSpecialInverse(specsA);
                if (single == null) {
                    single = specsToID(specsA, REVERSE);
                }
            }
            single.filter = specsA.filter;
        }

        return single;
    }

    /**
     * Parse a global filter of the form "[f]" or "([f])", depending
     * on 'withParens'.
     * @param id the pattern the parse
     * @param pos INPUT-OUTPUT parameter.  On input, the position of
     * the first character to parse.  On output, the position after
     * the last character parsed.
     * @param dir the direction.
     * @param withParens INPUT-OUTPUT parameter.  On entry, if
     * withParens[0] is 0, then parens are disallowed.  If it is 1,
     * then parens are requires.  If it is -1, then parens are
     * optional, and the return result will be set to 0 or 1.
     * @param canonID OUTPUT parameter.  The pattern for the filter
     * added to the canonID, either at the end, if dir is FORWARD, or
     * at the start, if dir is REVERSE.  The pattern will be enclosed
     * in parentheses if appropriate, and will be suffixed with an
     * ID_DELIM character.  May be null.
     * @return a UnicodeSet object or null.  A non-null results
     * indicates a successful parse, regardless of whether the filter
     * applies to the given direction.  The caller should discard it
     * if withParens != (dir == REVERSE).
     */
    public static UnicodeSet parseGlobalFilter(String id, int[] pos, int dir,
                                               int[] withParens,
                                               StringBuffer canonID) {
        UnicodeSet filter = null;
        int start = pos[0];

        if (withParens[0] == -1) {
            withParens[0] = Utility.parseChar(id, pos, OPEN_REV) ? 1 : 0;
        } else if (withParens[0] == 1) {
            if (!Utility.parseChar(id, pos, OPEN_REV)) {
                pos[0] = start;
                return null;
            }
        }
        
        pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);

        if (UnicodeSet.resemblesPattern(id, pos[0])) {
            ParsePosition ppos = new ParsePosition(pos[0]);
            try {
                filter = new UnicodeSet(id, ppos, null);
            } catch (IllegalArgumentException e) {
                pos[0] = start;
                return null;
            }

            String pattern = id.substring(pos[0], ppos.getIndex());
            pos[0] = ppos.getIndex();

            if (withParens[0] == 1 && !Utility.parseChar(id, pos, CLOSE_REV)) {
                pos[0] = start;
                return null;
            }

            // In the forward direction, append the pattern to the
            // canonID.  In the reverse, insert it at zero, and invert
            // the presence of parens ("A" <-> "(A)").
            if (canonID != null) {
                if (dir == FORWARD) {
                    if (withParens[0] == 1) {
                        pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                    }
                    canonID.append(pattern + ID_DELIM);
                } else {
                    if (withParens[0] == 0) {
                        pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                    }
                    canonID.insert(0, pattern + ID_DELIM);
                }
            }
        }

        return filter;
    }

    /**
     * Parse a compound ID, consisting of an optional forward global
     * filter, a separator, one or more single IDs delimited by
     * separators, an an optional reverse global filter.  The
     * separator is a semicolon.  The global filters are UnicodeSet
     * patterns.  The reverse global filter must be enclosed in
     * parentheses.
     * @param id the pattern the parse
     * @param dir the direction.
     * @param canonID OUTPUT parameter that receives the canonical ID,
     * consisting of canonical IDs for all elements, as returned by
     * parseSingleID(), separated by semicolons.  Previous contents
     * are discarded.
     * @param list OUTPUT parameter that receives a list of SingleID
     * objects representing the parsed IDs.  Previous contents are
     * discarded.
     * @param globalFilter OUTPUT parameter that receives a pointer to
     * a newly created global filter for this ID in this direction, or
     * null if there is none.
     * @return true if the parse succeeds, that is, if the entire
     * id is consumed without syntax error.
     */
    public static boolean parseCompoundID(String id, int dir,
                                          StringBuffer canonID,
                                          List<SingleID> list,
                                          UnicodeSet[] globalFilter) {
        int[] pos = new int[] { 0 };
        int[] withParens = new int[1];
        list.clear();
        UnicodeSet filter;
        globalFilter[0] = null;
        canonID.setLength(0);

        // Parse leading global filter, if any
        withParens[0] = 0; // parens disallowed
        filter = parseGlobalFilter(id, pos, dir, withParens, canonID);
        if (filter != null) {
            if (!Utility.parseChar(id, pos, ID_DELIM)) {
                // Not a global filter; backup and resume
                canonID.setLength(0);
                pos[0] = 0;
            }
            if (dir == FORWARD) {
                globalFilter[0] = filter;
            }
        }

        boolean sawDelimiter = true;
        for (;;) {
            SingleID single = parseSingleID(id, pos, dir);
            if (single == null) {
                break;
            }
            if (dir == FORWARD) {
                list.add(single);
            } else {
                list.add(0, single);
            }
            if (!Utility.parseChar(id, pos, ID_DELIM)) {
                sawDelimiter = false;
                break;
            }
        }

        if (list.size() == 0) {
            return false;
        }

        // Construct canonical ID
        for (int i=0; i<list.size(); ++i) {
            SingleID single = list.get(i);
            canonID.append(single.canonID);
            if (i != (list.size()-1)) {
                canonID.append(ID_DELIM);
            }
        }

        // Parse trailing global filter, if any, and only if we saw
        // a trailing delimiter after the IDs.
        if (sawDelimiter) {
            withParens[0] = 1; // parens required
            filter = parseGlobalFilter(id, pos, dir, withParens, canonID);
            if (filter != null) {
                // Don't require trailing ';', but parse it if present
                Utility.parseChar(id, pos, ID_DELIM);
                
                if (dir == REVERSE) {
                    globalFilter[0] = filter;
                }
            }
        }

        // Trailing unparsed text is a syntax error
        pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);
        if (pos[0] != id.length()) {
            return false;
        }

        return true;
    }

    /**
     * Returns the list of Transliterator objects for the
     * given list of SingleID objects.
     * 
     * @param ids list vector of SingleID objects.
     * @return Actual transliterators for the list of SingleIDs
     */
    static List<Transliterator> instantiateList(List<SingleID> ids) {
        Transliterator t;
        List<Transliterator> translits = new ArrayList<Transliterator>();
        for (SingleID single : ids) {
            if (single.basicID.length() == 0) {
                continue;
            }
            t = single.getInstance();
            if (t == null) {
                throw new IllegalArgumentException("Illegal ID " + single.canonID);
            }
            translits.add(t);
        }

        // An empty list is equivalent to a Null transliterator.
        if (translits.size() == 0) {
            t = Transliterator.getBasicInstance("Any-Null", null);
            if (t == null) {
                // Should never happen
                throw new IllegalArgumentException("Internal error; cannot instantiate Any-Null");
            }
            translits.add(t);
        }
        return translits;
    }

    /**
     * Parse an ID into pieces.  Take IDs of the form T, T/V, S-T,
     * S-T/V, or S/V-T.  If the source is missing, return a source of
     * ANY.
     * @param id the id string, in any of several forms
     * @return an array of 4 strings: source, target, variant, and
     * isSourcePresent.  If the source is not present, ANY will be
     * given as the source, and isSourcePresent will be null.  Otherwise
     * isSourcePresent will be non-null.  The target may be empty if the
     * id is not well-formed.  The variant may be empty.
     */
    public static String[] IDtoSTV(String id) {
        String source = ANY;
        String target = null;
        String variant = "";
        
        int sep = id.indexOf(TARGET_SEP);
        int var = id.indexOf(VARIANT_SEP);
        if (var < 0) {
            var = id.length();
        }
        boolean isSourcePresent = false;
        
        if (sep < 0) {
            // Form: T/V or T (or /V)
            target = id.substring(0, var);
            variant = id.substring(var);
        } else if (sep < var) {
            // Form: S-T/V or S-T (or -T/V or -T)
            if (sep > 0) {
                source = id.substring(0, sep);
              isSourcePresent = true;
            }
            target = id.substring(++sep, var);
            variant = id.substring(var);
        } else {
            // Form: (S/V-T or /V-T)
            if (var > 0) {
                source = id.substring(0, var);
                isSourcePresent = true;
            }
            variant = id.substring(var, sep++);
            target = id.substring(sep);
        }

        if (variant.length() > 0) {
            variant = variant.substring(1);
        }
        
        return new String[] { source, target, variant,
                              isSourcePresent ? "" : null };
    }

    /**
     * Given source, target, and variant strings, concatenate them into a
     * full ID.  If the source is empty, then "Any" will be used for the
     * source, so the ID will always be of the form s-t/v or s-t.
     */
    public static String STVtoID(String source,
                                 String target,
                                 String variant) {
        StringBuilder id = new StringBuilder(source);
        if (id.length() == 0) {
            id.append(ANY);
        }
        id.append(TARGET_SEP).append(target);
        if (variant != null && variant.length() != 0) {
            id.append(VARIANT_SEP).append(variant);
        }
        return id.toString();
    }

    /**
     * Register two targets as being inverses of one another.  For
     * example, calling registerSpecialInverse("NFC", "NFD", true) causes
     * Transliterator to form the following inverse relationships:
     *
     * <pre>NFC => NFD
     * Any-NFC => Any-NFD
     * NFD => NFC
     * Any-NFD => Any-NFC</pre>
     *
     * (Without the special inverse registration, the inverse of NFC
     * would be NFC-Any.)  Note that NFD is shorthand for Any-NFD, but
     * that the presence or absence of "Any-" is preserved.
     *
     * <p>The relationship is symmetrical; registering (a, b) is
     * equivalent to registering (b, a).
     *
     * <p>The relevant IDs must still be registered separately as
     * factories or classes.
     *
     * <p>Only the targets are specified.  Special inverses always
     * have the form Any-Target1 <=> Any-Target2.  The target should
     * have canonical casing (the casing desired to be produced when
     * an inverse is formed) and should contain no whitespace or other
     * extraneous characters.
     *
     * @param target the target against which to register the inverse
     * @param inverseTarget the inverse of target, that is
     * Any-target.getInverse() => Any-inverseTarget
     * @param bidirectional if true, register the reverse relation
     * as well, that is, Any-inverseTarget.getInverse() => Any-target
     */
    public static void registerSpecialInverse(String target,
                                              String inverseTarget,
                                              boolean bidirectional) {
        SPECIAL_INVERSES.put(new CaseInsensitiveString(target), inverseTarget);
        if (bidirectional && !target.equalsIgnoreCase(inverseTarget)) {
            SPECIAL_INVERSES.put(new CaseInsensitiveString(inverseTarget), target);
        }
    }

    //----------------------------------------------------------------
    // Private implementation
    //----------------------------------------------------------------

    /**
     * Parse an ID into component pieces.  Take IDs of the form T,
     * T/V, S-T, S-T/V, or S/V-T.  If the source is missing, return a
     * source of ANY.
     * @param id the id string, in any of several forms
     * @param pos INPUT-OUTPUT parameter.  On input, pos[0] is the
     * offset of the first character to parse in id.  On output,
     * pos[0] is the offset after the last parsed character.  If the
     * parse failed, pos[0] will be unchanged.
     * @param allowFilter if true, a UnicodeSet pattern is allowed
     * at any location between specs or delimiters, and is returned
     * as the fifth string in the array.
     * @return a Specs object, or null if the parse failed.  If
     * neither source nor target was seen in the parsed id, then the
     * parse fails.  If allowFilter is true, then the parsed filter
     * pattern is returned in the Specs object, otherwise the returned
     * filter reference is null.  If the parse fails for any reason
     * null is returned.
     */
    private static Specs parseFilterID(String id, int[] pos,
                                       boolean allowFilter) {
        String first = null;
        String source = null;
        String target = null;
        String variant = null;
        String filter = null;
        char delimiter = 0;
        int specCount = 0;
        int start = pos[0];

        // This loop parses one of the following things with each
        // pass: a filter, a delimiter character (either '-' or '/'),
        // or a spec (source, target, or variant).
        for (;;) {
            pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);
            if (pos[0] == id.length()) {
                break;
            }

            // Parse filters
            if (allowFilter && filter == null &&
                UnicodeSet.resemblesPattern(id, pos[0])) {

                ParsePosition ppos = new ParsePosition(pos[0]);
                // Parse the set to get the position.
                new UnicodeSet(id, ppos, null);
                filter = id.substring(pos[0], ppos.getIndex());
                pos[0] = ppos.getIndex();
                continue;
            }

            if (delimiter == 0) {
                char c = id.charAt(pos[0]);
                if ((c == TARGET_SEP && target == null) ||
                    (c == VARIANT_SEP && variant == null)) {
                    delimiter = c;
                    ++pos[0];
                    continue;
                }
            }

            // We are about to try to parse a spec with no delimiter
            // when we can no longer do so (we can only do so at the
            // start); break.
            if (delimiter == 0 && specCount > 0) {
                break;
            }

            String spec = Utility.parseUnicodeIdentifier(id, pos);
            if (spec == null) {
                // Note that if there was a trailing delimiter, we
                // consume it.  So Foo-, Foo/, Foo-Bar/, and Foo/Bar-
                // are legal.
                break;
            }

            switch (delimiter) {
            case 0:
                first = spec;
                break;
            case TARGET_SEP:
                target = spec;
                break;
            case VARIANT_SEP:
                variant = spec;
                break;
            }
            ++specCount;
            delimiter = 0;
        }

        // A spec with no prior character is either source or target,
        // depending on whether an explicit "-target" was seen.
        if (first != null) {
            if (target == null) {
                target = first;
            } else {
                source = first;
            }
        }

        // Must have either source or target
        if (source == null && target == null) {
            pos[0] = start;
            return null;
        }

        // Empty source or target defaults to ANY
        boolean sawSource = true;
        if (source == null) {
            source = ANY;
            sawSource = false;
        }
        if (target == null) {
            target = ANY;
        }

        return new Specs(source, target, variant, sawSource, filter);
    }

    /**
     * Givens a Spec object, convert it to a SingleID object.  The
     * Spec object is a more unprocessed parse result.  The SingleID
     * object contains information about canonical and basic IDs.
     * @return a SingleID; never returns null.  Returned object always
     * has 'filter' field of null.
     */
    private static SingleID specsToID(Specs specs, int dir) {
        String canonID = "";
        String basicID = "";
        String basicPrefix = "";
        if (specs != null) {
            StringBuilder buf = new StringBuilder();
            if (dir == FORWARD) {
                if (specs.sawSource) {
                    buf.append(specs.source).append(TARGET_SEP);
                } else {
                    basicPrefix = specs.source + TARGET_SEP;
                }
                buf.append(specs.target);
            } else {
                buf.append(specs.target).append(TARGET_SEP).append(specs.source);
            }
            if (specs.variant != null) {
                buf.append(VARIANT_SEP).append(specs.variant);
            }
            basicID = basicPrefix + buf.toString();
            if (specs.filter != null) {
                buf.insert(0, specs.filter);
            }
            canonID = buf.toString();
        }
        return new SingleID(canonID, basicID);
    }

    /**
     * Given a Specs object, return a SingleID representing the
     * special inverse of that ID.  If there is no special inverse
     * then return null.
     * @return a SingleID or null.  Returned object always has
     * 'filter' field of null.
     */
    private static SingleID specsToSpecialInverse(Specs specs) {
        if (!specs.source.equalsIgnoreCase(ANY)) {
            return null;
        }
        String inverseTarget = SPECIAL_INVERSES.get(new CaseInsensitiveString(specs.target));
        if (inverseTarget != null) {
            // If the original ID contained "Any-" then make the
            // special inverse "Any-Foo"; otherwise make it "Foo".
            // So "Any-NFC" => "Any-NFD" but "NFC" => "NFD".
            StringBuilder buf = new StringBuilder();
            if (specs.filter != null) {
                buf.append(specs.filter);
            }
            if (specs.sawSource) {
                buf.append(ANY).append(TARGET_SEP);
            }
            buf.append(inverseTarget);

            String basicID = ANY + TARGET_SEP + inverseTarget;

            if (specs.variant != null) {
                buf.append(VARIANT_SEP).append(specs.variant);
                basicID = basicID + VARIANT_SEP + specs.variant;
            }
            return new SingleID(buf.toString(), basicID);
        }
        return null;
    }
}

//eof
