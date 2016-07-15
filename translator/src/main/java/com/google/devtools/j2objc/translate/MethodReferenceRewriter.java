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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.lang.reflect.Modifier;
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

  private int emtCount = 0;

  @Override
  public void endVisit(ExpressionMethodReference node) {
    ITypeBinding exprBinding = node.getTypeBinding();
    IMethodBinding methodBinding = node.getMethodBinding();
    LambdaExpression lambda = new LambdaExpression(
        "ExpressionMethodReference:" + node.getLineNumber(), exprBinding);
    Iterator<IVariableBinding> params = createParameters(lambda);
    Expression target = TreeUtil.remove(node.getExpression());

    // There are a couple of cases to deal with here. We need to make sure
    // that we don't bring the target of the method into the lambda if that
    // target has side effects, as those side effects should only happen at
    // the location of the original method reference, not every time the lambda
    // is run. To do this, we check to see if the expression is a Name or ThisExpression
    // in which case we just pull it into the lambda. If not, we make a temporary
    // variable for the result of evaluating the target, then evaluate the target and
    // assign the result to a temporary in a CommaExpression that also contains the
    // lambda. By doing it this way, the side effects should happen only right before
    // lambda creation, fitting with expected behavior of the original method reference.

    // This is slightly more complicated in the case where the lambda is part of the
    // initializer of a field in a class (class T { F f = new Q::q; }). In this case
    // we pull the creation of the temporary and lambda initialization out into an
    // initializer block for that class and make it static if the member is static.
    Expression callExpression = null;

    if (!BindingUtil.isStatic(methodBinding) && target instanceof Name
        && ElementUtil.isType(((Name) target).getElement())) {
      // The expression is actually a type name and doesn't evaluate to an invocable object.
      target = new SimpleName(params.next());
      callExpression = lambda;
    } else if (isFinalExpression(target)) {
      callExpression = lambda;
    } else {
      // We check to see if the method reference is in a class member
      // initialization. If it is we create a new Initializer and block to
      // put the temporary in.
      FieldDeclaration fieldDeclaration = TreeUtil.getNearestAncestorWithType(
          FieldDeclaration.class, node);
      // this is a class member declaration, we have to put everything into an
      // Initializer instead
      if (fieldDeclaration != null) {
        extractFieldInitializers(fieldDeclaration);
      }
      GeneratedVariableBinding variableBinding = new GeneratedVariableBinding(
          "__emt$" + emtCount++, 0, target.getTypeBinding(), false, false,
          TreeUtil.getEnclosingTypeBinding(node), TreeUtil.getEnclosingMethodBinding(node));
      VariableDeclarationStatement variableDeclaration = new VariableDeclarationStatement(
          variableBinding, null);
      TreeUtil.asStatementList(TreeUtil.getOwningStatement(node)).add(0, variableDeclaration);
      Assignment assignment = new Assignment(new SimpleName(variableBinding), target);
      target = new SimpleName(variableBinding);
      callExpression = new CommaExpression(assignment, lambda);
    }

    MethodInvocation invocation = new MethodInvocation(methodBinding, target);
    lambda.setBody(invocation);
    addParamsToInvocation(params, invocation.getArguments());
    node.replaceWith(callExpression);
  }

  private void extractFieldInitializers(FieldDeclaration decl) {
    Initializer init = new Initializer(new Block(),
        Modifier.isStatic(decl.getModifiers()));
    TreeUtil.asDeclarationSublist(decl).add(init);
    for (VariableDeclarationFragment memberDeclaration : decl.getFragments()) {
      IVariableBinding memberVar = memberDeclaration.getVariableBinding();
      Assignment assignment = new Assignment(new SimpleName(memberVar),
          TreeUtil.remove(memberDeclaration.getInitializer()));
      init.getBody().addStatement(new ExpressionStatement(assignment));
    }
  }

  private boolean isFinalExpression(Expression expr) {
    if (expr instanceof ThisExpression) {
      return true;
    }
    if (expr instanceof Name && ElementUtil.isType(((Name) expr).getElement())) {
      return true;
    }
    IVariableBinding var = TreeUtil.getVariableBinding(expr);
    return var != null && BindingUtil.isFinal(var);
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
      lambda.addParameter(new VariableDeclarationFragment(param, null));
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
