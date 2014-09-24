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

import com.google.common.collect.ImmutableMap;
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
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 * Must be run after JavaToIOSMethodTranslator because the varargs conversion
 * needs to know if the method is mapped.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends TreeVisitor {

  private static final ImmutableMap<String, String> INIT_METHODS =
      ImmutableMap.<String, String>builder()
      .put("IOSBooleanArray", " arrayWithBooleans:(jboolean *)booleans count:(jint)count")
      .put("IOSByteArray", " arrayWithBytes:(jbyte *)bytes count:(jint)count")
      .put("IOSCharArray", " arrayWithChars:(jchar *)chars count:(jint)count")
      .put("IOSDoubleArray", " arrayWithDoubles:(jdouble *)doubles count:(jint)count")
      .put("IOSFloatArray", " arrayWithFloats:(jfloat *)floats count:(jint)count")
      .put("IOSIntArray", " arrayWithInts:(jint *)ints count:(jint)count")
      .put("IOSLongArray", " arrayWithLongs:(jlong *)longs count:(jint)count")
      .put("IOSShortArray", " arrayWithShorts:(jshort *)shorts count:(jint)count")
      .put("IOSObjectArray",
           " arrayWithObjects:(id *)objects count:(jint)count type:(IOSClass *)type")
      .build();

  private static final ImmutableMap<String, String> RETAINED_INIT_METHODS =
      ImmutableMap.<String, String>builder()
      .put("IOSBooleanArray", " newArrayWithBooleans:(jboolean *)booleans count:(jint)count")
      .put("IOSByteArray", " newArrayWithBytes:(jbyte *)bytes count:(jint)count")
      .put("IOSCharArray", " newArrayWithChars:(jchar *)chars count:(jint)count")
      .put("IOSDoubleArray", " newArrayWithDoubles:(jdouble *)doubles count:(jint)count")
      .put("IOSFloatArray", " newArrayWithFloats:(jfloat *)floats count:(jint)count")
      .put("IOSIntArray", " newArrayWithInts:(jint *)ints count:(jint)count")
      .put("IOSLongArray", " newArrayWithLongs:(jlong *)longs count:(jint)count")
      .put("IOSShortArray", " newArrayWithShorts:(jshort *)shorts count:(jint)count")
      .put("IOSObjectArray",
           " newArrayWithObjects:(id *)objects count:(jint)count type:(IOSClass *)type")
      .build();

  private static final IOSMethod IOSCLASS_METHOD = IOSMethod.create("IOSArray iosClass");
  private static final IOSMethod IOSCLASS_METHOD_DIM = IOSMethod.create(
      "IOSArray iosClassWithDimensions:(NSUInteger)dimensions");
  private static final IOSMethod IOSCLASS_METHOD_OBJ = IOSMethod.create(
      "IOSObjectArray iosClassWithType:(IOSClass *)type");
  private static final IOSMethod IOSCLASS_METHOD_OBJ_DIM = IOSMethod.create(
      "IOSObjectArray iosClassWithDimensions:(NSUInteger)dimensions type:(IOSClass *)type");

  private static final IOSMethod ISINSTANCE_METHOD = IOSMethod.create(
      "IOSClass isInstance:(id)object");

  @Override
  public void endVisit(ArrayCreation node) {
    node.replaceWith(createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    ITypeBinding arrayType = node.getTypeBinding();
    assert arrayType.isArray();
    boolean retainedResult = node.hasRetainedResult();
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
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getInitializeMethod(iosArrayBinding, retainedResult);
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, new SimpleName(iosArrayBinding));

    // Create the array initializer and add it as the first parameter.
    ArrayInitializer arrayInit = new ArrayInitializer(arrayType);
    for (Expression element : elements) {
      arrayInit.getExpressions().add(element.copy());
    }
    invocation.getArguments().add(arrayInit);

    // Add the array size parameter.
    invocation.getArguments().add(NumberLiteral.newIntLiteral(arrayInit.getExpressions().size()));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      invocation.getArguments().add(newTypeLiteral(componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getInitializeMethod(IOSTypeBinding arrayType, boolean retainedResult) {
    String typeName = arrayType.getName();
    String methodName =
        retainedResult ? RETAINED_INIT_METHODS.get(typeName) : INIT_METHODS.get(typeName);
    assert methodName != null;
    IOSMethod iosMethod = IOSMethod.create(typeName + methodName);
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(arrayType);
    binding.addParameter(Types.resolveJavaType("int"));
    if (arrayType.getName().equals("IOSObjectArray")) {
      binding.addParameter(Types.getIOSClass());
    }
    return binding;
  }

  private MethodInvocation newSingleDimensionArrayInvocation(
      ITypeBinding arrayType, Expression dimensionExpr, boolean retainedResult) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getSingleDimensionMethod(iosArrayBinding, retainedResult);
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, new SimpleName(iosArrayBinding));

    // Add the array length argument.
    invocation.getArguments().add(dimensionExpr.copy());

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      invocation.getArguments().add(newTypeLiteral(componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getSingleDimensionMethod(
      IOSTypeBinding arrayType, boolean retainedResult) {
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName() + (retainedResult ? " newArray" : " array") + "WithLength:(int)length"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(Types.resolveJavaType("int"));
    if (needsTypeParam) {
      binding.addParameter(Types.getIOSClass());
    }
    return binding;
  }

  private MethodInvocation newMultiDimensionArrayInvocation(
      ITypeBinding arrayType, List<Expression> dimensions, boolean retainedResult) {
    assert dimensions.size() > 1;
    ITypeBinding componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      componentType = componentType.getComponentType();
    }
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getMultiDimensionMethod(iosArrayBinding, retainedResult);
    MethodInvocation invocation =
        new MethodInvocation(methodBinding, new SimpleName(iosArrayBinding));

    // Add the dimension count argument.
    invocation.getArguments().add(NumberLiteral.newIntLiteral(dimensions.size()));

    // Create the dimensions array.
    ArrayInitializer dimensionsArg = new ArrayInitializer(
        GeneratedTypeBinding.newArrayType(Types.resolveJavaType("int")));
    for (Expression e : dimensions) {
      dimensionsArg.getExpressions().add(e.copy());
    }
    invocation.getArguments().add(dimensionsArg);

    if (!componentType.isPrimitive()) {
      invocation.getArguments().add(newTypeLiteral(componentType));
    }

    return invocation;
  }

  private IOSMethodBinding getMultiDimensionMethod(
      IOSTypeBinding arrayType, boolean retainedResult) {
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName() + (retainedResult ? " newArray" : " array")
        + "WithDimensions:(int)dimensionCount lengths:(int *)dimensionLengths"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, Types.resolveIOSType("IOSObjectArray"),
        arrayType);
    ITypeBinding intType = Types.resolveJavaType("int");
    binding.addParameter(intType);
    binding.addParameter(GeneratedTypeBinding.newArrayType(intType));
    if (needsTypeParam) {
      binding.addParameter(Types.getIOSClass());
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
    ITypeBinding componentType = node.getTypeBinding();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    boolean assignable = needsAssignableAccess(node);
    node.replaceWith(newArrayAccess(node, componentType, iosArrayBinding, assignable));
  }

  private static boolean needsAssignableAccess(ArrayAccess node) {
    TreeNode parent = node.getParent();
    if (parent instanceof PostfixExpression) {
      PostfixExpression.Operator op = ((PostfixExpression) parent).getOperator();
      if (op == PostfixExpression.Operator.INCREMENT
          || op == PostfixExpression.Operator.DECREMENT) {
        return true;
      }
    } else if (parent instanceof PrefixExpression) {
      PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
      if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT
          || op == PrefixExpression.Operator.ADDRESS_OF) {
        return true;
      }
    } else if (parent instanceof Assignment) {
      return node == ((Assignment) parent).getLeftHandSide();
    }
    return false;
  }

  private Expression newArrayAccess(
      ArrayAccess arrayAccessNode, ITypeBinding componentType, IOSTypeBinding iosArrayBinding,
      boolean assignable) {
    String funcName = iosArrayBinding.getName() + "_Get";
    ITypeBinding returnType = componentType;
    ITypeBinding declaredReturnType =
        componentType.isPrimitive() ? componentType : Types.resolveIOSType("id");
    if (assignable) {
      funcName += "Ref";
      returnType = declaredReturnType = Types.getPointerType(declaredReturnType);
    }
    FunctionInvocation invocation = new FunctionInvocation(
        funcName, returnType, declaredReturnType, iosArrayBinding);
    invocation.getArguments().add(arrayAccessNode.getArray().copy());
    invocation.getArguments().add(arrayAccessNode.getIndex().copy());
    if (assignable) {
      return new PrefixExpression(PrefixExpression.Operator.DEREFERENCE, invocation);
    }
    return invocation;
  }

  private FunctionInvocation newArrayAssignment(
      Assignment assignmentNode, ArrayAccess arrayAccessNode, ITypeBinding componentType) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    Expression value = TreeUtil.remove(assignmentNode.getRightHandSide());
    String funcName =
        TreeUtil.retainResult(value) ? "IOSObjectArray_SetAndConsume" : "IOSObjectArray_Set";
    FunctionInvocation invocation = new FunctionInvocation(
        funcName, componentType, Types.resolveIOSType("id"),
        Types.resolveIOSType("IOSObjectArray"));
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
          "size", Modifier.PUBLIC, Types.resolveJavaType("int"), true, false,
          Types.mapType(exprType), null);
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
        ISINSTANCE_METHOD, Modifier.PUBLIC, Types.resolveJavaType("boolean"), Types.getIOSClass());
    binding.addParameter(Types.resolveIOSType("id"));
    MethodInvocation invocation = new MethodInvocation(binding, newTypeLiteralInvocation(type));
    invocation.getArguments().add(TreeUtil.remove(node.getLeftOperand()));
    node.replaceWith(invocation);
  }

  @Override
  public void endVisit(TypeLiteral node) {
    ITypeBinding type = node.getType().getTypeBinding();
    if (type.isArray()) {
      node.replaceWith(newTypeLiteralInvocation(type));
    }
  }

  private static Expression newTypeLiteral(ITypeBinding type) {
    if (type.isArray()) {
      return newTypeLiteralInvocation(type);
    }
    return new TypeLiteral(type);
  }

  private static MethodInvocation newTypeLiteralInvocation(ITypeBinding type) {
    assert type.isArray();
    ITypeBinding elementType = type.getElementType();
    IOSTypeBinding iosArrayType = Types.resolveArrayType(elementType);
    int dimensions = type.getDimensions();
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        getTypeLiteralMethod(elementType, dimensions), Modifier.PUBLIC | Modifier.STATIC,
        Types.getIOSClass(), iosArrayType);
    MethodInvocation invocation = new MethodInvocation(binding, new SimpleName(iosArrayType));
    if (dimensions > 1) {
      binding.addParameter(Types.resolveJavaType("int"));
      invocation.getArguments().add(NumberLiteral.newIntLiteral(dimensions));
    }
    if (!elementType.isPrimitive()) {
      binding.addParameter(Types.getIOSClass());
      invocation.getArguments().add(newTypeLiteral(elementType));
    }
    return invocation;
  }

  private static IOSMethod getTypeLiteralMethod(ITypeBinding elementType, int dimensions) {
    if (elementType.isPrimitive()) {
      if (dimensions > 1) {
        return IOSCLASS_METHOD_DIM;
      } else {
        return IOSCLASS_METHOD;
      }
    } else {
      if (dimensions > 1) {
        return IOSCLASS_METHOD_OBJ_DIM;
      } else {
        return IOSCLASS_METHOD_OBJ;
      }
    }
  }
}
