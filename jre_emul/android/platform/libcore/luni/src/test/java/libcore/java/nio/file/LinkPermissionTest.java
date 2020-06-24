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
 * limitations under the License
 */

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.nio.file.LinkPermission;

public class LinkPermissionTest extends TestCase {

    public void test_constructor$String() {
        // Only "hard" and "symbolic" are the supported permission target names.
        LinkPermission linkPermission = new LinkPermission("hard");
        // Sanity check that getName() doesn't throw.
        linkPermission.getName();

        linkPermission = new LinkPermission("symbolic");
        linkPermission.getName();

        // Non supported permission target names.
        try {
            new LinkPermission("test");
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_constructor$String$String() {
        // Only empty string or null is accepted as action.
        String actions = "";
        LinkPermission linkPermission = new LinkPermission("hard", actions);
        assertEquals("", linkPermission.getActions());

        actions = null;
        linkPermission = new LinkPermission("hard", actions);
        assertEquals("", linkPermission.getActions());

        // When actions is non empty string.
        try {
            actions = "abc";
            new LinkPermission("hard", actions);
            fail();
        } catch (IllegalArgumentException expected) {}
    }
}
