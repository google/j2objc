/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.logging.tests.java.util.logging;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import junit.framework.TestCase;

public class FormatterTest extends TestCase {
	Formatter f;

	LogRecord r;

	static String MSG = "msg, pls. ignore it";

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		f = new MockFormatter();
		r = new LogRecord(Level.FINE, MSG);
	}

	public void testFormat() {
		assertEquals("format", f.format(r));
	}

	public void testGetHead() {
		assertEquals("", f.getHead(null));
	}

	public void testGetTail() {
		assertEquals("", f.getTail(null));
	}

	public void testFormatMessage() {
		assertEquals(MSG, f.formatMessage(r));

		String pattern = "test formatter {0, number}";
		r.setMessage(pattern);
		assertEquals(pattern, f.formatMessage(r));

		Object[] oa = new Object[0];
		r.setParameters(oa);
		assertEquals(pattern, f.formatMessage(r));

		oa = new Object[] { new Integer(100), new Float(1.1) };
		r.setParameters(oa);
		assertEquals(MessageFormat.format(pattern, oa), f.formatMessage(r));

		r.setMessage(MSG);
		assertEquals(MSG, f.formatMessage(r));

		pattern = "wrong pattern {0, asdfasfd}";
		r.setMessage(pattern);
		assertEquals(pattern, f.formatMessage(r));

		pattern = "pattern without 0 {1, number}";
		r.setMessage(pattern);
		assertEquals(pattern, f.formatMessage(r));

		pattern = null;
		r.setMessage(pattern);
		assertNull(f.formatMessage(r));
	}

	public void testLocalizedFormatMessage() {
		// normal case
		r.setMessage("msg");
		ResourceBundle rb = ResourceBundle
				.getBundle("bundles/java/util/logging/res");
		r.setResourceBundle(rb);
		assertEquals(rb.getString("msg"), f.formatMessage(r));

		// local message is a pattern
		r.setMessage("pattern");
		Object[] oa = new Object[] { new Integer(3) };
		r.setParameters(oa);
		assertEquals(MessageFormat.format(rb.getString("pattern"), oa), f
				.formatMessage(r));

		// key is a pattern, but local message is not
		r.setMessage("pattern{0,number}");
		oa = new Object[] { new Integer(3) };
		r.setParameters(oa);
		assertEquals(rb.getString("pattern{0,number}"), f.formatMessage(r));

		// another bundle
		rb = ResourceBundle.getBundle("bundles/java/util/logging/res",
				Locale.US);
		r.setMessage("msg");
		r.setResourceBundle(rb);
		assertEquals(rb.getString("msg"), f.formatMessage(r));

		// cannot find local message in bundle
		r.setMessage("msg without locale");
		assertEquals("msg without locale", f.formatMessage(r));

		// set bundle name but not bundle
		r.setResourceBundle(null);
		r.setResourceBundleName("bundles/java/util/logging/res");
		r.setMessage("msg");
		assertEquals("msg", f.formatMessage(r));
	}

	public static class MockFormatter extends Formatter {

		public String format(LogRecord arg0) {
			return "format";
		}

	}

}
