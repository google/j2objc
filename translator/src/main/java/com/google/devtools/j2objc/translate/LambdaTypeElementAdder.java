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

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.FunctionalExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.types.LambdaTypeElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Adds LambdaTypeElement instances to LambdaExpression and MethodReference nodes.
 *
 * @author Keith Stanger
 */
public class LambdaTypeElementAdder extends UnitTreeVisitor {

  private Map<TypeElement, Integer> lambdaCounts = new HashMap<>();

  public LambdaTypeElementAdder(CompilationUnit unit) {
    super(unit);
  }

  private String getLambdaUniqueName(FunctionalExpression node) {
    TypeElement enclosingType = TreeUtil.getEnclosingTypeElement(node);
    Integer count = lambdaCounts.get(enclosingType);
    if (count == null) {
      count = 0;
    }
    lambdaCounts.put(enclosingType, ++count);
    return "$Lambda$" + count;
  }

  private boolean handleFunctionalExpression(FunctionalExpression node) {
    LambdaTypeElement elem = new LambdaTypeElement(
        getLambdaUniqueName(node), TreeUtil.getEnclosingElement(node),
        typeUtil.getJavaObject().asType(), isWeakOuter(node));
    elem.addInterfaces(node.getTargetTypes());
    node.setTypeElement(elem);
    return true;
  }

  private boolean isWeakOuter(FunctionalExpression node) {
    VariableElement var = getAssignedVariable(node);
    return var != null && ElementUtil.hasNamedAnnotation(var, "WeakOuter");
  }

  private VariableElement getAssignedVariable(FunctionalExpression node) {
    TreeNode parent = node.getParent();
    if (parent instanceof Assignment) {
      return TreeUtil.getVariableElement(((Assignment) parent).getLeftHandSide());
    } else if (parent instanceof VariableDeclaration) {
      return ((VariableDeclaration) parent).getVariableElement();
    }
    return null;
  }

  @Override
  public boolean visit(LambdaExpression node) {
    return handleFunctionalExpression(node);
  }

  @Override
  public boolean visit(CreationReference node) {
    return handleFunctionalExpression(node);
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    return handleFunctionalExpression(node);
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    return handleFunctionalExpression(node);
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    return handleFunctionalExpression(node);
  }
}
