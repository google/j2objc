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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Set;

/**
 * Unit tests for {@link HiddenFieldDetector}.
 *
 * @author Tom Ball
 */
public class HiddenFieldDetectorTest extends GenerationTest {

  public void testFieldHidingParameter() {
    String source = "import java.util.*; public class Test {" +
        "private static class CheckedCollection<E> extends AbstractCollection<E> {" +
        "  Collection<E> c;" +
        "  public CheckedCollection(Collection<E> c_) { this.c = c_; }" +
        "  public int size() { return 0; }" +
        "  public Iterator<E> iterator() { return null; }}}";
    CompilationUnit unit = translateType("Test", source);
    Set<IVariableBinding> hiddenFields = HiddenFieldDetector.getFieldNameConflicts(unit);
    assertEquals(1, hiddenFields.size());
    IVariableBinding param = hiddenFields.iterator().next();
    assertEquals("c_", param.getName());
    assertTrue(param.isParameter());
  }
}
