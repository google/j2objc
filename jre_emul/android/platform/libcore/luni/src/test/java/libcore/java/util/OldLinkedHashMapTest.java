/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package libcore.java.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import junit.framework.TestCase;

public class OldLinkedHashMapTest extends TestCase {

    public void testLinkedHashMap() {
        // we want to test the LinkedHashMap in access ordering mode.
        LinkedHashMap map = new LinkedHashMap<String, String>(10, 0.75f, true);

        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        Iterator iterator = map.keySet().iterator();
        String id = (String) iterator.next();
        map.get(id);
        try {
            iterator.next();
            // A LinkedHashMap is supposed to throw this Exception when a
            // iterator.next() Operation takes place after a get
            // Operation. This is because the get Operation is considered
            // a structural modification if the LinkedHashMap is in
            // access order mode.
            fail("expected ConcurrentModificationException was not thrown.");
        } catch(ConcurrentModificationException expected) {
        }

        LinkedHashMap mapClone = (LinkedHashMap) map.clone();

        iterator = map.keySet().iterator();
        id = (String) iterator.next();
        mapClone.get(id);
        iterator.next();

        try {
            new LinkedHashMap<String, String>(-10, 0.75f, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            new LinkedHashMap<String, String>(10, -0.75f, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
