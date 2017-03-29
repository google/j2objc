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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.UnionType;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Collects the set of imports needed to resolve type references in an
 * implementation (.m) file.
 *
 * @author Tom Ball
 */
public class ImplementationImportCollector extends UnitTreeVisitor {

  private Set<Import> imports = new LinkedHashSet<>();

  public ImplementationImportCollector(CompilationUnit unit) {
    super(unit);
  }

  public Set<Import> getImports() {
    return imports;
  }

  private void addImports(Type type) {
    if (type instanceof UnionType) {
      for (Type t : ((UnionType) type).getTypes()) {
        addImports(t);
      }
    } else if (type != null) {
      addImports(type.getTypeMirror());
    }
  }

  private void addImports(TypeElement type) {
    if (type != null) {
      addImports(type.asType());
    }
  }

  private void addImports(TypeMirror type) {
    Import.addImports(type, imports, unit.getEnv());
  }

  private void addImports(Iterable<TypeMirror> types) {
    for (TypeMirror type : types) {
      addImports(type);
    }
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    addImports(node.getTypeElement());
    return true;
  }

  @Override
  public boolean visit(CastExpression node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public boolean visit(CatchClause node) {
    addImports(node.getException().getType());
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    addImports(node.getTypeElement());
    return true;
  }

  @Override
  public boolean visit(FieldAccess node) {
    addImports(node.getExpression().getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    addImports(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(FunctionInvocation node) {
    FunctionElement element = node.getFunctionElement();
    addImports(element.getDeclaringClass());
    for (Expression arg : node.getArguments()) {
      addImports(arg.getTypeMirror());
    }
    addImports(element.getReturnType());
    return true;
  }

  @Override
  public void endVisit(Assignment node) {
    addImports(node.getRightHandSide().getTypeMirror());
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    addImports(node.getRightOperand().getTypeMirror());
    return true;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    addImports(node.getTypeMirror());
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (Modifier.isAbstract(node.getModifiers())) {
      return false;
    }
    addImports(node.getReturnTypeMirror());
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    addImports(node.getExecutableType().getReturnType());
    Expression receiver = node.getExpression();
    if (receiver != null) {
      addImports(receiver.getTypeMirror());
    }
    for (Expression arg : node.getArguments()) {
      addImports(arg.getTypeMirror());
    }
    return true;
  }

  @Override
  public boolean visit(NativeDeclaration node) {
    addImports(node.getImplementationImportTypes());
    return true;
  }

  @Override
  public boolean visit(NativeExpression node) {
    addImports(node.getImportTypes());
    return true;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null) {
      if (ElementUtil.isGlobalVar(var)) {
        addImports(ElementUtil.getDeclaringClass(var));
        return false;
      } else {
        addImports(node.getQualifier().getTypeMirror());
      }
    }
    return true;
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if (expr != null) {
      addImports(expr.getTypeMirror());
    }
  }

  @Override
  public boolean visit(SimpleName node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null && ElementUtil.isGlobalVar(var)) {
      addImports(ElementUtil.getDeclaringClass(var));
    }
    return true;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return false;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    addImports(node.getVariableElement().asType());
    return true;
  }

  @Override
  public boolean visit(TryStatement node) {
    if (!node.getResources().isEmpty()) {
      addImports(typeUtil.resolveJavaType("java.lang.Throwable"));
    }
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    addImports(node.getTypeElement());
    return true;
  }

  @Override
  public boolean visit(TypeLiteral node) {
    TypeMirror type = node.getType().getTypeMirror();
    if (type.getKind().isPrimitive()) {
      addImports(TypeUtil.IOS_CLASS);
    } else if (type.getKind().equals(TypeKind.ARRAY)) {
      addImports(TypeUtil.IOS_CLASS);
      addImports(((ArrayType) type).getComponentType());
    } else {
      addImports(node.getType());
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    addImports(node.getTypeMirror());
    return true;
  }
}
