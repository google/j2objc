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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;

/**
 * UnitTests for the {@link BindingUtil} class.
 *
 * @author Keith Stanger
 */
public class BindingUtilTest extends GenerationTest {

  public void testGenericParamInDeepTypeHierarchy() throws IOException {
    CompilationUnit unit = translateType("Test", "class Test { "
        + "static class A<T> { void foo(T x) {} } "
        + "static class B extends A<Double> { void foo(Double x) {} } "
        + "static class C extends B { void foo(Double x) {} } }");
    ITypeBinding cType =
        BindingUtil.findDeclaredType(Types.getTypeBinding(ASTUtil.getTypes(unit).get(0)), "C");
    assertNotNull(cType);
    IMethodBinding fooMethod = BindingUtil.findDeclaredMethod(cType, "foo", "java.lang.Double");
    assertNotNull(fooMethod);
    IMethodBinding original = BindingUtil.getOriginalMethodBinding(fooMethod);
    assertEquals("A", original.getDeclaringClass().getName());
    ITypeBinding[] params = original.getParameterTypes();
    assertEquals(1, params.length);
    assertEquals("T", params[0].getName());
  }

  public void testIsRuntimeAnnotation() throws IOException {
    // SuppressWarnings is a source-level annotation.
    CompilationUnit unit = translateType("Example", "@SuppressWarnings(\"test\") class Example {}");
    TypeDeclaration decl = (TypeDeclaration) unit.types().get(0);
    IExtendedModifier mod = ASTUtil.getModifiers(decl).get(0);
    assertTrue(mod.isAnnotation());
    assertFalse(BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(mod)));

    // Deprecated is a runtime annotation..
    unit = translateType("Example", "@Deprecated class Example {}");
    decl = (TypeDeclaration) unit.types().get(0);
    mod = ASTUtil.getModifiers(decl).get(0);
    assertTrue(mod.isAnnotation());
    assertTrue(BindingUtil.isRuntimeAnnotation(mod));
  }
}
