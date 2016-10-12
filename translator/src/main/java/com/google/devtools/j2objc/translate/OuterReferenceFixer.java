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
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Updates variable references outside an inner class to the new fields
 * injected into it.
 *
 * @author Keith Stanger
 */
public class OuterReferenceFixer extends TreeVisitor {

  private final CaptureInfo captureInfo;

  public OuterReferenceFixer(CaptureInfo captureInfo) {
    this.captureInfo = captureInfo;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    TypeElement newType = (TypeElement) node.getExecutableElement().getEnclosingElement();
    List<TypeMirror> parameterTypes = new ArrayList<>();
    List<Expression> outerArgs = node.getArguments().subList(0, 0);

    if (captureInfo.needsOuterParam(newType)) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(newType);
      outerArgs.add(getOuterArg(node, declaringClass.asType()));
      parameterTypes.add(captureInfo.getOuterType(newType));
    }

    Expression superOuterArg = TreeUtil.remove(node.getSuperOuterArg());
    if (superOuterArg != null) {
      outerArgs.add(superOuterArg);
      parameterTypes.add(superOuterArg.getTypeMirror());
    }

    for (Expression captureArg : node.getCaptureArgs()) {
      parameterTypes.add(captureArg.getTypeMirror());
    }
    TreeUtil.moveList(node.getCaptureArgs(), outerArgs);

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
    return new ThisExpression(declaringClass);
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    CommonTypeDeclaration typeDecl = TreeUtil.getEnclosingType(node);
    TypeElement superType = ElementUtil.getSuperclass(typeDecl.getTypeElement());
    List<Expression> args = node.getArguments().subList(0, 0);
    List<TypeMirror> parameterTypes = new ArrayList<>();

    // Outer arg.
    if (captureInfo.needsOuterParam(superType)) {
      Expression outerArg = TreeUtil.remove(node.getExpression());
      if (outerArg == null) {
        outerArg = typeDecl.getSuperOuter().copy();
      }
      args.add(outerArg);
      parameterTypes.add(captureInfo.getOuterType(superType));
    }

    // Capture args.
    for (Expression captureArg : typeDecl.getSuperCaptureArgs()) {
      parameterTypes.add(captureArg.getTypeMirror());
    }
    TreeUtil.moveList(typeDecl.getSuperCaptureArgs(), args);

    if (!parameterTypes.isEmpty()) {
      GeneratedExecutableElement element =
          new GeneratedExecutableElement(node.getExecutableElement());
      element.addParametersPlaceholderFront(parameterTypes);
      node.setExecutableElement(element);
      assert element.isVarArgs() || node.getArguments().size() == element.getParameters().size();
    }
  }
}
