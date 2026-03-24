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
import static com.google.common.collect.Streams.stream;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor9;
import org.jspecify.annotations.Nullable;

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
 * <h3>Type Matching (Candidates)</h3>
 *
 * <p>When matching types, the translator generates candidate types by progressively replacing type
 * arguments with wildcards ({@code ?}) from deepest to shallowest. It searches for a matching
 * adapter method for each candidate, prioritizing the most specific type first.
 *
 * <p>For example, for the type {@code List<String>}, the candidates are:
 *
 * <ol>
 *   <li>{@code List<String>} (depth 1)
 *   <li>{@code List<?>} (depth 0)
 * </ol>
 *
 * <p>The translator will first try to find an adapter method that matches {@code List<String>}. If
 * not found, it will fallback to a method that matches {@code List<?>}.
 */
public final class ObjectiveCKmpMethodTranslator extends UnitTreeVisitor {

  private static final ImmutableMap<String, String> JAVA_TO_NATIVE_TYPE_MAP =
      ImmutableMap.<String, String>builder()
          .put("java.util.List", "NSArray")
          .put("java.util.Map", "NSDictionary")
          .put("java.util.Set", "NSSet")
          .put("java.lang.String", "NSString")
          .put("java.lang.Integer", "NSNumber")
          .put("java.lang.Long", "NSNumber")
          .put("java.lang.Double", "NSNumber")
          .put("java.lang.Float", "NSNumber")
          .put("java.lang.Boolean", "NSNumber")
          .buildOrThrow();

  private final ImmutableSet<String> supportedConversionTypes = JAVA_TO_NATIVE_TYPE_MAP.keySet();

  public ObjectiveCKmpMethodTranslator(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(MethodDeclaration methodDeclaration) {
    // Skip methods that are not annotated with @ObjectiveCKmpMethod.
    ExecutableElement methodExecutable = methodDeclaration.getExecutableElement();
    if (!ElementUtil.hasAnnotation(methodExecutable, ObjectiveCKmpMethod.class)) {
      super.endVisit(methodDeclaration);
      return;
    }
    new AdapterContext(methodDeclaration).processMethod();
    super.endVisit(methodDeclaration);
  }

  @Override
  public void endVisit(TypeDeclaration typeDeclaration) {
    if (typeDeclaration.isInterface()) {
      return; // Skip interfaces.
    }
    super.endVisit(typeDeclaration);
  }

  /** Context for processing a single method and generating its Objective-C adapter. */
  private class AdapterContext {
    private final String selector;
    private final MethodDeclaration originalMethodDeclaration;
    private final ExecutableElement originalMethodExecutable;
    private final TypeMirror originalMethodReturnType;
    private final ImmutableList<? extends VariableElement> originalMethodParameters;
    private final TypeMirror adapter;
    private final ImmutableList<ExecutableElement> adapterMethods;

    private final List<Expression> adaptingArguments = new ArrayList<>();
    private final List<VariableElement> adapterParameters = new ArrayList<>();
    private final Block adapterBodyBlock = new Block();

    private AdapterContext(MethodDeclaration originalMethodDeclaration) {
      this.originalMethodDeclaration = originalMethodDeclaration;
      this.originalMethodExecutable = originalMethodDeclaration.getExecutableElement();
      this.originalMethodReturnType = originalMethodExecutable.getReturnType();
      AnnotationMirror annotation =
          ElementUtil.getAnnotation(
              originalMethodDeclaration.getExecutableElement(), ObjectiveCKmpMethod.class);
      this.originalMethodParameters =
          ImmutableList.copyOf(originalMethodExecutable.getParameters());
      this.selector = (String) ElementUtil.getAnnotationValue(annotation, "selector");
      this.adapter = (TypeMirror) ElementUtil.getAnnotationValue(annotation, "adapter");
      this.adapterMethods = getAdapterMethods(this.adapter);
    }

    private ImmutableList<ExecutableElement> getAdapterMethods(TypeMirror adapterType) {
      TypeElement adapterElement = TypeUtil.asTypeElement(adapterType);
      if (adapterElement == null) {
        return ImmutableList.of();
      }
      return stream(ElementUtil.getMethods(adapterElement))
          .filter(m -> m.getParameters().size() == 1)
          .collect(toImmutableList());
    }

    /** Orchestrates the parameter conversion and adapter method creation. */
    private void processMethod() {
      // Process the parameters of the original method, preparing arguments for the call
      // to the original method and populating the parameters for the adapter method.
      processParameters();

      // Calculate the return type of the adapter method. If the original method returns void,
      // the adapter method also returns void. Otherwise, calculate the native Objective-C type.
      TypeMirror adapterReturnType =
          TypeUtil.isVoid(originalMethodReturnType)
              ? originalMethodReturnType
              : calculateNativeType(originalMethodReturnType, originalMethodDeclaration);

      // Create the ExecutableElement for the new adapter method.
      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);

      // Get the declaration of the class containing the original method.
      AbstractTypeDeclaration typeDeclaration =
          (AbstractTypeDeclaration) originalMethodDeclaration.getParent();

      // Create a MethodInvocation to call the original Java method.
      MethodInvocation adaptingMethodInvocation =
          new MethodInvocation(new ExecutablePair(originalMethodExecutable), null);
      // Add the (potentially converted) arguments to the MethodInvocation.
      adaptingArguments.forEach(adaptingMethodInvocation::addArgument);

      // Create the return statement for the adapter method body. This might involve
      // converting the result from the original method call back to a native type.
      adapterBodyBlock.addStatement(
          createReturnStatement(adaptingMethodInvocation, adapterReturnType));

      MethodDeclaration adapterMethodDeclaration =
          createAdapterMethodDeclaration(
              adapterMethodExecutable, adapterBodyBlock, adapterParameters);
      // Add the new adapter method to the class declaration.
      typeDeclaration.addBodyDeclaration(adapterMethodDeclaration);
    }

    /**
     * Processes the parameters of the original method.
     *
     * <p>If a parameter type is a supported collection (List, Map, Set), it prepares a call to a
     * converter method defined in the adapter class. Otherwise, it passes the parameter as is.
     */
    private void processParameters() {
      for (VariableElement parameter : originalMethodParameters) {
        TypeMirror parameterType = parameter.asType();
        if (!isTypeSupported(parameterType)) {
          adaptingArguments.add(new SimpleName(parameter));
          adapterParameters.add(parameter);
          continue;
        }

        NativeType nativeType = calculateNativeType(parameterType, originalMethodDeclaration);
        adapterParameters.add(
            GeneratedVariableElement.newParameter(
                parameter.toString(), nativeType, parameter.getEnclosingElement()));

        Expression converterMethodInvocation =
            createConverterMethodInvocation(
                ConversionDirection.TO, parameterType, parameterType, new SimpleName(parameter));
        adaptingArguments.add(converterMethodInvocation);
      }
    }

    private Statement createReturnStatement(
        MethodInvocation adaptingMethodInvocation, TypeMirror adapterReturnType) {
      if (TypeUtil.isVoid(originalMethodReturnType)) {
        return new ExpressionStatement(adaptingMethodInvocation);
      }
      Expression converterMethodInvocation =
          createConverterMethodInvocation(
              ConversionDirection.FROM,
              originalMethodReturnType,
              adapterReturnType,
              adaptingMethodInvocation);
      if (!typeUtil.isSameType(converterMethodInvocation.getTypeMirror(), adapterReturnType)
          && !typeUtil.isSubtype(converterMethodInvocation.getTypeMirror(), adapterReturnType)) {
        return new ReturnStatement(
            new CastExpression(adapterReturnType, converterMethodInvocation));
      }
      return new ReturnStatement(converterMethodInvocation);
    }

    private Expression createConverterMethodInvocation(
        ConversionDirection prefix,
        TypeMirror originalType,
        TypeMirror returnType,
        Expression argument) {
      List<TypeMirror> candidates = calculateConverterTypeCandidates(originalType);
      ExecutableElement foundMethod =
          candidates.stream()
              .map(candidate -> findAdapterMethod(prefix, candidate))
              .filter(Objects::nonNull)
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          String.format(
                              "No converter method found in %s for type %s. Candidates searched:"
                                  + " %s",
                              adapter, originalType, candidates)));

      TypeElement adapterElement = TypeUtil.asTypeElement(adapter);
      MethodInvocation converterMethodInvocation =
          new MethodInvocation(new ExecutablePair(foundMethod), new SimpleName(adapterElement));
      converterMethodInvocation.addArgument(argument);

      if (!typeUtil.isSameType(foundMethod.getReturnType(), returnType)
          && !typeUtil.isSubtype(foundMethod.getReturnType(), returnType)) {
        return new CastExpression(returnType, converterMethodInvocation);
      }

      return converterMethodInvocation;
    }

    private @Nullable ExecutableElement findAdapterMethod(
        ConversionDirection prefix, TypeMirror candidate) {
      return adapterMethods.stream()
          .filter(
              method ->
                  prefix.matches(method.getSimpleName().toString())
                      && prefix.matchesType(method, candidate, typeUtil))
          .findFirst()
          .orElse(null);
    }

    private MethodDeclaration createAdapterMethodDeclaration(
        GeneratedExecutableElement adapterMethodExecutable,
        Block adapterBodyBlock,
        List<VariableElement> adapterParameters) {
      MethodDeclaration adapterMethodDeclaration =
          new MethodDeclaration(adapterMethodExecutable)
              .setExecutableElement(adapterMethodExecutable);
      adapterMethodDeclaration.getParameters().clear();
      adapterMethodDeclaration.setBody(adapterBodyBlock);
      for (VariableElement adapterParam : adapterParameters) {
        adapterMethodDeclaration.addParameter(new SingleVariableDeclaration(adapterParam));
      }
      return adapterMethodDeclaration;
    }
  }

  private List<TypeMirror> calculateConverterTypeCandidates(TypeMirror type) {
    int maxDepth = calculateTypeDepth(type);
    List<TypeMirror> candidates = new ArrayList<>();
    // Add candidates in reverse order to prioritize most specific (deepest) first.
    for (int i = maxDepth; i >= 0; i--) {
      candidates.add(type.accept(new CandidateTypeVisitor(), i));
    }
    return candidates;
  }

  private int calculateTypeDepth(TypeMirror type) {
    if (!TypeUtil.isDeclaredType(type)) {
      return 0;
    }
    DeclaredType declaredType = (DeclaredType) type;
    int maxDepth = 0;
    for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
      maxDepth = Math.max(maxDepth, calculateTypeDepth(typeArgument) + 1);
    }
    return maxDepth;
  }

  /**
   * Calculates the native Objective-C type for a given Java type.
   *
   * <p>For example, for List&lt;String&gt;, it returns "NSArray&lt;NSString *&gt; *".
   */
  private NativeType calculateNativeType(
      TypeMirror parameterType, MethodDeclaration methodDeclaration) {
    StringBuilder builder = new StringBuilder();
    parameterType.accept(new NativeTypeVisitor(methodDeclaration), builder);
    return new NativeType(builder.toString());
  }

  private String toNativeType(TypeMirror typeMirror, MethodDeclaration methodDeclaration) {
    String fullyQualifiedNameJavaName = TypeUtil.getQualifiedName(typeMirror);
    String nativeType = JAVA_TO_NATIVE_TYPE_MAP.get(fullyQualifiedNameJavaName);
    if (nativeType != null) {
      return nativeType;
    }
    TypeElement typeElement = TypeUtil.asTypeElement(typeMirror);
    if (typeElement != null) {
      return nameTable.getFullName(typeElement);
    }
    throw new IllegalArgumentException(
        "Unsupported type: "
            + fullyQualifiedNameJavaName
            + " in method: "
            + methodDeclaration.getExecutableElement());
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

    private final MethodDeclaration methodDeclaration;

    private NativeTypeVisitor(MethodDeclaration methodDeclaration) {
      this.methodDeclaration = methodDeclaration;
    }

    @Override
    public Void visitDeclared(DeclaredType type, StringBuilder builder) {
      builder.append(toNativeType(type, methodDeclaration));
      List<? extends TypeMirror> typeArguments = type.getTypeArguments();
      if (!typeArguments.isEmpty()) {
        String typeArgsString = buildTypeArgumentString(typeArguments);
        builder.append("<").append(typeArgsString).append(">");
      }
      builder.append(" *");
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

  /**
   * Visitor to generate candidate types for converter method lookup.
   *
   * <p>This visitor takes a {@link DeclaredType} and a target depth. It generates a new type where
   * type arguments are replaced by wildcards or recursively processed based on the depth.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code List<String>}, depth 0: {@code List<?>}
   *   <li>{@code List<String>}, depth 1: {@code List<String>}
   *   <li>{@code Map<String, Integer>}, depth 0: {@code Map<?, ?>}
   *   <li>{@code Map<String, Integer>}, depth 1: {@code Map<String, Integer>}
   *   <li>{@code List<List<String>>}, depth 0: {@code List<?>}
   *   <li>{@code List<List<String>>}, depth 1: {@code List<List<?>>}
   *   <li>{@code List<List<String>>}, depth 2: {@code List<List<String>>}
   * </ul>
   */
  private class CandidateTypeVisitor extends SimpleTypeVisitor9<TypeMirror, Integer> {
    @Override
    public TypeMirror visitDeclared(DeclaredType type, Integer targetDepth) {
      TypeElement element = (TypeElement) type.asElement();
      List<? extends TypeMirror> typeArguments = type.getTypeArguments();
      if (typeArguments.isEmpty()) {
        return type;
      }

      if (targetDepth == 0) {
        TypeMirror[] wildcards = new TypeMirror[typeArguments.size()];
        Arrays.fill(wildcards, typeUtil.getWildcardType(null, null));
        return typeUtil.getDeclaredType(element, wildcards);
      }

      TypeMirror[] args = new TypeMirror[typeArguments.size()];
      for (int i = 0; i < typeArguments.size(); i++) {
        args[i] = typeArguments.get(i).accept(this, targetDepth - 1);
      }
      return typeUtil.getDeclaredType(element, args);
    }

    @Override
    protected TypeMirror defaultAction(TypeMirror e, Integer p) {
      return e;
    }
  }

  private enum ConversionDirection {
    FROM("from") {
      @Override
      TypeMirror getType(ExecutableElement method) {
        return method.getParameters().get(0).asType();
      }
    },
    TO("to") {
      @Override
      TypeMirror getType(ExecutableElement method) {
        return method.getReturnType();
      }
    };

    private final String prefix;

    ConversionDirection(String prefix) {
      this.prefix = prefix;
    }

    boolean matches(String methodName) {
      return methodName.startsWith(prefix);
    }

    boolean matchesType(ExecutableElement method, TypeMirror candidate, TypeUtil typeUtil) {
      return typeUtil.isSameType(getType(method), candidate);
    }

    abstract TypeMirror getType(ExecutableElement method);
  }
}
