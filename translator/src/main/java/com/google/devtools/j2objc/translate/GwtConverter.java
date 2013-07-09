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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import java.util.List;
import java.util.Set;

/**
 * Updates the Java AST to remove code bound by GWT.isClient and
 * GWT.isScript tests, and translate GWT.create(Class) invocations
 * into Class.newInstance().
 *
 * @author Tom Ball
 */
public class GwtConverter extends ErrorReportingASTVisitor {

  private static final String GWT_CLASS = "com.google.gwt.core.client.GWT";

  /**
   * The list of APIs that can be translated by J2ObjC, but not by GWT. These
   * strings were found by scanning common sources like the Google Guava
   * library.
   *
   * Note: this list is neither exhaustive, nor guaranteed to match a
   * specified GwtIncompatible value, since it takes unchecked strings.
   */
  private static final Set<String> compatibleAPIs = Sets.newHashSet(
      "Array.newArray(Class, int)", "Array.newInstance(Class, int)", "Class.isInstance",
      "Class.isAssignableFrom", "CopyOnWriteArraySet", "InputStream", "java.io.BufferedReader",
      "java.io.Closeable,java.io.Flushable", "java.io.Writer", "java.lang.reflect",
      "java.lang.String.getBytes()", "java.lang.System#getProperty", "java.util.ArrayDeque",
      "java.util.BitSet", "java.util.Locale", "java.util.regex", "java.util.regex.Pattern",
      "java.util.String(byte[], Charset)", "MapMakerInternalMap", "NavigableMap", "NavigableAsMap",
      "NavigableSet", "Non-UTF-8 Charset", "OutputStream", "proto", "protos", "Readable",
      "Reader", "Reader,InputStream", "reflection", "regular expressions", "String.format()",
      "uses NavigableMap", "Writer", "Writer,OutputStream");

  @Override
  public boolean visit(ConditionalExpression node) {
    if (isGwtTest(node.getExpression())) {
      // Replace this node with the else expression, removing this conditional.
      ASTUtil.setProperty(node, NodeCopier.copySubtree(node.getAST(), node.getElseExpression()));
    }
    node.getElseExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(IfStatement node) {
    if (isGwtTest(node.getExpression())) {
      if (node.getElseStatement() != null) {
        // Replace this node with the else statement.
        ASTUtil.setProperty(node, NodeCopier.copySubtree(node.getAST(), node.getElseStatement()));
        node.getElseStatement().accept(this);
      } else {
        // No else statement, so remove this if statement or replace it
        // with an empty statement.
        ASTNode parent = node.getParent();
        if (parent instanceof Block) {
          ASTUtil.getStatements((Block) parent).remove(node);
        } else {
          ASTUtil.setProperty(node, node.getAST().newEmptyStatement());
        }
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (Options.stripGwtIncompatibleMethods()
        && hasAnnotation(GwtIncompatible.class, ASTUtil.getModifiers(node))) {
      // Remove method from its declaring class.
      ASTNode parent = node.getParent();
      if (parent instanceof TypeDeclarationStatement) {
        parent = ((TypeDeclarationStatement) parent).getDeclaration();
      }
      if (parent instanceof AbstractTypeDeclaration) {
        ((AbstractTypeDeclaration) parent).bodyDeclarations().remove(node);
      } else if (parent instanceof AnonymousClassDeclaration) {
        ((AnonymousClassDeclaration) parent).bodyDeclarations().remove(node);
      } else {
        throw new AssertionError("unknown parent type: " + parent.getClass().getSimpleName());
      }
    }
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    AST ast = node.getAST();
    IMethodBinding method = Types.getMethodBinding(node);
    List<Expression> args = ASTUtil.getArguments(node);
    if (method.getName().equals("create") &&
        method.getDeclaringClass().getQualifiedName().equals(GWT_CLASS) &&
        args.size() == 1) {
      // Convert GWT.create(Foo.class) to Foo.class.newInstance().
      SimpleName name = ast.newSimpleName("newInstance");
      node.setName(name);
      Expression clazz = NodeCopier.copySubtree(ast, args.get(0));
      args.remove(0);
      node.setExpression(clazz);
      IMethodBinding newBinding = BindingUtil.findDeclaredMethod(
          ast.resolveWellKnownType("java.lang.Class"), "newInstance");
      Types.addBinding(name, newBinding);
      Types.addBinding(node, newBinding);
    } else if (isGwtTest(node)) {
      BooleanLiteral falseLiteral = ASTFactory.newBooleanLiteral(ast, false);
      ASTUtil.setProperty(node, falseLiteral);
    }
    return true;
  }

  /**
   * Returns true if expression is a method invocation, and that
   * method refers to GWT.isClient() or GWT.isScript().
   *
   * NOTE: this method only checks for GWT.isClient() method invocations,
   * not any other boolean expression that might contain them.  For
   * example, !GWT.isClient() won't detect the method, since the binding
   * for that expression will be a boolean type rather than a method
   * binding.  The isGwtTest() call in visit(MethodInvocation) warns when
   * code like this is translated.
   */
  private boolean isGwtTest(Expression node) {
    IBinding binding = Types.getBinding(node);
    if (binding instanceof IMethodBinding) {
      IMethodBinding method = (IMethodBinding) binding;
      if (method.getDeclaringClass().getQualifiedName().equals(GWT_CLASS)) {
        String name = method.getName();
        return name.equals("isClient") || name.equals("isScript");
      }
    }
    return false;
  }

  private boolean hasAnnotation(Class<?> annotation, List<IExtendedModifier> modifiers) {
    // Annotation bindings don't have the annotation's package.
    String annotationName = annotation.getSimpleName();
    for (IExtendedModifier mod : modifiers) {
      if (mod.isAnnotation()) {
        Annotation annotationNode = (Annotation) mod;
        String modName = annotationNode.getTypeName().getFullyQualifiedName();
        if (modName.equals(annotationName)) {
          if (annotationName.equals("GwtIncompatible") &&
              annotationNode.isSingleMemberAnnotation()) {
            Expression value = ((SingleMemberAnnotation) annotationNode).getValue();
            if (value instanceof StringLiteral) {
              if (compatibleAPIs.contains(((StringLiteral) value).getLiteralValue())) {
                // Pretend incompatible annotation isn't present, since what it's
                // flagging is J2ObjC-compatible.
                return false;
              }
            }
          }
          return true;
        }
      }
    }
    return false;
  }
}
