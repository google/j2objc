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

package libcore.java.net;

import java.net.PasswordAuthentication;
import junit.framework.TestCase;

public class OldPasswordAuthenticationTest extends TestCase {

    public void test_ConstructorLjava_lang_String$C() {
        String name = "name";
        char[] password = "hunter2".toCharArray();
        try {
            new PasswordAuthentication(name, null);
            fail("NullPointerException was not thrown.");
        } catch (NullPointerException npe) {
            //expected
        }

        PasswordAuthentication pa = new PasswordAuthentication(null, password);
        assertNull(pa.getUserName());
        assertEquals(password.length, pa.getPassword().length);
    }
}
