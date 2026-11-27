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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import java.io.IOException;

/**
 * Tests native subclass override dispatch generation for concrete {@code @ObjectiveCKmpMethod}
 * declarations in {@link ObjectiveCKmpMethodTranslator}.
 */
public class ObjectiveCKmpNativeOverrideTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.load(new String[] {"--objc-generics"});
    addSourceFile("public class Foo {}", "Foo.java");
    addSourceFile("public class SubFoo extends Foo {}", "SubFoo.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import java.util.List;
        import java.util.function.Function;

        public class TestAdapter {
          public static native Object fromJavaUtilList(List<String> list) /*-[ return nil; ]-*/;
          public static native List<String> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
          public static native <T> Object fromJavaUtilList(
              List<T> list, Function<T, Object> converter) /*-[ return nil; ]-*/;
          public static native <T> List<T> toJavaUtilList(
              Object list, Function<Object, T> converter) /*-[ return nil; ]-*/;
          public static native Object fromImmutableList(
              ImmutableList<String> list) /*-[ return nil; ]-*/;
          public static native ImmutableList<String> toImmutableList(
              Object list) /*-[ return nil; ]-*/;
          public static native Object fromBoolean(Boolean b) /*-[ return nil; ]-*/;
          public static native Boolean toBoolean(Object b) /*-[ return nil; ]-*/;
        }
        """,
        "TestAdapter.java");
  }

  private void addOpenService() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class OpenService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = TestAdapter.class)
          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "OpenService.java");
  }

  private void addInterfaceWithDefault() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface InterfaceWithDefault {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          default ImmutableList<String> getItems() {
            return ImmutableList.of();
          }

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = TestAdapter.class)
          default void setItems(ImmutableList<String> items) {}
        }
        """,
        "InterfaceWithDefault.java");
  }

  public void testOpenConcreteClass_emitsOverrideCheckAndInlinesAdaptedCall() throws IOException {
    addOpenService();

    String header = translateSourceFile("OpenService", "OpenService.h");
    assertOccurrences(
        header,
        "FOUNDATION_EXPORT bool OpenService_isOverride(OpenService *self, SEL selector);",
        1);
    assertNotInTranslation(header, "_dispatch");

    String impl = translateSourceFile("OpenService", "OpenService.m");
    assertOccurrences(
        impl,
        """
        bool OpenService_isOverride(OpenService *self, SEL selector) {
          if (self == nil) {
            return false;
          }
          Class cls = [self class];
          Class owner = [OpenService class];
          if (cls == owner || ![cls isSubclassOfClass:owner]) {
            return false;
          }
          IMP clsImp = [cls instanceMethodForSelector:selector];
          IMP ownerImp = [owner instanceMethodForSelector:selector];
          return clsImp != ownerImp;
        }
        """,
        1);
    assertInTranslation(
        impl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (OpenService_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertInTranslation(
        impl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (OpenService_isOverride(self, @selector(setItems:))) {
            [self setItems:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
            return;
          }
        }
        """);
    assertNotInTranslation(impl, "_dispatch");
  }

  public void testNonOverridableDeclarations_skipOverrideChecks() throws IOException {
    addInterfaceWithDefault();
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class ServiceWithFinalMethod {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          public final ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ServiceWithFinalMethod.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public final class FinalService {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "FinalService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public enum ServiceEnum {
          INSTANCE;

          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          public ImmutableList<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ServiceEnum.java");
    addSourceFile(
        """
        public class AnonAndPrivateFactory {
          private static class PrivateImpl implements InterfaceWithDefault {}

          public static InterfaceWithDefault createAnon() {
            return new InterfaceWithDefault() {};
          }

          public static InterfaceWithDefault createPrivate() {
            return new PrivateImpl();
          }
        }
        """,
        "AnonAndPrivateFactory.java");

    for (String className :
        new String[] {
          "ServiceWithFinalMethod", "FinalService", "ServiceEnum", "AnonAndPrivateFactory"
        }) {
      String impl = translateSourceFile(className, className + ".m");
      assertInTranslation(impl, "- (NSArray<NSString *> *)getItemsAsNsArray");
      assertNotInTranslation(impl, "_isOverride");
      assertNotInTranslation(impl, "_dispatch");
    }
  }

  public void testMultiLevelJavaSubclass_delegatesToBaseOverrideCheck() throws IOException {
    addOpenService();
    addSourceFile("public class SilentMidService extends OpenService {}", "SilentMidService.java");
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
    assertNotInTranslation(midImpl, "_isOverride");
    assertNotInTranslation(midImpl, "_dispatch");

    String subImpl = translateSourceFile("JavaSubService", "JavaSubService.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (OpenService_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return [super getItems];
        }
        """);
    assertInTranslation(
        subImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (OpenService_isOverride(self, @selector(setItems:))) {
            [self setItems:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
            return;
          }
          [super setItemsWithComGoogleCommonCollectImmutableList:items];
        }
        """);
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(subImpl, "- (void)setItems:");
    assertNotInTranslation(subImpl, "JavaSubService_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
  }

  public void testFinalSubclassAndFinalOverride_skipOverrideChecks() throws IOException {
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

    for (String className : new String[] {"FinalSub", "NonFinalSub"}) {
      String impl = translateSourceFile(className, className + ".m");
      assertInTranslation(impl, "- (ComGoogleCommonCollectImmutableList *)getItems");
      assertNotInTranslation(impl, "_isOverride");
      assertNotInTranslation(impl, "_dispatch");
    }
  }

  public void testPrivateSuperclassKmpMethod_notInheritedBySubclass() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class BaseWithPrivate {
          @ObjectiveCKmpMethod(selector = "getSecretAsNsArray", adapter = TestAdapter.class)
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
    assertNotInTranslation(subImpl, "_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
    assertNotInTranslation(subImpl, "getSecretAsNsArray");
  }

  public void testOneWayAdapter_skipsOverrideChecksOnBaseAndSubclass() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class OneWayReturnAdapter {
          public static native Object fromImmutableList(
              ImmutableList<String> list) /*-[ return nil; ]-*/;
        }
        """,
        "OneWayReturnAdapter.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

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

    String openImpl = translateSourceFile("OpenService", "OpenService.m");
    assertInTranslation(openImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(openImpl, "_isOverride");
    assertNotInTranslation(openImpl, "_dispatch");

    String subImpl = translateSourceFile("JavaSubService", "JavaSubService.m");
    assertNotInTranslation(subImpl, "_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
  }

  public void testUnannotatedSuperclassImplementingKmpInterface_synthesizesSuperBridge()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class UnannotatedBase<T extends Foo> {
          public T getItem(ImmutableList<String> items) {
            return null;
          }

          public void setItems(ImmutableList<String> items) {}
        }
        """,
        "UnannotatedBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface KmpSubFooInterface {
          @ObjectiveCKmpMethod(selector = "getItem:", adapter = TestAdapter.class)
          Foo getItem(ImmutableList<String> items);

          @ObjectiveCKmpMethod(selector = "setItems:", adapter = TestAdapter.class)
          void setItems(ImmutableList<String> items);
        }
        """,
        "KmpSubFooInterface.java");
    addSourceFile(
        "public class MidService extends UnannotatedBase<SubFoo> implements KmpSubFooInterface {}",
        "MidService.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class SubService extends MidService {
          @Override
          public SubFoo getItem(ImmutableList<String> items) {
            return null;
          }
        }
        """,
        "SubService.java");

    String midHeader = translateSourceFile("MidService", "MidService.h");
    assertInTranslation(
        midHeader, "FOUNDATION_EXPORT bool MidService_isOverride(MidService *self, SEL selector);");
    assertNotInTranslation(midHeader, "_dispatch");
    assertOccurrences(
        midHeader,
        "- (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList<NSString *> *)items;",
        1);
    assertNotInTranslation(midHeader, "- (void)setItemsWithComGoogleCommonCollectImmutableList:");

    String midImpl = translateSourceFile("MidService", "MidService.m");
    assertInTranslation(midImpl, "- (SubFoo *)getItem:(NSArray<NSString *> *)items");
    assertInTranslation(midImpl, "- (void)setItems:(NSArray<NSString *> *)items");
    assertInTranslation(
        midImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(getItem:))) {
            return (SubFoo *) [self getItem:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
          }
          return (SubFoo *) [super getItemWithComGoogleCommonCollectImmutableList:items];
        }
        """);
    assertInTranslation(
        midImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(setItems:))) {
            [self setItems:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
            return;
          }
          [super setItemsWithComGoogleCommonCollectImmutableList:items];
        }
        """);

    String subImpl = translateSourceFile("SubService", "SubService.m");
    assertInTranslation(
        subImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (MidService_isOverride(self, @selector(getItem:))) {
            return (SubFoo *) [self getItem:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
          }
          return nil;
        }
        """);
    assertNotInTranslation(subImpl, "UnannotatedBase_isOverride");
    assertNotInTranslation(subImpl, "- (SubFoo *)getItem:(NSArray<NSString *> *)items");
  }

  public void testInheritedInterfaceDefaultMethod_prependsCheckAndSharesWithSubclass()
      throws IOException {
    addInterfaceWithDefault();
    addSourceFile(
        "public class ConcreteInherits implements InterfaceWithDefault {}",
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
        inheritsImpl, "bool ConcreteInherits_isOverride(ConcreteInherits *self, SEL selector)");
    assertNotInTranslation(inheritsImpl, "_dispatch");
    assertInTranslation(
        inheritsImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteInherits_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return InterfaceWithDefault_getItems(self);
        }
        """);
    assertInTranslation(
        inheritsImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)arg0 {
          if (ConcreteInherits_isOverride(self, @selector(setItems:))) {
            [self setItems:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:arg0]];
            return;
          }
          InterfaceWithDefault_setItemsWithComGoogleCommonCollectImmutableList_(self, arg0);
        }
        """);

    String subImpl = translateSourceFile("SubOverrides", "SubOverrides.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteInherits_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "- (NSArray<NSString *> *)getItemsAsNsArray");
    assertNotInTranslation(subImpl, "SubOverrides_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
  }

  public void testSubinterfaceDefaultMethodOverride_reusesBaseConcreteOwnerCheck()
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
        "public class ConcreteBase implements InterfaceWithDefault {}", "ConcreteBase.java");
    addSourceFile(
        "public class SubConcrete extends ConcreteBase implements SubInterfaceWithDefault {}",
        "SubConcrete.java");

    String subImpl = translateSourceFile("SubConcrete", "SubConcrete.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ConcreteBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return SubInterfaceWithDefault_getItems(self);
        }
        """);
    assertNotInTranslation(subImpl, "SubConcrete_isOverride");
    assertNotInTranslation(subImpl, "_dispatch");
  }

  private void assertMultiSelectorDefaultMethodShims(
      String implName,
      String subProtoName,
      String superInterfaces,
      String primarySelectorPart,
      String secondarySelectorPart)
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public interface GenericDefaultProto<T> {
          @ObjectiveCKmpMethod(selector = "workWith:items:", adapter = TestAdapter.class)
          default void work(T tag, ImmutableList<String> items) {}
        }
        """,
        "GenericDefaultProto.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public interface StringDefaultProto {
          void work(String tag, ImmutableList<String> items);
        }
        """,
        "StringDefaultProto.java");
    addSourceFile(
        String.format(
            """
            import com.google.common.collect.ImmutableList;

            public interface %s extends %s {
              @Override
              default void work(String tag, ImmutableList<String> items) {}
            }
            """,
            subProtoName, superInterfaces),
        subProtoName + ".java");

    String impl =
        translateSourceFile(
            "public class " + implName + " implements " + subProtoName + " {}",
            implName,
            implName + ".m");
    assertOccurrences(impl, "if (" + implName + "_isOverride(", 1);
    assertInTranslation(
        impl,
        String.format(
            """
            - (void)workWith%s:(NSString *)arg0
            withComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)arg1 {
              if (%s_isOverride(self, @selector(workWith:items:))) {
                [self workWith:arg0 items:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:arg1]];
                return;
              }
              %s_workWithNSString_withComGoogleCommonCollectImmutableList_(self, arg0, arg1);
            }

            - (void)workWith%s:(NSString *)arg0
            withComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)arg1 {
              [self workWith%s:arg0 withComGoogleCommonCollectImmutableList:arg1];
            }
            """,
            primarySelectorPart,
            implName,
            subProtoName,
            secondarySelectorPart,
            primarySelectorPart));
    assertNotInTranslation(
        impl, subProtoName + "_workWithNSString_withComGoogleCommonCollectImmutableList__dispatch");
  }

  public void testMultiSelectorDefaultMethodShims_genericFirstForwardsViaPrimaryCheck()
      throws IOException {
    assertMultiSelectorDefaultMethodShims(
        "MultiSelectorDefaultImpl",
        "SubDefaultProto",
        "GenericDefaultProto<String>, StringDefaultProto",
        "Id",
        "NSString");
  }

  public void testMultiSelectorDefaultMethodShims_stringFirstForwardsViaPrimaryCheck()
      throws IOException {
    assertMultiSelectorDefaultMethodShims(
        "StringFirstDefaultImpl",
        "StringFirstSubProto",
        "StringDefaultProto, GenericDefaultProto<String>",
        "NSString",
        "Id");
  }

  public void testNoWrapperMethodsWithSwiftName_preservesNullabilityWithoutSwiftFunctionWrappers()
      throws IOException {
    options.load(new String[] {"--nullability", "--no-wrapper-methods"});
    options.setNullMarked(true);

    String header =
        translateSourceFile(
            """
            import com.google.common.collect.ImmutableList;
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import com.google.j2objc.annotations.SwiftName;
            import org.jspecify.annotations.NullMarked;
            import org.jspecify.annotations.Nullable;

            @NullMarked
            @SwiftName("SwiftOpenService")
            public class OpenService {
              @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
              public @Nullable ImmutableList<String> getItems() {
                return null;
              }
            }
            """,
            "OpenService",
            "OpenService.h");
    assertInTranslation(
        header, "FOUNDATION_EXPORT bool OpenService_isOverride(OpenService *self, SEL selector);");
    assertInTranslation(
        header, "- (NSArray<NSString *> * _Nullable)getItemsAsNsArray NS_SWIFT_NAME(getItemsAsNsArray());");
    assertNotInTranslation(header, "_dispatch");
    assertNotInTranslation(header, "OpenService_isOverride_swiftName");
  }

  public void testCovariantMappedReturnType_inlinesNarrowedReturnConverter() throws IOException {
    String subImpl =
        translateSourceFile(
            """
            import com.google.common.collect.ImmutableList;
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            class ListBase {
              @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
              public List<String> getItems() {
                return ImmutableList.of();
              }
            }

            public class ImmutableSub extends ListBase {
              @Override
              public ImmutableList<String> getItems() {
                return ImmutableList.of();
              }
            }
            """,
            "ImmutableSub",
            "ImmutableSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (ListBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return [TestAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "ListBase_getItems_dispatch");
  }

  public void testCovariantUnadaptedReturnType_skipsOverrideCheck() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        class ListOnlyAdapter {
          public static native Object fromJavaUtilList(List<String> list) /*-[ return nil; ]-*/;
          public static native List<String> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
        }

        public class ListOnlyBase {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = ListOnlyAdapter.class)
          public List<String> getItems() {
            return ImmutableList.of();
          }
        }
        """,
        "ListOnlyBase.java");

    String subImpl =
        translateSourceFile(
            """
            import com.google.common.collect.ImmutableList;

            public class UnadaptedImmutableSub extends ListOnlyBase {
              @Override
              public ImmutableList<String> getItems() {
                return ImmutableList.of();
              }
            }
            """,
            "UnadaptedImmutableSub",
            "UnadaptedImmutableSub.m");
    assertInTranslation(subImpl, "- (ComGoogleCommonCollectImmutableList *)getItems");
    assertNotInTranslation(subImpl, "ListOnlyBase_isOverride");
  }

  public void testCovariantUnmappedReturnType_castsAdaptedReturnInSubclass() throws IOException {
    String subImpl =
        translateSourceFile(
            """
            import com.google.common.collect.ImmutableList;
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            class FooBase {
              @ObjectiveCKmpMethod(selector = "getItem:", adapter = TestAdapter.class)
              public Foo getItem(ImmutableList<String> items) {
                return null;
              }
            }

            public class SubFooService extends FooBase {
              @Override
              public SubFoo getItem(ImmutableList<String> items) {
                return null;
              }
            }
            """,
            "SubFooService",
            "SubFooService.m");
    assertInTranslation(
        subImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (FooBase_isOverride(self, @selector(getItem:))) {
            return (SubFoo *) [self getItem:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
          }
          return nil;
        }
        """);
  }

  public void testGenericBaseWithBidirectionalConverter_delegatesSubclassToBase()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;

        public class BidirectionalGenericAdapter {
          public static native <T> Object fromImmutableList(
              ImmutableList<T> list) /*-[ return nil; ]-*/;
          public static native <T> ImmutableList<T> toImmutableList(
              Object list) /*-[ return nil; ]-*/;
        }
        """,
        "BidirectionalGenericAdapter.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public class GenericBase<T> {
          @ObjectiveCKmpMethod(
              selector = "getItemsAsNsArray",
              adapter = BidirectionalGenericAdapter.class)
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
    assertNotInTranslation(baseImpl, "_dispatch");

    String subImpl = translateSourceFile("StringSub", "StringSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)getItems {
          if (GenericBase_isOverride(self, @selector(getItemsAsNsArray))) {
            return (ComGoogleCommonCollectImmutableList *) [BidirectionalGenericAdapter toImmutableListWithId:[self getItemsAsNsArray]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertInTranslation(
        subImpl,
        """
        - (void)setItemsWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (GenericBase_isOverride(self, @selector(setItems:))) {
            [self setItems:(NSArray<id> *) [BidirectionalGenericAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *) items]];
            return;
          }
        }
        """);
  }

  public void testGenericBaseSpecializedParamWithCovariantReturn_usesBaseParameterContract()
      throws IOException {
    String subImpl =
        translateSourceFile(
            """
            import com.google.common.collect.ImmutableList;
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            class GenericBase<T> {
              @ObjectiveCKmpMethod(selector = "process:items:", adapter = TestAdapter.class)
              public List<String> process(T tag, List<String> items) {
                return items;
              }
            }

            public class BooleanSub extends GenericBase<Boolean> {
              @Override
              public ImmutableList<String> process(Boolean id, List<String> inputItems) {
                return ImmutableList.of();
              }
            }
            """,
            "BooleanSub",
            "BooleanSub.m");
    assertInTranslation(
        subImpl,
        """
        - (ComGoogleCommonCollectImmutableList *)processWithId:(JavaLangBoolean *)id_
                                              withJavaUtilList:(id<JavaUtilList>)inputItems {
          if (GenericBase_isOverride(self, @selector(process:items:))) {
            return [TestAdapter toImmutableListWithId:[self process:id_ items:(NSArray<NSString *> *) [TestAdapter fromJavaUtilListWithJavaUtilList:inputItems]]];
          }
          return ComGoogleCommonCollectImmutableList_of();
        }
        """);
    assertNotInTranslation(subImpl, "fromBoolean");
  }

  public void testAbstractGenericClassWithConcreteMethod_castsAdaptedReturnInSubclass()
      throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;

        public abstract class AbstractGenericBase<T extends Foo> {
          @ObjectiveCKmpMethod(selector = "getItem:", adapter = TestAdapter.class)
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

    String baseImpl = translateSourceFile("AbstractGenericBase", "AbstractGenericBase.m");
    assertInTranslation(
        baseImpl,
        """
        - (Foo *)getItem:(NSArray<NSString *> *)items {
          return (Foo *) [self getItemWithComGoogleCommonCollectImmutableList:[TestAdapter toImmutableListWithId:items]];
        }
        """);
    assertNotInTranslation(baseImpl, "_dispatch");

    String subImpl = translateSourceFile("ConcreteSubFoo", "ConcreteSubFoo.m");
    assertInTranslation(
        subImpl,
        """
        - (SubFoo *)getItemWithComGoogleCommonCollectImmutableList:(ComGoogleCommonCollectImmutableList *)items {
          if (AbstractGenericBase_isOverride(self, @selector(getItem:))) {
            return (SubFoo *) [self getItem:(NSArray<NSString *> *) [TestAdapter fromImmutableListWithComGoogleCommonCollectImmutableList:items]];
          }
          return nil;
        }
        """);
  }

  public void testComposableConverterOnSubclass_lowersSynthesizedLambda() throws IOException {
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class NestedListBase {
          @ObjectiveCKmpMethod(selector = "getNestedItems", adapter = TestAdapter.class)
          public List<ImmutableList<String>> getNestedItems() {
            return null;
          }
        }
        """,
        "NestedListBase.java");
    addSourceFile(
        """
        import com.google.common.collect.ImmutableList;
        import java.util.List;

        public class NestedListSub extends NestedListBase {
          @Override
          public List<ImmutableList<String>> getNestedItems() {
            return null;
          }
        }
        """,
        "NestedListSub.java");

    String baseImpl = translateSourceFile("NestedListBase", "NestedListBase.m");
    assertInTranslation(
        baseImpl, "if (NestedListBase_isOverride(self, @selector(getNestedItems)))");
    assertInTranslation(baseImpl, "NestedListBase_$KmpLambda$");

    String subImpl = translateSourceFile("NestedListSub", "NestedListSub.m");
    assertInTranslation(subImpl, "if (NestedListBase_isOverride(self, @selector(getNestedItems)))");
    assertInTranslation(subImpl, "NestedListSub_$KmpLambda$");
    assertInTranslation(subImpl, "return TestAdapter_toImmutableListWithId_(elem);");
  }

  public void testSynchronizedConcreteMethod_prependsOverrideCheckOutsideSynchronizedBlock()
      throws IOException {
    String impl =
        translateSourceFile(
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public class SyncService {
              @ObjectiveCKmpMethod(selector = "fetchItems", adapter = TestAdapter.class)
              public synchronized List<String> getItems() {
                return null;
              }
            }
            """,
            "SyncService",
            "SyncService.m");

    assertInTranslation(
        impl,
        """
        - (id<JavaUtilList>)getItems {
          if (SyncService_isOverride(self, @selector(fetchItems))) {
            return [TestAdapter toJavaUtilListWithId:[self fetchItems]];
          }
          @synchronized(self) {
            return JreRetainedLocalValue(nil);
          }
        }
        """);
  }

  public void testNativeConcreteMethods_ocniAndJniPrependOverrideCheck() throws IOException {
    String impl =
        translateSourceFile(
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public class NativeService {
              @ObjectiveCKmpMethod(selector = "fetchOcniItems", adapter = TestAdapter.class)
              public native List<String> getOcniItems() /*-[
                return nil;
              ]-*/;

              @ObjectiveCKmpMethod(selector = "fetchJniItems", adapter = TestAdapter.class)
              public native List<String> getJniItems();
            }
            """,
            "NativeService",
            "NativeService.m");

    assertInTranslation(
        impl,
        """
        - (id<JavaUtilList>)getOcniItems {
          if (NativeService_isOverride(self, @selector(fetchOcniItems))) {
            return [TestAdapter toJavaUtilListWithId:[self fetchOcniItems]];
          }
          return nil;
        }
        """);
    assertInTranslation(
        impl,
        """
        - (id<JavaUtilList>)getJniItems {
          if (NativeService_isOverride(self, @selector(fetchJniItems))) {
            return [TestAdapter toJavaUtilListWithId:[self fetchJniItems]];
          }
          return NativeService_getJniItems(self);
        }
        """);
  }

  public void testDeadCodeEliminatedOwnerMethod_stillEmitsOverrideHelperForSubclass()
      throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.Collections;
        import java.util.List;

        public abstract class OpenBase {
          @ObjectiveCKmpMethod(selector = "getItemsAsNsArray", adapter = TestAdapter.class)
          public List<String> getItems() {
            return Collections.emptyList();
          }
        }
        """,
        "OpenBase.java");
    addSourceFile(
        """
        import java.util.Collections;
        import java.util.List;

        public class SubService extends OpenBase {
          @Override
          public List<String> getItems() {
            return Collections.singletonList("sub");
          }
        }
        """,
        "SubService.java");
    setDeadCodeMap(
        CodeReferenceMap.builder().addMethod("OpenBase", "getItems", "()Ljava/util/List;").build());

    String baseHeader = translateSourceFile("OpenBase", "OpenBase.h");
    assertInTranslation(
        baseHeader, "FOUNDATION_EXPORT bool OpenBase_isOverride(OpenBase *self, SEL selector);");

    String baseImpl = translateSourceFile("OpenBase", "OpenBase.m");
    assertInTranslation(baseImpl, "bool OpenBase_isOverride(OpenBase *self, SEL selector)");
    assertNotInTranslation(baseImpl, "- (id<JavaUtilList>)getItems");

    String subImpl = translateSourceFile("SubService", "SubService.m");
    assertInTranslation(subImpl, "if (OpenBase_isOverride(self, @selector(getItemsAsNsArray)))");
  }
}
