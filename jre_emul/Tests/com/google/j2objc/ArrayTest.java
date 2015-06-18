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

import junit.framework.TestCase;

import java.lang.reflect.Array;

/**
 * Functional tests for correct behavior of arrays.
 *
 * @author Keith Stanger
 */
public class ArrayTest extends TestCase {

  public void testArrayNewInstanceVararg() {
    Object arr = Array.newInstance(int.class, new int[] { 3 });
    assertEquals(3, Array.getLength(arr));
  }
}
