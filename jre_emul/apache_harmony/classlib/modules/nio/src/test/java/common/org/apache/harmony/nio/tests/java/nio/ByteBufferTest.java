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

package org.apache.harmony.nio.tests.java.nio;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;

/**
 * Tests java.nio.ByteBuffer
 * 
 */
public class ByteBufferTest extends AbstractBufferTest {
    protected static final int SMALL_TEST_LENGTH = 5;
    protected static final int BUFFER_LENGTH = 250;
    
    protected ByteBuffer buf;

    protected void setUp() throws Exception {
        buf = ByteBuffer.allocate(10);
        loadTestData1(buf);
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testArray() {
        if (buf.hasArray()) {
            byte array[] = buf.array();
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData1(array, buf.arrayOffset(), buf.capacity());
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData2(array, buf.arrayOffset(), buf.capacity());
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData1(buf);
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData2(buf);
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        } else {
            if (buf.isReadOnly()) {
                try {
                    buf.array();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                    // Note:can not tell when to throw 
                    // UnsupportedOperationException
                    // or ReadOnlyBufferException, so catch all.
                }
            } else {
                try {
                    buf.array();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                }
            }
        }
    }

    public void testArrayOffset() {
        if (buf.hasArray()) {
            byte array[] = buf.array();
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData1(array, buf.arrayOffset(), buf.capacity());
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData2(array, buf.arrayOffset(), buf.capacity());
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData1(buf);
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());

            loadTestData2(buf);
            assertContentEquals(buf, array, buf.arrayOffset(), buf.capacity());
        } else {
            if (buf.isReadOnly()) {
                try {
                    buf.arrayOffset();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                    // Note:can not tell when to throw 
                    // UnsupportedOperationException
                    // or ReadOnlyBufferException, so catch all.
                }
            } else {
                try {
                    buf.arrayOffset();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                }
            }
        }
    }

    public void testAsReadOnlyBuffer() {
        buf.clear();
        buf.mark();
        buf.position(buf.limit());

        // readonly's contents should be the same as buf
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        assertNotSame(buf, readonly);
        assertTrue(readonly.isReadOnly());
        assertEquals(buf.position(), readonly.position());
        assertEquals(buf.limit(), readonly.limit());
        assertEquals(buf.isDirect(), readonly.isDirect());
        assertEquals(buf.order(), readonly.order());
        assertContentEquals(buf, readonly);

        // readonly's position, mark, and limit should be independent to buf
        readonly.reset();
        assertEquals(readonly.position(), 0);
        readonly.clear();
        assertEquals(buf.position(), buf.limit());
        buf.reset();
        assertEquals(buf.position(), 0);
    }

    public void testCompact() {
        if (buf.isReadOnly()) {
            try {
                buf.compact();
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        // case: buffer is full
        buf.clear();
        buf.mark();
        loadTestData1(buf);
        ByteBuffer ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), buf.capacity());
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, (byte) 0, buf.capacity());
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // case: buffer is empty
        buf.position(0);
        buf.limit(0);
        buf.mark();
        ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), 0);
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, (byte) 0, buf.capacity());
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // case: normal
        assertTrue(buf.capacity() > SMALL_TEST_LENGTH);
        buf.position(1);
        buf.limit(SMALL_TEST_LENGTH);
        buf.mark();
        ret = buf.compact();
        assertSame(ret, buf);
        assertEquals(buf.position(), 4);
        assertEquals(buf.limit(), buf.capacity());
        assertContentLikeTestData1(buf, 0, (byte) 1, 4);
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }
    }

    public void testCompareTo() {
        // compare to self
        assertEquals(0, buf.compareTo(buf));

        // normal cases
        if (!buf.isReadOnly()) {
            assertTrue(buf.capacity() > SMALL_TEST_LENGTH);
            buf.clear();
            ByteBuffer other = ByteBuffer.allocate(buf.capacity());
            loadTestData1(buf);
            loadTestData1(other);
            assertEquals(0, buf.compareTo(other));
            assertEquals(0, other.compareTo(buf));
            buf.position(1);
            assertTrue(buf.compareTo(other) > 0);
            assertTrue(other.compareTo(buf) < 0);
            other.position(2);
            assertTrue(buf.compareTo(other) < 0);
            assertTrue(other.compareTo(buf) > 0);
            buf.position(2);
            other.limit(SMALL_TEST_LENGTH);
            assertTrue(buf.compareTo(other) > 0);
            assertTrue(other.compareTo(buf) < 0);
        }
    }

    public void testDuplicate() {
        buf.clear();
        buf.mark();
        buf.position(buf.limit());

        // duplicate's contents should be the same as buf
        ByteBuffer duplicate = buf.duplicate();
        assertNotSame(buf, duplicate);
        assertEquals(buf.position(), duplicate.position());
        assertEquals(buf.limit(), duplicate.limit());
        assertEquals(buf.isReadOnly(), duplicate.isReadOnly());
        assertEquals(buf.isDirect(), duplicate.isDirect());
        assertEquals(buf.order(), duplicate.order());
        assertContentEquals(buf, duplicate);

        // duplicate's position, mark, and limit should be independent to buf
        duplicate.reset();
        assertEquals(duplicate.position(), 0);
        duplicate.clear();
        assertEquals(buf.position(), buf.limit());
        buf.reset();
        assertEquals(buf.position(), 0);

        // duplicate share the same content with buf
        if (!duplicate.isReadOnly()) {
            loadTestData1(buf);
            assertContentEquals(buf, duplicate);
            loadTestData2(duplicate);
            assertContentEquals(buf, duplicate);
        }
    }

    public void testEquals() {
        // equal to self
        assertTrue(buf.equals(buf));
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        assertTrue(buf.equals(readonly));
        ByteBuffer duplicate = buf.duplicate();
        assertTrue(buf.equals(duplicate));

        // always false, if type mismatch
        assertFalse(buf.equals(Boolean.TRUE));

        assertTrue(buf.capacity() > SMALL_TEST_LENGTH);

        buf.limit(buf.capacity()).position(0);
        readonly.limit(readonly.capacity()).position(1);
        assertFalse(buf.equals(readonly));

        buf.limit(buf.capacity() - 1).position(0);
        duplicate.limit(duplicate.capacity()).position(0);
        assertFalse(buf.equals(duplicate));
    }

    /*
     * Class under test for byte get()
     */
    public void testGet() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            assertEquals(buf.get(), buf.get(i));
        }
        try {
            buf.get();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }
    }

    /*
     * Class under test for java.nio.ByteBuffer get(byte[])
     */
    public void testGetbyteArray() {
        byte array[] = new byte[1];
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            ByteBuffer ret = buf.get(array);
            assertEquals(array[0], buf.get(i));
            assertSame(ret, buf);
        }
        try {
            buf.get(array);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }
        try {
            buf.get((byte[])null);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Class under test for java.nio.ByteBuffer get(byte[], int, int)
     */
    public void testGetbyteArrayintint() {
        buf.clear();
        byte array[] = new byte[buf.capacity()];

        try {
            buf.get(new byte[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }
        assertEquals(buf.position(), 0);
        try {
            buf.get(array, -1, array.length);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        buf.get(array, array.length, 0);
        try {
            buf.get(array, array.length + 1, 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        assertEquals(buf.position(), 0);
        try {
            buf.get(array, 2, -1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.get(array, 2, array.length);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.get((byte[])null, -1, 0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
        try {
            buf.get(array, 1, Integer.MAX_VALUE);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.get(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        assertEquals(buf.position(), 0);

        buf.clear();
        ByteBuffer ret = buf.get(array, 0, array.length);
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(buf, array, 0, array.length);
        assertSame(ret, buf);
    }

    /*
     * Class under test for byte get(int)
     */
    public void testGetint() {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            assertEquals(buf.get(), buf.get(i));
        }
        try {
            buf.get(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.get(buf.limit());
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testHasArray() {
        if (buf.hasArray()) {
            assertNotNull(buf.array());
        } else {
            if (buf.isReadOnly()) {
                try {
                    buf.array();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                    // Note:can not tell when to throw 
                    // UnsupportedOperationException
                    // or ReadOnlyBufferException, so catch all.
                }
            } else {
                try {
                    buf.array();
                    fail("Should throw Exception"); //$NON-NLS-1$
                } catch (UnsupportedOperationException e) {
                    // expected
                }
            }
        }
    }

    public void testHashCode() {
        buf.clear();
        loadTestData1(buf);
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        ByteBuffer duplicate = buf.duplicate();
        assertTrue(buf.hashCode() == readonly.hashCode());
        assertTrue(buf.capacity() > SMALL_TEST_LENGTH);
        duplicate.position(buf.capacity()/2);
        assertTrue(buf.hashCode()!= duplicate.hashCode());
    }
    
    //for the testHashCode() method of readonly subclasses
    protected void readOnlyHashCode() {
        //create a new buffer initiated with some data 
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_LENGTH);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        buf.clear();
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        ByteBuffer duplicate = buf.duplicate();
        assertEquals(buf.hashCode(),readonly.hashCode());
        duplicate.position(buf.capacity()/2);
        assertTrue(buf.hashCode()!= duplicate.hashCode());
    }

    public void testIsDirect() {
        buf.isDirect();
    }

    public void testOrder() {
        // BIG_ENDIAN is the default byte order
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order());

        buf.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buf.order());
        
        buf.order(ByteOrder.BIG_ENDIAN);
        assertEquals(ByteOrder.BIG_ENDIAN, buf.order());

        // Regression test for HARMONY-798
        buf.order((ByteOrder)null);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buf.order());
        
        buf.order(ByteOrder.BIG_ENDIAN);
    }

    /*
     * Class under test for java.nio.ByteBuffer put(byte)
     */
    public void testPutbyte() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.put((byte) 0);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            ByteBuffer ret = buf.put((byte) i);
            assertEquals(buf.get(i), (byte) i);
            assertSame(ret, buf);
        }
        try {
            buf.put((byte) 0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }
    }

    /*
     * Class under test for java.nio.ByteBuffer put(byte[])
     */
    public void testPutbyteArray() {
        byte array[] = new byte[1];
        if (buf.isReadOnly()) {
            try {
                buf.put(array);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), i);
            array[0] = (byte) i;
            ByteBuffer ret = buf.put(array);
            assertEquals(buf.get(i), (byte) i);
            assertSame(ret, buf);
        }
        try {
            buf.put(array);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }
        try {
            buf.put((byte[])null);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Class under test for java.nio.ByteBuffer put(byte[], int, int)
     */
    public void testPutbyteArrayintint() {
        buf.clear();
        byte array[] = new byte[buf.capacity()];
        if (buf.isReadOnly()) {
            try {
                buf.put(array, 0, array.length);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        try {
            buf.put(new byte[buf.capacity() + 1], 0, buf.capacity() + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }
        assertEquals(buf.position(), 0);
        try {
            buf.put(array, -1, array.length);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.put(array, array.length + 1, 0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        buf.put(array, array.length, 0);
        assertEquals(buf.position(), 0);
        try {
            buf.put(array, 0, -1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.put(array, 2, array.length);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            buf.put(array, 2, Integer.MAX_VALUE);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.put(array, Integer.MAX_VALUE, 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.put((byte[])null, 2, Integer.MAX_VALUE);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
        
        assertEquals(buf.position(), 0);

        loadTestData2(array, 0, array.length);
        ByteBuffer ret = buf.put(array, 0, array.length);
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(buf, array, 0, array.length);
        assertSame(ret, buf);
    }

    /*
     * Class under test for java.nio.ByteBuffer put(java.nio.ByteBuffer)
     */
    public void testPutByteBuffer() {
        ByteBuffer other = ByteBuffer.allocate(buf.capacity());
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.put(other);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            try {
                buf.clear();
                buf.put((ByteBuffer)null);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        try {
            buf.put(buf);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            buf.put(ByteBuffer.allocate(buf.capacity() + 1));
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }
        
        try {
            buf.put((ByteBuffer)null);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
        loadTestData2(other);
        other.clear();
        buf.clear();
        ByteBuffer ret = buf.put(other);
        assertEquals(other.position(), other.capacity());
        assertEquals(buf.position(), buf.capacity());
        assertContentEquals(other, buf);
        assertSame(ret, buf);
    }

    /*
     * Class under test for java.nio.ByteBuffer put(int, byte)
     */
    public void testPutintbyte() {
        if (buf.isReadOnly()) {
            try {
                buf.put(0, (byte) 0);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.position(), 0);
            ByteBuffer ret = buf.put(i, (byte) i);
            assertEquals(buf.get(i), (byte) i);
            assertSame(ret, buf);
        }
        try {
            buf.put(-1, (byte) 0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.put(buf.limit(), (byte) 0);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testSlice() {
        assertTrue(buf.capacity() > SMALL_TEST_LENGTH);
        buf.position(1);
        buf.limit(buf.capacity() - 1);

        ByteBuffer slice = buf.slice();
        assertEquals(buf.isReadOnly(), slice.isReadOnly());
        assertEquals(buf.isDirect(), slice.isDirect());
        assertEquals(buf.order(), slice.order());
        assertEquals(slice.position(), 0);
        assertEquals(slice.limit(), buf.remaining());
        assertEquals(slice.capacity(), buf.remaining());
        try {
            slice.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // slice share the same content with buf
        if (!slice.isReadOnly()) {
            loadTestData1(slice);
            assertContentLikeTestData1(buf, 1, (byte) 0, slice.capacity());
            buf.put(2, (byte) 100);
            assertEquals(slice.get(1), 100);
        }
    }

    public void testToString() {
        String str = buf.toString();
        assertTrue(str.indexOf("Byte") >= 0 || str.indexOf("byte") >= 0);
        assertTrue(str.indexOf("" + buf.position()) >= 0);
        assertTrue(str.indexOf("" + buf.limit()) >= 0);
        assertTrue(str.indexOf("" + buf.capacity()) >= 0);
    }

    public void testGetChar() {
        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        char value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getChar();
            assertEquals(bytes2char(bytes, buf.order()), value);
        }

        try {
            buf.getChar();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetCharint() {
        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        char value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getChar(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertEquals(bytes2char(bytes, buf.order()), value);
        }

        try {
            buf.getChar(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getChar(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutChar() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putChar((char) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        char value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (char) i;
            buf.mark();
            buf.putChar(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(char2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putChar(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutCharint() {
        if (buf.isReadOnly()) {
            try {
                buf.putChar(0, (char) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        char value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (char) i;
            buf.position(i);
            buf.putChar(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(char2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putChar(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putChar(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetDouble() {
        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        double value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getDouble();
            if (!(Double.isNaN(bytes2double(bytes, buf.order())) && Double
                    .isNaN(value))) {
                assertEquals(bytes2double(bytes, buf.order()), value, 0.00);
            }
        }

        try {
            buf.getDouble();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetDoubleint() {
        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        double value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getDouble(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            if (!(Double.isNaN(bytes2double(bytes, buf.order())) && Double
                    .isNaN(value))) {
                assertEquals(bytes2double(bytes, buf.order()), value, 0.00);
            }
        }

        try {
            buf.getDouble(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getDouble(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutDouble() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putDouble((double) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        double value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (double) i;
            buf.mark();
            buf.putDouble(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(double2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putDouble(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutDoubleint() {
        if (buf.isReadOnly()) {
            try {
                buf.putDouble(0, (double) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        double value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (double) i;
            buf.position(i);
            buf.putDouble(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(double2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putDouble(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putDouble(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetFloat() {
        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        float value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getFloat();
            if (!(Float.isNaN(bytes2float(bytes, buf.order())) && Float
                    .isNaN(value))) {
                assertEquals(bytes2float(bytes, buf.order()), value, 0.00);
            }
        }

        try {
            buf.getFloat();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetFloatint() {
        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        float value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getFloat(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            if (!(Float.isNaN(bytes2float(bytes, buf.order())) && Float
                    .isNaN(value))) {
                assertEquals(bytes2float(bytes, buf.order()), value, 0.00);
            }
        }

        try {
            buf.getFloat(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getFloat(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutFloat() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putFloat((float) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        float value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (float) i;
            buf.mark();
            buf.putFloat(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(float2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putFloat(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutFloatint() {
        if (buf.isReadOnly()) {
            try {
                buf.putFloat(0, (float) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        float value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (float) i;
            buf.position(i);
            buf.putFloat(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(float2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putFloat(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putFloat(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetInt() {
        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        int value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getInt();
            assertEquals(bytes2int(bytes, buf.order()), value);
        }

        try {
            buf.getInt();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetIntint() {
        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        int value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getInt(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertEquals(bytes2int(bytes, buf.order()), value);
        }

        try {
            buf.getInt(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getInt(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutInt() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putInt((int) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        int value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (int) i;
            buf.mark();
            buf.putInt(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(int2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putInt(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutIntint() {
        if (buf.isReadOnly()) {
            try {
                buf.putInt(0, (int) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 4;
        byte bytes[] = new byte[nbytes];
        int value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (int) i;
            buf.position(i);
            buf.putInt(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(int2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putInt(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putInt(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetLong() {
        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        long value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getLong();
            assertEquals(bytes2long(bytes, buf.order()), value);
        }

        try {
            buf.getLong();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetLongint() {
        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        long value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getLong(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertEquals(bytes2long(bytes, buf.order()), value);
        }

        try {
            buf.getLong(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getLong(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutLong() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putLong((long) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        long value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (long) i;
            buf.mark();
            buf.putLong(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(long2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putLong(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutLongint() {
        if (buf.isReadOnly()) {
            try {
                buf.putLong(0, (long) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 8;
        byte bytes[] = new byte[nbytes];
        long value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (long) i;
            buf.position(i);
            buf.putLong(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(long2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putLong(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putLong(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetShort() {
        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        short value;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            assertEquals(i * nbytes, buf.position());
            buf.mark();
            buf.get(bytes);
            buf.reset();
            value = buf.getShort();
            assertEquals(bytes2short(bytes, buf.order()), value);
        }

        try {
            buf.getShort();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferUnderflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testGetShortint() {
        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        short value;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            buf.position(i);
            value = buf.getShort(i);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertEquals(bytes2short(bytes, buf.order()), value);
        }

        try {
            buf.getShort(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.getShort(buf.limit() - nbytes + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutShort() {
        if (buf.isReadOnly()) {
            try {
                buf.clear();
                buf.putShort((short) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        short value = 0;
        buf.clear();
        for (int i = 0; buf.remaining() >= nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (short) i;
            buf.mark();
            buf.putShort(value);
            assertEquals((i + 1) * nbytes, buf.position());
            buf.reset();
            buf.get(bytes);
            assertTrue(Arrays.equals(short2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putShort(value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (BufferOverflowException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }

    public void testPutShortint() {
        if (buf.isReadOnly()) {
            try {
                buf.putShort(0, (short) 1);
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (ReadOnlyBufferException e) {
                // expected
            }
            return;
        }

        int nbytes = 2;
        byte bytes[] = new byte[nbytes];
        short value = 0;
        buf.clear();
        for (int i = 0; i <= buf.limit() - nbytes; i++) {
            buf.order(i % 2 == 0 ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN);
            value = (short) i;
            buf.position(i);
            buf.putShort(i, value);
            assertEquals(i, buf.position());
            buf.get(bytes);
            assertTrue(Arrays.equals(short2bytes(value, buf.order()), bytes));
        }

        try {
            buf.putShort(-1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            buf.putShort(buf.limit() - nbytes + 1, value);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        buf.order(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * @tests java.nio.ByteBuffer.wrap(byte[],int,int)
     */
    public void testWrappedByteBuffer_null_array() {
        // Regression for HARMONY-264
        byte array[] = null;
        try {
            ByteBuffer.wrap(array, -1, 0);
            fail("Should throw NPE"); //$NON-NLS-1$
        } catch (NullPointerException e) {
        }
        try {
            ByteBuffer.wrap(new byte[10], Integer.MAX_VALUE, 2);
            fail("Should throw IndexOutOfBoundsException"); //$NON-NLS-1$
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private void loadTestData1(byte array[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            array[offset + i] = (byte) i;
        }
    }

    private void loadTestData2(byte array[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            array[offset + i] = (byte) (length - i);
        }
    }

    private void loadTestData1(ByteBuffer buf) {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, (byte) i);
        }
    }

    private void loadTestData2(ByteBuffer buf) {
        buf.clear();
        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, (byte) (buf.capacity() - i));
        }
    }

    private void assertContentEquals(ByteBuffer buf, byte array[],
            int offset, int length) {
        for (int i = 0; i < length; i++) {
            assertEquals(buf.get(i), array[offset + i]);
        }
    }

    private void assertContentEquals(ByteBuffer buf, ByteBuffer other) {
        assertEquals(buf.capacity(), other.capacity());
        for (int i = 0; i < buf.capacity(); i++) {
            assertEquals(buf.get(i), other.get(i));
        }
    }

    private void assertContentLikeTestData1(ByteBuffer buf,
            int startIndex, byte startValue, int length) {
        byte value = startValue;
        for (int i = 0; i < length; i++) {
            assertEquals(buf.get(startIndex + i), value);
            value = (byte) (value + 1);
        }
    }

    private int bytes2int(byte bytes[], ByteOrder order) {
        int nbytes = 4, bigHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            bigHead = 0;
            step = 1;
        } else {
            bigHead = nbytes - 1;
            step = -1;
        }
        int result = 0;
        int p = bigHead;
        for (int i = 0; i < nbytes; i++) {
            result = result << 8;
            result = result | (bytes[p] & 0xff);
            p += step;
        }
        return result;
    }

    private long bytes2long(byte bytes[], ByteOrder order) {
        int nbytes = 8, bigHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            bigHead = 0;
            step = 1;
        } else {
            bigHead = nbytes - 1;
            step = -1;
        }
        long result = 0;
        int p = bigHead;
        for (int i = 0; i < nbytes; i++) {
            result = result << 8;
            result = result | (bytes[p] & 0xff);
            p += step;
        }
        return result;
    }

    private short bytes2short(byte bytes[], ByteOrder order) {
        int nbytes = 2, bigHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            bigHead = 0;
            step = 1;
        } else {
            bigHead = nbytes - 1;
            step = -1;
        }
        short result = 0;
        int p = bigHead;
        for (int i = 0; i < nbytes; i++) {
            result = (short) (result << 8);
            result = (short) (result | (bytes[p] & 0xff));
            p += step;
        }
        return result;
    }

    private char bytes2char(byte bytes[], ByteOrder order) {
        return (char) bytes2short(bytes, order);
    }

    private float bytes2float(byte bytes[], ByteOrder order) {
        return Float.intBitsToFloat(bytes2int(bytes, order));
    }

    private double bytes2double(byte bytes[], ByteOrder order) {
        return Double.longBitsToDouble(bytes2long(bytes, order));
    }

    private byte[] int2bytes(int value, ByteOrder order) {
        int nbytes = 4, smallHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            smallHead = nbytes - 1;
            step = -1;
        } else {
            smallHead = 0;
            step = 1;
        }
        byte bytes[] = new byte[nbytes];
        int p = smallHead;
        for (int i = 0; i < nbytes; i++) {
            bytes[p] = (byte) (value & 0xff);
            value = value >> 8;
            p += step;
        }
        return bytes;
    }

    private byte[] long2bytes(long value, ByteOrder order) {
        int nbytes = 8, smallHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            smallHead = nbytes - 1;
            step = -1;
        } else {
            smallHead = 0;
            step = 1;
        }
        byte bytes[] = new byte[nbytes];
        int p = smallHead;
        for (int i = 0; i < nbytes; i++) {
            bytes[p] = (byte) (value & 0xff);
            value = value >> 8;
            p += step;
        }
        return bytes;
    }

    private byte[] short2bytes(short value, ByteOrder order) {
        int nbytes = 2, smallHead, step;
        if (order == ByteOrder.BIG_ENDIAN) {
            smallHead = nbytes - 1;
            step = -1;
        } else {
            smallHead = 0;
            step = 1;
        }
        byte bytes[] = new byte[nbytes];
        int p = smallHead;
        for (int i = 0; i < nbytes; i++) {
            bytes[p] = (byte) (value & 0xff);
            value = (short) (value >> 8);
            p += step;
        }
        return bytes;
    }

    private byte[] char2bytes(char value, ByteOrder order) {
        return short2bytes((short) value, order);
    }

    private byte[] float2bytes(float value, ByteOrder order) {
        return int2bytes(Float.floatToRawIntBits(value), order);
    }

    private byte[] double2bytes(double value, ByteOrder order) {
        return long2bytes(Double.doubleToRawLongBits(value), order);
    }
}
