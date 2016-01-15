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
 * $Id: ElemVariable.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.objects.XRTreeFragSelectWrapper;
import org.apache.xpath.objects.XString;
import org.apache.xalan.res.XSLTErrorResources;

/**
 * Implement xsl:variable.
 * <pre>
 * <!ELEMENT xsl:variable %template;>
 * <!ATTLIST xsl:variable
 *   name %qname; #REQUIRED
 *   select %expr; #IMPLIED
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#variables">variables in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemVariable extends ElemTemplateElement
{
    static final long serialVersionUID = 9111131075322790061L;

  /**
   * Constructor ElemVariable
   *
   */
  public ElemVariable(){}

  /**
   * This is the index into the stack frame.
   */
  protected int m_index;
  
  /**
   * The stack frame size for this variable if it is a global variable 
   * that declares an RTF, which is equal to the maximum number 
   * of variables that can be declared in the variable at one time.
   */
  int m_frameSize = -1;

  
  /**
   * Sets the relative position of this variable within the stack frame (if local)
   * or the global area (if global).  Note that this should be called only for
   * global variables since the local position is computed in the compose() method.
   */
  public void setIndex(int index)
  {
    m_index = index;
  }

  /**
   * If this element is not at the top-level, get the relative position of the
   * variable into the stack frame.  If this variable is at the top-level, get
   * the relative position within the global area.
   */
  public int getIndex()
  {
    return m_index;
  }

  /**
   * The value of the "select" attribute.
   * @serial
   */
  private XPath m_selectPattern;

  /**
   * Set the "select" attribute.
   * If the variable-binding element has a select attribute,
   * then the value of the attribute must be an expression and
   * the value of the variable is the object that results from
   * evaluating the expression. In this case, the content
   * of the variable must be empty.
   *
   * @param v Value to set for the "select" attribute.
   */
  public void setSelect(XPath v)
  {
    m_selectPattern = v;
  }

  /**
   * Get the "select" attribute.
   * If the variable-binding element has a select attribute,
   * then the value of the attribute must be an expression and
   * the value of the variable is the object that results from
   * evaluating the expression. In this case, the content
   * of the variable must be empty.
   *
   * @return Value of the "select" attribute.
   */
  public XPath getSelect()
  {
    return m_selectPattern;
  }

  /**
   * The value of the "name" attribute.
   * @serial
   */
  protected QName m_qname;

  /**
   * Set the "name" attribute.
   * Both xsl:variable and xsl:param have a required name
   * attribute, which specifies the name of the variable. The
   * value of the name attribute is a QName, which is expanded
   * as described in [2.4 Qualified Names].
   * @see <a href="http://www.w3.org/TR/xslt#qname">qname in XSLT Specification</a>
   *
   * @param v Value to set for the "name" attribute.
   */
  public void setName(QName v)
  {
    m_qname = v;
  }

  /**
   * Get the "name" attribute.
   * Both xsl:variable and xsl:param have a required name
   * attribute, which specifies the name of the variable. The
   * value of the name attribute is a QName, which is expanded
   * as described in [2.4 Qualified Names].
   * @see <a href="http://www.w3.org/TR/xslt#qname">qname in XSLT Specification</a>
   *
   * @return Value of the "name" attribute.
   */
  public QName getName()
  {
    return m_qname;
  }

  /**
   * Tells if this is a top-level variable or param, or not.
   * @serial
   */
  private boolean m_isTopLevel = false;

  /**
   * Set if this is a top-level variable or param, or not.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param v Boolean indicating whether this is a top-level variable
   * or param, or not.
   */
  public void setIsTopLevel(boolean v)
  {
    m_isTopLevel = v;
  }

  /**
   * Get if this is a top-level variable or param, or not.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @return Boolean indicating whether this is a top-level variable
   * or param, or not.
   */
  public boolean getIsTopLevel()
  {
    return m_isTopLevel;
  }

  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_VARIABLE;
  }

  /**
   * Return the node name.
   *
   * @return The node name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_VARIABLE_STRING;
  }

  /**
   * Copy constructor.
   *
   * @param param An element created from an xsl:variable
   *
   * @throws TransformerException
   */
  public ElemVariable(ElemVariable param) throws TransformerException
  {

    m_selectPattern = param.m_selectPattern;
    m_qname = param.m_qname;
    m_isTopLevel = param.m_isTopLevel;

    // m_value = param.m_value;
    // m_varContext = param.m_varContext;
  }

  /**
   * Execute a variable declaration and push it onto the variable stack.
   * @see <a href="http://www.w3.org/TR/xslt#variables">variables in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    int sourceNode = transformer.getXPathContext().getCurrentNode();
  
    XObject var = getValue(transformer, sourceNode);

    // transformer.getXPathContext().getVarStack().pushVariable(m_qname, var);
    transformer.getXPathContext().getVarStack().setLocalVariable(m_index, var);
  }

  /**
   * Get the XObject representation of the variable.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   *
   * @return the XObject representation of the variable.
   *
   * @throws TransformerException
   */
  public XObject getValue(TransformerImpl transformer, int sourceNode)
          throws TransformerException
  {

    XObject var;
    XPathContext xctxt = transformer.getXPathContext();

    xctxt.pushCurrentNode(sourceNode);
 
    try
    {
      if (null != m_selectPattern)
      {
        var = m_selectPattern.execute(xctxt, sourceNode, this);

        var.allowDetachToRelease(false);
      }
      else if (null == getFirstChildElem())
      {
        var = XString.EMPTYSTRING;
      }
      else
      {

        // Use result tree fragment.
        // Global variables may be deferred (see XUnresolvedVariable) and hence
        // need to be assigned to a different set of DTMs than local variables
        // so they aren't popped off the stack on return from a template.
        int df;

		// Bugzilla 7118: A variable set via an RTF may create local
		// variables during that computation. To keep them from overwriting
		// variables at this level, push a new variable stack.
		////// PROBLEM: This is provoking a variable-used-before-set
		////// problem in parameters. Needs more study.
		try
		{
			//////////xctxt.getVarStack().link(0);
			if(m_parentNode instanceof Stylesheet) // Global variable
				df = transformer.transformToGlobalRTF(this);
			else
				df = transformer.transformToRTF(this);
    	}
		finally{ 
			//////////////xctxt.getVarStack().unlink(); 
			}

        var = new XRTreeFrag(df, xctxt, this);
      }
    }
    finally
    {      
      xctxt.popCurrentNode();
    }

    return var;
  }
  
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    // See if we can reduce an RTF to a select with a string expression.
    if(null == m_selectPattern  
       && sroot.getOptimizer())
    {
      XPath newSelect = rewriteChildToExpression(this);
      if(null != newSelect)
        m_selectPattern = newSelect;
    }
    
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    
    // This should be done before addVariableName, so we don't have visibility 
    // to the variable now being defined.
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_selectPattern)
      m_selectPattern.fixupVariables(vnames, cstate.getGlobalsSize());
      
    // Only add the variable if this is not a global.  If it is a global, 
    // it was already added by stylesheet root.
    if(!(m_parentNode instanceof Stylesheet) && m_qname != null)
    {
      m_index = cstate.addVariableName(m_qname) - cstate.getGlobalsSize();
    }
    else if (m_parentNode instanceof Stylesheet)
    {
    	// If this is a global, then we need to treat it as if it's a xsl:template, 
    	// and count the number of variables it contains.  So we set the count to 
    	// zero here.
		cstate.resetStackFrameSize();
    }
    
    // This has to be done after the addVariableName, so that the variable 
    // pushed won't be immediately popped again in endCompose.
    super.compose(sroot);
  }
  
  /**
   * This after the template's children have been composed.  We have to get 
   * the count of how many variables have been declared, so we can do a link 
   * and unlink.
   */
  public void endCompose(StylesheetRoot sroot) throws TransformerException
  {
    super.endCompose(sroot);
    if(m_parentNode instanceof Stylesheet)
    {
    	StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    	m_frameSize = cstate.getFrameSize();
    	cstate.resetStackFrameSize();
    }
  }

  
  
//  /**
//   * This after the template's children have been composed.
//   */
//  public void endCompose() throws TransformerException
//  {
//    super.endCompose();
//  }


  /**
   * If the children of a variable is a single xsl:value-of or text literal, 
   * it is cheaper to evaluate this as an expression, so try and adapt the 
   * child an an expression.
   *
   * @param varElem Should be a ElemParam, ElemVariable, or ElemWithParam.
   *
   * @return An XPath if rewrite is possible, else null.
   *
   * @throws TransformerException
   */
  static XPath rewriteChildToExpression(ElemTemplateElement varElem)
          throws TransformerException
  {

    ElemTemplateElement t = varElem.getFirstChildElem();

    // Down the line this can be done with multiple string objects using 
    // the concat function.
    if (null != t && null == t.getNextSiblingElem())
    {
      int etype = t.getXSLToken();

      if (Constants.ELEMNAME_VALUEOF == etype)
      {
        ElemValueOf valueof = (ElemValueOf) t;

        // %TBD% I'm worried about extended attributes here.
        if (valueof.getDisableOutputEscaping() == false
                && valueof.getDOMBackPointer() == null)
        {
          varElem.m_firstChild = null;

          return new XPath(new XRTreeFragSelectWrapper(valueof.getSelect().getExpression()));
        }
      }
      else if (Constants.ELEMNAME_TEXTLITERALRESULT == etype)
      {
        ElemTextLiteral lit = (ElemTextLiteral) t;

        if (lit.getDisableOutputEscaping() == false
                && lit.getDOMBackPointer() == null)
        {
          String str = lit.getNodeValue();
          XString xstr = new XString(str);

          varElem.m_firstChild = null;

          return new XPath(new XRTreeFragSelectWrapper(xstr));
        }
      }
    }

    return null;
  }

  /**
   * This function is called during recomposition to
   * control how this element is composed.
   * @param root The root stylesheet for this transformation.
   */
  public void recompose(StylesheetRoot root)
  {
    root.recomposeVariables(this);
  }
  
  /**
   * Set the parent as an ElemTemplateElement.
   *
   * @param p This node's parent as an ElemTemplateElement
   */
  public void setParentElem(ElemTemplateElement p)
  {
    super.setParentElem(p);
    p.m_hasVariableDecl = true;
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
  	return visitor.visitVariableOrParamDecl(this);
  }

  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(null != m_selectPattern)
  		m_selectPattern.getExpression().callVisitors(m_selectPattern, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
  
  /**
   * Tell if this is a psuedo variable reference, declared by Xalan instead 
   * of by the user.
   */
  public boolean isPsuedoVar()
  {
  	java.lang.String ns = m_qname.getNamespaceURI();
  	if((null != ns) && ns.equals(RedundentExprEliminator.PSUEDOVARNAMESPACE))
  	{
  		if(m_qname.getLocalName().startsWith("#"))
  			return true;
  	}
  	return false;
  }
  
  /**
   * Add a child to the child list. If the select attribute
   * is present, an error will be raised.
   *
   * @param elem New element to append to this element's children list
   *
   * @return null if the select attribute was present, otherwise the 
   * child just added to the child list 
   */
  public ElemTemplateElement appendChild(ElemTemplateElement elem)
  {
    // cannot have content and select
    if (m_selectPattern != null)
    {
      error(XSLTErrorResources.ER_CANT_HAVE_CONTENT_AND_SELECT, 
          new Object[]{"xsl:" + this.getNodeName()});
      return null;
    }
    return super.appendChild(elem);
  }

}
