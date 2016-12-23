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
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionalExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites LambdaExpression nodes into TypeDeclarations.
 *
 * @author Nathan Braswell, Keith Stanger
 */
public class LambdaRewriter extends UnitTreeVisitor {

  private final CaptureInfo captureInfo;

  public LambdaRewriter(CompilationUnit unit) {
    super(unit);
    this.captureInfo = unit.getEnv().captureInfo();
  }

  private class RewriteContext {

    private final FunctionalExpression node;
    private final TypeElement lambdaType;
    private final TypeMirror typeMirror;
    private ExecutableElement functionalInterface;
    private ExecutableType functionalInterfaceType;
    private TypeDeclaration typeDecl;
    private GeneratedExecutableElement implElement;
    private MethodDeclaration implDecl;
    private ClassInstanceCreation creation;

    private RewriteContext(FunctionalExpression node) {
      this.node = node;
      lambdaType = node.getTypeElement();
      typeMirror = node.getTypeMirror();
      resolveFunctionalInterface();
      createTypeDeclaration();
      createImplementation();
      createCreation();
      removeCastExpression();
      replaceNode();
    }

    public void resolveFunctionalInterface() {
      typeUtil.visitTypeHierarchy(typeMirror, baseType -> {
        TypeElement element = (TypeElement) baseType.asElement();
        if (element.getKind().isClass()) {
          return true;
        }
        for (ExecutableElement method : ElementUtil.getMethods(element)) {
          if (!ElementUtil.isDefault(method) && !ElementUtil.isStatic(method)) {
            functionalInterface = method;
            functionalInterfaceType = typeUtil.asMemberOf(baseType, method);
            return false;
          }
        }
        return true;
      });
      assert functionalInterface != null : "Could not find functional interface for " + typeMirror;
    }

    private void createTypeDeclaration() {
      typeDecl = new TypeDeclaration(lambdaType);
      typeDecl.setSourceRange(node.getStartPosition(), node.getLength());
      TreeUtil.getEnclosingTypeBodyDeclarations(node).add(typeDecl);
    }

    private void createImplementation() {
      String selector = nameTable.getMethodSelector(functionalInterface);
      implElement = GeneratedExecutableElement.newMethodWithSelector(
          selector, functionalInterface.getReturnType(), lambdaType);
      implDecl = new MethodDeclaration(implElement);
      typeDecl.addBodyDeclaration(implDecl);
    }

    private void createCreation() {
      ExecutableElement constructorElement =
          GeneratedExecutableElement.newConstructor(lambdaType, typeUtil);

      // Add the implicit constructor to call.
      MethodDeclaration constructorDecl = new MethodDeclaration(constructorElement);
      constructorDecl.setBody(new Block());
      constructorDecl.getBody().addStatement(new SuperConstructorInvocation(
          new ExecutablePair(getObjectConstructor())));
      typeDecl.addBodyDeclaration(constructorDecl);

      creation = new ClassInstanceCreation(
          new ExecutablePair(constructorElement), lambdaType.asType());
      creation.setExpression(TreeUtil.remove(node.getLambdaOuterArg()));
      TreeUtil.moveList(node.getLambdaCaptureArgs(), creation.getCaptureArgs());
    }

    private void removeCastExpression() {
      TreeNode parent = node.getParent();
      if (parent instanceof CastExpression) {
        parent.replaceWith(TreeUtil.remove(node));
      }
    }

    private void replaceNode() {
      if (captureInfo.isCapturing(lambdaType)) {
        node.replaceWith(creation);
      } else {
        // For non-capturing lambdas, create a static final instance.
        VariableElement instanceVar = GeneratedVariableElement.newField(
            "instance", lambdaType.asType(), lambdaType)
            .addModifiers(Modifier.STATIC, Modifier.FINAL);
        typeDecl.addBodyDeclaration(new FieldDeclaration(instanceVar, creation));
        node.replaceWith(new SimpleName(instanceVar));
      }
    }

    private void setImplementationBody(TreeNode body) {
      implDecl.setBody(
          body instanceof Block ? (Block) body : asImplementationBlock((Expression) body));
    }

    private Block asImplementationBlock(Expression expr) {
      Block block = new Block();
      if (TypeUtil.isVoid(functionalInterface.getReturnType())) {
        block.addStatement(new ExpressionStatement(expr));
      } else {
        block.addStatement(new ReturnStatement(expr));
      }
      return block;
    }

    private void rewriteLambdaExpression(LambdaExpression node) {
      for (VariableDeclaration decl : node.getParameters()) {
        VariableElement var = decl.getVariableElement();
        implElement.addParameter(var);
        implDecl.addParameter(new SingleVariableDeclaration(var));
      }
      setImplementationBody(TreeUtil.remove(node.getBody()));
    }

    private Iterator<VariableElement> createParameters() {
      List<? extends TypeMirror> paramTypes = functionalInterfaceType.getParameterTypes();
      List<VariableElement> params = new ArrayList<>(paramTypes.size());
      int i = 0;
      for (TypeMirror type : paramTypes) {
        GeneratedVariableElement param = GeneratedVariableElement.newParameter(
            getParamName(i++), type, implElement);
        params.add(param);
        implElement.addParameter(param);
        implDecl.addParameter(new SingleVariableDeclaration(param));
      }
      return params.iterator();
    }

    private void forwardRemainingArgs(Iterator<VariableElement> params, List<Expression> args) {
      while (params.hasNext()) {
        args.add(new SimpleName(params.next()));
      }
    }

    private void rewriteCreationReference(CreationReference node) {
      TypeMirror creationType = node.getType().getTypeMirror();
      if (TypeUtil.isArray(creationType)) {
        ArrayCreation creation = new ArrayCreation((ArrayType) creationType, typeUtil);
        forwardRemainingArgs(createParameters(), creation.getDimensions());
        setImplementationBody(creation);
      } else {
        ClassInstanceCreation creation =
            new ClassInstanceCreation(new ExecutablePair(node.getExecutableElement()), creationType)
            .setVarargsType(node.getVarargsType());
        forwardRemainingArgs(createParameters(), creation.getArguments());
        creation.setExpression(TreeUtil.remove(node.getCreationOuterArg()));
        TreeUtil.moveList(node.getCreationCaptureArgs(), creation.getCaptureArgs());
        setImplementationBody(creation);
      }
    }

    private void rewriteExpressionMethodReference(ExpressionMethodReference node) {
      ExecutableElement method = node.getExecutableElement();
      Iterator<VariableElement> params = createParameters();
      Expression invocationTarget = null;

      if (!ElementUtil.isStatic(method)) {
        VariableElement receiverField = captureInfo.getReceiverField(lambdaType);
        if (receiverField != null) {
          invocationTarget = new SimpleName(receiverField);
          creation.setExpression(TreeUtil.remove(node.getExpression()));
        } else {
          // The expression is actually a type name and doesn't evaluate to an invocable object.
          invocationTarget = new SimpleName(params.next());
        }
      }

      MethodInvocation invocation =
          new MethodInvocation(new ExecutablePair(method), invocationTarget)
          .setVarargsType(node.getVarargsType());
      forwardRemainingArgs(params, invocation.getArguments());
      setImplementationBody(invocation);
    }

    private void rewriteSuperMethodReference(SuperMethodReference node) {
      SuperMethodInvocation invocation =
          new SuperMethodInvocation(new ExecutablePair(node.getExecutableElement()))
          .setVarargsType(node.getVarargsType());
      invocation.setReceiver(TreeUtil.remove(node.getReceiver()));
      forwardRemainingArgs(createParameters(), invocation.getArguments());
      setImplementationBody(invocation);
    }

    private void rewriteTypeMethodReference(TypeMethodReference node) {
      ExecutableElement method = node.getExecutableElement();
      Iterator<VariableElement> params = createParameters();
      MethodInvocation invocation = new MethodInvocation(
          new ExecutablePair(method),
          ElementUtil.isStatic(method) ? null : new SimpleName(params.next()))
          .setVarargsType(node.getVarargsType());
      forwardRemainingArgs(params, invocation.getArguments());
      setImplementationBody(invocation);
    }
  }

  @Override
  public void endVisit(LambdaExpression node) {
    new RewriteContext(node).rewriteLambdaExpression(node);
  }

  @Override
  public void endVisit(CreationReference node) {
    new RewriteContext(node).rewriteCreationReference(node);
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    new RewriteContext(node).rewriteExpressionMethodReference(node);
  }

  @Override
  public void endVisit(SuperMethodReference node) {
    new RewriteContext(node).rewriteSuperMethodReference(node);
  }

  @Override
  public void endVisit(TypeMethodReference node) {
    new RewriteContext(node).rewriteTypeMethodReference(node);
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

  private ExecutableElement getObjectConstructor() {
    for (ExecutableElement constructor : ElementUtil.getConstructors(typeUtil.getJavaObject())) {
      if (constructor.getParameters().isEmpty()) {
        return constructor;
      }
    }
    throw new AssertionError("Can't find constructor for java.lang.Object.");
  }
}
