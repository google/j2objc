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

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.Set;

/**
 * Collects the set of imports needed to resolve type references in an
 * implementation (.m) file.
 *
 * @author Tom Ball
 */
public class ImplementationImportCollector extends ErrorReportingASTVisitor {

  private String mainTypeName;
  private Set<Import> imports = Sets.newLinkedHashSet();
  private Set<Import> declaredTypes = Sets.newHashSet();

  public void collect(CompilationUnit unit, String sourceFileName) {
    mainTypeName = NameTable.getMainTypeName(unit, sourceFileName);
    run(unit);
    for (Import imp : declaredTypes) {
      imports.remove(imp);
    }
  }

  public Set<Import> getImports() {
    return imports;
  }

  private void addImports(Type type) {
    if (type != null) {
      addImports(Types.getTypeBinding(type));
    }
  }

  private void addImports(ITypeBinding type) {
    Import.addImports(type, imports);
  }

  // Keep track of any declared types to avoid invalid imports.  The
  // exception is the main type, as it's needed to import the matching
  // header file.
  private void addDeclaredType(ITypeBinding type, boolean isEnum) {
    if (type != null
        && !NameTable.getFullName(type).equals(mainTypeName + (isEnum ? "Enum" : ""))) {
      Import.addImports(type, declaredTypes);
    }
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    addImports(type);
    addDeclaredType(type, false);
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public boolean visit(Assignment node) {
    if (node.getOperator() == Operator.PLUS_ASSIGN &&
        Types.isJavaStringType(Types.getTypeBinding(node.getLeftHandSide())) &&
        Types.isBooleanType(Types.getTypeBinding(node.getRightHandSide()))) {
      // Implicit conversion from boolean -> String translates into a
      // Boolean.toString(...) call, so add a reference to java.lang.Boolean.
      addImports(node.getAST().resolveWellKnownType("java.lang.Boolean"));
    }
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
  public boolean visit(ClassInstanceCreation node) {
    addImports(node.getType());
    IMethodBinding binding = Types.getMethodBinding(node);
    if (binding != null) {
      ITypeBinding[] parameterTypes = binding.getParameterTypes();
      for (int i = 0; i < node.arguments().size(); i++) {

        ITypeBinding parameterType;
        if (i < parameterTypes.length) {
          parameterType = parameterTypes[i];
        } else {
          parameterType = parameterTypes[parameterTypes.length - 1];
        }
        ITypeBinding actualType = Types.getTypeBinding(node.arguments().get(i));
        if (!parameterType.equals(actualType) &&
            actualType.isAssignmentCompatible(parameterType)) {
          addImports(actualType);
        } else {
          addImports(parameterType);
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(TryStatement node) {
    if (ASTUtil.getResources(node).size() > 0) {
      addImports(Types.mapTypeName("java.lang.Throwable"));
    }
    return true;
  }

  @Override
  public boolean visit(TypeLiteral node) {
    ITypeBinding type = Types.getTypeBinding(node.getType());
    if (type.isPrimitive()) {
      addImports(Types.getWrapperType(type));
    } else {
      addImports(type);
      addImports(Types.resolveIOSType("IOSClass"));
    }
    return false;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    addImports(type);
    addDeclaredType(type, true);
    addImports(Types.resolveIOSType("IOSClass"));
    addImports(GeneratedTypeBinding.newTypeBinding("java.lang.IllegalArgumentException",
        Types.resolveJavaType("java.lang.RuntimeException"), false));
    return true;
  }

  @Override
  public boolean visit(FieldAccess node) {
    addImports(Types.getTypeBinding(node.getName()));
    return true;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    addImports(Types.getTypeBinding(node.getRightOperand()));
    return true;
  }

  @Override
  public boolean visit(InfixExpression node) {
    if (Types.isJavaStringType(Types.getTypeBinding(node))) {
      boolean needsImport = false;
      if (Types.isBooleanType(Types.getTypeBinding(node.getLeftOperand())) ||
          Types.isBooleanType(Types.getTypeBinding(node.getRightOperand()))) {
        needsImport = true;
      } else {
        for (Expression extendedExpression : ASTUtil.getExtendedOperands(node)) {
          if (Types.isBooleanType(Types.getTypeBinding(extendedExpression))) {
            needsImport = true;
            break;
          }
        }
      }

      if (needsImport) {
        // Implicit conversion from boolean -> String translates into a
        // Boolean.toString(...) call, so add a reference to java.lang.Boolean.
        addImports(node.getAST().resolveWellKnownType("java.lang.Boolean"));
      }
    }
    return true;
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    addDefaultValueTypes(Types.getAnnotationBinding(node));
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    addImports(node.getReturnType2());
    IMethodBinding binding = Types.getMethodBinding(node);
    for (ITypeBinding paramType : binding.getParameterTypes()) {
      addImports(paramType);
    }
    for (ITypeBinding exceptionType : binding.getExceptionTypes()) {
      addImports(exceptionType);
      addImports(Types.resolveIOSType("IOSClass"));
    }
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    IMethodBinding methodBinding = Types.getMethodBinding(node);
    addImports(methodBinding.getReturnType());
    // Check for vararg method
    IMethodBinding binding = Types.getMethodBinding(node);
    if (binding != null) {
      ITypeBinding[] parameterTypes = binding.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        ITypeBinding actualType = Types.getTypeBinding(node.arguments().get(i));
        if (!parameterType.equals(actualType) &&
            actualType.isAssignmentCompatible(parameterType)) {
          addImports(actualType);
        }
      }
    }
    // Check for static method references.
    Expression expr = node.getExpression();
    if (expr == null) {
      // check for method that's been statically imported
      if (binding != null) {
        ITypeBinding typeBinding = binding.getDeclaringClass();
        if (typeBinding != null) {
          addImports(typeBinding);
        }
      }
    } else {
      IMethodBinding receiver = Types.getMethodBinding(expr);
      if (receiver != null && !receiver.isConstructor()) {
        addImports(receiver.getReturnType());
      }
      if (receiver == null) {
        // Check for class variable or enum constant.
        IVariableBinding var = Types.getVariableBinding(expr);
        if (var == null || var.isEnumConstant()) {
          addImports(Types.getTypeBinding(expr));
        } else {
          addImports(var.getType());
        }
      }
    }
    while (expr != null && expr instanceof Name) {
      ITypeBinding typeBinding = Types.getTypeBinding(expr);
      if (typeBinding != null && typeBinding.isClass()) { // if class literal
        addImports(typeBinding);
        break;
      }
      if (expr instanceof QualifiedName) {
        expr = ((QualifiedName) expr).getQualifier();
      } else {
        break;
      }
    }
    return true;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    addDefaultValueTypes(Types.getAnnotationBinding(node));
    return true;
  }

  @Override
  public boolean visit(QualifiedName node) {
    IBinding type = Types.getTypeBinding(node);
    if (type != null) {
      addImports((ITypeBinding) type);
    }
    return true;
  }

  @Override
  public boolean visit(SimpleName node) {
    IVariableBinding var = Types.getVariableBinding(node);
    if (var != null && Modifier.isStatic(var.getModifiers())) {
      ITypeBinding declaringClass = var.getDeclaringClass();
      addImports(declaringClass);
    }
    ITypeBinding type = Types.getTypeBinding(node);
    if (BindingUtil.isRuntimeAnnotation(type)) {
      addImports(type);
      addImports(Types.resolveIOSType("IOSClass"));
    }
    return true;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    addDefaultValueTypes(Types.getAnnotationBinding(node));
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    addImports(type);
    addDeclaredType(type, false);
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    Type type = node.getType();
    addImports(type);
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    addImports(node.getType());
    return true;
  }

  private void addDefaultValueTypes(IAnnotationBinding binding) {
    for (IMemberValuePairBinding memberValuePair : binding.getAllMemberValuePairs()) {
      if (memberValuePair.isDefault()) {
        Object value = memberValuePair.getValue();
        if (value instanceof IVariableBinding) {
          addImports(((IVariableBinding) value).getType());
        }
      }
    }
  }
}
