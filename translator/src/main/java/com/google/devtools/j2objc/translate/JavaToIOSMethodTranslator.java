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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Translates invocations of mapped constructors to method invocation nodes.
 * Adds copyWithZone methods to Cloneable types.
 *
 * @author Tom Ball
 */
public class JavaToIOSMethodTranslator extends UnitTreeVisitor {

  private static final TypeMirror NSZONE_TYPE = new NativeType("NSZone *");

  private static final ExecutableElement RETAIN_METHOD =
      GeneratedExecutableElement.newMethodWithSelector(
          NameTable.RETAIN_METHOD, TypeUtil.ID_TYPE, TypeUtil.NS_OBJECT)
      .addModifiers(Modifier.PUBLIC);

  public JavaToIOSMethodTranslator(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    ExecutableElement method = node.getExecutableElement();

    // Check if @ObjectiveCName is used but is mismatched with an overriden method.
    String name = NameTable.getMethodNameFromAnnotation(method);
    if (name != null) {
      String selector = nameTable.selectorForMethodName(method, name);
      String actualSelector = nameTable.getMethodSelector(method);
      if (!selector.equals(actualSelector)) {
        ErrorUtil.warning("ObjectiveCName(" + selector
            + "): Renamed method overrides a method with a different name.");
      }
    }
    return true;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    // translate any embedded method invocations
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    for (Expression e : node.getArguments()) {
      e.accept(this);
    }
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }

    ExecutableElement method = node.getExecutableElement();
    String key = Mappings.getMethodKey(method, typeUtil);
    String selector = Mappings.STRING_CONSTRUCTOR_TO_METHOD_MAPPINGS.get(key);
    if (selector != null) {
      assert !node.hasRetainedResult();
      if (key.equals("java.lang.String.<init>(Ljava/lang/String;)V")) {
        // Special case: replace new String(constant) to constant (avoid clang warning).
        Expression arg = node.getArgument(0);
        if (arg instanceof StringLiteral) {
          node.replaceWith(arg.copy());
          return false;
        }
      }
      ExecutableElement newElement = GeneratedExecutableElement.newMappedMethod(selector, method);
      MethodInvocation newInvocation = new MethodInvocation(
          new ExecutablePair(newElement), new SimpleName(ElementUtil.getDeclaringClass(method)));
      TreeUtil.copyList(node.getArguments(), newInvocation.getArguments());

      node.replaceWith(newInvocation);
    }
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    // If this type implements Cloneable but its parent doesn't, add a
    // copyWithZone: method that calls clone().
    TypeElement type = node.getTypeElement();
    if (implementsCloneable(type.asType()) && !implementsCloneable(type.getSuperclass())) {
      addCopyWithZoneMethod(node, false);
    } else if (ElementUtil.getQualifiedName(type).equals("java.lang.Enum")) {
      addCopyWithZoneMethod(node, true);
    }
  }

  private boolean implementsCloneable(TypeMirror type) {
    return type != null && typeUtil.findSupertype(type, "java.lang.Cloneable") != null;
  }

  private void addCopyWithZoneMethod(TypeDeclaration node, boolean singleton) {
    TypeElement type = node.getTypeElement();

    // Create copyWithZone: method.
    GeneratedExecutableElement copyElement = GeneratedExecutableElement.newMethodWithSelector(
        "copyWithZone:", TypeUtil.ID_TYPE, type);
    MethodDeclaration copyDecl = new MethodDeclaration(copyElement);
    copyDecl.setHasDeclaration(false);

    // Add NSZone *zone parameter.
    VariableElement zoneParam =
        GeneratedVariableElement.newParameter("zone", NSZONE_TYPE, copyElement);
    copyElement.addParameter(zoneParam);
    copyDecl.addParameter(new SingleVariableDeclaration(zoneParam));

    Block block = new Block();
    copyDecl.setBody(block);

    if (singleton) {
      block.addStatement(new ReturnStatement(new ThisExpression(type.asType())));
    } else {
      ExecutableElement cloneElement = ElementUtil.findMethod(typeUtil.getJavaObject(), "clone");
      MethodInvocation invocation = new MethodInvocation(new ExecutablePair(cloneElement), null);
      if (options.useReferenceCounting()) {
        invocation = new MethodInvocation(new ExecutablePair(RETAIN_METHOD), invocation);
      }
      block.addStatement(new ReturnStatement(invocation));
    }

    node.addBodyDeclaration(copyDecl);
  }
}
