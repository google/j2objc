/*
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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Tests for {@link SuperMethodInvocationRewriter}.
 *
 * @author Keith Stanger
 */
public class SuperMethodInvocationRewriterTest extends GenerationTest {

  public void testQualifiedSuperMethodInvocation() throws IOException {
    addSourceFile("class SuperClass { double foo(int i) { return 1.2; } }", "SuperClass.java");
    String translation = translateSourceFile(
        "class Test extends SuperClass { Runnable test() { return new Runnable() { "
        + "public void run() { Test.super.foo(1); } }; } }", "Test", "Test.m");
    // Declaration of the super function.
    assertTranslation(translation,
        "static jdouble (*Test_super$_fooWithInt_)(id, SEL, jint);");
    // Initialization of the super function.
    assertTranslation(translation,
        "Test_super$_fooWithInt_ = (jdouble (*)(id, SEL, jint))"
        + "[SuperClass instanceMethodForSelector:@selector(fooWithInt:)];");
    // Invocation of the super function.
    assertTranslation(translation,
        "Test_super$_fooWithInt_(this$0_, @selector(fooWithInt:), 1);");
  }

  public void testSuperFunctionInitializedBeforeStaticInit() throws IOException {
    String translation = translateSourceFile(
        "class Test { static Test instance = new Test(); Test() { super.toString(); } }",
        "Test", "Test.m");
    // Initialization of the super$ function pointer must occur before other
    // static initialization in case the super$ function is invoked during
    // static initialization.
    assertTranslatedLines(translation,
        "Test_super$_description = (id (*)(id, SEL))"
          + "[NSObject instanceMethodForSelector:@selector(description)];",
        "JreStrongAssignAndConsume(&Test_instance, new_Test_init());");
  }
}
