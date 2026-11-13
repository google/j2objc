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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.ExecutableElement;

/** Unit tests for {@link KmpMethodInvocationRewriter}. */
public class KmpMethodInvocationRewriterTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    addSourceFile(
        """
        import java.util.List;

        public class Adapter {
          public static native List<?> toJavaUtilList(Object list) /*-[ return nil; ]-*/;
          public static native Object fromJavaUtilList(List<?> list) /*-[ return nil; ]-*/;
        }
        """,
        "Adapter.java");
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public interface BaseProto {
          @ObjectiveCKmpMethod(selector = "acceptItems:", adapter = Adapter.class)
          void accept(List<?> items);

          @ObjectiveCKmpMethod(selector = "computeItems:", adapter = Adapter.class)
          List<?> compute(List<?> items);
        }
        """,
        "BaseProto.java");
    addSourceFile("public interface SubProto extends BaseProto {}", "SubProto.java");
  }

  private void runRewriter(CompilationUnit unit) {
    var env = unit.getEnv();
    KmpTypeConverter converter =
        new KmpTypeConverter(env.typeUtil(), env.nameTable(), env.options());
    KmpMethodHierarchy hierarchy = new KmpMethodHierarchy(env.typeUtil(), env.elementUtil());
    KmpAdapterEmitter emitter =
        new KmpAdapterEmitter(env.typeUtil(), env.nameTable(), env.options(), converter);
    new KmpMethodInvocationRewriter(unit, hierarchy, emitter).run();
  }

  private static MethodDeclaration findMethod(
      CompilationUnit unit, String typeName, String methodName) {
    for (var typeDecl : unit.getTypes()) {
      if (typeDecl.getTypeElement().getSimpleName().contentEquals(typeName)) {
        for (MethodDeclaration md : TreeUtil.getMethodDeclarations(typeDecl)) {
          if (ElementUtil.getName(md.getExecutableElement()).equals(methodName)) {
            return md;
          }
        }
      }
    }
    throw new AssertionError("Method not found: " + typeName + "#" + methodName);
  }

  public void testRewritesExplicitReceiverCallToDispatchFunction() {
    CompilationUnit unit =
        compileType(
            "Caller",
            """
            import java.util.List;

            public class Caller {
              public void call(BaseProto proto, List<?> items) {
                proto.accept(items);
              }
            }
            """);

    runRewriter(unit);

    MethodDeclaration callMethod = findMethod(unit, "Caller", "call");
    ExpressionStatement stmt = (ExpressionStatement) callMethod.getBody().getStatements().get(0);
    assertThat(stmt.getExpression()).isInstanceOf(FunctionInvocation.class);

    FunctionInvocation dispatchCall = (FunctionInvocation) stmt.getExpression();
    assertThat(dispatchCall.getFunctionElement().getName())
        .isEqualTo("BaseProto_acceptWithJavaUtilList__dispatch");
    assertThat(dispatchCall.getArguments()).hasSize(2);
    assertThat(((SimpleName) dispatchCall.getArguments().get(0)).getIdentifier())
        .isEqualTo("proto");
    assertThat(((SimpleName) dispatchCall.getArguments().get(1)).getIdentifier())
        .isEqualTo("items");
  }

  public void testRewritesImplicitThisReceiverCallToDispatchWithSynthesizedThis() {
    CompilationUnit unit =
        compileType(
            "AbstractImpl",
            """
            public abstract class AbstractImpl implements BaseProto {
              public void trigger() {
                accept(null);
              }
            }
            """);

    runRewriter(unit);

    MethodDeclaration trigger = findMethod(unit, "AbstractImpl", "trigger");
    ExpressionStatement stmt = (ExpressionStatement) trigger.getBody().getStatements().get(0);
    FunctionInvocation dispatchCall = (FunctionInvocation) stmt.getExpression();
    assertThat(dispatchCall.getFunctionElement().getName())
        .isEqualTo("BaseProto_acceptWithJavaUtilList__dispatch");
    assertThat(dispatchCall.getArguments()).hasSize(2);
    assertThat(dispatchCall.getArguments().get(0)).isInstanceOf(ThisExpression.class);
    ThisExpression thisExpr = (ThisExpression) dispatchCall.getArguments().get(0);
    assertThat(thisExpr.getTypeMirror().toString()).isEqualTo("AbstractImpl");
    assertThat(dispatchCall.getArguments().get(1)).isInstanceOf(NullLiteral.class);
  }

  public void testRewritesSubinterfaceInheritedMethodCallToDeclaringInterfaceDispatch() {
    CompilationUnit unit =
        compileType(
            "Caller",
            """
            import java.util.List;

            public class Caller {
              public void call(SubProto sub, List<?> items) {
                sub.accept(items);
              }
            }
            """);

    runRewriter(unit);

    MethodDeclaration call = findMethod(unit, "Caller", "call");
    ExpressionStatement stmt = (ExpressionStatement) call.getBody().getStatements().get(0);
    FunctionInvocation dispatchCall = (FunctionInvocation) stmt.getExpression();
    assertThat(dispatchCall.getFunctionElement().getName())
        .isEqualTo("BaseProto_acceptWithJavaUtilList__dispatch");
  }

  public void testLeavesConcreteNonInterfaceAndStaticCallsUntouched() throws IOException {
    addSourceFile(
        """
        import com.google.j2objc.annotations.ObjectiveCKmpMethod;
        import java.util.List;

        public class PlainClass {
          @ObjectiveCKmpMethod(selector = "doLocal:", adapter = Adapter.class)
          public void doLocal(List<?> items) {}

          @ObjectiveCKmpMethod(selector = "doStatic:", adapter = Adapter.class)
          public static void doStatic(List<?> items) {}
        }
        """,
        "PlainClass.java");
    addSourceFile("public interface UnannotatedProto { void ping(); }", "UnannotatedProto.java");
    CompilationUnit unit =
        compileType(
            "Caller",
            """
            public class Caller {
              public void call(PlainClass plain, UnannotatedProto proto) {
                plain.doLocal(null);
                PlainClass.doStatic(null);
                proto.ping();
              }
            }
            """);

    runRewriter(unit);

    MethodDeclaration call = findMethod(unit, "Caller", "call");
    List<Statement> stmts = call.getBody().getStatements();
    assertThat(stmts).hasSize(3);
    for (Statement stmt : stmts) {
      ExpressionStatement exprStmt = (ExpressionStatement) stmt;
      assertThat(exprStmt.getExpression()).isInstanceOf(MethodInvocation.class);
    }
  }

  public void testRewritesNestedMethodInvocationArgumentsAndReceiver() {
    CompilationUnit unit =
        compileType(
            "Caller",
            """
            import java.util.List;

            public class Caller {
              private BaseProto getProto() {
                return null;
              }

              public List<?> call(BaseProto proto, List<?> items) {
                return getProto().compute(proto.compute(items));
              }
            }
            """);

    runRewriter(unit);

    MethodDeclaration call = findMethod(unit, "Caller", "call");
    ReturnStatement returnStmt = (ReturnStatement) call.getBody().getStatements().get(0);
    assertThat(returnStmt.getExpression()).isInstanceOf(FunctionInvocation.class);

    FunctionInvocation outerDispatch = (FunctionInvocation) returnStmt.getExpression();
    assertThat(outerDispatch.getFunctionElement().getName())
        .isEqualTo("BaseProto_computeWithJavaUtilList__dispatch");
    assertThat(outerDispatch.getArguments()).hasSize(2);
    assertThat(outerDispatch.getArguments().get(0)).isInstanceOf(MethodInvocation.class);
    ExecutableElement receiverMethod =
        ((MethodInvocation) outerDispatch.getArguments().get(0)).getExecutableElement();
    assertThat(ElementUtil.getName(receiverMethod)).isEqualTo("getProto");

    assertThat(outerDispatch.getArguments().get(1)).isInstanceOf(FunctionInvocation.class);
    FunctionInvocation innerDispatch = (FunctionInvocation) outerDispatch.getArguments().get(1);
    assertThat(innerDispatch.getFunctionElement().getName())
        .isEqualTo("BaseProto_computeWithJavaUtilList__dispatch");
  }
}
