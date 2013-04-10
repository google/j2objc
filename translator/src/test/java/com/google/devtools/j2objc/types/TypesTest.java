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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import java.io.IOException;

/**
 * UnitTests for the {@link Types} class.
 *
 * @author Tom Ball
 */
public class TypesTest extends GenerationTest {
  private CompilationUnit unit;
  private AST ast;

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    // Compile any file to initialize Types instance.
    unit = translateType("Example", "public class Example {}");
    ast = unit.getAST();
  }

  public void testMakeType() throws IOException {
    ITypeBinding binding = Types.getTypeBinding(unit.types().get(0));
    Type type = Types.makeType(binding);
    assertTrue(type instanceof SimpleType);
    SimpleType simpleType = (SimpleType) type;
    assertEquals("Example", simpleType.getName().getFullyQualifiedName());
  }

  public void testMakePrimitiveType() throws IOException {
    ITypeBinding binding = ast.resolveWellKnownType("int");
    Type type = Types.makeType(binding);
    assertTrue(type instanceof PrimitiveType);
    PrimitiveType primitiveType = (PrimitiveType) type;
    assertEquals(PrimitiveType.INT, primitiveType.getPrimitiveTypeCode());
  }
}
