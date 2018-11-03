/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.impl;

import com.google.j2objc.annotations.WeakOuter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.MissingResourceException;

import android.icu.lang.UCharacter;
import android.icu.lang.UCharacter.HangulSyllableType;
import android.icu.lang.UCharacter.NumericType;
import android.icu.lang.UCharacterCategory;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import android.icu.util.VersionInfo;

/**
* <p>Internal class used for Unicode character property database.</p>
* <p>This classes store binary data read from uprops.icu.
* It does not have the capability to parse the data into more high-level
* information. It only returns bytes of information when required.</p>
* <p>Due to the form most commonly used for retrieval, array of char is used
* to store the binary data.</p>
* <p>UCharacterPropertyDB also contains information on accessing indexes to
* significant points in the binary data.</p>
* <p>Responsibility for molding the binary data into more meaning form lies on
* <a href=UCharacter.html>UCharacter</a>.</p>
* @author Syn Wee Quek
* @hide Only a subset of ICU is exposed in Android
*/

public final class UCharacterProperty
{
    // public data members -----------------------------------------------

    /*
     * public singleton instance
     */
    public static final UCharacterProperty INSTANCE;

    /**
    * Trie data
    */
    public Trie2_16 m_trie_;
    /**
    * Unicode version
    */
    public VersionInfo m_unicodeVersion_;
    /**
    * Latin capital letter i with dot above
    */
    public static final char LATIN_CAPITAL_LETTER_I_WITH_DOT_ABOVE_ = 0x130;
    /**
    * Latin small letter i with dot above
    */
    public static final char LATIN_SMALL_LETTER_DOTLESS_I_ = 0x131;
    /**
    * Latin lowercase i
    */
    public static final char LATIN_SMALL_LETTER_I_ = 0x69;
    /**
    * Character type mask
    */
    public static final int TYPE_MASK = 0x1F;

    // uprops.h enum UPropertySource --------------------------------------- ***

    /** No source, not a supported property. */
    public static final int SRC_NONE=0;
    /** From uchar.c/uprops.icu main trie */
    public static final int SRC_CHAR=1;
    /** From uchar.c/uprops.icu properties vectors trie */
    public static final int SRC_PROPSVEC=2;
    /** From unames.c/unames.icu */
    public static final int SRC_NAMES=3;
    /** From ucase.c/ucase.icu */
    public static final int SRC_CASE=4;
    /** From ubidi_props.c/ubidi.icu */
    public static final int SRC_BIDI=5;
    /** From uchar.c/uprops.icu main trie as well as properties vectors trie */
    public static final int SRC_CHAR_AND_PROPSVEC=6;
    /** From ucase.c/ucase.icu as well as unorm.cpp/unorm.icu */
    public static final int SRC_CASE_AND_NORM=7;
    /** From normalizer2impl.cpp/nfc.nrm */
    public static final int SRC_NFC=8;
    /** From normalizer2impl.cpp/nfkc.nrm */
    public static final int SRC_NFKC=9;
    /** From normalizer2impl.cpp/nfkc_cf.nrm */
    public static final int SRC_NFKC_CF=10;
    /** From normalizer2impl.cpp/nfc.nrm canonical iterator data */
    public static final int SRC_NFC_CANON_ITER=11;
    /** One more than the highest UPropertySource (SRC_) constant. */
    public static final int SRC_COUNT=12;

    // public methods ----------------------------------------------------

    /**
    * Gets the main property value for code point ch.
    * @param ch code point whose property value is to be retrieved
    * @return property value of code point
    */
    public final int getProperty(int ch)
    {
        return m_trie_.get(ch);
    }

    /**
     * Gets the unicode additional properties.
     * Java version of C u_getUnicodeProperties().
     * @param codepoint codepoint whose additional properties is to be
     *                  retrieved
     * @param column The column index.
     * @return unicode properties
     */
    public int getAdditional(int codepoint, int column) {
        assert column >= 0;
        if (column >= m_additionalColumnsCount_) {
            return 0;
        }
        return m_additionalVectors_[m_additionalTrie_.get(codepoint) + column];
    }

    static final int MY_MASK = UCharacterProperty.TYPE_MASK
        & ((1<<UCharacterCategory.UPPERCASE_LETTER) |
            (1<<UCharacterCategory.LOWERCASE_LETTER) |
            (1<<UCharacterCategory.TITLECASE_LETTER) |
            (1<<UCharacterCategory.MODIFIER_LETTER) |
            (1<<UCharacterCategory.OTHER_LETTER));


       /**
     * <p>Get the "age" of the code point.</p>
     * <p>The "age" is the Unicode version when the code point was first
     * designated (as a non-character or for Private Use) or assigned a
     * character.</p>
     * <p>This can be useful to avoid emitting code points to receiving
     * processes that do not accept newer characters.</p>
     * <p>The data is from the UCD file DerivedAge.txt.</p>
     * <p>This API does not check the validity of the codepoint.</p>
     * @param codepoint The code point.
     * @return the Unicode version number
     */
    public VersionInfo getAge(int codepoint)
    {
        int version = getAdditional(codepoint, 0) >> AGE_SHIFT_;
        return VersionInfo.getInstance(
                           (version >> FIRST_NIBBLE_SHIFT_) & LAST_NIBBLE_MASK_,
                           version & LAST_NIBBLE_MASK_, 0, 0);
    }

    private static final int GC_CN_MASK = getMask(UCharacter.UNASSIGNED);
    private static final int GC_CC_MASK = getMask(UCharacter.CONTROL);
    private static final int GC_CS_MASK = getMask(UCharacter.SURROGATE);
    private static final int GC_ZS_MASK = getMask(UCharacter.SPACE_SEPARATOR);
    private static final int GC_ZL_MASK = getMask(UCharacter.LINE_SEPARATOR);
    private static final int GC_ZP_MASK = getMask(UCharacter.PARAGRAPH_SEPARATOR);
    /** Mask constant for multiple UCharCategory bits (Z Separators). */
    private static final int GC_Z_MASK = GC_ZS_MASK|GC_ZL_MASK|GC_ZP_MASK;

    /**
     * Checks if c is in
     * [^\p{space}\p{gc=Control}\p{gc=Surrogate}\p{gc=Unassigned}]
     * with space=\p{Whitespace} and Control=Cc.
     * Implements UCHAR_POSIX_GRAPH.
     * @hide draft / provisional / internal are hidden on Android
     */
    private static final boolean isgraphPOSIX(int c) {
        /* \p{space}\p{gc=Control} == \p{gc=Z}\p{Control} */
        /* comparing ==0 returns FALSE for the categories mentioned */
        return (getMask(UCharacter.getType(c))&
                (GC_CC_MASK|GC_CS_MASK|GC_CN_MASK|GC_Z_MASK))
               ==0;
    }

    // binary properties --------------------------------------------------- ***
    @WeakOuter
    private class BinaryProperty {
        int column;  // SRC_PROPSVEC column, or "source" if mask==0
        int mask;
        BinaryProperty(int column, int mask) {
            this.column=column;
            this.mask=mask;
        }
        BinaryProperty(int source) {
            this.column=source;
            this.mask=0;
        }
        final int getSource() {
            return mask==0 ? column : SRC_PROPSVEC;
        }
        boolean contains(int c) {
            // systematic, directly stored properties
            return (getAdditional(c, column)&mask)!=0;
        }
    }

    private class CaseBinaryProperty extends BinaryProperty {  // case mapping properties
        int which;
        CaseBinaryProperty(int which) {
            super(SRC_CASE);
            this.which=which;
        }
        @Override
        boolean contains(int c) {
            return UCaseProps.INSTANCE.hasBinaryProperty(c, which);
        }
    }

    private class NormInertBinaryProperty extends BinaryProperty {  // UCHAR_NF*_INERT properties
        int which;
        NormInertBinaryProperty(int source, int which) {
            super(source);
            this.which=which;
        }
        @Override
        boolean contains(int c) {
            return Norm2AllModes.getN2WithImpl(which-UProperty.NFD_INERT).isInert(c);
        }
    }

    BinaryProperty[] binProps={
        /*
         * Binary-property implementations must be in order of corresponding UProperty,
         * and there must be exactly one entry per binary UProperty.
         */
        new BinaryProperty(1, (1<<ALPHABETIC_PROPERTY_)),
        new BinaryProperty(1, (1<<ASCII_HEX_DIGIT_PROPERTY_)),
        new BinaryProperty(SRC_BIDI) {  // UCHAR_BIDI_CONTROL
            @Override
            boolean contains(int c) {
                return UBiDiProps.INSTANCE.isBidiControl(c);
            }
        },
        new BinaryProperty(SRC_BIDI) {  // UCHAR_BIDI_MIRRORED
            @Override
            boolean contains(int c) {
                return UBiDiProps.INSTANCE.isMirrored(c);
            }
        },
        new BinaryProperty(1, (1<<DASH_PROPERTY_)),
        new BinaryProperty(1, (1<<DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_)),
        new BinaryProperty(1, (1<<DEPRECATED_PROPERTY_)),
        new BinaryProperty(1, (1<<DIACRITIC_PROPERTY_)),
        new BinaryProperty(1, (1<<EXTENDER_PROPERTY_)),
        new BinaryProperty(SRC_NFC) {  // UCHAR_FULL_COMPOSITION_EXCLUSION
            @Override
            boolean contains(int c) {
                // By definition, Full_Composition_Exclusion is the same as NFC_QC=No.
                Normalizer2Impl impl=Norm2AllModes.getNFCInstance().impl;
                return impl.isCompNo(impl.getNorm16(c));
            }
        },
        new BinaryProperty(1, (1<<GRAPHEME_BASE_PROPERTY_)),
        new BinaryProperty(1, (1<<GRAPHEME_EXTEND_PROPERTY_)),
        new BinaryProperty(1, (1<<GRAPHEME_LINK_PROPERTY_)),
        new BinaryProperty(1, (1<<HEX_DIGIT_PROPERTY_)),
        new BinaryProperty(1, (1<<HYPHEN_PROPERTY_)),
        new BinaryProperty(1, (1<<ID_CONTINUE_PROPERTY_)),
        new BinaryProperty(1, (1<<ID_START_PROPERTY_)),
        new BinaryProperty(1, (1<<IDEOGRAPHIC_PROPERTY_)),
        new BinaryProperty(1, (1<<IDS_BINARY_OPERATOR_PROPERTY_)),
        new BinaryProperty(1, (1<<IDS_TRINARY_OPERATOR_PROPERTY_)),
        new BinaryProperty(SRC_BIDI) {  // UCHAR_JOIN_CONTROL
            @Override
            boolean contains(int c) {
                return UBiDiProps.INSTANCE.isJoinControl(c);
            }
        },
        new BinaryProperty(1, (1<<LOGICAL_ORDER_EXCEPTION_PROPERTY_)),
        new CaseBinaryProperty(UProperty.LOWERCASE),
        new BinaryProperty(1, (1<<MATH_PROPERTY_)),
        new BinaryProperty(1, (1<<NONCHARACTER_CODE_POINT_PROPERTY_)),
        new BinaryProperty(1, (1<<QUOTATION_MARK_PROPERTY_)),
        new BinaryProperty(1, (1<<RADICAL_PROPERTY_)),
        new CaseBinaryProperty(UProperty.SOFT_DOTTED),
        new BinaryProperty(1, (1<<TERMINAL_PUNCTUATION_PROPERTY_)),
        new BinaryProperty(1, (1<<UNIFIED_IDEOGRAPH_PROPERTY_)),
        new CaseBinaryProperty(UProperty.UPPERCASE),
        new BinaryProperty(1, (1<<WHITE_SPACE_PROPERTY_)),
        new BinaryProperty(1, (1<<XID_CONTINUE_PROPERTY_)),
        new BinaryProperty(1, (1<<XID_START_PROPERTY_)),
        new CaseBinaryProperty(UProperty.CASE_SENSITIVE),
        new BinaryProperty(1, (1<<S_TERM_PROPERTY_)),
        new BinaryProperty(1, (1<<VARIATION_SELECTOR_PROPERTY_)),
        new NormInertBinaryProperty(SRC_NFC, UProperty.NFD_INERT),
        new NormInertBinaryProperty(SRC_NFKC, UProperty.NFKD_INERT),
        new NormInertBinaryProperty(SRC_NFC, UProperty.NFC_INERT),
        new NormInertBinaryProperty(SRC_NFKC, UProperty.NFKC_INERT),
        new BinaryProperty(SRC_NFC_CANON_ITER) {  // UCHAR_SEGMENT_STARTER
            @Override
            boolean contains(int c) {
                return Norm2AllModes.getNFCInstance().impl.
                    ensureCanonIterData().isCanonSegmentStarter(c);
            }
        },
        new BinaryProperty(1, (1<<PATTERN_SYNTAX)),
        new BinaryProperty(1, (1<<PATTERN_WHITE_SPACE)),
        new BinaryProperty(SRC_CHAR_AND_PROPSVEC) {  // UCHAR_POSIX_ALNUM
            @Override
            boolean contains(int c) {
                return UCharacter.isUAlphabetic(c) || UCharacter.isDigit(c);
            }
        },
        new BinaryProperty(SRC_CHAR) {  // UCHAR_POSIX_BLANK
            @Override
            boolean contains(int c) {
                // "horizontal space"
                if(c<=0x9f) {
                    return c==9 || c==0x20; /* TAB or SPACE */
                } else {
                    /* Zs */
                    return UCharacter.getType(c)==UCharacter.SPACE_SEPARATOR;
                }
            }
        },
        new BinaryProperty(SRC_CHAR) {  // UCHAR_POSIX_GRAPH
            @Override
            boolean contains(int c) {
                return isgraphPOSIX(c);
            }
        },
        new BinaryProperty(SRC_CHAR) {  // UCHAR_POSIX_PRINT
            @Override
            boolean contains(int c) {
                /*
                 * Checks if codepoint is in \p{graph}\p{blank} - \p{cntrl}.
                 *
                 * The only cntrl character in graph+blank is TAB (in blank).
                 * Here we implement (blank-TAB)=Zs instead of calling u_isblank().
                 */
                return (UCharacter.getType(c)==UCharacter.SPACE_SEPARATOR) || isgraphPOSIX(c);
            }
        },
        new BinaryProperty(SRC_CHAR) {  // UCHAR_POSIX_XDIGIT
            @Override
            boolean contains(int c) {
                /* check ASCII and Fullwidth ASCII a-fA-F */
                if(
                    (c<=0x66 && c>=0x41 && (c<=0x46 || c>=0x61)) ||
                    (c>=0xff21 && c<=0xff46 && (c<=0xff26 || c>=0xff41))
                ) {
                    return true;
                }
                return UCharacter.getType(c)==UCharacter.DECIMAL_DIGIT_NUMBER;
            }
        },
        new CaseBinaryProperty(UProperty.CASED),
        new CaseBinaryProperty(UProperty.CASE_IGNORABLE),
        new CaseBinaryProperty(UProperty.CHANGES_WHEN_LOWERCASED),
        new CaseBinaryProperty(UProperty.CHANGES_WHEN_UPPERCASED),
        new CaseBinaryProperty(UProperty.CHANGES_WHEN_TITLECASED),
        new BinaryProperty(SRC_CASE_AND_NORM) {  // UCHAR_CHANGES_WHEN_CASEFOLDED
            @Override
            boolean contains(int c) {
                String nfd=Norm2AllModes.getNFCInstance().impl.getDecomposition(c);
                if(nfd!=null) {
                    /* c has a decomposition */
                    c=nfd.codePointAt(0);
                    if(Character.charCount(c)!=nfd.length()) {
                        /* multiple code points */
                        c=-1;
                    }
                } else if(c<0) {
                    return false;  /* protect against bad input */
                }
                if(c>=0) {
                    /* single code point */
                    UCaseProps csp=UCaseProps.INSTANCE;
                    UCaseProps.dummyStringBuilder.setLength(0);
                    return csp.toFullFolding(c, UCaseProps.dummyStringBuilder,
                                             UCharacter.FOLD_CASE_DEFAULT)>=0;
                } else {
                    String folded=UCharacter.foldCase(nfd, true);
                    return !folded.equals(nfd);
                }
            }
        },
        new CaseBinaryProperty(UProperty.CHANGES_WHEN_CASEMAPPED),
        new BinaryProperty(SRC_NFKC_CF) {  // UCHAR_CHANGES_WHEN_NFKC_CASEFOLDED
            @Override
            boolean contains(int c) {
                Normalizer2Impl kcf=Norm2AllModes.getNFKC_CFInstance().impl;
                String src=UTF16.valueOf(c);
                StringBuilder dest=new StringBuilder();
                // Small destCapacity for NFKC_CF(c).
                Normalizer2Impl.ReorderingBuffer buffer=new Normalizer2Impl.ReorderingBuffer(kcf, dest, 5);
                kcf.compose(src, 0, src.length(), false, true, buffer);
                return !Normalizer2Impl.UTF16Plus.equal(dest, src);
            }
        },
        new BinaryProperty(2, 1<<PROPS_2_EMOJI),
        new BinaryProperty(2, 1<<PROPS_2_EMOJI_PRESENTATION),
        new BinaryProperty(2, 1<<PROPS_2_EMOJI_MODIFIER),
        new BinaryProperty(2, 1<<PROPS_2_EMOJI_MODIFIER_BASE),
    };

    public boolean hasBinaryProperty(int c, int which) {
         if(which<UProperty.BINARY_START || UProperty.BINARY_LIMIT<=which) {
            // not a known binary property
            return false;
        } else {
            return binProps[which].contains(c);
        }
    }

    // int-value and enumerated properties --------------------------------- ***

    public int getType(int c) {
        return getProperty(c)&TYPE_MASK;
    }

    /*
     * Map some of the Grapheme Cluster Break values to Hangul Syllable Types.
     * Hangul_Syllable_Type is fully redundant with a subset of Grapheme_Cluster_Break.
     */
    private static final int /* UHangulSyllableType */ gcbToHst[]={
        HangulSyllableType.NOT_APPLICABLE,   /* U_GCB_OTHER */
        HangulSyllableType.NOT_APPLICABLE,   /* U_GCB_CONTROL */
        HangulSyllableType.NOT_APPLICABLE,   /* U_GCB_CR */
        HangulSyllableType.NOT_APPLICABLE,   /* U_GCB_EXTEND */
        HangulSyllableType.LEADING_JAMO,     /* U_GCB_L */
        HangulSyllableType.NOT_APPLICABLE,   /* U_GCB_LF */
        HangulSyllableType.LV_SYLLABLE,      /* U_GCB_LV */
        HangulSyllableType.LVT_SYLLABLE,     /* U_GCB_LVT */
        HangulSyllableType.TRAILING_JAMO,    /* U_GCB_T */
        HangulSyllableType.VOWEL_JAMO        /* U_GCB_V */
        /*
         * Omit GCB values beyond what we need for hst.
         * The code below checks for the array length.
         */
    };

    @WeakOuter
    private class IntProperty {
        int column;  // SRC_PROPSVEC column, or "source" if mask==0
        int mask;
        int shift;
        IntProperty(int column, int mask, int shift) {
            this.column=column;
            this.mask=mask;
            this.shift=shift;
        }
        IntProperty(int source) {
            this.column=source;
            this.mask=0;
        }
        final int getSource() {
            return mask==0 ? column : SRC_PROPSVEC;
        }
        int getValue(int c) {
            // systematic, directly stored properties
            return (getAdditional(c, column)&mask)>>>shift;
        }
        int getMaxValue(int which) {
            return (getMaxValues(column)&mask)>>>shift;
        }
    }

    private class BiDiIntProperty extends IntProperty {
        BiDiIntProperty() {
            super(SRC_BIDI);
        }
        @Override
        int getMaxValue(int which) {
            return UBiDiProps.INSTANCE.getMaxValue(which);
        }
    }

    private class CombiningClassIntProperty extends IntProperty {
        CombiningClassIntProperty(int source) {
            super(source);
        }
        @Override
        int getMaxValue(int which) {
            return 0xff;
        }
    }

    private class NormQuickCheckIntProperty extends IntProperty {  // UCHAR_NF*_QUICK_CHECK properties
        int which;
        int max;
        NormQuickCheckIntProperty(int source, int which, int max) {
            super(source);
            this.which=which;
            this.max=max;
        }
        @Override
        int getValue(int c) {
            return Norm2AllModes.getN2WithImpl(which-UProperty.NFD_QUICK_CHECK).getQuickCheck(c);
        }
        @Override
        int getMaxValue(int which) {
            return max;
        }
    }

    /*
     * J2ObjC: the following 3 classes were created from anonymous classes in order to
     * annotate them with @WeakOuter.
     */
    @WeakOuter
    private class IntProperty_GeneralCategory extends IntProperty {
        IntProperty_GeneralCategory() {
            super(SRC_CHAR);
        }
        @Override
        int getValue(int c) {
            return getType(c);
        }
        @Override
        int getMaxValue(int which) {
            return UCharacterCategory.CHAR_CATEGORY_COUNT-1;
        }
    }

    @WeakOuter
    private class IntProperty_NumericType extends IntProperty {
        IntProperty_NumericType() {
            super(SRC_CHAR);
        }
        @Override
        int getValue(int c) {
            return ntvGetType(getNumericTypeValue(getProperty(c)));
        }
        @Override
        int getMaxValue(int which) {
            return NumericType.COUNT-1;
        }
    }

    @WeakOuter
    private class IntProperty_HangulSyllableType extends IntProperty {
        IntProperty_HangulSyllableType() {
            super(SRC_PROPSVEC);
        }
        @Override
        int getValue(int c) {
            /* see comments on gcbToHst[] above */
            int gcb=(getAdditional(c, 2)&GCB_MASK)>>>GCB_SHIFT;
            if(gcb<gcbToHst.length) {
                return gcbToHst[gcb];
            } else {
                return HangulSyllableType.NOT_APPLICABLE;
            }
        }
        @Override
        int getMaxValue(int which) {
            return HangulSyllableType.COUNT-1;
        }
    }

    IntProperty intProps[]={
        new BiDiIntProperty() {  // BIDI_CLASS
            @Override
            int getValue(int c) {
                return UBiDiProps.INSTANCE.getClass(c);
            }
        },
        new IntProperty(0, BLOCK_MASK_, BLOCK_SHIFT_),
        new CombiningClassIntProperty(SRC_NFC) {  // CANONICAL_COMBINING_CLASS
            @Override
            int getValue(int c) {
                return Normalizer2.getNFDInstance().getCombiningClass(c);
            }
        },
        new IntProperty(2, DECOMPOSITION_TYPE_MASK_, 0),
        new IntProperty(0, EAST_ASIAN_MASK_, EAST_ASIAN_SHIFT_),
        new IntProperty_GeneralCategory(),  // GENERAL_CATEGORY
        new BiDiIntProperty() {  // JOINING_GROUP
            @Override
            int getValue(int c) {
                return UBiDiProps.INSTANCE.getJoiningGroup(c);
            }
        },
        new BiDiIntProperty() {  // JOINING_TYPE
            @Override
            int getValue(int c) {
                return UBiDiProps.INSTANCE.getJoiningType(c);
            }
        },
        new IntProperty(2, LB_MASK, LB_SHIFT),  // LINE_BREAK
        new IntProperty_NumericType(),  // NUMERIC_TYPE
        new IntProperty(0, SCRIPT_MASK_, 0) {
            @Override
            int getValue(int c) {
                return UScript.getScript(c);
            }
        },
        new IntProperty_HangulSyllableType(),  // HANGUL_SYLLABLE_TYPE
        // max=1=YES -- these are never "maybe", only "no" or "yes"
        new NormQuickCheckIntProperty(SRC_NFC, UProperty.NFD_QUICK_CHECK, 1),
        new NormQuickCheckIntProperty(SRC_NFKC, UProperty.NFKD_QUICK_CHECK, 1),
        // max=2=MAYBE
        new NormQuickCheckIntProperty(SRC_NFC, UProperty.NFC_QUICK_CHECK, 2),
        new NormQuickCheckIntProperty(SRC_NFKC, UProperty.NFKC_QUICK_CHECK, 2),
        new CombiningClassIntProperty(SRC_NFC) {  // LEAD_CANONICAL_COMBINING_CLASS
            @Override
            int getValue(int c) {
                return Norm2AllModes.getNFCInstance().impl.getFCD16(c)>>8;
            }
        },
        new CombiningClassIntProperty(SRC_NFC) {  // TRAIL_CANONICAL_COMBINING_CLASS
            @Override
            int getValue(int c) {
                return Norm2AllModes.getNFCInstance().impl.getFCD16(c)&0xff;
            }
        },
        new IntProperty(2, GCB_MASK, GCB_SHIFT),  // GRAPHEME_CLUSTER_BREAK
        new IntProperty(2, SB_MASK, SB_SHIFT),  // SENTENCE_BREAK
        new IntProperty(2, WB_MASK, WB_SHIFT),  // WORD_BREAK
        new BiDiIntProperty() {  // BIDI_PAIRED_BRACKET_TYPE
            @Override
            int getValue(int c) {
                return UBiDiProps.INSTANCE.getPairedBracketType(c);
            }
        },
    };

    public int getIntPropertyValue(int c, int which) {
        if(which<UProperty.INT_START) {
            if(UProperty.BINARY_START<=which && which<UProperty.BINARY_LIMIT) {
                return binProps[which].contains(c) ? 1 : 0;
            }
        } else if(which<UProperty.INT_LIMIT) {
            return intProps[which-UProperty.INT_START].getValue(c);
        } else if (which == UProperty.GENERAL_CATEGORY_MASK) {
            return getMask(getType(c));
        }
        return 0; // undefined
    }

    public int getIntPropertyMaxValue(int which) {
        if(which<UProperty.INT_START) {
            if(UProperty.BINARY_START<=which && which<UProperty.BINARY_LIMIT) {
                return 1;  // maximum TRUE for all binary properties
            }
        } else if(which<UProperty.INT_LIMIT) {
            return intProps[which-UProperty.INT_START].getMaxValue(which);
        }
        return -1; // undefined
    }

    public final int getSource(int which) {
        if(which<UProperty.BINARY_START) {
            return SRC_NONE; /* undefined */
        } else if(which<UProperty.BINARY_LIMIT) {
            return binProps[which].getSource();
        } else if(which<UProperty.INT_START) {
            return SRC_NONE; /* undefined */
        } else if(which<UProperty.INT_LIMIT) {
            return intProps[which-UProperty.INT_START].getSource();
        } else if(which<UProperty.STRING_START) {
            switch(which) {
            case UProperty.GENERAL_CATEGORY_MASK:
            case UProperty.NUMERIC_VALUE:
                return SRC_CHAR;

            default:
                return SRC_NONE;
            }
        } else if(which<UProperty.STRING_LIMIT) {
            switch(which) {
            case UProperty.AGE:
                return SRC_PROPSVEC;

            case UProperty.BIDI_MIRRORING_GLYPH:
                return SRC_BIDI;

            case UProperty.CASE_FOLDING:
            case UProperty.LOWERCASE_MAPPING:
            case UProperty.SIMPLE_CASE_FOLDING:
            case UProperty.SIMPLE_LOWERCASE_MAPPING:
            case UProperty.SIMPLE_TITLECASE_MAPPING:
            case UProperty.SIMPLE_UPPERCASE_MAPPING:
            case UProperty.TITLECASE_MAPPING:
            case UProperty.UPPERCASE_MAPPING:
                return SRC_CASE;

            case UProperty.ISO_COMMENT:
            case UProperty.NAME:
            case UProperty.UNICODE_1_NAME:
                return SRC_NAMES;

            default:
                return SRC_NONE;
            }
        } else {
            switch(which) {
            case UProperty.SCRIPT_EXTENSIONS:
                return SRC_PROPSVEC;
            default:
                return SRC_NONE; /* undefined */
            }
        }
    }

    /**
     * <p>
     * Unicode property names and property value names are compared
     * "loosely". Property[Value]Aliases.txt say:
     * <quote>
     *   "With loose matching of property names, the case distinctions,
     *    whitespace, and '_' are ignored."
     * </quote>
     * </p>
     * <p>
     * This function does just that, for ASCII (char *) name strings.
     * It is almost identical to ucnv_compareNames() but also ignores
     * ASCII White_Space characters (U+0009..U+000d).
     * </p>
     * @param name1 name to compare
     * @param name2 name to compare
     * @return 0 if names are equal, < 0 if name1 is less than name2 and > 0
     *         if name1 is greater than name2.
     */
    /* to be implemented in 2.4
     * public static int comparePropertyNames(String name1, String name2)
    {
        int result = 0;
        int i1 = 0;
        int i2 = 0;
        while (true) {
            char ch1 = 0;
            char ch2 = 0;
            // Ignore delimiters '-', '_', and ASCII White_Space
            if (i1 < name1.length()) {
                ch1 = name1.charAt(i1 ++);
            }
            while (ch1 == '-' || ch1 == '_' || ch1 == ' ' || ch1 == '\t'
                   || ch1 == '\n' // synwee what is || ch1 == '\v'
                   || ch1 == '\f' || ch1=='\r') {
                if (i1 < name1.length()) {
                    ch1 = name1.charAt(i1 ++);
                }
                else {
                    ch1 = 0;
                }
            }
            if (i2 < name2.length()) {
                ch2 = name2.charAt(i2 ++);
            }
            while (ch2 == '-' || ch2 == '_' || ch2 == ' ' || ch2 == '\t'
                   || ch2 == '\n' // synwee what is || ch1 == '\v'
                   || ch2 == '\f' || ch2=='\r') {
                if (i2 < name2.length()) {
                    ch2 = name2.charAt(i2 ++);
                }
                else {
                    ch2 = 0;
                }
            }

            // If we reach the ends of both strings then they match
            if (ch1 == 0 && ch2 == 0) {
                return 0;
            }

            // Case-insensitive comparison
            if (ch1 != ch2) {
                result = Character.toLowerCase(ch1)
                                                - Character.toLowerCase(ch2);
                if (result != 0) {
                    return result;
                }
            }
        }
    }
    */

    /**
     * Get the the maximum values for some enum/int properties.
     * @return maximum values for the integer properties.
     */
    public int getMaxValues(int column)
    {
       // return m_maxBlockScriptValue_;

        switch(column) {
        case 0:
            return m_maxBlockScriptValue_;
        case 2:
            return m_maxJTGValue_;
        default:
            return 0;
        }
    }

    /**
     * Gets the type mask
     * @param type character type
     * @return mask
     */
    public static final int getMask(int type)
    {
        return 1 << type;
    }


    /**
     * Returns the digit values of characters like 'A' - 'Z', normal,
     * half-width and full-width. This method assumes that the other digit
     * characters are checked by the calling method.
     * @param ch character to test
     * @return -1 if ch is not a character of the form 'A' - 'Z', otherwise
     *         its corresponding digit will be returned.
     */
    public static int getEuropeanDigit(int ch) {
        if ((ch > 0x7a && ch < 0xff21)
            || ch < 0x41 || (ch > 0x5a && ch < 0x61)
            || ch > 0xff5a || (ch > 0xff3a && ch < 0xff41)) {
            return -1;
        }
        if (ch <= 0x7a) {
            // ch >= 0x41 or ch < 0x61
            return ch + 10 - ((ch <= 0x5a) ? 0x41 : 0x61);
        }
        // ch >= 0xff21
        if (ch <= 0xff3a) {
            return ch + 10 - 0xff21;
        }
        // ch >= 0xff41 && ch <= 0xff5a
        return ch + 10 - 0xff41;
    }

    public int digit(int c) {
        int value = getNumericTypeValue(getProperty(c)) - NTV_DECIMAL_START_;
        if(value<=9) {
            return value;
        } else {
            return -1;
        }
    }

    public int getNumericValue(int c) {
        // slightly pruned version of getUnicodeNumericValue(), plus getEuropeanDigit()
        int ntv = getNumericTypeValue(getProperty(c));

        if(ntv==NTV_NONE_) {
            return getEuropeanDigit(c);
        } else if(ntv<NTV_DIGIT_START_) {
            /* decimal digit */
            return ntv-NTV_DECIMAL_START_;
        } else if(ntv<NTV_NUMERIC_START_) {
            /* other digit */
            return ntv-NTV_DIGIT_START_;
        } else if(ntv<NTV_FRACTION_START_) {
            /* small integer */
            return ntv-NTV_NUMERIC_START_;
        } else if(ntv<NTV_LARGE_START_) {
            /* fraction */
            return -2;
        } else if(ntv<NTV_BASE60_START_) {
            /* large, single-significant-digit integer */
            int mant=(ntv>>5)-14;
            int exp=(ntv&0x1f)+2;
            if(exp<9 || (exp==9 && mant<=2)) {
                int numValue=mant;
                do {
                    numValue*=10;
                } while(--exp>0);
                return numValue;
            } else {
                return -2;
            }
        } else if(ntv<NTV_FRACTION20_START_) {
            /* sexagesimal (base 60) integer */
            int numValue=(ntv>>2)-0xbf;
            int exp=(ntv&3)+1;

            switch(exp) {
            case 4:
                numValue*=60*60*60*60;
                break;
            case 3:
                numValue*=60*60*60;
                break;
            case 2:
                numValue*=60*60;
                break;
            case 1:
                numValue*=60;
                break;
            case 0:
            default:
                break;
            }

            return numValue;
        } else if(ntv<NTV_RESERVED_START_) {
            // fraction-20 e.g. 3/80
            return -2;
        } else {
            /* reserved */
            return -2;
        }
    }

    public double getUnicodeNumericValue(int c) {
        // equivalent to c version double u_getNumericValue(UChar32 c)
        int ntv = getNumericTypeValue(getProperty(c));

        if(ntv==NTV_NONE_) {
            return UCharacter.NO_NUMERIC_VALUE;
        } else if(ntv<NTV_DIGIT_START_) {
            /* decimal digit */
            return ntv-NTV_DECIMAL_START_;
        } else if(ntv<NTV_NUMERIC_START_) {
            /* other digit */
            return ntv-NTV_DIGIT_START_;
        } else if(ntv<NTV_FRACTION_START_) {
            /* small integer */
            return ntv-NTV_NUMERIC_START_;
        } else if(ntv<NTV_LARGE_START_) {
            /* fraction */
            int numerator=(ntv>>4)-12;
            int denominator=(ntv&0xf)+1;
            return (double)numerator/denominator;
        } else if(ntv<NTV_BASE60_START_) {
            /* large, single-significant-digit integer */
            double numValue;
            int mant=(ntv>>5)-14;
            int exp=(ntv&0x1f)+2;
            numValue=mant;

            /* multiply by 10^exp without math.h */
            while(exp>=4) {
                numValue*=10000.;
                exp-=4;
            }
            switch(exp) {
            case 3:
                numValue*=1000.;
                break;
            case 2:
                numValue*=100.;
                break;
            case 1:
                numValue*=10.;
                break;
            case 0:
            default:
                break;
            }

            return numValue;
        } else if(ntv<NTV_FRACTION20_START_) {
            /* sexagesimal (base 60) integer */
            int numValue=(ntv>>2)-0xbf;
            int exp=(ntv&3)+1;

            switch(exp) {
            case 4:
                numValue*=60*60*60*60;
                break;
            case 3:
                numValue*=60*60*60;
                break;
            case 2:
                numValue*=60*60;
                break;
            case 1:
                numValue*=60;
                break;
            case 0:
            default:
                break;
            }

            return numValue;
        } else if(ntv<NTV_RESERVED_START_) {
            // fraction-20 e.g. 3/80
            int frac20=ntv-NTV_FRACTION20_START_;  // 0..0x17
            int numerator=2*(frac20&3)+1;
            int denominator=20<<(frac20>>2);
            return (double)numerator/denominator;
        } else {
            /* reserved */
            return UCharacter.NO_NUMERIC_VALUE;
        }
    }

    // protected variables -----------------------------------------------

    /**
     * Extra property trie
     */
    Trie2_16 m_additionalTrie_;
    /**
     * Extra property vectors, 1st column for age and second for binary
     * properties.
     */
    int m_additionalVectors_[];
    /**
     * Number of additional columns
     */
    int m_additionalColumnsCount_;
    /**
     * Maximum values for block, bits used as in vector word
     * 0
     */
    int m_maxBlockScriptValue_;
    /**
     * Maximum values for script, bits used as in vector word
     * 0
     */
     int m_maxJTGValue_;

    /**
     * Script_Extensions data
     */
    public char[] m_scriptExtensions_;

    // private variables -------------------------------------------------

    /**
    * Default name of the datafile
    */
    private static final String DATA_FILE_NAME_ = "uprops.icu";

    // property data constants -------------------------------------------------

    /**
     * Numeric types and values in the main properties words.
     */
    private static final int NUMERIC_TYPE_VALUE_SHIFT_ = 6;
    private static final int getNumericTypeValue(int props) {
        return props >> NUMERIC_TYPE_VALUE_SHIFT_;
    }
    /* constants for the storage form of numeric types and values */
    /** No numeric value. */
    private static final int NTV_NONE_ = 0;
    /** Decimal digits: nv=0..9 */
    private static final int NTV_DECIMAL_START_ = 1;
    /** Other digits: nv=0..9 */
    private static final int NTV_DIGIT_START_ = 11;
    /** Small integers: nv=0..154 */
    private static final int NTV_NUMERIC_START_ = 21;
    /** Fractions: ((ntv>>4)-12) / ((ntv&0xf)+1) = -1..17 / 1..16 */
    private static final int NTV_FRACTION_START_ = 0xb0;
    /**
     * Large integers:
     * ((ntv>>5)-14) * 10^((ntv&0x1f)+2) = (1..9)*(10^2..10^33)
     * (only one significant decimal digit)
     */
    private static final int NTV_LARGE_START_ = 0x1e0;
    /**
     * Sexagesimal numbers:
     * ((ntv>>2)-0xbf) * 60^((ntv&3)+1) = (1..9)*(60^1..60^4)
     */
    private static final int NTV_BASE60_START_=0x300;
    /**
     * Fraction-20 values:
     * frac20 = ntv-0x324 = 0..0x17 -> 1|3|5|7 / 20|40|80|160|320|640
     * numerator: num = 2*(frac20&3)+1
     * denominator: den = 20<<(frac20>>2)
     */
    private static final int NTV_FRACTION20_START_ = NTV_BASE60_START_ + 36;  // 0x300+9*4=0x324
    /** No numeric value (yet). */
    private static final int NTV_RESERVED_START_ = NTV_FRACTION20_START_ + 24;  // 0x324+6*4=0x34c

    private static final int ntvGetType(int ntv) {
        return
            (ntv==NTV_NONE_) ? NumericType.NONE :
            (ntv<NTV_DIGIT_START_) ?  NumericType.DECIMAL :
            (ntv<NTV_NUMERIC_START_) ? NumericType.DIGIT :
            NumericType.NUMERIC;
    }

    /*
     * Properties in vector word 0
     * Bits
     * 31..24   DerivedAge version major/minor one nibble each
     * 23..22   3..1: Bits 7..0 = Script_Extensions index
     *             3: Script value from Script_Extensions
     *             2: Script=Inherited
     *             1: Script=Common
     *             0: Script=bits 7..0
     * 21..20   reserved
     * 19..17   East Asian Width
     * 16.. 8   UBlockCode
     *  7.. 0   UScriptCode
     */

    /**
     * Script_Extensions: mask includes Script
     */
    public static final int SCRIPT_X_MASK = 0x00c000ff;
    //private static final int SCRIPT_X_SHIFT = 22;
    /**
     * Integer properties mask and shift values for East Asian cell width.
     * Equivalent to icu4c UPROPS_EA_MASK
     */
    private static final int EAST_ASIAN_MASK_ = 0x000e0000;
    /**
     * Integer properties mask and shift values for East Asian cell width.
     * Equivalent to icu4c UPROPS_EA_SHIFT
     */
    private static final int EAST_ASIAN_SHIFT_ = 17;
    /**
     * Integer properties mask and shift values for blocks.
     * Equivalent to icu4c UPROPS_BLOCK_MASK
     */
    private static final int BLOCK_MASK_ = 0x0001ff00;
    /**
     * Integer properties mask and shift values for blocks.
     * Equivalent to icu4c UPROPS_BLOCK_SHIFT
     */
    private static final int BLOCK_SHIFT_ = 8;
    /**
     * Integer properties mask and shift values for scripts.
     * Equivalent to icu4c UPROPS_SHIFT_MASK
     */
    public static final int SCRIPT_MASK_ = 0x000000ff;

    /* SCRIPT_X_WITH_COMMON must be the lowest value that involves Script_Extensions. */
    public static final int SCRIPT_X_WITH_COMMON = 0x400000;
    public static final int SCRIPT_X_WITH_INHERITED = 0x800000;
    public static final int SCRIPT_X_WITH_OTHER = 0xc00000;

    /**
     * Additional properties used in internal trie data
     */
    /*
     * Properties in vector word 1
     * Each bit encodes one binary property.
     * The following constants represent the bit number, use 1<<UPROPS_XYZ.
     * UPROPS_BINARY_1_TOP<=32!
     *
     * Keep this list of property enums in sync with
     * propListNames[] in icu/source/tools/genprops/props2.c!
     *
     * ICU 2.6/uprops format version 3.2 stores full properties instead of "Other_".
     */
    private static final int WHITE_SPACE_PROPERTY_ = 0;
    private static final int DASH_PROPERTY_ = 1;
    private static final int HYPHEN_PROPERTY_ = 2;
    private static final int QUOTATION_MARK_PROPERTY_ = 3;
    private static final int TERMINAL_PUNCTUATION_PROPERTY_ = 4;
    private static final int MATH_PROPERTY_ = 5;
    private static final int HEX_DIGIT_PROPERTY_ = 6;
    private static final int ASCII_HEX_DIGIT_PROPERTY_ = 7;
    private static final int ALPHABETIC_PROPERTY_ = 8;
    private static final int IDEOGRAPHIC_PROPERTY_ = 9;
    private static final int DIACRITIC_PROPERTY_ = 10;
    private static final int EXTENDER_PROPERTY_ = 11;
    private static final int NONCHARACTER_CODE_POINT_PROPERTY_ = 12;
    private static final int GRAPHEME_EXTEND_PROPERTY_ = 13;
    private static final int GRAPHEME_LINK_PROPERTY_ = 14;
    private static final int IDS_BINARY_OPERATOR_PROPERTY_ = 15;
    private static final int IDS_TRINARY_OPERATOR_PROPERTY_ = 16;
    private static final int RADICAL_PROPERTY_ = 17;
    private static final int UNIFIED_IDEOGRAPH_PROPERTY_ = 18;
    private static final int DEFAULT_IGNORABLE_CODE_POINT_PROPERTY_ = 19;
    private static final int DEPRECATED_PROPERTY_ = 20;
    private static final int LOGICAL_ORDER_EXCEPTION_PROPERTY_ = 21;
    private static final int XID_START_PROPERTY_ = 22;
    private static final int XID_CONTINUE_PROPERTY_ = 23;
    private static final int ID_START_PROPERTY_    = 24;
    private static final int ID_CONTINUE_PROPERTY_ = 25;
    private static final int GRAPHEME_BASE_PROPERTY_ = 26;
    private static final int S_TERM_PROPERTY_ = 27;
    private static final int VARIATION_SELECTOR_PROPERTY_ = 28;
    private static final int PATTERN_SYNTAX = 29;                   /* new in ICU 3.4 and Unicode 4.1 */
    private static final int PATTERN_WHITE_SPACE = 30;

    /*
     * Properties in vector word 2
     * Bits
     * 31..28   http://www.unicode.org/reports/tr51/#Emoji_Properties
     * 27..26   reserved
     * 25..20   Line Break
     * 19..15   Sentence Break
     * 14..10   Word Break
     *  9.. 5   Grapheme Cluster Break
     *  4.. 0   Decomposition Type
     */
    private static final int PROPS_2_EMOJI = 28;
    private static final int PROPS_2_EMOJI_PRESENTATION = 29;
    private static final int PROPS_2_EMOJI_MODIFIER = 30;
    private static final int PROPS_2_EMOJI_MODIFIER_BASE = 31;

    private static final int LB_MASK          = 0x03f00000;
    private static final int LB_SHIFT         = 20;

    private static final int SB_MASK          = 0x000f8000;
    private static final int SB_SHIFT         = 15;

    private static final int WB_MASK          = 0x00007c00;
    private static final int WB_SHIFT         = 10;

    private static final int GCB_MASK         = 0x000003e0;
    private static final int GCB_SHIFT        = 5;

    /**
     * Integer properties mask for decomposition type.
     * Equivalent to icu4c UPROPS_DT_MASK.
     */
    private static final int DECOMPOSITION_TYPE_MASK_ = 0x0000001f;

    /**
     * First nibble shift
     */
    private static final int FIRST_NIBBLE_SHIFT_ = 0x4;
    /**
     * Second nibble mask
     */
    private static final int LAST_NIBBLE_MASK_ = 0xF;
    /**
     * Age value shift
     */
    private static final int AGE_SHIFT_ = 24;


    // private constructors --------------------------------------------------

    /**
     * Constructor
     * @exception IOException thrown when data reading fails or data corrupted
     */
    private UCharacterProperty() throws IOException
    {
        // consistency check
        if(binProps.length!=UProperty.BINARY_LIMIT) {
            throw new ICUException("binProps.length!=UProperty.BINARY_LIMIT");
        }
        if(intProps.length!=(UProperty.INT_LIMIT-UProperty.INT_START)) {
            throw new ICUException("intProps.length!=(UProperty.INT_LIMIT-UProperty.INT_START)");
        }

        // jar access
        ByteBuffer bytes=ICUBinary.getRequiredData(DATA_FILE_NAME_);
        m_unicodeVersion_ = ICUBinary.readHeaderAndDataVersion(bytes, DATA_FORMAT, new IsAcceptable());
        // Read or skip the 16 indexes.
        int propertyOffset = bytes.getInt();
        /* exceptionOffset = */ bytes.getInt();
        /* caseOffset = */ bytes.getInt();
        int additionalOffset = bytes.getInt();
        int additionalVectorsOffset = bytes.getInt();
        m_additionalColumnsCount_ = bytes.getInt();
        int scriptExtensionsOffset = bytes.getInt();
        int reservedOffset7 = bytes.getInt();
        /* reservedOffset8 = */ bytes.getInt();
        /* dataTopOffset = */ bytes.getInt();
        m_maxBlockScriptValue_ = bytes.getInt();
        m_maxJTGValue_ = bytes.getInt();
        ICUBinary.skipBytes(bytes, (16 - 12) << 2);

        // read the main properties trie
        m_trie_ = Trie2_16.createFromSerialized(bytes);
        int expectedTrieLength = (propertyOffset - 16) * 4;
        int trieLength = m_trie_.getSerializedLength();
        if(trieLength > expectedTrieLength) {
            throw new IOException("uprops.icu: not enough bytes for main trie");
        }
        // skip padding after trie bytes
        ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);

        // skip unused intervening data structures
        ICUBinary.skipBytes(bytes, (additionalOffset - propertyOffset) * 4);

        if(m_additionalColumnsCount_ > 0) {
            // reads the additional property block
            m_additionalTrie_ = Trie2_16.createFromSerialized(bytes);
            expectedTrieLength = (additionalVectorsOffset-additionalOffset)*4;
            trieLength = m_additionalTrie_.getSerializedLength();
            if(trieLength > expectedTrieLength) {
                throw new IOException("uprops.icu: not enough bytes for additional-properties trie");
            }
            // skip padding after trie bytes
            ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);

            // additional properties
            int size = scriptExtensionsOffset - additionalVectorsOffset;
            m_additionalVectors_ = ICUBinary.getInts(bytes, size, 0);
        }

        // Script_Extensions
        int numChars = (reservedOffset7 - scriptExtensionsOffset) * 2;
        if(numChars > 0) {
            m_scriptExtensions_ = ICUBinary.getChars(bytes, numChars, 0);
        }
    }

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        // @Override when we switch to Java 6
        @Override
        public boolean isDataVersionAcceptable(byte version[]) {
            return version[0] == 7;
        }
    }
    private static final int DATA_FORMAT = 0x5550726F;  // "UPro"

    // private methods -------------------------------------------------------

    /*
     * Compare additional properties to see if it has argument type
     * @param property 32 bit properties
     * @param type character type
     * @return true if property has type
     */
    /*private boolean compareAdditionalType(int property, int type)
    {
        return (property & (1 << type)) != 0;
    }*/

    // property starts for UnicodeSet -------------------------------------- ***

    private static final int TAB     = 0x0009;
    //private static final int LF      = 0x000a;
    //private static final int FF      = 0x000c;
    private static final int CR      = 0x000d;
    private static final int U_A     = 0x0041;
    private static final int U_F     = 0x0046;
    private static final int U_Z     = 0x005a;
    private static final int U_a     = 0x0061;
    private static final int U_f     = 0x0066;
    private static final int U_z     = 0x007a;
    private static final int DEL     = 0x007f;
    private static final int NL      = 0x0085;
    private static final int NBSP    = 0x00a0;
    private static final int CGJ     = 0x034f;
    private static final int FIGURESP= 0x2007;
    private static final int HAIRSP  = 0x200a;
    //private static final int ZWNJ    = 0x200c;
    //private static final int ZWJ     = 0x200d;
    private static final int RLM     = 0x200f;
    private static final int NNBSP   = 0x202f;
    private static final int WJ      = 0x2060;
    private static final int INHSWAP = 0x206a;
    private static final int NOMDIG  = 0x206f;
    private static final int U_FW_A  = 0xff21;
    private static final int U_FW_F  = 0xff26;
    private static final int U_FW_Z  = 0xff3a;
    private static final int U_FW_a  = 0xff41;
    private static final int U_FW_f  = 0xff46;
    private static final int U_FW_z  = 0xff5a;
    private static final int ZWNBSP  = 0xfeff;

    public UnicodeSet addPropertyStarts(UnicodeSet set) {
        /* add the start code point of each same-value range of the main trie */
        Iterator<Trie2.Range> trieIterator = m_trie_.iterator();
        Trie2.Range range;
        while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
            set.add(range.startCodePoint);
        }

        /* add code points with hardcoded properties, plus the ones following them */

        /* add for u_isblank() */
        set.add(TAB);
        set.add(TAB+1);

        /* add for IS_THAT_CONTROL_SPACE() */
        set.add(CR+1); /* range TAB..CR */
        set.add(0x1c);
        set.add(0x1f+1);
        set.add(NL);
        set.add(NL+1);

        /* add for u_isIDIgnorable() what was not added above */
        set.add(DEL); /* range DEL..NBSP-1, NBSP added below */
        set.add(HAIRSP);
        set.add(RLM+1);
        set.add(INHSWAP);
        set.add(NOMDIG+1);
        set.add(ZWNBSP);
        set.add(ZWNBSP+1);

        /* add no-break spaces for u_isWhitespace() what was not added above */
        set.add(NBSP);
        set.add(NBSP+1);
        set.add(FIGURESP);
        set.add(FIGURESP+1);
        set.add(NNBSP);
        set.add(NNBSP+1);

        /* add for u_charDigitValue() */
        // TODO remove when UCharacter.getHanNumericValue() is changed to just return
        // Unicode numeric values
        set.add(0x3007);
        set.add(0x3008);
        set.add(0x4e00);
        set.add(0x4e01);
        set.add(0x4e8c);
        set.add(0x4e8d);
        set.add(0x4e09);
        set.add(0x4e0a);
        set.add(0x56db);
        set.add(0x56dc);
        set.add(0x4e94);
        set.add(0x4e95);
        set.add(0x516d);
        set.add(0x516e);
        set.add(0x4e03);
        set.add(0x4e04);
        set.add(0x516b);
        set.add(0x516c);
        set.add(0x4e5d);
        set.add(0x4e5e);

        /* add for u_digit() */
        set.add(U_a);
        set.add(U_z+1);
        set.add(U_A);
        set.add(U_Z+1);
        set.add(U_FW_a);
        set.add(U_FW_z+1);
        set.add(U_FW_A);
        set.add(U_FW_Z+1);

        /* add for u_isxdigit() */
        set.add(U_f+1);
        set.add(U_F+1);
        set.add(U_FW_f+1);
        set.add(U_FW_F+1);

        /* add for UCHAR_DEFAULT_IGNORABLE_CODE_POINT what was not added above */
        set.add(WJ); /* range WJ..NOMDIG */
        set.add(0xfff0);
        set.add(0xfffb+1);
        set.add(0xe0000);
        set.add(0xe0fff+1);

        /* add for UCHAR_GRAPHEME_BASE and others */
        set.add(CGJ);
        set.add(CGJ+1);

        return set; // for chaining
    }

    public void upropsvec_addPropertyStarts(UnicodeSet set) {
        /* add the start code point of each same-value range of the properties vectors trie */
        if(m_additionalColumnsCount_>0) {
            /* if m_additionalColumnsCount_==0 then the properties vectors trie may not be there at all */
            Iterator<Trie2.Range> trieIterator = m_additionalTrie_.iterator();
            Trie2.Range range;
            while(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
                set.add(range.startCodePoint);
            }
        }
    }

    // This static initializer block must be placed after
    // other static member initialization
    static {
        try {
            INSTANCE = new UCharacterProperty();
        }
        catch (IOException e) {
            throw new MissingResourceException(e.getMessage(),"","");
        }
    }

/*----------------------------------------------------------------
 * Inclusions list
 *----------------------------------------------------------------*/

    /*
     * Return a set of characters for property enumeration.
     * The set implicitly contains 0x110000 as well, which is one more than the highest
     * Unicode code point.
     *
     * This set is used as an ordered list - its code points are ordered, and
     * consecutive code points (in Unicode code point order) in the set define a range.
     * For each two consecutive characters (start, limit) in the set,
     * all of the UCD/normalization and related properties for
     * all code points start..limit-1 are all the same,
     * except for character names and ISO comments.
     *
     * All Unicode code points U+0000..U+10ffff are covered by these ranges.
     * The ranges define a partition of the Unicode code space.
     * ICU uses the inclusions set to enumerate properties for generating
     * UnicodeSets containing all code points that have a certain property value.
     *
     * The Inclusion List is generated from the UCD. It is generated
     * by enumerating the data tries, and code points for hardcoded properties
     * are added as well.
     *
     * --------------------------------------------------------------------------
     *
     * The following are ideas for getting properties-unique code point ranges,
     * with possible optimizations beyond the current implementation.
     * These optimizations would require more code and be more fragile.
     * The current implementation generates one single list (set) for all properties.
     *
     * To enumerate properties efficiently, one needs to know ranges of
     * repetitive values, so that the value of only each start code point
     * can be applied to the whole range.
     * This information is in principle available in the uprops.icu/unorm.icu data.
     *
     * There are two obstacles:
     *
     * 1. Some properties are computed from multiple data structures,
     *    making it necessary to get repetitive ranges by intersecting
     *    ranges from multiple tries.
     *
     * 2. It is not economical to write code for getting repetitive ranges
     *    that are precise for each of some 50 properties.
     *
     * Compromise ideas:
     *
     * - Get ranges per trie, not per individual property.
     *   Each range contains the same values for a whole group of properties.
     *   This would generate currently five range sets, two for uprops.icu tries
     *   and three for unorm.icu tries.
     *
     * - Combine sets of ranges for multiple tries to get sufficient sets
     *   for properties, e.g., the uprops.icu main and auxiliary tries
     *   for all non-normalization properties.
     *
     * Ideas for representing ranges and combining them:
     *
     * - A UnicodeSet could hold just the start code points of ranges.
     *   Multiple sets are easily combined by or-ing them together.
     *
     * - Alternatively, a UnicodeSet could hold each even-numbered range.
     *   All ranges could be enumerated by using each start code point
     *   (for the even-numbered ranges) as well as each limit (end+1) code point
     *   (for the odd-numbered ranges).
     *   It should be possible to combine two such sets by xor-ing them,
     *   but no more than two.
     *
     * The second way to represent ranges may(?!) yield smaller UnicodeSet arrays,
     * but the first one is certainly simpler and applicable for combining more than
     * two range sets.
     *
     * It is possible to combine all range sets for all uprops/unorm tries into one
     * set that can be used for all properties.
     * As an optimization, there could be less-combined range sets for certain
     * groups of properties.
     * The relationship of which less-combined range set to use for which property
     * depends on the implementation of the properties and must be hardcoded
     * - somewhat error-prone and higher maintenance but can be tested easily
     * by building property sets "the simple way" in test code.
     *
     * ---
     *
     * Do not use a UnicodeSet pattern because that causes infinite recursion;
     * UnicodeSet depends on the inclusions set.
     *
     * ---
     *
     * getInclusions() is commented out starting 2005-feb-12 because
     * UnicodeSet now calls the uxyz_addPropertyStarts() directly,
     * and only for the relevant property source.
     */
    /*
    public UnicodeSet getInclusions() {
        UnicodeSet set = new UnicodeSet();
        NormalizerImpl.addPropertyStarts(set);
        addPropertyStarts(set);
        return set;
    }
    */
}
