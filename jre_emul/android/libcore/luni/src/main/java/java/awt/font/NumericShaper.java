/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Ilya S. Okomin
 * @version $Revision$
 */
package java.awt.font;

import java.io.IOException;
import java.io.Serializable;

/**
 * The Class NumericShaper provides methods to convert latin character codes
 * to unicode character codes.
 * For tables of the character codes used,
 * see <a href="http://www.unicode.org/Public/UNIDATA/">unicode.org</a>.
 */
public final class NumericShaper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8022764705923730308L;

    /**
     * The Constant EUROPEAN indicates the latin and extended range,
     * and latin decimal base.
     */
    public static final int EUROPEAN = 1;

    /** The Constant ARABIC indicates the ARABIC range and decimal base. */
    public static final int ARABIC = 2;

    /**
     * The Constant EASTERN_ARABIC indicates the ARABIC range and
     * ARABIC_EXTENDED decimal base.
     */
    public static final int EASTERN_ARABIC = 4;

    /**
     * The Constant DEVANAGARI indicates the DEVANAGARI range and
     * decimal base.
     */
    public static final int DEVANAGARI = 8;

    /** The Constant BENGALI indicates the BENGALI range and decimal base. */
    public static final int BENGALI = 16;

    /** The Constant GURMUKHI indicates the GURMUKHI range and decimal base. */
    public static final int GURMUKHI = 32;

    /** The Constant GUJARATI indicates the GUJARATI range and decimal base. */
    public static final int GUJARATI = 64;

    /** The Constant ORIYA indicates the ORIYA range and decimal base. */
    public static final int ORIYA = 128;

    /** The Constant TAMIL indicates the TAMIL range and decimal base. */
    public static final int TAMIL = 256;

    /** The Constant TELUGU indicates the TELUGU range and decimal base. */
    public static final int TELUGU = 512;

    /** The Constant KANNADA indicates the KANNADA range and decimal base. */
    public static final int KANNADA = 1024;

    /**
     * The Constant MALAYALAM indicates the MALAYALAM range and decimal base.
     */
    public static final int MALAYALAM = 2048;

    /** The Constant THAI indicates the THAI range and decimal base. */
    public static final int THAI = 4096;

    /** The Constant LAO indicates the LAO range and decimal base. */
    public static final int LAO = 8192;

    /** The Constant TIBETAN indicates the TIBETAN range and decimal base. */
    public static final int TIBETAN = 16384;

    /** The Constant MYANMAR indicates the MYANMAR range and decimal base. */
    public static final int MYANMAR = 32768;

    /**
     * The Constant ETHIOPIC indicates the ETHIOPIC range and decimal base.
     */
    public static final int ETHIOPIC = 65536;

    /** The Constant KHMER indicates the KHMER range and decimal base. */
    public static final int KHMER = 131072;

    /**
     * The Constant MONGOLIAN indicates the MONGOLIAN range and
     * decimal base.
     */
    public static final int MONGOLIAN = 262144;

    /** The Constant ALL_RANGES indicates all ranges. */
    public static final int ALL_RANGES = 524287;

    // Index of the EUROPEAN range
    /** The Constant INDEX_EUROPEAN. */
    private static final int INDEX_EUROPEAN = 0;

    // Index of the ETHIOPIC range
    /** The Constant INDEX_ETHIOPIC. */
    private static final int INDEX_ETHIOPIC = 16;

    // Maximum index that range can't exceed
    /** The Constant MAX_INDEX. */
    private static final int MAX_INDEX = 19;

    /*
     * Scripts ranges array. Array represents ranges as pairs of
     * lowest and highest range bounds.
     * Data is taken from the UnicodeData.txt file from
     * http://www.unicode.org/Public/UNIDATA/
     */
    /** The scripts ranges. */
    private final int[] scriptsRanges = {
            0x0000, 0x024F,     // EUROPEAN (basic latin + latin-1 + extended)
            0x0600, 0x06FF,     // ARABIC
            0x0600, 0x06FF,     // EASTERN_ARABIC (XXX: diff with ARABIC ? )
            0x0900, 0x097F,     // DEVANAGARI
            0x0980, 0x09FF,     // BENGALI
            0x0A00, 0x0A7F,     // GURMUKHI
            0x0A80, 0x0AFF,     // GUJARATI
            0x0B00, 0x0B7F,     // ORIYA
            0x0B80, 0x0BFF,     // TAMIL
            0x0C00, 0x0C7F,     // TELUGU
            0x0C80, 0x0CFF,     // KANNADA
            0x0D00, 0x0D7F,     // MALAYALAM
            0x0E00, 0x0E7F,     // THAI
            0x0E80, 0x0EFF,     // LAO
            0x0F00, 0x0FFF,     // TIBETAN
            0x1000, 0x109F,     // MYANMAR
            0x1200, 0x137F,     // ETHIOPIC
            0x1780, 0x17FF,     // KHMER
            0x1800, 0x18AF      // MONGOLIAN
    };

    /*
     * Digit low ranges values decreased by 0x0030. Each low range
     * value decreased by 0x0030 for easy obtaing unicode value of the
     * context dependent digit. European digits starts from 0x0030 hence
     * context dependent unicode digit value equals to
     *      digitsLowRanges[script index] + european digit char unicode value.
     * !! the only exception is ETHIOPIC script where there is no '0' digit
     * Data is taken from the UnicodeData.txt file from
     * http://www.unicode.org/Public/UNIDATA/
     */
    /** The digits low ranges. */
    private final int[] digitsLowRanges = {
            0x0000,             // EUROPEAN
            0x0630,             // ARABIC
            0x0630,             // EASTERN_ARABIC
            0x0936,             // DEVANAGARI
            0x09B6,             // BENGALI
            0x0A36,             // GURMUKHI
            0x0AB6,             // GUJARATI
            0x0B36,             // ORIYA
            0x0BB6,             // TAMIL
            0x0C36,             // TELUGU
            0x0CB6,             // KANNADA
            0x0D36,             // MALAYALAM
            0x0E20,             // THAI
            0x0EA0,             // LAO
            0x0EF0,             // TIBETAN
            0x1010,             // MYANMAR
            0x1338,             // ETHIOPIC - (low range-1) no ETHIOPIC '0' DIGIT!
            0x17B0,             // KHMER
            0x17E0              // MONGOLIAN
    };

    // Set of context names used in toString method
    /** The contexts. */
    private final String[] contexts = {
            "EUROPEAN",
            "ARABIC",
            "EASTERN_ARABIC",
            "DEVANAGARI",
            "BENGALI",
            "GURMUKHI",
            "GUJARATI",
            "ORIYA",
            "TAMIL",
            "TELUGU",
            "KANNADA",
            "MALAYALAM",
            "THAI",
            "LAO",
            "TIBETAN",
            "MYANMAR",
            "ETHIOPIC",
            "KHMER",
            "MONGOLIAN"
    };

    /*
     * Strong characters flags array is to determine if the
     * unicode bidirectional category of the character is strong,
     * according to Unicode specification. If the bit with index equals to
     * character's unicode value is 1 - the character is strong.
     * This array was generated using UnicodeData.txt file from
     * http://www.unicode.org/Public/UNIDATA/
     */

    /** The Constant STRONG_TEXT_FLAGS. */
    /*-[
    	static int STRONG_TEXT_FLAGS[] = { 0, 0, 134217726, 134217726,
            0, 69207040, -8388609, -8388609, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -65533, -1, -1, -100663297, 196611, 16415, 0, 0, 0,
            67108864, -10432, -5, -32769, -4194305, -1, -1, -1, -1, -1017, -1,
            -32769, 67108863, 65535, -131072, -25165825, -2, 767, 1073741824,
            -65463, 2033663, -939513841, 134217726, 2047, -73728, -1, -1,
            541065215, -67059616, -180225, 65535, -8192, 16383, -1, 131135, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, -8, -469762049, -16703999, 537001971,
            -417812, -473563649, -1333765759, 133431235, -423960, -1016201729,
            1577058305, 1900480, -278552, -470942209, 72193, 65475, -417812,
            1676541439, -1333782143, 262083, -700594200, -1006647528, 8396230,
            524224, -139282, 66059775, 30, 65475, -139284, -470811137,
            1080036831, 65475, -139284, -1006633473, 8396225, 65475, -58720276,
            805044223, -16547713, 1835008, -2, 917503, 268402815, 0, -17816170,
            537783470, 872349791, 0, -50331649, -1050673153, -257, -2147481601,
            3872, -1073741824, 237503, 0, -1, 16914171, 16777215, 0, 0, -1,
            -65473, 536870911, -1, -1, -2080374785, -1, -1, -249, -1, 67108863,
            -1, -1, 1031749119, -1, -49665, 2134769663, -8388803, -1,
            -12713985, -1, 134217727, 536870911, 65535, -1, -1, 2097151, -2,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 8388607, 134217726, -1, -1, 131071, 253951, 6553599, 262143,
            122879, -1, -1065353217, 401605055, 1023, 67043328, -1, -1,
            16777215, -1, 511, 0, 0, 536870911, 33226872, -64, 2047999, -1,
            -64513, 67044351, 0, -830472193, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0,
            -1, -1, -1, -1, 268435455, -1, -1, 67108863, 1061158911, -1,
            -1426112705, 1073741823, -1, 1608515583, 265232348, 534519807,
            49152, 27648, 0, -2147352576, 2031616, 0, 0, 0, 1043332228,
            -201605808, 992, -1, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            -4194304, -1, 134217727, 2097152, 0, 0, 0, 0, 0, 0, 0, -268435456,
            -1, -1, 1023, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4096, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1,
            -32769, 2147483647, 0, -1, -1, -1, 31, -1, -65473, -1, 32831,
            8388607, 2139062143, 2139062143, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 224, 524157950, -2, -1, -528482305, -2, -1,
            -134217729, -32, -122881, -1, -1, -32769, 16777215, 0, -65536,
            536870911, -1, 15, -1879048193, -1, 131071, -61441, 2147483647, -1,
            -1, -1, -125829121, -1, -1, 1073741823, 2147483647, 1, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 2097152, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            134217728, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, 8191, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2117, 159, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 8, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, 1, 0, 0, -2147483648,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2147483648, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -49153, -1, -63489, -1, -1, 67108863, 0,
            -1594359681, 1602223615, -37, -1, -1, 262143, -524288, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, 1073741823, -65536, -1, -196609, -1,
            255, 536805376, 0, 0, 0, -2162688, -1, -1, -1, 536870911, 0,
            134217726, 134217726, -64, -1, 2147483647, 486341884, 0
    	};
    ]-*/

    // index of context range (Serialization support)
    /** The key. */
    private int key;

    // flag, true if shaping contextual (Serialization support)
    /** The mask. */
    private int mask;

    // ranges to be shaped
    /** The ranges. */
    private int fRanges;

    // index of the default context
    /** The default context index. */
    private int fDefaultContextIndex;

    // flag if NumericShaper shapes contextually
    /** The contextual. */
    private boolean fContextual;

    // uses for non-context dependent case only
    /** The single range index. */
    private int fSingleRangeIndex;

    /**
     * Creates NumericShaper with specified parameters.
     *
     * @param ranges specified ranges to be shaped
     * @param defaultContext default context range
     * @param isContextual specifies if the instance is contextual
     */
    private NumericShaper(int ranges, int defaultContext, boolean isContextual){
        this.fRanges = ranges;
        this.fDefaultContextIndex = getIndexFromRange(defaultContext);
        this.fContextual = isContextual;

        if (!fContextual){
            fSingleRangeIndex = getIndexFromRange(ranges);
        }
    }

    /**
     * Returns script index of the specified context range.
     *
     * @param range specified range
     *
     * @return one of the script indices according to the specified range.
     */
    private int getIndexFromRange(int range){
        if (range == 0) {
            throw rangeException(range);
        }

        int index = 0;
        while (index < MAX_INDEX){
            if (range == (1 << index)){
                return index;
            }
            index++;
        }

        throw rangeException(range);
    }

    private static IllegalArgumentException rangeException(int value) {
        throw new IllegalArgumentException("Illegal range argument value: " + value);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + fRanges;
        result = 31 * result + fDefaultContextIndex;
        result = 31 * result + (fContextual ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        try {
            NumericShaper ns = (NumericShaper)obj;
            return (fRanges == ns.fRanges &&
                    fDefaultContextIndex == ns.fDefaultContextIndex &&
                    fContextual == ns.fContextual);
        } catch (ClassCastException e){
        }

        return false;
    }

    @Override
    public String toString() {
        /* !! There is no description in the documentation what this method must
         * return. Thus format of toString method is based on 1.5 release
         * behavior and can be obtained using next test sample:
         *
         * // Simple shapers toString format
         * System.out.println(NumericShaper.getShaper(NumericShaper.EASTERN_ARABIC));
         *
         * // Context shapers with default context toString format
         * System.out.println(NumericShaper.getContextualShaper(
         *      NumericShaper.ARABIC | NumericShaper.TAMIL));
         *
         * // Context shapers with context
         * System.out.println(NumericShaper.getContextualShaper(
         *      NumericShaper.ARABIC | NumericShaper.TAMIL,
         *      NumericShaper.EASTERN_ARABIC));
         */
        StringBuilder sb = new StringBuilder(super.toString());

        sb.append("[contextual:");
        sb.append(fContextual);

        if (fContextual){
            sb.append(", context:");
            sb.append(contexts[fDefaultContextIndex]);
        }

        sb.append(", range(s): ");
        if (fContextual) {
            int index = 0;
            boolean isFirst = true;
            while (index < MAX_INDEX){
                if ((fRanges & (1 << index)) != 0){
                    if (isFirst){
                        isFirst = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(contexts[index]);
                }
                index++;
            }
        } else {
            sb.append(contexts[fSingleRangeIndex]);
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Gets the NumericShaper for the specified unicode ranges
     * and default unicode range. The defaultContext parameter
     * is used as the starting context (which indicates the
     * language/script being used). The OR logical operation
     * should be used for multiple ranges:
     * NumericShaper.DEVANAGARI | NumericShaper.BENGALI.
     * The NumericShaper returned by this method is contextual
     * in that it supports multiple character ranges, depending
     * on the context.
     *
     * @param ranges the unicode ranges.
     * @param defaultContext the default, starting context.
     *
     * @return the NumericShaper for the specified ranges.
     */
    public static NumericShaper getContextualShaper(int ranges,
            int defaultContext) {
        ranges &= ALL_RANGES;
        defaultContext &= ALL_RANGES;
        return new NumericShaper(ranges, defaultContext, true);
    }

    /**
     * Gets the NumericShaper for the specified unicode ranges.
     * The OR logical operation should be used for multiple ranges:
     * NumericShaper.DEVANAGARI | NumericShaper.BENGALI.
     * The NumericShaper returned by this method is contextual
     * in that it supports multiple character ranges, depending
     * on the context.
     *
     * @param ranges the unicode ranges.
     *
     * @return the NumericShaper for the specified ranges.
     */
    public static NumericShaper getContextualShaper(int ranges) {
        ranges &= ALL_RANGES;
        return new NumericShaper(ranges, EUROPEAN, true);
    }

    /**
     * Gets the masks for all of the ranges supported by this NumericShaper,
     * packed into an int value using the logical OR logical operation
     * for multiple ranges:
     * NumericShaper.DEVANAGARI | NumericShaper.BENGALI.
     *
     * @return all ranges of this NumericShaper.
     */
    public int getRanges() {
        return fRanges;
    }

    /**
     * Gets a NumericShaper for the specified unicode range.
     * The NumericShaper supports only a single range and
     * hence is not contextual.
     *
     * @param singleRange the specified unicode single range.
     *
     * @return the NumericShaper for the specified unicode range.
     */
    public static NumericShaper getShaper(int singleRange) {
        singleRange &= ALL_RANGES;
        return new NumericShaper(singleRange, EUROPEAN, false);
    }

    /**
     * Checks if this NumericShaper is contextual (supporting
     * multiple script ranges) or not.
     *
     * @return true, if this NumericShaper is contextual, false otherwise.
     */
    public boolean isContextual() {
        return fContextual;
    }

    /**
     * Transforms the encoding of the text, starting from the character
     * at index start and transforming count characters,
     * using the specified context.
     *
     * @param text the text to be shaped.
     * @param start the start offset of the text.
     * @param count the number of characters to be shaped.
     * @param context the context to be used for shaping.
     */
    public void shape(char[] text, int start, int count, int context) {
        if (isContextual()){
            contextualShape(text, start, count, getIndexFromRange(context));
        } else {
            nonContextualShape(text, start, count);
        }
    }

    /**
     * Transforms the encoding of the text, starting from the character
     * at index start and transforming count characters.
     *
     * @param text the text to be shaped.
     * @param start the start offset of the text.
     * @param count the number of characters to be shaped.
     */
    public void shape(char[] text, int start, int count) {
        if (isContextual()){
            contextualShape(text, start, count, fDefaultContextIndex);
        } else {
            nonContextualShape(text, start, count);
        }
    }

    /**
     * Converts count of digits of the given array of characters from the start
     * index using specified context. This method is applied for the contextual
     * shaping, if the shaper instance is not contextual use nonContextualShape
     * method.
     *
     * @param text an array of chars
     * @param start index of the first character to convert
     * @param count a number of characters to convert
     * @param contextIndex index of the script index to use in shaper
     */
    private void contextualShape(char[] text, int start, int count,
            int contextIndex){
        char maxDigit = (char)0x0039;
        char minDigit = (char)0x0030;

        int currIndex;
        if (((1 << contextIndex) & fRanges) == 0 ){
            currIndex = INDEX_EUROPEAN;
        } else {
            currIndex = contextIndex;
        }

        for (int ind = start; ind < start + count; ind++){
            if (minDigit <= text[ind] && text[ind] <= maxDigit){
                if (currIndex != INDEX_ETHIOPIC || text[ind] != '0'){
                    text[ind] = (char)(digitsLowRanges[currIndex] + text[ind]);
                }
            } else {
                if(isCharStrong(text[ind])){
                    int index = getCharIndex(text[ind]);
                    if (currIndex != index){
                        if (((1 << index) & fRanges) != 0){
                            currIndex = index;
                        } else {
                            currIndex = INDEX_EUROPEAN;
                        }
                    }
                }
            }
        }

    }

    /**
     * Converts count of digits of the given array of characters from the start
     * index. Method is applied for non-contextual shaper.
     *
     * @param text an array of chars
     * @param start index of the first character to convert
     * @param count a number of characters to convert
     */
    private void nonContextualShape(char[] text, int start, int count){
        char maxDigit = (char)0x0039;
        char minDigit = (char)((fRanges == ETHIOPIC) ? 0x0031 : 0x0030);
        for (int ind = start; ind < start + count; ind++){
            if (minDigit <= text[ind] && text[ind] <= maxDigit){
                    text[ind] = (char)(digitsLowRanges[fSingleRangeIndex] + text[ind]);
            }
        }

    }

    /**
     * Returns the index of the script of the specified char.
     *
     * @param ch specified unicode character
     *
     * @return script index corresponding to the given char
     */
    private int getCharIndex(char ch){
        int index = INDEX_EUROPEAN;
        for (int i=0; i < MAX_INDEX; i++){
            int j = i * 2;
            if (scriptsRanges[j] <= ch && ch <= scriptsRanges[j+1]){
                return i;
            }
        }

        return index;
    }

    /**
     * Returns true if the bidirectional category of the character
     * is strong.
     *
     * @param chr the chr
     *
     * @return true, if the character is strong, false otherwise
     */
    private native boolean isCharStrong(int chr) /*-[
        return (STRONG_TEXT_FLAGS[chr >> 5] & (1 << (chr % 32))) != 0;
    ]-*/;

    /**
     * Updates all private serialized fields for object to be correctly serialized
     * according to the serialized form of this class mentioned in the
     * documentation.
     */
     private void updateRangesFields(){
        fRanges = (mask & ~(1 << 31));
        fContextual = ((mask &(1 << 31)) != 0);
        if (fContextual){
            fRanges = (mask & ~(1 << 31));
            fDefaultContextIndex = key;
        } else {
            fRanges = mask;
            fSingleRangeIndex = key;
        }
    }

    /**
     * Updates private fields for object after deserialization
     * according to the serialized form of this class mentioned in the
     * documentation.
     */
    private void updateKeyMaskFields(){
        mask = fRanges;
        if (fContextual){
            mask |= (1 << 31);
            key = fDefaultContextIndex;
        } else{
            key = fSingleRangeIndex;
        }
    }

    /**
     * Write object.
     *
     * @param out the out
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(java.io.ObjectOutputStream out)
                                throws IOException{
        updateKeyMaskFields();
        out.defaultWriteObject();
    }

    /**
     * Read object.
     *
     * @param in the in
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(java.io.ObjectInputStream in)
                                throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        updateRangesFields();
    }

}
