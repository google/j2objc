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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.security.Permission;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.HandlerTest.NullOutputStream;
import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;
import tests.util.CallVerificationStack;

/**
 * Test the class StreamHandler.
 */
public class StreamHandlerTest extends TestCase {

	private final static String INVALID_LEVEL = "impossible_level";

    private final PrintStream err = System.err;

    private OutputStream errSubstituteStream = null;

	private static String className = StreamHandlerTest.class.getName();

	private static CharsetEncoder encoder;

	static {
		encoder = Charset.forName("iso-8859-1").newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		LogManager.getLogManager().reset();
		CallVerificationStack.getInstance().clear();
        System.setErr(err);
        super.tearDown();
	}

	/*
	 * Test the constructor with no parameter, and no relevant log manager
	 * properties are set.
	 */
	public void testConstructor_NoParameter_NoProperties() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));

		StreamHandler h = new StreamHandler();
		assertSame(Level.INFO, h.getLevel());
		assertTrue(h.getFormatter() instanceof SimpleFormatter);
		assertNull(h.getFilter());
		assertNull(h.getEncoding());
	}

	/*
	 * Test the constructor with insufficient privilege.
	 *
	public void testConstructor_NoParameter_InsufficientPrivilege() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));

		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		// set a normal value
		try {
			StreamHandler h = new StreamHandler();
			assertSame(Level.INFO, h.getLevel());
			assertTrue(h.getFormatter() instanceof SimpleFormatter);
			assertNull(h.getFilter());
			assertNull(h.getEncoding());
		} finally {
			System.setSecurityManager(oldMan);
		}
	}

	/*
	 * Test the constructor with no parameter, and valid relevant log manager
	 * properties are set.
	 */
	public void testConstructor_NoParameter_ValidProperties() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", "FINE");
		p.put("java.util.logging.StreamHandler.filter", className
				+ "$MockFilter");
		p.put("java.util.logging.StreamHandler.formatter", className
				+ "$MockFormatter");
		p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals("FINE", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		StreamHandler h = new StreamHandler();
		assertSame(h.getLevel(), Level.parse("FINE"));
		assertTrue(h.getFormatter() instanceof MockFormatter);
		assertTrue(h.getFilter() instanceof MockFilter);
		assertEquals("iso-8859-1", h.getEncoding());
	}

	/*
	 * Test the constructor with no parameter, and invalid relevant log manager
	 * properties are set.
	 */
	public void testConstructor_NoParameter_InvalidProperties()
			throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", INVALID_LEVEL);
		p.put("java.util.logging.StreamHandler.filter", className + "");
		p.put("java.util.logging.StreamHandler.formatter", className + "");
		p.put("java.util.logging.StreamHandler.encoding", "XXXX");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals(INVALID_LEVEL, LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("XXXX", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		StreamHandler h = new StreamHandler();
		assertSame(Level.INFO, h.getLevel());
		assertTrue(h.getFormatter() instanceof SimpleFormatter);
		assertNull(h.getFilter());
		assertNull(h.getEncoding());
		h.publish(new LogRecord(Level.SEVERE, "test"));
		assertTrue(CallVerificationStack.getInstance().empty());
		assertNull(h.getEncoding());
	}

	/*
	 * Test the constructor with normal parameter values, and no relevant log
	 * manager properties are set.
	 */
	public void testConstructor_HasParameters_NoProperties() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));

		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new MockFormatter2());
		assertSame(Level.INFO, h.getLevel());
		assertTrue(h.getFormatter() instanceof MockFormatter2);
		assertNull(h.getFilter());
		assertNull(h.getEncoding());
	}

	/*
	 * Test the constructor with insufficient privilege.
	 *
	public void testConstructor_HasParameter_InsufficientPrivilege() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));

		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		// set a normal value
		try {
			StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
					new MockFormatter2());
			assertSame(Level.INFO, h.getLevel());
			assertTrue(h.getFormatter() instanceof MockFormatter2);
			assertNull(h.getFilter());
			assertNull(h.getEncoding());
		} finally {
			System.setSecurityManager(oldMan);
		}
	}

	/*
	 * Test the constructor with normal parameter values, and valid relevant log
	 * manager properties are set.
	 */
	public void testConstructor_HasParameters_ValidProperties()
			throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", "FINE");
		p.put("java.util.logging.StreamHandler.filter", className
				+ "$MockFilter");
		p.put("java.util.logging.StreamHandler.formatter", className
				+ "$MockFormatter");
		p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals("FINE", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new MockFormatter2());
		assertSame(h.getLevel(), Level.parse("FINE"));
		assertTrue(h.getFormatter() instanceof MockFormatter2);
		assertTrue(h.getFilter() instanceof MockFilter);
		assertEquals("iso-8859-1", h.getEncoding());
	}

	/*
	 * Test the constructor with normal parameter, and invalid relevant log
	 * manager properties are set.
	 */
	public void testConstructor_HasParameters_InvalidProperties()
			throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", INVALID_LEVEL);
		p.put("java.util.logging.StreamHandler.filter", className + "");
		p.put("java.util.logging.StreamHandler.formatter", className + "");
		p.put("java.util.logging.StreamHandler.encoding", "XXXX");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals(INVALID_LEVEL, LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("XXXX", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new MockFormatter2());
		assertSame(Level.INFO, h.getLevel());
		assertTrue(h.getFormatter() instanceof MockFormatter2);
		assertNull(h.getFilter());
		assertNull(h.getEncoding());
	}

	/*
	 * Test the constructor with null formatter, and invalid relevant log manager
	 * properties are set.
	 */
	public void testConstructor_HasParameters_ValidPropertiesNullStream()
			throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", "FINE");
		p.put("java.util.logging.StreamHandler.filter", className
				+ "$MockFilter");
		p.put("java.util.logging.StreamHandler.formatter", className
				+ "$MockFormatter");
		p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals("FINE", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		try {
			new StreamHandler(new ByteArrayOutputStream(), null);
			fail("Should throw NullPointerException!");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/*
	 * Test the constructor with null output stream, and invalid relevant log
	 * manager properties are set.
	 */
	public void testConstructor_HasParameters_ValidPropertiesNullFormatter()
			throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", "FINE");
		p.put("java.util.logging.StreamHandler.filter", className
				+ "$MockFilter");
		p.put("java.util.logging.StreamHandler.formatter", className
				+ "$MockFormatter");
		p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals("FINE", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.level"));
		assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
				"java.util.logging.StreamHandler.encoding"));
		try {
			new StreamHandler(null, new MockFormatter2());
			fail("Should throw NullPointerException!");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/*
	 * Test close() when having sufficient privilege, and a record has been
	 * written to the output stream.
	 */
	public void testClose_SufficientPrivilege_NormalClose() {
		ByteArrayOutputStream aos = new MockOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.publish(new LogRecord(Level.SEVERE,
				"testClose_SufficientPrivilege_NormalClose msg"));
		h.close();
		assertEquals("close", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		assertNull(CallVerificationStack.getInstance().pop());
		assertEquals("flush", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		CallVerificationStack.getInstance().clear();
		assertTrue(aos.toString().endsWith("MockFormatter_Tail"));
		h.close();
	}

	/*
	 * Test close() when having sufficient privilege, and an output stream that
	 * always throws exceptions.
	 */
	public void testClose_SufficientPrivilege_Exception() {
		ByteArrayOutputStream aos = new MockExceptionOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.publish(new LogRecord(Level.SEVERE,
				"testClose_SufficientPrivilege_Exception msg"));
		h.flush();
		h.close();
	}

	/*
	 * Test close() when having sufficient privilege, and no record has been
	 * written to the output stream.
	 */
	public void testClose_SufficientPrivilege_DirectClose() {
		ByteArrayOutputStream aos = new MockOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.close();
		assertEquals("close", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		assertNull(CallVerificationStack.getInstance().pop());
		assertEquals("flush", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		CallVerificationStack.getInstance().clear();
		assertEquals("MockFormatter_HeadMockFormatter_Tail", aos.toString()
				);
	}

	/*
	 * Test close() when having insufficient privilege.
	 *
	public void testClose_InsufficientPrivilege() {
		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		try {
			StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
					new MockFormatter());
			h.close();
			fail("Should throw SecurityException!");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(oldMan);
		}
	}

	/*
	 * Test close() when having no output stream.
	 */
	public void testClose_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		h.close();
	}

	/*
	 * Test flush().
	 */
	public void testFlush_Normal() {
		ByteArrayOutputStream aos = new MockOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.flush();
		assertEquals("flush", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		assertNull(CallVerificationStack.getInstance().pop());
		CallVerificationStack.getInstance().clear();
	}

	/*
	 * Test flush() when having no output stream.
	 */
	public void testFlush_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		h.flush();
	}

	/*
	 * Test isLoggable(), use no filter, having output stream
	 */
	public void testIsLoggable_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		LogRecord r = new LogRecord(Level.INFO, null);
		assertFalse(h.isLoggable(r));

		h.setLevel(Level.WARNING);
		assertFalse(h.isLoggable(r));

		h.setLevel(Level.CONFIG);
		assertFalse(h.isLoggable(r));

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		assertFalse(h.isLoggable(r));
	}

	/*
	 * Test isLoggable(), use no filter, having output stream
	 */
	public void testIsLoggable_NoFilter() {
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new SimpleFormatter());
		LogRecord r = new LogRecord(Level.INFO, null);
		assertTrue(h.isLoggable(r));

		h.setLevel(Level.WARNING);
		assertFalse(h.isLoggable(r));

		h.setLevel(Level.CONFIG);
		assertTrue(h.isLoggable(r));

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		assertFalse(h.isLoggable(r));
	}

	/*
	 * Test isLoggable(), use a filter, having output stream
	 */
	public void testIsLoggable_WithFilter() {
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new SimpleFormatter());
		LogRecord r = new LogRecord(Level.INFO, null);
		h.setFilter(new MockFilter());
		assertFalse(h.isLoggable(r));
		assertSame(r, CallVerificationStack.getInstance().pop());

		h.setLevel(Level.CONFIG);
		assertFalse(h.isLoggable(r));
		assertSame(r, CallVerificationStack.getInstance().pop());

		h.setLevel(Level.WARNING);
		assertFalse(h.isLoggable(r));
		assertTrue(CallVerificationStack.getInstance().empty());
	}

	/*
	 * Test isLoggable(), null log record, having output stream. Handler should
	 * call ErrorManager to handle exceptional case
	 */
	public void testIsLoggable_Null() {
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new SimpleFormatter());
		assertFalse(h.isLoggable(null));
	}

	/*
	 * Test isLoggable(), null log record, without output stream
	 */
	public void testIsLoggable_Null_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		assertFalse(h.isLoggable(null));
	}

	/*
	 * Test publish(), use no filter, having output stream, normal log record.
	 */
	public void testPublish_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		LogRecord r = new LogRecord(Level.INFO, "testPublish_NoOutputStream");
		h.publish(r);

		h.setLevel(Level.WARNING);
		h.publish(r);

		h.setLevel(Level.CONFIG);
		h.publish(r);

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		h.publish(r);
	}

	/*
	 * Test publish(), use no filter, having output stream, normal log record.
	 */
	public void testPublish_NoFilter() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());

		LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
		h.setLevel(Level.INFO);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter", aos
				.toString());

		h.setLevel(Level.WARNING);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter", aos
				.toString());

		h.setLevel(Level.CONFIG);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
				+ "testPublish_NoFilter", aos.toString());

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
				+ "testPublish_NoFilter", aos.toString());
	}

	/*
	 * Test publish(), use a filter, having output stream, normal log record.
	 */
	public void testPublish_WithFilter() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.setFilter(new MockFilter());

		LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
		h.setLevel(Level.INFO);
		h.publish(r);
		h.flush();
		assertEquals("", aos.toString());
		assertSame(r, CallVerificationStack.getInstance().pop());

		h.setLevel(Level.WARNING);
		h.publish(r);
		h.flush();
		assertEquals("", aos.toString());
		assertTrue(CallVerificationStack.getInstance().empty());

		h.setLevel(Level.CONFIG);
		h.publish(r);
		h.flush();
		assertEquals("", aos.toString());
		assertSame(r, CallVerificationStack.getInstance().pop());

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		h.publish(r);
		h.flush();
		assertEquals("", aos.toString());
		assertTrue(CallVerificationStack.getInstance().empty());
	}

	/*
	 * Test publish(), null log record, handler should call ErrorManager to
	 * handle exceptional case
	 */
	public void testPublish_Null() {
		StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
				new SimpleFormatter());
		h.publish(null);
	}

	/*
	 * Test publish(), null log record, without output stream
	 */
	public void testPublish_Null_NoOutputStream() {
		StreamHandler h = new StreamHandler();
		h.publish(null);
		// regression test for Harmony-1279
		MockFilter filter = new MockFilter();
		h.setLevel(Level.FINER);
		h.setFilter(filter);
		LogRecord record = new LogRecord(Level.FINE, "abc");
		h.publish(record);
		// verify that filter.isLoggable is not called, because there's no
		// associated output stream.
		assertTrue(CallVerificationStack.getInstance().empty());
	}

	/*
	 * Test publish(), a log record with empty msg, having output stream
	 */
	public void testPublish_EmptyMsg() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		LogRecord r = new LogRecord(Level.INFO, "");
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head", aos.toString());
	}

	/*
	 * Test publish(), a log record with null msg, having output stream
	 */
	public void testPublish_NullMsg() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		LogRecord r = new LogRecord(Level.INFO, null);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head", aos.toString());
	}

	/*
	 * Test publish(), after close.
	 */
	public void testPublish_AfterClose() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.StreamHandler.level", "FINE");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		assertSame(h.getLevel(), Level.FINE);
		LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFormatter");
		assertTrue(h.isLoggable(r));
		h.close();
		assertFalse(h.isLoggable(r));
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_HeadMockFormatter_Tail", aos.toString());
	}

	/*
	 * Test setEncoding() method with supported encoding.
	 *
	public void testSetEncoding_Normal() throws Exception {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.setEncoding("iso-8859-1");
		assertEquals("iso-8859-1", h.getEncoding());
		LogRecord r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
		h.publish(r);
		h.flush();

		byte[] bytes = encoder.encode(
				CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
				.array();
		assertTrue(Arrays.equals(bytes, aos.toByteArray()));
	}

	/*
	 * Test setEncoding() method with supported encoding, after a log record
	 * has been written.
	 *
	public void testSetEncoding_AfterPublish() throws Exception {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		h.setEncoding("iso-8859-1");
		assertEquals("iso-8859-1", h.getEncoding());
		LogRecord r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
		h.publish(r);
		h.flush();
		assertTrue(Arrays.equals(aos.toByteArray(), encoder.encode(
				CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
				.array()));

		h.setEncoding("iso8859-1");
		assertEquals("iso8859-1", h.getEncoding());
		r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
		h.publish(r);
		h.flush();
		assertFalse(Arrays.equals(aos.toByteArray(), encoder.encode(
				CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"
						+ "testSetEncoding_Normal2")).array()));
		byte[] b0 = aos.toByteArray();
		byte[] b1 = encoder.encode(
				CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
				.array();
		byte[] b2 = encoder.encode(CharBuffer.wrap("\u6881\u884D\u8F69"))
				.array();
		byte[] b3 = new byte[b1.length + b2.length];
		System.arraycopy(b1, 0, b3, 0, b1.length);
		System.arraycopy(b2, 0, b3, b1.length, b2.length);
		assertTrue(Arrays.equals(b0, b3));
	}

	/*
	 * Test setEncoding() methods with null.
	 */
	public void testSetEncoding_Null() throws Exception {
		StreamHandler h = new StreamHandler();
		h.setEncoding(null);
		assertNull(h.getEncoding());
	}

	/*
	 * Test setEncoding() methods with unsupported encoding.
	 */
	public void testSetEncoding_Unsupported() {
		StreamHandler h = new StreamHandler();
		try {
			h.setEncoding("impossible");
			fail("Should throw UnsupportedEncodingException!");
		} catch (UnsupportedEncodingException e) {
			// expected
		}
		assertNull(h.getEncoding());
	}

	/*
	 * Test setEncoding() with insufficient privilege.
	 *
	public void testSetEncoding_InsufficientPrivilege() throws Exception {
		StreamHandler h = new StreamHandler();
		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		// set a normal value
		try {
			h.setEncoding("iso-8859-1");
			fail("Should throw SecurityException!");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(oldMan);
		}
		assertNull(h.getEncoding());
		System.setSecurityManager(new MockSecurityManager());
		// set an invalid value
		try {

			h.setEncoding("impossible");
			fail("Should throw SecurityException!");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(oldMan);
		}
		assertNull(h.getEncoding());
	}

	/*
	 * Test setEncoding() methods will flush a stream before setting.
	 */
	public void testSetEncoding_FlushBeforeSetting() throws Exception {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		StreamHandler h = new StreamHandler(aos, new MockFormatter());
		LogRecord r = new LogRecord(Level.INFO, "abcd");
		h.publish(r);
		assertFalse(aos.toString().indexOf("abcd") > 0);
		h.setEncoding("iso-8859-1");
		assertTrue(aos.toString().indexOf("abcd") > 0);
	}

	/*
	 * Test setOutputStream() with null.
	 */
	public void testSetOutputStream_null() {
		MockStreamHandler h = new MockStreamHandler(
				new ByteArrayOutputStream(), new SimpleFormatter());
		try {
			h.setOutputStream(null);
			fail("Should throw NullPointerException!");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/*
	 * Test setOutputStream() under normal condition.
	 */
	public void testSetOutputStream_Normal() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		MockStreamHandler h = new MockStreamHandler(aos, new MockFormatter());

		LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
		h.publish(r);
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		h.flush();
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal", aos
				.toString());

		ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
		h.setOutputStream(aos2);
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
				+ "MockFormatter_Tail", aos.toString());
		r = new LogRecord(Level.INFO, "testSetOutputStream_Normal2");
		h.publish(r);
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		h.flush();
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal2", aos2
				.toString());
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
				+ "MockFormatter_Tail", aos.toString());
	}

	/*
	 * Test setOutputStream() after close.
	 */
	public void testSetOutputStream_AfterClose() {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		MockStreamHandler h = new MockStreamHandler(aos, new MockFormatter());

		LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
		h.publish(r);
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		h.flush();
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal", aos
				.toString());
		h.close();

		ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
		h.setOutputStream(aos2);
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
				+ "MockFormatter_Tail", aos.toString());
		r = new LogRecord(Level.INFO, "testSetOutputStream_Normal2");
		h.publish(r);
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		h.flush();
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal2", aos2
				.toString());
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
				+ "MockFormatter_Tail", aos.toString());
	}

	/*
	 * Test setOutputStream() when having insufficient privilege.
	 *
	public void testSetOutputStream_InsufficientPrivilege() {
		MockStreamHandler h = new MockStreamHandler();
		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());

		try {
			h.setOutputStream(new ByteArrayOutputStream());
			fail("Should throw SecurityException!");
		} catch (SecurityException e) {
			// expected
		} finally {
			System.setSecurityManager(oldMan);
		}

		h = new MockStreamHandler();
		System.setSecurityManager(new MockSecurityManager());
		try {
			h.setOutputStream(null);
			fail("Should throw NullPointerException!");
		} catch (NullPointerException e) {
			// expected
		} finally {
			System.setSecurityManager(oldMan);
		}
	}

	/*
	 * A mock stream handler, expose setOutputStream.
	 */
	public static class MockStreamHandler extends StreamHandler {
		public MockStreamHandler() {
			super();
		}

		public MockStreamHandler(OutputStream out, Formatter formatter) {
			super(out, formatter);
		}

		public void setOutputStream(OutputStream out) {
			super.setOutputStream(out);
		}

		public boolean isLoggable(LogRecord r) {
			CallVerificationStack.getInstance().push(r);
			return super.isLoggable(r);
		}
	}

	/*
	 * A mock filter, always return false.
	 */
	public static class MockFilter implements Filter {

		public boolean isLoggable(LogRecord record) {
			CallVerificationStack.getInstance().push(record);
			return false;
		}
	}

	/*
	 * A mock formatter.
	 */
	public static class MockFormatter extends java.util.logging.Formatter {
		public String format(LogRecord r) {
			// System.out.println("formatter called...");
			return super.formatMessage(r);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
		 */
		public String getHead(Handler h) {
			return "MockFormatter_Head";
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.logging.Formatter#getTail(java.util.logging.Handler)
		 */
		public String getTail(Handler h) {
			return "MockFormatter_Tail";
		}
	}

	/*
	 * Another mock formatter.
	 */
	public static class MockFormatter2 extends java.util.logging.Formatter {
		public String format(LogRecord r) {
			// System.out.println("formatter2 called...");
			return r.getMessage();
		}
	}

	/*
	 * A mock output stream.
	 */
	public static class MockOutputStream extends ByteArrayOutputStream {

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#close()
		 */
		public void close() throws IOException {
			CallVerificationStack.getInstance().push(null);
			super.close();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#flush()
		 */
		public void flush() throws IOException {
			CallVerificationStack.getInstance().push(null);
			super.flush();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#write(int)
		 */
		public void write(int oneByte) {
			// TODO Auto-generated method stub
			super.write(oneByte);
		}
	}

	/*
	 * A mock output stream that always throw exception.
	 */
	public static class MockExceptionOutputStream extends ByteArrayOutputStream {

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#close()
		 */
		public void close() throws IOException {
			throw new IOException();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#flush()
		 */
		public void flush() throws IOException {
			throw new IOException();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		public synchronized void write(byte[] buffer, int offset, int count) {
			throw new NullPointerException();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#write(int)
		 */
		public synchronized void write(int oneByte) {
			throw new NullPointerException();
		}
	}

	/*
	 * Used to grant all permissions except logging control.
	 */
	public static class MockSecurityManager extends SecurityManager {

		public MockSecurityManager() {
		}

		public void checkPermission(Permission perm) {
			// grant all permissions except logging control
			if (perm instanceof LoggingPermission) {
				throw new SecurityException();
			}
		}

		public void checkPermission(Permission perm, Object context) {
			// grant all permissions except logging control
			if (perm instanceof LoggingPermission) {
				throw new SecurityException();
			}
		}
	}

}
