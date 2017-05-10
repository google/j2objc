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

package libcore.java.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.ErrorManager;
import junit.framework.TestCase;

public class OldErrorManagerTest extends TestCase {

    private final PrintStream err = System.err;
    private final PrintStream out = System.out;

    public void tearDown() throws Exception{
        System.setErr(err);
        System.setOut(out);
        super.tearDown();
    }

    public void test_errorCheck() {
        ErrorManager em = new ErrorManager();
        MockStream aos = new MockStream();
        PrintStream st = new PrintStream(aos);
        System.setErr(st);
        System.setOut(st);
        em.error("supertest", null, ErrorManager.GENERIC_FAILURE);
        st.flush();
        assertTrue("message appears (supertest)", aos.getWrittenData().indexOf("supertest") != -1);
    }

    public class MockStream extends ByteArrayOutputStream {

        private StringBuffer linesWritten = new StringBuffer();

        public void flush() {}
        public  void close() {}

        @Override
        public void write(byte[] buffer) {
            linesWritten.append(new String(buffer));
        }

        @Override
        public synchronized void write(byte[] buffer, int offset, int len) {
            linesWritten.append(new String(buffer, offset, len));
        }

        public String getWrittenData() {return linesWritten.toString();}
    }
}
