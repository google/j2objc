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
 * $Id: ProcessorOutputElem.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SystemIDResolver;
import org.xml.sax.Attributes;

/**
 * TransformerFactory for xsl:output markup.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#output">output in XSLT Specification</a>
 */
class ProcessorOutputElem extends XSLTElementProcessor
{
    static final long serialVersionUID = 3513742319582547590L;

  /** The output properties, set temporarily while the properties are 
   *  being set from the attributes, and then nulled after that operation 
   *  is completed.  */
  private OutputProperties m_outputProperties;

  /**
   * Set the cdata-section-elements property from the attribute value.
   * @see javax.xml.transform.OutputKeys#CDATA_SECTION_ELEMENTS
   * @param newValue non-null reference to processed attribute value.
   */
  public void setCdataSectionElements(java.util.Vector newValue)
  {
    m_outputProperties.setQNameProperties(OutputKeys.CDATA_SECTION_ELEMENTS, newValue);
  }

  /**
   * Set the doctype-public property from the attribute value.
   * @see javax.xml.transform.OutputKeys#DOCTYPE_PUBLIC
   * @param newValue non-null reference to processed attribute value.
   */
  public void setDoctypePublic(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC, newValue);
  }

  /**
   * Set the doctype-system property from the attribute value.
   * @see javax.xml.transform.OutputKeys#DOCTYPE_SYSTEM
   * @param newValue non-null reference to processed attribute value.
   */
  public void setDoctypeSystem(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM, newValue);
  }

  /**
   * Set the encoding property from the attribute value.
   * @see javax.xml.transform.OutputKeys#ENCODING
   * @param newValue non-null reference to processed attribute value.
   */
  public void setEncoding(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.ENCODING, newValue);
  }

  /**
   * Set the indent property from the attribute value.
   * @see javax.xml.transform.OutputKeys#INDENT
   * @param newValue non-null reference to processed attribute value.
   */
  public void setIndent(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.INDENT, newValue);
  }

  /**
   * Set the media type property from the attribute value.
   * @see javax.xml.transform.OutputKeys#MEDIA_TYPE
   * @param newValue non-null reference to processed attribute value.
   */
  public void setMediaType(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.MEDIA_TYPE, newValue);
  }

  /**
   * Set the method property from the attribute value.
   * @see javax.xml.transform.OutputKeys#METHOD
   * @param newValue non-null reference to processed attribute value.
   */
  public void setMethod(org.apache.xml.utils.QName newValue)
  {
    m_outputProperties.setQNameProperty(OutputKeys.METHOD, newValue);
  }

  /**
   * Set the omit-xml-declaration property from the attribute value.
   * @see javax.xml.transform.OutputKeys#OMIT_XML_DECLARATION
   * @param newValue processed attribute value.
   */
  public void setOmitXmlDeclaration(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.OMIT_XML_DECLARATION, newValue);
  }

  /**
   * Set the standalone property from the attribute value.
   * @see javax.xml.transform.OutputKeys#STANDALONE
   * @param newValue processed attribute value.
   */
  public void setStandalone(boolean newValue)
  {
    m_outputProperties.setBooleanProperty(OutputKeys.STANDALONE, newValue);
  }

  /**
   * Set the version property from the attribute value.
   * @see javax.xml.transform.OutputKeys#VERSION
   * @param newValue non-null reference to processed attribute value.
   */
  public void setVersion(String newValue)
  {
    m_outputProperties.setProperty(OutputKeys.VERSION, newValue);
  }
  
  /**
   * Set a foreign property from the attribute value.
   * @param newValue non-null reference to attribute value.
   */
  public void setForeignAttr(String attrUri, String attrLocalName, String attrRawName, String attrValue)
  {
    QName key = new QName(attrUri, attrLocalName);
    m_outputProperties.setProperty(key, attrValue);
  }
  
  /**
   * Set a foreign property from the attribute value.
   * @param newValue non-null reference to attribute value.
   */
  public void addLiteralResultAttribute(String attrUri, String attrLocalName, String attrRawName, String attrValue)
  {
    QName key = new QName(attrUri, attrLocalName);
    m_outputProperties.setProperty(key, attrValue);
  }

  /**
   * Receive notification of the start of an xsl:output element.
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
   *
   * @throws org.xml.sax.SAXException
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    // Hmmm... for the moment I don't think I'll have default properties set for this. -sb
    m_outputProperties = new OutputProperties();

    m_outputProperties.setDOMBackPointer(handler.getOriginatingNode());
    m_outputProperties.setLocaterInfo(handler.getLocator());
    m_outputProperties.setUid(handler.nextUid());
    setPropertiesFromAttributes(handler, rawName, attributes, this);
    
    // Access this only from the Hashtable level... we don't want to 
    // get default properties.
    String entitiesFileName =
      (String) m_outputProperties.getProperties().get(OutputPropertiesFactory.S_KEY_ENTITIES);

    if (null != entitiesFileName)
    {
      try
      {
        String absURL = SystemIDResolver.getAbsoluteURI(entitiesFileName,
                    handler.getBaseIdentifier());
        m_outputProperties.getProperties().put(OutputPropertiesFactory.S_KEY_ENTITIES, absURL);
      }
      catch(TransformerException te)
      {
        handler.error(te.getMessage(), te);
      }
    }
    
    handler.getStylesheet().setOutput(m_outputProperties);
    
    ElemTemplateElement parent = handler.getElemTemplateElement();
    parent.appendChild(m_outputProperties);
    
    m_outputProperties = null;
  }
}
