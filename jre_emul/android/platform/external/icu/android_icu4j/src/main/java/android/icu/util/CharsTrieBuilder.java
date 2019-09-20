/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011-2014, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jan07
*   created by: Markus W. Scherer
*   ported from ICU4C ucharstriebuilder/.cpp
*/

package android.icu.util;

import java.nio.CharBuffer;

/**
 * Builder class for CharsTrie.
 *
 * <p>This class is not intended for public subclassing.
 *
 * @author Markus W. Scherer
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CharsTrieBuilder extends StringTrieBuilder {
    /**
     * Constructs an empty builder.
     */
    public CharsTrieBuilder() {}

    /**
     * Adds a (string, value) pair.
     * The string must be unique.
     * The string contents will be copied; the builder does not keep
     * a reference to the input CharSequence.
     * @param s The input string.
     * @param value The value associated with this char sequence.
     * @return this
     */
    public CharsTrieBuilder add(CharSequence s, int value) {
        addImpl(s, value);
        return this;
    }

    /**
     * Builds a CharsTrie for the add()ed data.
     * Once built, no further data can be add()ed until clear() is called.
     *
     * <p>A CharsTrie cannot be empty. At least one (string, value) pair
     * must have been add()ed.
     *
     * <p>Multiple calls to build() or buildCharSequence() return tries or sequences
     * which share the builder's char array, without rebuilding.
     * After clear() has been called, a new array will be used.
     * @param buildOption Build option, see StringTrieBuilder.Option.
     * @return A new CharsTrie for the add()ed data.
     */
    public CharsTrie build(StringTrieBuilder.Option buildOption) {
        return new CharsTrie(buildCharSequence(buildOption), 0);
    }

    /**
     * Builds a CharsTrie for the add()ed data and char-serializes it.
     * Once built, no further data can be add()ed until clear() is called.
     *
     * <p>A CharsTrie cannot be empty. At least one (string, value) pair
     * must have been add()ed.
     *
     * <p>Multiple calls to build() or buildCharSequence() return tries or sequences
     * which share the builder's char array, without rebuilding.
     * After clear() has been called, a new array will be used.
     * @param buildOption Build option, see StringTrieBuilder.Option.
     * @return A CharSequence with the char-serialized CharsTrie for the add()ed data.
     */
    public CharSequence buildCharSequence(StringTrieBuilder.Option buildOption) {
        buildChars(buildOption);
        return CharBuffer.wrap(chars, chars.length-charsLength, charsLength);
    }

    private void buildChars(StringTrieBuilder.Option buildOption) {
        // Create and char-serialize the trie for the elements.
        if(chars==null) {
            chars=new char[1024];
        }
        buildImpl(buildOption);
    }

    /**
     * Removes all (string, value) pairs.
     * New data can then be add()ed and a new trie can be built.
     * @return this
     */
    public CharsTrieBuilder clear() {
        clearImpl();
        chars=null;
        charsLength=0;
        return this;
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected boolean matchNodesCanHaveValues() /*const*/ { return true; }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMaxBranchLinearSubNodeLength() /*const*/ { return CharsTrie.kMaxBranchLinearSubNodeLength; }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMinLinearMatch() /*const*/ { return CharsTrie.kMinLinearMatch; }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMaxLinearMatchLength() /*const*/ { return CharsTrie.kMaxLinearMatchLength; }

    private void ensureCapacity(int length) {
        if(length>chars.length) {
            int newCapacity=chars.length;
            do {
                newCapacity*=2;
            } while(newCapacity<=length);
            char[] newChars=new char[newCapacity];
            System.arraycopy(chars, chars.length-charsLength,
                             newChars, newChars.length-charsLength, charsLength);
            chars=newChars;
        }
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int write(int unit) {
        int newLength=charsLength+1;
        ensureCapacity(newLength);
        charsLength=newLength;
        chars[chars.length-charsLength]=(char)unit;
        return charsLength;
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int write(int offset, int length) {
        int newLength=charsLength+length;
        ensureCapacity(newLength);
        charsLength=newLength;
        int charsOffset=chars.length-charsLength;
        while(length>0) {
            chars[charsOffset++]=strings.charAt(offset++);
            --length;
        }
        return charsLength;
    }
    private int write(char[] s, int length) {
        int newLength=charsLength+length;
        ensureCapacity(newLength);
        charsLength=newLength;
        System.arraycopy(s, 0, chars, chars.length-charsLength, length);
        return charsLength;
    }

    // For writeValueAndFinal(), writeValueAndType() and writeDeltaTo().
    private final char[] intUnits=new char[3];

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeValueAndFinal(int i, boolean isFinal) {
        if(0<=i && i<=CharsTrie.kMaxOneUnitValue) {
            return write(i|(isFinal ? CharsTrie.kValueIsFinal : 0));
        }
        int length;
        if(i<0 || i>CharsTrie.kMaxTwoUnitValue) {
            intUnits[0]=(char)(CharsTrie.kThreeUnitValueLead);
            intUnits[1]=(char)(i>>16);
            intUnits[2]=(char)i;
            length=3;
        // } else if(i<=CharsTrie.kMaxOneUnitValue) {
        //     intUnits[0]=(char)(i);
        //     length=1;
        } else {
            intUnits[0]=(char)(CharsTrie.kMinTwoUnitValueLead+(i>>16));
            intUnits[1]=(char)i;
            length=2;
        }
        intUnits[0]=(char)(intUnits[0]|(isFinal ? CharsTrie.kValueIsFinal : 0));
        return write(intUnits, length);
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeValueAndType(boolean hasValue, int value, int node) {
        if(!hasValue) {
            return write(node);
        }
        int length;
        if(value<0 || value>CharsTrie.kMaxTwoUnitNodeValue) {
            intUnits[0]=(char)(CharsTrie.kThreeUnitNodeValueLead);
            intUnits[1]=(char)(value>>16);
            intUnits[2]=(char)value;
            length=3;
        } else if(value<=CharsTrie.kMaxOneUnitNodeValue) {
            intUnits[0]=(char)((value+1)<<6);
            length=1;
        } else {
            intUnits[0]=(char)(CharsTrie.kMinTwoUnitNodeValueLead+((value>>10)&0x7fc0));
            intUnits[1]=(char)value;
            length=2;
        }
        intUnits[0]|=(char)node;
        return write(intUnits, length);
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeDeltaTo(int jumpTarget) {
        int i=charsLength-jumpTarget;
        assert(i>=0);
        if(i<=CharsTrie.kMaxOneUnitDelta) {
            return write(i);
        }
        int length;
        if(i<=CharsTrie.kMaxTwoUnitDelta) {
            intUnits[0]=(char)(CharsTrie.kMinTwoUnitDeltaLead+(i>>16));
            length=1;
        } else {
            intUnits[0]=(char)(CharsTrie.kThreeUnitDeltaLead);
            intUnits[1]=(char)(i>>16);
            length=2;
        }
        intUnits[length++]=(char)i;
        return write(intUnits, length);
    }

    // char serialization of the trie.
    // Grows from the back: charsLength measures from the end of the buffer!
    private char[] chars;
    private int charsLength;
}
