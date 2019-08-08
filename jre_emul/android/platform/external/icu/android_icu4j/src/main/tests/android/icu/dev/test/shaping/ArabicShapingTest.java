/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2009, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.shaping;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.text.ArabicShaping;
import android.icu.text.ArabicShapingException;

/**
 * Interactive test for Arabic shaping.
 * Invoke from a command line passing args and strings.  Use '-help' to see description of arguments.
 */
// TODO(junit): wasn't running before - needs to be fixed
public class ArabicShapingTest{
    private static final int COPY = 0;
    private static final int INPLACE = 1;
    private static final int STRING = 2;

    // TODO(junit): marked with a test to keep from failing during ant run
    @Ignore
    @Test
    public void dummyTest() {
    }
    
    public static final void main(String[] args) {
        int testtype = COPY;
        int options = 0;
        int ss = 0;
        int sl = -1;
        int ds = 0;
        int dl = -1;
        String text = "$22.4 test 123 \ufef6\u0644\u0622 456 \u0664\u0665\u0666!";

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                String opt = arg.substring(1);
                String val = opt;
                int index = arg.indexOf(':');
                if (index != -1) {
                    opt = opt.substring(0, Math.min(index, 3));
                    val = arg.substring(index + 1);
                }
                
                if (opt.equalsIgnoreCase("len")) {
                    options &= ~ArabicShaping.LENGTH_MASK;
                    if (val.equalsIgnoreCase("gs")) {
                        options |= ArabicShaping.LENGTH_GROW_SHRINK;
                    } else if (val.equalsIgnoreCase("sn")) {
                        options |= ArabicShaping.LENGTH_FIXED_SPACES_NEAR;
                    } else if (val.equalsIgnoreCase("se")) {
                        options |= ArabicShaping.LENGTH_FIXED_SPACES_AT_END;
                    } else if (val.equalsIgnoreCase("sb")) {
                        options |= ArabicShaping.LENGTH_FIXED_SPACES_AT_BEGINNING;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("dir")) {
                    options &= ~ArabicShaping.TEXT_DIRECTION_MASK;
                    if (val.equalsIgnoreCase("log")) {
                        options |= ArabicShaping.TEXT_DIRECTION_LOGICAL;
                    } else if (val.equalsIgnoreCase("vis")) {
                        options |= ArabicShaping.TEXT_DIRECTION_VISUAL_LTR;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("let")) {
                    options &= ~ArabicShaping.LETTERS_MASK;
                    if (val.equalsIgnoreCase("no")) {
                        options |= ArabicShaping.LETTERS_NOOP;
                    } else if (val.equalsIgnoreCase("sh")) {
                        options |= ArabicShaping.LETTERS_SHAPE;
                    } else if (val.equalsIgnoreCase("un")) {
                        options |= ArabicShaping.LETTERS_UNSHAPE;
                    } else if (val.equalsIgnoreCase("ta")) {
                        options |= ArabicShaping.LETTERS_SHAPE_TASHKEEL_ISOLATED;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("dig")) {
                    options &= ~ArabicShaping.DIGITS_MASK;
                    if (val.equalsIgnoreCase("no")) {
                        options |= ArabicShaping.DIGITS_NOOP;
                    } else if (val.equalsIgnoreCase("ea")) {
                        options |= ArabicShaping.DIGITS_EN2AN;
                    } else if (val.equalsIgnoreCase("ae")) {
                        options |= ArabicShaping.DIGITS_AN2EN;
                    } else if (val.equalsIgnoreCase("lr")) {
                        options |= ArabicShaping.DIGITS_EN2AN_INIT_LR;
                    } else if (val.equalsIgnoreCase("al")) {
                        options |= ArabicShaping.DIGITS_EN2AN_INIT_AL;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("typ")) {
                    options &= ~ArabicShaping.DIGIT_TYPE_MASK;
                    if (val.equalsIgnoreCase("an")) {
                        options |= ArabicShaping.DIGIT_TYPE_AN;
                    } else if (val.equalsIgnoreCase("ex")) {
                        options |= ArabicShaping.DIGIT_TYPE_AN_EXTENDED;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("dst")) {
                    try {
                        ds = Integer.parseInt(val);
                    }
                    catch (Exception e) {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("dln")) {
                    try {
                        dl = Integer.parseInt(val);
                    }
                    catch (Exception e) {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("sst")) {
                    try {
                        ss = Integer.parseInt(val);
                    }
                    catch (Exception e) {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("sln")) {
                    try {
                        sl = Integer.parseInt(val);
                    }
                    catch (Exception e) {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("tes")) {
                    if (val.equalsIgnoreCase("cp")) {
                        testtype = COPY;
                    } else if (val.equalsIgnoreCase("ip")) {
                        testtype = INPLACE;
                    } else if (val.equalsIgnoreCase("st")) {
                        testtype = STRING;
                    } else {
                        throwValError(opt, val);
                    }
                } else if (opt.equalsIgnoreCase("help")) {
                    System.out.println(usage);
                } else {
                    throwOptError(opt);
                }
            } else {
                // assume text
                text = parseText(arg);
            }
        }

        if (sl < 0) {
            sl = text.length() - ss;
            System.out.println("sl defaulting to " + sl);
        }
        if (dl < 0) {
            dl = 2 * sl;
            System.out.println("dl defaulting to " + dl);
        }

        ArabicShaping shaper = new ArabicShaping(options);
        System.out.println("shaper: " + shaper);

        char[] src = text.toCharArray();
        System.out.println(" input: '" + escapedText(src, ss, sl) + "'");
        if (testtype != STRING) {
            System.out.println("start: " + ss + " length: " + sl + " total length: " + src.length);
        }

        int result = -1;
        char[] dest = null;

        try {
            switch (testtype) {
            case COPY:
                dest = new char[ds + dl];
                result = shaper.shape(src, ss, sl, dest, ds, dl);
                break;

            case INPLACE:
                shaper.shape(src, ss, sl);
                ds = ss;
                result = sl;
                dest = src;
                break;

            case STRING:
                dest = shaper.shape(text).toCharArray();
                ds = 0;
                result = dest.length;
                break;
            }

            System.out.println("output: '" + escapedText(dest, ds, result) + "'");
            System.out.println("length: " + result);
            if (ds != 0 || result != dest.length) {
                System.out.println("full output: '" + escapedText(dest, 0, dest.length) + "'");
            }
        }
        catch (ArabicShapingException e) {
            System.out.println("Caught ArabicShapingException");
            System.out.println(e);
        }
        catch (Exception e) {
            System.out.println("Caught Exception");
            System.out.println(e);
        }
    }

    private static void throwOptError(String opt) {
        throwUsageError("unknown option: " + opt);
    }

    private static void throwValError(String opt, String val) {
        throwUsageError("unknown value: " + val + " for option: " + opt);
    }

    private static void throwUsageError(String message) {
        StringBuffer buf = new StringBuffer("*** usage error ***\n");
        buf.append(message);
        buf.append("\n");
        buf.append(usage);
        throw new Error(buf.toString());
    }

    private static final String usage = 
        "Usage: [option]* [text]\n" +
        "  where option is in the format '-opt[:val]'\n" +
        "  options are:\n" +
        "    -len:[gs|sn|se|sb]    (length: grow/shrink, spaces near, spaces end, spaces beginning)\n" +
        "    -dir:[log|vis]        (direction: logical, visual)\n" +
        "    -let:[no|sh|un|ta]    (letters: noop, shape, unshape, tashkeel)\n" +
        // "    -let:[no|sh|un]       (letters: noop, shape, unshape)\n" +
        "    -dig:[no|ea|ae|lr|al] (digits: noop, en2an, an2en, en2an_lr, en2an_al)\n" +
        "    -typ:[an|ex]          (digit type: arabic, arabic extended)\n" +
        "    -dst:#                (dest start: [integer])\n" +
        "    -dln:#                (dest length (max size): [integer])\n" +
        "    -sst:#                (source start: [integer])\n" +
        "    -sln:#                (source length: [integer])\n" +
        "    -tes:[cp|ip|st]       (test type: copy, in place, string)\n" +
        "    -help                 (print this help message)\n" +
        "  text can contain unicode escape values in the format '\\uXXXX' only\n";
        
    private static String escapedText(char[] text, int start, int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = start, e = start + length; i < e; ++i) {
            char ch = text[i];
            if (ch < 0x20 || ch > 0x7e) {
                buf.append("\\u");
                if (ch < 0x1000) {
                    buf.append('0');
                }
                if (ch < 0x100) {
                    buf.append('0');
                }
                if (ch < 0x10) {
                    buf.append('0');
                }
                buf.append(Integer.toHexString(ch));
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    private static String parseText(String text) {
        // process unicode escapes (only)
        StringBuffer buf = new StringBuffer();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char ch = chars[i];
            if (ch == '\\') {
                if ((i < chars.length - 1) &&
                    (chars[i+1] == 'u')) {
                    int val = Integer.parseInt(text.substring(i+2, i+6), 16);
                    buf.append((char)val);
                    i += 5;
                } else {
                    buf.append('\\');
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}
