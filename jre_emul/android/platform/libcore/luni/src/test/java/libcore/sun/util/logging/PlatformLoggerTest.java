/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.sun.util.logging;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import sun.util.logging.PlatformLogger;

public class PlatformLoggerTest extends TestCase {

    /**
     * Checks that the values of the private static final int constants in
     * {@link PlatformLogger} code match the corresponding
     * {@link Level#intValue()}. This constraint is mentioned in a comment
     * in PlatformLogger.java.
     */
    public void testLogLevelConstants() throws Exception {
        assertLogLevel("SEVERE", Level.SEVERE);
        assertLogLevel("WARNING", Level.WARNING);
        assertLogLevel("INFO", Level.INFO);
        assertLogLevel("CONFIG", Level.CONFIG);
        assertLogLevel("FINE", Level.FINE);
        assertLogLevel("FINER", Level.FINER);
        assertLogLevel("FINEST", Level.FINEST);
        assertLogLevel("ALL", Level.ALL);
        assertLogLevel("OFF", Level.OFF);
    }

    private void assertLogLevel(String levelName, Level javaUtilLoggingLevel) throws Exception {
        Field field =  PlatformLogger.class.getDeclaredField(levelName);
        field.setAccessible(true);
        int platformLoggerValue = field.getInt(PlatformLogger.class);
        int javaUtilLoggingValue = javaUtilLoggingLevel.intValue();
        assertEquals(levelName, javaUtilLoggingValue, platformLoggerValue);

        // Check that the field is a constant (static and final); we don't care about
        // other modifiers (public/private).
        int requiredModifiers = Modifier.STATIC | Modifier.FINAL;
        assertEquals(requiredModifiers, field.getModifiers() & requiredModifiers);
    }

}
