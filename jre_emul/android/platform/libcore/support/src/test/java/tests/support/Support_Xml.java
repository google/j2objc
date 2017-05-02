/*
 * Copyright (C) 2009 The Android Open Source Project
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

package tests.support;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static junit.framework.Assert.assertEquals;

public class Support_Xml {
    public static Document domOf(String xml) throws Exception {
        // DocumentBuilderTest assumes we're using DocumentBuilder to do this parsing!
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder builder = dbf.newDocumentBuilder();

        return builder.parse(stream);
    }

    public static String firstChildTextOf(Document doc) throws Exception {
        NodeList children = doc.getFirstChild().getChildNodes();
        assertEquals(1, children.getLength());
        return children.item(0).getNodeValue();
    }

    public static Element firstElementOf(Document doc) throws Exception {
        return (Element) doc.getFirstChild();
    }

    public static String attrOf(Element e) throws Exception {
        return e.getAttribute("attr");
    }
}
