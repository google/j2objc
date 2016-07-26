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
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies enum types for Objective C.
 *
 * @author Keith Stanger
 */
public class EnumRewriter extends TreeVisitor {

  private GeneratedVariableBinding nameVar = null;
  private GeneratedVariableBinding ordinalVar = null;

  private GeneratedMethodBinding addEnumConstructorParams(IMethodBinding method) {
    GeneratedMethodBinding newMethod = new GeneratedMethodBinding(method);
    newMethod.addParameter(typeEnv.resolveIOSType("NSString"));
    newMethod.addParameter(typeEnv.resolveJavaType("int"));
    return newMethod;
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
    if (Options.useARC()) {
      addArcInitialization(node);
    } else {
      addNonArcInitialization(node);
    }
  }

  private void addNonArcInitialization(EnumDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    int baseTypeCount = 0;
    List<Statement> sizeStatements = new ArrayList<>();
    List<Statement> initStatements = new ArrayList<>();
    ITypeBinding idType = typeEnv.resolveIOSType("id");
    ITypeBinding voidType = typeEnv.resolveJavaType("void");
    GeneratedVariableBinding localEnum =
        new GeneratedVariableBinding("e", 0, idType, false, false, null, null);

    int i = 0;
    for (EnumConstantDeclaration constant : node.getEnumConstants()) {
      IVariableBinding varBinding = constant.getVariableBinding();
      IMethodBinding methodBinding = constant.getMethodBinding().getMethodDeclaration();
      ITypeBinding valueType = methodBinding.getDeclaringClass();
      methodBinding = addEnumConstructorParams(methodBinding);
      boolean isAnonymous = valueType != type;
      String classExpr = isAnonymous ? "[" + nameTable.getFullName(valueType) + " class]" : "self";
      String sizeName = "objSize" + (isAnonymous ? "_" + varBinding.getName() : "");

      if (isAnonymous) {
        sizeStatements.add(new NativeStatement(UnicodeUtils.format(
            "size_t %s = class_getInstanceSize(%s);", sizeName, classExpr)));
        sizeStatements.add(new NativeStatement(UnicodeUtils.format("allocSize += %s;", sizeName)));
      } else {
        baseTypeCount++;
      }

      initStatements.add(new ExpressionStatement(new CommaExpression(
          new Assignment(new SimpleName(varBinding), new Assignment(
          new SimpleName(localEnum), new NativeExpression(UnicodeUtils.format(
              "objc_constructInstance(%s, (void *)ptr)", classExpr), type))),
          new NativeExpression("ptr += " + sizeName, voidType))));
      String initName = nameTable.getFullFunctionName(methodBinding);
      FunctionBinding initBinding = new FunctionBinding(initName, voidType, valueType);
      initBinding.addParameters(valueType);
      initBinding.addParameters(methodBinding.getParameterTypes());
      FunctionInvocation initFunc = new FunctionInvocation(initBinding, type);
      initFunc.addArgument(new SimpleName(localEnum));
      TreeUtil.copyList(constant.getArguments(), initFunc.getArguments());
      initFunc.addArgument(new StringLiteral(varBinding.getName(), typeEnv));
      initFunc.addArgument(new NumberLiteral(i++, typeEnv));
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
      IVariableBinding varBinding = constant.getVariableBinding();
      IMethodBinding binding =
          addEnumConstructorParams(constant.getMethodBinding().getMethodDeclaration());
      ClassInstanceCreation creation = new ClassInstanceCreation(binding);
      TreeUtil.copyList(constant.getArguments(), creation.getArguments());
      creation.addArgument(new StringLiteral(varBinding.getName(), typeEnv));
      creation.addArgument(new NumberLiteral(i++, typeEnv));
      creation.setHasRetainedResult(true);
      stmts.add(new ExpressionStatement(new Assignment(new SimpleName(varBinding), creation)));
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    assert nameVar == null && ordinalVar == null;
    IMethodBinding binding = node.getMethodBinding();
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (!binding.isConstructor() || !declaringClass.isEnum()) {
      return false;
    }
    GeneratedMethodBinding newBinding = addEnumConstructorParams(node.getMethodBinding());
    node.setMethodBinding(newBinding);
    node.removeModifiers(Modifier.PUBLIC | Modifier.PROTECTED);
    node.addModifiers(Modifier.PRIVATE);
    newBinding.setModifiers((newBinding.getModifiers() & ~(Modifier.PUBLIC | Modifier.PROTECTED))
        | Modifier.PRIVATE);
    nameVar = new GeneratedVariableBinding(
        "__name", 0, typeEnv.resolveIOSType("NSString"), false, true, declaringClass, newBinding);
    ordinalVar = new GeneratedVariableBinding(
        "__ordinal", 0, typeEnv.resolveJavaType("int"), false, true, declaringClass, newBinding);
    node.addParameter(new SingleVariableDeclaration(nameVar));
    node.addParameter(new SingleVariableDeclaration(ordinalVar));
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
    node.addArgument(new SimpleName(nameVar));
    node.addArgument(new SimpleName(ordinalVar));
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    assert nameVar != null && ordinalVar != null;
    node.setMethodBinding(addEnumConstructorParams(node.getMethodBinding()));
    node.addArgument(new SimpleName(nameVar));
    node.addArgument(new SimpleName(ordinalVar));
  }

  private void addValuesMethod(EnumDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    IMethodBinding method = BindingUtil.findDeclaredMethod(type, "values");
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
    ITypeBinding type = node.getTypeBinding();
    IMethodBinding method = BindingUtil.findDeclaredMethod(type, "valueOf", "java.lang.String");
    assert method != null : "Can't find valueOf method on enum type.";
    String typeName = nameTable.getFullName(type);
    int numConstants = node.getEnumConstants().size();

    GeneratedVariableBinding nameParam = new GeneratedVariableBinding(
        "name", 0, method.getParameterTypes()[0], false, true, null, method);
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
    String typeName = nameTable.getFullName(node.getTypeBinding());
    int numConstants = node.getEnumConstants().size();
    boolean swiftFriendly = Options.swiftFriendly();

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
        GeneratedTypeBinding.newTypeBinding(
            "java.lang.IllegalArgumentException",
            typeEnv.resolveJavaType("java.lang.RuntimeException"), false));
    node.addBodyDeclaration(outerDecl);
  }
}
