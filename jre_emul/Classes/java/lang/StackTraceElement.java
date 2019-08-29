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

import java.io.Serializable;
import java.util.Objects;

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
public class StackTraceElement implements Serializable {

  public static final String STRIPPED = "<stripped>";

  private String declaringClass;
  private String methodName;
  private String fileName;
  private final int lineNumber;
  private transient long address;
  private transient String hexAddress;
  private transient String offset;

  public String getClassName() {
    initializeFromAddress();
    return declaringClass;
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
    this.declaringClass = Objects.requireNonNull(className, "Declaring class is null");
    this.methodName = Objects.requireNonNull(methodName, "Method name is null");
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  StackTraceElement(long address) {
    this.declaringClass = null;
    this.methodName = null;
    this.fileName = null;
    this.lineNumber = -1;
    this.address = address;
  }

  public String toString() {
    initializeFromAddress();
    boolean strippedClass = declaringClass.startsWith(STRIPPED);
    StringBuilder sb = new StringBuilder();
    sb.append(hexAddress);
    sb.append(" ");
    if (!strippedClass) {
      sb.append(declaringClass);
      sb.append('.');
    }
    if (!methodName.equals(STRIPPED)) {
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
    } else if (!strippedClass) {
      sb.append("()");
    }
    if (offset != null) {
      sb.append(" + ");
      sb.append(offset);
    }
    return sb.toString();
  }

  /**
   * Returns true if the specified object is another
   * {@code StackTraceElement} instance representing the same execution
   * point as this instance.  Two stack trace elements {@code a} and
   * {@code b} are equal if and only if:
   * <pre>
   *     equals(a.getFileName(), b.getFileName()) &&
   *     a.getLineNumber() == b.getLineNumber()) &&
   *     equals(a.getClassName(), b.getClassName()) &&
   *     equals(a.getMethodName(), b.getMethodName())
   * </pre>
   * where {@code equals} has the semantics of {@link
   * java.util.Objects#equals(Object, Object) Objects.equals}.
   *
   * @param  obj the object to be compared with this stack trace element.
   * @return true if the specified object is another
   *         {@code StackTraceElement} instance representing the same
   *         execution point as this instance.
   */
  public boolean equals(Object obj) {
      if (obj==this)
          return true;
      if (!(obj instanceof StackTraceElement))
          return false;
      StackTraceElement e = (StackTraceElement)obj;
      initializeFromAddress();
      e.initializeFromAddress();
      return e.declaringClass.equals(declaringClass) &&
          e.lineNumber == lineNumber &&
          Objects.equals(methodName, e.methodName) &&
          Objects.equals(fileName, e.fileName);
  }

  /**
   * Returns a hash code value for this stack trace element.
   */
  public int hashCode() {
      initializeFromAddress();
      int result = 31*declaringClass.hashCode() + methodName.hashCode();
      result = 31*result + Objects.hashCode(fileName);
      result = 31*result + lineNumber;
      return result;
  }

  private static final long serialVersionUID = 6992337162326171013L;

  /*-[
  static NSString *ExtractMethodName(
      char *rawName, char paramSeparator, NSStringEncoding encoding) {
    char *hasParamSeparator = strchr(rawName, paramSeparator);
    if (hasParamSeparator) {
      char *paramsStart = strstr(rawName, "With");
      if (paramsStart) {
        *paramsStart = '\0';
      }
    }
    if (strcmp(rawName, "init") == 0) {
      return @"<init>";
    }
    if (strcmp(rawName, "initialize") == 0) {
      return @"<clinit>";
    }
    return [[NSString alloc] initWithCString:rawName encoding:encoding];
  }
  ]-*/

  /*-[
  // This is based on undocumented details of the Swift compiler as reverse-engineered by several
  // sources online, and it doesn't attempt to be comprehensive, but tries to demangle the most
  // common type of object instance methods.
  static void DemangleSwiftMethod(
      JavaLangStackTraceElement *self, char *start) {
    // "_T" is a global Swift marker. "F" means this symbol refers to a function/method.
    if (0 != bcmp(start, "_TF", 3)) return;

    // Next comes a series of "C" to represent the declaring type in terms of nested classes.
    // Other non-decimal characters can appear here, but I'm not sure what they mean so we'll bail.
    start += 3;
    while (*start == 'C') {
      start++;
    }
    if (*start < '0' || *start > '9') return;

    // Next up is a series of length-prefixed names, starting with the module name, followed
    // by nested class names, and finally ending with the function name.
  #define MAX_SWIFT_NESTING 8
    NSMutableArray *names = [[NSMutableArray alloc] initWithCapacity:MAX_SWIFT_NESTING];
    char *lenEnd;
    BOOL ignoreName = NO;
    while (*start && [names count] < MAX_SWIFT_NESTING) {
      if (*start == 'P') {
        // Apparently private functions have a random(?) hexidecimal component preceding the real
        // name. It's marked by a 'P' prior to the length of that hexidecimal component.
        start++;
        ignoreName = YES;
      }
      else ignoreName = NO;

      long len = strtol(start, &lenEnd, 10);
      if (start == lenEnd) {
        break;
      }
      if (!ignoreName) {
        NSString *name = 
            [[NSString alloc] initWithBytes:lenEnd length:len encoding:NSASCIIStringEncoding];
        [names addObject:name];
        RELEASE_(name);
      }
      start = lenEnd + len;
    }
    if (start != lenEnd || [names count] < 2) {
      RELEASE_(names);
      return;
    }
    self->methodName_ = RETAIN_([names lastObject]);
    [names removeLastObject];
    self->declaringClass_ = RETAIN_([names componentsJoinedByString:@"."]);
    RELEASE_(names);
  }
  ]-*/

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
    NSStringEncoding encoding = [NSString defaultCStringEncoding];

    // Extract hexAddress.
    char *start = strstr(*stackSymbol, "0x");  // Skip text before address.
    char *addressEnd = strstr(start, " ");
    char *hex = strndup(start, addressEnd - start);
    self->hexAddress_ = [[NSString alloc] initWithCString:hex encoding:encoding];
    free(hex);
    start = addressEnd + 1;

    // Extract the offset if symbol looks like "<method> + 123".
    char *offset = strstr(start, " + ");
    if (offset) {
      self->offset_ = [[NSString alloc] initWithCString:offset + 3 encoding:encoding];
      *offset = '\0';
    }

    // See if a class and method names can be extracted.
    char *leftBrace = strchr(start, '[');
    char *rightBrace = strchr(start, ']');
    if (leftBrace && rightBrace && (rightBrace - leftBrace) > 0) {
      char *signature = leftBrace + 1;
      char *className = strsep(&signature, "[ ]");
      if (className && strlen(className) > 0) {
        IOSClass *cls = [IOSClass classForIosName:
            [NSString stringWithCString:className encoding:encoding]];
        if (cls) {
          self->declaringClass_ = RETAIN_([cls getName]);
        }
      }
      char *selector = strsep(&signature, "[ ]");
      if (selector) {
        self->methodName_ = ExtractMethodName(selector, ':', encoding);
      }
    } else {
      // Functionized method. Look for the class name portion.
      IOSClass *cls = nil;
      // Search backwards for '_' so that we find inner classes before their
      // outer class.
      char *idx = start;
      while (*idx) {
        idx++;
      }
      while (!cls) {
        while (--idx > start && *idx != '_');
        if (idx == start) {
          break;
        }
        NSString *className = [[[NSString alloc] initWithBytesNoCopy:start
                                                              length:idx - start
                                                            encoding:encoding
                                                        freeWhenDone:false] autorelease];
        cls = [IOSClass classForIosName:className];
      }
      if (cls) {
        self->declaringClass_ = RETAIN_([cls getName]);
        start = idx + 1;
      }
      else {
        // Try to demangle Swift symbol. If it succeeds, methodName_ and declaringClass_ will be
        // populated.
        DemangleSwiftMethod(self, start);
      }
      if (!self->methodName_) {
        self->methodName_ = ExtractMethodName(start, '_', encoding);
      }
    }
    if (!self->declaringClass_) {
      self->declaringClass_ = [[NSString alloc] initWithFormat:@"%@ %@",
          JavaLangStackTraceElement_STRIPPED, self->hexAddress_];
    }
    if (!self->methodName_) {
      self->methodName_ = JavaLangStackTraceElement_STRIPPED;
    }
    free(stackSymbol);
  ]-*/;
}
