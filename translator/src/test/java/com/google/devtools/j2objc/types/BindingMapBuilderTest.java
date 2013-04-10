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

/**
 * Unit tests for the {@link BindingMapBuilder} class.
 *
 * @author Tom Ball
 */
public class BindingMapBuilderTest extends GenerationTest {

  public void testContinueWithLabel() {
    String source =
        "class Test { " +
        "  public void foo() { " +
        "    testLabel: for (Object o: new Object[] { }) { " +
        "      continue testLabel;" +
        "    }" +
        "  }" +
        "}";
    translateType("Test", source);
  }
}
