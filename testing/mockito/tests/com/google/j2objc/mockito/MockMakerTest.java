// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.j2objc.mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Unit tests for the iOS Mockito MockMaker plugin. */
@RunWith(JUnit4.class)
public class MockMakerTest {

  @Test
  public void isAssignableFromMockObject() {
    ClassA mockClassA = Mockito.mock(ClassA.class);
    assertTrue(ClassA.class.isAssignableFrom(mockClassA.getClass()));
  }

  @Test
  public void whenThenReturnMockObject() {
    InterfaceA mockInterfaceA = Mockito.mock(InterfaceA.class);
    ClassA mockClassA = Mockito.mock(ClassA.class);
    when(mockInterfaceA.getClassA()).thenReturn(mockClassA);
    Object cls = mockInterfaceA.getClassA();
    assertEquals(cls, mockClassA);
  }

  @Test
  public void shouldInstanceOfOriginalClass_true() {
    Object mockClassA = Mockito.mock(ClassA.class);
    assertTrue(mockClassA instanceof ClassA);
  }

  @Test
  public void shouldInstanceOfOriginalInterface_true() {
    Object mockInterfaceA = Mockito.mock(InterfaceA.class);
    assertTrue(mockInterfaceA instanceof InterfaceA);
  }

  static class ClassA {}

  static interface InterfaceA {
    ClassA getClassA();
  }
}
