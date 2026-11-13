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

import static org.junit.Assert.assertThrows;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

public class ObjectiveCKmpMethodTranslatorTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.load(new String[] {"--objc-generics"});
    addSourceFile(
        """
        package com.google.common.collect;
        public class ImmutableList<E> extends java.util.AbstractList<E> {
          public static <E> ImmutableList<E> of() {
            return new ImmutableList<E>();
          }
          public E get(int index) {
            return null;
          }
          public int size() {
            return 0;
          }
        }
        """,
        "com/google/common/collect/ImmutableList.java");
    addSourceFile("public class Foo {}", "Foo.java");
    addSourceFile("public class Bar implements Baz {}", "Bar.java");
    addSourceFile("public interface Baz {}", "Baz.java");
    addSourceFile("public class Container<T> {}", "Container.java");
    addSourceFile(
        """
        import java.util.List;
        import java.util.Set;
        import java.util.Map;
        import java.util.function.Function;

        public class Adapter {
          public static native Integer toInteger(Object n) /*-[ return nil; ]-*/;
          public static native Object fromInteger(Integer n) /*-[ return nil; ]-*/;
          public static native Long toLong(Object n) /*-[ return nil; ]-*/;
          public static native Object fromLong(Long n) /*-[ return nil; ]-*/;
          public static native Double toDouble(Object n) /*-[ return nil; ]-*/;
          public static native Object fromDouble(Double n) /*-[ return nil; ]-*/;
          public static native Float toFloat(Object n) /*-[ return nil; ]-*/;
          public static native Object fromFloat(Float n) /*-[ return nil; ]-*/;
          public static native Boolean toBoolean(Object n) /*-[ return nil; ]-*/;
          public static native Object fromBoolean(Boolean n) /*-[ return nil; ]-*/;

          public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList(List<?> list) /*-[ return nil; ]-*/;
          public static native Set<?> toJavaUtilSet(Object set) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilSet(Set<?> set) /*-[ return nil; ]-*/;
          public static native Map<?, ?> toJavaUtilMap(Object map) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilMap(Map<?, ?> map) /*-[ return nil; ]-*/;

          public static native List<List<?>> toJavaUtilList_JavaUtilList_(Object list) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList_JavaUtilList_(List<List<?>> list) /*-[ return nil; ]-*/;
          public static native Map<?, List<?>> toJavaUtilMap_JavaUtilList_(Object map) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilMap_JavaUtilList_(Map<?, List<?>> map) /*-[ return nil; ]-*/;
          public static native Map<String, List<?>> toJavaUtilMap_String_JavaUtilList_(Object map) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilMap_String_JavaUtilList_(Map<String, List<?>> map) /*-[ return nil; ]-*/;

          public static native List<Foo> toJavaUtilList_Foo_(Object vector) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList_Foo_(List<Foo> vector) /*-[ return nil; ]-*/;
          public static native Map<String, Foo> toJavaUtilMap_String_Foo_(Object map) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilMap_String_Foo_(Map<String, Foo> map) /*-[ return nil; ]-*/;
          public static native Map<Foo, String> toJavaUtilMap_Foo_String_(Object map) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilMap_Foo_String_(Map<Foo, String> map) /*-[ return nil; ]-*/;

          public static native List<Integer> toJavaUtilList_Integer_(Object n) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList_Integer_(List<Integer> n) /*-[ return nil; ]-*/;

          public static native <T> List<T> toJavaUtilList(Object list, Function<Object, T> conv) /*-[ return nil; ]-*/;
          public static native <T> Object fromJavaUtilList(List<T> list, Function<T, Object> conv) /*-[ return nil; ]-*/;
          public static native <T> Set<T> toJavaUtilSet(Object set, Function<Object, T> conv) /*-[ return nil; ]-*/;
          public static native <T> Object fromJavaUtilSet(Set<T> set, Function<T, Object> conv) /*-[ return nil; ]-*/;
          public static native <K, V> Map<K, V> toJavaUtilMap(Object map, Function<Object, K> kConv, Function<Object, V> vConv) /*-[ return nil; ]-*/;
          public static native <K, V> Object fromJavaUtilMap(Map<K, V> map, Function<K, Object> kConv, Function<V, Object> vConv) /*-[ return nil; ]-*/;
          public static native <T, S> Container<T> toContainer(Container<S> c, Function<S, T> conv) /*-[ return nil; ]-*/;
          public static native <S, T> Container<T> fromContainer(Container<S> c, Function<S, T> conv) /*-[ return nil; ]-*/;
        }
        """,
        "Adapter.java");
  }

  public void testNoAnnotation() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class NoAnnotation {
          public boolean getFalse(List<String> list) {
            return false;
          }
        }
        """,
        "NoAnnotation.java");
    String unused = translateSourceFile("NoAnnotation", "NoAnnotation.h");
    assertNoErrors();
  }

  /**
   * Tests basic conversion of a method with @ObjectiveCKmpMethod.
   *
   * <p>This verifies that an adapter method is generated with native Objective-C types (NSArray
   * instead of List) and that it correctly calls the original Java method after conversion.
   */
  public void testBasicConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.ArrayList;

        public class Simple {
          @ObjectiveCKmpMethod(selector="setListWithNSArray:", adapter=Adapter.class)
          public void setList(List<String> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getListAsNArray", adapter=Adapter.class)
          public List<String> getList() {
            return new ArrayList<String>();
          }
        }
        """,
        "Simple.java");
    String testHeader = translateSourceFile("Simple", "Simple.h");
    assertInTranslation(testHeader, "- (void)setListWithNSArray:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (NSArray<NSString *> *)getListAsNArray;");

    // List<String> will use toJavaUtilList as it is correctly available in Adapter,
    // and toJavaUtilList_String_ is NOT available in Adapter.
    String testImplementation = translateSourceFile("Simple", "Simple.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setListWithNSArray:(NSArray<NSString *> *)list {
          [self setListWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSArray<NSString *> *)getListAsNArray {
          return (NSArray<NSString *> *) [Adapter fromJavaUtilListWithJavaUtilList:[self getList]];
        }
        """);
  }

  /** Tests swiftName attribute in @ObjectiveCKmpMethod for concrete methods. */
  public void testSwiftNameInObjectiveCKmpMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class SwiftNameTest {
          @ObjectiveCKmpMethod(
              selector = "setListWithNSArray:",
              adapter = Adapter.class,
              swiftName = "setList(array:)")
          public void setList(List<String> list) {}
        }
        """,
        "SwiftNameTest.java");

    String testHeader = translateSourceFile("SwiftNameTest", "SwiftNameTest.h");
    assertInTranslation(
        testHeader,
        "- (void)setListWithNSArray:(NSArray<NSString *> *)list NS_SWIFT_NAME(setList(array:));");
  }

  /**
   * Tests that an explicit swiftName in @ObjectiveCKmpMethod is used for the adapter method even
   * when @SwiftName is also present on the Java method to rename the original method.
   */
  public void testSwiftNameInObjectiveCKmpMethodWithMethodLevelSwiftName() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import java.util.List;

        @SwiftName
        public class SwiftNameConflictTest {
          @ObjectiveCKmpMethod(
              selector = "setListWithArray:",
              adapter = Adapter.class,
              swiftName = "setList(list:)")
          @SwiftName("setList(listWithList:)")
          public void setList(List<String> list) {}
        }
        """,
        "SwiftNameConflictTest.java");

    String testHeader = translateSourceFile("SwiftNameConflictTest", "SwiftNameConflictTest.h");
    assertInTranslation(
        testHeader,
        "- (void)setListWithJavaUtilList:(id<JavaUtilList>)list"
            + " NS_SWIFT_NAME(setList(listWithList:));");
    assertInTranslation(
        testHeader,
        "- (void)setListWithArray:(NSArray<NSString *> *)list NS_SWIFT_NAME(setList(list:));");
  }

  /**
   * Tests that a method-level @SwiftName only applies to the original Java method and does not
   * apply to the adapter method when swiftName is not specified in @ObjectiveCKmpMethod.
   */
  public void testMethodLevelSwiftNameOnlyAppliesToOriginalMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import java.util.List;

        public class MethodLevelSwiftNameOnlyOriginalTest {
          @ObjectiveCKmpMethod(
              selector = "getSummarizeItemsPromptWith:",
              adapter = Adapter.class)
          @SwiftName("getSummarizeItemsPrompt(items:)")
          public void getSummarizeItemsPrompt(List<String> items) {}
        }
        """,
        "MethodLevelSwiftNameOnlyOriginalTest.java");

    String testHeader =
        translateSourceFile(
            "MethodLevelSwiftNameOnlyOriginalTest", "MethodLevelSwiftNameOnlyOriginalTest.h");
    assertInTranslation(
        testHeader,
        "- (void)getSummarizeItemsPromptWithJavaUtilList:(id<JavaUtilList>)items"
            + " NS_SWIFT_NAME(getSummarizeItemsPrompt(items:));");
    assertInTranslation(
        testHeader, "- (void)getSummarizeItemsPromptWith:(NSArray<NSString *> *)items;");
  }

  /** Tests swiftName attribute in @ObjectiveCKmpMethod for abstract methods. */
  public void testSwiftNameInAbstractObjectiveCKmpMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public interface AbstractSwiftNameTest {
          @ObjectiveCKmpMethod(
              selector = "processListWithNSArray:",
              adapter = Adapter.class,
              swiftName = "processList(array:)")
          void processList(List<String> list);
        }
        """,
        "AbstractSwiftNameTest.java");

    String testHeader = translateSourceFile("AbstractSwiftNameTest", "AbstractSwiftNameTest.h");
    assertInTranslation(
        testHeader,
        "- (void)processListWithNSArray:(NSArray<NSString *> *)list"
            + " NS_SWIFT_NAME(processList(array:));");
  }

  /** Tests default swiftName in @ObjectiveCKmpMethod when class has @SwiftName annotation. */
  public void testDefaultSwiftNameInClassSwiftNameContext() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import java.util.List;
        import java.util.Map;

        @SwiftName("DefaultSwiftNameClass")
        public class DefaultSwiftNameTest {
          @ObjectiveCKmpMethod(
              selector = "calculateSignalDescriptionSignalArray:selfEmail:flagDictionary:",
              adapter = Adapter.class)
          public String calculateSignalDescription(
              List<String> signals, String selfEmail, Map<String, String> flagMap) {
            return "test";
          }
        }
        """,
        "DefaultSwiftNameTest.java");

    String testHeader = translateSourceFile("DefaultSwiftNameTest", "DefaultSwiftNameTest.h");
    assertInTranslation(
        testHeader,
        "- (NSString *)calculateSignalDescriptionSignalArray:(NSArray<NSString *> *)signals\n"
            + "                                          selfEmail:(NSString *)selfEmail\n"
            + "                                     flagDictionary:(NSDictionary<NSString *,"
            + " NSString *> *)flagMap"
            + " NS_SWIFT_NAME(calculateSignalDescriptionSignalArray(signals:selfEmail:flagMap:));");
  }

  /** Tests default swiftName in @ObjectiveCKmpMethod for abstract methods in @SwiftName context. */
  public void testDefaultSwiftNameInAbstractObjectiveCKmpMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import java.util.List;

        @SwiftName("AbstractDefaultSwiftNameInterface")
        public interface AbstractDefaultSwiftNameTest {
          @ObjectiveCKmpMethod(
              selector = "processListWithNSArray:",
              adapter = Adapter.class)
          void processList(List<String> list);
        }
        """,
        "AbstractDefaultSwiftNameTest.java");

    String testHeader =
        translateSourceFile("AbstractDefaultSwiftNameTest", "AbstractDefaultSwiftNameTest.h");
    assertInTranslation(
        testHeader,
        "- (void)processListWithNSArray:(NSArray<NSString *> *)list"
            + " NS_SWIFT_NAME(processListWithNSArray(list:));");
  }

  /** Tests conversion of Set types with @ObjectiveCKmpMethod. */
  public void testSetConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        import java.util.HashSet;

        public class SetTest {
          @ObjectiveCKmpMethod(selector="setNamesWithNSSet:", adapter=Adapter.class)
          public void setNames(Set<String> names) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getNamesAsNSSet", adapter=Adapter.class)
          public Set<String> getNames() {
            return new HashSet<String>();
          }
        }
        """,
        "SetTest.java");

    String testHeader = translateSourceFile("SetTest", "SetTest.h");
    assertInTranslation(testHeader, "- (void)setNamesWithNSSet:(NSSet<NSString *> *)names;");
    assertInTranslation(testHeader, "- (NSSet<NSString *> *)getNamesAsNSSet;");

    String testImplementation = translateSourceFile("SetTest", "SetTest.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setNamesWithNSSet:(NSSet<NSString *> *)names {
          [self setNamesWithJavaUtilSet:(id<JavaUtilSet>) [Adapter toJavaUtilSetWithId:names]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSSet<NSString *> *)getNamesAsNSSet {
          return (NSSet<NSString *> *) [Adapter fromJavaUtilSetWithJavaUtilSet:[self getNames]];
        }
        """);
  }

  /** Tests conversion of Map types with @ObjectiveCKmpMethod. */
  public void testMapConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Map;
        import java.util.HashMap;

        public class MapTest {
          @ObjectiveCKmpMethod(selector="getMetadataAsNSDictionary", adapter=Adapter.class)
          public Map<String, String> getMetadata() {
            return new HashMap<String, String>();
          }

          @ObjectiveCKmpMethod(selector="setMetadataWithNSDictionary:", adapter=Adapter.class)
          public void setMetadata(Map<String, String> metadata) {
            return;
          }
        }
        """,
        "MapTest.java");

    String testHeader = translateSourceFile("MapTest", "MapTest.h");
    assertInTranslation(
        testHeader, "- (NSDictionary<NSString *, NSString *> *)getMetadataAsNSDictionary;");
    assertInTranslation(
        testHeader,
        "- (void)setMetadataWithNSDictionary:(NSDictionary<NSString *, NSString *> *)metadata;");

    String testImplementation = translateSourceFile("MapTest", "MapTest.m");
    assertInTranslation(
        testImplementation,
        """
        - (NSDictionary<NSString *, NSString *> *)getMetadataAsNSDictionary {
          return (NSDictionary<NSString *, NSString *> *) [Adapter fromJavaUtilMapWithJavaUtilMap:[self getMetadata]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (void)setMetadataWithNSDictionary:(NSDictionary<NSString *, NSString *> *)metadata {
          [self setMetadataWithJavaUtilMap:(id<JavaUtilMap>) [Adapter toJavaUtilMapWithId:metadata]];
        }
        """);
  }

  /** Tests nested collection conversion with @ObjectiveCKmpMethod. */
  public void testNestedCollectionConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class Nested {
          @ObjectiveCKmpMethod(selector="setMatrixWithNSArray:", adapter=Adapter.class)
          public void setMatrix(List<List<String>> matrix) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getMatrixAsNArray", adapter=Adapter.class)
          public List<List<String>> getMatrix() {
            return null;
          }
        }
        """,
        "Nested.java");

    String testHeader = translateSourceFile("Nested", "Nested.h");
    assertInTranslation(
        testHeader, "- (void)setMatrixWithNSArray:(NSArray<NSArray<NSString *> *> *)matrix;");
    assertInTranslation(testHeader, "- (NSArray<NSArray<NSString *> *> *)getMatrixAsNArray;");

    String testImplementation = translateSourceFile("Nested", "Nested.m");
    assertInTranslation(
        testImplementation,
        "[self setMatrixWithJavaUtilList:(id<JavaUtilList>) [Adapter"
            + " toJavaUtilList_JavaUtilList_WithId:matrix]]");
    assertInTranslation(
        testImplementation,
        "return (NSArray<NSArray<NSString *> *> *) [Adapter"
            + " fromJavaUtilList_JavaUtilList_WithJavaUtilList:(id<JavaUtilList>) [self"
            + " getMatrix]]");
  }

  /** Tests mixed nested collection conversion with @ObjectiveCKmpMethod. */
  public void testMixedNestedCollectionConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class Mixed {
          @ObjectiveCKmpMethod(selector="setGroups:", adapter=Adapter.class)
          public void setGroups(Map<String, List<String>> groups) {
            return;
          }
        }
        """,
        "Mixed.java");

    String testHeader = translateSourceFile("Mixed", "Mixed.h");
    assertInTranslation(
        testHeader,
        "- (void)setGroups:(NSDictionary<NSString *, NSArray<NSString *> *>" + " *)groups;");

    String testImplementation = translateSourceFile("Mixed", "Mixed.m");
    assertInTranslation(
        testImplementation,
        "[self setGroupsWithJavaUtilMap:(id<JavaUtilMap>) [Adapter"
            + " toJavaUtilMap_String_JavaUtilList_WithId:groups]]");
  }

  /** Tests custom class in collection with @ObjectiveCKmpMethod. */
  public void testCustomClassInCollection() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class Custom {
          @ObjectiveCKmpMethod(selector="setFooWithNSArray:", adapter=Adapter.class)
          public void setFoos(List<Foo> foos) {
            return;
          }
        }
        """,
        "Custom.java");

    String testHeader = translateSourceFile("Custom", "Custom.h");
    assertInTranslation(testHeader, "- (void)setFooWithNSArray:(NSArray<Foo *> *)foos;");

    String testImplementation = translateSourceFile("Custom", "Custom.m");
    assertInTranslation(
        testImplementation,
        "[self setFoosWithJavaUtilList:[Adapter toJavaUtilList_Foo_WithId:foos]]");
  }

  /** Tests map argument order with custom class and @ObjectiveCKmpMethod. */
  public void testMapWithCustomClass() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Map;

        public class MapCustom {
          @ObjectiveCKmpMethod(selector="setMap1:", adapter=Adapter.class)
          public void setMap1(Map<String, Foo> map) {
            return;
          }

          @ObjectiveCKmpMethod(selector="setMap2:", adapter=Adapter.class)
          public void setMap2(Map<Foo, String> map) {
            return;
          }
        }
        """,
        "MapCustom.java");

    String testHeader = translateSourceFile("MapCustom", "MapCustom.h");
    assertInTranslation(testHeader, "- (void)setMap1:(NSDictionary<NSString *, Foo *> *)map;");
    assertInTranslation(testHeader, "- (void)setMap2:(NSDictionary<Foo *, NSString *> *)map;");

    String testImplementation = translateSourceFile("MapCustom", "MapCustom.m");
    assertInTranslation(
        testImplementation,
        "[self setMap1WithJavaUtilMap:[Adapter toJavaUtilMap_String_Foo_WithId:map]]");
    assertInTranslation(
        testImplementation,
        "[self setMap2WithJavaUtilMap:[Adapter toJavaUtilMap_Foo_String_WithId:map]]");
  }

  /** Tests fallback and intermediate conversion with @ObjectiveCKmpMethod. */
  public void testFallbackAndIntermediateConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class FallbackTest {
          @ObjectiveCKmpMethod(selector="setList:", adapter=Adapter.class)
          public void setList(List<Bar> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="setMap:", adapter=Adapter.class)
          public void setMap(Map<String, List<Bar>> map) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getList", adapter=Adapter.class)
          public List<Bar> getList() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getMap", adapter=Adapter.class)
          public Map<String, List<Bar>> getMap() {
            return null;
          }
        }
        """,
        "FallbackTest.java");

    String testHeader = translateSourceFile("FallbackTest", "FallbackTest.h");
    assertInTranslation(testHeader, "- (void)setList:(NSArray<Bar *> *)list;");
    assertInTranslation(
        testHeader, "- (void)setMap:(NSDictionary<NSString *, NSArray<Bar *> *> *)map;");
    assertInTranslation(testHeader, "- (NSArray<Bar *> *)getList;");
    assertInTranslation(testHeader, "- (NSDictionary<NSString *, NSArray<Bar *> *> *)getMap;");

    String testImplementation = translateSourceFile("FallbackTest", "FallbackTest.m");

    // List<Bar> should fall back to toJavaUtilList
    assertInTranslation(
        testImplementation,
        "[self setListWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]]");

    // Map<String, List<Bar>> should pick toJavaUtilMap_String_JavaUtilList_
    assertInTranslation(
        testImplementation,
        "[self setMapWithJavaUtilMap:(id<JavaUtilMap>) [Adapter"
            + " toJavaUtilMap_String_JavaUtilList_WithId:map]]");

    // List<Bar> should fall back to fromJavaUtilList
    assertInTranslation(
        testImplementation,
        """
        - (NSArray<Bar *> *)getList {
          return (NSArray<Bar *> *) [Adapter fromJavaUtilListWithJavaUtilList:[self getList]];
        }
        """);

    // Map<String, List<Bar>> should pick fromJavaUtilMap_String_JavaUtilList_
    assertInTranslation(
        testImplementation,
        """
        - (NSDictionary<NSString *, NSArray<Bar *> *> *)getMap {
          return (NSDictionary<NSString *, NSArray<Bar *> *> *) [Adapter fromJavaUtilMap_String_JavaUtilList_WithJavaUtilMap:(id<JavaUtilMap>) [self getMap]];
        }
        """);
  }

  /** Tests multiple parameters needing conversion with @ObjectiveCKmpMethod. */
  public void testMultipleParameters() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class MultiParam {
          @ObjectiveCKmpMethod(selector="setList:andMap:", adapter=Adapter.class)
          public void setListAndMap(List<String> list, Map<String, String> map) {
            return;
          }
        }
        """,
        "MultiParam.java");

    String testHeader = translateSourceFile("MultiParam", "MultiParam.h");
    assertInTranslation(
        testHeader,
        """
        - (void)setList:(NSArray<NSString *> *)list
                 andMap:(NSDictionary<NSString *, NSString *> *)map;\
        """);

    String testImplementation = translateSourceFile("MultiParam", "MultiParam.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setList:(NSArray<NSString *> *)list
                 andMap:(NSDictionary<NSString *, NSString *> *)map {
          [self setListAndMapWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list] withJavaUtilMap:(id<JavaUtilMap>) [Adapter toJavaUtilMapWithId:map]];
        }
        """);
  }

  /** Tests a mix of collection and simple parameters with @ObjectiveCKmpMethod. */
  public void testMixedParameters() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class MixedParams {
          @ObjectiveCKmpMethod(selector="setCount:items:", adapter=Adapter.class)
          public void setMixed(int count, List<String> items) {
            return;
          }
        }
        """,
        "MixedParams.java");

    String testHeader = translateSourceFile("MixedParams", "MixedParams.h");
    assertInTranslation(
        testHeader,
        """
        - (void)setCount:(int32_t)count
                   items:(NSArray<NSString *> *)items;
        """);

    String testImplementation = translateSourceFile("MixedParams", "MixedParams.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setCount:(int32_t)count
                   items:(NSArray<NSString *> *)items {
          [self setMixedWithInt:count withJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:items]];
        }
        """);
  }

  public void testPrimitiveReturnWithConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class PrimitiveReturn {
          @ObjectiveCKmpMethod(selector="checkElement:inList:", adapter=Adapter.class)
          public boolean checkElement(String element, List<String> list) {
            return true;
          }
        }
        """,
        "PrimitiveReturn.java");

    String testHeader = translateSourceFile("PrimitiveReturn", "PrimitiveReturn.h");
    assertInTranslation(
        testHeader,
        "- (BOOL)checkElement:(NSString *)element\n"
            + "              inList:(NSArray<NSString *> *)list;");
  }

  public void testAllPrimitiveReturns() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class PrimitiveReturns {
          @ObjectiveCKmpMethod(selector="returnBoolean:", adapter=Adapter.class)
          public boolean returnBoolean(List<String> list) { return true; }
          @ObjectiveCKmpMethod(selector="returnInt:", adapter=Adapter.class)
          public int returnInt(List<String> list) { return 1; }
          @ObjectiveCKmpMethod(selector="returnLong:", adapter=Adapter.class)
          public long returnLong(List<String> list) { return 1L; }
          @ObjectiveCKmpMethod(selector="returnFloat:", adapter=Adapter.class)
          public float returnFloat(List<String> list) { return 1.0f; }
          @ObjectiveCKmpMethod(selector="returnDouble:", adapter=Adapter.class)
          public double returnDouble(List<String> list) { return 1.0; }
          @ObjectiveCKmpMethod(selector="returnChar:", adapter=Adapter.class)
          public char returnChar(List<String> list) { return 'a'; }
          @ObjectiveCKmpMethod(selector="returnByte:", adapter=Adapter.class)
          public byte returnByte(List<String> list) { return 1; }
          @ObjectiveCKmpMethod(selector="returnShort:", adapter=Adapter.class)
          public short returnShort(List<String> list) { return 1; }

          @ObjectiveCKmpMethod(selector="staticReturnBoolean:", adapter=Adapter.class)
          public static boolean staticReturnBoolean(List<String> list) { return true; }
          @ObjectiveCKmpMethod(selector="staticReturnInt:", adapter=Adapter.class)
          public static int staticReturnInt(List<String> list) { return 1; }
        }
        """,
        "PrimitiveReturns.java");

    String testHeader = translateSourceFile("PrimitiveReturns", "PrimitiveReturns.h");
    assertInTranslation(testHeader, "- (BOOL)returnBoolean:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (int32_t)returnInt:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (int64_t)returnLong:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (float)returnFloat:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (double)returnDouble:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (unichar)returnChar:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (char)returnByte:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (int16_t)returnShort:(NSArray<NSString *> *)list;");

    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT BOOL PrimitiveReturns_staticReturnBoolean_(NSArray<NSString *> *list);");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT int32_t PrimitiveReturns_staticReturnInt_(NSArray<NSString *> *list);");
  }

  /** Tests multiple complex parameters needing conversion with @ObjectiveCKmpMethod. */
  public void testMultipleComplexParameters() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class MultiComplex {
          @ObjectiveCKmpMethod(selector="setMatrix:andGroups:", adapter=Adapter.class)
          public void setComplex(List<List<String>> matrix, Map<String, List<String>> groups) {
            return;
          }
        }
        """,
        "MultiComplex.java");

    String testHeader = translateSourceFile("MultiComplex", "MultiComplex.h");
    assertInTranslation(
        testHeader,
        """
        - (void)setMatrix:(NSArray<NSArray<NSString *> *> *)matrix
                andGroups:(NSDictionary<NSString *, NSArray<NSString *> *> *)groups;
        """);

    String testImplementation = translateSourceFile("MultiComplex", "MultiComplex.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setMatrix:(NSArray<NSArray<NSString *> *> *)matrix
                andGroups:(NSDictionary<NSString *, NSArray<NSString *> *> *)groups {
          [self setComplexWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilList_JavaUtilList_WithId:matrix] withJavaUtilMap:(id<JavaUtilMap>) [Adapter toJavaUtilMap_String_JavaUtilList_WithId:groups]];
        }
        """);
  }

  /** Tests conversion of numeric and boolean wrapper types with @ObjectiveCKmpMethod. */
  public void testWrapperConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class Wrappers {
          @ObjectiveCKmpMethod(selector="processInt:", adapter=Adapter.class)
          public Integer processInt(Integer i) { return i; }
          @ObjectiveCKmpMethod(selector="processLong:", adapter=Adapter.class)
          public Long processLong(Long l) { return l; }
          @ObjectiveCKmpMethod(selector="processDouble:", adapter=Adapter.class)
          public Double processDouble(Double d) { return d; }
          @ObjectiveCKmpMethod(selector="processFloat:", adapter=Adapter.class)
          public Float processFloat(Float f) { return f; }
          @ObjectiveCKmpMethod(selector="processBoolean:", adapter=Adapter.class)
          public Boolean processBoolean(Boolean b) { return b; }
        }
        """,
        "Wrappers.java");

    String testHeader = translateSourceFile("Wrappers", "Wrappers.h");
    assertInTranslation(testHeader, "- (NSNumber *)processInt:(NSNumber *)i;");
    assertInTranslation(testHeader, "- (NSNumber *)processLong:(NSNumber *)l;");
    assertInTranslation(testHeader, "- (NSNumber *)processDouble:(NSNumber *)d;");
    assertInTranslation(testHeader, "- (NSNumber *)processFloat:(NSNumber *)f;");
    assertInTranslation(testHeader, "- (NSNumber *)processBoolean:(NSNumber *)b;");

    String testImplementation = translateSourceFile("Wrappers", "Wrappers.m");

    assertInTranslation(
        testImplementation,
        """
        - (NSNumber *)processInt:(NSNumber *)i {
          return (NSNumber *) [Adapter fromIntegerWithJavaLangInteger:[self processIntWithJavaLangInteger:[Adapter toIntegerWithId:i]]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSNumber *)processLong:(NSNumber *)l {
          return (NSNumber *) [Adapter fromLongWithJavaLangLong:[self processLongWithJavaLangLong:[Adapter toLongWithId:l]]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSNumber *)processDouble:(NSNumber *)d {
          return (NSNumber *) [Adapter fromDoubleWithJavaLangDouble:[self processDoubleWithJavaLangDouble:[Adapter toDoubleWithId:d]]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSNumber *)processFloat:(NSNumber *)f {
          return (NSNumber *) [Adapter fromFloatWithJavaLangFloat:[self processFloatWithJavaLangFloat:[Adapter toFloatWithId:f]]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSNumber *)processBoolean:(NSNumber *)b {
          return (NSNumber *) [Adapter fromBooleanWithJavaLangBoolean:[self processBooleanWithJavaLangBoolean:[Adapter toBooleanWithId:b]]];
        }
        """);
  }

  public void testListIntegerConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.ArrayList;

        public class ListInteger {
          @ObjectiveCKmpMethod(selector="setIntegers:", adapter=Adapter.class)
          public void setIntegers(List<Integer> integers) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getIntegers", adapter=Adapter.class)
          public List<Integer> getIntegers() {
            return new ArrayList<Integer>();
          }
        }
        """,
        "ListInteger.java");
    String testHeader = translateSourceFile("ListInteger", "ListInteger.h");
    assertInTranslation(testHeader, "- (void)setIntegers:(NSArray<NSNumber *> *)integers;");
    assertInTranslation(testHeader, "- (NSArray<NSNumber *> *)getIntegers;");

    String testImplementation = translateSourceFile("ListInteger", "ListInteger.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setIntegers:(NSArray<NSNumber *> *)integers {
          [self setIntegersWithJavaUtilList:[Adapter toJavaUtilList_Integer_WithId:integers]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSArray<NSNumber *> *)getIntegers {
          return (NSArray<NSNumber *> *) [Adapter fromJavaUtilList_Integer_WithJavaUtilList:[self getIntegers]];
        }
        """);
  }

  /** Tests specific String conversion and fallback for String. */
  public void testStringSpecificConversion() throws IOException {
    addSourceFile(
        """
        import java.util.List;
        import com.google.j2objc.annotations.ObjectiveCName;

        public class StringAdapter {
          public static native List<?> toJavaUtilList_String_(Object list) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList_String_(List<?> list) /*-[ return nil; ]-*/;
          public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "StringAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class StringSpecific {
          @ObjectiveCKmpMethod(selector="setNames:", adapter=StringAdapter.class)
          public void setNames(List<String> names) {}

          @ObjectiveCKmpMethod(selector="getNames", adapter=StringAdapter.class)
          public List<String> getNames() { return null; }
        }
        """,
        "StringSpecific.java");

    String testHeader = translateSourceFile("StringSpecific", "StringSpecific.h");
    assertInTranslation(testHeader, "- (void)setNames:(NSArray<NSString *> *)names;");
    assertInTranslation(testHeader, "- (NSArray<NSString *> *)getNames;");

    String testImplementation = translateSourceFile("StringSpecific", "StringSpecific.m");
    assertInTranslation(
        testImplementation,
        "[self setNamesWithJavaUtilList:(id<JavaUtilList>) [StringAdapter"
            + " toJavaUtilList_String_WithId:names]]");
    assertInTranslation(
        testImplementation,
        "return (NSArray<NSString *> *) [StringAdapter"
            + " fromJavaUtilList_String_WithJavaUtilList:[self getNames]]");
  }

  /** Tests four-level nesting with @ObjectiveCKmpMethod. */
  public void testFourLevelNesting() throws IOException {
    addSourceFile(
        """
        import java.util.List;
        import java.util.Map;
        public class NestedAdapter {
          public static native List<Map<String, List<Foo>>> toMatrix(Object list);
        }
        """,
        "NestedAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class FourLevel {
          @ObjectiveCKmpMethod(selector="setMatrix:", adapter=NestedAdapter.class)
          public void setMatrix(List<Map<String, List<Foo>>> matrix) {}
        }
        """,
        "FourLevel.java");

    String testHeader = translateSourceFile("FourLevel", "FourLevel.h");
    assertInTranslation(
        testHeader,
        "- (void)setMatrix:(NSArray<NSDictionary<NSString *, NSArray<Foo *> *> *> *)matrix;");

    String testImplementation = translateSourceFile("FourLevel", "FourLevel.m");
    assertInTranslation(
        testImplementation,
        "[self setMatrixWithJavaUtilList:[NestedAdapter toMatrixWithId:matrix]]");
  }

  /**
   * Tests that it picks the most specific candidate available. This allows for custom adapter
   * implementations to handle parameterized types with collection types.
   */
  public void testIntermediateNesting() throws IOException {
    addSourceFile(
        """
        import java.util.List;
        import java.util.Map;
        public class IntermediateAdapter {
          public static native List<?> toGeneric(Object list) /*-[ return nil; ]-*/;
          public static native List<Map<String, List<?>>> toList_Map_String_List__(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "IntermediateAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;

        public class Intermediate {
          @ObjectiveCKmpMethod(selector="setMatrix:", adapter=IntermediateAdapter.class)
          public void setMatrix(List<Map<String, List<Foo>>> matrix) {}

          @ObjectiveCKmpMethod(selector="setList:", adapter=IntermediateAdapter.class)
          public void setList(List<Bar> list) {}
        }
        """,
        "Intermediate.java");

    String testHeader = translateSourceFile("Intermediate", "Intermediate.h");
    assertInTranslation(
        testHeader,
        "- (void)setMatrix:(NSArray<NSDictionary<NSString *, NSArray<Foo *> *> *> *)matrix;");
    assertInTranslation(testHeader, "- (void)setList:(NSArray<Bar *> *)list;");

    String testImplementation = translateSourceFile("Intermediate", "Intermediate.m");
    // Matches candidate List<Map<String, List<?>>>
    assertInTranslation(
        testImplementation,
        "[self setMatrixWithJavaUtilList:(id<JavaUtilList>) [IntermediateAdapter"
            + " toList_Map_String_List__WithId:matrix]]");
    // Matches candidate List<?>
    assertInTranslation(
        testImplementation,
        "[self setListWithJavaUtilList:(id<JavaUtilList>) [IntermediateAdapter"
            + " toGenericWithId:list]]");
  }

  public void testInterface() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface MyInterface {
          @ObjectiveCKmpMethod(selector="setItems:", adapter=ImmutableListAdapter.class)
          void setItems(ImmutableList<String> items);

          @ObjectiveCKmpMethod(selector="getItems", adapter=ImmutableListAdapter.class)
          ImmutableList<String> getItems();
        }
        """,
        "MyInterface.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ConcreteImpl implements MyInterface {
          @Override
          public void setItems(ImmutableList<String> items) {}

          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ConcreteImpl.java");

    String interfaceHeader = translateSourceFile("MyInterface", "MyInterface.h");
    assertInTranslation(interfaceHeader, "- (void)setItems:(NSArray<NSString *> *)items;");
    assertInTranslation(interfaceHeader, "- (NSArray<NSString *> *)getItems;");

    String concreteHeader = translateSourceFile("ConcreteImpl", "ConcreteImpl.h");
    assertInTranslation(concreteHeader, "- (void)setItems:(NSArray<NSString *> *)items;");
    assertInTranslation(concreteHeader, "- (NSArray<NSString *> *)getItems;");

    String concreteImpl = translateSourceFile("ConcreteImpl", "ConcreteImpl.m");
    assertInTranslation(
        concreteImpl,
        """
        - (void)setItems:(NSArray<NSString *> *)items {
          [self setItemsWithComGoogleCommonCollectImmutableList:[ImmutableListAdapter toImmutableListWithId:items]];
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (NSArray<NSString *> *)getItems {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);
  }

  public void testInterfaceDefaultMethod() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface InterfaceWithDefault {
          @ObjectiveCKmpMethod(selector="getItemsAsNsArray", adapter=ImmutableListAdapter.class)
          default ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "InterfaceWithDefault.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ConcreteClassOverrides implements InterfaceWithDefault {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ConcreteClassOverrides.java");
    addSourceFile(
        """
        public class ConcreteClassInherits implements InterfaceWithDefault {}
        """,
        "ConcreteClassInherits.java");

    String interfaceHeader = translateSourceFile("InterfaceWithDefault", "InterfaceWithDefault.h");
    assertInTranslation(
        interfaceHeader,
        """
        @protocol InterfaceWithDefault < JavaObject >

        - (NSArray<NSString *> *)getItemsAsNsArray;

        #if !defined(__swift__)
        @optional
        #endif

        - (ComGoogleCommonCollectImmutableList<NSString *> *)getItems;

        @end
        """);
    assertInTranslation(
        interfaceHeader,
        "FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *"
            + "InterfaceWithDefault_getItems_dispatch(id<InterfaceWithDefault> self);");

    String interfaceImpl = translateSourceFile("InterfaceWithDefault", "InterfaceWithDefault.m");
    assertInTranslation(
        interfaceImpl,
        """
        ComGoogleCommonCollectImmutableList *InterfaceWithDefault_getItems_dispatch(id<InterfaceWithDefault> self) {
          if ([self respondsToSelector:@selector(getItems)]) {
            return [self getItems];
          }
          else if ([self respondsToSelector:@selector(getItemsAsNsArray)]) {
            return [ImmutableListAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          else {
            return InterfaceWithDefault_getItems(self);
          }
        }
        """);

    // Translate ConcreteClassOverrides and verify it generates getItems adapter
    String overridesHeader =
        translateSourceFile("ConcreteClassOverrides", "ConcreteClassOverrides.h");
    assertInTranslation(overridesHeader, "- (NSArray<NSString *> *)getItemsAsNsArray;");
    String overridesImpl =
        translateSourceFile("ConcreteClassOverrides", "ConcreteClassOverrides.m");
    assertInTranslation(
        overridesImpl,
        """
        - (NSArray<NSString *> *)getItemsAsNsArray {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);

    // Translate ConcreteClassInherits and verify it generates getItems adapter
    String inheritsHeader = translateSourceFile("ConcreteClassInherits", "ConcreteClassInherits.h");
    assertInTranslation(inheritsHeader, "- (NSArray<NSString *> *)getItemsAsNsArray;");
    String inheritsImpl = translateSourceFile("ConcreteClassInherits", "ConcreteClassInherits.m");
    assertInTranslation(
        inheritsImpl,
        """
        - (NSArray<NSString *> *)getItemsAsNsArray {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);
  }

  public void testAbstractClass() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public abstract class AbstractClass {
          @ObjectiveCKmpMethod(selector="setItems:", adapter=ImmutableListAdapter.class)
          public abstract void setItems(ImmutableList<String> items);

          @ObjectiveCKmpMethod(selector="getItemsAsNSArray", adapter=ImmutableListAdapter.class)
          public abstract ImmutableList<String> getItems();
        }
        """,
        "AbstractClass.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ConcreteClass extends AbstractClass {
          @Override
          public void setItems(ImmutableList<String> items) {}

          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ConcreteClass.java");

    String abstractHeader = translateSourceFile("AbstractClass", "AbstractClass.h");
    assertInTranslation(abstractHeader, "- (void)setItems:(NSArray<NSString *> *)items;");
    assertInTranslation(abstractHeader, "- (NSArray<NSString *> *)getItemsAsNSArray;");
    assertInTranslation(
        abstractHeader,
        "- (void)setItemsWithComGoogleCommonCollectImmutableList:("
            + "ComGoogleCommonCollectImmutableList<NSString *> *)items;");
    assertInTranslation(
        abstractHeader, "- (ComGoogleCommonCollectImmutableList<NSString *> *)getItems;");

    String abstractImpl = translateSourceFile("AbstractClass", "AbstractClass.m");
    // Original method should call adapter
    assertInTranslation(
        abstractImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          [self setItems:(NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
        }
        """);
    assertInTranslation(
        abstractImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          return [ImmutableListAdapter toImmutableListWithId:[self getItemsAsNSArray]];
        }
        """);
    // Adapter methods should have default implementation throwing exception
    assertInTranslation(
        abstractImpl,
        """
        - (void)setItems:(NSArray<NSString *> *)items {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
        }
        """);
    assertInTranslation(
        abstractImpl,
        """
        - (NSArray<NSString *> *)getItemsAsNSArray {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
          return 0;
        }
        """);

    String concreteHeader = translateSourceFile("ConcreteClass", "ConcreteClass.h");

    assertInTranslation(concreteHeader, "- (void)setItems:(NSArray<NSString *> *)items;");
    assertInTranslation(concreteHeader, "- (NSArray<NSString *> *)getItemsAsNSArray;");
    assertInTranslation(
        concreteHeader,
        "- (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<NSString"
            + " *> *)items;");
    assertInTranslation(
        concreteHeader, "- (ComGoogleCommonCollectImmutableList<NSString *> *)getItems;");

    String concreteImpl = translateSourceFile("ConcreteClass", "ConcreteClass.m");
    // Adapter methods should call original methods
    assertInTranslation(
        concreteImpl,
        """
        - (void)setItems:(NSArray<NSString *> *)items {
          [self setItemsWithComGoogleCommonCollectImmutableList:[ImmutableListAdapter toImmutableListWithId:items]];
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (NSArray<NSString *> *)getItemsAsNSArray {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);
    // Original methods should include the override check for non-final concrete classes
    assertInTranslation(
        concreteImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (ConcreteClass_isOverride(self, @selector(setItems:))) {
            ConcreteClass_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteClass_isOverride(self, @selector(getItemsAsNSArray))) {
            return ConcreteClass_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
  }

  public void testAbstractMethodWithCustomClass() throws IOException {
    addSourceFile(
        """
        package my.pkg;
        public class CustomClass {
        }
        """,
        "my/pkg/CustomClass.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native <T> Object fromImmutableList(ImmutableList<T> list) /*-[ return nil; ]-*/;
          public static native <T> ImmutableList<T> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;
        import my.pkg.CustomClass;

        public abstract class AbstractClass {
          @ObjectiveCKmpMethod(selector="setItems:", adapter=ImmutableListAdapter.class)
          public abstract void setItems(ImmutableList<CustomClass> items);

          @ObjectiveCKmpMethod(selector="getItemsAsNSArray", adapter=ImmutableListAdapter.class)
          public abstract ImmutableList<CustomClass> getItems();
        }
        """,
        "AbstractClass.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import my.pkg.CustomClass;

        public class ConcreteClass extends AbstractClass {
          @Override
          public void setItems(ImmutableList<CustomClass> items) {}

          @Override
          public ImmutableList<CustomClass> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ConcreteClass.java");

    String abstractHeader = translateSourceFile("AbstractClass", "AbstractClass.h");
    assertInTranslation(abstractHeader, "- (void)setItems:(NSArray<MyPkgCustomClass *> *)items;");
    assertInTranslation(abstractHeader, "- (NSArray<MyPkgCustomClass *> *)getItemsAsNSArray;");
    assertInTranslation(
        abstractHeader,
        "- (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<MyPkgCustomClass"
            + " *> *)items;");
    assertInTranslation(
        abstractHeader, "- (ComGoogleCommonCollectImmutableList<MyPkgCustomClass *> *)getItems;");
    assertInTranslation(abstractHeader, "@class MyPkgCustomClass;");

    String abstractImpl = translateSourceFile("AbstractClass", "AbstractClass.m");
    // Original method should call adapter
    assertInTranslation(
        abstractImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          [self setItems:(NSArray<MyPkgCustomClass *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *) items]];
        }
        """);
    assertInTranslation(
        abstractImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          return (ComGoogleCommonCollectImmutableList *) [ImmutableListAdapter toImmutableListWithId:[self getItemsAsNSArray]];
        }
        """);
    // Adapter methods should have default implementation throwing exception
    assertInTranslation(
        abstractImpl,
        """
        - (void)setItems:(NSArray<MyPkgCustomClass *> *)items {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
        }
        """);
    assertInTranslation(
        abstractImpl,
        """
        - (NSArray<MyPkgCustomClass *> *)getItemsAsNSArray {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
          return 0;
        }
        """);

    String concreteHeader = translateSourceFile("ConcreteClass", "ConcreteClass.h");

    assertInTranslation(concreteHeader, "- (void)setItems:(NSArray<MyPkgCustomClass *> *)items;");
    assertInTranslation(concreteHeader, "- (NSArray<MyPkgCustomClass *> *)getItemsAsNSArray;");
    assertInTranslation(
        concreteHeader,
        "- (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<MyPkgCustomClass"
            + " *> *)items;");
    assertInTranslation(
        concreteHeader, "- (ComGoogleCommonCollectImmutableList<MyPkgCustomClass *> *)getItems;");

    String concreteImpl = translateSourceFile("ConcreteClass", "ConcreteClass.m");
    // Adapter methods should call original methods
    assertInTranslation(
        concreteImpl,
        """
        - (void)setItems:(NSArray<MyPkgCustomClass *> *)items {
          [self setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *) [ImmutableListAdapter toImmutableListWithId:items]];
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (NSArray<MyPkgCustomClass *> *)getItemsAsNSArray {
          return (NSArray<MyPkgCustomClass *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *) [self getItems]];
        }
        """);
    // Original methods should include the override check for non-final concrete classes
    assertInTranslation(
        concreteImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (ConcreteClass_isOverride(self, @selector(setItems:))) {
            ConcreteClass_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteClass_isOverride(self, @selector(getItemsAsNSArray))) {
            return ConcreteClass_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
  }

  public void testImmutableListIsConverted() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");

    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class ImmutableListTest {
          @ObjectiveCKmpMethod(selector="setListWithNSArray:", adapter=ImmutableListAdapter.class)
          public void setList(ImmutableList<String> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getListAsNSArray", adapter=ImmutableListAdapter.class)
          public ImmutableList<String> getList() {
            return null;
          }
        }
        """,
        "ImmutableListTest.java");

    String testHeader = translateSourceFile("ImmutableListTest", "ImmutableListTest.h");
    assertInTranslation(testHeader, "- (void)setListWithNSArray:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (NSArray<NSString *> *)getListAsNSArray;");

    String testImplementation = translateSourceFile("ImmutableListTest", "ImmutableListTest.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setListWithNSArray:(NSArray<NSString *> *)list {
          [self setListWithComGoogleCommonCollectImmutableList:[ImmutableListAdapter toImmutableListWithId:list]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSArray<NSString *> *)getListAsNSArray {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getList]];
        }
        """);
  }

  public void testNoMethodFoundThrowsException() throws IOException {
    addSourceFile(
        """
        public class EmptyAdapter {}
        """,
        "EmptyAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class NoMethod {
          @ObjectiveCKmpMethod(selector="foo:", adapter=EmptyAdapter.class)
          public void foo(List<String> list) {}
        }
        """,
        "NoMethod.java");

    Throwable e =
        assertThrows(Throwable.class, () -> translateSourceFile("NoMethod", "NoMethod.m"));
    String message = e.getMessage();
    assertTrue(message.contains("No converter method found"));
    assertTrue(message.contains("java.util.List<java.lang.String>"));
  }

  public void testNonCollectionFallback() throws IOException {
    addSourceFile(
        """
        public class FallbackAdapter {}
        """,
        "FallbackAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class FallbackTest {
          @ObjectiveCKmpMethod(selector="foo:", adapter=FallbackAdapter.class)
          public void foo(String s) {}
        }
        """,
        "FallbackTest.java");

    var unused = translateSourceFile("FallbackTest", "FallbackTest.m");
  }

  public void testNonCollectionFallbackWithStringReturn() throws IOException {
    addSourceFile(
        """
        public class FallbackAdapter {}
        """,
        "FallbackAdapter.java");

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class FallbackReturnTest {
          @ObjectiveCKmpMethod(selector="foo:", adapter=FallbackAdapter.class)
          public String foo(String s) { return s; }
        }
        """,
        "FallbackReturnTest.java");

    var unused = translateSourceFile("FallbackReturnTest", "FallbackReturnTest.m");
  }

  public void testStaticMethods() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.ArrayList;

        public class StaticTest {
          @ObjectiveCKmpMethod(selector="setStaticListWithNSArray:", adapter=Adapter.class)
          public static void setStaticList(List<String> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getStaticListAsNArray", adapter=Adapter.class)
          public static List<String> getStaticList() {
            return new ArrayList<String>();
          }
        }
        """,
        "StaticTest.java");
    String testHeader = translateSourceFile("StaticTest", "StaticTest.h");
    assertInTranslation(
        testHeader, "+ (void)setStaticListWithNSArray:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "+ (NSArray<NSString *> *)getStaticListAsNArray;");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT void StaticTest_setStaticListWithNSArray_(NSArray<NSString *> *list);");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT NSArray<NSString *> *StaticTest_getStaticListAsNArray(void);");

    String testImplementation = translateSourceFile("StaticTest", "StaticTest.m");
    assertInTranslation(
        testImplementation,
        """
        void StaticTest_setStaticListWithNSArray_(NSArray<NSString *> *list) {
          StaticTest_setStaticListWithJavaUtilList_((id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        NSArray<NSString *> *StaticTest_getStaticListAsNArray() {
          return (NSArray<NSString *> *) [Adapter fromJavaUtilListWithJavaUtilList:StaticTest_getStaticList()];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        + (void)setStaticListWithNSArray:(NSArray<NSString *> *)list {
          StaticTest_setStaticListWithJavaUtilList_((id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        + (NSArray<NSString *> *)getStaticListAsNArray {
          return (NSArray<NSString *> *) [Adapter fromJavaUtilListWithJavaUtilList:StaticTest_getStaticList()];
        }
        """);
  }

  public void testNoWrapperMethods() throws IOException {
    options.load(new String[] {"--no-wrapper-methods"});
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.ArrayList;

        public class NoWrapper {
          @ObjectiveCKmpMethod(selector="setListWithNSArray:", adapter=Adapter.class)
          public void setList(List<String> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getListAsNArray", adapter=Adapter.class)
          public List<String> getList() {
            return new ArrayList<String>();
          }

          @ObjectiveCKmpMethod(selector="setStaticListWithNSArray:", adapter=Adapter.class)
          public static void setStaticList(List<String> list) {
            return;
          }

          @ObjectiveCKmpMethod(selector="getStaticListAsNArray", adapter=Adapter.class)
          public static List<String> getStaticList() {
            return new ArrayList<String>();
          }
        }
        """,
        "NoWrapper.java");
    String testHeader = translateSourceFile("NoWrapper", "NoWrapper.h");
    assertInTranslation(testHeader, "- (void)setListWithNSArray:(NSArray<NSString *> *)list;");
    assertInTranslation(testHeader, "- (NSArray<NSString *> *)getListAsNArray;");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT void NoWrapper_setStaticListWithNSArray_(NSArray<NSString *> *list);");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT NSArray<NSString *> *NoWrapper_getStaticListAsNArray(void);");

    String testImplementation = translateSourceFile("NoWrapper", "NoWrapper.m");
    assertInTranslation(
        testImplementation,
        """
        - (void)setListWithNSArray:(NSArray<NSString *> *)list {
          [self setListWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        - (NSArray<NSString *> *)getListAsNArray {
          return (NSArray<NSString *> *) [Adapter fromJavaUtilListWithJavaUtilList:[self getList]];
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        void NoWrapper_setStaticListWithNSArray_(NSArray<NSString *> *list) {
          NoWrapper_setStaticListWithJavaUtilList_((id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        NSArray<NSString *> *NoWrapper_getStaticListAsNArray() {
          return (NSArray<NSString *> *) [Adapter fromJavaUtilListWithJavaUtilList:NoWrapper_getStaticList()];
        }
        """);
  }

  public void testConstructorConversion_wrapperMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class ConstTest {
          @ObjectiveCKmpMethod(selector="initWithList:", adapter=Adapter.class)
          public ConstTest(List<String> list) {
            super();
          }
        }
        """,
        "ConstTest.java");

    String testHeader = translateSourceFile("ConstTest", "ConstTest.h");
    assertInTranslation(testHeader, "- (instancetype)initWithList:(NSArray<NSString *> *)list;");

    String testImplementation = translateSourceFile("ConstTest", "ConstTest.m");
    assertInTranslation(
        testImplementation,
        """
        - (instancetype)initWithList:(NSArray<NSString *> *)list {
          ConstTest_initWithJavaUtilList_(self, (id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
          return self;
        }
        """);
  }

  public void testConstructorConversion_noWrapperMethod() throws IOException {
    options.load(new String[] {"--no-wrapper-methods"});
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class ConstTest {
          @ObjectiveCKmpMethod(selector="initWithList:", adapter=Adapter.class)
          public ConstTest(List<String> list) {
            super();
          }
        }
        """,
        "ConstTest.java");

    String testHeader = translateSourceFile("ConstTest", "ConstTest.h");
    assertNotInTranslation(testHeader, "- (instancetype)initWithList:(NSArray<NSString *> *)list;");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT void ConstTest_initWithList_(ConstTest *self_, NSArray<NSString *>"
            + " *list);");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT ConstTest *new_ConstTest_initWithList_(NSArray<NSString *> *list);");
    assertInTranslation(
        testHeader,
        "FOUNDATION_EXPORT ConstTest *create_ConstTest_initWithList_(NSArray<NSString *> *list);");

    String testImplementation = translateSourceFile("ConstTest", "ConstTest.m");
    assertNotInTranslation(
        testImplementation, "- (instancetype)initWithList:(NSArray<NSString *> *)list");
    assertInTranslation(
        testImplementation,
        """
        void ConstTest_initWithList_(ConstTest *self_, NSArray<NSString *> *list) {
          ConstTest_initWithJavaUtilList_(self_, (id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        ConstTest *new_ConstTest_initWithList_(NSArray<NSString *> *list) {
          return new_ConstTest_initWithJavaUtilList_((id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
    assertInTranslation(
        testImplementation,
        """
        ConstTest *create_ConstTest_initWithList_(NSArray<NSString *> *list) {
          return create_ConstTest_initWithJavaUtilList_((id<JavaUtilList>) [Adapter toJavaUtilListWithId:list]);
        }
        """);
  }

  public void testObjCGenerics() throws IOException {

    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class GenericsOn {
          @ObjectiveCKmpMethod(selector="setList:", adapter=Adapter.class)
          public void setList(List<String> list) {}
        }
        """,
        "GenericsOn.java");
    String headerOn = translateSourceFile("GenericsOn", "GenericsOn.h");
    assertInTranslation(headerOn, "- (void)setList:(NSArray<NSString *> *)list;");

    options.setAsObjCGenericDecl(false);
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class GenericsOff {
          @ObjectiveCKmpMethod(selector="setList:", adapter=Adapter.class)
          public void setList(List<String> list) {}
        }
        """,
        "GenericsOff.java");
    String headerOff = translateSourceFile("GenericsOff", "GenericsOff.h");
    assertInTranslation(headerOff, "- (void)setList:(NSArray *)list;");
    options.setAsObjCGenericDecl(true);
  }

  public void testTranslate_setOfBoolean() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        public class TestSetOfBoolean {
          @ObjectiveCKmpMethod(selector="setBooleanSet:", adapter=Adapter.class)
          public void setBooleanSet(Set<Boolean> set) {}
        }
        """,
        "TestSetOfBoolean.java");

    String header = translateSourceFile("TestSetOfBoolean", "TestSetOfBoolean.h");
    assertInTranslation(header, "- (void)setBooleanSet:(NSSet<NSNumber *> *)set;");
    String impl = translateSourceFile("TestSetOfBoolean", "TestSetOfBoolean.m");
    assertInTranslation(
        impl,
        """
        - (void)setBooleanSet:(NSSet<NSNumber *> *)set {
          [self setBooleanSetWithJavaUtilSet:(id<JavaUtilSet>) [Adapter toJavaUtilSetWithId:set withJavaUtilFunctionFunction:JreLoadStatic(TestSetOfBoolean_$KmpLambda$1, instance)]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id)applyWithId:(id)elem {
          return Adapter_toBooleanWithId_(elem);
        }
        """);
    assertInTranslation(
        impl,
        """
        + (void)initialize {
          if (self == [TestSetOfBoolean_$KmpLambda$1 class]) {
            JreStrongAssignAndConsume(&TestSetOfBoolean_$KmpLambda$1_instance, new_TestSetOfBoolean_$KmpLambda$1_init());
            J2OBJC_SET_INITIALIZED(TestSetOfBoolean_$KmpLambda$1)
          }
        }
        """);
  }

  public void testTranslate_composableReturn_retainsSynthesizedLambdaSingleton()
      throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        public class TestReturnSetOfBoolean {
          @ObjectiveCKmpMethod(selector="getBooleanSet", adapter=Adapter.class)
          public Set<Boolean> getBooleanSet() { return null; }
        }
        """,
        "TestReturnSetOfBoolean.java");

    String impl = translateSourceFile("TestReturnSetOfBoolean", "TestReturnSetOfBoolean.m");
    assertInTranslation(
        impl,
        """
        + (void)initialize {
          if (self == [TestReturnSetOfBoolean_$KmpLambda$1 class]) {
            JreStrongAssignAndConsume(&TestReturnSetOfBoolean_$KmpLambda$1_instance, new_TestReturnSetOfBoolean_$KmpLambda$1_init());
            J2OBJC_SET_INITIALIZED(TestReturnSetOfBoolean_$KmpLambda$1)
          }
        }
        """);
  }

  public void testTranslate_parameterizedSet_leafElementConversion() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        public class TestSetLeaf {
          @ObjectiveCKmpMethod(selector="setBooleanSet:", adapter=Adapter.class)
          public void setBooleanSet(Set<Boolean> set) {}
        }
        """,
        "TestSetLeaf.java");

    String header = translateSourceFile("TestSetLeaf", "TestSetLeaf.h");
    assertInTranslation(header, "- (void)setBooleanSet:(NSSet<NSNumber *> *)set;");
    String impl = translateSourceFile("TestSetLeaf", "TestSetLeaf.m");
    assertInTranslation(
        impl,
        """
        - (void)setBooleanSet:(NSSet<NSNumber *> *)set {
          [self setBooleanSetWithJavaUtilSet:(id<JavaUtilSet>) [Adapter toJavaUtilSetWithId:set withJavaUtilFunctionFunction:JreLoadStatic(TestSetLeaf_$KmpLambda$1, instance)]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id)applyWithId:(id)elem {
          return Adapter_toBooleanWithId_(elem);
        }
        """);
  }

  public void testTranslate_parameterizedList_lambdaParameterType() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class TestListParam {
          @ObjectiveCKmpMethod(selector="setBooleanList:", adapter=Adapter.class)
          public void setBooleanList(List<Boolean> list) {}
        }
        """,
        "TestListParam.java");

    String header = translateSourceFile("TestListParam", "TestListParam.h");
    assertInTranslation(header, "- (void)setBooleanList:(NSArray<NSNumber *> *)list;");
    String impl = translateSourceFile("TestListParam", "TestListParam.m");
    assertInTranslation(
        impl,
        """
        - (void)setBooleanList:(NSArray<NSNumber *> *)list {
          [self setBooleanListWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list withJavaUtilFunctionFunction:JreLoadStatic(TestListParam_$KmpLambda$1, instance)]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id)applyWithId:(id)elem {
          return Adapter_toBooleanWithId_(elem);
        }
        """);
  }

  public void testTranslate_setOfList() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        import java.util.List;
        public class TestSetOfList {
          @ObjectiveCKmpMethod(selector="setListSet:", adapter=Adapter.class)
          public void setListSet(Set<List<String>> set) {}
        }
        """,
        "TestSetOfList.java");

    String header = translateSourceFile("TestSetOfList", "TestSetOfList.h");
    assertInTranslation(header, "- (void)setListSet:(NSSet<NSArray<NSString *> *> *)set;");
    String impl = translateSourceFile("TestSetOfList", "TestSetOfList.m");
    assertInTranslation(
        impl,
        """
        - (void)setListSet:(NSSet<NSArray<NSString *> *> *)set {
          [self setListSetWithJavaUtilSet:(id<JavaUtilSet>) [Adapter toJavaUtilSetWithId:set withJavaUtilFunctionFunction:JreLoadStatic(TestSetOfList_$KmpLambda$1, instance)]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id)applyWithId:(id)elem {
          return (id<JavaUtilList>) Adapter_toJavaUtilListWithId_(elem);
        }
        """);
  }

  public void testInterfaceTypes() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Set;
        import java.util.List;
        public class InterfaceUsage {
          @ObjectiveCKmpMethod(selector="getInterface", adapter=Adapter.class)
          public Baz getInterface() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="setInterface:", adapter=Adapter.class)
          public void setInterface(Baz value) {
            return;
          }
        }
        """,
        "InterfaceUsage.java");

    String header = translateSourceFile("InterfaceUsage", "InterfaceUsage.h");
    assertInTranslation(
        header,
        """
        - (id<Baz>)getInterface;
        """);
    assertInTranslation(
        header,
        """
        - (void)setInterface:(id<Baz>)value;
        """);
  }

  public void testListOfInterface() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class ListOfInterface {
          @ObjectiveCKmpMethod(selector="getListOfInterface", adapter=Adapter.class)
          public List<Baz> getListOfInterface() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="setListOfInterface:", adapter=Adapter.class)
          public void setListOfInterface(List<Baz> list) {
            return;
          }
        }
        """,
        "ListOfInterface.java");

    String header = translateSourceFile("ListOfInterface", "ListOfInterface.h");
    assertInTranslation(
        header,
        """
        - (NSArray<id<Baz>> *)getListOfInterface;
        """);
    assertInTranslation(
        header,
        """
        - (void)setListOfInterface:(NSArray<id<Baz>> *)list;
        """);
  }

  public void testNullableTypes() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import java.util.Map;
        import com.google.common.collect.ImmutableList;
        import com.google.common.collect.ImmutableMap;
        import org.jspecify.annotations.Nullable;

        public class NullableTypes {
          @ObjectiveCKmpMethod(selector="getNullableInterface", adapter=Adapter.class)
          public @Nullable Baz getNullableInterface() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getNullableClass", adapter=Adapter.class)
          public @Nullable Bar getNullableClass() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getListOfNullableInterface", adapter=Adapter.class)
          public List<@Nullable Baz> getListOfNullableInterface() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector="getListOfNullableClass", adapter=Adapter.class)
          public List<@Nullable Bar> getListOfNullableClass() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector="getNullableListOfInterface", adapter=Adapter.class)
          public @Nullable List<Baz> getNullableListOfInterface() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getNullableListOfClass", adapter=Adapter.class)
          public @Nullable List<Bar> getNullableListOfClass() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getMapOfNullableInterface", adapter=Adapter.class)
          public Map<String, @Nullable Baz> getMapOfNullableInterface() {
            return ImmutableMap.of();
          }

          @ObjectiveCKmpMethod(selector="getMapOfNullableClass", adapter=Adapter.class)
          public Map<String, @Nullable Bar> getMapOfNullableClass() {
            return ImmutableMap.of();
          }

          @ObjectiveCKmpMethod(selector="getNullableMapOfInterface", adapter=Adapter.class)
          public @Nullable Map<String, Baz> getNullableMapOfInterface() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="getNullableMapOfClass", adapter=Adapter.class)
          public @Nullable Map<String, Bar> getNullableMapOfClass() {
            return null;
          }

          @ObjectiveCKmpMethod(selector="setWithNullableBar:withNullableBaz:withNullableList:withNullableMap:", adapter=Adapter.class)
          public @Nullable Foo set(@Nullable Bar bar, @Nullable Baz baz, @Nullable List<@Nullable Bar> list,
              @Nullable Map<String, @Nullable Baz> map) {
            return null;
          }
        }
        """,
        "NullableTypes.java");

    String header = translateSourceFile("NullableTypes", "NullableTypes.h");
    assertInTranslation(
        header,
        """
        - (id<Baz> _Nullable)getNullableInterface;
        """);
    assertInTranslation(
        header,
        """
        - (Bar * _Nullable)getNullableClass;
        """);
    assertInTranslation(
        header,
        """
        - (NSArray<id<Baz> _Nullable> *)getListOfNullableInterface;
        """);
    assertInTranslation(
        header,
        """
        - (NSArray<Bar * _Nullable> *)getListOfNullableClass;
        """);
    assertInTranslation(
        header,
        """
        - (NSArray<id<Baz>> * _Nullable)getNullableListOfInterface;
        """);
    assertInTranslation(
        header,
        """
        - (NSArray<Bar *> * _Nullable)getNullableListOfClass;
        """);
    assertInTranslation(
        header,
        """
        - (NSDictionary<NSString *, id<Baz> _Nullable> *)getMapOfNullableInterface;
        """);
    assertInTranslation(
        header,
        """
        - (NSDictionary<NSString *, Bar * _Nullable> *)getMapOfNullableClass;
        """);
    assertInTranslation(
        header,
        """
        - (NSDictionary<NSString *, id<Baz>> * _Nullable)getNullableMapOfInterface;
        """);
    assertInTranslation(
        header,
        """
        - (NSDictionary<NSString *, Bar *> * _Nullable)getNullableMapOfClass;
        """);
    assertInTranslation(
        header,
        """
        - (Foo * _Nullable)setWithNullableBar:(Bar * _Nullable)bar
                              withNullableBaz:(id<Baz> _Nullable)baz
                             withNullableList:(NSArray<Bar * _Nullable> * _Nullable)list
                              withNullableMap:(NSDictionary<NSString *, id<Baz> _Nullable> * _Nullable)map;
        """);
  }

  public void testVoidNullability() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import org.jspecify.annotations.Nullable;

        public class VoidNullability {
          @ObjectiveCKmpMethod(selector="doSomethingWithVoid:", adapter=Adapter.class)
          public void doSomething(@Nullable Void v) {}

          @ObjectiveCKmpMethod(selector="getVoid", adapter=Adapter.class)
          public @Nullable Void getVoid() {
            return null;
          }
        }
        """,
        "VoidNullability.java");

    String header = translateSourceFile("VoidNullability", "VoidNullability.h");
    assertInTranslation(header, "- (void)doSomethingWithVoid:(JavaLangVoid *)v;");
    assertInTranslation(header, "- (JavaLangVoid *)getVoid;");
  }

  public void testUntranslatedInterfaceGenerics() throws IOException {
    addSourceFile("public interface GenericInterface<T> {}", "GenericInterface.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class InterfaceGenerics {
          @ObjectiveCKmpMethod(selector="doSomethingWithInterface:", adapter=Adapter.class)
          public void doSomething(GenericInterface<String> i) {}
        }
        """,
        "InterfaceGenerics.java");

    String header = translateSourceFile("InterfaceGenerics", "InterfaceGenerics.h");
    assertInTranslation(header, "- (void)doSomethingWithInterface:(id<GenericInterface>)i;");
  }

  public void testTranslate_nestedListParameter() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class TestClass {
          @ObjectiveCKmpMethod(selector = "verifyList:", adapter = Adapter.class)
          public void verifyList(List<List<List<Foo>>> list) {}
        }
        """,
        "TestClass.java");
    String impl = translateSourceFile("TestClass", "TestClass.m");
    assertInTranslation(
        impl,
        """
        - (void)verifyList:(NSArray<NSArray<NSArray<Foo *> *> *> *)list {
          [self verifyListWithJavaUtilList:(id<JavaUtilList>) [Adapter toJavaUtilListWithId:list \
        withJavaUtilFunctionFunction:JreLoadStatic(TestClass_$KmpLambda$1, instance)]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id)applyWithId:(id)elem {
          return (id<JavaUtilList>) Adapter_toJavaUtilList_JavaUtilList_WithId_(elem);
        }
        """);
  }

  public void testTranslate_genericContainerWithNullableNumberReturn() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import org.jspecify.annotations.Nullable;
        public class TestClass {
          @ObjectiveCKmpMethod(selector = "getNsNumberContainer", adapter = Adapter.class)
          public Container<@Nullable Float> getFloatContainer() {
            return null;
          }
        }
        """,
        "TestClass.java");

    String header = translateSourceFile("TestClass", "TestClass.h");
    assertInTranslation(header, "- (Container<NSNumber *> *)getNsNumberContainer;");

    String impl = translateSourceFile("TestClass", "TestClass.m");
    assertInTranslation(
        impl,
        """
        - (Container<NSNumber *> *)getNsNumberContainer {
          return (Container<NSNumber *> *) [Adapter fromContainerWithContainer:(Container *) \
        [self getFloatContainer] withJavaUtilFunctionFunction:JreLoadStatic(TestClass_$KmpLambda$1, instance)];
        }
        """);
  }

  public void testTranslate_genericContainerWithNullableNumberParameter() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import org.jspecify.annotations.Nullable;
        public class TestClass {
          @ObjectiveCKmpMethod(selector = "setNsNumberContainer:", adapter = Adapter.class)
          public void setFloatContainer(Container<@Nullable Float> c) {}
        }
        """,
        "TestClass.java");

    String header = translateSourceFile("TestClass", "TestClass.h");
    assertInTranslation(header, "- (void)setNsNumberContainer:(Container<NSNumber *> *)c;");

    String impl = translateSourceFile("TestClass", "TestClass.m");
    assertInTranslation(
        impl,
        """
        - (void)setNsNumberContainer:(Container<NSNumber *> *)c {
          [self setFloatContainerWithContainer:(Container *) [Adapter \
        toContainerWithContainer:(Container *) c \
        withJavaUtilFunctionFunction:JreLoadStatic(TestClass_$KmpLambda$1, instance)]];
        }
        """);
  }

  public void testTranslate_genericContainerWithBooleanParameter() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        public class TestClass {
          @ObjectiveCKmpMethod(selector = "setBooleanContainer:", adapter = Adapter.class)
          public void setBooleanContainer(Container<Boolean> c) {}
        }
        """,
        "TestClass.java");

    String header = translateSourceFile("TestClass", "TestClass.h");
    assertInTranslation(header, "- (void)setBooleanContainer:(Container<NSNumber *> *)c;");

    String impl = translateSourceFile("TestClass", "TestClass.m");
    assertInTranslation(
        impl,
        """
        - (void)setBooleanContainer:(Container<NSNumber *> *)c {
          [self setBooleanContainerWithContainer:(Container *) [Adapter \
        toContainerWithContainer:(Container *) c \
        withJavaUtilFunctionFunction:JreLoadStatic(TestClass_$KmpLambda$1, instance)]];
        }
        """);
  }

  public void testTranslate_nonNullParameterizedContainerParameter() throws IOException {
    addSourceFile("public class CustomFuture<T> {}", "CustomFuture.java");
    addSourceFile("public class UnadaptedFlow<T> {}", "UnadaptedFlow.java");
    addSourceFile(
        """
        public class FutureAdapter {
          public static native CustomFuture<Boolean> toBooleanFuture(CustomFuture<Number> future)
              /*-[ return nil; ]-*/;
          public static native CustomFuture<Number> fromBooleanFuture(CustomFuture<Boolean> future)
              /*-[ return nil; ]-*/;
        }
        """,
        "FutureAdapter.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import org.jspecify.annotations.NullMarked;
        import org.jspecify.annotations.Nullable;

        @NullMarked
        public class FutureTestClass {
          @ObjectiveCKmpMethod(selector = "echoBooleanFuture:", adapter = FutureAdapter.class)
          public @Nullable CustomFuture<Boolean> echoBooleanFuture(
              CustomFuture<Boolean> future) {
            return future;
          }

          @ObjectiveCKmpMethod(selector = "processFlow:", adapter = FutureAdapter.class)
          public void processFlow(UnadaptedFlow<ImmutableList<Foo>> flow) {}
        }
        """,
        "FutureTestClass.java");

    String header = translateSourceFile("FutureTestClass", "FutureTestClass.h");
    assertInTranslation(
        header,
        "- (CustomFuture<NSNumber *> * _Nullable)echoBooleanFuture:"
            + "(CustomFuture<NSNumber *> *)future;");
    assertInTranslation(
        header,
        "- (void)processFlow:(UnadaptedFlow<ComGoogleCommonCollectImmutableList<Foo *> *> *)flow;");

    String impl = translateSourceFile("FutureTestClass", "FutureTestClass.m");
    assertInTranslation(
        impl,
        """
        - (CustomFuture<NSNumber *> * _Nullable)echoBooleanFuture:(CustomFuture<NSNumber *> *)future {
          return (CustomFuture<NSNumber *> * _Nullable) [FutureAdapter fromBooleanFutureWithCustomFuture:[self echoBooleanFutureWithCustomFuture:[FutureAdapter toBooleanFutureWithCustomFuture:(CustomFuture *) future]]];
        }
        """);
    assertInTranslation(
        impl,
        """
        - (void)processFlow:(UnadaptedFlow *)flow {
          [self processFlowWithUnadaptedFlow:flow];
        }
        """);
  }

  public void testAbstractClassImplementingInterface_withoutRedeclaring() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface CountService {
          @ObjectiveCKmpMethod(selector = "updateCount:", adapter = Adapter.class)
          void setCount(Integer count);

          @ObjectiveCKmpMethod(selector = "fetchCount", adapter = Adapter.class)
          Integer getCount();
        }
        """,
        "CountService.java");
    addSourceFile(
        "public abstract class AbstractCountService implements CountService {}",
        "AbstractCountService.java");
    addSourceFile(
        """
        public class ConcreteCountService extends AbstractCountService {
          @Override
          public void setCount(Integer count) {}

          @Override
          public Integer getCount() {
            return 0;
          }
        }
        """,
        "ConcreteCountService.java");

    String abstractHeader = translateSourceFile("AbstractCountService", "AbstractCountService.h");
    assertInTranslation(abstractHeader, "- (void)updateCount:(NSNumber *)count;");
    assertInTranslation(abstractHeader, "- (NSNumber *)fetchCount;");
    assertInTranslation(
        abstractHeader, "- (void)setCountWithJavaLangInteger:(JavaLangInteger *)count;");
    assertInTranslation(abstractHeader, "- (JavaLangInteger *)getCount;");

    String abstractImpl = translateSourceFile("AbstractCountService", "AbstractCountService.m");
    assertInTranslation(
        abstractImpl,
        """
        - (void)updateCount:(NSNumber *)count {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
        }

        - (void)setCountWithJavaLangInteger:(JavaLangInteger *)count {
          [self updateCount:(NSNumber *) [Adapter fromIntegerWithJavaLangInteger:count]];
        }

        - (NSNumber *)fetchCount {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
          return 0;
        }

        - (JavaLangInteger *)getCount {
          return [Adapter toIntegerWithId:[self fetchCount]];
        }
        """);

    String concreteImpl = translateSourceFile("ConcreteCountService", "ConcreteCountService.m");
    assertInTranslation(
        concreteImpl,
        """
        - (void)setCountWithJavaLangInteger:(JavaLangInteger *)count {
          if (ConcreteCountService_isOverride(self, @selector(updateCount:))) {
            ConcreteCountService_setCountWithJavaLangInteger__dispatch(self, count);
            return;
          }
        }

        - (JavaLangInteger *)getCount {
          if (ConcreteCountService_isOverride(self, @selector(fetchCount))) {
            return ConcreteCountService_getCount_dispatch(self);
          }
          return JavaLangInteger_valueOfWithInt_(0);
        }
        """);
    assertInTranslation(
        concreteImpl,
        """
        - (void)updateCount:(NSNumber *)count {
          [self setCountWithJavaLangInteger:[Adapter toIntegerWithId:count]];
        }

        - (NSNumber *)fetchCount {
          return (NSNumber *) [Adapter fromIntegerWithJavaLangInteger:[self getCount]];
        }
        """);
  }

  public void testAbstractClassImplementingInterface_withRedeclaring() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface CountService {
          @ObjectiveCKmpMethod(selector = "fetchCount", adapter = Adapter.class)
          Integer getCount();
        }
        """,
        "CountService.java");
    addSourceFile(
        """
        public abstract class AbstractCountService implements CountService {
          @Override
          public abstract Integer getCount();
        }
        """,
        "AbstractCountService.java");

    String abstractHeader = translateSourceFile("AbstractCountService", "AbstractCountService.h");
    assertOccurrences(abstractHeader, "- (NSNumber *)fetchCount;", 1);
    assertOccurrences(abstractHeader, "- (JavaLangInteger *)getCount;", 1);

    String abstractImpl = translateSourceFile("AbstractCountService", "AbstractCountService.m");
    assertInTranslation(
        abstractImpl,
        """
        - (JavaLangInteger *)getCount {
          return [Adapter toIntegerWithId:[self fetchCount]];
        }
        """);
    // An Objective-C subclass would be expected to implement this method.
    assertInTranslation(
        abstractImpl,
        """
        - (NSNumber *)fetchCount {
          // can't call an abstract method
          [self doesNotRecognizeSelector:_cmd];
          return 0;
        }
        """);
  }

  public void testAbstractClassImplementingInterface_subclassHierarchy() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface CountService {
          @ObjectiveCKmpMethod(selector = "fetchCount", adapter = Adapter.class)
          Integer getCount();
        }
        """,
        "CountService.java");
    addSourceFile(
        "public abstract class AbstractBase implements CountService {}", "AbstractBase.java");
    addSourceFile("public abstract class AbstractSub extends AbstractBase {}", "AbstractSub.java");
    addSourceFile(
        """
        public class ConcreteSub extends AbstractSub {
          @Override
          public Integer getCount() {
            return 0;
          }
        }
        """,
        "ConcreteSub.java");

    String baseImpl = translateSourceFile("AbstractBase", "AbstractBase.m");
    assertInTranslation(
        baseImpl,
        """
        - (JavaLangInteger *)getCount {
          return [Adapter toIntegerWithId:[self fetchCount]];
        }
        """);

    // Check that the abstract methods are not translated in the header or implementation of
    // AbstractSub.
    String subHeader = translateSourceFile("AbstractSub", "AbstractSub.h");
    assertNotInTranslation(subHeader, "fetchCount");
    assertNotInTranslation(subHeader, "getCount");

    String subImpl = translateSourceFile("AbstractSub", "AbstractSub.m");
    assertNotInTranslation(subImpl, "fetchCount");
    assertNotInTranslation(subImpl, "getCount");

    String concreteImpl = translateSourceFile("ConcreteSub", "ConcreteSub.m");
    assertInTranslation(
        concreteImpl,
        """
        - (NSNumber *)fetchCount {
          return (NSNumber *) [Adapter fromIntegerWithJavaLangInteger:[self getCount]];
        }
        """);
  }

  public void testAbstractClassImplementingInterface_genericInterface() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface GenericService<T> {
          @ObjectiveCKmpMethod(selector = "echoValue:", adapter = Adapter.class)
          T echo(T value);
        }
        """,
        "GenericService.java");
    addSourceFile(
        "public abstract class AbstractIntService implements GenericService<Integer> {}",
        "AbstractIntService.java");

    String abstractHeader = translateSourceFile("AbstractIntService", "AbstractIntService.h");
    assertInTranslation(abstractHeader, "- (NSNumber *)echoValue:(NSNumber *)value;");
    assertOccurrences(
        abstractHeader, "- (JavaLangInteger *)echoWithId:(JavaLangInteger *)value;", 1);

    String abstractImpl = translateSourceFile("AbstractIntService", "AbstractIntService.m");
    assertInTranslation(
        abstractImpl,
        """
        - (JavaLangInteger *)echoWithId:(JavaLangInteger *)value {
          return [Adapter toIntegerWithId:[self echoValue:(NSNumber *) [Adapter fromIntegerWithJavaLangInteger:value]]];
        }
        """);
  }

  public void testAbstractClassImplementingInterface_annotations() throws IOException {
    options.load(new String[] {"--nullability"});
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.ObjectiveCName;
        import com.google.j2objc.annotations.SwiftName;
        import org.jspecify.annotations.NullMarked;
        import org.jspecify.annotations.Nullable;

        @NullMarked
        @SwiftName("AnnotatedService")
        public interface AnnotatedService {
          @ObjectiveCKmpMethod(
              selector = "echoCount:",
              adapter = Adapter.class,
              swiftName = "echo(count:)")
          @ObjectiveCName("customEchoCount:")
          @SwiftName("customEcho(count:)")
          @Nullable Integer echo(@Nullable Integer count);
        }
        """,
        "AnnotatedService.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.SwiftName;
        import org.jspecify.annotations.NullMarked;

        @NullMarked
        @SwiftName("AbstractAnnotatedService")
        public abstract class AbstractAnnotatedService implements AnnotatedService {}
        """,
        "AbstractAnnotatedService.java");

    String abstractHeader =
        translateSourceFile("AbstractAnnotatedService", "AbstractAnnotatedService.h");
    assertInTranslation(
        abstractHeader,
        """
        - (NSNumber * _Nullable)echoCount:(NSNumber * _Nullable)count NS_SWIFT_NAME(echo(count:));\
        """);
    assertInTranslation(
        abstractHeader,
        "- (JavaLangInteger * _Nullable)customEchoCount:(JavaLangInteger * _Nullable)count;");
    assertNotInTranslation(abstractHeader, "NS_SWIFT_NAME(customEcho");

    String abstractImpl =
        translateSourceFile("AbstractAnnotatedService", "AbstractAnnotatedService.m");
    assertInTranslation(
        abstractImpl,
        """
        - (JavaLangInteger *)customEchoCount:(JavaLangInteger *)count {
          return [Adapter toIntegerWithId:[self echoCount:(NSNumber * _Nullable) [Adapter fromIntegerWithJavaLangInteger:count]]];
        }
        """);
  }

  public void testAbstractClassImplementingInterface_composableConverter() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface ContainerService {
          @ObjectiveCKmpMethod(selector = "echoBooleanContainer:", adapter = Adapter.class)
          Container<Boolean> echo(Container<Boolean> c);
        }
        """,
        "ContainerService.java");
    addSourceFile(
        "public abstract class AbstractContainerService implements ContainerService {}",
        "AbstractContainerService.java");

    String abstractHeader =
        translateSourceFile("AbstractContainerService", "AbstractContainerService.h");
    assertInTranslation(
        abstractHeader,
        "- (Container<NSNumber *> *)echoBooleanContainer:(Container<NSNumber *> *)c;");
    assertInTranslation(
        abstractHeader,
        "- (Container<JavaLangBoolean *> *)echoWithContainer:(Container<JavaLangBoolean *> *)c;");

    String abstractImpl =
        translateSourceFile("AbstractContainerService", "AbstractContainerService.m");
    assertInTranslation(
        abstractImpl,
        """
        - (Container *)echoWithContainer:(Container *)c {
          return (Container *) [Adapter \
        toContainerWithContainer:(Container *) [self echoBooleanContainer:(Container<NSNumber *> *) [Adapter \
        fromContainerWithContainer:(Container *) c \
        withJavaUtilFunctionFunction:JreLoadStatic(AbstractContainerService_$KmpLambda$1, instance)]] \
        withJavaUtilFunctionFunction:JreLoadStatic(AbstractContainerService_$KmpLambda$2, instance)];
        }
        """);
  }

  public void testLambdaImplementingInterface_doesNotDuplicateBridgeMethod() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface CountService {
          @ObjectiveCKmpMethod(selector = "setCountWithNumber:", adapter = Adapter.class)
          void setCount(Integer count);
        }
        """,
        "CountService.java");
    addSourceFile(
        """
        public class LambdaConsumer {
          public CountService getService() {
            return count -> {};
          }
        }
        """,
        "LambdaConsumer.java");

    String translation = translateSourceFile("LambdaConsumer", "LambdaConsumer.m");
    assertInTranslation(
        translation, "- (void)setCountWithJavaLangInteger:(JavaLangInteger *)count;");
    assertNotInTranslation(
        translation, "- (void)setCountWithJavaLangInteger:(JavaLangInteger *)arg0");
    assertNotInTranslation(translation, "[self doesNotRecognizeSelector:_cmd]");
  }

  public void testLambdaImplementingInterface_withDefaultKmpMethod_emitsDefaultAdapter()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface ActionCallback {
          @ObjectiveCKmpMethod(selector = "onActionWithArray:", adapter = ImmutableListAdapter.class)
          void onAction(ImmutableList<String> actions);

          @ObjectiveCKmpMethod(selector = "onRenderWithArray:", adapter = ImmutableListAdapter.class)
          default void onRender(ImmutableList<String> actions) {}
        }
        """,
        "ActionCallback.java");
    addSourceFile(
        """
        public class CallbackFactory {
          public static ActionCallback create() {
            return actions -> {};
          }
        }
        """,
        "CallbackFactory.java");

    String impl = translateSourceFile("CallbackFactory", "CallbackFactory.m");
    assertInTranslation(impl, "@implementation CallbackFactory_$Lambda$1");
    assertInTranslation(
        impl,
        """
        - (void)onRenderWithArray:(NSArray<NSString *> *)actions {
          [self onRenderWithComGoogleCommonCollectImmutableList:[ImmutableListAdapter toImmutableListWithId:actions]];
        }
        """);
    assertNotInTranslation(impl, "CallbackFactory_$Lambda$1_isOverride");
    assertNotInTranslation(impl, "_dispatch");
  }

  public void testComposableConverter_withPreexistingLambdasInClass() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Map;
        import java.util.function.Function;

        public class StateService {
          public Function<String, String> helper() {
            return s -> s.trim();
          }

          @ObjectiveCKmpMethod(selector = "commitStates:", adapter = Adapter.class)
          public void commitStates(Map<Foo, Boolean> states) {}
        }
        """,
        "StateService.java");

    String impl = translateSourceFile("StateService", "StateService.m");
    assertInTranslation(impl, "StateService_$Lambda$1");
    assertInTranslation(impl, "StateService_$KmpLambda$1");
    assertInTranslation(impl, "StateService_$KmpLambda$2");
  }

  private void addImmutableListAdapter() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
  }

  public void testInterfaceDispatchHelperGeneration() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface MyInterface {
          @ObjectiveCKmpMethod(selector = "setItems:", adapter = ImmutableListAdapter.class)
          void setItems(ImmutableList<String> items);

          @ObjectiveCKmpMethod(selector = "getItemsAsNSArray", adapter = ImmutableListAdapter.class)
          ImmutableList<String> getItems();
        }
        """,
        "MyInterface.java");

    String interfaceHeader = translateSourceFile("MyInterface", "MyInterface.h");
    assertInTranslation(
        interfaceHeader,
        """
        @protocol MyInterface < JavaObject >

        - (void)setItems:(NSArray<NSString *> *)items;

        - (NSArray<NSString *> *)getItemsAsNSArray;

        #if !defined(__swift__)
        @optional
        #endif

        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<NSString *> *)items;

        - (ComGoogleCommonCollectImmutableList<NSString *> *)getItems;

        @end
        """);
    assertInTranslation(
        interfaceHeader,
        """
        FOUNDATION_EXPORT void MyInterface_setItemsWithComGoogleCommonCollectImmutableList__dispatch(id<MyInterface> self, ComGoogleCommonCollectImmutableList *items);
        """);
    assertInTranslation(
        interfaceHeader,
        """
        FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *MyInterface_getItems_dispatch(id<MyInterface> self);
        """);

    String interfaceImpl = translateSourceFile("MyInterface", "MyInterface.m");
    assertInTranslation(
        interfaceImpl,
        """
        void MyInterface_setItemsWithComGoogleCommonCollectImmutableList__dispatch(id<MyInterface> self, ComGoogleCommonCollectImmutableList *items) {
          if ([self respondsToSelector:@selector(setItemsWithComGoogleCommonCollectImmutableList:)]) {
            [self setItemsWithComGoogleCommonCollectImmutableList:items];
          }
          else {
            [self setItems:(NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
          }
        }
        """);
    assertInTranslation(
        interfaceImpl,
        """
        ComGoogleCommonCollectImmutableList *MyInterface_getItems_dispatch(id<MyInterface> self) {
          if ([self respondsToSelector:@selector(getItems)]) {
            return [self getItems];
          }
          else {
            return [ImmutableListAdapter toImmutableListWithId:[self getItemsAsNSArray]];
          }
        }
        """);
  }

  public void testInterfaceMethodCallSiteRewriting() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface MyInterface {
          @ObjectiveCKmpMethod(selector = "echoItems:", adapter = ImmutableListAdapter.class)
          ImmutableList<String> echoItems(ImmutableList<String> items);
        }
        """,
        "MyInterface.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class Caller {
          public static ImmutableList<String> callEcho(MyInterface instance, ImmutableList<String> items) {
            return instance.echoItems(items);
          }
        }
        """,
        "Caller.java");

    String callerImpl = translateSourceFile("Caller", "Caller.m");
    assertInTranslation(
        callerImpl,
        """
        ComGoogleCommonCollectImmutableList *Caller_callEchoWithMyInterface_withComGoogleCommonCollectImmutableList_(id<MyInterface> instance, ComGoogleCommonCollectImmutableList *items) {
          Caller_initialize();
          return MyInterface_echoItemsWithComGoogleCommonCollectImmutableList__dispatch((id<MyInterface>) nil_chk(instance), items);
        }
        """);
  }

  public void testSubInterfaceMethodCallSiteRewriting() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface SuperInterface {
          @ObjectiveCKmpMethod(selector = "echoItems:", adapter = ImmutableListAdapter.class)
          ImmutableList<String> echoItems(ImmutableList<String> items);
        }
        """,
        "SuperInterface.java");
    addSourceFile(
        """
        public interface SubInterface extends SuperInterface {}
        """,
        "SubInterface.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubCaller {
          public static ImmutableList<String> callSubEcho(SubInterface instance, ImmutableList<String> items) {
            return instance.echoItems(items);
          }
        }
        """,
        "SubCaller.java");

    String subCallerImpl = translateSourceFile("SubCaller", "SubCaller.m");
    assertInTranslation(subCallerImpl, "#include \"SuperInterface.h\"");
    assertInTranslation(
        subCallerImpl,
        """
        ComGoogleCommonCollectImmutableList *SubCaller_callSubEchoWithSubInterface_withComGoogleCommonCollectImmutableList_(id<SubInterface> instance, ComGoogleCommonCollectImmutableList *items) {
          SubCaller_initialize();
          return SuperInterface_echoItemsWithComGoogleCommonCollectImmutableList__dispatch((id<SubInterface>) nil_chk(instance), items);
        }
        """);
  }

  public void testSubInterfaceOverrideWithoutAnnotation() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface SuperInterface {
          @ObjectiveCKmpMethod(selector = "echoItems:", adapter = ImmutableListAdapter.class)
          ImmutableList<String> echoItems(ImmutableList<String> items);
        }
        """,
        "SuperInterface.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public interface SubInterface extends SuperInterface {
          @Override
          ImmutableList<String> echoItems(ImmutableList<String> items);
        }
        """,
        "SubInterface.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubCaller {
          public static ImmutableList<String> callSubEcho(SubInterface instance, ImmutableList<String> items) {
            return instance.echoItems(items);
          }
        }
        """,
        "SubCaller.java");

    String subInterfaceHeader = translateSourceFile("SubInterface", "SubInterface.h");
    assertInTranslation(
        subInterfaceHeader,
        """
        @protocol SubInterface < SuperInterface, JavaObject >

        #if !defined(__swift__)
        @optional
        #endif

        - (ComGoogleCommonCollectImmutableList<NSString *> *)echoItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<NSString *> *)items;

        @end
        """);
    assertNotInTranslation(
        subInterfaceHeader,
        "SubInterface_echoItemsWithComGoogleCommonCollectImmutableList__dispatch");

    String subCallerImpl = translateSourceFile("SubCaller", "SubCaller.m");
    assertInTranslation(subCallerImpl, "#include \"SuperInterface.h\"");
    assertInTranslation(
        subCallerImpl,
        """
        ComGoogleCommonCollectImmutableList *SubCaller_callSubEchoWithSubInterface_withComGoogleCommonCollectImmutableList_(id<SubInterface> instance, ComGoogleCommonCollectImmutableList *items) {
          SubCaller_initialize();
          return SuperInterface_echoItemsWithComGoogleCommonCollectImmutableList__dispatch((id<SubInterface>) nil_chk(instance), items);
        }
        """);
  }

  public void testInterfaceMethodSwiftName() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface CustomInterface {
          @ObjectiveCKmpMethod(
              selector = "setItems:",
              swiftName = "set(items:)",
              adapter = ImmutableListAdapter.class)
          void setItems(ImmutableList<String> items);
        }
        """,
        "CustomInterface.java");

    String interfaceHeader = translateSourceFile("CustomInterface", "CustomInterface.h");
    assertInTranslation(
        interfaceHeader,
        "- (void)setItems:(NSArray<NSString *> *)items NS_SWIFT_NAME(set(items:));");
  }

  public void testInterfaceDefaultMethodCallSiteRewriting() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "ImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface InterfaceWithDefault {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          default ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "InterfaceWithDefault.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class DefaultCaller {
          public static ImmutableList<String> callGetItems(InterfaceWithDefault instance) {
            return instance.getItems();
          }
        }
        """,
        "DefaultCaller.java");

    String callerImpl = translateSourceFile("DefaultCaller", "DefaultCaller.m");
    assertInTranslation(
        callerImpl,
        """
        ComGoogleCommonCollectImmutableList *DefaultCaller_callGetItemsWithInterfaceWithDefault_(id<InterfaceWithDefault> instance) {
          DefaultCaller_initialize();
          return InterfaceWithDefault_getItems_dispatch((id<InterfaceWithDefault>) nil_chk(instance));
        }
        """);
  }
  public void testTranslate_reannotatedConcreteSubclassEmitsAdapterOnlyWhenConfigDiffers()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class BaseService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "BaseService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public final class SameConfigService extends BaseService {
          @Override
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "SameConfigService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public final class RenamedConfigService extends BaseService {
          @Override
          @ObjectiveCKmpMethod(selector = "getRenamedItems", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "RenamedConfigService.java");

    String sameImpl = translateSourceFile("SameConfigService", "SameConfigService.m");
    assertNotInTranslation(sameImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");

    String renamedHeader = translateSourceFile("RenamedConfigService", "RenamedConfigService.h");
    assertInTranslation(renamedHeader, "- (NSArray<NSString *> *)getRenamedItems;");
  }

  private void addOpenService() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class OpenService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = ImmutableListAdapter.class)
          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "OpenService.java");
  }

  private void addOpenBase() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class OpenBase {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "OpenBase.java");
  }

  private void addInterfaceWithDefault() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface InterfaceWithDefault {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          default ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "InterfaceWithDefault.java");
  }

  private void addCovariantAdapter() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import java.util.List;

        public class CovariantAdapter {
          public static native Object fromJavaUtilList(List<String> list) /*-[ return nil; ]-*/;
          public static native List<String> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
          public static native Object fromBoolean(Boolean b) /*-[ return nil; ]-*/;
          public static native Boolean toBoolean(Object b) /*-[ return nil; ]-*/;
        }
        """,
        "CovariantAdapter.java");
  }

  public void testConcreteClassNativeOverride_generatesCheckAndHelpers() throws IOException {
    addOpenService();

    String openHeader = translateSourceFile("OpenService", "OpenService.h");
    assertOccurrences(
        openHeader,
        "FOUNDATION_EXPORT bool OpenService_isOverride(OpenService *self, SEL selector);",
        1);
    assertInTranslation(
        openHeader,
        """
        FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *OpenService_getItems_dispatch(OpenService *self);\
        """);
    assertInTranslation(
        openHeader,
        """
        FOUNDATION_EXPORT void OpenService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(OpenService *self, ComGoogleCommonCollectImmutableList *items);\
        """);

    String openImpl = translateSourceFile("OpenService", "OpenService.m");
    assertOccurrences(
        openImpl,
        """
        bool OpenService_isOverride(OpenService *self, SEL selector) {
          return self != nil && [self methodForSelector:selector] != [OpenService instanceMethodForSelector:selector];
        }
        """,
        1);
    assertInTranslation(
        openImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (OpenService_isOverride(self, @selector(getItemsAsNsArray))) {
            return OpenService_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertInTranslation(
        openImpl,
        """
        ComGoogleCommonCollectImmutableList *OpenService_getItems_dispatch(OpenService *self) {
          return [ImmutableListAdapter toImmutableListWithId:[self getItemsAsNsArray]];
        }
        """);
    assertInTranslation(
        openImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (OpenService_isOverride(self, @selector(setItems:))) {
            OpenService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
        }
        """);
    assertInTranslation(
        openImpl,
        """
        void OpenService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(OpenService *self, ComGoogleCommonCollectImmutableList *items) {
          [self setItems:(NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
        }
        """);
  }

  public void testConcreteClassNativeOverride_finalMethodSkipsCheckAndHelpers() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class ServiceWithFinalMethod {
          @ObjectiveCKmpMethod(selector = "getFinalItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public final ImmutableList<String> getFinalItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ServiceWithFinalMethod.java");

    String header = translateSourceFile("ServiceWithFinalMethod", "ServiceWithFinalMethod.h");
    assertInTranslation(header, "- (NSArray<NSString *> *)getFinalItemsAsNsArray;");
    assertNotInTranslation(header, "ServiceWithFinalMethod_isOverride");
    assertNotInTranslation(header, "ServiceWithFinalMethod_getFinalItems_dispatch");

    String impl = translateSourceFile("ServiceWithFinalMethod", "ServiceWithFinalMethod.m");
    assertInTranslation(
        impl,
        """
        - (ComGoogleCommonCollectImmutableList *)getFinalItems {
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(impl, "ServiceWithFinalMethod_isOverride");
    assertNotInTranslation(impl, "ServiceWithFinalMethod_getFinalItems_dispatch");
  }

  public void testConcreteClassNativeOverride_finalClassSkipsCheckAndHelpers() throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public final class FinalService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "FinalService.java");

    String header = translateSourceFile("FinalService", "FinalService.h");
    assertInTranslation(header, "- (NSArray<NSString *> *)getItemsAsNsArray;");
    assertNotInTranslation(header, "FinalService_isOverride");
    assertNotInTranslation(header, "FinalService_getItems_dispatch");

    String impl = translateSourceFile("FinalService", "FinalService.m");
    assertInTranslation(
        impl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(impl, "FinalService_isOverride");
    assertNotInTranslation(impl, "FinalService_getItems_dispatch");
  }

  public void testConcreteClassNativeOverride_multiLevelJavaSubclassDelegatesToBase()
      throws IOException {
    addOpenService();
    addSourceFile(
        """
        public class SilentMidService extends OpenService {}
        """,
        "SilentMidService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class JavaSubService extends SilentMidService {
          @Override
          public ImmutableList<String> getItems() {
            return super.getItems();
          }

          @Override
          public void setItems(ImmutableList<String> items) {
            super.setItems(items);
          }
        }
        """,
        "JavaSubService.java");

    String midImpl = translateSourceFile("SilentMidService", "SilentMidService.m");
    assertNotInTranslation(midImpl, "getItemsAsNsArray");
    assertNotInTranslation(midImpl, "setItems:");
    assertNotInTranslation(midImpl, "SilentMidService_isOverride");
    assertNotInTranslation(midImpl, "_dispatch");

    String subImpl = translateSourceFile("JavaSubService", "JavaSubService.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (OpenService_isOverride(self, @selector(getItemsAsNsArray))) {
            return OpenService_getItems_dispatch(self);
          }
          return [super getItems];
        }
        """);
    assertInTranslation(
        subImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (OpenService_isOverride(self, @selector(setItems:))) {
            OpenService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
          [super setItemsWithComGoogleCommonCollectImmutableList:items];
        }
        """);
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(subImpl, "- (void)setItems:");
    assertNotInTranslation(subImpl, "JavaSubService_isOverride");
    assertNotInTranslation(subImpl, "JavaSubService_getItems_dispatch");
    assertNotInTranslation(
        subImpl, "JavaSubService_setItemsWithComGoogleCommonCollectImmutableList__dispatch");
  }

  public void testConcreteClassNativeOverride_oneWayReturnAdapterSkipsCheckOnBaseAndSubclass()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class OneWayReturnAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
        }
        """,
        "OneWayReturnAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class OpenService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = OneWayReturnAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "OpenService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class JavaSubService extends OpenService {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "JavaSubService.java");

    String openHeader = translateSourceFile("OpenService", "OpenService.h");
    assertNotInTranslation(openHeader, "OpenService_isOverride");
    assertNotInTranslation(openHeader, "OpenService_getItems_dispatch");

    String openImpl = translateSourceFile("OpenService", "OpenService.m");
    assertInTranslation(openImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(openImpl, "OpenService_isOverride");
    assertNotInTranslation(openImpl, "OpenService_getItems_dispatch");

    String subImpl = translateSourceFile("JavaSubService", "JavaSubService.m");
    assertNotInTranslation(subImpl, "OpenService_isOverride");
    assertNotInTranslation(subImpl, "OpenService_getItems_dispatch");
  }

  public void testConcreteClassNativeOverride_unannotatedSuperclassImplementingKmpInterface()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class UnannotatedBase {
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "UnannotatedBase.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface KmpInterface {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          ImmutableList<String> getItems();

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = ImmutableListAdapter.class)
          void setItems(ImmutableList<String> items);
        }
        """,
        "KmpInterface.java");
    addSourceFile(
        """
        public class MidService extends UnannotatedBase implements KmpInterface {}
        """,
        "MidService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubService extends MidService {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          @Override
          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "SubService.java");

    String midHeader = translateSourceFile("MidService", "MidService.h");
    assertInTranslation(
        midHeader, "FOUNDATION_EXPORT bool MidService_isOverride(MidService *self, SEL selector);");
    assertInTranslation(
        midHeader,
        """
        FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *MidService_getItems_dispatch(MidService *self);\
        """);
    assertInTranslation(
        midHeader,
        """
        FOUNDATION_EXPORT void MidService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(MidService *self, ComGoogleCommonCollectImmutableList *items);\
        """);
    assertNotInTranslation(midHeader, "- (ComGoogleCommonCollectImmutableList *)getItems;");
    assertNotInTranslation(
        midHeader,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items;\
        """);

    String midImpl = translateSourceFile("MidService", "MidService.m");
    assertInTranslation(midImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertInTranslation(midImpl, "- (void)setItems:(NSArray<NSString *> *)items");
    assertInTranslation(midImpl, "bool MidService_isOverride(MidService *self, SEL selector)");
    assertInTranslation(
        midImpl,
        "ComGoogleCommonCollectImmutableList *MidService_getItems_dispatch(MidService *self)");
    assertInTranslation(
        midImpl,
        """
        void MidService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(MidService *self, ComGoogleCommonCollectImmutableList *items)\
        """);
    assertInTranslation(
        midImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (MidService_isOverride(self, @selector(getItemsAsNsArray))) {
            return MidService_getItems_dispatch(self);
          }
          return [super getItems];
        }
        """);
    assertInTranslation(
        midImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(setItems:))) {
            MidService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
          [super setItemsWithComGoogleCommonCollectImmutableList:items];
        }
        """);

    String subImpl = translateSourceFile("SubService", "SubService.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (MidService_isOverride(self, @selector(getItemsAsNsArray))) {
            return MidService_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertInTranslation(
        subImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(setItems:))) {
            MidService_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
        }
        """);
    assertNotInTranslation(subImpl, "UnannotatedBase_isOverride");
    assertNotInTranslation(subImpl, "UnannotatedBase_getItems_dispatch");
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
  }

  public void testConcreteClassNativeOverride_genericUnannotatedSuperclassBridgeCastsReturn()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile("public class SubFoo extends Foo {}", "SubFoo.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class UnannotatedBase<T extends Foo> {
          public T getItem(ImmutableList<String> items) {
            return null;
          }
        }
        """,
        "UnannotatedBase.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface KmpSubFooInterface {
          @ObjectiveCKmpMethod(selector = "getItem:", adapter = ImmutableListAdapter.class)
          SubFoo getItem(ImmutableList<String> items);
        }
        """,
        "KmpSubFooInterface.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface KmpFooInterface {
          @ObjectiveCKmpMethod(selector = "getItem:", adapter = ImmutableListAdapter.class)
          Foo getItem(ImmutableList<String> items);
        }
        """,
        "KmpFooInterface.java");
    addSourceFile(
        """
        public class MidService extends UnannotatedBase<SubFoo> implements KmpSubFooInterface {}
        """,
        "MidService.java");
    addSourceFile(
        """
        public class MidFooService extends UnannotatedBase<SubFoo> implements KmpFooInterface {}
        """,
        "MidFooService.java");

    String midHeader = translateSourceFile("MidService", "MidService.h");
    assertNotInTranslation(
        midHeader, "- (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:");
    assertInTranslation(
        midHeader, "FOUNDATION_EXPORT bool MidService_isOverride(MidService *self, SEL selector);");

    String midImpl = translateSourceFile("MidService", "MidService.m");
    assertInTranslation(
        midImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(getItem:))) {
            return MidService_getItemWithComGoogleCommonCollectImmutableList__dispatch(self, items);
          }
          return (SubFoo *) [super getItemWithComGoogleCommonCollectImmutableList:items];
        }
        """);

    String midFooHeader = translateSourceFile("MidFooService", "MidFooService.h");
    assertOccurrences(
        midFooHeader,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<NSString *> *)items;\
        """,
        1);

    String midFooImpl = translateSourceFile("MidFooService", "MidFooService.m");
    assertInTranslation(
        midFooImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidFooService_isOverride(self, @selector(getItem:))) {
            return MidFooService_getItemWithComGoogleCommonCollectImmutableList__dispatch(self, items);
          }
          return (SubFoo *) [super getItemWithComGoogleCommonCollectImmutableList:items];
        }
        """);
  }

  public void testConcreteClassNativeOverride_privateSuperclassKmpMethodNotInherited()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class BaseWithPrivate {
          @ObjectiveCKmpMethod(selector = "getSecretAsNsArray", adapter = ImmutableListAdapter.class)
          private ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "BaseWithPrivate.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubWithPublic extends BaseWithPrivate {
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "SubWithPublic.java");

    String baseImpl = translateSourceFile("BaseWithPrivate", "BaseWithPrivate.m");
    assertInTranslation(baseImpl, "- (NSArray<NSString *> *)getSecretAsNsArray");
    assertNotInTranslation(baseImpl, "BaseWithPrivate_isOverride");
    assertNotInTranslation(baseImpl, "BaseWithPrivate_getItems_dispatch");

    String subImpl = translateSourceFile("SubWithPublic", "SubWithPublic.m");
    assertInTranslation(subImpl, "- (ComGoogleCommonCollectImmutableList *)getItems");
    assertNotInTranslation(subImpl, "BaseWithPrivate_isOverride");
    assertNotInTranslation(subImpl, "BaseWithPrivate_getItems_dispatch");
    assertNotInTranslation(subImpl, "SubWithPublic_isOverride");
    assertNotInTranslation(subImpl, "SubWithPublic_getItems_dispatch");
    assertNotInTranslation(subImpl, "getSecretAsNsArray");
  }

  public void testConcreteClassNativeOverride_inheritedInterfaceDefaultMethodAndSubclass()
      throws IOException {
    addInterfaceWithDefault();
    addSourceFile(
        """
        public class ConcreteInherits implements InterfaceWithDefault {}
        """,
        "ConcreteInherits.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubOverrides extends ConcreteInherits {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "SubOverrides.java");

    String inheritsImpl = translateSourceFile("ConcreteInherits", "ConcreteInherits.m");
    assertInTranslation(
        inheritsImpl,
        "bool ConcreteInherits_isOverride(ConcreteInherits *self, SEL selector)");
    assertInTranslation(
        inheritsImpl,
        """
        ComGoogleCommonCollectImmutableList *ConcreteInherits_getItems_dispatch(ConcreteInherits *self)\
        """);
    assertInTranslation(
        inheritsImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteInherits_isOverride(self, @selector(getItemsAsNsArray))) {
            return ConcreteInherits_getItems_dispatch(self);
          }
          return InterfaceWithDefault_getItems(self);
        }
        """);

    String subImpl = translateSourceFile("SubOverrides", "SubOverrides.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteInherits_isOverride(self, @selector(getItemsAsNsArray))) {
            return ConcreteInherits_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(subImpl, "SubOverrides_isOverride");
    assertNotInTranslation(subImpl, "SubOverrides_getItems_dispatch");
  }

  public void testConcreteClassNativeOverride_inheritedParameterizedInterfaceDefaultMethod()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public interface InterfaceWithDefaultSetter {
          @ObjectiveCKmpMethod(selector = "setItems:", adapter = ImmutableListAdapter.class)
          default void setItems(ImmutableList<String> items) {}
        }
        """,
        "InterfaceWithDefaultSetter.java");
    addSourceFile(
        """
        public class ConcreteInheritsSetter implements InterfaceWithDefaultSetter {}
        """,
        "ConcreteInheritsSetter.java");

    String inheritsImpl =
        translateSourceFile("ConcreteInheritsSetter", "ConcreteInheritsSetter.m");
    assertInTranslation(
        inheritsImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)arg0 {
          if (ConcreteInheritsSetter_isOverride(self, @selector(setItems:))) {
            ConcreteInheritsSetter_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, arg0);
            return;
          }
          InterfaceWithDefaultSetter_setItemsWithComGoogleCommonCollectImmutableList_(self, arg0);
        }
        """);
  }

  public void testConcreteClassNativeOverride_reannotatedSubclassSameSelectorDelegatesToBase()
      throws IOException {
    addOpenBase();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class ReannotatedSub extends OpenBase {
          @Override
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ReannotatedSub.java");

    String subImpl = translateSourceFile("ReannotatedSub", "ReannotatedSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (OpenBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return OpenBase_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(subImpl, "ReannotatedSub_isOverride");
  }

  public void testConcreteClassNativeOverride_reannotatedSubclassNewSelectorGeneratesNewAdapter()
      throws IOException {
    addOpenBase();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class RenamedSub extends OpenBase {
          @Override
          @ObjectiveCKmpMethod(selector = "getSubItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "RenamedSub.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class GrandSub extends RenamedSub {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "GrandSub.java");

    String renamedImpl = translateSourceFile("RenamedSub", "RenamedSub.m");
    assertInTranslation(renamedImpl, "- (NSArray<NSString *> *)getSubItemsAsNsArray");
    assertInTranslation(renamedImpl, "bool RenamedSub_isOverride(RenamedSub *self, SEL selector)");
    assertInTranslation(
        renamedImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (RenamedSub_isOverride(self, @selector(getSubItemsAsNsArray))) {
            return RenamedSub_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);

    String grandImpl = translateSourceFile("GrandSub", "GrandSub.m");
    assertInTranslation(
        grandImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (RenamedSub_isOverride(self, @selector(getSubItemsAsNsArray))) {
            return RenamedSub_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(grandImpl, "- (NSArray<NSString *> *)getSubItemsAsNsArray");
  }

  public void testConcreteClassNativeOverride_noWrapperMethodsWithSwiftNameOmitsHelperSwiftWrappers()
      throws IOException {
    options.load(new String[] {"--no-wrapper-methods"});
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import com.google.common.collect.ImmutableList;

        @SwiftName("SwiftOpenService")
        public class OpenService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "OpenService.java");

    String header = translateSourceFile("OpenService", "OpenService.h");
    assertInTranslation(
        header, "FOUNDATION_EXPORT bool OpenService_isOverride(OpenService *self, SEL selector);");
    assertInTranslation(
        header,
        """
        FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *OpenService_getItems_dispatch(OpenService *self);\
        """);
    assertNotInTranslation(header, "OpenService_isOverride_swiftName");
    assertNotInTranslation(header, "OpenService_getItems_dispatch_swiftName");
  }

  public void
      testConcreteClassNativeOverride_nullMarkedNullableReturnPreservesNullabilityOnDispatch()
          throws IOException {
    options.load(new String[] {"--nullability", "--no-wrapper-methods"});
    options.setNullMarked(true);
    addImmutableListAdapter();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.j2objc.annotations.SwiftName;
        import org.jspecify.annotations.NullMarked;
        import org.jspecify.annotations.Nullable;

        @NullMarked
        @SwiftName("SwiftOpenService")
        public class OpenService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public @Nullable ImmutableList<String> getItems() {
            return null;
          }
        }
        """,
        "OpenService.java");

    String header = translateSourceFile("OpenService", "OpenService.h");
    assertInTranslation(
        header,
        """
        FOUNDATION_EXPORT ComGoogleCommonCollectImmutableList *_Nullable OpenService_getItems_dispatch(OpenService *self);\
        """);
    assertNotInTranslation(header, "OpenService_getItems_dispatch_swiftName");
  }

  public void testConcreteClassNativeOverride_covariantReturnTypeUsesNarrowedConverter()
      throws IOException {
    addCovariantAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        import com.google.common.collect.ImmutableList;

        public class ListBase {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = CovariantAdapter.class)
          public List<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ListBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ImmutableSub extends ListBase {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ImmutableSub.java");

    String subImpl = translateSourceFile("ImmutableSub", "ImmutableSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ListBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return [CovariantAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "ListBase_getItems_dispatch");
  }

  public void testConcreteClassNativeOverride_genericBaseWithStringOnlyReturnConverterSkipsCheck()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class PartialGenericAdapter {
          public static native <T> Object fromImmutableList(ImmutableList<T> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "PartialGenericAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class GenericBase<T> {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = PartialGenericAdapter.class)
          public ImmutableList<T> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "GenericBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class StringSub extends GenericBase<String> {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "StringSub.java");

    String baseImpl = translateSourceFile("GenericBase", "GenericBase.m");
    assertInTranslation(baseImpl, "- (NSArray<id> *)getItemsAsNsArray");
    assertNotInTranslation(baseImpl, "GenericBase_isOverride");
    assertNotInTranslation(baseImpl, "GenericBase_getItems_dispatch");

    String subImpl = translateSourceFile("StringSub", "StringSub.m");
    assertInTranslation(subImpl, "- (ComGoogleCommonCollectImmutableList *)getItems");
    assertNotInTranslation(subImpl, "_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
  }

  public void testConcreteClassNativeOverride_genericBaseBidirectionalDelegatesToBase()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class BidirectionalGenericAdapter {
          public static native <T> Object fromImmutableList(ImmutableList<T> list) /*-[ return nil; ]-*/;
          public static native <T> ImmutableList<T> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "BidirectionalGenericAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class GenericBase<T> {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = BidirectionalGenericAdapter.class)
          public ImmutableList<T> getItems() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = BidirectionalGenericAdapter.class)
          public void setItems(ImmutableList<T> items) {}
        }
        """,
        "GenericBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class StringSub extends GenericBase<String> {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          @Override
          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "StringSub.java");

    String baseImpl = translateSourceFile("GenericBase", "GenericBase.m");
    assertInTranslation(baseImpl, "bool GenericBase_isOverride(GenericBase *self, SEL selector)");
    assertInTranslation(
        baseImpl,
        "ComGoogleCommonCollectImmutableList *GenericBase_getItems_dispatch(GenericBase *self)");

    String subImpl = translateSourceFile("StringSub", "StringSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (GenericBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return GenericBase_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertInTranslation(
        subImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (GenericBase_isOverride(self, @selector(setItems:))) {
            GenericBase_setItemsWithComGoogleCommonCollectImmutableList__dispatch(self, items);
            return;
          }
        }
        """);
  }

  public void testConcreteClassNativeOverride_subinterfaceDefaultMethodOverrideIncludesBaseCheck()
      throws IOException {
    addInterfaceWithDefault();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public interface SubInterfaceWithDefault extends InterfaceWithDefault {
          @Override
          default ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "SubInterfaceWithDefault.java");
    addSourceFile(
        """
        public class ConcreteBase implements InterfaceWithDefault {}
        """,
        "ConcreteBase.java");
    addSourceFile(
        """
        public class SubConcrete extends ConcreteBase implements SubInterfaceWithDefault {}
        """,
        "SubConcrete.java");

    String subImpl = translateSourceFile("SubConcrete", "SubConcrete.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return ConcreteBase_getItems_dispatch(self);
          }
          return SubInterfaceWithDefault_getItems(self);
        }
        """);
    assertNotInTranslation(subImpl, "SubConcrete_isOverride");
    assertNotInTranslation(subImpl, "SubConcrete_getItems_dispatch");
  }

  public void
      testConcreteClassNativeOverride_genericBaseSpecializedParamWithCovariantReturnUsesBaseContract()
          throws IOException {
    addCovariantAdapter();
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class GenericBase<T> {
          @ObjectiveCKmpMethod(selector = "process:items:", adapter = CovariantAdapter.class)
          public List<String> process(T tag, List<String> items) {
            return items;
          }
        }
        """,
        "GenericBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import java.util.List;

        public class BooleanSub extends GenericBase<Boolean> {
          @Override
          public ImmutableList<String> process(Boolean id, List<String> inputItems) {
            return ImmutableList.of();
          }
        }
        """,
        "BooleanSub.java");

    String subImpl = translateSourceFile("BooleanSub", "BooleanSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)processWithId:(JavaLangBoolean *)id_
                                              withJavaUtilList:(id<JavaUtilList>)inputItems {
          if (GenericBase_isOverride(self, @selector(process:items:))) {
            return [CovariantAdapter toImmutableListWithId:[self process:id_ items:(NSArray<NSString *> *) [CovariantAdapter fromJavaUtilListWithJavaUtilList:inputItems]]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "fromBoolean");
  }

  public void testConcreteClassNativeOverride_finalSubclassFinalOverrideAndEnumSkipChecks()
      throws IOException {
    addOpenService();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public final class FinalSub extends OpenService {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "FinalSub.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class NonFinalSub extends OpenService {
          @Override
          public final ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "NonFinalSub.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public enum ServiceEnum {
          INSTANCE;

          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ServiceEnum.java");

    String finalSubImpl = translateSourceFile("FinalSub", "FinalSub.m");
    assertInTranslation(finalSubImpl, "- (ComGoogleCommonCollectImmutableList *)getItems");
    assertNotInTranslation(finalSubImpl, "_isOverride");
    assertNotInTranslation(finalSubImpl, "_dispatch");

    String nonFinalSubImpl = translateSourceFile("NonFinalSub", "NonFinalSub.m");
    assertInTranslation(nonFinalSubImpl, "- (ComGoogleCommonCollectImmutableList *)getItems");
    assertNotInTranslation(nonFinalSubImpl, "_isOverride");
    assertNotInTranslation(nonFinalSubImpl, "_dispatch");

    String enumImpl = translateSourceFile("ServiceEnum", "ServiceEnum.m");
    assertInTranslation(enumImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(enumImpl, "_isOverride");
    assertNotInTranslation(enumImpl, "_dispatch");
  }

  public void testConcreteClassNativeOverride_reannotatedSubclassDifferentAdapterGeneratesNewAdapter()
      throws IOException {
    addOpenBase();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class AltImmutableListAdapter {
          public static native Object fromImmutableList(ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(Object list) /*-[ return nil; ]-*/;
        }
        """,
        "AltImmutableListAdapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public class AltSub extends OpenBase {
          @Override
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = AltImmutableListAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "AltSub.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class GrandAltSub extends AltSub {
          @Override
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "GrandAltSub.java");

    String altImpl = translateSourceFile("AltSub", "AltSub.m");
    assertInTranslation(
        altImpl,
        """
        - (NSArray<NSString *> *)getItemsAsNsArray {
          return (NSArray<NSString *> *) [AltImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);
    assertInTranslation(altImpl, "bool AltSub_isOverride(AltSub *self, SEL selector)");
    assertInTranslation(
        altImpl,
        """
        ComGoogleCommonCollectImmutableList *AltSub_getItems_dispatch(AltSub *self) {
          return [AltImmutableListAdapter toImmutableListWithId:[self getItemsAsNsArray]];
        }
        """);

    String grandImpl = translateSourceFile("GrandAltSub", "GrandAltSub.m");
    assertInTranslation(
        grandImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (AltSub_isOverride(self, @selector(getItemsAsNsArray))) {
            return AltSub_getItems_dispatch(self);
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(grandImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(grandImpl, "OpenBase_isOverride");
  }

  public void testConcreteClassNativeOverride_abstractClassWithConcreteMethodAndGenericReturnCast()
      throws IOException {
    addImmutableListAdapter();
    addSourceFile("public class SubFoo extends Foo {}", "SubFoo.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import com.google.common.collect.ImmutableList;

        public abstract class AbstractGenericBase<T extends Foo> {
          @ObjectiveCKmpMethod(selector = "getItem:", adapter = ImmutableListAdapter.class)
          public T getItem(ImmutableList<String> items) {
            return null;
          }
        }
        """,
        "AbstractGenericBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class ConcreteSubFoo extends AbstractGenericBase<SubFoo> {
          @Override
          public SubFoo getItem(ImmutableList<String> items) {
            return null;
          }
        }
        """,
        "ConcreteSubFoo.java");

    String baseHeader = translateSourceFile("AbstractGenericBase", "AbstractGenericBase.h");
    assertInTranslation(baseHeader, "- (Foo *)getItem:(NSArray<NSString *> *)items;");

    String baseImpl = translateSourceFile("AbstractGenericBase", "AbstractGenericBase.m");
    assertInTranslation(
        baseImpl,
        """
        - (Foo *)getItem:(NSArray<NSString *> *)items {
          return (Foo *) [self getItemWithComGoogleCommonCollectImmutableList:[ImmutableListAdapter toImmutableListWithId:items]];
        }
        """);
    assertInTranslation(
        baseImpl, "bool AbstractGenericBase_isOverride(AbstractGenericBase *self, SEL selector)");
    assertInTranslation(
        baseImpl,
        """
        Foo *AbstractGenericBase_getItemWithComGoogleCommonCollectImmutableList__dispatch(AbstractGenericBase *self, ComGoogleCommonCollectImmutableList *items)\
        """);

    String subImpl = translateSourceFile("ConcreteSubFoo", "ConcreteSubFoo.m");
    assertInTranslation(
        subImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (AbstractGenericBase_isOverride(self, @selector(getItem:))) {
            return (SubFoo *) AbstractGenericBase_getItemWithComGoogleCommonCollectImmutableList__dispatch(self, items);
          }
          return nil;
        }
        """);
  }
}
