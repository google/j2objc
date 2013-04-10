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

/**
 * Unit tests for the {@link ModifiedTypeBinding} class.
 *
 * @author Tom Ball
 */
public class ModifiedTypeBindingTest extends GenerationTest {

  public void testGetSuperclass() throws Exception {
    CompilationUnit unit = translateType(
        "Foo", "public class Foo extends java.util.Observable {}");
    TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
    type.setSuperclassType(null);
    ITypeBinding originalBinding = type.resolveBinding();
    ITypeBinding modifiedBinding = ModifiedTypeBinding.bind(type);
    assertNotNull(originalBinding.getSuperclass());
    assertNull(modifiedBinding.getSuperclass());
  }

  public void testGetInterfaces() throws Exception {
    CompilationUnit unit = translateType(
        "Foo", "public class Foo implements Cloneable, java.io.Serializable {}");
    TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
    ITypeBinding originalBinding = type.resolveBinding();
    ITypeBinding[] interfaces = originalBinding.getInterfaces();
    assertEquals(2, interfaces.length);
    assertEquals("Cloneable", interfaces[0].getName());
    assertEquals("Serializable", interfaces[1].getName());

    type.superInterfaceTypes().remove(0);
    ITypeBinding modifiedBinding = ModifiedTypeBinding.bind(type);
    interfaces = modifiedBinding.getInterfaces();
    assertEquals(1, interfaces.length);
    assertEquals("Serializable", interfaces[0].getName());
  }
}

