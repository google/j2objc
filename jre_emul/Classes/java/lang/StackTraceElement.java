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
#import "IOSClass.h"
#import "java/lang/ClassNotFoundException.h"

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
  private String hexAddress;
  private String offset;

  public String getClassName() {
    initializeFromAddress();
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
    sb.append(hexAddress);
    sb.append(" ");
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
    } else if (className != null) {
      sb.append("()");
    }
    if (offset != null) {
      sb.append(" + ");
      sb.append(offset);
    }
    return sb.toString();
  }

  /**
   * Implements lazy loading of symbol information from application.
   */
  private native void initializeFromAddress() /*-[
    if (self->address_ == 0L || self->methodName_) {
      return;
    }
    void *shortStack[1];
    shortStack[0] = (void *)self->address_;
    char **stackSymbol = backtrace_symbols(shortStack, 1);

    // Extract hexAddress.
    char *start = strstr(*stackSymbol, "0x");  // Skip text before address.
    char *addressEnd = strstr(start, " ");
    char *hex = strndup(start, addressEnd - start);
    self->hexAddress_ = [[NSString alloc] initWithCString:hex
                                                 encoding:[NSString defaultCStringEncoding]];
    free(hex);
    start = addressEnd + 1;

    // See if a class and method names can be extracted.
    char *leftBrace = strchr(start, '[');
    char *rightBrace = strchr(start, ']');
    if (rightBrace && strlen(rightBrace) > 4) {  // If pattern is similar to: ...] + 123
      // Save trailing function address offset, then "remove" it.
      self->offset_ = [[NSString alloc] initWithCString:rightBrace + 4
                                               encoding:[NSString defaultCStringEncoding]];
      *(rightBrace + 1) = '\0';
    }
    if (leftBrace && rightBrace && (rightBrace - leftBrace) > 0) {
      char *signature = leftBrace + 1;
      char *className = strsep(&signature, "[ ]");
      if (className && strlen(className) > 0) {
        IOSClass *cls = [IOSClass classForIosName:[NSString stringWithCString:className
            encoding:[NSString defaultCStringEncoding]]];
        if (cls) {
          self->className__ = RETAIN_([cls getName]);
        }
      }
      char *selector = strsep(&signature, "[ ]");
      if (selector) {
        char *methodName = NULL;

        // Strip all parameter type mangling.
        char *colon = strchr(selector, ':');
        if (colon) {
          if (strlen(selector) > 8 &&
              strncmp(selector, "initWith", 8) == 0) {
            methodName = "<init>";
          } else {
            char *paramsStart = strstr(selector, "With");
            if (paramsStart) {
              *paramsStart = '\0';
            }
            methodName = selector;
          }
        } else if (strcmp(selector, "init") == 0) {
          methodName = "<init>";
        } else if (strcmp(selector, "initialize") == 0) {
          methodName = "<clinit>";
        } else {
          methodName = selector;
        }
        if (methodName) {
          self->methodName_ = [[NSString alloc] initWithCString:methodName
                                                       encoding:[NSString defaultCStringEncoding]];
        }
      }
    } else {
      // Copy rest of stack symbol to methodName.
      self->methodName_ = [[NSString alloc] initWithCString:start
                                                   encoding:[NSString defaultCStringEncoding]];
    }
    free(stackSymbol);
  ]-*/;
}
