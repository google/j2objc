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

import java.nio.Buffer;
import java.nio.InvalidMarkException;

import junit.framework.TestCase;

/**
 * Test a java.nio.Buffer instance.
 */
public class BufferTest extends TestCase {

    public static void testBufferInstance(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        testCapacity(buf);
        testClear(buf);
        testFlip(buf);
        testHasRemaining(buf);
        testIsReadOnly(buf);
        testLimit(buf);
        testLimitint(buf);
        testMark(buf);
        testPosition(buf);
        testPositionint(buf);
        testRemaining(buf);
        testReset(buf);
        testRewind(buf);
        
        // check state, should not change
        assertEquals(buf.position(), oldPosition);
        assertEquals(buf.limit(), oldLimit);
    }

    public static void testCapacity(Buffer buf) {
        assertTrue(0 <= buf.position() && buf.position() <= buf.limit()
                && buf.limit() <= buf.capacity());
    }

    public static void testClear(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        Buffer ret = buf.clear();
        assertSame(ret, buf);
        assertEquals(buf.position(), 0);
        assertEquals(buf.limit(), buf.capacity());
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testFlip(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        Buffer ret = buf.flip();
        assertSame(ret, buf);
        assertEquals(buf.position(), 0);
        assertEquals(buf.limit(), oldPosition);
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testHasRemaining(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        assertEquals(buf.hasRemaining(), buf.position() < buf.limit());
        buf.position(buf.limit());
        assertFalse(buf.hasRemaining());

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testIsReadOnly(Buffer buf) {
        buf.isReadOnly();
    }

    /*
     * Class under test for int limit()
     */
    public static void testLimit(Buffer buf) {
        assertTrue(0 <= buf.position() && buf.position() <= buf.limit()
                && buf.limit() <= buf.capacity());
    }

    /*
     * Class under test for Buffer limit(int)
     */
    public static void testLimitint(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();
        
        Buffer ret = buf.limit(buf.limit());
        assertSame(ret, buf);

        buf.mark();
        buf.limit(buf.capacity());
        assertEquals(buf.limit(), buf.capacity());
        // position should not change
        assertEquals(buf.position(), oldPosition);
        // mark should be valid
        buf.reset();

        if (buf.capacity() > 0) {
            buf.limit(buf.capacity());
            buf.position(buf.capacity());
            buf.mark();
            buf.limit(buf.capacity() - 1);
            // position should be the new limit
            assertEquals(buf.position(), buf.limit());
            // mark should be invalid
            try {
                buf.reset();
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (InvalidMarkException e) {
                // expected
            }
        }
        
        try {
            buf.limit(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            buf.limit(buf.capacity() + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testMark(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        Buffer ret = buf.mark();
        assertSame(ret, buf);

        buf.mark();
        buf.position(buf.limit());
        buf.reset();
        assertEquals(buf.position(), oldPosition);

        buf.mark();
        buf.position(buf.limit());
        buf.reset();
        assertEquals(buf.position(), oldPosition);

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    /*
     * Class under test for int position()
     */
    public static void testPosition(Buffer buf) {
        assertTrue(0 <= buf.position() && buf.position() <= buf.limit()
                && buf.limit() <= buf.capacity());
    }

    /*
     * Class under test for Buffer position(int)
     */
    public static void testPositionint(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();
        
        try {
            buf.position(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            buf.position(buf.limit() + 1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        buf.mark();
        buf.position(buf.position());
        buf.reset();
        assertEquals(buf.position(), oldPosition);
        
        buf.position(0);
        assertEquals(buf.position(), 0);
        buf.position(buf.limit());
        assertEquals(buf.position(), buf.limit());
        
        if (buf.capacity() > 0) {
            buf.limit(buf.capacity());
            buf.position(buf.limit());
            buf.mark();
            buf.position(buf.limit() - 1);
            assertEquals(buf.position(), buf.limit() - 1);
            // mark should be invalid
            try {
                buf.reset();
                fail("Should throw Exception"); //$NON-NLS-1$
            } catch (InvalidMarkException e) {
                // expected
            }
        }
        
        Buffer ret = buf.position(0);
        assertSame(ret, buf);

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testRemaining(Buffer buf) {
        assertEquals(buf.remaining(), buf.limit() - buf.position());
    }

    public static void testReset(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();

        buf.mark();
        buf.position(buf.limit());
        buf.reset();
        assertEquals(buf.position(), oldPosition);

        buf.mark();
        buf.position(buf.limit());
        buf.reset();
        assertEquals(buf.position(), oldPosition);
        
        Buffer ret = buf.reset();
        assertSame(ret, buf);

        buf.clear();
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }

    public static void testRewind(Buffer buf) {
        // save state
        int oldPosition = buf.position();
        int oldLimit = buf.limit();
        
        Buffer ret = buf.rewind();
        assertEquals(buf.position(), 0);
        assertSame(ret, buf);
        try {
            buf.reset();
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (InvalidMarkException e) {
            // expected
        }

        // restore state
        buf.limit(oldLimit);
        buf.position(oldPosition);
    }
    
    public void testNothing() {
        // to remove JUnit warning
    }
    
}