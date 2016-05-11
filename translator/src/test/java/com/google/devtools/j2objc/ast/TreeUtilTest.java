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

package com.google.devtools.j2objc.ast;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.BindingUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests for {@link TreeUtil}.
 */
public class TreeUtilTest extends GenerationTest {

  public void testSortMethods() throws IOException {
    String source = "class A {"
        + "void zebra() {}"
        + "void gnu(String s, int i, Runnable r) {}"
        + "A(int i) {}"
        + "void gnu() {}"
        + "void gnu(int i, Runnable r) {}"
        + "void yak() {}"
        + "A(String s) {}"
        + "A() {}"
        + "A(int i, Runnable r) {}"
        + "void gnu(String s, int i) {}}";
    CompilationUnit unit = translateType("A", source);
    final ArrayList<MethodDeclaration> methods = Lists.newArrayList();
    unit.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (!BindingUtil.isSynthetic(node.getModifiers())) {
          methods.add(node);
        }
      }
    });
    TreeUtil.sortMethods(methods);
    assertTrue(methods.get(0).toString().startsWith("A()"));
    assertTrue(methods.get(1).toString().startsWith("A(int i)"));
    assertTrue(methods.get(2).toString().startsWith("A(int i,Runnable r)"));
    assertTrue(methods.get(3).toString().startsWith("A(String s)"));
    assertTrue(methods.get(4).toString().startsWith("void gnu()"));
    assertTrue(methods.get(5).toString().startsWith("void gnu(int i,Runnable r)"));
    assertTrue(methods.get(6).toString().startsWith("void gnu(String s,int i)"));
    assertTrue(methods.get(7).toString().startsWith("void gnu(String s,int i,Runnable r)"));
    assertTrue(methods.get(8).toString().startsWith("void yak()"));
    assertTrue(methods.get(9).toString().startsWith("void zebra()"));
  }

}
