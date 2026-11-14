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

import static com.google.common.truth.Truth.assertThat;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.translate.KmpTypeConverter.MappingStrategy;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import com.google.j2objc.annotations.SwiftName;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import org.jspecify.annotations.Nullable;

/** Unit tests for {@link KmpAdapterEmitter}. */
public class KmpAdapterEmitterTest extends GenerationTest {

  private static final String ADAPTER_SOURCE =
      """
      import java.util.List;

      public class Adapter {
        public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
        public static native Object fromJavaUtilList(List<?> list) /*-[ return nil; ]-*/;
      }
      """;

  private static final String STATIC_SAMPLE_SOURCE =
      """
      import com.google.j2objc.annotations.ObjectiveCKmpMethod;
      import java.util.List;

      public class Sample {
        @ObjectiveCKmpMethod(selector = "convertList:", adapter = Adapter.class)
        public static List<?> convert(List<?> items) {
          return items;
        }
      }
      """;

  @Override
  public void setUp() throws IOException {
    super.setUp();
    addSourceFile(ADAPTER_SOURCE, "Adapter.java");
  }

  private KmpAdapterEmitter createEmitter(CompilationUnit unit) {
    var env = unit.getEnv();
    KmpTypeConverter converter =
        new KmpTypeConverter(env.typeUtil(), env.nameTable(), env.options());
    return new KmpAdapterEmitter(env.typeUtil(), env.nameTable(), env.options(), converter);
  }

  @CanIgnoreReturnValue
  private AbstractTypeDeclaration emitMethod(
      CompilationUnit unit,
      String typeName,
      String methodName,
      boolean isInterface,
      MappingStrategy strategy) {
    AbstractTypeDeclaration typeDecl = findTypeDecl(unit, typeName);
    ExecutableElement method = findMethodElement(typeDecl.getTypeElement(), methodName);
    AnnotationMirror annotation = ElementUtil.getAnnotation(method, ObjectiveCKmpMethod.class);
    createEmitter(unit)
        .emit(typeDecl, new ExecutablePair(method), annotation, isInterface, strategy);
    return typeDecl;
  }

  private static AbstractTypeDeclaration findTypeDecl(CompilationUnit unit, String simpleName) {
    for (AbstractTypeDeclaration typeDecl : unit.getTypes()) {
      if (typeDecl.getTypeElement().getSimpleName().contentEquals(simpleName)) {
        return typeDecl;
      }
    }
    throw new AssertionError("Type not found: " + simpleName);
  }

  private static ExecutableElement findMethodElement(TypeElement type, String methodName) {
    for (ExecutableElement exec : ElementUtil.getExecutables(type)) {
      if (ElementUtil.getName(exec).equals(methodName)) {
        return exec;
      }
    }
    throw new AssertionError("Method not found: " + methodName + " in " + type);
  }

  private static FunctionDeclaration findFunction(
      AbstractTypeDeclaration typeDecl, String functionName) {
    for (FunctionDeclaration fn : TreeUtil.getFunctionDeclarations(typeDecl)) {
      if (fn.getName().equals(functionName)) {
        return fn;
      }
    }
    throw new AssertionError("Function not found: " + functionName);
  }

  private static @Nullable MethodDeclaration findMethodBySelector(
      AbstractTypeDeclaration typeDecl, CompilationUnit unit, String selector) {
    for (MethodDeclaration md : TreeUtil.getMethodDeclarations(typeDecl)) {
      if (unit.getEnv().nameTable().getMethodSelector(md.getExecutableElement()).equals(selector)) {
        return md;
      }
    }
    return null;
  }

  public void testCreateDispatchElement_buildsInstanceFunctionSignature() {
    CompilationUnit unit =
        compileType(
            "MyProto",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public interface MyProto {
              @ObjectiveCKmpMethod(selector = "acceptItems:", adapter = Adapter.class)
              int accept(List<String> items);
            }
            """);
    TypeElement proto = findTypeDecl(unit, "MyProto").getTypeElement();
    ExecutableElement method = findMethodElement(proto, "accept");

    FunctionElement dispatchElement = createEmitter(unit).createDispatchElement(proto, method);
    String dispatchName = dispatchElement.getName();

    assertThat(dispatchName).endsWith("_dispatch");
    assertThat(dispatchName).startsWith("MyProto_acceptWith");
    var typeUtil = unit.getEnv().typeUtil();
    assertThat(typeUtil.isSameType(dispatchElement.getReturnType(), method.getReturnType()))
        .isTrue();
    assertThat(dispatchElement.getParameterTypes()).hasSize(2);
    assertThat(typeUtil.isSameType(dispatchElement.getParameterTypes().get(0), proto.asType()))
        .isTrue();
  }

  public void testEmit_staticMethodEmitsCFunctionAndWrapperMethod() {
    CompilationUnit unit = compileType("Sample", STATIC_SAMPLE_SOURCE);
    AbstractTypeDeclaration typeDecl =
        emitMethod(unit, "Sample", "convert", false, MappingStrategy.NATIVE_TO_JAVA);

    FunctionDeclaration cFunc = findFunction(typeDecl, "Sample_convertList_");
    assertThat(cFunc.getParameters()).hasSize(1);
    ReturnStatement returnStmt = (ReturnStatement) cFunc.getBody().getStatements().get(0);
    CastExpression castExpr = (CastExpression) returnStmt.getExpression();
    MethodInvocation fromCall = (MethodInvocation) castExpr.getExpression();
    assertThat(ElementUtil.getName(fromCall.getExecutableElement())).isEqualTo("fromJavaUtilList");
    assertThat(fromCall.getArguments().get(0)).isInstanceOf(FunctionInvocation.class);

    MethodDeclaration objcWrapper = findMethodBySelector(typeDecl, unit, "convertList:");
    assertThat(objcWrapper).isNotNull();
    assertThat(Modifier.isStatic(objcWrapper.getModifiers())).isTrue();
  }

  public void testEmit_staticMethodWithoutWrapperMethodsOmitsObjcWrapper() throws IOException {
    options.load(new String[] {"--no-wrapper-methods"});
    CompilationUnit unit = compileType("Sample", STATIC_SAMPLE_SOURCE);
    AbstractTypeDeclaration typeDecl =
        emitMethod(unit, "Sample", "convert", false, MappingStrategy.NATIVE_TO_JAVA);

    assertThat(findFunction(typeDecl, "Sample_convertList_")).isNotNull();
    assertThat(findMethodBySelector(typeDecl, unit, "convertList:")).isNull();
  }

  public void testEmit_constructorEmitsInitNewCreateFunctionsAndMethod() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public class Sample {
              @ObjectiveCKmpMethod(selector = "initWithItems:", adapter = Adapter.class)
              public Sample(List<?> items) {}
            }
            """);
    AbstractTypeDeclaration typeDecl = findTypeDecl(unit, "Sample");
    ExecutableElement ctor = ElementUtil.getConstructors(typeDecl.getTypeElement()).iterator().next();
    AnnotationMirror annotation = ElementUtil.getAnnotation(ctor, ObjectiveCKmpMethod.class);

    createEmitter(unit)
        .emit(
            typeDecl, new ExecutablePair(ctor), annotation, false, MappingStrategy.NATIVE_TO_JAVA);

    assertThat(findFunction(typeDecl, "Sample_initWithItems_").getParameters()).hasSize(2);
    assertThat(findFunction(typeDecl, "new_Sample_initWithItems_").getParameters()).hasSize(1);
    assertThat(findFunction(typeDecl, "create_Sample_initWithItems_").getParameters()).hasSize(1);

    MethodDeclaration initMethod = findMethodBySelector(typeDecl, unit, "initWithItems:");
    assertThat(initMethod).isNotNull();
    List<Statement> stmts = initMethod.getBody().getStatements();
    assertThat(stmts).hasSize(2);
    assertThat(stmts.get(0)).isInstanceOf(ExpressionStatement.class);
    assertThat(((ReturnStatement) stmts.get(1)).getExpression()).isInstanceOf(ThisExpression.class);
  }

  public void testEmit_interfaceAbstractMethodEmitsProtocolMethodAndTwoWayDispatch() {
    CompilationUnit unit =
        compileType(
            "MyProto",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public interface MyProto {
              @ObjectiveCKmpMethod(selector = "acceptItems:", adapter = Adapter.class)
              void accept(List<?> items);
            }
            """);
    AbstractTypeDeclaration protoDecl =
        emitMethod(unit, "MyProto", "accept", true, MappingStrategy.JAVA_TO_NATIVE);

    MethodDeclaration adaptedDecl = findMethodBySelector(protoDecl, unit, "acceptItems:");
    assertThat(adaptedDecl).isNotNull();
    assertThat(Modifier.isAbstract(adaptedDecl.getModifiers())).isTrue();
    assertThat(adaptedDecl.getBody()).isNull();

    ExecutableElement method = findMethodElement(protoDecl.getTypeElement(), "accept");
    String dispatchName =
        createEmitter(unit).createDispatchElement(protoDecl.getTypeElement(), method).getName();
    FunctionDeclaration dispatchFn = findFunction(protoDecl, dispatchName);
    IfStatement outerIf = (IfStatement) dispatchFn.getBody().getStatements().get(0);
    assertThat(((NativeExpression) outerIf.getExpression()).getCode())
        .contains("respondsToSelector:@selector(acceptWithJavaUtilList:)");
    assertThat(outerIf.getElseStatement()).isInstanceOf(Block.class);
  }

  public void testEmit_interfaceDefaultMethodEmitsThreeWayDispatchFallback() {
    CompilationUnit unit =
        compileType(
            "MyProto",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public interface MyProto {
              @ObjectiveCKmpMethod(selector = "processItems:", adapter = Adapter.class)
              default void process(List<?> items) {}
            }
            """);
    AbstractTypeDeclaration protoDecl =
        emitMethod(unit, "MyProto", "process", true, MappingStrategy.JAVA_TO_NATIVE);

    ExecutableElement method = findMethodElement(protoDecl.getTypeElement(), "process");
    String dispatchName =
        createEmitter(unit).createDispatchElement(protoDecl.getTypeElement(), method).getName();
    FunctionDeclaration dispatchFn = findFunction(protoDecl, dispatchName);
    IfStatement outerIf = (IfStatement) dispatchFn.getBody().getStatements().get(0);
    assertThat(outerIf.getElseStatement()).isInstanceOf(IfStatement.class);

    IfStatement fallbackIf = (IfStatement) outerIf.getElseStatement();
    assertThat(((NativeExpression) fallbackIf.getExpression()).getCode())
        .contains("respondsToSelector:@selector(processItems:)");
    Block defaultBlock = (Block) fallbackIf.getElseStatement();
    ExpressionStatement defaultStmt = (ExpressionStatement) defaultBlock.getStatements().get(0);
    assertThat(defaultStmt.getExpression()).isInstanceOf(FunctionInvocation.class);
  }

  public void testEmit_abstractClassDeclaredMethodEmitsStubAndUpdatesOriginalBody() {
    CompilationUnit unit =
        compileType(
            "AbstractBase",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public abstract class AbstractBase {
              @ObjectiveCKmpMethod(selector = "acceptItems:", adapter = Adapter.class)
              public abstract void accept(List<?> items);
            }
            """);
    AbstractTypeDeclaration typeDecl =
        emitMethod(unit, "AbstractBase", "accept", false, MappingStrategy.JAVA_TO_NATIVE);
    ExecutableElement method = findMethodElement(typeDecl.getTypeElement(), "accept");

    MethodDeclaration adapterDecl = findMethodBySelector(typeDecl, unit, "acceptItems:");
    assertThat(adapterDecl).isNotNull();
    NativeStatement nativeBody = (NativeStatement) adapterDecl.getBody().getStatements().get(0);
    assertThat(nativeBody.getCode()).contains("doesNotRecognizeSelector:_cmd");

    MethodDeclaration originalDecl = KmpAdapterEmitter.findMethodDeclaration(typeDecl, method);
    assertThat(originalDecl).isNotNull();
    assertThat(Modifier.isAbstract(originalDecl.getModifiers())).isFalse();
    ExpressionStatement forwardStmt =
        (ExpressionStatement) originalDecl.getBody().getStatements().get(0);
    MethodInvocation forwardCall = (MethodInvocation) forwardStmt.getExpression();
    assertThat(unit.getEnv().nameTable().getMethodSelector(forwardCall.getExecutableElement()))
        .isEqualTo("acceptItems:");
  }

  public void testEmit_abstractClassInheritedInterfaceMethodEmitsConcreteBridge()
      throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public interface Proto {
          @ObjectiveCKmpMethod(selector = "acceptItems:", adapter = Adapter.class)
          void accept(List<?> items);
        }
        """,
        "Proto.java");
    CompilationUnit unit =
        compileType("AbstractImpl", "public abstract class AbstractImpl implements Proto {}");
    AbstractTypeDeclaration abstractDecl = findTypeDecl(unit, "AbstractImpl");
    TypeElement protoType = unit.getEnv().typeUtil().resolveJavaType("Proto");
    ExecutableElement ifaceMethod = findMethodElement(protoType, "accept");
    AnnotationMirror annotation = ElementUtil.getAnnotation(ifaceMethod, ObjectiveCKmpMethod.class);
    ExecutableType resolvedType =
        (ExecutableType)
            unit.getEnv()
                .typeUtil()
                .asMemberOf((DeclaredType) abstractDecl.getTypeElement().asType(), ifaceMethod);

    createEmitter(unit)
        .emit(
            abstractDecl,
            new ExecutablePair(ifaceMethod, resolvedType),
            annotation,
            false,
            MappingStrategy.JAVA_TO_NATIVE);

    assertThat(findMethodBySelector(abstractDecl, unit, "acceptItems:")).isNotNull();
    MethodDeclaration bridgeDecl =
        findMethodBySelector(abstractDecl, unit, "acceptWithJavaUtilList:");
    assertThat(bridgeDecl).isNotNull();
    assertThat(Modifier.isAbstract(bridgeDecl.getModifiers())).isFalse();
    GeneratedExecutableElement bridgeElem =
        (GeneratedExecutableElement) bridgeDecl.getExecutableElement();
    assertThat(bridgeElem.getOriginalElement()).isEqualTo(ifaceMethod);
    assertThat(ElementUtil.getAnnotation(bridgeElem, ObjectiveCKmpMethod.class)).isNull();
    assertThat(ElementUtil.getAnnotation(bridgeElem, SwiftName.class)).isNull();
  }

  public void testEmit_attachesExplicitAndAutoComputedSwiftNames() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import com.google.j2objc.annotations.SwiftName;
            import java.util.List;

            @SwiftName
            public class Sample {
              @ObjectiveCKmpMethod(
                  selector = "doWork:count:",
                  swiftName = "work(with:total:)",
                  adapter = Adapter.class)
              public void doWork(List<?> items, int count) {}

              @ObjectiveCKmpMethod(selector = "setItems:tag:", adapter = Adapter.class)
              public void setItems(List<?> items, int tag) {}
            }
            """);
    AbstractTypeDeclaration typeDecl =
        emitMethod(unit, "Sample", "doWork", false, MappingStrategy.NATIVE_TO_JAVA);
    emitMethod(unit, "Sample", "setItems", false, MappingStrategy.NATIVE_TO_JAVA);

    MethodDeclaration explicitDecl = findMethodBySelector(typeDecl, unit, "doWork:count:");
    AnnotationMirror explicitMirror =
        ElementUtil.getAnnotation(explicitDecl.getExecutableElement(), SwiftName.class);
    assertThat(explicitMirror).isNotNull();
    assertThat(ElementUtil.getAnnotationValue(explicitMirror, "value"))
        .isEqualTo("work(with:total:)");

    MethodDeclaration autoDecl = findMethodBySelector(typeDecl, unit, "setItems:tag:");
    AnnotationMirror autoMirror =
        ElementUtil.getAnnotation(autoDecl.getExecutableElement(), SwiftName.class);
    assertThat(autoMirror).isNotNull();
    assertThat(ElementUtil.getAnnotationValue(autoMirror, "value"))
        .isEqualTo("setItems(items:tag:)");
  }
}
