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

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

/*
 * Stub implementation of java.lang.Throwable.
 *
 * @see java.lang.Object
 */

/**
 * The superclass of all classes which can be thrown by the VM. The
 * two direct subclasses are recoverable exceptions ({@code Exception}) and
 * unrecoverable errors ({@code Error}). This class provides common methods for
 * accessing a string message which provides extra information about the
 * circumstances in which the {@code Throwable} was created (basically an error
 * message in most cases), and for saving a stack trace (that is, a record of
 * the call stack at a particular point in time) which can be printed later.
 *
 * <p>A {@code Throwable} can also include a cause, which is a nested {@code
 * Throwable} that represents the original problem that led to this {@code
 * Throwable}. It is often used for wrapping various types of errors into a
 * common {@code Throwable} without losing the detailed original error
 * information. When printing the stack trace, the trace of the cause is
 * included.
 *
 * @see Error
 * @see Exception
 * @see RuntimeException
 */
@SuppressWarnings("unused")
public class Throwable implements Serializable {
  private String detailMessage;
  private Throwable cause;
  private StackTraceElement[] stackTrace;
  private List<Throwable> suppressedExceptions;

  private static final long serialVersionUID = -3042686055658047285L;

  public Throwable() {}

  public Throwable(String message) {}

  public Throwable(String message, Throwable cause) {}

  public Throwable(Throwable cause) {}

  protected Throwable(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {}

  public Throwable fillInStackTrace() {
    return null;
  }

  public Throwable getCause() {
    return null;
  }

  public String getLocalizedMessage() {
    return null;
  }

  public String getMessage() {
    return null;
  }

  public StackTraceElement[] getStackTrace() {
    return null;
  }

  public Throwable initCause(Throwable cause) {
    return null;
  }

  public void printStackTrace() {}

  public void printStackTrace(PrintWriter w) {}

  public void printStackTrace(PrintStream ps) {}

  public void setStackTrace(StackTraceElement[] stackTrace) {}

  public final void addSuppressed(Throwable exception) {}

  public final Throwable[] getSuppressed() {
    return null;
  }

  @Override
  public String toString() {
    return null;
  }
}
