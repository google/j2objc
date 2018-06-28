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

package com.google.j2objc.util;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Utilities to interact with reflection-stripped code. */
public final class ReflectionUtil {

  /**
   * When reflection is stripped, the transpiled code uses NSStringFromClass to return the name of a
   * class. For example, instead of getting something like java.lang.Throwable, we get
   * JavaLangThrowable.
   *
   * <p>This method assumes that {@code actual} and {@code expected} contain a class name as prefix.
   * Firts, it compares directly {@code actual} to {@code expected}; if it fails, then it compares
   * {@actual} to the cammel case version of {@code expected}.
   */
  public static boolean matchClassNamePrefix(String actual, String expected) {
    if (actual.equals(expected)) {
      return true;
    }
    expected =
        Stream.of(expected.split("\\."))
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
            .collect(Collectors.joining());
    return actual.equals(expected);
  }

  /**
   * Transpiled code that directly acccess the serialVersionUID field when reflection is stripped
   * won't compile because this field is also stripped.
   *
   * <p>Accessing it via reflection allows the non-stripped code to keep the same behavior and
   * allows the stripped code to compile. Note that in the later case, a ReflectionStrippedError
   * will be thrown, this is OK because serialization code is not supported when reflection is
   * stripped.
   */
  public static long getSerialVersionUID(Class<? extends Serializable> clazz) {
    try {
      return clazz.getField("serialVersionUID").getLong(null);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      // ignored.
      return 0;
    }
  }
}
