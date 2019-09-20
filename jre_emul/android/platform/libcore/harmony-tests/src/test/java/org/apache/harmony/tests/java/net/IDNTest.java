/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.tests.java.net;

import java.net.IDN;

import junit.framework.TestCase;

public class IDNTest extends TestCase {

    /**
     * {@link java.net.IDN#toASCII(String)}
     * @since 1.6
     */
    public void test_ToASCII_LString() {
        try {
            IDN.toASCII(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals("www.xn--gwtq9nb2a.jp", IDN
                .toASCII("www.\u65E5\u672C\u5E73.jp"));
        assertEquals(
                "www.xn--vckk7bxa0eza9ezc9d.com",
                IDN
                        .toASCII("www.\u30CF\u30F3\u30C9\u30DC\u30FC\u30EB\u30B5\u30E0\u30BA.com"));
        assertEquals("www.xn--frgbolaget-q5a.nu", IDN
                .toASCII("www.f\u00E4rgbolaget.nu"));
        assertEquals("www.xn--bcher-kva.de", IDN.toASCII("www.b\u00FCcher.de"));
        assertEquals("www.xn--brndendekrlighed-vobh.com", IDN
                .toASCII("www.br\u00E6ndendek\u00E6rlighed.com"));
        assertEquals("www.xn--rksmrgs-5wao1o.se", IDN
                .toASCII("www.r\u00E4ksm\u00F6rg\u00E5s.se"));
        assertEquals("www.xn--9d0bm53a3xbzui.com", IDN
                .toASCII("www.\uC608\uBE44\uAD50\uC0AC.com"));
        assertEquals("xn--lck1c3crb1723bpq4a.com", IDN
                .toASCII("\u7406\u5BB9\u30CA\u30AB\u30E0\u30E9.com"));
        assertEquals("xn--l8je6s7a45b.org", IDN
                .toASCII("\u3042\u30FC\u308B\u3044\u3093.org"));
        assertEquals("www.xn--frjestadsbk-l8a.net", IDN
                .toASCII("www.f\u00E4rjestadsbk.net"));
        assertEquals("www.xn--mkitorppa-v2a.edu", IDN
                .toASCII("www.m\u00E4kitorppa.edu"));
    }

    /**
     * {@link java.net.IDN#toASCII(String, int)}
     * @since 1.6
     */
    public void test_ToASCII_LString_I() {
        try {
            IDN.toASCII("www.br\u00E6ndendek\u00E6rlighed.com",
                    IDN.USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            IDN.toASCII("www.r\u00E4ksm\u00F6rg\u00E5s.se",
                    IDN.USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            IDN.toASCII("www.f\u00E4rjestadsbk.net", IDN.ALLOW_UNASSIGNED
                    | IDN.USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException e) {
            // expected
        }

        assertEquals("www.xn--gwtq9nb2a.jp", IDN.toASCII(
                "www.\u65E5\u672C\u5E73.jp", 0));
        assertEquals(
                "www.xn--vckk7bxa0eza9ezc9d.com",
                IDN
                        .toASCII(
                                "www.\u30CF\u30F3\u30C9\u30DC\u30FC\u30EB\u30B5\u30E0\u30BA.com",
                                0));
        assertEquals("www.xn--frgbolaget-q5a.nu", IDN.toASCII(
                "www.f\u00E4rgbolaget.nu", IDN.ALLOW_UNASSIGNED));
        assertEquals("www.xn--bcher-kva.de", IDN.toASCII("www.b\u00FCcher.de",
                IDN.ALLOW_UNASSIGNED));
        assertEquals("www.google.com", IDN.toASCII("www.google\u002Ecom",
                IDN.USE_STD3_ASCII_RULES));
    }

    /**
     * {@link java.net.IDN#toUnicode(String)}
     * @since 1.6
     */
    public void test_ToUnicode_LString() {
        try {
            IDN.toUnicode(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals("", IDN.toUnicode(""));
        assertEquals("www.bcher.de", IDN.toUnicode("www.bcher.de"));
        assertEquals("www.b\u00FCcher.de", IDN.toUnicode("www.b\u00FCcher.de"));
        assertEquals("www.\u65E5\u672C\u5E73.jp", IDN
                .toUnicode("www.\u65E5\u672C\u5E73.jp"));
        assertEquals("www.\u65E5\u672C\u5E73.jp", IDN.toUnicode("www\uFF0Exn--gwtq9nb2a\uFF61jp"));
        assertEquals("www.\u65E5\u672C\u5E73.jp", IDN.toUnicode("www.xn--gwtq9nb2a.jp"));
    }

    /**
     * {@link java.net.IDN#toUnicode(String, int)}
     * @since 1.6
     */
    public void test_ToUnicode_LString_I() {
        assertEquals("", IDN.toUnicode("", IDN.ALLOW_UNASSIGNED));
        assertEquals("www.f\u00E4rgbolaget.nu", IDN.toUnicode(
                "www.f\u00E4rgbolaget.nu", IDN.USE_STD3_ASCII_RULES));
        assertEquals("www.r\u00E4ksm\u00F6rg\u00E5s.nu", IDN.toUnicode(
                "www.r\u00E4ksm\u00F6rg\u00E5s\u3002nu",
                IDN.USE_STD3_ASCII_RULES));
        // RI bug. It cannot parse "www.xn--gwtq9nb2a.jp" when
        // USE_STD3_ASCII_RULES is set.
        assertEquals("www.\u65E5\u672C\u5E73.jp", IDN.toUnicode(
                "www\uFF0Exn--gwtq9nb2a\uFF61jp", IDN.USE_STD3_ASCII_RULES));

    }
}
