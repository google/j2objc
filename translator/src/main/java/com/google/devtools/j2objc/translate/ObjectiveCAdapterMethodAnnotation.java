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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeCollectionType;
import com.google.devtools.j2objc.types.NativeEnumType;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCAdapterMethod;
import com.google.j2objc.annotations.ObjectiveCAdapterMethod.Adaptation;
import com.google.j2objc.annotations.ObjectiveCAdapterProtocol;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.nullness.Nullable;

/** Implements the ObjectiveCAdapterMethod annotation. */
public class ObjectiveCAdapterMethodAnnotation extends UnitTreeVisitor {

  public ObjectiveCAdapterMethodAnnotation(CompilationUnit unit) {
    super(unit);
  }

  private static class AdapterConfig {
    private TypeMirror adapterMethodReturnType;
    private final List<VariableElement> adapterMethodParameters;
    private final List<Expression> adaptedMethodArguments;
    private final Block adapterMethodBody = new Block();
    private final MethodInvocation adaptedMethodInvocation;
    // For void return methods this is just an expression.
    private Expression returnValueExpression;

    AdapterConfig(
        TypeMirror adapterMethodReturnType,
        List<VariableElement> adapterMethodParameters,
        List<Expression> adaptedMethodArguments,
        MethodInvocation adaptedMethodInvocation,
        Expression returnValueExpression) {
      this.adapterMethodReturnType = adapterMethodReturnType;
      this.adapterMethodParameters = adapterMethodParameters;
      this.adaptedMethodArguments = adaptedMethodArguments;
      this.adaptedMethodInvocation = adaptedMethodInvocation;
      this.returnValueExpression = returnValueExpression;
    }
  }

  private @Nullable String adapterSelectorForMethod(ExecutableElement methodExecutable) {
    AnnotationMirror annotation =
        ElementUtil.getAnnotation(methodExecutable, ObjectiveCAdapterMethod.class);
    if (annotation != null) {
      String selector = (String) ElementUtil.getAnnotationValue(annotation, "selector");
      if (selector != null) {
        NameTable.validateMethodSelector(selector);
      }
      return selector;
    }
    return null;
  }

  private @Nullable EnumSet<Adaptation> adaptationsForMethod(ExecutableElement methodExecutable) {
    AnnotationMirror annotation =
        ElementUtil.getAnnotation(methodExecutable, ObjectiveCAdapterMethod.class);
    if (annotation != null) {
      @SuppressWarnings("unchecked")
      List<? extends AnnotationValue> annotationValues =
          (List<? extends AnnotationValue>)
              ElementUtil.getAnnotationValue(annotation, "adaptations");
      if (annotationValues != null) {
        List<Adaptation> adaptations = new ArrayList<>();
        for (AnnotationValue av : annotationValues) {
          for (Adaptation adaptation : Adaptation.values()) {
            if (av.toString().equals(adaptation.toString())) {
              adaptations.add(adaptation);
            }
          }
        }
        if (adaptations.isEmpty()) {
          ErrorUtil.error("ObjectiveCAdapterMethod did not specify valid adaptations.");
          return null;
        }
        return EnumSet.copyOf(adaptations);
      }
    }
    return null;
  }

  private boolean isMethodAnnotatedForAdapter(ExecutableElement methodExecutable) {
    boolean methodIsAnnotated =
        ElementUtil.hasAnnotation(methodExecutable, ObjectiveCAdapterMethod.class);
    if (!methodIsAnnotated) {
      return false;
    }

    String selector = adapterSelectorForMethod(methodExecutable);
    if (isNullOrEmpty(selector)) {
      ErrorUtil.error("ObjectiveCAdapterMethod must specify a selector.");
      return false;
    }

    long expectedArgCount = methodExecutable.getParameters().size();
    if (methodAdaptsExceptionsAsError(methodExecutable)) {
      if (expectedArgCount == 0) {
        if (!selector.endsWith("WithError:")) {
          ErrorUtil.error(
              "ObjectiveCAdapterMethod handling exceptions requires a \"WithError:\" selector.");
          return false;
        }
      } else {
        if (!selector.endsWith(":error:")) {
          ErrorUtil.error(
              "ObjectiveCAdapterMethod handling exceptions requires selector with a final"
                  + " \"error:\" argument.");
          return false;
        }
      }
      expectedArgCount++;
    }

    long colonCount = selector.chars().filter(ch -> ch == ':').count();
    if (expectedArgCount != colonCount) {
      ErrorUtil.error("ObjectiveCAdapterMethod selector does not match the number of arguments.");
      return false;
    }

    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if ((adaptations == null) || adaptations.isEmpty()) {
      ErrorUtil.error("ObjectiveCAdapterMethod must specify at least one adaptation.");
      return false;
    }

    if (methodExecutable.isVarArgs()) {
      ErrorUtil.error("ObjectiveCAdapterMethod does not support varargs.");
      return false;
    }

    return true;
  }

  private boolean methodAdaptsExceptionsAsError(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.EXCEPTIONS_AS_ERRORS);
    }
    return false;
  }

  private void adaptExceptionsAsErrors(AdapterConfig adapterConfig) {
    Statement catchReturnStatement = null;
    boolean wasVoidReturn = false;
    if (TypeUtil.isVoid(adapterConfig.adapterMethodReturnType)) {
      adapterConfig.adapterMethodReturnType = TypeUtil.BOOL_TYPE;
      wasVoidReturn = true;
      catchReturnStatement = new NativeStatement("return NO;");
    } else if (TypeUtil.isPrimitiveOrVoid(adapterConfig.adapterMethodReturnType)) {
      catchReturnStatement = new NativeStatement("return 0;");
    } else if (TypeUtil.isDeclaredType(adapterConfig.adapterMethodReturnType)
        || TypeUtil.isInterface(adapterConfig.adapterMethodReturnType)
        || TypeUtil.isEnum(adapterConfig.adapterMethodReturnType)) {
      catchReturnStatement = new NativeStatement("return nil;");
    } else if (TypeUtil.isNativeType(adapterConfig.adapterMethodReturnType)) {
      Expression nativeDefaultExpression =
          ((NativeType) adapterConfig.adapterMethodReturnType).getDefaultValueExpression();
      if (nativeDefaultExpression != null) {
        catchReturnStatement = new ReturnStatement(nativeDefaultExpression);
      } else {
        ErrorUtil.error(
            "ObjectiveCAdapterMethod cannot handle exception return value for native return type.");
        return;
      }
    } else {
      ErrorUtil.error(
          "ObjectiveCAdapterMethod cannot handle exceptions for annotated method return type.");
      return;
    }

    // Preceed any conversion body.
    adapterConfig.adapterMethodBody.addStatement(0, new NativeStatement("@try {"));
    // Everthing else is after updated body.
    if (wasVoidReturn) {
      adapterConfig.adapterMethodBody.addStatement(
          new ExpressionStatement(adapterConfig.returnValueExpression));
      adapterConfig.adapterMethodBody.addStatement(new NativeStatement("return YES;"));
    } else {
      adapterConfig.adapterMethodBody.addStatement(
          new ReturnStatement(adapterConfig.returnValueExpression));
    }
    // Catch all exceptions, not just java.lang.Throwable. However, we do not catch all throws
    // (i.e. not "@catch (id e)").
    adapterConfig.adapterMethodBody.addStatement(
        new NativeStatement("} @catch (NSException *e) {"));
    adapterConfig.adapterMethodBody.addStatement(
        new NativeStatement("if (nativeError) { *nativeError = JREErrorFromException(e); }"));
    adapterConfig.adapterMethodBody.addStatement(catchReturnStatement);
    adapterConfig.adapterMethodBody.addStatement(new NativeStatement("}"));
  }

  private boolean methodAdaptsBooleanReturns(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.RETURN_NATIVE_BOOLS);
    }
    return false;
  }

  private boolean methodAdaptsBooleanArgs(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.ACCEPT_NATIVE_BOOLS);
    }
    return false;
  }

  private void adaptBooleanReturns(AdapterConfig adapterConfig) {
    if (TypeUtil.isBoolean(adapterConfig.adapterMethodReturnType)) {
      adapterConfig.adapterMethodReturnType = TypeUtil.BOOL_TYPE;
      // To handle OBJC_BOOL_IS_BOOL difference, always map to YES/NO.
      ConditionalExpression boolConditionExpression = new ConditionalExpression();
      var unused1 = boolConditionExpression.setTypeMirror(TypeUtil.BOOL_TYPE);
      var unused2 = boolConditionExpression.setExpression(adapterConfig.returnValueExpression);
      var unused3 =
          boolConditionExpression.setThenExpression(
              new NativeExpression("YES", TypeUtil.BOOL_TYPE));
      boolConditionExpression.setElseExpression(new NativeExpression("NO", TypeUtil.BOOL_TYPE));
      adapterConfig.returnValueExpression = boolConditionExpression;
    } else {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod native BOOL return type adaptation used on a method without a"
              + " boolean return.");
    }
  }

  private void adaptBooleanArgs(
      ExecutableElement adaptedMethodExecutable, AdapterConfig adapterConfig) {
    boolean atLeastOneBoolean = false;

    for (int i = 0; i < adaptedMethodExecutable.getParameters().size(); i++) {
      VariableElement param = adaptedMethodExecutable.getParameters().get(i);
      if (!TypeUtil.isBoolean(param.asType())) {
        continue;
      }
      atLeastOneBoolean = true;

      // All BOOL values are valid jbooleans, regardless of OBJC_BOOL_IS_BOOL
      GeneratedVariableElement nativeParam =
          GeneratedVariableElement.newParameter(
              param.getSimpleName().toString(), TypeUtil.BOOL_TYPE, param.getEnclosingElement());
      adapterConfig.adapterMethodParameters.set(i, nativeParam);
    }

    if (!atLeastOneBoolean) {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod native boolean argument adaptation used on method without"
              + " boolean arguments.");
    }
  }

  private boolean methodAdaptsEnumReturns(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.RETURN_NATIVE_ENUMS);
    }
    return false;
  }

  private boolean methodAdaptsEnumArgs(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.ACCEPT_NATIVE_ENUMS);
    }
    return false;
  }

  private void adaptEnumReturns(AdapterConfig adapterConfig) {
    if (!TypeUtil.isEnum(adapterConfig.adapterMethodReturnType)) {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod native enum return type adaptation used on a method without an"
              + " enum return.");
      return;
    }

    TypeElement returnTypeElement = TypeUtil.asTypeElement(adapterConfig.adapterMethodReturnType);
    String nativeReturnTypeName = nameTable.getNativeEnumName(returnTypeElement);
    adapterConfig.adapterMethodReturnType =
        new NativeEnumType(nativeReturnTypeName, adapterConfig.adapterMethodReturnType);

    ExecutableElement toNsEnumExecutable =
        GeneratedExecutableElement.newMethodWithSelector(
            "toNSEnum", adapterConfig.adapterMethodReturnType, null);
    ExecutablePair toNsEnumPair = new ExecutablePair(toNsEnumExecutable);
    adapterConfig.returnValueExpression =
        new MethodInvocation(toNsEnumPair, adapterConfig.adaptedMethodInvocation);
  }

  private MethodInvocation fromEnumInvocation(TypeMirror enumType, Expression originalExpression) {
    TypeElement enumObjcClass = TypeUtil.asTypeElement(enumType);
    ExecutableElement fromEnumElement =
        GeneratedExecutableElement.newMethodWithSelector("fromNSEnum:", enumType, enumObjcClass)
            .addModifiers(Modifier.STATIC);
    MethodInvocation fromEnumInvocation =
        new MethodInvocation(new ExecutablePair(fromEnumElement), new SimpleName(enumObjcClass));
    fromEnumInvocation.addArgument(originalExpression.copy());
    return fromEnumInvocation;
  }

  private void adaptEnumArgs(
      ExecutableElement adaptedMethodExecutable, AdapterConfig adapterConfig) {
    boolean atLeastOneEnum = false;

    for (int i = 0; i < adaptedMethodExecutable.getParameters().size(); i++) {
      VariableElement param = adaptedMethodExecutable.getParameters().get(i);
      if (!TypeUtil.isEnum(param.asType())) {
        continue;
      }
      atLeastOneEnum = true;

      TypeElement paramTypeElement = TypeUtil.asTypeElement(param.asType());
      if (paramTypeElement == null) {
        continue;
      }

      String nativeParamTypeName = nameTable.getNativeEnumName(paramTypeElement);
      NativeEnumType nativeParamType = new NativeEnumType(nativeParamTypeName, param.asType());
      String nativeParamName = param.getSimpleName().toString(); // Reuse the same name is OK.
      GeneratedVariableElement nativeParam =
          GeneratedVariableElement.newParameter(
              nativeParamName, nativeParamType, param.getEnclosingElement());
      adapterConfig.adapterMethodParameters.set(i, nativeParam);

      MethodInvocation fromEnumInvocation =
          fromEnumInvocation(
              param.asType(), new NativeExpression(nativeParamName, nativeParamType));
      adapterConfig.adaptedMethodArguments.set(i, fromEnumInvocation);
    }

    if (!atLeastOneEnum) {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod native enum argument adaptation used on method without enum"
              + " arguments.");
    }
  }

  private boolean methodAdaptsArrayReturns(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.RETURN_LISTS_AS_NATIVE_ARRAYS);
    }

    return false;
  }

  private boolean isList(TypeMirror type) {
    TypeElement element = TypeUtil.asTypeElement(type);
    if (element != null && element.getQualifiedName().contentEquals("java.util.List")) {
      return true;
    }
    for (TypeMirror t : typeUtil.directSupertypes(type)) {
      boolean result = isList(t);
      if (result) {
        return true;
      }
    }
    return false;
  }

  private void adaptArrayReturns(AdapterConfig adapterConfig) {
    if (!isList(adapterConfig.adapterMethodReturnType)) {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod array return type adaptation used on a non-list return type.");
      return;
    }

    List<? extends TypeMirror> originalTypeArguments =
        TypeUtil.getTypeArguments(adapterConfig.adapterMethodReturnType);
    NativeCollectionType nativeReturnType =
        NativeCollectionType.newNativeArray(originalTypeArguments);
    adapterConfig.adapterMethodReturnType = nativeReturnType;

    FunctionElement functionElement =
        new FunctionElement("JREAdaptedArrayFromJavaList", nativeReturnType, null);
    FunctionInvocation functionInvocation =
        new FunctionInvocation(functionElement, nativeReturnType);
    functionInvocation.addArgument(adapterConfig.adaptedMethodInvocation);
    adapterConfig.returnValueExpression = functionInvocation;
  }

  private boolean methodAdaptsProtocolReturns(ExecutableElement methodExecutable) {
    EnumSet<Adaptation> adaptations = adaptationsForMethod(methodExecutable);
    if (adaptations != null) {
      return adaptations.contains(Adaptation.RETURN_ADAPTER_PROTOCOLS);
    }
    return false;
  }

  private TypeMirror adaptedProtocolReturnType(TypeMirror originalReturnType) {
    TypeElement returnTypeElement = TypeUtil.asTypeElement(originalReturnType);
    if (returnTypeElement != null) {
      AnnotationMirror protocolAnnotation =
          ElementUtil.getAnnotation(returnTypeElement, ObjectiveCAdapterProtocol.class);
      if (protocolAnnotation != null) {
        String protocolName = (String) ElementUtil.getAnnotationValue(protocolAnnotation, "value");
        if (isNullOrEmpty(protocolName)) {
          ErrorUtil.error(
              "ObjectiveCAdapterMethod protocol return type adaptation invalid"
                  + " ObjectiveCAdapterProtocol.");
          return originalReturnType;
        }

        // Assume the class annotated with ObjectiveCAdapterProtocol already has imported the
        // header. Other usages will use the forward declaration autogenerated by
        // GeneratedTypeElement.
        return GeneratedTypeElement.newIosInterface(protocolName, "").asType();
      }
    }

    List<? extends TypeMirror> typeArguments = TypeUtil.getTypeArguments(originalReturnType);

    if (typeArguments.isEmpty()) {
      return originalReturnType;
    }

    List<TypeMirror> adaptedTypeArgs = new ArrayList<>();
    boolean adaptedAtLeastOneType = false;
    for (TypeMirror typeArg : typeArguments) {
      TypeMirror adaptedTypeArg = adaptedProtocolReturnType(typeArg);
      if (adaptedTypeArg != typeArg) {
        adaptedAtLeastOneType = true;
      }
      adaptedTypeArgs.add(adaptedTypeArg);
    }

    if (!adaptedAtLeastOneType) {
      return originalReturnType;
    }

    if (returnTypeElement != null) {
      GeneratedTypeElement adaptedReturnTypeElement =
          GeneratedTypeElement.mutableCopy(returnTypeElement);
      adaptedReturnTypeElement.setTypeArguments(adaptedTypeArgs);
      return adaptedReturnTypeElement.asType();
    } else if (TypeUtil.isNativeType(originalReturnType)) {
      ((NativeType) originalReturnType).setTypeArguments(adaptedTypeArgs);
      return originalReturnType;
    } else {
      return originalReturnType;
    }
  }

  private void adaptProtocolReturns(AdapterConfig adapterConfig) {
    TypeMirror adaptedReturn = adaptedProtocolReturnType(adapterConfig.adapterMethodReturnType);
    if (typeUtil.isSameType(adaptedReturn, adapterConfig.adapterMethodReturnType)) {
      ErrorUtil.warning(
          "ObjectiveCAdapterMethod protocol return type adaptation used on a return type not"
              + " annotated with ObjectiveCAdapterProtocol.");
    } else {
      adapterConfig.adapterMethodReturnType = adaptedReturn;
    }
  }

  private void addAdapterMethod(
      AbstractTypeDeclaration typeDeclaration, ExecutableElement methodExecutable) {
    if (!isMethodAnnotatedForAdapter(methodExecutable)) {
      return;
    }

    List<VariableElement> configParameters = new ArrayList<>();
    List<Expression> configArguments = new ArrayList<>();
    for (VariableElement param : methodExecutable.getParameters()) {
      configParameters.add(param);
      configArguments.add(new SimpleName(param));
    }
    if (methodAdaptsExceptionsAsError(methodExecutable)) {
      GeneratedVariableElement errorParam =
          GeneratedVariableElement.newParameter(
              "nativeError", new NativeType("NSError **", "JreExceptionAdapters.h"), null);
      configParameters.add(errorParam);
    }

    MethodInvocation orginalMethodInvocation =
        new MethodInvocation(new ExecutablePair(methodExecutable), null);
    AdapterConfig adapterConfig =
        new AdapterConfig(
            methodExecutable.getReturnType(),
            configParameters,
            configArguments,
            orginalMethodInvocation,
            orginalMethodInvocation);

    if (methodAdaptsBooleanReturns(methodExecutable)) {
      adaptBooleanReturns(adapterConfig);
    }

    if (methodAdaptsBooleanArgs(methodExecutable)) {
      adaptBooleanArgs(methodExecutable, adapterConfig);
    }

    if (methodAdaptsEnumReturns(methodExecutable)) {
      adaptEnumReturns(adapterConfig);
    }

    if (methodAdaptsEnumArgs(methodExecutable)) {
      adaptEnumArgs(methodExecutable, adapterConfig);
    }

    if (methodAdaptsProtocolReturns(methodExecutable)) {
      adaptProtocolReturns(adapterConfig);
    }

    if (methodAdaptsArrayReturns(methodExecutable)) {
      adaptArrayReturns(adapterConfig);
    }

    // Process exception wrapping last as it affects the postion of return statement in body.
    if (methodAdaptsExceptionsAsError(methodExecutable)) {
      adaptExceptionsAsErrors(adapterConfig);
    } else {
      // Outside exception handling the return statement should be final statement.
      if (TypeUtil.isVoid(adapterConfig.adapterMethodReturnType)) {
        adapterConfig.adapterMethodBody.addStatement(
            new ExpressionStatement(adapterConfig.returnValueExpression));
      } else {
        adapterConfig.adapterMethodBody.addStatement(
            new ReturnStatement(adapterConfig.returnValueExpression));
      }
    }

    var unused1 =
        adapterConfig.adaptedMethodInvocation.setArguments(adapterConfig.adaptedMethodArguments);
    GeneratedExecutableElement adapterMethodExecutable =
        GeneratedExecutableElement.newAdapterMethod(
            adapterSelectorForMethod(methodExecutable),
            adapterConfig.adapterMethodReturnType,
            adapterConfig.adapterMethodParameters,
            methodExecutable);
    MethodDeclaration adapterMethodDeclaration = new MethodDeclaration(adapterMethodExecutable);
    var unused2 = adapterMethodDeclaration.setExecutableElement(adapterMethodExecutable);
    adapterMethodDeclaration.setBody(adapterConfig.adapterMethodBody);
    adapterMethodDeclaration.getParameters().clear();
    for (VariableElement adapterParam : adapterConfig.adapterMethodParameters) {
      adapterMethodDeclaration.addParameter(new SingleVariableDeclaration(adapterParam));
    }

    typeDeclaration.addBodyDeclaration(adapterMethodDeclaration);
  }

  @Override
  public void endVisit(MethodDeclaration methodDeclaration) {
    AbstractTypeDeclaration typeDeclaration =
        (AbstractTypeDeclaration) methodDeclaration.getParent();
    if (!typeDeclaration.getTypeElement().getKind().isInterface()) {
      addAdapterMethod(typeDeclaration, methodDeclaration.getExecutableElement());
    }
  }

  @Override
  public void endVisit(TypeDeclaration typeDeclaration) {
    if (typeDeclaration.isInterface()) {
      return;
    }
    for (TypeMirror supertype :
        typeUtil.directSupertypes(typeDeclaration.getTypeElement().asType())) {
      TypeElement superTypeElement = TypeUtil.asTypeElement(supertype);
      if (superTypeElement.getKind().isInterface()) {
        for (ExecutableElement superMethodExecutable :
            Iterables.filter(
                ElementUtil.getMethods(superTypeElement), ElementUtil::isInstanceMethod)) {
          addAdapterMethod(typeDeclaration, superMethodExecutable);
        }
      }
    }
  }
}
