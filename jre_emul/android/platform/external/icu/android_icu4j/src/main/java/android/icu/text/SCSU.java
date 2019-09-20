/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.text;

/**
 * An interface defining  constants for the Standard Compression Scheme
 * for Unicode (SCSU) as outlined in <A
 * HREF="http://www.unicode.org/unicode/reports/tr6">Unicode Technical
 * Report #6</A>.
 *
 * @author Stephen F. Booth
 * @version 1.1 05 Aug 99
 * @version 1.0 26 Jul 99
 */
interface SCSU
{
    //==========================
    // Generic window shift
    //==========================
    final static int COMPRESSIONOFFSET           = 0x80;

    //==========================
    // Number of windows
    //==========================
    final static int NUMWINDOWS                  = 8;
    final static int NUMSTATICWINDOWS            = 8;

    //==========================
    // Indicates a window index is invalid
    //==========================
    final static int INVALIDWINDOW               = -1;

    //==========================
    // Indicates a character doesn't exist in input (past end of buffer)
    //==========================
    final static int INVALIDCHAR                 = -1;

    //==========================
    // Compression modes
    //==========================
    final static int SINGLEBYTEMODE              = 0;
    final static int UNICODEMODE                 = 1;

    //==========================
    // Maximum value for a window's index
    //==========================
    final static int MAXINDEX                    = 0xFF;

    //==========================
    // Reserved index value (characters belongs to first block)
    //==========================
    final static int RESERVEDINDEX               = 0x00;

    //==========================
    // Indices for scripts which cross a half-block boundary
    //==========================
    final static int LATININDEX                  = 0xF9;
    final static int IPAEXTENSIONINDEX           = 0xFA;
    final static int GREEKINDEX                  = 0xFB;
    final static int ARMENIANINDEX               = 0xFC;
    final static int HIRAGANAINDEX               = 0xFD;
    final static int KATAKANAINDEX               = 0xFE;
    final static int HALFWIDTHKATAKANAINDEX      = 0xFF;

    //==========================
    // Single-byte mode tags
    //==========================
    final static int SDEFINEX                    = 0x0B;
    final static int SRESERVED                   = 0x0C;  // reserved value
    final static int SQUOTEU                     = 0x0E;
    final static int SCHANGEU                    = 0x0F;

    final static int SQUOTE0                     = 0x01;
    final static int SQUOTE1                     = 0x02;
    final static int SQUOTE2                     = 0x03;
    final static int SQUOTE3                     = 0x04;
    final static int SQUOTE4                     = 0x05;
    final static int SQUOTE5                     = 0x06;
    final static int SQUOTE6                     = 0x07;
    final static int SQUOTE7                     = 0x08;

    final static int SCHANGE0                    = 0x10;
    final static int SCHANGE1                    = 0x11;
    final static int SCHANGE2                    = 0x12;
    final static int SCHANGE3                    = 0x13;
    final static int SCHANGE4                    = 0x14;
    final static int SCHANGE5                    = 0x15;
    final static int SCHANGE6                    = 0x16;
    final static int SCHANGE7                    = 0x17;

    final static int SDEFINE0                    = 0x18;
    final static int SDEFINE1                    = 0x19;
    final static int SDEFINE2                    = 0x1A;
    final static int SDEFINE3                    = 0x1B;
    final static int SDEFINE4                    = 0x1C;
    final static int SDEFINE5                    = 0x1D;
    final static int SDEFINE6                    = 0x1E;
    final static int SDEFINE7                    = 0x1F;

    //==========================
    // Unicode mode tags
    //==========================
    final static int UCHANGE0                    = 0xE0;
    final static int UCHANGE1                    = 0xE1;
    final static int UCHANGE2                    = 0xE2;
    final static int UCHANGE3                    = 0xE3;
    final static int UCHANGE4                    = 0xE4;
    final static int UCHANGE5                    = 0xE5;
    final static int UCHANGE6                    = 0xE6;
    final static int UCHANGE7                    = 0xE7;

    final static int UDEFINE0                    = 0xE8;
    final static int UDEFINE1                    = 0xE9;
    final static int UDEFINE2                    = 0xEA;
    final static int UDEFINE3                    = 0xEB;
    final static int UDEFINE4                    = 0xEC;
    final static int UDEFINE5                    = 0xED;
    final static int UDEFINE6                    = 0xEE;
    final static int UDEFINE7                    = 0xEF;

    final static int UQUOTEU                     = 0xF0;
    final static int UDEFINEX                    = 0xF1;
    final static int URESERVED                   = 0xF2;  // reserved value


    //==========================
    // Class variables
    //==========================

    /** For window offset mapping */
    final static int [] sOffsetTable = { 
        // table generated by CompressionTableGenerator
        0x0, 0x80, 0x100, 0x180, 0x200, 0x280, 0x300, 0x380, 0x400,
    0x480, 0x500, 0x580, 0x600, 0x680, 0x700, 0x780, 0x800, 0x880,
    0x900, 0x980, 0xa00, 0xa80, 0xb00, 0xb80, 0xc00, 0xc80, 0xd00,
    0xd80, 0xe00, 0xe80, 0xf00, 0xf80, 0x1000, 0x1080, 0x1100,
    0x1180, 0x1200, 0x1280, 0x1300, 0x1380, 0x1400, 0x1480,
    0x1500, 0x1580, 0x1600, 0x1680, 0x1700, 0x1780, 0x1800,
    0x1880, 0x1900, 0x1980, 0x1a00, 0x1a80, 0x1b00, 0x1b80,
    0x1c00, 0x1c80, 0x1d00, 0x1d80, 0x1e00, 0x1e80, 0x1f00,
    0x1f80, 0x2000, 0x2080, 0x2100, 0x2180, 0x2200, 0x2280,
    0x2300, 0x2380, 0x2400, 0x2480, 0x2500, 0x2580, 0x2600,
    0x2680, 0x2700, 0x2780, 0x2800, 0x2880, 0x2900, 0x2980,
    0x2a00, 0x2a80, 0x2b00, 0x2b80, 0x2c00, 0x2c80, 0x2d00,
    0x2d80, 0x2e00, 0x2e80, 0x2f00, 0x2f80, 0x3000, 0x3080,
    0x3100, 0x3180, 0x3200, 0x3280, 0x3300, 0x3380, 0xe000,
    0xe080, 0xe100, 0xe180, 0xe200, 0xe280, 0xe300, 0xe380,
    0xe400, 0xe480, 0xe500, 0xe580, 0xe600, 0xe680, 0xe700,
    0xe780, 0xe800, 0xe880, 0xe900, 0xe980, 0xea00, 0xea80,
    0xeb00, 0xeb80, 0xec00, 0xec80, 0xed00, 0xed80, 0xee00,
    0xee80, 0xef00, 0xef80, 0xf000, 0xf080, 0xf100, 0xf180,
    0xf200, 0xf280, 0xf300, 0xf380, 0xf400, 0xf480, 0xf500,
    0xf580, 0xf600, 0xf680, 0xf700, 0xf780, 0xf800, 0xf880,
    0xf900, 0xf980, 0xfa00, 0xfa80, 0xfb00, 0xfb80, 0xfc00,
    0xfc80, 0xfd00, 0xfd80, 0xfe00, 0xfe80, 0xff00, 0xff80, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0xc0, 0x250, 0x370,
    0x530, 0x3040, 0x30a0, 0xff60  
    };

    /** Static compression window offsets */
    final static int [] sOffsets = {
        0x0000,  // for quoting single-byte mode tags
        0x0080,  // Latin-1 Supplement
        0x0100,  // Latin Extended-A
        0x0300,  // Combining Diacritical Marks
        0x2000,  // General Punctuation
        0x2080,  // Curency Symbols
        0x2100,  // Letterlike Symbols and Number Forms
        0x3000   // CJK Symbols and Punctuation
    };

}

