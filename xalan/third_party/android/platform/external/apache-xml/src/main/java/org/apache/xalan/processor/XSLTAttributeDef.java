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
 * $Id: XSLTAttributeDef.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringToIntTable;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPath;

 
/**
 * This class defines an attribute for an element in a XSLT stylesheet,
 * is meant to reflect the structure defined in http://www.w3.org/TR/xslt#dtd, and the
 * mapping between Xalan classes and the markup attributes in the element.
 */
public class XSLTAttributeDef
{
   // How to handle invalid values for this attribute 
   static final int FATAL = 0;
   static final int ERROR = 1;
   static final int WARNING = 2;
   
   
  /**
   * Construct an instance of XSLTAttributeDef.
   *
   * @param namespace The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param type One of T_CDATA, T_URL, T_AVT, T_PATTERN, T_EXPR, T_CHAR,
   * T_NUMBER, T_YESNO, T_QNAME, T_QNAMES, T_ENUM, T_SIMPLEPATTERNLIST,
   * T_NMTOKEN, T_STRINGLIST, T_PREFIX_URLLIST, T_ENUM_OR_PQNAME, T_NCNAME.
   * @param required true if this is attribute is required by the XSLT specification.
   * @param supportsAVT true if this attribute supports AVT's.
   * @param errorType the type of error to issue if validation fails.  One of FATAL, ERROR, WARNING. 
   */
  XSLTAttributeDef(String namespace, String name, int type, boolean required, boolean supportsAVT, int errorType)
  {
    this.m_namespace = namespace;
    this.m_name = name;
    this.m_type = type;
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;
    this.m_errorType = errorType;
  }

  /**
   * Construct an instance of XSLTAttributeDef.
   *
   * @param namespace The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param type One of T_CDATA, T_URL, T_AVT, T_PATTERN, T_EXPR,
   * T_CHAR, T_NUMBER, T_YESNO, T_QNAME, T_QNAMES, T_ENUM,
   * T_SIMPLEPATTERNLIST, T_NMTOKEN, T_STRINGLIST, T_PREFIX_URLLIST, 
   * T_ENUM_OR_PQNAME, T_NCNAME.
   * @param supportsAVT true if this attribute supports AVT's. 
   * @param errorType the type of error to issue if validation fails.  One of FATAL, ERROR, WARNING. 
   * @param defaultVal The default value for this attribute.
   */
  XSLTAttributeDef(String namespace, String name, int type, boolean supportsAVT, int errorType, String defaultVal)
  {

    this.m_namespace = namespace;
    this.m_name = name;
    this.m_type = type;
    this.m_required = false;
    this.m_supportsAVT = supportsAVT;  
    this.m_errorType = errorType;      
    this.m_default = defaultVal;
   }

  /**
   * Construct an instance of XSLTAttributeDef that uses two
   * enumerated values.
   *
   * @param namespace The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param required true if this attribute is required by the XSLT specification.
   * @param supportsAVT true if this attribute supports AVT's.  
   * @param prefixedQNameValAllowed If true, the type is T_ENUM_OR_PQNAME       
   * @param errorType the type of error to issue if validation fails.  One of FATAL, ERROR, WARNING. 
   * @param k1 The XSLT name of the enumerated value.
   * @param v1 An integer representation of k1.
   * @param k2 The XSLT name of the enumerated value.
   * @param v2 An integer representation of k2.
    */
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, 
                    boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2)
  {

    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;    
    this.m_errorType = errorType;    
    m_enums = new StringToIntTable(2);

    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
  }

  /**
   * Construct an instance of XSLTAttributeDef that uses three
   * enumerated values.
   *
   * @param namespace The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param required true if this attribute is required by the XSLT specification.
   * @param supportsAVT true if this attribute supports AVT's.
   * @param prefixedQNameValAllowed If true, the type is T_ENUM_OR_PQNAME
   * @param errorType the type of error to issue if validation fails.  One of FATAL, ERROR, WARNING.    * 
   * @param k1 The XSLT name of the enumerated value.
   * @param v1 An integer representation of k1.
   * @param k2 The XSLT name of the enumerated value.
   * @param v2 An integer representation of k2.
   * @param k3 The XSLT name of the enumerated value.
   * @param v3 An integer representation of k3.
   */
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT,
                    boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3)
  {

    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT; 
    this.m_errorType = errorType;      
    m_enums = new StringToIntTable(3);

    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
    m_enums.put(k3, v3);
  }

  /**
   * Construct an instance of XSLTAttributeDef that uses three
   * enumerated values.
   *
   * @param namespace The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param required true if this attribute is required by the XSLT specification.
   * @param supportsAVT true if this attribute supports AVT's.
   * @param prefixedQNameValAllowed If true, the type is T_ENUM_OR_PQNAME
   * @param errorType the type of error to issue if validation fails.  One of FATAL, ERROR, WARNING.    * @param k1 The XSLT name of the enumerated value.
   * @param v1 An integer representation of k1.
   * @param k2 The XSLT name of the enumerated value.
   * @param v2 An integer representation of k2.
   * @param k3 The XSLT name of the enumerated value.
   * @param v3 An integer representation of k3.
   * @param k4 The XSLT name of the enumerated value.
   * @param v4 An integer representation of k4.
   */
  XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT,
                   boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, 
                   String k3, int v3, String k4, int v4)
  {

    this.m_namespace = namespace;
    this.m_name = name;
	this.m_type = prefixedQNameValAllowed ? this.T_ENUM_OR_PQNAME : this.T_ENUM;    
    this.m_required = required;
    this.m_supportsAVT = supportsAVT;      
    this.m_errorType = errorType; 
    m_enums = new StringToIntTable(4);

    m_enums.put(k1, v1);
    m_enums.put(k2, v2);
    m_enums.put(k3, v3);
    m_enums.put(k4, v4);
  }

  /** Type values that represent XSLT attribute types. */
  static final int T_CDATA = 1,

  // <!-- Used for the type of an attribute value that is a URI reference.-->
  T_URL = 2,

  // <!-- Used for the type of an attribute value that is an
  // attribute value template.-->
  T_AVT = 3,  // Attribute Value Template

  // <!-- Used for the type of an attribute value that is a pattern.-->
  T_PATTERN = 4,

  // <!-- Used for the type of an attribute value that is an expression.-->
  T_EXPR = 5,

  // <!-- Used for the type of an attribute value that consists
  // of a single character.-->
  T_CHAR = 6,

  // <!-- Used for the type of an attribute value that is a number. -->
  T_NUMBER = 7,

  // Used for boolean values
  T_YESNO = 8,

  // <!-- Used for the type of an attribute value that is a QName; the prefix
  // gets expanded by the XSLT processor. -->
  T_QNAME = 9,

  // <!--Used for a whitespace-separated list of QNames where the non-prefixed
  // entries are not to be placed in the default namespace. -->
  T_QNAMES = 10,

  // <!-- Used for enumerated values -->
  T_ENUM = 11,

  // Used for simple match patterns, i.e. xsl:strip-space spec.
  T_SIMPLEPATTERNLIST = 12,

  // Used for a known token.
  T_NMTOKEN = 13,

  // Used for a list of white-space delimited strings.
  T_STRINGLIST = 14,

  // Used for a list of white-space delimited strings.
  // Prefixes are checked to make sure they refer to 
  // valid namespaces, and are resolved when processed
  T_PREFIX_URLLIST = 15,
  
  // Used for enumerated values, one of which could be a qname-but-not-ncname
  T_ENUM_OR_PQNAME = 16,

  // Used for the type of an attribute value that is a NCName
  T_NCNAME = 17,
  
  // Used for QName attributes that are always AVT.  Prefix isn't resolved.
  T_AVT_QNAME = 18,
  
  // Used for a list of QNames where non-prefixed items are to be resolved
  // using the default namespace (This is only true for cdata-section-elements)
  T_QNAMES_RESOLVE_NULL = 19,
  
  // Used for a list of white-space delimited strings.
  // strings are checked to make sure they are valid 
  // prefixes, and are not expanded when processed. 
  T_PREFIXLIST = 20;

  /** Representation for an attribute in a foreign namespace. */
  static final XSLTAttributeDef m_foreignAttr = new XSLTAttributeDef("*", "*",
                                            XSLTAttributeDef.T_CDATA,false, false, WARNING);

  /** Method name that objects may implement if they wish to have forein attributes set. */
  static final String S_FOREIGNATTR_SETTER = "setForeignAttr";

  /**
   * The allowed namespace for this element.
   */
  private String m_namespace;

  /**
   * Get the allowed namespace for this attribute.
   *
   * @return The allowed namespace for this attribute, which may be null, or may be "*".
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
   * Get the name of this attribute.
   *
   * @return non-null reference to the name of this attribute, which may be "*".
   */
  String getName()
  {
    return m_name;
  }

  /**
   * The type of this attribute value.
   */
  private int m_type;

  /**
   * Get the type of this attribute value.
   *
   * @return One of T_CDATA, T_URL, T_AVT, T_PATTERN, T_EXPR, T_CHAR,
   * T_NUMBER, T_YESNO, T_QNAME, T_QNAMES, T_ENUM, T_SIMPLEPATTERNLIST,
   * T_NMTOKEN, T_STRINGLIST, T_PREFIX_URLLIST, T_ENUM_OR_PQNAME.
   */
  int getType()
  {
    return m_type;
  }

  /**
   * If this element is of type T_ENUM, this will contain
   * a map from the attribute string to the Xalan integer
   * value.
   */
  private StringToIntTable m_enums;

  /**
   * If this element is of type T_ENUM, this will return
   * a map from the attribute string to the Xalan integer
   * value.
   * @param key The XSLT attribute value.
   *
   * @return The integer representation of the enumerated value for this attribute.
   * @throws Throws NullPointerException if m_enums is null.
   */
  private int getEnum(String key)
  {
    return m_enums.get(key);
  }

 /**
   * If this element is of type T_ENUM, this will return
   * an array of strings - the values in the enumeration
   *
   * @return An array of the enumerated values permitted for this attribute.
   *
   * @throws Throws NullPointerException if m_enums is null.
   */
  private String[] getEnumNames()
  {
    return m_enums.keys();
  }

  /**
   * The default value for this attribute.
   */
  private String m_default;

  /**
   * Get the default value for this attribute.
   *
   * @return The default value for this attribute, or null.
   */
  String getDefault()
  {
    return m_default;
  }

  /**
   * Set the default value for this attribute.
   *
   * @param def String representation of the default value for this attribute.
   */
  void setDefault(String def)
  {
    m_default = def;
  }

  /**
   * If true, this is a required attribute.
   */
  private boolean m_required;

  /**
   * Get whether or not this is a required attribute.
   *
   * @return true if this is a required attribute.
   */
  boolean getRequired()
  {
    return m_required;
  }

  /**
   * If true, this is attribute supports AVT's.
   */
  private boolean m_supportsAVT;

  /**
   * Get whether or not this attribute supports AVT's.
   *
   * @return true if this attribute supports AVT's.
   */
  boolean getSupportsAVT()
  {
    return m_supportsAVT;
  }
  
  int m_errorType = this.WARNING;
  
  /**
   * Get the type of error message to use if the attribute value is invalid.
   *
   * @return one of XSLAttributeDef.FATAL, XSLAttributeDef.ERROR, XSLAttributeDef.WARNING
   */
  int getErrorType()
  {
    return m_errorType;
  }
  /**
   * String that should represent the setter method which which
   * may be used on objects to set a value that represents this attribute  
   */
  String m_setterString = null;

  /**
   * Return a string that should represent the setter method.
   * The setter method name will be created algorithmically the
   * first time this method is accessed, and then cached for return
   * by subsequent invocations of this method.
   *
   * @return String that should represent the setter method which which
   * may be used on objects to set a value that represents this attribute,
   * of null if no setter method should be called.
   */
  public String getSetterMethodName()
  {

    if (null == m_setterString)
    {
      if (m_foreignAttr == this)
      {
        return S_FOREIGNATTR_SETTER;
      }
      else if (m_name.equals("*"))
      {
        m_setterString = "addLiteralResultAttribute";

        return m_setterString;
      }

      StringBuffer outBuf = new StringBuffer();

      outBuf.append("set");

      if ((m_namespace != null)
              && m_namespace.equals(Constants.S_XMLNAMESPACEURI))
      {
        outBuf.append("Xml");
      }

      int n = m_name.length();

      for (int i = 0; i < n; i++)
      {
        char c = m_name.charAt(i);

        if ('-' == c)
        {
          i++;

          c = m_name.charAt(i);
          c = Character.toUpperCase(c);
        }
        else if (0 == i)
        {
          c = Character.toUpperCase(c);
        }

        outBuf.append(c);
      }

      m_setterString = outBuf.toString();
    }

    return m_setterString;
  }

  /**
   * Process an attribute string of type T_AVT into
   * a AVT value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value Should be an Attribute Value Template string.
   *
   * @return An AVT object that may be used to evaluate the Attribute Value Template.
   *
   * @throws org.xml.sax.SAXException which will wrap a
   * {@link javax.xml.transform.TransformerException}, if there is a syntax error
   * in the attribute value template string.
   */
  AVT processAVT(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    try
    {
      AVT avt = new AVT(handler, uri, name, rawName, value, owner);

      return avt;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Process an attribute string of type T_CDATA into
   * a String value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value non-null string reference.
   *
   * @return The value argument.
   * 
   * @throws org.xml.sax.SAXException.
   */
  Object processCDATA(StylesheetHandler handler, String uri, String name,
                      String rawName, String value, ElemTemplateElement owner)
                      throws org.xml.sax.SAXException
  {
  	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
  	} else {  	  	
	    return value;
  	}
  }

  /**
   * Process an attribute string of type T_CHAR into
   * a Character value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value Should be a string with a length of 1.
   *
   * @return Character object.
   *
   * @throws org.xml.sax.SAXException if the string is not a length of 1.
   */
  Object processCHAR(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	
		  // If an AVT wasn't used, validate the value
		  if ((avt.isSimple()) && (value.length() != 1)) {
		  	handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[] {name, value},null);
            return null;
		  }	
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	} else {    
	    if (value.length() != 1)
	    {
            handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[] {name, value},null);
            return null;
	    }

	    return new Character(value.charAt(0));
	}
  }

  /**
   * Process an attribute string of type T_ENUM into a int value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value non-null string that represents an enumerated value that is
   * valid for this element.
   * @param owner
   *
   * @return An Integer representation of the enumerated value if this attribute does not support
   *         AVT.  Otherwise, and AVT is returned.
   */
  Object processENUM(StylesheetHandler handler, String uri, String name,
                     String rawName, String value, ElemTemplateElement owner)
                     throws org.xml.sax.SAXException
  {

	AVT avt = null;
	if (getSupportsAVT()) {
	    try
	    {
	      avt = new AVT(handler, uri, name, rawName, value, owner);
	      
	      // If this attribute used an avt, then we can't validate at this time.
	      if (!avt.isSimple()) return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	}    
	
    int retVal = this.getEnum(value);
    
	if (retVal == StringToIntTable.INVALID_KEY) 
    {
       StringBuffer enumNamesList = getListOfEnums();
       handleError(handler, XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },null);
       return null;
    }

	if (getSupportsAVT()) return avt;
	else return new Integer(retVal);	

  }

  /**
   * Process an attribute string of that is either an enumerated value or a qname-but-not-ncname.
   * Returns an AVT, if this attribute support AVT; otherwise returns int or qname.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value non-null string that represents an enumerated value that is
   * valid for this element.
   * @param owner
   *
   * @return AVT if attribute supports AVT. An Integer representation of the enumerated value if
   *         attribute does not support AVT and an enumerated value was used.  Otherwise a qname
   *         is returned.
   */
  Object processENUM_OR_PQNAME(StylesheetHandler handler, String uri, String name,
                     String rawName, String value, ElemTemplateElement owner)
                     throws org.xml.sax.SAXException
  {

	Object objToReturn = null;
	
	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	      if (!avt.isSimple()) return avt;
	      else objToReturn = avt;
	    }  
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }
	}    
	
    // An avt wasn't used.
  	int key = this.getEnum(value);
    
    if (key != StringToIntTable.INVALID_KEY) 
    {
        if (objToReturn == null) objToReturn = new Integer(key);
    }

    // enum not used.  Validate qname-but-not-ncname.
    else
    {
        try 
        {
			QName qname = new QName(value, handler, true);
            if (objToReturn == null) objToReturn = qname;	
	        
			if (qname.getPrefix() == null) {
	           StringBuffer enumNamesList = getListOfEnums();

 	           enumNamesList.append(" <qname-but-not-ncname>");
               handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },null); 
               return null;
        
	        }            
        }
        catch (IllegalArgumentException ie) 
        {
           StringBuffer enumNamesList = getListOfEnums();
           enumNamesList.append(" <qname-but-not-ncname>");
           
           handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },ie); 
           return null;

        }
        catch (RuntimeException re)
        {
           StringBuffer enumNamesList = getListOfEnums();
           enumNamesList.append(" <qname-but-not-ncname>");

           handleError(handler,XSLTErrorResources.INVALID_ENUM,new Object[]{name, value, enumNamesList.toString() },re); 
           return null;
        }    
  	}
  	
  	return objToReturn;
  }

  /**
   * Process an attribute string of type T_EXPR into
   * an XPath value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value An XSLT expression string.
   *
   * @return an XPath object that may be used for evaluation.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the expression
   * string contains a syntax error.
   */
  Object processEXPR(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    try
    {
      XPath expr = handler.createXPath(value, owner);

      return expr;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Process an attribute string of type T_NMTOKEN into
   * a String value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A NMTOKEN string.
   *
   * @return the value argument or an AVT if this attribute supports AVTs.
   * 
   * @throws org.xml.sax.SAXException if the value is not a valid nmtoken
   */
  Object processNMTOKEN(StylesheetHandler handler, String uri, String name,
                        String rawName, String value, ElemTemplateElement owner)
             throws org.xml.sax.SAXException
  {
  	
  	if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	
		  // If an AVT wasn't used, validate the value
		  if ((avt.isSimple()) && (!XML11Char.isXML11ValidNmtoken(value))) {
            handleError(handler,XSLTErrorResources.INVALID_NMTOKEN, new Object[] {name,value},null);
            return null;
		  }	
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
  	} else {
  		if (!XML11Char.isXML11ValidNmtoken(value)) {
            handleError(handler,XSLTErrorResources.INVALID_NMTOKEN, new Object[] {name,value},null);
            return null;
  		}
  	}	  			
    return value;
  }

  /**
   * Process an attribute string of type T_PATTERN into
   * an XPath match pattern value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A match pattern string.
   *
   * @return An XPath pattern that may be used to evaluate the XPath.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the match pattern
   * string contains a syntax error.
   */
  Object processPATTERN(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    try
    {
      XPath pattern = handler.createMatchPatternXPath(value, owner);

      return pattern;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Process an attribute string of type T_NUMBER into
   * a double value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A string that can be parsed into a double value.
   * @param number
   *
   * @return A Double object.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException}
   * if the string does not contain a parsable number.
   */
  Object processNUMBER(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {


	if (getSupportsAVT()) 
	{
		Double val;
		AVT avt = null;
	    try
	    {
	      avt = new AVT(handler, uri, name, rawName, value, owner);
	      
	      // If this attribute used an avt, then we can't validate at this time.
	      if (avt.isSimple()) 
	      {
	      	val = Double.valueOf(value);
	      }
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    } 
	    catch (NumberFormatException nfe)
	    {
	     	handleError(handler,XSLTErrorResources.INVALID_NUMBER, new Object[] {name, value}, nfe);
            return null;
	    }
	    return avt;
	
	} 
	else
    {
	    try
	    {
	      return Double.valueOf(value);
	    }
	    catch (NumberFormatException nfe)
	    {
            handleError(handler,XSLTErrorResources.INVALID_NUMBER, new Object[] {name, value}, nfe);
            return null;
	    }
    }    
  }

  /**
   * Process an attribute string of type T_QNAME into a QName value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A string that represents a potentially prefix qualified name.
   * @param owner
   *
   * @return A QName object if this attribute does not support AVT's.  Otherwise, an AVT
   *         is returned.
   *
   * @throws org.xml.sax.SAXException if the string contains a prefix that can not be
   * resolved, or the string contains syntax that is invalid for a qualified name.
   */
  Object processQNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

     try 
        {	
   	      QName qname = new QName(value, handler, true);
          return qname;
        }
        catch (IllegalArgumentException ie)
        {
            // thrown by QName constructor
            handleError(handler,XSLTErrorResources.INVALID_QNAME, new Object[] {name, value},ie);
            return null;
        }
        catch (RuntimeException re) {
            // thrown by QName constructor
            handleError(handler,XSLTErrorResources.INVALID_QNAME, new Object[] {name, value},re);
            return null;
        }
  	}
 

  /**
   * Process an attribute string of type T_QNAME into a QName value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A string that represents a potentially prefix qualified name.
   * @param owner
   *
   * @return An AVT is returned.
   *
   * @throws org.xml.sax.SAXException if the string contains a prefix that can not be
   * resolved, or the string contains syntax that is invalid for a qualified name.
   */
  Object processAVT_QNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

       AVT avt = null;
       try
       {
          avt = new AVT(handler, uri, name, rawName, value, owner);
    
          // If an AVT wasn't used, validate the value
          if (avt.isSimple())
          {
             int indexOfNSSep = value.indexOf(':');

             if (indexOfNSSep >= 0) 
             {   
                  String prefix = value.substring(0, indexOfNSSep);
                  if (!XML11Char.isXML11ValidNCName(prefix))
                  {
                     handleError(handler,XSLTErrorResources.INVALID_QNAME,new Object[]{name,value },null);
                     return null;
                  }
             }
                 
             String localName =  (indexOfNSSep < 0)
                 ? value : value.substring(indexOfNSSep + 1); 
             
             if ((localName == null) || (localName.length() == 0) ||
                 (!XML11Char.isXML11ValidNCName(localName)))
             {    
                     handleError(handler,XSLTErrorResources.INVALID_QNAME,new Object[]{name,value },null );
                     return null;
             }
          }  
        }
        catch (TransformerException te)
        {
           // thrown by AVT constructor
          throw new org.xml.sax.SAXException(te);
        } 
    
    return avt;
 }

  /**
   * Process an attribute string of type NCName into a String
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A string that represents a potentially prefix qualified name.
   * @param owner
   *
   * @return A String object if this attribute does not support AVT's.  Otherwise, an AVT
   *         is returned.
   *
   * @throws org.xml.sax.SAXException if the string contains a prefix that can not be
   * resolved, or the string contains syntax that is invalid for a NCName.
   */
  Object processNCNAME(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {
    
    if (getSupportsAVT()) 
    {
        AVT avt = null;
        try
        {
          avt = new AVT(handler, uri, name, rawName, value, owner);
    
          // If an AVT wasn't used, validate the value
          if ((avt.isSimple()) &&  (!XML11Char.isXML11ValidNCName(value))) 
          {
             handleError(handler,XSLTErrorResources.INVALID_NCNAME,new Object[] {name,value},null);
             return null;
          }      
          return avt;
        }
        catch (TransformerException te)
        {
           // thrown by AVT constructor
          throw new org.xml.sax.SAXException(te);
        } 
        
    } else {
        if (!XML11Char.isXML11ValidNCName(value)) 
        {
            handleError(handler,XSLTErrorResources.INVALID_NCNAME,new Object[] {name,value},null);
            return null;
        }
        return value;
    }
 }

  /**
   * Process an attribute string of type T_QNAMES into a vector of QNames where
   * the specification requires that non-prefixed elements not be placed in a
   * namespace.  (See section 2.4 of XSLT 1.0.)
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A whitespace delimited list of qualified names.
   *
   * @return a Vector of QName objects.
   *
   * @throws org.xml.sax.SAXException if the one of the qualified name strings
   * contains a prefix that can not be
   * resolved, or a qualified name contains syntax that is invalid for a qualified name.
   */
  Vector processQNAMES(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nQNames = tokenizer.countTokens();
    Vector qnames = new Vector(nQNames);

    for (int i = 0; i < nQNames; i++)
    {
      // Fix from Alexander Rudnev
      qnames.addElement(new QName(tokenizer.nextToken(), handler));
    }

    return qnames;
  }

 /**
   * Process an attribute string of type T_QNAMES_RESOLVE_NULL into a vector
   * of QNames where the specification requires non-prefixed elements to be
   * placed in the default namespace.  (See section 16 of XSLT 1.0; the
   * <em>only</em> time that this will get called is for the
   * <code>cdata-section-elements</code> attribute on <code>xsl:output</code>.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A whitespace delimited list of qualified names.
   *
   * @return a Vector of QName objects.
   *
   * @throws org.xml.sax.SAXException if the one of the qualified name strings
   * contains a prefix that can not be resolved, or a qualified name contains
   * syntax that is invalid for a qualified name.
   */
  final Vector processQNAMESRNU(StylesheetHandler handler, String uri,
    String name, String rawName, String value)
    throws org.xml.sax.SAXException
  {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nQNames = tokenizer.countTokens();
    Vector qnames = new Vector(nQNames);

    String defaultURI = handler.getNamespaceForPrefix("");
    for (int i = 0; i < nQNames; i++)
    {
      String tok = tokenizer.nextToken();
      if (tok.indexOf(':') == -1) {
        qnames.addElement(new QName(defaultURI,tok));
      } else {
        qnames.addElement(new QName(tok, handler));
      }
    }
    return qnames;
  }

  /**
   * Process an attribute string of type T_SIMPLEPATTERNLIST into
   * a vector of XPath match patterns.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A whitespace delimited list of simple match patterns.
   *
   * @return A Vector of XPath objects.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if one of the match pattern
   * strings contains a syntax error.
   */
  Vector processSIMPLEPATTERNLIST(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    try
    {
      StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
      int nPatterns = tokenizer.countTokens();
      Vector patterns = new Vector(nPatterns);

      for (int i = 0; i < nPatterns; i++)
      {
        XPath pattern =
          handler.createMatchPatternXPath(tokenizer.nextToken(), owner);

        patterns.addElement(pattern);
      }

      return patterns;
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Process an attribute string of type T_STRINGLIST into
   * a vector of XPath match patterns.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value a whitespace delimited list of string values.
   *
   * @return A StringVector of the tokenized strings.
   */
  StringVector processSTRINGLIST(StylesheetHandler handler, String uri,
                                 String name, String rawName, String value)
  {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nStrings = tokenizer.countTokens();
    StringVector strings = new StringVector(nStrings);

    for (int i = 0; i < nStrings; i++)
    {
      strings.addElement(tokenizer.nextToken());
    }

    return strings;
  }

  /**
   * Process an attribute string of type T_URLLIST into
   * a vector of prefixes that may be resolved to URLs.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A list of whitespace delimited prefixes.
   *
   * @return A vector of strings that may be resolved to URLs.
   *
   * @throws org.xml.sax.SAXException if one of the prefixes can not be resolved.
   */
  StringVector processPREFIX_URLLIST(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {

    StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
    int nStrings = tokenizer.countTokens();
    StringVector strings = new StringVector(nStrings);

    for (int i = 0; i < nStrings; i++)
    {
      String prefix = tokenizer.nextToken();
      String url = handler.getNamespaceForPrefix(prefix);

      if (url != null)
        strings.addElement(url);
      else
        throw new org.xml.sax.SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, new Object[] {prefix}));
    
    }

    return strings;
  }

  /**
    * Process an attribute string of type T_PREFIXLIST into
    * a vector of prefixes that may be resolved to URLs.
    *
    * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
    * @param uri The Namespace URI, or an empty string.
    * @param name The local name (without prefix), or empty string if not namespace processing.
    * @param rawName The qualified name (with prefix).
    * @param value A list of whitespace delimited prefixes.
    *
    * @return A vector of strings that may be resolved to URLs.
    *
    * @throws org.xml.sax.SAXException if one of the prefixes can not be resolved.
    */
   StringVector processPREFIX_LIST(
           StylesheetHandler handler, String uri, String name, 
           String rawName, String value) throws org.xml.sax.SAXException
   {
    
     StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
     int nStrings = tokenizer.countTokens();
     StringVector strings = new StringVector(nStrings);

     for (int i = 0; i < nStrings; i++)
     {
       String prefix = tokenizer.nextToken();
       String url = handler.getNamespaceForPrefix(prefix);
       if (prefix.equals(Constants.ATTRVAL_DEFAULT_PREFIX) || url != null)
         strings.addElement(prefix);
       else
         throw new org.xml.sax.SAXException(
              XSLMessages.createMessage(
                   XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, 
                   new Object[] {prefix}));
    
     }

     return strings;
   }


  /**
   * Process an attribute string of type T_URL into
   * a URL value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value non-null string that conforms to the URL syntax.
   *
   * @return The non-absolutized URL argument, in other words, the value argument.  If this 
   *         attribute supports AVT, an AVT is returned.
   *
   * @throws org.xml.sax.SAXException if the URL does not conform to the URL syntax.
   */
  Object processURL(
          StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    if (getSupportsAVT()) {
	    try
	    {
	      AVT avt = new AVT(handler, uri, name, rawName, value, owner);
	
		  // If an AVT wasn't used, validate the value
		 // if (avt.getSimpleString() != null) {
			   // TODO: syntax check URL value.
			    // return SystemIDResolver.getAbsoluteURI(value, 
			    //                                         handler.getBaseIdentifier());
		  //}	
	      return avt;
	    }
	    catch (TransformerException te)
	    {
	      throw new org.xml.sax.SAXException(te);
	    }  		
     } else {
    // TODO: syntax check URL value.
    // return SystemIDResolver.getAbsoluteURI(value, 
    //                                         handler.getBaseIdentifier());
     	
	    return value;
    }
  }

  /**
   * Process an attribute string of type T_YESNO into
   * a Boolean value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value A string that should be "yes" or "no".
   *
   * @return Boolean object representation of the value.
   *
   * @throws org.xml.sax.SAXException
   */
  private Boolean processYESNO(
          StylesheetHandler handler, String uri, String name, String rawName, String value)
            throws org.xml.sax.SAXException
  {

    // Is this already checked somewhere else?  -sb
    if (!(value.equals("yes") || value.equals("no")))
    {
      handleError(handler, XSLTErrorResources.INVALID_BOOLEAN, new Object[] {name,value}, null);
      return null;
   }
 
     return new Boolean(value.equals("yes") ? true : false);
  }

  /**
   * Process an attribute value.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param name The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param value The unprocessed string value of the attribute.
   *
   * @return The processed Object representation of the attribute.
   *
   * @throws org.xml.sax.SAXException if the attribute value can not be processed.
   */
  Object processValue(
          StylesheetHandler handler, String uri, String name, String rawName, String value,
          ElemTemplateElement owner)
            throws org.xml.sax.SAXException
  {

    int type = getType();
    Object processedValue = null;

    switch (type)
    {
    case T_AVT :
      processedValue = processAVT(handler, uri, name, rawName, value, owner);
      break;
    case T_CDATA :
      processedValue = processCDATA(handler, uri, name, rawName, value, owner);
      break;
    case T_CHAR :
      processedValue = processCHAR(handler, uri, name, rawName, value, owner);
      break;
    case T_ENUM :
      processedValue = processENUM(handler, uri, name, rawName, value, owner);
      break;
    case T_EXPR :
      processedValue = processEXPR(handler, uri, name, rawName, value, owner);
      break;
    case T_NMTOKEN :
      processedValue = processNMTOKEN(handler, uri, name, rawName, value, owner);
      break;
    case T_PATTERN :
      processedValue = processPATTERN(handler, uri, name, rawName, value, owner);
      break;
    case T_NUMBER :
      processedValue = processNUMBER(handler, uri, name, rawName, value, owner);
      break;
    case T_QNAME :
      processedValue = processQNAME(handler, uri, name, rawName, value, owner);
      break;
    case T_QNAMES :
      processedValue = processQNAMES(handler, uri, name, rawName, value);
      break;
	case T_QNAMES_RESOLVE_NULL:
      processedValue = processQNAMESRNU(handler, uri, name, rawName, value);
      break;
    case T_SIMPLEPATTERNLIST :
      processedValue = processSIMPLEPATTERNLIST(handler, uri, name, rawName,
                                                value, owner);
      break;
    case T_URL :
      processedValue = processURL(handler, uri, name, rawName, value, owner);
      break;
    case T_YESNO :
      processedValue = processYESNO(handler, uri, name, rawName, value);
      break;
    case T_STRINGLIST :
      processedValue = processSTRINGLIST(handler, uri, name, rawName, value);
      break;
    case T_PREFIX_URLLIST :
      processedValue = processPREFIX_URLLIST(handler, uri, name, rawName,
                                             value);
      break;
    case T_ENUM_OR_PQNAME :
    	processedValue = processENUM_OR_PQNAME(handler, uri, name, rawName, value, owner);
    	break;
    case T_NCNAME :
        processedValue = processNCNAME(handler, uri, name, rawName, value, owner);
        break;
    case T_AVT_QNAME :
        processedValue = processAVT_QNAME(handler, uri, name, rawName, value, owner);
        break;
    case T_PREFIXLIST :
      processedValue = processPREFIX_LIST(handler, uri, name, rawName,
                                             value);
      break;

    default :
    }

    return processedValue;
  }

  /**
   * Set the default value of an attribute.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param elem The object on which the property will be set.
   *
   * @throws org.xml.sax.SAXException wraps an invocation exception if the
   * setter method can not be invoked on the object.
   */
  void setDefAttrValue(StylesheetHandler handler, ElemTemplateElement elem)
          throws org.xml.sax.SAXException
  {
    setAttrValue(handler, this.getNamespace(), this.getName(),
                 this.getName(), this.getDefault(), elem);
  }

  /**
   * Get the primative type for the class, if there
   * is one.  If the class is a Double, for instance,
   * this will return double.class.  If the class is not one
   * of the 9 primative types, it will return the same
   * class that was passed in.
   *
   * @param obj The object which will be resolved to a primative class object if possible.
   *
   * @return The most primative class representation possible for the object, never null.
   */
  private Class getPrimativeClass(Object obj)
  {

    if (obj instanceof XPath)
      return XPath.class;

    Class cl = obj.getClass();

    if (cl == Double.class)
    {
      cl = double.class;
    }

    if (cl == Float.class)
    {
      cl = float.class;
    }
    else if (cl == Boolean.class)
    {
      cl = boolean.class;
    }
    else if (cl == Byte.class)
    {
      cl = byte.class;
    }
    else if (cl == Character.class)
    {
      cl = char.class;
    }
    else if (cl == Short.class)
    {
      cl = short.class;
    }
    else if (cl == Integer.class)
    {
      cl = int.class;
    }
    else if (cl == Long.class)
    {
      cl = long.class;
    }

    return cl;
  }
  
  /**
   * StringBuffer containing comma delimited list of valid values for ENUM type.
   * Used to build error message.
   */
  private StringBuffer getListOfEnums() 
  {
     StringBuffer enumNamesList = new StringBuffer();            
     String [] enumValues = this.getEnumNames();

     for (int i = 0; i < enumValues.length; i++)
     {
        if (i > 0)
        {
           enumNamesList.append(' ');
        }
        enumNamesList.append(enumValues[i]);
    }        
    return enumNamesList;
  }

  /**
   * Set a value on an attribute.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param attrUri The Namespace URI of the attribute, or an empty string.
   * @param attrLocalName The local name (without prefix), or empty string if not namespace processing.
   * @param attrRawName The raw name of the attribute, including possible prefix.
   * @param attrValue The attribute's value.
   * @param elem The object that should contain a property that represents the attribute.
   *
   * @throws org.xml.sax.SAXException
   */
  boolean setAttrValue(
          StylesheetHandler handler, String attrUri, String attrLocalName, 
          String attrRawName, String attrValue, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {
    if(attrRawName.equals("xmlns") || attrRawName.startsWith("xmlns:"))
      return true;
      
    String setterString = getSetterMethodName();

    // If this is null, then it is a foreign namespace and we 
    // do not process it.
    if (null != setterString)
    {
      try
      {
        Method meth;
        Object[] args;

        if(setterString.equals(S_FOREIGNATTR_SETTER))
        {
          // workaround for possible crimson bug
          if( attrUri==null) attrUri="";
          // First try to match with the primative value.
          Class sclass = attrUri.getClass();
          Class[] argTypes = new Class[]{ sclass, sclass,
                                      sclass, sclass };
  
          meth = elem.getClass().getMethod(setterString, argTypes);
  
          args = new Object[]{ attrUri, attrLocalName,
                                      attrRawName, attrValue };
        }
        else
        {
          Object value = processValue(handler, attrUri, attrLocalName,
                                      attrRawName, attrValue, elem);
          // If a warning was issued because the value for this attribute was
          // invalid, then the value will be null.  Just return
          if (null == value) return false;
                                      
          // First try to match with the primative value.
          Class[] argTypes = new Class[]{ getPrimativeClass(value) };
  
          try
          {
            meth = elem.getClass().getMethod(setterString, argTypes);
          }
          catch (NoSuchMethodException nsme)
          {
            Class cl = ((Object) value).getClass();
  
            // If this doesn't work, try it with the non-primative value;
            argTypes[0] = cl;
            meth = elem.getClass().getMethod(setterString, argTypes);
          }
  
          args = new Object[]{ value };
        }

        meth.invoke(elem, args);
      }
      catch (NoSuchMethodException nsme)
      {
        if (!setterString.equals(S_FOREIGNATTR_SETTER)) 
        {
          handler.error(XSLTErrorResources.ER_FAILED_CALLING_METHOD, new Object[]{setterString}, nsme);//"Failed calling " + setterString + " method!", nsme);
          return false;
        }
      }
      catch (IllegalAccessException iae)
      {
        handler.error(XSLTErrorResources.ER_FAILED_CALLING_METHOD, new Object[]{setterString}, iae);//"Failed calling " + setterString + " method!", iae);
        return false;
      }
      catch (InvocationTargetException nsme)
      {
        handleError(handler, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE,
            new Object[]{ Constants.ATTRNAME_NAME, getName()}, nsme);
        return false;
      }
    }
    
    return true;
  }
  
  private void handleError(StylesheetHandler handler, String msg, Object [] args, Exception exc) throws org.xml.sax.SAXException
  {
    switch (getErrorType()) 
    {
        case (FATAL):
        case (ERROR):
                handler.error(msg, args, exc);          
                break;
        case (WARNING):
                handler.warn(msg, args);       
        default: break;
    }
  }
}
