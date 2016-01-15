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
 * $Id: OutputProperties.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.OutputPropertyUtils;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.QName;

/**
 * This class provides information from xsl:output elements. It is mainly
 * a wrapper for {@link java.util.Properties}, but can not extend that class
 * because it must be part of the {@link org.apache.xalan.templates.ElemTemplateElement}
 * heararchy.
 * <p>An OutputProperties list can contain another OutputProperties list as
 * its "defaults"; this second property list is searched if the property key
 * is not found in the original property list.</p>
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#output">xsl:output in XSLT Specification</a>
 *
 */
public class OutputProperties extends ElemTemplateElement
        implements Cloneable
{
    static final long serialVersionUID = -6975274363881785488L;
  /**
   * Creates an empty OutputProperties with no default values.
   */
  public OutputProperties()
  {
    this(org.apache.xml.serializer.Method.XML);
  }

  /**
   * Creates an empty OutputProperties with the specified defaults.
   *
   * @param   defaults   the defaults.
   */
  public OutputProperties(Properties defaults)
  {
    m_properties = new Properties(defaults);
  }

  /**
   * Creates an empty OutputProperties with the defaults specified by
   * a property file.  The method argument is used to construct a string of
   * the form output_[method].properties (for instance, output_html.properties).
   * The output_xml.properties file is always used as the base.
   * <p>At the moment, anything other than 'text', 'xml', and 'html', will
   * use the output_xml.properties file.</p>
   *
   * @param   method non-null reference to method name.
   */
  public OutputProperties(String method)
  {
    m_properties = new Properties(
        OutputPropertiesFactory.getDefaultMethodProperties(method));
  }

  /**
   * Clone this OutputProperties, including a clone of the wrapped Properties
   * reference.
   *
   * @return A new OutputProperties reference, mutation of which should not
   *         effect this object.
   */
  public Object clone()
  {

    try
    {
      OutputProperties cloned = (OutputProperties) super.clone();

      cloned.m_properties = (Properties) cloned.m_properties.clone();

      return cloned;
    }
    catch (CloneNotSupportedException e)
    {
      return null;
    }
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setProperty(QName key, String value)
  {
    setProperty(key.toNamespacedString(), value);
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setProperty(String key, String value)
  {
    if(key.equals(OutputKeys.METHOD))
    {
      setMethodDefaults(value);
    }
    
    if (key.startsWith(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL))
      key = OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL
         + key.substring(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN);
    
    m_properties.put(key, value);
  }

  /**
   * Searches for the property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list with the specified key value.
   */
  public String getProperty(QName key)
  {
    return m_properties.getProperty(key.toNamespacedString());
  }

  /**
   * Searches for the property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list with the specified key value.
   */
  public String getProperty(String key) 
  {
    if (key.startsWith(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL))
      key = OutputPropertiesFactory.S_BUILTIN_EXTENSIONS_UNIVERSAL 
        + key.substring(OutputPropertiesFactory.S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN);
    return m_properties.getProperty(key);
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setBooleanProperty(QName key, boolean value)
  {
    m_properties.put(key.toNamespacedString(), value ? "yes" : "no");
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setBooleanProperty(String key, boolean value)
  {
    m_properties.put(key, value ? "yes" : "no");
  }

  /**
   * Searches for the boolean property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>false</code> if the property is not found, or if the value is other
   * than "yes".
   *
   * @param   key   the property key.
   * @return  the value in this property list as a boolean value, or false
   * if null or not "yes".
   */
  public boolean getBooleanProperty(QName key)
  {
    return getBooleanProperty(key.toNamespacedString());
  }

  /**
   * Searches for the boolean property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>false</code> if the property is not found, or if the value is other
   * than "yes".
   *
   * @param   key   the property key.
   * @return  the value in this property list as a boolean value, or false
   * if null or not "yes".
   */
  public boolean getBooleanProperty(String key)
  {
    return OutputPropertyUtils.getBooleanProperty(key, m_properties);
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setIntProperty(QName key, int value)
  {
    setIntProperty(key.toNamespacedString(), value);
  }

  /**
   * Set an output property.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setIntProperty(String key, int value)
  {
    m_properties.put(key, Integer.toString(value));
  }

  /**
   * Searches for the int property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>false</code> if the property is not found, or if the value is other
   * than "yes".
   *
   * @param   key   the property key.
   * @return  the value in this property list as a int value, or false
   * if null or not a number.
   */
  public int getIntProperty(QName key)
  {
    return getIntProperty(key.toNamespacedString());
  }

  /**
   * Searches for the int property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>false</code> if the property is not found, or if the value is other
   * than "yes".
   *
   * @param   key   the property key.
   * @return  the value in this property list as a int value, or false
   * if null or not a number.
   */
  public int getIntProperty(String key)
  {
    return OutputPropertyUtils.getIntProperty(key, m_properties);
  }


  /**
   * Set an output property with a QName value.  The QName will be turned
   * into a string with the namespace in curly brackets.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setQNameProperty(QName key, QName value)
  {
    setQNameProperty(key.toNamespacedString(), value);
  }
  
  /**
   * Reset the default properties based on the method.
   *
   * @param method the method value.
   * @see javax.xml.transform.OutputKeys
   */
  public void setMethodDefaults(String method)
  {
        String defaultMethod = m_properties.getProperty(OutputKeys.METHOD);
 
        if((null == defaultMethod) || !defaultMethod.equals(method)
         // bjm - add the next condition as a hack
         // but it is because both output_xml.properties and
         // output_unknown.properties have the same method=xml
         // for their default. Otherwise we end up with
         // a ToUnknownStream wraping a ToXMLStream even
         // when the users says method="xml"
         //
         || defaultMethod.equals("xml")
         )
        {
            Properties savedProps = m_properties;
            Properties newDefaults = 
                OutputPropertiesFactory.getDefaultMethodProperties(method);
            m_properties = new Properties(newDefaults);
            copyFrom(savedProps, false);
        }
  }
  

  /**
   * Set an output property with a QName value.  The QName will be turned
   * into a string with the namespace in curly brackets.
   *
   * @param key the key to be placed into the property list.
   * @param value the value corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setQNameProperty(String key, QName value)
  {
    setProperty(key, value.toNamespacedString());
  }

  /**
   * Searches for the qname property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list as a QName value, or false
   * if null or not "yes".
   */
  public QName getQNameProperty(QName key)
  {
    return getQNameProperty(key.toNamespacedString());
  }

  /**
   * Searches for the qname property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list as a QName value, or false
   * if null or not "yes".
   */
  public QName getQNameProperty(String key)
  {
    return getQNameProperty(key, m_properties);
  }

  /**
   * Searches for the qname property with the specified key in the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @param props the list of properties to search in.
   * @return  the value in this property list as a QName value, or false
   * if null or not "yes".
   */
  public static QName getQNameProperty(String key, Properties props)
  {

    String s = props.getProperty(key);

    if (null != s)
      return QName.getQNameFromString(s);
    else
      return null;
  }

  /**
   * Set an output property with a QName list value.  The QNames will be turned
   * into strings with the namespace in curly brackets.
   *
   * @param key the key to be placed into the property list.
   * @param v non-null list of QNames corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setQNameProperties(QName key, Vector v)
  {
    setQNameProperties(key.toNamespacedString(), v);
  }

  /**
   * Set an output property with a QName list value.  The QNames will be turned
   * into strings with the namespace in curly brackets.
   *
   * @param key the key to be placed into the property list.
   * @param v non-null list of QNames corresponding to <tt>key</tt>.
   * @see javax.xml.transform.OutputKeys
   */
  public void setQNameProperties(String key, Vector v)
  {

    int s = v.size();

    // Just an initial guess at reasonable tuning parameters
    FastStringBuffer fsb = new FastStringBuffer(9,9);

    for (int i = 0; i < s; i++)
    {
      QName qname = (QName) v.elementAt(i);

      fsb.append(qname.toNamespacedString());
      // Don't append space after last value
      if (i < s-1) 
        fsb.append(' ');
    }

    m_properties.put(key, fsb.toString());
  }

  /**
   * Searches for the list of qname properties with the specified key in
   * the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list as a vector of QNames, or false
   * if null or not "yes".
   */
  public Vector getQNameProperties(QName key)
  {
    return getQNameProperties(key.toNamespacedString());
  }

  /**
   * Searches for the list of qname properties with the specified key in
   * the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value in this property list as a vector of QNames, or false
   * if null or not "yes".
   */
  public Vector getQNameProperties(String key)
  {
    return getQNameProperties(key, m_properties);
  }

  /**
   * Searches for the list of qname properties with the specified key in
   * the property list.
   * If the key is not found in this property list, the default property list,
   * and its defaults, recursively, are then checked. The method returns
   * <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @param props the list of properties to search in.
   * @return  the value in this property list as a vector of QNames, or false
   * if null or not "yes".
   */
  public static Vector getQNameProperties(String key, Properties props)
  {

    String s = props.getProperty(key);

    if (null != s)
    {
      Vector v = new Vector();
      int l = s.length();
      boolean inCurly = false;
      FastStringBuffer buf = new FastStringBuffer();

      // parse through string, breaking on whitespaces.  I do this instead 
      // of a tokenizer so I can track whitespace inside of curly brackets, 
      // which theoretically shouldn't happen if they contain legal URLs.
      for (int i = 0; i < l; i++)
      {
        char c = s.charAt(i);

        if (Character.isWhitespace(c))
        {
          if (!inCurly)
          {
            if (buf.length() > 0)
            {
              QName qname = QName.getQNameFromString(buf.toString());
              v.addElement(qname);
              buf.reset();
            }
            continue;
          }
        }
        else if ('{' == c)
          inCurly = true;
        else if ('}' == c)
          inCurly = false;

        buf.append(c);
      }

      if (buf.length() > 0)
      {
        QName qname = QName.getQNameFromString(buf.toString());
        v.addElement(qname);
        buf.reset();
      }

      return v;
    }
    else
      return null;
  }

  /**
   * This function is called to recompose all of the output format extended elements.
   *
   * @param root non-null reference to the stylesheet root object.
   */
  public void recompose(StylesheetRoot root)
    throws TransformerException
  {
    root.recomposeOutput(this);
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
   * Get the Properties object that this class wraps.
   *
   * @return non-null reference to Properties object.
   */
  public Properties getProperties()
  {
    return m_properties;
  }
  
  /**
   * Copy the keys and values from the source to this object.  This will
   * not copy the default values.  This is meant to be used by going from
   * a higher precedence object to a lower precedence object, so that if a
   * key already exists, this method will not reset it.
   *
   * @param src non-null reference to the source properties.
   */
  public void copyFrom(Properties src)
  {
    copyFrom(src, true);
  }

  /**
   * Copy the keys and values from the source to this object.  This will
   * not copy the default values.  This is meant to be used by going from
   * a higher precedence object to a lower precedence object, so that if a
   * key already exists, this method will not reset it.
   *
   * @param src non-null reference to the source properties.
   * @param shouldResetDefaults true if the defaults should be reset based on 
   *                            the method property.
   */
  public void copyFrom(Properties src, boolean shouldResetDefaults)
  {

    Enumeration keys = src.keys();

    while (keys.hasMoreElements())
    {
      String key = (String) keys.nextElement();
    
      if (!isLegalPropertyKey(key))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{key})); //"output property not recognized: "
      
      Object oldValue = m_properties.get(key);
      if (null == oldValue)
      {
        String val = (String) src.get(key);
        
        if(shouldResetDefaults && key.equals(OutputKeys.METHOD))
        {
          setMethodDefaults(val);
        }

        m_properties.put(key, val);
      }
      else if (key.equals(OutputKeys.CDATA_SECTION_ELEMENTS))
      {
        m_properties.put(key, (String) oldValue + " " + (String) src.get(key));
      }
    }
  }

  /**
   * Copy the keys and values from the source to this object.  This will
   * not copy the default values.  This is meant to be used by going from
   * a higher precedence object to a lower precedence object, so that if a
   * key already exists, this method will not reset it.
   *
   * @param opsrc non-null reference to an OutputProperties.
   */
  public void copyFrom(OutputProperties opsrc)
    throws TransformerException
  {
   // Bugzilla 6157: recover from xsl:output statements
    // checkDuplicates(opsrc);
    copyFrom(opsrc.getProperties());
  }

  /**
   * Report if the key given as an argument is a legal xsl:output key.
   *
   * @param key non-null reference to key name.
   *
   * @return true if key is legal.
   */
  public static boolean isLegalPropertyKey(String key)
  {

    return (key.equals(OutputKeys.CDATA_SECTION_ELEMENTS)
            || key.equals(OutputKeys.DOCTYPE_PUBLIC)
            || key.equals(OutputKeys.DOCTYPE_SYSTEM)
            || key.equals(OutputKeys.ENCODING)
            || key.equals(OutputKeys.INDENT)
            || key.equals(OutputKeys.MEDIA_TYPE)
            || key.equals(OutputKeys.METHOD)
            || key.equals(OutputKeys.OMIT_XML_DECLARATION)
            || key.equals(OutputKeys.STANDALONE)
            || key.equals(OutputKeys.VERSION)
            || (key.length() > 0) 
                  && (key.charAt(0) == '{') 
                  && (key.lastIndexOf('{') == 0)
                  && (key.indexOf('}') > 0)
                  && (key.lastIndexOf('}') == key.indexOf('}')));
  }

  /** The output properties.
   *  @serial */
  private Properties m_properties = null;

    /**
     * Creates an empty OutputProperties with the defaults specified by
     * a property file.  The method argument is used to construct a string of
     * the form output_[method].properties (for instance, output_html.properties).
     * The output_xml.properties file is always used as the base.
     * <p>At the moment, anything other than 'text', 'xml', and 'html', will
     * use the output_xml.properties file.</p>
     *
     * @param   method non-null reference to method name.
     *
     * @return Properties object that holds the defaults for the given method.
     * 
     * @deprecated Use org.apache.xml.serializer.OuputPropertiesFactory.
     * getDefaultMethodProperties directly.
     */
    static public Properties getDefaultMethodProperties(String method)
    {
        return org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties(method);
    }
}
