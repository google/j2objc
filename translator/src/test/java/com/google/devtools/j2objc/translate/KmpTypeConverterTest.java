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
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.translate.KmpTypeConverter.MappingStrategy;
import com.google.devtools.j2objc.translate.KmpTypeConverter.ParameterMapping;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/** Unit tests for {@link KmpTypeConverter}. */
public class KmpTypeConverterTest extends GenerationTest {

  private static final String ADAPTER_SOURCE =
      """
      import java.util.List;
      import java.util.Map;
      import java.util.function.Function;

      public class Adapter {
        public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
        public static native Object fromJavaUtilList(List<?> list) /*-[ return nil; ]-*/;
        public static native <T> List<T> toJavaUtilList(
            Object list, Function<Object, T> conv) /*-[ return nil; ]-*/;
        public static native <T> Object fromJavaUtilList(
            List<T> list, Function<T, Object> conv) /*-[ return nil; ]-*/;
        public static native Map<?, ?> toJavaUtilMap(Object map) /*-[ return nil; ]-*/;
        public static native Object fromJavaUtilMap(Map<?, ?> map) /*-[ return nil; ]-*/;
        public static native Object fromJavaUtilList_String_(
            List<String> list) /*-[ return nil; ]-*/;
        public static native List<String> toJavaUtilList_String_(
            Object list) /*-[ return nil; ]-*/;
      }
      """;

  private static final String WILDCARD_ADAPTER_SOURCE =
      """
      import java.util.List;

      public class WildcardAdapter {
        public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
        public static native Object fromJavaUtilList(List<?> list) /*-[ return nil; ]-*/;
      }
      """;

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.load(new String[] {"--objc-generics"});
    addSourceFile(ADAPTER_SOURCE, "Adapter.java");
    addSourceFile(WILDCARD_ADAPTER_SOURCE, "WildcardAdapter.java");
    addSourceFile("public interface MyProto {}", "MyProto.java");
    addSourceFile("public class PlainObj {}", "PlainObj.java");
    addSourceFile("public class SubPlainObj extends PlainObj {}", "SubPlainObj.java");
  }

  private static KmpTypeConverter createConverter(CompilationUnit unit) {
    return new KmpTypeConverter(
        unit.getEnv().typeUtil(), unit.getEnv().nameTable(), unit.getEnv().options());
  }

  private static TypeMirror resolveType(CompilationUnit unit, String typeName) {
    return unit.getEnv().typeUtil().resolveJavaType(typeName).asType();
  }

  private static ExecutableElement findMethod(CompilationUnit unit, String methodName) {
    List<ExecutableElement> matches = new ArrayList<>();
    for (var typeDecl : unit.getTypes()) {
      for (MethodDeclaration md : TreeUtil.getMethodDeclarations(typeDecl)) {
        if (ElementUtil.getName(md.getExecutableElement()).equals(methodName)) {
          matches.add(md.getExecutableElement());
        }
      }
    }
    assertThat(matches).hasSize(1);
    return matches.get(0);
  }

  private static String returnNativeName(
      CompilationUnit unit, KmpTypeConverter converter, String methodName) {
    ExecutableElement method = findMethod(unit, methodName);
    return converter.calculateNativeType(method.getReturnType(), method).getName();
  }

  @CanIgnoreReturnValue
  private static MethodInvocation assertCastCall(
      CompilationUnit unit,
      Expression expr,
      TypeMirror expectedCastType,
      String expectedMethodName) {
    assertThat(expr).isInstanceOf(CastExpression.class);
    CastExpression cast = (CastExpression) expr;
    assertThat(
            unit.getEnv().typeUtil().isSameType(cast.getType().getTypeMirror(), expectedCastType))
        .isTrue();
    return assertDirectCall(cast.getExpression(), expectedMethodName);
  }

  @CanIgnoreReturnValue
  private static MethodInvocation assertDirectCall(Expression expr, String expectedMethodName) {
    assertThat(expr).isInstanceOf(MethodInvocation.class);
    MethodInvocation call = (MethodInvocation) expr;
    assertThat(ElementUtil.getName(call.getExecutableElement())).isEqualTo(expectedMethodName);
    return call;
  }

  private static void assertNativeParamType(VariableElement param, String expectedNativeName) {
    assertThat(param.asType()).isInstanceOf(NativeType.class);
    assertThat(((NativeType) param.asType()).getName()).isEqualTo(expectedNativeName);
  }

  public void testCalculateNativeType_collectionsAndBoxedGenerics() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableSet;
            import java.util.List;
            import java.util.Map;
            import java.util.Set;
            public class Sample {
              public List<String> strings() { return null; }
              public Map<Integer, List<String>> nestedMap() { return null; }
              public Set<Boolean> boolSet() { return null; }
              public ImmutableList<Long> immutableList() { return null; }
              public ImmutableMap<Byte, Short> immutableMap() { return null; }
              public ImmutableSet<Double> immutableSet() { return null; }
              public List<Float> floatList() { return null; }
              public List<?> wildcardList() { return null; }
              public <T> List<T> typeVarList() { return null; }
              @SuppressWarnings("rawtypes")
              public List rawList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    assertThat(returnNativeName(unit, converter, "strings")).isEqualTo("NSArray<NSString *> *");
    assertThat(returnNativeName(unit, converter, "nestedMap"))
        .isEqualTo("NSDictionary<NSNumber *, NSArray<NSString *> *> *");
    assertThat(returnNativeName(unit, converter, "boolSet")).isEqualTo("NSSet<NSNumber *> *");
    assertThat(returnNativeName(unit, converter, "immutableList"))
        .isEqualTo("NSArray<NSNumber *> *");
    assertThat(returnNativeName(unit, converter, "immutableMap"))
        .isEqualTo("NSDictionary<NSNumber *, NSNumber *> *");
    assertThat(returnNativeName(unit, converter, "immutableSet")).isEqualTo("NSSet<NSNumber *> *");
    assertThat(returnNativeName(unit, converter, "floatList")).isEqualTo("NSArray<NSNumber *> *");
    assertThat(returnNativeName(unit, converter, "wildcardList")).isEqualTo("NSArray<id> *");
    assertThat(returnNativeName(unit, converter, "typeVarList")).isEqualTo("NSArray<id> *");
    assertThat(returnNativeName(unit, converter, "rawList")).isEqualTo("NSArray *");
  }

  public void testCalculateNativeType_genericsDisabledOmitsTypeArguments() {
    options.setAsObjCGenericDecl(false);
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              public List<String> strings() { return null; }
              public List<MyProto> protos() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    assertThat(returnNativeName(unit, converter, "strings")).isEqualTo("NSArray *");
    assertThat(returnNativeName(unit, converter, "protos")).isEqualTo("NSArray *");
  }

  public void testCalculateNativeType_interfacesAndReferencedTypes() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            import org.jspecify.annotations.Nullable;
            public class Sample {
              public MyProto proto() { return null; }
              public @Nullable MyProto nullableProto() { return null; }
              public List<@Nullable MyProto> protoList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    ExecutableElement proto = findMethod(unit, "proto");
    NativeType protoNative = converter.calculateNativeType(proto.getReturnType(), proto);
    assertThat(protoNative.getName()).isEqualTo("id<MyProto>");
    assertThat(protoNative.getReferencedTypes()).containsExactly(proto.getReturnType());

    assertThat(returnNativeName(unit, converter, "nullableProto"))
        .isEqualTo("id<MyProto> _Nullable");

    ExecutableElement protoList = findMethod(unit, "protoList");
    NativeType protoListNative =
        converter.calculateNativeType(protoList.getReturnType(), protoList);
    assertThat(protoListNative.getName()).isEqualTo("NSArray<id<MyProto> _Nullable> *");
    assertThat(protoListNative.getReferencedTypes()).hasSize(1);
  }

  public void testCalculateNativeType_primitives() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            public class Sample {
              public void take(
                  boolean z, int i, long j, float f, double d, char c, byte b, short s) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement take = findMethod(unit, "take");
    List<String> actual =
        take.getParameters().stream()
            .map(p -> converter.calculateNativeType(p.asType(), take).getName())
            .toList();

    assertThat(actual)
        .containsExactly(
            "BOOL", "int32_t", "int64_t", "float", "double", "unichar", "char", "int16_t")
        .inOrder();
  }

  public void testCalculateNativeType_nullabilityAndVoidExemption() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            import org.jspecify.annotations.Nullable;
            public class Sample {
              public @Nullable List<@Nullable String> mappedElemNullable() { return null; }
              public List<@Nullable PlainObj> customElemNullable() { return null; }
              public @Nullable PlainObj customNullable() { return null; }
              public PlainObj customNonNull() { return null; }
              public @Nullable Void nullableVoid() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    // Mapped Foundation types omit inner `_Nullable` markers inside ObjC generic arguments.
    assertThat(returnNativeName(unit, converter, "mappedElemNullable"))
        .isEqualTo("NSArray<NSString *> * _Nullable");
    assertThat(returnNativeName(unit, converter, "customElemNullable"))
        .isEqualTo("NSArray<PlainObj * _Nullable> *");
    assertThat(returnNativeName(unit, converter, "customNullable"))
        .isEqualTo("PlainObj * _Nullable");
    assertThat(returnNativeName(unit, converter, "customNonNull")).isEqualTo("PlainObj *");
    assertThat(returnNativeName(unit, converter, "nullableVoid")).isEqualTo("JavaLangVoid *");
  }

  public void testProcessParameters_nativeToJavaAdaptsMappedAndPassesThroughPrimitives() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              public void accept(List<String> items, int count) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());

    ParameterMapping mapping =
        converter.processParameters(
            resolveType(unit, "Adapter"), MappingStrategy.NATIVE_TO_JAVA, accept, params);
    assertThat(mapping.adapterParameters()).hasSize(2);
    assertNativeParamType(mapping.adapterParameters().get(0), "NSArray<NSString *> *");
    assertThat(mapping.adapterParameters().get(1)).isEqualTo(params.get(1));

    assertDirectCall(mapping.adaptingArguments().get(0), "toJavaUtilList_String_");
    assertThat(mapping.adaptingArguments().get(1)).isInstanceOf(SimpleName.class);
    assertThat(((SimpleName) mapping.adaptingArguments().get(1)).getElement())
        .isEqualTo(params.get(1));
  }

  public void testProcessParameters_unmappedInterfaceNullableAndCustomConverter()
      throws IOException {
    addSourceFile(
        """
        public class CustomAdapter {
          public static native PlainObj toPlainObj(Object obj) /*-[ return nil; ]-*/;
        }
        """,
        "CustomAdapter.java");
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import org.jspecify.annotations.Nullable;
            public class Sample {
              private CustomAdapter adapterRef;
              public void accept(
                  MyProto proto,
                  @Nullable SubPlainObj nullableUnmapped,
                  PlainObj customMapped,
                  SubPlainObj plainUnmapped) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());

    ParameterMapping mapping =
        converter.processParameters(
            resolveType(unit, "CustomAdapter"), MappingStrategy.NATIVE_TO_JAVA, accept, params);
    assertThat(mapping.adapterParameters()).hasSize(4);
    assertNativeParamType(mapping.adapterParameters().get(0), "id<MyProto>");
    assertNativeParamType(mapping.adapterParameters().get(1), "SubPlainObj * _Nullable");
    assertNativeParamType(mapping.adapterParameters().get(2), "PlainObj *");
    assertThat(mapping.adapterParameters().get(3)).isEqualTo(params.get(3));

    assertThat(mapping.adaptingArguments().get(0)).isInstanceOf(SimpleName.class);
    assertThat(mapping.adaptingArguments().get(1)).isInstanceOf(SimpleName.class);
    assertDirectCall(mapping.adaptingArguments().get(2), "toPlainObj");
    assertThat(mapping.adaptingArguments().get(3)).isInstanceOf(SimpleName.class);
  }

  public void testProcessParameters_javaToNativeDirection() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              public void accept(List<String> items, PlainObj plain) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());
    NativeType paramNativeType = converter.calculateNativeType(params.get(0).asType(), accept);

    ParameterMapping mapping =
        converter.processParameters(
            resolveType(unit, "Adapter"), MappingStrategy.JAVA_TO_NATIVE, accept, params);
    assertCastCall(
        unit, mapping.adaptingArguments().get(0), paramNativeType, "fromJavaUtilList_String_");
    assertThat(mapping.adapterParameters().get(1)).isEqualTo(params.get(1));
    assertThat(mapping.adaptingArguments().get(1)).isInstanceOf(SimpleName.class);
  }

  public void testProcessParameters_wildcardFallbackAndArgumentAssignability() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private WildcardAdapter wildcardRef;
              public void accept(List<PlainObj> items) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror wildcardAdapter = resolveType(unit, "WildcardAdapter");
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());
    NativeType nativeType = converter.calculateNativeType(params.get(0).asType(), accept);

    ParameterMapping n2j =
        converter.processParameters(
            wildcardAdapter, MappingStrategy.NATIVE_TO_JAVA, accept, params);
    MethodInvocation toCall =
        assertCastCall(
            unit, n2j.adaptingArguments().get(0), params.get(0).asType(), "toJavaUtilList");
    assertThat(toCall.getArguments()).hasSize(1);
    assertThat(toCall.getArguments().get(0)).isInstanceOf(SimpleName.class);

    ParameterMapping j2n =
        converter.processParameters(
            wildcardAdapter, MappingStrategy.JAVA_TO_NATIVE, accept, params);
    MethodInvocation fromCall =
        assertCastCall(unit, j2n.adaptingArguments().get(0), nativeType, "fromJavaUtilList");
    assertThat(fromCall.getArguments()).hasSize(1);
    assertThat(fromCall.getArguments().get(0)).isInstanceOf(SimpleName.class);
  }

  public void testCreateReturnStatement_voidAndUnmappedNonVoid() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            public class Sample {
              private Adapter adapterRef;
              public void doWork() {}
              public PlainObj getPlain() { return null; }
              public SubPlainObj getSub() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "Adapter");
    ExecutableElement doWork = findMethod(unit, "doWork");
    ExecutableElement getPlain = findMethod(unit, "getPlain");
    ExecutableElement getSub = findMethod(unit, "getSub");

    Statement voidStmt =
        converter.createReturnStatement(
            adapter,
            MappingStrategy.NATIVE_TO_JAVA,
            doWork,
            doWork.getReturnType(),
            new MethodInvocation(new ExecutablePair(getSub), null),
            doWork.getReturnType());
    assertThat(voidStmt).isInstanceOf(ExpressionStatement.class);

    // Unmapped subtype returned as supertype omits redundant cast.
    Expression subCall = new MethodInvocation(new ExecutablePair(getSub), null);
    Statement subtypeStmt =
        converter.createReturnStatement(
            adapter,
            MappingStrategy.JAVA_TO_NATIVE,
            getPlain,
            getPlain.getReturnType(),
            subCall,
            getPlain.getReturnType());
    assertThat(subtypeStmt).isInstanceOf(ReturnStatement.class);
    assertThat(((ReturnStatement) subtypeStmt).getExpression()).isSameInstanceAs(subCall);

    // Unmapped Java return adapted to NativeType inserts cast to NativeType.
    NativeType plainNative = converter.calculateNativeType(getPlain.getReturnType(), getPlain);
    Expression plainCall = new MethodInvocation(new ExecutablePair(getPlain), null);
    Statement nativeStmt =
        converter.createReturnStatement(
            adapter,
            MappingStrategy.NATIVE_TO_JAVA,
            getPlain,
            getPlain.getReturnType(),
            plainCall,
            plainNative);
    assertThat(nativeStmt).isInstanceOf(ReturnStatement.class);
    assertCastCall(unit, ((ReturnStatement) nativeStmt).getExpression(), plainNative, "getPlain");
  }

  public void testCreateReturnStatement_collectionConversions() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              private WildcardAdapter wildcardRef;
              public List<String> echo(List<String> items) { return items; }
              public List<PlainObj> echoCustom(List<PlainObj> items) { return items; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "Adapter");
    TypeMirror wildcardAdapter = resolveType(unit, "WildcardAdapter");
    ExecutableElement echo = findMethod(unit, "echo");
    ExecutableElement echoCustom = findMethod(unit, "echoCustom");
    NativeType echoNative = converter.calculateNativeType(echo.getReturnType(), echo);

    Statement n2jStmt =
        converter.createReturnStatement(
            adapter,
            MappingStrategy.NATIVE_TO_JAVA,
            echo,
            echo.getReturnType(),
            new SimpleName(echo.getParameters().get(0)),
            echoNative);
    assertThat(n2jStmt).isInstanceOf(ReturnStatement.class);
    assertCastCall(
        unit, ((ReturnStatement) n2jStmt).getExpression(), echoNative, "fromJavaUtilList_String_");

    Expression nativeResult =
        new SimpleName(GeneratedVariableElement.newParameter("res", echoNative, echo));
    Statement j2nExactStmt =
        converter.createReturnStatement(
            adapter,
            MappingStrategy.JAVA_TO_NATIVE,
            echo,
            echo.getReturnType(),
            nativeResult,
            echo.getReturnType());
    assertThat(j2nExactStmt).isInstanceOf(ReturnStatement.class);
    assertDirectCall(((ReturnStatement) j2nExactStmt).getExpression(), "toJavaUtilList_String_");

    NativeType customNative = converter.calculateNativeType(echoCustom.getReturnType(), echoCustom);
    Expression customNativeResult =
        new SimpleName(GeneratedVariableElement.newParameter("res", customNative, echoCustom));
    Statement j2nWildcardStmt =
        converter.createReturnStatement(
            wildcardAdapter,
            MappingStrategy.JAVA_TO_NATIVE,
            echoCustom,
            echoCustom.getReturnType(),
            customNativeResult,
            echoCustom.getReturnType());
    assertThat(j2nWildcardStmt).isInstanceOf(ReturnStatement.class);
    assertCastCall(
        unit,
        ((ReturnStatement) j2nWildcardStmt).getExpression(),
        echoCustom.getReturnType(),
        "toJavaUtilList");
  }

  public void testComposingConverters_nativeToJavaMapWithPassthroughAndNestedList()
      throws IOException {
    addSourceFile(
        """
        import java.util.List;
        import java.util.Map;
        import java.util.function.Function;
        public class MapComposingAdapter {
          public static native <K, V> Map<K, V> toJavaUtilMap(
              Object map, Function<Object, K> kConv, Function<Object, V> vConv)
              /*-[ return nil; ]-*/;
          public static native List<String> toJavaUtilList_String_(Object list)
              /*-[ return nil; ]-*/;
        }
        """,
        "MapComposingAdapter.java");
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            import java.util.Map;
            public class Sample {
              private MapComposingAdapter adapterRef;
              public void accept(Map<PlainObj, List<String>> map) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());

    ParameterMapping mapping =
        converter.processParameters(
            resolveType(unit, "MapComposingAdapter"),
            MappingStrategy.NATIVE_TO_JAVA,
            accept,
            params);
    MethodInvocation outerCall =
        assertCastCall(
            unit, mapping.adaptingArguments().get(0), params.get(0).asType(), "toJavaUtilMap");
    assertThat(outerCall.getArguments()).hasSize(3);
    assertThat(outerCall.getArguments().get(1)).isInstanceOf(LambdaExpression.class);
    LambdaExpression keyLambda = (LambdaExpression) outerCall.getArguments().get(1);
    assertThat(keyLambda.getBody()).isInstanceOf(SimpleName.class);

    assertThat(outerCall.getArguments().get(2)).isInstanceOf(LambdaExpression.class);
    LambdaExpression valLambda = (LambdaExpression) outerCall.getArguments().get(2);
    assertThat(valLambda.getBody()).isInstanceOf(Expression.class);
    assertDirectCall((Expression) valLambda.getBody(), "toJavaUtilList_String_");
  }

  public void testComposingConverters_javaToNativeNestedList() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              public void accept(List<List<String>> nested, List<String> innerSample) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement accept = findMethod(unit, "accept");
    List<VariableElement> params = ImmutableList.copyOf(accept.getParameters());
    NativeType outerNative = converter.calculateNativeType(params.get(0).asType(), accept);
    NativeType innerNative = converter.calculateNativeType(params.get(1).asType(), accept);

    ParameterMapping mapping =
        converter.processParameters(
            resolveType(unit, "Adapter"), MappingStrategy.JAVA_TO_NATIVE, accept, params);
    MethodInvocation outerCall =
        assertCastCall(unit, mapping.adaptingArguments().get(0), outerNative, "fromJavaUtilList");
    assertThat(outerCall.getArguments()).hasSize(2);
    assertThat(outerCall.getArguments().get(1)).isInstanceOf(LambdaExpression.class);
    LambdaExpression innerLambda = (LambdaExpression) outerCall.getArguments().get(1);
    assertThat(innerLambda.getBody()).isInstanceOf(Expression.class);
    assertCastCall(
        unit, (Expression) innerLambda.getBody(), innerNative, "fromJavaUtilList_String_");
  }

  public void testProcessParameters_mappedTypeWithWildcardOnlyThrowsIllegalState() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private WildcardAdapter wildcardRef;
              public void take(List<Boolean> flags) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement take = findMethod(unit, "take");
    List<VariableElement> params = ImmutableList.copyOf(take.getParameters());

    IllegalStateException error =
        assertThrows(
            IllegalStateException.class,
            () ->
                converter.processParameters(
                    resolveType(unit, "WildcardAdapter"),
                    MappingStrategy.NATIVE_TO_JAVA,
                    take,
                    params));
    assertThat(error).hasMessageThat().contains("Exact converter required for mapped type");
  }

  public void testProcessParameters_missingCollectionConverterThrowsIllegalArgument() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.Set;
            public class Sample {
              private Adapter adapterRef;
              public void take(Set<PlainObj> items) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement take = findMethod(unit, "take");
    List<VariableElement> params = ImmutableList.copyOf(take.getParameters());

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                converter.processParameters(
                    resolveType(unit, "Adapter"), MappingStrategy.NATIVE_TO_JAVA, take, params));
    assertThat(error).hasMessageThat().contains("No converter method found in Adapter");
  }

  public void testProcessParameters_nonDeclaredAdapterType() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              public void takePlain(int invalidAdapter, PlainObj plain) {}
              public void takeList(List<String> items) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement takePlain = findMethod(unit, "takePlain");
    TypeMirror intAdapter = takePlain.getParameters().get(0).asType();
    List<VariableElement> plainParams = ImmutableList.of(takePlain.getParameters().get(1));

    // Unmapped parameter checks hasConverter, which safely returns false for non-DeclaredType.
    ParameterMapping passThrough =
        converter.processParameters(
            intAdapter, MappingStrategy.NATIVE_TO_JAVA, takePlain, plainParams);
    assertThat(passThrough.adaptingArguments().get(0)).isInstanceOf(SimpleName.class);

    // Mapped parameter requires conversion and rejects the non-DeclaredType adapter mirror.
    ExecutableElement takeList = findMethod(unit, "takeList");
    List<VariableElement> listParams = ImmutableList.copyOf(takeList.getParameters());
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                converter.processParameters(
                    intAdapter, MappingStrategy.NATIVE_TO_JAVA, takeList, listParams));
    assertThat(error).hasMessageThat().contains("No cannot find adapter int");
  }

  public void testProcessParameters_composingConverterWithoutSamThrowsAssertionError()
      throws IOException {
    addSourceFile(
        """
        import java.util.List;
        public class MarkerAdapter {
          public interface NotASam {}
          public static native <T> List<T> toJavaUtilList(
              Object list, NotASam marker) /*-[ return nil; ]-*/;
        }
        """,
        "MarkerAdapter.java");
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private MarkerAdapter adapterRef;
              public void take(List<List<String>> nested) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement take = findMethod(unit, "take");
    List<VariableElement> params = ImmutableList.copyOf(take.getParameters());

    AssertionError error =
        assertThrows(
            AssertionError.class,
            () ->
                converter.processParameters(
                    resolveType(unit, "MarkerAdapter"),
                    MappingStrategy.NATIVE_TO_JAVA,
                    take,
                    params));
    assertThat(error).hasMessageThat().contains("Cannot find abstract SAM method");
  }
}
