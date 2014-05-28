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

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class LogRecordTest extends TestCase {

	static final String MSG = "test msg, pls. ignore itb";

	private LogRecord lr;

	private static String className = LogRecordTest.class.getName();

	protected void setUp() throws Exception {
		super.setUp();
		lr = new LogRecord(Level.CONFIG, MSG);

	}

	public void testLogRecordWithNullPointers() {
		try {
			new LogRecord(null, null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		try {
			new LogRecord(null, MSG);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		LogRecord r = new LogRecord(Level.WARNING, null);
		assertSame(r.getLevel(), Level.WARNING);
		assertNull(r.getMessage());
	}

	public void testGetSetLoggerName() {
		assertNull(lr.getLoggerName());
		lr.setLoggerName(null);
		assertNull(lr.getLoggerName());
		lr.setLoggerName("test logger name");
		assertEquals("test logger name", lr.getLoggerName());
	}

	public void testGetSetResourceBundle() {
		assertNull(lr.getResourceBundleName());
		assertNull(lr.getResourceBundle());

		lr.setResourceBundle(null);
		assertNull(lr.getResourceBundle());

		lr.setResourceBundleName("bundles/java/util/logging/res");
		assertNull(lr.getResourceBundle());

		lr.setResourceBundleName(null);
		ResourceBundle rb = ResourceBundle
				.getBundle("bundles/java/util/logging/res");
		lr.setResourceBundle(rb);
		assertEquals(rb, lr.getResourceBundle());
		assertNull(lr.getResourceBundleName());
	}

	public void testGetSetResourceBundleName() {
		assertNull(lr.getResourceBundleName());
		lr.setResourceBundleName(null);
		assertNull(lr.getResourceBundleName());
		lr.setResourceBundleName("test");
		assertEquals("test", lr.getResourceBundleName());
	}

	public void testGetSetLevel() {
		try {
			lr.setLevel(null);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
		}
		assertSame(lr.getLevel(), Level.CONFIG);
	}

	public void testGetSetSequenceNumber() {
		long l = lr.getSequenceNumber();
		lr.setSequenceNumber(-111);
		assertEquals(lr.getSequenceNumber(), -111L);
		lr.setSequenceNumber(0);
		assertEquals(lr.getSequenceNumber(), 0L);
		lr = new LogRecord(Level.ALL, null);
		assertEquals(lr.getSequenceNumber(), l + 1);
	}

	public void testGetSetSourceClassName() {
		lr.setSourceClassName(null);
		assertNull(lr.getSourceClassName());
		lr.setSourceClassName("bad class name");
		assertEquals("bad class name", lr.getSourceClassName());
		lr.setSourceClassName(this.getClass().getName());
		assertEquals(this.getClass().getName(), lr.getSourceClassName());
	}

	public void testGetSetSourceMethodName() {
		lr.setSourceMethodName(null);
		assertNull(lr.getSourceMethodName());
		lr.setSourceMethodName("bad class name");
		assertEquals("bad class name", lr.getSourceMethodName());
		lr.setSourceMethodName(this.getClass().getName());
		assertEquals(this.getClass().getName(), lr.getSourceMethodName());
	}

	public void testGetSourceDefaultValue() {
		assertNull(lr.getSourceMethodName());
		assertNull(lr.getSourceClassName());

		// find class and method who called logger
		Logger logger = Logger.global;
		MockHandler handler = new MockHandler();
		logger.addHandler(handler);
		logger.log(Level.SEVERE, MSG);
		assertEquals(this.getClass().getName(), handler.getSourceClassName());
		assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());

		// only set source method to null
		lr = new LogRecord(Level.SEVERE, MSG);
		lr.setSourceMethodName(null);
		logger.log(lr);
		assertNull(handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());

		// only set source class to null
		lr = new LogRecord(Level.SEVERE, MSG);
		lr.setSourceClassName(null);
		logger.log(lr);
		assertNull(handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());

		// set both
		lr = new LogRecord(Level.SEVERE, MSG);
		lr.setSourceClassName("className");
		lr.setSourceMethodName(null);
		logger.log(lr);
		assertEquals("className", handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());

		// test if LogRecord is constructed in another class, and is published
		// by Logger
		logger.log(RecordFactory.getDefaultRecord());
		assertEquals(this.getClass().getName(), handler.getSourceClassName());
		assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());

		lr = RecordFactory.getDefaultRecord();
		// assertNull(lr.getSourceClassName());
		// assertNull(lr.getSourceMethodName());
		RecordFactory.log(logger, lr);
		assertEquals(RecordFactory.class.getName(), handler
				.getSourceClassName());
		assertEquals("log", handler.getSourceMethodName());

		// only try once to get the default value
		lr = RecordFactory.getDefaultRecord();
		assertNull(lr.getSourceClassName());
		assertNull(lr.getSourceMethodName());
		RecordFactory.log(logger, lr);
		assertNull(handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());

		// it can find nothing when only call Subclass
		MockLogger ml = new MockLogger("foo", null);
		ml.addHandler(handler);
		ml.log(Level.SEVERE, MSG);
		assertNull(handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());

		// test if don't call logger, what is the default value
		lr = new LogRecord(Level.SEVERE, MSG);
		handler.publish(lr);
		assertNull(handler.getSourceClassName());
		assertNull(handler.getSourceMethodName());
		logger.removeHandler(handler);
	}

	public void testGetSetMessage() {
		assertEquals(MSG, lr.getMessage());
		lr.setMessage(null);
		assertNull(lr.getMessage());
		lr.setMessage("");
		assertEquals("", lr.getMessage());
	}

	public void testGetSetParameters() {
		assertNull(lr.getParameters());
		lr.setParameters(null);
		assertNull(lr.getParameters());
		Object[] oa = new Object[0];
		lr.setParameters(oa);
		assertEquals(oa, lr.getParameters());
		oa = new Object[] { new Object(), new Object() };
		lr.setParameters(oa);
		assertSame(oa, lr.getParameters());
	}

	public void testGetSetMillis() {
		long milli = lr.getMillis();
        assertTrue(milli > 0);
		lr.setMillis(-1);
		assertEquals(-1, lr.getMillis());
		lr.setMillis(0);
		assertEquals(0, lr.getMillis());
	}

	public void testGetSetThreadID() {
		// TODO how to test the different thread
		int id = lr.getThreadID();
		lr = new LogRecord(Level.ALL, "a1");
		assertEquals(id, lr.getThreadID());
		lr.setThreadID(id + 10);
		assertEquals(id + 10, lr.getThreadID());
		lr = new LogRecord(Level.ALL, "a1");
		assertEquals(id, lr.getThreadID());
	}

	public void testGetSetThrown() {
		assertNull(lr.getThrown());
		lr.setThrown(null);
		assertNull(lr.getThrown());
		Throwable e = new Exception();
		lr.setThrown(e);
		assertEquals(e, lr.getThrown());
	}

    // comparator for LogRecord objects
    private static final SerializableAssert LOGRECORD_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            LogRecord init = (LogRecord) initial;
            LogRecord dser = (LogRecord) deserialized;

            assertEquals("Class", init.getClass(), dser.getClass());
            assertEquals("Level", init.getLevel(), dser.getLevel());
            assertEquals("LoggerName", init.getLoggerName(), dser
                    .getLoggerName());
            assertEquals("Message", init.getMessage(), dser.getMessage());
            assertEquals("Millis", init.getMillis(), dser.getMillis());

            // compare parameters
            Object[] paramInit = init.getParameters();
            Object[] paramDser = dser.getParameters();
            assertEquals("Parameters length", paramInit.length,
                    paramDser.length);
            for (int i = 0; i < paramInit.length; i++) {
                assertEquals("Param: " + i, paramInit[i].toString(),
                        paramDser[i]);
            }

            // don't check ResourceBundle object
            // verify only bundle's name
            assertEquals("ResourceBundleName", init.getResourceBundleName(),
                    dser.getResourceBundleName());
            assertEquals("SequenceNumber", init.getSequenceNumber(), dser
                    .getSequenceNumber());
            assertEquals("SourceClassName", init.getSourceClassName(), dser
                    .getSourceClassName());
            assertEquals("SourceMethodName", init.getSourceMethodName(), dser
                    .getSourceMethodName());
            assertEquals("ThreadID", init.getThreadID(), dser.getThreadID());

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(init
                    .getThrown(), dser.getThrown());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     *
    TODO(tball): b/12032235
    public void testSerializationSelf() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r
                .setParameters(new Object[] { "test string",
                        new Exception("ex-msg") });
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));

        SerializationTest.verifySelf(r, LOGRECORD_COMPARATOR);
    }
    */

    /**
     * @tests resolution of resource bundle for serialization/deserialization.
     */
    public void testSerializationResourceBundle() throws Exception {

        // test case: valid resource bundle name
        lr.setResourceBundleName("bundles/java/util/logging/res2");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));

        LogRecord result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNotNull(result.getResourceBundle());

        // test case: invalid resource bundle name, it is not resolved during
        // deserialization LogRecord object so check for returned null value
        lr.setResourceBundleName("bad bundle name");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));

        result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNull(result.getResourceBundle());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     *
    TODO(tball): b/12032235
    public void testSerializationCompatibility() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r
                .setParameters(new Object[] { "test string",
                        new Exception("ex-msg") });
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));

        SerializationTest.verifyGolden(this, r, LOGRECORD_COMPARATOR);
    }
    */

	public static class MockHandler extends Handler {
		private String className;

		private String methodName;

		public void close() {
		}

		public void flush() {
		}

		public void publish(LogRecord record) {
			className = record.getSourceClassName();
			methodName = record.getSourceMethodName();
		}

		public String getSourceMethodName() {
			return methodName;
		}

		public String getSourceClassName() {
			return className;
		}
	}

	// mock class, try to test when the sourceclass and sourcemethod of
	// LogRecord is initiated
	public static class RecordFactory {

		public static LogRecord getDefaultRecord() {
			return new LogRecord(Level.SEVERE, MSG);
		}

		public static void log(Logger logger, LogRecord lr) {
			logger.log(lr);
		}
	}

	public static class MockLogger extends Logger {

		/**
		 * @param name
		 * @param resourceBundleName
		 */
		public MockLogger(String name, String resourceBundleName) {
			super(name, resourceBundleName);
		}

		public void log(Level l, String s) {
			this.log(new LogRecord(l, s));
		}

		public void info(String s) {
			super.info(s);
		}

		public void log(LogRecord record) {
			if (isLoggable(record.getLevel())) {
				// call the handlers of this logger
				// TODO: What if an exception occurred in handler?
				Handler[] ha = this.getHandlers();
				for (int i = 0; i < ha.length; i++) {
					ha[i].publish(record);
				}
				// call the parent's handlers if set useParentHandlers
				if (getUseParentHandlers()) {
					Logger anyParent = this.getParent();
					while (null != anyParent) {
						ha = anyParent.getHandlers();
						for (int i = 0; i < ha.length; i++) {
							ha[i].publish(record);
						}
						if (anyParent.getUseParentHandlers()) {
							anyParent = anyParent.getParent();
						} else {
							break;
						}
					}
				}
			}
		}
	}
}
