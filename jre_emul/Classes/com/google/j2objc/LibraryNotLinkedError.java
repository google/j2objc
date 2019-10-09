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

package com.google.j2objc;

/**
 * Thrown when a class is requested that is part of a J2ObjC JRE library which wasn't linked into
 * the application.
 */
public class LibraryNotLinkedError extends Error {

  private static final String EXCEPTION_MESSAGE =
      "%s is unavailable. Fix this by:\n"
          + "1) If linking with -ObjC, add -l%s to the link flags.\n"
          + "2) If linking without -ObjC, call \"%s()\" (Swift) or\n"
          + "   \"%s_class_()\" (Objective C) to add a build dependency.";

  private static final long serialVersionUID = -4102976930308040481L;

  /**
   * Create a new LibraryNotLinkedException.
   *
   * @param functionalName the Java functionality that was requested
   * @param libraryName the name of the J2ObjC JRE library that was not linked
   * @param dependencyClassName the class to statically reference to force linking
   */
  public LibraryNotLinkedError(
      String functionalName, String libraryName, String dependencyClassName) {
    super(
        String.format(
            EXCEPTION_MESSAGE,
            functionalName,
            libraryName,
            dependencyClassName,
            dependencyClassName));
  }

  /**
   * Create a new LibraryNotLinkedException.
   *
   * @param functionalName the Java functionality that was requested
   * @param libraryName the name of the J2ObjC JRE library that was not linked
   * @param dependencyClassName the class to statically reference to force linking
   * @param addedText text to be appended to the exception message
   * @param args any values to be used when formatting the addedText string.
   */
  public LibraryNotLinkedError(
      String functionalName,
      String libraryName,
      String dependencyClassName,
      String addedText,
      Object... args) {
    super(
        String.format(
                EXCEPTION_MESSAGE,
                functionalName,
                libraryName,
                dependencyClassName,
                dependencyClassName)
            + '\n'
            + String.format(addedText, args));
  }
}
