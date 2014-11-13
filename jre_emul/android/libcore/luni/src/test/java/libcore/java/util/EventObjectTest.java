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

import java.util.EventObject;
import junit.framework.TestCase;
import libcore.util.SerializationTester;

public final class EventObjectTest extends TestCase {

    public void testConstructor() {
        try {
            new EventObject(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetSource() {
        Object source = new Object();
        assertSame(source, new EventObject(source).getSource());
    }

    public void testToString() {
        assertEquals("java.util.EventObject[source=x]", new EventObject("x").toString());
    }

    public void testSerializationNullsOutSource() {
        String s = "aced0005737200156a6176612e7574696c2e4576656e744f626a6563744"
                + "c8d094e186d7da80200007870";
        Object source = new Object();
        EventObject eventObject = new EventObject(source);
        new SerializationTester<EventObject>(eventObject, s) {
            @Override protected boolean equals(EventObject a, EventObject b) {
                return a.getSource() == null
                        || b.getSource() == null
                        || a.getSource() == b.getSource();
            }
        }.test();
    }
}
