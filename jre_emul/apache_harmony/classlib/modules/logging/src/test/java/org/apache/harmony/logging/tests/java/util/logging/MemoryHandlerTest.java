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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.security.Permission;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.MemoryHandler;
import java.util.logging.SimpleFormatter;

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.HandlerTest.NullOutputStream;
import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;

/**
 *
 */
public class MemoryHandlerTest extends TestCase {

	final static LogManager manager = LogManager.getLogManager();

	final static Properties props = new Properties();

	final static String baseClassName = MemoryHandlerTest.class.getName();

	final static StringWriter writer = new StringWriter();

	final static SecurityManager securityManager = new MockSecurityManager();

    private final PrintStream err = System.err;

    private OutputStream errSubstituteStream = null;

	MemoryHandler handler;

	Handler target = new MockHandler();

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		manager.reset();
		initProps();
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));
	}

	/**
	 *
	 */
	private void initProps() {
		props.put("java.util.logging.MemoryHandler.level", "FINE");
		props.put("java.util.logging.MemoryHandler.filter", baseClassName
				+ "$MockFilter");
		props.put("java.util.logging.MemoryHandler.size", "2");
		props.put("java.util.logging.MemoryHandler.push", "WARNING");
		props.put("java.util.logging.MemoryHandler.target", baseClassName
				+ "$MockHandler");
		props.put("java.util.logging.MemoryHandler.formatter", baseClassName
				+ "$MockFormatter");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		manager.readConfiguration();
		props.clear();
        System.setErr(err);
	}

	/*
	public void testSecurity() {
		SecurityManager currentManager = System.getSecurityManager();
		System.setSecurityManager(securityManager);
		try {
			try {
				handler.close();
				fail("should throw security exception");
			} catch (SecurityException e) {
			}
			try {
				handler.setPushLevel(Level.CONFIG);
				fail("should throw security exception");
			} catch (SecurityException e) {
			}
			handler.flush();
			handler.push();
			handler.getPushLevel();
			handler.isLoggable(new LogRecord(Level.ALL, "message"));
			handler.publish(new LogRecord(Level.ALL, "message"));
		} finally {
			System.setSecurityManager(currentManager);
		}

	}
	*/

	public void testClose() {
		Filter filter = handler.getFilter();
		Formatter formatter = handler.getFormatter();
		writer.getBuffer().setLength(0);
		handler.close();
		assertEquals(writer.toString(), "close");
		assertEquals(handler.getFilter(), filter);
		assertEquals(handler.getFormatter(), formatter);
		assertNull(handler.getEncoding());
		assertNotNull(handler.getErrorManager());
		assertEquals(handler.getLevel(), Level.OFF);
		assertEquals(handler.getPushLevel(), Level.WARNING);
		assertFalse(handler.isLoggable(new LogRecord(Level.SEVERE, "test")));
	}

	public void testFlush() {
		Filter filter = handler.getFilter();
		Formatter formatter = handler.getFormatter();
		writer.getBuffer().setLength(0);
		handler.flush();
		assertEquals(writer.toString(), "flush");
		assertEquals(handler.getFilter(), filter);
		assertEquals(handler.getFormatter(), formatter);
		assertNull(handler.getEncoding());
		assertNotNull(handler.getErrorManager());
		assertEquals(handler.getLevel(), Level.FINE);
		assertEquals(handler.getPushLevel(), Level.WARNING);
		assertTrue(handler.isLoggable(new LogRecord(Level.SEVERE, "test")));
	}

	public void testIsLoggable() {
		try {
			handler.isLoggable(null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
		LogRecord record = new LogRecord(Level.FINER, "MSG1");
		assertFalse(handler.isLoggable(record));

		record = new LogRecord(Level.FINE, "MSG2");
		assertTrue(handler.isLoggable(record));

		record = new LogRecord(Level.CONFIG, "MSG3");
		assertTrue(handler.isLoggable(record));

		record = new LogRecord(Level.CONFIG, "false");
		assertFalse(handler.isLoggable(record));

		handler.setFilter(null);
		record = new LogRecord(Level.CONFIG, "false");
		assertTrue(handler.isLoggable(record));
	}

	/*
	 * Class under test for void MemoryHandler()
	 */
	public void testMemoryHandler() {
		assertTrue(handler.getFilter() instanceof MockFilter);
		assertTrue(handler.getFormatter() instanceof MockFormatter);
		assertNull(handler.getEncoding());
		assertNotNull(handler.getErrorManager());
		assertEquals(handler.getLevel(), Level.FINE);
		assertEquals(handler.getPushLevel(), Level.WARNING);
	}

	public void testMemoryHandlerInvalidProps() throws IOException {
		// null target
		try {
			props.remove("java.util.logging.MemoryHandler.target");
			manager.readConfiguration(EnvironmentHelper
					.PropertiesToInputStream(props));
			handler = new MemoryHandler();
			fail("should throw RuntimeException: target must be set");
		} catch (RuntimeException e) {
		}

		// invalid target
		try {
			props.put("java.util.logging.MemoryHandler.target", "badname");
			manager.readConfiguration(EnvironmentHelper
					.PropertiesToInputStream(props));
			handler = new MemoryHandler();
			fail("should throw RuntimeException: target must be valid");
		} catch (RuntimeException e) {
		}

		// invalid formatter
		initProps();
		props.put("java.util.logging.MemoryHandler.formatter", "badname");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		assertTrue(handler.getFormatter() instanceof SimpleFormatter);

		// invalid level
		initProps();
		props.put("java.util.logging.MemoryHandler.level", "badname");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		assertEquals(handler.getLevel(), Level.ALL);

		// invalid pushlevel
		initProps();
		props.put("java.util.logging.MemoryHandler.push", "badname");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		assertEquals(handler.getPushLevel(), Level.SEVERE);

		// invalid filter
		initProps();
		props.put("java.util.logging.MemoryHandler.filter", "badname");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		assertNull(handler.getFilter());

		// invalid size
		initProps();
		props.put("java.util.logging.MemoryHandler.size", "-1");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		initProps();
		props.put("java.util.logging.MemoryHandler.size", "badsize");
		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();

	}

	public void testMemoryHandlerDefaultValue() throws SecurityException,
			IOException {
		props.clear();
		props.put("java.util.logging.MemoryHandler.target", baseClassName
				+ "$MockHandler");

		manager.readConfiguration(EnvironmentHelper
				.PropertiesToInputStream(props));
		handler = new MemoryHandler();
		assertNull(handler.getFilter());
		assertTrue(handler.getFormatter() instanceof SimpleFormatter);
		assertNull(handler.getEncoding());
		assertNotNull(handler.getErrorManager());
		assertEquals(handler.getLevel(), Level.ALL);
		assertEquals(handler.getPushLevel(), Level.SEVERE);
	}

	/*
	 * Class under test for void MemoryHandler(Handler, int, Level)
	 */
	public void testMemoryHandlerHandlerintLevel() {
		handler = new MemoryHandler(target, 2, Level.FINEST);
		assertTrue(handler.getFilter() instanceof MockFilter);
		assertTrue(handler.getFormatter() instanceof MockFormatter);
		assertNull(handler.getEncoding());
		assertNotNull(handler.getErrorManager());
		assertEquals(handler.getLevel(), Level.FINE);
		assertEquals(handler.getPushLevel(), Level.FINEST);
		assertNull(target.getFormatter());

		try {
			handler = new MemoryHandler(null, 2, Level.FINEST);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		try {
			handler = new MemoryHandler(target, 2, null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		try {
			handler = new MemoryHandler(target, 0, Level.FINEST);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			handler = new MemoryHandler(target, -1, Level.FINEST);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

	}

	public void testGetPushLevel() {
		try {
			handler.setPushLevel(null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		handler.setPushLevel(Level.parse("123"));
		assertEquals(handler.getPushLevel(), Level.parse("123"));
	}

	public void testSetPushLevel() {
		// change push level don't trigger push action
		writer.getBuffer().setLength(0);
		LogRecord lr = new LogRecord(Level.CONFIG, "lr");
		assertTrue(handler.isLoggable(lr));
		handler.publish(lr);
		assertEquals(writer.toString(), "");
		// assertEquals(writer.toString(), "flush");
		writer.getBuffer().setLength(0);
		handler.setPushLevel(Level.FINE);
		assertEquals(writer.toString(), "");
		handler.publish(lr);
		assertEquals(writer.toString(), lr.getMessage() + lr.getMessage());
	}

	public void testPushPublic() {
		writer.getBuffer().setLength(0);
		// loggable but don't trig push
		handler.publish(new LogRecord(Level.CONFIG, "MSG1"));
		assertEquals("", writer.toString());
		// trig push
		handler.publish(new LogRecord(Level.SEVERE, "MSG2"));
		assertEquals(writer.toString(), "MSG1MSG2");
		writer.getBuffer().setLength(0);

        // regression test for Harmony-1292
        handler.publish(new LogRecord(Level.WARNING, "MSG"));
        assertEquals("MSG",writer.toString());

        writer.getBuffer().setLength(0);
		// push nothing
		handler.push();
		assertEquals("", writer.toString());
		// loggable but not push
		handler.publish(new LogRecord(Level.CONFIG, "MSG3"));
		assertEquals("", writer.toString());
		// not loggable
		handler.publish(new LogRecord(Level.FINEST, "MSG4"));
		assertEquals("", writer.toString());
		// loggable but not push
		handler.publish(new LogRecord(Level.CONFIG, "MSG5"));
		assertEquals("", writer.toString());
		// not loggable
		handler.publish(new LogRecord(Level.FINER, "MSG6"));
		assertEquals("", writer.toString());
		// not loggable
		handler.publish(new LogRecord(Level.FINER, "false"));
		assertEquals("", writer.toString());
		// loggable but not push
		handler.publish(new LogRecord(Level.CONFIG, "MSG8"));
		assertEquals("", writer.toString());
		// push all
		handler.push();
		assertEquals(writer.toString(), "MSG5MSG8");
		writer.getBuffer().setLength(0);
		handler.push();
		assertEquals("", writer.toString());
	}

	/*
	 * mock classes
	 */
	public static class MockFilter implements Filter {
		public boolean isLoggable(LogRecord record) {
			return !record.getMessage().equals("false");
		}
	}

	public static class MockHandler extends Handler {
		public void close() {
			writer.write("close");
		}

		public void flush() {
			writer.write("flush");
		}

		public void publish(LogRecord record) {
			writer.write(record.getMessage());
		}

	}

	public static class MockFormatter extends Formatter {
		public String format(LogRecord r) {
			return r.getMessage();
		}
	}

	public static class MockSecurityManager extends SecurityManager {
		public void checkPermission(Permission perm) {
			if (perm instanceof LoggingPermission) {
				throw new SecurityException();
			}
			return;
		}
	}

}
