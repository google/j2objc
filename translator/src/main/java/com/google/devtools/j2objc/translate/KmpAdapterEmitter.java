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

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.translate.KmpMethodHierarchy.ConcreteKmpOwner;
import com.google.devtools.j2objc.translate.KmpTypeConverter.MappingStrategy;
import com.google.devtools.j2objc.translate.KmpTypeConverter.ParameterMapping;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedAnnotationMirror;
import com.google.devtools.j2objc.types.GeneratedAnnotationValue;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import com.google.j2objc.annotations.SwiftName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.jspecify.annotations.Nullable;

/**
 * Generates Objective-C KMP adapter methods, C helper functions, bridge methods, protocol dispatch
 * helpers, and {@link SwiftName} annotations for {@link ObjectiveCKmpMethodTranslator}.
 */
final class KmpAdapterEmitter {

  private final TypeUtil typeUtil;
  private final NameTable nameTable;
  private final Options options;
  private final KmpTypeConverter typeConverter;
  private final Set<TypeElement> emittedOverrideCheckTypes = new HashSet<>();

  KmpAdapterEmitter(
      TypeUtil typeUtil, NameTable nameTable, Options options, KmpTypeConverter typeConverter) {
    this.typeUtil = typeUtil;
    this.nameTable = nameTable;
    this.options = options;
    this.typeConverter = typeConverter;
  }

  /** Emits all Objective-C adapter declarations required for {@code originalMethod}. */
  void emit(
      AbstractTypeDeclaration typeNode,
      ExecutablePair originalMethod,
      AnnotationMirror annotation,
      boolean isInterface,
      MappingStrategy strategy) {
    new AdapterContext(typeNode, originalMethod, annotation, isInterface, strategy).processMethod();
  }

  /**
   * Prepends a native-override check to an overriding or default-shim method on a subclass so Java
   * callers delegate to {@code baseOwner}'s adapted Objective-C selector when overridden natively.
   */
  void prependSubclassOverrideCheck(
      AbstractTypeDeclaration typeNode, ExecutablePair method, ConcreteKmpOwner baseOwner) {
    if (!canPrependSubclassOverrideCheck(typeNode, method, baseOwner, typeConverter)) {
      return;
    }
    MethodDeclaration concreteDecl = findConcreteMethodDeclaration(typeNode, method.element());
    if (concreteDecl == null) {
      return;
    }
    new AdapterContext(
            typeNode,
            method,
            baseOwner.annotation(),
            /* isInterface= */ false,
            MappingStrategy.JAVA_TO_NATIVE)
        .prependOverrideCheck(concreteDecl, baseOwner.ownerType(), baseOwner.method());
  }

  private static boolean canPrependSubclassOverrideCheck(
      AbstractTypeDeclaration typeNode,
      ExecutablePair method,
      ConcreteKmpOwner baseOwner,
      KmpTypeConverter typeConverter) {
    TypeMirror adapter =
        (TypeMirror) ElementUtil.getAnnotationValue(baseOwner.annotation(), "adapter");
    // A subclass override can dispatch to a native override only when:
    // 1. The subclass is exposed in the ObjC header and both the subclass and base methods can be
    //    overridden natively (non-final/non-local class, non-final/non-private method).
    // 2. The base method's signature can be reverse-adapted between Java and native types.
    // 3. Any covariant subclass return type can be converted back from the base native return type.
    return !typeNode.hasPrivateDeclaration()
        && canOverrideInNative(typeNode.getTypeElement(), method.element())
        && canOverrideInNative(baseOwner.ownerType(), baseOwner.method().element())
        && typeConverter.canReverseAdapt(baseOwner.method(), adapter)
        && typeConverter.canReverseAdaptSubclassReturn(
            baseOwner.method().type().getReturnType(), method.type().getReturnType(), adapter);
  }

  private static boolean canOverrideInNative(TypeElement type, ExecutableElement method) {
    return type.getKind() == ElementKind.CLASS
        && !ElementUtil.isFinal(type)
        && !ElementUtil.isLocal(type)
        && !ElementUtil.isFinal(method)
        && !ElementUtil.isPrivate(method);
  }

  static @Nullable MethodDeclaration findMethodDeclaration(
      @Nullable AbstractTypeDeclaration typeNode, ExecutableElement element) {
    if (typeNode == null) {
      return null;
    }
    for (MethodDeclaration methodDecl : TreeUtil.getMethodDeclarations(typeNode)) {
      if (methodDecl.getExecutableElement().equals(element)) {
        return methodDecl;
      }
    }
    return null;
  }

  static boolean isConcreteGeneratedShim(MethodDeclaration decl) {
    return decl.getBody() != null
        && decl.getExecutableElement() instanceof GeneratedExecutableElement gee
        && !ElementUtil.isAbstract(gee)
        && gee.getOriginalElement() != null;
  }

  /**
   * Returns true if {@code decl} is a concrete renaming shim emitted by {@link
   * DefaultMethodShimGenerator} that delegates to {@code self} under another selector.
   */
  static boolean isDefaultRenamingShim(MethodDeclaration decl) {
    if (!isConcreteGeneratedShim(decl) || decl.getBody().getStatements().size() != 1) {
      return false;
    }
    Statement stmt = decl.getBody().getStatements().get(0);
    Expression expr =
        stmt instanceof ReturnStatement ret
            ? ret.getExpression()
            : stmt instanceof ExpressionStatement exprStmt ? exprStmt.getExpression() : null;
    while (expr instanceof CastExpression cast) {
      expr = cast.getExpression();
    }
    return expr instanceof MethodInvocation call && call.getExpression() == null;
  }

  /**
   * Finds the primary concrete declaration or {@code default}-method shim implementing {@code
   * element} on {@code typeNode}, excluding {@code [self ...]} renaming shims.
   */
  private static @Nullable MethodDeclaration findConcreteMethodDeclaration(
      AbstractTypeDeclaration typeNode, ExecutableElement element) {
    for (MethodDeclaration methodDecl : TreeUtil.getMethodDeclarations(typeNode)) {
      if (methodDecl.getBody() == null) {
        continue;
      }
      ExecutableElement declElem = methodDecl.getExecutableElement();
      if (declElem.equals(element)
          || (isConcreteGeneratedShim(methodDecl)
              && element.equals(((GeneratedExecutableElement) declElem).getOriginalElement())
              && !isDefaultRenamingShim(methodDecl))) {
        return methodDecl;
      }
    }
    return null;
  }

  /**
   * Emits a class-level helper {@code bool <Class>_isOverride(<Class> *self, SEL selector)} at most
   * once per class, returning true when {@code selector}'s implementation differs from the class's.
   *
   * <p>Inspects {@code [self class]} rather than {@code object_getClass(self)} so ISA-swizzled
   * dynamic subclasses (such as {@code OCMPartialMock} or KVO {@code NSKVONotifying_*}) do not
   * falsely trigger adapted override dispatch and cause infinite recursion on unstubbed calls.
   *
   * <p>Java subclasses override the unadapted method directly (prepending their own check against
   * the owner class), so only native subclass overrides take the adapted branch. Calling {@code
   * [super ...]} from a native override—and cross-file Java {@code super} calls on a native
   * subclass instance—are unsupported because {@code super} dispatch preserves {@code self}.
   */
  private void ensureOverrideCheckHelper(AbstractTypeDeclaration typeNode) {
    TypeElement ownerType = typeNode.getTypeElement();
    if (!emittedOverrideCheckTypes.add(ownerType)) {
      return;
    }
    String className = nameTable.getFullName(ownerType);
    String signature =
        String.format("bool %s(%s *self, SEL selector)", getOverrideCheckName(ownerType), className);
    String body =
        String.format(
            """
            %s {
              if (self == nil) {
                return false;
              }
              Class cls = [self class];
              Class owner = [%s class];
              if (cls == owner || ![cls isSubclassOfClass:owner]) {
                return false;
              }
              IMP clsImp = [cls instanceMethodForSelector:selector];
              IMP ownerImp = [owner instanceMethodForSelector:selector];
              return clsImp != ownerImp;
            }\
            """,
            signature, className);
    typeNode.addBodyDeclaration(
        NativeDeclaration.newOuterDeclaration("FOUNDATION_EXPORT " + signature + ";", body));
  }

  private String getOverrideCheckName(TypeElement ownerType) {
    return nameTable.getFullName(ownerType) + "_isOverride";
  }

  /**
   * Returns true if {@code decl} is an abstract return-type-narrowing stub added by {@link
   * AbstractMethodRewriter} for {@code targetSelector}.
   */
  private static boolean isAbstractNarrowingStubFor(BodyDeclaration decl, String targetSelector) {
    return decl instanceof MethodDeclaration methodDecl
        && methodDecl.getExecutableElement() instanceof GeneratedExecutableElement gee
        && ElementUtil.isAbstract(gee)
        && gee.getOriginalElement() != null
        && targetSelector.equals(gee.getSelector());
  }

  /**
   * Marks unadapted protocol declarations (including return-type-narrowing stubs) {@code @optional}
   * so native conformers only implement the adapted selector.
   */
  void markUnadaptedProtocolDeclarationsOptional(
      AbstractTypeDeclaration typeNode,
      ExecutableElement element,
      ExecutableElement annotatedElement) {
    String targetSelector = nameTable.getMethodSelector(annotatedElement);
    for (MethodDeclaration methodDecl : TreeUtil.getMethodDeclarations(typeNode)) {
      if (methodDecl.getExecutableElement().equals(element)
          || isAbstractNarrowingStubFor(methodDecl, targetSelector)) {
        methodDecl.setOptional(true);
      }
    }
  }

  /**
   * Returns the function name for the C dispatch helper of an interface or concrete method owned by
   * {@code ownerType}.
   *
   * <p>Appends {@code "_dispatch"} to avoid colliding with functions generated by J2ObjC (such as
   * default method implementation bodies).
   */
  private String getDispatchFunctionName(TypeElement ownerType, ExecutableElement method) {
    return nameTable.getFullName(ownerType) + "_" + nameTable.getFunctionName(method) + "_dispatch";
  }

  /** Creates the function element for the C dispatch helper. */
  FunctionElement createDispatchElement(TypeElement ownerType, ExecutableElement method) {
    return createInstanceFunctionElement(
        getDispatchFunctionName(ownerType, method), ownerType, method);
  }

  private FunctionElement createDefaultMethodFunctionElement(ExecutableElement method) {
    TypeElement declaringInterface = ElementUtil.getDeclaringClass(method);
    return createInstanceFunctionElement(
        nameTable.getFullFunctionName(method), declaringInterface, method);
  }

  private FunctionElement createInstanceFunctionElement(
      String functionName, TypeElement ownerType, ExecutableElement method) {
    return new FunctionElement(functionName, method.getReturnType(), ownerType)
        .addParameters(ownerType.asType())
        .addParameters(ElementUtil.asTypes(method.getParameters()));
  }

  /** Context for processing a single method and generating its Objective-C adapter. */
  private class AdapterContext {
    private final String selector;
    private final String swiftName;
    private final AbstractTypeDeclaration typeNode;
    private final ExecutablePair originalMethod;
    private final ExecutableElement originalMethodExecutable;
    private final TypeMirror originalMethodReturnType;
    private final ImmutableList<VariableElement> originalMethodParameters;
    private final TypeMirror adapter;
    private final boolean isInterface;
    private final MappingStrategy strategy;

    private AdapterContext(
        AbstractTypeDeclaration typeNode,
        ExecutablePair originalMethod,
        AnnotationMirror annotation,
        boolean isInterface,
        MappingStrategy strategy) {
      this.typeNode = typeNode;
      this.originalMethod = originalMethod;
      this.originalMethodExecutable = originalMethod.element();
      this.originalMethodReturnType = originalMethod.type().getReturnType();
      this.originalMethodParameters =
          resolveParameters(originalMethod, originalMethod.element().getParameters());
      this.selector = (String) ElementUtil.getAnnotationValue(annotation, "selector");
      this.swiftName = (String) ElementUtil.getAnnotationValue(annotation, "swiftName");
      this.adapter = (TypeMirror) ElementUtil.getAnnotationValue(annotation, "adapter");
      this.isInterface = isInterface;
      this.strategy = strategy;
    }

    /**
     * Pairs {@code contractMethod}'s resolved parameter types and annotations with {@code
     * nameSources}' variable names and enclosing elements.
     */
    private ImmutableList<VariableElement> resolveParameters(
        ExecutablePair contractMethod, List<? extends VariableElement> nameSources) {
      List<? extends VariableElement> contractParams = contractMethod.element().getParameters();
      List<? extends TypeMirror> contractTypes = contractMethod.type().getParameterTypes();
      ImmutableList.Builder<VariableElement> resolved = ImmutableList.builder();
      for (int i = 0; i < contractParams.size(); i++) {
        VariableElement contractParam = contractParams.get(i);
        TypeMirror contractType = contractTypes.get(i);
        VariableElement nameSource = nameSources.get(i);
        if (nameSource.equals(contractParam)
            && typeUtil.isSameType(contractParam.asType(), contractType)) {
          resolved.add(contractParam);
        } else {
          String paramName =
              nameSource.equals(contractParam)
                  ? ElementUtil.getName(contractParam)
                  : nameTable.getVariableShortName(nameSource);
          resolved.add(
              GeneratedVariableElement.newParameter(
                      paramName, contractType, nameSource.getEnclosingElement())
                  .addAnnotationMirrors(contractParam.getAnnotationMirrors()));
        }
      }
      return resolved.build();
    }

    private TypeMirror getAdapterReturnType(ExecutablePair method) {
      TypeMirror returnType = method.type().getReturnType();
      return TypeUtil.isVoid(returnType)
          ? returnType
          : typeConverter.calculateNativeType(returnType, method.element());
    }

    /** Orchestrates the parameter conversion and adapter method creation. */
    private void processMethod() {
      // Calculate the return type of the adapter method. If the original method returns void,
      // the adapter method also returns void. Otherwise, calculate the native Objective-C type.
      TypeMirror adapterReturnType = getAdapterReturnType(originalMethod);

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
      } else if (isInterface) {
        generateInterfaceMethodDeclaration(adapterReturnType);
      } else if (strategy == MappingStrategy.JAVA_TO_NATIVE) {
        generateAbstractInstanceMethodDeclaration(adapterReturnType);
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

    private GeneratedExecutableElement createAdapterExecutable(
        TypeMirror adapterReturnType, List<VariableElement> adapterParameters) {
      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);
      attachSwiftNameAnnotation(adapterMethodExecutable);
      return adapterMethodExecutable;
    }

    private void generateStaticMethodDeclaration(
        TypeMirror adapterReturnType, FunctionElement functionElement) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      GeneratedExecutableElement adapterMethodExecutable =
          createAdapterExecutable(adapterReturnType, adapterParameters);

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
          createAdapterExecutable(adapterReturnType, adapterParameters);

      // Create a MethodInvocation to call the original instance Java method.
      MethodInvocation adaptingMethodInvocation =
          new MethodInvocation(new ExecutablePair(originalMethodExecutable), null);
      // Add the (potentially converted) arguments to the MethodInvocation.
      adaptingArguments.forEach(adaptingMethodInvocation::addArgument);

      addMethodDeclaration(
          adapterMethodExecutable, adaptingMethodInvocation, adapterReturnType, adapterParameters);
      emitConcreteNativeOverrideSupport();
    }

    // Enables native ObjC/Swift subclasses to override this concrete @ObjectiveCKmpMethod via its
    // adapted selector: ensures the class-level `<Owner>_isOverride` helper is emitted and prepends
    // an override check to the unadapted Java method body so Java callers dispatch to the native
    // override when present.
    private void emitConcreteNativeOverrideSupport() {
      TypeElement ownerType = typeNode.getTypeElement();
      if (typeNode.hasPrivateDeclaration()
          || !canOverrideInNative(ownerType, originalMethodExecutable)
          || !typeConverter.canReverseAdapt(originalMethod, adapter)) {
        return;
      }
      MethodDeclaration concreteDecl =
          findConcreteMethodDeclaration(typeNode, originalMethodExecutable);
      // When this class inherits an unannotated superclass implementation that satisfies a KMP
      // interface method, there is no local body yet; synthesize a [super ...] bridge to attach
      // the override check to.
      if (concreteDecl == null
          && !ElementUtil.isDefault(originalMethodExecutable)
          && !ElementUtil.getDeclaringClass(originalMethodExecutable).equals(ownerType)) {
        concreteDecl = synthesizeSuperBridgeDeclaration();
        typeNode.addBodyDeclaration(concreteDecl);
      }
      // Emit the helper even when DeadCodeEliminator stripped the owner's concrete body so live
      // subclass overrides referencing `<Owner>_isOverride` still compile and link.
      ensureOverrideCheckHelper(typeNode);
      if (concreteDecl != null) {
        prependOverrideCheck(concreteDecl, ownerType, originalMethod);
      }
    }

    // Synthesizes a local bridge forwarding to an unannotated superclass implementation so the
    // override check can be prepended on this KMP owner class. Example:
    //   - (SubFoo *)getItemWithImmutableList:(ImmutableList *)items {
    //     // (override check prepended here)
    //     return (SubFoo *) [super getItemWithImmutableList:items];
    //   }
    private MethodDeclaration synthesizeSuperBridgeDeclaration() {
      SuperMethodInvocation superCall =
          new SuperMethodInvocation(new ExecutablePair(originalMethodExecutable));
      for (VariableElement param : originalMethodParameters) {
        superCall.addArgument(new SimpleName(param));
      }
      Expression superExpr =
          typeConverter.maybeCast(typeUtil.erasure(originalMethodReturnType), superCall);
      MethodDeclaration bridgeDecl =
          createBridgeMethodDeclaration(createReturnOrExpressionBlock(superExpr));
      bridgeDecl.setHasDeclaration(false);
      return bridgeDecl;
    }

    // Prepends a runtime override guard to `concreteDecl`'s body so Java callers dispatch to a
    // native ObjC/Swift override when present. Example:
    //   if (Owner_isOverride(self, @selector(getItemsAsNsArray))) {
    //     return [Adapter toImmutableListWithId:[self getItemsAsNsArray]];
    //   }
    // (or `[self setItems:[Adapter fromImmutableList:...]]; return;` for void methods).
    private void prependOverrideCheck(
        MethodDeclaration concreteDecl, TypeElement ownerType, ExecutablePair contractMethod) {
      ImmutableList<VariableElement> contractParams =
          resolveParameters(contractMethod, getParameterElements(concreteDecl));
      ParameterMapping reverseMapping =
          typeConverter.processParameters(
              adapter, MappingStrategy.JAVA_TO_NATIVE, contractMethod.element(), contractParams);
      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector,
              getAdapterReturnType(contractMethod),
              reverseMapping.adapterParameters(),
              contractMethod.element());
      Block thenBlock =
          createAdaptedInvocationBlock(
              adapterMethodExecutable, /* receiver= */ null, reverseMapping.adaptingArguments());
      if (TypeUtil.isVoid(originalMethodReturnType)) {
        thenBlock.addStatement(new ReturnStatement());
      }
      Expression checkExpression =
          new NativeExpression(
              String.format("%s(self, @selector(%s))", getOverrideCheckName(ownerType), selector),
              typeUtil.getBoolean());
      concreteDecl
          .getBody()
          .getStatements()
          .add(0, new IfStatement().setExpression(checkExpression).setThenStatement(thenBlock));
    }

    private static ImmutableList<VariableElement> getParameterElements(MethodDeclaration decl) {
      return decl.getParameters().stream()
          .map(SingleVariableDeclaration::getVariableElement)
          .collect(toImmutableList());
    }

    private boolean isInSwiftNameContext() {
      if (options.swiftNaming()) {
        return true;
      }
      Element current = typeNode.getTypeElement();
      while (current != null) {
        if (ElementUtil.getAnnotation(current, SwiftName.class) != null) {
          return true;
        }
        current = current.getEnclosingElement();
      }
      PackageElement pkg = ElementUtil.getPackage(typeNode.getTypeElement());
      return pkg != null && ElementUtil.getAnnotation(pkg, SwiftName.class) != null;
    }

    private @Nullable String getOrComputeSwiftName() {
      if (swiftName != null && !swiftName.isEmpty()) {
        return swiftName;
      }
      if (!isInSwiftNameContext()) {
        return null;
      }
      String baseName = selector;
      int firstColon = selector.indexOf(':');
      if (firstColon != -1) {
        baseName = selector.substring(0, firstColon);
      }
      if (originalMethodParameters.isEmpty()) {
        return baseName + "()";
      }
      StringBuilder sb = new StringBuilder(baseName).append("(");
      for (VariableElement param : originalMethodParameters) {
        String paramName = param.getSimpleName().toString();
        if (paramName.isEmpty()) {
          sb.append("_:");
        } else {
          sb.append(paramName).append(":");
        }
      }
      sb.append(")");
      return sb.toString();
    }

    private void attachSwiftNameAnnotation(GeneratedExecutableElement adapterMethodExecutable) {
      AnnotationMirror existingSwiftName =
          ElementUtil.getAnnotation(adapterMethodExecutable, SwiftName.class);
      if (existingSwiftName != null) {
        adapterMethodExecutable.removeAnnotationMirror(existingSwiftName);
      }
      String effectiveSwiftName = getOrComputeSwiftName();
      if (effectiveSwiftName == null || effectiveSwiftName.isEmpty()) {
        return;
      }
      TypeElement swiftNameElement = typeUtil.resolveJavaType(SwiftName.class.getName());
      DeclaredType swiftNameType = (DeclaredType) swiftNameElement.asType();
      GeneratedAnnotationMirror annotationMirror = new GeneratedAnnotationMirror(swiftNameType);
      ExecutableElement valueElement =
          GeneratedExecutableElement.newMethodWithSelector(
              "value", typeUtil.getJavaString().asType(), swiftNameElement);
      adapterMethodExecutable.addAnnotationMirror(annotationMirror);
      annotationMirror.addElementValue(
          valueElement, new GeneratedAnnotationValue(effectiveSwiftName));
    }

    private void generateInterfaceMethodDeclaration(TypeMirror adapterReturnType) {
      ParameterMapping mapping = processParameters();
      GeneratedExecutableElement adapterMethodExecutable =
          createAdapterExecutable(adapterReturnType, mapping.adapterParameters());

      // 1. Declare the adapted KMP method on the @protocol.
      // Example: - (void)setItems:(NSArray<NSString *> *)items;
      // (The unadapted method declaration was already marked @optional in visit()).
      typeNode.addBodyDeclaration(
          createAdapterMethodDeclaration(
              adapterMethodExecutable,
              /* adapterBodyBlock= */ null,
              mapping.adapterParameters(),
              /* isInterface= */ true));

      // 2. Generate the static C dispatch helper function that checks respondsToSelector:
      // at runtime to dispatch to either the JVM selector or the adapted Objective-C selector.
      typeNode.addBodyDeclaration(
          generateInterfaceDispatchHelper(adapterMethodExecutable, mapping.adaptingArguments()));
    }

    private void generateAbstractInstanceMethodDeclaration(TypeMirror adapterReturnType) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      GeneratedExecutableElement adapterMethodExecutable =
          createAdapterExecutable(adapterReturnType, adapterParameters);

      Block adapterBodyBlock = new Block();
      String bodyCode =
          "// can't call an abstract method\n" + "[self doesNotRecognizeSelector:_cmd];";
      if (!TypeUtil.isVoid(adapterReturnType)) {
        bodyCode += "\nreturn 0;";
      }
      adapterBodyBlock.addStatement(new NativeStatement(bodyCode));

      MethodDeclaration adapterMethodDeclaration =
          createAdapterMethodDeclaration(
              adapterMethodExecutable, adapterBodyBlock, adapterParameters, false);

      typeNode.addBodyDeclaration(adapterMethodDeclaration);

      // Now modify original method to call adapter
      Block bodyBlock =
          createAdaptedInvocationBlock(
              adapterMethodExecutable, /* receiver= */ null, adaptingArguments);

      // Find the method declaration in typeNode and update it, or synthesize a bridge declaration
      // if the abstract class inherited the method without redeclaring it.
      MethodDeclaration md = findMethodDeclaration(typeNode, originalMethodExecutable);
      if (md != null) {
        md.setBody(bodyBlock);
        md.removeModifiers(ABSTRACT);
        return;
      }
      // Remove any abstract return-type-narrowing stub added earlier by AbstractMethodRewriter.
      String originalSelector = nameTable.getMethodSelector(originalMethodExecutable);
      typeNode
          .getBodyDeclarations()
          .removeIf(decl -> isAbstractNarrowingStubFor(decl, originalSelector));
      typeNode.addBodyDeclaration(createBridgeMethodDeclaration(bodyBlock));
    }

    private Block createAdaptedInvocationBlock(
        GeneratedExecutableElement targetMethod,
        @Nullable Expression receiver,
        List<Expression> arguments) {
      MethodInvocation invocation =
          new MethodInvocation(new ExecutablePair(targetMethod), receiver);
      arguments.forEach(invocation::addArgument);
      return new Block()
          .addStatement(
              createReturnStatement(
                  MappingStrategy.JAVA_TO_NATIVE, invocation, originalMethodReturnType));
    }

    private FunctionDeclaration createDispatchFunctionSkeleton() {
      TypeElement ownerType = typeNode.getTypeElement();
      VariableElement selfParam =
          GeneratedVariableElement.newParameter(
              NameTable.SELF_NAME, ownerType.asType(), originalMethodExecutable);
      FunctionDeclaration functionDecl =
          new FunctionDeclaration(
              getDispatchFunctionName(ownerType, originalMethodExecutable),
              originalMethodReturnType,
              originalMethodExecutable);
      functionDecl.addParameter(new SingleVariableDeclaration(selfParam));
      for (VariableElement param : originalMethodParameters) {
        functionDecl.addParameter(new SingleVariableDeclaration(param));
      }
      return functionDecl;
    }

    /**
     * Generates a static C dispatch helper function for an interface method annotated with
     * {@code @ObjectiveCKmpMethod}.
     *
     * <p>Java call sites targeting interface methods are rewritten to invoke this function instead
     * of dispatching Objective-C messages directly. At runtime, the receiver instance may either
     * be:
     *
     * <ul>
     *   <li>A transpiled Java class implementing the interface, which implements the unadapted JVM
     *       selector.
     *   <li>A native Objective-C or Swift class conforming to the transpiled {@code @protocol},
     *       which only implements the adapted Objective-C selector.
     * </ul>
     *
     * <p>The helper checks {@code [self respondsToSelector:]} on the unadapted selector first. If
     * implemented, it dispatches directly; otherwise, it adapts arguments and invokes the adapted
     * selector.
     *
     * <p>When {@code originalMethodExecutable} is a {@code default} method, a 3-way fallback is
     * generated: if the receiver implements neither the unadapted nor the adapted selector (e.g. a
     * native Objective-C class that inherits the protocol default method without overriding it),
     * the helper falls back to invoking the interface's default method implementation function.
     */
    private FunctionDeclaration generateInterfaceDispatchHelper(
        GeneratedExecutableElement adapterMethodExecutable, List<Expression> adaptingArguments) {
      // 1. Build the static C function declaration with `self` as the first parameter.
      // Example:
      //   void MyInterface_setItemsWith..._dispatch(id<MyInterface> self,
      // ComGoogleCommonCollectImmutableList *items)
      FunctionDeclaration functionDecl = createDispatchFunctionSkeleton();
      VariableElement selfParam = functionDecl.getParameter(0).getVariableElement();

      // 2. Direct invocation on `self` using the unadapted JVM selector.
      // Fast path for Java classes implementing the interface.
      // Example: [self setItemsWithComGoogleCommonCollectImmutableList:items];
      String selfName = nameTable.getVariableShortName(selfParam);
      MethodInvocation directInvocation =
          new MethodInvocation(
              new ExecutablePair(originalMethodExecutable), new SimpleName(selfParam));
      for (VariableElement param : originalMethodParameters) {
        directInvocation.addArgument(new SimpleName(param));
      }

      // 3. Adapted invocation on `self` using the KMP adapted selector with converted arguments.
      // Fallback for native Objective-C/Swift classes conforming to the @protocol.
      // Example: [self setItems:(NSArray *) [ImmutableListAdapter fromImmutableListWith...:items]];
      Block adaptedBlock =
          createAdaptedInvocationBlock(
              adapterMethodExecutable, new SimpleName(selfParam), adaptingArguments);

      // 4. Runtime check routing between direct invocation and fallback:
      //   if ([self
      // respondsToSelector:@selector(setItemsWithComGoogleCommonCollectImmutableList:)]) {
      //     [self setItemsWithComGoogleCommonCollectImmutableList:items];
      //   } else if (isDefaultMethod && ![self respondsToSelector:@selector(setItems:)]) {
      //     Interface_setItems(self, items); // default method implementation
      //   } else {
      //     [self setItems:(NSArray *) [ImmutableListAdapter fromImmutableListWith...:items]];
      //   }
      Statement elseStatement =
          ElementUtil.isDefault(originalMethodExecutable)
              ? createDefaultMethodFallback(selfName, selfParam, adaptedBlock)
              : adaptedBlock;
      String originalSelector = nameTable.getMethodSelector(originalMethodExecutable);
      IfStatement ifStatement =
          new IfStatement()
              .setExpression(
                  new NativeExpression(
                      "[" + selfName + " respondsToSelector:@selector(" + originalSelector + ")]",
                      typeUtil.getBoolean()))
              .setThenStatement(createReturnOrExpressionBlock(directInvocation))
              .setElseStatement(elseStatement);
      functionDecl.setBody(new Block().addStatement(ifStatement));
      return functionDecl;
    }

    private Statement createDefaultMethodFallback(
        String selfName, VariableElement selfParam, Block adaptedBlock) {
      FunctionInvocation defaultInvocation =
          createForwardingFunctionInvocation(
              createDefaultMethodFunctionElement(originalMethodExecutable),
              originalMethodReturnType,
              new SimpleName(selfParam),
              originalMethodParameters);
      return new IfStatement()
          .setExpression(
              new NativeExpression(
                  "[" + selfName + " respondsToSelector:@selector(" + selector + ")]",
                  typeUtil.getBoolean()))
          .setThenStatement(adaptedBlock)
          .setElseStatement(createReturnOrExpressionBlock(defaultInvocation));
    }

    private FunctionInvocation createForwardingFunctionInvocation(
        FunctionElement targetFunction,
        TypeMirror returnType,
        Expression receiver,
        List<? extends VariableElement> parameters) {
      FunctionInvocation invocation = new FunctionInvocation(targetFunction, returnType);
      invocation.addArgument(receiver);
      for (VariableElement param : parameters) {
        invocation.addArgument(new SimpleName(param));
      }
      return invocation;
    }

    /** Wraps {@code expression} in a return statement, or an expression statement if void. */
    private Block createReturnOrExpressionBlock(Expression expression) {
      return new Block()
          .addStatement(
              TypeUtil.isVoid(originalMethodReturnType)
                  ? new ExpressionStatement(expression)
                  : new ReturnStatement(expression));
    }

    /**
     * Synthesizes a concrete unadapted bridge method (for abstract-class or {@code [super ...]}
     * delegation) with {@code bodyBlock}.
     */
    private MethodDeclaration createBridgeMethodDeclaration(Block bodyBlock) {
      String originalSelector = nameTable.getMethodSelector(originalMethodExecutable);
      GeneratedExecutableElement bridgeExecutable =
          GeneratedExecutableElement.newMethodWithSelector(
                  originalSelector, originalMethodReturnType, typeNode.getTypeElement())
              .addModifiers(originalMethodExecutable.getModifiers())
              .removeModifiers(Modifier.ABSTRACT, Modifier.DEFAULT)
              .setOriginalElement(originalMethodExecutable);
      bridgeExecutable
          .addAnnotationMirrors(originalMethodExecutable.getAnnotationMirrors())
          .removeAnnotationMirror(
              ElementUtil.getAnnotation(bridgeExecutable, ObjectiveCKmpMethod.class))
          .removeAnnotationMirror(ElementUtil.getAnnotation(bridgeExecutable, SwiftName.class));

      MethodDeclaration bridgeDeclaration =
          new MethodDeclaration(bridgeExecutable).setBody(bodyBlock);
      for (VariableElement param : originalMethodParameters) {
        bridgeExecutable.addParameter(param);
        bridgeDeclaration.addParameter(new SingleVariableDeclaration(param));
      }
      return bridgeDeclaration;
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
          createAdapterExecutable(adapterReturnType, adapterParameters);

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

    private ParameterMapping processParameters() {
      return typeConverter.processParameters(
          adapter, strategy, originalMethodExecutable, originalMethodParameters);
    }

    private Statement createReturnStatement(
        Expression adaptingMethodInvocation, TypeMirror adapterReturnType) {
      return createReturnStatement(strategy, adaptingMethodInvocation, adapterReturnType);
    }

    private Statement createReturnStatement(
        MappingStrategy mappingStrategy,
        Expression adaptingMethodInvocation,
        TypeMirror adapterReturnType) {
      return typeConverter.createReturnStatement(
          adapter,
          mappingStrategy,
          originalMethodExecutable,
          originalMethodReturnType,
          adaptingMethodInvocation,
          adapterReturnType);
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
}
