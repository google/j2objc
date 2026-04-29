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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.STATIC;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.RecordDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor9;

/**
 * Implements the @ObjectiveCKmpMethod translator.
 *
 * <p>This translator detects methods annotated with {@link ObjectiveCKmpMethod} and generates an
 * "adapter" method in Objective-C. This adapter method uses native Objective-C types (like NSArray,
 * NSDictionary, NSSet) instead of their Java counterparts (List, Map, Set), which is useful for
 * better interoperability in Kotlin Multiplatform (KMP) environments.
 *
 * <p>The generated adapter method calls the original Java method after converting the native
 * Objective-C types back to Java types using a provided adapter class.
 *
 * <h2>How the Adapter Works</h2>
 *
 * <p>The adapter class (specified in the annotation) should contain methods that convert between
 * Java collection types and native Objective-C collection types. The translator looks for methods
 * in the adapter class based on name prefixes and type matching.
 *
 * <h3>Method Selection Rules</h3>
 *
 * <p>The translator selects adapter methods based on the direction of conversion:
 *
 * <ul>
 *   <li><b>Native to Java (Method Parameters)</b>: The translator looks for methods whose name
 *       starts with "to" and whose <b>return type</b> matches the target Java type. It assumes the
 *       single parameter of this method is the native Objective-C type.
 *   <li><b>Java to Native (Method Return Value)</b>: The translator looks for methods whose name
 *       starts with "from" and whose <b>parameter type</b> matches the source Java type. It assumes
 *       the return type of this method is the native Objective-C type.
 * </ul>
 *
 * <h3>Type Matching (Selection)</h3>
 *
 * <p>The translator finds all applicable methods in the adapter class that start with the required
 * prefix ("to" or "from"). It then scores how well their signatures match the target type.
 *
 * <p>The translator prioritizes matching candidate methods by:
 *
 * <ol>
 *   <li><b>Exact Match</b> over <b>Wildcard Match</b>.
 *   <li>Deeper type arguments (higher nesting level).
 *   <li>Lower score for matching type arguments (fewer wildcards used).
 * </ol>
 *
 * <p>Wildcard matches are disallowed for mapped types (like {@code List}, {@code Map}, {@code Set}
 * , {@code Boolean}) to ensure strict typing at the conversion boundaries.
 */
public final class ObjectiveCKmpMethodTranslator extends UnitTreeVisitor {

  private static final ImmutableMap<String, String> JAVA_TO_NATIVE_TYPE_MAP =
      ImmutableMap.<String, String>builder()
          .put("com.google.common.collect.ImmutableList", "NSArray")
          .put("com.google.common.collect.ImmutableMap", "NSDictionary")
          .put("com.google.common.collect.ImmutableSet", "NSSet")
          .put("java.util.List", "NSArray")
          .put("java.util.Map", "NSDictionary")
          .put("java.util.Set", "NSSet")
          .put("java.lang.String", "NSString")
          .put("java.lang.Integer", "NSNumber")
          .put("java.lang.Long", "NSNumber")
          .put("java.lang.Double", "NSNumber")
          .put("java.lang.Float", "NSNumber")
          .put("java.lang.Boolean", "NSNumber")
          .put("java.lang.Byte", "NSNumber")
          .put("java.lang.Short", "NSNumber")
          .buildOrThrow();

  private static final ImmutableSet<String> COLLECTION_TYPES =
      ImmutableSet.of(
          "com.google.common.collect.ImmutableList",
          "com.google.common.collect.ImmutableMap",
          "com.google.common.collect.ImmutableSet",
          "java.util.List",
          "java.util.Map",
          "java.util.Set");

  private final ImmutableSet<String> supportedConversionTypes = JAVA_TO_NATIVE_TYPE_MAP.keySet();
  private final AdapterLookup adapterLookup;

  private boolean isCollectionType(TypeMirror type) {
    return COLLECTION_TYPES.contains(TypeUtil.getQualifiedName(typeUtil.erasure(type)));
  }

  public ObjectiveCKmpMethodTranslator(CompilationUnit unit) {
    super(unit);
    this.adapterLookup = new AdapterLookup(typeUtil);
  }

  private String getOverrideSignature(ExecutablePair method) {
    StringBuilder sb = new StringBuilder(ElementUtil.getName(method.element()));
    sb.append('(');
    for (TypeMirror pType : method.type().getParameterTypes()) {
      sb.append(typeUtil.getSignatureName(pType));
    }
    sb.append(')');
    return sb.toString();
  }

  private class AdapterGenerator {
    private final AbstractTypeDeclaration typeNode;
    private final TypeElement typeElem;
    private final Set<TypeElement> visitedTypes = new HashSet<>();
    private final Map<String, ExecutablePair> allMethods = new LinkedHashMap<>();
    private final Map<String, AnnotationMirror> annotatedSignatures = new LinkedHashMap<>();

    private AdapterGenerator(AbstractTypeDeclaration node) {
      typeNode = node;
      typeElem = node.getTypeElement();
    }

    private AdapterGenerator(TypeElement typeElem) {
      typeNode = null;
      this.typeElem = typeElem;
    }

    private boolean superclassHasAdapter(String signature) {
      TypeMirror superclass = typeElem.getSuperclass();
      if (TypeUtil.isNone(superclass)) {
        return false;
      }
      AdapterGenerator superGen = new AdapterGenerator(TypeUtil.asTypeElement(superclass));
      superGen.collectMethods((DeclaredType) superclass);
      return superGen.annotatedSignatures.containsKey(signature);
    }

    private void visit() {
      collectMethods((DeclaredType) typeElem.asType());

      boolean isInterface = typeElem.getKind().isInterface();

      for (Map.Entry<String, AnnotationMirror> entry : annotatedSignatures.entrySet()) {
        String signature = entry.getKey();
        AnnotationMirror annotation = entry.getValue();
        ExecutablePair method = allMethods.get(signature);

        boolean shouldGenerate;
        if (isInterface) {
          shouldGenerate = ElementUtil.getDeclaringClass(method.element()).equals(typeElem);
        } else {
          shouldGenerate = !superclassHasAdapter(signature);
        }

        if (shouldGenerate) {
          new AdapterContext(typeNode, method, annotation, isInterface).processMethod();
        }
      }
    }

    private void collectMethods(DeclaredType type) {
      if (TypeUtil.isNone(type)) {
        return;
      }
      TypeElement element = TypeUtil.asTypeElement(type);
      if (element == null || visitedTypes.contains(element)) {
        return;
      }
      visitedTypes.add(element);

      for (ExecutableElement methodElem : ElementUtil.getExecutables(element)) {
        ExecutablePair method =
            new ExecutablePair(methodElem, typeUtil.asMemberOf(type, methodElem));
        String signature = getOverrideSignature(method);

        if (!allMethods.containsKey(signature)) {
          allMethods.put(signature, method);
        } else {
          ExecutablePair existing = allMethods.get(signature);
          if (takesPrecedence(method, existing)) {
            allMethods.put(signature, method);
          }
        }

        AnnotationMirror annotation =
            ElementUtil.getAnnotation(methodElem, ObjectiveCKmpMethod.class);
        if (annotation != null && !annotatedSignatures.containsKey(signature)) {
          annotatedSignatures.put(signature, annotation);
        }
      }

      for (TypeMirror supertype : typeUtil.directSupertypes(type)) {
        collectMethods((DeclaredType) supertype);
      }
    }

    private boolean takesPrecedence(ExecutablePair a, ExecutablePair b) {
      if (b == null) {
        return true;
      }
      // Since we traverse from subclass to superclass/interfaces (bottom-up), 'b' (already seen)
      // is generally more specific than 'a' (currently visiting).
      // The only case 'a' should replace 'b' is when 'a' is a concrete class implementation
      // and 'b' is an interface declaration, ensuring we use the class's method element.
      boolean aIsClass = ElementUtil.getDeclaringClass(a.element()).getKind().isClass();
      boolean bIsClass = ElementUtil.getDeclaringClass(b.element()).getKind().isClass();
      return aIsClass && !bIsClass;
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    new AdapterGenerator(node).visit();
  }

  @Override
  public void endVisit(RecordDeclaration node) {
    new AdapterGenerator(node).visit();
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    new AdapterGenerator(node).visit();
  }

  /** Context for processing a single method and generating its Objective-C adapter. */
  private class AdapterContext {
    private final String selector;
    private final AbstractTypeDeclaration typeNode;
    private final ExecutableElement originalMethodExecutable;
    private final TypeMirror originalMethodReturnType;
    private final ImmutableList<? extends VariableElement> originalMethodParameters;
    private final TypeMirror adapter;
    private final boolean isInterface;

    private record ParameterMapping(
        List<Expression> adaptingArguments, List<VariableElement> adapterParameters) {}

    private AdapterContext(
        AbstractTypeDeclaration typeNode,
        ExecutablePair originalMethod,
        AnnotationMirror annotation,
        boolean isInterface) {
      this.typeNode = typeNode;
      this.originalMethodExecutable = originalMethod.element();
      this.originalMethodReturnType = originalMethod.type().getReturnType();
      this.originalMethodParameters =
          ImmutableList.copyOf(originalMethodExecutable.getParameters());
      this.selector = (String) ElementUtil.getAnnotationValue(annotation, "selector");
      this.adapter = (TypeMirror) ElementUtil.getAnnotationValue(annotation, "adapter");
      this.isInterface = isInterface;
    }

    /** Orchestrates the parameter conversion and adapter method creation. */
    private void processMethod() {
      // Calculate the return type of the adapter method. If the original method returns void,
      // the adapter method also returns void. Otherwise, calculate the native Objective-C type.
      TypeMirror adapterReturnType =
          TypeUtil.isVoid(originalMethodReturnType)
              ? originalMethodReturnType
              : calculateNativeType(originalMethodReturnType, originalMethodExecutable);

      // For static methods and constructors, we always generate calls to the C-style function
      // generated by J2ObjC for the static method, rather than an Objective-C class method. This is
      // because some clients have no-wrapper-methods enabled, in which case the class methods might
      // not be generated. We also generate an Objective-C class method or initializer if wrapper
      // methods are. enabled.
      if (ElementUtil.isConstructor(originalMethodExecutable)) {
        generateConstructorFunctionDeclaration();
        if (options.emitWrapperMethods()) {
          generateConstructorMethodDeclaration(adapterReturnType);
        }
      } else if (ElementUtil.isStatic(originalMethodExecutable)) {
        // Create an FunctionInvocation to call the original static Java method.
        FunctionElement functionElement =
            new FunctionElement(
                nameTable.getFullFunctionName(originalMethodExecutable),
                originalMethodExecutable.getReturnType(),
                typeNode.getTypeElement());
        generateStaticFunctionDeclaration(adapterReturnType, functionElement);
        if (options.emitWrapperMethods()) {
          generateStaticMethodDeclaration(adapterReturnType, functionElement);
        }
      } else {
        generateInstanceMethodDeclaration(adapterReturnType);
      }
    }

    private void generateStaticFunctionDeclaration(
        TypeMirror adapterReturnType, FunctionElement functionElement) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      FunctionInvocation functionInvocation =
          createFunctionInvocation(functionElement, adaptingArguments);

      Block functionBodyBlock = new Block();
      // Create the return statement for the adapter method body.
      functionBodyBlock.addStatement(createReturnStatement(functionInvocation, adapterReturnType));

      // Add the new function declaration to the type node.
      String selectorName = selector.replaceAll(":", "_");
      String functionName = nameTable.getFullName(typeNode.getTypeElement()) + "_" + selectorName;
      FunctionDeclaration functionDecl =
          new FunctionDeclaration(functionName, adapterReturnType, originalMethodExecutable);
      functionDecl.setBody(functionBodyBlock);
      for (VariableElement adapterParam : adapterParameters) {
        functionDecl.getParameters().add(new SingleVariableDeclaration(adapterParam));
      }
      typeNode.addBodyDeclaration(functionDecl);
    }

    private FunctionInvocation createFunctionInvocation(
        FunctionElement functionElement, List<Expression> adaptingArguments) {
      // Create a FunctionInvocation to call the original static Java method.
      FunctionInvocation invocation =
          new FunctionInvocation(functionElement, originalMethodExecutable.getReturnType());
      adaptingArguments.forEach(invocation::addArgument);
      return invocation;
    }

    private void generateStaticMethodDeclaration(
        TypeMirror adapterReturnType, FunctionElement functionElement) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);

      FunctionInvocation methodFunctionInvocation =
          createFunctionInvocation(functionElement, adaptingArguments);

      addMethodDeclaration(
          adapterMethodExecutable, methodFunctionInvocation, adapterReturnType, adapterParameters);
    }

    private void generateInstanceMethodDeclaration(TypeMirror adapterReturnType) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);

      // Create a MethodInvocation to call the original instance Java method.
      MethodInvocation adaptingMethodInvocation =
          new MethodInvocation(new ExecutablePair(originalMethodExecutable), null);
      // Add the (potentially converted) arguments to the MethodInvocation.
      adaptingArguments.forEach(adaptingMethodInvocation::addArgument);

      addMethodDeclaration(
          adapterMethodExecutable, adaptingMethodInvocation, adapterReturnType, adapterParameters);
    }

    private FunctionInvocation createConstructorInvocation(TypeElement declaringClass) {
      String functionName = nameTable.getFullFunctionName(originalMethodExecutable);
      FunctionElement funcElement =
          new FunctionElement(functionName, typeUtil.getVoid(), declaringClass);
      funcElement.addParameters(declaringClass.asType()); // self
      funcElement.addParameters(ElementUtil.asTypes(originalMethodExecutable.getParameters()));
      return new FunctionInvocation(funcElement, typeUtil.getVoid());
    }

    private void generateConstructorFunctionDeclaration() {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(originalMethodExecutable);

      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      FunctionInvocation functionInvocation = createConstructorInvocation(declaringClass);

      Block functionBodyBlock = new Block();

      // Generate C-style function
      String selectorName = selector.replaceAll(":", "_");
      String cFunctionName = nameTable.getFullName(declaringClass) + "_" + selectorName;
      FunctionDeclaration functionDecl =
          new FunctionDeclaration(cFunctionName, typeUtil.getVoid(), originalMethodExecutable);

      VariableElement selfParam =
          GeneratedVariableElement.newParameter(
              "self", declaringClass.asType(), originalMethodExecutable);
      functionDecl.getParameters().add(new SingleVariableDeclaration(selfParam));

      for (VariableElement adapterParam : adapterParameters) {
        functionDecl.getParameters().add(new SingleVariableDeclaration(adapterParam));
      }

      List<Expression> functionArgs = functionInvocation.getArguments();
      functionArgs.add(new SimpleName(selfParam));
      for (Expression arg : adaptingArguments) {
        functionArgs.add((Expression) arg.copy());
      }

      functionBodyBlock.addStatement(new ExpressionStatement(functionInvocation));
      functionDecl.setBody(functionBodyBlock);

      typeNode.addBodyDeclaration(functionDecl);

      generateConstructorHelperFunction(
          "new_", declaringClass, adapterParameters, adaptingArguments);
      generateConstructorHelperFunction(
          "create_", declaringClass, adapterParameters, adaptingArguments);
    }

    private void generateConstructorHelperFunction(
        String prefix,
        TypeElement declaringClass,
        List<VariableElement> adapterParameters,
        List<Expression> adaptingArguments) {

      String originalFunctionName = nameTable.getFullFunctionName(originalMethodExecutable);
      String delegatedFunctionName = prefix + originalFunctionName;

      String selectorName = selector.replaceAll(":", "_");
      String functionName = prefix + nameTable.getFullName(declaringClass) + "_" + selectorName;

      FunctionDeclaration functionDecl =
          new FunctionDeclaration(functionName, declaringClass.asType(), originalMethodExecutable);

      functionDecl
          .getParameters()
          .addAll(
              adapterParameters.stream()
                  .map(SingleVariableDeclaration::new)
                  .collect(toImmutableList()));

      FunctionElement delegatedFuncElement =
          new FunctionElement(delegatedFunctionName, declaringClass.asType(), declaringClass);

      FunctionInvocation invocation =
          new FunctionInvocation(delegatedFuncElement, declaringClass.asType());
      for (Expression arg : adaptingArguments) {
        invocation.addArgument((Expression) arg.copy());
      }

      Block bodyBlock = new Block();
      bodyBlock.addStatement(new ReturnStatement(invocation));
      functionDecl.setBody(bodyBlock);

      typeNode.addBodyDeclaration(functionDecl);
    }

    private void generateConstructorMethodDeclaration(TypeMirror adapterReturnType) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(originalMethodExecutable);

      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      FunctionInvocation functionInvocation = createConstructorInvocation(declaringClass);

      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);

      List<Expression> args = functionInvocation.getArguments();
      args.add(new ThisExpression(declaringClass.asType()));

      args.addAll(adaptingArguments);

      Block adapterBodyBlock = new Block();
      adapterBodyBlock.addStatement(new ExpressionStatement(functionInvocation));
      adapterBodyBlock.addStatement(
          new ReturnStatement(new ThisExpression(declaringClass.asType())));

      MethodDeclaration adapterMethodDeclaration =
          createAdapterMethodDeclaration(
              adapterMethodExecutable, adapterBodyBlock, adapterParameters, isInterface);

      typeNode.addBodyDeclaration(adapterMethodDeclaration);
    }

    private void addMethodDeclaration(
        GeneratedExecutableElement adapterMethodExecutable,
        Expression adaptingMethodInvocation,
        TypeMirror adapterReturnType,
        List<VariableElement> adapterParameters) {
      Block adapterBodyBlock = new Block();
      // Create the return statement for the adapter method body. This might involve
      // converting the result from the original method call back to a native type.
      adapterBodyBlock.addStatement(
          createReturnStatement(adaptingMethodInvocation, adapterReturnType));

      MethodDeclaration adapterMethodDeclaration =
          createAdapterMethodDeclaration(
              adapterMethodExecutable, adapterBodyBlock, adapterParameters, isInterface);
      // Add the new adapter method to the class declaration.
      typeNode.addBodyDeclaration(adapterMethodDeclaration);
    }

    /**
     * Processes the parameters of the original method.
     *
     * <p>If a parameter type is a supported collection (List, Map, Set), it prepares a call to a
     * converter method defined in the adapter class. Otherwise, it passes the parameter as is.
     */
    private ParameterMapping processParameters() {
      // Process the parameters of the original method, preparing arguments for the call
      // to the original method and populating the parameters for the adapter method.
      List<Expression> adaptingArguments = new ArrayList<>();
      List<VariableElement> adapterParameters = new ArrayList<>();
      for (VariableElement parameter : originalMethodParameters) {
        TypeMirror parameterType = parameter.asType();
        if (!isTypeSupported(parameterType)) {
          adaptingArguments.add(new SimpleName(parameter));
          adapterParameters.add(parameter);
          continue;
        }

        NativeType nativeType = calculateNativeType(parameterType, originalMethodExecutable);
        adapterParameters.add(
            GeneratedVariableElement.newParameter(
                parameter.toString(), nativeType, parameter.getEnclosingElement()));

        Expression converterMethodInvocation =
            createConverterMethodInvocation(
                ConversionDirection.TO,
                parameterType,
                /* castType= */ parameterType, // provide the original type as the cast type
                new SimpleName(parameter));
        adaptingArguments.add(converterMethodInvocation);
      }
      return new ParameterMapping(adaptingArguments, adapterParameters);
    }

    private Statement createReturnStatement(
        Expression adaptingMethodInvocation, TypeMirror adapterReturnType) {
      if (TypeUtil.isVoid(originalMethodReturnType)) {
        return new ExpressionStatement(adaptingMethodInvocation);
      }
      Expression converterMethodInvocation =
          createConverterMethodInvocation(
              ConversionDirection.FROM,
              originalMethodReturnType,
              adapterReturnType,
              adaptingMethodInvocation);
      return new ReturnStatement(maybeCast(adapterReturnType, converterMethodInvocation));
    }

    private Expression createConverterMethodInvocation(
        ConversionDirection prefix,
        TypeMirror originalType,
        TypeMirror castType,
        Expression argument) {
      TypeElement adapterElement = TypeUtil.asTypeElement(adapter);
      if (adapterElement == null) {
        throw new IllegalArgumentException(
            String.format("No cannot find adapter %s", adapter.toString()));
      }
      AdapterLookup.ConverterMatch match;
      if (prefix == ConversionDirection.TO) {
        match = adapterLookup.findConverterByParamType(adapterElement, originalType);
      } else {
        match = adapterLookup.findConverterByReturnType(adapterElement, originalType);
      }

      if (match.matchType() == AdapterLookup.MatchType.NONE_EXACT_REQUIRED) {
        throw new IllegalStateException(
            String.format(
                "Exact converter required for mapped type %s in adapter %s for %s",
                originalType,
                adapterElement.getQualifiedName(),
                originalMethodExecutable.getSimpleName()));
      }

      ExecutableElement foundMethod = match.converter();

      if (foundMethod == null) {
        if (!isCollectionType(originalType)) {
          // Fallback to the original argument if it's not a collection type.
          return argument;
        }
        throw new IllegalArgumentException(
            String.format("No converter method found in %s for type %s.", adapter, originalType));
      }

      Expression converterInvocation =
          createAdapterInvocation(foundMethod, adapterElement, argument);

      return maybeCast(castType, converterInvocation);
    }

    // Adapter invocation are always with wrapped methods, which means that the package it is in
    // must enable wrapper methods.
    private Expression createAdapterInvocation(
        ExecutableElement foundMethod, TypeElement adapterElement, Expression argument) {
      MethodInvocation methodInvocation =
          new MethodInvocation(new ExecutablePair(foundMethod), new SimpleName(adapterElement));
      TypeMirror paramType = foundMethod.getParameters().get(0).asType();
      methodInvocation.addArgument(maybeCast(paramType, argument));
      return methodInvocation;
    }

    private Expression maybeCast(TypeMirror targetType, Expression expression) {
      // If the target type is Object (id), we don't need to cast the expression.
      if (TypeUtil.isDeclaredType(targetType)
          && "java.lang.Object".equals(TypeUtil.getQualifiedName(targetType))) {
        return expression;
      }
      if (!typeUtil.isSameType(expression.getTypeMirror(), targetType)
          && !typeUtil.isSubtype(expression.getTypeMirror(), targetType)) {
        return new CastExpression(targetType, expression);
      }
      return expression;
    }

    private MethodDeclaration createAdapterMethodDeclaration(
        GeneratedExecutableElement adapterMethodExecutable,
        Block adapterBodyBlock,
        List<VariableElement> adapterParameters,
        boolean isInterface) {
      MethodDeclaration adapterMethodDeclaration =
          new MethodDeclaration(adapterMethodExecutable)
              .setExecutableElement(adapterMethodExecutable);
      adapterMethodDeclaration.getParameters().clear();
      if (!isInterface) {
        adapterMethodDeclaration.setBody(adapterBodyBlock);
      } else {
        adapterMethodDeclaration.removeModifiers(STATIC);
        adapterMethodDeclaration.addModifiers(ABSTRACT);
      }
      for (VariableElement adapterParam : adapterParameters) {
        adapterMethodDeclaration.addParameter(new SingleVariableDeclaration(adapterParam));
      }
      return adapterMethodDeclaration;
    }
  }

  /**
   * Calculates the native Objective-C type for a given Java type.
   *
   * <p>For example, for List&lt;String&gt;, it returns "NSArray&lt;NSString *&gt; *".
   */
  private NativeType calculateNativeType(
      TypeMirror parameterType, ExecutableElement methodExecutable) {
    StringBuilder builder = new StringBuilder();
    List<TypeMirror> referencedTypes = new ArrayList<>();
    parameterType.accept(new NativeTypeVisitor(methodExecutable, referencedTypes), builder);
    return new NativeType(builder.toString(), null, null, referencedTypes);
  }

  private String toNativeType(
      TypeMirror typeMirror, ExecutableElement methodExecutable, List<TypeMirror> referencedTypes) {
    String fullyQualifiedNameJavaName = TypeUtil.getQualifiedName(typeMirror);
    String nativeType = JAVA_TO_NATIVE_TYPE_MAP.get(fullyQualifiedNameJavaName);
    if (nativeType != null) {
      return nativeType;
    }
    TypeElement typeElement = TypeUtil.asTypeElement(typeMirror);
    if (typeElement != null) {
      referencedTypes.add(typeMirror);
      return nameTable.getFullName(typeElement);
    }
    throw new IllegalArgumentException(
        "Unsupported type: " + fullyQualifiedNameJavaName + " in method: " + methodExecutable);
  }

  private boolean isTypeSupported(TypeMirror typeMirror) {
    return supportedConversionTypes.contains(
        TypeUtil.getQualifiedName(typeUtil.erasure(typeMirror)));
  }

  /**
   * Recursively visits parameterized types to build a string representation.
   *
   * <p>This is used for both generating converter method names and native Objective-C types by
   * configuring prefixes, separators, and suffixes.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code List<String>} becomes {@code NSArray<NSString *> *}
   *   <li>{@code Map<Integer, String>} becomes {@code NSDictionary<NSNumber *, NSString *> *}
   *   <li>{@code Set<List<String>>} becomes {@code NSSet<NSArray<NSString *> *> *}
   * </ul>
   */
  private class NativeTypeVisitor extends SimpleTypeVisitor9<Void, StringBuilder> {

    private final ExecutableElement methodExecutable;
    private final List<TypeMirror> referencedTypes;

    private NativeTypeVisitor(
        ExecutableElement methodExecutable, List<TypeMirror> referencedTypes) {
      this.methodExecutable = methodExecutable;
      this.referencedTypes = referencedTypes;
    }

    @Override
    public Void visitDeclared(DeclaredType type, StringBuilder builder) {
      builder.append(toNativeType(type, methodExecutable, referencedTypes));
      List<? extends TypeMirror> typeArguments = type.getTypeArguments();
      if (options.asObjCGenericDecl() && !typeArguments.isEmpty()) {
        String typeArgsString = buildTypeArgumentString(typeArguments);
        builder.append("<").append(typeArgsString).append(">");
      }
      builder.append(" *");
      return null;
    }

    @Override
    public Void visitPrimitive(PrimitiveType type, StringBuilder builder) {
      switch (type.getKind()) {
        case BOOLEAN -> builder.append("BOOL");
        case INT -> builder.append("int32_t");
        case LONG -> builder.append("int64_t");
        case FLOAT -> builder.append("float");
        case DOUBLE -> builder.append("double");
        case CHAR -> builder.append("unichar");
        case BYTE -> builder.append("char");
        case SHORT -> builder.append("int16_t");
        default -> builder.append(type.toString());
      }
      return null;
    }

    private String buildTypeArgumentString(List<? extends TypeMirror> typeArguments) {
      List<String> typeArgumentStrings = new ArrayList<>();
      for (TypeMirror typeArgument : typeArguments) {
        if (TypeUtil.isDeclaredType(typeArgument)) {
          StringBuilder sb = new StringBuilder();
          typeArgument.accept(this, sb);
          typeArgumentStrings.add(sb.toString());
        } else {
          typeArgumentStrings.add("id");
        }
      }
      return Joiner.on(", ").join(typeArgumentStrings);
    }
  }

  private enum ConversionDirection {
    FROM,
    TO;
  }
}
