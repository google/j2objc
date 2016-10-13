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
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites LambdaExpression nodes into TypeDeclarations.
 *
 * @author Nathan Braswell, Keith Stanger
 */
public class LambdaRewriter extends UnitTreeVisitor {

  private final CaptureInfo captureInfo;

  public LambdaRewriter(CompilationUnit unit, CaptureInfo captureInfo) {
    super(unit);
    this.captureInfo = captureInfo;
  }

  private class RewriteContext {

    private final FunctionalExpression node;
    private final TypeElement lambdaType;
    private final TypeMirror typeMirror;
    private ExecutableElement functionalInterface;
    private ExecutableType functionalInterfaceType;
    private TypeDeclaration typeDecl;
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
      List<DeclaredType> declaredTypes =
          ElementUtil.getInheritedDeclaredTypesInclusive(typeMirror, env);
      for (DeclaredType baseType : declaredTypes) {
        TypeElement element = (TypeElement) baseType.asElement();
        for (Element i : element.getEnclosedElements()) {
          if (i.getKind() == ElementKind.METHOD && !ElementUtil.isDefault(i)
              && !i.getModifiers().contains(Modifier.STATIC)) {
            functionalInterface = (ExecutableElement) i;
            functionalInterfaceType =
                (ExecutableType) env.typeUtilities().asMemberOf(baseType, i);
            return;
          }
        }
      }
      throw new AssertionError("Could not find functional interface for " + typeMirror);
    }

    private void createTypeDeclaration() {
      typeDecl = new TypeDeclaration(lambdaType);
      typeDecl.setSourceRange(node.getStartPosition(), node.getLength());
      TreeUtil.getEnclosingTypeBodyDeclarations(node).add(typeDecl);
    }

    private void createImplementation() {
      String selector = nameTable.getMethodSelector(functionalInterface);
      IOSMethodBinding implBinding = IOSMethodBinding.newMappedMethod(
          selector, BindingConverter.unwrapExecutableElement(functionalInterface));
      implBinding.setDeclaringClass(BindingConverter.unwrapTypeElement(lambdaType));
      implBinding.removeModifiers(java.lang.reflect.Modifier.ABSTRACT);
      implDecl = new MethodDeclaration(implBinding);
      typeDecl.addBodyDeclaration(implDecl);
    }

    private void createCreation() {
      GeneratedMethodBinding constructorBinding = GeneratedMethodBinding.newConstructor(
          BindingConverter.unwrapTypeElement(lambdaType), java.lang.reflect.Modifier.PRIVATE,
          typeEnv);

      // Add the implicit constructor to call.
      MethodDeclaration constructorDecl = new MethodDeclaration(constructorBinding);
      constructorDecl.setBody(new Block());
      typeDecl.addBodyDeclaration(constructorDecl);

      creation = new ClassInstanceCreation(constructorBinding, Type.newType(lambdaType.asType()));
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
        VariableElement instanceVar = new GeneratedVariableElement(
            "instance", lambdaType.asType(), ElementKind.FIELD, lambdaType)
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
        implDecl.addParameter(new SingleVariableDeclaration(decl.getVariableElement()));
      }
      setImplementationBody(TreeUtil.remove(node.getBody()));
    }

    private Iterator<VariableElement> createParameters() {
      List<? extends TypeMirror> paramTypes = functionalInterfaceType.getParameterTypes();
      List<VariableElement> params = new ArrayList<>(paramTypes.size());
      int i = 0;
      for (TypeMirror type : paramTypes) {
        GeneratedVariableElement param = new GeneratedVariableElement(
            getParamName(i++), type, ElementKind.PARAMETER, null);
        params.add(param);
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
        ArrayCreation creation = new ArrayCreation((ArrayType) creationType, typeEnv);
        forwardRemainingArgs(createParameters(), creation.getDimensions());
        setImplementationBody(creation);
      } else {
        ClassInstanceCreation creation = new ClassInstanceCreation(
            node.getExecutableElement(), Type.newType(creationType));
        forwardRemainingArgs(createParameters(), creation.getArguments());
        creation.setExpression(TreeUtil.remove(node.getCreationOuterArg()));
        TreeUtil.moveList(node.getCreationCaptureArgs(), creation.getCaptureArgs());
        setImplementationBody(creation);
      }
    }

    private void rewriteExpressionMethodReference(ExpressionMethodReference node) {
      ExecutableElement methodElem = node.getExecutableElement();
      Iterator<VariableElement> params = createParameters();
      Expression invocationTarget = null;

      if (!ElementUtil.isStatic(methodElem)) {
        VariableElement targetField = captureInfo.getOuterField(lambdaType);
        if (targetField != null) {
          invocationTarget = new SimpleName(targetField);
          creation.setExpression(TreeUtil.remove(node.getExpression()));
        } else {
          // The expression is actually a type name and doesn't evaluate to an invocable object.
          invocationTarget = new SimpleName(params.next());
        }
      }

      MethodInvocation invocation = new MethodInvocation(
          methodElem, (ExecutableType) methodElem.asType(), invocationTarget);
      forwardRemainingArgs(params, invocation.getArguments());
      setImplementationBody(invocation);
    }

    private void rewriteSuperMethodReference(SuperMethodReference node) {
      SuperMethodInvocation invocation = new SuperMethodInvocation(node.getExecutableElement());
      invocation.setReceiver(TreeUtil.remove(node.getReceiver()));
      forwardRemainingArgs(createParameters(), invocation.getArguments());
      setImplementationBody(invocation);
    }

    private void rewriteTypeMethodReference(TypeMethodReference node) {
      ExecutableElement methodElem = getExecutableElement(node);
      Iterator<VariableElement> params = createParameters();
      MethodInvocation invocation = new MethodInvocation(
          methodElem, (ExecutableType) methodElem.asType(),
          ElementUtil.isStatic(methodElem) ? null : new SimpleName(params.next()));
      forwardRemainingArgs(params, invocation.getArguments());
      setImplementationBody(invocation);
    }

    private ExecutableElement getExecutableElement(TypeMethodReference node) {
      if (!TypeUtil.isArray(node.getType().getTypeMirror())) {
        return node.getExecutableElement();
      }
      // JDT does not provide the correct method binding on array types, so we find it from
      // java.lang.Object.
      String name = node.getName().getIdentifier();
      int numParams = functionalInterface.getParameters().size() - 1;
      TypeElement javaObject = typeEnv.getJavaObjectElement();
      for (ExecutableElement method : ElementUtil.getDeclaredMethods(javaObject)) {
        if (ElementUtil.getName(method).equals(name)
            && method.getParameters().size() == numParams) {
          return method;
        }
      }
      throw new AssertionError("Can't find method element for method: " + name);
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
}
