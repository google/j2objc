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
 * $Id: ElemApplyTemplates.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.xml.sax.SAXException;

/**
 * Implement xsl:apply-templates.
 * <pre>
 * &amp;!ELEMENT xsl:apply-templates (xsl:sort|xsl:with-param)*>
 * &amp;!ATTLIST xsl:apply-templates
 *   select %expr; "node()"
 *   mode %qname; #IMPLIED
 * &amp;
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Applying-Template-Rules">section-Applying-Template-Rules in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemApplyTemplates extends ElemCallTemplate
{
    static final long serialVersionUID = 2903125371542621004L;

  /**
   * mode %qname; #IMPLIED
   * @serial
   */
  private QName m_mode = null;

  /**
   * Set the mode attribute for this element.
   *
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   */
  public void setMode(QName mode)
  {
    m_mode = mode;
  }

  /**
   * Get the mode attribute for this element.
   *
   * @return The mode attribute for this element
   */
  public QName getMode()
  {
    return m_mode;
  }

  /**
   * Tells if this belongs to a default template,
   * in which case it will act different with
   * regard to processing modes.
   * @see <a href="http://www.w3.org/TR/xslt#built-in-rule">built-in-rule in XSLT Specification</a>
   * @serial
   */
  private boolean m_isDefaultTemplate = false;
  
//  /**
//   * List of namespace/localname IDs, for identification of xsl:with-param to 
//   * xsl:params.  Initialized in the compose() method.
//   */
//  private int[] m_paramIDs;

  /**
   * Set if this belongs to a default template,
   * in which case it will act different with
   * regard to processing modes.
   * @see <a href="http://www.w3.org/TR/xslt#built-in-rule">built-in-rule in XSLT Specification</a>
   *
   * @param b boolean value to set.
   */
  public void setIsDefaultTemplate(boolean b)
  {
    m_isDefaultTemplate = b;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return Token ID for this element types
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_APPLY_TEMPLATES;
  }
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
  }

  /**
   * Return the node name.
   *
   * @return Element name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_APPLY_TEMPLATES_STRING;
  }

  /**
   * Apply the context node to the matching templates.
   * @see <a href="http://www.w3.org/TR/xslt#section-Applying-Template-Rules">section-Applying-Template-Rules in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    transformer.pushCurrentTemplateRuleIsNull(false);

    boolean pushMode = false;

    try
    {
      // %REVIEW% Do we need this check??
      //      if (null != sourceNode)
      //      {
      // boolean needToTurnOffInfiniteLoopCheck = false;
      QName mode = transformer.getMode();

      if (!m_isDefaultTemplate)
      {
        if (((null == mode) && (null != m_mode))
                || ((null != mode) &&!mode.equals(m_mode)))
        {
          pushMode = true;

          transformer.pushMode(m_mode);
        }
      }

      transformSelectedNodes(transformer);
    }
    finally
    {
      if (pushMode)
        transformer.popMode();

      transformer.popCurrentTemplateRuleIsNull();
    }
  }

  
  /**
   * Perform a query if needed, and call transformNode for each child.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException Thrown in a variety of circumstances.
   * @xsl.usage advanced
   */
  public void transformSelectedNodes(TransformerImpl transformer)
            throws TransformerException
  {

    final XPathContext xctxt = transformer.getXPathContext();
    final int sourceNode = xctxt.getCurrentNode();
    DTMIterator sourceNodes = m_selectExpression.asIterator(xctxt, sourceNode);
    VariableStack vars = xctxt.getVarStack();
    int nParams = getParamElemCount();
    int thisframe = vars.getStackFrame();

    boolean pushContextNodeListFlag = false;
      
    try
    {

            xctxt.pushCurrentNode(DTM.NULL);
            xctxt.pushCurrentExpressionNode(DTM.NULL);
            xctxt.pushSAXLocatorNull();
            transformer.pushElemTemplateElement(null);
      final Vector keys = (m_sortElems == null)
                          ? null
                          : transformer.processSortKeys(this, sourceNode);

      // Sort if we need to.
      if (null != keys)
        sourceNodes = sortNodes(xctxt, keys, sourceNodes);
            
      final SerializationHandler rth = transformer.getSerializationHandler();
//      ContentHandler chandler = rth.getContentHandler();
      final StylesheetRoot sroot = transformer.getStylesheet();
      final TemplateList tl = sroot.getTemplateListComposed();
      final boolean quiet = transformer.getQuietConflictWarnings();
      
      // Should be able to get this from the iterator but there must be a bug.
      DTM dtm = xctxt.getDTM(sourceNode);
      
      int argsFrame = -1;
      if(nParams > 0)
      {
        // This code will create a section on the stack that is all the 
        // evaluated arguments.  These will be copied into the real params 
        // section of each called template.
        argsFrame = vars.link(nParams);
        vars.setStackFrame(thisframe);
        
        for (int i = 0; i < nParams; i++) 
        {
          ElemWithParam ewp = m_paramElems[i];
          XObject obj = ewp.getValue(transformer, sourceNode);

          vars.setLocalVariable(i, obj, argsFrame);
        }
        vars.setStackFrame(argsFrame);
      }
      
      xctxt.pushContextNodeList(sourceNodes);
      pushContextNodeListFlag = true;
      
      IntStack currentNodes = xctxt.getCurrentNodeStack();
      
      IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();     
      
      // pushParams(transformer, xctxt);
      
      int child;
      while (DTM.NULL != (child = sourceNodes.nextNode()))
      {
        currentNodes.setTop(child);
        currentExpressionNodes.setTop(child);

        if(xctxt.getDTM(child) != dtm)
        {
          dtm = xctxt.getDTM(child);
        }
        
        final int exNodeType = dtm.getExpandedTypeID(child);

        final int nodeType = dtm.getNodeType(child);

        final QName mode = transformer.getMode();

        ElemTemplate template = tl.getTemplateFast(xctxt, child, exNodeType, mode, 
                                      -1, quiet, dtm);

        // If that didn't locate a node, fall back to a default template rule.
        // See http://www.w3.org/TR/xslt#built-in-rule.
        if (null == template)
        {
          switch (nodeType)
          {
          case DTM.DOCUMENT_FRAGMENT_NODE :
          case DTM.ELEMENT_NODE :
            template = sroot.getDefaultRule();
            // %OPT% direct faster?
            break;
          case DTM.ATTRIBUTE_NODE :
          case DTM.CDATA_SECTION_NODE :
          case DTM.TEXT_NODE :
            // if(rth.m_elemIsPending || rth.m_docPending)
            //  rth.flushPending(true);
            transformer.pushPairCurrentMatched(sroot.getDefaultTextRule(), child);
            transformer.setCurrentElement(sroot.getDefaultTextRule());
            // dtm.dispatchCharactersEvents(child, chandler, false);
            dtm.dispatchCharactersEvents(child, rth, false);
            transformer.popCurrentMatched();
            continue;
          case DTM.DOCUMENT_NODE :
            template = sroot.getDefaultRootRule();
            break;
          default :

            // No default rules for processing instructions and the like.
            continue;
          }
        }
        else
        {
        	transformer.setCurrentElement(template);
        }
                
        transformer.pushPairCurrentMatched(template, child);

        int currentFrameBottom;  // See comment with unlink, below
        if(template.m_frameSize > 0)
        {
          xctxt.pushRTFContext();
          currentFrameBottom = vars.getStackFrame();  // See comment with unlink, below
          vars.link(template.m_frameSize);
          // You can't do the check for nParams here, otherwise the 
          // xsl:params might not be nulled.
          if(/* nParams > 0 && */ template.m_inArgsSize > 0)
          {
            int paramIndex = 0;
            for (ElemTemplateElement elem = template.getFirstChildElem(); 
                 null != elem; elem = elem.getNextSiblingElem()) 
            {
              if(Constants.ELEMNAME_PARAMVARIABLE == elem.getXSLToken())
              {
                ElemParam ep = (ElemParam)elem;
                
                int i;
                for (i = 0; i < nParams; i++) 
                {
                  ElemWithParam ewp = m_paramElems[i];
                  if(ewp.m_qnameID == ep.m_qnameID)
                  {
                    XObject obj = vars.getLocalVariable(i, argsFrame);
                    vars.setLocalVariable(paramIndex, obj);
                    break;
                  }
                }
                if(i == nParams)
                  vars.setLocalVariable(paramIndex, null);
              }
              else
                break;
              paramIndex++;
            }
            
          }
        }
        else
        	currentFrameBottom = 0;

        // And execute the child templates.
        // Loop through the children of the template, calling execute on 
        // each of them.
        for (ElemTemplateElement t = template.m_firstChild; 
             t != null; t = t.m_nextSibling)
        {
          xctxt.setSAXLocator(t);
          try
          {
          	transformer.pushElemTemplateElement(t);
          	t.execute(transformer);
          }
          finally
          {
          	transformer.popElemTemplateElement();
          }
        }
        
        if(template.m_frameSize > 0)
        {
          // See Frank Weiss bug around 03/19/2002 (no Bugzilla report yet).
          // While unlink will restore to the proper place, the real position 
          // may have been changed for xsl:with-param, so that variables 
          // can be accessed.  
          // of right now.
          // More:
          // When we entered this function, the current 
          // frame buffer (cfb) index in the variable stack may 
          // have been manually set.  If we just call 
          // unlink(), however, it will restore the cfb to the 
          // previous link index from the link stack, rather than 
          // the manually set cfb.  So, 
          // the only safe solution is to restore it back 
          // to the same position it was on entry, since we're 
          // really not working in a stack context here. (Bug4218)
          vars.unlink(currentFrameBottom);
          xctxt.popRTFContext();
        }
          
        transformer.popCurrentMatched();
        
      } // end while (DTM.NULL != (child = sourceNodes.nextNode()))
    }
    catch (SAXException se)
    {
      transformer.getErrorListener().fatalError(new TransformerException(se));
    }
    finally
    {
      // Unlink to the original stack frame
      if(nParams > 0)
        vars.unlink(thisframe);
      xctxt.popSAXLocator();
      if (pushContextNodeListFlag) xctxt.popContextNodeList();
      transformer.popElemTemplateElement();
      xctxt.popCurrentExpressionNode();
      xctxt.popCurrentNode();
      sourceNodes.detach();
    }
  }

}
