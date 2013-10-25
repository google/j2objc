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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

public class SequenceInputStreamTest extends junit.framework.TestCase {

	SequenceInputStream si;

	String s1 = "Hello";

	String s2 = "World";

	/**
	 * @tests java.io.SequenceInputStream#SequenceInputStream(java.io.InputStream,
	 *        java.io.InputStream)
	 */
	public void test_ConstructorLjava_io_InputStreamLjava_io_InputStream() {
		// Test for method java.io.SequenceInputStream(java.io.InputStream,
		// java.io.InputStream)
		// Used in tests
	}
	
	/**
	 * @tests SequenceInputStream#SequenceInputStream(java.io.InputStream,
	 *        java.io.InputStream)
	 */
	public void test_Constructor_LInputStreamLInputStream_Null() throws UnsupportedEncodingException {		
		try {
			si = new SequenceInputStream(null , null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			//expected
		}
		
		//will not throw NullPointerException if the first InputStream is not null
		InputStream is = new ByteArrayInputStream(s1.getBytes("UTF-8")); 
		si = new SequenceInputStream(is , null);
	}

	/**
	 * @tests java.io.SequenceInputStream#SequenceInputStream(java.util.Enumeration)
	 */
	@SuppressWarnings("unchecked")
    public void test_ConstructorLjava_util_Enumeration() {
		// Test for method java.io.SequenceInputStream(java.util.Enumeration)
		class StreamEnumerator implements Enumeration {
			InputStream streams[] = new InputStream[2];

			int count = 0;

			public StreamEnumerator() throws UnsupportedEncodingException {
				streams[0] = new ByteArrayInputStream(s1.getBytes("UTF-8"));
				streams[1] = new ByteArrayInputStream(s2.getBytes("UTF-8"));
			}

			public boolean hasMoreElements() {
				return count < streams.length;
			}

			public Object nextElement() {
				return streams[count++];
			}
		}

		try {
			si = new SequenceInputStream(new StreamEnumerator());
			byte buf[] = new byte[s1.length() + s2.length()];
			si.read(buf, 0, s1.length());
			si.read(buf, s1.length(), s2.length());
			assertTrue("Read incorrect bytes: " + new String(buf), new String(
					buf, "UTF-8").equals(s1 + s2));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}

	}

	/**
	 * @tests java.io.SequenceInputStream#available()
	 */
	public void test_available() {
		// Test for method int java.io.SequenceInputStream.available()
		try {

			assertTrue("Returned incorrect number of bytes: " + si.available(),
					si.available() == s1.length());
		} catch (IOException e) {
			fail("IOException during available test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.SequenceInputStream#close()
	 */
	public void test_close() throws IOException {
		si.close();		
		//will not throw IOException to close a stream which is closed already
		si.close();
	}

	/**
	 * @tests java.io.SequenceInputStream#read()
	 */
	public void test_read() throws IOException {
		// Test for method int java.io.SequenceInputStream.read()
		try {
			si.read();
			assertTrue("Read incorrect char", (char) si.read() == s1.charAt(1));
		} catch (IOException e) {
			fail("IOException during read test: " + e.getMessage());
		}
		
		//returns -1 if the stream is closed , do not throw IOException
		si.close();
		int result = si.read();
		assertEquals(-1 , result);		
	}

	/**
	 * @tests java.io.SequenceInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() throws IOException {
		// Test for method int java.io.SequenceInputStream.read(byte [], int,
		// int)
		try {
			byte buf[] = new byte[s1.length() + s2.length()];
			si.read(buf, 0, s1.length());
			si.read(buf, s1.length(), s2.length());
			assertTrue("Read incorrect bytes: " + new String(buf), new String(
					buf, "UTF-8").equals(s1 + s2));
		} catch (IOException e) {
			fail("IOException during read test : " + e.getMessage());
		}
		
		ByteArrayInputStream bis1 = new ByteArrayInputStream(
				new byte[] { 1, 2, 3, 4 });
		ByteArrayInputStream bis2 = new ByteArrayInputStream(
				new byte[] { 5, 6, 7, 8 });
		SequenceInputStream sis = new SequenceInputStream(bis1, bis2);

		try {
			sis.read(null, 0, -1);
			fail("Expected NullPointerException exception");
		} catch (NullPointerException e) {
			// expected
		}
		
        //returns -1 if the stream is closed , do not throw IOException
		byte[] array = new byte[] { 1 , 2 , 3 ,4 };
		sis.close();
		int result = sis.read(array , 0 , 5);
		assertEquals(-1 , result);	
		
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() throws UnsupportedEncodingException {
		si = new SequenceInputStream(new ByteArrayInputStream(s1.getBytes("UTF-8")),
				new ByteArrayInputStream(s2.getBytes("UTF-8")));
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
