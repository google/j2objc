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

package java.lang;

/*-[
#import <execinfo.h>
]-*/

/**
 * Simple iOS version of java.lang.StackTraceElement.
 *
 * @author Pankaj Kakkar
 */
public class StackTraceElement {

  private String className;
  private String methodName;
  private String fileName;
  private final int lineNumber;
  private long address;

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    initializeFromAddress();
    return methodName;
  }

  public String getFileName() {
    return fileName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public StackTraceElement(String className, String methodName, String fileName, int lineNumber) {
    this.className = className;
    this.methodName = methodName;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  StackTraceElement(long address) {
    this(null, null, null, -1);
    this.address = address;
  }

  public String toString() {
    initializeFromAddress();
    StringBuilder sb = new StringBuilder();
    if (className != null) {
      sb.append(className);
      sb.append('.');
    }
    if (methodName != null) {
      sb.append(methodName);
    }
    if (fileName != null || lineNumber != -1) {
      sb.append('(');
      if (fileName != null) {
        sb.append(fileName);
      }
      if (lineNumber != -1) {
        sb.append(':');
        sb.append(lineNumber);
      }
      sb.append(')');
    }
    return sb.toString();
  }

  /**
   * Implements lazy loading of symbol information from application.
   */
  private native void initializeFromAddress() /*-[
    if (address_ == 0L || methodName_) {
      return;
    }
    void *shortStack[1];
    shortStack[0] = (void *)address_;
    char **stackSymbol = backtrace_symbols(shortStack, 1);
    char *start = strstr(*stackSymbol, "0x");  // Skip text before address.
    methodName_ =
        RETAIN([NSString stringWithCString:start
                                  encoding:[NSString defaultCStringEncoding]]);
    free(stackSymbol);
  ]-*/;
}
