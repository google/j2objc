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
import com.google.devtools.j2objc.util.TypeUtil;
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

  private static final String INTEROP_ADAPTER_SOURCE =
      """
      import com.google.common.collect.ImmutableList;
      import com.google.common.collect.ImmutableMap;
      import java.util.List;
      import java.util.Map;
      import java.util.function.Function;

      public class InteropAdapter {
        public static native <T> List<T> toList(Object list) /*-[ return nil; ]-*/;
        public static native <T> Object fromList(List<T> list) /*-[ return nil; ]-*/;
        public static native <T> List<T> toList(
            Object list, Function<Object, T> conv) /*-[ return nil; ]-*/;
        public static native <T> Object fromList(
            List<T> list, Function<T, Object> conv) /*-[ return nil; ]-*/;
        public static native <T> ImmutableList<T> toImmutableList(
            Object list) /*-[ return nil; ]-*/;
        public static native <T> Object fromImmutableList(
            ImmutableList<T> list) /*-[ return nil; ]-*/;
        public static native ImmutableList<Integer> toImmutableListInteger(
            Object list) /*-[ return nil; ]-*/;
        public static native Object fromImmutableListInteger(
            ImmutableList<Integer> list) /*-[ return nil; ]-*/;
        public static native <K, V> Map<K, V> toMap(
            Object map, Function<Object, K> kConv, Function<Object, V> vConv) /*-[ return nil; ]-*/;
        public static native <K, V> Object fromMap(
            Map<K, V> map, Function<K, Object> kConv, Function<V, Object> vConv) /*-[
          return nil;
        ]-*/;
        public static native <T, U> ImmutableMap<T, ImmutableList<U>>
            toImmutableMapTImmutableList(Object map) /*-[ return nil; ]-*/;
        public static native <T, U> Object fromImmutableMapTImmutableList(
            ImmutableMap<T, ImmutableList<U>> map) /*-[ return nil; ]-*/;
        public static native <T> FutureBox<T> toFuture(FutureBox<T> box) /*-[ return nil; ]-*/;
        public static native <T> FutureBox<Object> fromFuture(
            FutureBox<T> box) /*-[ return nil; ]-*/;
        public static native FutureBox<Boolean> toBooleanFuture(
            FutureBox<Number> box) /*-[ return nil; ]-*/;
        public static native FutureBox<Number> fromBooleanFuture(
            FutureBox<Boolean> box) /*-[ return nil; ]-*/;
        public static native Boolean toBoolean(Object b) /*-[ return nil; ]-*/;
        public static native Object fromBoolean(Boolean b) /*-[ return nil; ]-*/;
        public static native Integer toInteger(Object i) /*-[ return nil; ]-*/;
        public static native Object fromInteger(Integer i) /*-[ return nil; ]-*/;
        public static native PlainObj toPlainObj(Object obj) /*-[ return nil; ]-*/;
        public static native Object fromPlainObj(PlainObj obj) /*-[ return nil; ]-*/;
        public static native SubPlainObj toSubPlainObj(Object obj) /*-[ return nil; ]-*/;
        public static native Object fromSubPlainObj(SubPlainObj obj) /*-[ return nil; ]-*/;
      }
      """;

  private static final String ASYMMETRIC_ADAPTER_SOURCE =
      """
      import java.util.List;
      import java.util.function.Function;

      public class AsymmetricAdapter {
        public static native PlainObj toPlainObj(Object obj) /*-[ return nil; ]-*/;
        public static native Object fromSubPlainObj(SubPlainObj obj) /*-[ return nil; ]-*/;
        public static native Object fromString(String s) /*-[ return nil; ]-*/;
        public static native Object fromBoolean(Boolean b) /*-[ return nil; ]-*/;
        public static native <T> List<T> toJavaUtilList(
            Object list, Function<Object, T> conv) /*-[ return nil; ]-*/;
        public static native <T> Object fromJavaUtilList(
            List<T> list, Function<T, Object> conv) /*-[ return nil; ]-*/;
      }
      """;

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.load(new String[] {"--objc-generics"});
    addSourceFile(ADAPTER_SOURCE, "Adapter.java");
    addSourceFile(WILDCARD_ADAPTER_SOURCE, "WildcardAdapter.java");
    addSourceFile(INTEROP_ADAPTER_SOURCE, "InteropAdapter.java");
    addSourceFile(ASYMMETRIC_ADAPTER_SOURCE, "AsymmetricAdapter.java");
    addSourceFile("public interface MyProto {}", "MyProto.java");
    addSourceFile("public class PlainObj {}", "PlainObj.java");
    addSourceFile("public class SubPlainObj extends PlainObj {}", "SubPlainObj.java");
    addSourceFile("public class FutureBox<T> {}", "FutureBox.java");
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

  public void testCalculateNativeType_typeVariablesAndBounds() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            import org.jspecify.annotations.Nullable;
            public class Sample<
                U, B extends PlainObj, P extends MyProto, S extends @Nullable String> {
              public U unbounded() { return null; }
              public @Nullable U nullableUnbounded() { return null; }
              public B classBounded() { return null; }
              public @Nullable B nullableClassBounded() { return null; }
              public @Nullable S nullableSupportedBounded() { return null; }
              public P interfaceBounded() { return null; }
              public List<U> unboundedList() { return null; }
              public List<B> classBoundedList() { return null; }
              public List<P> interfaceBoundedList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    ExecutableElement unbounded = findMethod(unit, "unbounded");
    NativeType unboundedNative =
        converter.calculateNativeType(unbounded.getReturnType(), unbounded);
    assertThat(unboundedNative.getName()).isEqualTo("id");
    assertThat(unboundedNative.getReferencedTypes()).isEmpty();

    assertThat(returnNativeName(unit, converter, "nullableUnbounded")).isEqualTo("id _Nullable");

    ExecutableElement classBounded = findMethod(unit, "classBounded");
    NativeType classBoundedNative =
        converter.calculateNativeType(classBounded.getReturnType(), classBounded);
    assertThat(classBoundedNative.getName()).isEqualTo("PlainObj *");
    assertThat(classBoundedNative.getReferencedTypes())
        .containsExactly(resolveType(unit, "PlainObj"));

    assertThat(returnNativeName(unit, converter, "nullableClassBounded"))
        .isEqualTo("PlainObj * _Nullable");
    assertThat(returnNativeName(unit, converter, "nullableSupportedBounded"))
        .isEqualTo("NSString * _Nullable");
    assertThat(returnNativeName(unit, converter, "interfaceBounded")).isEqualTo("id<MyProto>");

    assertThat(returnNativeName(unit, converter, "unboundedList")).isEqualTo("NSArray<id> *");
    ExecutableElement classBoundedList = findMethod(unit, "classBoundedList");
    NativeType classBoundedListNative =
        converter.calculateNativeType(classBoundedList.getReturnType(), classBoundedList);
    assertThat(classBoundedListNative.getName()).isEqualTo("NSArray<PlainObj *> *");
    assertThat(classBoundedListNative.getReferencedTypes())
        .contains(resolveType(unit, "PlainObj"));
    assertThat(returnNativeName(unit, converter, "interfaceBoundedList"))
        .isEqualTo("NSArray<id<MyProto>> *");
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

  private static boolean canReverseAdapt(
      CompilationUnit unit, KmpTypeConverter converter, String methodName, TypeMirror adapter) {
    return converter.canReverseAdapt(new ExecutablePair(findMethod(unit, methodName)), adapter);
  }

  public void testCanReverseAdapt_bidirectionalAndPassThroughTypes() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              public List<String> echo(List<String> items, String tag, int count, PlainObj plain) {
                return items;
              }
              public void acceptVoid(String tag, int count) {}
              public Boolean missingBoxed() { return Boolean.TRUE; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "Adapter");
    ExecutableElement echo = findMethod(unit, "echo");
    ExecutableElement acceptVoid = findMethod(unit, "acceptVoid");
    TypeMirror intAdapter = echo.getParameters().get(2).asType();

    assertThat(canReverseAdapt(unit, converter, "echo", adapter)).isTrue();
    assertThat(canReverseAdapt(unit, converter, "acceptVoid", adapter)).isTrue();
    assertThat(canReverseAdapt(unit, converter, "missingBoxed", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "echo", null)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "echo", intAdapter)).isFalse();
    assertThat(converter.canReverseAdaptReturnType(acceptVoid.getReturnType(), null)).isTrue();
    assertThat(converter.canReverseAdaptReturnType(echo.getReturnType(), null)).isFalse();
    assertThat(converter.canReverseAdaptReturnType(echo.getReturnType(), adapter)).isTrue();
  }

  public void testCanReverseAdapt_asymmetricCustomAndStringConvertersReturnFalse() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            public class Sample {
              private AsymmetricAdapter adapterRef;
              public void takeToOnly(PlainObj p) {}
              public PlainObj returnToOnly() { return null; }
              public void takeFromOnly(SubPlainObj p) {}
              public SubPlainObj returnFromOnly() { return null; }
              public void takeOneWayString(String s) {}
              public String returnOneWayString() { return ""; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "AsymmetricAdapter");

    assertThat(canReverseAdapt(unit, converter, "takeToOnly", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "returnToOnly", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "takeFromOnly", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "returnFromOnly", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "takeOneWayString", adapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "returnOneWayString", adapter)).isFalse();
  }

  public void testCanReverseAdapt_noneExactRequiredReturnsFalse() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private WildcardAdapter wildcardRef;
              public void takeBoolList(List<Boolean> flags) {}
              public List<Boolean> getBoolList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror wildcardAdapter = resolveType(unit, "WildcardAdapter");

    assertThat(canReverseAdapt(unit, converter, "takeBoolList", wildcardAdapter)).isFalse();
    assertThat(canReverseAdapt(unit, converter, "getBoolList", wildcardAdapter)).isFalse();
  }

  public void testCanReverseAdapt_composingConvertersRecurseIntoTypeArguments() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private Adapter adapterRef;
              private AsymmetricAdapter partialBoolRef;
              public List<List<String>> nested(List<List<String>> items) { return items; }
              public List<Boolean> boolList(List<Boolean> flags) { return flags; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);

    assertThat(canReverseAdapt(unit, converter, "nested", resolveType(unit, "Adapter"))).isTrue();
    assertThat(canReverseAdapt(unit, converter, "boolList", resolveType(unit, "AsymmetricAdapter")))
        .isFalse();
  }

  private static boolean canReverseAdaptSubclassReturn(
      CompilationUnit unit,
      KmpTypeConverter converter,
      String baseMethod,
      String subMethod,
      TypeMirror adapter) {
    return converter.canReverseAdaptSubclassReturn(
        findMethod(unit, baseMethod).getReturnType(),
        findMethod(unit, subMethod).getReturnType(),
        adapter);
  }

  public void testCanReverseAdaptSubclassReturn_voidPassThroughAndFoundationCollections() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.common.collect.ImmutableList;
            import java.util.List;
            import java.util.Map;
            public class Sample {
              private Adapter unmappedAdapterRef;
              private InteropAdapter adapterRef;
              public void voidBase() {}
              public void voidSub() {}
              public PlainObj plainBase() { return null; }
              public SubPlainObj subPlain() { return null; }
              public Object objectBase() { return null; }
              public String stringVal() { return ""; }
              public List<String> stringList() { return null; }
              public ImmutableList<String> immutableStringList() { return null; }
              public Map<String, String> stringMap() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "InteropAdapter");
    TypeMirror unmappedAdapter = resolveType(unit, "Adapter");

    assertThat(canReverseAdaptSubclassReturn(unit, converter, "voidBase", "voidSub", adapter))
        .isTrue();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "voidBase", "stringVal", adapter))
        .isFalse();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "stringVal", "voidSub", adapter))
        .isFalse();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "plainBase", "subPlain", null))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "plainBase", "subPlain", unmappedAdapter))
        .isTrue();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "plainBase", "stringVal", unmappedAdapter))
        .isFalse();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "objectBase", "stringList", adapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "stringList", "immutableStringList", adapter))
        .isTrue();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "stringList", "stringMap", adapter))
        .isFalse();
  }

  public void testCanReverseAdaptSubclassReturn_rejectsMismatchedElementConversions() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import java.util.List;
            public class Sample {
              private InteropAdapter interopAdapterRef;
              private Adapter specializedAdapterRef;
              public List<?> wildcardList() { return null; }
              public List<Object> objectList() { return null; }
              @SuppressWarnings("rawtypes")
              public List rawList() { return null; }
              public List<Boolean> boolList() { return null; }
              public List<List<String>> nestedStringList() { return null; }
              public List<PlainObj> plainList() { return null; }
              public List<String> stringList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror interopAdapter = resolveType(unit, "InteropAdapter");
    TypeMirror specializedAdapter = resolveType(unit, "Adapter");

    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardList", "boolList", interopAdapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "objectList", "boolList", interopAdapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(unit, converter, "rawList", "boolList", interopAdapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardList", "nestedStringList", interopAdapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardList", "plainList", interopAdapter))
        .isTrue();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardList", "stringList", specializedAdapter))
        .isFalse();
  }

  public void testCanReverseAdaptSubclassReturn_rejectsComposingOrSpecializedVsDirectMismatch() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import java.util.List;
            import java.util.Map;
            public class Sample {
              private InteropAdapter adapterRef;
              public Map<Integer, ImmutableList<Integer>> intMap() { return null; }
              public ImmutableMap<Integer, ImmutableList<Integer>> immutableIntMap() {
                return null;
              }
              public Map<String, ImmutableList<Integer>> stringToIntListMap() { return null; }
              public ImmutableMap<String, ImmutableList<Integer>> immutableStringToIntListMap() {
                return null;
              }
              public Map<String, ImmutableList<String>> stringMap() { return null; }
              public ImmutableMap<String, ImmutableList<String>> immutableStringMap() {
                return null;
              }
              public List<Integer> intList() { return null; }
              public ImmutableList<Integer> immutableIntList() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "InteropAdapter");

    // Composing Map<Integer, ...> converts Integer keys via toInteger/fromInteger, whereas
    // direct ImmutableMap<T, ImmutableList<U>> passes keys through unconverted.
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "intMap", "immutableIntMap", adapter))
        .isFalse();
    // Composing Map<String, ImmutableList<Integer>> converts inner Integer elements via
    // toImmutableListInteger, whereas direct ImmutableMap<T, ImmutableList<U>> leaves U
    // unconverted.
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "stringToIntListMap", "immutableStringToIntListMap", adapter))
        .isFalse();
    // Direct List<Integer> skips Integer conversion, whereas specialized ImmutableList<Integer>
    // converts elements.
    assertThat(
            canReverseAdaptSubclassReturn(unit, converter, "intList", "immutableIntList", adapter))
        .isFalse();
    // Composing Map<String, ...> passes String through unconverted (no String converter), matching
    // direct ImmutableMap<T, ImmutableList<U>>.
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "stringMap", "immutableStringMap", adapter))
        .isTrue();
  }

  public void testCanReverseAdaptSubclassReturn_nonFoundationConvertersRequireCompatibleContract() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            public class Sample {
              private InteropAdapter adapterRef;
              public FutureBox<?> wildcardFuture() { return null; }
              public FutureBox<Boolean> boolFuture() { return null; }
              public FutureBox<String> stringFuture() { return null; }
              public PlainObj plainBase() { return null; }
              public SubPlainObj subPlain() { return null; }
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    TypeMirror adapter = resolveType(unit, "InteropAdapter");

    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardFuture", "boolFuture", adapter))
        .isFalse();
    assertThat(
            canReverseAdaptSubclassReturn(
                unit, converter, "wildcardFuture", "stringFuture", adapter))
        .isTrue();
    assertThat(canReverseAdaptSubclassReturn(unit, converter, "plainBase", "subPlain", adapter))
        .isFalse();
  }

  public void testMaybeCast_narrowsSupertypeAndSkipsObjectOrAssignable() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            public class Sample {
              public void inspect(PlainObj plain, SubPlainObj sub) {}
            }
            """);
    KmpTypeConverter converter = createConverter(unit);
    ExecutableElement inspect = findMethod(unit, "inspect");
    Expression plainExpr = new SimpleName(inspect.getParameters().get(0));
    Expression subExpr = new SimpleName(inspect.getParameters().get(1));
    TypeMirror plainType = plainExpr.getTypeMirror();
    TypeMirror subType = subExpr.getTypeMirror();
    TypeMirror objectType = unit.getEnv().typeUtil().getJavaObject().asType();

    Expression narrowed = converter.maybeCast(subType, plainExpr);
    assertThat(narrowed).isInstanceOf(CastExpression.class);
    assertThat(
            unit.getEnv()
                .typeUtil()
                .isSameType(((CastExpression) narrowed).getType().getTypeMirror(), subType))
        .isTrue();
    assertThat(converter.maybeCast(plainType, subExpr)).isSameInstanceAs(subExpr);
    assertThat(converter.maybeCast(objectType, subExpr)).isSameInstanceAs(subExpr);
    assertThat(converter.maybeCast(unit.getEnv().typeUtil().getVoid(), subExpr))
        .isSameInstanceAs(subExpr);
    assertThat(converter.maybeCast(TypeUtil.ID_TYPE, subExpr)).isSameInstanceAs(subExpr);
    assertThat(converter.maybeCast(TypeUtil.NULLABLE_ID_TYPE, subExpr)).isSameInstanceAs(subExpr);
  }

  public void testIsDeclaredAsNullableId() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import org.jspecify.annotations.Nullable;
            public class Sample<U, B extends PlainObj> {
              public Object nonNullObject() { return null; }
              public @Nullable Object nullableObject() { return null; }
              public U nonNullTypeVar() { return null; }
              public @Nullable U nullableTypeVar() { return null; }
              public @Nullable B nullableBoundedTypeVar() { return null; }
            }
            """);
    TypeUtil typeUtil = unit.getEnv().typeUtil();

    assertThat(typeUtil.isDeclaredAsNullableId(TypeUtil.NULLABLE_ID_TYPE)).isTrue();
    assertThat(typeUtil.isDeclaredAsNullableId(TypeUtil.ID_TYPE)).isFalse();
    assertThat(typeUtil.isDeclaredAsId(TypeUtil.NULLABLE_ID_TYPE)).isFalse();

    assertThat(typeUtil.isDeclaredAsNullableId(findMethod(unit, "nullableObject").getReturnType()))
        .isTrue();
    assertThat(typeUtil.isDeclaredAsNullableId(findMethod(unit, "nonNullObject").getReturnType()))
        .isFalse();
    assertThat(typeUtil.isDeclaredAsNullableId(findMethod(unit, "nullableTypeVar").getReturnType()))
        .isTrue();
    assertThat(typeUtil.isDeclaredAsNullableId(findMethod(unit, "nonNullTypeVar").getReturnType()))
        .isFalse();
    assertThat(
            typeUtil.isDeclaredAsNullableId(
                findMethod(unit, "nullableBoundedTypeVar").getReturnType()))
        .isFalse();
  }
}
