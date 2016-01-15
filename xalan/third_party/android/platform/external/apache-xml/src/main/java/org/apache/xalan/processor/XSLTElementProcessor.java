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
 * $Id: XSLTElementProcessor.java 469688 2006-10-31 22:39:43Z minchau $
 */
package org.apache.xalan.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.IntStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class acts as the superclass for all stylesheet element
 * processors, and deals with things that are common to all elements.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 */
public class XSLTElementProcessor extends ElemTemplateElement
{
    static final long serialVersionUID = 5597421564955304421L;

  /**
   * Construct a processor for top-level elements.
   * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
   */
  XSLTElementProcessor(){}
	
	private IntStack m_savedLastOrder;

  /**
   * The element definition that this processor conforms to.
   */
  private XSLTElementDef m_elemDef;

  /**
   * Get the element definition that belongs to this element.
   *
   * @return The element definition object that produced and constrains this element.
   */
  XSLTElementDef getElemDef()
  {
    return m_elemDef;
  }

  /**
   * Set the element definition that belongs to this element.
   *
   * @param def The element definition object that produced and constrains this element.
   */
  void setElemDef(XSLTElementDef def)
  {
    m_elemDef = def;
  }

  /**
   * Resolve an external entity.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param publicId The public identifer, or null if none is
   *                 available.
   * @param systemId The system identifier provided in the XML
   *                 document.
   * @return The new input source, or null to require the
   *         default behaviour.
   */
  public InputSource resolveEntity(
          StylesheetHandler handler, String publicId, String systemId)
            throws org.xml.sax.SAXException
  {
    return null;
  }

  /**
   * Receive notification of a notation declaration.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param name The notation name.
   * @param publicId The notation public identifier, or null if not
   *                 available.
   * @param systemId The notation system identifier.
   * @see org.xml.sax.DTDHandler#notationDecl
   */
  public void notationDecl(StylesheetHandler handler, String name,
                           String publicId, String systemId)
  {

    // no op
  }

  /**
   * Receive notification of an unparsed entity declaration.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param name The entity name.
   * @param publicId The entity public identifier, or null if not
   *                 available.
   * @param systemId The entity system identifier.
   * @param notationName The name of the associated notation.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void unparsedEntityDecl(StylesheetHandler handler, String name,
                                 String publicId, String systemId,
                                 String notationName)
  {

    // no op
  }

  /**
   * Receive notification of the start of the non-text event.  This
   * is sent to the current processor when any non-text event occurs.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   */
  public void startNonText(StylesheetHandler handler) throws org.xml.sax.SAXException
  {

    // no op
  }

  /**
   * Receive notification of the start of an element.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param attributes The specified or defaulted attributes.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

    if (m_savedLastOrder == null)
				m_savedLastOrder = new IntStack();
			m_savedLastOrder.push(getElemDef().getLastOrder());
			getElemDef().setLastOrder(-1);
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
		if (m_savedLastOrder != null && !m_savedLastOrder.empty())
			getElemDef().setLastOrder(m_savedLastOrder.pop());
		
		if (!getElemDef().getRequiredFound())
			handler.error(XSLTErrorResources.ER_REQUIRED_ELEM_NOT_FOUND, new Object[]{getElemDef().getRequiredElem()}, null);
  }

  /**
   * Receive notification of character data inside an element.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param ch The characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   */
  public void characters(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {
    handler.error(XSLTErrorResources.ER_CHARS_NOT_ALLOWED, null, null);//"Characters are not allowed at this point in the document!",
                  //null);
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param ch The whitespace characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   */
  public void ignorableWhitespace(
          StylesheetHandler handler, char ch[], int start, int length)
            throws org.xml.sax.SAXException
  {

    // no op
  }

  /**
   * Receive notification of a processing instruction.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if
   *             none is supplied.
   */
  public void processingInstruction(
          StylesheetHandler handler, String target, String data)
            throws org.xml.sax.SAXException
  {

    // no op
  }

  /**
   * Receive notification of a skipped entity.
   *
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param name The name of the skipped entity.
   */
  public void skippedEntity(StylesheetHandler handler, String name)
          throws org.xml.sax.SAXException
  {

    // no op
  }

  /**
   * Set the properties of an object from the given attribute list.
   * @param handler The stylesheet's Content handler, needed for
   *                error reporting.
   * @param rawName The raw name of the owner element, needed for
   *                error reporting.
   * @param attributes The list of attributes.
   * @param target The target element where the properties will be set.
   */
  void setPropertiesFromAttributes(
          StylesheetHandler handler, String rawName, Attributes attributes, 
          ElemTemplateElement target)
            throws org.xml.sax.SAXException
  {
    setPropertiesFromAttributes(handler, rawName, attributes, target, true);
  }

  /**
   * Set the properties of an object from the given attribute list.
   * @param handler The stylesheet's Content handler, needed for
   *                error reporting.
   * @param rawName The raw name of the owner element, needed for
   *                error reporting.
   * @param attributes The list of attributes.
   * @param target The target element where the properties will be set.
   * @param throwError True if it should throw an error if an
   * attribute is not defined.
   * @return the attributes not allowed on this element.
   *
   * @throws TransformerException
   */
  Attributes setPropertiesFromAttributes(
          StylesheetHandler handler, String rawName, Attributes attributes, 
          ElemTemplateElement target, boolean throwError)
            throws org.xml.sax.SAXException
  {

    XSLTElementDef def = getElemDef();
    AttributesImpl undefines = null;
    boolean isCompatibleMode = ((null != handler.getStylesheet() 
                                 && handler.getStylesheet().getCompatibleMode())
                                || !throwError);
    if (isCompatibleMode)
      undefines = new AttributesImpl();


    // Keep track of which XSLTAttributeDefs have been processed, so 
    // I can see which default values need to be set.
    List processedDefs = new ArrayList();

    // Keep track of XSLTAttributeDefs that were invalid
    List errorDefs = new ArrayList();    
    int nAttrs = attributes.getLength();

    for (int i = 0; i < nAttrs; i++)
    {
      String attrUri = attributes.getURI(i);
      // Hack for Crimson.  -sb
      if((null != attrUri) && (attrUri.length() == 0)
                           && (attributes.getQName(i).startsWith("xmlns:") || 
                               attributes.getQName(i).equals("xmlns")))
      {
        attrUri = org.apache.xalan.templates.Constants.S_XMLNAMESPACEURI;
      }
      String attrLocalName = attributes.getLocalName(i);
      XSLTAttributeDef attrDef = def.getAttributeDef(attrUri, attrLocalName);

      if (null == attrDef)
      {
        if (!isCompatibleMode)
        {

          // Then barf, because this element does not allow this attribute.
          handler.error(XSLTErrorResources.ER_ATTR_NOT_ALLOWED, new Object[]{attributes.getQName(i), rawName}, null);//"\""+attributes.getQName(i)+"\""
                        //+ " attribute is not allowed on the " + rawName
                       // + " element!", null);
        }
        else
        {
          undefines.addAttribute(attrUri, attrLocalName,
                                 attributes.getQName(i),
                                 attributes.getType(i),
                                 attributes.getValue(i));
        }
      }
      else
      {
        //handle secure processing
        if(handler.getStylesheetProcessor()==null)
            System.out.println("stylesheet processor null");
        if(attrDef.getName().compareTo("*")==0 && handler.getStylesheetProcessor().isSecureProcessing())
        {
            //foreign attributes are not allowed in secure processing mode
            // Then barf, because this element does not allow this attribute.
            handler.error(XSLTErrorResources.ER_ATTR_NOT_ALLOWED, new Object[]{attributes.getQName(i), rawName}, null);//"\""+attributes.getQName(i)+"\""
            //+ " attribute is not allowed on the " + rawName
            // + " element!", null);
        }
        else
        {


            boolean success = attrDef.setAttrValue(handler, attrUri, attrLocalName,
                                 attributes.getQName(i), attributes.getValue(i),
                                 target);

            // Now we only add the element if it passed a validation check
            if (success)
                processedDefs.add(attrDef);
            else
                errorDefs.add(attrDef);
        }
      }
    }

    XSLTAttributeDef[] attrDefs = def.getAttributes();
    int nAttrDefs = attrDefs.length;

    for (int i = 0; i < nAttrDefs; i++)
    {
      XSLTAttributeDef attrDef = attrDefs[i];
      String defVal = attrDef.getDefault();

      if (null != defVal)
      {
        if (!processedDefs.contains(attrDef))
        {
          attrDef.setDefAttrValue(handler, target);
        }
      }

      if (attrDef.getRequired())
      {
        if ((!processedDefs.contains(attrDef)) && (!errorDefs.contains(attrDef)))
          handler.error(
            XSLMessages.createMessage(
              XSLTErrorResources.ER_REQUIRES_ATTRIB, new Object[]{ rawName,
                                                                   attrDef.getName() }), null);
      }
    }

    return undefines;
  }
}
