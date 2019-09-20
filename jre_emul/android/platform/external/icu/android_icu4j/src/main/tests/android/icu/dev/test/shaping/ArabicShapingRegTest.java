/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2012, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.shaping;

import java.lang.reflect.Method;
import java.util.MissingResourceException;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.ArabicShaping;
import android.icu.text.ArabicShapingException;

/**
 * Regression test for Arabic shaping.
 */
public class ArabicShapingRegTest extends TestFmwk {

    /* constants copied from ArabicShaping for convenience */

    public static final int LENGTH_GROW_SHRINK = 0;
    public static final int LENGTH_FIXED_SPACES_NEAR = 1;
    public static final int LENGTH_FIXED_SPACES_AT_END = 2;
    public static final int LENGTH_FIXED_SPACES_AT_BEGINNING = 3;

    public static final int TEXT_DIRECTION_LOGICAL = 0;
    public static final int TEXT_DIRECTION_VISUAL_LTR = 4;

    public static final int LETTERS_NOOP = 0;
    public static final int LETTERS_SHAPE = 8;
    public static final int LETTERS_SHAPE_TASHKEEL_ISOLATED = 0x18;
    public static final int LETTERS_UNSHAPE = 0x10;

    public static final int DIGITS_NOOP = 0;
    public static final int DIGITS_EN2AN = 0x20;
    public static final int DIGITS_AN2EN = 0x40;
    public static final int DIGITS_EN2AN_INIT_LR = 0x60;
    public static final int DIGITS_EN2AN_INIT_AL = 0x80;
//    private static final int DIGITS_RESERVED = 0xa0;

    public static final int DIGIT_TYPE_AN = 0;
    public static final int DIGIT_TYPE_AN_EXTENDED = 0x100;

    public static class TestData {
        public int type;
        public String source;
        public int flags;
        public String result;
        public int length;
        public Class error;

        public static final int STANDARD = 0;
        public static final int PREFLIGHT = 1;
        public static final int ERROR = 2;

        public static TestData standard(String source, int flags, String result) {
            return new TestData(STANDARD, source, flags, result, 0, null);
        }

        public static TestData preflight(String source, int flags, int length) {
            return new TestData(PREFLIGHT, source, flags, null, length, null);
        }

        public static TestData error(String source, int flags, Class error) {
            return new TestData(ERROR, source, flags, null, 0, error);
        }

        private TestData(int type, String source, int flags, String result, int length, Class error) {
            this.type = type;
            this.source = source;
            this.flags = flags;
            this.result = result;
            this.length = length;
            this.error = error;
        }

        private static final String[] typenames = { "standard", "preflight", "error" };

        public String toString() {
            StringBuffer buf = new StringBuffer(super.toString());
            buf.append("[\n");
            buf.append(typenames[type]);
            buf.append(",\n");
            if (source == null) {
                buf.append("null");
            } else {
                buf.append('"');
                buf.append(escapedString(source));
                buf.append('"');
            }
            buf.append(",\n");
            buf.append(Integer.toHexString(flags));
            buf.append(",\n");
            if (result == null) {
                buf.append("null");
            } else {
                buf.append('"');
                buf.append(escapedString(result));
                buf.append('"');
            }
            buf.append(",\n");
            buf.append(length);
            buf.append(",\n");
            buf.append(error);
            buf.append(']');
            return buf.toString();
        }
    }

    private static final String lamAlefSpecialVLTR =
        "\u0020\u0646\u0622\u0644\u0627\u0020" +
         "\u0646\u0623\u064E\u0644\u0627\u0020" +
         "\u0646\u0627\u0670\u0644\u0627\u0020" +
         "\u0646\u0622\u0653\u0644\u0627\u0020" +
         "\u0646\u0625\u0655\u0644\u0627\u0020" +
         "\u0646\u0622\u0654\u0644\u0627\u0020" +
         "\uFEFC\u0639";

    private static final String tashkeelSpecialVLTR =
        "\u064A\u0628\u0631\u0639\u0020" +
        "\u064A\u0628\u0651\u0631\u064E\u0639\u0020" +
        "\u064C\u064A\u0628\u0631\u064F\u0639\u0020" +
        "\u0628\u0670\u0631\u0670\u0639\u0020" +
        "\u0628\u0653\u0631\u0653\u0639\u0020" +
        "\u0628\u0654\u0631\u0654\u0639\u0020" +
        "\u0628\u0655\u0631\u0655\u0639\u0020";

    private static final String tashkeelShaddaRTL=
            "\u0634\u0651\u0645\u0652\u0633";
    private static final String tashkeelShaddaLTR=
            "\u0633\u0652\u0645\u0651\u0634";

    private static final String ArMathSym =
        "\uD83B\uDE00\uD83B\uDE01\uD83B\uDE02\uD83B\uDE03\u0020" +
        "\uD83B\uDE24\uD83B\uDE05\uD83B\uDE06\u0020" +
        "\uD83B\uDE07\uD83B\uDE08\uD83B\uDE09\u0020" +
        "\uD83B\uDE0A\uD83B\uDE0B\uD83B\uDE0C\uD83B\uDE0D\u0020" +
        "\uD83B\uDE0E\uD83B\uDE0F\uD83B\uDE10\uD83B\uDE11\u0020" +
        "\uD83B\uDE12\uD83B\uDE13\uD83B\uDE14\uD83B\uDE15\u0020" +
        "\uD83B\uDE16\uD83B\uDE17\uD83B\uDE18\u0020" +
        "\uD83B\uDE19\uD83B\uDE1A\uD83B\uDE1B";

    private static final String ArMathSymLooped =
        "\uD83B\uDE80\uD83B\uDE81\uD83B\uDE82\uD83B\uDE83\u0020" +
        "\uD83B\uDE84\uD83B\uDE85\uD83B\uDE86\u0020" +
        "\uD83B\uDE87\uD83B\uDE88\uD83B\uDE89\u0020" +
        "\uD83B\uDE8B\uD83B\uDE8C\uD83B\uDE8D\u0020" +
        "\uD83B\uDE8E\uD83B\uDE8F\uD83B\uDE90\uD83B\uDE91\u0020" +
        "\uD83B\uDE92\uD83B\uDE93\uD83B\uDE94\uD83B\uDE95\u0020" +
        "\uD83B\uDE96\uD83B\uDE97\uD83B\uDE98\u0020" +
        "\uD83B\uDE99\uD83B\uDE9A\uD83B\uDE9B";

    private static final String ArMathSymDoubleStruck =
        "\uD83B\uDEA1\uD83B\uDEA2\uD83B\uDEA3\u0020" +
        "\uD83B\uDEA5\uD83B\uDEA6\u0020" +
        "\uD83B\uDEA7\uD83B\uDEA8\uD83B\uDEA9\u0020" +
        "\uD83B\uDEAB\uD83B\uDEAC\uD83B\uDEAD\u0020" +
        "\uD83B\uDEAE\uD83B\uDEAF\uD83B\uDEB0\uD83B\uDEB1\u0020" +
        "\uD83B\uDEB2\uD83B\uDEB3\uD83B\uDEB4\uD83B\uDEB5\u0020" +
        "\uD83B\uDEB6\uD83B\uDEB7\uD83B\uDEB8\u0020" +
        "\uD83B\uDEB9\uD83B\uDEBA\uD83B\uDEBB";

    private static final String ArMathSymInitial =
        "\uD83B\uDE21\uD83B\uDE22\u0020" +
        "\uD83B\uDE27\uD83B\uDE29\u0020" +
        "\uD83B\uDE2A\uD83B\uDE2B\uD83B\uDE2C\uD83B\uDE2D\u0020" +
        "\uD83B\uDE2E\uD83B\uDE2F\uD83B\uDE30\uD83B\uDE31\u0020" +
        "\uD83B\uDE32\uD83B\uDE34\uD83B\uDE35\u0020" +
        "\uD83B\uDE36\uD83B\uDE37\u0020" +
        "\uD83B\uDE39\uD83B\uDE3B";

    private static final String ArMathSymTailed =
        "\uD83B\uDE42\uD83B\uDE47\uD83B\uDE49\uD83B\uDE4B\u0020" +
        "\uD83B\uDE4D\uD83B\uDE4E\uD83B\uDE4F\u0020" +
        "\uD83B\uDE51\uD83B\uDE52\uD83B\uDE54\uD83B\uDE57\u0020" +
        "\uD83B\uDE59\uD83B\uDE5B\uD83B\uDE5D\uD83B\uDE5F";

    private static final String ArMathSymStretched =
        "\uD83B\uDE21\u0633\uD83B\uDE62\u0647";

    private static final String logicalUnshape =
        "\u0020\u0020\u0020\uFE8D\uFEF5\u0020\uFEE5\u0020\uFE8D\uFEF7\u0020" +
        "\uFED7\uFEFC\u0020\uFEE1\u0020\uFE8D\uFEDF\uFECC\uFEAE\uFE91\uFEF4" +
        "\uFE94\u0020\uFE8D\uFEDF\uFEA4\uFEAE\uFE93\u0020\u0020\u0020\u0020";

    private static final String numSource =
        "\u0031" +  /* en:1 */
        "\u0627" +  /* arabic:alef */
        "\u0032" +  /* en:2 */
        "\u06f3" +  /* an:3 */
        "\u0061" +  /* latin:a */
        "\u0034";   /* en:4 */

    private static final TestData[] standardTests = {
        /* lam alef special visual ltr */
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\ufee5\u0020\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\ufe76\ufef7\ufe8d\u0020" +
                          "\ufee5\u0020\u0670\ufefb\ufe8d\u0020" +
                          "\ufee5\u0020\u0653\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\u0655\ufef9\ufe8d\u0020" +
                          "\ufee5\u0020\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb\u0020\u0020\u0020\u0020" +
                          "\u0020\u0020"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0020\u0020\u0020\u0020\u0020\u0020" +
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_GROW_SHRINK,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),

        /* TASHKEEL */
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\ufee5\u0020\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\ufe76\ufef7\ufe8d\u0020" +
                          "\ufee5\u0020\u0670\ufefb\ufe8d\u0020" +
                          "\ufee5\u0020\u0653\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\u0655\ufef9\ufe8d\u0020" +
                          "\ufee5\u0020\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb\u0020\u0020\u0020\u0020" +
                          "\u0020\u0020"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0020\u0020\u0020\u0020\u0020\u0020" +
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_GROW_SHRINK,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),

        /* tashkeel special visual ltr */
        TestData.standard(tashkeelSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\ufef2\ufe91\ufeae\ufecb\u0020" +
                          "\ufef2\ufe91\ufe7c\ufeae\ufe77\ufecb\u0020" +
                          "\ufe72\ufef2\ufe91\ufeae\ufe79\ufecb\u0020" +
                          "\ufe8f\u0670\ufeae\u0670\ufecb\u0020" +
                          "\ufe8f\u0653\ufeae\u0653\ufecb\u0020" +
                          "\ufe8f\u0654\ufeae\u0654\ufecb\u0020" +
                          "\ufe8f\u0655\ufeae\u0655\ufecb\u0020"),

        TestData.standard(tashkeelSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\ufef2\ufe91\ufeae\ufecb\u0020" +
                          "\ufef2\ufe91\ufe7c\ufeae\ufe76\ufecb\u0020" +
                          "\ufe72\ufef2\ufe91\ufeae\ufe78\ufecb\u0020" +
                          "\ufe8f\u0670\ufeae\u0670\ufecb\u0020" +
                          "\ufe8f\u0653\ufeae\u0653\ufecb\u0020" +
                          "\ufe8f\u0654\ufeae\u0654\ufecb\u0020" +
                          "\ufe8f\u0655\ufeae\u0655\ufecb\u0020"),

        TestData.standard(tashkeelShaddaRTL,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_BEGIN |ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\u0020\ufeb7\ufe7d\ufee4\ufeb2"),
        TestData.standard(tashkeelShaddaRTL,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_END|ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\ufeb7\ufe7d\ufee4\ufeb2\u0020"),
        TestData.standard(tashkeelShaddaRTL,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_RESIZE|ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\ufeb7\ufe7d\ufee4\ufeb2"),
        TestData.standard(tashkeelShaddaRTL,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL|ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\ufeb7\ufe7d\ufee4\u0640\ufeb2"),

        TestData.standard(tashkeelShaddaLTR,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_BEGIN |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\u0020\ufeb2\ufee4\ufe7d\ufeb7"),
        TestData.standard(tashkeelShaddaLTR,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_END |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\ufeb2\ufee4\ufe7d\ufeb7\u0020"),
        TestData.standard(tashkeelShaddaLTR,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_RESIZE |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\ufeb2\ufee4\ufe7d\ufeb7"),
        TestData.standard(tashkeelShaddaLTR,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_REPLACE_BY_TATWEEL |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\ufeb2\u0640\ufee4\ufe7d\ufeb7"),

        TestData.standard(ArMathSym,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_BEGIN |ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\uD83B\uDE00\uD83B\uDE01\uD83B\uDE02\uD83B\uDE03\u0020" +
                          "\uD83B\uDE24\uD83B\uDE05\uD83B\uDE06\u0020" +
                          "\uD83B\uDE07\uD83B\uDE08\uD83B\uDE09\u0020" +
                          "\uD83B\uDE0A\uD83B\uDE0B\uD83B\uDE0C\uD83B\uDE0D\u0020" +
                          "\uD83B\uDE0E\uD83B\uDE0F\uD83B\uDE10\uD83B\uDE11\u0020" +
                          "\uD83B\uDE12\uD83B\uDE13\uD83B\uDE14\uD83B\uDE15\u0020" +
                          "\uD83B\uDE16\uD83B\uDE17\uD83B\uDE18\u0020" +
                          "\uD83B\uDE19\uD83B\uDE1A\uD83B\uDE1B"),
        TestData.standard(ArMathSymLooped,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_END|ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\uD83B\uDE80\uD83B\uDE81\uD83B\uDE82\uD83B\uDE83\u0020" +
                          "\uD83B\uDE84\uD83B\uDE85\uD83B\uDE86\u0020" +
                          "\uD83B\uDE87\uD83B\uDE88\uD83B\uDE89\u0020" +
                          "\uD83B\uDE8B\uD83B\uDE8C\uD83B\uDE8D\u0020" +
                          "\uD83B\uDE8E\uD83B\uDE8F\uD83B\uDE90\uD83B\uDE91\u0020" +
                          "\uD83B\uDE92\uD83B\uDE93\uD83B\uDE94\uD83B\uDE95\u0020" +
                          "\uD83B\uDE96\uD83B\uDE97\uD83B\uDE98\u0020" +
                          "\uD83B\uDE99\uD83B\uDE9A\uD83B\uDE9B"),
        TestData.standard(ArMathSymDoubleStruck,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_RESIZE|ArabicShaping.TEXT_DIRECTION_VISUAL_RTL ,
                          "\uD83B\uDEA1\uD83B\uDEA2\uD83B\uDEA3\u0020" +
                          "\uD83B\uDEA5\uD83B\uDEA6\u0020" +
                          "\uD83B\uDEA7\uD83B\uDEA8\uD83B\uDEA9\u0020" +
                          "\uD83B\uDEAB\uD83B\uDEAC\uD83B\uDEAD\u0020" +
                          "\uD83B\uDEAE\uD83B\uDEAF\uD83B\uDEB0\uD83B\uDEB1\u0020" +
                          "\uD83B\uDEB2\uD83B\uDEB3\uD83B\uDEB4\uD83B\uDEB5\u0020" +
                          "\uD83B\uDEB6\uD83B\uDEB7\uD83B\uDEB8\u0020" +
                          "\uD83B\uDEB9\uD83B\uDEBA\uD83B\uDEBB"),

        TestData.standard(ArMathSymInitial,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_BEGIN |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\uD83B\uDE21\uD83B\uDE22\u0020" +
                          "\uD83B\uDE27\uD83B\uDE29\u0020" +
                          "\uD83B\uDE2A\uD83B\uDE2B\uD83B\uDE2C\uD83B\uDE2D\u0020" +
                          "\uD83B\uDE2E\uD83B\uDE2F\uD83B\uDE30\uD83B\uDE31\u0020" +
                          "\uD83B\uDE32\uD83B\uDE34\uD83B\uDE35\u0020" +
                          "\uD83B\uDE36\uD83B\uDE37\u0020" +
                          "\uD83B\uDE39\uD83B\uDE3B"),
        TestData.standard(ArMathSymTailed,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_END |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\uD83B\uDE42\uD83B\uDE47\uD83B\uDE49\uD83B\uDE4B\u0020" +
                          "\uD83B\uDE4D\uD83B\uDE4E\uD83B\uDE4F\u0020" +
                          "\uD83B\uDE51\uD83B\uDE52\uD83B\uDE54\uD83B\uDE57\u0020" +
                          "\uD83B\uDE59\uD83B\uDE5B\uD83B\uDE5D\uD83B\uDE5F"),
        TestData.standard(ArMathSymStretched,
                          ArabicShaping.LETTERS_SHAPE|ArabicShaping.TASHKEEL_RESIZE |ArabicShaping.TEXT_DIRECTION_VISUAL_LTR ,
                          "\uD83B\uDE21\uFEB1\uD83B\uDE62\uFEE9"),

        /* logical unshape */
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0646\u0020\u0627\u0644\u0623\u0642\u0644\u0627" +
                          "\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627\u0644\u062d\u0631" +
                          "\u0629\u0020\u0020\u0020\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642" +
                          "\u0644\u0627\u0020\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627" +
                          "\u0644\u062d\u0631\u0629\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642\u0644\u0627\u0020" +
                          "\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627\u0644\u062d\u0631" +
                          "\u0629\u0020\u0020\u0020\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_GROW_SHRINK,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642" +
                          "\u0644\u0627\u0020\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627" +
                          "\u0644\u062d\u0631\u0629\u0020\u0020\u0020\u0020"),

        /* numbers */
        TestData.standard(numSource,
                          DIGITS_EN2AN | DIGIT_TYPE_AN,
                          "\u0661\u0627\u0662\u06f3\u0061\u0664"),
        TestData.standard(numSource,
                          DIGITS_AN2EN | DIGIT_TYPE_AN_EXTENDED,
                          "\u0031\u0627\u0032\u0033\u0061\u0034"),
        TestData.standard(numSource,
                          DIGITS_EN2AN_INIT_LR | DIGIT_TYPE_AN,
                          "\u0031\u0627\u0662\u06f3\u0061\u0034"),
        TestData.standard(numSource,
                          DIGITS_EN2AN_INIT_AL | DIGIT_TYPE_AN_EXTENDED,
                          "\u06f1\u0627\u06f2\u06f3\u0061\u0034"),
        TestData.standard(numSource,
                          DIGITS_EN2AN_INIT_LR | DIGIT_TYPE_AN | TEXT_DIRECTION_VISUAL_LTR,
                          "\u0661\u0627\u0032\u06f3\u0061\u0034"),
        TestData.standard(numSource,
                          DIGITS_EN2AN_INIT_AL | DIGIT_TYPE_AN_EXTENDED | TEXT_DIRECTION_VISUAL_LTR,
                          "\u06f1\u0627\u0032\u06f3\u0061\u06f4"),

        /* no-op */
        TestData.standard(numSource,
                          0,
                          numSource),
    };

    private static final TestData[] preflightTests = {
        /* preflight */
        TestData.preflight("\u0644\u0627",
                           LETTERS_SHAPE | LENGTH_GROW_SHRINK,
                           1),

        TestData.preflight("\u0644\u0627\u0031",
                           DIGITS_EN2AN | DIGIT_TYPE_AN_EXTENDED | LENGTH_GROW_SHRINK,
                           3),

        TestData.preflight("\u0644\u0644",
                           LETTERS_SHAPE | LENGTH_GROW_SHRINK,
                           2),

        TestData.preflight("\ufef7",
                           LETTERS_UNSHAPE | LENGTH_GROW_SHRINK,
                           2),
    };

    private static final TestData[] errorTests = {
        /* bad data */
        TestData.error("\u0020\ufef7\u0644\u0020",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_NEAR,
                       ArabicShapingException.class),

        TestData.error("\u0020\ufef7",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_AT_END,
                       ArabicShapingException.class),

        TestData.error("\ufef7\u0020",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_AT_BEGINNING,
                       ArabicShapingException.class),

        /* bad options */
        TestData.error("\ufef7",
                       0xffffffff,
                       IllegalArgumentException.class),

        TestData.error("\ufef7",
                       LETTERS_UNSHAPE | LENGTH_GROW_SHRINK,
                       ArabicShapingException.class),

        TestData.error(null,
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_AT_END,
                       IllegalArgumentException.class),
    };

    @Test
    public void TestStandard() {
        for (int i = 0; i < standardTests.length; ++i) {
            TestData test = standardTests[i];

            Exception ex = null;
            String result = null;
            ArabicShaping shaper = null;

            try {
                shaper = new ArabicShaping(test.flags);
                result = shaper.shape(test.source);
            }
            catch(MissingResourceException e){
                throw e;
            }
            catch (IllegalStateException ie){
                warnln("IllegalStateException: "+ie.toString());
                return;
            }
            catch (Exception e) {
                ex = e;
            }

            if (!test.result.equals(result)) {
                reportTestFailure(i, test, shaper, result, ex);
            }
        }
    }

    @Test
    public void TestPreflight() {
        for (int i = 0; i < preflightTests.length; ++i) {
            TestData test = preflightTests[i];

            Exception ex = null;
            char src[] = null;
            int len = 0;
            ArabicShaping shaper = null;

            if (test.source != null) {
                src = test.source.toCharArray();
            }

            try {
                shaper = new ArabicShaping(test.flags);
                len = shaper.shape(src, 0, src.length, null, 0, 0);
            }
            catch (Exception e) {
                ex = e;
            }

            if (test.length != len) {
                reportTestFailure(i, test, shaper, test.source, ex);
            }
        }
    }

    @Test
    public void TestError() {
        for (int i = 0; i < errorTests.length; ++i) {
            TestData test = errorTests[i];

            Exception ex = null;
            char src[] = null;
            int len = 0;
            ArabicShaping shaper = null;

            if (test.source != null) {
                src = test.source.toCharArray();
                len = src.length;
            }

            try {
                shaper = new ArabicShaping(test.flags);
                shaper.shape(src, 0, len);
            }
            catch (Exception e) {
                ex = e;
            }

            if (!test.error.isInstance(ex)) {
                reportTestFailure(i, test, shaper, test.source, ex);
            }
        }
    }

    @Test
    public void TestEquals()
    {
        ArabicShaping as1 = new ArabicShaping(LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR);
        ArabicShaping as2 = new ArabicShaping(LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR);
        ArabicShaping as3 = new ArabicShaping(LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_AT_BEGINNING);

        if (! as1.equals(as1)) {
            err("as1: " + as1 + " does not equal itself!\n");
        }

        if (! as1.equals(as2)) {
            err("as1: " + as1 + ", as2: " + as2 + " are not equal, but should be.\n");
        }

        if (as1.equals(as3)) {
            err("as1: " + as1 + ", as3: " + as3 + " are equal but should not be.\n");
        }
    }

    // TODO(user): remove this and convert callers to parameterized tests
    private void reportTestFailure(int index, TestData test, ArabicShaping shaper, String result, Exception error) {
        if (error != null && error instanceof MissingResourceException ) {
            warnln(error.getMessage());
        }

        StringBuffer buf = new StringBuffer();
        buf.append("*** test failure ***\n");
        buf.append("index: " + index + "\n");
        buf.append("test: " + test + "\n");
        buf.append("shaper: " + shaper + "\n");
        buf.append("result: " + escapedString(result) + "\n");
        buf.append("error: " + error + "\n");

        if (result != null && test.result != null && !test.result.equals(result)) {
            for (int i = 0; i < Math.max(test.result.length(), result.length()); ++i) {
                String temp = Integer.toString(i);
                if (temp.length() < 2) {
                    temp = " ".concat(temp);
                }
                char trg = i < test.result.length() ? test.result.charAt(i) : '\uffff';
                char res = i < result.length() ? result.charAt(i) : '\uffff';

                buf.append("[" + temp + "] ");
                buf.append(escapedString("" + trg) + " ");
                buf.append(escapedString("" + res) + " ");
                if (trg != res) {
                    buf.append("***");
                }
                buf.append("\n");
            }
        }
        err(buf.toString());
    }

    private static String escapedString(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(str.length() * 6);
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            buf.append("\\u");
            if (ch < 0x1000) {
                buf.append('0');
            }
            if (ch < 0x0100) {
                buf.append('0');
            }
            if (ch < 0x0010) {
                buf.append('0');
            }
            buf.append(Integer.toHexString(ch));
        }
        return buf.toString();
    }

    /* Tests the method
     *      public int shape(char[] source, int sourceStart, int sourceLength,
     *      char[] dest, int destStart, int destSize) throws ArabicShapingException)
     */
    @Test
    public void TestShape(){
        // Tests when
        //      if (sourceStart < 0 || sourceLength < 0 || sourceStart + sourceLength > source.length)
        // Is true
        ArabicShaping as = new ArabicShaping(0);
        char[] source = {'d','u','m','m','y'};
        char[] dest = {'d','u','m','m','y'};
        int[] negNum = {-1,-2,-5,-10,-100};


        for(int i=0; i<negNum.length; i++){
            try{
                // Checks when "sourceStart < 0"
                as.shape(source, negNum[i], 0, dest, 0, 0);
                errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                        "suppose to return an exception when 'sourceStart < 0'.");
            } catch(Exception e){}

            try{
                // Checks when "sourceLength < 0"
                as.shape(source, 0, negNum[i], dest, 0, 0);
                errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                        "suppose to return an exception when 'sourceLength < 0'.");
            } catch(Exception e){}
        }

        // Checks when "sourceStart + sourceLength > source.length"
        try{
            as.shape(source, 3, 3, dest, 0, 0);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when 'sourceStart + sourceLength > source.length'.");
        } catch(Exception e){}
        try{
            as.shape(source, 2, 4, dest, 0, 0);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when 'sourceStart + sourceLength > source.length'.");
        } catch(Exception e){}
        try{
            as.shape(source, 1, 5, dest, 0, 0);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when 'sourceStart + sourceLength > source.length'.");
        } catch(Exception e){}
        try{
            as.shape(source, 0, 6, dest, 0, 0);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when 'sourceStart + sourceLength > source.length'.");
        } catch(Exception e){}

        // Checks when "if (dest == null && destSize != 0)" is true
        try{
            as.shape(source, 2, 2, null, 0, 1);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when 'dest == null && destSize != 0'.");
        } catch(Exception e){}

        // Checks when
        // if ((destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length))
        for(int i=0; i<negNum.length; i++){
            try{
                as.shape(source, 2, 2, dest, negNum[i], 1);
                errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                        "suppose to return an exception when " +
                        "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
            } catch(Exception e){}

            try{
                as.shape(source, 2, 2, dest, 0, negNum[i]);
                errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                        "suppose to return an exception when " +
                        "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
            } catch(Exception e){}
        }

        // Checks when "destStart + destSize > dest.length"
        try{
            as.shape(source, 2, 2, dest, 3, 3);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when " +
                    "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
        } catch(Exception e){}
        try{
            as.shape(source, 2, 2, dest, 2, 4);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when " +
                    "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
        } catch(Exception e){}
        try{
            as.shape(source, 2, 2, dest, 1, 5);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when " +
                    "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
        } catch(Exception e){}
        try{
            as.shape(source, 2, 2, dest, 0, 6);
            errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                    "suppose to return an exception when " +
                    "(destSize != 0) && (destStart < 0 || destSize < 0 || destStart + destSize > dest.length).");
        } catch(Exception e){}

        // Tests when "throw new IllegalArgumentException("Wrong Tashkeel argument")"
        int[] invalid_Tashkeel = {-1000, -500, -100};
        for(int i=0; i < invalid_Tashkeel.length; i++){
            ArabicShaping arabicShape = new ArabicShaping(invalid_Tashkeel[i]);
            try {
                arabicShape.shape(source,0,0,dest,0,1);
                errln("ArabicShaping.shape(char[],int,int,char[],int,int) was " +
                        "suppose to return an exception for 'Wrong Tashkeel argument' for " +
                        "an option value of " + invalid_Tashkeel[i]);
            } catch (Exception e) {}
        }
    }

    @Test
    public void TestCoverage() {
        ArabicShaping shp = new ArabicShaping(LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR);

        // Test ArabicShaping#toString();
        assertEquals("ArabicShaping#toString() failed.",
                shp.toString(),
                "android.icu.text.ArabicShaping@d[LamAlef spaces at near, visual, shape letters," +
                        " no digit shaping, standard Arabic-Indic digits]");

        // Test ArabicShaping#hashCode()
        assertEquals("ArabicShaping#hashCode() failed.", shp.hashCode(), 13);
    }

    private boolean getStaticCharacterHelperFunctionValue(String methodName, char testValue) throws Exception {
        Method m = ArabicShaping.class.getDeclaredMethod(methodName, Character.TYPE);
        m.setAccessible(true);
        Object returnValue = m.invoke(null, testValue);

        if (Integer.class.isInstance(returnValue)) {
            return (Integer)returnValue == 1;
        }
        return (Boolean)returnValue;
    }

    @Test
    public void TestHelperFunctions() throws Exception {
        // Test private static helper functions that are used internally:

        // ArabicShaping.isSeenTailFamilyChar(char)
        assertTrue("ArabicShaping.isSeenTailFamilyChar(char) failed.",
                getStaticCharacterHelperFunctionValue("isSeenTailFamilyChar", (char)0xfeb1));

        // ArabicShaping.isAlefMaksouraChar(char)
        assertTrue("ArabicShaping.isAlefMaksouraChar(char) failed.",
                getStaticCharacterHelperFunctionValue("isAlefMaksouraChar", (char)0xfeef));

        // ArabicShaping.isTailChar(char)
        assertTrue("ArabicShaping.isTailChar(char) failed.",
                getStaticCharacterHelperFunctionValue("isTailChar", (char)0x200B));

        // ArabicShaping.isYehHamzaChar(char)
        assertTrue("ArabicShaping.isYehHamzaChar(char) failed.",
                getStaticCharacterHelperFunctionValue("isYehHamzaChar", (char)0xfe89));

    }
}
