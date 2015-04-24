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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Updates variable references outside an inner class to the new fields
 * injected into it.
 *
 * @author Keith Stanger
 */
public class OuterReferenceFixer extends TreeVisitor {

  private IVariableBinding outerParam = null;

  @Override
  public boolean visit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    if (binding.isConstructor()) {
      List<SingleVariableDeclaration> params = node.getParameters();
      if (params.size() > 0) {
        IVariableBinding firstParam = params.get(0).getVariableBinding();
        if (firstParam.getName().equals("outer$")) {
          outerParam = firstParam;
        }
      }
    }
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    outerParam = null;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    ITypeBinding newType = node.getTypeBinding().getTypeDeclaration();
    ITypeBinding declaringClass = newType.getDeclaringClass();
    if (Modifier.isStatic(newType.getModifiers()) || declaringClass == null) {
      return true;
    }

    GeneratedMethodBinding binding =
        new GeneratedMethodBinding(node.getMethodBinding().getMethodDeclaration());
    node.setMethodBinding(binding);

    List<Expression> captureArgs = node.getArguments().subList(0, 0);
    List<ITypeBinding> captureParams = binding.getParameters().subList(0, 0);
    if (OuterReferenceResolver.needsOuterParam(newType)) {
      captureArgs.add(getOuterArg(node, declaringClass));
      captureParams.add(declaringClass);
    }

    for (IVariableBinding capturedVar : getCapturedVariables(node)) {
      captureArgs.add(new SimpleName(capturedVar));
      captureParams.add(capturedVar.getType());
    }

    assert binding.isVarargs() || node.getArguments().size() == binding.getParameterTypes().length;
    return true;
  }

  private List<IVariableBinding> getCapturedVariables(ClassInstanceCreation node) {
    ITypeBinding newType = node.getTypeBinding().getTypeDeclaration();
    ITypeBinding owningType = TreeUtil.getOwningType(node).getTypeBinding().getTypeDeclaration();
    // Test for the recursive construction of a local class.
    if (owningType.isEqualTo(newType)) {
      return OuterReferenceResolver.getInnerFields(newType);
    }
    return OuterReferenceResolver.getCapturedVars(newType);
  }

  private Expression getOuterArg(ClassInstanceCreation node, ITypeBinding declaringClass) {
    Expression outerExpr = node.getExpression();
    if (outerExpr != null) {
      node.setExpression(null);
      return outerExpr;
    }
    List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
    if (path != null) {
      return Name.newName(fixPath(path));
    }
    return new ThisExpression(declaringClass);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
    if (path != null) {
      node.setExpression(Name.newName(fixPath(path)));
    }
    return true;
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
    if (path != null) {
      // We substitute the qualifying type name with the outer variable name.
      node.setQualifier(Name.newName(fixPath(path)));
    } else {
      node.setQualifier(null);
    }
  }

  @Override
  public boolean visit(SimpleName node) {
    List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
    if (path != null) {
      if (path.size() == 1 && path.get(0).getConstantValue() != null) {
        IVariableBinding var = path.get(0);
        node.replaceWith(TreeUtil.newLiteral(var.getConstantValue(), typeEnv));
      } else {
        node.replaceWith(Name.newName(fixPath(path)));
      }
    }
    return true;
  }

  @Override
  public boolean visit(ThisExpression node) {
    List<IVariableBinding> path = OuterReferenceResolver.getPath(node);
    if (path != null) {
      node.replaceWith(Name.newName(fixPath(path)));
    } else {
      node.setQualifier(null);
    }
    return true;
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    Expression outerExpression = node.getExpression();
    if (outerExpression == null) {
      return;
    }
    node.setExpression(null);
    ITypeBinding outerExpressionType = outerExpression.getTypeBinding();
    GeneratedMethodBinding binding =
        new GeneratedMethodBinding(node.getMethodBinding().getMethodDeclaration());
    node.setMethodBinding(binding);
    node.getArguments().add(0, outerExpression);
    binding.addParameter(0, outerExpressionType);
  }

  private List<IVariableBinding> fixPath(List<IVariableBinding> path) {
    if (path.get(0) == OuterReferenceResolver.OUTER_PARAMETER) {
      assert outerParam != null;
      path = Lists.newArrayList(path);
      path.set(0, outerParam);
    }
    return path;
  }
}
