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

package libcore.java.util.logging;


import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;

public class OldLogManagerTest extends TestCase {

    private static final String FOO = "LogManagerTestFoo";

    LogManager mockManager;

    LogManager manager = LogManager.getLogManager();

    Properties props;

    private static String className = OldLogManagerTest.class.getName();

    static Handler handler = null;

    @Override protected void setUp() throws Exception {
        super.setUp();
        mockManager = new MockLogManager();
        handler = new MockHandler();
        props = new Properties();
        props.put("handlers", className + "$MockHandler " + className + "$MockHandler");
        props.put("java.util.logging.FileHandler.pattern", "%h/java%u.log");
        props.put("java.util.logging.FileHandler.limit", "50000");
        props.put("java.util.logging.FileHandler.count", "5");
        props.put("java.util.logging.FileHandler.formatter", "java.util.logging.XMLFormatter");
        props.put(".level", "FINE");
        props.put("java.util.logging.ConsoleHandler.level", "OFF");
        props.put("java.util.logging.ConsoleHandler.formatter","java.util.logging.SimpleFormatter");
        props.put("LogManagerTestFoo.handlers", "java.util.logging.ConsoleHandler");
        props.put("LogManagerTestFoo.level", "WARNING");
    }



    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        handler = null;
    }

    public void testLogManager() {
       class TestLogManager extends LogManager {
           public TestLogManager() {
               super();
           }
       }
       TestLogManager tlm = new TestLogManager();
       assertNotNull(tlm.toString());
    }

    /*
     * test for method public Logger getLogger(String name)
     * test covers following use cases:
     * case 1: test default and valid value
     * case 2: test throw NullPointerException
     * case 3: test bad name
     * case 4: check correct tested value
     */

    public void testGetLogger() throws Exception {

        // case 1: test default and valid value
        Logger log = new MockLogger(FOO, null);
        Logger foo = mockManager.getLogger(FOO);
        assertNull("Logger should be null", foo);
        assertTrue("logger wasn't registered successfully", mockManager.addLogger(log));
        foo = mockManager.getLogger(FOO);
        assertSame("two loggers not refer to the same object", foo, log);
        assertNull("logger foo should not haven parent", foo.getParent());

        // case 2: test throw NullPointerException
        try {
            mockManager.getLogger(null);
            fail("get null should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        // case 3: test bad name
        assertNull("LogManager should not have logger with unforeseen name", mockManager
                .getLogger("bad name"));

        // case 4: check correct tested value
        Enumeration<String> enumar = mockManager.getLoggerNames();
        int i = 0;
        while (enumar.hasMoreElements()) {
            String name = enumar.nextElement();
            i++;
            assertEquals("name logger should be equal to foreseen name", FOO, name);
        }
        assertEquals("LogManager should contain one element", 1, i);
    }

    /*
     * test for method public Logger getLogger(String name)
     */
    public void testGetLogger_duplicateName() throws Exception {
        // test duplicate name
        // add logger with duplicate name has no effect
        mockManager.reset();
        Logger foo2 = new MockLogger(FOO, null);
        Logger foo3 = new MockLogger(FOO, null);
        mockManager.addLogger(foo2);
        assertSame(foo2, mockManager.getLogger(FOO));
        mockManager.addLogger(foo3);
        assertSame(foo2, mockManager.getLogger(FOO));

        Enumeration<String> enumar2 = mockManager.getLoggerNames();
        int i = 0;
        while (enumar2.hasMoreElements()) {
            enumar2.nextElement();
            i++;
        }
        assertEquals(1, i);
    }

    /*
     * test for method public Logger getLogger(String name)
     */
    public void testGetLogger_hierarchy() throws Exception {
        // test hierarchy
        Logger foo = new MockLogger("testGetLogger_hierachy.foo", null);
        // but for non-mock LogManager, foo's parent should be root
        assertTrue(manager.addLogger(foo));
        assertSame(manager.getLogger(""), manager.getLogger("testGetLogger_hierachy.foo")
                .getParent());
    }

    /*
     * test for method public Logger getLogger(String name)
     */
    public void testGetLogger_nameSpace() throws Exception {
        // test name with space
        Logger foo = new MockLogger(FOO, null);
        Logger fooBeforeSpace = new MockLogger(FOO + " ", null);
        Logger fooAfterSpace = new MockLogger(" " + FOO, null);
        Logger fooWithBothSpace = new MockLogger(" " + FOO + " ", null);
        assertTrue(mockManager.addLogger(foo));
        assertTrue(mockManager.addLogger(fooBeforeSpace));
        assertTrue(mockManager.addLogger(fooAfterSpace));
        assertTrue(mockManager.addLogger(fooWithBothSpace));

        assertSame(foo, mockManager.getLogger(FOO));
        assertSame(fooBeforeSpace, mockManager.getLogger(FOO + " "));
        assertSame(fooAfterSpace, mockManager.getLogger(" " + FOO));
        assertSame(fooWithBothSpace, mockManager.getLogger(" " + FOO + " "));
    }

    /*
     * test for method public void checkAccess() throws SecurityException
     */
    public void testCheckAccess() {
        try {
            manager.checkAccess();
        } catch (SecurityException e) {
            fail("securityException should not be thrown");
        }
    }

    public void testReadConfiguration() throws SecurityException,
            IOException {

        MockConfigLogManager lm = new MockConfigLogManager();
        assertFalse(lm.isCalled);

        lm.readConfiguration();
        assertTrue(lm.isCalled);
    }

    public void testReadConfigurationInputStream_IOException_1parm() throws SecurityException {
        try {
            mockManager.readConfiguration(new MockInputStream());
            fail("should throw IOException");
        } catch (IOException expected) {
        }
    }

    public static class MockInputStream extends InputStream {
        @Override public int read() throws IOException {
            throw new IOException();
        }
    }

    public static class MockLogger extends Logger {
        public MockLogger(String name, String rbName) {
            super(name, rbName);
        }
    }

    public static class MockLogManager extends LogManager {}

    public static class MockConfigLogManager extends LogManager {
        public boolean isCalled = false;

        public void readConfiguration(InputStream ins) throws IOException {
            isCalled = true;
            super.readConfiguration(ins);
        }
    }

    public static class MockHandler extends Handler {
        static int number = 0;

        public MockHandler() {
            addNumber();
        }

        private synchronized void addNumber() {
            number++;
        }

        public void close() {
            minusNumber();
        }

        private synchronized void minusNumber() {
            number--;
        }

        public void flush() {}

        public void publish(LogRecord record) {}
    }
}
