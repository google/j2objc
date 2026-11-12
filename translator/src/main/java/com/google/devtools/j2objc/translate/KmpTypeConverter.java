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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor9;

/**
 * Handles Java-to-Objective-C type mapping, nullability/generic formatting, and adapter converter
 * invocation synthesis for {@link com.google.j2objc.annotations.ObjectiveCKmpMethod}.
 */
final class KmpTypeConverter {

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

  private static final ImmutableSet<String> TYPES_THAT_OMIT_NULLABILITY_MARKER =
      ImmutableSet.of("java.lang.Void");

  private final TypeUtil typeUtil;
  private final NameTable nameTable;
  private final Options options;
  private final ImmutableSet<String> supportedConversionTypes = JAVA_TO_NATIVE_TYPE_MAP.keySet();
  private final AdapterLookup adapterLookup;

  KmpTypeConverter(TypeUtil typeUtil, NameTable nameTable, Options options) {
    this.typeUtil = typeUtil;
    this.nameTable = nameTable;
    this.options = options;
    this.adapterLookup = new AdapterLookup(typeUtil);
  }

  /** Returns true if {@code type} is a Java or Guava collection supported for KMP conversion. */
  private boolean isCollectionType(TypeMirror type) {
    return COLLECTION_TYPES.contains(TypeUtil.getQualifiedName(typeUtil.erasure(type)));
  }

  /** Adapted parameter declarations and their converted argument expressions. */
  record ParameterMapping(
      List<Expression> adaptingArguments, List<VariableElement> adapterParameters) {}

  /**
   * Processes the parameters of a method, preparing arguments for the delegated call and populating
   * the parameters for the generated adapter method.
   */
  ParameterMapping processParameters(
      TypeMirror adapter,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      List<VariableElement> methodParameters) {
    List<Expression> adaptingArguments = new ArrayList<>();
    List<VariableElement> adapterParameters = new ArrayList<>();
    for (VariableElement parameter : methodParameters) {
      TypeMirror parameterType = parameter.asType();
      // If the type is not supported (a.k.a mapped to a native type), but is an interface or
      // nullable, we still go through the computation of the type as we will be adding more
      // information than just the type name (id brackets and nullability marker).
      if (!isTypeSupported(parameterType)
          && !isNullable(parameterType)
          && !TypeUtil.isInterface(parameterType)
          && !hasConverter(adapter, strategy, parameterType, /* isParam= */ true)) {
        adaptingArguments.add(new SimpleName(parameter));
        adapterParameters.add(parameter);
        continue;
      }
      NativeType nativeType = calculateNativeType(parameterType, methodExecutable);
      adapterParameters.add(
          GeneratedVariableElement.newParameter(
              parameter.toString(), nativeType, parameter.getEnclosingElement()));

      Expression converterMethodInvocation =
          createConverterMethodInvocation(
              adapter,
              strategy,
              methodExecutable,
              /* isParam= */ true,
              parameterType,
              strategy.getParamCastType(parameterType, nativeType),
              new SimpleName(parameter));
      adaptingArguments.add(converterMethodInvocation);
    }
    return new ParameterMapping(adaptingArguments, adapterParameters);
  }

  /** Returns true if {@code adapter} defines a converter (or requires an exact match) for type. */
  private boolean hasConverter(
      TypeMirror adapter, MappingStrategy strategy, TypeMirror type, boolean isParam) {
    TypeElement adapterElement = TypeUtil.asTypeElement(adapter);
    if (adapterElement == null) {
      return false;
    }
    AdapterLookup.ConverterMatch match =
        strategy.findConverter(adapterLookup, adapterElement, type, isParam);
    return match.converter() != null
        || match.matchType() == AdapterLookup.MatchType.NONE_EXACT_REQUIRED;
  }

  /** Wraps an invocation expression with return-value adaptation or an expression statement. */
  Statement createReturnStatement(
      TypeMirror adapter,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      TypeMirror originalMethodReturnType,
      Expression adaptingMethodInvocation,
      TypeMirror adapterReturnType) {
    if (TypeUtil.isVoid(originalMethodReturnType)) {
      return new ExpressionStatement(adaptingMethodInvocation);
    }
    Expression converterMethodInvocation =
        createConverterMethodInvocation(
            adapter,
            strategy,
            methodExecutable,
            /* isParam= */ false,
            originalMethodReturnType,
            strategy.getReturnCastType(originalMethodReturnType, adapterReturnType),
            adaptingMethodInvocation);
    return new ReturnStatement(maybeCast(adapterReturnType, converterMethodInvocation));
  }

  /** Resolves and synthesizes a converter invocation expression (plus cast if needed). */
  private Expression createConverterMethodInvocation(
      TypeMirror adapter,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      boolean isParam,
      TypeMirror originalType,
      TypeMirror castType,
      Expression argument) {
    TypeElement adapterElement = TypeUtil.asTypeElement(adapter);
    if (adapterElement == null) {
      throw new IllegalArgumentException(
          String.format("No cannot find adapter %s", adapter.toString()));
    }
    return convertExpression(
        adapter,
        adapterElement,
        strategy,
        methodExecutable,
        isParam,
        originalType,
        castType,
        argument);
  }

  /** Looks up the converter match for a type and validates exact-match requirements. */
  private AdapterLookup.ConverterMatch resolveTypeConversion(
      TypeElement adapterElement,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      TypeMirror originalType,
      boolean isParam) {
    AdapterLookup.ConverterMatch match =
        strategy.findConverter(adapterLookup, adapterElement, originalType, isParam);
    if (match.matchType() == AdapterLookup.MatchType.NONE_EXACT_REQUIRED) {
      throw new IllegalStateException(
          String.format(
              "Exact converter required for mapped type %s in adapter %s for %s",
              originalType, adapterElement.getQualifiedName(), methodExecutable.getSimpleName()));
    }
    return match;
  }

  private Expression convertExpression(
      TypeMirror adapter,
      TypeElement adapterElement,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      boolean isParam,
      TypeMirror originalType,
      TypeMirror castType,
      Expression argument) {
    AdapterLookup.ConverterMatch match =
        resolveTypeConversion(adapterElement, strategy, methodExecutable, originalType, isParam);
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
        createAdapterInvocation(
            foundMethod,
            adapter,
            adapterElement,
            strategy,
            methodExecutable,
            argument,
            isParam,
            originalType,
            match.matchType().isComposing());

    return maybeCast(castType, converterInvocation);
  }

  // Adapter invocation are always with wrapped methods, which means that the package it is in
  // must enable wrapper methods.
  private Expression createAdapterInvocation(
      ExecutableElement foundMethod,
      TypeMirror adapter,
      TypeElement adapterElement,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      Expression argument,
      boolean isParam,
      TypeMirror originalType,
      boolean isComposing) {
    MethodInvocation methodInvocation =
        new MethodInvocation(new ExecutablePair(foundMethod), new SimpleName(adapterElement));
    TypeMirror paramType = foundMethod.getParameters().get(0).asType();
    methodInvocation.addArgument(maybeCast(paramType, argument));
    if (isComposing && originalType instanceof DeclaredType declaredType) {
      populateComposingArguments(
          methodInvocation,
          foundMethod,
          adapter,
          adapterElement,
          strategy,
          methodExecutable,
          declaredType,
          isParam);
    }
    return methodInvocation;
  }

  private void populateComposingArguments(
      MethodInvocation methodInvocation,
      ExecutableElement foundMethod,
      TypeMirror adapter,
      TypeElement adapterElement,
      MappingStrategy strategy,
      ExecutableElement methodExecutable,
      DeclaredType declaredType,
      boolean isParam) {
    for (int i = 1; i < foundMethod.getParameters().size(); i++) {
      if (i - 1 >= declaredType.getTypeArguments().size()) {
        break;
      }
      var elemType = declaredType.getTypeArguments().get(i - 1);
      var convParam = foundMethod.getParameters().get(i);
      var convType = convParam.asType();

      // Synthesize an anonymous adaptation lambda for each composing parameter:
      //   elem -> Adapter.toJavaUtilList(elem, nestedLambda)
      // or pass-through:
      //   elem -> elem
      var functionInterface = TypeUtil.asTypeElement(convType);
      ExecutableElement samMethod = null;
      TypeMirror samParamType = elemType;
      if (functionInterface != null) {
        samMethod = findSamMethod(functionInterface);
        if (samMethod != null && !samMethod.getParameters().isEmpty()) {
          samParamType = samMethod.getParameters().get(0).asType();
        }
      }
      var paramElem = GeneratedVariableElement.newParameter("elem", samParamType, null);
      var paramDecl = new SingleVariableDeclaration().setVariableElement(paramElem);

      var lambda = new LambdaExpression().setTypeMirror(convType);
      lambda.addTargetType(convType);
      lambda.getParameters().add(paramDecl);

      if (samMethod != null) {
        lambda.setDescriptor(new ExecutablePair(samMethod));
      }

      var nativeType = calculateNativeType(elemType, methodExecutable, /* isTypeArgument= */ true);
      var elemCastType = strategy.getCastType(elemType, nativeType, isParam);
      Expression bodyExpr =
          convertExpression(
              adapter,
              adapterElement,
              strategy,
              methodExecutable,
              isParam,
              elemType,
              elemCastType,
              new SimpleName(paramElem));
      lambda.setBody(bodyExpr);
      methodInvocation.addArgument(lambda);
    }
  }

  private static ExecutableElement findSamMethod(TypeElement interfaceElement) {
    for (ExecutableElement method : ElementUtil.getMethods(interfaceElement)) {
      if (method.getModifiers().contains(Modifier.ABSTRACT)) {
        return method;
      }
    }
    throw new AssertionError("Cannot find abstract SAM method in " + interfaceElement);
  }

  /** Inserts a cast to {@code targetType} unless {@code expression} is already assignable. */
  private Expression maybeCast(TypeMirror targetType, Expression expression) {
    // If the target type is Object (id), we don't need to cast the expression.
    if (TypeUtil.isDeclaredType(targetType)
        && TypeUtil.getQualifiedName(targetType).equals("java.lang.Object")) {
      return expression;
    }
    if (!typeUtil.isSameType(expression.getTypeMirror(), targetType)
        && !typeUtil.isSubtype(expression.getTypeMirror(), targetType)) {
      return new CastExpression(targetType, expression);
    }
    return expression;
  }

  /**
   * Calculates the native Objective-C type for a given Java type.
   *
   * <p>For example, for List&lt;String&gt;, it returns "NSArray&lt;NSString *&gt; *".
   */
  NativeType calculateNativeType(TypeMirror parameterType, ExecutableElement methodExecutable) {
    return calculateNativeType(parameterType, methodExecutable, /* isTypeArgument= */ false);
  }

  /**
   * Calculates the native Objective-C type, optionally omitting outer nullability for type args.
   */
  private NativeType calculateNativeType(
      TypeMirror parameterType, ExecutableElement methodExecutable, boolean isTypeArgument) {
    StringBuilder builder = new StringBuilder();
    List<TypeMirror> referencedTypes = new ArrayList<>();
    parameterType.accept(
        new NativeTypeVisitor(methodExecutable, referencedTypes, isTypeArgument), builder);
    return new NativeType(builder.toString(), null, null, referencedTypes);
  }

  private PrintableNativeType toNativeType(
      TypeMirror typeMirror,
      ExecutableElement methodExecutable,
      List<TypeMirror> referencedTypes,
      boolean isTypeArgument) {
    String fullyQualifiedNameJavaName = TypeUtil.getQualifiedName(typeMirror);
    boolean isNullable = isNullable(typeMirror);
    String nativeType = JAVA_TO_NATIVE_TYPE_MAP.get(fullyQualifiedNameJavaName);
    if (nativeType != null) {
      return new PrintableNativeType(nativeType, false, !isTypeArgument && isNullable);
    }
    TypeElement typeElement = TypeUtil.asTypeElement(typeMirror);
    if (typeElement != null) {
      referencedTypes.add(typeMirror);
      return new PrintableNativeType(
          nameTable.getFullName(typeElement), TypeUtil.isInterface(typeMirror), isNullable);
    }
    throw new IllegalArgumentException(
        "Unsupported type: " + fullyQualifiedNameJavaName + " in method: " + methodExecutable);
  }

  /** Returns true if {@code typeMirror} maps directly to a native Foundation type. */
  private boolean isTypeSupported(TypeMirror typeMirror) {
    return supportedConversionTypes.contains(
        TypeUtil.getQualifiedName(typeUtil.erasure(typeMirror)));
  }

  /** Returns true if {@code typeMirror} carries a nullability annotation and is not Void. */
  private boolean isNullable(TypeMirror typeMirror) {
    return !TYPES_THAT_OMIT_NULLABILITY_MARKER.contains(TypeUtil.getQualifiedName(typeMirror))
        && typeMirror.getAnnotationMirrors().stream()
            .anyMatch(annotation ->
                annotation
                    .getAnnotationType()
                    .asElement()
                    .getSimpleName()
                    .toString()
                    .contains("Nullable"));
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
   *   <li>{@code JavaInterface} becomes {@code id<JavaInterface>}
   *   <li>{@code List<JavaInterface>} becomes {@code NSArray<id<JavaInterface>> *}
   * </ul>
   */
  private class NativeTypeVisitor extends SimpleTypeVisitor9<Void, StringBuilder> {

    private final ExecutableElement methodExecutable;
    private final List<TypeMirror> referencedTypes;
    private final boolean isTypeArgument;

    private NativeTypeVisitor(
        ExecutableElement methodExecutable,
        List<TypeMirror> referencedTypes,
        boolean isTypeArgument) {
      this.methodExecutable = methodExecutable;
      this.referencedTypes = referencedTypes;
      this.isTypeArgument = isTypeArgument;
    }

    @Override
    public Void visitDeclared(DeclaredType type, StringBuilder builder) {
      PrintableNativeType nativeType =
          toNativeType(type, methodExecutable, referencedTypes, isTypeArgument);
      if (nativeType.isUntranslatedInterface) {
        builder.append("id<");
      }
      builder.append(nativeType.nativeTypeName);
      List<? extends TypeMirror> typeArguments = type.getTypeArguments();
      if (options.asObjCGenericDecl()
          && !typeArguments.isEmpty()
          && !nativeType.isUntranslatedInterface) {
        String typeArgsString = buildTypeArgumentString(typeArguments);
        builder.append("<").append(typeArgsString).append(">");
      }
      if (nativeType.isUntranslatedInterface) {
        builder.append(">");
      } else {
        builder.append(" *");
      }
      if (nativeType.nullable) {
        builder.append(" _Nullable");
      }
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
      NativeTypeVisitor argVisitor =
          new NativeTypeVisitor(methodExecutable, referencedTypes, /* isTypeArgument= */ true);
      for (TypeMirror typeArgument : typeArguments) {
        if (TypeUtil.isDeclaredType(typeArgument)) {
          StringBuilder sb = new StringBuilder();
          typeArgument.accept(argVisitor, sb);
          typeArgumentStrings.add(sb.toString());
        } else {
          typeArgumentStrings.add("id");
        }
      }
      return Joiner.on(", ").join(typeArgumentStrings);
    }
  }

  /** Direction in which a generated wrapper delegates calls across the language boundary. */
  enum MappingStrategy {
    /** Delegates from a native Objective-C entry point to a Java implementation. */
    NATIVE_TO_JAVA {
      @Override
      AdapterLookup.ConverterMatch findReturnConverter(
          AdapterLookup lookup, TypeElement adapterClass, TypeMirror type) {
        return lookup.findConverterByReturnType(adapterClass, type);
      }

      @Override
      AdapterLookup.ConverterMatch findParamConverter(
          AdapterLookup lookup, TypeElement adapterClass, TypeMirror type) {
        return lookup.findConverterByParamType(adapterClass, type);
      }

      @Override
      TypeMirror getParamCastType(TypeMirror parameterType, TypeMirror nativeType) {
        return parameterType;
      }

      @Override
      TypeMirror getReturnCastType(
          TypeMirror originalMethodReturnType, TypeMirror adapterReturnType) {
        return adapterReturnType;
      }
    },
    /** Delegates from a Java entry point or dispatch helper to a native Objective-C selector. */
    JAVA_TO_NATIVE {
      @Override
      AdapterLookup.ConverterMatch findReturnConverter(
          AdapterLookup lookup, TypeElement adapterClass, TypeMirror type) {
        return lookup.findConverterByParamType(adapterClass, type);
      }

      @Override
      AdapterLookup.ConverterMatch findParamConverter(
          AdapterLookup lookup, TypeElement adapterClass, TypeMirror type) {
        return lookup.findConverterByReturnType(adapterClass, type);
      }

      @Override
      TypeMirror getParamCastType(TypeMirror parameterType, TypeMirror nativeType) {
        return nativeType;
      }

      @Override
      TypeMirror getReturnCastType(
          TypeMirror originalMethodReturnType, TypeMirror adapterReturnType) {
        return originalMethodReturnType;
      }
    };

    abstract AdapterLookup.ConverterMatch findReturnConverter(
        AdapterLookup lookup, TypeElement adapterClass, TypeMirror type);

    abstract AdapterLookup.ConverterMatch findParamConverter(
        AdapterLookup lookup, TypeElement adapterClass, TypeMirror type);

    abstract TypeMirror getParamCastType(TypeMirror parameterType, TypeMirror nativeType);

    abstract TypeMirror getReturnCastType(
        TypeMirror originalMethodReturnType, TypeMirror adapterReturnType);

    private AdapterLookup.ConverterMatch findConverter(
        AdapterLookup lookup, TypeElement adapterClass, TypeMirror type, boolean isParam) {
      return isParam
          ? findParamConverter(lookup, adapterClass, type)
          : findReturnConverter(lookup, adapterClass, type);
    }

    private TypeMirror getCastType(TypeMirror type, TypeMirror nativeType, boolean isParam) {
      return isParam ? getParamCastType(type, nativeType) : getReturnCastType(type, nativeType);
    }
  }

  private record PrintableNativeType(
      String nativeTypeName, boolean isUntranslatedInterface, boolean nullable) {}
}
