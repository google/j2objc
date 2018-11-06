/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011, Google, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.icu.impl.Utility;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrie.Result;
import android.icu.util.BytesTrieBuilder;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder.Option;

// would be nice to have a BytesTrieBuilder.add(aByte);
// question: can bytetrie store <"",x>?
// can you store the same string twice, eg add(bytes1, value), add(bytes1, value)? What happens? If an error,
// should happen on add, not on build.
// the BytesTrieBuilder.build should create a BytesTrie, not a raw array. For the latter, use buildArray or something.
// need class description; examples of usage; which method can/should be called after which others.

public abstract class TrieMap<V> implements Iterable<Entry<CharSequence, V>> {

    public enum Style {
        BYTES, CHARS
    }

    private static final boolean DEBUG = true;

    protected final V[] intToValue;
    protected final int size;

    protected TrieMap(V[] intToValue, int size) {
        this.intToValue = intToValue;
        this.size = size;
    }

    public int keyByteSize() {
        return size;
    }

    public static abstract class Builder<V> {
        Option option;
        protected List<V> intToValueTemp = new ArrayList<V>();
        protected Map<V, Integer> valueToIntegerTemp = new HashMap<V, Integer>();

        static public <K extends CharSequence, V> Builder<V> with(Style style, Map<K, V> keyValuePairs) {
            return with(style, Option.SMALL, keyValuePairs);
        }
        
        static public <K extends CharSequence, V> Builder<V> with(Style style, Option option, Map<K, V> keyValuePairs) {
            Builder<V> result = style == Style.BYTES ? new BytesTrieMap.BytesBuilder<V>()
                    : new CharsTrieMap.CharsBuilder<V>();
            result.option = option;
            return result.addAll(keyValuePairs);
        }

        static public <K extends CharSequence, V> Builder<V> with(Style style, K key, V value) {
            return with(style, Option.SMALL, key, value);
        }

        static public <K extends CharSequence, V> Builder<V> with(Style style, Option option, K key, V value) {
            Builder<V> result = style == Style.BYTES ? new BytesTrieMap.BytesBuilder<V>()
                    : new CharsTrieMap.CharsBuilder<V>();
            result.option = option;
            return result.add(key, value);
        }

        public abstract Builder<V> add(CharSequence key, V value);

        public abstract <K extends CharSequence> Builder<V> addAll(Map<K, V> keyValuePairs);

        public abstract TrieMap<V> build();
    }

    abstract public V get(CharSequence test);

    /**
     * Warning: the entry contents are only valid until the next next() call!!
     */
    abstract public Iterator<Entry<CharSequence, V>> iterator();

    abstract public Matcher<V> getMatcher();

    public abstract static class Matcher<V> {
        protected CharSequence text = "";
        protected int start = 0;
        protected int current = 0;

        abstract void set(CharSequence string, int i);

        abstract boolean next();

        abstract V getValue();

        abstract int getStart();

        abstract int getEnd();

        abstract boolean nextStart();
    }

    private static class BytesTrieMap<V> extends TrieMap<V> {
        private final BytesTrie bytesTrie;
        private byte[] bytes = new byte[3];

        private BytesTrieMap(BytesTrie bytesTrie, V[] intToValue, int size) {
            super(intToValue, size);
            this.bytesTrie = bytesTrie;
        }

        public V get(CharSequence test) {
            int length = test.length();
            bytesTrie.reset();
            if (length == 0) {
                return bytesTrie.current().hasValue() ? intToValue[bytesTrie.getValue()] : null;
            }
            Result result = Result.NO_VALUE;
            for (int i = 0; i < length; ++i) {
                if (!result.hasNext()) {
                    return null;
                }
                char c = test.charAt(i);
                int limit = ByteConverter.getBytes(c, bytes, 0);
                result = limit == 1 ? bytesTrie.next(bytes[0]) : bytesTrie.next(bytes, 0, limit);
            }
            return result.hasValue() ? intToValue[bytesTrie.getValue()] : null;

            // int length = test.length();
            // if (length == 0) {
            // return null;
            // }
            // bytesTrie.reset();
            // Result result = null;
            // byte[] bytes = new byte[3];
            // for (int i = 0; i < length; ++i) {
            // char c = test.charAt(i);
            // int limit = ByteConverter.getBytes(c, bytes, 0);
            // for (int j = 0; j < limit; ++j) {
            // result = bytesTrie.next(bytes[j]&0xFF);
            // if (!result.matches()) {
            // return null;
            // }
            // }
            // }
            // return result.hasValue() ? intToValue[bytesTrie.getValue()] : null;
        }

        public String toString() {
            return toString(bytesTrie, " : ", "\n");
        }

        /**
         * Warning: the entry contents are only valid until the next next() call!!
         */
        public Iterator<Entry<CharSequence, V>> iterator() {
            // TODO Auto-generated method stub
            return new BytesIterator();
        }

        public TrieMap.Matcher<V> getMatcher() {
            return new BytesMatcher();
        }

        private class BytesIterator implements Iterator<Entry<CharSequence, V>> {
            BytesTrie.Iterator iterator = bytesTrie.iterator();
            BytesEntry entry = new BytesEntry();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Entry<CharSequence, V> next() {
                entry.bytesEntry = iterator.next();
                return entry;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        private class BytesEntry implements Entry<CharSequence, V> {
            public BytesTrie.Entry bytesEntry;
            StringBuilder buffer = new StringBuilder();

            public CharSequence getKey() {
                buffer.setLength(0);
                ByteConverter.getChars(bytesEntry, buffer);
                return buffer;
            }

            public V getValue() {
                return intToValue[bytesEntry.value];
            }

            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        }

        public class BytesMatcher extends Matcher<V> {
            private byte[] bytes = new byte[3];
            private V value = null;

            public void set(CharSequence text, int start) {
                this.text = text;
                this.start = start;
                this.current = start;
            }

            public int getStart() {
                return start;
            }

            public int getEnd() {
                return current;
            }

            /**
             * Finds the next match. Returns false when there are no possible further matches from the current start
             * point. Once that happens, call nextStart(); Call getValue to get the current value.
             * 
             * @return false when done. There may be a value, however.
             */
            public boolean next() {
                while (current < text.length()) {
                    char c = text.charAt(current++);
                    int limit = ByteConverter.getBytes(c, bytes, 0);
                    for (int j = 0; j < limit; ++j) {
                        Result result = bytesTrie.next(bytes[j]);
                        if (result.hasValue()) {
                            if (j < limit - 1) {
                                throw new IllegalArgumentException("Data corrupt");
                            }
                            value = intToValue[bytesTrie.getValue()];
                            return result.hasNext();
                        } else if (!result.matches()) {
                            value = null;
                            return false;
                        }
                    }
                }
                value = null;
                return false;
            }

            public boolean nextStart() {
                if (start >= text.length()) {
                    return false;
                }
                ++start;
                current = start;
                bytesTrie.reset();
                return true;
            }

            public V getValue() {
                return value;
            }
        }
    }

    public static class BytesBuilder<V> extends Builder<V> {
        BytesTrieBuilder builder = new BytesTrieBuilder();
        byte[] bytes = new byte[200];
        List<String> debugBytes = DEBUG ? new ArrayList<String>() : null;

        public BytesBuilder<V> add(CharSequence key, V value) {
            // traverse the values, and get a mapping of a byte string to list of
            // integers, and a mapping from those integers to a set of values
            Integer index;
            if (option == Option.SMALL) {
                index = valueToIntegerTemp.get(value);
                if (index == null) {
                    index = intToValueTemp.size();
                    intToValueTemp.add(value);
                    valueToIntegerTemp.put(value, index);
                }
            } else {
                index = intToValueTemp.size();
                intToValueTemp.add(value);
            }
            // dumb implementation for now
            // the buffer size is at most 3 * number_of_chars
            if (bytes.length < key.length() * 3) {
                bytes = new byte[64 + key.length() * 3];
            }
            int limit = 0;
            for (int i = 0; i < key.length(); ++i) {
                char c = key.charAt(i);
                limit = ByteConverter.getBytes(c, bytes, limit);
            }
            try {
                builder.add(bytes, limit, index);
                return this;
            } catch (Exception e) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < limit; ++i) {
                    list.add(Utility.hex(bytes[i]));
                }
                throw new IllegalArgumentException("Failed to add " + value + ", " + key + "=" + list, e);
            }
        }

        public <K extends CharSequence> BytesBuilder<V> addAll(Map<K, V> keyValuePairs) {
            for (Entry<K, V> entry : keyValuePairs.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public TrieMap<V> build() {
            int size = builder.buildByteBuffer(option).remaining();
            BytesTrie bytesTrie = builder.build(option);
            @SuppressWarnings("unchecked")
            V[] intToValueArray = intToValueTemp.toArray((V[]) (new Object[intToValueTemp.size()]));
            return new BytesTrieMap<V>(bytesTrie, intToValueArray, size);
        }
    }

    private static class CharsTrieMap<V> extends TrieMap<V> {
        private final CharsTrie charsTrie;

        private CharsTrieMap(CharsTrie charsTrie, V[] intToValue, int size) {
            super(intToValue, size);
            this.charsTrie = charsTrie;
        }

        public V get(CharSequence test) {
            Result result = charsTrie.reset().next(test, 0, test.length());
            return result.hasValue() ? intToValue[charsTrie.getValue()] : null;
        }

        public String toString() {
            return toString(charsTrie, " : ", "\n");
        }

        /**
         * Warning: the entry contents are only valid until the next next() call!!
         */
        public Iterator<Entry<CharSequence, V>> iterator() {
            // TODO Auto-generated method stub
            return new CharsIterator();
        }

        public TrieMap.Matcher<V> getMatcher() {
            return new CharsMatcher();
        }

        private class CharsIterator implements Iterator<Entry<CharSequence, V>> {
            CharsTrie.Iterator iterator = charsTrie.iterator();
            CharsEntry entry = new CharsEntry();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Entry<CharSequence, V> next() {
                entry.charsEntry = iterator.next();
                return entry;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        private class CharsEntry implements Entry<CharSequence, V> {
            public CharsTrie.Entry charsEntry;

            public CharSequence getKey() {
                return charsEntry.chars;
            }

            public V getValue() {
                return intToValue[charsEntry.value];
            }

            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        }

        public class CharsMatcher extends Matcher<V> {
            private V value = null;

            public void set(CharSequence text, int start) {
                this.text = text;
                this.start = start;
                this.current = start;
            }

            public int getStart() {
                return start;
            }

            public int getEnd() {
                return current;
            }

            /**
             * Finds the next match. Returns false when there are no possible further matches from the current start
             * point. Once that happens, call nextStart(); Call getValue to get the current value.
             * 
             * @return false when done. There may be a value, however.
             */
            public boolean next() {
                while (current < text.length()) {
                    char c = text.charAt(current++);
                    Result result = charsTrie.next(c);
                    if (result.hasValue()) {
                        value = intToValue[charsTrie.getValue()];
                        return result.hasNext();
                    } else if (!result.matches()) {
                        value = null;
                        return false;

                    }
                }
                value = null;
                return false;
            }

            public boolean nextStart() {
                if (start >= text.length()) {
                    return false;
                }
                ++start;
                current = start;
                charsTrie.reset();
                return true;
            }

            public V getValue() {
                return value;
            }
        }
    }

    public static class CharsBuilder<V> extends Builder<V> {
        CharsTrieBuilder builder = new CharsTrieBuilder();

        public CharsBuilder<V> add(CharSequence key, V value) {
            // traverse the values, and get a mapping of a byte string to list of
            // integers, and a mapping from those integers to a set of values
            Integer index;
            if (option == Option.SMALL) {
                index = valueToIntegerTemp.get(value);
                if (index == null) {
                    index = intToValueTemp.size();
                    intToValueTemp.add(value);
                    valueToIntegerTemp.put(value, index);
                }
            } else {
                index = intToValueTemp.size();
                intToValueTemp.add(value);
            }
            try {
                builder.add(key.toString(), index);
                return this;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to add " + value + ", " + key + "=" + Utility.hex(key), e);
            }
        }

        public <K extends CharSequence> CharsBuilder<V> addAll(Map<K, V> keyValuePairs) {
            for (Entry<K, V> entry : keyValuePairs.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public TrieMap<V> build() {
            CharSequence buildCharSequence = builder.buildCharSequence(option);
            int size = 2 * buildCharSequence.length();
            // CharsTrie charsTrie = builder.build(option);
            CharsTrie charsTrie = new CharsTrie(buildCharSequence, 0);
            @SuppressWarnings("unchecked")
            V[] intToValueArray = intToValueTemp.toArray((V[]) (new Object[intToValueTemp.size()]));
            return new CharsTrieMap<V>(charsTrie, intToValueArray, size);
        }
    }

    /**
     * Supports the following format for encoding chars (Unicode 16-bit code units). The format is slightly simpler and
     * more compact than UTF8, but also maintains ordering. It is not, however self-synchronizing, and is not intended
     * for general usage
     * 
     * <pre>
     * 0000..007F - 0xxx xxxx
     * 0000..7EFF - 1yyy yyyy xxxx xxxx
     * 7F00..FFFF - 1111 1111 yyyy yyyy xxxx xxxx
     * </pre>
     */
    static class ByteConverter {
        public static int getBytes(char source, byte[] bytes, int limit) {
            if (source < 0x80) {
                bytes[limit++] = (byte) source;
            } else if (source < 0x7F00) {
                bytes[limit++] = (byte) (0x80 | (source >> 8));
                bytes[limit++] = (byte) source;
            } else {
                bytes[limit++] = (byte) -1;
                bytes[limit++] = (byte) (source >> 8);
                bytes[limit++] = (byte) source;
            }
            return limit;
        }

        /**
         * Transform the string into a sequence of bytes, appending them after start, and return the new limit.
         */
        public static int getBytes(CharSequence source, byte[] bytes, int limit) {
            for (int i = 0; i < source.length(); ++i) {
                limit = getBytes(source.charAt(i), bytes, limit);
            }
            return limit;
        }

        /**
         * Transform a sequence of bytes into a string, according to the format in getBytes. No error checking.
         */
        public static String getChars(byte[] bytes, int start, int limit) {
            StringBuilder buffer = new StringBuilder();
            char[] output = new char[1];
            for (int i = start; i < limit;) {
                i = getChar(bytes, i, output);
                buffer.append(output[0]);
            }
            return buffer.toString();
        }

        public static int getChar(byte[] bytes, int start, char[] output) {
            byte b = bytes[start++];
            if (b >= 0) {
                output[0] = (char) b;
            } else if (b != (byte) -1) { // 2 bytes
                int b1 = 0x7F & b;
                int b2 = 0xFF & bytes[start++];
                output[0] = (char) ((b1 << 8) | b2);
            } else {
                int b2 = bytes[start++];
                int b3 = 0xFF & bytes[start++];
                output[0] = (char) ((b2 << 8) | b3);
            }
            return start;
        }

        private static void getChars(BytesTrie.Entry entry, StringBuilder stringBuilder) {
            int len = entry.bytesLength();
            for (int i = 0; i < len;) {
                byte b = entry.byteAt(i++);
                if (b >= 0) {
                    stringBuilder.append((char) b);
                } else if (b != (byte) -1) { // 2 bytes
                    int b1 = 0x7F & b;
                    int b2 = 0xFF & entry.byteAt(i++);
                    stringBuilder.append((char) ((b1 << 8) | b2));
                } else {
                    int b2 = entry.byteAt(i++);
                    int b3 = 0xFF & entry.byteAt(i++);
                    stringBuilder.append((char) ((b2 << 8) | b3));
                }
            }
        }
    }

    public static String toString(BytesTrie bytesTrie2) {
        return toString(bytesTrie2, " : ", "\n");
    }

    public static String toString(BytesTrie bytesTrie2, String keyValueSeparator, String itemSeparator) {
        StringBuilder buffer = new StringBuilder();
        BytesTrie.Iterator iterator = bytesTrie2.iterator();
        while (iterator.hasNext()) {
            BytesTrie.Entry bytesEntry = iterator.next();
            int len = bytesEntry.bytesLength();
            byte[] bytes = new byte[len];
            bytesEntry.copyBytesTo(bytes, 0);
            buffer.append(Utility.hex(bytes, 0, len, " "))
                    .append(keyValueSeparator)
                    .append(bytesEntry.value)
                    .append(itemSeparator);
        }
        return buffer.toString();
    }

    public static String toString(CharsTrie bytesTrie2, String keyValueSeparator, String itemSeparator) {
        StringBuilder buffer = new StringBuilder();
        CharsTrie.Iterator iterator = bytesTrie2.iterator();
        while (iterator.hasNext()) {
            CharsTrie.Entry bytesEntry = iterator.next();
            buffer.append(Utility.hex(bytesEntry.chars))
                    .append(keyValueSeparator)
                    .append(bytesEntry.value)
                    .append(itemSeparator);
        }
        return buffer.toString();
    }

}
