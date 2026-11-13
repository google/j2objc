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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.RecordDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.translate.KmpMethodHierarchy.TypeMethods;
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
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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

  private final KmpTypeConverter typeConverter;
  private final KmpMethodHierarchy methodHierarchy;

  public ObjectiveCKmpMethodTranslator(CompilationUnit unit) {
    super(unit);
    this.typeConverter = new KmpTypeConverter(typeUtil, nameTable, options);
    this.methodHierarchy = new KmpMethodHierarchy(typeUtil, elementUtil);
  }

  // Re-run lambda lowering passes because ObjectiveCKmpMethodTranslator synthesizes new
  // LambdaExpression AST arguments for composable collection converters after LambdaRewriter
  // originally executed in the translator pipeline.
  @Override
  public void run() {
    Set<AbstractTypeDeclaration> existingTypes = new HashSet<>(unit.getTypes());
    super.run();
    new LambdaTypeElementAdder(unit, "$KmpLambda$").run();
    new LambdaRewriter(unit).run();
    new InnerClassExtractor(unit).run();

    Functionizer functionizer = new Functionizer(unit);
    var unused = functionizer.visit(unit);
    InitializationNormalizer initNormalizer = new InitializationNormalizer(unit);
    OperatorRewriter operatorRewriter = new OperatorRewriter(unit);

    // Pass 1: Run InitializationNormalizer, Functionizer, and OperatorRewriter exclusively over
    // newly introduced lambda type declarations created during this translation pass.
    for (AbstractTypeDeclaration type : unit.getTypes()) {
      if (!existingTypes.contains(type)) {
        type.accept(initNormalizer);
        type.accept(functionizer);
        for (Statement stmt : type.getClassInitStatements()) {
          stmt.accept(operatorRewriter);
        }
      }
    }

    // Pass 2: Run Functionizer specifically on ClassInstanceCreation nodes across the unit.
    // This updates newly generated lambda instantiations (e.g., inside generated adapter methods).
    unit.accept(
        new TreeVisitor() {
          @Override
          public void endVisit(ClassInstanceCreation node) {
            functionizer.endVisit(node);
          }
        });

    new StaticVarRewriter(unit).run();
    new PrivateDeclarationResolver(unit).run();
  }

  private void processType(AbstractTypeDeclaration typeNode) {
    TypeElement typeElem = typeNode.getTypeElement();
    TypeMethods resolved = methodHierarchy.getTypeMethods(typeElem);
    boolean isInterface = typeElem.getKind().isInterface();

    for (Map.Entry<String, AnnotationMirror> entry : resolved.annotatedSignatures().entrySet()) {
      String signature = entry.getKey();
      AnnotationMirror annotation = entry.getValue();
      ExecutablePair method = resolved.allMethods().get(signature);

      boolean shouldGenerate;
      MappingStrategy strategy = MappingStrategy.NATIVE_TO_JAVA;

      if (isInterface) {
        strategy = MappingStrategy.JAVA_TO_NATIVE;
        MethodDeclaration unadaptedDecl = findMethodDeclaration(typeNode, method.element());
        if (unadaptedDecl != null) {
          unadaptedDecl.setOptional(true);
        }
        // Only generate the adapted protocol method and _dispatch helper when the method is
        // declared and directly annotated on this interface (skipping inherited methods and
        // unannotated subinterface overrides).
        boolean isDeclaredOnCurrentInterface =
            ElementUtil.getDeclaringClass(method.element()).equals(typeElem);
        boolean hasDirectAnnotation =
            ElementUtil.getAnnotation(method.element(), ObjectiveCKmpMethod.class) != null;
        shouldGenerate = isDeclaredOnCurrentInterface && hasDirectAnnotation;
      } else {
        TypeMethods superHierarchy = methodHierarchy.getSupertypeHierarchy(typeElem, signature);
        if (ElementUtil.isAbstract(method.element())) {
          strategy = MappingStrategy.JAVA_TO_NATIVE;
          // Generate if declared on this class, or if inherited by an abstract class from an
          // interface rather than an abstract superclass that already generated them.
          shouldGenerate =
              ElementUtil.getDeclaringClass(method.element()).equals(typeElem)
                  || (ElementUtil.isAbstract(typeElem)
                      && (superHierarchy == null
                          || superHierarchy.typeElement().getKind().isInterface()));
        } else {
          shouldGenerate =
              superHierarchy == null
                  || ElementUtil.isAbstract(superHierarchy.allMethods().get(signature).element())
                  || ElementUtil.isDefault(superHierarchy.allMethods().get(signature).element());
        }
      }

      if (shouldGenerate) {
        new AdapterContext(typeNode, method, annotation, isInterface, strategy).processMethod();
      }
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    processType(node);
  }

  @Override
  public void endVisit(RecordDeclaration node) {
    processType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    TypeElement typeElem = node.getTypeElement();
    if (typeElem != null && typeElem.getSimpleName().contentEquals("package-info")) {
      return;
    }
    processType(node);
  }

  @Override
  public void endVisit(MethodInvocation node) {
    // Check if the called method is (or overrides) an @ObjectiveCKmpMethod interface method.
    ExecutableElement targetMethod =
        methodHierarchy.findInterfaceMethodRequiringDispatch(node.getExecutableElement());
    if (targetMethod == null) {
      return;
    }

    // Rewrite `receiver.foo(args...)` to `Interface_foo_dispatch(receiver, args...)`.
    FunctionElement funcElement =
        createDispatchElement(ElementUtil.getDeclaringClass(targetMethod), targetMethod);
    FunctionInvocation functionInvocation =
        new FunctionInvocation(funcElement, node.getTypeMirror());
    List<Expression> funcArgs = functionInvocation.getArguments();

    // Pass the receiver as the first argument (`self`), synthesizing `this` for implicit calls.
    Expression receiver = node.getExpression();
    if (receiver == null) {
      receiver = new ThisExpression(TreeUtil.getEnclosingTypeElement(node).asType());
    } else {
      receiver = TreeUtil.remove(receiver);
    }
    funcArgs.add(receiver);

    // Move the remaining method arguments and replace the method call with the C function call.
    TreeUtil.moveList(node.getArguments(), funcArgs);
    node.replaceWith(functionInvocation);
  }

  private static @Nullable MethodDeclaration findMethodDeclaration(
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
  private FunctionElement createDispatchElement(TypeElement ownerType, ExecutableElement method) {
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
      this.originalMethodExecutable = originalMethod.element();
      this.originalMethodReturnType = originalMethod.type().getReturnType();
      this.originalMethodParameters = resolveMethodParameters(originalMethod);
      this.selector = (String) ElementUtil.getAnnotationValue(annotation, "selector");
      this.swiftName = (String) ElementUtil.getAnnotationValue(annotation, "swiftName");
      this.adapter = (TypeMirror) ElementUtil.getAnnotationValue(annotation, "adapter");
      this.isInterface = isInterface;
      this.strategy = strategy;
    }

    /** Substitutes generic parameter types when the method is inherited from a supertype. */
    private ImmutableList<VariableElement> resolveMethodParameters(ExecutablePair originalMethod) {
      List<? extends VariableElement> params = originalMethod.element().getParameters();
      List<? extends TypeMirror> resolvedTypes = originalMethod.type().getParameterTypes();
      ImmutableList.Builder<VariableElement> resolvedParams = ImmutableList.builder();
      for (int i = 0; i < params.size(); i++) {
        VariableElement param = params.get(i);
        TypeMirror resolvedType = resolvedTypes.get(i);
        resolvedParams.add(
            typeUtil.isSameType(param.asType(), resolvedType)
                ? param
                : GeneratedVariableElement.newParameter(
                        ElementUtil.getName(param), resolvedType, param.getEnclosingElement())
                    .addAnnotationMirrors(param.getAnnotationMirrors()));
      }
      return resolvedParams.build();
    }

    /** Orchestrates the parameter conversion and adapter method creation. */
    private void processMethod() {
      // Calculate the return type of the adapter method. If the original method returns void,
      // the adapter method also returns void. Otherwise, calculate the native Objective-C type.
      TypeMirror adapterReturnType =
          TypeUtil.isVoid(originalMethodReturnType)
              ? originalMethodReturnType
              : typeConverter.calculateNativeType(
                  originalMethodReturnType, originalMethodExecutable);

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

    private void generateStaticMethodDeclaration(
        TypeMirror adapterReturnType, FunctionElement functionElement) {
      ParameterMapping mapping = processParameters();
      List<Expression> adaptingArguments = mapping.adaptingArguments();
      List<VariableElement> adapterParameters = mapping.adapterParameters();

      GeneratedExecutableElement adapterMethodExecutable =
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);
      attachSwiftNameAnnotation(adapterMethodExecutable);

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
      attachSwiftNameAnnotation(adapterMethodExecutable);

      // Create a MethodInvocation to call the original instance Java method.
      MethodInvocation adaptingMethodInvocation =
          new MethodInvocation(new ExecutablePair(originalMethodExecutable), null);
      // Add the (potentially converted) arguments to the MethodInvocation.
      adaptingArguments.forEach(adaptingMethodInvocation::addArgument);

      addMethodDeclaration(
          adapterMethodExecutable, adaptingMethodInvocation, adapterReturnType, adapterParameters);
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
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, mapping.adapterParameters(), originalMethodExecutable);
      attachSwiftNameAnnotation(adapterMethodExecutable);

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
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);
      attachSwiftNameAnnotation(adapterMethodExecutable);

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
      typeNode
          .getBodyDeclarations()
          .removeIf(
              decl ->
                  decl instanceof MethodDeclaration methodDecl
                      && methodDecl.getExecutableElement() instanceof GeneratedExecutableElement gee
                      && ElementUtil.isAbstract(gee)
                      && originalMethodExecutable.equals(gee.getOriginalElement()));
      typeNode.addBodyDeclaration(createBridgeMethodDeclaration(bodyBlock));
    }

    private Block createAdaptedInvocationBlock(
        GeneratedExecutableElement targetMethod,
        @Nullable Expression receiver,
        List<Expression> arguments) {
      MethodInvocation invocation =
          new MethodInvocation(new ExecutablePair(targetMethod), receiver);
      arguments.forEach(invocation::addArgument);
      return new Block().addStatement(createReturnStatement(invocation, originalMethodReturnType));
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
        GeneratedExecutableElement adapterMethodExecutable,
        List<Expression> adaptingArguments) {
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
        Expression receiver,
        List<? extends VariableElement> parameters) {
      FunctionInvocation invocation =
          new FunctionInvocation(targetFunction, originalMethodReturnType);
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

    /** Creates a concrete Java bridge method that delegates to the abstract KMP adapter stub. */
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
          GeneratedExecutableElement.newAdapterMethod(
              selector, adapterReturnType, adapterParameters, originalMethodExecutable);
      attachSwiftNameAnnotation(adapterMethodExecutable);

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
      return typeConverter.createReturnStatement(
          adapter,
          strategy,
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
