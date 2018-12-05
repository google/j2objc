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

public class MessageDigestTestMD5 extends MessageDigestTest {

    public MessageDigestTestMD5() {
        super("MD5");

        super.source1 = "abc";
        super.source2 = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu";
        super.expected1 = singleblock;
        super.expected2 = multiblock;
        super.expected3 = longmessage;
    }

    private static final String singleblock = "900150983cd24fb0d6963f7d28e17f72";
    private static final String multiblock = "03dd8807a93175fb062dfb55dc7d359c";
    private static final String longmessage = "7707d6ae4e027c70eea2a935c2296f21";

}
