/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.luni.tests.java.util;

import java.util.UUID;

import junit.framework.TestCase;

public class UUIDTest extends TestCase {

    /**
     * @see UUID#UUID(long, long)
     */
    public void test_ConstructorJJ() {
        UUID uuid = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L);
        assertEquals(2, uuid.variant());
        assertEquals(1, uuid.version());
        assertEquals(0x1d07decf81d4faeL, uuid.timestamp());
        assertEquals(130742845922168750L, uuid.timestamp());
        assertEquals(0x2765, uuid.clockSequence());
        assertEquals(0xA0C91E6BF6L, uuid.node());
    }

    /**
     * @see UUID#getLeastSignificantBits()
     */
    public void test_getLeastSignificantBits() {
        UUID uuid = new UUID(0, 0);
        assertEquals(0, uuid.getLeastSignificantBits());
        uuid = new UUID(0, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, uuid.getLeastSignificantBits());
        uuid = new UUID(0, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, uuid.getLeastSignificantBits());
    }

    /**
     * @see UUID#getMostSignificantBits()
     */
    public void test_getMostSignificantBits() {
        UUID uuid = new UUID(0, 0);
        assertEquals(0, uuid.getMostSignificantBits());
        uuid = new UUID(Long.MIN_VALUE, 0);
        assertEquals(Long.MIN_VALUE, uuid.getMostSignificantBits());
        uuid = new UUID(Long.MAX_VALUE, 0);
        assertEquals(Long.MAX_VALUE, uuid.getMostSignificantBits());
    }

    /**
     * @see UUID#version()
     */
    public void test_version() {
        UUID uuid = new UUID(0, 0);
        assertEquals(0, uuid.version());
        uuid = new UUID(0x0000000000001000L, 0);
        assertEquals(1, uuid.version());
        uuid = new UUID(0x0000000000002000L, 0);
        assertEquals(2, uuid.version());
        uuid = new UUID(0x0000000000003000L, 0);
        assertEquals(3, uuid.version());
        uuid = new UUID(0x0000000000004000L, 0);
        assertEquals(4, uuid.version());
        uuid = new UUID(0x0000000000005000L, 0);
        assertEquals(5, uuid.version());
    }

    /**
     * @see UUID#variant()
     */
    public void test_variant() {
        UUID uuid = new UUID(0, 0x0000000000000000L);
        assertEquals(0, uuid.variant());
        uuid = new UUID(0, 0x7000000000000000L);
        assertEquals(0, uuid.variant());
        uuid = new UUID(0, 0x3000000000000000L);
        assertEquals(0, uuid.variant());
        uuid = new UUID(0, 0x1000000000000000L);
        assertEquals(0, uuid.variant());

        uuid = new UUID(0, 0x8000000000000000L);
        assertEquals(2, uuid.variant());
        uuid = new UUID(0, 0xB000000000000000L);
        assertEquals(2, uuid.variant());
        uuid = new UUID(0, 0xA000000000000000L);
        assertEquals(2, uuid.variant());
        uuid = new UUID(0, 0x9000000000000000L);
        assertEquals(2, uuid.variant());

        uuid = new UUID(0, 0xC000000000000000L);
        assertEquals(6, uuid.variant());
        uuid = new UUID(0, 0xD000000000000000L);
        assertEquals(6, uuid.variant());

        uuid = new UUID(0, 0xE000000000000000L);
        assertEquals(7, uuid.variant());
        uuid = new UUID(0, 0xF000000000000000L);
        assertEquals(7, uuid.variant());
    }

    /**
     * @see UUID#timestamp()
     */
    public void test_timestamp() {
        UUID uuid = new UUID(0x0000000000001000L, 0x8000000000000000L);
        assertEquals(0x0, uuid.timestamp());

        uuid = new UUID(0x7777777755551333L, 0x8000000000000000L);
        assertEquals(0x333555577777777L, uuid.timestamp());

        uuid = new UUID(0x0000000000000000L, 0x8000000000000000L);
        try {
            uuid.timestamp();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}

        uuid = new UUID(0x0000000000002000L, 0x8000000000000000L);
        try {
            uuid.timestamp();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}
    }

    /**
     * @see UUID#clockSequence()
     */
    public void test_clockSequence() {
        UUID uuid = new UUID(0x0000000000001000L, 0x8000000000000000L);
        assertEquals(0x0, uuid.clockSequence());

        uuid = new UUID(0x0000000000001000L, 0x8FFF000000000000L);
        assertEquals(0x0FFF, uuid.clockSequence());

        uuid = new UUID(0x0000000000001000L, 0xBFFF000000000000L);
        assertEquals(0x3FFF, uuid.clockSequence());

        uuid = new UUID(0x0000000000000000L, 0x8000000000000000L);
        try {
            uuid.clockSequence();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}

        uuid = new UUID(0x0000000000002000L, 0x8000000000000000L);
        try {
            uuid.clockSequence();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}
    }

    /**
     * @see UUID#node()
     */
    public void test_node() {
        UUID uuid = new UUID(0x0000000000001000L, 0x8000000000000000L);
        assertEquals(0x0, uuid.node());

        uuid = new UUID(0x0000000000001000L, 0x8000FFFFFFFFFFFFL);
        assertEquals(0xFFFFFFFFFFFFL, uuid.node());

        uuid = new UUID(0x0000000000000000L, 0x8000000000000000L);
        try {
            uuid.node();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}

        uuid = new UUID(0x0000000000002000L, 0x8000000000000000L);
        try {
            uuid.node();
            fail("No UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}
    }

    /**
     * @see UUID#compareTo(UUID)
     */
    public void test_compareTo() {
        UUID uuid1 = new UUID(0, 0);
        assertEquals(0, uuid1.compareTo(uuid1));
        UUID uuid2 = new UUID(1, 0);
        assertEquals(-1, uuid1.compareTo(uuid2));
        assertEquals(1, uuid2.compareTo(uuid1));

        uuid2 = new UUID(0, 1);
        assertEquals(-1, uuid1.compareTo(uuid2));
        assertEquals(1, uuid2.compareTo(uuid1));
    }

    /**
     * @see UUID#hashCode()
     */
    public void test_hashCode() {
        UUID uuid = new UUID(0, 0);
        assertEquals(0, uuid.hashCode());
        uuid = new UUID(123, 123);
        UUID uuidClone = new UUID(123, 123);
        assertEquals(uuid.hashCode(), uuidClone.hashCode());
    }

    /**
     * @see UUID#equals(Object)
     */
    public void test_equalsObject() {
        UUID uuid1 = new UUID(0, 0);
        assertEquals(uuid1, uuid1);
        assertFalse(uuid1.equals(null));
        assertFalse(uuid1.equals("NOT A UUID"));
        UUID uuid2 = new UUID(0, 0);
        assertEquals(uuid1, uuid2);
        assertEquals(uuid2, uuid1);

        uuid1 = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L);
        uuid2 = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L);
        assertEquals(uuid1, uuid2);
        assertEquals(uuid2, uuid1);

        uuid2 = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf7L);
        assertFalse(uuid1.equals(uuid2));
        assertFalse(uuid2.equals(uuid1));
    }

    /**
     * @see UUID#toString()
     */
    public void test_toString() {
        UUID uuid = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L);
        String actual = uuid.toString();
        assertEquals("f81d4fae-7dec-11d0-a765-00a0c91e6bf6", actual);

        uuid = new UUID(0x0000000000001000L, 0x8000000000000000L);
        actual = uuid.toString();
        assertEquals("00000000-0000-1000-8000-000000000000", actual);
    }

    /**
     * @see UUID#randomUUID()
     */
    public void test_randomUUID() {
        UUID uuid = UUID.randomUUID();
        assertEquals(2, uuid.variant());
        assertEquals(4, uuid.version());
    }

    /**
     * @see UUID#nameUUIDFromBytes(byte[])
     */
    public void test_nameUUIDFromBytes() throws Exception {
        byte[] name = { (byte) 0x6b, (byte) 0xa7, (byte) 0xb8, (byte) 0x11,
                (byte) 0x9d, (byte) 0xad, (byte) 0x11, (byte) 0xd1,
                (byte) 0x80, (byte) 0xb4, (byte) 0x00, (byte) 0xc0,
                (byte) 0x4f, (byte) 0xd4, (byte) 0x30, (byte) 0xc8 };

        UUID uuid = UUID.nameUUIDFromBytes(name);

        assertEquals(2, uuid.variant());
        assertEquals(3, uuid.version());

        assertEquals(0xaff565bc2f771745L, uuid.getLeastSignificantBits());
        assertEquals(0x14cdb9b4de013faaL, uuid.getMostSignificantBits());

        uuid = UUID.nameUUIDFromBytes(new byte[0]);
        assertEquals(2, uuid.variant());
        assertEquals(3, uuid.version());

        assertEquals(0xa9800998ecf8427eL, uuid.getLeastSignificantBits());
        assertEquals(0xd41d8cd98f003204L, uuid.getMostSignificantBits());

        try {
            UUID.nameUUIDFromBytes(null);
            fail("No NPE");
        } catch (NullPointerException e) {}
    }

    /**
     * @see UUID#fromString(String)
     */
    public void test_fromString() {
        UUID actual = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        UUID expected = new UUID(0xf81d4fae7dec11d0L, 0xa76500a0c91e6bf6L);
        assertEquals(expected, actual);

        assertEquals(2, actual.variant());
        assertEquals(1, actual.version());
        assertEquals(130742845922168750L, actual.timestamp());
        assertEquals(10085, actual.clockSequence());
        assertEquals(690568981494L, actual.node());

        actual = UUID.fromString("00000000-0000-1000-8000-000000000000");
        expected = new UUID(0x0000000000001000L, 0x8000000000000000L);
        assertEquals(expected, actual);

        assertEquals(2, actual.variant());
        assertEquals(1, actual.version());
        assertEquals(0L, actual.timestamp());
        assertEquals(0, actual.clockSequence());
        assertEquals(0L, actual.node());

        try {
            UUID.fromString(null);
            fail("No NPE");
        } catch (NullPointerException e) {}

        try {
            UUID.fromString("");
            fail("No IAE");
        } catch (IllegalArgumentException e) {}

        try {
            UUID.fromString("f81d4fae_7dec-11d0-a765-00a0c91e6bf6");
            fail("No IAE");
        } catch (IllegalArgumentException e) {}

        try {
            UUID.fromString("f81d4fae-7dec_11d0-a765-00a0c91e6bf6");
            fail("No IAE");
        } catch (IllegalArgumentException e) {}

        try {
            UUID.fromString("f81d4fae-7dec-11d0_a765-00a0c91e6bf6");
            fail("No IAE");
        } catch (IllegalArgumentException e) {}

        try {
            UUID.fromString("f81d4fae-7dec-11d0-a765_00a0c91e6bf6");
            fail("No IAE");
        } catch (IllegalArgumentException e) {}
    }

	/**
	 * @tests java.util.UUID#fromString(String)
	 */
	public void test_fromString_LString_Exception() {

		UUID uuid = UUID.fromString("0-0-0-0-0");

		try {
			uuid = UUID.fromString("0-0-0-0-");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("-0-0-0-0-0");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("-0-0-0-0");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("-0-0-0-");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("0--0-0-0");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("0-0-0-0-");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("-1-0-0-0-0");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		uuid = UUID.fromString("123456789-0-0-0-0");
		assertEquals(0x2345678900000000L, uuid.getMostSignificantBits());
		assertEquals(0x0L, uuid.getLeastSignificantBits());

		uuid = UUID.fromString("111123456789-0-0-0-0");
		assertEquals(0x2345678900000000L, uuid.getMostSignificantBits());
		assertEquals(0x0L, uuid.getLeastSignificantBits());

		uuid = UUID.fromString("7fffffffffffffff-0-0-0-0");
		assertEquals(0xffffffff00000000L, uuid.getMostSignificantBits());
		assertEquals(0x0L, uuid.getLeastSignificantBits());

		try {
			uuid = UUID.fromString("8000000000000000-0-0-0-0");
			fail("should throw NumberFormatException");
		} catch (NumberFormatException e) {
			// expected
		}

		uuid = UUID
				.fromString("7fffffffffffffff-7fffffffffffffff-7fffffffffffffff-0-0");
		assertEquals(0xffffffffffffffffL, uuid.getMostSignificantBits());
		assertEquals(0x0L, uuid.getLeastSignificantBits());

		uuid = UUID.fromString("0-0-0-7fffffffffffffff-7fffffffffffffff");
		assertEquals(0x0L, uuid.getMostSignificantBits());
		assertEquals(0xffffffffffffffffL, uuid.getLeastSignificantBits());

		try {
			uuid = UUID.fromString("0-0-0-8000000000000000-0");
			fail("should throw NumberFormatException");
		} catch (NumberFormatException e) {
			// expected
		}

		try {
			uuid = UUID.fromString("0-0-0-0-8000000000000000");
			fail("should throw NumberFormatException");
		} catch (NumberFormatException e) {
			// expected
		}
	}
}
