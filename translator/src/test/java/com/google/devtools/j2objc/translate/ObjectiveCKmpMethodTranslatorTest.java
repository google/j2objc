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
    addSourceFile("public class Bar {}", "Bar.java");
    addSourceFile(
        """
        import java.util.List;
        import java.util.Set;
        import java.util.Map;

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
            + " fromJavaUtilList_JavaUtilList_WithJavaUtilList:[self getMatrix]]");
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
          return (NSDictionary<NSString *, NSArray<Bar *> *> *) [Adapter fromJavaUtilMap_String_JavaUtilList_WithJavaUtilMap:[self getMap]];
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

          @ObjectiveCKmpMethod(selector="getItems", adapter=ImmutableListAdapter.class)
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
    assertInTranslation(abstractHeader, "- (NSArray<NSString *> *)getItems;");

    String abstractImpl = translateSourceFile("AbstractClass", "AbstractClass.m");
    assertInTranslation(
        abstractImpl,
        """
        - (NSArray<NSString *> *)getItems {
          return (NSArray<NSString *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);

    String concreteHeader = translateSourceFile("ConcreteClass", "ConcreteClass.h");
    assertNotInTranslation(concreteHeader, "- (void)setItems:(NSArray<NSString *> *)items;");
    assertNotInTranslation(concreteHeader, "- (NSArray<NSString *> *)getItems;");

    String concreteImpl = translateSourceFile("ConcreteClass", "ConcreteClass.m");
    assertNotInTranslation(concreteImpl, "- (void)setItems:(NSArray<NSString *> *)items {");
    assertNotInTranslation(concreteImpl, "- (NSArray<NSString *> *)getItems {");
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

          @ObjectiveCKmpMethod(selector="getItems", adapter=ImmutableListAdapter.class)
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
    assertInTranslation(abstractHeader, "- (NSArray<MyPkgCustomClass *> *)getItems;");
    assertInTranslation(abstractHeader, "@class MyPkgCustomClass;");

    String abstractImpl = translateSourceFile("AbstractClass", "AbstractClass.m");
    assertInTranslation(
        abstractImpl,
        """
        - (void)setItems:(NSArray<MyPkgCustomClass *> *)items {
          [self setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *) [ImmutableListAdapter toImmutableListWithId:items]];
        }
        """);
    assertInTranslation(
        abstractImpl,
        """
        - (NSArray<MyPkgCustomClass *> *)getItems {
          return (NSArray<MyPkgCustomClass *> *) [ImmutableListAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:[self getItems]];
        }
        """);

    String concreteHeader = translateSourceFile("ConcreteClass", "ConcreteClass.h");
    assertNotInTranslation(
        concreteHeader, "- (void)setItems:(NSArray<MyPkgCustomClass *> *)items;");
    assertNotInTranslation(concreteHeader, "- (NSArray<MyPkgCustomClass *> *)getItems;");
    assertNotInTranslation(concreteHeader, "@class MyPkgCustomClass;");

    String concreteImpl = translateSourceFile("ConcreteClass", "ConcreteClass.m");
    assertNotInTranslation(concreteImpl, "- (void)setItems:(NSArray<MyPkgCustomClass *> *)items {");
    assertNotInTranslation(concreteImpl, "- (NSArray<MyPkgCustomClass *> *)getItems {");
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
    assertTrue(message.contains("java.util.List<?>"));
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
}
