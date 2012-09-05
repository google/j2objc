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

import java.io.IOException;
import java.io.StringReader;

public class StringReaderTest extends junit.framework.TestCase {

	String testString = "This is a test string";

	StringReader sr;

	/**
	 * @tests java.io.StringReader#StringReader(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.StringReader(java.lang.String)
		assertTrue("Used in tests", true);
	}

	/**
	 * @tests java.io.StringReader#close()
	 */
	public void test_close() throws Exception {
		// Test for method void java.io.StringReader.close()
		try {
			sr = new StringReader(testString);
			sr.close();
			char[] buf = new char[10];
			sr.read(buf, 0, 2);
			fail("Close failed");
		} catch (java.io.IOException e) {
			return;
		}
	}

	/**
	 * @tests java.io.StringReader#mark(int)
	 */
	public void test_markI() throws Exception {
		// Test for method void java.io.StringReader.mark(int)
                sr = new StringReader(testString);
                sr.skip(5);
                sr.mark(0);
                sr.skip(5);
                sr.reset();
                char[] buf = new char[10];
                sr.read(buf, 0, 2);
                assertTrue("Failed to return to mark", new String(buf, 0, 2)
                                .equals(testString.substring(5, 7)));
	}

	/**
	 * @tests java.io.StringReader#markSupported()
	 */
	public void test_markSupported() {
		// Test for method boolean java.io.StringReader.markSupported()

		sr = new StringReader(testString);
		assertTrue("markSupported returned false", sr.markSupported());
	}

	/**
	 * @tests java.io.StringReader#read()
	 */
	public void test_read() throws Exception {
		// Test for method int java.io.StringReader.read()
                sr = new StringReader(testString);
                int r = sr.read();
                assertEquals("Failed to read char", 'T', r);
                sr = new StringReader(new String(new char[] { '\u8765' }));
                assertTrue("Wrong double byte char", sr.read() == '\u8765');
	}

	/**
	 * @tests java.io.StringReader#read(char[], int, int)
	 */
	public void test_read$CII() throws Exception {
		// Test for method int java.io.StringReader.read(char [], int, int)
                sr = new StringReader(testString);
                char[] buf = new char[testString.length()];
                int r = sr.read(buf, 0, testString.length());
                assertTrue("Failed to read chars", r == testString.length());
                assertTrue("Read chars incorrectly", new String(buf, 0, r)
                                .equals(testString));
	}

	/**
	 * @tests java.io.StringReader#ready()
	 */
	public void test_ready() throws Exception {
		// Test for method boolean java.io.StringReader.ready()
                sr = new StringReader(testString);
                assertTrue("Steam not ready", sr.ready());
                sr.close();
                int r = 0;
                try {
                        sr.ready();
                } catch (IOException e) {
                        r = 1;
                }
                assertEquals("Expected IOException not thrown in read()", 1, r);
	}

	/**
	 * @tests java.io.StringReader#reset()
	 */
	public void test_reset() throws Exception {
		// Test for method void java.io.StringReader.reset()
                sr = new StringReader(testString);
                sr.skip(5);
                sr.mark(0);
                sr.skip(5);
                sr.reset();
                char[] buf = new char[10];
                sr.read(buf, 0, 2);
                assertTrue("Failed to reset properly", new String(buf, 0, 2)
                                .equals(testString.substring(5, 7)));
	}

	/**
	 * @tests java.io.StringReader#skip(long)
	 */
	public void test_skipJ() throws Exception {
		// Test for method long java.io.StringReader.skip(long)
                sr = new StringReader(testString);
                sr.skip(5);
                char[] buf = new char[10];
                sr.read(buf, 0, 2);
                assertTrue("Failed to skip properly", new String(buf, 0, 2)
                                .equals(testString.substring(5, 7)));
	}
}
