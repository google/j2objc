/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.transform.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.xml.transform.dom.DOMSource;
import org.apache.harmony.xml.dom.CDATASectionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Node;

@RunWith(JUnit4.class)
public class DOMSourceTest {

    @Test
    public void constructor() {
        DOMSource source = new DOMSource();
        assertNotNull(source);
        assertNull(source.getNode());
        assertNull(source.getSystemId());
    }

    @Test
    public void constructorWithNodeAndString() {
        Node node = new CDATASectionImpl(null, "");
        DOMSource source = new DOMSource(node, "systemId");
        assertEquals(node, source.getNode());
        assertEquals("systemId", source.getSystemId());
    }

    @Test
    public void setSystemId() {
        DOMSource source = new DOMSource();
        assertNull(source.getSystemId());

        source.setSystemId("systemId");
        assertEquals("systemId", source.getSystemId());
    }
}
