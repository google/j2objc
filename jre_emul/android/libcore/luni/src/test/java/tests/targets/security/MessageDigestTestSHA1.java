/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.targets.security;

import tests.security.MessageDigestTest;

public class MessageDigestTestSHA1 extends MessageDigestTest {

    public MessageDigestTestSHA1() {
        super("SHA-1");

        super.source1 = "abc";
        super.source2 = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
        super.expected1 = singleblock;
        super.expected2 = multiblock;
        super.expected3 = longmessage;
    }

    // results from fips180-2
    private static final String singleblock = "a9993e364706816aba3e25717850c26c9cd0d89d";
    private static final String multiblock = "84983e441c3bd26ebaae4aa1f95129e5e54670f1";
    private static final String longmessage = "34aa973cd4c4daa4f61eeb2bdbad27316534016f";

}
