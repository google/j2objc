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
 * $Id: ElemMessage.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;

/**
 * Implement xsl:message.
 * <pre>
 * <!ELEMENT xsl:message %template;>
 * <!ATTLIST xsl:message
 *   %space-att;
 *   terminate (yes|no) "no"
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#message">message in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemMessage extends ElemTemplateElement
{
    static final long serialVersionUID = 1530472462155060023L;

  /**
   * If the terminate attribute has the value yes, then the
   * XSLT transformer should terminate processing after sending
   * the message. The default value is no.
   * @serial
   */
  private boolean m_terminate = Constants.ATTRVAL_NO;  // default value 

  /**
   * Set the "terminate" attribute.
   * If the terminate attribute has the value yes, then the
   * XSLT transformer should terminate processing after sending
   * the message. The default value is no.
   *
   * @param v Value to set for "terminate" attribute. 
   */
  public void setTerminate(boolean v)
  {
    m_terminate = v;
  }

  /**
   * Get the "terminate" attribute.
   * If the terminate attribute has the value yes, then the
   * XSLT transformer should terminate processing after sending
   * the message. The default value is no.
   *
   * @return value of "terminate" attribute.
   */
  public boolean getTerminate()
  {
    return m_terminate;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_MESSAGE;
  }

  /**
   * Return the node name.
   *
   * @return name of the element 
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_MESSAGE_STRING;
  }

  /**
   * Send a message to diagnostics.
   * The xsl:message instruction sends a message in a way that
   * is dependent on the XSLT transformer. The content of the xsl:message
   * instruction is a template. The xsl:message is instantiated by
   * instantiating the content to create an XML fragment. This XML
   * fragment is the content of the message.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

    String data = transformer.transformToString(this);

    transformer.getMsgMgr().message(this, data, m_terminate);
    
    if(m_terminate)
      transformer.getErrorListener().fatalError(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_STYLESHEET_DIRECTED_TERMINATION, null))); //"Stylesheet directed termination"));
  }
}
