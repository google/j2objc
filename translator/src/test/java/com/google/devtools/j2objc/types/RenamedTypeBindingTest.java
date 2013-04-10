/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.GenerationTest;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;

/**
 * Unit tests for the {@link RenamedTypeBinding} class.
 *
 * @author Tom Ball
 */
public class RenamedTypeBindingTest extends GenerationTest {
  private CompilationUnit unit;
  private ITypeBinding originalBinding;
  private ITypeBinding renamedBinding;

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    unit = translateType(
        "Foo", "package com.google.test; public class Foo extends java.util.Observable {}");
    originalBinding = ((TypeDeclaration) unit.types().get(0)).resolveBinding();
    renamedBinding = RenamedTypeBinding.rename("Test", originalBinding);
  }

  /**
   * Verify that a renamed binding returns the new name.
   */
  public void testGetName() {
    assertEquals("Foo", originalBinding.getName());
    assertEquals("Test", renamedBinding.getName());
  }

  /**
   * Verify that a renamed binding returns the new name with the existing
   * package.
   */
  public void testGetQualifiedName() {
    assertEquals("com.google.test.Foo", originalBinding.getQualifiedName());
    assertEquals("com.google.test.Test", renamedBinding.getQualifiedName());
  }

  /**
   * Verify that a renamed binding with a different declaring class has that
   * declaring class.
   */
  public void testGetDeclaringClass() {
    // Verify that the declaring class isn't modified by default.
    renamedBinding = RenamedTypeBinding.rename("Test2", originalBinding);
    assertEquals(originalBinding.getDeclaringClass(), renamedBinding.getDeclaringClass());

    ITypeBinding stringBinding = unit.getAST().resolveWellKnownType("java.util.String");
    renamedBinding = RenamedTypeBinding.rename("Test", stringBinding, originalBinding);
    assertEquals(stringBinding, renamedBinding.getDeclaringClass());
  }

  /**
   * Verify that a renamed binding doesn't affect other binding methods.
   */
  public void testRenameTypeBinding() {
    assertEquals(originalBinding.getAnnotations(), renamedBinding.getAnnotations());
    assertEquals(originalBinding.getBound(), renamedBinding.getBound());
    assertEquals(originalBinding.getComponentType(), renamedBinding.getComponentType());
    assertEquals(originalBinding.getDeclaredFields(), renamedBinding.getDeclaredFields());
    assertEquals(originalBinding.getDeclaredMethods(), renamedBinding.getDeclaredMethods());
    assertEquals(originalBinding.getDeclaredModifiers(), renamedBinding.getDeclaredModifiers());
    assertEquals(originalBinding.getDeclaredTypes(), renamedBinding.getDeclaredTypes());
    assertEquals(originalBinding.getDeclaringClass(), renamedBinding.getDeclaringClass());
    assertEquals(originalBinding.getDeclaringMethod(), renamedBinding.getDeclaringMethod());
    assertEquals(originalBinding.getDimensions(), renamedBinding.getDimensions());
    assertEquals(originalBinding.getElementType(), renamedBinding.getElementType());
    assertEquals(originalBinding.getInterfaces(), renamedBinding.getInterfaces());
    assertEquals(originalBinding.getJavaElement(), renamedBinding.getJavaElement());
    assertEquals(originalBinding.getKind(), renamedBinding.getKind());
    assertEquals(originalBinding.getModifiers(), renamedBinding.getModifiers());
    assertEquals(originalBinding.getPackage(), renamedBinding.getPackage());
    assertEquals(originalBinding.getRank(), renamedBinding.getRank());
    assertEquals(originalBinding.getSuperclass(), renamedBinding.getSuperclass());
    assertEquals(originalBinding.getTypeArguments(), renamedBinding.getTypeArguments());
    assertEquals(originalBinding.getTypeBounds(), renamedBinding.getTypeBounds());
    assertEquals(originalBinding.getTypeDeclaration(), renamedBinding.getTypeDeclaration());
    assertEquals(originalBinding.getTypeParameters(), renamedBinding.getTypeParameters());
    assertEquals(originalBinding.getWildcard(), renamedBinding.getWildcard());
    assertEquals(originalBinding.isAnnotation(), renamedBinding.isAnnotation());
    assertEquals(originalBinding.isAnonymous(), renamedBinding.isAnonymous());
    assertEquals(originalBinding.isArray(), renamedBinding.isArray());
    assertEquals(originalBinding.isClass(), renamedBinding.isClass());
    assertEquals(originalBinding.isEnum(), renamedBinding.isEnum());
    assertEquals(originalBinding.isInterface(), renamedBinding.isInterface());
    assertEquals(originalBinding.isLocal(), renamedBinding.isLocal());
    assertEquals(originalBinding.isMember(), renamedBinding.isMember());
    assertEquals(originalBinding.isNested(), renamedBinding.isNested());
    assertEquals(originalBinding.isParameterizedType(), renamedBinding.isParameterizedType());
    assertEquals(originalBinding.isPrimitive(), renamedBinding.isPrimitive());
    assertEquals(originalBinding.isRawType(), renamedBinding.isRawType());
    assertEquals(originalBinding.isSynthetic(), renamedBinding.isSynthetic());
    assertEquals(originalBinding.isTopLevel(), renamedBinding.isTopLevel());
    assertEquals(originalBinding.isTypeVariable(), renamedBinding.isTypeVariable());
    assertEquals(originalBinding.isWildcardType(), renamedBinding.isWildcardType());
  }
}
