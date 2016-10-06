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

import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CommonTypeDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayList;
import java.util.List;
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

  public OuterReferenceFixer(OuterReferenceResolver outerResolver) {
    this.outerResolver = outerResolver;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    TypeElement newType = (TypeElement) node.getExecutableElement().getEnclosingElement();
    List<TypeMirror> parameterTypes = new ArrayList<>();
    List<Expression> captureArgs = node.getArguments().subList(0, 0);

    if (outerResolver.needsOuterParam(newType)) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(newType);
      captureArgs.add(getOuterArg(node, declaringClass.asType()));
      parameterTypes.add(outerResolver.getOuterType(newType));
    }

    Expression superOuterArg = TreeUtil.remove(node.getSuperOuterArg());
    if (superOuterArg != null) {
      captureArgs.add(superOuterArg);
      parameterTypes.add(superOuterArg.getTypeMirror());
    }

    for (List<VariableElement> captureArgPath : outerResolver.getCaptureArgPaths(node)) {
      captureArgs.add(Name.newName(captureArgPath));
      parameterTypes.add(captureArgPath.get(captureArgPath.size() - 1).asType());
    }

    if (!parameterTypes.isEmpty()) {
      GeneratedExecutableElement element =
          new GeneratedExecutableElement(node.getExecutableElement());
      element.addParametersPlaceholderFront(parameterTypes);
      node.setExecutableElement(element);
      assert element.isVarArgs() || node.getArguments().size() == element.getParameters().size();
    }
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
      return Name.newName(path);
    }
    return new ThisExpression(declaringClass);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      node.setExpression(Name.newName(path));
    }
    return true;
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      // We substitute the qualifying type name with the outer variable name.
      node.setQualifier(Name.newName(path));
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
        node.replaceWith(Name.newName(path));
      }
    }
    return true;
  }

  @Override
  public boolean visit(ThisExpression node) {
    List<VariableElement> path = outerResolver.getPath(node);
    if (path != null) {
      node.replaceWith(Name.newName(path));
    } else {
      node.setQualifier(null);
    }
    return true;
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    CommonTypeDeclaration typeDecl = TreeUtil.getEnclosingType(node);
    TypeElement superType = ElementUtil.getSuperclass(typeDecl.getTypeElement());
    TreeNode typeNode = typeDecl.asNode();
    List<Expression> args = node.getArguments().subList(0, 0);
    List<TypeMirror> parameterTypes = new ArrayList<>();

    // Outer arg.
    if (outerResolver.needsOuterParam(superType)) {
      Expression outerArg = TreeUtil.remove(node.getExpression());
      if (outerArg == null) {
        outerArg = typeDecl.getSuperOuter().copy();
      }
      args.add(outerArg);
      parameterTypes.add(outerResolver.getOuterType(superType));
    }

    // Capture args.
    for (List<VariableElement> captureArgPath : outerResolver.getCaptureArgPaths(typeNode)) {
      args.add(Name.newName(captureArgPath));
      parameterTypes.add(captureArgPath.get(captureArgPath.size() - 1).asType());
    }

    if (!parameterTypes.isEmpty()) {
      GeneratedExecutableElement element =
          new GeneratedExecutableElement(node.getExecutableElement());
      element.addParametersPlaceholderFront(parameterTypes);
      node.setExecutableElement(element);
      assert element.isVarArgs() || node.getArguments().size() == element.getParameters().size();
    }
  }
}
