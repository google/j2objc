/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import java.util.Iterator;
import java.util.List;

/**
 * Adds support for inner and anonymous classes, and extracts them to be
 * top-level classes (also like class files). This is similar to how Java
 * compilers convert inner classes into class files, which are all top-level.
 *
 * @author Tom Ball
 */
public class InnerClassExtractor extends ErrorReportingASTVisitor {

  private final List<AbstractTypeDeclaration> unitTypes;

  public InnerClassExtractor(CompilationUnit unit) {
    unitTypes = ASTUtil.getTypes(unit);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    super.visit(node);
    visitType(node);
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    super.visit(node);
    visitType(node);
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    super.visit(node);
    return false; // ignore annotations
  }

  public boolean visitType(AbstractTypeDeclaration node) {
    ASTNode parentNode = node.getParent();
    if (!(parentNode instanceof CompilationUnit)) {
      ITypeBinding type = Types.getTypeBinding(node);
      if (!type.isInterface() && !type.isAnnotation() && !Modifier.isStatic(type.getModifiers())) {
        addOuterFields(node);
      }

      if (parentNode instanceof AbstractTypeDeclaration) {
        // Remove declaration from declaring type.
        boolean success =
            ASTUtil.getBodyDeclarations((AbstractTypeDeclaration) parentNode).remove(node);
        assert success;
      } else {
        TypeDeclarationStatement typeStatement = (TypeDeclarationStatement) parentNode;
        node = NodeCopier.copySubtree(node.getAST(), typeStatement.getDeclaration());

        // Remove stmt from method body (or an if/else/try/catch/finally clause).
        Block body = (Block) typeStatement.getParent();
        boolean success = ASTUtil.getStatements(body).remove(typeStatement);
        assert success;
      }

      // Make this node non-private, if necessary, and add it to the unit's type
      // list.
      Iterator<IExtendedModifier> iter = ASTUtil.getModifiers(node).iterator();
      while (iter.hasNext()) {
        IExtendedModifier iem = iter.next();
        if ((iem instanceof Modifier) && ((Modifier) iem).isPrivate()) {
          iter.remove();
          break;
        }
      }
      unitTypes.add(node);
    }
    return true;
  }

  private void addOuterFields(AbstractTypeDeclaration node) {
    List<BodyDeclaration> members = ASTUtil.getBodyDeclarations(node);
    AST ast = node.getAST();
    ITypeBinding clazz = Types.getTypeBinding(node);
    ITypeBinding outerClazz = clazz.getDeclaringClass();
    assert outerClazz != null;

    IVariableBinding outerFieldBinding = OuterReferenceResolver.getOuterField(clazz);
    if (outerFieldBinding != null) {
      members.add(0, ASTFactory.newFieldDeclaration(ast, outerFieldBinding, null));
    }

    // Insert new parameters for each constructor in class.
    boolean needsConstructor = true;
    for (BodyDeclaration member : members) {
      if (member instanceof MethodDeclaration && ((MethodDeclaration) member).isConstructor()) {
        needsConstructor = false;
        MethodDeclaration constructor = (MethodDeclaration) member;
        IMethodBinding oldBinding = Types.getMethodBinding(constructor);
        GeneratedMethodBinding newBinding = new GeneratedMethodBinding(oldBinding);
        Types.addBinding(constructor, newBinding);
        addOuterParameter(node, constructor, newBinding, outerFieldBinding);
        assert constructor.parameters().size() == newBinding.getParameterTypes().length;
      }
    }

    if (needsConstructor) {
      MethodDeclaration constructor = ast.newMethodDeclaration();
      constructor.setConstructor(true);
      ITypeBinding voidType = ast.resolveWellKnownType("void");
      GeneratedMethodBinding binding = new GeneratedMethodBinding("init", 0,
          voidType, clazz, true, false, true);
      Types.addBinding(constructor, binding);
      Types.addBinding(constructor.getReturnType2(), voidType);
      SimpleName name = ast.newSimpleName("init");
      Types.addBinding(name, binding);
      constructor.setName(name);
      constructor.setBody(ast.newBlock());
      addOuterParameter(node, constructor, binding, outerFieldBinding);
      members.add(constructor);
      assert constructor.parameters().size() == binding.getParameterTypes().length;
    }
  }

  private IMethodBinding getDefaultConstructorDeclaration(ITypeBinding type) {
    for (IMethodBinding method : type.getTypeDeclaration().getDeclaredMethods()) {
      if (method.isConstructor()) {
        IMethodBinding decl = method.getMethodDeclaration();
        if (decl.getParameterTypes().length == 0) {
          return decl;
        }
      }
    }
    return null;
  }

  private boolean hasOuterArg(SuperConstructorInvocation superCall) {
    List<Expression> superCallArgs = ASTUtil.getArguments(superCall);
    if (superCallArgs.size() == 0) {
      return false;
    }
    Expression firstArg = superCallArgs.get(0);
    if (!(firstArg instanceof SimpleName)) {
      return false;
    }
    SimpleName name = (SimpleName) firstArg;
    return name.getIdentifier().startsWith("capture$");
  }

  protected void addOuterParameter(
      AbstractTypeDeclaration typeNode, MethodDeclaration constructor,
      GeneratedMethodBinding binding, IVariableBinding outerField) {
    AST ast = typeNode.getAST();
    ITypeBinding outerType = Types.getTypeBinding(typeNode).getDeclaringClass();
    GeneratedVariableBinding outerParamBinding = new GeneratedVariableBinding(
        "outer$", Modifier.FINAL, outerType, false, true, binding.getDeclaringClass(), binding);
    SingleVariableDeclaration outerParam =
        ASTFactory.newSingleVariableDeclaration(ast, outerParamBinding);
    ASTUtil.getParameters(constructor).add(0, outerParam);
    binding.addParameter(0, outerParamBinding);

    ConstructorInvocation thisCall = null;
    SuperConstructorInvocation superCall = null;

    List<Statement> statements = ASTUtil.getStatements(constructor.getBody());
    Statement firstStatement = statements.size() > 0 ? statements.get(0) : null;
    if (firstStatement != null && firstStatement instanceof ConstructorInvocation) {
      thisCall = (ConstructorInvocation) firstStatement;
    } else if (firstStatement != null && firstStatement instanceof SuperConstructorInvocation) {
      superCall = (SuperConstructorInvocation) firstStatement;
    }

    if (thisCall != null) {
      IMethodBinding thisBinding = Types.getMethodBinding(thisCall);
      GeneratedMethodBinding newThisBinding =
          new GeneratedMethodBinding(thisBinding.getMethodDeclaration());
      ASTUtil.getArguments(thisCall).add(0, ASTFactory.newSimpleName(ast, outerParamBinding));
      newThisBinding.addParameter(0, outerParamBinding.getType());
      Types.addBinding(thisCall, newThisBinding);
    } else {
      ITypeBinding superType = binding.getDeclaringClass().getSuperclass().getTypeDeclaration();
      if (superType.getDeclaringClass() != null && !Modifier.isStatic(superType.getModifiers())
          && (superCall == null || !hasOuterArg(superCall))) {
        IMethodBinding superCallDecl = superCall != null ?
            Types.getMethodBinding(superCall).getMethodDeclaration() :
            getDefaultConstructorDeclaration(superType);
        GeneratedMethodBinding superCallBinding = new GeneratedMethodBinding(superCallDecl);
        if (superCall == null) {
          superCall = ast.newSuperConstructorInvocation();
          ASTUtil.getStatements(constructor.getBody()).add(0, superCall);
        }

        List<IVariableBinding> path = OuterReferenceResolver.getPath(typeNode);
        assert path != null && path.size() > 0;
        path = Lists.newArrayList(path);
        path.set(0, outerParamBinding);
        Name superOuterArg = ASTFactory.newName(ast, path);

        ASTUtil.getArguments(superCall).add(0, superOuterArg);
        superCallBinding.addParameter(0, superType.getDeclaringClass());
        Types.addBinding(superCall, superCallBinding);
      }
      if (outerField != null) {
        ASTUtil.getStatements(constructor.getBody()).add(superCall == null ? 0 : 1,
            ast.newExpressionStatement(ASTFactory.newAssignment(ast,
            ASTFactory.newSimpleName(ast, outerField),
            ASTFactory.newSimpleName(ast, outerParamBinding))));
      }
    }
  }
}
