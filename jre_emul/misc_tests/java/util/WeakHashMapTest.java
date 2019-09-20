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

package java.util;

import com.google.j2objc.annotations.AutoreleasePool;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * iOS-specific unit tests for {@link WeakHashMap}. The Android and
 * Apache Harmony tests can't be reused because they assume GC behavior.
 * This class uses autorelease pools to manage when references have
 * weak referents.
 *
 * @author Tom Ball
 */
public class WeakHashMapTest extends TestCase {

  @Test
  public void testWeakHashMap() {
    WeakHashMap<Integer, String> weakMap = new WeakHashMap<Integer, String>();
    for (@AutoreleasePool int i = 0; i < 1; i++) {
      // Add a map entry.
      String value = "value";
      Integer key = Integer.valueOf(666);
      Object[] array = new Object[1];
      array[0] = key;
      array = null;
      weakMap.put(key, value);
      assertSame("weak map get doesn't return referent", value, weakMap.get(key));

      // Clear key, verify it's still available in the reference.
      key = null;
      assertEquals("weak map released key/value before autorelease", 1, weakMap.size());
    }

    // Verify weak reference was cleared.
    assertTrue("weakMap not empty", weakMap.isEmpty());
  }
}
