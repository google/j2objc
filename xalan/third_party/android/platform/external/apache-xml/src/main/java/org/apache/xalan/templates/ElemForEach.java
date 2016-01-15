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
 * $Id: ElemForEach.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.NodeSorter;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.IntStack;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;

import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Implement xsl:for-each.
 * <pre>
 * <!ELEMENT xsl:for-each
 *  (#PCDATA
 *   %instructions;
 *   %result-elements;
 *   | xsl:sort)
 * >
 *
 * <!ATTLIST xsl:for-each
 *   select %expr; #REQUIRED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#for-each">for-each in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemForEach extends ElemTemplateElement implements ExpressionOwner
{
    static final long serialVersionUID = 6018140636363583690L;
  /** Set true to request some basic status reports */
  static final boolean DEBUG = false;
  
  /**
   * This is set by an "xalan-doc-cache-off" pi, or the old "xalan:doc-cache-off" pi.
   * The old form of the PI only works for XML parsers that are not namespace aware.
   * It tells the engine that
   * documents created in the location paths executed by this element
   * will not be reparsed. It's set by StylesheetHandler during
   * construction. Note that this feature applies _only_ to xsl:for-each
   * elements in its current incarnation; a more general cache management
   * solution is desperately needed.
   */
  public boolean m_doc_cache_off=false;
  
  /**
   * Construct a element representing xsl:for-each.
   */
  public ElemForEach(){}

  /**
   * The "select" expression.
   * @serial
   */
  protected Expression m_selectExpression = null;
  
  
  /**
   * Used to fix bug#16889
   * Store XPath away for later processing.
   */
  protected XPath m_xpath = null;  

  /**
   * Set the "select" attribute.
   *
   * @param xpath The XPath expression for the "select" attribute.
   */
  public void setSelect(XPath xpath)
  {
    m_selectExpression = xpath.getExpression();
    
    // The following line is part of the codes added to fix bug#16889
    // Store xpath which will be needed when firing Selected Event
    m_xpath = xpath;    
  }

  /**
   * Get the "select" attribute.
   *
   * @return The XPath expression for the "select" attribute.
   */
  public Expression getSelect()
  {
    return m_selectExpression;
  }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   *
   * NEEDSDOC @param sroot
   *
   * @throws TransformerException
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {

    super.compose(sroot);

    int length = getSortElemCount();

    for (int i = 0; i < length; i++)
    {
      getSortElem(i).compose(sroot);
    }

    java.util.Vector vnames = sroot.getComposeState().getVariableNames();

    if (null != m_selectExpression)
      m_selectExpression.fixupVariables(
        vnames, sroot.getComposeState().getGlobalsSize());
    else
    {
      m_selectExpression =
        getStylesheetRoot().m_selectDefault.getExpression();
    }
  }
  
  /**
   * This after the template's children have been composed.
   */
  public void endCompose(StylesheetRoot sroot) throws TransformerException
  {
    int length = getSortElemCount();

    for (int i = 0; i < length; i++)
    {
      getSortElem(i).endCompose(sroot);
    }
    
    super.endCompose(sroot);
  }


  //  /**
  //   * This function is called after everything else has been
  //   * recomposed, and allows the template to set remaining
  //   * values that may be based on some other property that
  //   * depends on recomposition.
  //   *
  //   * @throws TransformerException
  //   */
  //  public void compose() throws TransformerException
  //  {
  //
  //    if (null == m_selectExpression)
  //    {
  //      m_selectExpression =
  //        getStylesheetRoot().m_selectDefault.getExpression();
  //    }
  //  }

  /**
   * Vector containing the xsl:sort elements associated with this element.
   *  @serial
   */
  protected Vector m_sortElems = null;

  /**
   * Get the count xsl:sort elements associated with this element.
   * @return The number of xsl:sort elements.
   */
  public int getSortElemCount()
  {
    return (m_sortElems == null) ? 0 : m_sortElems.size();
  }

  /**
   * Get a xsl:sort element associated with this element.
   *
   * @param i Index of xsl:sort element to get
   *
   * @return xsl:sort element at given index
   */
  public ElemSort getSortElem(int i)
  {
    return (ElemSort) m_sortElems.elementAt(i);
  }

  /**
   * Set a xsl:sort element associated with this element.
   *
   * @param sortElem xsl:sort element to set
   */
  public void setSortElem(ElemSort sortElem)
  {

    if (null == m_sortElems)
      m_sortElems = new Vector();

    m_sortElems.addElement(sortElem);
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_FOREACH;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_FOREACH_STRING;
  }

  /**
   * Execute the xsl:for-each transformation
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    transformer.pushCurrentTemplateRuleIsNull(true);    
    try
    {
      transformSelectedNodes(transformer);
    }
    finally
    {
      transformer.popCurrentTemplateRuleIsNull();
    }
  }

  /**
   * Get template element associated with this
   *
   *
   * @return template element associated with this (itself)
   */
  protected ElemTemplateElement getTemplateMatch()
  {
    return this;
  }

  /**
   * Sort given nodes
   *
   *
   * @param xctxt The XPath runtime state for the sort.
   * @param keys Vector of sort keyx
   * @param sourceNodes Iterator of nodes to sort
   *
   * @return iterator of sorted nodes
   *
   * @throws TransformerException
   */
  public DTMIterator sortNodes(
          XPathContext xctxt, Vector keys, DTMIterator sourceNodes)
            throws TransformerException
  {

    NodeSorter sorter = new NodeSorter(xctxt);
    sourceNodes.setShouldCacheNodes(true);
    sourceNodes.runTo(-1);
    xctxt.pushContextNodeList(sourceNodes);

    try
    {
      sorter.sort(sourceNodes, keys, xctxt);
      sourceNodes.setCurrentPos(0);
    }
    finally
    {
      xctxt.popContextNodeList();
    }

    return sourceNodes;
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
    DTMIterator sourceNodes = m_selectExpression.asIterator(xctxt,
            sourceNode);

    try
    {

      final Vector keys = (m_sortElems == null)
              ? null
              : transformer.processSortKeys(this, sourceNode);

      // Sort if we need to.
      if (null != keys)
        sourceNodes = sortNodes(xctxt, keys, sourceNodes);

      xctxt.pushCurrentNode(DTM.NULL);

      IntStack currentNodes = xctxt.getCurrentNodeStack();

      xctxt.pushCurrentExpressionNode(DTM.NULL);

      IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();

      xctxt.pushSAXLocatorNull();
      xctxt.pushContextNodeList(sourceNodes);
      transformer.pushElemTemplateElement(null);

      // pushParams(transformer, xctxt);
      // Should be able to get this from the iterator but there must be a bug.
      DTM dtm = xctxt.getDTM(sourceNode);
      int docID = sourceNode & DTMManager.IDENT_DTM_DEFAULT;
      int child;

      while (DTM.NULL != (child = sourceNodes.nextNode()))
      {
        currentNodes.setTop(child);
        currentExpressionNodes.setTop(child);

        if ((child & DTMManager.IDENT_DTM_DEFAULT) != docID)
        {
          dtm = xctxt.getDTM(child);
          docID = child & DTMManager.IDENT_DTM_DEFAULT;
        }

        //final int exNodeType = dtm.getExpandedTypeID(child);
        final int nodeType = dtm.getNodeType(child); 

        // And execute the child templates.
        // Loop through the children of the template, calling execute on 
        // each of them.
        for (ElemTemplateElement t = this.m_firstChild; t != null;
             t = t.m_nextSibling)
        {
          xctxt.setSAXLocator(t);
          transformer.setCurrentElement(t);
          t.execute(transformer);
        }

        // KLUGE: Implement <?xalan:doc_cache_off?>
	 	// ASSUMPTION: This will be set only when the XPath was indeed
	 	// a call to the Document() function. Calling it in other
	 	// situations is likely to fry Xalan.
	 	//
	 	// %REVIEW% We need a MUCH cleaner solution -- one that will
	 	// handle cleaning up after document() and getDTM() in other
		// contexts. The whole SourceTreeManager mechanism should probably
	 	// be moved into DTMManager rather than being explicitly invoked in
	 	// FuncDocument and here.
	 	if(m_doc_cache_off)
		{
	 	  if(DEBUG)
	 	    System.out.println("JJK***** CACHE RELEASE *****\n"+
				       "\tdtm="+dtm.getDocumentBaseURI());
	  	// NOTE: This will work because this is _NOT_ a shared DTM, and thus has
	  	// only a single Document node. If it could ever be an RTF or other
	 	// shared DTM, this would require substantial rework.
	 	  xctxt.getSourceTreeManager().removeDocumentFromCache(dtm.getDocument());
	 	  xctxt.release(dtm,false);
	 	}
      }
    }
    finally
    {
      xctxt.popSAXLocator();
      xctxt.popContextNodeList();
      transformer.popElemTemplateElement();
      xctxt.popCurrentExpressionNode();
      xctxt.popCurrentNode();
      sourceNodes.detach();
    }
  }

  /**
   * Add a child to the child list.
   * <!ELEMENT xsl:apply-templates (xsl:sort|xsl:with-param)*>
   * <!ATTLIST xsl:apply-templates
   *   select %expr; "node()"
   *   mode %qname; #IMPLIED
   * >
   *
   * @param newChild Child to add to child list
   *
   * @return Child just added to child list
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    int type = ((ElemTemplateElement) newChild).getXSLToken();

    if (Constants.ELEMNAME_SORT == type)
    {
      setSortElem((ElemSort) newChild);

      return newChild;
    }
    else
      return super.appendChild(newChild);
  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  public void callChildVisitors(XSLTVisitor visitor, boolean callAttributes)
  {
  	if(callAttributes && (null != m_selectExpression))
  		m_selectExpression.callVisitors(this, visitor);
  		
    int length = getSortElemCount();

    for (int i = 0; i < length; i++)
    {
      getSortElem(i).callVisitors(visitor);
    }

    super.callChildVisitors(visitor, callAttributes);
  }

  /**
   * @see ExpressionOwner#getExpression()
   */
  public Expression getExpression()
  {
    return m_selectExpression;
  }

  /**
   * @see ExpressionOwner#setExpression(Expression)
   */
  public void setExpression(Expression exp)
  {
  	exp.exprSetParent(this);
  	m_selectExpression = exp;
  }

  /*
   * to keep the binary compatibility, assign a default value for newly added
   * globel varialbe m_xpath during deserialization of an object which was 
   * serialized using an older version
   */
   private void readObject(ObjectInputStream os) throws 
        IOException, ClassNotFoundException {
           os.defaultReadObject();
           m_xpath = null;
   }
}
