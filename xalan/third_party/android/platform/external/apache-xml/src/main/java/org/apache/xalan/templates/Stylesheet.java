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
 * $Id: Stylesheet.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.SystemIDResolver;

/**
 * Represents a stylesheet element.
 * <p>All properties in this class have a fixed form of bean-style property
 * accessors for all properties that represent XSL attributes or elements.
 * These properties have setter method names accessed generically by the
 * processor, and so these names must be fixed according to the system
 * defined in the <a href="XSLTAttributeDef#getSetterMethodName">getSetterMethodName</a>
 * function.</p>
 * <p><pre>
 * <!ENTITY % top-level "
 *  (xsl:import*,
 *   (xsl:include
 *   | xsl:strip-space
 *   | xsl:preserve-space
 *   | xsl:output
 *   | xsl:key
 *   | xsl:decimal-format
 *   | xsl:attribute-set
 *   | xsl:variable
 *   | xsl:param
 *   | xsl:template
 *   | xsl:namespace-alias
 *   %non-xsl-top-level;)*)
 * ">
 *
 * <!ENTITY % top-level-atts '
 *   extension-element-prefixes CDATA #IMPLIED
 *   exclude-result-prefixes CDATA #IMPLIED
 *   id ID #IMPLIED
 *   version NMTOKEN #REQUIRED
 *   xmlns:xsl CDATA #FIXED "http://www.w3.org/1999/XSL/Transform"
 *   %space-att;
 * '>
 *
 * <!ELEMENT xsl:stylesheet %top-level;>
 * <!ATTLIST xsl:stylesheet %top-level-atts;>
 *
 * <!ELEMENT xsl:transform %top-level;>
 * <!ATTLIST xsl:transform %top-level-atts;>
 *
 * </p></pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Stylesheet-Structure">section-Stylesheet-Structure in XSLT Specification</a>
 */
public class Stylesheet extends ElemTemplateElement
        implements java.io.Serializable /* , Document */
{
    static final long serialVersionUID = 2085337282743043776L;

  /**
   * Constructor for a Stylesheet.
   * @param parent  The including or importing stylesheet.
   */
  public Stylesheet(Stylesheet parent)
  {

    if (null != parent)
    {
      m_stylesheetParent = parent;
      m_stylesheetRoot = parent.getStylesheetRoot();
    }
  }

  /**
   * Get the owning stylesheet.  This looks up the
   * inheritance chain until it calls getStylesheet
   * on a Stylesheet object, which will return itself.
   *
   * @return The owning stylesheet, itself.
   */
  public Stylesheet getStylesheet()
  {
    return this;
  }

  /**
   * Tell if this can be cast to a StylesheetComposed, meaning, you
   * can ask questions from getXXXComposed functions.
   *
   * @return False if this is not a StylesheetComposed
   */
  public boolean isAggregatedType()
  {
    return false;
  }

  /**
   * Tell if this is the root of the stylesheet tree.
   *
   * @return False is this is not the root of the stylesheet tree.
   */
  public boolean isRoot()
  {
    return false;
  }

  /**
   * Extension to be used when serializing to disk.
   */
  public static final String STYLESHEET_EXT = ".lxc";

  /**
   * Read the stylesheet from a serialization stream.
   *
   * @param stream Input stream to read from
   *
   * @throws IOException
   * @throws TransformerException
   */
  private void readObject(ObjectInputStream stream)
          throws IOException, TransformerException
  {

    // System.out.println("Reading Stylesheet");
    try
    {
      stream.defaultReadObject();
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new TransformerException(cnfe);
    }

    // System.out.println("Done reading Stylesheet");
  }

  /**
   * Write out the given output stream 
   *
   *
   * @param stream The output stream to write out
   *
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {

    // System.out.println("Writing Stylesheet");
    stream.defaultWriteObject();

    // System.out.println("Done writing Stylesheet");
  }

  //============== XSLT Properties =================

  /**
   * The "xmlns:xsl" property.
   * @serial
   */
  private String m_XmlnsXsl;

  /**
   * Set the "xmlns:xsl" property.
   * @see <a href="http://www.w3.org/TR/xslt#xslt-namespace">xslt-namespace in XSLT Specification</a>
   *
   * @param v The value to be set for the "xmlns:xsl" property.
   */
  public void setXmlnsXsl(String v)
  {
    m_XmlnsXsl = v;
  }

  /**
   * Get the "xmlns:xsl" property.
   * @see <a href="http://www.w3.org/TR/xslt#xslt-namespace">xslt-namespace in XSLT Specification</a>
   *
   * @return The value of the "xmlns:xsl" property.
   */
  public String getXmlnsXsl()
  {
    return m_XmlnsXsl;
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
   * @param v The value to be set for the "extension-element-prefixes" 
   * property: a vector of extension element URIs.
   */
  public void setExtensionElementPrefixes(StringVector v)
  {
    m_ExtensionElementURIs = v;
  }

  /**
   * Get and "extension-element-prefix" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param i Index of extension element URI in list 
   *
   * @return The extension element URI at the given index
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
   * @return Number of URIs in the list
   */
  public int getExtensionElementPrefixCount()
  {
    return (null != m_ExtensionElementURIs)
           ? m_ExtensionElementURIs.size() : 0;
  }

  /**
   * Find out if this contains a given "extension-element-prefix" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param uri URI of extension element to look for
   *
   * @return True if the given URI was found in the list 
   */
  public boolean containsExtensionElementURI(String uri)
  {

    if (null == m_ExtensionElementURIs)
      return false;

    return m_ExtensionElementURIs.contains(uri);
  }

  /**
   * The "exclude-result-prefixes" property.
   * @serial
   */
  private StringVector m_ExcludeResultPrefixs;

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
   * @param v A StringVector of prefixes to exclude 
   */
  public void setExcludeResultPrefixes(StringVector v)
  {
    m_ExcludeResultPrefixs = v;
  }

  /**
   * Get an "exclude-result-prefix" property.
   * The designation of a namespace as an excluded namespace is
   * effective within the subtree of the stylesheet rooted at
   * the element bearing the exclude-result-prefixes or
   * xsl:exclude-result-prefixes attribute; a subtree rooted
   * at an xsl:stylesheet element does not include any stylesheets
   * imported or included by children of that xsl:stylesheet element.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param i Index of prefix to get in list 
   *
   * @return Prefix to be excluded at the given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getExcludeResultPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_ExcludeResultPrefixs)
      throw new ArrayIndexOutOfBoundsException();

    return m_ExcludeResultPrefixs.elementAt(i);
  }

  /**
   * Get the number of "exclude-result-prefixes" Strings.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @return The number of prefix strings to be excluded. 
   */
  public int getExcludeResultPrefixCount()
  {
    return (null != m_ExcludeResultPrefixs)
           ? m_ExcludeResultPrefixs.size() : 0;
  }

  /**
   * Get whether or not the passed prefix is contained flagged by
   * the "exclude-result-prefixes" property.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param prefix non-null reference to prefix that might be excluded.
   * @param uri reference to namespace that prefix maps to
   *
   * @return true if the prefix should normally be excluded.>
   */
  public boolean containsExcludeResultPrefix(String prefix, String uri) 
  {

    if (null == m_ExcludeResultPrefixs || uri == null )
      return false;
    
    // This loop is ok here because this code only runs during
    // stylesheet compile time.
    for (int i =0; i< m_ExcludeResultPrefixs.size(); i++)
    {
      if (uri.equals(getNamespaceForPrefix(m_ExcludeResultPrefixs.elementAt(i))))
        return true;
    }
    
    return false;

  /*  if (prefix.length() == 0)
      prefix = Constants.ATTRVAL_DEFAULT_PREFIX;

    return m_ExcludeResultPrefixs.contains(prefix); */
  }

  /**
   * The "id" property.
   * @serial
   */
  private String m_Id;

  /**
   * Set the "id" property.
   * @see <a href="http://www.w3.org/TR/xslt#section-Embedding-Stylesheets">section-Embedding-Stylesheets in XSLT Specification</a>
   *
   * @param v Value for the "id" property.
   */
  public void setId(String v)
  {
    m_Id = v;
  }

  /**
   * Get the "id" property.
   * @see <a href="http://www.w3.org/TR/xslt#section-Embedding-Stylesheets">section-Embedding-Stylesheets in XSLT Specification</a>
   *
   * @return The value of the "id" property.
   */
  public String getId()
  {
    return m_Id;
  }

  /**
   * The "version" property.
   * @serial
   */
  private String m_Version;
  
  /**
   * Whether or not the stylesheet is in "Forward Compatibility Mode" 
   * @serial
   */
  private boolean m_isCompatibleMode = false;

  /**
   * Set the "version" property.
   * @see <a href="http://www.w3.org/TR/xslt#forwards">forwards in XSLT Specification</a>
   *
   * @param v Value for the "version" property.
   */
  public void setVersion(String v)
  {
    m_Version = v;
    m_isCompatibleMode = (Double.valueOf(v).doubleValue() > Constants.XSLTVERSUPPORTED);
  }

  /**
   * Get whether or not the stylesheet is in "Forward Compatibility Mode"
   * 
   * @return true if in forward compatible mode, false otherwise
   */
  public boolean getCompatibleMode()
  {
  	return m_isCompatibleMode;
  }

  /**
   * Get the "version" property.
   * @see <a href="http://www.w3.org/TR/xslt#forwards">forwards in XSLT Specification</a>
   *
   * @return The value of the "version" property.
   */
  public String getVersion()
  {
    return m_Version;
  }

  /**
   * The "xsl:import" list.
   * @serial
   */
  private Vector m_imports;

  /**
   * Add a stylesheet to the "import" list.
   * @see <a href="http://www.w3.org/TR/xslt#import">import in XSLT Specification</a>
   *
   * @param v Stylesheet to add to the import list
   */
  public void setImport(StylesheetComposed v)
  {

    if (null == m_imports)
      m_imports = new Vector();

    // I'm going to insert the elements in backwards order,
    // so I can walk them 0 to n.
    m_imports.addElement(v);
  }

  /**
   * Get a stylesheet from the "import" list.
   * @see <a href="http://www.w3.org/TR/xslt#import">import in XSLT Specification</a>
   *
   * @param i Index of the stylesheet to get
   *
   * @return The stylesheet at the given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public StylesheetComposed getImport(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_imports)
      throw new ArrayIndexOutOfBoundsException();

    return (StylesheetComposed) m_imports.elementAt(i);
  }

  /**
   * Get the number of imported stylesheets.
   * @see <a href="http://www.w3.org/TR/xslt#import">import in XSLT Specification</a>
   *
   * @return the number of imported stylesheets.
   */
  public int getImportCount()
  {
    return (null != m_imports) ? m_imports.size() : 0;
  }

  /**
   * The "xsl:include" properties.
   * @serial
   */
  private Vector m_includes;

  /**
   * Add a stylesheet to the "include" list.
   * @see <a href="http://www.w3.org/TR/xslt#include">include in XSLT Specification</a>
   *
   * @param v Stylesheet to add to the "include" list  
   */
  public void setInclude(Stylesheet v)
  {

    if (null == m_includes)
      m_includes = new Vector();

    m_includes.addElement(v);
  }

  /**
   * Get the stylesheet at the given in index in "include" list
   * @see <a href="http://www.w3.org/TR/xslt#include">include in XSLT Specification</a>
   *
   * @param i Index of stylesheet to get
   *
   * @return Stylesheet at the given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public Stylesheet getInclude(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_includes)
      throw new ArrayIndexOutOfBoundsException();

    return (Stylesheet) m_includes.elementAt(i);
  }

  /**
   * Get the number of included stylesheets.
   * @see <a href="http://www.w3.org/TR/xslt#import">import in XSLT Specification</a>
   *
   * @return the number of included stylesheets.
   */
  public int getIncludeCount()
  {
    return (null != m_includes) ? m_includes.size() : 0;
  }

  /**
   * Table of tables of element decimal-format.
   * @see DecimalFormatProperties
   * @serial
   */
  Stack m_DecimalFormatDeclarations;

  /**
   * Process the xsl:decimal-format element.
   *
   * @param edf Decimal-format element to push into stack  
   */
  public void setDecimalFormat(DecimalFormatProperties edf)
  {

    if (null == m_DecimalFormatDeclarations)
      m_DecimalFormatDeclarations = new Stack();

    // Elements are pushed in by order of importance
    // so that when recomposed, they get overiden properly.
    m_DecimalFormatDeclarations.push(edf);
  }

  /**
   * Get an "xsl:decimal-format" property.
   * 
   * @see DecimalFormatProperties
   * @see <a href="http://www.w3.org/TR/xslt#format-number">format-number in XSLT Specification</a>
   *
   * @param name The qualified name of the decimal format property.
   * @return null if not found, otherwise a DecimalFormatProperties
   * object, from which you can get a DecimalFormatSymbols object.
   */
  public DecimalFormatProperties getDecimalFormat(QName name)
  {

    if (null == m_DecimalFormatDeclarations)
      return null;

    int n = getDecimalFormatCount();

    for (int i = (n - 1); i >= 0; i++)
    {
      DecimalFormatProperties dfp = getDecimalFormat(i);

      if (dfp.getName().equals(name))
        return dfp;
    }

    return null;
  }

  /**
   * Get an "xsl:decimal-format" property.
   * @see <a href="http://www.w3.org/TR/xslt#format-number">format-number in XSLT Specification</a>
   * @see DecimalFormatProperties
   *
   * @param i Index of decimal-format property in stack
   *
   * @return The decimal-format property at the given index 
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public DecimalFormatProperties getDecimalFormat(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_DecimalFormatDeclarations)
      throw new ArrayIndexOutOfBoundsException();

    return (DecimalFormatProperties) m_DecimalFormatDeclarations.elementAt(i);
  }

  /**
   * Get the number of xsl:decimal-format declarations.
   * @see DecimalFormatProperties
   *
   * @return the number of xsl:decimal-format declarations.
   */
  public int getDecimalFormatCount()
  {
    return (null != m_DecimalFormatDeclarations)
           ? m_DecimalFormatDeclarations.size() : 0;
  }

  /**
   * The "xsl:strip-space" properties,
   * A lookup table of all space stripping elements.
   * @serial
   */
  private Vector m_whitespaceStrippingElements;

  /**
   * Set the "xsl:strip-space" properties.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param wsi WhiteSpaceInfo element to add to list 
   */
  public void setStripSpaces(WhiteSpaceInfo wsi)
  {

    if (null == m_whitespaceStrippingElements)
    {
      m_whitespaceStrippingElements = new Vector();
    }

    m_whitespaceStrippingElements.addElement(wsi);
  }

  /**
   * Get an "xsl:strip-space" property.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param i Index of WhiteSpaceInfo to get
   *
   * @return WhiteSpaceInfo at given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public WhiteSpaceInfo getStripSpace(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_whitespaceStrippingElements)
      throw new ArrayIndexOutOfBoundsException();

    return (WhiteSpaceInfo) m_whitespaceStrippingElements.elementAt(i);
  }

  /**
   * Get the number of "xsl:strip-space" properties.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @return the number of "xsl:strip-space" properties.
   */
  public int getStripSpaceCount()
  {
    return (null != m_whitespaceStrippingElements)
           ? m_whitespaceStrippingElements.size() : 0;
  }

  /**
   * The "xsl:preserve-space" property,
   * A lookup table of all space preserving elements.
   * @serial
   */
  private Vector m_whitespacePreservingElements;

  /**
   * Set the "xsl:preserve-space" property.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param wsi WhiteSpaceInfo element to add to list
   */
  public void setPreserveSpaces(WhiteSpaceInfo wsi)
  {

    if (null == m_whitespacePreservingElements)
    {
      m_whitespacePreservingElements = new Vector();
    }

    m_whitespacePreservingElements.addElement(wsi);
  }

  /**
   * Get a "xsl:preserve-space" property.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param i Index of WhiteSpaceInfo to get
   *
   * @return WhiteSpaceInfo at the given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public WhiteSpaceInfo getPreserveSpace(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_whitespacePreservingElements)
      throw new ArrayIndexOutOfBoundsException();

    return (WhiteSpaceInfo) m_whitespacePreservingElements.elementAt(i);
  }

  /**
   * Get the number of "xsl:preserve-space" properties.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @return the number of "xsl:preserve-space" properties.
   */
  public int getPreserveSpaceCount()
  {
    return (null != m_whitespacePreservingElements)
           ? m_whitespacePreservingElements.size() : 0;
  }

  /**
   * The "xsl:output" properties.  This is a vector of OutputProperties objects.
   * @serial
   */
  private Vector m_output;

  /**
   * Set the "xsl:output" property.
   * @see <a href="http://www.w3.org/TR/xslt#output">output in XSLT Specification</a>
   *
   * @param v non-null reference to the OutputProperties object to be 
   *          added to the collection.
   */
  public void setOutput(OutputProperties v)
  {
    if (null == m_output)
    {
      m_output = new Vector();
    }

    m_output.addElement(v);
  }

  /**
   * Get an "xsl:output" property.
   * @see <a href="http://www.w3.org/TR/xslt#output">output in XSLT Specification</a>
   *
   * @param i Index of OutputFormatExtended to get
   *
   * @return non-null reference to an OutputProperties object.
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public OutputProperties getOutput(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_output)
      throw new ArrayIndexOutOfBoundsException();

    return (OutputProperties) m_output.elementAt(i);
  }

  /**
   * Get the number of "xsl:output" properties.
   * @see <a href="http://www.w3.org/TR/xslt#output">output in XSLT Specification</a>
   *
   * @return The number of OutputProperties objects contained in this stylesheet.
   */
  public int getOutputCount()
  {
    return (null != m_output)
           ? m_output.size() : 0;
  }

  /**
   * The "xsl:key" property.
   * @serial
   */
  private Vector m_keyDeclarations;

  /**
   * Set the "xsl:key" property.
   * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
   *
   * @param v KeyDeclaration element to add to the list of key declarations 
   */
  public void setKey(KeyDeclaration v)
  {

    if (null == m_keyDeclarations)
      m_keyDeclarations = new Vector();

    m_keyDeclarations.addElement(v);
  }

  /**
   * Get an "xsl:key" property.
   * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
   *
   * @param i Index of KeyDeclaration element to get
   *
   * @return KeyDeclaration element at given index in list 
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public KeyDeclaration getKey(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_keyDeclarations)
      throw new ArrayIndexOutOfBoundsException();

    return (KeyDeclaration) m_keyDeclarations.elementAt(i);
  }

  /**
   * Get the number of "xsl:key" properties.
   * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
   *
   * @return the number of "xsl:key" properties.
   */
  public int getKeyCount()
  {
    return (null != m_keyDeclarations) ? m_keyDeclarations.size() : 0;
  }

  /**
   * The "xsl:attribute-set" property.
   * @serial
   */
  private Vector m_attributeSets;

  /**
   * Set the "xsl:attribute-set" property.
   * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
   *
   * @param attrSet ElemAttributeSet to add to the list of attribute sets
   */
  public void setAttributeSet(ElemAttributeSet attrSet)
  {

    if (null == m_attributeSets)
    {
      m_attributeSets = new Vector();
    }

    m_attributeSets.addElement(attrSet);
  }

  /**
   * Get an "xsl:attribute-set" property.
   * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
   *
   * @param i Index of ElemAttributeSet to get in list
   *
   * @return ElemAttributeSet at the given index
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public ElemAttributeSet getAttributeSet(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_attributeSets)
      throw new ArrayIndexOutOfBoundsException();

    return (ElemAttributeSet) m_attributeSets.elementAt(i);
  }

  /**
   * Get the number of "xsl:attribute-set" properties.
   * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
   *
   * @return the number of "xsl:attribute-set" properties.
   */
  public int getAttributeSetCount()
  {
    return (null != m_attributeSets) ? m_attributeSets.size() : 0;
  }

  /**
   * The "xsl:variable" and "xsl:param" properties.
   * @serial
   */
  private Vector m_topLevelVariables;

  /**
   * Set the "xsl:variable" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param v ElemVariable object to add to list of top level variables
   */
  public void setVariable(ElemVariable v)
  {

    if (null == m_topLevelVariables)
      m_topLevelVariables = new Vector();

    m_topLevelVariables.addElement(v);
  }
  
  /**
   * Get an "xsl:variable" or "xsl:param" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param qname non-null reference to the qualified name of the variable.
   *
   * @return The ElemVariable with the given name in the list or null
   */
  public ElemVariable getVariableOrParam(QName qname)
  {

    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();

      for (int i = 0; i < n; i++)
      {
        ElemVariable var = (ElemVariable) getVariableOrParam(i);

        if (var.getName().equals(qname))
          return var;
      }
    }

    return null;
  }


  /**
   * Get an "xsl:variable" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param qname Qualified name of the xsl:variable to get 
   *
   * @return reference to the variable named by qname, or null if not found.
   */
  public ElemVariable getVariable(QName qname)
  {

    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();

      for (int i = 0; i < n; i++)
      {
        ElemVariable var = getVariableOrParam(i);
        if((var.getXSLToken() == Constants.ELEMNAME_VARIABLE) &&
           (var.getName().equals(qname)))
          return var;
      }
    }

    return null;
  }

  /**
   * Get an "xsl:variable" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param i Index of variable to get in the list
   *
   * @return ElemVariable at the given index in the list 
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public ElemVariable getVariableOrParam(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_topLevelVariables)
      throw new ArrayIndexOutOfBoundsException();

    return (ElemVariable) m_topLevelVariables.elementAt(i);
  }

  /**
   * Get the number of "xsl:variable" properties.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @return the number of "xsl:variable" properties.
   */
  public int getVariableOrParamCount()
  {
    return (null != m_topLevelVariables) ? m_topLevelVariables.size() : 0;
  }

  /**
   * Set an "xsl:param" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param v A non-null ElemParam reference.
   */
  public void setParam(ElemParam v)
  {
    setVariable(v);
  }

  /**
   * Get an "xsl:param" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param qname non-null reference to qualified name of the parameter.
   *
   * @return ElemParam with the given name in the list or null
   */
  public ElemParam getParam(QName qname)
  {

    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();

      for (int i = 0; i < n; i++)
      {
        ElemVariable var = getVariableOrParam(i);
        if((var.getXSLToken() == Constants.ELEMNAME_PARAMVARIABLE) &&
           (var.getName().equals(qname)))
          return (ElemParam)var;
      }
    }

    return null;
  }

  /**
   * The "xsl:template" properties.
   * @serial
   */
  private Vector m_templates;

  /**
   * Set an "xsl:template" property.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @param v ElemTemplate to add to list of templates
   */
  public void setTemplate(ElemTemplate v)
  {

    if (null == m_templates)
      m_templates = new Vector();

    m_templates.addElement(v);
    v.setStylesheet(this);
  }

  /**
   * Get an "xsl:template" property.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @param i Index of ElemTemplate in the list to get
   *
   * @return ElemTemplate at the given index in the list
   *
   * @throws TransformerException
   */
  public ElemTemplate getTemplate(int i) throws TransformerException
  {

    if (null == m_templates)
      throw new ArrayIndexOutOfBoundsException();

    return (ElemTemplate) m_templates.elementAt(i);
  }

  /**
   * Get the number of "xsl:template" properties.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @return the number of "xsl:template" properties.
   */
  public int getTemplateCount()
  {
    return (null != m_templates) ? m_templates.size() : 0;
  }

  /**
   * The "xsl:namespace-alias" properties.
   * @serial
   */
  private Vector m_prefix_aliases;

  /**
   * Set the "xsl:namespace-alias" property.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param na NamespaceAlias elemeent to add to the list
   */
  public void setNamespaceAlias(NamespaceAlias na)
  {

    if (m_prefix_aliases == null)
      m_prefix_aliases = new Vector();

    m_prefix_aliases.addElement(na);
  }

  /**
   * Get an "xsl:namespace-alias" property.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param i Index of NamespaceAlias element to get from the list 
   *
   * @return NamespaceAlias element at the given index in the list
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public NamespaceAlias getNamespaceAlias(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_prefix_aliases)
      throw new ArrayIndexOutOfBoundsException();

    return (NamespaceAlias) m_prefix_aliases.elementAt(i);
  }

  /**
   * Get the number of "xsl:namespace-alias" properties.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @return the number of "xsl:namespace-alias" properties.
   */
  public int getNamespaceAliasCount()
  {
    return (null != m_prefix_aliases) ? m_prefix_aliases.size() : 0;
  }

  /**
   * The "non-xsl-top-level" properties.
   * @serial
   */
  private Hashtable m_NonXslTopLevel;

  /**
   * Set found a non-xslt element.
   * @see <a href="http://www.w3.org/TR/xslt#stylesheet-element">stylesheet-element in XSLT Specification</a>
   *
   * @param name Qualified name of the element
   * @param obj The element object
   */
  public void setNonXslTopLevel(QName name, Object obj)
  {

    if (null == m_NonXslTopLevel)
      m_NonXslTopLevel = new Hashtable();

    m_NonXslTopLevel.put(name, obj);
  }

  /**
   * Get a non-xslt element.
   * @see <a href="http://www.w3.org/TR/xslt#stylesheet-element">stylesheet-element in XSLT Specification</a>
   *
   * @param name Qualified name of the element to get
   *
   * @return The object associate with the given name 
   */
  public Object getNonXslTopLevel(QName name)
  {
    return (null != m_NonXslTopLevel) ? m_NonXslTopLevel.get(name) : null;
  }

  // =========== End top-level XSLT properties ===========

  /**
   * The base URL of the XSL document.
   * @serial
   */
  private String m_href = null;

  /** The doctype-public element.
   *  @serial          */
  private String m_publicId;

  /** The doctype-system element.
   *  @serial          */
  private String m_systemId;

  /**
   * Get the base identifier with which this stylesheet is associated.
   *
   * @return the base identifier with which this stylesheet is associated.
   */
  public String getHref()
  {
    return m_href;
  }

  /**
   * Set the base identifier with which this stylesheet is associated.
   *
   * @param baseIdent the base identifier with which this stylesheet is associated.
   */
  public void setHref(String baseIdent)
  {
    m_href = baseIdent;
  }

  /**
   * Set the location information for this element.
   *
   * @param locator SourceLocator object with location information  
   */
  public void setLocaterInfo(SourceLocator locator)
  {

    if (null != locator)
    {
      m_publicId = locator.getPublicId();
      m_systemId = locator.getSystemId();

      if (null != m_systemId)
      {
        try
        {
          m_href = SystemIDResolver.getAbsoluteURI(m_systemId, null);
        }
        catch (TransformerException se)
        {

          // Ignore this for right now
        }
      }

      super.setLocaterInfo(locator);
    }
  }

  /**
   * The root of the stylesheet, where all the tables common
   * to all stylesheets are kept.
   * @serial
   */
  private StylesheetRoot m_stylesheetRoot;

  /**
   * Get the root of the stylesheet, where all the tables common
   * to all stylesheets are kept.
   *
   * @return the root of the stylesheet
   */
  public StylesheetRoot getStylesheetRoot()
  {
    return m_stylesheetRoot;
  }

  /**
   * Set the root of the stylesheet, where all the tables common
   * to all stylesheets are kept.
   *
   * @param v the root of the stylesheet
   */
  public void setStylesheetRoot(StylesheetRoot v)
  {
    m_stylesheetRoot = v;
  }

  /**
   * The parent of the stylesheet.  This will be null if this
   * is the root stylesheet.
   * @serial
   */
  private Stylesheet m_stylesheetParent;

  /**
   * Get the parent of the stylesheet.  This will be null if this
   * is the root stylesheet.
   *
   * @return the parent of the stylesheet.
   */
  public Stylesheet getStylesheetParent()
  {
    return m_stylesheetParent;
  }

  /**
   * Set the parent of the stylesheet.  This should be null if this
   * is the root stylesheet.
   *
   * @param v the parent of the stylesheet.
   */
  public void setStylesheetParent(Stylesheet v)
  {
    m_stylesheetParent = v;
  }

  /**
   * Get the owning aggregated stylesheet, or this
   * stylesheet if it is aggregated.
   *
   * @return the owning aggregated stylesheet or itself
   */
  public StylesheetComposed getStylesheetComposed()
  {

    Stylesheet sheet = this;

    while (!sheet.isAggregatedType())
    {
      sheet = sheet.getStylesheetParent();
    }

    return (StylesheetComposed) sheet;
  }

  /**
   * Get the type of the node.  We'll pretend we're a Document.
   *
   * @return the type of the node: document node.
   */
  public short getNodeType()
  {
    return DTM.DOCUMENT_NODE;
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
    return Constants.ELEMNAME_STYLESHEET;
  }

  /**
   * Return the node name.
   *
   * @return The node name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_STYLESHEET_STRING;
  }

  /**
   * Replace an "xsl:template" property.
   * This is a hook for CompilingStylesheetHandler, to allow
   * us to access a template, compile it, instantiate it,
   * and replace the original with the compiled instance.
   * ADDED 9/5/2000 to support compilation experiment
   *
   * @param v Compiled template to replace with
   * @param i Index of template to be replaced
   *
   * @throws TransformerException
   */
  public void replaceTemplate(ElemTemplate v, int i) throws TransformerException
  {

    if (null == m_templates)
      throw new ArrayIndexOutOfBoundsException();

    replaceChild(v, (ElemTemplateElement)m_templates.elementAt(i));
    m_templates.setElementAt(v, i);
    v.setStylesheet(this);
  }
  
    /**
     * Call the children visitors.
     * @param visitor The visitor whose appropriate method will be called.
     */
    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
    {
      int s = getImportCount();
      for (int j = 0; j < s; j++)
      {
      	getImport(j).callVisitors(visitor);
      }
   
      s = getIncludeCount();
      for (int j = 0; j < s; j++)
      {
      	getInclude(j).callVisitors(visitor);
      }

      s = getOutputCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getOutput(j));
      }

      // Next, add in the attribute-set elements

      s = getAttributeSetCount();
      for (int j = 0; j < s; j++)
      {
      	ElemAttributeSet attrSet = getAttributeSet(j);
        if (visitor.visitTopLevelInstruction(attrSet))
        {
          attrSet.callChildVisitors(visitor);
        }
      }
      // Now the decimal-formats

      s = getDecimalFormatCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getDecimalFormat(j));
      }

      // Now the keys

      s = getKeyCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getKey(j));
      }

      // And the namespace aliases

      s = getNamespaceAliasCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getNamespaceAlias(j));
      }

      // Next comes the templates

      s = getTemplateCount();
      for (int j = 0; j < s; j++)
      {
        try
        {
          ElemTemplate template = getTemplate(j);
          if (visitor.visitTopLevelInstruction(template))
          {
            template.callChildVisitors(visitor);
          }
        }
        catch (TransformerException te)
        {
          throw new org.apache.xml.utils.WrappedRuntimeException(te);
        }
      }

      // Then, the variables

      s = getVariableOrParamCount();
      for (int j = 0; j < s; j++)
      {
      	ElemVariable var = getVariableOrParam(j);
        if (visitor.visitTopLevelVariableOrParamDecl(var))
        {
          var.callChildVisitors(visitor);
        }
      }

      // And lastly the whitespace preserving and stripping elements

      s = getStripSpaceCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getStripSpace(j));
      }

      s = getPreserveSpaceCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getPreserveSpace(j));
      }
      
      if(null != m_NonXslTopLevel)
      {
      	java.util.Enumeration elements = m_NonXslTopLevel.elements();
      	while(elements.hasMoreElements())
      	{
      	  ElemTemplateElement elem = (ElemTemplateElement)elements.nextElement();
          if (visitor.visitTopLevelInstruction(elem))
          {
            elem.callChildVisitors(visitor);
          }
      		
      	}
      }
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
  	return visitor.visitStylesheet(this);
  }

  
}
