/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

/**
 * Command-line tests for java.lang.Throwable support
 *
 * @author Keith Stanger
 */
public class ThrowableTest extends TestCase {

  public void testGetMessage() throws Exception {
    Throwable t = new Throwable("themessage");
    assertEquals("themessage", t.getMessage());
  }

  public void testGetStackTrace() throws Exception {
    Throwable t = new Throwable();
    StackTraceElement[] stackTrace = t.getStackTrace();
    boolean foundSelf = false;
    for (StackTraceElement element : stackTrace) {
      if (element.getMethodName().contains("testGetStackTrace")) {
        foundSelf = true;
      }
    }
    assertTrue("Did not find this method in the stack trace.", foundSelf);
  }

  public void testSetStackTrace() throws Exception {
    StackTraceElement[] stackTraceIn = new StackTraceElement[1];
    stackTraceIn[0] = new StackTraceElement("class", "method", "file", 42);
    Throwable t = new Throwable();
    t.setStackTrace(stackTraceIn);
    StackTraceElement[] stackTraceOut = t.getStackTrace();
    assertEquals(1, stackTraceOut.length);
    assertEquals("class", stackTraceOut[0].getClassName());
    assertEquals("method", stackTraceOut[0].getMethodName());
    assertEquals("file", stackTraceOut[0].getFileName());
    assertEquals(42, stackTraceOut[0].getLineNumber());
  }

  public void testStackTraceWithPrintStream() throws Exception {
    Exception testException = new Exception("test exception");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    testException.printStackTrace(out);
    out.flush();
    String trace = baos.toString("UTF-8");
    assertTrue(trace.contains("com.google.j2objc.ThrowableTest.testStackTraceWithPrintStream("));
  }

  public void testStackTraceWithPrintWriter() throws Exception {
    Exception testException = new Exception("test exception");
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    testException.printStackTrace(out);
    out.flush();
    String trace = sw.toString();
    assertTrue(trace.contains("com.google.j2objc.ThrowableTest.testStackTraceWithPrintWriter("));
  }
}
