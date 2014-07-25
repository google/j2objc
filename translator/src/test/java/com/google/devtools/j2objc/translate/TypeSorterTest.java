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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;

import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link TypeSorter}.
 *
 * @author Keith Stanger
 */
public class TypeSorterTest extends GenerationTest {

  public void testTypeSortInnerInterface() throws IOException {
    CompilationUnit unit = translateType("Example",
        "public class Example { Foo foo; Bar bar; class Bar implements Foo {} interface Foo {} }");
    List<AbstractTypeDeclaration> types = unit.getTypes();
    assertEquals(3, types.size());

    // Expecting Foo before Bar, otherwise no sort change, one forward.
    assertEquals("Example", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
    assertEquals("Bar", types.get(2).getName().getIdentifier());
  }
}
