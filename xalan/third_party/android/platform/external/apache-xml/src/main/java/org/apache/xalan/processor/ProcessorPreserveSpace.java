/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * $Id: ProcessorPreserveSpace.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import java.util.Vector;

import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.WhiteSpaceInfo;
import org.apache.xpath.XPath;

import org.xml.sax.Attributes;

/**
 * TransformerFactory for xsl:preserve-space markup.
 * <pre>
 * <!ELEMENT xsl:preserve-space EMPTY>
 * <!ATTLIST xsl:preserve-space elements CDATA #REQUIRED>
 * </pre>
 */
class ProcessorPreserveSpace extends XSLTElementProcessor
{
    static final long serialVersionUID = -5552836470051177302L;

  /**
   * Receive notification of the start of an preserve-space element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param attributes The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, 
          Attributes attributes)
            throws org.xml.sax.SAXException
  {
    Stylesheet thisSheet = handler.getStylesheet();
	WhitespaceInfoPaths paths = new WhitespaceInfoPaths(thisSheet);
    setPropertiesFromAttributes(handler, rawName, attributes, paths);

    Vector xpaths = paths.getElements();

    for (int i = 0; i < xpaths.size(); i++)
    {
      WhiteSpaceInfo wsi = new WhiteSpaceInfo((XPath) xpaths.elementAt(i), false, thisSheet);
      wsi.setUid(handler.nextUid());

      thisSheet.setPreserveSpaces(wsi);
    }
    paths.clearElements();
  }
}
