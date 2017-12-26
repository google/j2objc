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

package com.google.devtools.j2objc.gen;

import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import java.lang.reflect.Modifier;

/**
 * Generates private type declarations within the source file.
 *
 * @author Tom Ball, Keith Stanger
 */
public class TypePrivateDeclarationGenerator extends TypeDeclarationGenerator {

  protected TypePrivateDeclarationGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder, node);
  }

  public static void generate(SourceBuilder builder, AbstractTypeDeclaration node) {
    new TypePrivateDeclarationGenerator(builder, node).generate();
  }

  @Override
  protected boolean printPrivateDeclarations() {
    return true;
  }

  private void generate() {
    if (typeNode.hasPrivateDeclaration()) {
      generateInitialDeclaration();
    } else {
      generateDeclarationExtension();
    }
  }

  private void generateDeclarationExtension() {
    printClassExtension();
    printCompanionClassDeclaration();
    printFieldSetters();
    printStaticFieldDeclarations();
    printOuterDeclarations();
  }

  private void printClassExtension() {
    if (isInterfaceType()) {
      return;
    }
    boolean hasPrivateFields = !Iterables.isEmpty(getInstanceFields());
    Iterable<BodyDeclaration> privateDecls = getInnerDeclarations();
    if (!Iterables.isEmpty(privateDecls) || hasPrivateFields) {
      newline();
      printf("@interface %s ()", typeName);
      printInstanceVariables();
      printDeclarations(privateDecls);
      println("\n@end");
    }
  }

  @Override
  protected void printStaticAccessors() {
    // Static accessors are only needed by the public API.
  }

  @Override
  protected void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration) {
    Expression initializer = fragment.getInitializer();
    print("static " + baseDeclaration);
    if (initializer != null) {
      print(" = " + generateExpression(initializer));
    }
    println(";");
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    newline();
    // We expect native functions to be defined externally.
    if (!Modifier.isNative(function.getModifiers())) {
      print("__attribute__((unused)) static ");
    }
    print(getFunctionSignature(function, true));
    if (function.returnsRetained()) {
      print(" NS_RETURNS_RETAINED");
    }
    println(";");
  }
}
