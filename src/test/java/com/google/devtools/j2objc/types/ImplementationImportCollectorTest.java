/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Unit tests for the {@link ImplementationImportCollector} class.
 *
 * @author Tom Ball
 */
public class ImplementationImportCollectorTest extends GenerationTest {

  // Verify that invoked method's return value has associated header.
  public void testMethodReturnHasHeader() throws IOException {
    addSourceFile("class FooException extends Exception { AssertionError asAssertion() { " +
        "return new AssertionError(this); }}", "FooException.java");
    addSourceFile(
        "class FooMaker { static FooException makeException() { return new FooException(); }}",
        "FooMaker.java");
    String translation = translateSourceFile(
        "class A { void test() { " +
        "throw FooMaker.makeException().asAssertion(); }}", "A", "A.m");
    assertTranslation(translation, "#import \"FooException.h\"");
  }

  public void testVarargsMethodNoActualArguments() throws IOException {
    translateSourceFile(
        "class Test { Test(String ... values) { } Test test = new Test(); }",
        "Test", "Test.m");
    // Nothing to do; Successful translation is the test.
  }

  public void testVarargsMethodManyArguments() throws IOException {
    translateSourceFile(
        "class Test { Test(int... values) { } Test test = new Test(1,2,3); }",
        "Test", "Test.m");
    // Nothing to do; Successful translation is the test.
  }
}
