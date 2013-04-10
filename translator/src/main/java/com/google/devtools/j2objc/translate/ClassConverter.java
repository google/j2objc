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
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.TypeTrackingVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

/**
 * Base class for the anonymous class converter and inner class extractor,
 * containing shared methods.
 *
 * @author Tom Ball
 */
public abstract class ClassConverter extends TypeTrackingVisitor {
  protected final CompilationUnit unit;

  protected ClassConverter(CompilationUnit unit) {
    this.unit = unit;
  }

  /**
   * Returns a list of inner variables that need to be added to an inner type,
   * to resolve its references to outer classes.
   */
  protected List<IVariableBinding> getInnerVars(List<ReferenceDescription> references) {
    List<IVariableBinding> innerVars = Lists.newArrayList();
    outer: for (ReferenceDescription desc : references) {
      ITypeBinding declaringClass = desc.declaringClass;
      if (declaringClass == null) {
        declaringClass = desc.binding.getType();
      }
      declaringClass = declaringClass.getTypeDeclaration();
      if (desc.binding.isField()) {
        // Combine references to a type and its supertypes.
        for (int i = 0; i < innerVars.size(); i++) {
          IVariableBinding var = innerVars.get(i);
          ITypeBinding varType = var.getDeclaringClass();
          if (varType != null && varType.isAssignmentCompatible(declaringClass)) {
            desc.declaringClass = varType;
            continue outer;
          } else if (varType == null) {
            desc.declaringMethod = var.getDeclaringMethod();
          }
        }
      }
      if (!innerVars.contains(desc.binding)) {
        innerVars.add(desc.binding);
      }
    }
    return innerVars;
  }

  protected FieldDeclaration createField(String name, ITypeBinding varType,
       ITypeBinding declaringClass, AST ast) {
    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
    SimpleName fieldName = ast.newSimpleName(name);
    GeneratedVariableBinding fieldBinding = new GeneratedVariableBinding(
        fieldName.getIdentifier(), Modifier.PRIVATE | Modifier.FINAL, varType,
        true, false, declaringClass, null);
    Types.addBinding(fieldName, fieldBinding);
    fragment.setName(fieldName);
    Types.addBinding(fragment, fieldBinding);

    FieldDeclaration field = ast.newFieldDeclaration(fragment);
    field.setType(Types.makeType(varType));
    @SuppressWarnings("unchecked")
    List<IExtendedModifier> mods = field.modifiers(); // safe by definition
    mods.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
    mods.add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
    return field;
  }

  protected SimpleName makeFieldRef(IVariableBinding newVar, AST ast) {
    SimpleName fieldRef = ast.newSimpleName(newVar.getName());
    Types.addBinding(fieldRef, newVar);
    return fieldRef;
  }

  @SuppressWarnings("unchecked")
  public static void setProperty(ASTNode node, Expression expr) {
    ASTNode parent = node.getParent();
    StructuralPropertyDescriptor locator = node.getLocationInParent();
    if (locator instanceof ChildPropertyDescriptor) {
      parent.setStructuralProperty(locator, expr);
    } else {
      // JDT doesn't directly support ChildListProperty replacement.
      List<Expression> args;
      if (parent instanceof MethodInvocation) {
        args = ((MethodInvocation) parent).arguments();
      } else if (parent instanceof ConstructorInvocation) {
        args = ((ConstructorInvocation) parent).arguments();
      } else if (parent instanceof ClassInstanceCreation) {
        args = ((ClassInstanceCreation) parent).arguments();
      } else if (parent instanceof InfixExpression) {
        args = ((InfixExpression) parent).extendedOperands();
      } else if (parent instanceof SynchronizedStatement) {
        SynchronizedStatement stmt = (SynchronizedStatement) parent;
        if (node.equals(stmt.getExpression())) {
          stmt.setExpression((Expression) node);
        }
        return;
      } else if (parent instanceof SuperConstructorInvocation) {
        args = ((SuperConstructorInvocation) parent).arguments();
      } else if (parent instanceof ArrayCreation) {
        args = ((ArrayCreation) parent).dimensions();
      } else if (parent instanceof ArrayInitializer) {
        args = ((ArrayInitializer) parent).expressions();
      } else if (parent instanceof EnumConstantDeclaration) {
        args = ((EnumConstantDeclaration) parent).arguments();
      } else {
        throw new AssertionError("unknown parent node type: " + parent.getClass().getSimpleName());
      }
      for (int i = 0; i < args.size(); i++) {
        if (node.equals(args.get(i))) {
          args.set(i, expr);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void setProperty(ASTNode node, Statement stmt) {
    ASTNode parent = node.getParent();
    StructuralPropertyDescriptor locator = node.getLocationInParent();
    if (locator instanceof ChildPropertyDescriptor) {
      parent.setStructuralProperty(locator, stmt);
    } else {
      // JDT doesn't directly support ChildListProperty replacement.
      List<Statement> args;
      if (parent instanceof Block) {
        args = ((Block) parent).statements();
      } else if (parent instanceof SwitchStatement) {
        args = ((SwitchStatement) parent).statements();
      } else {
        throw new AssertionError("unknown parent node type: " + parent.getClass().getSimpleName());
      }
      for (int i = 0; i < args.size(); i++) {
        if (node.equals(args.get(i))) {
          args.set(i, stmt);
        }
      }
    }
  }
}
