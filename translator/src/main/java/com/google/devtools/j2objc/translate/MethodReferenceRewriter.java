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
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Arrays;
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
    IMethodBinding functionalInterface = exprBinding.getFunctionalInterfaceMethod();
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
    addRemainingLambdaParams(
        Arrays.asList(functionalInterface.getParameterTypes()), invocationArguments, lambda, null);
    node.replaceWith(lambda);
  }

  private void addRemainingLambdaParams(
      Iterable<ITypeBinding> paramTypes, List<Expression> invocationArguments,
      LambdaExpression lambda, char[] lastVar) {
    for (ITypeBinding paramType : paramTypes) {
      lastVar = NameTable.incrementVariable(lastVar);
      IVariableBinding variableBinding = new GeneratedVariableBinding(
          new String(lastVar), 0, paramType, false, true, null, null);
      lambda.getParameters().add(new VariableDeclarationFragment(variableBinding, null));
      invocationArguments.add(new SimpleName(variableBinding));
    }
  }
}
