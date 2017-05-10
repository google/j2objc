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

import java.io.Serializable;
import java.util.logging.Level;
import junit.framework.TestCase;

public final class OldLevelTest extends TestCase {

     public void testGetResourceBundleName() {
        String bundleName = "bundles/java/util/logging/res";
        Level l = new MockLevel("level1", 120);
        assertNull("level's localization resource bundle name is not null", l
                .getResourceBundleName());
        l = new MockLevel("level1", 120, bundleName);
        assertEquals("bundleName is non equal to actual value", bundleName, l
                .getResourceBundleName());
        l = new MockLevel("level1", 120, bundleName + "+abcdef");
        assertEquals("bundleName is non equal to actual value", bundleName
                + "+abcdef", l.getResourceBundleName());
    }

     /*
     * test for method public final int intValue()
     */
    public void testIntValue() {
        int value1 = 120;
        Level l = new MockLevel("level1", value1);
        assertEquals("integer value for this level is non equal to actual value",
                value1, l.intValue());
    }

    public static class MockLevel extends Level implements Serializable {
        private static final long serialVersionUID = 1L;

        public MockLevel(String name, int value) {
            super(name, value);
        }

        public MockLevel(String name, int value, String resourceBundleName) {
            super(name, value, resourceBundleName);
        }
    }
}
