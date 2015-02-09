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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Modifies enum types for Objective C.
 *
 * @author Keith Stanger
 */
public class EnumRewriter extends TreeVisitor {

  private GeneratedVariableBinding nameVar = null;
  private GeneratedVariableBinding ordinalVar = null;

  private final ITypeBinding stringType = Types.resolveIOSType("NSString");
  private final ITypeBinding intType = Types.resolveJavaType("int");

  private IMethodBinding addEnumConstructorParams(IMethodBinding method) {
    GeneratedMethodBinding newMethod = new GeneratedMethodBinding(method);
    newMethod.addParameter(stringType);
    newMethod.addParameter(intType);
    return newMethod;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    List<Statement> stmts = node.getClassInitStatements().subList(0, 0);
    int i = 0;
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      IMethodBinding binding =
          addEnumConstructorParams(constant.getMethodBinding().getMethodDeclaration());
      ClassInstanceCreation creation = new ClassInstanceCreation(binding);
      TreeUtil.copyList(constant.getArguments(), creation.getArguments());
      String name = NameTable.getName(constant.getName().getBinding());
      creation.getArguments().add(new StringLiteral(name));
      creation.getArguments().add(new NumberLiteral(i++));
      creation.setHasRetainedResult(true);
      stmts.add(new ExpressionStatement(new Assignment(
          new SimpleName(constant.getVariableBinding()), creation)));
    }

    addExtraNativeDecls(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    assert nameVar == null && ordinalVar == null;
    IMethodBinding binding = node.getMethodBinding();
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (!binding.isConstructor() || !declaringClass.isEnum()) {
      return false;
    }
    IMethodBinding newBinding = addEnumConstructorParams(node.getMethodBinding());
    node.setMethodBinding(newBinding);
    nameVar = new GeneratedVariableBinding(
        "__name", 0, stringType, false, true, declaringClass, newBinding);
    ordinalVar = new GeneratedVariableBinding(
        "__ordinal", 0, intType, false, true, declaringClass, newBinding);
    node.getParameters().add(new SingleVariableDeclaration(nameVar));
    node.getParameters().add(new SingleVariableDeclaration(ordinalVar));
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    nameVar = ordinalVar = null;
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    assert nameVar != null && ordinalVar != null;
    node.setMethodBinding(addEnumConstructorParams(node.getMethodBinding()));
    node.getArguments().add(new SimpleName(nameVar));
    node.getArguments().add(new SimpleName(ordinalVar));
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    assert nameVar != null && ordinalVar != null;
    node.setMethodBinding(addEnumConstructorParams(node.getMethodBinding()));
    node.getArguments().add(new SimpleName(nameVar));
    node.getArguments().add(new SimpleName(ordinalVar));
  }

  private static void addExtraNativeDecls(EnumDeclaration node) {
    String typeName = NameTable.getFullName(node.getTypeBinding());
    int numConstants = node.getEnumConstants().size();

    String header = String.format(
        "+ (IOSObjectArray *)values;\n"
        + "FOUNDATION_EXPORT IOSObjectArray *%s_values();\n\n"
        + "+ (%s *)valueOfWithNSString:(NSString *)name;\n\n"
        + "FOUNDATION_EXPORT %s *%s_valueOfWithNSString_(NSString *name);\n"
        + "- (id)copyWithZone:(NSZone *)zone;\n", typeName, typeName, typeName, typeName);

    StringBuilder sb = new StringBuilder();
    sb.append(String.format(
        "IOSObjectArray *%s_values() {\n"
        + "  %s_init();\n"
        + "  return [IOSObjectArray arrayWithObjects:%s_values_ count:%s type:%s_class_()];\n"
        + "}\n"
        + "+ (IOSObjectArray *)values {\n"
        + "  return %s_values();\n"
        + "}\n\n", typeName, typeName, typeName, numConstants, typeName, typeName));

    sb.append(String.format(
        "+ (%s *)valueOfWithNSString:(NSString *)name {\n"
        + "  return %s_valueOfWithNSString_(name);\n"
        + "}\n\n", typeName, typeName));

    sb.append(String.format(
        "%s *%s_valueOfWithNSString_(NSString *name) {\n"
            + "  %s_init();\n"
            + "  for (int i = 0; i < %s; i++) {\n"
            + "    %s *e = %s_values_[i];\n"
            + "    if ([name isEqual:[e name]]) {\n"
            + "      return e;\n"
            + "    }\n"
            + "  }\n", typeName, typeName, typeName, numConstants, typeName, typeName));
    if (Options.useReferenceCounting()) {
      sb.append(
          "  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name]"
          + " autorelease];\n");
    } else {
      sb.append("  @throw [[JavaLangIllegalArgumentException alloc] initWithNSString:name];\n");
    }
    sb.append("  return nil;\n}\n\n");

    // Enum constants needs to implement NSCopying.  Being singletons, they
    // can just return self, as long the retain count is incremented.
    String selfString = Options.useReferenceCounting() ? "[self retain]" : "self";
    sb.append(String.format("- (id)copyWithZone:(NSZone *)zone {\n  return %s;\n}\n", selfString));

    node.getBodyDeclarations().add(new NativeDeclaration(header, sb.toString()));
  }
}
