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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.MethodReference;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.RetainedLocalRef;
import com.google.j2objc.annotations.Weak;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Rewrites the Java AST to replace difficult to translate code with methods
 * that are more Objective C/iOS specific. For example, Objective C doesn't have
 * the concept of class variables, so they need to be replaced with static
 * accessor methods referencing private static data.
 *
 * @author Tom Ball
 */
public class Rewriter extends TreeVisitor {

  private Map<IVariableBinding, IVariableBinding> localRefs = Maps.newHashMap();
  private final OuterReferenceResolver outerResolver;

  public Rewriter(OuterReferenceResolver outerResolver) {
    this.outerResolver = outerResolver;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    if (BindingUtil.hasAnnotation(binding, AutoreleasePool.class)) {
      if (!binding.getReturnType().isPrimitive()) {
        ErrorUtil.warning(
            "Ignoring AutoreleasePool annotation on method with retainable return type");
      } else if (node.getBody() != null) {
        node.getBody().setHasAutoreleasePool(true);
      }
    }
    return true;
  }

  @Override
  public void endVisit(ForStatement node) {
    // It should not be possible to have multiple VariableDeclarationExpression
    // nodes in the initializers.
    if (node.getInitializers().size() == 1) {
      Object initializer = node.getInitializers().get(0);
      if (initializer instanceof VariableDeclarationExpression) {
        List<VariableDeclarationFragment> fragments =
            ((VariableDeclarationExpression) initializer).getFragments();
        for (VariableDeclarationFragment fragment : fragments) {
          if (BindingUtil.hasAnnotation(fragment.getVariableBinding(), AutoreleasePool.class)) {
            Statement loopBody = node.getBody();
            if (!(loopBody instanceof Block)) {
              Block block = new Block();
              node.setBody(block);
              block.getStatements().add(loopBody);
            }
            ((Block) node.getBody()).setHasAutoreleasePool(true);
          }
        }
      }
    }
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding type = node.getTypeBinding();
    if (typeEnv.isJavaStringType(type) && op == InfixExpression.Operator.PLUS) {
      rewriteStringConcat(node);
    } else if (op == InfixExpression.Operator.CONDITIONAL_AND) {
      // Avoid logical-op-parentheses compiler warnings.
      if (node.getParent() instanceof InfixExpression) {
        InfixExpression parent = (InfixExpression) node.getParent();
        if (parent.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
          ParenthesizedExpression.parenthesizeAndReplace(node);
        }
      }
    } else if (op == InfixExpression.Operator.AND) {
      // Avoid bitwise-op-parentheses compiler warnings.
      if (node.getParent() instanceof InfixExpression
          && ((InfixExpression) node.getParent()).getOperator() == InfixExpression.Operator.OR) {
        ParenthesizedExpression.parenthesizeAndReplace(node);
      }
    }

    // Avoid lower precedence compiler warnings.
    if (op == InfixExpression.Operator.AND || op == InfixExpression.Operator.OR) {
      for (Expression operand : node.getOperands()) {
        if (operand instanceof InfixExpression) {
          ParenthesizedExpression.parenthesizeAndReplace(operand);
        }
      }
    }
  }

  private void rewriteStringConcat(InfixExpression node) {
    // Collect all non-string operands that precede the first string operand.
    // If there are multiple such operands, move them into a sub-expression.
    List<Expression> nonStringOperands = Lists.newArrayList();
    ITypeBinding nonStringExprType = null;
    for (Expression operand : node.getOperands()) {
      ITypeBinding operandType = operand.getTypeBinding();
      if (typeEnv.isJavaStringType(operandType)) {
        break;
      }
      nonStringOperands.add(operand);
      nonStringExprType = getAdditionType(nonStringExprType, operandType);
    }

    if (nonStringOperands.size() < 2) {
      return;
    }

    InfixExpression nonStringExpr =
        new InfixExpression(nonStringExprType, InfixExpression.Operator.PLUS);
    for (Expression operand : nonStringOperands) {
      nonStringExpr.getOperands().add(TreeUtil.remove(operand));
    }
    node.getOperands().add(0, nonStringExpr);
  }

  private ITypeBinding getAdditionType(ITypeBinding aType, ITypeBinding bType) {
    ITypeBinding doubleType = typeEnv.resolveJavaType("double");
    ITypeBinding boxedDoubleType = typeEnv.resolveJavaType("java.lang.Double");
    if (aType == doubleType || bType == doubleType
        || aType == boxedDoubleType || bType == boxedDoubleType) {
      return doubleType;
    }
    ITypeBinding floatType = typeEnv.resolveJavaType("float");
    ITypeBinding boxedFloatType = typeEnv.resolveJavaType("java.lang.Float");
    if (aType == floatType || bType == floatType
        || aType == boxedFloatType || bType == boxedFloatType) {
      return floatType;
    }
    ITypeBinding longType = typeEnv.resolveJavaType("long");
    ITypeBinding boxedLongType = typeEnv.resolveJavaType("java.lang.Long");
    if (aType == longType || bType == longType
        || aType == boxedLongType || bType == boxedLongType) {
      return longType;
    }
    return typeEnv.resolveJavaType("int");
  }

  /**
   * Moves all variable declarations above the first case statement.
   */
  @Override
  public void endVisit(SwitchStatement node) {
    List<Statement> statements = node.getStatements();
    int insertIdx = 0;
    Block block = new Block();
    List<Statement> blockStmts = block.getStatements();
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      if (stmt instanceof VariableDeclarationStatement) {
        VariableDeclarationStatement declStmt = (VariableDeclarationStatement) stmt;
        statements.remove(i--);
        List<VariableDeclarationFragment> fragments = declStmt.getFragments();
        for (VariableDeclarationFragment decl : fragments) {
          Expression initializer = decl.getInitializer();
          if (initializer != null) {
            Assignment assignment = new Assignment(decl.getName().copy(), initializer.copy());
            statements.add(++i, new ExpressionStatement(assignment));
            decl.setInitializer(null);
          }
        }
        blockStmts.add(insertIdx++, declStmt.copy());
      }
    }
    if (blockStmts.size() > 0) {
      // There is at least one variable declaration, so copy this switch
      // statement into the new block and replace it in the parent list.
      node.replaceWith(block);
      blockStmts.add(node);
    }
  }

  @Override
  public void endVisit(SingleVariableDeclaration node) {
    if (node.getExtraDimensions() > 0) {
      node.setType(Type.newType(node.getVariableBinding().getType()));
      node.setExtraDimensions(0);
    }
  }

  @Override
  public void endVisit(VariableDeclarationStatement node) {
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations =
        rewriteExtraDimensions(node.getType(), node.getFragments());
    if (newDeclarations != null) {
      List<Statement> statements = ((Block) node.getParent()).getStatements();
      int location = 0;
      while (location < statements.size() && !node.equals(statements.get(location))) {
        location++;
      }
      for (Integer dimensions : newDeclarations.keySet()) {
        List<VariableDeclarationFragment> fragments = newDeclarations.get(dimensions);
        VariableDeclarationStatement newDecl = new VariableDeclarationStatement(fragments.get(0));
        newDecl.getFragments().addAll(fragments.subList(1, fragments.size()));
        statements.add(++location, newDecl);
      }
    }
    // Scan modifiers since variable declarations don't have variable bindings.
    if (TreeUtil.hasAnnotation(RetainedLocalRef.class, node.getAnnotations())) {
      ITypeBinding localRefType = typeEnv.getLocalRefType();
      node.setType(Type.newType(localRefType));

      // Convert fragments to retained local refs.
      for (VariableDeclarationFragment fragment : node.getFragments()) {
        IVariableBinding var = fragment.getVariableBinding();
        GeneratedVariableBinding newVar = new GeneratedVariableBinding(
            var.getName(), var.getModifiers(), localRefType, false, false,
            var.getDeclaringClass(), var.getDeclaringMethod());
        localRefs.put(var, newVar);

        Expression initializer = fragment.getInitializer();
        if (localRefs.containsKey(TreeUtil.getVariableBinding(initializer))) {
          initializer.accept(this);
        } else {
          // Create a constructor for a ScopedLocalRef for this fragment.
          IMethodBinding constructor = null;
          for (IMethodBinding m : localRefType.getDeclaredMethods()) {
            if (m.isConstructor()) {
              constructor = m;
              break;
            }
          }
          assert constructor != null : "failed finding ScopedLocalRef(var)";
          ClassInstanceCreation newInvocation = new ClassInstanceCreation(constructor);
          newInvocation.getArguments().add(initializer.copy());
          fragment.setInitializer(newInvocation);
          fragment.setVariableBinding(newVar);
        }
      }
    }
  }

  @Override
  public void endVisit(FieldDeclaration node) {
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations =
        rewriteExtraDimensions(node.getType(), node.getFragments());
    if (newDeclarations != null) {
      List<BodyDeclaration> bodyDecls = TreeUtil.getBodyDeclarations(node.getParent());
      int location = 0;
      while (location < bodyDecls.size() && !node.equals(bodyDecls.get(location))) {
        location++;
      }
      for (Integer dimensions : newDeclarations.keySet()) {
        List<VariableDeclarationFragment> fragments = newDeclarations.get(dimensions);
        FieldDeclaration newDecl = new FieldDeclaration(fragments.get(0));
        newDecl.getFragments().addAll(fragments.subList(1, fragments.size()));
        bodyDecls.add(++location, newDecl);
      }
    }
  }

  @Override
  public boolean visit(QualifiedName node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    Expression qualifier = node.getQualifier();
    if (var != null && var.isField() && TreeUtil.getVariableBinding(qualifier) != null) {
      // FieldAccess nodes are more easily mutated than QualifiedName.
      FieldAccess fieldAccess = new FieldAccess(var, TreeUtil.remove(qualifier));
      node.replaceWith(fieldAccess);
      fieldAccess.accept(this);
      return false;
    }
    return true;
  }

  @Override
  public void endVisit(SimpleName node) {
    // Check for ScopedLocalRefs.
    IVariableBinding localRef = localRefs.get(node.getBinding());
    if (localRef != null) {
      FieldAccess access = new FieldAccess(
          typeEnv.getLocalRefType().getDeclaredFields()[0], new SimpleName(localRef));
      CastExpression newCast = new CastExpression(node.getTypeBinding(), access);
      ParenthesizedExpression newParens = ParenthesizedExpression.parenthesize(newCast);
      node.replaceWith(newParens);
    }
  }

  private LinkedListMultimap<Integer, VariableDeclarationFragment> rewriteExtraDimensions(
      Type typeNode, List<VariableDeclarationFragment> fragments) {
    // Removes extra dimensions on variable declaration fragments and creates extra field
    // declaration nodes if necessary.
    // eg. "int i1, i2[], i3[][];" becomes "int i1; int[] i2; int[][] i3".
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations = null;
    int masterDimensions = -1;
    Iterator<VariableDeclarationFragment> iter = fragments.iterator();
    while (iter.hasNext()) {
      VariableDeclarationFragment frag = iter.next();
      int dimensions = frag.getExtraDimensions();
      ITypeBinding binding = frag.getVariableBinding().getType();
      if (masterDimensions == -1) {
        masterDimensions = dimensions;
        if (dimensions != 0) {
          typeNode.replaceWith(Type.newType(binding));
        }
      } else if (dimensions != masterDimensions) {
        if (newDeclarations == null) {
          newDeclarations = LinkedListMultimap.create();
        }
        VariableDeclarationFragment newFrag = new VariableDeclarationFragment(
            frag.getVariableBinding(), TreeUtil.remove(frag.getInitializer()));
        newDeclarations.put(dimensions, newFrag);
        iter.remove();
      } else {
        frag.setExtraDimensions(0);
      }
    }
    return newDeclarations;
  }

  @Override
  public boolean visit(LambdaExpression node) {
    if (!(node.getBody() instanceof Block)) {
      // Add explicit blocks for lambdas with expression bodies.
      Block block = new Block();
      Statement statement;
      Expression expression = (Expression) TreeUtil.remove(node.getBody());
      if (BindingUtil.isVoid(
          node.getTypeBinding().getFunctionalInterfaceMethod().getReturnType())) {
        statement = new ExpressionStatement(expression);
      } else {
        statement = new ReturnStatement(expression);
      }
      block.getStatements().add(statement);
      node.setBody(block);
    }
    // Resolve whether a lambda captures variables from the enclosing scope.
    ITypeBinding binding = node.getTypeBinding();
    node.setIsCapturing(outerResolver.hasImplicitCaptures(binding));
    return true;
  }

  @Override
  public boolean visit(CreationReference node) {
    IMethodBinding methodBinding = node.getMethodBinding().getMethodDeclaration();
    IMethodBinding functionalInterface = node.getTypeBinding().getFunctionalInterfaceMethod();
    Type type = node.getType().copy();
    ClassInstanceCreation invocation = new ClassInstanceCreation(methodBinding, type);
    List<Expression> invocationArguments = invocation.getArguments();
    buildMethodReferenceInvocationArguments(invocationArguments, node, null);
    // The functional interface may return void, in which case the initialization is only being used
    // for side effects, and we don't need a return.
    if (BindingUtil.isVoid(functionalInterface.getReturnType())) {
      node.setInvocation(new ExpressionStatement(invocation));
    } else {
      node.setInvocation(new ReturnStatement(invocation));
    }
    return true;
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    IMethodBinding methodBinding = node.getMethodBinding().getMethodDeclaration();
    Expression expression = node.getExpression().copy();
    MethodInvocation invocation = new MethodInvocation(methodBinding, expression);
    List<Expression> invocationArguments = invocation.getArguments();
    buildMethodReferenceInvocationArguments(invocationArguments, node, invocation);
    if (BindingUtil.isVoid(methodBinding.getReturnType())) {
      node.setInvocation(new ExpressionStatement(invocation));
    } else {
      node.setInvocation(new ReturnStatement(invocation));
    }
    return true;
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    IMethodBinding methodBinding = node.getMethodBinding().getMethodDeclaration();
    Name qualifier = node.getQualifier() == null ? null : node.getQualifier().copy();
    SuperMethodInvocation invocation = new SuperMethodInvocation(methodBinding);
    invocation.setQualifier(qualifier);
    List<Expression> invocationArguments = invocation.getArguments();
    buildMethodReferenceInvocationArguments(invocationArguments, node, null);
    if (BindingUtil.isVoid(methodBinding.getReturnType())) {
      node.setInvocation(new ExpressionStatement(invocation));
    } else {
      node.setInvocation(new ReturnStatement(invocation));
    }
    return true;
  }

  /**
   * The signatures of TypeMethodReferences include the object parameter, which will be passed in
   * our case as the first argument. We need to create a method binding without that first argument
   * for the MethodInvocation, so we are duplicating code from
   * buildMethodReferenceInvocationArguments.
   */
  @Override
  public boolean visit(TypeMethodReference node) {
    IMethodBinding oldMethodBinding = node.getMethodBinding();
    GeneratedMethodBinding methodBinding = GeneratedMethodBinding.newNamedMethod(
        node.getName().toString(), oldMethodBinding);
    methodBinding.addParameters(oldMethodBinding);
    methodBinding.setModifiers(methodBinding.getModifiers() & ~Modifier.STATIC);
    methodBinding.getParameters().remove(0);
    IMethodBinding functionalInterface = node.getTypeBinding().getFunctionalInterfaceMethod();
    ITypeBinding[] methodParams = methodBinding.getParameterTypes();
    ITypeBinding[] functionalParams = functionalInterface.getParameterTypes();
    char[] var = nameTable.incrementVariable(null);
    ITypeBinding functionalParam = functionalParams[0];
    IVariableBinding variableBinding = new GeneratedVariableBinding(new String(var), 0,
        functionalParam, false, true, null, null);
    SimpleName expression = new SimpleName(variableBinding);
    MethodInvocation invocation = new MethodInvocation(methodBinding, expression);
    List<Expression> invocationArguments = invocation.getArguments();
    if (BindingUtil.isVoid(methodBinding.getReturnType())) {
      node.setInvocation(new ExpressionStatement(invocation));
    } else {
      node.setInvocation(new ReturnStatement(invocation));
    }
    int methodParamStopIndex = methodBinding.isVarargs() ? methodParams.length
        : methodParams.length + 1;
    for (int i = 1; i < methodParamStopIndex; i++) {
      functionalParam = functionalParams[i];
      variableBinding = new GeneratedVariableBinding(new String(var), 0, functionalParam, false,
          true, null, null);
      invocationArguments.add(new SimpleName(variableBinding));
      var = nameTable.incrementVariable(var);
    }
    if (methodBinding.isVarargs()) {
      for (int i = methodParamStopIndex; i < functionalInterface.getParameterTypes().length; i++) {
        functionalParam = functionalParams[i];
        variableBinding = new GeneratedVariableBinding(new String(var), 0,
            functionalParam, false, true, null, null);
        invocationArguments.add(new SimpleName(variableBinding));
        var = nameTable.incrementVariable(var);
      }
    }
    return true;
  }

  /**
   * Fill in the arguments in a method reference invocation. The argument list must come from the
   * functional interface's method, not the invoked method: this is because it is possible to
   * make a reference to a method with varargs, but the actual number of arguments is determined
   * during compile time by the matching functional interface's method.
   */
  // TODO(kirbs): In the case that we have a referenced method with an int arg, a functional
  // interface method with an Integer arg, and an invocation with an int arg, we will end up
  // immediately boxing and unboxing the value. We should solve this by making the types of the
  // referenced method and the functional interface method the same, but this requires a rewrite of
  // the selectors that target the method reference on invocation.
  public void buildMethodReferenceInvocationArguments(List<Expression> invocationArguments,
                                                      MethodReference node,
                                                      MethodInvocation invocation) {
    IMethodBinding methodBinding = node.getMethodBinding();
    IMethodBinding functionalInterface = node.getTypeBinding().getFunctionalInterfaceMethod();
    ITypeBinding[] functionalParams = functionalInterface.getParameterTypes();
    char[] var = nameTable.incrementVariable(null);
    int paramIdx = 0;

    // If this is an ExpressionMethodReference EXP::METHOD, and if EXP is a type binding and
    // METHOD is an instance method, then EXP should be replaced with the first parameter in
    // functionalParams. For example, String::compareTo matches the functional interface
    // int Comparator<T>::compare(T a, T b), but the JDT method invocation node is actually
    // String.compareTo() at this point. We need to fix that to a.compareTo(), so that in the
    // code that follows the second functional parameter b will be filled correctly into the
    // the invocation, resulting in the invocation a.compareTo(b).
    if (node instanceof ExpressionMethodReference) {
      Expression expression = ((ExpressionMethodReference) node).getExpression();
      if (expression instanceof Name) {
        IBinding binding = ((Name) expression).getBinding();
        if (binding.getKind() == IBinding.TYPE) {
          // Only ExpressionMethodReference needs this potential invocation fix.
          assert invocation != null;
          if (!BindingUtil.isStatic(methodBinding)) {
            ITypeBinding functionalParam = functionalParams[paramIdx];
            IVariableBinding variableBinding = new GeneratedVariableBinding(new String(var), 0,
                functionalParam, false, true, null, null);
            invocation.setExpression(new SimpleName(variableBinding));
            var = nameTable.incrementVariable(var);
            paramIdx++;
          }
        }
      }
    }

    // Fill in the invocation parameters.
    for (; paramIdx < functionalParams.length; paramIdx++) {
      ITypeBinding functionalParam = functionalParams[paramIdx];
      IVariableBinding variableBinding = new GeneratedVariableBinding(new String(var), 0,
          functionalParam, false, true, null, null);
      invocationArguments.add(new SimpleName(variableBinding));
      var = nameTable.incrementVariable(var);
    }
  }

  /**
   * Verify, update property attributes. Accessor methods are not checked since a
   * property annotation may apply to separate variables in a field declaration, so
   * each variable needs to be checked separately during generation.
   */
  @Override
  public void endVisit(PropertyAnnotation node) {
    FieldDeclaration field = (FieldDeclaration) node.getParent();
    VariableDeclarationFragment firstVarNode = field.getFragments().get(0);
    if (field.getType().getTypeBinding().getName().equals("String")) {
      node.addAttribute("copy");
    } else if (BindingUtil.hasAnnotation(firstVarNode.getVariableBinding(), Weak.class)) {
      if (node.hasAttribute("strong")) {
        ErrorUtil.error(field, "Weak field annotation conflicts with strong Property attribute");
        return;
      }
      node.addAttribute("weak");
    }

    node.removeAttribute("readwrite");
    node.removeAttribute("strong");
    node.removeAttribute("atomic");

    // Make sure attempt isn't made to specify an accessor method for fields with multiple
    // fragments, since each variable needs unique accessors.
    String getter = node.getGetter();
    String setter = node.getSetter();
    if (field.getFragments().size() > 1) {
      if (getter != null) {
        ErrorUtil.error(field, "@Property getter declared for multiple fields");
        return;
      }
      if (setter != null) {
        ErrorUtil.error(field, "@Property setter declared for multiple fields");
        return;
      }
    } else {
      // Check that specified accessors exist.
      IVariableBinding var = field.getFragments().get(0).getVariableBinding();
      if (getter != null) {
        if (BindingUtil.findDeclaredMethod(var.getType(), getter) == null) {
          ErrorUtil.error(field, "Non-existent getter specified: " + getter);
        }
      }
      if (setter != null) {
        if (BindingUtil.findDeclaredMethod(var.getType(), setter,
            var.getType().getQualifiedName()) == null) {
          ErrorUtil.error(field, "Non-existent setter specified: " + setter);
        }
      }
    }
  }
}
