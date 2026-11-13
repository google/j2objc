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

import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.List;
import javax.lang.model.element.ExecutableElement;

/**
 * Rewrites call sites targeting {@link ObjectiveCKmpMethod} interface methods to invoke the
 * corresponding {@code *_dispatch} C helper function so Java and native Objective-C/Swift
 * implementations dispatch cleanly at runtime.
 */
final class KmpMethodInvocationRewriter extends UnitTreeVisitor {

  private final KmpMethodHierarchy methodHierarchy;
  private final KmpAdapterEmitter adapterEmitter;

  KmpMethodInvocationRewriter(
      CompilationUnit unit, KmpMethodHierarchy methodHierarchy, KmpAdapterEmitter adapterEmitter) {
    super(unit);
    this.methodHierarchy = methodHierarchy;
    this.adapterEmitter = adapterEmitter;
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
        adapterEmitter.createDispatchElement(
            ElementUtil.getDeclaringClass(targetMethod), targetMethod);
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
}
