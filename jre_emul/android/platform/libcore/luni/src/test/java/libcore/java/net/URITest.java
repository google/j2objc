/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.net;

import junit.framework.TestCase;
import java.net.URI;
import java.net.URISyntaxException;
import libcore.libcore.util.SerializationTester;

public final class URITest extends TestCase {

    public void testUriParts() throws Exception {
        URI uri = new URI("http://username:password@host:8080/directory/file?query#ref");
        assertEquals("http", uri.getScheme());
        assertEquals("username:password@host:8080", uri.getAuthority());
        assertEquals("username:password@host:8080", uri.getRawAuthority());
        assertEquals("username:password", uri.getUserInfo());
        assertEquals("username:password", uri.getRawUserInfo());
        assertEquals("host", uri.getHost());
        assertEquals(8080, uri.getPort());
        assertEquals("/directory/file", uri.getPath());
        assertEquals("/directory/file", uri.getRawPath());
        assertEquals("query", uri.getQuery());
        assertEquals("query", uri.getRawQuery());
        assertEquals("ref", uri.getFragment());
        assertEquals("ref", uri.getRawFragment());
        assertEquals("//username:password@host:8080/directory/file?query",
                uri.getSchemeSpecificPart());
        assertEquals("//username:password@host:8080/directory/file?query",
                uri.getRawSchemeSpecificPart());
    }

    public void testEqualsCaseMapping() throws Exception {
        assertEquals(new URI("HTTP://localhost/foo?bar=baz#quux"),
                new URI("HTTP://localhost/foo?bar=baz#quux"));
        assertEquals(new URI("http://localhost/foo?bar=baz#quux"),
                new URI("http://LOCALHOST/foo?bar=baz#quux"));
        assertFalse(new URI("http://localhost/foo?bar=baz#quux")
                .equals(new URI("http://localhost/FOO?bar=baz#quux")));
        assertFalse(new URI("http://localhost/foo?bar=baz#quux")
                .equals(new URI("http://localhost/foo?BAR=BAZ#quux")));
        assertFalse(new URI("http://localhost/foo?bar=baz#quux")
                .equals(new URI("http://localhost/foo?bar=baz#QUUX")));
    }

    public void testEqualsEscaping() throws Exception {
        // Case insensitive when comparing escaped values, but not when
        // comparing unescaped values.
        assertEquals(new URI("http://localhost/foo?bar=fooobar%E0%AE%A8%E0bar"),
                new URI("http://localhost/foo?bar=fooobar%E0%AE%a8%e0bar"));
        assertFalse(new URI("http://localhost/foo?bar=fooobar%E0%AE%A8%E0bar").equals(
                new URI("http://localhost/foo?bar=FoooBar%E0%AE%a8%e0bar")));
        assertFalse(new URI("http://localhost/foo?bar=fooobar%E0%AE%A8%E0bar").equals(
                new URI("http://localhost/foo?bar=fooobar%E0%AE%a8%e0BaR")));

        // Last byte replaced by an unescaped value.
        assertFalse(new URI("http://localhost/foo?bar=%E0%AE%A8%E0").equals(
                new URI("http://localhost/foo?bar=%E0%AE%a8xxx")));
        // Missing byte.
        assertFalse(new URI("http://localhost/foo?bar=%E0%AE%A8%E0").equals(
                new URI("http://localhost/foo?bar=%E0%AE%a8")));
    }

    public void testFileEqualsWithEmptyHost() throws Exception {
        assertEquals(new URI("file", "", "/a/", null), new URI("file:/a/"));
        assertEquals(new URI("file", null, "/a/", null), new URI("file:/a/"));
    }

    public void testUriSerialization() throws Exception {
        String s = "aced00057372000c6a6176612e6e65742e555249ac01782e439e49ab0300014c0006737472696e6"
                + "77400124c6a6176612f6c616e672f537472696e673b787074002a687474703a2f2f757365723a706"
                + "1737340686f73742f706174682f66696c653f7175657279236861736878";
        URI uri = new URI("http://user:pass@host/path/file?query#hash");
        new SerializationTester<URI>(uri, s).test();
    }

    public void testEmptyHost() throws Exception {
        URI uri = new URI("http:///path");
        assertEquals(null, uri.getHost());
        assertEquals("/path", uri.getPath());
    }

    public void testNoHost() throws Exception {
        URI uri = new URI("http:/path");
        assertEquals(null, uri.getHost());
        assertEquals("/path", uri.getPath());
    }

    public void testNoPath() throws Exception {
        URI uri = new URI("http://host");
        assertEquals("host", uri.getHost());
        assertEquals("", uri.getPath());
    }

    public void testEmptyHostAndNoPath() throws Exception {
        try {
            new URI("http://");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    // http://b/26632332
    public void testSingleLetterHost() throws Exception {
        URI uri = new URI("http://a");
        assertEquals("a", uri.getHost());
        assertEquals("", uri.getPath());
    }

    public void testNoHostAndNoPath() throws Exception {
        try {
            new URI("http:");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testAtSignInUserInfo() throws Exception {
        URI uri = new URI("http://user@userhost.com:password@host");
        assertEquals("user@userhost.com:password@host", uri.getAuthority());
        assertEquals(null, uri.getUserInfo());
        assertEquals(null, uri.getHost());
    }

    public void testUserNoPassword() throws Exception {
        URI uri = new URI("http://user@host");
        assertEquals("user@host", uri.getAuthority());
        assertEquals("user", uri.getUserInfo());
        assertEquals("host", uri.getHost());
    }

    // http://b/26632332
    public void testUserNoHost() throws Exception {
        URI uri = new URI("http://user@");
        assertEquals("user@", uri.getAuthority());
        // from RI. this is curious
        assertEquals(null, uri.getUserInfo());
        assertEquals(null, uri.getHost());
    }

    public void testUserNoPasswordExplicitPort() throws Exception {
        URI uri = new URI("http://user@host:8080");
        assertEquals("user@host:8080", uri.getAuthority());
        assertEquals("user", uri.getUserInfo());
        assertEquals("host", uri.getHost());
        assertEquals(8080, uri.getPort());
    }

    public void testUserPasswordHostPort() throws Exception {
        URI uri = new URI("http://user:password@host:8080");
        assertEquals("user:password@host:8080", uri.getAuthority());
        assertEquals("user:password", uri.getUserInfo());
        assertEquals("host", uri.getHost());
        assertEquals(8080, uri.getPort());
    }

    public void testUserPasswordEmptyHostPort() throws Exception {
        URI uri = new URI("http://user:password@:8080");
        assertEquals("user:password@:8080", uri.getAuthority());
        // from RI. this is curious
        assertEquals(null, uri.getUserInfo());
        assertEquals(null, uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testUserPasswordEmptyHostEmptyPort() throws Exception {
        URI uri = new URI("http://user:password@:");
        assertEquals("user:password@:", uri.getAuthority());
        // from RI. this is curious
        assertEquals(null, uri.getUserInfo());
        assertEquals(null, uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testPathOnly() throws Exception {
        URI uri = new URI("http://host/path");
        assertEquals("host", uri.getHost());
        assertEquals("/path", uri.getPath());
    }

    public void testQueryOnly() throws Exception {
        URI uri = new URI("http://host?query");
        assertEquals("host", uri.getHost());
        assertEquals("", uri.getPath());
        assertEquals("query", uri.getQuery());
    }

    public void testFragmentOnly() throws Exception {
        URI uri = new URI("http://host#fragment");
        assertEquals("host", uri.getHost());
        assertEquals("", uri.getPath());
        assertEquals(null, uri.getQuery());
        assertEquals("fragment", uri.getFragment());
    }

    public void testAtSignInPath() throws Exception {
        URI uri = new URI("http://host/file@foo");
        assertEquals("/file@foo", uri.getPath());
        assertEquals(null, uri.getUserInfo());
    }


    public void testColonInPath() throws Exception {
        URI uri = new URI("http://host/file:colon");
        assertEquals("/file:colon", uri.getPath());
    }

    public void testSlashInQuery() throws Exception {
        URI uri = new URI("http://host/file?query/path");
        assertEquals("/file", uri.getPath());
        assertEquals("query/path", uri.getQuery());
    }

    public void testQuestionMarkInQuery() throws Exception {
        URI uri = new URI("http://host/file?query?another");
        assertEquals("/file", uri.getPath());
        assertEquals("query?another", uri.getQuery());
    }

    public void testAtSignInQuery() throws Exception {
        URI uri = new URI("http://host/file?query@at");
        assertEquals("/file", uri.getPath());
        assertEquals("query@at", uri.getQuery());
    }

    public void testColonInQuery() throws Exception {
        URI uri = new URI("http://host/file?query:colon");
        assertEquals("/file", uri.getPath());
        assertEquals("query:colon", uri.getQuery());
    }

    public void testQuestionMarkInFragment() throws Exception {
        URI uri = new URI("http://host/file#fragment?query");
        assertEquals("/file", uri.getPath());
        assertEquals(null, uri.getQuery());
        assertEquals("fragment?query", uri.getFragment());
    }

    public void testColonInFragment() throws Exception {
        URI uri = new URI("http://host/file#fragment:80");
        assertEquals("/file", uri.getPath());
        assertEquals(-1, uri.getPort());
        assertEquals("fragment:80", uri.getFragment());
    }

    public void testSlashInFragment() throws Exception {
        URI uri = new URI("http://host/file#fragment/path");
        assertEquals("/file", uri.getPath());
        assertEquals("fragment/path", uri.getFragment());
    }

    public void testHashInFragment() throws Exception {
        try {
            // This is not consistent with java.net.URL
            new URI("http://host/file#fragment#another");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testEmptyPort() throws Exception {
        URI uri = new URI("http://host:/");
        assertEquals(-1, uri.getPort());
    }

    public void testNonNumericPort() throws Exception {
        URI uri = new URI("http://host:x/");
        // From the RI. This is curious
        assertEquals(null, uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testNegativePort() throws Exception {
        URI uri = new URI("http://host:-2/");
        // From the RI. This is curious
        assertEquals(null, uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testNegativePortEqualsPlaceholder() throws Exception {
        URI uri = new URI("http://host:-1/");
        // From the RI. This is curious
        assertEquals(null, uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testRelativePathOnQuery() throws Exception {
        URI base = new URI("http://host/file?query/x");
        URI uri = base.resolve("another");
        assertEquals("http://host/another", uri.toString());
        assertEquals("/another", uri.getPath());
        assertEquals(null, uri.getQuery());
        assertEquals(null, uri.getFragment());
    }

    public void testRelativeFragmentOnQuery() throws Exception {
        URI base = new URI("http://host/file?query/x#fragment");
        URI uri = base.resolve("#another");
        assertEquals("http://host/file?query/x#another", uri.toString());
        assertEquals("/file", uri.getPath());
        assertEquals("query/x", uri.getQuery());
        assertEquals("another", uri.getFragment());
    }

    public void testPathContainsRelativeParts() throws Exception {
        URI uri = new URI("http://host/a/b/../c");
//        assertEquals("http://host/a/c", uri.toString()); // RI doesn't canonicalize
    }

    public void testRelativePathAndFragment() throws Exception {
        URI base = new URI("http://host/file");
        assertEquals("http://host/another#fragment", base.resolve("another#fragment").toString());
    }

    public void testRelativeParentDirectory() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("http://host/a/d", base.resolve("../d").toString());
    }

    public void testRelativeChildDirectory() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("http://host/a/b/d/e", base.resolve("d/e").toString());
    }

    public void testRelativeRootDirectory() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("http://host/d", base.resolve("/d").toString());
    }

    public void testRelativeFullUrl() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("http://host2/d/e", base.resolve("http://host2/d/e").toString());
        assertEquals("https://host2/d/e", base.resolve("https://host2/d/e").toString());
    }

    public void testRelativeDifferentScheme() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("https://host2/d/e", base.resolve("https://host2/d/e").toString());
    }

    public void testRelativeDifferentAuthority() throws Exception {
        URI base = new URI("http://host/a/b/c");
        assertEquals("http://another/d/e", base.resolve("//another/d/e").toString());
    }

    public void testRelativeWithScheme() throws Exception {
        URI base = new URI("http://host/a/b/c");
        try {
            base.resolve("http:");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        assertEquals("http:/", base.resolve("http:/").toString());
    }

    public void testMalformedUrlsRefusedByFirefoxAndChrome() throws Exception {
        URI base = new URI("http://host/a/b/c");
        try {
            base.resolve("http://");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            base.resolve("//");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            base.resolve("https:");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        assertEquals("https:/", base.resolve("https:/").toString());
        try {
            base.resolve("https://");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRfc1808NormalExamples() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        assertEquals("https:h", base.resolve("https:h").toString());
        assertEquals("http://a/b/c/g", base.resolve("g").toString());
        assertEquals("http://a/b/c/g", base.resolve("./g").toString());
        assertEquals("http://a/b/c/g/", base.resolve("g/").toString());
        assertEquals("http://a/g", base.resolve("/g").toString());
        assertEquals("http://g", base.resolve("//g").toString());
        assertEquals("http://a/b/c/d;p?y", base.resolve("?y").toString()); // RI fails; loses file
        assertEquals("http://a/b/c/g?y", base.resolve("g?y").toString());
        assertEquals("http://a/b/c/d;p?q#s", base.resolve("#s").toString());
        assertEquals("http://a/b/c/g#s", base.resolve("g#s").toString());
        assertEquals("http://a/b/c/g?y#s", base.resolve("g?y#s").toString());
        assertEquals("http://a/b/c/;x", base.resolve(";x").toString());
        assertEquals("http://a/b/c/g;x", base.resolve("g;x").toString());
        assertEquals("http://a/b/c/g;x?y#s", base.resolve("g;x?y#s").toString());
        assertEquals("http://a/b/c/d;p?q", base.resolve("").toString()); // RI returns http://a/b/c/
        assertEquals("http://a/b/c/", base.resolve(".").toString());
        assertEquals("http://a/b/c/", base.resolve("./").toString());
        assertEquals("http://a/b/", base.resolve("..").toString());
        assertEquals("http://a/b/", base.resolve("../").toString());
        assertEquals("http://a/b/g", base.resolve("../g").toString());
        assertEquals("http://a/", base.resolve("../..").toString());
        assertEquals("http://a/", base.resolve("../../").toString());
        assertEquals("http://a/g", base.resolve("../../g").toString());
    }

    public void testRfc1808AbnormalExampleTooManyDotDotSequences() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        assertEquals("http://a/g", base.resolve("../../../g").toString()); // RI doesn't normalize
        assertEquals("http://a/g", base.resolve("../../../../g").toString()); // fails on RI
    }

    public void testRfc1808AbnormalExampleRemoveDotSegments() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        assertEquals("http://a/g", base.resolve("/./g").toString()); // RI doesn't normalize
        assertEquals("http://a/g", base.resolve("/../g").toString()); // fails on RI
        assertEquals("http://a/b/c/g.", base.resolve("g.").toString());
        assertEquals("http://a/b/c/.g", base.resolve(".g").toString());
        assertEquals("http://a/b/c/g..", base.resolve("g..").toString());
        assertEquals("http://a/b/c/..g", base.resolve("..g").toString());
    }

    public void testRfc1808AbnormalExampleNonsensicalDots() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        assertEquals("http://a/b/g", base.resolve("./../g").toString());
        assertEquals("http://a/b/c/g/", base.resolve("./g/.").toString());
        assertEquals("http://a/b/c/g/h", base.resolve("g/./h").toString());
        assertEquals("http://a/b/c/h", base.resolve("g/../h").toString());
        assertEquals("http://a/b/c/g;x=1/y", base.resolve("g;x=1/./y").toString());
        assertEquals("http://a/b/c/y", base.resolve("g;x=1/../y").toString());
    }

    public void testRfc1808AbnormalExampleRelativeScheme() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        URI uri = base.resolve("http:g");
        assertEquals("http:g", uri.toString()); // this is an opaque URI
        assertEquals(true, uri.isOpaque());
        assertEquals(true, uri.isAbsolute());
    }

    public void testRfc1808AbnormalExampleQueryOrFragmentDots() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        assertEquals("http://a/b/c/g?y/./x", base.resolve("g?y/./x").toString());
        assertEquals("http://a/b/c/g?y/../x", base.resolve("g?y/../x").toString());
        assertEquals("http://a/b/c/g#s/./x", base.resolve("g#s/./x").toString());
        assertEquals("http://a/b/c/g#s/../x", base.resolve("g#s/../x").toString());
    }

    public void testSquareBracketsInUserInfo() throws Exception {
        try {
            new URI("http://user:[::1]@host");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testFileUriExtraLeadingSlashes() throws Exception {
        URI uri = new URI("file:////foo");
        assertEquals(null, uri.getAuthority());
        assertEquals("//foo", uri.getPath());
        assertEquals("file:////foo", uri.toString());
    }

    public void testFileUrlWithAuthority() throws Exception {
        URI uri = new URI("file://x/foo");
        assertEquals("x", uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("file://x/foo", uri.toString());
    }

    public void testEmptyAuthority() throws Exception {
        URI uri = new URI("http:///foo");
        assertEquals(null, uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("http:///foo", uri.toString());
    }

    public void testHttpUrlExtraLeadingSlashes() throws Exception {
        URI uri = new URI("http:////foo");
        assertEquals(null, uri.getAuthority());
        assertEquals("//foo", uri.getPath());
        assertEquals("http:////foo", uri.toString());
    }

    public void testFileUrlRelativePath() throws Exception {
        URI base = new URI("file:/a/b/c");
        assertEquals("file:/a/b/d", base.resolve("d").toString());
    }

    public void testFileUrlDottedPath() throws Exception {
        URI url = new URI("file:../a/b");
        assertTrue(url.isOpaque());
        assertNull(url.getPath());
    }

    /**
     * Regression test for http://b/issue?id=2604061
     */
    public void testParsingDotAsHostname() throws Exception {
        assertEquals(null, new URI("http://./").getHost());
    }

    public void testSquareBracketsWithIPv4() throws Exception {
        try {
            new URI("http://[192.168.0.1]/");
            fail();
        } catch (URISyntaxException e) {
        }
    }

    public void testSquareBracketsWithHostname() throws Exception {
        try {
            new URI("http://[google.com]/");
            fail();
        } catch (URISyntaxException e) {
        }
    }

    public void testIPv6WithoutSquareBrackets() throws Exception {
        assertEquals(null, new URI("http://fe80::1234/").getHost());
    }

    public void testEqualityWithNoPath() throws Exception {
        assertFalse(new URI("http://android.com").equals(new URI("http://android.com/")));
    }

    public void testRelativize() throws Exception {
        URI a = new URI("http://host/a/b");
        URI b = new URI("http://host/a/b/c");
        assertEquals("b/c", a.relativize(b).toString()); // RI assumes a directory
    }

    public void testParseServerAuthorityInvalidPortMinus() throws Exception {
        URI uri = new URI("http://host:-2/");
        assertEquals("host:-2", uri.getAuthority());
        assertNull(uri.getHost());
        assertEquals(-1, uri.getPort());
        try {
            uri.parseServerAuthority();
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testParseServerAuthorityInvalidPortPlus() throws Exception {
        URI uri = new URI("http://host:+2/");
        assertEquals("host:+2", uri.getAuthority());
        assertNull(uri.getHost());
        assertEquals(-1, uri.getPort());
        try {
            uri.parseServerAuthority();
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testParseServerAuthorityInvalidPortNonASCII() throws Exception {
        URI uri = new URI("http://host:١٢٣/"); // 123 in arabic
        assertEquals("host:١٢٣", uri.getAuthority());
        assertNull(uri.getHost());
        assertEquals(-1, uri.getPort());
        try {
            uri.parseServerAuthority();
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testParseServerAuthorityOmittedAuthority() throws Exception {
        URI uri = new URI("http:file");
        uri.parseServerAuthority(); // does nothing!
        assertNull(uri.getAuthority());
        assertNull(uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    public void testEncodingParts() throws Exception {
        URI uri = new URI("http", "user:pa55w?rd", "host", 80, "/doc|search",
                "q=green robots", "over 6\"");
        assertEquals("http", uri.getScheme());
        assertEquals("user:pa55w?rd@host:80", uri.getAuthority());
        assertEquals("user:pa55w%3Frd@host:80", uri.getRawAuthority());
        assertEquals("user:pa55w?rd", uri.getUserInfo());
        assertEquals("user:pa55w%3Frd", uri.getRawUserInfo());
        assertEquals("/doc|search", uri.getPath());
        assertEquals("/doc%7Csearch", uri.getRawPath());
        assertEquals("q=green robots", uri.getQuery());
        assertEquals("q=green%20robots", uri.getRawQuery());
        assertEquals("over 6\"", uri.getFragment());
        assertEquals("over%206%22", uri.getRawFragment());
        assertEquals("//user:pa55w?rd@host:80/doc|search?q=green robots",
                uri.getSchemeSpecificPart());
        assertEquals("//user:pa55w%3Frd@host:80/doc%7Csearch?q=green%20robots",
                uri.getRawSchemeSpecificPart());
        assertEquals("http://user:pa55w%3Frd@host:80/doc%7Csearch?q=green%20robots#over%206%22",
                uri.toString());
    }

    public void testSchemeCaseIsNotCanonicalized() throws Exception {
        URI uri = new URI("HTTP://host/path");
        assertEquals("HTTP", uri.getScheme());
    }

    public void testEmptyAuthorityWithPath() throws Exception {
        URI uri = new URI("http:///path");
        assertEquals(null, uri.getAuthority());
        assertEquals("/path", uri.getPath());
    }

    public void testEmptyAuthorityWithQuery() throws Exception {
        URI uri = new URI("http://?query");
        assertEquals(null, uri.getAuthority());
        assertEquals("", uri.getPath());
        assertEquals("query", uri.getQuery());
    }

    public void testEmptyAuthorityWithFragment() throws Exception {
        URI uri = new URI("http://#fragment");
        assertEquals(null, uri.getAuthority());
        assertEquals("", uri.getPath());
        assertEquals("fragment", uri.getFragment());
    }

    public void testEncodingConstructorsRefuseRelativePath() throws Exception {
        try {
            new URI("http", "host", "relative", null);
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http", "host", "relative", null, null);
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http", null, "host", -1, "relative", null, null);
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    public void testEncodingConstructorsAcceptEmptyPath() throws Exception {
        assertEquals("", new URI("http", "host", "", null).getPath());
        assertEquals("", new URI("http", "host", "", null, null).getPath());
        assertEquals("", new URI("http", null, "host", -1, "", null, null).getPath());
    }

    public void testResolveRelativeAndAbsolute() throws Exception {
        URI absolute = new URI("http://android.com/");
        URI relative = new URI("robots.txt");
        assertEquals(absolute, absolute.resolve(absolute));
        assertEquals(new URI("http://android.com/robots.txt"), absolute.resolve(relative));
        assertEquals(absolute, relative.resolve(absolute));
        assertEquals(relative, relative.resolve(relative));
    }

    public void testRelativizeRelativeAndAbsolute() throws Exception {
        URI absolute = new URI("http://android.com/");
        URI relative = new URI("robots.txt");
        assertEquals(relative, absolute.relativize(new URI("http://android.com/robots.txt")));
        assertEquals(new URI(""), absolute.relativize(absolute));
        assertEquals(relative, absolute.relativize(relative));
        assertEquals(absolute, relative.relativize(absolute));
        assertEquals(new URI(""), relative.relativize(relative));
    }

    public void testPartContainsSpace() throws Exception {
        try {
            new URI("ht tp://host/");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://user name@host/");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://ho st/");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://host:80 80/");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://host/fi le");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://host/file?que ry");
            fail();
        } catch (URISyntaxException expected) {
        }
        try {
            new URI("http://host/file?query#re f");
            fail();
        } catch (URISyntaxException expected) {
        }
    }

    // http://code.google.com/p/android/issues/detail?id=37577
    // http://b/18023709
    // http://b/17579865
    // http://b/18016625
    public void testUnderscore() throws Exception {
        URI uri = new URI("http://a_b.c.d.net/");
        assertEquals("a_b.c.d.net", uri.getAuthority());
        // The RFC's don't permit underscores in hostnames, but URI has to because
        // a certain large website doesn't seem to care about standards and specs.
        assertEquals("a_b.c.d.net", uri.getHost());
    }

    // RFC1034#section-3.5 doesn't permit empty labels in hostnames. This was accepted prior to N,
    // but returns null in later releases.
    // http://b/25991669
    // http://b/29560247
    public void testHostWithEmptyLabel() throws Exception {
        assertNull(new URI("http://.example.com/").getHost());
        assertNull(new URI("http://example..com/").getHost());
    }

    public void test_JDK7171415() {
        URI lower, mixed;
        lower = URI.create("http://www.example.com/%2b");
        mixed = URI.create("http://wWw.ExAmPlE.com/%2B");
        assertTrue(lower.equals(mixed));
        assertEquals(lower.hashCode(), mixed.hashCode());

        lower = URI.create("http://www.example.com/%2bbb");
        mixed = URI.create("http://wWw.ExAmPlE.com/%2BbB");
        assertFalse(lower.equals(mixed));
        assertFalse(lower.hashCode() == mixed.hashCode());
    }

    // Adding a new test? Consider adding an equivalent test to URLTest.java
}
