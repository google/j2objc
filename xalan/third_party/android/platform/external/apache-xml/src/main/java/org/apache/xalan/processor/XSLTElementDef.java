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
 * $Id: XSLTElementDef.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.xalan.templates.Constants;
import org.apache.xml.utils.QName;

/**
 * This class defines the allowed structure for an element in a XSLT stylesheet,
 * is meant to reflect the structure defined in http://www.w3.org/TR/xslt#dtd, and the
 * mapping between Xalan classes and the markup elements in the XSLT instance.
 * This actually represents both text nodes and elements.
 */
public class XSLTElementDef
{

  /**
   * Construct an instance of XSLTElementDef.  This must be followed by a
   * call to build().
   */
  XSLTElementDef(){}

  /**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject)
  {
    build(namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    if ( (null != namespace)
    &&  (namespace.equals(Constants.S_XSLNAMESPACEURL)
        || namespace.equals(Constants.S_BUILTIN_EXTENSIONS_URL)
        || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)))
    {
      schema.addAvailableElement(new QName(namespace, name));
      if(null != nameAlias)
        schema.addAvailableElement(new QName(namespace, nameAlias));
    } 
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param has_required true if this element has required elements by the XSLT specification.
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, boolean has_required)
  {
		this.m_has_required = has_required;
    build(namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    if ( (null != namespace)
    &&  (namespace.equals(Constants.S_XSLNAMESPACEURL)
        || namespace.equals(Constants.S_BUILTIN_EXTENSIONS_URL)
        || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)))
    {
      schema.addAvailableElement(new QName(namespace, name));
      if(null != nameAlias)
        schema.addAvailableElement(new QName(namespace, nameAlias));
    } 
		
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param has_required true if this element has required elements by the XSLT specification.
   * @param required true if this element is required by the XSLT specification.
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required)
  {
    this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required);
		this.m_required = required;
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param has_required true if this element has required elements by the XSLT specification.
   * @param required true if this element is required by the XSLT specification.
   * @param order the order this element should appear according to the XSLT specification.   
   * @param multiAllowed whether this element is allowed more than once
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required, int order, 
								 boolean multiAllowed)
  {
		this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required, required);    
		this.m_order = order;
		this.m_multiAllowed = multiAllowed;
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param has_required true if this element has required elements by the XSLT specification.
   * @param required true if this element is required by the XSLT specification.
   * @param has_order whether this element has ordered child elements
   * @param order the order this element should appear according to the XSLT specification.   
   * @param multiAllowed whether this element is allowed more than once
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_required, boolean required, boolean has_order, int order, 
								 boolean multiAllowed)
  {
		this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, has_required, required);    
		this.m_order = order;
		this.m_multiAllowed = multiAllowed;
    this.m_isOrdered = has_order;		
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param has_order whether this element has ordered child elements
   * @param order the order this element should appear according to the XSLT specification.   
   * @param multiAllowed whether this element is allowed more than once
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 boolean has_order, int order, boolean multiAllowed)
  {
    this(schema, namespace, name,  nameAlias,
                 elements, attributes,
                 contentHandler, classObject, 
								 order, multiAllowed);
		this.m_isOrdered = has_order;		
  }
	
	/**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   * @param order the order this element should appear according to the XSLT specification.   
   * @param multiAllowed whether this element is allowed more than once
   */
  XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias,
                 XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
                 XSLTElementProcessor contentHandler, Class classObject, 
								 int order, boolean multiAllowed)
  {
    this(schema, namespace, name, nameAlias, elements, attributes, contentHandler,
          classObject);
    this.m_order = order;
		this.m_multiAllowed = multiAllowed;
  }

  /**
   * Construct an instance of XSLTElementDef that represents text.
   *
   * @param classObject The class of the object that this element def should produce.
   * @param contentHandler The element processor for this element.
   * @param type Content type, one of T_ELEMENT, T_PCDATA, or T_ANY.
   */
  XSLTElementDef(Class classObject, XSLTElementProcessor contentHandler,
                 int type)
  {

    this.m_classObject = classObject;
    this.m_type = type;

    setElementProcessor(contentHandler);
  }

  /**
   * Construct an instance of XSLTElementDef.
   *
   * @param namespace  The Namespace URI, "*", or null.
   * @param name The local name (without prefix), "*", or null.
   * @param nameAlias A potential alias for the name, or null.
   * @param elements An array of allowed child element defs, or null.
   * @param attributes An array of allowed attribute defs, or null.
   * @param contentHandler The element processor for this element.
   * @param classObject The class of the object that this element def should produce.
   */
  void build(String namespace, String name, String nameAlias,
             XSLTElementDef[] elements, XSLTAttributeDef[] attributes,
             XSLTElementProcessor contentHandler, Class classObject)
  {

    this.m_namespace = namespace;
    this.m_name = name;
    this.m_nameAlias = nameAlias;
    this.m_elements = elements;
    this.m_attributes = attributes;

    setElementProcessor(contentHandler);

    this.m_classObject = classObject;
		
		if (hasRequired() && m_elements != null)
		{
			int n = m_elements.length;
			for (int i = 0; i < n; i++)
			{
				XSLTElementDef def = m_elements[i];
				
				if (def != null && def.getRequired())
				{
					if (m_requiredFound == null)			
						m_requiredFound = new Hashtable();
					m_requiredFound.put(def.getName(), "xsl:" +def.getName()); 
				}
			}
		}
  }

  /**
   * Tell if two objects are equal, when either one may be null.
   * If both are null, they are considered equal.
   *
   * @param obj1 A reference to the first object, or null.
   * @param obj2 A reference to the second object, or null.
   *
   * @return true if the to objects are equal by both being null or 
   * because obj2.equals(obj1) returns true.
   */
  private static boolean equalsMayBeNull(Object obj1, Object obj2)
  {
    return (obj2 == obj1)
           || ((null != obj1) && (null != obj2) && obj2.equals(obj1));
  }

  /**
   * Tell if the two string refs are equal,
   * equality being defined as:
   * 1) Both strings are null.
   * 2) One string is null and the other is empty.
   * 3) Both strings are non-null, and equal.
   *
   * @param s1 A reference to the first string, or null.
   * @param s2 A reference to the second string, or null.
   *
   * @return true if Both strings are null, or if 
   * one string is null and the other is empty, or if 
   * both strings are non-null, and equal because 
   * s1.equals(s2) returns true.
   */
  private static boolean equalsMayBeNullOrZeroLen(String s1, String s2)
  {

    int len1 = (s1 == null) ? 0 : s1.length();
    int len2 = (s2 == null) ? 0 : s2.length();

    return (len1 != len2) ? false 
						 : (len1 == 0) ? true 
								 : s1.equals(s2);
  }

  /** Content type enumerations    */
  static final int T_ELEMENT = 1, T_PCDATA = 2, T_ANY = 3;

  /**
   * The type of this element.
   */
  private int m_type = T_ELEMENT;

  /**
   * Get the type of this element.
   *
   * @return Content type, one of T_ELEMENT, T_PCDATA, or T_ANY.
   */
  int getType()
  {
    return m_type;
  }

  /**
   * Set the type of this element.
   *
   * @param t Content type, one of T_ELEMENT, T_PCDATA, or T_ANY.
   */
  void setType(int t)
  {
    m_type = t;
  }

  /**
   * The allowed namespace for this element.
   */
  private String m_namespace;

  /**
   * Get the allowed namespace for this element.
   *
   * @return The Namespace URI, "*", or null.
   */
  String getNamespace()
  {
    return m_namespace;
  }

  /**
   * The name of this element.
   */
  private String m_name;

  /**
   * Get the local name of this element.
   *
   * @return The local name of this element, "*", or null.
   */
  String getName()
  {
    return m_name;
  }

  /**
   * The name of this element.
   */
  private String m_nameAlias;

  /**
   * Get the name of this element.
   *
   * @return A potential alias for the name, or null.
   */
  String getNameAlias()
  {
    return m_nameAlias;
  }

  /**
   * The allowed elements for this type.
   */
  private XSLTElementDef[] m_elements;

  /**
   * Get the allowed elements for this type.
   *
   * @return An array of allowed child element defs, or null.
   * @xsl.usage internal
   */
  public XSLTElementDef[] getElements()
  {
    return m_elements;
  }

  /**
   * Set the allowed elements for this type.
   *
   * @param defs An array of allowed child element defs, or null.
   */
  void setElements(XSLTElementDef[] defs)
  {
    m_elements = defs;
  }

  /**
   * Tell if the namespace URI and local name match this
   * element.
   * @param uri The namespace uri, which may be null.
   * @param localName The local name of an element, which may be null.
   *
   * @return true if the uri and local name arguments are considered 
   * to match the uri and local name of this element def.
   */
  private boolean QNameEquals(String uri, String localName)
  {

    return (equalsMayBeNullOrZeroLen(m_namespace, uri)
            && (equalsMayBeNullOrZeroLen(m_name, localName)
                || equalsMayBeNullOrZeroLen(m_nameAlias, localName)));
  }

  /**
   * Given a namespace URI, and a local name, get the processor
   * for the element, or return null if not allowed.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   *
   * @return The element processor that matches the arguments, or null.
   */
  XSLTElementProcessor getProcessorFor(String uri, String localName) 
	{

    XSLTElementProcessor elemDef = null;  // return value

    if (null == m_elements)
      return null;

    int n = m_elements.length;
    int order = -1;
		boolean multiAllowed = true;
    for (int i = 0; i < n; i++)
    {
      XSLTElementDef def = m_elements[i];

      // A "*" signals that the element allows literal result
      // elements, so just assign the def, and continue to  
      // see if anything else matches.
      if (def.m_name.equals("*"))
      {
				
        // Don't allow xsl elements
        if (!equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL))
				{
          elemDef = def.m_elementProcessor;
				  order = def.getOrder();
					multiAllowed = def.getMultiAllowed();
				}
      }
			else if (def.QNameEquals(uri, localName))
			{	
				if (def.getRequired())
					this.setRequiredFound(def.getName(), true);
				order = def.getOrder();
				multiAllowed = def.getMultiAllowed();
				elemDef = def.m_elementProcessor;
				break;
			}
		}		
		
		if (elemDef != null && this.isOrdered())
		{			
			int lastOrder = getLastOrder();
			if (order > lastOrder)
				setLastOrder(order);
			else if (order == lastOrder && !multiAllowed)
			{
				return null;
			}
			else if (order < lastOrder && order > 0)
			{
				return null;
			}
		}

    return elemDef;
  }

  /**
   * Given an unknown element, get the processor
   * for the element.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   *
   * @return normally a {@link ProcessorUnknown} reference.
   * @see ProcessorUnknown
   */
  XSLTElementProcessor getProcessorForUnknown(String uri, String localName)
  {

    // XSLTElementProcessor lreDef = null; // return value
    if (null == m_elements)
      return null;

    int n = m_elements.length;

    for (int i = 0; i < n; i++)
    {
      XSLTElementDef def = m_elements[i];

      if (def.m_name.equals("unknown") && uri.length() > 0)
      {
        return def.m_elementProcessor;
      }
    }

    return null;
  }

  /**
   * The allowed attributes for this type.
   */
  private XSLTAttributeDef[] m_attributes;

  /**
   * Get the allowed attributes for this type.
   *
   * @return An array of allowed attribute defs, or null.
   */
  XSLTAttributeDef[] getAttributes()
  {
    return m_attributes;
  }

  /**
   * Given a namespace URI, and a local name, return the element's
   * attribute definition, if it has one.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   *
   * @return The attribute def that matches the arguments, or null.
   */
  XSLTAttributeDef getAttributeDef(String uri, String localName)
  {

    XSLTAttributeDef defaultDef = null;
    XSLTAttributeDef[] attrDefs = getAttributes();
    int nAttrDefs = attrDefs.length;

    for (int k = 0; k < nAttrDefs; k++)
    {
      XSLTAttributeDef attrDef = attrDefs[k];
      String uriDef = attrDef.getNamespace();
      String nameDef = attrDef.getName();
      
      if (nameDef.equals("*") && (equalsMayBeNullOrZeroLen(uri, uriDef) || 
          (uriDef != null && uriDef.equals("*") && uri!=null && uri.length() > 0 )))
      {
        return attrDef;
      }
      else if (nameDef.equals("*") && (uriDef == null))
      {

        // In this case, all attributes are legal, so return 
        // this as the last resort.
        defaultDef = attrDef;
      }
      else if (equalsMayBeNullOrZeroLen(uri, uriDef)
               && localName.equals(nameDef))
      {
        return attrDef;
      }
    }

    if (null == defaultDef)
    {
      if (uri.length() > 0 && !equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL))
      {
        return XSLTAttributeDef.m_foreignAttr;
      }
    }

    return defaultDef;
  }

  /**
   * If non-null, the ContentHandler/TransformerFactory for this element.
   */
  private XSLTElementProcessor m_elementProcessor;

  /**
   * Return the XSLTElementProcessor for this element.
   *
   * @return The element processor for this element.
   * @xsl.usage internal
   */
  public XSLTElementProcessor getElementProcessor()
  {
    return m_elementProcessor;
  }

  /**
   * Set the XSLTElementProcessor for this element.
   *
   * @param handler The element processor for this element.
   * @xsl.usage internal
   */
  public void setElementProcessor(XSLTElementProcessor handler)
  {

    if (handler != null)
    {
      m_elementProcessor = handler;

      m_elementProcessor.setElemDef(this);
    }
  }

  /**
   * If non-null, the class object that should in instantiated for
   * a Xalan instance of this element.
   */
  private Class m_classObject;

  /**
   * Return the class object that should in instantiated for
   * a Xalan instance of this element.
   *
   * @return The class of the object that this element def should produce, or null.
   */
  Class getClassObject()
  {
    return m_classObject;
  }
	
	/**
   * If true, this has a required element.
   */
  private boolean m_has_required = false;

  /**
   * Get whether or not this has a required element.
   *
   * @return true if this this has a required element.
   */
  boolean hasRequired()
  {
    return m_has_required;
  }
	
	/**
   * If true, this is a required element.
   */
  private boolean m_required = false;

  /**
   * Get whether or not this is a required element.
   *
   * @return true if this is a required element.
   */
  boolean getRequired()
  {
    return m_required;
  }
	
	Hashtable m_requiredFound;
	
	/**
   * Set this required element found.
   *
   */
  void setRequiredFound(String elem, boolean found)
  {
   if (m_requiredFound.get(elem) != null) 
		 m_requiredFound.remove(elem);
  }
	
	/**
   * Get whether all required elements were found.
   *
   * @return true if all required elements were found.
   */
  boolean getRequiredFound()
  {
		if (m_requiredFound == null)
			return true;
    return m_requiredFound.isEmpty();
  }
	
	/**
   * Get required elements that were not found.
   *
   * @return required elements that were not found.
   */
  String getRequiredElem()
  {
		if (m_requiredFound == null)
			return null;
		Enumeration elems = m_requiredFound.elements();
		String s = "";
		boolean first = true;
		while (elems.hasMoreElements())
		{
			if (first)
				first = false;
			else
			 s = s + ", ";
			s = s + (String)elems.nextElement();
		}
    return s;
  }
	
	boolean m_isOrdered = false;	
	
	/**
   * Get whether this element requires ordered children.
   *
   * @return true if this element requires ordered children.
   */
  boolean isOrdered()
  {
		/*if (!m_CheckedOrdered)
		{
			m_CheckedOrdered = true;
			m_isOrdered = false;
			if (null == m_elements)
				return false;

			int n = m_elements.length;

			for (int i = 0; i < n; i++)
			{
				if (m_elements[i].getOrder() > 0)
				{
					m_isOrdered = true;
					return true;
				}
			}
			return false;
		}
		else*/
			return m_isOrdered;
  }
	
	/**
   * the order that this element should appear, or -1 if not ordered
   */
  private int m_order = -1;
	
	/**
   * Get the order that this element should appear .
   *
   * @return the order that this element should appear.
   */
  int getOrder()
  {
    return m_order;
  }
	
	/**
   * the highest order of child elements have appeared so far, 
   * or -1 if not ordered
   */
  private int m_lastOrder = -1;
	
	/**
   * Get the highest order of child elements have appeared so far .
   *
   * @return the highest order of child elements have appeared so far.
   */
  int getLastOrder()
  {
    return m_lastOrder;
  }
	
	/**
   * Set the highest order of child elements have appeared so far .
   *
   * @param order the highest order of child elements have appeared so far.
   */
  void setLastOrder(int order)
  {
    m_lastOrder = order ;
  }
	
	/**
   * True if this element can appear multiple times
   */
  private boolean m_multiAllowed = true;
	
	/**
   * Get whether this element can appear multiple times
   *
   * @return true if this element can appear multiple times
   */
  boolean getMultiAllowed()
  {
    return m_multiAllowed;
  }
}
