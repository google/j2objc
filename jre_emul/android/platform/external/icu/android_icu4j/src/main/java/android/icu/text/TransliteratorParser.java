/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2001-2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
*/
package android.icu.text;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.RuleBasedTransliterator.Data;

class TransliteratorParser {

    //----------------------------------------------------------------------
    // Data members
    //----------------------------------------------------------------------

    /**
     * PUBLIC data member.
     * A Vector of RuleBasedTransliterator.Data objects, one for each discrete group
     * of rules in the rule set
     */
    public List<Data> dataVector;

    /**
     * PUBLIC data member.
     * A Vector of Strings containing all of the ID blocks in the rule set
     */
    public List<String> idBlockVector;

    /**
     * The current data object for which we are parsing rules
     */
    private Data curData;

    /**
     * PUBLIC data member containing the parsed compound filter, if any.
     */
    public UnicodeSet compoundFilter;


    private int direction;

    /**
     * Temporary symbol table used during parsing.
     */
    private ParseData parseData;

    /**
     * Temporary vector of set variables.  When parsing is complete, this
     * is copied into the array data.variables.  As with data.variables,
     * element 0 corresponds to character data.variablesBase.
     */
    private List<Object> variablesVector;

    /**
     * Temporary table of variable names.  When parsing is complete, this is
     * copied into data.variableNames.
     */
    private Map<String, char[]> variableNames;

    /**
     * String of standins for segments.  Used during the parsing of a single
     * rule.  segmentStandins.charAt(0) is the standin for "$1" and corresponds
     * to StringMatcher object segmentObjects.elementAt(0), etc.
     */
    private StringBuffer segmentStandins;

    /**
     * Vector of StringMatcher objects for segments.  Used during the
     * parsing of a single rule.
     * segmentStandins.charAt(0) is the standin for "$1" and corresponds
     * to StringMatcher object segmentObjects.elementAt(0), etc.
     */
    private List<StringMatcher> segmentObjects;

    /**
     * The next available stand-in for variables.  This starts at some point in
     * the private use area (discovered dynamically) and increments up toward
     * <code>variableLimit</code>.  At any point during parsing, available
     * variables are <code>variableNext..variableLimit-1</code>.
     */
    private char variableNext;

    /**
     * The last available stand-in for variables.  This is discovered
     * dynamically.  At any point during parsing, available variables are
     * <code>variableNext..variableLimit-1</code>.  During variable definition
     * we use the special value variableLimit-1 as a placeholder.
     */
    private char variableLimit;

    /**
     * When we encounter an undefined variable, we do not immediately signal
     * an error, in case we are defining this variable, e.g., "$a = [a-z];".
     * Instead, we save the name of the undefined variable, and substitute
     * in the placeholder char variableLimit - 1, and decrement
     * variableLimit.
     */
    private String undefinedVariableName;

    /**
     * The stand-in character for the 'dot' set, represented by '.' in
     * patterns.  This is allocated the first time it is needed, and
     * reused thereafter.
     */
    private int dotStandIn = -1;

    //----------------------------------------------------------------------
    // Constants
    //----------------------------------------------------------------------

    // Indicator for ID blocks
    private static final String ID_TOKEN = "::";
    private static final int ID_TOKEN_LEN = 2;

/*
(reserved for future expansion)
    // markers for beginning and end of rule groups
    private static final String BEGIN_TOKEN = "BEGIN";
    private static final String END_TOKEN = "END";
*/

    // Operators
    private static final char VARIABLE_DEF_OP   = '=';
    private static final char FORWARD_RULE_OP   = '>';
    private static final char REVERSE_RULE_OP   = '<';
    private static final char FWDREV_RULE_OP    = '~'; // internal rep of <> op

    private static final String OPERATORS = "=><\u2190\u2192\u2194";
    private static final String HALF_ENDERS = "=><\u2190\u2192\u2194;";

    // Other special characters
    private static final char QUOTE               = '\'';
    private static final char ESCAPE              = '\\';
    private static final char END_OF_RULE         = ';';
    private static final char RULE_COMMENT_CHAR   = '#';

    private static final char CONTEXT_ANTE        = '{'; // ante{key
    private static final char CONTEXT_POST        = '}'; // key}post
    private static final char CURSOR_POS          = '|';
    private static final char CURSOR_OFFSET       = '@';
    private static final char ANCHOR_START        = '^';

    private static final char KLEENE_STAR         = '*';
    private static final char ONE_OR_MORE         = '+';
    private static final char ZERO_OR_ONE         = '?';

    private static final char DOT                 = '.';
    private static final String DOT_SET           = "[^[:Zp:][:Zl:]\\r\\n$]";

    // By definition, the ANCHOR_END special character is a
    // trailing SymbolTable.SYMBOL_REF character.
    // private static final char ANCHOR_END       = '$';

    // Segments of the input string are delimited by "(" and ")".  In the
    // output string these segments are referenced as "$1", "$2", etc.
    private static final char SEGMENT_OPEN        = '(';
    private static final char SEGMENT_CLOSE       = ')';

    // A function is denoted &Source-Target/Variant(text)
    private static final char FUNCTION            = '&';

    // Aliases for some of the syntax characters. These are provided so
    // transliteration rules can be expressed in XML without clashing with
    // XML syntax characters '<', '>', and '&'.
    private static final char ALT_REVERSE_RULE_OP = '\u2190'; // Left Arrow
    private static final char ALT_FORWARD_RULE_OP = '\u2192'; // Right Arrow
    private static final char ALT_FWDREV_RULE_OP  = '\u2194'; // Left Right Arrow
    private static final char ALT_FUNCTION        = '\u2206'; // Increment (~Greek Capital Delta)

    // Special characters disallowed at the top level
    private static UnicodeSet ILLEGAL_TOP = new UnicodeSet("[\\)]");

    // Special characters disallowed within a segment
    private static UnicodeSet ILLEGAL_SEG = new UnicodeSet("[\\{\\}\\|\\@]");

    // Special characters disallowed within a function argument
    private static UnicodeSet ILLEGAL_FUNC = new UnicodeSet("[\\^\\(\\.\\*\\+\\?\\{\\}\\|\\@]");

    //----------------------------------------------------------------------
    // class ParseData
    //----------------------------------------------------------------------

    /**
     * This class implements the SymbolTable interface.  It is used
     * during parsing to give UnicodeSet access to variables that
     * have been defined so far.  Note that it uses variablesVector,
     * _not_ data.variables.
     */
    private class ParseData implements SymbolTable {

        /**
         * Implement SymbolTable API.
         */
        @Override
        public char[] lookup(String name) {
            return variableNames.get(name);
        }

        /**
         * Implement SymbolTable API.
         */
        @Override
        public UnicodeMatcher lookupMatcher(int ch) {
            // Note that we cannot use data.lookup() because the
            // set array has not been constructed yet.
            int i = ch - curData.variablesBase;
            if (i >= 0 && i < variablesVector.size()) {
                return (UnicodeMatcher) variablesVector.get(i);
            }
            return null;
        }

        /**
         * Implement SymbolTable API.  Parse out a symbol reference
         * name.
         */
        @Override
        public String parseReference(String text, ParsePosition pos, int limit) {
            int start = pos.getIndex();
            int i = start;
            while (i < limit) {
                char c = text.charAt(i);
                if ((i==start && !UCharacter.isUnicodeIdentifierStart(c)) ||
                    !UCharacter.isUnicodeIdentifierPart(c)) {
                    break;
                }
                ++i;
            }
            if (i == start) { // No valid name chars
                return null;
            }
            pos.setIndex(i);
            return text.substring(start, i);
        }

        /**
         * Return true if the given character is a matcher standin or a plain
         * character (non standin).
         */
        public boolean isMatcher(int ch) {
            // Note that we cannot use data.lookup() because the
            // set array has not been constructed yet.
            int i = ch - curData.variablesBase;
            if (i >= 0 && i < variablesVector.size()) {
                return variablesVector.get(i) instanceof UnicodeMatcher;
            }
            return true;
        }

        /**
         * Return true if the given character is a replacer standin or a plain
         * character (non standin).
         */
        public boolean isReplacer(int ch) {
            // Note that we cannot use data.lookup() because the
            // set array has not been constructed yet.
            int i = ch - curData.variablesBase;
            if (i >= 0 && i < variablesVector.size()) {
                return variablesVector.get(i) instanceof UnicodeReplacer;
            }
            return true;
        }
    }

    //----------------------------------------------------------------------
    // classes RuleBody, RuleArray, and RuleReader
    //----------------------------------------------------------------------

    /**
     * A private abstract class representing the interface to rule
     * source code that is broken up into lines.  Handles the
     * folding of lines terminated by a backslash.  This folding
     * is limited; it does not account for comments, quotes, or
     * escapes, so its use to be limited.
     */
    private static abstract class RuleBody {

        /**
         * Retrieve the next line of the source, or return null if
         * none.  Folds lines terminated by a backslash into the
         * next line, without regard for comments, quotes, or
         * escapes.
         */
        String nextLine() {
            String s = handleNextLine();
            if (s != null &&
                s.length() > 0 &&
                s.charAt(s.length() - 1) == '\\') {
                StringBuilder b = new StringBuilder(s);
                do {
                    b.deleteCharAt(b.length()-1);
                    s = handleNextLine();
                    if (s == null) {
                        break;
                    }
                    b.append(s);
                } while (s.length() > 0 &&
                         s.charAt(s.length() - 1) == '\\');
                s = b.toString();
            }
            return s;
        }

        /**
         * Reset to the first line of the source.
         */
        abstract void reset();

        /**
         * Subclass method to return the next line of the source.
         */
        abstract String handleNextLine();
    }

    /**
     * RuleBody subclass for a String[] array.
     */
    private static class RuleArray extends RuleBody {
        String[] array;
        int i;
        public RuleArray(String[] array) { this.array = array; i = 0; }
        @Override
        public String handleNextLine() {
            return (i < array.length) ? array[i++] : null;
        }
        @Override
        public void reset() {
            i = 0;
        }
    }

    /*
     * RuleBody subclass for a ResourceReader.
     */
/*    private static class RuleReader extends RuleBody {
        ResourceReader reader;
        public RuleReader(ResourceReader reader) { this.reader = reader; }
        public String handleNextLine() {
            try {
                return reader.readLine();
            } catch (java.io.IOException e) {}
            return null;
        }
        public void reset() {
            reader.reset();
        }
    }*/

    //----------------------------------------------------------------------
    // class RuleHalf
    //----------------------------------------------------------------------

    /**
     * A class representing one side of a rule.  This class knows how to
     * parse half of a rule.  It is tightly coupled to the method
     * TransliteratorParser.parseRule().
     */
    private static class RuleHalf {

        public String text;

        public int cursor = -1; // position of cursor in text
        public int ante = -1;   // position of ante context marker '{' in text
        public int post = -1;   // position of post context marker '}' in text

        // Record the offset to the cursor either to the left or to the
        // right of the key.  This is indicated by characters on the output
        // side that allow the cursor to be positioned arbitrarily within
        // the matching text.  For example, abc{def} > | @@@ xyz; changes
        // def to xyz and moves the cursor to before abc.  Offset characters
        // must be at the start or end, and they cannot move the cursor past
        // the ante- or postcontext text.  Placeholders are only valid in
        // output text.  The length of the ante and post context is
        // determined at runtime, because of supplementals and quantifiers.
        public int cursorOffset = 0; // only nonzero on output side

        // Position of first CURSOR_OFFSET on _right_.  This will be -1
        // for |@, -2 for |@@, etc., and 1 for @|, 2 for @@|, etc.
        private int cursorOffsetPos = 0;

        public boolean anchorStart = false;
        public boolean anchorEnd   = false;

        /**
         * The segment number from 1..n of the next '(' we see
         * during parsing; 1-based.
         */
        private int nextSegmentNumber = 1;

        /**
         * Parse one side of a rule, stopping at either the limit,
         * the END_OF_RULE character, or an operator.
         * @return the index after the terminating character, or
         * if limit was reached, limit
         */
        public int parse(String rule, int pos, int limit,
                         TransliteratorParser parser) {
            int start = pos;
            StringBuffer buf = new StringBuffer();
            pos = parseSection(rule, pos, limit, parser, buf, ILLEGAL_TOP, false);
            text = buf.toString();

            if (cursorOffset > 0 && cursor != cursorOffsetPos) {
                syntaxError("Misplaced " + CURSOR_POS, rule, start);
            }

            return pos;
        }

        /**
         * Parse a section of one side of a rule, stopping at either
         * the limit, the END_OF_RULE character, an operator, or a
         * segment close character.  This method parses both a
         * top-level rule half and a segment within such a rule half.
         * It calls itself recursively to parse segments and nested
         * segments.
         * @param buf buffer into which to accumulate the rule pattern
         * characters, either literal characters from the rule or
         * standins for UnicodeMatcher objects including segments.
         * @param illegal the set of special characters that is illegal during
         * this parse.
         * @param isSegment if true, then we've already seen a '(' and
         * pos on entry points right after it.  Accumulate everything
         * up to the closing ')', put it in a segment matcher object,
         * generate a standin for it, and add the standin to buf.  As
         * a side effect, update the segments vector with a reference
         * to the segment matcher.  This works recursively for nested
         * segments.  If isSegment is false, just accumulate
         * characters into buf.
         * @return the index after the terminating character, or
         * if limit was reached, limit
         */
        private int parseSection(String rule, int pos, int limit,
                                 TransliteratorParser parser,
                                 StringBuffer buf,
                                 UnicodeSet illegal,
                                 boolean isSegment) {
            int start = pos;
            ParsePosition pp = null;
            int quoteStart = -1; // Most recent 'single quoted string'
            int quoteLimit = -1;
            int varStart = -1; // Most recent $variableReference
            int varLimit = -1;
            int[] iref = new int[1];
            int bufStart = buf.length();

        main:
            while (pos < limit) {
                // Since all syntax characters are in the BMP, fetching
                // 16-bit code units suffices here.
                char c = rule.charAt(pos++);
                if (PatternProps.isWhiteSpace(c)) {
                    continue;
                }
                // HALF_ENDERS is all chars that end a rule half: "<>=;"
                if (HALF_ENDERS.indexOf(c) >= 0) {
                    ///CLOVER:OFF
                    // isSegment is always false
                    if (isSegment) {
                        syntaxError("Unclosed segment", rule, start);
                    }
                    ///CLOVER:ON
                    break main;
                }
                if (anchorEnd) {
                    // Text after a presumed end anchor is a syntax err
                    syntaxError("Malformed variable reference", rule, start);
                }
                if (UnicodeSet.resemblesPattern(rule, pos-1)) {
                    if (pp == null) {
                        pp = new ParsePosition(0);
                    }
                    pp.setIndex(pos-1); // Backup to opening '['
                    buf.append(parser.parseSet(rule, pp));
                    pos = pp.getIndex();
                    continue;
                }
                // Handle escapes
                if (c == ESCAPE) {
                    if (pos == limit) {
                        syntaxError("Trailing backslash", rule, start);
                    }
                    iref[0] = pos;
                    int escaped = Utility.unescapeAt(rule, iref);
                    pos = iref[0];
                    if (escaped == -1) {
                        syntaxError("Malformed escape", rule, start);
                    }
                    parser.checkVariableRange(escaped, rule, start);
                    UTF16.append(buf, escaped);
                    continue;
                }
                // Handle quoted matter
                if (c == QUOTE) {
                    int iq = rule.indexOf(QUOTE, pos);
                    if (iq == pos) {
                        buf.append(c); // Parse [''] outside quotes as [']
                        ++pos;
                    } else {
                        /* This loop picks up a run of quoted text of the
                         * form 'aaaa' each time through.  If this run
                         * hasn't really ended ('aaaa''bbbb') then it keeps
                         * looping, each time adding on a new run.  When it
                         * reaches the final quote it breaks.
                         */
                        quoteStart = buf.length();
                        for (;;) {
                            if (iq < 0) {
                                syntaxError("Unterminated quote", rule, start);
                            }
                            buf.append(rule.substring(pos, iq));
                            pos = iq+1;
                            if (pos < limit && rule.charAt(pos) == QUOTE) {
                            // Parse [''] inside quotes as [']
                                iq = rule.indexOf(QUOTE, pos+1);
                            // Continue looping
                            } else {
                                break;
                            }
                        }
                        quoteLimit = buf.length();

                        for (iq=quoteStart; iq<quoteLimit; ++iq) {
                            parser.checkVariableRange(buf.charAt(iq), rule, start);
                        }
                    }
                    continue;
                }

                parser.checkVariableRange(c, rule, start);

                if (illegal.contains(c)) {
                    syntaxError("Illegal character '" + c + '\'', rule, start);
                }

                switch (c) {

                //------------------------------------------------------
                // Elements allowed within and out of segments
                //------------------------------------------------------
                case ANCHOR_START:
                    if (buf.length() == 0 && !anchorStart) {
                        anchorStart = true;
                    } else {
                        syntaxError("Misplaced anchor start",
                                    rule, start);
                    }
                    break;
                case SEGMENT_OPEN:
                    {
                        // bufSegStart is the offset in buf to the first
                        // character of the segment we are parsing.
                        int bufSegStart = buf.length();

                        // Record segment number now, since nextSegmentNumber
                        // will be incremented during the call to parseSection
                        // if there are nested segments.
                        int segmentNumber = nextSegmentNumber++; // 1-based

                        // Parse the segment
                        pos = parseSection(rule, pos, limit, parser, buf, ILLEGAL_SEG, true);

                        // After parsing a segment, the relevant characters are
                        // in buf, starting at offset bufSegStart.  Extract them
                        // into a string matcher, and replace them with a
                        // standin for that matcher.
                        StringMatcher m =
                            new StringMatcher(buf.substring(bufSegStart),
                                              segmentNumber, parser.curData);

                        // Record and associate object and segment number
                        parser.setSegmentObject(segmentNumber, m);
                        buf.setLength(bufSegStart);
                        buf.append(parser.getSegmentStandin(segmentNumber));
                    }
                    break;
                case FUNCTION:
                case ALT_FUNCTION:
                    {
                        iref[0] = pos;
                        TransliteratorIDParser.SingleID single = TransliteratorIDParser.parseFilterID(rule, iref);
                        // The next character MUST be a segment open
                        if (single == null ||
                            !Utility.parseChar(rule, iref, SEGMENT_OPEN)) {
                            syntaxError("Invalid function", rule, start);
                        }

                        Transliterator t = single.getInstance();
                        if (t == null) {
                            syntaxError("Invalid function ID", rule, start);
                        }

                        // bufSegStart is the offset in buf to the first
                        // character of the segment we are parsing.
                        int bufSegStart = buf.length();

                        // Parse the segment
                        pos = parseSection(rule, iref[0], limit, parser, buf, ILLEGAL_FUNC, true);

                        // After parsing a segment, the relevant characters are
                        // in buf, starting at offset bufSegStart.
                        FunctionReplacer r =
                            new FunctionReplacer(t,
                                new StringReplacer(buf.substring(bufSegStart), parser.curData));

                        // Replace the buffer contents with a stand-in
                        buf.setLength(bufSegStart);
                        buf.append(parser.generateStandInFor(r));
                    }
                    break;
                case SymbolTable.SYMBOL_REF:
                    // Handle variable references and segment references "$1" .. "$9"
                    {
                        // A variable reference must be followed immediately
                        // by a Unicode identifier start and zero or more
                        // Unicode identifier part characters, or by a digit
                        // 1..9 if it is a segment reference.
                        if (pos == limit) {
                            // A variable ref character at the end acts as
                            // an anchor to the context limit, as in perl.
                            anchorEnd = true;
                            break;
                        }
                        // Parse "$1" "$2" .. "$9" .. (no upper limit)
                        c = rule.charAt(pos);
                        int r = UCharacter.digit(c, 10);
                        if (r >= 1 && r <= 9) {
                            iref[0] = pos;
                            r = Utility.parseNumber(rule, iref, 10);
                            if (r < 0) {
                                syntaxError("Undefined segment reference",
                                            rule, start);
                            }
                            pos = iref[0];
                            buf.append(parser.getSegmentStandin(r));
                        } else {
                            if (pp == null) { // Lazy create
                                pp = new ParsePosition(0);
                            }
                            pp.setIndex(pos);
                            String name = parser.parseData.
                                parseReference(rule, pp, limit);
                            if (name == null) {
                                // This means the '$' was not followed by a
                                // valid name.  Try to interpret it as an
                                // end anchor then.  If this also doesn't work
                                // (if we see a following character) then signal
                                // an error.
                                anchorEnd = true;
                                break;
                            }
                            pos = pp.getIndex();
                            // If this is a variable definition statement,
                            // then the LHS variable will be undefined.  In
                            // that case appendVariableDef() will append the
                            // special placeholder char variableLimit-1.
                            varStart = buf.length();
                            parser.appendVariableDef(name, buf);
                            varLimit = buf.length();
                        }
                    }
                    break;
                case DOT:
                    buf.append(parser.getDotStandIn());
                    break;
                case KLEENE_STAR:
                case ONE_OR_MORE:
                case ZERO_OR_ONE:
                    // Quantifiers.  We handle single characters, quoted strings,
                    // variable references, and segments.
                    //  a+      matches  aaa
                    //  'foo'+  matches  foofoofoo
                    //  $v+     matches  xyxyxy if $v == xy
                    //  (seg)+  matches  segsegseg
                    {
                        ///CLOVER:OFF
                        // isSegment is always false
                        if (isSegment && buf.length() == bufStart) {
                            // The */+ immediately follows '('
                            syntaxError("Misplaced quantifier", rule, start);
                            break;
                        }
                        ///CLOVER:ON

                        int qstart, qlimit;
                        // The */+ follows an isolated character or quote
                        // or variable reference
                        if (buf.length() == quoteLimit) {
                            // The */+ follows a 'quoted string'
                            qstart = quoteStart;
                            qlimit = quoteLimit;
                        } else if (buf.length() == varLimit) {
                            // The */+ follows a $variableReference
                            qstart = varStart;
                            qlimit = varLimit;
                        } else {
                            // The */+ follows a single character, possibly
                            // a segment standin
                            qstart = buf.length() - 1;
                            qlimit = qstart + 1;
                        }

                        UnicodeMatcher m;
                        try {
                            m = new StringMatcher(buf.toString(), qstart, qlimit,
                                              0, parser.curData);
                        } catch (RuntimeException e) {
                            final String precontext = pos < 50 ? rule.substring(0, pos) : "..." + rule.substring(pos - 50, pos);
                            final String postContext = limit-pos <= 50 ? rule.substring(pos, limit) : rule.substring(pos, pos+50) + "...";
                            throw new IllegalIcuArgumentException("Failure in rule: " + precontext + "$$$"
                                    + postContext).initCause(e);
                        }
                        int min = 0;
                        int max = Quantifier.MAX;
                        switch (c) {
                        case ONE_OR_MORE:
                            min = 1;
                            break;
                        case ZERO_OR_ONE:
                            min = 0;
                            max = 1;
                            break;
                            // case KLEENE_STAR:
                            //    do nothing -- min, max already set
                        }
                        m = new Quantifier(m, min, max);
                        buf.setLength(qstart);
                        buf.append(parser.generateStandInFor(m));
                    }
                    break;

                //------------------------------------------------------
                // Elements allowed ONLY WITHIN segments
                //------------------------------------------------------
                case SEGMENT_CLOSE:
                    // assert(isSegment);
                    // We're done parsing a segment.
                    break main;

                //------------------------------------------------------
                // Elements allowed ONLY OUTSIDE segments
                //------------------------------------------------------
                case CONTEXT_ANTE:
                    if (ante >= 0) {
                        syntaxError("Multiple ante contexts", rule, start);
                    }
                    ante = buf.length();
                    break;
                case CONTEXT_POST:
                    if (post >= 0) {
                        syntaxError("Multiple post contexts", rule, start);
                    }
                    post = buf.length();
                    break;
                case CURSOR_POS:
                    if (cursor >= 0) {
                        syntaxError("Multiple cursors", rule, start);
                    }
                    cursor = buf.length();
                    break;
                case CURSOR_OFFSET:
                    if (cursorOffset < 0) {
                        if (buf.length() > 0) {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                        --cursorOffset;
                    } else if (cursorOffset > 0) {
                        if (buf.length() != cursorOffsetPos || cursor >= 0) {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                        ++cursorOffset;
                    } else {
                        if (cursor == 0 && buf.length() == 0) {
                            cursorOffset = -1;
                        } else if (cursor < 0) {
                            cursorOffsetPos = buf.length();
                            cursorOffset = 1;
                        } else {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                    }
                    break;

                //------------------------------------------------------
                // Non-special characters
                //------------------------------------------------------
                default:
                    // Disallow unquoted characters other than [0-9A-Za-z]
                    // in the printable ASCII range.  These characters are
                    // reserved for possible future use.
                    if (c >= 0x0021 && c <= 0x007E &&
                        !((c >= '0' && c <= '9') ||
                          (c >= 'A' && c <= 'Z') ||
                          (c >= 'a' && c <= 'z'))) {
                        syntaxError("Unquoted " + c, rule, start);
                    }
                    buf.append(c);
                    break;
                }
            }
            return pos;
        }

        /**
         * Remove context.
         */
        void removeContext() {
            text = text.substring(ante < 0 ? 0 : ante,
                                  post < 0 ? text.length() : post);
            ante = post = -1;
            anchorStart = anchorEnd = false;
        }

        /**
         * Return true if this half looks like valid output, that is, does not
         * contain quantifiers or other special input-only elements.
         */
        public boolean isValidOutput(TransliteratorParser parser) {
            for (int i=0; i<text.length(); ) {
                int c = UTF16.charAt(text, i);
                i += UTF16.getCharCount(c);
                if (!parser.parseData.isReplacer(c)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Return true if this half looks like valid input, that is, does not
         * contain functions or other special output-only elements.
         */
        public boolean isValidInput(TransliteratorParser parser) {
            for (int i=0; i<text.length(); ) {
                int c = UTF16.charAt(text, i);
                i += UTF16.getCharCount(c);
                if (!parser.parseData.isMatcher(c)) {
                    return false;
                }
            }
            return true;
        }
    }

    //----------------------------------------------------------------------
    // PUBLIC methods
    //----------------------------------------------------------------------

    /**
     * Constructor.
     */
    public TransliteratorParser() {
    }

    /**
     * Parse a set of rules.  After the parse completes, examine the public
     * data members for results.
     */
    public void parse(String rules, int dir) {
        parseRules(new RuleArray(new String[] { rules }), dir);
    }

    /*
     * Parse a set of rules.  After the parse completes, examine the public
     * data members for results.
     */
/*    public void parse(ResourceReader rules, int direction) {
        parseRules(new RuleReader(rules), direction);
    }*/

    //----------------------------------------------------------------------
    // PRIVATE methods
    //----------------------------------------------------------------------

    /**
     * Parse an array of zero or more rules.  The strings in the array are
     * treated as if they were concatenated together, with rule terminators
     * inserted between array elements if not present already.
     *
     * Any previous rules are discarded.  Typically this method is called exactly
     * once, during construction.
     *
     * The member this.data will be set to null if there are no rules.
     *
     * @exception IllegalIcuArgumentException if there is a syntax error in the
     * rules
     */
    void parseRules(RuleBody ruleArray, int dir) {
        boolean parsingIDs = true;
        int ruleCount = 0;

        dataVector = new ArrayList<Data>();
        idBlockVector = new ArrayList<String>();
        curData = null;
        direction = dir;
        compoundFilter = null;
        variablesVector = new ArrayList<Object>();
        variableNames = new HashMap<String, char[]>();
        parseData = new ParseData();

        List<RuntimeException> errors = new ArrayList<RuntimeException>();
        int errorCount = 0;

        ruleArray.reset();

        StringBuilder idBlockResult = new StringBuilder();

        // The compound filter offset is an index into idBlockResult.
        // If it is 0, then the compound filter occurred at the start,
        // and it is the offset to the _start_ of the compound filter
        // pattern.  Otherwise it is the offset to the _limit_ of the
        // compound filter pattern within idBlockResult.
        this.compoundFilter = null;
        int compoundFilterOffset = -1;

    main:
        for (;;) {
            String rule = ruleArray.nextLine();
            if (rule == null) {
                break;
            }
            int pos = 0;
            int limit = rule.length();
            while (pos < limit) {
                char c = rule.charAt(pos++);
                if (PatternProps.isWhiteSpace(c)) {
                    continue;
                }
                // Skip lines starting with the comment character
                if (c == RULE_COMMENT_CHAR) {
                    pos = rule.indexOf("\n", pos) + 1;
                    if (pos == 0) {
                        break; // No "\n" found; rest of rule is a commnet
                    }
                    continue; // Either fall out or restart with next line
                }

                // skip empty rules
                if (c == END_OF_RULE)
                    continue;

                // Often a rule file contains multiple errors.  It's
                // convenient to the rule author if these are all reported
                // at once.  We keep parsing rules even after a failure, up
                // to a specified limit, and report all errors at once.
                try {
                    ++ruleCount;

                    // We've found the start of a rule or ID.  c is its first
                    // character, and pos points past c.
                    --pos;
                    // Look for an ID token.  Must have at least ID_TOKEN_LEN + 1
                    // chars left.
                    if ((pos + ID_TOKEN_LEN + 1) <= limit &&
                            rule.regionMatches(pos, ID_TOKEN, 0, ID_TOKEN_LEN)) {
                        pos += ID_TOKEN_LEN;
                        c = rule.charAt(pos);
                        while (PatternProps.isWhiteSpace(c) && pos < limit) {
                            ++pos;
                            c = rule.charAt(pos);
                        }
                        int[] p = new int[] { pos };

                        if (!parsingIDs) {
                            if (curData != null) {
                                if (direction == Transliterator.FORWARD)
                                    dataVector.add(curData);
                                else
                                    dataVector.add(0, curData);
                                curData = null;
                            }
                            parsingIDs = true;
                        }

                        TransliteratorIDParser.SingleID id =
                            TransliteratorIDParser.parseSingleID(
                                          rule, p, direction);
                        if (p[0] != pos && Utility.parseChar(rule, p, END_OF_RULE)) {
                            // Successful ::ID parse.

                            if (direction == Transliterator.FORWARD) {
                                idBlockResult.append(id.canonID).append(END_OF_RULE);
                            } else {
                                idBlockResult.insert(0, id.canonID + END_OF_RULE);
                            }

                        } else {
                            // Couldn't parse an ID.  Try to parse a global filter
                            int[] withParens = new int[] { -1 };
                            UnicodeSet f = TransliteratorIDParser.parseGlobalFilter(rule, p, direction, withParens, null);
                            if (f != null && Utility.parseChar(rule, p, END_OF_RULE)) {
                                if ((direction == Transliterator.FORWARD) ==
                                    (withParens[0] == 0)) {
                                    if (compoundFilter != null) {
                                        // Multiple compound filters
                                        syntaxError("Multiple global filters", rule, pos);
                                    }
                                    compoundFilter = f;
                                    compoundFilterOffset = ruleCount;
                               }
                            } else {
                                // Invalid ::id
                                // Can be parsed as neither an ID nor a global filter
                                syntaxError("Invalid ::ID", rule, pos);
                            }
                        }

                        pos = p[0];
                    } else {
                        if (parsingIDs) {
                            if (direction == Transliterator.FORWARD)
                                idBlockVector.add(idBlockResult.toString());
                            else
                                idBlockVector.add(0, idBlockResult.toString());
                            idBlockResult.delete(0, idBlockResult.length());
                            parsingIDs = false;
                            curData = new RuleBasedTransliterator.Data();

                            // By default, rules use part of the private use area
                            // E000..F8FF for variables and other stand-ins.  Currently
                            // the range F000..F8FF is typically sufficient.  The 'use
                            // variable range' pragma allows rule sets to modify this.
                            setVariableRange(0xF000, 0xF8FF);
                        }

                        if (resemblesPragma(rule, pos, limit)) {
                            int ppp = parsePragma(rule, pos, limit);
                            if (ppp < 0) {
                                syntaxError("Unrecognized pragma", rule, pos);
                            }
                            pos = ppp;
                        // Parse a rule
                        } else {
                            pos = parseRule(rule, pos, limit);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    if (errorCount == 30) {
                        IllegalIcuArgumentException icuEx = new IllegalIcuArgumentException("\nMore than 30 errors; further messages squelched");
                        icuEx.initCause(e);
                        errors.add(icuEx);
                        break main;
                    }
                    e.fillInStackTrace();
                    errors.add(e);
                    ++errorCount;
                    pos = ruleEnd(rule, pos, limit) + 1; // +1 advances past ';'
                }
            }
        }
        if (parsingIDs && idBlockResult.length() > 0) {
            if (direction == Transliterator.FORWARD)
                idBlockVector.add(idBlockResult.toString());
            else
                idBlockVector.add(0, idBlockResult.toString());
        }
        else if (!parsingIDs && curData != null) {
            if (direction == Transliterator.FORWARD)
                dataVector.add(curData);
            else
                dataVector.add(0, curData);
        }

        // Convert the set vector to an array
        for (int i = 0; i < dataVector.size(); i++) {
            Data data = dataVector.get(i);
            data.variables = new Object[variablesVector.size()];
            variablesVector.toArray(data.variables);
            data.variableNames = new HashMap<String, char[]>();
            data.variableNames.putAll(variableNames);
        }
        variablesVector = null;

        // Do more syntax checking and index the rules
        try {
            if (compoundFilter != null) {
                if ((direction == Transliterator.FORWARD &&
                     compoundFilterOffset != 1) ||
                    (direction == Transliterator.REVERSE &&
                     compoundFilterOffset != ruleCount)) {
                    throw new IllegalIcuArgumentException("Compound filters misplaced");
                }
            }

            for (int i = 0; i < dataVector.size(); i++) {
                Data data = dataVector.get(i);
                data.ruleSet.freeze();
            }

            if (idBlockVector.size() == 1 && (idBlockVector.get(0)).length() == 0)
                idBlockVector.remove(0);

        } catch (IllegalArgumentException e) {
            e.fillInStackTrace();
            errors.add(e);
        }

        if (errors.size() != 0) {
            for (int i = errors.size()-1; i > 0; --i) {
                RuntimeException previous = errors.get(i-1);
                while (previous.getCause() != null) {
                    previous = (RuntimeException) previous.getCause(); // chain specially
                }
                previous.initCause(errors.get(i));
            }
            throw errors.get(0);
            // if initCause not supported: throw new IllegalArgumentException(errors.toString());
        }
    }

    /**
     * MAIN PARSER.  Parse the next rule in the given rule string, starting
     * at pos.  Return the index after the last character parsed.  Do not
     * parse characters at or after limit.
     *
     * Important:  The character at pos must be a non-whitespace character
     * that is not the comment character.
     *
     * This method handles quoting, escaping, and whitespace removal.  It
     * parses the end-of-rule character.  It recognizes context and cursor
     * indicators.  Once it does a lexical breakdown of the rule at pos, it
     * creates a rule object and adds it to our rule list.
     *
     * This method is tightly coupled to the inner class RuleHalf.
     */
    private int parseRule(String rule, int pos, int limit) {
        // Locate the left side, operator, and right side
        int start = pos;
        char operator = 0;

        // Set up segments data
        segmentStandins = new StringBuffer();
        segmentObjects = new ArrayList<StringMatcher>();

        RuleHalf left  = new RuleHalf();
        RuleHalf right = new RuleHalf();

        undefinedVariableName = null;
        pos = left.parse(rule, pos, limit, this);

        if (pos == limit ||
            OPERATORS.indexOf(operator = rule.charAt(--pos)) < 0) {
            syntaxError("No operator pos=" + pos, rule, start);
        }
        ++pos;

        // Found an operator char.  Check for forward-reverse operator.
        if (operator == REVERSE_RULE_OP &&
            (pos < limit && rule.charAt(pos) == FORWARD_RULE_OP)) {
            ++pos;
            operator = FWDREV_RULE_OP;
        }

        // Translate alternate op characters.
        switch (operator) {
        case ALT_FORWARD_RULE_OP:
            operator = FORWARD_RULE_OP;
            break;
        case ALT_REVERSE_RULE_OP:
            operator = REVERSE_RULE_OP;
            break;
        case ALT_FWDREV_RULE_OP:
            operator = FWDREV_RULE_OP;
            break;
        }

        pos = right.parse(rule, pos, limit, this);

        if (pos < limit) {
            if (rule.charAt(--pos) == END_OF_RULE) {
                ++pos;
            } else {
                // RuleHalf parser must have terminated at an operator
                syntaxError("Unquoted operator", rule, start);
            }
        }

        if (operator == VARIABLE_DEF_OP) {
            // LHS is the name.  RHS is a single character, either a literal
            // or a set (already parsed).  If RHS is longer than one
            // character, it is either a multi-character string, or multiple
            // sets, or a mixture of chars and sets -- syntax error.

            // We expect to see a single undefined variable (the one being
            // defined).
            if (undefinedVariableName == null) {
                syntaxError("Missing '$' or duplicate definition", rule, start);
            }
            if (left.text.length() != 1 || left.text.charAt(0) != variableLimit) {
                syntaxError("Malformed LHS", rule, start);
            }
            if (left.anchorStart || left.anchorEnd ||
                right.anchorStart || right.anchorEnd) {
                syntaxError("Malformed variable def", rule, start);
            }
            // We allow anything on the right, including an empty string.
            int n = right.text.length();
            char[] value = new char[n];
            right.text.getChars(0, n, value, 0);
            variableNames.put(undefinedVariableName, value);

            ++variableLimit;
            return pos;
        }

        // If this is not a variable definition rule, we shouldn't have
        // any undefined variable names.
        if (undefinedVariableName != null) {
            syntaxError("Undefined variable $" + undefinedVariableName,
                        rule, start);
        }

        // Verify segments
        if (segmentStandins.length() > segmentObjects.size()) {
            syntaxError("Undefined segment reference", rule, start);
        }
        for (int i=0; i<segmentStandins.length(); ++i) {
            if (segmentStandins.charAt(i) == 0) {
                syntaxError("Internal error", rule, start); // will never happen
            }
        }
        for (int i=0; i<segmentObjects.size(); ++i) {
            if (segmentObjects.get(i) == null) {
                syntaxError("Internal error", rule, start); // will never happen
            }
        }

        // If the direction we want doesn't match the rule
        // direction, do nothing.
        if (operator != FWDREV_RULE_OP &&
            ((direction == Transliterator.FORWARD) != (operator == FORWARD_RULE_OP))) {
            return pos;
        }

        // Transform the rule into a forward rule by swapping the
        // sides if necessary.
        if (direction == Transliterator.REVERSE) {
            RuleHalf temp = left;
            left = right;
            right = temp;
        }

        // Remove non-applicable elements in forward-reverse
        // rules.  Bidirectional rules ignore elements that do not
        // apply.
        if (operator == FWDREV_RULE_OP) {
            right.removeContext();
            left.cursor = -1;
            left.cursorOffset = 0;
        }

        // Normalize context
        if (left.ante < 0) {
            left.ante = 0;
        }
        if (left.post < 0) {
            left.post = left.text.length();
        }

        // Context is only allowed on the input side.  Cursors are only
        // allowed on the output side.  Segment delimiters can only appear
        // on the left, and references on the right.  Cursor offset
        // cannot appear without an explicit cursor.  Cursor offset
        // cannot place the cursor outside the limits of the context.
        // Anchors are only allowed on the input side.
        if (right.ante >= 0 || right.post >= 0 || left.cursor >= 0 ||
            (right.cursorOffset != 0 && right.cursor < 0) ||
            // - The following two checks were used to ensure that the
            // - the cursor offset stayed within the ante- or postcontext.
            // - However, with the addition of quantifiers, we have to
            // - allow arbitrary cursor offsets and do runtime checking.
            //(right.cursorOffset > (left.text.length() - left.post)) ||
            //(-right.cursorOffset > left.ante) ||
            right.anchorStart || right.anchorEnd ||
            !left.isValidInput(this) || !right.isValidOutput(this) ||
            left.ante > left.post) {
            syntaxError("Malformed rule", rule, start);
        }

        // Flatten segment objects vector to an array
        UnicodeMatcher[] segmentsArray = null;
        if (segmentObjects.size() > 0) {
            segmentsArray = new UnicodeMatcher[segmentObjects.size()];
            segmentObjects.toArray(segmentsArray);
        }

        curData.ruleSet.addRule(new TransliterationRule(
                                     left.text, left.ante, left.post,
                                     right.text, right.cursor, right.cursorOffset,
                                     segmentsArray,
                                     left.anchorStart, left.anchorEnd,
                                     curData));

        return pos;
    }

    /**
     * Set the variable range to [start, end] (inclusive).
     */
    private void setVariableRange(int start, int end) {
        if (start > end || start < 0 || end > 0xFFFF) {
            throw new IllegalIcuArgumentException("Invalid variable range " + start + ", " + end);
        }

        curData.variablesBase = (char) start; // first private use

        if (dataVector.size() == 0) {
            variableNext = (char) start;
            variableLimit = (char) (end + 1);
        }
    }

    /**
     * Assert that the given character is NOT within the variable range.
     * If it is, signal an error.  This is neccesary to ensure that the
     * variable range does not overlap characters used in a rule.
     */
    private void checkVariableRange(int ch, String rule, int start) {
        if (ch >= curData.variablesBase && ch < variableLimit) {
            syntaxError("Variable range character in rule", rule, start);
        }
    }

    // (The following method is part of an unimplemented feature.
    // Remove this clover pragma after the feature is implemented.
    // 2003-06-11 ICU 2.6 Alan)
    ///CLOVER:OFF
    /**
     * Set the maximum backup to 'backup', in response to a pragma
     * statement.
     */
    private void pragmaMaximumBackup(int backup) {
        //TODO Finish
        throw new IllegalIcuArgumentException("use maximum backup pragma not implemented yet");
    }
    ///CLOVER:ON

    // (The following method is part of an unimplemented feature.
    // Remove this clover pragma after the feature is implemented.
    // 2003-06-11 ICU 2.6 Alan)
    ///CLOVER:OFF
    /**
     * Begin normalizing all rules using the given mode, in response
     * to a pragma statement.
     */
    private void pragmaNormalizeRules(Normalizer.Mode mode) {
        //TODO Finish
        throw new IllegalIcuArgumentException("use normalize rules pragma not implemented yet");
    }
    ///CLOVER:ON

    /**
     * Return true if the given rule looks like a pragma.
     * @param pos offset to the first non-whitespace character
     * of the rule.
     * @param limit pointer past the last character of the rule.
     */
    static boolean resemblesPragma(String rule, int pos, int limit) {
        // Must start with /use\s/i
        return Utility.parsePattern(rule, pos, limit, "use ", null) >= 0;
    }

    /**
     * Parse a pragma.  This method assumes resemblesPragma() has
     * already returned true.
     * @param pos offset to the first non-whitespace character
     * of the rule.
     * @param limit pointer past the last character of the rule.
     * @return the position index after the final ';' of the pragma,
     * or -1 on failure.
     */
    private int parsePragma(String rule, int pos, int limit) {
        int[] array = new int[2];

        // resemblesPragma() has already returned true, so we
        // know that pos points to /use\s/i; we can skip 4 characters
        // immediately
        pos += 4;

        // Here are the pragmas we recognize:
        // use variable range 0xE000 0xEFFF;
        // use maximum backup 16;
        // use nfd rules;
        int p = Utility.parsePattern(rule, pos, limit, "~variable range # #~;", array);
        if (p >= 0) {
            setVariableRange(array[0], array[1]);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~maximum backup #~;", array);
        if (p >= 0) {
            pragmaMaximumBackup(array[0]);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~nfd rules~;", null);
        if (p >= 0) {
            pragmaNormalizeRules(Normalizer.NFD);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~nfc rules~;", null);
        if (p >= 0) {
            pragmaNormalizeRules(Normalizer.NFC);
            return p;
        }

        // Syntax error: unable to parse pragma
        return -1;
    }

    /**
     * Throw an exception indicating a syntax error.  Search the rule string
     * for the probable end of the rule.  Of course, if the error is that
     * the end of rule marker is missing, then the rule end will not be found.
     * In any case the rule start will be correctly reported.
     * @param msg error description
     * @param rule pattern string
     * @param start position of first character of current rule
     */
    static final void syntaxError(String msg, String rule, int start) {
        int end = ruleEnd(rule, start, rule.length());
        throw new IllegalIcuArgumentException(msg + " in \"" +
                                           Utility.escape(rule.substring(start, end)) + '"');
    }

    static final int ruleEnd(String rule, int start, int limit) {
        int end = Utility.quotedIndexOf(rule, start, limit, ";");
        if (end < 0) {
            end = limit;
        }
        return end;
    }

    /**
     * Parse a UnicodeSet out, store it, and return the stand-in character
     * used to represent it.
     */
    private final char parseSet(String rule, ParsePosition pos) {
        UnicodeSet set = new UnicodeSet(rule, pos, parseData);
        if (variableNext >= variableLimit) {
            throw new RuntimeException("Private use variables exhausted");
        }
        set.compact();
        return generateStandInFor(set);
    }

    /**
     * Generate and return a stand-in for a new UnicodeMatcher or UnicodeReplacer.
     * Store the object.
     */
    char generateStandInFor(Object obj) {
        // assert(obj != null);

        // Look up previous stand-in, if any.  This is a short list
        // (typical n is 0, 1, or 2); linear search is optimal.
        for (int i=0; i<variablesVector.size(); ++i) {
            if (variablesVector.get(i) == obj) { // [sic] pointer comparison
                return (char) (curData.variablesBase + i);
            }
        }

        if (variableNext >= variableLimit) {
            throw new RuntimeException("Variable range exhausted");
        }
        variablesVector.add(obj);
        return variableNext++;
    }

    /**
     * Return the standin for segment seg (1-based).
     */
    public char getSegmentStandin(int seg) {
        if (segmentStandins.length() < seg) {
            segmentStandins.setLength(seg);
        }
        char c = segmentStandins.charAt(seg-1);
        if (c == 0) {
            if (variableNext >= variableLimit) {
                throw new RuntimeException("Variable range exhausted");
            }
            c = variableNext++;
            // Set a placeholder in the master variables vector that will be
            // filled in later by setSegmentObject().  We know that we will get
            // called first because setSegmentObject() will call us.
            variablesVector.add(null);
            segmentStandins.setCharAt(seg-1, c);
        }
        return c;
    }

    /**
     * Set the object for segment seg (1-based).
     */
    public void setSegmentObject(int seg, StringMatcher obj) {
        // Since we call parseSection() recursively, nested
        // segments will result in segment i+1 getting parsed
        // and stored before segment i; be careful with the
        // vector handling here.
        while (segmentObjects.size() < seg) {
            segmentObjects.add(null);
        }
        int index = getSegmentStandin(seg) - curData.variablesBase;
        if (segmentObjects.get(seg-1) != null ||
            variablesVector.get(index) != null) {
            throw new RuntimeException(); // should never happen
        }
        segmentObjects.set(seg-1, obj);
        variablesVector.set(index, obj);
    }

    /**
     * Return the stand-in for the dot set.  It is allocated the first
     * time and reused thereafter.
     */
    char getDotStandIn() {
        if (dotStandIn == -1) {
            dotStandIn = generateStandInFor(new UnicodeSet(DOT_SET));
        }
        return (char) dotStandIn;
    }

    /**
     * Append the value of the given variable name to the given
     * StringBuffer.
     * @exception IllegalIcuArgumentException if the name is unknown.
     */
    private void appendVariableDef(String name, StringBuffer buf) {
        char[] ch = variableNames.get(name);
        if (ch == null) {
            // We allow one undefined variable so that variable definition
            // statements work.  For the first undefined variable we return
            // the special placeholder variableLimit-1, and save the variable
            // name.
            if (undefinedVariableName == null) {
                undefinedVariableName = name;
                if (variableNext >= variableLimit) {
                    throw new RuntimeException("Private use variables exhausted");
                }
                buf.append(--variableLimit);
            } else {
                throw new IllegalIcuArgumentException("Undefined variable $"
                                                   + name);
            }
        } else {
            buf.append(ch);
        }
    }
}

//eof
