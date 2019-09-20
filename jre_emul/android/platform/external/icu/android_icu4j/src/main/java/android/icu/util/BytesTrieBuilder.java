/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011-2014, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jan05
*   created by: Markus W. Scherer
*   ported from ICU4C bytestriebuilder.h/.cpp
*/

package android.icu.util;

import java.nio.ByteBuffer;

/**
 * Builder class for BytesTrie.
 *
 * <p>This class is not intended for public subclassing.
 *
 * @author Markus W. Scherer
 * @hide Only a subset of ICU is exposed in Android
 */
public final class BytesTrieBuilder extends StringTrieBuilder {
    /**
     * Constructs an empty builder.
     */
    public BytesTrieBuilder() {}

    // Used in add() to wrap the bytes into a CharSequence for StringTrieBuilder.addImpl().
    private static final class BytesAsCharSequence implements CharSequence {
        public BytesAsCharSequence(byte[] sequence, int length) {
            s=sequence;
            len=length;
        }
        public char charAt(int i) { return (char)(s[i]&0xff); }
        public int length() { return len; }
        public CharSequence subSequence(int start, int end) { return null; }

        private byte[] s;
        private int len;
    }

    /**
     * Adds a (byte sequence, value) pair.
     * The byte sequence must be unique.
     * Bytes 0..length-1 will be copied; the builder does not keep
     * a reference to the input array.
     * @param sequence The array that contains the byte sequence, starting at index 0.
     * @param length The length of the byte sequence.
     * @param value The value associated with this byte sequence.
     * @return this
     */
    public BytesTrieBuilder add(byte[] sequence, int length, int value) {
        addImpl(new BytesAsCharSequence(sequence, length), value);
        return this;
    }

    /**
     * Builds a BytesTrie for the add()ed data.
     * Once built, no further data can be add()ed until clear() is called.
     *
     * <p>A BytesTrie cannot be empty. At least one (byte sequence, value) pair
     * must have been add()ed.
     *
     * <p>Multiple calls to build() or buildByteBuffer() return tries or buffers
     * which share the builder's byte array, without rebuilding.
     * <em>The byte array must not be modified via the buildByteBuffer() result object.</em>
     * After clear() has been called, a new array will be used.
     * @param buildOption Build option, see StringTrieBuilder.Option.
     * @return A new BytesTrie for the add()ed data.
     */
    public BytesTrie build(StringTrieBuilder.Option buildOption) {
        buildBytes(buildOption);
        return new BytesTrie(bytes, bytes.length-bytesLength);
    }

    /**
     * Builds a BytesTrie for the add()ed data and byte-serializes it.
     * Once built, no further data can be add()ed until clear() is called.
     *
     * <p>A BytesTrie cannot be empty. At least one (byte sequence, value) pair
     * must have been add()ed.
     *
     * <p>Multiple calls to build() or buildByteBuffer() return tries or buffers
     * which share the builder's byte array, without rebuilding.
     * <em>Do not modify the bytes in the buffer!</em>
     * After clear() has been called, a new array will be used.
     *
     * <p>The serialized BytesTrie is accessible via the buffer's
     * array()/arrayOffset()+position() or remaining()/get(byte[]) etc.
     * @param buildOption Build option, see StringTrieBuilder.Option.
     * @return A ByteBuffer with the byte-serialized BytesTrie for the add()ed data.
     *         The buffer is not read-only and array() can be called.
     */
    public ByteBuffer buildByteBuffer(StringTrieBuilder.Option buildOption) {
        buildBytes(buildOption);
        return ByteBuffer.wrap(bytes, bytes.length-bytesLength, bytesLength);
    }

    private void buildBytes(StringTrieBuilder.Option buildOption) {
        // Create and byte-serialize the trie for the elements.
        if(bytes==null) {
            bytes=new byte[1024];
        }
        buildImpl(buildOption);
    }

    /**
     * Removes all (byte sequence, value) pairs.
     * New data can then be add()ed and a new trie can be built.
     * @return this
     */
    public BytesTrieBuilder clear() {
        clearImpl();
        bytes=null;
        bytesLength=0;
        return this;
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected boolean matchNodesCanHaveValues() /*const*/ { return false; }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMaxBranchLinearSubNodeLength() /*const*/ { return BytesTrie.kMaxBranchLinearSubNodeLength; }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMinLinearMatch() /*const*/ { return BytesTrie.kMinLinearMatch; }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int getMaxLinearMatchLength() /*const*/ { return BytesTrie.kMaxLinearMatchLength; }

    private void ensureCapacity(int length) {
        if(length>bytes.length) {
            int newCapacity=bytes.length;
            do {
                newCapacity*=2;
            } while(newCapacity<=length);
            byte[] newBytes=new byte[newCapacity];
            System.arraycopy(bytes, bytes.length-bytesLength,
                             newBytes, newBytes.length-bytesLength, bytesLength);
            bytes=newBytes;
        }
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int write(int b) {
        int newLength=bytesLength+1;
        ensureCapacity(newLength);
        bytesLength=newLength;
        bytes[bytes.length-bytesLength]=(byte)b;
        return bytesLength;
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int write(int offset, int length) {
        int newLength=bytesLength+length;
        ensureCapacity(newLength);
        bytesLength=newLength;
        int bytesOffset=bytes.length-bytesLength;
        while(length>0) {
            bytes[bytesOffset++]=(byte)strings.charAt(offset++);
            --length;
        }
        return bytesLength;
    }
    private int write(byte[] b, int length) {
        int newLength=bytesLength+length;
        ensureCapacity(newLength);
        bytesLength=newLength;
        System.arraycopy(b, 0, bytes, bytes.length-bytesLength, length);
        return bytesLength;
    }

    // For writeValueAndFinal() and writeDeltaTo().
    private final byte[] intBytes=new byte[5];

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeValueAndFinal(int i, boolean isFinal) {
        if(0<=i && i<=BytesTrie.kMaxOneByteValue) {
            return write(((BytesTrie.kMinOneByteValueLead+i)<<1)|(isFinal?1:0));
        }
        int length=1;
        if(i<0 || i>0xffffff) {
            intBytes[0]=(byte)BytesTrie.kFiveByteValueLead;
            intBytes[1]=(byte)(i>>24);
            intBytes[2]=(byte)(i>>16);
            intBytes[3]=(byte)(i>>8);
            intBytes[4]=(byte)i;
            length=5;
        // } else if(i<=BytesTrie.kMaxOneByteValue) {
        //     intBytes[0]=(byte)(BytesTrie.kMinOneByteValueLead+i);
        } else {
            if(i<=BytesTrie.kMaxTwoByteValue) {
                intBytes[0]=(byte)(BytesTrie.kMinTwoByteValueLead+(i>>8));
            } else {
                if(i<=BytesTrie.kMaxThreeByteValue) {
                    intBytes[0]=(byte)(BytesTrie.kMinThreeByteValueLead+(i>>16));
                } else {
                    intBytes[0]=(byte)BytesTrie.kFourByteValueLead;
                    intBytes[1]=(byte)(i>>16);
                    length=2;
                }
                intBytes[length++]=(byte)(i>>8);
            }
            intBytes[length++]=(byte)i;
        }
        intBytes[0]=(byte)((intBytes[0]<<1)|(isFinal?1:0));
        return write(intBytes, length);
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeValueAndType(boolean hasValue, int value, int node) {
        int offset=write(node);
        if(hasValue) {
            offset=writeValueAndFinal(value, false);
        }
        return offset;
    }
    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected int writeDeltaTo(int jumpTarget) {
        int i=bytesLength-jumpTarget;
        assert(i>=0);
        if(i<=BytesTrie.kMaxOneByteDelta) {
            return write(i);
        }
        int length;
        if(i<=BytesTrie.kMaxTwoByteDelta) {
            intBytes[0]=(byte)(BytesTrie.kMinTwoByteDeltaLead+(i>>8));
            length=1;
        } else {
            if(i<=BytesTrie.kMaxThreeByteDelta) {
                intBytes[0]=(byte)(BytesTrie.kMinThreeByteDeltaLead+(i>>16));
                length=2;
            } else {
                if(i<=0xffffff) {
                    intBytes[0]=(byte)BytesTrie.kFourByteDeltaLead;
                    length=3;
                } else {
                    intBytes[0]=(byte)BytesTrie.kFiveByteDeltaLead;
                    intBytes[1]=(byte)(i>>24);
                    length=4;
                }
                intBytes[1]=(byte)(i>>16);
            }
            intBytes[1]=(byte)(i>>8);
        }
        intBytes[length++]=(byte)i;
        return write(intBytes, length);
    }

    // Byte serialization of the trie.
    // Grows from the back: bytesLength measures from the end of the buffer!
    private byte[] bytes;
    private int bytesLength;
}
