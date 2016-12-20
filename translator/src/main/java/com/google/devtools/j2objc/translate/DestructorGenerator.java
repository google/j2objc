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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Adds release methods to Java classes, in preparation for translation
 * to iOS.  Because Objective-C allows messages to be sent to nil, all
 * fields can be released regardless of whether they currently reference
 * data.
 *
 * @author Tom Ball
 */
public class DestructorGenerator extends UnitTreeVisitor {

  private final ExecutableElement superDeallocElement =
      GeneratedExecutableElement.newMethodWithSelector(
          NameTable.DEALLOC_METHOD, typeUtil.getVoid(), TypeUtil.NS_OBJECT)
      .addModifiers(Modifier.PUBLIC);

  public DestructorGenerator(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    addDeallocMethod(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (!node.isInterface()) {
      addDeallocMethod(node);
    }
  }

  private void addDeallocMethod(AbstractTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    boolean hasFinalize = hasFinalizeMethod(type);
    List<Statement> releaseStatements = createReleaseStatements(node);
    if (releaseStatements.isEmpty() && !hasFinalize) {
      return;
    }

    ExecutableElement deallocElement = GeneratedExecutableElement.newMethodWithSelector(
        NameTable.DEALLOC_METHOD, typeUtil.getVoid(), type)
        .addModifiers(Modifier.PUBLIC);
    MethodDeclaration deallocDecl = new MethodDeclaration(deallocElement);
    deallocDecl.setHasDeclaration(false);
    Block block = new Block();
    deallocDecl.setBody(block);
    List<Statement> stmts = block.getStatements();
    if (hasFinalize) {
      String clsName = nameTable.getFullName(type);
      stmts.add(new NativeStatement("JreCheckFinalize(self, [" + clsName + " class]);"));
    }
    stmts.addAll(releaseStatements);
    if (options.useReferenceCounting()) {
      stmts.add(new ExpressionStatement(
          new SuperMethodInvocation(new ExecutablePair(superDeallocElement))));
    }

    node.addBodyDeclaration(deallocDecl);
  }

  private boolean hasFinalizeMethod(TypeElement type) {
    if (type == null || ElementUtil.getQualifiedName(type).equals("java.lang.Object")) {
      return false;
    }
    for (ExecutableElement method : ElementUtil.getMethods(type)) {
      if (ElementUtil.getName(method).equals(NameTable.FINALIZE_METHOD)
          && method.getParameters().isEmpty()) {
        return true;
      }
    }
    return hasFinalizeMethod(ElementUtil.getSuperclass(type));
  }

  private List<Statement> createReleaseStatements(AbstractTypeDeclaration node) {
    List<Statement> statements = Lists.newArrayList();
    for (VariableDeclarationFragment fragment : TreeUtil.getAllFields(node)) {
      Statement releaseStmt = createRelease(fragment.getVariableElement());
      if (releaseStmt != null) {
        statements.add(releaseStmt);
      }
    }
    return statements;
  }

  private Statement createRelease(VariableElement var) {
    TypeMirror varType = var.asType();
    if (ElementUtil.isStatic(var) || varType.getKind().isPrimitive()
        || ElementUtil.isWeakReference(var)) {
      return null;
    }
    boolean isVolatile = ElementUtil.isVolatile(var);
    boolean isRetainedWith = ElementUtil.isRetainedWithField(var);
    String funcName = null;
    if (isRetainedWith) {
      funcName = isVolatile ? "JreVolatileRetainedWithRelease" : "JreRetainedWithRelease";
    } else if (isVolatile) {
      funcName = "JreReleaseVolatile";
    } else if (options.useReferenceCounting()) {
      funcName = "RELEASE_";
    }
    if (funcName == null) {
      return null;
    }
    TypeMirror voidType = typeUtil.getVoid();
    TypeMirror idType = TypeUtil.ID_TYPE;
    FunctionElement element = new FunctionElement(funcName, voidType, null);
    FunctionInvocation releaseInvocation = new FunctionInvocation(element, voidType);
    if (isRetainedWith) {
      element.addParameters(idType);
      releaseInvocation.addArgument(
          new ThisExpression(ElementUtil.getDeclaringClass(var).asType()));
    }
    element.addParameters(isVolatile ? TypeUtil.ID_PTR_TYPE : idType);
    Expression arg = new SimpleName(var);
    if (isVolatile) {
      arg = new PrefixExpression(
          new PointerType(varType), PrefixExpression.Operator.ADDRESS_OF, arg);
    }
    releaseInvocation.addArgument(arg);
    return new ExpressionStatement(releaseInvocation);
  }
}
