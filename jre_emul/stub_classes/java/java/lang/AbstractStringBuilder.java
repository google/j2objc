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

/**
 * Stub implementation of AbstractStringBuilder.
 *
 * @see Object
 */
abstract class AbstractStringBuilder implements Appendable, CharSequence {

  AbstractStringBuilder() {}
  AbstractStringBuilder(int i) {}

  public int length() {
    return 0;
  }

  public int capacity() {
    return 0;
  }

  public void ensureCapacity(int i) {}

  public void trimToSize() {}

  public void setLength(int i) {}

  public char charAt(int i) {
    return 0;
  }

  public int codePointAt(int i) {
    return 0;
  }

  public int codePointBefore(int i) {
    return 0;
  }

  public int codePointCount(int i, int j) {
    return 0;
  }

  public int offsetByCodePoints(int i, int j) {
    return 0;
  }

  public void getChars(int i, int j, char[] buf, int k) {}

  public void setCharAt(int i, char c) {}

  public String substring(int i) {
    return null;
  }

  public CharSequence subSequence(int i, int j) {
    return null;
  }

  public String substring(int i, int j) {
    return null;
  }

  public int indexOf(String s) {
    return 0;
  }

  public int indexOf(String s, int i) {
    return 0;
  }

  public int lastIndexOf(String s) {
    return 0;
  }

  public int lastIndexOf(String s, int i) {
    return 0;
  }

  public abstract String toString();

  final char[] getValue() {
    return null;
  }

  public AbstractStringBuilder append(char c) {
    return null;
  }

  public AbstractStringBuilder append(CharSequence s) {
    return null;
  }

  public AbstractStringBuilder append(CharSequence s, int start, int end) {
    return null;
  }
}
