/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.tests.java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.spi.AbstractInterruptibleChannel;

import junit.framework.TestCase;

public class AbstractInterruptibleChannelTest extends TestCase {
    
    /**
     * @tests AbstractInterruptibleChannel#close()
     */
    public void test_close() throws IOException {
        MockInterruptibleChannel testMiChannel = new MockInterruptibleChannel();
        assertTrue(testMiChannel.isOpen());
        testMiChannel.isImplCloseCalled = false;
        testMiChannel.close();
        assertTrue(testMiChannel.isImplCloseCalled);
        assertFalse(testMiChannel.isOpen());
    }

    /**
     * @tests AbstractInterruptibleChannel#begin/end()
     */
    public void test_begin_end() throws IOException {
        boolean complete = false;
        MockInterruptibleChannel testChannel = new MockInterruptibleChannel();
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }

        try {
            testChannel.superBegin();
            complete = false;
        } finally {
            testChannel.superEnd(complete);
        }
        
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        
        testChannel.superBegin();
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertTrue(testChannel.isOpen());
        testChannel.close();
    }

    /**
     * @tests AbstractInterruptibleChannel#close/begin/end()
     */
    public void test_close_begin_end() throws IOException {
        boolean complete = false;
        MockInterruptibleChannel testChannel = new MockInterruptibleChannel();
        assertTrue(testChannel.isOpen());
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertTrue(testChannel.isOpen());
        testChannel.close();
        try {
            testChannel.superBegin();
            complete = false;
        } finally {
            try {
                testChannel.superEnd(complete);
                fail("should throw AsynchronousCloseException");
            } catch (AsynchronousCloseException e) {
                // expected
            }
        }
        assertFalse(testChannel.isOpen());
        try {
            testChannel.superBegin();
            complete = true;
        } finally {
            testChannel.superEnd(complete);
        }
        assertFalse(testChannel.isOpen());
    }
    
    private class MockInterruptibleChannel extends AbstractInterruptibleChannel {

        private boolean isImplCloseCalled = false;
        
        public MockInterruptibleChannel() {
            super();
        }

        protected void implCloseChannel() throws IOException {
        	isImplCloseCalled = true; 
        }

        // call super.begin() for test
        void superBegin() {
            super.begin();
        }
        
        // call super.end() for test
        void superEnd(boolean completed) throws AsynchronousCloseException {
            super.end(completed);
        }
    }
}
