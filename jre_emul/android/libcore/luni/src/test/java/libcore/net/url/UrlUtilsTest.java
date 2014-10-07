/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.net.url;

import junit.framework.TestCase;

public final class UrlUtilsTest extends TestCase {
    public void testCanonicalizePath() {
        assertEquals("", UrlUtils.canonicalizePath("", true));
        assertEquals("", UrlUtils.canonicalizePath(".", true));
        assertEquals("", UrlUtils.canonicalizePath("..", true));
        assertEquals("...", UrlUtils.canonicalizePath("...", true));
        assertEquals("", UrlUtils.canonicalizePath("./", true));
        assertEquals("", UrlUtils.canonicalizePath("../", true));
        assertEquals("a", UrlUtils.canonicalizePath("../a", true));
        assertEquals("a", UrlUtils.canonicalizePath("a", true));
        assertEquals("a/", UrlUtils.canonicalizePath("a/", true));
        assertEquals("a/", UrlUtils.canonicalizePath("a/.", true));
        assertEquals("a/b", UrlUtils.canonicalizePath("a/./b", true));
        assertEquals("", UrlUtils.canonicalizePath("a/..", true));
        assertEquals("b", UrlUtils.canonicalizePath("a/../b", true));
        assertEquals("a/.../b", UrlUtils.canonicalizePath("a/.../b", true));
        assertEquals("a/b", UrlUtils.canonicalizePath("a/b", true));
        assertEquals("a/b/", UrlUtils.canonicalizePath("a/b/.", true));
        assertEquals("a/b/", UrlUtils.canonicalizePath("a/b/./", true));
        assertEquals("a/b/c", UrlUtils.canonicalizePath("a/b/./c", true));
        assertEquals("a/", UrlUtils.canonicalizePath("a/b/..", true));
        assertEquals("a/", UrlUtils.canonicalizePath("a/b/../", true));
        assertEquals("a//", UrlUtils.canonicalizePath("a/b/..//", true));
        assertEquals("a/c", UrlUtils.canonicalizePath("a/b/../c", true));
        assertEquals("a//c", UrlUtils.canonicalizePath("a/b/..//c", true));
        assertEquals("c", UrlUtils.canonicalizePath("a/b/../../c", true));
        assertEquals("/", UrlUtils.canonicalizePath("/", true));
        assertEquals("//", UrlUtils.canonicalizePath("//", true));
        assertEquals("/", UrlUtils.canonicalizePath("/.", true));
        assertEquals("/", UrlUtils.canonicalizePath("/./", true));
        assertEquals("", UrlUtils.canonicalizePath("/..", true));
        assertEquals("c", UrlUtils.canonicalizePath("/../c", true));
        assertEquals("/a/b/c", UrlUtils.canonicalizePath("/a/b/c", true));
    }

    public void testGetProtocolPrefix() {
        assertEquals("http", UrlUtils.getSchemePrefix("http:"));
        assertEquals("http", UrlUtils.getSchemePrefix("HTTP:"));
        assertEquals("http", UrlUtils.getSchemePrefix("http:x"));
        assertEquals("a", UrlUtils.getSchemePrefix("a:"));
        assertEquals("z", UrlUtils.getSchemePrefix("z:"));
        assertEquals("a", UrlUtils.getSchemePrefix("A:"));
        assertEquals("z", UrlUtils.getSchemePrefix("Z:"));
        assertEquals("h0", UrlUtils.getSchemePrefix("h0:"));
        assertEquals("h5", UrlUtils.getSchemePrefix("h5:"));
        assertEquals("h9", UrlUtils.getSchemePrefix("h9:"));
        assertEquals("h+", UrlUtils.getSchemePrefix("h+:"));
        assertEquals("h-", UrlUtils.getSchemePrefix("h-:"));
        assertEquals("h.", UrlUtils.getSchemePrefix("h.:"));
    }

    public void testGetProtocolPrefixInvalidScheme() {
        assertNull(UrlUtils.getSchemePrefix(""));
        assertNull(UrlUtils.getSchemePrefix("http"));
        assertNull(UrlUtils.getSchemePrefix(":"));
        assertNull(UrlUtils.getSchemePrefix("+:"));
        assertNull(UrlUtils.getSchemePrefix("-:"));
        assertNull(UrlUtils.getSchemePrefix(".:"));
        assertNull(UrlUtils.getSchemePrefix("0:"));
        assertNull(UrlUtils.getSchemePrefix("5:"));
        assertNull(UrlUtils.getSchemePrefix("9:"));
        assertNull(UrlUtils.getSchemePrefix("http//"));
        assertNull(UrlUtils.getSchemePrefix("http/:"));
        assertNull(UrlUtils.getSchemePrefix("ht tp://"));
        assertNull(UrlUtils.getSchemePrefix(" http://"));
        assertNull(UrlUtils.getSchemePrefix("http ://"));
        assertNull(UrlUtils.getSchemePrefix(":://"));
    }
}
