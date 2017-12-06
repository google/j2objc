/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.support;

import java.util.HashMap;

/**
 * Golden data for Message Digest related tests.<br>
 * Encapsulates the following data:<br>
 * - reference message<br>
 * - reference message digests calculated using
 *   BEA JRockit j2sdk1.4.2_04 (http://www.bea.com)
 *   for various MD algorithms:
 *   SHA-1, SHA-256, SHA-384, SHA-512, MD-5.
 * Standard algorithm names are defined in
 * "JavaTM Cryptography Architecture API Specification & Reference"
 *
 */
public class MDGoldenData {
    // The length of test message
    private static final int MY_MESSAGE_LEN = 1024;
    // Test message for digest computations
    private static final byte[] myMessage = new byte[MY_MESSAGE_LEN];
    // Reference digests for various algorithms calculated
    // for <code>myMessage</code>
    private static final HashMap<String, byte[]> goldenData = new HashMap<String, byte[]>();

    static {
        // fill myMessage
        for (int i=0; i<myMessage.length; i++) {
            myMessage[i] = (byte)i;
        }
        // fill goldenData
        // digest updated with myMessage bytes
        goldenData.put("SHA-1", new byte[] {
                (byte)0x5b, (byte)0x00, (byte)0x66, (byte)0x9c,
                (byte)0x48, (byte)0x0d, (byte)0x5c, (byte)0xff,
                (byte)0xbd, (byte)0xfa, (byte)0x8b, (byte)0xdb,
                (byte)0xa9, (byte)0x95, (byte)0x61, (byte)0x16,
                (byte)0x0f, (byte)0x2d, (byte)0x1b, (byte)0x77
        });
        // digest without updates at all;
        // use MD algorithm name + "_NU" if not updated MD value is needed
        goldenData.put("SHA-1_NU", new byte[] {
                (byte)0xda, (byte)0x39, (byte)0xa3, (byte)0xee,
                (byte)0x5e, (byte)0x6b, (byte)0x4b, (byte)0x0d,
                (byte)0x32, (byte)0x55, (byte)0xbf, (byte)0xef,
                (byte)0x95, (byte)0x60, (byte)0x18, (byte)0x90,
                (byte)0xaf, (byte)0xd8, (byte)0x07, (byte)0x09
        });

        goldenData.put("SHA", goldenData.get("SHA-1"));
        goldenData.put("SHA_NU", goldenData.get("SHA-1_NU"));

        goldenData.put("SHA1", goldenData.get("SHA-1"));
        goldenData.put("SHA1_NU", goldenData.get("SHA-1_NU"));

        goldenData.put("SHA-256", new byte[] {
                (byte)0x78, (byte)0x5b, (byte)0x07, (byte)0x51,
                (byte)0xfc, (byte)0x2c, (byte)0x53, (byte)0xdc,
                (byte)0x14, (byte)0xa4, (byte)0xce, (byte)0x3d,
                (byte)0x80, (byte)0x0e, (byte)0x69, (byte)0xef,
                (byte)0x9c, (byte)0xe1, (byte)0x00, (byte)0x9e,
                (byte)0xb3, (byte)0x27, (byte)0xcc, (byte)0xf4,
                (byte)0x58, (byte)0xaf, (byte)0xe0, (byte)0x9c,
                (byte)0x24, (byte)0x2c, (byte)0x26, (byte)0xc9
        });
        goldenData.put("SHA-256_NU", new byte[] {
                (byte)0xe3, (byte)0xb0, (byte)0xc4, (byte)0x42,
                (byte)0x98, (byte)0xfc, (byte)0x1c, (byte)0x14,
                (byte)0x9a, (byte)0xfb, (byte)0xf4, (byte)0xc8,
                (byte)0x99, (byte)0x6f, (byte)0xb9, (byte)0x24,
                (byte)0x27, (byte)0xae, (byte)0x41, (byte)0xe4,
                (byte)0x64, (byte)0x9b, (byte)0x93, (byte)0x4c,
                (byte)0xa4, (byte)0x95, (byte)0x99, (byte)0x1b,
                (byte)0x78, (byte)0x52, (byte)0xb8, (byte)0x55
        });
        goldenData.put("SHA-384", new byte[] {
                (byte)0x55, (byte)0xfd, (byte)0x17, (byte)0xee,
                (byte)0xb1, (byte)0x61, (byte)0x1f, (byte)0x91,
                (byte)0x93, (byte)0xf6, (byte)0xac, (byte)0x60,
                (byte)0x02, (byte)0x38, (byte)0xce, (byte)0x63,
                (byte)0xaa, (byte)0x29, (byte)0x8c, (byte)0x2e,
                (byte)0x33, (byte)0x2f, (byte)0x04, (byte)0x2b,
                (byte)0x80, (byte)0xc8, (byte)0xf6, (byte)0x91,
                (byte)0xf8, (byte)0x00, (byte)0xe4, (byte)0xc7,
                (byte)0x50, (byte)0x5a, (byte)0xf2, (byte)0x0c,
                (byte)0x1a, (byte)0x86, (byte)0xa3, (byte)0x1f,
                (byte)0x08, (byte)0x50, (byte)0x45, (byte)0x87,
                (byte)0x39, (byte)0x5f, (byte)0x08, (byte)0x1f
        });
        goldenData.put("SHA-384_NU", new byte[] {
                (byte)0x38, (byte)0xb0, (byte)0x60, (byte)0xa7,
                (byte)0x51, (byte)0xac, (byte)0x96, (byte)0x38,
                (byte)0x4c, (byte)0xd9, (byte)0x32, (byte)0x7e,
                (byte)0xb1, (byte)0xb1, (byte)0xe3, (byte)0x6a,
                (byte)0x21, (byte)0xfd, (byte)0xb7, (byte)0x11,
                (byte)0x14, (byte)0xbe, (byte)0x07, (byte)0x43,
                (byte)0x4c, (byte)0x0c, (byte)0xc7, (byte)0xbf,
                (byte)0x63, (byte)0xf6, (byte)0xe1, (byte)0xda,
                (byte)0x27, (byte)0x4e, (byte)0xde, (byte)0xbf,
                (byte)0xe7, (byte)0x6f, (byte)0x65, (byte)0xfb,
                (byte)0xd5, (byte)0x1a, (byte)0xd2, (byte)0xf1,
                (byte)0x48, (byte)0x98, (byte)0xb9, (byte)0x5b
        });
        goldenData.put("SHA-512", new byte[] {
                (byte)0x37, (byte)0xf6, (byte)0x52, (byte)0xbe,
                (byte)0x86, (byte)0x7f, (byte)0x28, (byte)0xed,
                (byte)0x03, (byte)0x32, (byte)0x69, (byte)0xcb,
                (byte)0xba, (byte)0x20, (byte)0x1a, (byte)0xf2,
                (byte)0x11, (byte)0x2c, (byte)0x2b, (byte)0x3f,
                (byte)0xd3, (byte)0x34, (byte)0xa8, (byte)0x9f,
                (byte)0xd2, (byte)0xf7, (byte)0x57, (byte)0x93,
                (byte)0x8d, (byte)0xde, (byte)0xe8, (byte)0x15,
                (byte)0x78, (byte)0x7c, (byte)0xc6, (byte)0x1d,
                (byte)0x6e, (byte)0x24, (byte)0xa8, (byte)0xa3,
                (byte)0x33, (byte)0x40, (byte)0xd0, (byte)0xf7,
                (byte)0xe8, (byte)0x6f, (byte)0xfc, (byte)0x05,
                (byte)0x88, (byte)0x16, (byte)0xb8, (byte)0x85,
                (byte)0x30, (byte)0x76, (byte)0x6b, (byte)0xa6,
                (byte)0xe2, (byte)0x31, (byte)0x62, (byte)0x0a,
                (byte)0x13, (byte)0x0b, (byte)0x56, (byte)0x6c
        });
        goldenData.put("SHA-512_NU", new byte[] {
                (byte)0xcf, (byte)0x83, (byte)0xe1, (byte)0x35,
                (byte)0x7e, (byte)0xef, (byte)0xb8, (byte)0xbd,
                (byte)0xf1, (byte)0x54, (byte)0x28, (byte)0x50,
                (byte)0xd6, (byte)0x6d, (byte)0x80, (byte)0x07,
                (byte)0xd6, (byte)0x20, (byte)0xe4, (byte)0x05,
                (byte)0x0b, (byte)0x57, (byte)0x15, (byte)0xdc,
                (byte)0x83, (byte)0xf4, (byte)0xa9, (byte)0x21,
                (byte)0xd3, (byte)0x6c, (byte)0xe9, (byte)0xce,
                (byte)0x47, (byte)0xd0, (byte)0xd1, (byte)0x3c,
                (byte)0x5d, (byte)0x85, (byte)0xf2, (byte)0xb0,
                (byte)0xff, (byte)0x83, (byte)0x18, (byte)0xd2,
                (byte)0x87, (byte)0x7e, (byte)0xec, (byte)0x2f,
                (byte)0x63, (byte)0xb9, (byte)0x31, (byte)0xbd,
                (byte)0x47, (byte)0x41, (byte)0x7a, (byte)0x81,
                (byte)0xa5, (byte)0x38, (byte)0x32, (byte)0x7a,
                (byte)0xf9, (byte)0x27, (byte)0xda, (byte)0x3e
        });
        goldenData.put("MD5", new byte[] {
                (byte)0xb2, (byte)0xea, (byte)0x9f, (byte)0x7f,
                (byte)0xce, (byte)0xa8, (byte)0x31, (byte)0xa4,
                (byte)0xa6, (byte)0x3b, (byte)0x21, (byte)0x3f,
                (byte)0x41, (byte)0xa8, (byte)0x85, (byte)0x5b
        });
        goldenData.put("MD5_NU", new byte[] {
                (byte)0xd4, (byte)0x1d, (byte)0x8c, (byte)0xd9,
                (byte)0x8f, (byte)0x00, (byte)0xb2, (byte)0x04,
                (byte)0xe9, (byte)0x80, (byte)0x09, (byte)0x98,
                (byte)0xec, (byte)0xf8, (byte)0x42, (byte)0x7e
        });
    }

    // No need to instantiate
    private MDGoldenData() {
    }

    /**
     * Returns reference message
     *
     * @return reference message
     */
    public static byte[] getMessage() {
        return myMessage.clone();
    }

    /**
     * Returns digest golden data
     *
     * @param key
     *  MD algorithm name or MD algorithm name + "_NU" if
     *  not updated MD value requested
     * @return
     *  reference digest for specified MD algorithm name
     */
    public static byte[] getDigest(String key) {
        return ((byte[])goldenData.get(key)).clone();
    }

}
