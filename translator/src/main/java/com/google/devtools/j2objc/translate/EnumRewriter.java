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

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import com.google.j2objc.annotations.ObjectiveCName;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Modifies enum types for Objective C.
 *
 * @author Keith Stanger
 */
public class EnumRewriter extends UnitTreeVisitor {

  public EnumRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    addEnumInitialization(node);
    addValuesMethod(node);
    addValueOfMethod(node);
    addExtraNativeDecls(node);
  }

  private void addEnumInitialization(EnumDeclaration node) {
    if (node.getEnumConstants().isEmpty()) {
      return;
    }
    if (options.useARC()) {
      addArcInitialization(node);
    } else {
      if (isSimpleEnum(node)) {
        addSimpleNonArcInitialization(node);
      } else {
        addNonArcInitialization(node);
      }
    }
  }

  /**
   * Returns true if an enum doesn't have custom or renamed constructors,
   * vararg constructors or constants with anonymous class extensions.
   */
  private boolean isSimpleEnum(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      ExecutableElement method = constant.getExecutableElement();
      if (method.getParameters().size() > 0 || method.isVarArgs()) {
        return false;
      }
      if (ElementUtil.hasAnnotation(method, ObjectiveCName.class)) {
        return false;
      }
      TypeElement valueType = ElementUtil.getDeclaringClass(method);
      if (valueType != type) {
        return false;
      }
    }
    return true;
  }

  private void addSimpleNonArcInitialization(EnumDeclaration node) {
    List<EnumConstantDeclaration> constants = node.getEnumConstants();
    List<Statement> stmts = node.getClassInitStatements().subList(0, 0);
    stmts.add(new NativeStatement("size_t objSize = class_getInstanceSize(self);"));
    stmts.add(new NativeStatement(UnicodeUtils.format(
        "size_t allocSize = %s * objSize;", constants.size())));
    stmts.add(new NativeStatement("uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);"));
    VariableElement localEnum = GeneratedVariableElement.newLocalVar("e", TypeUtil.ID_TYPE, null);
    stmts.add(new VariableDeclarationStatement(localEnum, null));

    StringBuffer sb = new StringBuffer("id names[] = {\n  ");
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      sb.append("@\"" + ElementUtil.getName(constant.getVariableElement()) + "\", ");
    }
    sb.append("\n};");
    stmts.add(new NativeStatement(sb.toString()));

    TypeMirror intType = typeUtil.getInt();
    GeneratedVariableElement loopCounterElement =
        GeneratedVariableElement.newLocalVar("i", intType, TreeUtil.getEnclosingElement(node));
    VariableDeclarationExpression loopCounter =
        new VariableDeclarationExpression().setType(Type.newType(loopCounterElement.asType()))
            .addFragment(new VariableDeclarationFragment(
                loopCounterElement, TreeUtil.newLiteral(0, typeUtil)));
    Expression loopTest = new InfixExpression()
        .setOperator(InfixExpression.Operator.LESS)
        .setTypeMirror(intType)
        .addOperand(new SimpleName(loopCounterElement))
        .addOperand(TreeUtil.newLiteral(constants.size(), typeUtil));
    Expression loopUpdater =
        new PostfixExpression(loopCounterElement, PostfixExpression.Operator.INCREMENT);
    Block loopBody = new Block();
    stmts.add(new ForStatement()
        .addInitializer(loopCounter)
        .setExpression(loopTest)
        .addUpdater(loopUpdater)
        .setBody(loopBody));
    String enumClassName = nameTable.getFullName(node.getTypeElement());
    loopBody.addStatement(new NativeStatement("(" + enumClassName
        + "_values_[i] = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);"));
    loopBody.addStatement(new NativeStatement(enumClassName
        + "_initWithNSString_withInt_(e, names[i], i);"));
   }

  private void addNonArcInitialization(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    int baseTypeCount = 0;
    List<Statement> sizeStatements = new ArrayList<>();
    List<Statement> initStatements = new ArrayList<>();
    TypeMirror voidType = typeUtil.getVoid();
    VariableElement localEnum = GeneratedVariableElement.newLocalVar("e", TypeUtil.ID_TYPE, null);

    int i = 0;
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      VariableElement varElement = constant.getVariableElement();
      String varName = ElementUtil.getName(varElement);
      ExecutableElement methodElement = constant.getExecutableElement();
      TypeElement valueType = ElementUtil.getDeclaringClass(methodElement);
      boolean isAnonymous = valueType != type;
      String classExpr = isAnonymous ? "[" + nameTable.getFullName(valueType) + " class]" : "self";
      String sizeName = "objSize" + (isAnonymous ? "_" + varName : "");

      if (isAnonymous) {
        sizeStatements.add(new NativeStatement(UnicodeUtils.format(
            "size_t %s = class_getInstanceSize(%s);", sizeName, classExpr)));
        sizeStatements.add(new NativeStatement(UnicodeUtils.format("allocSize += %s;", sizeName)));
      } else {
        baseTypeCount++;
      }

      initStatements.add(new ExpressionStatement(new CommaExpression(
          new Assignment(new SimpleName(varElement), new Assignment(
          new SimpleName(localEnum), new NativeExpression(UnicodeUtils.format(
              "objc_constructInstance(%s, (void *)ptr)", classExpr), type.asType()))),
          new NativeExpression("ptr += " + sizeName, voidType))));
      String initName = nameTable.getFullFunctionName(methodElement);
      FunctionElement initElement = new FunctionElement(initName, voidType, valueType)
          .addParameters(valueType.asType())
          .addParameters(ElementUtil.asTypes(methodElement.getParameters()));
      FunctionInvocation initFunc = new FunctionInvocation(initElement, voidType);
      initFunc.addArgument(new SimpleName(localEnum));
      TreeUtil.copyList(constant.getArguments(), initFunc.getArguments());
      initFunc.addArgument(new StringLiteral(varName, typeUtil));
      initFunc.addArgument(new NumberLiteral(i++, typeUtil));
      initStatements.add(new ExpressionStatement(initFunc));
    }

    List<Statement> stmts = node.getClassInitStatements().subList(0, 0);
    if (baseTypeCount == 0) {
      stmts.add(new NativeStatement("size_t allocSize = 0;"));
    } else {
      stmts.add(new NativeStatement("size_t objSize = class_getInstanceSize(self);"));
      stmts.add(new NativeStatement(UnicodeUtils.format(
          "size_t allocSize = %s * objSize;", baseTypeCount)));
    }
    stmts.addAll(sizeStatements);
    stmts.add(new NativeStatement("uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);"));
    stmts.add(new VariableDeclarationStatement(localEnum, null));
    stmts.addAll(initStatements);
  }

  // ARC does not allow using "objc_constructInstance" so ARC code doesn't get
  // the shared allocation optimization.
  private void addArcInitialization(EnumDeclaration node) {
    List<Statement> stmts = node.getClassInitStatements().subList(0, 0);
    int i = 0;
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      VariableElement varElement = constant.getVariableElement();
      ClassInstanceCreation creation = new ClassInstanceCreation(constant.getExecutablePair());
      TreeUtil.copyList(constant.getArguments(), creation.getArguments());
      creation.addArgument(new StringLiteral(ElementUtil.getName(varElement), typeUtil));
      creation.addArgument(new NumberLiteral(i++, typeUtil));
      creation.setHasRetainedResult(true);
      stmts.add(new ExpressionStatement(new Assignment(new SimpleName(varElement), creation)));
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    ExecutableElement element = node.getExecutableElement();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    if (!ElementUtil.isConstructor(element) || !ElementUtil.isEnum(declaringClass)) {
      return false;
    }
    node.removeModifiers(Modifier.PUBLIC | Modifier.PROTECTED);
    node.addModifiers(Modifier.PRIVATE);
    return true;
  }

  private void addValuesMethod(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    ExecutableElement method = ElementUtil.findMethod(type, "values");
    assert method != null : "Can't find values method on enum type.";
    String typeName = nameTable.getFullName(type);
    MethodDeclaration methodDecl = new MethodDeclaration(method);
    Block body = new Block();
    methodDecl.setBody(body);
    body.addStatement(new NativeStatement(UnicodeUtils.format(
        "  return [IOSObjectArray arrayWithObjects:%s_values_ count:%s type:%s_class_()];",
        typeName, node.getEnumConstants().size(), typeName)));
    node.addBodyDeclaration(methodDecl);
  }

  private void addValueOfMethod(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    ExecutableElement method = ElementUtil.findMethod(type, "valueOf", "java.lang.String");
    assert method != null : "Can't find valueOf method on enum type.";
    String typeName = nameTable.getFullName(type);
    int numConstants = node.getEnumConstants().size();

    VariableElement nameParam = GeneratedVariableElement.newParameter(
        "name", method.getParameters().get(0).asType(), method);
    MethodDeclaration methodDecl = new MethodDeclaration(method);
    methodDecl.addParameter(new SingleVariableDeclaration(nameParam));
    Block body = new Block();
    methodDecl.setBody(body);

    StringBuilder impl = new StringBuilder();
    if (numConstants > 0) {
      impl.append(UnicodeUtils.format(
          "  for (int i = 0; i < %s; i++) {\n"
          + "    %s *e = %s_values_[i];\n"
          + "    if ([name isEqual:[e name]]) {\n"
          + "      return e;\n"
          + "    }\n"
          + "  }\n", numConstants, typeName, typeName));
    }
    impl.append(
        "  @throw create_JavaLangIllegalArgumentException_initWithNSString_(name);\n"
        + "  return nil;");

    body.addStatement(new NativeStatement(impl.toString()));
    node.addBodyDeclaration(methodDecl);
  }

  private void addExtraNativeDecls(EnumDeclaration node) {
    String typeName = nameTable.getFullName(node.getTypeElement());
    int numConstants = node.getEnumConstants().size();
    boolean swiftFriendly = options.swiftFriendly();

    StringBuilder header = new StringBuilder();
    StringBuilder implementation = new StringBuilder();

    header.append("- (id)copyWithZone:(NSZone *)zone;\n");

    // Append enum type suffix.
    String nativeName = NameTable.getNativeEnumName(typeName);

    // The native type is not declared for an empty enum.
    if (swiftFriendly && numConstants > 0) {
      header.append(UnicodeUtils.format("- (%s)toNSEnum;\n", nativeName));
    }

    if (swiftFriendly && numConstants > 0) {
      implementation.append(UnicodeUtils.format(
          "- (%s)toNSEnum {\n"
              + "  return (%s)[self ordinal];\n"
              + "}\n\n", nativeName, nativeName));
    }

    // Enum constants needs to implement NSCopying. Being singletons, they can
    // just return self. No need to increment the retain count because enum
    // values are never deallocated.
    implementation.append("- (id)copyWithZone:(NSZone *)zone {\n  return self;\n}\n");

    node.addBodyDeclaration(NativeDeclaration.newInnerDeclaration(
        header.toString(), implementation.toString()));

    StringBuilder outerHeader = new StringBuilder();
    StringBuilder outerImpl = new StringBuilder();

    outerHeader.append(UnicodeUtils.format(
        "FOUNDATION_EXPORT %s *%s_fromOrdinal(NSUInteger ordinal);\n",
        typeName, typeName));

    outerImpl.append(UnicodeUtils.format(
        "%s *%s_fromOrdinal(NSUInteger ordinal) {\n", typeName, typeName));
    // Avoid "comparison of unsigned expression >= 0 is always true" error.
    if (numConstants == 0) {
      outerImpl.append("  return nil;\n}\n");
    } else {
      outerImpl.append(UnicodeUtils.format(
          "  %s_initialize();\n"
          // Param is unsigned, so don't need to check lower bound.
          + "  if (ordinal >= %s) {\n"
          + "    return nil;\n"
          + "  }\n"
          + "  return %s_values_[ordinal];\n"
          + "}\n",
          typeName, numConstants, typeName));
    }

    NativeDeclaration outerDecl =
        NativeDeclaration.newOuterDeclaration(outerHeader.toString(), outerImpl.toString());
    outerDecl.addImplementationImportType(
        GeneratedTypeElement.newEmulatedClass(
            "java.lang.IllegalArgumentException",
            typeUtil.resolveJavaType("java.lang.RuntimeException").asType()).asType());
    node.addBodyDeclaration(outerDecl);
  }
}
