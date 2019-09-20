/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2006-2009, Google, International Business Machines Corporation *
 * and others. All Rights Reserved.                                            *
 *******************************************************************************
 */
package android.icu.impl;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

/**
 * A simple parsing class for patterns and rules. Handles '...' quotations, \\uxxxx and \\Uxxxxxxxx, and symple syntax.
 * The '' (two quotes) is treated as a single quote, inside or outside a quote
 * <ul>
 * <li>Any ignorable characters are ignored in parsing.</li>
 * <li>Any syntax characters are broken into separate tokens</li>
 * <li>Quote characters can be specified: '...', "...", and \x </li>
 * <li>Other characters are treated as literals</li>
 * </ul>
 * @hide Only a subset of ICU is exposed in Android
 */
public class PatternTokenizer {
    // settings used in the interpretation of the pattern
    private UnicodeSet ignorableCharacters = new UnicodeSet();
    private UnicodeSet syntaxCharacters = new UnicodeSet();
    private UnicodeSet extraQuotingCharacters = new UnicodeSet();
    private UnicodeSet escapeCharacters = new UnicodeSet();
    private boolean usingSlash = false;
    private boolean usingQuote = false;
    
    // transient data, set when needed. Null it out for any changes in the above fields.
    private transient UnicodeSet needingQuoteCharacters = null;
    
    // data about the current pattern being parsed. start gets moved as we go along.
    private int start;
    private int limit;
    private String pattern;
    
    public UnicodeSet getIgnorableCharacters() {
        return (UnicodeSet) ignorableCharacters.clone();
    }
    /**
     * Sets the characters to be ignored in parsing, eg new UnicodeSet("[:pattern_whitespace:]");
     * @param ignorableCharacters Characters to be ignored.
     * @return A PatternTokenizer object in which characters are specified as ignored characters.
     */
    public PatternTokenizer setIgnorableCharacters(UnicodeSet ignorableCharacters) {
        this.ignorableCharacters = (UnicodeSet) ignorableCharacters.clone();
        needingQuoteCharacters = null;
        return this;
    }
    public UnicodeSet getSyntaxCharacters() {
        return (UnicodeSet) syntaxCharacters.clone();
    }
    public UnicodeSet getExtraQuotingCharacters() {
        return (UnicodeSet) extraQuotingCharacters.clone();
    }
    /**
     *  Sets the characters to be interpreted as syntax characters in parsing, eg new UnicodeSet("[:pattern_syntax:]")
     * @param syntaxCharacters Characters to be set as syntax characters.
     * @return A PatternTokenizer object in which characters are specified as syntax characters.
     */
    public PatternTokenizer setSyntaxCharacters(UnicodeSet syntaxCharacters) {
        this.syntaxCharacters = (UnicodeSet) syntaxCharacters.clone();
        needingQuoteCharacters = null;
        return this;
    }   
    /**
     *  Sets the extra characters to be quoted in literals
     * @param syntaxCharacters Characters to be set as extra quoting characters.
     * @return A PatternTokenizer object in which characters are specified as extra quoting characters.
     */
    public PatternTokenizer setExtraQuotingCharacters(UnicodeSet syntaxCharacters) {
        this.extraQuotingCharacters = (UnicodeSet) syntaxCharacters.clone();
        needingQuoteCharacters = null;
        return this;
    }   
    
    public UnicodeSet getEscapeCharacters() {
        return (UnicodeSet) escapeCharacters.clone();
    }
    /**
     * Set characters to be escaped in literals, in quoteLiteral and normalize, eg new UnicodeSet("[^\\u0020-\\u007E]");
     * @param escapeCharacters Characters to be set as escape characters.
     * @return A PatternTokenizer object in which characters are specified as escape characters.
     */
    public PatternTokenizer setEscapeCharacters(UnicodeSet escapeCharacters) {
        this.escapeCharacters = (UnicodeSet) escapeCharacters.clone();
        return this;
    }
    public boolean isUsingQuote() {
        return usingQuote;
    }
    public PatternTokenizer setUsingQuote(boolean usingQuote) {
        this.usingQuote = usingQuote;
        needingQuoteCharacters = null;
        return this;
    }
    public boolean isUsingSlash() {
        return usingSlash;
    }
    public PatternTokenizer setUsingSlash(boolean usingSlash) {
        this.usingSlash = usingSlash;
        needingQuoteCharacters = null;
        return this;
    }
    //    public UnicodeSet getQuoteCharacters() {
//  return (UnicodeSet) quoteCharacters.clone();
//  }
//  public PatternTokenizer setQuoteCharacters(UnicodeSet quoteCharacters) {
//  this.quoteCharacters = (UnicodeSet) quoteCharacters.clone();
//  needingQuoteCharacters = null;
//  return this;
//  }
    public int getLimit() {
        return limit;
    }
    public PatternTokenizer setLimit(int limit) {
        this.limit = limit;
        return this;
    }
    public int getStart() {
        return start;
    }
    public PatternTokenizer setStart(int start) {
        this.start = start;
        return this;
    }

    public PatternTokenizer setPattern(CharSequence pattern) {
        return setPattern(pattern.toString());
    }

    public PatternTokenizer setPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Inconsistent arguments");
        }
        this.start = 0;
        this.limit = pattern.length();
        this.pattern = pattern;
        return this;
    }

    public static final char SINGLE_QUOTE = '\'';
    public static final char BACK_SLASH = '\\';
    private static int NO_QUOTE = -1, IN_QUOTE = -2;

    public String quoteLiteral(CharSequence string) {
        return quoteLiteral(string.toString());
    }

    /**
     * Quote a literal string, using the available settings. Thus syntax characters, quote characters, and ignorable characters will be put into quotes.
     * @param string String passed to quote a literal string.
     * @return A string using the available settings will place syntax, quote, or ignorable characters into quotes.
     */
    public String quoteLiteral(String string) {
        if (needingQuoteCharacters == null) {
            needingQuoteCharacters = new UnicodeSet().addAll(syntaxCharacters).addAll(ignorableCharacters).addAll(extraQuotingCharacters); // .addAll(quoteCharacters)
            if (usingSlash) needingQuoteCharacters.add(BACK_SLASH);
            if (usingQuote) needingQuoteCharacters.add(SINGLE_QUOTE);
        }
        StringBuffer result = new StringBuffer();
        int quotedChar = NO_QUOTE;
        int cp;
        for (int i = 0; i < string.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(string, i);
            if (escapeCharacters.contains(cp)) {
                // we may have to fix up previous characters
                if (quotedChar == IN_QUOTE) {
                    result.append(SINGLE_QUOTE);
                    quotedChar = NO_QUOTE;
                }
                appendEscaped(result, cp);
                continue;
            }
            
            if (needingQuoteCharacters.contains(cp)) {
                // if we have already started a quote
                if (quotedChar == IN_QUOTE) {
                    UTF16.append(result, cp);
                    if (usingQuote && cp == SINGLE_QUOTE) { // double it
                        result.append(SINGLE_QUOTE);
                    }
                    continue;
                }
                // otherwise not already in quote
                if (usingSlash) {
                    result.append(BACK_SLASH);
                    UTF16.append(result, cp);
                    continue;
                }
                if (usingQuote) {
                    if (cp == SINGLE_QUOTE) { // double it and continue
                        result.append(SINGLE_QUOTE);
                        result.append(SINGLE_QUOTE);
                        continue;
                    }
                    result.append(SINGLE_QUOTE);
                    UTF16.append(result, cp);
                    quotedChar = IN_QUOTE;
                    continue;
                }
                // we have no choice but to use \\u or \\U
                appendEscaped(result, cp);
                continue;
            }
            // otherwise cp doesn't need quoting
            // we may have to fix up previous characters
            if (quotedChar == IN_QUOTE) {
                result.append(SINGLE_QUOTE);
                quotedChar = NO_QUOTE;
            }
            UTF16.append(result, cp);
        }
        // all done. 
        // we may have to fix up previous characters
        if (quotedChar == IN_QUOTE) {
            result.append(SINGLE_QUOTE);
        }
        return result.toString();
    }

    private void appendEscaped(StringBuffer result, int cp) {
        if (cp <= 0xFFFF) {
            result.append("\\u").append(Utility.hex(cp,4));
        } else {
            result.append("\\U").append(Utility.hex(cp,8));
        }
    }
    
    public String normalize() {
        int oldStart = start;
        StringBuffer result = new StringBuffer();
        StringBuffer buffer = new StringBuffer();
        while (true) {
            buffer.setLength(0);
            int status = next(buffer);
            if (status == DONE) {
                start = oldStart;
                return result.toString();
            }
            if (status != SYNTAX) {
                result.append(quoteLiteral(buffer));
            } else {
                result.append(buffer);
            }
        }
    }
    
    public static final int DONE = 0, SYNTAX = 1, LITERAL = 2, BROKEN_QUOTE = 3, BROKEN_ESCAPE = 4, UNKNOWN = 5;
    
    private static final int AFTER_QUOTE = -1, NONE = 0, START_QUOTE = 1, NORMAL_QUOTE = 2, SLASH_START = 3, HEX = 4;
    
    public int next(StringBuffer buffer) {
        if (start >= limit) return DONE;
        int status = UNKNOWN;
        int lastQuote = UNKNOWN;
        int quoteStatus = NONE;
        int hexCount = 0;
        int hexValue = 0;
        int cp;
        main:
            for (int i = start; i < limit; i += UTF16.getCharCount(cp)) {
                cp = UTF16.charAt(pattern, i);
                // if we are in a quote, then handle it.
                switch (quoteStatus) {
                case SLASH_START:
                    switch (cp) {
                    case 'u':
                        quoteStatus = HEX;
                        hexCount = 4;
                        hexValue = 0;
                        continue main;
                    case 'U': 
                        quoteStatus = HEX;
                        hexCount = 8;
                        hexValue = 0;
                        continue main;
                    default:
                        if (usingSlash) {
                            UTF16.append(buffer, cp);
                            quoteStatus = NONE;
                            continue main;
                        } else {
                            buffer.append(BACK_SLASH);
                            quoteStatus = NONE;
                        }
                    }
                    break; // fall through to NONE
                case HEX:
                    hexValue <<= 4;
                    hexValue += cp;
                    switch (cp) {
                    case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                        hexValue -= '0'; break;
                    case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                        hexValue -= 'a' - 10; break;
                    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                        hexValue -= 'A' - 10; break;
                    default:
                        start = i;
                    return BROKEN_ESCAPE;
                    }
                    --hexCount;
                    if (hexCount == 0) {
                        quoteStatus = NONE;
                        UTF16.append(buffer, hexValue);
                    }
                    continue main;
                case AFTER_QUOTE:
                    // see if we get another quote character
                    // if we just ended a quote BUT the following character is the lastQuote character, then we have a situation like '...''...', so we restart the quote
                    if (cp == lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = NORMAL_QUOTE;
                        continue main;
                    }
                    quoteStatus = NONE;
                    break; // fall through to NONE
                case START_QUOTE:
                    // if we are at the very start of a quote, and we hit another quote mark then we emit a literal quote character and end the quote
                    if (cp == lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = NONE; // get out of quote, with no trace remaining
                        continue;                            
                    }
                    // otherwise get into quote
                    UTF16.append(buffer, cp);
                    quoteStatus = NORMAL_QUOTE;
                    continue main;
                case NORMAL_QUOTE: 
                    if (cp == lastQuote) {
                        quoteStatus = AFTER_QUOTE; // get out of quote
                        continue main;
                    }
                    UTF16.append(buffer, cp);
                    continue main;
                }
                
                if (ignorableCharacters.contains(cp)) {
                    continue;
                }
                // do syntax characters
                if (syntaxCharacters.contains(cp)) {
                    if (status == UNKNOWN) {
                        UTF16.append(buffer, cp);
                        start = i + UTF16.getCharCount(cp);
                        return SYNTAX;
                    } else { // LITERAL, so back up and break
                        start = i;
                        return status;
                    }
                }
                // otherwise it is a literal; keep on going
                status = LITERAL;
                if (cp == BACK_SLASH) {
                    quoteStatus = SLASH_START;
                    continue;
                } else if (usingQuote && cp == SINGLE_QUOTE) {
                    lastQuote = cp;
                    quoteStatus = START_QUOTE;
                    continue;
                }
                // normal literals
                UTF16.append(buffer, cp);
            }
        // handle final cleanup
        start = limit;
        switch (quoteStatus) {
        case HEX:
            status = BROKEN_ESCAPE;
            break;
        case SLASH_START:
            if (usingSlash) {
                status = BROKEN_ESCAPE;
            } else {
                buffer.append(BACK_SLASH);
            }
            break;
        case START_QUOTE: case NORMAL_QUOTE:
            status = BROKEN_QUOTE;
            break;
        }
        return status;
    }
    
    
}
//eof
