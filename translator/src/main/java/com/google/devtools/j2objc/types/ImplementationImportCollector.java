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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Assignment.Operator;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.UnionType;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Set;

/**
 * Collects the set of imports needed to resolve type references in an
 * implementation (.m) file.
 *
 * @author Tom Ball
 */
public class ImplementationImportCollector extends TreeVisitor {

  private String mainTypeName;
  private Set<Import> imports = Sets.newLinkedHashSet();
  private Set<Import> declaredTypes = Sets.newHashSet();

  public void collect(CompilationUnit unit) {
    mainTypeName = unit.getMainTypeName();
    run(unit);
    for (Import imp : declaredTypes) {
      imports.remove(imp);
    }
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
      addImports(type.getTypeBinding());
    }
  }

  private void addImports(ITypeBinding type) {
    Import.addImports(type, imports);
  }

  // Keep track of any declared types to avoid invalid imports.  The
  // exception is the main type, as it's needed to import the matching
  // header file.
  private void addDeclaredType(ITypeBinding type) {
    if (type != null && !type.getName().equals(mainTypeName)) {
      Import.addImports(type, declaredTypes);
    }
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    addImports(type);
    addDeclaredType(type);
    addImports(Types.resolveIOSType("IOSClass"));
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public boolean visit(Assignment node) {
    if (node.getOperator() == Operator.PLUS_ASSIGN
        && Types.isJavaStringType(node.getLeftHandSide().getTypeBinding())
        && Types.isBooleanType(node.getRightHandSide().getTypeBinding())) {
      // Implicit conversion from boolean -> String translates into a
      // Boolean.toString(...) call, so add a reference to java.lang.Boolean.
      addImports(Types.resolveJavaType("java.lang.Boolean"));
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
    IMethodBinding binding = node.getMethodBinding();
    if (binding != null) {
      ITypeBinding[] parameterTypes = binding.getParameterTypes();
      List<Expression> arguments = node.getArguments();
      for (int i = 0; i < arguments.size(); i++) {

        ITypeBinding parameterType;
        if (i < parameterTypes.length) {
          parameterType = parameterTypes[i];
        } else {
          parameterType = parameterTypes[parameterTypes.length - 1];
        }
        ITypeBinding actualType = arguments.get(i).getTypeBinding();
        if (!parameterType.equals(actualType)
            && actualType.isAssignmentCompatible(parameterType)) {
          addImports(actualType);
        } else {
          addImports(parameterType);
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    addImports(type);
    addDeclaredType(type);
    addImports(Types.resolveIOSType("IOSClass"));
    addImports(GeneratedTypeBinding.newTypeBinding("java.lang.IllegalArgumentException",
        Types.resolveJavaType("java.lang.RuntimeException"), false));
    return true;
  }

  @Override
  public boolean visit(FieldAccess node) {
    addImports(node.getName().getTypeBinding());
    return true;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    addImports(node.getType());
    return true;
  }

  @Override
  public void endVisit(FunctionInvocation node) {
    // The return type is needed because the expression might need a cast.
    addImports(node.getTypeBinding());
    addImports(node.getDeclaringType());
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    addImports(node.getRightOperand().getTypeBinding());
    return true;
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    addImports(node.getReturnType());
    IMethodBinding binding = node.getMethodBinding();
    for (ITypeBinding exceptionType : binding.getExceptionTypes()) {
      addImports(exceptionType);
      addImports(Types.resolveIOSType("IOSClass"));
    }
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    addImports(binding.getReturnType());
    // Check for vararg method
    if (binding != null) {
      ITypeBinding[] parameterTypes = binding.getParameterTypes();
      int nParameters = parameterTypes.length;
      if (binding.isVarargs()) {
        // Only check type for varargs parameters, since the actual
        // number of arguments will vary.
        addImports(parameterTypes[nParameters - 1]);
        --nParameters;
      }
      List<Expression> arguments = node.getArguments();
      for (int i = 0; i < nParameters; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        ITypeBinding actualType = arguments.get(i).getTypeBinding();
        if (!parameterType.equals(actualType)
            && actualType.isAssignmentCompatible(parameterType)) {
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
      addImports(expr.getTypeBinding());
    }
    while (expr != null && expr instanceof Name) {
      ITypeBinding typeBinding = expr.getTypeBinding();
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
  public boolean visit(FunctionInvocation node) {
    for (Expression arg : node.getArguments()) {
      addImports(arg.getTypeBinding());
    }
    addImports(node.getDeclaredReturnType());
    return true;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public boolean visit(QualifiedName node) {
    IBinding type = node.getTypeBinding();
    if (type != null) {
      addImports((ITypeBinding) type);
    }
    return true;
  }

  @Override
  public boolean visit(SimpleName node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var != null && Modifier.isStatic(var.getModifiers())) {
      ITypeBinding declaringClass = var.getDeclaringClass();
      addImports(declaringClass);
    }
    ITypeBinding type = node.getTypeBinding();
    if (BindingUtil.isRuntimeAnnotation(type)) {
      addImports(type);
      addImports(Types.resolveIOSType("IOSClass"));
    }
    return true;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    addImports(node.getVariableBinding().getType());
    return true;
  }

  @Override
  public boolean visit(TryStatement node) {
    if (node.getResources().size() > 0) {
      addImports(Types.mapTypeName("java.lang.Throwable"));
    }
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    addImports(type);
    addDeclaredType(type);
    return true;
  }

  @Override
  public boolean visit(TypeLiteral node) {
    ITypeBinding type = node.getType().getTypeBinding();
    if (type.isPrimitive()) {
      addImports(Types.resolveIOSType("IOSClass"));
    } else {
      addImports(node.getType());
    }
    return false;
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

  private boolean visitAnnotation(Annotation node) {
    IAnnotationBinding binding = node.getAnnotationBinding();
    boolean needsReflection = false;
    AbstractTypeDeclaration owningType = TreeUtil.getOwningType(node);
    if (owningType != null) {
      needsReflection = TranslationUtil.needsReflection(owningType);
    } else {
      needsReflection = TranslationUtil.needsReflection(
          TreeUtil.getNearestAncestorWithType(PackageDeclaration.class, node));
    }
    if (!BindingUtil.isRuntimeAnnotation(binding) || !needsReflection) {
      return false;
    }
    for (IMemberValuePairBinding memberValuePair : binding.getAllMemberValuePairs()) {
      if (memberValuePair.isDefault()) {
        Object value = memberValuePair.getValue();
        if (value instanceof IVariableBinding) {
          addImports(((IVariableBinding) value).getType());
        } else if (value instanceof ITypeBinding) {
          addImports((ITypeBinding) value);
        }
      }
    }
    return true;
  }
}
