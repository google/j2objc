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

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.translate.KmpMethodHierarchy.TypeMethods;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/** Unit tests for {@link KmpMethodHierarchy}. */
public class KmpMethodHierarchyTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    addSourceFile("public class DummyAdapter {}", "DummyAdapter.java");
    addSourceFile("package adapter; public class DummyAdapter {}", "adapter/DummyAdapter.java");
  }

  private void addPkg1Base() throws IOException {
    addSourceFile(
        """
        package pkg1;
        import adapter.DummyAdapter;
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;
        public class Base {
          @ObjectiveCKmpMethod(selector = "foo:", adapter = DummyAdapter.class)
          void foo(List<String> items) {}
        }
        """,
        "pkg1/Base.java");
  }

  private KmpMethodHierarchy createHierarchy(CompilationUnit unit) {
    return new KmpMethodHierarchy(unit.getEnv().typeUtil(), unit.getEnv().elementUtil());
  }

  private static TypeElement findType(CompilationUnit unit, String simpleName) {
    for (var typeDecl : unit.getTypes()) {
      if (typeDecl instanceof TypeDeclaration td
          && td.getTypeElement().getSimpleName().contentEquals(simpleName)) {
        return td.getTypeElement();
      }
    }
    throw new AssertionError("Type not found: " + simpleName);
  }

  private static ExecutableElement findMethod(TypeElement type, String methodName) {
    List<ExecutableElement> matches = new ArrayList<>();
    for (ExecutableElement elem : ElementUtil.getExecutables(type)) {
      if (ElementUtil.getName(elem).equals(methodName)) {
        matches.add(elem);
      }
    }
    if (matches.isEmpty()) {
      throw new AssertionError("Method not found: " + methodName + " in " + type);
    }
    if (matches.size() > 1) {
      throw new AssertionError(
          "Multiple overloads found for " + methodName + " in " + type + ": " + matches);
    }
    return matches.get(0);
  }

  public void testGetTypeMethods_distinguishesOverloadsAndErasesTypeVariables() {
    CompilationUnit unit =
        compileType(
            "Sample",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            public class Sample<T, U extends Number> {
              @ObjectiveCKmpMethod(selector = "fooString:", adapter = DummyAdapter.class)
              public void foo(String s) {}

              @ObjectiveCKmpMethod(selector = "fooList:", adapter = DummyAdapter.class)
              public void foo(List<T> items) {}

              public void bar(T value) {}

              public void baz(U num) {}

              public void qux(int count, String[] names) {}
            }
            """);
    TypeMethods resolved = createHierarchy(unit).getTypeMethods(findType(unit, "Sample"));

    assertThat(resolved.allMethods().keySet())
        .containsAtLeast(
            "foo(Ljava/lang/String;)",
            "foo(Ljava/util/List;)",
            "bar(Ljava/lang/Object;)",
            "baz(Ljava/lang/Number;)",
            "qux(I[Ljava/lang/String;)");
    assertThat(resolved.annotatedSignatures().keySet())
        .containsExactly("foo(Ljava/lang/String;)", "foo(Ljava/util/List;)");
  }

  public void testGetTypeMethods_prefersClassImplementationOverInterfaces() {
    CompilationUnit unit =
        compileType(
            "Child",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            interface AnnotatedIface {
              @ObjectiveCKmpMethod(selector = "run", adapter = DummyAdapter.class)
              void run();
            }

            class ConcreteBase implements AnnotatedIface {
              @Override
              public void run() {}
            }

            interface SubIface extends AnnotatedIface {}

            public class Child extends ConcreteBase implements SubIface {}
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    TypeElement concreteBase = findType(unit, "ConcreteBase");
    TypeElement child = findType(unit, "Child");

    for (TypeElement type : ImmutableList.of(concreteBase, child)) {
      TypeMethods resolved = hierarchy.getTypeMethods(type);
      assertThat(resolved.annotatedSignatures()).containsKey("run()");
      assertThat(ElementUtil.getDeclaringClass(resolved.allMethods().get("run()").element()))
          .isEqualTo(concreteBase);
    }
  }

  public void testGetTypeMethods_substitutesGenericParameterAndReturnTypes() {
    CompilationUnit unit =
        compileType(
            "StringChannel",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            interface GenericChannel<T> {
              @ObjectiveCKmpMethod(selector = "exchange:", adapter = DummyAdapter.class)
              T exchange(T item);
            }

            public abstract class StringChannel implements GenericChannel<String> {}
            """);
    TypeMethods resolved = createHierarchy(unit).getTypeMethods(findType(unit, "StringChannel"));

    String sig = "exchange(Ljava/lang/String;)";
    assertThat(resolved.annotatedSignatures()).containsKey(sig);
    ExecutablePair resolvedMethod = resolved.allMethods().get(sig);
    assertThat(resolvedMethod).isNotNull();
    TypeUtil typeUtil = unit.getEnv().typeUtil();
    TypeMirror stringType = typeUtil.getJavaString().asType();
    assertThat(resolvedMethod.type().getParameterTypes()).hasSize(1);
    assertThat(typeUtil.isSameType(resolvedMethod.type().getParameterTypes().get(0), stringType))
        .isTrue();
    assertThat(typeUtil.isSameType(resolvedMethod.type().getReturnType(), stringType)).isTrue();
  }

  public void testGetTypeMethods_deduplicatesDiamondInterfaces() {
    CompilationUnit unit =
        compileType(
            "LeafIface",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            interface BaseIface {
              @ObjectiveCKmpMethod(selector = "ping", adapter = DummyAdapter.class)
              void ping();
            }

            interface LeftIface extends BaseIface {}

            interface RightIface extends BaseIface {}

            public interface LeafIface extends LeftIface, RightIface {}
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    ExecutableElement basePing = findMethod(findType(unit, "BaseIface"), "ping");
    TypeMethods resolved = hierarchy.getTypeMethods(findType(unit, "LeafIface"));

    assertThat(resolved.annotatedSignatures()).containsKey("ping()");
    assertThat(resolved.allMethods().get("ping()").element()).isEqualTo(basePing);
  }

  public void testGetSupertypeHierarchy_resolvesDirectAndTransitiveAnnotatedAncestors() {
    CompilationUnit unit =
        compileType(
            "Child",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            abstract class GrandParent {
              @ObjectiveCKmpMethod(selector = "work", adapter = DummyAdapter.class)
              public abstract void work();
            }

            abstract class MidParent extends GrandParent {}

            public class Child extends MidParent {
              @Override
              public void work() {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    TypeElement grandParent = findType(unit, "GrandParent");
    TypeElement midParent = findType(unit, "MidParent");
    TypeElement child = findType(unit, "Child");

    TypeMethods childSuper = hierarchy.getSupertypeHierarchy(child, "work()");
    assertThat(childSuper).isNotNull();
    assertThat(childSuper.typeElement()).isEqualTo(midParent);
    assertThat(childSuper.annotatedSignatures()).containsKey("work()");

    TypeMethods midSuper = hierarchy.getSupertypeHierarchy(midParent, "work()");
    assertThat(midSuper).isNotNull();
    assertThat(midSuper.typeElement()).isEqualTo(grandParent);
    assertThat(hierarchy.getSupertypeHierarchy(grandParent, "work()")).isNull();
  }

  public void testGetSupertypeHierarchy_skipsUnannotatedSuperclassToFindAnnotatedInterface() {
    CompilationUnit unit =
        compileType(
            "Impl",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            class UnannotatedBase {
              public void work() {}
            }

            interface AnnotatedIface {
              @ObjectiveCKmpMethod(selector = "work", adapter = DummyAdapter.class)
              void work();
            }

            public class Impl extends UnannotatedBase implements AnnotatedIface {
              @Override
              public void work() {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    TypeMethods superMethods = hierarchy.getSupertypeHierarchy(findType(unit, "Impl"), "work()");

    assertThat(superMethods).isNotNull();
    assertThat(superMethods.typeElement()).isEqualTo(findType(unit, "AnnotatedIface"));
  }

  public void testFindInterfaceMethodRequiringDispatch_resolvesDirectInheritedAndNearest() {
    CompilationUnit unit =
        compileType(
            "LeafIface",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            interface BaseIface {
              @ObjectiveCKmpMethod(selector = "baseSend:", adapter = DummyAdapter.class)
              void send(List<String> items);
            }

            interface UnannotatedSubIface extends BaseIface {
              @Override
              void send(List<String> items);
            }

            interface MidIface extends BaseIface {
              @ObjectiveCKmpMethod(selector = "midSend:", adapter = DummyAdapter.class)
              @Override
              void send(List<String> items);
            }

            public interface LeafIface extends MidIface {
              @Override
              void send(List<String> items);
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    ExecutableElement baseSend = findMethod(findType(unit, "BaseIface"), "send");
    ExecutableElement unannotatedSubSend =
        findMethod(findType(unit, "UnannotatedSubIface"), "send");
    ExecutableElement midSend = findMethod(findType(unit, "MidIface"), "send");
    ExecutableElement leafSend = findMethod(findType(unit, "LeafIface"), "send");

    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(baseSend)).isEqualTo(baseSend);
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(unannotatedSubSend))
        .isEqualTo(baseSend);
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(leafSend)).isEqualTo(midSend);
  }

  public void testFindInterfaceMethodRequiringDispatch_returnsNullForNonDispatchableElements() {
    CompilationUnit unit =
        compileType(
            "ConcreteImpl",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;

            interface BaseIface {
              @ObjectiveCKmpMethod(selector = "send", adapter = DummyAdapter.class)
              void send();

              @ObjectiveCKmpMethod(selector = "make", adapter = DummyAdapter.class)
              static void staticHelper() {}

              void plain();
            }

            public class ConcreteImpl implements BaseIface {
              public ConcreteImpl() {}

              @Override
              public void send() {}

              @Override
              public void plain() {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    TypeElement baseIface = findType(unit, "BaseIface");
    TypeElement concreteImpl = findType(unit, "ConcreteImpl");
    ExecutableElement ctor = ElementUtil.getConstructors(concreteImpl).iterator().next();

    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(null)).isNull();
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(ctor)).isNull();
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(findMethod(baseIface, "staticHelper")))
        .isNull();
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(findMethod(baseIface, "plain")))
        .isNull();
    assertThat(hierarchy.findInterfaceMethodRequiringDispatch(findMethod(concreteImpl, "send")))
        .isNull();
  }

  public void testGetTypeMethods_crossPackagePackagePrivateNotInherited() throws IOException {
    addPkg1Base();
    CompilationUnit unit =
        compileType(
            "pkg2.RedeclaringSub",
            """
            package pkg2;
            import java.util.List;
            import pkg1.Base;

            class EmptySub extends Base {}

            public class RedeclaringSub extends Base {
              void foo(List<String> items) {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    String sig = "foo(Ljava/util/List;)";

    TypeMethods emptyResolved = hierarchy.getTypeMethods(findType(unit, "EmptySub"));
    assertThat(emptyResolved.annotatedSignatures()).isEmpty();
    assertThat(emptyResolved.allMethods().keySet()).doesNotContain(sig);

    TypeElement redeclaringSub = findType(unit, "RedeclaringSub");
    TypeMethods redeclaringResolved = hierarchy.getTypeMethods(redeclaringSub);
    assertThat(redeclaringResolved.annotatedSignatures()).isEmpty();
    assertThat(ElementUtil.getDeclaringClass(redeclaringResolved.allMethods().get(sig).element()))
        .isEqualTo(redeclaringSub);
  }

  public void testGetTypeMethods_packagePrivateBrokenByForeignMidNotInherited() throws IOException {
    addPkg1Base();
    addSourceFile(
        """
        package pkg2;
        import pkg1.Base;
        public class Mid extends Base {}
        """,
        "pkg2/Mid.java");
    CompilationUnit unit =
        compileType(
            "pkg1.RedeclaringSub",
            """
            package pkg1;
            import java.util.List;
            import pkg2.Mid;

            class EmptySub extends Mid {}

            public class RedeclaringSub extends Mid {
              void foo(List<String> items) {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    String sig = "foo(Ljava/util/List;)";

    TypeMethods emptyResolved = hierarchy.getTypeMethods(findType(unit, "EmptySub"));
    assertThat(emptyResolved.annotatedSignatures()).isEmpty();
    assertThat(emptyResolved.allMethods().keySet()).doesNotContain(sig);

    TypeElement redeclaringSub = findType(unit, "RedeclaringSub");
    assertThat(hierarchy.getTypeMethods(redeclaringSub).annotatedSignatures()).isEmpty();
    assertThat(hierarchy.getSupertypeHierarchy(redeclaringSub, sig)).isNull();
  }

  public void testGetTypeMethods_packagePrivateWidenedAcrossSamePackageMidIsInherited()
      throws IOException {
    addPkg1Base();
    addSourceFile(
        """
        package pkg1;
        import java.util.List;
        public class WideningMid extends Base {
          @Override
          protected void foo(List<String> items) {}
        }
        """,
        "pkg1/WideningMid.java");
    CompilationUnit unit =
        compileType(
            "pkg2.OverridingSub",
            """
            package pkg2;
            import java.util.List;
            import pkg1.WideningMid;

            class InheritingSub extends WideningMid {}

            public class OverridingSub extends WideningMid {
              @Override
              public void foo(List<String> items) {}
            }
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    String sig = "foo(Ljava/util/List;)";

    TypeMethods inheritingResolved = hierarchy.getTypeMethods(findType(unit, "InheritingSub"));
    assertThat(inheritingResolved.annotatedSignatures()).containsKey(sig);
    assertThat(
            ElementUtil.getDeclaringClass(inheritingResolved.allMethods().get(sig).element())
                .getQualifiedName()
                .toString())
        .isEqualTo("pkg1.WideningMid");

    TypeElement overridingSub = findType(unit, "OverridingSub");
    TypeMethods overridingResolved = hierarchy.getTypeMethods(overridingSub);
    assertThat(overridingResolved.annotatedSignatures()).containsKey(sig);
    assertThat(ElementUtil.getDeclaringClass(overridingResolved.allMethods().get(sig).element()))
        .isEqualTo(overridingSub);
  }

  public void testGetTypeMethods_subinterfaceAnnotationOverridesSuperinterfaceAnnotation() {
    CompilationUnit unit =
        compileType(
            "ForwardDiamond",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            interface SuperI {
              @ObjectiveCKmpMethod(selector = "superHandle:", adapter = DummyAdapter.class)
              default void handle(List<String> items) {}
            }

            interface SubI extends SuperI {
              @ObjectiveCKmpMethod(selector = "subHandle:", adapter = DummyAdapter.class)
              @Override
              default void handle(List<String> items) {}
            }

            class UnannotatedBase {
              public void handle(List<String> items) {}
            }

            public class ForwardDiamond extends UnannotatedBase implements SuperI, SubI {}

            class ReverseDiamond extends UnannotatedBase implements SubI, SuperI {}
            """);
    KmpMethodHierarchy hierarchy = createHierarchy(unit);
    String sig = "handle(Ljava/util/List;)";
    ExecutableElement subHandle = findMethod(findType(unit, "SubI"), "handle");

    for (String className : ImmutableList.of("ForwardDiamond", "ReverseDiamond")) {
      TypeMethods resolved = hierarchy.getTypeMethods(findType(unit, className));
      assertThat(ElementUtil.getAnnotationValue(resolved.annotatedSignatures().get(sig), "selector"))
          .isEqualTo("subHandle:");
      assertThat(resolved.annotatedMethods().get(sig)).isEqualTo(subHandle);
    }
  }

  public void testGetTypeMethods_subinterfaceDefaultOverrideTakesPrecedence() {
    CompilationUnit unit =
        compileType(
            "Diamond",
            """
            import com.google.j2objc.annotations.ObjectiveCKmpMethod;
            import java.util.List;

            interface SuperI {
              @ObjectiveCKmpMethod(selector = "handle:", adapter = DummyAdapter.class)
              default void handle(List<String> items) {}
            }

            interface SubI extends SuperI {
              @Override
              default void handle(List<String> items) {}
            }

            public class Diamond implements SuperI, SubI {}
            """);
    TypeMethods resolved = createHierarchy(unit).getTypeMethods(findType(unit, "Diamond"));

    String sig = "handle(Ljava/util/List;)";
    assertThat(resolved.annotatedSignatures()).containsKey(sig);
    assertThat(ElementUtil.getDeclaringClass(resolved.allMethods().get(sig).element()))
        .isEqualTo(findType(unit, "SubI"));
  }
}
