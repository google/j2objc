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
 * $Id: ElemExtensionCall.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExtensionHandler;
import org.apache.xalan.extensions.ExtensionsTable;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

/**
 * Implement an extension element.
 * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemExtensionCall extends ElemLiteralResult
{
    static final long serialVersionUID = 3171339708500216920L;

  /** The Namespace URI for this extension call element.
   *  @serial          */
  String m_extns;

  /** Language used by extension.
   *  @serial          */
  String m_lang;

  /** URL pointing to extension.
   *  @serial          */
  String m_srcURL;

  /** Source for script.
   *  @serial          */
  String m_scriptSrc;

  /** Declaration for Extension element. 
   *  @serial          */
  ElemExtensionDecl m_decl = null;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   *@return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONCALL;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */

  // public String getNodeName()
  // {
  // TODO: Need prefix.
  // return localPart;
  // }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    m_extns = this.getNamespace();   
    m_decl = getElemExtensionDecl(sroot, m_extns);
    // Register the extension namespace if the extension does not have
    // an ElemExtensionDecl ("component").
    if (m_decl == null)
      sroot.getExtensionNamespacesManager().registerExtension(m_extns);
  }
 
  /**
   * Return the ElemExtensionDecl for this extension element 
   *
   *
   * @param stylesheet Stylesheet root associated with this extension element
   * @param namespace Namespace associated with this extension element
   *
   * @return the ElemExtensionDecl for this extension element. 
   */
  private ElemExtensionDecl getElemExtensionDecl(StylesheetRoot stylesheet,
          String namespace)
  {

    ElemExtensionDecl decl = null;
    int n = stylesheet.getGlobalImportCount();

    for (int i = 0; i < n; i++)
    {
      Stylesheet imported = stylesheet.getGlobalImport(i);

      for (ElemTemplateElement child = imported.getFirstChildElem();
              child != null; child = child.getNextSiblingElem())
      {
        if (Constants.ELEMNAME_EXTENSIONDECL == child.getXSLToken())
        {
          decl = (ElemExtensionDecl) child;

          String prefix = decl.getPrefix();
          String declNamespace = child.getNamespaceForPrefix(prefix);

          if (namespace.equals(declNamespace))
          {
            return decl;
          }
        }
      }
    }

    return null;
  }
  
  /**
   * Execute the fallbacks when an extension is not available.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  private void executeFallbacks(
          TransformerImpl transformer)
            throws TransformerException
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
      {
        try
        {
          transformer.pushElemTemplateElement(child);
          ((ElemFallback) child).executeFallback(transformer);
        }
        finally
        {
          transformer.popElemTemplateElement();
        }
      }
    }

  }
  
  /**
   * Return true if this extension element has a <xsl:fallback> child element.
   *
   * @return true if this extension element has a <xsl:fallback> child element.
   */
  private boolean hasFallbackChildren()
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
        return true;
    }
    
    return false;
  }


  /**
   * Execute an extension.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.getStylesheet().isSecureProcessing())
      throw new TransformerException(
        XSLMessages.createMessage(
          XSLTErrorResources.ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
          new Object[] {getRawName()}));
          
    try
    {
      transformer.getResultTreeHandler().flushPending();

      ExtensionsTable etable = transformer.getExtensionsTable();
      ExtensionHandler nsh = etable.get(m_extns);

      if (null == nsh)
      {
        if (hasFallbackChildren())
        {
          executeFallbacks(transformer);
        }
        else
        {
	  TransformerException te = new TransformerException(XSLMessages.createMessage(
	  	XSLTErrorResources.ER_CALL_TO_EXT_FAILED, new Object[]{getNodeName()}));
	  transformer.getErrorListener().fatalError(te);
        }
        
        return;
      }

      try
      {
        nsh.processElement(this.getLocalName(), this, transformer,
                           getStylesheet(), this);
      }
      catch (Exception e)
      {

	if (hasFallbackChildren())
	  executeFallbacks(transformer);
	else
	{
          if(e instanceof TransformerException)
          {
            TransformerException te = (TransformerException)e;
            if(null == te.getLocator())
              te.setLocator(this);
            
            transformer.getErrorListener().fatalError(te);            
          }
          else if (e instanceof RuntimeException)
          {
            transformer.getErrorListener().fatalError(new TransformerException(e));
          }
          else
          {
            transformer.getErrorListener().warning(new TransformerException(e));
          }
        }
      }
    }
    catch(TransformerException e)
    {
      transformer.getErrorListener().fatalError(e);
    }
    catch(SAXException se) {
      throw new TransformerException(se);
    }
  }

  /**
   * Return the value of the attribute interpreted as an Attribute
   * Value Template (in other words, you can use curly expressions
   * such as href="http://{website}".
   *
   * @param rawName Raw name of the attribute to get
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @return the value of the attribute
   *
   * @throws TransformerException
   */
  public String getAttribute(
          String rawName, org.w3c.dom.Node sourceNode, TransformerImpl transformer)
            throws TransformerException
  {

    AVT avt = getLiteralResultAttribute(rawName);

    if ((null != avt) && avt.getRawName().equals(rawName))
    {
      XPathContext xctxt = transformer.getXPathContext();

      return avt.evaluate(xctxt, 
            xctxt.getDTMHandleFromNode(sourceNode), 
            this);
    }

    return null;
  }
  
  /**
   * Accept a visitor and call the appropriate method 
   * for this class.
   * 
   * @param visitor The visitor whose appropriate method will be called.
   * @return true if the children of the object should be visited.
   */
  protected boolean accept(XSLTVisitor visitor)
  {
  	return visitor.visitExtensionElement(this);
  }

  
}
