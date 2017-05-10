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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;

public class OldLoggerTest extends TestCase {
    private final static String VALID_RESOURCE_BUNDLE = "bundles/java/util/logging/res";
    private final static String INVALID_RESOURCE_BUNDLE = "impossible_not_existing";
    private final static String VALID_KEY = "LOGGERTEST";
    private final static String VALID_VALUE = "Test_ZH_CN";

    @Override protected void setUp() throws Exception {
        super.setUp();
        LogManager.getLogManager().reset();
        Locale.setDefault(new Locale("zh", "CN"));
    }

    @Override protected void tearDown() throws Exception {
        LogManager.getLogManager().reset();
        super.tearDown();
    }

    public void testGetLoggerWithRes_InvalidResourceBundle() {
        assertNull(LogManager.getLogManager().getLogger(
                "testMissingResourceException"));

        assertNotNull(LogManager.getLogManager().getLogger(""));
        // The root logger always exists TODO
        try {
            Logger.getLogger("", INVALID_RESOURCE_BUNDLE);
            fail();
        } catch (MissingResourceException expected) {
        }
    }

    public void testGlobalLogger() {
        assertNull(Logger.global.getFilter());
        assertEquals(0, Logger.global.getHandlers().length);
        assertNull(Logger.global.getLevel());
        assertEquals("global", Logger.global.getName());
        assertNull(Logger.global.getParent().getParent());
        assertNull(Logger.global.getResourceBundle());
        assertNull(Logger.global.getResourceBundleName());
        assertTrue(Logger.global.getUseParentHandlers());
        assertSame(Logger.global, Logger.getLogger("global"));
        assertSame(Logger.global, LogManager.getLogManager().getLogger("global"));
        assertSame(Logger.global, Logger.getGlobal());
    }

    public void testConstructor_Normal() {
        MockLogger mlog = new MockLogger("myname", VALID_RESOURCE_BUNDLE);
        assertNull(mlog.getFilter());
        assertEquals(0, mlog.getHandlers().length);
        assertNull(mlog.getLevel());
        assertEquals("myname", mlog.getName());
        assertNull(mlog.getParent());
        ResourceBundle rb = mlog.getResourceBundle();
        assertEquals(VALID_VALUE, rb.getString(VALID_KEY));
        assertEquals(mlog.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(mlog.getUseParentHandlers());
    }

    public void testConstructor_Null() {
        MockLogger mlog = new MockLogger(null, null);
        assertNull(mlog.getFilter());
        assertEquals(0, mlog.getHandlers().length);
        assertNull(mlog.getLevel());
        assertNull(mlog.getName());
        assertNull(mlog.getParent());
        assertNull(mlog.getResourceBundle());
        assertNull(mlog.getResourceBundleName());
        assertTrue(mlog.getUseParentHandlers());
    }

    public void testConstructor_InvalidName() {
        MockLogger mlog = new MockLogger("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|",
                null);
        assertEquals("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|", mlog.getName());
    }

    /*
     * Test constructor with empty name.
     */
    public void testConstructor_EmptyName() {
        MockLogger mlog = new MockLogger("", null);
        assertEquals("", mlog.getName());
    }

    public void testConstructor_InvalidResourceBundle() {
        try {
            new MockLogger("testConstructor_InvalidResourceBundle",
                    INVALID_RESOURCE_BUNDLE);
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException expected) {
        }
    }

    public void testGetLogger_Null() {
        try {
            Logger.getLogger(null, null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException expected) {
        }
    }

    public void testGetLogger_WithParent() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_WithParent_ParentLogger"));

        // get root of hierarchy
        Logger root = Logger.getLogger("");
        // create the parent logger
        Logger pLog = Logger.getLogger("testGetLogger_WithParent_ParentLogger",
                VALID_RESOURCE_BUNDLE);
        pLog.setLevel(Level.CONFIG);
        pLog.addHandler(new MockHandler());
        pLog.setFilter(new MockFilter());
        pLog.setUseParentHandlers(false);
        // check root parent
        assertEquals("testGetLogger_WithParent_ParentLogger", pLog.getName());
        assertSame(pLog.getParent(), root);

        // child part
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_WithParent_ParentLogger.child"));
        // create the child logger
        Logger child = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger.child");
        assertNull(child.getFilter());
        assertEquals(0, child.getHandlers().length);
        assertNull(child.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger.child", child
                .getName());
        assertSame(child.getParent(), pLog);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
        assertTrue(child.getUseParentHandlers());

        // create not valid child
        Logger notChild = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger1.child");
        assertNull(notChild.getFilter());
        assertEquals(0, notChild.getHandlers().length);
        assertNull(notChild.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger1.child", notChild
                .getName());
        assertNotSame(notChild.getParent(), pLog);
        assertNull(notChild.getResourceBundle());
        assertNull(notChild.getResourceBundleName());
        assertTrue(notChild.getUseParentHandlers());
        // verify two level root.parent
        assertEquals("testGetLogger_WithParent_ParentLogger.child", child
                .getName());
        assertSame(child.getParent().getParent(), root);


        // create three level child
        Logger childOfChild = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger.child.child");
        assertNull(childOfChild.getFilter());
        assertEquals(0, childOfChild.getHandlers().length);
        assertSame(child.getParent().getParent(), root);
        assertNull(childOfChild.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger.child.child",
                childOfChild.getName());

        assertSame(childOfChild.getParent(), child);
        assertSame(childOfChild.getParent().getParent(), pLog);
        assertSame(childOfChild.getParent().getParent().getParent(), root);
        assertNull(childOfChild.getResourceBundle());
        assertNull(childOfChild.getResourceBundleName());
        assertTrue(childOfChild.getUseParentHandlers());

        // abnormal case : lookup to root parent in a hierarchy without a logger
        // parent created between
        assertEquals("testGetLogger_WithParent_ParentLogger1.child", notChild
                .getName());
        assertSame(child.getParent().getParent(), root);
        assertNotSame(child.getParent(), root);

        // abnormal cases
        assertNotSame(root.getParent(), root);
        Logger twoDot = Logger.getLogger("..");
        assertSame(twoDot.getParent(), root);

    }

    public static class MockLogger extends Logger {
        public MockLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }
    }

    public static class MockHandler extends Handler {
        public void close() {}
        public void flush() {}
        public void publish(LogRecord record) {}
    }

    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            return false;
        }
    }
}
