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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.TestCase;

public class OldLogRecordTest extends TestCase {

    static final String MSG = "test msg, pls. ignore itb";

    private LogRecord lr = new LogRecord(Level.CONFIG, MSG);

    public void testGetSetTimeCheck() {
        long before = lr.getMillis();
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogRecord lr2 = new LogRecord(Level.CONFIG, "MSG2");
        long after = lr2.getMillis();
        assertTrue(after-before>0);
    }

    public void testGetSetLevelNormal() {
        assertSame(lr.getLevel(), Level.CONFIG);
        lr.setLevel(Level.ALL);
        assertSame(lr.getLevel(), Level.ALL);
        lr.setLevel(Level.FINEST);
        assertSame(lr.getLevel(), Level.FINEST);
    }

    public void testGetSetThreadID_DifferentThread() {
        lr.getThreadID();
        // Create and start the thread
        MockThread thread = new MockThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Create and start the thread2
        MockThread thread2 = new MockThread();
        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //All threadID must be different, based on the ThreadLocal.java ID
        assertTrue(lr.getThreadID() != thread.lr.getThreadID());
        assertTrue(lr.getThreadID() != thread2.lr.getThreadID());
        assertTrue(thread.lr.getThreadID() != thread2.lr.getThreadID());
    }

    public class MockThread extends Thread {
        public LogRecord lr = null; //will be update by the thread

        public void run() {
            update();
        }

        public synchronized void update(){
            lr = new LogRecord(Level.CONFIG, "msg thread");
        }
    }
}
