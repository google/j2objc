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

package libcore.java.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class OldFileReaderTest extends junit.framework.TestCase {

    FileReader br;

    public void test_ConstructorLjava_io_File() {
        File noFile = new File(System.getProperty("java.io.tmpdir"), "noreader.tst");
        try {
            br = new FileReader(noFile);
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        }
    }

    public void test_ConstructorLjava_lang_String() {
        try {
            br = new FileReader(System.getProperty("java.io.tmpdir") + "/noreader.tst");
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        }
    }
}
