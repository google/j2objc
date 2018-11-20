/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2013, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import java.util.Arrays;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Bidi;
import android.icu.text.BidiRun;
import android.icu.util.VersionInfo;

/**
 * A base class for the Bidi test suite.
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class BidiFmwk extends TestFmwk {

    protected static final char[] charFromDirProp = {
         /* L      R    EN    ES    ET     AN    CS    B    S    WS    ON */
         0x61, 0x5d0, 0x30, 0x2f, 0x25, 0x660, 0x2c, 0xa, 0x9, 0x20, 0x26,
         /* LRE     LRO     AL     RLE     RLO     PDF    NSM      BN */
         0x202a, 0x202d, 0x627, 0x202b, 0x202e, 0x202c, 0x308, 0x200c,
         /* FSI     LRI     RLI     PDI */
         0x2068, 0x2066, 0x2067, 0x2069  /* new in Unicode 6.3/ICU 52 */
    };

    static {
        initCharFromDirProps();
    }

    private static void initCharFromDirProps() {
        final VersionInfo ucd401 =  VersionInfo.getInstance(4, 0, 1, 0);
        VersionInfo ucdVersion = VersionInfo.getInstance(0, 0, 0, 0);

        /* lazy initialization */
        if (ucdVersion.getMajor() > 0) {
            return;

        }
        ucdVersion = UCharacter.getUnicodeVersion();
        if (ucdVersion.compareTo(ucd401) >= 0) {
            /* Unicode 4.0.1 changes bidi classes for +-/ */
            /* change ES character from / to + */
            charFromDirProp[TestData.ES] = 0x2b;
        }
    }

    protected boolean assertEquals(String message, String expected, String actual,
                                   String src, String mode, String option,
                                   String level) {
        if (expected == null || actual == null) {
            return super.assertEquals(message, expected, actual);
        }
        if (expected.equals(actual)) {
            return true;
        }
        errln("");
        errcontln(message);
        if (src != null) {
            errcontln("source            : \"" + Utility.escape(src) + "\"");
        }
        errcontln("expected          : \"" + Utility.escape(expected) + "\"");
        errcontln("actual            : \"" + Utility.escape(actual) + "\"");
        if (mode != null) {
            errcontln("reordering mode   : " + mode);
        }
        if (option != null) {
            errcontln("reordering option : " + option);
        }
        if (level != null) {
            errcontln("paragraph level   : " + level);
        }
        return false;
    }

    protected static String valueOf(int[] array) {
        StringBuffer result = new StringBuffer(array.length * 4);
        for (int i = 0; i < array.length; i++) {
            result.append(' ');
            result.append(array[i]);
        }
        return result.toString();
    }

    private static final String[] modeDescriptions = {
        "REORDER_DEFAULT",
        "REORDER_NUMBERS_SPECIAL",
        "REORDER_GROUP_NUMBERS_WITH_R",
        "REORDER_RUNS_ONLY",
        "REORDER_INVERSE_NUMBERS_AS_L",
        "REORDER_INVERSE_LIKE_DIRECT",
        "REORDER_INVERSE_FOR_NUMBERS_SPECIAL"
    };

    protected static String modeToString(int mode) {
        if (mode < Bidi.REORDER_DEFAULT ||
            mode > Bidi.REORDER_INVERSE_FOR_NUMBERS_SPECIAL) {
            return "INVALID";
        }
        return modeDescriptions[mode];
    }

    private static final short SETPARA_MASK = Bidi.OPTION_INSERT_MARKS |
        Bidi.OPTION_REMOVE_CONTROLS | Bidi.OPTION_STREAMING;

    private static final String[] setParaDescriptions = {
        "OPTION_INSERT_MARKS",
        "OPTION_REMOVE_CONTROLS",
        "OPTION_STREAMING"
    };

    protected static String spOptionsToString(int option) {
        return optionToString(option, SETPARA_MASK, setParaDescriptions);
    }

    private static final int MAX_WRITE_REORDERED_OPTION = Bidi.OUTPUT_REVERSE;
    private static final int REORDER_MASK = (MAX_WRITE_REORDERED_OPTION << 1) - 1;

    private static final String[] writeReorderedDescriptions = {
        "KEEP_BASE_COMBINING",      //  1
        "DO_MIRRORING",             //  2
        "INSERT_LRM_FOR_NUMERIC",   //  4
        "REMOVE_BIDI_CONTROLS",     //  8
        "OUTPUT_REVERSE"            // 16
    };

    public static String wrOptionsToString(int option) {
        return optionToString(option, REORDER_MASK, writeReorderedDescriptions);
    }
    public static String optionToString(int option, int mask,
                                        String[] descriptions) {
        StringBuffer desc = new StringBuffer(50);

        if ((option &= mask) == 0) {
            return "0";
        }
        desc.setLength(0);

        for (int i = 0; option > 0; i++, option >>= 1) {
            if ((option & 1) != 0) {
                if (desc.length() > 0) {
                    desc.append(" | ");
                }
                desc.append(descriptions[i]);
            }
        }
        return desc.toString();
    }

    static final String columnString =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static final char[] columns = columnString.toCharArray();
    private static final int TABLE_SIZE = 256;
    private static boolean tablesInitialized = false;
    private static char[] pseudoToUChar;
    private static char[] UCharToPseudo;    /* used for Unicode chars < 0x0100 */
    private static char[] UCharToPseud2;    /* used for Unicode chars >=0x0100 */

    static void buildPseudoTables()
    /*
        The rules for pseudo-Bidi are as follows:
        - [ == LRE
        - ] == RLE
        - { == LRO
        - } == RLO
        - ^ == PDF
        - @ == LRM
        - & == RLM
        - A-F == Arabic Letters 0631-0636
        - G-V == Hebrew letters 05d7-05ea
        - W-Z == Unassigned RTL 08d0-08d3
        - 0-5 == western digits 0030-0035
        - 6-9 == Arabic-Indic digits 0666-0669
        - ` == Combining Grave Accent 0300 (NSM)
        - ~ == Delete 007f (BN)
        - | == Paragraph Separator 2029 (B)
        - _ == Info Separator 1 001f (S)
        All other characters represent themselves as Latin-1, with the corresponding
        Bidi properties.
    */
    {
        int     i;
        char    uchar;
        char    c;

        /* initialize all tables to unknown */
        pseudoToUChar = new char[TABLE_SIZE];
        UCharToPseudo = new char[TABLE_SIZE];
        UCharToPseud2 = new char[TABLE_SIZE];
        for (i = 0; i < TABLE_SIZE; i++) {
            pseudoToUChar[i] = 0xFFFD;
            UCharToPseudo[i] = '?';
            UCharToPseud2[i] = '?';
        }
        /* initialize non letters or digits */
        pseudoToUChar[ 0 ] = 0x0000;    UCharToPseudo[0x00] =  0 ;
        pseudoToUChar[' '] = 0x0020;    UCharToPseudo[0x20] = ' ';
        pseudoToUChar['!'] = 0x0021;    UCharToPseudo[0x21] = '!';
        pseudoToUChar['"'] = 0x0022;    UCharToPseudo[0x22] = '"';
        pseudoToUChar['#'] = 0x0023;    UCharToPseudo[0x23] = '#';
        pseudoToUChar['$'] = 0x0024;    UCharToPseudo[0x24] = '$';
        pseudoToUChar['%'] = 0x0025;    UCharToPseudo[0x25] = '%';
        pseudoToUChar['\'']= 0x0027;    UCharToPseudo[0x27] = '\'';
        pseudoToUChar['('] = 0x0028;    UCharToPseudo[0x28] = '(';
        pseudoToUChar[')'] = 0x0029;    UCharToPseudo[0x29] = ')';
        pseudoToUChar['*'] = 0x002A;    UCharToPseudo[0x2A] = '*';
        pseudoToUChar['+'] = 0x002B;    UCharToPseudo[0x2B] = '+';
        pseudoToUChar[','] = 0x002C;    UCharToPseudo[0x2C] = ',';
        pseudoToUChar['-'] = 0x002D;    UCharToPseudo[0x2D] = '-';
        pseudoToUChar['.'] = 0x002E;    UCharToPseudo[0x2E] = '.';
        pseudoToUChar['/'] = 0x002F;    UCharToPseudo[0x2F] = '/';
        pseudoToUChar[':'] = 0x003A;    UCharToPseudo[0x3A] = ':';
        pseudoToUChar[';'] = 0x003B;    UCharToPseudo[0x3B] = ';';
        pseudoToUChar['<'] = 0x003C;    UCharToPseudo[0x3C] = '<';
        pseudoToUChar['='] = 0x003D;    UCharToPseudo[0x3D] = '=';
        pseudoToUChar['>'] = 0x003E;    UCharToPseudo[0x3E] = '>';
        pseudoToUChar['?'] = 0x003F;    UCharToPseudo[0x3F] = '?';
        pseudoToUChar['\\']= 0x005C;    UCharToPseudo[0x5C] = '\\';
        /* initialize specially used characters */
        pseudoToUChar['`'] = 0x0300;    UCharToPseud2[0x00] = '`';  /* NSM */
        pseudoToUChar['@'] = 0x200E;    UCharToPseud2[0x0E] = '@';  /* LRM */
        pseudoToUChar['&'] = 0x200F;    UCharToPseud2[0x0F] = '&';  /* RLM */
        pseudoToUChar['_'] = 0x001F;    UCharToPseudo[0x1F] = '_';  /* S   */
        pseudoToUChar['|'] = 0x2029;    UCharToPseud2[0x29] = '|';  /* B   */
        pseudoToUChar['['] = 0x202A;    UCharToPseud2[0x2A] = '[';  /* LRE */
        pseudoToUChar[']'] = 0x202B;    UCharToPseud2[0x2B] = ']';  /* RLE */
        pseudoToUChar['^'] = 0x202C;    UCharToPseud2[0x2C] = '^';  /* PDF */
        pseudoToUChar['{'] = 0x202D;    UCharToPseud2[0x2D] = '{';  /* LRO */
        pseudoToUChar['}'] = 0x202E;    UCharToPseud2[0x2E] = '}';  /* RLO */
        pseudoToUChar['~'] = 0x007F;    UCharToPseudo[0x7F] = '~';  /* BN  */
        /* initialize western digits */
        for (i = 0, uchar = 0x0030; i < 6; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseudo[uchar & 0x00ff] = c;
        }
        /* initialize Hindi digits */
        for (i = 6, uchar = 0x0666; i < 10; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseud2[uchar & 0x00ff] = c;
        }
        /* initialize Arabic letters */
        for (i = 10, uchar = 0x0631; i < 16; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseud2[uchar & 0x00ff] = c;
        }
        /* initialize Hebrew letters */
        for (i = 16, uchar = 0x05D7; i < 32; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseud2[uchar & 0x00ff] = c;
        }
        /* initialize Unassigned code points */
        for (i = 32, uchar = 0x08D0; i < 36; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseud2[uchar & 0x00ff] = c;
        }
        /* initialize Latin lower case letters */
        for (i = 36, uchar = 0x0061; i < 62; i++, uchar++) {
            c = columns[i];
            pseudoToUChar[c] = uchar;
            UCharToPseudo[uchar & 0x00ff] = c;
        }
        tablesInitialized = true;
    }

    /*----------------------------------------------------------------------*/

    static String pseudoToU16(String input)
    /*  This function converts a pseudo-Bidi string into a char string.
        It returns the char string.
    */
    {
        int len = input.length();
        char[] output = new char[len];
        int i;
        if (!tablesInitialized) {
            buildPseudoTables();
        }
        for (i = 0; i < len; i++)
            output[i] = pseudoToUChar[input.charAt(i)];
        return new String(output);
    }

    /*----------------------------------------------------------------------*/

    static String u16ToPseudo(String input)
    /*  This function converts a char string into a pseudo-Bidi string.
        It returns the pseudo-Bidi string.
    */
    {
        int len = input.length();
        char[] output = new char[len];
        int i;
        char uchar;
        if (!tablesInitialized) {
            buildPseudoTables();
        }
        for (i = 0; i < len; i++)
        {
            uchar = input.charAt(i);
            output[i] = uchar < 0x0100 ? UCharToPseudo[uchar] :
                                         UCharToPseud2[uchar & 0x00ff];
        }
        return new String(output);
    }

    void errcont(String message) {
        msg(message, ERR, false, false);
    }

    void errcontln(String message) {
        msg(message, ERR, false, true);
    }

    void printCaseInfo(Bidi bidi, String src, String dst)
    {
        int length = bidi.getProcessedLength();
        byte[] levels = bidi.getLevels();
        char[] levelChars  = new char[length];
        byte lev;
        int runCount = bidi.countRuns();
        errcontln("========================================");
        errcontln("Processed length: " + length);
        for (int i = 0; i < length; i++) {
            lev = levels[i];
            if (lev < 0) {
                levelChars[i] = '-';
            } else if (lev < columns.length) {
                levelChars[i] = columns[lev];
            } else {
                levelChars[i] = '+';
            }
        }
        errcontln("Levels: " + new String(levelChars));
        errcontln("Source: " + src);
        errcontln("Result: " + dst);
        errcontln("Direction: " + bidi.getDirection());
        errcontln("paraLevel: " + Byte.toString(bidi.getParaLevel()));
        errcontln("reorderingMode: " + modeToString(bidi.getReorderingMode()));
        errcontln("reorderingOptions: " + spOptionsToString(bidi.getReorderingOptions()));
        errcont("Runs: " + runCount + " => logicalStart.length/level: ");
        for (int i = 0; i < runCount; i++) {
            BidiRun run;
            run = bidi.getVisualRun(i);
            errcont(" " + run.getStart() + "." + run.getLength() + "/" +
                    run.getEmbeddingLevel());
        }
        errcont("\n");
    }

    static final String mates1 = "<>()[]{}";
    static final String mates2 = "><)(][}{";
    static final char[] mates1Chars = mates1.toCharArray();
    static final char[] mates2Chars = mates2.toCharArray();

    boolean matchingPair(Bidi bidi, int i, char c1, char c2)
    {
        if (c1 == c2) {
            return true;
        }
        /* For REORDER_RUNS_ONLY, it would not be correct to check levels[i],
           so we use the appropriate run's level, which is good for all cases.
         */
        if (bidi.getLogicalRun(i).getDirection() == 0) {
            return false;
        }
        for (int k = 0; k < mates1Chars.length; k++) {
            if ((c1 == mates1Chars[k]) && (c2 == mates2Chars[k])) {
                return true;
            }
        }
        return false;
    }

    boolean checkWhatYouCan(Bidi bidi, String src, String dst)
    {
        int i, idx, logLimit, visLimit;
        boolean testOK, errMap, errDst;
        char[] srcChars = src.toCharArray();
        char[] dstChars = dst.toCharArray();
        int[] visMap = bidi.getVisualMap();
        int[] logMap = bidi.getLogicalMap();

        testOK = true;
        errMap = errDst = false;
        logLimit = bidi.getProcessedLength();
        visLimit = bidi.getResultLength();
        if (visLimit > dstChars.length) {
            visLimit = dstChars.length;
        }
        char[] accumSrc = new char[logLimit];
        char[] accumDst = new char[visLimit];
        Arrays.fill(accumSrc, '?');
        Arrays.fill(accumDst, '?');

        if (logMap.length != logLimit) {
            errMap = true;
        }
        for (i = 0; i < logLimit; i++) {
            idx = bidi.getVisualIndex(i);
            if (idx != logMap[i]) {
                errMap = true;
            }
            if (idx == Bidi.MAP_NOWHERE) {
                continue;
            }
            if (idx >= visLimit) {
                continue;
            }
            accumDst[idx] = srcChars[i];
            if (!matchingPair(bidi, i, srcChars[i], dstChars[idx])) {
                errDst = true;
            }
        }
        if (errMap) {
            if (testOK) {
                printCaseInfo(bidi, src, dst);
                testOK = false;
            }
            errln("Mismatch between getLogicalMap() and getVisualIndex()");
            errcont("Map    :" + valueOf(logMap));
            errcont("\n");
            errcont("Indexes:");
            for (i = 0; i < logLimit; i++) {
                errcont(" " + bidi.getVisualIndex(i));
            }
            errcont("\n");
        }
        if (errDst) {
            if (testOK) {
                printCaseInfo(bidi, src, dst);
                testOK = false;
            }
            errln("Source does not map to Result");
            errcontln("We got: " + new String(accumDst));
        }

        errMap = errDst = false;
        if (visMap.length != visLimit) {
            errMap = true;
        }
        for (i = 0; i < visLimit; i++) {
            idx = bidi.getLogicalIndex(i);
            if (idx != visMap[i]) {
                errMap = true;
            }
            if (idx == Bidi.MAP_NOWHERE) {
                continue;
            }
            if (idx >= logLimit) {
                continue;
            }
            accumSrc[idx] = dstChars[i];
            if (!matchingPair(bidi, idx, srcChars[idx], dstChars[i])) {
                errDst = true;
            }
        }
        if (errMap) {
            if (testOK) {
                printCaseInfo(bidi, src, dst);
                testOK = false;
            }
            errln("Mismatch between getVisualMap() and getLogicalIndex()");
            errcont("Map    :" + valueOf(visMap));
            errcont("\n");
            errcont("Indexes:");
            for (i = 0; i < visLimit; i++) {
                errcont(" " + bidi.getLogicalIndex(i));
            }
            errcont("\n");
        }
        if (errDst) {
            if (testOK) {
                printCaseInfo(bidi, src, dst);
                testOK = false;
            }
            errln("Result does not map to Source");
            errcontln("We got: " + new String(accumSrc));
        }
        return testOK;
    }

}
