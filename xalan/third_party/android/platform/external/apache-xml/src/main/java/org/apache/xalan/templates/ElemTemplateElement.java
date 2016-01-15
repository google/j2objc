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
 * $Id: ElemTemplateElement.java 475981 2006-11-16 23:35:53Z minchau $
 */
package org.apache.xalan.templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.UnImplNode;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.WhitespaceStrippingElementMatcher;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.helpers.NamespaceSupport;

/**
 * An instance of this class represents an element inside
 * an xsl:template class.  It has a single "execute" method
 * which is expected to perform the given action on the
 * result tree.
 * This class acts like a Element node, and implements the
 * Element interface, but is not a full implementation
 * of that interface... it only implements enough for
 * basic traversal of the tree.
 *
 * @see Stylesheet
 * @xsl.usage advanced
 */
public class ElemTemplateElement extends UnImplNode
        implements PrefixResolver, Serializable, ExpressionNode, 
                   WhitespaceStrippingElementMatcher, XSLTVisitable
{
    static final long serialVersionUID = 4440018597841834447L;

  /**
   * Construct a template element instance.
   *
   */
  public ElemTemplateElement(){}

  /**
   * Tell if this template is a compiled template.
   *
   * @return Boolean flag indicating whether this is a compiled template   
   */
  public boolean isCompiledTemplate()
  {
    return false;
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
    return Constants.ELEMNAME_UNDEFINED;
  }

  /**
   * Return the node name.
   *
   * @return An invalid node name
   */
  public String getNodeName()
  {
    return "Unknown XSLT Element";
  }
  
  /**
   * For now, just return the result of getNodeName(), which 
   * the local name.
   *
   * @return The result of getNodeName().
   */
  public String getLocalName()
  {

    return getNodeName();
  }


  /**
   * This function will be called on top-level elements
   * only, just before the transform begins.
   *
   * @param transformer The XSLT TransformerFactory.
   *
   * @throws TransformerException
   */
  public void runtimeInit(TransformerImpl transformer) throws TransformerException{}

  /**
   * Execute the element's primary function.  Subclasses of this
   * function may recursivly execute down the element tree.
   *
   * @param transformer The XSLT TransformerFactory.
   * 
   * @throws TransformerException if any checked exception occurs.
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException{}

  /**
   * Get the owning "composed" stylesheet.  This looks up the
   * inheritance chain until it calls getStylesheetComposed
   * on a Stylesheet object, which will Get the owning
   * aggregated stylesheet, or that stylesheet if it is aggregated.
   *
   * @return the owning "composed" stylesheet.
   */
  public StylesheetComposed getStylesheetComposed()
  {
    return m_parentNode.getStylesheetComposed();
  }

  /**
   * Get the owning stylesheet.  This looks up the
   * inheritance chain until it calls getStylesheet
   * on a Stylesheet object, which will return itself.
   *
   * @return the owning stylesheet
   */
  public Stylesheet getStylesheet()
  {
    return (null==m_parentNode) ? null : m_parentNode.getStylesheet();
  }

  /**
   * Get the owning root stylesheet.  This looks up the
   * inheritance chain until it calls StylesheetRoot
   * on a Stylesheet object, which will return a reference
   * to the root stylesheet.
   *
   * @return the owning root stylesheet
   */
  public StylesheetRoot getStylesheetRoot()
  {
    return m_parentNode.getStylesheetRoot();
  }

  /**
   * This function is called during recomposition to
   * control how this element is composed.
   */
  public void recompose(StylesheetRoot root) throws TransformerException
  {
  }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    resolvePrefixTables();
    ElemTemplateElement t = getFirstChildElem();
    m_hasTextLitOnly = ((t != null) 
              && (t.getXSLToken() == Constants.ELEMNAME_TEXTLITERALRESULT) 
              && (t.getNextSiblingElem() == null));
              
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    cstate.pushStackMark();
  }
  
  /**
   * This after the template's children have been composed.
   */
  public void endCompose(StylesheetRoot sroot) throws TransformerException
  {
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    cstate.popStackMark();
  }

  /**
   * Throw a template element runtime error.  (Note: should we throw a TransformerException instead?)
   *
   * @param msg key of the error that occured.
   * @param args Arguments to be used in the message
   */
  public void error(String msg, Object[] args)
  {

    String themsg = XSLMessages.createMessage(msg, args);

    throw new RuntimeException(XSLMessages.createMessage(
                                    XSLTErrorResources.ER_ELEMTEMPLATEELEM_ERR,
                                    new Object[]{ themsg }));
  }
  
  /*
   * Throw an error.
   *
   * @param msg Message key for the error
   *
   */
  public void error(String msg)
  {
    error(msg, null);
  }
  

  // Implemented DOM Element methods.
  /**
   * Add a child to the child list.
   * NOTE: This presumes the child did not previously have a parent.
   * Making that assumption makes this a less expensive operation -- but
   * requires that if you *do* want to reparent a node, you use removeChild()
   * first to remove it from its previous context. Failing to do so will
   * damage the tree.
   *
   * @param newChild Child to be added to child list
   *
   * @return Child just added to the child list
   * @throws DOMException
   */
  public Node appendChild(Node newChild) throws DOMException
  {

    if (null == newChild)
    {
      error(XSLTErrorResources.ER_NULL_CHILD, null);  //"Trying to add a null child!");
    }

    ElemTemplateElement elem = (ElemTemplateElement) newChild;

    if (null == m_firstChild)
    {
      m_firstChild = elem;
    }
    else
    {
      ElemTemplateElement last = (ElemTemplateElement) getLastChild();

      last.m_nextSibling = elem;
    }

    elem.m_parentNode = this;

    return newChild;
  }

  /**
   * Add a child to the child list.
   * NOTE: This presumes the child did not previously have a parent.
   * Making that assumption makes this a less expensive operation -- but
   * requires that if you *do* want to reparent a node, you use removeChild()
   * first to remove it from its previous context. Failing to do so will
   * damage the tree.
   *
   * @param elem Child to be added to child list
   *
   * @return Child just added to the child list
   */
  public ElemTemplateElement appendChild(ElemTemplateElement elem)
  {

    if (null == elem)
    {
      error(XSLTErrorResources.ER_NULL_CHILD, null);  //"Trying to add a null child!");
    }

    if (null == m_firstChild)
    {
      m_firstChild = elem;
    }
    else
    {
      ElemTemplateElement last = getLastChildElem();

      last.m_nextSibling = elem;
    }

    elem.setParentElem(this);

    return elem;
  }


  /**
   * Tell if there are child nodes.
   *
   * @return True if there are child nodes
   */
  public boolean hasChildNodes()
  {
    return (null != m_firstChild);
  }

  /**
   * Get the type of the node.
   *
   * @return Constant for this node type
   */
  public short getNodeType()
  {
    return org.w3c.dom.Node.ELEMENT_NODE;
  }

  /**
   * Return the nodelist (same reference).
   *
   * @return The nodelist containing the child nodes (this)
   */
  public NodeList getChildNodes()
  {
    return this;
  }

  /**
   * Remove a child.
   * ADDED 9/8/200 to support compilation.
   * TODO: ***** Alternative is "removeMe() from my parent if any"
   * ... which is less well checked, but more convenient in some cases.
   * Given that we assume only experts are calling this class, it might
   * be preferable. It's less DOMish, though.
   * 
   * @param childETE The child to remove. This operation is a no-op
   * if oldChild is not a child of this node.
   *
   * @return the removed child, or null if the specified
   * node was not a child of this element.
   */
  public ElemTemplateElement removeChild(ElemTemplateElement childETE)
  {

    if (childETE == null || childETE.m_parentNode != this)
      return null;

    // Pointers to the child
    if (childETE == m_firstChild)
      m_firstChild = childETE.m_nextSibling;
    else
    {
      ElemTemplateElement prev = childETE.getPreviousSiblingElem();

      prev.m_nextSibling = childETE.m_nextSibling;
    }

    // Pointers from the child
    childETE.m_parentNode = null;
    childETE.m_nextSibling = null;

    return childETE;
  }

  /**
   * Replace the old child with a new child.
   *
   * @param newChild New child to replace with
   * @param oldChild Old child to be replaced
   *
   * @return The new child
   *
   * @throws DOMException
   */
  public Node replaceChild(Node newChild, Node oldChild) throws DOMException
  {

    if (oldChild == null || oldChild.getParentNode() != this)
      return null;

    ElemTemplateElement newChildElem = ((ElemTemplateElement) newChild);
    ElemTemplateElement oldChildElem = ((ElemTemplateElement) oldChild);

    // Fix up previous sibling.
    ElemTemplateElement prev =
      (ElemTemplateElement) oldChildElem.getPreviousSibling();

    if (null != prev)
      prev.m_nextSibling = newChildElem;

    // Fix up parent (this)
    if (m_firstChild == oldChildElem)
      m_firstChild = newChildElem;

    newChildElem.m_parentNode = this;
    oldChildElem.m_parentNode = null;
    newChildElem.m_nextSibling = oldChildElem.m_nextSibling;
    oldChildElem.m_nextSibling = null;

    // newChildElem.m_stylesheet = oldChildElem.m_stylesheet;
    // oldChildElem.m_stylesheet = null;
    return newChildElem;
  }
  
  /**
   * Unimplemented. See org.w3c.dom.Node
   *
   * @param newChild New child node to insert
   * @param refChild Insert in front of this child
   *
   * @return null
   *
   * @throws DOMException
   */
  public Node insertBefore(Node newChild, Node refChild) throws DOMException
  {
  	if(null == refChild)
  	{
  		appendChild(newChild);
  		return newChild;
  	}
  	
  	if(newChild == refChild)
  	{
  		// hmm...
  		return newChild;
  	}

    Node node = m_firstChild; 
    Node prev = null;  
    boolean foundit = false;
    
    while (null != node)
    {
    	// If the newChild is already in the tree, it is first removed.
    	if(newChild == node)
    	{
    		if(null != prev)
    			((ElemTemplateElement)prev).m_nextSibling = 
    				(ElemTemplateElement)node.getNextSibling();
    		else
    			m_firstChild = (ElemTemplateElement)node.getNextSibling();
    		node = node.getNextSibling();
    		continue; // prev remains the same.
    	}
    	if(refChild == node)
    	{
    		if(null != prev)
    		{
    			((ElemTemplateElement)prev).m_nextSibling = (ElemTemplateElement)newChild;
    		}
    		else
    		{
    			m_firstChild = (ElemTemplateElement)newChild;
    		}
    		((ElemTemplateElement)newChild).m_nextSibling = (ElemTemplateElement)refChild;
    		((ElemTemplateElement)newChild).setParentElem(this);
    		prev = newChild;
    		node = node.getNextSibling();
    		foundit = true;
    		continue;
    	}
    	prev = node;
    	node = node.getNextSibling();
    }
    
    if(!foundit)
    	throw new DOMException(DOMException.NOT_FOUND_ERR, 
    		"refChild was not found in insertBefore method!");
    else
    	return newChild;
  }


  /**
   * Replace the old child with a new child.
   *
   * @param newChildElem New child to replace with
   * @param oldChildElem Old child to be replaced
   *
   * @return The new child
   *
   * @throws DOMException
   */
  public ElemTemplateElement replaceChild(ElemTemplateElement newChildElem, 
                                          ElemTemplateElement oldChildElem)
  {

    if (oldChildElem == null || oldChildElem.getParentElem() != this)
      return null;

    // Fix up previous sibling.
    ElemTemplateElement prev =
      oldChildElem.getPreviousSiblingElem();

    if (null != prev)
      prev.m_nextSibling = newChildElem;

    // Fix up parent (this)
    if (m_firstChild == oldChildElem)
      m_firstChild = newChildElem;

    newChildElem.m_parentNode = this;
    oldChildElem.m_parentNode = null;
    newChildElem.m_nextSibling = oldChildElem.m_nextSibling;
    oldChildElem.m_nextSibling = null;

    // newChildElem.m_stylesheet = oldChildElem.m_stylesheet;
    // oldChildElem.m_stylesheet = null;
    return newChildElem;
  }

  /**
   * NodeList method: Count the immediate children of this node
   *
   * @return The count of children of this node
   */
  public int getLength()
  {

    // It is assumed that the getChildNodes call synchronized
    // the children. Therefore, we can access the first child
    // reference directly.
    int count = 0;

    for (ElemTemplateElement node = m_firstChild; node != null;
            node = node.m_nextSibling)
    {
      count++;
    }

    return count;
  }  // getLength():int

  /**
   * NodeList method: Return the Nth immediate child of this node, or
   * null if the index is out of bounds.
   *
   * @param index Index of child to find
   * @return org.w3c.dom.Node: the child node at given index
   */
  public Node item(int index)
  {

    // It is assumed that the getChildNodes call synchronized
    // the children. Therefore, we can access the first child
    // reference directly.
    ElemTemplateElement node = m_firstChild;

    for (int i = 0; i < index && node != null; i++)
    {
      node = node.m_nextSibling;
    }

    return node;
  }  // item(int):Node

  /**
   * Get the stylesheet owner.
   *
   * @return The stylesheet owner
   */
  public Document getOwnerDocument()
  {
    return getStylesheet();
  }
  
  /**
   * Get the owning xsl:template element.
   *
   * @return The owning xsl:template element, this element if it is a xsl:template, or null if not found.
   */
  public ElemTemplate getOwnerXSLTemplate()
  {
  	ElemTemplateElement el = this;
  	int type = el.getXSLToken();
  	while((null != el) && (type != Constants.ELEMNAME_TEMPLATE))
  	{
    	el = el.getParentElem();
    	if(null != el)
  			type = el.getXSLToken();
  	}
  	return (ElemTemplate)el;
  }


  /**
   * Return the element name.
   *
   * @return The element name
   */
  public String getTagName()
  {
    return getNodeName();
  }
  
  /**
   * Tell if this element only has one text child, for optimization purposes.
   * @return true of this element only has one text literal child.
   */
  public boolean hasTextLitOnly()
  {
    return m_hasTextLitOnly;
  }

  /**
   * Return the base identifier.
   *
   * @return The base identifier 
   */
  public String getBaseIdentifier()
  {

    // Should this always be absolute?
    return this.getSystemId();
  }

  /** line number where the current document event ends.
   *  @serial         */
  private int m_lineNumber;

  /** line number where the current document event ends.
   *  @serial         */
  private int m_endLineNumber;

  /**
   * Return the line number where the current document event ends.
   * Note that this is the line position of the first character
   * after the text associated with the document event.
   * @return The line number, or -1 if none is available.
   * @see #getColumnNumber
   */
  public int getEndLineNumber()
  {
	return m_endLineNumber;
  }

  /**
   * Return the line number where the current document event ends.
   * Note that this is the line position of the first character
   * after the text associated with the document event.
   * @return The line number, or -1 if none is available.
   * @see #getColumnNumber
   */
  public int getLineNumber()
  {
    return m_lineNumber;
  }

  /** the column number where the current document event ends.
   *  @serial        */
  private int m_columnNumber;

  /** the column number where the current document event ends.
   *  @serial        */
  private int m_endColumnNumber;

  /**
   * Return the column number where the current document event ends.
   * Note that this is the column number of the first
   * character after the text associated with the document
   * event.  The first column in a line is position 1.
   * @return The column number, or -1 if none is available.
   * @see #getLineNumber
   */
  public int getEndColumnNumber()
  {
	return m_endColumnNumber;
  }

  /**
   * Return the column number where the current document event ends.
   * Note that this is the column number of the first
   * character after the text associated with the document
   * event.  The first column in a line is position 1.
   * @return The column number, or -1 if none is available.
   * @see #getLineNumber
   */
  public int getColumnNumber()
  {
    return m_columnNumber;
  }

  /**
   * Return the public identifier for the current document event.
   * <p>This will be the public identifier
   * @return A string containing the public identifier, or
   *         null if none is available.
   * @see #getSystemId
   */
  public String getPublicId()
  {
    return (null != m_parentNode) ? m_parentNode.getPublicId() : null;
  }

  /**
   * Return the system identifier for the current document event.
   *
   * <p>If the system identifier is a URL, the parser must resolve it
   * fully before passing it to the application.</p>
   *
   * @return A string containing the system identifier, or null
   *         if none is available.
   * @see #getPublicId
   */
  public String getSystemId()
  {
    Stylesheet sheet=getStylesheet();
    return (sheet==null) ? null : sheet.getHref();
  }

  /**
   * Set the location information for this element.
   *
   * @param locator Source Locator with location information for this element
   */
  public void setLocaterInfo(SourceLocator locator)
  {
    m_lineNumber = locator.getLineNumber();
    m_columnNumber = locator.getColumnNumber();
  }
  
  /**
   * Set the end location information for this element.
   *
   * @param locator Source Locator with location information for this element
   */
  public void setEndLocaterInfo(SourceLocator locator)
  {
	m_endLineNumber = locator.getLineNumber();
	m_endColumnNumber = locator.getColumnNumber();
  } 

  /**
   * Tell if this element has the default space handling
   * turned off or on according to the xml:space attribute.
   * @serial
   */
  private boolean m_defaultSpace = true;

  /**
   * Tell if this element only has one text child, for optimization purposes.
   * @serial
   */
  private boolean m_hasTextLitOnly = false;

  /**
   * Tell if this element only has one text child, for optimization purposes.
   * @serial
   */
  protected boolean m_hasVariableDecl = false;
  
  public boolean hasVariableDecl()
  {
    return m_hasVariableDecl;
  }

  /**
   * Set the "xml:space" attribute.
   * A text node is preserved if an ancestor element of the text node
   * has an xml:space attribute with a value of preserve, and
   * no closer ancestor element has xml:space with a value of default.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Text">section-Creating-Text in XSLT Specification</a>
   *
   * @param v  Enumerated value, either Constants.ATTRVAL_PRESERVE 
   * or Constants.ATTRVAL_STRIP.
   */
  public void setXmlSpace(int v)
  {
    m_defaultSpace = ((Constants.ATTRVAL_STRIP == v) ? true : false);
  }

  /**
   * Get the "xml:space" attribute.
   * A text node is preserved if an ancestor element of the text node
   * has an xml:space attribute with a value of preserve, and
   * no closer ancestor element has xml:space with a value of default.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Text">section-Creating-Text in XSLT Specification</a>
   *
   * @return The value of the xml:space attribute
   */
  public boolean getXmlSpace()
  {
    return m_defaultSpace;
  }

  /**
   * The list of namespace declarations for this element only.
   * @serial
   */
  private List m_declaredPrefixes;

  /**
   * Return a table that contains all prefixes available
   * within this element context.
   *
   * @return Vector containing the prefixes available within this
   * element context 
   */
  public List getDeclaredPrefixes()
  {
    return m_declaredPrefixes;
  }

  /**
   * From the SAX2 helper class, set the namespace table for
   * this element.  Take care to call resolveInheritedNamespaceDecls.
   * after all namespace declarations have been added.
   *
   * @param nsSupport non-null reference to NamespaceSupport from 
   * the ContentHandler.
   *
   * @throws TransformerException
   */
  public void setPrefixes(NamespaceSupport nsSupport) throws TransformerException
  {
    setPrefixes(nsSupport, false);
  }

  /**
   * Copy the namespace declarations from the NamespaceSupport object.  
   * Take care to call resolveInheritedNamespaceDecls.
   * after all namespace declarations have been added.
   *
   * @param nsSupport non-null reference to NamespaceSupport from 
   * the ContentHandler.
   * @param excludeXSLDecl true if XSLT namespaces should be ignored.
   *
   * @throws TransformerException
   */
  public void setPrefixes(NamespaceSupport nsSupport, boolean excludeXSLDecl)
          throws TransformerException
  {

    Enumeration decls = nsSupport.getDeclaredPrefixes();

    while (decls.hasMoreElements())
    {
      String prefix = (String) decls.nextElement();

      if (null == m_declaredPrefixes)
        m_declaredPrefixes = new ArrayList();

      String uri = nsSupport.getURI(prefix);

      if (excludeXSLDecl && uri.equals(Constants.S_XSLNAMESPACEURL))
        continue;

      // System.out.println("setPrefixes - "+prefix+", "+uri);
      XMLNSDecl decl = new XMLNSDecl(prefix, uri, false);

      m_declaredPrefixes.add(decl);
    }
  }

  /**
   * Fullfill the PrefixResolver interface.  Calling this for this class 
   * will throw an error.
   *
   * @param prefix The prefix to look up, which may be an empty string ("") 
   *               for the default Namespace.
   * @param context The node context from which to look up the URI.
   *
   * @return null if the error listener does not choose to throw an exception.
   */
  public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context)
  {
    this.error(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, null);

    return null;
  }

  /**
   * Given a namespace, get the corrisponding prefix.
   * 9/15/00: This had been iteratively examining the m_declaredPrefixes
   * field for this node and its parents. That makes life difficult for
   * the compilation experiment, which doesn't have a static vector of
   * local declarations. Replaced a recursive solution, which permits
   * easier subclassing/overriding.
   *
   * @param prefix non-null reference to prefix string, which should map 
   *               to a namespace URL.
   *
   * @return The namespace URL that the prefix maps to, or null if no 
   *         mapping can be found.
   */
  public String getNamespaceForPrefix(String prefix)
  {
//    if (null != prefix && prefix.equals("xmlns"))
//    {
//      return Constants.S_XMLNAMESPACEURI;
//    }

    List nsDecls = m_declaredPrefixes;

    if (null != nsDecls)
    {
      int n = nsDecls.size();
      if(prefix.equals(Constants.ATTRVAL_DEFAULT_PREFIX))
      {
        prefix = "";
      }

      for (int i = 0; i < n; i++)
      {
        XMLNSDecl decl = (XMLNSDecl) nsDecls.get(i);

        if (prefix.equals(decl.getPrefix()))
          return decl.getURI();
      }
    }

    // Not found; ask our ancestors
    if (null != m_parentNode)
      return m_parentNode.getNamespaceForPrefix(prefix);

    // JJK: No ancestors; try implicit
    // %REVIEW% Are there literals somewhere that we should use instead?
    // %REVIEW% Is this really the best place to patch?
    if("xml".equals(prefix))
      return "http://www.w3.org/XML/1998/namespace";

    // No parent, so no definition
    return null;
  }

  /**
   * The table of {@link XMLNSDecl}s for this element
   * and all parent elements, screened for excluded prefixes.
   * @serial
   */
  private List m_prefixTable;

  /**
   * Return a table that contains all prefixes available
   * within this element context.
   *
   * @return reference to vector of {@link XMLNSDecl}s, which may be null.
   */
  List getPrefixTable()
  {
    return m_prefixTable;
  }
  
  void setPrefixTable(List list) {
      m_prefixTable = list;
  }
  
  /**
   * Get whether or not the passed URL is contained flagged by
   * the "extension-element-prefixes" property.  This method is overridden 
   * by {@link ElemLiteralResult#containsExcludeResultPrefix}.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param prefix non-null reference to prefix that might be excluded.
   *
   * @return true if the prefix should normally be excluded.
   */
  public boolean containsExcludeResultPrefix(String prefix, String uri)
  {
    ElemTemplateElement parent = this.getParentElem();
    if(null != parent)
      return parent.containsExcludeResultPrefix(prefix, uri);
      
    return false;
  }

  /**
   * Tell if the result namespace decl should be excluded.  Should be called before
   * namespace aliasing (I think).
   *
   * @param prefix non-null reference to prefix.
   * @param uri reference to namespace that prefix maps to, which is protected 
   *            for null, but should really never be passed as null.
   *
   * @return true if the given namespace should be excluded.
   *
   * @throws TransformerException
   */
  private boolean excludeResultNSDecl(String prefix, String uri)
          throws TransformerException
  {

    if (uri != null)
    {
      if (uri.equals(Constants.S_XSLNAMESPACEURL)
              || getStylesheet().containsExtensionElementURI(uri))
        return true;

      if (containsExcludeResultPrefix(prefix, uri))
        return true;
    }

    return false;
  }
  
  /**
   * Combine the parent's namespaces with this namespace
   * for fast processing, taking care to reference the
   * parent's namespace if this namespace adds nothing new.
   * (Recursive method, walking the elements depth-first,
   * processing parents before children).
   * Note that this method builds m_prefixTable with aliased 
   * namespaces, *not* the original namespaces.
   *
   * @throws TransformerException
   */
  public void resolvePrefixTables() throws TransformerException
  {
    // Always start with a fresh prefix table!
    setPrefixTable(null);

    // If we have declared declarations, then we look for 
    // a parent that has namespace decls, and add them 
    // to this element's decls.  Otherwise we just point 
    // to the parent that has decls.
    if (null != this.m_declaredPrefixes)
    {
      StylesheetRoot stylesheet = this.getStylesheetRoot();
      
      // Add this element's declared prefixes to the 
      // prefix table.
      int n = m_declaredPrefixes.size();

      for (int i = 0; i < n; i++)
      {
        XMLNSDecl decl = (XMLNSDecl) m_declaredPrefixes.get(i);
        String prefix = decl.getPrefix();
        String uri = decl.getURI();
        if(null == uri)
          uri = "";
        boolean shouldExclude = excludeResultNSDecl(prefix, uri);

        // Create a new prefix table if one has not already been created.
        if (null == m_prefixTable)
            setPrefixTable(new ArrayList());

        NamespaceAlias nsAlias = stylesheet.getNamespaceAliasComposed(uri);
        if(null != nsAlias)
        {
          // Should I leave the non-aliased element in the table as 
          // an excluded element?
          
          // The exclusion should apply to the non-aliased prefix, so 
          // we don't calculate it here.  -sb
          // Use stylesheet prefix, as per xsl WG
          decl = new XMLNSDecl(nsAlias.getStylesheetPrefix(), 
                              nsAlias.getResultNamespace(), shouldExclude);
        }
        else
          decl = new XMLNSDecl(prefix, uri, shouldExclude);

        m_prefixTable.add(decl);
        
      }
    }

    ElemTemplateElement parent = this.getParentNodeElem();

    if (null != parent)
    {

      // The prefix table of the parent should never be null!
      List prefixes = parent.m_prefixTable;

      if (null == m_prefixTable && !needToCheckExclude())
      {

        // Nothing to combine, so just use parent's table!
        setPrefixTable(parent.m_prefixTable);
      }
      else
      {

        // Add the prefixes from the parent's prefix table.
        int n = prefixes.size();
        
        for (int i = 0; i < n; i++)
        {
          XMLNSDecl decl = (XMLNSDecl) prefixes.get(i);
          boolean shouldExclude = excludeResultNSDecl(decl.getPrefix(),
                                                      decl.getURI());

          if (shouldExclude != decl.getIsExcluded())
          {
            decl = new XMLNSDecl(decl.getPrefix(), decl.getURI(),
                                 shouldExclude);
          }
          
          //m_prefixTable.addElement(decl);
          addOrReplaceDecls(decl);
        }
      }
    }
    else if (null == m_prefixTable)
    {

      // Must be stylesheet element without any result prefixes!
      setPrefixTable(new ArrayList());
    }
  }
  
  /**
   * Add or replace this namespace declaration in list
   * of namespaces in scope for this element.
   *
   * @param newDecl namespace declaration to add to list
   */
  void addOrReplaceDecls(XMLNSDecl newDecl)
  {
      int n = m_prefixTable.size();

        for (int i = n - 1; i >= 0; i--)
        {
          XMLNSDecl decl = (XMLNSDecl) m_prefixTable.get(i);

          if (decl.getPrefix().equals(newDecl.getPrefix()))
          {
            return;
          }
        }
      m_prefixTable.add(newDecl);    
    
  }
  
  /**
   * Return whether we need to check namespace prefixes 
   * against and exclude result prefixes list.
   */
  boolean needToCheckExclude()
  {
    return false;    
  } 

  /**
   * Send startPrefixMapping events to the result tree handler
   * for all declared prefix mappings in the stylesheet.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  void executeNSDecls(TransformerImpl transformer) throws TransformerException
  {
       executeNSDecls(transformer, null);
  }

  /**
   * Send startPrefixMapping events to the result tree handler
   * for all declared prefix mappings in the stylesheet.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param ignorePrefix string prefix to not startPrefixMapping
   *
   * @throws TransformerException
   */
  void executeNSDecls(TransformerImpl transformer, String ignorePrefix) throws TransformerException
  {  
    try
    {
      if (null != m_prefixTable)
      {
        SerializationHandler rhandler = transformer.getResultTreeHandler();
        int n = m_prefixTable.size();

        for (int i = n - 1; i >= 0; i--)
        {
          XMLNSDecl decl = (XMLNSDecl) m_prefixTable.get(i);

          if (!decl.getIsExcluded() && !(null != ignorePrefix && decl.getPrefix().equals(ignorePrefix)))
          {
            rhandler.startPrefixMapping(decl.getPrefix(), decl.getURI(), true);
          }
        }
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }

  /**
   * Send endPrefixMapping events to the result tree handler
   * for all declared prefix mappings in the stylesheet.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  void unexecuteNSDecls(TransformerImpl transformer) throws TransformerException
  {
       unexecuteNSDecls(transformer, null);
  }

  /**
   * Send endPrefixMapping events to the result tree handler
   * for all declared prefix mappings in the stylesheet.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param ignorePrefix string prefix to not endPrefixMapping
   * 
   * @throws TransformerException
   */
  void unexecuteNSDecls(TransformerImpl transformer, String ignorePrefix) throws TransformerException
  {
 
    try
    {
      if (null != m_prefixTable)
      {
        SerializationHandler rhandler = transformer.getResultTreeHandler();
        int n = m_prefixTable.size();

        for (int i = 0; i < n; i++)
        {
          XMLNSDecl decl = (XMLNSDecl) m_prefixTable.get(i);

          if (!decl.getIsExcluded() && !(null != ignorePrefix && decl.getPrefix().equals(ignorePrefix)))
          {
            rhandler.endPrefixMapping(decl.getPrefix());
          }
        }
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }
  
  /** The *relative* document order number of this element.
   *  @serial */
  protected int m_docOrderNumber = -1;
  
  /**
   * Set the UID (document order index).
   *
   * @param i Index of this child.
   */
  public void setUid(int i)
  {
    m_docOrderNumber = i;
  }

  /**
   * Get the UID (document order index).
   *
   * @return Index of this child
   */
  public int getUid()
  {
    return m_docOrderNumber;
  }


  /**
   * Parent node.
   * @serial
   */
  protected ElemTemplateElement m_parentNode;

  /**
   * Get the parent as a Node.
   *
   * @return This node's parent node
   */
  public Node getParentNode()
  {
    return m_parentNode;
  }

  /**
   * Get the parent as an ElemTemplateElement.
   *
   * @return This node's parent as an ElemTemplateElement
   */
  public ElemTemplateElement getParentElem()
  {
    return m_parentNode;
  }

  /**
   * Set the parent as an ElemTemplateElement.
   *
   * @param p This node's parent as an ElemTemplateElement
   */
  public void setParentElem(ElemTemplateElement p)
  {
    m_parentNode = p;
  }

  /**
   * Next sibling.
   * @serial
   */
  ElemTemplateElement m_nextSibling;

  /**
   * Get the next sibling (as a Node) or return null.
   *
   * @return this node's next sibling or null
   */
  public Node getNextSibling()
  {
    return m_nextSibling;
  }

  /**
   * Get the previous sibling (as a Node) or return null.
   * Note that this may be expensive if the parent has many kids;
   * we accept that price in exchange for avoiding the prev pointer
   * TODO: If we were sure parents and sibs are always ElemTemplateElements,
   * we could hit the fields directly rather than thru accessors.
   *
   * @return This node's previous sibling or null
   */
  public Node getPreviousSibling()
  {

    Node walker = getParentNode(), prev = null;

    if (walker != null)
      for (walker = walker.getFirstChild(); walker != null;
              prev = walker, walker = walker.getNextSibling())
      {
        if (walker == this)
          return prev;
      }

    return null;
  }

  /**
   * Get the previous sibling (as a Node) or return null.
   * Note that this may be expensive if the parent has many kids;
   * we accept that price in exchange for avoiding the prev pointer
   * TODO: If we were sure parents and sibs are always ElemTemplateElements,
   * we could hit the fields directly rather than thru accessors.
   *
   * @return This node's previous sibling or null
   */
  public ElemTemplateElement getPreviousSiblingElem()
  {

    ElemTemplateElement walker = getParentNodeElem();
    ElemTemplateElement prev = null;

    if (walker != null)
      for (walker = walker.getFirstChildElem(); walker != null;
              prev = walker, walker = walker.getNextSiblingElem())
      {
        if (walker == this)
          return prev;
      }

    return null;
  }


  /**
   * Get the next sibling (as a ElemTemplateElement) or return null.
   *
   * @return This node's next sibling (as a ElemTemplateElement) or null 
   */
  public ElemTemplateElement getNextSiblingElem()
  {
    return m_nextSibling;
  }
  
  /**
   * Get the parent element.
   *
   * @return This node's next parent (as a ElemTemplateElement) or null 
   */
  public ElemTemplateElement getParentNodeElem()
  {
    return m_parentNode;
  }


  /**
   * First child.
   * @serial
   */
  ElemTemplateElement m_firstChild;

  /**
   * Get the first child as a Node.
   *
   * @return This node's first child or null
   */
  public Node getFirstChild()
  {
    return m_firstChild;
  }

  /**
   * Get the first child as a ElemTemplateElement.
   *
   * @return This node's first child (as a ElemTemplateElement) or null
   */
  public ElemTemplateElement getFirstChildElem()
  {
    return m_firstChild;
  }

  /**
   * Get the last child.
   *
   * @return This node's last child
   */
  public Node getLastChild()
  {

    ElemTemplateElement lastChild = null;

    for (ElemTemplateElement node = m_firstChild; node != null;
            node = node.m_nextSibling)
    {
      lastChild = node;
    }

    return lastChild;
  }

  /**
   * Get the last child.
   *
   * @return This node's last child
   */
  public ElemTemplateElement getLastChildElem()
  {

    ElemTemplateElement lastChild = null;

    for (ElemTemplateElement node = m_firstChild; node != null;
            node = node.m_nextSibling)
    {
      lastChild = node;
    }

    return lastChild;
  }


  /** DOM backpointer that this element originated from.          */
  transient private org.w3c.dom.Node m_DOMBackPointer;

  /**
   * If this stylesheet was created from a DOM, get the
   * DOM backpointer that this element originated from.
   * For tooling use.
   *
   * @return DOM backpointer that this element originated from or null.
   */
  public org.w3c.dom.Node getDOMBackPointer()
  {
    return m_DOMBackPointer;
  }

  /**
   * If this stylesheet was created from a DOM, set the
   * DOM backpointer that this element originated from.
   * For tooling use.
   *
   * @param n DOM backpointer that this element originated from.
   */
  public void setDOMBackPointer(org.w3c.dom.Node n)
  {
    m_DOMBackPointer = n;
  }

  /**
   * Compares this object with the specified object for precedence order.
   * The order is determined by the getImportCountComposed() of the containing
   * composed stylesheet and the getUid() of this element.
   * Returns a negative integer, zero, or a positive integer as this
   * object is less than, equal to, or greater than the specified object.
   * 
   * @param o The object to be compared to this object
   * @return  a negative integer, zero, or a positive integer as this object is
   *          less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's
   *         type prevents it from being compared to this Object.
   */
  public int compareTo(Object o) throws ClassCastException {
    
    ElemTemplateElement ro = (ElemTemplateElement) o;
    int roPrecedence = ro.getStylesheetComposed().getImportCountComposed();
    int myPrecedence = this.getStylesheetComposed().getImportCountComposed();

    if (myPrecedence < roPrecedence)
      return -1;
    else if (myPrecedence > roPrecedence)
      return 1;
    else
      return this.getUid() - ro.getUid();
  }
  
  /**
   * Get information about whether or not an element should strip whitespace.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param support The XPath runtime state.
   * @param targetElement Element to check
   *
   * @return true if the whitespace should be stripped.
   *
   * @throws TransformerException
   */
  public boolean shouldStripWhiteSpace(
          org.apache.xpath.XPathContext support, 
          org.w3c.dom.Element targetElement) throws TransformerException
  {
    StylesheetRoot sroot = this.getStylesheetRoot();
    return (null != sroot) ? sroot.shouldStripWhiteSpace(support, targetElement) :false;
  }
  
  /**
   * Get information about whether or not whitespace can be stripped.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @return true if the whitespace can be stripped.
   */
  public boolean canStripWhiteSpace()
  {
    StylesheetRoot sroot = this.getStylesheetRoot();
    return (null != sroot) ? sroot.canStripWhiteSpace() : false;
  }
  
  /**
   * Tell if this element can accept variable declarations.
   * @return true if the element can accept and process variable declarations.
   */
  public boolean canAcceptVariables()
  {
  	return true;
  }
  
  //=============== ExpressionNode methods ================
  
  /** 
   * Set the parent of this node.
   * @param n Must be a ElemTemplateElement.
   */
  public void exprSetParent(ExpressionNode n)
  {
  	// This obviously requires that only a ElemTemplateElement can 
  	// parent a node of this type.
  	setParentElem((ElemTemplateElement)n);
  }
  
  /**
   * Get the ExpressionNode parent of this node.
   */
  public ExpressionNode exprGetParent()
  {
  	return getParentElem();
  }

  /** 
   * This method tells the node to add its argument to the node's
   * list of children. 
   * @param n Must be a ElemTemplateElement. 
   */
  public void exprAddChild(ExpressionNode n, int i)
  {
  	appendChild((ElemTemplateElement)n);
  }

  /** This method returns a child node.  The children are numbered
     from zero, left to right. */
  public ExpressionNode exprGetChild(int i)
  {
  	return (ExpressionNode)item(i);
  }

  /** Return the number of children the node has. */
  public int exprGetNumChildren()
  {
  	return getLength();
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
  	return visitor.visitInstruction(this);
  }

  /**
   * @see XSLTVisitable#callVisitors(XSLTVisitor)
   */
  public void callVisitors(XSLTVisitor visitor)
  {
  	if(accept(visitor))
  	{
		callChildVisitors(visitor);
  	}
  }

  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttributes)
  {
    for (ElemTemplateElement node = m_firstChild;
      node != null;
      node = node.m_nextSibling)
      {
      node.callVisitors(visitor);
    }
  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor)
  {
  	callChildVisitors(visitor, true);
  }


	/**
	 * @see PrefixResolver#handlesNullPrefixes()
	 */
	public boolean handlesNullPrefixes() {
		return false;
	}

}
