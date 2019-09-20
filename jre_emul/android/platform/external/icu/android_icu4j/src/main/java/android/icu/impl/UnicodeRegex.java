/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2015, Google, International Business Machines Corporation
 * and others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import android.icu.text.StringTransform;
import android.icu.text.SymbolTable;
import android.icu.text.UnicodeSet;
import android.icu.util.Freezable;

/**
 * Contains utilities to supplement the JDK Regex, since it doesn't handle
 * Unicode well.
 *
 * <p>TODO: Move to android.icu.dev.somewhere.
 * 2015-sep-03: This is used there, and also in CLDR and in UnicodeTools.
 *
 * @author markdavis
 * @hide Only a subset of ICU is exposed in Android
 */
public class UnicodeRegex implements Cloneable, Freezable<UnicodeRegex>, StringTransform {
    // Note: we don't currently have any state, but intend to in the future,
    // particularly for the regex style supported.

    private SymbolTable symbolTable;

    /**
     * Set the symbol table for internal processing
     * @hide draft / provisional / internal are hidden on Android
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Get the symbol table for internal processing
     * @hide draft / provisional / internal are hidden on Android
     */
    public UnicodeRegex setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        return this;
    }

    /**
     * Adds full Unicode property support, with the latest version of Unicode,
     * to Java Regex, bringing it up to Level 1 (see
     * http://www.unicode.org/reports/tr18/). It does this by preprocessing the
     * regex pattern string and interpreting the character classes (\p{...},
     * \P{...}, [...]) according to their syntax and meaning in UnicodeSet. With
     * this utility, Java regex expressions can be updated to work with the
     * latest version of Unicode, and with all Unicode properties. Note that the
     * UnicodeSet syntax has not yet, however, been updated to be completely
     * consistent with Java regex, so be careful of the differences.
     * <p>Not thread-safe; create a separate copy for different threads.
     * <p>In the future, we may extend this to support other regex packages.
     *
     * @regex A modified Java regex pattern, as in the input to
     *        Pattern.compile(), except that all "character classes" are
     *        processed as if they were UnicodeSet patterns. Example:
     *        "abc[:bc=N:]. See UnicodeSet for the differences in syntax.
     * @return A processed Java regex pattern, suitable for input to
     *         Pattern.compile().
     */
    @Override
    public String transform(String regex) {
        StringBuilder result = new StringBuilder();
        UnicodeSet temp = new UnicodeSet();
        ParsePosition pos = new ParsePosition(0);
        int state = 0; // 1 = after \

        // We add each character unmodified to the output, unless we have a
        // UnicodeSet. Note that we don't worry about supplementary characters,
        // since none of the syntax uses them.

        for (int i = 0; i < regex.length(); ++i) {
            // look for UnicodeSets, allowing for quoting with \ and \Q
            char ch = regex.charAt(i);
            switch (state) {
            case 0: // we only care about \, and '['.
                if (ch == '\\') {
                    if (UnicodeSet.resemblesPattern(regex, i)) {
                        // should only happen with \p
                        i = processSet(regex, i, result, temp, pos);
                        continue;
                    }
                    state = 1;
                } else if (ch == '[') {
                    // if we have what looks like a UnicodeSet
                    if (UnicodeSet.resemblesPattern(regex, i)) {
                        i = processSet(regex, i, result, temp, pos);
                        continue;
                    }
                }
                break;

            case 1: // we are after a \
                if (ch == 'Q') {
                    state = 1;
                } else {
                    state = 0;
                }
                break;

            case 2: // we are in a \Q...
                if (ch == '\\') {
                    state = 3;
                }
                break;

            case 3: // we are in at \Q...\
                if (ch == 'E') {
                    state = 0;
                }
                state = 2;
                break;
            }
            result.append(ch);
        }
        return result.toString();
    }

    /**
     * Convenience static function, using standard parameters.
     * @param regex as in process()
     * @return processed regex pattern, as in process()
     */
    public static String fix(String regex) {
        return STANDARD.transform(regex);
    }

    /**
     * Compile a regex string, after processing by fix(...).
     *
     * @param regex Raw regex pattern, as in fix(...).
     * @return Pattern
     */
    public static Pattern compile(String regex) {
        return Pattern.compile(STANDARD.transform(regex));
    }

    /**
     * Compile a regex string, after processing by fix(...).
     *
     * @param regex Raw regex pattern, as in fix(...).
     * @return Pattern
     */
    public static Pattern compile(String regex, int options) {
        return Pattern.compile(STANDARD.transform(regex), options);
    }

    /**
     * Compile a composed string from a set of BNF lines; see the List version for more information.
     *
     * @param bnfLines Series of BNF lines.
     * @return Pattern
     */
    public String compileBnf(String bnfLines) {
        return compileBnf(Arrays.asList(bnfLines.split("\\r\\n?|\\n")));
    }

    /**
     * Compile a composed string from a set of BNF lines, such as for composing a regex
     * expression. The lines can be in any order, but there must not be any
     * cycles. The result can be used as input for fix().
     * <p>
     * Example:
     * <pre>
     * uri = (?: (scheme) \\:)? (host) (?: \\? (query))? (?: \\u0023 (fragment))?;
     * scheme = reserved+;
     * host = // reserved+;
     * query = [\\=reserved]+;
     * fragment = reserved+;
     * reserved = [[:ascii:][:alphabetic:]];
     * </pre>
     * <p>
     * Caveats: at this point the parsing is simple; for example, # cannot be
     * quoted (use \\u0023); you can set it to null to disable.
     * The equality sign and a few others can be reset with
     * setBnfX().
     *
     * @param lines Series of lines that represent a BNF expression. The lines contain
     *          a series of statements that of the form x=y;. A statement can take
     *          multiple lines, but there can't be multiple statements on a line.
     *          A hash quotes to the end of the line.
     * @return Pattern
     */
    public String compileBnf(List<String> lines) {
        Map<String, String> variables = getVariables(lines);
        Set<String> unused = new LinkedHashSet<String>(variables.keySet());
        // brute force replacement; do twice to allow for different order
        // later on can optimize
        for (int i = 0; i < 2; ++i) {
            for (Entry<String, String> entry : variables.entrySet()) {
                String variable   = entry.getKey(),
                       definition = entry.getValue();

                for (Entry<String, String> entry2 : variables.entrySet()) {
                    String variable2 = entry2.getKey(),
                           definition2 = entry2.getValue();
                    if (variable.equals(variable2)) {
                        continue;
                    }
                    String altered2 = definition2.replace(variable, definition);
                    if (!altered2.equals(definition2)) {
                        unused.remove(variable);
                        variables.put(variable2, altered2);
//                        if (log != null) {
//                            try {
//                                log.append(variable2 + "=" + altered2 + ";");
//                            } catch (IOException e) {
//                                throw (IllegalArgumentException) new IllegalArgumentException().initCause(e);
//                            }
//                        }
                    }
                }
            }
        }
        if (unused.size() != 1) {
            throw new IllegalArgumentException("Not a single root: " + unused);
        }
        return variables.get(unused.iterator().next());
    }

    public String getBnfCommentString() {
        return bnfCommentString;
    }

    public void setBnfCommentString(String bnfCommentString) {
        this.bnfCommentString = bnfCommentString;
    }

    public String getBnfVariableInfix() {
        return bnfVariableInfix;
    }

    public void setBnfVariableInfix(String bnfVariableInfix) {
        this.bnfVariableInfix = bnfVariableInfix;
    }

    public String getBnfLineSeparator() {
        return bnfLineSeparator;
    }

    public void setBnfLineSeparator(String bnfLineSeparator) {
        this.bnfLineSeparator = bnfLineSeparator;
    }

    /**
     * Utility for loading lines from a file.
     * @param result The result of the appended lines.
     * @param file The file to have an input stream.
     * @param encoding if null, then UTF-8
     * @return filled list
     * @throws IOException If there were problems opening the file for input stream.
     */
    public static List<String> appendLines(List<String> result, String file, String encoding) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return appendLines(result, is, encoding);
        } finally {
            is.close();
        }
    }

    /**
     * Utility for loading lines from a UTF8 file.
     * @param result The result of the appended lines.
     * @param inputStream The input stream.
     * @param encoding if null, then UTF-8
     * @return filled list
     * @throws IOException  If there were problems opening the input stream for reading.
     */
    public static List<String> appendLines(List<String> result, InputStream inputStream, String encoding)
            throws UnsupportedEncodingException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, encoding == null ? "UTF-8" : encoding));
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            result.add(line);
        }
        return result;
    }



    /* (non-Javadoc)
     * @see android.icu.util.Freezable#cloneAsThawed()
     */
    @Override
    public UnicodeRegex cloneAsThawed() {
        // TODO Auto-generated method stub
        try {
            return (UnicodeRegex)clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(); // should never happen
        }
    }

    /* (non-Javadoc)
     * @see android.icu.util.Freezable#freeze()
     */
    @Override
    public UnicodeRegex freeze() {
        // no action needed now.
        return this;
    }

    /* (non-Javadoc)
     * @see android.icu.util.Freezable#isFrozen()
     */
    @Override
    public boolean isFrozen() {
        // at this point, always true
        return true;
    }

    // ===== PRIVATES =====

    private int processSet(String regex, int i, StringBuilder result, UnicodeSet temp, ParsePosition pos) {
        try {
            pos.setIndex(i);
            UnicodeSet x = temp.clear().applyPattern(regex, pos, symbolTable, 0);
            x.complement().complement(); // hack to fix toPattern
            result.append(x.toPattern(false));
            i = pos.getIndex() - 1; // allow for the loop increment
            return i;
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Error in " + regex).initCause(e);
        }
    }

    private static final UnicodeRegex STANDARD = new UnicodeRegex();
    private String bnfCommentString = "#";
    private String bnfVariableInfix = "=";
    private String bnfLineSeparator = "\n";
//    private Appendable log = null;

    private Comparator<Object> LongestFirst = new Comparator<Object>() {
        @Override
        public int compare(Object obj0, Object obj1) {
            String arg0 = obj0.toString();
            String arg1 = obj1.toString();
            int len0 = arg0.length();
            int len1 = arg1.length();
            if (len0 != len1) return len1 - len0;
            return arg0.compareTo(arg1);
        }
    };

    private Map<String, String> getVariables(List<String> lines) {
        Map<String, String> variables = new TreeMap<String, String>(LongestFirst);
        String variable = null;
        StringBuffer definition = new StringBuffer();
        int count = 0;
        for (String line : lines) {
            ++count;
            // remove initial bom, comments
            if (line.length() == 0) continue;
            if (line.charAt(0) == '\uFEFF') line = line.substring(1);

            if (bnfCommentString != null) {
                int hashPos = line.indexOf(bnfCommentString);
                if (hashPos >= 0) line = line.substring(0, hashPos);
            }
            String trimline = line.trim();
            if (trimline.length() == 0) continue;

            // String[] lineParts = line.split(";");
            String linePart = line; // lineParts[i]; // .trim().replace("\\s+", " ");
            if (linePart.trim().length() == 0) continue;
            boolean terminated = trimline.endsWith(";");
            if (terminated) {
                linePart = linePart.substring(0,linePart.lastIndexOf(';'));
            }
            int equalsPos = linePart.indexOf(bnfVariableInfix);
            if (equalsPos >= 0) {
                if (variable != null) {
                    throw new IllegalArgumentException("Missing ';' before " + count + ") " + line);
                }
                variable = linePart.substring(0,equalsPos).trim();
                if (variables.containsKey(variable)) {
                    throw new IllegalArgumentException("Duplicate variable definition in " + line);
                }
                definition.append(linePart.substring(equalsPos+1).trim());
            } else { // no equals, so
                if (variable == null) {
                    throw new IllegalArgumentException("Missing '=' at " + count + ") " + line);
                }
                definition.append(bnfLineSeparator).append(linePart);
            }
            // we are terminated if i is not at the end, or the line ends with a ;
            if (terminated) {
                variables.put(variable, definition.toString());
                variable = null; // signal we have no variable
                definition.setLength(0);
            }
        }
        if (variable != null) {
            throw new IllegalArgumentException("Missing ';' at end");
        }
        return variables;
    }
}
