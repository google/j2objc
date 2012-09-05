/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.lang;

import java.io.PrintWriter;
import java.io.Serializable;

/*-{
#import <TargetConditionals.h>
#ifndef TARGET_OS_IPHONE
#import <NSExceptionHandler.h>
#endif

#import "NSException+StackTrace.h"
}-*/

/**
 * Modified version of GWT's java.lang.Throwable class to run on iOS.
 * 
 * NOTE: this class is NOT built by the JreEmulation project, since it
 * requires native constructor code (NSException classes cannot be initialized
 * with the default init message if they are to be caught by class name).  If 
 * changes are made to this class, translate it by hand, then merge the 
 * translated code with the JreEmulation/Classes/JavaLangThrowable.? files.
 */
public class Throwable implements Serializable {

  private transient Throwable cause;
  private String detailMessage;

  public Throwable() {
  }

  public Throwable(String message) {
    detailMessage = message;
  }

  public Throwable(String message, Throwable cause) {
    this.cause = cause;
    detailMessage = message;
  }

  public Throwable(Throwable cause) {
    detailMessage = (cause == null) ? null : cause.toString();
    this.cause = cause;
  }

  public Throwable fillInStackTrace() {
    return this;  // NSException already filled in the stack trace.
  }

  public Throwable getCause() {
    return cause;
  }

  public String getLocalizedMessage() {
    return getMessage();
  }

  public String getMessage() {
    return detailMessage;
  }

  public StackTraceElement[] getStackTrace() {
    throw new AssertionError("not implemented");
  }

  public Throwable initCause(Throwable cause) {
    if (this.cause != null) {
      throw new IllegalStateException("Can't overwrite cause");
    }
    if (cause == this) {
      throw new IllegalArgumentException("Self-causation not permitted");
    }
    this.cause = cause;
    return this;
  }

  public native void printStackTrace() /*-{
    [super printStackTrace];
  }-*/;

  public void printStackTrace(PrintWriter w) {
    // Not implemented until there is similar support in NSException+StackTrace.
  }

  public void setStackTrace(StackTraceElement[] stackTrace) {
    throw new AssertionError("not implemented");
  }

  @Override
  public String toString() {
    String className = this.getClass().getName();
    String msg = getMessage();
    if (msg != null) {
      return className + ": " + msg;
    } else {
      return className;
    }
  }
}
