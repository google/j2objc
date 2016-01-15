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
 * $Id: ProcessorStylesheetElement.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetComposed;
import org.apache.xalan.templates.StylesheetRoot;

import org.xml.sax.Attributes;

/**
 * TransformerFactory for xsl:stylesheet or xsl:transform markup.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#stylesheet-element">stylesheet-element in XSLT Specification</a>
 * 
 * @xsl.usage internal
 */
public class ProcessorStylesheetElement extends XSLTElementProcessor
{
    static final long serialVersionUID = -877798927447840792L;

  /**
   * Receive notification of the start of an strip-space element.
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
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

		super.startElement(handler, uri, localName, rawName, attributes);
    try
    {
      int stylesheetType = handler.getStylesheetType();
      Stylesheet stylesheet;

      if (stylesheetType == StylesheetHandler.STYPE_ROOT)
      {
        try
        {
          stylesheet = getStylesheetRoot(handler);
        }
        catch(TransformerConfigurationException tfe)
        {
          throw new TransformerException(tfe);
        }
      }
      else
      {
        Stylesheet parent = handler.getStylesheet();

        if (stylesheetType == StylesheetHandler.STYPE_IMPORT)
        {
          StylesheetComposed sc = new StylesheetComposed(parent);

          parent.setImport(sc);

          stylesheet = sc;
        }
        else
        {
          stylesheet = new Stylesheet(parent);

          parent.setInclude(stylesheet);
        }
      }

      stylesheet.setDOMBackPointer(handler.getOriginatingNode());
      stylesheet.setLocaterInfo(handler.getLocator());

      stylesheet.setPrefixes(handler.getNamespaceSupport());
      handler.pushStylesheet(stylesheet);
      setPropertiesFromAttributes(handler, rawName, attributes,
                                  handler.getStylesheet());
      handler.pushElemTemplateElement(handler.getStylesheet());
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * This method can be over-ridden by a class that extends this one.
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   */
  protected Stylesheet getStylesheetRoot(StylesheetHandler handler) throws TransformerConfigurationException
  {
    StylesheetRoot stylesheet;
    stylesheet = new StylesheetRoot(handler.getSchema(), handler.getStylesheetProcessor().getErrorListener());
    
    if (handler.getStylesheetProcessor().isSecureProcessing())
      stylesheet.setSecureProcessing(true);
    
    return stylesheet;
  }

/**
   * Receive notification of the end of an element.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   */
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
		super.endElement(handler, uri, localName, rawName);
    handler.popElemTemplateElement();
    handler.popStylesheet();
  }
}
