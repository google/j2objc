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

package libcore.java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import junit.framework.TestCase;

public class JarExceptionTest extends TestCase {
    /**
     * java.util.jar.JarException#JarException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws Exception {
        try {
            new Manifest(new ByteArrayInputStream(
                    "jlkasj dl: dsklf jlks dslka : fdsfsd\n\n\n\ndsfas".getBytes()));
            fail("Should have thrown exception");
        } catch (IOException e) {
            // correct
        }
    }

    /**
     * java.util.jar.JarException#JarException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String1() throws Exception {
        assertEquals("Jar Exception", new JarException("Jar Exception").getMessage());
    }

    /**
     * java.util.jar.JarException#JarException()
     */
    public void test_Constructor_void() throws Exception {
        new JarException();
    }
}
