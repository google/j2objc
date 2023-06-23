package com.google.devtools.j2objc.translate;

import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.util.ArrayDeque;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Throws error if reflection code detected when metadata is stripped and ReflectionSupport is not
 * present. This prevents runtime errors from code that uses the Reflection API without the
 * developers knowledge or intent. This checker only checks detects functions that require metadata
 * and not those that use the class loader, aka name mapping, like Class.forName.
 *
 * @author Gabriel Curtis
 */
public class ReflectionCodeDetector extends UnitTreeVisitor {

  private static final ImmutableSet<String> UNSAFE_CLASS_REFLECTION_METHODS =
      ImmutableSet.of(
          "getMethods",
          "getDeclaredMethods",
          "getEnclosingClass",
          "getDeclaredAnnotations",
          "getDeclaredAnnotation",
          "getEnclosingMethod",
          "getFields",
          "getDeclaredFields",
          "getConstructor",
          "getConstructors");
  static final String UNSAFE_REFLECTION_CODE_MESSAGE =
      "Reflection method invoked when --strip-reflection flag is set."
          + " Please see go/j2objc-reflection for more info.";

  private final ArrayDeque<TypeDeclaration> typeDeclarationStack;
  
  public ReflectionCodeDetector(CompilationUnit unit) {
    super(unit);
    typeDeclarationStack = new ArrayDeque<>();
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    typeDeclarationStack.addFirst(node);
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (typeDeclarationStack.isEmpty()) {
      ErrorUtil.error(node, "This stack should never be empty");
      return;
    }
    typeDeclarationStack.removeFirst();
  }

  @Override
  public void endVisit(MethodInvocation node) {
    TypeElement nodeType = (TypeElement) ElementUtil.getDeclaringClass(node.getExecutableElement());
    ExecutableElement nodeExecutable = node.getExecutableElement();
    String nodeName = ElementUtil.getName(nodeExecutable);
    if (nodeType.equals(typeUtil.getJavaClass())
        && !translationUtil.needsReflection(typeDeclarationStack.peekFirst())) {
      if (UNSAFE_CLASS_REFLECTION_METHODS.contains(nodeName)) {
        ErrorUtil.error(node, "[" + nodeName + "] " + UNSAFE_REFLECTION_CODE_MESSAGE);
      }
    }
  }
}
