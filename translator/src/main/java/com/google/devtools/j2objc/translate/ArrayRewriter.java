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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends TreeVisitor {

  @Override
  public void endVisit(ArrayCreation node) {
    node.replaceWith(createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    ITypeBinding arrayType = node.getTypeBinding();
    assert arrayType.isArray();
    boolean retainedResult = node.hasRetainedResult() || Options.useARC();
    ArrayInitializer initializer = node.getInitializer();
    if (initializer != null) {
      return newInitializedArrayInvocation(arrayType, initializer.getExpressions(), retainedResult);
    } else {
      List<Expression> dimensions = node.getDimensions();
      if (dimensions.size() == 1) {
        return newSingleDimensionArrayInvocation(arrayType, dimensions.get(0), retainedResult);
      } else {
        return newMultiDimensionArrayInvocation(arrayType, dimensions, retainedResult);
      }
    }
  }

  private MethodInvocation newInitializedArrayInvocation(
      ITypeBinding arrayType, List<Expression> elements, boolean retainedResult) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = typeEnv.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        getInitializeSelector(componentType, retainedResult), Modifier.PUBLIC | Modifier.STATIC,
        iosArrayBinding, iosArrayBinding);
    methodBinding.addParameter(iosArrayBinding);
    methodBinding.addParameter(typeEnv.resolveJavaType("int"));
    if (!componentType.isPrimitive()) {
      methodBinding.addParameter(typeEnv.getIOSClass());
    }
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, arrayType, new SimpleName(iosArrayBinding));

    // Create the array initializer and add it as the first parameter.
    ArrayInitializer arrayInit = new ArrayInitializer(arrayType);
    for (Expression element : elements) {
      arrayInit.addExpression(element.copy());
    }
    invocation.addArgument(arrayInit);

    // Add the array size parameter.
    invocation.addArgument(
        NumberLiteral.newIntLiteral(arrayInit.getExpressions().size(), typeEnv));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      invocation.addArgument(new TypeLiteral(componentType, typeEnv));
    }

    return invocation;
  }

  private String paramNameForPrimitive(char binaryName) {
    switch (binaryName) {
      case 'B': return "Bytes";
      case 'C': return "Chars";
      case 'D': return "Doubles";
      case 'F': return "Floats";
      case 'I': return "Ints";
      case 'J': return "Longs";
      case 'S': return "Shorts";
      case 'Z': return "Booleans";
    }
    throw new AssertionError("Unknown primitive type: " + binaryName);
  }

  private String getInitializeSelector(ITypeBinding componentType, boolean retainedResult) {
    String selectorFmt = "arrayWith%s:count:";
    if (retainedResult) {
      selectorFmt = "newArrayWith%s:count:";
    }
    String paramName;
    if (componentType.isPrimitive()) {
      paramName = paramNameForPrimitive(componentType.getBinaryName().charAt(0));
    } else {
      paramName = "Objects";
      selectorFmt += "type:";
    }
    return UnicodeUtils.format(selectorFmt, paramName);
  }

  private MethodInvocation newSingleDimensionArrayInvocation(
      ITypeBinding arrayType, Expression dimensionExpr, boolean retainedResult) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = typeEnv.resolveArrayType(componentType);

    String selector = (retainedResult ? "newArray" : "array") + "WithLength:"
        + (componentType.isPrimitive() ? "" : "type:");
    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        selector, Modifier.PUBLIC | Modifier.STATIC, iosArrayBinding, iosArrayBinding);
    methodBinding.addParameter(typeEnv.resolveJavaType("int"));
    if (!componentType.isPrimitive()) {
      methodBinding.addParameter(typeEnv.getIOSClass());
    }
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, arrayType, new SimpleName(iosArrayBinding));

    // Add the array length argument.
    invocation.addArgument(dimensionExpr.copy());

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      invocation.addArgument(new TypeLiteral(componentType, typeEnv));
    }

    return invocation;
  }

  private MethodInvocation newMultiDimensionArrayInvocation(
      ITypeBinding arrayType, List<Expression> dimensions, boolean retainedResult) {
    assert dimensions.size() > 1;
    ITypeBinding componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      componentType = componentType.getComponentType();
    }
    IOSTypeBinding iosArrayBinding = typeEnv.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getMultiDimensionMethod(
        componentType, iosArrayBinding, retainedResult);
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, arrayType, new SimpleName(iosArrayBinding));

    // Add the dimension count argument.
    invocation.addArgument(NumberLiteral.newIntLiteral(dimensions.size(), typeEnv));

    // Create the dimensions array.
    ArrayInitializer dimensionsArg = new ArrayInitializer(
        GeneratedTypeBinding.newArrayType(typeEnv.resolveJavaType("int")));
    for (Expression e : dimensions) {
      dimensionsArg.addExpression(e.copy());
    }
    invocation.addArgument(dimensionsArg);

    if (!componentType.isPrimitive()) {
      invocation.addArgument(new TypeLiteral(componentType, typeEnv));
    }

    return invocation;
  }

  private IOSMethodBinding getMultiDimensionMethod(
      ITypeBinding componentType, IOSTypeBinding iosArrayType, boolean retainedResult) {
    String selector = (retainedResult ? "newArray" : "array") + "WithDimensions:lengths:"
        + (componentType.isPrimitive() ? "" : "type:");
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        selector, Modifier.PUBLIC | Modifier.STATIC, typeEnv.resolveIOSType("IOSObjectArray"),
        iosArrayType);
    ITypeBinding intType = typeEnv.resolveJavaType("int");
    binding.addParameter(intType);
    binding.addParameter(GeneratedTypeBinding.newArrayType(intType));
    if (!componentType.isPrimitive()) {
      binding.addParameter(typeEnv.getIOSClass());
    }
    return binding;
  }

  // We must handle object array assignment before its children because if the
  // rhs is an array creation, we can optimize with "SetAndConsume".
  @Override
  public boolean visit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    ITypeBinding lhsType = lhs.getTypeBinding();
    if (lhs instanceof ArrayAccess && !lhsType.isPrimitive()) {
      FunctionInvocation newAssignment = newArrayAssignment(node, (ArrayAccess) lhs, lhsType);
      node.replaceWith(newAssignment);
      newAssignment.accept(this);
      return false;
    }
    return true;
  }

  @Override
  public void endVisit(ArrayAccess node) {
    TypeMirror componentType = node.getTypeMirror();
    TypeMirror iosArrayBinding = typeEnv.resolveArrayType(componentType);

    node.replaceWith(newArrayAccess(
        node, componentType, iosArrayBinding, TranslationUtil.isAssigned(node)));
  }

  private Expression newArrayAccess(
      ArrayAccess arrayAccessNode, TypeMirror componentType, TypeMirror iosArrayBinding,
      boolean assignable) {
    String funcName = iosArrayBinding.toString() + "_Get";
    TypeMirror returnType = componentType;
    TypeMirror declaredReturnType =
        componentType.getKind().isPrimitive() ? componentType : typeEnv.resolveIOSTypeMirror("id");
    if (assignable) {
      funcName += "Ref";
      returnType = declaredReturnType = typeEnv.getPointerType(componentType);
    }
    FunctionBinding binding = new FunctionBinding(funcName, declaredReturnType, iosArrayBinding);
    binding.addParameters(iosArrayBinding, typeEnv.resolveJavaTypeMirror("int"));
    FunctionInvocation invocation = new FunctionInvocation(binding, returnType);
    invocation.addArgument(arrayAccessNode.getArray().copy());
    invocation.addArgument(arrayAccessNode.getIndex().copy());
    if (assignable) {
      return new PrefixExpression(componentType, PrefixExpression.Operator.DEREFERENCE, invocation);
    }
    return invocation;
  }

  private FunctionInvocation newArrayAssignment(
      Assignment assignmentNode, ArrayAccess arrayAccessNode, ITypeBinding componentType) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    Expression value = TreeUtil.remove(assignmentNode.getRightHandSide());
    Expression retainedValue = TranslationUtil.retainResult(value);
    String funcName = "IOSObjectArray_Set";
    if (retainedValue != null) {
      funcName = "IOSObjectArray_SetAndConsume";
      value = retainedValue;
    }
    TypeMirror objArrayType = typeEnv.resolveIOSTypeMirror("IOSObjectArray");
    TypeMirror idType = typeEnv.resolveIOSTypeMirror("id");
    FunctionBinding binding = new FunctionBinding(funcName, idType, objArrayType);
    binding.addParameters(objArrayType, typeEnv.resolveJavaTypeMirror("int"), idType);
    FunctionInvocation invocation = new FunctionInvocation(binding, componentType);
    List<Expression> args = invocation.getArguments();
    args.add(TreeUtil.remove(arrayAccessNode.getArray()));
    args.add(TreeUtil.remove(arrayAccessNode.getIndex()));
    args.add(value);
    return invocation;
  }

  @Override
  public void endVisit(FieldAccess node) {
    maybeRewriteArrayLength(node, node.getName(), node.getExpression());
  }

  @Override
  public void endVisit(QualifiedName node) {
    maybeRewriteArrayLength(node, node.getName(), node.getQualifier());
  }

  private void maybeRewriteArrayLength(Expression node, SimpleName name, Expression expr) {
    ITypeBinding exprType = expr.getTypeBinding();
    if (name.getIdentifier().equals("length") && exprType.isArray()) {
      GeneratedVariableBinding sizeField = new GeneratedVariableBinding(
          "size", Modifier.PUBLIC, typeEnv.resolveJavaType("int"), true, false,
          typeEnv.mapType(exprType), null);
      node.replaceWith(new FieldAccess(sizeField, TreeUtil.remove(expr)));
    }
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    ITypeBinding type = node.getRightOperand().getTypeBinding();
    if (!type.isArray() || type.getComponentType().isPrimitive()) {
      return;
    }
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        "isInstance", Modifier.PUBLIC, typeEnv.resolveJavaType("boolean"), typeEnv.getIOSClass());
    binding.addParameter(typeEnv.resolveIOSType("id"));
    MethodInvocation invocation = new MethodInvocation(binding, new TypeLiteral(type, typeEnv));
    invocation.addArgument(TreeUtil.remove(node.getLeftOperand()));
    node.replaceWith(invocation);
  }
}
