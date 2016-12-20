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

import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CompilationUnit;
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
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends UnitTreeVisitor {

  public ArrayRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(ArrayCreation node) {
    node.replaceWith(createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    ArrayType arrayType = node.getTypeMirror();
    boolean retainedResult = node.hasRetainedResult() || options.useARC();
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
      ArrayType arrayType, List<Expression> elements, boolean retainedResult) {
    TypeMirror componentType = arrayType.getComponentType();
    TypeElement iosArrayElement = typeUtil.getIosArray(componentType);

    GeneratedExecutableElement methodElement = GeneratedExecutableElement.newMethodWithSelector(
        getInitializeSelector(componentType, retainedResult), iosArrayElement.asType(),
        iosArrayElement)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    methodElement.addParameter(GeneratedVariableElement.newParameter(
        "values", new PointerType(componentType), methodElement));
    methodElement.addParameter(GeneratedVariableElement.newParameter(
        "count", typeUtil.getInt(), methodElement));
    if (!componentType.getKind().isPrimitive()) {
      methodElement.addParameter(GeneratedVariableElement.newParameter(
          "type", TypeUtil.IOS_CLASS.asType(), methodElement));
    }
    MethodInvocation invocation = new MethodInvocation(
        new ExecutablePair(methodElement), arrayType, new SimpleName(iosArrayElement));

    // Create the array initializer and add it as the first parameter.
    ArrayInitializer arrayInit = new ArrayInitializer(arrayType);
    for (Expression element : elements) {
      arrayInit.addExpression(element.copy());
    }
    invocation.addArgument(arrayInit);

    // Add the array size parameter.
    invocation.addArgument(
        NumberLiteral.newIntLiteral(arrayInit.getExpressions().size(), typeUtil));

    // Add the type argument for object arrays.
    if (!componentType.getKind().isPrimitive()) {
      invocation.addArgument(new TypeLiteral(componentType, typeUtil));
    }

    return invocation;
  }

  private String paramNameForPrimitive(TypeMirror t) {
    switch (t.getKind()) {
      case BOOLEAN: return "Booleans";
      case BYTE: return "Bytes";
      case CHAR: return "Chars";
      case DOUBLE: return "Doubles";
      case FLOAT: return "Floats";
      case INT: return "Ints";
      case LONG: return "Longs";
      case SHORT: return "Shorts";
      default: throw new AssertionError("Not a  primitive type: " + t);
    }
  }

  private String getInitializeSelector(TypeMirror componentType, boolean retainedResult) {
    String selectorFmt = "arrayWith%s:count:";
    if (retainedResult) {
      selectorFmt = "newArrayWith%s:count:";
    }
    String paramName;
    if (componentType.getKind().isPrimitive()) {
      paramName = paramNameForPrimitive(componentType);
    } else {
      paramName = "Objects";
      selectorFmt += "type:";
    }
    return UnicodeUtils.format(selectorFmt, paramName);
  }

  private MethodInvocation newSingleDimensionArrayInvocation(
      ArrayType arrayType, Expression dimensionExpr, boolean retainedResult) {
    TypeMirror componentType = arrayType.getComponentType();
    TypeElement iosArrayElement = typeUtil.getIosArray(componentType);
    boolean isPrimitive = componentType.getKind().isPrimitive();

    String selector = (retainedResult ? "newArray" : "array") + "WithLength:"
        + (isPrimitive ? "" : "type:");
    GeneratedExecutableElement methodElement = GeneratedExecutableElement.newMethodWithSelector(
        selector, iosArrayElement.asType(), iosArrayElement)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    methodElement.addParameter(GeneratedVariableElement.newParameter(
        "length", typeUtil.getInt(), methodElement));
    if (!isPrimitive) {
      methodElement.addParameter(GeneratedVariableElement.newParameter(
          "type", TypeUtil.IOS_CLASS.asType(), methodElement));
    }
    MethodInvocation invocation = new MethodInvocation(
        new ExecutablePair(methodElement), arrayType, new SimpleName(iosArrayElement));

    // Add the array length argument.
    invocation.addArgument(dimensionExpr.copy());

    // Add the type argument for object arrays.
    if (!isPrimitive) {
      invocation.addArgument(new TypeLiteral(componentType, typeUtil));
    }

    return invocation;
  }

  private MethodInvocation newMultiDimensionArrayInvocation(
      ArrayType arrayType, List<Expression> dimensions, boolean retainedResult) {
    assert dimensions.size() > 1;
    TypeMirror componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      assert TypeUtil.isArray(componentType);
      componentType = ((ArrayType) componentType).getComponentType();
    }
    TypeElement iosArrayElement = typeUtil.getIosArray(componentType);

    ExecutableElement methodElement =
        getMultiDimensionMethod(componentType, iosArrayElement, retainedResult);
    MethodInvocation invocation = new MethodInvocation(
        new ExecutablePair(methodElement), arrayType, new SimpleName(iosArrayElement));

    // Add the dimension count argument.
    invocation.addArgument(NumberLiteral.newIntLiteral(dimensions.size(), typeUtil));

    // Create the dimensions array.
    ArrayInitializer dimensionsArg = new ArrayInitializer(typeUtil.getArrayType(typeUtil.getInt()));
    for (Expression e : dimensions) {
      dimensionsArg.addExpression(e.copy());
    }
    invocation.addArgument(dimensionsArg);

    if (!componentType.getKind().isPrimitive()) {
      invocation.addArgument(new TypeLiteral(componentType, typeUtil));
    }

    return invocation;
  }

  private ExecutableElement getMultiDimensionMethod(
      TypeMirror componentType, TypeElement iosArrayType, boolean retainedResult) {
    boolean isPrimitive = componentType.getKind().isPrimitive();
    String selector = (retainedResult ? "newArray" : "array") + "WithDimensions:lengths:"
        + (isPrimitive ? "" : "type:");
    GeneratedExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
        selector, TypeUtil.IOS_OBJECT_ARRAY.asType(), iosArrayType)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    TypeMirror intType = typeUtil.getInt();
    element.addParameter(GeneratedVariableElement.newParameter("dimensions", intType, element));
    element.addParameter(GeneratedVariableElement.newParameter(
        "dimensionLengths", new PointerType(intType), element));
    if (!isPrimitive) {
      element.addParameter(GeneratedVariableElement.newParameter(
          "type", TypeUtil.IOS_CLASS.asType(), element));
    }
    return element;
  }

  // We must handle object array assignment before its children because if the
  // rhs is an array creation, we can optimize with "SetAndConsume".
  @Override
  public boolean visit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    TypeMirror lhsType = lhs.getTypeMirror();
    if (lhs instanceof ArrayAccess && !lhsType.getKind().isPrimitive()) {
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
    TypeElement iosArrayElement = typeUtil.getIosArray(componentType);

    node.replaceWith(newArrayAccess(
        node, componentType, iosArrayElement, TranslationUtil.isAssigned(node)));
  }

  private Expression newArrayAccess(
      ArrayAccess arrayAccessNode, TypeMirror componentType, TypeElement iosArrayElement,
      boolean assignable) {
    String funcName = ElementUtil.getName(iosArrayElement) + "_Get";
    TypeMirror returnType = componentType;
    TypeMirror declaredReturnType =
        componentType.getKind().isPrimitive() ? componentType : TypeUtil.ID_TYPE;
    if (assignable) {
      funcName += "Ref";
      returnType = declaredReturnType = new PointerType(componentType);
    }
    FunctionElement element = new FunctionElement(funcName, declaredReturnType, iosArrayElement)
        .addParameters(iosArrayElement.asType(), typeUtil.getInt());
    FunctionInvocation invocation = new FunctionInvocation(element, returnType);
    invocation.addArgument(arrayAccessNode.getArray().copy());
    invocation.addArgument(arrayAccessNode.getIndex().copy());
    if (assignable) {
      return new PrefixExpression(componentType, PrefixExpression.Operator.DEREFERENCE, invocation);
    }
    return invocation;
  }

  private FunctionInvocation newArrayAssignment(
      Assignment assignmentNode, ArrayAccess arrayAccessNode, TypeMirror componentType) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.getKind().isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    Expression value = TreeUtil.remove(assignmentNode.getRightHandSide());
    Expression retainedValue = TranslationUtil.retainResult(value);
    String funcName = "IOSObjectArray_Set";
    if (retainedValue != null) {
      funcName = "IOSObjectArray_SetAndConsume";
      value = retainedValue;
    }
    TypeElement objArrayType = TypeUtil.IOS_OBJECT_ARRAY;
    TypeMirror idType = TypeUtil.ID_TYPE;
    FunctionElement element = new FunctionElement(funcName, idType, objArrayType)
        .addParameters(objArrayType.asType(), typeUtil.getInt(), idType);
    FunctionInvocation invocation = new FunctionInvocation(element, componentType);
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
    TypeMirror exprType = expr.getTypeMirror();
    if (name.getIdentifier().equals("length") && TypeUtil.isArray(exprType)) {
      VariableElement sizeField = GeneratedVariableElement.newField(
          "size", typeUtil.getInt(),
          typeUtil.getIosArray(((ArrayType) exprType).getComponentType()));
      node.replaceWith(new FieldAccess(sizeField, TreeUtil.remove(expr)));
    }
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    TypeMirror type = node.getRightOperand().getTypeMirror();
    if (!TypeUtil.isArray(type) || ((ArrayType) type).getComponentType().getKind().isPrimitive()) {
      return;
    }
    GeneratedExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
        "isInstance", typeUtil.getBoolean(), TypeUtil.IOS_CLASS);
    element.addParameter(
        GeneratedVariableElement.newParameter("object", TypeUtil.ID_TYPE, element));
    MethodInvocation invocation =
        new MethodInvocation(new ExecutablePair(element), new TypeLiteral(type, typeUtil));
    invocation.addArgument(TreeUtil.remove(node.getLeftOperand()));
    node.replaceWith(invocation);
  }
}
