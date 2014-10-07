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
import java.security.Permission;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.SimpleFormatter;

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;

import tests.util.CallVerificationStack;

/**
 * Test class java.util.logging.ConsoleHandler
 */
public class ConsoleHandlerTest extends TestCase {

	private final static String INVALID_LEVEL = "impossible_level";

	private final PrintStream err = System.err;

	private OutputStream errSubstituteStream = null;

	private static String className = ConsoleHandlerTest.class.getName();

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		errSubstituteStream = new MockOutputStream();
		System.setErr(new PrintStream(errSubstituteStream));
		LogManager.getLogManager().reset();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		LogManager.getLogManager().reset();
		CallVerificationStack.getInstance().clear();
		System.setErr(err);
	}

	/*
	 * Test the constructor with no relevant log manager properties are set.
	 */
	public void testConstructor_NoProperties() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.encoding"));

		ConsoleHandler h = new ConsoleHandler();
		assertSame(h.getLevel(), Level.INFO);
		assertTrue(h.getFormatter() instanceof SimpleFormatter);
		assertNull(h.getFilter());
		assertSame(h.getEncoding(), null);
	}

	/*
	 * Test the constructor with insufficient privilege.
	 *
	public void testConstructor_InsufficientPrivilege() {
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.level"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.filter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.formatter"));
		assertNull(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.encoding"));

		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		// set a normal value
		try {
			ConsoleHandler h = new ConsoleHandler();
			assertSame(h.getLevel(), Level.INFO);
			assertTrue(h.getFormatter() instanceof SimpleFormatter);
			assertNull(h.getFilter());
			assertSame(h.getEncoding(), null);
		} finally {
			System.setSecurityManager(oldMan);
		}
	}
	*/

	/*
	 * Test the constructor with valid relevant log manager properties are set.
	 */
	public void testConstructor_ValidProperties() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.level", "FINE");
		p.put("java.util.logging.ConsoleHandler.filter", className
				+ "$MockFilter");
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		p.put("java.util.logging.ConsoleHandler.encoding", "iso-8859-1");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.level"), "FINE");
		assertEquals(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.encoding"), "iso-8859-1");
		ConsoleHandler h = new ConsoleHandler();
		assertSame(h.getLevel(), Level.parse("FINE"));
		assertTrue(h.getFormatter() instanceof MockFormatter);
		assertTrue(h.getFilter() instanceof MockFilter);
		assertEquals(h.getEncoding(), "iso-8859-1");
	}

	/*
	 * Test the constructor with invalid relevant log manager properties are
	 * set.
	 */
	public void testConstructor_InvalidProperties() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.level", INVALID_LEVEL);
		p.put("java.util.logging.ConsoleHandler.filter", className);
		p.put("java.util.logging.ConsoleHandler.formatter", className);
		p.put("java.util.logging.ConsoleHandler.encoding", "XXXX");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));

		assertEquals(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.level"), INVALID_LEVEL);
		assertEquals(LogManager.getLogManager().getProperty(
				"java.util.logging.ConsoleHandler.encoding"), "XXXX");
		ConsoleHandler h = new ConsoleHandler();
		assertSame(h.getLevel(), Level.INFO);
		assertTrue(h.getFormatter() instanceof SimpleFormatter);
		assertNull(h.getFilter());
		assertNull(h.getEncoding());
		h.publish(new LogRecord(Level.SEVERE, "test"));
		assertNull(h.getEncoding());
	}

	/*
	 * Test close() when having sufficient privilege, and a record has been
	 * written to the output stream.
	 */
	public void testClose_SufficientPrivilege_NormalClose() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		h.publish(new LogRecord(Level.SEVERE,
				"testClose_SufficientPrivilege_NormalClose msg"));
		h.close();
		assertEquals("flush", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		assertNull(CallVerificationStack.getInstance().pop());
		h.close();
	}

	/*
	 * Test close() when having sufficient privilege, and an output stream that
	 * always throws exceptions.
	 */
	public void testClose_SufficientPrivilege_Exception() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();

		h.publish(new LogRecord(Level.SEVERE,
				"testClose_SufficientPrivilege_Exception msg"));
		h.flush();
		h.close();
	}

	/*
	 * Test close() when having sufficient privilege, and no record has been
	 * written to the output stream.
	 */
	public void testClose_SufficientPrivilege_DirectClose() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();

		h.close();
		assertEquals("flush", CallVerificationStack.getInstance()
				.getCurrentSourceMethod());
		assertNull(CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
	}

	/*
	 * Test close() when having insufficient privilege.
	 *
	public void testClose_InsufficientPrivilege() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		SecurityManager oldMan = System.getSecurityManager();
		System.setSecurityManager(new MockSecurityManager());
		try {
			h.close();
		} finally {
			System.setSecurityManager(oldMan);
		}
	}
	*/

	/*
	 * Test publish(), use no filter, having output stream, normal log record.
	 */
	public void testPublish_NoFilter() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();

		LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
		h.setLevel(Level.INFO);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter",
				this.errSubstituteStream.toString());

		h.setLevel(Level.WARNING);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter",
				this.errSubstituteStream.toString());

		h.setLevel(Level.CONFIG);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
				+ "testPublish_NoFilter", this.errSubstituteStream.toString());

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
				+ "testPublish_NoFilter", this.errSubstituteStream.toString());
	}

	/*
	 * Test publish(), after system err is reset.
	 */
	public void testPublish_AfterResetSystemErr() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		h.setFilter(new MockFilter());

		System.setErr(new PrintStream(new ByteArrayOutputStream()));

		LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
		h.setLevel(Level.INFO);
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertEquals("", this.errSubstituteStream.toString());
	}

	/*
	 * Test publish(), use a filter, having output stream, normal log record.
	 */
	public void testPublish_WithFilter() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		h.setFilter(new MockFilter());

		LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
		h.setLevel(Level.INFO);
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertEquals("", this.errSubstituteStream.toString());

		h.setLevel(Level.WARNING);
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		assertEquals("", this.errSubstituteStream.toString());

		h.setLevel(Level.CONFIG);
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertEquals("", this.errSubstituteStream.toString());

		r.setLevel(Level.OFF);
		h.setLevel(Level.OFF);
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertEquals("", this.errSubstituteStream.toString());
		assertTrue(CallVerificationStack.getInstance().empty());
	}

	/*
	 * Test publish(), null log record, having output stream, spec said
	 * rather than throw exception, handler should call errormanager to handle
	 * exception case, so NullPointerException shouldn't be thrown.
	 */
	public void testPublish_Null() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		h.publish(null);
	}

	/*
	 * Test publish(), a log record with empty msg, having output stream
	 */
	public void testPublish_EmptyMsg() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		LogRecord r = new LogRecord(Level.INFO, "");
		h.publish(r);
		h.flush();
		assertEquals("MockFormatter_Head", this.errSubstituteStream.toString());
	}

	/*
	 * Test publish(), a log record with null msg, having output stream
	 */
	public void testPublish_NullMsg() throws Exception {
		Properties p = new Properties();
		p.put("java.util.logging.ConsoleHandler.formatter", className
				+ "$MockFormatter");
		LogManager.getLogManager().readConfiguration(
				EnvironmentHelper.PropertiesToInputStream(p));
		ConsoleHandler h = new ConsoleHandler();
		LogRecord r = new LogRecord(Level.INFO, null);
		h.publish(r);
		h.flush();
		// assertEquals("MockFormatter_Head",
		// this.errSubstituteStream.toString());
	}

	public void testPublish_AfterClose() throws Exception {
		PrintStream backup = System.err;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			System.setErr(new PrintStream(bos));
			Properties p = new Properties();
			p.put("java.util.logging.ConsoleHandler.level", "FINE");
			p.put("java.util.logging.ConsoleHandler.formatter", className
					+ "$MockFormatter");
			LogManager.getLogManager().readConfiguration(
					EnvironmentHelper.PropertiesToInputStream(p));
			ConsoleHandler h = new ConsoleHandler();
			assertSame(h.getLevel(), Level.FINE);
			LogRecord r1 = new LogRecord(Level.INFO, "testPublish_Record1");
			LogRecord r2 = new LogRecord(Level.INFO, "testPublish_Record2");
			assertTrue(h.isLoggable(r1));
			h.publish(r1);
			assertTrue(bos.toString().indexOf("testPublish_Record1") >= 0);
			h.close();
			// assertFalse(h.isLoggable(r));
			assertTrue(h.isLoggable(r2));
			h.publish(r2);
			assertTrue(bos.toString().indexOf("testPublish_Record2") >= 0);
			h.flush();
			// assertEquals("MockFormatter_Head",
			// this.errSubstituteStream.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.setErr(backup);
		}
	}

	/*
	 * Test setOutputStream() under normal condition.
	 */
	public void testSetOutputStream_Normal() {
		MockStreamHandler h = new MockStreamHandler();
		h.setFormatter(new MockFormatter());

		LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
		h.publish(r);
		assertNull(CallVerificationStack.getInstance().pop());
		assertSame(r, CallVerificationStack.getInstance().pop());
		assertTrue(CallVerificationStack.getInstance().empty());
		assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal",
				this.errSubstituteStream.toString());

		ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
		h.setOutputStream(aos2);

		// assertEquals("close", DelegationParameterStack.getInstance()
		// .getCurrentSourceMethod());
		// assertNull(DelegationParameterStack.getInstance().pop());
		// assertEquals("flush", DelegationParameterStack.getInstance()
		// .getCurrentSourceMethod());
		// assertNull(DelegationParameterStack.getInstance().pop());
		// assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
		// + "MockFormatter_Tail", this.errSubstituteStream.toString());
		// r = new LogRecord(Level.INFO, "testSetOutputStream_Normal2");
		// h.publish(r);
		// assertSame(r, DelegationParameterStack.getInstance().pop());
		// assertTrue(DelegationParameterStack.getInstance().empty());
		// assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal2",
		// aos2
		// .toString());
		// assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
		// + "MockFormatter_Tail", this.errSubstituteStream.toString());
	}

	/*
	 * A mock filter, always return false.
	 */
	public static class MockFilter implements Filter {

		public boolean isLoggable(LogRecord record) {
			CallVerificationStack.getInstance().push(record);
			// System.out.println("filter called...");
			return false;
		}
	}

	/*
	 * A mock formatter.
	 */
	public static class MockFormatter extends Formatter {
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

	/*
	 * A mock stream handler, expose setOutputStream.
	 */
	public static class MockStreamHandler extends ConsoleHandler {
		public MockStreamHandler() {
			super();
		}

		public void setOutputStream(OutputStream out) {
			super.setOutputStream(out);
		}

		public boolean isLoggable(LogRecord r) {
			CallVerificationStack.getInstance().push(r);
			return super.isLoggable(r);
		}
	}

}
