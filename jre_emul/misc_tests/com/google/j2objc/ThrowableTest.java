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

import com.google.j2objc.util.ReflectionUtil;
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

  static final String NSEXCEPTION_MESSAGE = "native exception";

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

  public void testEquals() {
    Throwable t1 = new Throwable();
    Throwable t2 = new Throwable();
    assertFalse(t1.equals(t2));
  }

  public void testHashCode() {
    Throwable t1 = new Throwable();
    Throwable t2 = new Throwable();
    assertFalse(t1.hashCode() == t2.hashCode());
  }

  public void testNotWritableStackTrace() throws Exception {
    Throwable t = new Throwable("test", null, true, false) {};
    StackTraceElement[] stackTrace = t.getStackTrace();
    assertNotNull(stackTrace);
    assertEquals(0, stackTrace.length);

    // Should be a no-op.
    t.fillInStackTrace();
    stackTrace = t.getStackTrace();
    assertNotNull(stackTrace);
    assertEquals(0, stackTrace.length);

    // Also should be a no-op.
    StackTraceElement[] newTrace = new Throwable().getStackTrace();
    assertTrue(newTrace.length > 0);
    t.setStackTrace(newTrace);
    stackTrace = t.getStackTrace();
    assertNotNull(stackTrace);
    assertEquals(0, stackTrace.length);
  }

  public void testThrowableToStringFormat() {
    String expected = "java.lang.Throwable";
    assertTrue(ReflectionUtil.matchClassNamePrefix(new Throwable().toString(), expected));
    expected += ": oops";
    assertTrue(ReflectionUtil.matchClassNamePrefix(new Throwable("oops").toString(), expected));
  }

  public void testNSExceptionDescriptionUnchanged() {
    assertEquals(NSEXCEPTION_MESSAGE, getNSExceptionDescription());
  }

  // Verify [NSException description] only returns the exception's reason, not
  // with the class name like java.lang.Throwable.toString() does.
  static native String getNSExceptionDescription() /*-[
    NSException *e = AUTORELEASE(
        [[NSException alloc] initWithName:@"MyException"
                                   reason:ComGoogleJ2objcThrowableTest_NSEXCEPTION_MESSAGE
                                 userInfo:nil]);
    return [e description];
  ]-*/;

  /* TODO(kstanger): Make this test pass.
  public void testOverwriteNullCause() throws Exception {
    Throwable t = new Throwable((Throwable) null);
    try {
      t.initCause(new Throwable());
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected.
    }
  }*/
}
