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
 * $Id: ElemLiteralResult.java 476350 2006-11-17 22:53:23Z minchau $
 */
package org.apache.xalan.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.StringVector;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

/**
 * Implement a Literal Result Element.
 * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemLiteralResult extends ElemUse
{
    static final long serialVersionUID = -8703409074421657260L;

    /** The return value as Empty String. */
    private static final String EMPTYSTRING = "";

  /**
   * Tells if this element represents a root element
   * that is also the stylesheet element.
   * TODO: This should be a derived class.
   * @serial
   */
  private boolean isLiteralResultAsStylesheet = false;

  /**
   * Set whether this element represents a root element
   * that is also the stylesheet element.
   *
   *
   * @param b boolean flag indicating whether this element
   * represents a root element that is also the stylesheet element.
   */
  public void setIsLiteralResultAsStylesheet(boolean b)
  {
    isLiteralResultAsStylesheet = b;
  }

  /**
   * Return whether this element represents a root element
   * that is also the stylesheet element.
   *
   *
   * @return boolean flag indicating whether this element
   * represents a root element that is also the stylesheet element.
   */
  public boolean getIsLiteralResultAsStylesheet()
  {
    return isLiteralResultAsStylesheet;
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
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if (null != m_avts)
    {
      int nAttrs = m_avts.size();

      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);
        avt.fixupVariables(vnames, cstate.getGlobalsSize());
      } 
    }   
  }
  
  /**
   * The created element node will have the attribute nodes
   * that were present on the element node in the stylesheet tree,
   * other than attributes with names in the XSLT namespace.
   * @serial
   */
  private List m_avts = null;

  /** List of attributes with the XSLT namespace.
   *  @serial */
  private List m_xslAttr = null;

  /**
   * Set a literal result attribute (AVTs only).
   *
   * @param avt literal result attribute to add (AVT only)
   */
  public void addLiteralResultAttribute(AVT avt)
  {

    if (null == m_avts)
      m_avts = new ArrayList();

    m_avts.add(avt);
  }

  /**
   * Set a literal result attribute (used for xsl attributes).
   *
   * @param att literal result attribute to add
   */
  public void addLiteralResultAttribute(String att)
  {

    if (null == m_xslAttr)
      m_xslAttr = new ArrayList();

    m_xslAttr.add(att);
  }
  
  /**
   * Set the "xml:space" attribute.
   * A text node is preserved if an ancestor element of the text node
   * has an xml:space attribute with a value of preserve, and
   * no closer ancestor element has xml:space with a value of default.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Text">section-Creating-Text in XSLT Specification</a>
   *
   * @param avt  Enumerated value, either Constants.ATTRVAL_PRESERVE 
   * or Constants.ATTRVAL_STRIP.
   */
  public void setXmlSpace(AVT avt)
  {
    // This function is a bit-o-hack, I guess...
    addLiteralResultAttribute(avt);
    String val = avt.getSimpleString();
    if(val.equals("default"))
    {
      super.setXmlSpace(Constants.ATTRVAL_STRIP);
    }
    else if(val.equals("preserve"))
    {
      super.setXmlSpace(Constants.ATTRVAL_PRESERVE);
    }
    // else maybe it's a real AVT, so we can't resolve it at this time.
  }

  /**
   * Get a literal result attribute by name.
   *
   * @param namespaceURI Namespace URI of attribute node to get
   * @param localName Local part of qualified name of attribute node to get
   *
   * @return literal result attribute (AVT)
   */
  public AVT getLiteralResultAttributeNS(String namespaceURI, String localName)
  {

    if (null != m_avts)
    {
      int nAttrs = m_avts.size();

      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);

        if (avt.getName().equals(localName) && 
                avt.getURI().equals(namespaceURI))
        {
          return avt;
        }
      }  // end for
    }

    return null;
  }

  /**
   * Return the raw value of the attribute.
   *
   * @param namespaceURI Namespace URI of attribute node to get
   * @param localName Local part of qualified name of attribute node to get
   *
   * @return The Attr value as a string, or the empty string if that attribute 
   * does not have a specified or default value
   */
  public String getAttributeNS(String namespaceURI, String localName)
  {

    AVT avt = getLiteralResultAttributeNS(namespaceURI, localName);

    if ((null != avt))
    {
      return avt.getSimpleString();
    }

    return EMPTYSTRING;
  }

  /**
   * Get a literal result attribute by name. The name is namespaceURI:localname  
   * if namespace is not null.
   *
   * @param name Name of literal result attribute to get
   *
   * @return literal result attribute (AVT)
   */
  public AVT getLiteralResultAttribute(String name)
  {

    if (null != m_avts)
    {
      int nAttrs = m_avts.size();
      String namespace = null;
      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.get(i);
        namespace = avt.getURI();
        
        if ((namespace != null && (!namespace.equals("")) && (namespace 
                +":"+avt.getName()).equals(name))|| ((namespace == null || 
                namespace.equals(""))&& avt.getRawName().equals(name)))
        {
          return avt;
        }
      }  // end for
    }

    return null;
  }

  /**
   * Return the raw value of the attribute.
   *
   * @param namespaceURI:localName or localName if the namespaceURI is null of 
   * the attribute to get
   *
   * @return The Attr value as a string, or the empty string if that attribute 
   * does not have a specified or default value
   */
  public String getAttribute(String rawName)
  {

    AVT avt = getLiteralResultAttribute(rawName);

    if ((null != avt))
    {
      return avt.getSimpleString();
    }

    return EMPTYSTRING;
  }
  
  /**
   * Get whether or not the passed URL is flagged by
   * the "extension-element-prefixes" or "exclude-result-prefixes"
   * properties.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param prefix non-null reference to prefix that might be excluded.(not currently used)
   * @param uri reference to namespace that prefix maps to
   *
   * @return true if the prefix should normally be excluded.
   */
  public boolean containsExcludeResultPrefix(String prefix, String uri)
  {
    if (uri == null ||
                (null == m_excludeResultPrefixes &&
                 null == m_ExtensionElementURIs)
                )
      return super.containsExcludeResultPrefix(prefix, uri);

    if (prefix.length() == 0)
      prefix = Constants.ATTRVAL_DEFAULT_PREFIX;

    // This loop is ok here because this code only runs during
    // stylesheet compile time.    
        if(m_excludeResultPrefixes!=null)
            for (int i =0; i< m_excludeResultPrefixes.size(); i++)
            {
                if (uri.equals(getNamespaceForPrefix(m_excludeResultPrefixes.elementAt(i))))
                    return true;
            }    
        
        // JJK Bugzilla 1133: Also check locally-scoped extensions
    if(m_ExtensionElementURIs!=null && m_ExtensionElementURIs.contains(uri))
       return true;

        return super.containsExcludeResultPrefix(prefix, uri);
  }

  /**
   * Augment resolvePrefixTables, resolving the namespace aliases once
   * the superclass has resolved the tables.
   *
   * @throws TransformerException
   */
  public void resolvePrefixTables() throws TransformerException
  {

    super.resolvePrefixTables();

    StylesheetRoot stylesheet = getStylesheetRoot();

    if ((null != m_namespace) && (m_namespace.length() > 0))
    {
      NamespaceAlias nsa = stylesheet.getNamespaceAliasComposed(m_namespace);

      if (null != nsa)
      {
        m_namespace = nsa.getResultNamespace();

        // String resultPrefix = nsa.getResultPrefix();
        String resultPrefix = nsa.getStylesheetPrefix();  // As per xsl WG, Mike Kay

        if ((null != resultPrefix) && (resultPrefix.length() > 0))
          m_rawName = resultPrefix + ":" + m_localName;
        else
          m_rawName = m_localName;
      }
    }

    if (null != m_avts)
    {
      int n = m_avts.size();

      for (int i = 0; i < n; i++)
      {
        AVT avt = (AVT) m_avts.get(i);

        // Should this stuff be a method on AVT?
        String ns = avt.getURI();

        if ((null != ns) && (ns.length() > 0))
        {
          NamespaceAlias nsa =
            stylesheet.getNamespaceAliasComposed(m_namespace); // %REVIEW% ns?

          if (null != nsa)
          {
            String namespace = nsa.getResultNamespace();

            // String resultPrefix = nsa.getResultPrefix();
            String resultPrefix = nsa.getStylesheetPrefix();  // As per XSL WG
            String rawName = avt.getName();

            if ((null != resultPrefix) && (resultPrefix.length() > 0))
              rawName = resultPrefix + ":" + rawName;

            avt.setURI(namespace);
            avt.setRawName(rawName);
          }
        }
      }
    }
  }

  /**
   * Return whether we need to check namespace prefixes
   * against the exclude result prefixes or extensions lists.
   * Note that this will create a new prefix table if one
   * has not been created already.
   *
   * NEEDSDOC ($objectName$) @return
   */
  boolean needToCheckExclude()
  {
    if (null == m_excludeResultPrefixes && null == getPrefixTable()
                && m_ExtensionElementURIs==null     // JJK Bugzilla 1133
                )
      return false;
    else
    {

      // Create a new prefix table if one has not already been created.
      if (null == getPrefixTable())
        setPrefixTable(new java.util.ArrayList());

      return true;
    }
  }

  /**
   * The namespace of the element to be created.
   * @serial
   */
  private String m_namespace;

  /**
   * Set the namespace URI of the result element to be created.
   * Note that after resolvePrefixTables has been called, this will
   * return the aliased result namespace, not the original stylesheet
   * namespace.
   *
   * @param ns The Namespace URI, or the empty string if the
   *        element has no Namespace URI.
   */
  public void setNamespace(String ns)
  {
    if(null == ns) // defensive, shouldn't have to do this.
      ns = "";
    m_namespace = ns;
  }

  /**
   * Get the original namespace of the Literal Result Element.
   * 
   * %REVIEW% Why isn't this overriding the getNamespaceURI method
   * rather than introducing a new one?
   *
   * @return The Namespace URI, or the empty string if the
   *        element has no Namespace URI.
   */
  public String getNamespace()
  {
    return m_namespace;
  }

  /**
   * The local name of the element to be created.
   * @serial
   */
  private String m_localName;

  /**
   * Set the local name of the LRE.
   *
   * @param localName The local name (without prefix) of the result element
   *                  to be created.
   */
  public void setLocalName(String localName)
  {
    m_localName = localName;
  }

  /**
   * Get the local name of the Literal Result Element.
   * Note that after resolvePrefixTables has been called, this will
   * return the aliased name prefix, not the original stylesheet
   * namespace prefix.
   *
   * @return The local name (without prefix) of the result element
   *                  to be created.
   */
  public String getLocalName()
  {
    return m_localName;
  }

  /**
   * The raw name of the element to be created.
   * @serial
   */
  private String m_rawName;

  /**
   * Set the raw name of the LRE.
   *
   * @param rawName The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   */
  public void setRawName(String rawName)
  {
    m_rawName = rawName;
  }

  /**
   * Get the raw name of the Literal Result Element.
   *
   * @return  The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   */
  public String getRawName()
  {
    return m_rawName;
  }
    
 /**
   * Get the prefix part of the raw name of the Literal Result Element.
   *
   * @return The prefix, or the empty string if noprefix was provided.
   */
  public String getPrefix()
  {
        int len=m_rawName.length()-m_localName.length()-1;
    return (len>0)
            ? m_rawName.substring(0,len)
            : "";
  }


  /**
   * The "extension-element-prefixes" property, actually contains URIs.
   * @serial
   */
  private StringVector m_ExtensionElementURIs;

  /**
   * Set the "extension-element-prefixes" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param v Vector of URIs (not prefixes) to set as the "extension-element-prefixes" property
   */
  public void setExtensionElementPrefixes(StringVector v)
  {
    m_ExtensionElementURIs = v;
  }

  /**
   * @see org.w3c.dom.Node
   *
   * @return NamedNodeMap
   */
  public NamedNodeMap getAttributes()
  {
        return new LiteralElementAttributes();
  }

  public class LiteralElementAttributes implements NamedNodeMap{
          private int m_count = -1;
          
          /**
           * Construct a NameNodeMap.
           *
           */
          public LiteralElementAttributes(){         
          }
          
          /**
           * Return the number of Attributes on this Element
           *
           * @return The number of nodes in this map. The range of valid child 
           * node indices is <code>0</code> to <code>length-1</code> inclusive
           */
          public int getLength()
          {
            if (m_count == -1)
            {
               if (null != m_avts) m_count = m_avts.size();
               else m_count = 0;
            }
            return m_count;
          }

          /**
           * Retrieves a node specified by name.
           * @param name The <code>nodeName</code> of a node to retrieve.
           * @return A <code>Node</code> (of any type) with the specified
           *   <code>nodeName</code>, or <code>null</code> if it does not 
           *   identify any node in this map.
           */
          public Node getNamedItem(String name)
          {
                if (getLength() == 0) return null;
                String uri = null;
                String localName = name; 
                int index = name.indexOf(":"); 
                if (-1 != index){
                         uri = name.substring(0, index);
                         localName = name.substring(index+1);
                }
                Node retNode = null;
                Iterator eum = m_avts.iterator();
                while (eum.hasNext()){
                        AVT avt = (AVT) eum.next();
                        if (localName.equals(avt.getName()))
                        {
                          String nsURI = avt.getURI(); 
                          if ((uri == null && nsURI == null)
                            || (uri != null && uri.equals(nsURI)))
                          {
                            retNode = new Attribute(avt, ElemLiteralResult.this);
                            break;
                          }
                        }
                }
                return retNode;
          }

          /**
           * Retrieves a node specified by local name and namespace URI.
           * @param namespaceURI Namespace URI of attribute node to get
           * @param localName Local part of qualified name of attribute node to 
           * get
           * @return A <code>Node</code> (of any type) with the specified
           *   <code>nodeName</code>, or <code>null</code> if it does not 
           *   identify any node in this map.
           */
          public Node getNamedItemNS(String namespaceURI, String localName)
          {
                  if (getLength() == 0) return null;
                  Node retNode = null;
                  Iterator eum = m_avts.iterator();
                  while (eum.hasNext())
                  {
                    AVT avt = (AVT) eum.next();      
                    if (localName.equals(avt.getName()))
                    {
                      String nsURI = avt.getURI(); 
                      if ((namespaceURI == null && nsURI == null)
                        || (namespaceURI != null && namespaceURI.equals(nsURI)))
                      {
                        retNode = new Attribute(avt, ElemLiteralResult.this);
                        break;
                      }
                    }
                  }
                  return retNode;
          }
          
          /**
           * Returns the <code>index</code>th item in the map. If <code>index
           * </code> is greater than or equal to the number of nodes in this 
           * map, this returns <code>null</code>.
           * @param i The index of the requested item.
           * @return The node at the <code>index</code>th position in the map, 
           *   or <code>null</code> if that is not a valid index.
           */
          public Node item(int i)
          {
                if (getLength() == 0 || i >= m_avts.size()) return null;
                else return 
                    new Attribute(((AVT)m_avts.get(i)), 
                        ElemLiteralResult.this);
          }
          
          /**
           * @see org.w3c.dom.NamedNodeMap
           *
           * @param name of the node to remove
           * 
           * @return The node removed from this map if a node with such 
           * a name exists. 
           *
           * @throws DOMException
           */
          public Node removeNamedItem(String name) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          
          /**
           * @see org.w3c.dom.NamedNodeMap
           *
           * @param namespaceURI Namespace URI of the node to remove
           * @param localName Local part of qualified name of the node to remove
           * 
           * @return The node removed from this map if a node with such a local
           *  name and namespace URI exists
           *
           * @throws DOMException
           */
          public Node removeNamedItemNS(String namespaceURI, String localName) 
                throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          } 
          
          /**
           * Unimplemented. See org.w3c.dom.NamedNodeMap
           *
           * @param A node to store in this map
           * 
           * @return If the new Node replaces an existing node the replaced 
           * Node is returned, otherwise null is returned
           *
           * @throws DOMException
           */
          public Node setNamedItem(Node arg) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }
          
          /**
           * Unimplemented. See org.w3c.dom.NamedNodeMap
           *
           * @param A node to store in this map
           * 
           * @return If the new Node replaces an existing node the replaced 
           * Node is returned, otherwise null is returned
           *
           * @throws DOMException
           */
          public Node setNamedItemNS(Node arg) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }                                                                         
  }

  public class Attribute implements Attr{
          private AVT m_attribute;
          private Element m_owner = null;
          /**
           * Construct a Attr.
           *
           */
          public Attribute(AVT avt, Element elem){
                m_attribute = avt;
                m_owner = elem;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @param newChild New node to append to the list of this node's 
           * children
           *
           *
           * @throws DOMException
           */
          public Node appendChild(Node newChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @param deep Flag indicating whether to clone deep 
           * (clone member variables)
           *
           * @return Returns a duplicate of this node
           */
          public Node cloneNode(boolean deep)
          {
                  return new Attribute(m_attribute, m_owner);
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public NamedNodeMap getAttributes()
          {
            return null;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return a NodeList containing no nodes. 
           */
          public NodeList getChildNodes()
          {
                  return new NodeList(){
                          public int getLength(){
                                  return 0;
                          }
                          public Node item(int index){
                                  return null;
                          }
                  };
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public Node getFirstChild()
          {
                  return null;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public Node getLastChild()
          {
                  return null;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return the local part of the qualified name of this node
           */
          public String getLocalName()
          {
                  return m_attribute.getName();
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return The namespace URI of this node, or null if it is 
           * unspecified
           */
          public String getNamespaceURI()
          {
                  String uri = m_attribute.getURI();
                  return (uri.equals(""))?null:uri;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public Node getNextSibling()
          {
                return null;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return The name of the attribute
           */
          public String getNodeName()
          {
                  String uri = m_attribute.getURI();
                  String localName = getLocalName();
                  return (uri.equals(""))?localName:uri+":"+localName;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return The node is an Attr
           */
          public short getNodeType()
          {
                  return ATTRIBUTE_NODE;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return The value of the attribute
           *
           * @throws DOMException
           */
          public String getNodeValue() throws DOMException
          {
                  return m_attribute.getSimpleString();
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public Document getOwnerDocument()
          {
            return m_owner.getOwnerDocument();
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return the containing element node
           */
          public Node getParentNode()
          {
                  return m_owner;
          }
                    
          /**
           * @see org.w3c.dom.Node
           *
           * @return The namespace prefix of this node, or null if it is 
           * unspecified
           */
          public String getPrefix()
          {
                  String uri = m_attribute.getURI();
                  String rawName = m_attribute.getRawName();
                  return (uri.equals(""))? 
                        null:rawName.substring(0, rawName.indexOf(":"));
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return null
           */
          public Node getPreviousSibling()
          {
                  return null;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return false
           */
          public boolean hasAttributes()
          {
                  return false;
          }
          
          /**
           * @see org.w3c.dom.Node
           *
           * @return false
           */
          public boolean hasChildNodes()
          {
                  return false;
          }                    

          /**
           * @see org.w3c.dom.Node
           *
           * @param newChild New child node to insert
           * @param refChild Insert in front of this child
           *
           * @return null
           *
           * @throws DOMException
           */
          public Node insertBefore(Node newChild, Node refChild) 
                throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
                  return null;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @return Returns <code>false</code>
           * @since DOM Level 2
           */
          public boolean isSupported(String feature, String version)
          {
            return false;
          }

          /** @see org.w3c.dom.Node */
          public void normalize(){}
          
          /**
           * @see org.w3c.dom.Node
           *
           * @param oldChild Child to be removed
           *
           * @return null
           *
           * @throws DOMException
           */
          public Node removeChild(Node oldChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }         

          /**
           * @see org.w3c.dom.Node
           *
           * @param newChild Replace existing child with this one
           * @param oldChild Existing child to be replaced
           *
           * @return null
           *
           * @throws DOMException
           */
          public Node replaceChild(Node newChild, Node oldChild) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
                  return null;
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @param nodeValue Value to set this node to
           *
           * @throws DOMException
           */
          public void setNodeValue(String nodeValue) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
          }

          /**
           * @see org.w3c.dom.Node
           *
           * @param prefix Prefix to set for this node
           *
           * @throws DOMException
           */
          public void setPrefix(String prefix) throws DOMException
          {
                  throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                      XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
          }
                                                                      
          /**
           *
           * @return The name of this attribute
           */          
          public String getName(){
                  return m_attribute.getName();                            
          }

          /**
           *
           * @return The value of this attribute returned as string
           */          
          public String getValue(){
                  return m_attribute.getSimpleString();                            
          }
          
          /**
           *
           * @return The Element node this attribute is attached to 
           * or null if this attribute is not in use
           */                    
          public Element getOwnerElement(){
                  return m_owner;
          }
          
          /**
           *
           * @return true
           */          
          public boolean getSpecified(){
                  return true;
          }
          
          /**
           * @see org.w3c.dom.Attr
           *
           * @param value Value to set this node to
           *
           * @throws DOMException
           */
          public void setValue(String value) throws DOMException
          {
            throwDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR); 
          }

 	  public TypeInfo getSchemaTypeInfo() { return null; }
    
  	  public boolean isId( ) { return false; }

  	  public Object setUserData(String key,
                                    Object data,
                                    UserDataHandler handler) {
        	return getOwnerDocument().setUserData( key, data, handler);
  	  }

  	  public Object getUserData(String key) {
        	return getOwnerDocument().getUserData( key);
  	  } 

  	  public Object getFeature(String feature, String version) {
        	return isSupported(feature, version) ? this : null;
   	  }
          
          public boolean isEqualNode(Node arg) {
          	return arg == this;
          }
          
          public String lookupNamespaceURI(String specifiedPrefix) {
             	return null;
          }
          
          public boolean isDefaultNamespace(String namespaceURI) {
            	return false;
          }

	  public String lookupPrefix(String namespaceURI) {
	    	return null;
	  }
	  
  	  public boolean isSameNode(Node other) {
        	// we do not use any wrapper so the answer is obvious
        	return this == other;
  	  }
          
  	  public void setTextContent(String textContent)
        	throws DOMException {
        	setNodeValue(textContent);
  	  }

  	  public String getTextContent() throws DOMException {
            	return getNodeValue();  // overriden in some subclasses
   	  }

    	  public short compareDocumentPosition(Node other) throws DOMException {
            	return 0;
    	  }

          public String getBaseURI() {
            	return null;
    	  }
  }        
  
  /**
   * Get an "extension-element-prefix" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param i Index of URI ("extension-element-prefix" property) to get
   *
   * @return URI at given index ("extension-element-prefix" property)
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getExtensionElementPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_ExtensionElementURIs)
      throw new ArrayIndexOutOfBoundsException();

    return m_ExtensionElementURIs.elementAt(i);
  }

  /**
   * Get the number of "extension-element-prefixes" Strings.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @return the number of "extension-element-prefixes" Strings
   */
  public int getExtensionElementPrefixCount()
  {
    return (null != m_ExtensionElementURIs)
           ? m_ExtensionElementURIs.size() : 0;
  }

  /**
   * Find out if the given "extension-element-prefix" property is defined.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param uri The URI to find
   *
   * @return True if the given URI is found
   */
  public boolean containsExtensionElementURI(String uri)
  {

    if (null == m_ExtensionElementURIs)
      return false;

    return m_ExtensionElementURIs.contains(uri);
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_LITERALRESULT;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {

    // TODO: Need prefix.
    return m_rawName;
  }

  /**
   * The XSLT version as specified by this element.
   * @serial
   */
  private String m_version;

  /**
   * Set the "version" property.
   * @see <a href="http://www.w3.org/TR/xslt#forwards">forwards in XSLT Specification</a>
   *
   * @param v Version property value to set
   */
  public void setVersion(String v)
  {
    m_version = v;
  }
  
  /**
   * Get the "version" property.
   * @see <a href="http://www.w3.org/TR/xslt#forwards">forwards in XSLT Specification</a>
   *
   * @return Version property value
   */
  public String getVersion()
  {
    return m_version;
  }

  /**
   * The "exclude-result-prefixes" property.
   * @serial
   */
  private StringVector m_excludeResultPrefixes;

  /**
   * Set the "exclude-result-prefixes" property.
   * The designation of a namespace as an excluded namespace is
   * effective within the subtree of the stylesheet rooted at
   * the element bearing the exclude-result-prefixes or
   * xsl:exclude-result-prefixes attribute; a subtree rooted
   * at an xsl:stylesheet element does not include any stylesheets
   * imported or included by children of that xsl:stylesheet element.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param v vector of prefixes that are resolvable to strings.
   */
  public void setExcludeResultPrefixes(StringVector v)
  {
    m_excludeResultPrefixes = v;
  }

  /**
   * Tell if the result namespace decl should be excluded.  Should be called before
   * namespace aliasing (I think).
   *
   * @param prefix Prefix of namespace to check
   * @param uri URI of namespace to check
   *
   * @return True if the given namespace should be excluded
   *
   * @throws TransformerException
   */
  private boolean excludeResultNSDecl(String prefix, String uri)
          throws TransformerException
  {

    if (null != m_excludeResultPrefixes)
    {
      return containsExcludeResultPrefix(prefix, uri);
    }

    return false;
  }
  
  /**
   * Copy a Literal Result Element into the Result tree, copy the
   * non-excluded namespace attributes, copy the attributes not
   * of the XSLT namespace, and execute the children of the LRE.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
    public void execute(TransformerImpl transformer)
        throws TransformerException
    {
        SerializationHandler rhandler = transformer.getSerializationHandler();

        try
        {

            // JJK Bugzilla 3464, test namespace85 -- make sure LRE's
            // namespace is asserted even if default, since xsl:element
            // may have changed the context.
            rhandler.startPrefixMapping(getPrefix(), getNamespace());

            // Add namespace declarations.
            executeNSDecls(transformer);
            rhandler.startElement(getNamespace(), getLocalName(), getRawName());
        }
        catch (SAXException se)
        {
            throw new TransformerException(se);
        }

        /*
         * If we make it to here we have done a successful startElement()
         * we will do an endElement() call for balance, no matter what happens
         * in the middle.  
         */

        // tException remembers if we had an exception "in the middle"
        TransformerException tException = null;
        try
        {

            // Process any possible attributes from xsl:use-attribute-sets first
            super.execute(transformer);

            //xsl:version, excludeResultPrefixes???
            // Process the list of avts next
            if (null != m_avts)
            {
                int nAttrs = m_avts.size();

                for (int i = (nAttrs - 1); i >= 0; i--)
                {
                    AVT avt = (AVT) m_avts.get(i);
                    XPathContext xctxt = transformer.getXPathContext();
                    int sourceNode = xctxt.getCurrentNode();
                    String stringedValue =
                        avt.evaluate(xctxt, sourceNode, this);

                    if (null != stringedValue)
                    {

                        // Important Note: I'm not going to check for excluded namespace 
                        // prefixes here.  It seems like it's too expensive, and I'm not 
                        // even sure this is right.  But I could be wrong, so this needs 
                        // to be tested against other implementations.

                        rhandler.addAttribute(
                            avt.getURI(),
                            avt.getName(),
                            avt.getRawName(),
                            "CDATA",
                            stringedValue, false);
                    }
                } // end for
            }

            // Now process all the elements in this subtree
            // TODO: Process m_extensionElementPrefixes && m_attributeSetsNames
            transformer.executeChildTemplates(this, true);
        }
        catch (TransformerException te)
        {
            // thrown in finally to prevent original exception consumed by subsequent exceptions
            tException = te;
        }
        catch (SAXException se)
        {
            tException = new TransformerException(se);
        }

        try
        {
            /* we need to do this endElement() to balance the
             * successful startElement() call even if 
             * there was an exception in the middle.
             * Otherwise an exception in the middle could cause a system to hang.
             */
            rhandler.endElement(getNamespace(), getLocalName(), getRawName());
        }
        catch (SAXException se)
        {
            /* we did call endElement(). If thee was an exception
             * in the middle throw that one, otherwise if there
             * was an exception from endElement() throw that one.
             */
            if (tException != null)
                throw tException;
            else
                throw new TransformerException(se);
        }
        
        /* If an exception was thrown in the middle but not with startElement() or
         * or endElement() then its time to let it percolate.
         */ 
        if (tException != null)
            throw tException; 
        
        unexecuteNSDecls(transformer);

        // JJK Bugzilla 3464, test namespace85 -- balance explicit start.
        try
        {
            rhandler.endPrefixMapping(getPrefix());
        }
        catch (SAXException se)
        {
            throw new TransformerException(se);
        }
    }

  /**
   * Compiling templates requires that we be able to list the AVTs
   * ADDED 9/5/2000 to support compilation experiment
   *
   * @return an Enumeration of the literal result attributes associated
   * with this element.
   */
  public Iterator enumerateLiteralResultAttributes()
  {
    return (null == m_avts) ? null : m_avts.iterator();
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
      return visitor.visitLiteralResultElement(this);
    }

    /**
     * Call the children visitors.
     * @param visitor The visitor whose appropriate method will be called.
     */
    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
    {
      if (callAttrs && null != m_avts)
      {
        int nAttrs = m_avts.size();

        for (int i = (nAttrs - 1); i >= 0; i--)
        {
          AVT avt = (AVT) m_avts.get(i);
          avt.callVisitors(visitor);
        }
      }
      super.callChildVisitors(visitor, callAttrs);
    }

    /**
     * Throw a DOMException
     *
     * @param msg key of the error that occured.
     */
    public void throwDOMException(short code, String msg)
    {

      String themsg = XSLMessages.createMessage(msg, null);

      throw new DOMException(code, themsg);
    }

}
