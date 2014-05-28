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
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeLiteral;

import java.util.List;
import java.util.Map;

/**
 * Rewrites array creation into a method invocation on an IOSArray class.
 * Must be run after JavaToIOSMethodTranslator because the varargs conversion
 * needs to know if the method is mapped.
 *
 * @author Keith Stanger
 */
public class ArrayRewriter extends ErrorReportingASTVisitor {

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
    ASTUtil.setProperty(node, createInvocation(node));
  }

  private MethodInvocation createInvocation(ArrayCreation node) {
    AST ast = node.getAST();
    ITypeBinding arrayType = Types.getTypeBinding(node);
    assert arrayType.isArray();
    ArrayInitializer initializer = node.getInitializer();
    if (initializer != null) {
      return newInitializedArrayInvocation(ast, arrayType, ASTUtil.getExpressions(initializer));
    } else {
      List<Expression> dimensions = ASTUtil.getDimensions(node);
      if (dimensions.size() == 1) {
        return newSingleDimensionArrayInvocation(ast, arrayType, dimensions.get(0));
      } else {
        return newMultiDimensionArrayInvocation(ast, arrayType, dimensions);
      }
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    ASTNode parent = node.getParent();
    if (!(parent instanceof ArrayCreation)) {
      ASTUtil.setProperty(node, newInitializedArrayInvocation(
          node.getAST(), Types.getTypeBinding(node), ASTUtil.getExpressions(node)));
    }
  }

  /**
   * Varargs rewriting must be done in a visit (not a endVisit) because it needs
   * the dimensions of the args before they are rewritten.
   */
  private void rewriteVarargs(IMethodBinding method, List<Expression> args, AST ast) {
    method = method.getMethodDeclaration();
    if (!method.isVarargs() || IOSMethodBinding.hasVarArgsTarget(method)) {
      return;
    }
    ITypeBinding[] paramTypes = method.getParameterTypes();
    ITypeBinding lastParam = paramTypes[paramTypes.length - 1];
    assert lastParam.isArray();
    int varargsSize = args.size() - paramTypes.length + 1;
    if (varargsSize == 1) {
      ITypeBinding lastArgType = Types.getTypeBinding(args.get(args.size() - 1));
      if (lastArgType.isNullType()) {
        return;
      }
      if (lastParam.getDimensions() == lastArgType.getDimensions()) {
        // Last argument is already an array.
        return;
      }
    }

    List<Expression> varargs = args.subList(paramTypes.length - 1, args.size());
    List<Expression> varargsCopy = Lists.newArrayList(varargs);
    varargs.clear();
    if (varargsCopy.isEmpty()) {
      args.add(newSingleDimensionArrayInvocation(
          ast, lastParam, ASTFactory.makeIntLiteral(ast, 0)));
    } else {
      ArrayInitializer newArray = ast.newArrayInitializer();
      Types.addBinding(newArray, lastParam);
      ASTUtil.getExpressions(newArray).addAll(varargsCopy);
      args.add(newArray);
    }
  }

  private MethodInvocation newInitializedArrayInvocation(
      AST ast, ITypeBinding arrayType, List<Expression> elements) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getInitializeMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Create the array initializer and add it as the first parameter.
    ArrayInitializer arrayInit = ast.newArrayInitializer();
    Types.addBinding(arrayInit, arrayType);
    for (Expression element : elements) {
      ASTUtil.getExpressions(arrayInit).add(NodeCopier.copySubtree(ast, element));
    }
    ASTUtil.getArguments(invocation).add(arrayInit);

    // Add the array size parameter.
    ASTUtil.getArguments(invocation).add(
        ASTFactory.makeIntLiteral(ast, arrayInit.expressions().size()));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(newTypeLiteral(ast, componentType));
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
      AST ast, ITypeBinding arrayType, Expression dimensionExpr) {
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getSingleDimensionMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Add the array length argument.
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, dimensionExpr));

    // Add the type argument for object arrays.
    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(newTypeLiteral(ast, componentType));
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
      AST ast, ITypeBinding arrayType, List<Expression> dimensions) {
    assert dimensions.size() > 1;
    ITypeBinding componentType = arrayType;
    for (int i = 0; i < dimensions.size(); i++) {
      componentType = componentType.getComponentType();
    }
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    IOSMethodBinding methodBinding = getMultiDimensionMethod(iosArrayBinding);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, methodBinding, ASTFactory.newSimpleName(ast, iosArrayBinding));

    // Add the dimension count argument.
    ASTUtil.getArguments(invocation).add(ASTFactory.makeIntLiteral(ast, dimensions.size()));

    // Create the dimensions array.
    ArrayInitializer dimensionsArg = ast.newArrayInitializer();
    Types.addBinding(dimensionsArg,
        GeneratedTypeBinding.newArrayType(Types.resolveJavaType("int")));
    for (Expression e : dimensions) {
      ASTUtil.getExpressions(dimensionsArg).add(NodeCopier.copySubtree(ast, e));
    }
    ASTUtil.getArguments(invocation).add(dimensionsArg);

    if (!componentType.isPrimitive()) {
      ASTUtil.getArguments(invocation).add(newTypeLiteral(ast, componentType));
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
    AST ast = node.getAST();
    ITypeBinding arrayType = Types.getTypeBinding(node.getArray());
    assert arrayType.isArray();
    ITypeBinding componentType = arrayType.getComponentType();
    IOSTypeBinding iosArrayBinding = Types.resolveArrayType(componentType);

    Assignment assignment = getArrayAssignment(node);
    if (assignment != null && !componentType.isPrimitive()) {
      assignment.getRightHandSide().accept(this);
      ASTUtil.setProperty(assignment, newArrayAssignment(ast, assignment, node, componentType));
    } else {
      boolean assignable = assignment != null || needsAssignableAccess(node);
      ASTUtil.setProperty(node, newArrayAccess(
          ast, node, componentType, iosArrayBinding, assignable));
    }
  }

  private static Assignment getArrayAssignment(ArrayAccess node) {
    ASTNode parent = node.getParent();
    if (parent instanceof Assignment) {
      Assignment assignment = (Assignment) parent;
      if (node == assignment.getLeftHandSide()) {
        return assignment;
      }
    }
    return null;
  }

  private static boolean needsAssignableAccess(ArrayAccess node) {
    ASTNode parent = node.getParent();
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
      AST ast, ArrayAccess arrayAccessNode, ITypeBinding componentType,
      IOSTypeBinding iosArrayBinding, boolean assignable) {
    IOSMethodBinding binding = getArrayAccessBinding(componentType, iosArrayBinding, assignable);
    if (!componentType.isPrimitive()) {
      binding = IOSMethodBinding.newTypedInvocation(binding, componentType);
    }
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, arrayAccessNode.getArray()));
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, arrayAccessNode.getIndex()));
    if (assignable) {
      invocation = ASTFactory.newDereference(ast, invocation);
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
      AST ast, Assignment assignmentNode, ArrayAccess arrayAccessNode, ITypeBinding componentType) {
    Assignment.Operator op = assignmentNode.getOperator();
    assert !componentType.isPrimitive();
    assert op == Assignment.Operator.ASSIGN;

    IOSMethodBinding binding =
        IOSMethodBinding.newTypedInvocation(objectArrayAssignmentFunction, componentType);

    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    args.add(NodeCopier.copySubtree(ast, arrayAccessNode.getArray()));
    args.add(NodeCopier.copySubtree(ast, arrayAccessNode.getIndex()));
    args.add(NodeCopier.copySubtree(ast, assignmentNode.getRightHandSide()));
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
    if (name.getIdentifier().equals("length") && Types.getTypeBinding(expr).isArray()) {
      AST ast = node.getAST();
      // needs cast: count returns an unsigned value
      ASTUtil.setProperty(node, ASTFactory.newCastExpression(ast,
          ASTFactory.newMethodInvocation(ast, arrayCountMethod, NodeCopier.copySubtree(ast, expr)),
          Types.resolveJavaType("int")));
    }
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    ITypeBinding type = Types.getTypeBinding(node.getRightOperand());
    if (!type.isArray() || type.getComponentType().isPrimitive()) {
      return;
    }
    AST ast = node.getAST();
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        ISINSTANCE_METHOD, Modifier.PUBLIC, Types.resolveJavaType("boolean"), Types.getIOSClass());
    binding.addParameter(Types.resolveIOSType("id"));
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, newTypeLiteralInvocation(ast, type));
    ASTUtil.getArguments(invocation).add(NodeCopier.copySubtree(ast, node.getLeftOperand()));
    ASTUtil.setProperty(node, invocation);
  }

  @Override
  public void endVisit(TypeLiteral node) {
    ITypeBinding type = Types.getTypeBinding(node.getType());
    if (type.isArray()) {
      ASTUtil.setProperty(node, newTypeLiteralInvocation(node.getAST(), type));
    }
  }

  private static Expression newTypeLiteral(AST ast, ITypeBinding type) {
    if (type.isArray()) {
      return newTypeLiteralInvocation(ast, type);
    }
    return ASTFactory.newTypeLiteral(ast, type);
  }

  private static MethodInvocation newTypeLiteralInvocation(AST ast, ITypeBinding type) {
    assert type.isArray();
    ITypeBinding elementType = type.getElementType();
    IOSTypeBinding iosArrayType = Types.resolveArrayType(elementType);
    int dimensions = type.getDimensions();
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        getTypeLiteralMethod(elementType, dimensions), Modifier.PUBLIC | Modifier.STATIC,
        Types.getIOSClass(), iosArrayType);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, ASTFactory.newSimpleName(ast, iosArrayType));
    if (dimensions > 1) {
      binding.addParameter(Types.resolveJavaType("int"));
      ASTUtil.getArguments(invocation).add(
          ASTFactory.makeLiteral(ast, Integer.valueOf(dimensions), Types.resolveJavaType("int")));
    }
    if (!elementType.isPrimitive()) {
      binding.addParameter(Types.getIOSClass());
      ASTUtil.getArguments(invocation).add(newTypeLiteral(ast, elementType));
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
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    rewriteVarargs(Types.getMethodBinding(node), ASTUtil.getArguments(node), node.getAST());
    return true;
  }
}
