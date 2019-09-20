/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2010-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.bidi;

import java.io.BufferedReader;
import java.io.IOException;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.lang.UCharacterDirection;
import android.icu.text.Bidi;
import android.icu.text.BidiClassifier;

/**
 * @author Markus W. Scherer
 * BiDi conformance test, using the Unicode BidiTest.txt and BidiCharacterTest.txt files.
 * Ported from ICU4C intltest/bidiconf.cpp .
 */
public class BiDiConformanceTest extends TestFmwk {
    public BiDiConformanceTest() {}

    @Test
    public void TestBidiTest() throws IOException {
        BufferedReader bidiTestFile = TestUtil.getDataReader("unicode/BidiTest.txt");
        try {
            Bidi ubidi = new Bidi();
            ubidi.setCustomClassifier(new ConfTestBidiClassifier());
            lineNumber = 0;
            levelsCount = 0;
            orderingCount = 0;
            errorCount = 0;
outerLoop:
            while (errorCount < 10 && (line = bidiTestFile.readLine()) != null) {
                ++lineNumber;
                lineIndex = 0;
                // Remove trailing comments and whitespace.
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                if (!skipWhitespace()) {
                    continue; // Skip empty and comment-only lines.
                }
                if (line.charAt(lineIndex) == '@') {
                    ++lineIndex;
                    if (line.startsWith("Levels:", lineIndex)) {
                        lineIndex += 7;
                        if (!parseLevels(line.substring(lineIndex))) {
                            break;
                        }
                    } else if (line.startsWith("Reorder:", lineIndex)) {
                        lineIndex += 8;
                        if (!parseOrdering(line.substring(lineIndex))) {
                            break;
                        }
                    }
                    // Skip unknown @Xyz: ...
                } else {
                    parseInputStringFromBiDiClasses();
                    if (!skipWhitespace() || line.charAt(lineIndex++) != ';') {
                        errln("missing ; separator on input line " + line);
                        return;
                    }
                    int bitset = Integer.parseInt(line.substring(lineIndex).trim(), 16);
                    // Loop over the bitset.
                    for (int i = 0; i <= 3; ++i) {
                        if ((bitset & (1 << i)) != 0) {
                            ubidi.setPara(inputString, paraLevels[i], null);
                            byte actualLevels[] = ubidi.getLevels();
                            paraLevelName = paraLevelNames[i];
                            if (!checkLevels(actualLevels)) {
                                continue outerLoop;
                            }
                            if (!checkOrdering(ubidi)) {
                                continue outerLoop;
                            }
                        }
                    }
                }
            }
        } finally {
            bidiTestFile.close();
        }
    }

    /*
    *******************************************************************************
    *
    *   created on: 2013jul01
    *   created by: Matitiahu Allouche

    This function performs a conformance test for implementations of the
    Unicode Bidirectional Algorithm, specified in UAX #9: Unicode
    Bidirectional Algorithm, at http://www.unicode.org/unicode/reports/tr9/

    Each test case is represented in a single line which is read from a file
    named BidiCharacter.txt.  Empty, blank and comment lines may also appear
    in this file.

    The format of the test data is specified below.  Note that each test
    case constitutes a single line of text; reordering is applied within a
    single line and independently of a rendering engine, and rules L3 and L4
    are out of scope.

    The number sign '#' is the comment character: everything is ignored from
    the occurrence of '#' until the end of the line,
    Empty lines and lines containing only spaces and/or comments are ignored.

    Lines which represent test cases consist of 4 or 5 fields separated by a
    semicolon.  Each field consists of tokens separated by whitespace (space
    or Tab).  Whitespace before and after semicolons is optional.

    Field 0: A sequence of hexadecimal code point values separated by space

    Field 1: A value representing the paragraph direction, as follows:
        - 0 represents left-to-right
        - 1 represents right-to-left
        - 2 represents auto-LTR according to rules P2 and P3 of the algorithm
        - 3 represents auto-RTL according to rules P2 and P3 of the algorithm
        - a negative number whose absolute value is taken as paragraph level;
          this may be useful to test cases where the embedding level approaches
          or exceeds the maximum embedding level.

    Field 2: The resolved paragraph embedding level.  If the input (field 0)
             includes more than one paragraph, this field represents the
             resolved level of the first paragraph.

    Field 3: An ordered list of resulting levels for each token in field 0
             (each token represents one source character).
             The UBA does not assign levels to certain characters (e.g. LRO);
             characters removed in rule X9 are indicated with an 'x'.

    Field 4: An ordered list of indices showing the resulting visual ordering
             from left to right; characters with a resolved level of 'x' are
             skipped.  The number are zero-based.  Each index corresponds to
             a character in the reordered (visual) string. It represents the
             index of the source character in the input (field 0).
             This field is optional.  When it is absent, the visual ordering
             is not verified.

    Examples:

    # This is a comment line.
    L L ON R ; 0 ; 0 ; 0 0 0 1 ; 0 1 2 3
    L L ON R;0;0;0 0 0 1;0 1 2 3

    # Note: in the next line, 'B' represents a block separator, not the letter 'B'.
    LRE A B C PDF;2;0;x 2 0 0 x;1 2 3
    # Note: in the next line, 'b' represents the letter 'b', not a block separator.
    a b c 05d0 05d1 x ; 0 ; 0 ; 0 0 0 1 1 0 ; 0 1 2 4 3 5

    a R R x ; 1 ; 1 ; 2 1 1 2
    L L R R R B R R L L L B ON ON ; 3 ; 0 ; 0 0 1 1 1 0 1 1 2 2 2 1 1 1

    *
    *******************************************************************************
    */
    @Test
    public void TestBidiCharacterTest() throws IOException {
        BufferedReader bidiTestFile = TestUtil.getDataReader("unicode/BidiCharacterTest.txt");
        try {
            Bidi ubidi = new Bidi();
            lineNumber = 0;
            levelsCount = 0;
            orderingCount = 0;
            errorCount = 0;
outerLoop:
            while (errorCount < 20 && (line = bidiTestFile.readLine()) != null) {
                ++lineNumber;
                paraLevelName = "N/A";
                inputString = "N/A";
                lineIndex = 0;
                // Remove trailing comments and whitespace.
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                if (!skipWhitespace()) {
                    continue; // Skip empty and comment-only lines.
                }
                String[] parts = line.split(";");
                if (parts.length < 4) {
                    errorCount++;
                    errln(" on line " + lineNumber + ": Missing ; separator on line: " + line);
                    continue;
                }
                // Parse the code point string in field 0.
                try {
                    inputStringBuilder.delete(0, inputStringBuilder.length());
                    for (String cp : parts[0].trim().split("[ \t]+")) {
                        inputStringBuilder.appendCodePoint(Integer.parseInt(cp, 16));
                    }
                    inputString = inputStringBuilder.toString();
                } catch (Exception e) {
                    errln(" ------------ Invalid string in field 0 on line '" + line + "'");
                    ++errorCount;
                    continue;
                }
                int paraDirection = intFromString(parts[1].trim());
                byte paraLevel;
                if (paraDirection == 0) {
                    paraLevel = 0;
                    paraLevelName = "LTR";
                } else if (paraDirection == 1) {
                    paraLevel = 1;
                    paraLevelName = "RTL";
                } else if (paraDirection == 2) {
                    paraLevel = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;
                    paraLevelName = "Auto/LTR";
                } else if (paraDirection == 3) {
                    paraLevel = Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT;
                    paraLevelName = "Auto/RTL";
                } else if (paraDirection < 0 && -paraDirection <= (Bidi.MAX_EXPLICIT_LEVEL + 1)) {
                    paraLevel = (byte) (-paraDirection);
                    paraLevelName = Byte.toString(paraLevel);
                } else {
                    errorCount++;
                    errln(" on line " + lineNumber + ": Input paragraph direction incorrect at " + line);
                    continue;
                }
                int resolvedParaLevel = intFromString(parts[2].trim());
                if (resolvedParaLevel < 0 || resolvedParaLevel > (Bidi.MAX_EXPLICIT_LEVEL + 1)) {
                    errorCount++;
                    errln(" on line " + lineNumber + ": Resolved paragraph level incorrect at " + line);
                    continue;
                }
                if (!parseLevels(parts[3])) {
                    continue;
                }
                if (parts.length > 4) {
                    if (!parseOrdering(parts[4])) {
                        continue;
                    }
                } else {
                    orderingCount = -1;
                }

                ubidi.setPara(inputString, paraLevel, null);
                byte actualParaLevel = ubidi.getParaLevel();
                if (actualParaLevel != resolvedParaLevel) {
                    errln(" ------------ Wrong resolved paragraph level; expected " + resolvedParaLevel + " actual "
                            + actualParaLevel);
                    printErrorLine();
                }
                byte[] actualLevels = ubidi.getLevels();
                if (!checkLevels(actualLevels)) {
                    continue outerLoop;
                }
                if (!checkOrdering(ubidi)) {
                    continue outerLoop;
                }
            }
        } finally {
            bidiTestFile.close();
        }
    }

    private static final byte paraLevels[]={
        Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT,
        0,
        1,
        Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT
    };
    private static final String paraLevelNames[]={ "auto/LTR", "LTR", "RTL", "auto/RTL" };

    private int intFromString(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -9999;
        }
    }

    private boolean parseLevels(String s) {
        directionBits=0;
        levelsCount=0;
        String[] levelStrings=s.trim().split("[ \t]+");
        for(String levelString: levelStrings) {
            if(levelString.length()==0) { continue; }
            if(levelString.equals("x")) {
                levels[levelsCount++]=-1;
            } else {
                try {
                    int value=Integer.parseInt(levelString);
                    if(0<=value && value<=(Bidi.MAX_EXPLICIT_LEVEL+1)) {
                        levels[levelsCount++]=(byte)value;
                        directionBits|=(1<<(value&1));
                        continue;
                    }
                } catch(Exception e) {
                }
                errln(" ------------ Levels parse error at '"+levelString+"'");
                printErrorLine();
                return false;
            }
        }
        return true;
    }
    private boolean parseOrdering(String s) {
        orderingCount=0;
        String[] orderingStrings=s.trim().split("[ \t]+");
        for(String orderingString: orderingStrings) {
            if(orderingString.length()==0) { continue; }
            try {
                int value=Integer.parseInt(orderingString);
                if(value<1000) {
                    ordering[orderingCount++]=value;
                    continue;
                }
            } catch(Exception e) {
            }
            errln(" ------------ Reorder parse error at '"+orderingString+"'");
            printErrorLine();
            return false;
        }
        return true;
    }
    private static char charFromBiDiClass[]={
        0x6c,   // 'l' for L
        0x52,   // 'R' for R
        0x33,   // '3' for EN
        0x2d,   // '-' for ES
        0x25,   // '%' for ET
        0x39,   // '9' for AN
        0x2c,   // ',' for CS
        0x2f,   // '/' for B
        0x5f,   // '_' for S
        0x20,   // ' ' for WS
        0x3d,   // '=' for ON
        0x65,   // 'e' for LRE
        0x6f,   // 'o' for LRO
        0x41,   // 'A' for AL
        0x45,   // 'E' for RLE
        0x4f,   // 'O' for RLO
        0x2a,   // '*' for PDF
        0x60,   // '`' for NSM
        0x7c,   // '|' for BN
        // new in Unicode 6.3/ICU 52
        0x53,   // 'S' for FSI
        0x69,   // 'i' for LRI
        0x49,   // 'I' for RLI
        0x2e    // '.' for PDI
    };
    private class ConfTestBidiClassifier extends BidiClassifier {
        public ConfTestBidiClassifier() {
            super(null);
        }
        @Override
        public int classify(int c) {
            for(int i=0; i<charFromBiDiClass.length; ++i) {
                if(c==charFromBiDiClass[i]) {
                    return i;
                }
            }
            // Character not in our hardcoded table.
            // Should not occur during testing.
            return Bidi.CLASS_DEFAULT;
        }
    }
    private static final int biDiClassNameLengths[]={
        1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 3, 3, 2, 3, 3, 3, 3, 2, 3, 3, 3, 3, 0
    };
    private void parseInputStringFromBiDiClasses() {
        inputStringBuilder.delete(0, 0x7fffffff);
        /*
         * Lengthy but fast BiDi class parser.
         * A simple parser could terminate or extract the name string and use
         *   int32_t biDiClassInt=u_getPropertyValueEnum(UCHAR_BIDI_CLASS, bidiClassString);
         * but that makes this test take significantly more time.
         */
        char c0, c1, c2;
        while(skipWhitespace() && (c0=line.charAt(lineIndex))!=';') {
            int biDiClass=UCharacterDirection.CHAR_DIRECTION_COUNT;
            // Compare each character once until we have a match on
            // a complete, short BiDi class name.
            if(c0=='L') {
                if((lineIndex+2)<line.length() && line.charAt(lineIndex+1)=='R') {
                    c2=line.charAt(lineIndex+2);
                    if(c2=='E') {
                        biDiClass=UCharacterDirection.LEFT_TO_RIGHT_EMBEDDING;
                    } else if(c2=='I') {
                        biDiClass=UCharacterDirection.LEFT_TO_RIGHT_ISOLATE;
                    } else if(c2=='O') {
                        biDiClass=UCharacterDirection.LEFT_TO_RIGHT_OVERRIDE;
                    }
                } else {
                    biDiClass=UCharacterDirection.LEFT_TO_RIGHT;
                }
            } else if(c0=='R') {
                if((lineIndex+2)<line.length() && line.charAt(lineIndex+1)=='L') {
                    c2=line.charAt(lineIndex+2);
                    if(c2=='E') {
                        biDiClass=UCharacterDirection.RIGHT_TO_LEFT_EMBEDDING;
                    } else if(c2=='I') {
                        biDiClass=UCharacterDirection.RIGHT_TO_LEFT_ISOLATE;
                    } else if(c2=='O') {
                        biDiClass=UCharacterDirection.RIGHT_TO_LEFT_OVERRIDE;
                    }
                } else {
                    biDiClass=UCharacterDirection.RIGHT_TO_LEFT;
                }
            } else if(c0=='E') {
                if((lineIndex+1)>=line.length()) {
                    // too short
                } else if((c1=line.charAt(lineIndex+1))=='N') {
                    biDiClass=UCharacterDirection.EUROPEAN_NUMBER;
                } else if(c1=='S') {
                    biDiClass=UCharacterDirection.EUROPEAN_NUMBER_SEPARATOR;
                } else if(c1=='T') {
                    biDiClass=UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR;
                }
            } else if(c0=='A') {
                if((lineIndex+1)>=line.length()) {
                    // too short
                } else if((c1=line.charAt(lineIndex+1))=='L') {
                    biDiClass=UCharacterDirection.RIGHT_TO_LEFT_ARABIC;
                } else if(c1=='N') {
                    biDiClass=UCharacterDirection.ARABIC_NUMBER;
                }
            } else if(c0=='C' && (lineIndex+1)<line.length() && line.charAt(lineIndex+1)=='S') {
                biDiClass=UCharacterDirection.COMMON_NUMBER_SEPARATOR;
            } else if(c0=='B') {
                if((lineIndex+1)<line.length() && line.charAt(lineIndex+1)=='N') {
                    biDiClass=UCharacterDirection.BOUNDARY_NEUTRAL;
                } else {
                    biDiClass=UCharacterDirection.BLOCK_SEPARATOR;
                }
            } else if(c0=='S') {
                biDiClass=UCharacterDirection.SEGMENT_SEPARATOR;
            } else if(c0=='W' && (lineIndex+1)<line.length() && line.charAt(lineIndex+1)=='S') {
                biDiClass=UCharacterDirection.WHITE_SPACE_NEUTRAL;
            } else if(c0=='O' && (lineIndex+1)<line.length() && line.charAt(lineIndex+1)=='N') {
                biDiClass=UCharacterDirection.OTHER_NEUTRAL;
            } else if(c0=='P' && (lineIndex+2)<line.length() && line.charAt(lineIndex+1)=='D') {
                if(line.charAt(lineIndex+2)=='F') {
                    biDiClass=UCharacterDirection.POP_DIRECTIONAL_FORMAT;
                } else if(line.charAt(lineIndex+2)=='I') {
                    biDiClass=UCharacterDirection.POP_DIRECTIONAL_ISOLATE;
                }
            } else if(c0=='N' && (lineIndex+2)<line.length() &&
                      line.charAt(lineIndex+1)=='S' && line.charAt(lineIndex+2)=='M') {
                biDiClass=UCharacterDirection.DIR_NON_SPACING_MARK;
            } else if(c0=='F' && (lineIndex+2)<line.length() &&
                    line.charAt(lineIndex+1)=='S' && line.charAt(lineIndex+2)=='I') {
                biDiClass=UCharacterDirection.FIRST_STRONG_ISOLATE;
            }
            // Now we verify that the class name is terminated properly,
            // and not just the start of a longer word.
            int biDiClassNameLength=biDiClassNameLengths[biDiClass];
            char c;
            if( biDiClass==UCharacterDirection.CHAR_DIRECTION_COUNT ||
                ((lineIndex+biDiClassNameLength)<line.length() &&
                 !isInvWhitespace(c=line.charAt(lineIndex+biDiClassNameLength)) &&
                 c!=';')
            ) {
                throw new IllegalArgumentException(
                    "BiDi class string not recognized at "+line.substring(lineIndex)+" in "+line);
            }
            inputStringBuilder.append(charFromBiDiClass[biDiClass]);
            lineIndex+=biDiClassNameLength;
        }
        inputString=inputStringBuilder.toString();
    }

    private static char printLevel(byte level) {
        if(level<0) {
            return 'x';
        } else {
            return (char)('0'+level);
        }
    }

    private static int getDirectionBits(byte actualLevels[]) {
        int actualDirectionBits=0;
        for(int i=0; i<actualLevels.length; ++i) {
            actualDirectionBits|=(1<<(actualLevels[i]&1));
        }
        return actualDirectionBits;
    }
    private boolean checkLevels(byte actualLevels[]) {
        boolean isOk=true;
        if(levelsCount!=actualLevels.length) {
            errln(" ------------ Wrong number of level values; expected "+levelsCount+" actual "+actualLevels.length);
            isOk=false;
        } else {
            for(int i=0; i<actualLevels.length; ++i) {
                if(levels[i]!=actualLevels[i] && levels[i]>=0) {
                    if(directionBits!=3 && directionBits==getDirectionBits(actualLevels)) {
                        // ICU used a shortcut:
                        // Since the text is unidirectional, it did not store the resolved
                        // levels but just returns all levels as the paragraph level 0 or 1.
                        // The reordering result is the same, so this is fine.
                        break;
                    } else {
                        errln(" ------------ Wrong level value at index "+i+"; expected "+levels[i]+" actual "+actualLevels[i]);
                        isOk=false;
                        break;
                    }
                }
            }
        }
        if(!isOk) {
            printErrorLine();
            StringBuilder els=new StringBuilder("Expected levels:   ");
            int i;
            for(i=0; i<levelsCount; ++i) {
                els.append(' ').append(printLevel(levels[i]));
            }
            StringBuilder als=new StringBuilder("Actual   levels:   ");
            for(i=0; i<actualLevels.length; ++i) {
                als.append(' ').append(printLevel(actualLevels[i]));
            }
            errln(els.toString());
            errln(als.toString());
        }
        return isOk;
    }

    // Note: ubidi_setReorderingOptions(ubidi, UBIDI_OPTION_REMOVE_CONTROLS);
    // does not work for custom BiDi class assignments
    // and anyway also removes LRM/RLM/ZWJ/ZWNJ which is not desirable here.
    // Therefore we just skip the indexes for BiDi controls while comparing
    // with the expected ordering that has them omitted.
    private boolean checkOrdering(Bidi ubidi) {
        if(orderingCount<0)
            return true;
        boolean isOk=true;
        int resultLength=ubidi.getResultLength();  // visual length including BiDi controls
        int i, visualIndex;
        // Note: It should be faster to call ubidi_countRuns()/ubidi_getVisualRun()
        // and loop over each run's indexes, but that seems unnecessary for this test code.
        for(i=visualIndex=0; i<resultLength; ++i) {
            int logicalIndex=ubidi.getLogicalIndex(i);
            if(levels[logicalIndex]<0) {
                continue;  // BiDi control, omitted from expected ordering.
            }
            if(visualIndex<orderingCount && logicalIndex!=ordering[visualIndex]) {
                errln(" ------------ Wrong ordering value at visual index "+visualIndex+"; expected "+
                      ordering[visualIndex]+" actual "+logicalIndex);
                isOk=false;
                break;
            }
            ++visualIndex;
        }
        // visualIndex is now the visual length minus the BiDi controls,
        // which should match the length of the BidiTest.txt ordering.
        if(isOk && orderingCount!=visualIndex) {
            errln(" ------------ Wrong number of ordering values; expected "+orderingCount+" actual "+visualIndex);
            isOk=false;
        }
        if(!isOk) {
            printErrorLine();
            StringBuilder eord=new StringBuilder("Expected ordering: ");
            for(i=0; i<orderingCount; ++i) {
                eord.append(' ').append((char)('0'+ordering[i]));
            }
            StringBuilder aord=new StringBuilder("Actual   ordering: ");
            for(i=0; i<resultLength; ++i) {
                int logicalIndex=ubidi.getLogicalIndex(i);
                if(levels[logicalIndex]<Bidi.LEVEL_DEFAULT_LTR) {
                    aord.append(' ').append((char)('0'+logicalIndex));
                }
            }
            errln(eord.toString());
            errln(aord.toString());
        }
        return isOk;
    }

    private void printErrorLine() {
        ++errorCount;
        errln(String.format("Input line %5d:   %s", lineNumber, line));
        errln("Input string:       "+inputString);
        errln("Para level:         "+paraLevelName);
    }

    private static boolean isInvWhitespace(char c) {
        return ((c)==' ' || (c)=='\t' || (c)=='\r' || (c)=='\n');
    }
    /**
     * Skip isInvWhitespace() characters.
     * @return true if line.charAt[lineIndex] is a non-whitespace, false if lineIndex>=line.length()
     */
    private boolean skipWhitespace() {
        while(lineIndex<line.length()) {
            if(!isInvWhitespace(line.charAt(lineIndex))) {
                return true;
            }
            ++lineIndex;
        }
        return false;
    }

    private String line;
    private int lineIndex;
    private byte levels[]=new byte[1000];  // UBiDiLevel
    private int directionBits;
    private int ordering[]=new int[1000];
    private int lineNumber;
    private int levelsCount;
    private int orderingCount;
    private int errorCount;
    private String inputString;
    private String paraLevelName;
    private StringBuilder inputStringBuilder=new StringBuilder();
}
