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
package org.apache.harmony.tests.javax.net.ssl;

import javax.net.ssl.SSLEngineResult;

import junit.framework.TestCase;

/**
 * Tests for SSLEngineResult.Status class
 *
 */
public class SSLEngineResultHandshakeStatusTest extends TestCase {

    /**
     * Test for <code> SSLEngineResult.HandshakeStatus.values() </code>
     */
    public void test_SSLEngineResultHandshakeStatus_values() {
        String[] str = {"NOT_HANDSHAKING", "FINISHED", "NEED_TASK", "NEED_WRAP", "NEED_UNWRAP"};
        SSLEngineResult.HandshakeStatus[] enS = SSLEngineResult.HandshakeStatus.values();
        if (enS.length == str.length) {
            for (int i = 0; i < enS.length; i++) {
                //System.out.println("enS[" + i + "] = " + enS[i]);
                assertEquals("Incorrect Status", enS[i].toString(), str[i]);
            }
        } else {
            fail("Incorrect number of enum constant was returned");
        }
    }

    /**
     * Test for <code> SSLEngineResult.HandshakeStatus.valueOf(String name) </code>
     */
    public void test_SSLEngineResultStatus_valueOf() {
        String[] str = {"FINISHED", "NEED_TASK", "NEED_UNWRAP", "NEED_WRAP", "NOT_HANDSHAKING"};
        String[] str_invalid = {"", "FINISHED1", "NEED_task", "NEED_UN",
                "NEED_WRAP_WRAP", "not_HANDSHAKING", "Bad string for verification valueOf method"};
        SSLEngineResult.HandshakeStatus enS;

        //Correct parameter
        for (int i = 0; i < str.length; i++) {
            try {
                enS = SSLEngineResult.HandshakeStatus.valueOf(str[i]);
                assertEquals("Incorrect Status", enS.toString(), str[i]);
            } catch (Exception e) {
                fail("Unexpected exception " + e + " was thrown for " + str[i]);
            }
        }

       //Incorrect parameter
        for (int i = 0; i < str_invalid.length; i++) {
            try {
                enS = SSLEngineResult.HandshakeStatus.valueOf(str_invalid[i]);
                fail("IllegalArgumentException should be thrown for " + str_invalid[i]);
            } catch (IllegalArgumentException iae) {
                //expected
            }
        }

        //Null parameter
        try {
            enS = SSLEngineResult.HandshakeStatus.valueOf(null);
            fail("NullPointerException/IllegalArgumentException should be thrown for NULL parameter");
        } catch (NullPointerException npe) {
            //expected
        } catch (IllegalArgumentException iae) {
        }
    }
}
