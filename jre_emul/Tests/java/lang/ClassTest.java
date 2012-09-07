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

package java.lang;

import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 * Command-line tests for java.lang.Class support (IOSClass)
 *
 * @author Tom Ball
 */
public class ClassTest extends TestCase {

  public int answerToLife() {
    return 42;
  }

  public void testForName() throws Exception {
    Class<?> thisClass = Class.forName("java.lang.ClassTest");
    assertNotNull(thisClass);
    assertEquals("JavaLangClassTest", thisClass.getName());
    Method answerToLife = thisClass.getMethod("answerToLife");
    Integer answer = (Integer) answerToLife.invoke(this);
    assertEquals(42, answer.intValue());
  }
}
