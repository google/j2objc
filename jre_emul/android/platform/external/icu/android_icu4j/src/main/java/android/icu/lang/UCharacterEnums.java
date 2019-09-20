/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2004-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.lang;

/**
 * A container for the different 'enumerated types' used by UCharacter.
 */
public class UCharacterEnums {

    /** This is just a namespace, it is not instantiatable. */
    ///CLOVER:OFF
    private UCharacterEnums() {}

    /**
     * 'Enum' for the CharacterCategory constants.  These constants are
     * compatible in name <b>but not in value</b> with those defined in
     * {@link java.lang.Character}.
     * @see UCharacterCategory
     */
    public static interface ECharacterCategory {
        /**
         * Unassigned character type
         */
        public static final byte UNASSIGNED              = 0;

        /**
         * Character type Cn
         * Not Assigned (no characters in [UnicodeData.txt] have this property)
         */
        public static final byte GENERAL_OTHER_TYPES     = 0;

        /**
         * Character type Lu
         */
        public static final byte UPPERCASE_LETTER        = 1;

        /**
         * Character type Ll
         */
        public static final byte LOWERCASE_LETTER        = 2;

        /**
         * Character type Lt
         */

        public static final byte TITLECASE_LETTER        = 3;

        /**
         * Character type Lm
         */
        public static final byte MODIFIER_LETTER         = 4;

        /**
         * Character type Lo
         */
        public static final byte OTHER_LETTER            = 5;

        /**
         * Character type Mn
         */
        public static final byte NON_SPACING_MARK        = 6;

        /**
         * Character type Me
         */
        public static final byte ENCLOSING_MARK          = 7;

        /**
         * Character type Mc
         */
        public static final byte COMBINING_SPACING_MARK  = 8;

        /**
         * Character type Nd
         */
        public static final byte DECIMAL_DIGIT_NUMBER    = 9;

        /**
         * Character type Nl
         */
        public static final byte LETTER_NUMBER           = 10;

        /**
         * Character type No
         */
        public static final byte OTHER_NUMBER            = 11;

        /**
         * Character type Zs
         */
        public static final byte SPACE_SEPARATOR         = 12;

        /**
         * Character type Zl
         */
        public static final byte LINE_SEPARATOR          = 13;

        /**
         * Character type Zp
         */
        public static final byte PARAGRAPH_SEPARATOR     = 14;

        /**
         * Character type Cc
         */
        public static final byte CONTROL                 = 15;

        /**
         * Character type Cf
         */
        public static final byte FORMAT                  = 16;

        /**
         * Character type Co
         */
        public static final byte PRIVATE_USE             = 17;

        /**
         * Character type Cs
         */
        public static final byte SURROGATE               = 18;

        /**
         * Character type Pd
         */
        public static final byte DASH_PUNCTUATION        = 19;

        /**
         * Character type Ps
         */
        public static final byte START_PUNCTUATION       = 20;

        /**
         * Character type Pe
         */
        public static final byte END_PUNCTUATION         = 21;

        /**
         * Character type Pc
         */
        public static final byte CONNECTOR_PUNCTUATION   = 22;

        /**
         * Character type Po
         */
        public static final byte OTHER_PUNCTUATION       = 23;

        /**
         * Character type Sm
         */
        public static final byte MATH_SYMBOL             = 24;

        /**
         * Character type Sc
         */
        public static final byte CURRENCY_SYMBOL         = 25;

        /**
         * Character type Sk
         */
        public static final byte MODIFIER_SYMBOL         = 26;

        /**
         * Character type So
         */
        public static final byte OTHER_SYMBOL            = 27;

        /**
         * Character type Pi
         * @see #INITIAL_QUOTE_PUNCTUATION
         */
        public static final byte INITIAL_PUNCTUATION     = 28;

        /**
         * Character type Pi
         * This name is compatible with java.lang.Character's name for this type.
         * @see #INITIAL_PUNCTUATION
         */
        public static final byte INITIAL_QUOTE_PUNCTUATION = 28;

        /**
         * Character type Pf
         * @see #FINAL_QUOTE_PUNCTUATION
         */
        public static final byte FINAL_PUNCTUATION       = 29;

        /**
         * Character type Pf
         * This name is compatible with java.lang.Character's name for this type.
         * @see #FINAL_PUNCTUATION
         */
        public static final byte FINAL_QUOTE_PUNCTUATION   = 29;

        /**
         * One more than the highest normal ECharacterCategory value.
         * This numeric value is stable (will not change), see
         * http://www.unicode.org/policies/stability_policy.html#Property_Value
         * @hide unsupported on Android
         */
        public static final byte CHAR_CATEGORY_COUNT     = 30;
    }

    /**
     * 'Enum' for the CharacterDirection constants. Some constants are
     * compatible in name <b>but not in value</b> with those defined in
     * {@link java.lang.Character}.
     * @see UCharacterDirection
     */
    public static interface ECharacterDirection {
        /**
         * Directional type L
         */
        public static final int LEFT_TO_RIGHT              = 0;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_LEFT_TO_RIGHT}.
         * Synonym of {@link #LEFT_TO_RIGHT}.
         */
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = (byte)LEFT_TO_RIGHT;

        /**
         * Directional type R
         */
        public static final int RIGHT_TO_LEFT              = 1;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_RIGHT_TO_LEFT}.
         * Synonym of {@link #RIGHT_TO_LEFT}.
         */
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = (byte)RIGHT_TO_LEFT;

        /**
         * Directional type EN
         */
        public static final int EUROPEAN_NUMBER            = 2;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_EUROPEAN_NUMBER}.
         * Synonym of {@link #EUROPEAN_NUMBER}.
         */
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = (byte)EUROPEAN_NUMBER;

        /**
         * Directional type ES
         */
        public static final int EUROPEAN_NUMBER_SEPARATOR  = 3;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR}.
         * Synonym of {@link #EUROPEAN_NUMBER_SEPARATOR}.
         */
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = (byte)EUROPEAN_NUMBER_SEPARATOR;

        /**
         * Directional type ET
         */
        public static final int EUROPEAN_NUMBER_TERMINATOR = 4;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR}.
         * Synonym of {@link #EUROPEAN_NUMBER_TERMINATOR}.
         */
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = (byte)EUROPEAN_NUMBER_TERMINATOR;

        /**
         * Directional type AN
         */
        public static final int ARABIC_NUMBER              = 5;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_ARABIC_NUMBER}.
         * Synonym of {@link #ARABIC_NUMBER}.
         */
        public static final byte DIRECTIONALITY_ARABIC_NUMBER = (byte)ARABIC_NUMBER;

        /**
         * Directional type CS
         */
        public static final int COMMON_NUMBER_SEPARATOR    = 6;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_COMMON_NUMBER_SEPARATOR}.
         * Synonym of {@link #COMMON_NUMBER_SEPARATOR}.
         */
        public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = (byte)COMMON_NUMBER_SEPARATOR;

        /**
         * Directional type B
         */
        public static final int BLOCK_SEPARATOR            = 7;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_PARAGRAPH_SEPARATOR}.
         * Synonym of {@link #BLOCK_SEPARATOR}.
         */
        public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = (byte)BLOCK_SEPARATOR;

        /**
         * Directional type S
         */
        public static final int SEGMENT_SEPARATOR          = 8;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_SEGMENT_SEPARATOR}.
         * Synonym of {@link #SEGMENT_SEPARATOR}.
         */
        public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = (byte)SEGMENT_SEPARATOR;

        /**
         * Directional type WS
         */
        public static final int WHITE_SPACE_NEUTRAL        = 9;

        /**
         * Equivalent to {@link java.lang.Character#DIRECTIONALITY_WHITESPACE}.
         * Synonym of {@link #WHITE_SPACE_NEUTRAL}.
         */
        public static final byte DIRECTIONALITY_WHITESPACE = (byte)WHITE_SPACE_NEUTRAL;

        /**
         * Directional type ON
         */
        public static final int OTHER_NEUTRAL              = 10;

        /**
         * Equivalent to {@link java.lang.Character#DIRECTIONALITY_OTHER_NEUTRALS}.
         * Synonym of {@link #OTHER_NEUTRAL}.
         */
        public static final byte DIRECTIONALITY_OTHER_NEUTRALS = (byte)OTHER_NEUTRAL;

        /**
         * Directional type LRE
         */
        public static final int LEFT_TO_RIGHT_EMBEDDING    = 11;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING}.
         * Synonym of {@link #LEFT_TO_RIGHT_EMBEDDING}.
         */
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = (byte)LEFT_TO_RIGHT_EMBEDDING;

        /**
         * Directional type LRO
         */
        public static final int LEFT_TO_RIGHT_OVERRIDE     = 12;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE}.
         * Synonym of {@link #LEFT_TO_RIGHT_OVERRIDE}.
         */
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = (byte)LEFT_TO_RIGHT_OVERRIDE;

        /**
         * Directional type AL
         */
        public static final int RIGHT_TO_LEFT_ARABIC       = 13;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC}.
         * Synonym of {@link #RIGHT_TO_LEFT_ARABIC}.
         */
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = (byte)RIGHT_TO_LEFT_ARABIC;

        /**
         * Directional type RLE
         */
        public static final int RIGHT_TO_LEFT_EMBEDDING    = 14;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING}.
         * Synonym of {@link #RIGHT_TO_LEFT_EMBEDDING}.
         */
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = (byte)RIGHT_TO_LEFT_EMBEDDING;

        /**
         * Directional type RLO
         */
        public static final int RIGHT_TO_LEFT_OVERRIDE     = 15;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE}.
         * Synonym of {@link #RIGHT_TO_LEFT_OVERRIDE}.
         */
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = (byte)RIGHT_TO_LEFT_OVERRIDE;

        /**
         * Directional type PDF
         */
        public static final int POP_DIRECTIONAL_FORMAT     = 16;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_POP_DIRECTIONAL_FORMAT}.
         * Synonym of {@link #POP_DIRECTIONAL_FORMAT}.
         */
        public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = (byte)POP_DIRECTIONAL_FORMAT;

        /**
         * Directional type NSM
         */
        public static final int DIR_NON_SPACING_MARK       = 17;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_NONSPACING_MARK}.
         * Synonym of {@link #DIR_NON_SPACING_MARK}.
         */
        public static final byte DIRECTIONALITY_NONSPACING_MARK = (byte)DIR_NON_SPACING_MARK;

        /**
         * Directional type BN
         */
        public static final int BOUNDARY_NEUTRAL           = 18;

        /**
         * Equivalent to {@link
         * java.lang.Character#DIRECTIONALITY_BOUNDARY_NEUTRAL}.
         * Synonym of {@link #BOUNDARY_NEUTRAL}.
         */
        public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = (byte)BOUNDARY_NEUTRAL;

        /**
         * Directional type FSI
         */
        public static final byte FIRST_STRONG_ISOLATE        = 19;

        /**
         * Directional type LRI
         */
        public static final byte LEFT_TO_RIGHT_ISOLATE       = 20;

        /**
         * Directional type RLI
         */
        public static final byte RIGHT_TO_LEFT_ISOLATE       = 21;

        /**
         * Directional type PDI
         */
        public static final byte POP_DIRECTIONAL_ISOLATE     = 22;

        /**
         * One more than the highest normal ECharacterDirection value.
         * The highest value is available via UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS).
         *
         * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
         * @hide unsupported on Android
         */
        @Deprecated
        public static final int CHAR_DIRECTION_COUNT       = 23;

        /**
         * Undefined bidirectional character type. Undefined <code>char</code>
         * values have undefined directionality in the Unicode specification.
         */
        public static final byte DIRECTIONALITY_UNDEFINED = -1;
    }
}
