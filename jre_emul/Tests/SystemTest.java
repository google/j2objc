/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;

import java.util.Map;

/**
 * Miscellaneous tests for java.lang.System.
 */
public class SystemTest extends TestCase {

  public void testGetenvKey() {
    assertNotNull(System.getenv("HOME"));
    assertNull(System.getenv("SOME_VARIABLE_THAT_SHOULD_NOT_EXIST"));
  }

  public void testGetEnv() {
    Map<String, String> variables = System.getenv();
    assertNotNull(variables);
    assertFalse(variables.keySet().isEmpty());
    
    // Verify an immutable map was returned.
    try {
      variables.put("SOME_KEY", "some value");
      fail("Should throw UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }
}
