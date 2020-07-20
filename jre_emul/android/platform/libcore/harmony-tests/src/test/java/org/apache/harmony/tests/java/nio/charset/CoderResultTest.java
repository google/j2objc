/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import junit.framework.TestCase;

/**
 * Test class java.nio.charset.CoderResult.
 */
public class CoderResultTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test the constant OVERFLOW and UNDERFLOW.
	 */
	public void testConstants() throws Exception {
		assertNotSame(CoderResult.OVERFLOW, CoderResult.UNDERFLOW);

		assertNotNull(CoderResult.OVERFLOW);
		assertFalse(CoderResult.OVERFLOW.isError());
		assertFalse(CoderResult.OVERFLOW.isMalformed());
		assertFalse(CoderResult.OVERFLOW.isUnderflow());
		assertFalse(CoderResult.OVERFLOW.isUnmappable());
		assertTrue(CoderResult.OVERFLOW.isOverflow());
		assertTrue(CoderResult.OVERFLOW.toString().indexOf("OVERFLOW") != -1);
		try {
			CoderResult.OVERFLOW.throwException();
			fail("Should throw BufferOverflowException");
		} catch (BufferOverflowException ex) {
			// expected
		}
		try {
			CoderResult.OVERFLOW.length();
			fail("Should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException ex) {
			// expected
		}

		assertNotNull(CoderResult.UNDERFLOW);
		assertFalse(CoderResult.UNDERFLOW.isError());
		assertFalse(CoderResult.UNDERFLOW.isMalformed());
		assertTrue(CoderResult.UNDERFLOW.isUnderflow());
		assertFalse(CoderResult.UNDERFLOW.isUnmappable());
		assertFalse(CoderResult.UNDERFLOW.isOverflow());
		assertTrue(CoderResult.UNDERFLOW.toString().indexOf("UNDERFLOW") != -1);
		try {
			CoderResult.UNDERFLOW.throwException();
			fail("Should throw BufferOverflowException");
		} catch (BufferUnderflowException ex) {
			// expected
		}
		try {
			CoderResult.UNDERFLOW.length();
			fail("Should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException ex) {
			// expected
		}
	}

	/**
	 * Test method isError().
	 * 
	 */
	public void testIsError() {
		assertFalse(CoderResult.UNDERFLOW.isError());
		assertFalse(CoderResult.OVERFLOW.isError());
		assertTrue(CoderResult.malformedForLength(1).isError());
		assertTrue(CoderResult.unmappableForLength(1).isError());
	}

	/**
	 * Test method isMalformed().
	 * 
	 */
	public void testIsMalformed() {
		assertFalse(CoderResult.UNDERFLOW.isMalformed());
		assertFalse(CoderResult.OVERFLOW.isMalformed());
		assertTrue(CoderResult.malformedForLength(1).isMalformed());
		assertFalse(CoderResult.unmappableForLength(1).isMalformed());
	}

	/**
	 * Test method isMalformed().
	 * 
	 */
	public void testIsUnmappable() {
		assertFalse(CoderResult.UNDERFLOW.isUnmappable());
		assertFalse(CoderResult.OVERFLOW.isUnmappable());
		assertFalse(CoderResult.malformedForLength(1).isUnmappable());
		assertTrue(CoderResult.unmappableForLength(1).isUnmappable());
	}

	/**
	 * Test method isOverflow().
	 * 
	 */
	public void testIsOverflow() {
		assertFalse(CoderResult.UNDERFLOW.isOverflow());
		assertTrue(CoderResult.OVERFLOW.isOverflow());
		assertFalse(CoderResult.malformedForLength(1).isOverflow());
		assertFalse(CoderResult.unmappableForLength(1).isOverflow());
	}

	/**
	 * Test method isUnderflow().
	 * 
	 */
	public void testIsUnderflow() {
		assertTrue(CoderResult.UNDERFLOW.isUnderflow());
		assertFalse(CoderResult.OVERFLOW.isUnderflow());
		assertFalse(CoderResult.malformedForLength(1).isUnderflow());
		assertFalse(CoderResult.unmappableForLength(1).isUnderflow());
	}

	/**
	 * Test method length().
	 * 
	 */
	public void testLength() {
		try {
			CoderResult.UNDERFLOW.length();
			fail("Should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException ex) {
			// expected
		}
		try {
			CoderResult.OVERFLOW.length();
			fail("Should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException ex) {
			// expected
		}

		assertEquals(CoderResult.malformedForLength(1).length(), 1);
		assertEquals(CoderResult.unmappableForLength(1).length(), 1);
	}

	/**
	 * Test method malformedForLength(int).
	 * 
	 */
	public void testMalformedForLength() {
		assertNotNull(CoderResult.malformedForLength(Integer.MAX_VALUE));
		assertNotNull(CoderResult.malformedForLength(1));
		assertSame(CoderResult.malformedForLength(1), CoderResult
				.malformedForLength(1));
		assertNotSame(CoderResult.malformedForLength(1), CoderResult
				.unmappableForLength(1));
		assertNotSame(CoderResult.malformedForLength(2), CoderResult
				.malformedForLength(1));
		try {
			CoderResult.malformedForLength(-1);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		try {
			CoderResult.malformedForLength(0);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	/**
	 * Test method unmappableForLength(int).
	 * 
	 */
	public void testUnmappableForLength() {
		assertNotNull(CoderResult.unmappableForLength(Integer.MAX_VALUE));
		assertNotNull(CoderResult.unmappableForLength(1));
		assertSame(CoderResult.unmappableForLength(1), CoderResult
				.unmappableForLength(1));
		assertNotSame(CoderResult.unmappableForLength(2), CoderResult
				.unmappableForLength(1));
		try {
			CoderResult.unmappableForLength(-1);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		try {
			CoderResult.unmappableForLength(0);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	/**
	 * Test method throwException().
	 * 
	 */
	public void testThrowException() throws Exception {
		try {
			CoderResult.OVERFLOW.throwException();
			fail("Should throw BufferOverflowException");
		} catch (BufferOverflowException ex) {
			// expected
		}
		try {
			CoderResult.UNDERFLOW.throwException();
			fail("Should throw BufferOverflowException");
		} catch (BufferUnderflowException ex) {
			// expected
		}
		try {
			CoderResult.malformedForLength(1).throwException();
			fail("Should throw MalformedInputException");
		} catch (MalformedInputException ex) {
			assertEquals(ex.getInputLength(), 1);
		}
		try {
			CoderResult.unmappableForLength(1).throwException();
			fail("Should throw UnmappableCharacterException");
		} catch (UnmappableCharacterException ex) {
			assertEquals(ex.getInputLength(), 1);
		}
	}

	/**
	 * Test method toString().
	 * 
	 */
	public void testToString() throws Exception {
		assertTrue(CoderResult.OVERFLOW.toString().indexOf("OVERFLOW") != -1);
		assertTrue(CoderResult.UNDERFLOW.toString().indexOf("UNDERFLOW") != -1);
		assertTrue(CoderResult.malformedForLength(666).toString()
				.indexOf("666") != -1);
		assertTrue(CoderResult.unmappableForLength(666).toString().indexOf(
				"666") != -1);
	}
}
