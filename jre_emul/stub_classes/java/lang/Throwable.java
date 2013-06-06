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

/**
 * Stub implementation of java.lang.Throwable.
 *
 * @see java.lang.Object
 */
public class Throwable implements Serializable {

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
}
