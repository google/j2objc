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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Map;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 * Must be run after JavaToIOSMethodTranslator because the varargs conversion
 * needs to know if the method is mapped.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends TreeVisitor {

  private static final IOSTypeBinding ARRAY_BASE_TYPE = IOSTypeBinding.newUnmappedClass("IOSArray");

  private static final ImmutableMap<String, String> INIT_METHODS =
      ImmutableMap.<String, String>builder()
      .put("IOSBooleanArray", " arrayWithBooleans:(BOOL *)booleans count:(int)count")
      .put("IOSByteArray", " arrayWithBytes:(char *)bytes count:(int)count")
      .put("IOSCharArray", " arrayWithChars:(unichar *)chars count:(int)count")
      .put("IOSDoubleArray", " arrayWithDoubles:(double *)doubles count:(int)count")
      .put("IOSFloatArray", " arrayWithFloats:(float *)floats count:(int)count")
      .put("IOSIntArray", " arrayWithInts:(int *)ints count:(int)count")
      .put("IOSLongArray", " arrayWithLongs:(long long *)longs count:(int)count")
      .put("IOSShortArray", " arrayWithShorts:(shorts *)shorts count:(int)count")
      .put("IOSObjectArray",
           " arrayWithObjects:(id *)objects count:(int)count type:(IOSClass *)type")
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

  private Map<IOSTypeBinding, IOSMethodBinding> initMethods = Maps.newHashMap();
  private Map<IOSTypeBinding, IOSMethodBinding> singleDimMethods = Maps.newHashMap();
  private Map<IOSTypeBinding, IOSMethodBinding> multiDimMethods = Maps.newHashMap();

  private final IOSMethodBinding arrayCountMethod = IOSMethodBinding.newMethod(
      IOSMethod.create("IOSArray count"), Modifier.PUBLIC, Types.resolveJavaType("int"),
      ARRAY_BASE_TYPE);

  @Override
  public void endVisit(ArrayCreation node) {
    node.replaceWith(createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    ITypeBinding arrayType = node.getTypeBinding();
    assert arrayType.isArray();
    ArrayInitializer initializer = node.getInitializer();
    if (initializer != null) {
      return newInitializedArrayInvocation(arrayType, initializer.getExpressions());
    } else {
      List<Expression> dimensions = node.getDimensions();
      if (dimensions.size() == 1) {
        return newSingleDimensionArrayInvocation(arrayType, dimensions.get(0));
      } else {
        return newMultiDimensionArrayInvocation(arrayType, dimensions);
      }
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    TreeNode parent = node.getParent();
    if (!(parent instanceof ArrayCreation)) {
      node.replaceWith(newInitializedArrayInvocation(node.getTypeBinding(), node.getExpressions()));
    }
  }

  /**
   * Varargs rewriting must be done in a visit (not a endVisit) because it needs
   * the dimensions of the args before they are rewritten.
   */
  private void rewriteVarargs(IMethodBinding method, List<Expression> args) {
    method = method.getMethodDeclaration();
    if (!method.isVarargs() || IOSMethodBinding.hasVarArgsTarget(method)) {
      return;
    }
    ITypeBinding[] paramTypes = method.getParameterTypes();
    ITypeBinding lastParam = paramTypes[paramTypes.length - 1];
    assert lastParam.isArray();
    int varargsSize = args.size() - paramTypes.length + 1;
    if (varargsSize == 1) {
      ITypeBinding lastArgType = args.get(args.size() - 1).getTypeBinding();
      if (lastArgType.isNullType()) {
        return;
      }
      if (lastParam.getDimensions() == lastArgType.getDimensions()
          && lastParam.getElementType().isPrimitive()
              == lastArgType.getElementType().isPrimitive()) {
        // Last argument is already an array.
        return;
      }
    }

    List<Expression> varargs = args.subList(paramTypes.length - 1, args.size());
    List<Expression> varargsCopy = Lists.newArrayList(varargs);
    varargs.clear();
    if (varargsCopy.isEmpty()) {
      args.add(newSingleDimensionArrayInvocation(lastParam, NumberLiteral.newIntLiteral(0)));
    } else {
      ArrayInitializer newArray = new ArrayInitializer(lastParam);
      newArray.getExpressions().addAll(varargsCopy);
      args.add(newArray);
    }
  }

  private MethodInvocation newInitializedArrayInvocation(
      ITypeBinding arrayType, List<Expression> elements) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getInitializeMethod(iosArrayBinding);
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

  private IOSMethodBinding getInitializeMethod(IOSTypeBinding arrayType) {
    IOSMethodBinding binding = initMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    String methodName = INIT_METHODS.get(arrayType.getName());
    assert methodName != null;
    IOSMethod iosMethod = IOSMethod.create(arrayType.getName() + methodName);
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(arrayType);
    binding.addParameter(Types.resolveJavaType("int"));
    if (arrayType.getName().equals("IOSObjectArray")) {
      binding.addParameter(Types.getIOSClass());
    }
    initMethods.put(arrayType, binding);
    return binding;
  }

  private MethodInvocation newSingleDimensionArrayInvocation(
      ITypeBinding arrayType, Expression dimensionExpr) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getSingleDimensionMethod(iosArrayBinding);
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

  private IOSMethodBinding getSingleDimensionMethod(IOSTypeBinding arrayType) {
    IOSMethodBinding binding = singleDimMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName() + " arrayWithLength:(int)length"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, arrayType, arrayType);
    binding.addParameter(Types.resolveJavaType("int"));
    if (needsTypeParam) {
      binding.addParameter(Types.getIOSClass());
    }
    singleDimMethods.put(arrayType, binding);
    return binding;
  }

  private MethodInvocation newMultiDimensionArrayInvocation(
      ITypeBinding arrayType, List<Expression> dimensions) {
    assert dimensions.size() > 1;
    ITypeBinding componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      componentType = componentType.getComponentType();
    }
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getMultiDimensionMethod(iosArrayBinding);
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

  private IOSMethodBinding getMultiDimensionMethod(IOSTypeBinding arrayType) {
    IOSMethodBinding binding = multiDimMethods.get(arrayType);
    if (binding != null) {
      return binding;
    }
    boolean needsTypeParam = arrayType.getName().equals("IOSObjectArray");
    IOSMethod iosMethod = IOSMethod.create(
        arrayType.getName()
        + " arrayWithDimensions:(int)dimensionCount lengths:(int *)dimensionLengths"
        + (needsTypeParam ? " type:(IOSClass *)type" : ""));
    binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, Types.resolveIOSType("IOSObjectArray"),
        arrayType);
    ITypeBinding intType = Types.resolveJavaType("int");
    binding.addParameter(intType);
    binding.addParameter(GeneratedTypeBinding.newArrayType(intType));
    if (needsTypeParam) {
      binding.addParameter(Types.getIOSClass());
    }
    multiDimMethods.put(arrayType, binding);
    return binding;
  }

  @Override
  public void endVisit(ArrayAccess node) {
    ITypeBinding arrayType = node.getArray().getTypeBinding();
    assert arrayType.isArray();
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    Assignment assignment = getArrayAssignment(node);
    if (assignment != null && !componentType.isPrimitive()) {
      assignment.getRightHandSide().accept(this);
      assignment.replaceWith(newArrayAssignment(assignment, node, componentType));
    } else {
      boolean assignable = assignment != null || needsAssignableAccess(node);
      node.replaceWith(newArrayAccess(node, componentType, iosArrayBinding, assignable));
    }
  }

  private static Assignment getArrayAssignment(ArrayAccess node) {
    TreeNode parent = node.getParent();
    if (parent instanceof Assignment) {
      Assignment assignment = (Assignment) parent;
      if (node == assignment.getLeftHandSide()) {
        return assignment;
      }
    }
    return null;
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
      if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT) {
        return true;
      }
    }
    return false;
  }

  private Map<String, IOSMethodBinding> accessFunctions = Maps.newHashMap();

  private IOSMethodBinding getArrayAccessBinding(
      ITypeBinding componentType, IOSTypeBinding iosArrayBinding, boolean assignable) {
    String name = iosArrayBinding.getName() + "_Get";
    if (assignable) {
      name += "Ref";
    }
    IOSMethodBinding binding = accessFunctions.get(name);
    if (binding == null) {
      ITypeBinding declaredReturnType =
          componentType.isPrimitive() ? componentType : Types.resolveIOSType("id");
      if (assignable) {
        declaredReturnType = new PointerTypeBinding(declaredReturnType);
      }
      binding = IOSMethodBinding.newFunction(
          name, declaredReturnType, iosArrayBinding, iosArrayBinding, Types.resolveJavaType("int"));
      accessFunctions.put(name, binding);
    }
    return binding;
  }

  private MethodInvocation newArrayAccess(
      ArrayAccess arrayAccessNode, ITypeBinding componentType, IOSTypeBinding iosArrayBinding,
      boolean assignable) {
    IOSMethodBinding binding = getArrayAccessBinding(componentType, iosArrayBinding, assignable);
    if (!componentType.isPrimitive()) {
      binding = IOSMethodBinding.newTypedInvocation(binding, componentType);
    }
    MethodInvocation invocation = new MethodInvocation(binding, null);
    invocation.getArguments().add(arrayAccessNode.getArray().copy());
    invocation.getArguments().add(arrayAccessNode.getIndex().copy());
    if (assignable) {
      invocation = MethodInvocation.newDereference(invocation);
    }
    return invocation;
  }

  private static IOSMethodBinding createObjectArrayAssignmentFunction() {
    ITypeBinding idType = Types.resolveIOSType("id");
    ITypeBinding objArrayType = Types.resolveIOSType("IOSObjectArray");
    return IOSMethodBinding.newFunction(
        "IOSObjectArray_Set", idType, objArrayType, objArrayType, Types.resolveJavaType("int"),
        idType);
  }

  private IOSMethodBinding objectArrayAssignmentFunction = createObjectArrayAssignmentFunction();

  private MethodInvocation newArrayAssignment(
      Assignment assignmentNode, ArrayAccess arrayAccessNode, ITypeBinding componentType) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    IOSMethodBinding binding =
        IOSMethodBinding.newTypedInvocation(objectArrayAssignmentFunction, componentType);

    MethodInvocation invocation = new MethodInvocation(binding, null);
    List<Expression> args = invocation.getArguments();
    args.add(arrayAccessNode.getArray().copy());
    args.add(arrayAccessNode.getIndex().copy());
    args.add(assignmentNode.getRightHandSide().copy());
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
    if (name.getIdentifier().equals("length") && expr.getTypeBinding().isArray()) {
      // needs cast: count returns an unsigned value
      node.replaceWith(new CastExpression(
          Types.resolveJavaType("int"), new MethodInvocation(arrayCountMethod, expr.copy())));
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

  @Override
  public boolean visit(ClassInstanceCreation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
    return true;
  }
}
