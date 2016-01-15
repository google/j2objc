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
 * $Id: ProcessorCharacters.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemText;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xml.utils.XMLCharacterRecognizer;

import org.w3c.dom.Node;

/**
 * This class processes character events for a XSLT template element.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#section-Creating-the-Result-Tree">section-Creating-the-Result-Tree in XSLT Specification</a>
 */
public class ProcessorCharacters extends XSLTElementProcessor
{
    static final long serialVersionUID = 8632900007814162650L;

  /**
   * Receive notification of the start of the non-text event.  This
   * is sent to the current processor when any non-text event occurs.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   */
  public void startNonText(StylesheetHandler handler) throws org.xml.sax.SAXException
  {
    if (this == handler.getCurrentProcessor())
    {
      handler.popProcessor();
    }

    int nChars = m_accumulator.length();

    if ((nChars > 0)
            && ((null != m_xslTextElement)
                ||!XMLCharacterRecognizer.isWhiteSpace(m_accumulator)) 
                || handler.isSpacePreserve())
    {
      ElemTextLiteral elem = new ElemTextLiteral();

      elem.setDOMBackPointer(m_firstBackPointer);
      elem.setLocaterInfo(handler.getLocator());
      try
      {
        elem.setPrefixes(handler.getNamespaceSupport());
      }
      catch(TransformerException te)
      {
        throw new org.xml.sax.SAXException(te);
      }

      boolean doe = (null != m_xslTextElement)
                    ? m_xslTextElement.getDisableOutputEscaping() : false;

      elem.setDisableOutputEscaping(doe);
      elem.setPreserveSpace(true);

      char[] chars = new char[nChars];

      m_accumulator.getChars(0, nChars, chars, 0);
      elem.setChars(chars);

      ElemTemplateElement parent = handler.getElemTemplateElement();

      parent.appendChild(elem);
    }

    m_accumulator.setLength(0);
    m_firstBackPointer = null;
  }
  
  protected Node m_firstBackPointer = null;

  /**
   * Receive notification of character data inside an element.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param ch The characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#characters
   */
  public void characters(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {

    m_accumulator.append(ch, start, length);
    
    if(null == m_firstBackPointer)
      m_firstBackPointer = handler.getOriginatingNode();

    // Catch all events until a non-character event.
    if (this != handler.getCurrentProcessor())
      handler.pushProcessor(this);
  }

  /**
   * Receive notification of the end of an element.
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
   * @see org.apache.xalan.processor.StylesheetHandler#startElement
   * @see org.apache.xalan.processor.StylesheetHandler#endElement
   * @see org.xml.sax.ContentHandler#startElement
   * @see org.xml.sax.ContentHandler#endElement
   * @see org.xml.sax.Attributes
   */
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {

    // Since this has been installed as the current processor, we 
    // may get and end element event, in which case, we pop and clear 
    // and then call the real element processor.
    startNonText(handler);
    handler.getCurrentProcessor().endElement(handler, uri, localName,
                                             rawName);
    handler.popProcessor();
  }

  /**
   * Accumulate characters, until a non-whitespace event has
   * occured.
   */
  private StringBuffer m_accumulator = new StringBuffer();

  /**
   * The xsl:text processor will call this to set a
   * preserve space state.
   */
  private ElemText m_xslTextElement;

  /**
   * Set the current setXslTextElement. The xsl:text 
   * processor will call this to set a preserve space state.
   *
   * @param xslTextElement The current xslTextElement that 
   *                       is preserving state, or null.
   */
  void setXslTextElement(ElemText xslTextElement)
  {
    m_xslTextElement = xslTextElement;
  }
}
