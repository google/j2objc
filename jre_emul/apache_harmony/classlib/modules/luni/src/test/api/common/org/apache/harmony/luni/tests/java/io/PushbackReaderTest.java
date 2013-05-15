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

package org.apache.harmony.luni.tests.java.io;

import java.io.CharArrayReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;

public class PushbackReaderTest extends junit.framework.TestCase {

	PushbackReader pbr;

	String pbString = "Hello World";

	/**
	 * @tests java.io.PushbackReader#PushbackReader(java.io.Reader)
	 */
	public void test_ConstructorLjava_io_Reader() {
		// Test for method java.io.PushbackReader(java.io.Reader)
		try {
			pbr.close();
			pbr = new PushbackReader(new StringReader(pbString));
			char buf[] = new char[5];
			pbr.read(buf, 0, 5);
			pbr.unread(buf);
		} catch (IOException e) {
			// Correct
			return;
		}
		fail("Created reader with buffer larger than 1");
	}

	/**
	 * @tests java.io.PushbackReader#PushbackReader(java.io.Reader, int)
	 */
	public void test_ConstructorLjava_io_ReaderI() {
		// Test for method java.io.PushbackReader(java.io.Reader, int)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.io.PushbackReader#close()
	 */
	public void test_close() {
		// Test for method void java.io.PushbackReader.close()
		try {
			pbr.close();
			pbr.read();
		} catch (Exception e) {
			return;
		}
		fail("Failed to throw exception reading from closed reader");
	}

	/**
	 * @tests java.io.PushbackReader#mark(int)
	 */
	public void test_markI() {
		try {
			pbr.mark(3);
		} catch (IOException e) {
			// correct
			return;
		}
		fail("mark failed to throw expected IOException");
	}

	/**
	 * @tests java.io.PushbackReader#markSupported()
	 */
	public void test_markSupported() {
		// Test for method boolean java.io.PushbackReader.markSupported()
		assertTrue("markSupported returned true", !pbr.markSupported());
	}

	/**
	 * @tests java.io.PushbackReader#read()
	 */
	public void test_read() {
		// Test for method int java.io.PushbackReader.read()
		try {
			char c;
			pbr.read();
			c = (char) pbr.read();
			assertTrue("Failed to read char: " + c, c == pbString.charAt(1));
			Reader reader = new PushbackReader(new CharArrayReader(
					new char[] { '\u8765' }));
			assertTrue("Wrong double byte character", reader.read() == '\u8765');
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PushbackReader#read(char[], int, int)
	 */
	public void test_read$CII() {
		// Test for method int java.io.PushbackReader.read(char [], int, int)
		try {
			char[] c = new char[5];
			pbr.read(c, 0, 5);
			assertTrue("Failed to read chars", new String(c).equals(pbString
					.substring(0, 5)));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
	}
	
	/**
	 * @tests java.io.PushbackReader#read(char[], int, int)
	 */
	public void test_read_$CII_Exception() throws IOException {
		pbr = new PushbackReader(new StringReader(pbString), 10);
		
		char[] nullCharArray = null;
		char[] charArray = new char[10];
		
		try {
			pbr.read(nullCharArray, -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		
		try {
			pbr.read(nullCharArray, 1, 0);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
		
		try {
			pbr.read(charArray, -1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		pbr.read(charArray, 0, 0);
        pbr.read(charArray, 0, charArray.length);
		pbr.read(charArray, charArray.length, 0);
		
		try {
			pbr.read(charArray, charArray.length + 1, 0);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		try {
			pbr.read(charArray, charArray.length + 1, 1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		pbr.close();

		try {
			pbr.read(nullCharArray, -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}

		try {
			pbr.read(charArray, -1, -1);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}

	/**
	 * @tests java.io.PushbackReader#ready()
	 */
	public void test_ready() {
		// Test for method boolean java.io.PushbackReader.ready()
		try {
			char[] c = new char[11];
			if (c.length > 0) {
			    // use c to avoid warning msg
			}
			assertTrue("Ready stream returned false to ready()", pbr.ready());
		} catch (IOException e) {
			fail("IOException during ready() test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PushbackReader#reset()
	 */
	public void test_reset() {
		try {
			pbr.reset();
		} catch (IOException e) {
			// correct
			return;
		}
		fail("mark failed to throw expected IOException");
	}

	/**
	 * @tests java.io.PushbackReader#unread(char[])
	 */
	public void test_unread$C() {
		// Test for method void java.io.PushbackReader.unread(char [])
		try {
			char[] c = new char[5];
			pbr.read(c, 0, 5);
			pbr.unread(c);
			pbr.read(c, 0, 5);
			assertTrue("Failed to unread chars", new String(c).equals(pbString
					.substring(0, 5)));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PushbackReader#skip(long)
	 */
	public void test_skip$J() {
		char chars[] = new char[] { 'h', 'e', 'l', 'l', 'o' };
		for (int i = 0; i < 3; i++) {
			Reader reader, reader2;
			switch (i) {
			case 0:
				reader = new StringReader(new String(chars));
				reader2 = new StringReader(new String(chars));
				break;
			case 1:
				reader = new FilterReader(new StringReader(new String(chars))) {
				};
				reader2 = new FilterReader(new StringReader(new String(chars))) {
				};
				break;
			default:
				reader = new CharArrayReader(chars);
				reader2 = new CharArrayReader(chars);
			}
			PushbackReader pReader = new PushbackReader(reader, 2);
			PushbackReader pReader2 = new PushbackReader(reader2, 2);
			boolean skipped = false;
			long numSkipped = 0;
			try {
				numSkipped = pReader2.skip(3);
				pReader2.unread('a');
				pReader2.unread('b');
				numSkipped += pReader2.skip(10);
				numSkipped += pReader2.skip(10);
				numSkipped += pReader2.skip(10);
				numSkipped += pReader2.skip(10);
				numSkipped += pReader2.skip(10);
				numSkipped += pReader2.skip(10);
				assertEquals("Did not skip correct number of characters",
						7, numSkipped);
				numSkipped = 0;
				numSkipped += pReader.skip(2);
				pReader.unread('i');
				numSkipped += pReader.skip(2);
				numSkipped += pReader.skip(0);
				skipped = true;
				numSkipped += pReader.skip(-1);
				fail("Failed to throw "
						+ new IllegalArgumentException().getClass().getName());
			} catch (IllegalArgumentException e) {
				assertTrue("Failed to skip characters" + e, skipped);
			} catch (IOException e) {
				fail("Failed to skip characters" + e);
			}
			try {
				numSkipped += pReader.skip(1);
				numSkipped += pReader.skip(1);
				numSkipped += pReader.skip(1);
				assertEquals("Failed to skip all characters", 6, numSkipped);
				long nextSkipped = pReader.skip(1);
				assertEquals("skipped empty reader", 0, nextSkipped);
			} catch (IOException e) {
				fail("Failed to skip more characters" + e);
			}
		}
	}

	/**
	 * @tests java.io.PushbackReader#unread(char[], int, int)
	 */
	public void test_unread$CII() {
		// Test for method void java.io.PushbackReader.unread(char [], int, int)
		try {
			char[] c = new char[5];
			pbr.read(c, 0, 5);
			pbr.unread(c, 0, 2);
			pbr.read(c, 0, 5);
			assertTrue("Failed to unread chars", new String(c).equals(pbString
					.substring(0, 2)
					+ pbString.substring(5, 8)));
		} catch (IOException e) {
			fail("IOException during unread test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.PushbackReader#unread(char[], int, int)
	 */
	public void test_unread_$CII_NullPointerException() throws IOException {
		//a pushback reader with one character buffer
		pbr = new PushbackReader(new StringReader(pbString));
		
		try {
			pbr.unread(null, 0, 1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}
	
	/**
	 * @tests java.io.PushbackReader#unread(char[], int, int)
	 */
	public void test_unread_$CII_Exception_InsufficientBuffer() throws IOException {
		//a pushback reader with one character buffer
		pbr = new PushbackReader(new StringReader(pbString));
		
		//if count > buffer's size , should throw IOException
		try {
			pbr.unread(new char[pbString.length()], 0, 2);
			fail("should throw IOException");
		} catch (IOException e) {
			// expected
		}
	}
	
	/**
	 * @tests java.io.PushbackReader#unread(char[], int, int)
	 */
	public void test_unread_$CII_ArrayIndexOutOfBoundsException() throws IOException {
		//a pushback reader with one character buffer
		pbr = new PushbackReader(new StringReader(pbString));
		
		try {
			pbr.unread(new char[pbString.length()], -1 , -1);
			fail("should throw ArrayIndexOutOfBoundsException");
		} catch (ArrayIndexOutOfBoundsException e) {
			// expected
		}
	}
	
	/**
	 * @tests java.io.PushbackReader#unread(int)
	 */
	public void test_unreadI() {
		// Test for method void java.io.PushbackReader.unread(int)

		try {
			int c;
			pbr.read();
			c = pbr.read();
			pbr.unread(c);
			assertTrue("Failed to unread char", pbr.read() == c);
		} catch (IOException e) {
			fail("IOException during unread test : " + e.getMessage());
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		pbr = new PushbackReader(new StringReader(pbString), 10);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			pbr.close();
		} catch (IOException e) {
		}
	}
}
