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

package com.google.devtools.j2objc.sym;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link Scope}.
 *
 * @author Tom Ball
 */
public class ScopeTest extends GenerationTest {
  private CompilationUnit unit;
  private TypeDeclaration testDeclaration;

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    unit = translateType("Test", "public class Test { void bar(int i) {} Runnable runner; }");
    testDeclaration = (TypeDeclaration) unit.types().get(0);
  }

  public void testNewScope() {
    Scope scope = new Scope(null, null, Symbols.getGlobalScope());
    assertEquals(Symbols.getGlobalScope(), scope.getParent());
    assertTrue(scope.getMembers().isEmpty());
    List<Symbol> topLevelSyms = Symbols.getGlobalScope().getMembers();
    int symsFound = 0;
    for (Symbol sym : topLevelSyms) {
      if (sym.getName().equals("Test") || sym.getName().equals("int")) {
        symsFound++;
      }
    }
    assertEquals(2, symsFound);
  }

  public void testTypeSymbol() throws IOException {
    ITypeBinding type = Types.getTypeBinding(testDeclaration);
    assertNotNull(Symbols.resolve(type));
  }

  public void testVariableSymbol() throws IOException {
    ITypeBinding type = Types.getTypeBinding(testDeclaration);
    IVariableBinding var = type.getDeclaredFields()[0];
    assertNotNull(Symbols.resolve(var));
  }

  public void testMethodSymbol() throws IOException {
    ITypeBinding type = Types.getTypeBinding(testDeclaration);
    IMethodBinding method = type.getDeclaredMethods()[0];
    assertNotNull(Symbols.resolve(method));
  }

  public void testVariableAddition() {
    ITypeBinding stringType = unit.getAST().resolveWellKnownType("java.lang.String");
    GeneratedVariableBinding var =
        new GeneratedVariableBinding("foo", 0, stringType, true, false, null, null);
    TypeSymbol testSymbol = Symbols.resolve(Types.getTypeBinding(testDeclaration));
    VariableSymbol symbol = new VariableSymbol(var, testSymbol.getScope());
    assertEquals(symbol, testSymbol.getScope().resolve(var));
  }

  /**
   * Verify that when the initialization normalizer moves init statements,
   * their symbols are loaded in the new method.
   */
  public void testInitializeMethod() {
    String source = "public class Test { static { for (int i = 0; i < 3; i++) { } } }";
    CompilationUnit unit = translateType("Test", source);
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(0);
    MethodDeclaration init = (MethodDeclaration) decl.bodyDeclarations().get(0);
    assertEquals("initialize", init.getName().getIdentifier());
    ForStatement stmt = (ForStatement) init.getBody().statements().get(0);
    Scope forScope = Symbols.getScope(stmt);
    VariableSymbol var = (VariableSymbol) forScope.getMembers().get(0);
    assertEquals("i", var.getName());
  }
}
