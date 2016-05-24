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

import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rewrites MethodReference nodes into equivalent LambdaExpression nodes.
 *
 * @author Keith Stanger, Seth Kirby
 */
public class MethodReferenceRewriter extends TreeVisitor {

  @Override
  public void endVisit(CreationReference node) {
    ITypeBinding exprBinding = node.getTypeBinding();
    ITypeBinding creationType = node.getType().getTypeBinding();
    Expression invocation;
    List<Expression> invocationArguments;
    if (creationType.isArray()) {
      ArrayCreation arrayCreation = new ArrayCreation(creationType, typeEnv);
      invocation = arrayCreation;
      invocationArguments = arrayCreation.getDimensions();
    } else {
      ClassInstanceCreation classCreation =
          new ClassInstanceCreation(node.getMethodBinding(), Type.newType(creationType));
      invocation = classCreation;
      invocationArguments = classCreation.getArguments();
    }
    LambdaExpression lambda = new LambdaExpression(
        "CreationReference:" + node.getLineNumber(), exprBinding);
    lambda.setBody(invocation);
    addParamsToInvocation(createParameters(lambda), invocationArguments);
    node.replaceWith(lambda);
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    ITypeBinding exprBinding = node.getTypeBinding();
    IMethodBinding methodBinding = node.getMethodBinding();
    LambdaExpression lambda = new LambdaExpression(
        "ExpressionMethodReference:" + node.getLineNumber(), exprBinding);
    Iterator<IVariableBinding> params = createParameters(lambda);
    Expression target = TreeUtil.remove(node.getExpression());
    if (!BindingUtil.isStatic(methodBinding) && target instanceof Name
        && ((Name) target).getBinding().getKind() == IBinding.TYPE) {
      // The expression is actually a type name and doesn't evaluate to an invocable object.
      target = new SimpleName(params.next());
    }
    MethodInvocation invocation = new MethodInvocation(methodBinding, target);
    lambda.setBody(invocation);
    addParamsToInvocation(params, invocation.getArguments());
    node.replaceWith(lambda);
  }

  @Override
  public void endVisit(TypeMethodReference node) {
    ITypeBinding exprBinding = node.getTypeBinding();
    IMethodBinding methodBinding = getMethodBinding(node);
    LambdaExpression lambda = new LambdaExpression(
        "TypeMethodReference:" + node.getLineNumber(), exprBinding);
    Iterator<IVariableBinding> params = createParameters(lambda);
    Expression target = null;
    if (!BindingUtil.isStatic(methodBinding)) {
      target = new SimpleName(params.next());
    }
    MethodInvocation invocation = new MethodInvocation(methodBinding, target);
    lambda.setBody(invocation);
    addParamsToInvocation(params, invocation.getArguments());
    node.replaceWith(lambda);
  }

  private IMethodBinding getMethodBinding(TypeMethodReference node) {
    if (node.getType().getTypeBinding().isArray()) {
      // JDT does not provide the correct method binding on array types, so we find it from
      // java.lang.Object.
      String name = node.getName().getIdentifier();
      IMethodBinding functionalInterface = node.getTypeBinding().getFunctionalInterfaceMethod();
      int numParams = functionalInterface.getParameterTypes().length - 1;
      for (IMethodBinding method : typeEnv.getJavaObject().getDeclaredMethods()) {
        if (method.getName().equals(name) && method.getParameterTypes().length == numParams) {
          return method;
        }
      }
      throw new AssertionError("Can't find method finding for method: " + name);
    }
    return node.getMethodBinding();
  }

  @Override
  public void endVisit(SuperMethodReference node) {
    ITypeBinding exprBinding = node.getTypeBinding();
    LambdaExpression lambda = new LambdaExpression(
        "SuperMethodReference:" + node.getLineNumber(), exprBinding);
    SuperMethodInvocation invocation = new SuperMethodInvocation(node.getMethodBinding());
    invocation.setQualifier(TreeUtil.remove(node.getQualifier()));
    lambda.setBody(invocation);
    addParamsToInvocation(createParameters(lambda), invocation.getArguments());
    node.replaceWith(lambda);
  }

  private Iterator<IVariableBinding> createParameters(LambdaExpression lambda) {
    IMethodBinding functionalInterface = lambda.getTypeBinding().getFunctionalInterfaceMethod();
    ITypeBinding[] paramTypes = functionalInterface.getParameterTypes();
    List<IVariableBinding> params = new ArrayList<>(paramTypes.length);
    for (int i = 0; i < paramTypes.length; i++) {
      GeneratedVariableBinding param = new GeneratedVariableBinding(
          getParamName(i), 0, paramTypes[i], false, true, null, null);
      params.add(param);
      lambda.getParameters().add(new VariableDeclarationFragment(param, null));
    }
    return params.iterator();
  }

  private void addParamsToInvocation(
      Iterator<IVariableBinding> params, List<Expression> invocationArguments) {
    while (params.hasNext()) {
      invocationArguments.add(new SimpleName(params.next()));
    }
  }

  private static String getParamName(int i) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      sb.append((char) ('a' + (i % 26)));
      i = i / 26;
      if (i == 0) {
        break;
      }
      i--;
    }
    return sb.reverse().toString();
  }
}
