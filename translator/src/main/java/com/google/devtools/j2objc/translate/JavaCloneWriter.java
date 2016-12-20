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
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Writes the __javaClone method in order to support correct Java clone()
 * behavior.
 *
 * @author Keith Stanger
 */
public class JavaCloneWriter extends UnitTreeVisitor {

  private static final String JAVA_CLONE_METHOD = "__javaClone:";

  private final ExecutableElement releaseMethod =
      GeneratedExecutableElement.newMethodWithSelector(
          NameTable.RELEASE_METHOD, typeUtil.getVoid(), TypeUtil.NS_OBJECT)
      .addModifiers(Modifier.PUBLIC);

  public JavaCloneWriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    VariableElement originalVar =
        GeneratedVariableElement.newParameter("original", type.asType(), null);
    List<Statement> adjustments = getFieldAdjustments(node, originalVar);
    if (adjustments.isEmpty()) {
      return;
    }

    TypeMirror voidType = typeUtil.getVoid();
    ExecutableElement javaCloneElement =
        GeneratedExecutableElement.newMethodWithSelector(JAVA_CLONE_METHOD, voidType, type)
        .addParameter(originalVar);

    MethodDeclaration declaration = new MethodDeclaration(javaCloneElement);
    declaration.setHasDeclaration(false);
    node.addBodyDeclaration(declaration);
    declaration.addParameter(new SingleVariableDeclaration(originalVar));

    Block body = new Block();
    declaration.setBody(body);
    List<Statement> statements = body.getStatements();

    ExecutableElement javaCloneSuperElement = GeneratedExecutableElement.newMethodWithSelector(
        JAVA_CLONE_METHOD, voidType, typeUtil.getJavaObject());
    SuperMethodInvocation superCall =
        new SuperMethodInvocation(new ExecutablePair(javaCloneSuperElement));
    superCall.addArgument(new SimpleName(originalVar));
    statements.add(new ExpressionStatement(superCall));

    statements.addAll(adjustments);
  }

  private List<Statement> getFieldAdjustments(TypeDeclaration node, VariableElement originalVar) {
    List<Statement> adjustments = Lists.newArrayList();
    for (VariableDeclarationFragment decl : TreeUtil.getAllFields(node)) {
      VariableElement var = decl.getVariableElement();
      if (ElementUtil.isStatic(var) || var.asType().getKind().isPrimitive()) {
        continue;
      }
      boolean isWeak = ElementUtil.isWeakReference(var);
      boolean isVolatile = ElementUtil.isVolatile(var);
      if (isVolatile) {
        adjustments.add(createVolatileCloneStatement(var, originalVar, isWeak));
      } else if (isWeak) {
        adjustments.add(createReleaseStatement(var));
      }
    }
    return adjustments;
  }

  private Statement createReleaseStatement(VariableElement var) {
    if (options.useARC()) {
      TypeMirror voidType = typeUtil.getVoid();
      FunctionElement element = new FunctionElement("JreRelease", voidType, null)
          .addParameters(TypeUtil.ID_TYPE);
      FunctionInvocation invocation = new FunctionInvocation(element, voidType);
      invocation.addArgument(new SimpleName(var));
      return new ExpressionStatement(invocation);
    } else {
      return new ExpressionStatement(
          new MethodInvocation(new ExecutablePair(releaseMethod), new SimpleName(var)));
    }
  }

  private Statement createVolatileCloneStatement(
      VariableElement var, VariableElement originalVar, boolean isWeak) {
    TypeMirror voidType = typeUtil.getVoid();
    TypeMirror pointerType = new PointerType(var.asType());
    String funcName = "JreCloneVolatile" + (isWeak ? "" : "Strong");
    FunctionElement element = new FunctionElement(funcName, voidType, null)
        .addParameters(pointerType, pointerType);
    FunctionInvocation invocation = new FunctionInvocation(element, voidType);
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF, new SimpleName(var)));
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF,
        new FieldAccess(var, new SimpleName(originalVar))));
    return new ExpressionStatement(invocation);
  }
}
