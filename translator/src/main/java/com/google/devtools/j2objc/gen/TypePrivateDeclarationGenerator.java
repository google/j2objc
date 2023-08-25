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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.lang.reflect.Modifier;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

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
    boolean shouldPrintClassExtension = shouldPrintClassExtension();
    if (shouldPrintClassExtension) {
      printClassExtension();
    }
    printCompanionClassDeclaration();
    printFieldSetters();
    printStaticFieldDeclarations();
    printOuterDeclarations();
    if (shouldPrintClassExtension) {
      printNonnullAuditedRegion(AuditedRegion.END);
    }
  }

  private void printClassExtension() {
    newline();
    printNonnullAuditedRegion(AuditedRegion.BEGIN);
    printf("@interface %s ()", typeName);
    printInstanceVariables();
    Iterable<BodyDeclaration> privateDecls = getInnerDeclarations();
    printDeclarations(privateDecls);
    println("\n@end");
  }

  private boolean shouldPrintClassExtension() {
    if (isInterfaceType()) {
      return false;
    }
    boolean hasPrivateFields = !Iterables.isEmpty(getInstanceFields());
    Iterable<BodyDeclaration> privateDecls = getInnerDeclarations();
    return !Iterables.isEmpty(privateDecls) || hasPrivateFields;
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
  protected void printDeadClassConstant(VariableDeclarationFragment fragment) {
    VariableElement var = fragment.getVariableElement();
    Object value = var.getConstantValue();
    assert value != null;
    String declType = getDeclarationType(var);
    declType += (declType.endsWith("*") ? "" : " ");
    String name = nameTable.getVariableShortName(var);
    if (ElementUtil.isPrimitiveConstant(var)) {
      printf("#define %s_%s %s\n", typeName, name, LiteralGenerator.generate(value));
    } else {
      print("static " + UnicodeUtils.format("%s%s_%s", declType, typeName, name));
      Expression initializer = fragment.getInitializer();
      if (initializer != null) {
        print(" = " + generateExpression(initializer));
      }
      println(";");
    }
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

  @Override
  protected String nullability(Element element) {
    return "";
  }
}
