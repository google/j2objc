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

import java.util.jar.JarException;
import junit.framework.TestCase;

public class OldJarExceptionTest extends TestCase {
    /**
     * java.util.jar.JarException#JarException(java.lang.String)
     */
    public void test_Constructor() throws Exception {
        JarException ex = new JarException();
        JarException ex1 = new JarException("Test string");
        JarException ex2 = new JarException(null);
        assertNotSame(ex, ex1);
        assertNotSame(ex.getMessage(), ex1.getMessage());
        assertNotSame(ex, ex2);
        assertSame(ex.getMessage(), ex2.getMessage());
    }

    public void test_ConstructorLjava_lang_String() throws Exception {
        JarException ex1 = new JarException("Test string");
        JarException ex2 = new JarException(null);
        assertNotSame(ex1, ex2);
        assertNotSame(ex1.getMessage(), ex2.getMessage());
    }
}
