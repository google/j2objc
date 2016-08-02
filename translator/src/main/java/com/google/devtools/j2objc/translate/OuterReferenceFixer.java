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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Updates variable references outside an inner class to the new fields
 * injected into it.
 *
 * @author Keith Stanger
 */
public class OuterReferenceFixer extends TreeVisitor {

  private final OuterReferenceResolver outerResolver;
  private VariableElement outerParam = null;

  public OuterReferenceFixer(OuterReferenceResolver outerResolver) {
    this.outerResolver = outerResolver;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (node.getMethodElement().getKind() == ElementKind.CONSTRUCTOR) {
      List<SingleVariableDeclaration> params = node.getParameters();
      if (params.size() > 0) {
        VariableElement firstParam = params.get(0).getVariableElement();
        if (firstParam.getSimpleName().toString().equals("outer$")) {
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
    TypeElement newType = (TypeElement) node.getExecutableElement().getEnclosingElement();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(newType);
    if (newType.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)
        || declaringClass == null) {
      return true;
    }
    List<TypeMirror> parameterTypes = new ArrayList<>();
    GeneratedExecutableElement element =
        new GeneratedExecutableElement(node.getExecutableElement());

    List<Expression> captureArgs = node.getArguments().subList(0, 0);
    if (outerResolver.needsOuterParam(BindingConverter.unwrapTypeElement(newType))) {
      captureArgs.add(getOuterArg(node, declaringClass.asType()));
      parameterTypes.add(declaringClass.asType());
    }

    for (List<VariableElement> captureArgPath : outerResolver.getCaptureArgPaths(node)) {
      captureArgPath = fixPath(captureArgPath);
      captureArgs.add(Name.newName(captureArgPath));
      parameterTypes.add(captureArgPath.get(captureArgPath.size() - 1).asType());
    }
    element.addParametersPlaceholderFront(parameterTypes);
    node.setExecutableElement(element);
    assert element.isVarArgs() || node.getArguments().size() == element.getParameters().size();
    return true;
  }

  private Expression getOuterArg(ClassInstanceCreation node, TypeMirror declaringClass) {
    Expression outerExpr = node.getExpression();
    if (outerExpr != null) {
      node.setExpression(null);
      return outerExpr;
    }
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      return Name.newName(fixPath(path));
    }
    return new ThisExpression(declaringClass);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      node.setExpression(Name.newName(fixPath(path)));
    }
    return true;
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    // Ignore default methods, they do not have outer paths.
    if (ElementUtil.isDefault(node.getExecutableElement())) {
      return;
    }
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      // We substitute the qualifying type name with the outer variable name.
      node.setQualifier(Name.newName(fixPath(path)));
    } else {
      node.setQualifier(null);
    }
  }

  @Override
  public boolean visit(SimpleName node) {
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      if (path.size() == 1 && path.get(0).getConstantValue() != null) {
        VariableElement var = path.get(0);
        node.replaceWith(TreeUtil.newLiteral(var.getConstantValue(), typeEnv));
      } else {
        node.replaceWith(Name.newName(fixPath(path)));
      }
    }
    return true;
  }

  @Override
  public boolean visit(ThisExpression node) {
    List<VariableElement> path = outerResolver.getPath(node);
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
    TypeMirror outerExpressionType = outerExpression.getTypeMirror();
    GeneratedExecutableElement element =
        new GeneratedExecutableElement(node.getExecutableElement());
    node.setExecutableElement(element);
    node.addArgument(0, outerExpression);
    element.addParameterPlaceholderFront(outerExpressionType);
  }

  private List<VariableElement> fixPath(List<VariableElement> path) {
    if (path.get(0) == OuterReferenceResolver.OUTER_PARAMETER) {
      assert outerParam != null;
      path = Lists.newArrayList(path);
      path.set(0, outerParam);
    }
    return path;
  }
}
