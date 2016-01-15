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
 * $Id: StylesheetRoot.java 476466 2006-11-18 08:22:31Z minchau $
 */
package org.apache.xalan.templates;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExtensionNamespacesManager;
import org.apache.xalan.processor.XSLTSchema;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.ExpandedNameTable;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;

/**
 * This class represents the root object of the stylesheet tree.
 * @xsl.usage general
 */
public class StylesheetRoot extends StylesheetComposed
        implements java.io.Serializable, Templates
{
    static final long serialVersionUID = 3875353123529147855L;
    
    /**
     * The flag for the setting of the optimize feature;
     */    
    private boolean m_optimizer = true;

    /**
     * The flag for the setting of the incremental feature;
     */    
    private boolean m_incremental = false;

    /**
     * The flag for the setting of the source_location feature;
     */  
    private boolean m_source_location = false;

    /**
     * State of the secure processing feature.
     */
    private boolean m_isSecureProcessing = false;

  /**
   * Uses an XSL stylesheet document.
   * @throws TransformerConfigurationException if the baseIdentifier can not be resolved to a URL.
   */
  public StylesheetRoot(ErrorListener errorListener) throws TransformerConfigurationException
  {

    super(null);

    setStylesheetRoot(this);

    try
    {
      m_selectDefault = new XPath("node()", this, this, XPath.SELECT, errorListener);

      initDefaultRule(errorListener);
    }
    catch (TransformerException se)
    {
      throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_INIT_DEFAULT_TEMPLATES, null), se); //"Can't init default templates!", se);
    }
  }

  /**
   * The schema used when creating this StylesheetRoot
   * @serial
   */
  private HashMap m_availElems;
  
  /**
   * Creates a StylesheetRoot and retains a pointer to the schema used to create this
   * StylesheetRoot.  The schema may be needed later for an element-available() function call.
   * 
   * @param schema The schema used to create this stylesheet
   * @throws TransformerConfigurationException if the baseIdentifier can not be resolved to a URL.
   */
  public StylesheetRoot(XSLTSchema schema, ErrorListener listener) throws TransformerConfigurationException
  {

    this(listener);
    m_availElems = schema.getElemsAvailable();
  }

  /**
   * Tell if this is the root of the stylesheet tree.
   *
   * @return True since this is the root of the stylesheet tree.
   */
  public boolean isRoot()
  {
    return true;
  }

  /**
   * Set the state of the secure processing feature.
   */
  public void setSecureProcessing(boolean flag)
  {
    m_isSecureProcessing = flag;
  }
  
  /**
   * Return the state of the secure processing feature.
   */
  public boolean isSecureProcessing()
  {
    return m_isSecureProcessing;
  }

  /**
   * Get the hashtable of available elements.
   *
   * @return table of available elements, keyed by qualified names, and with 
   * values of the same qualified names.
   */
  public HashMap getAvailableElements()
  {
    return m_availElems;
  }
  
  private transient ExtensionNamespacesManager m_extNsMgr = null;
  
  /**
   * Only instantiate an ExtensionNamespacesManager if one is called for
   * (i.e., if the stylesheet contains  extension functions and/or elements).
   */
  public ExtensionNamespacesManager getExtensionNamespacesManager()
  {
     if (m_extNsMgr == null)
       m_extNsMgr = new ExtensionNamespacesManager();
     return m_extNsMgr;
  }
  
  /**
   * Get the vector of extension namespaces. Used to provide
   * the extensions table access to a list of extension
   * namespaces encountered during composition of a stylesheet.
   */
  public Vector getExtensions()
  {
    return m_extNsMgr != null ? m_extNsMgr.getExtensions() : null;
  }  

/*
  public void runtimeInit(TransformerImpl transformer) throws TransformerException
  {
    System.out.println("StylesheetRoot.runtimeInit()");
      
  //    try{throw new Exception("StylesheetRoot.runtimeInit()");} catch(Exception e){e.printStackTrace();}

    }
*/  

  //============== Templates Interface ================

  /**
   * Create a new transformation context for this Templates object.
   *
   * @return A Transformer instance, never null.
   */
  public Transformer newTransformer()
  {
    return new TransformerImpl(this);
  }
  

  public Properties getDefaultOutputProps()
  {
    return m_outputProperties.getProperties();
  }
  
  /**
   * Get the static properties for xsl:output.  The object returned will
   * be a clone of the internal values, and thus it can be mutated
   * without mutating the Templates object, and then handed in to
   * the process method.
   *
   * <p>For XSLT, Attribute Value Templates attribute values will
   * be returned unexpanded (since there is no context at this point).</p>
   *
   * @return A Properties object, not null.
   */
  public Properties getOutputProperties()
  {    
    return (Properties)getDefaultOutputProps().clone();
  }

  //============== End Templates Interface ================

  /**
   * Recompose the values of all "composed" properties, meaning
   * properties that need to be combined or calculated from
   * the combination of imported and included stylesheets.  This
   * method determines the proper import precedence of all imported
   * stylesheets.  It then iterates through all of the elements and 
   * properties in the proper order and triggers the individual recompose
   * methods.
   *
   * @throws TransformerException
   */
  public void recompose() throws TransformerException
  {
    // Now we make a Vector that is going to hold all of the recomposable elements

      Vector recomposableElements = new Vector();

    // First, we build the global import tree.

    if (null == m_globalImportList)
    {

      Vector importList = new Vector();

      addImports(this, true, importList);            

      // Now we create an array and reverse the order of the importList vector.
      // We built the importList vector backwards so that we could use addElement
      // to append to the end of the vector instead of constantly pushing new
      // stylesheets onto the front of the vector and having to shift the rest
      // of the vector each time.

      m_globalImportList = new StylesheetComposed[importList.size()];

      for (int i =  0, j= importList.size() -1; i < importList.size(); i++)
      {  
        m_globalImportList[j] = (StylesheetComposed) importList.elementAt(i);
        // Build the global include list for this stylesheet.
        // This needs to be done ahead of the recomposeImports
        // because we need the info from the composed includes. 
        m_globalImportList[j].recomposeIncludes(m_globalImportList[j]);
        // Calculate the number of this import.    
        m_globalImportList[j--].recomposeImports();        
      }
    }    
    // Next, we walk the import tree and add all of the recomposable elements to the vector.
    int n = getGlobalImportCount();

    for (int i = 0; i < n; i++)
    {
      StylesheetComposed imported = getGlobalImport(i);
      imported.recompose(recomposableElements);
    }

    // We sort the elements into ascending order.

    QuickSort2(recomposableElements, 0, recomposableElements.size() - 1);

    // We set up the global variables that will hold the recomposed information.


    m_outputProperties = new OutputProperties(org.apache.xml.serializer.Method.UNKNOWN);
//  m_outputProperties = new OutputProperties(Method.XML);
    
    m_attrSets = new HashMap();
    m_decimalFormatSymbols = new Hashtable();
    m_keyDecls = new Vector();
    m_namespaceAliasComposed = new Hashtable();
    m_templateList = new TemplateList();
    m_variables = new Vector();

    // Now we sequence through the sorted elements, 
    // calling the recompose() function on each one.  This will call back into the
    // appropriate routine here to actually do the recomposition.
    // Note that we're going backwards, encountering the highest precedence items first.
    for (int i = recomposableElements.size() - 1; i >= 0; i--)
      ((ElemTemplateElement) recomposableElements.elementAt(i)).recompose(this);
    
/*
 * Backing out REE again, as it seems to cause some new failures
 * which need to be investigated. -is
 */      
    // This has to be done before the initialization of the compose state, because 
    // eleminateRedundentGlobals will add variables to the m_variables vector, which 
    // it then copied in the ComposeState constructor.
    
//    if(true && org.apache.xalan.processor.TransformerFactoryImpl.m_optimize)
//    {
//          RedundentExprEliminator ree = new RedundentExprEliminator();
//          callVisitors(ree);
//          ree.eleminateRedundentGlobals(this);
//    }
          
    initComposeState();

    // Need final composition of TemplateList.  This adds the wild cards onto the chains.
    m_templateList.compose(this);
    
    // Need to clear check for properties at the same import level.
    m_outputProperties.compose(this);
    m_outputProperties.endCompose(this);
    
    // Now call the compose() method on every element to give it a chance to adjust
    // based on composed values.
    
    n = getGlobalImportCount();

    for (int i = 0; i < n; i++)
    {
      StylesheetComposed imported = this.getGlobalImport(i);
      int includedCount = imported.getIncludeCountComposed();
      for (int j = -1; j < includedCount; j++)
      {
        Stylesheet included = imported.getIncludeComposed(j);
        composeTemplates(included);
      }
    }
    // Attempt to register any remaining unregistered extension namespaces.
    if (m_extNsMgr != null)
      m_extNsMgr.registerUnregisteredNamespaces();

    clearComposeState();
  }

  /**
   * Call the compose function for each ElemTemplateElement.
   *
   * @param templ non-null reference to template element that will have 
   * the composed method called on it, and will have it's children's composed 
   * methods called.
   */
  void composeTemplates(ElemTemplateElement templ) throws TransformerException
  {

    templ.compose(this);

    for (ElemTemplateElement child = templ.getFirstChildElem();
            child != null; child = child.getNextSiblingElem())
    {
      composeTemplates(child);
    }
    
    templ.endCompose(this);
  }

  /**
   * The combined list of imports.  The stylesheet with the highest
   * import precedence will be at element 0.  The one with the lowest
   * import precedence will be at element length - 1.
   * @serial
   */
  private StylesheetComposed[] m_globalImportList;

  /**
   * Add the imports in the given sheet to the working importList vector.
   * The will be added from highest import precedence to
   * least import precedence.  This is a post-order traversal of the
   * import tree as described in <a href="http://www.w3.org/TR/xslt.html#import">the
   * XSLT Recommendation</a>.
   * <p>For example, suppose</p>
   * <p>stylesheet A imports stylesheets B and C in that order;</p>
   * <p>stylesheet B imports stylesheet D;</p>
   * <p>stylesheet C imports stylesheet E.</p>
   * <p>Then the order of import precedence (highest first) is
   * A, C, E, B, D.</p>
   *
   * @param stylesheet Stylesheet to examine for imports.
   * @param addToList  <code>true</code> if this template should be added to the import list
   * @param importList The working import list.  Templates are added here in the reverse
   *        order of priority.  When we're all done, we'll reverse this to the correct
   *        priority in an array.
   */
  protected void addImports(Stylesheet stylesheet, boolean addToList, Vector importList)
  {

    // Get the direct imports of this sheet.

    int n = stylesheet.getImportCount();

    if (n > 0)
    {
      for (int i = 0; i < n; i++)
      {
        Stylesheet imported = stylesheet.getImport(i);

        addImports(imported, true, importList);
      }
    }

    n = stylesheet.getIncludeCount();

    if (n > 0)
    {
      for (int i = 0; i < n; i++)
      {
        Stylesheet included = stylesheet.getInclude(i);

        addImports(included, false, importList);
      }
    }

    if (addToList)
      importList.addElement(stylesheet);

  }

  /**
   * Get a stylesheet from the global import list. 
   * TODO: JKESS PROPOSES SPECIAL-CASE FOR NO IMPORT LIST, TO MATCH COUNT.
   * 
   * @param i Index of stylesheet to get from global import list 
   *
   * @return The stylesheet at the given index 
   */
  public StylesheetComposed getGlobalImport(int i)
  {
    return m_globalImportList[i];
  }

  /**
   * Get the total number of imports in the global import list.
   * 
   * @return The total number of imported stylesheets, including
   * the root stylesheet, thus the number will always be 1 or
   * greater.
   * TODO: JKESS PROPOSES SPECIAL-CASE FOR NO IMPORT LIST, TO MATCH DESCRIPTION.
   */
  public int getGlobalImportCount()
  {
          return (m_globalImportList!=null)
                        ? m_globalImportList.length 
                          : 1;
  }

  /**
   * Given a stylesheet, return the number of the stylesheet
   * in the global import list.
   * @param sheet The stylesheet which will be located in the
   * global import list.
   * @return The index into the global import list of the given stylesheet,
   * or -1 if it is not found (which should never happen).
   */
  public int getImportNumber(StylesheetComposed sheet)
  {

    if (this == sheet)
      return 0;

    int n = getGlobalImportCount();

    for (int i = 0; i < n; i++)
    {
      if (sheet == getGlobalImport(i))
        return i;
    }

    return -1;
  }

  /**
   * This will be set up with the default values, and then the values
   * will be set as stylesheets are encountered.
   * @serial
   */
  private OutputProperties m_outputProperties;

  /**
   * Recompose the output format object from the included elements.
   *
   * @param oprops non-null reference to xsl:output properties representation.
   */
  void recomposeOutput(OutputProperties oprops)
    throws TransformerException
  {
    
    m_outputProperties.copyFrom(oprops);
  }

  /**
   * Get the combined "xsl:output" property with the properties
   * combined from the included stylesheets.  If a xsl:output
   * is not declared in this stylesheet or an included stylesheet,
   * look in the imports.
   * Please note that this returns a reference to the OutputProperties
   * object, not a cloned object, like getOutputProperties does.
   * @see <a href="http://www.w3.org/TR/xslt#output">output in XSLT Specification</a>
   *
   * @return non-null reference to composed output properties object.
   */
  public OutputProperties getOutputComposed()
  {

    // System.out.println("getOutputComposed.getIndent: "+m_outputProperties.getIndent());
    // System.out.println("getOutputComposed.getIndenting: "+m_outputProperties.getIndenting());
    return m_outputProperties;
  }

  /** Flag indicating whether an output method has been set by the user.
   *  @serial           */
  private boolean m_outputMethodSet = false;

  /**
   * Find out if an output method has been set by the user.
   *
   * @return Value indicating whether an output method has been set by the user
   * @xsl.usage internal
   */
  public boolean isOutputMethodSet()
  {
    return m_outputMethodSet;
  }

  /**
   * Composed set of all included and imported attribute set properties.
   * Each entry is a vector of ElemAttributeSet objects.
   * @serial
   */
  private HashMap m_attrSets;

  /**
   * Recompose the attribute-set declarations.
   *
   * @param attrSet An attribute-set to add to the hashtable of attribute sets.
   */
  void recomposeAttributeSets(ElemAttributeSet attrSet)
  {
    ArrayList attrSetList = (ArrayList) m_attrSets.get(attrSet.getName());

    if (null == attrSetList)
    {
      attrSetList = new ArrayList();

      m_attrSets.put(attrSet.getName(), attrSetList);
    }

    attrSetList.add(attrSet);
  }

  /**
   * Get a list "xsl:attribute-set" properties that match the qname.
   * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
   *
   * @param name Qualified name of attribute set properties to get
   *
   * @return A vector of attribute sets matching the given name
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public ArrayList getAttributeSetComposed(QName name)
          throws ArrayIndexOutOfBoundsException
  {
    return (ArrayList) m_attrSets.get(name);
  }

  /**
   * Table of DecimalFormatSymbols, keyed by QName.
   * @serial
   */
  private Hashtable m_decimalFormatSymbols;

  /**
   * Recompose the decimal-format declarations.
   *
   * @param dfp A DecimalFormatProperties to add to the hashtable of decimal formats.
   */
  void recomposeDecimalFormats(DecimalFormatProperties dfp)
  {
    DecimalFormatSymbols oldDfs =
                  (DecimalFormatSymbols) m_decimalFormatSymbols.get(dfp.getName());
    if (null == oldDfs)
    {
      m_decimalFormatSymbols.put(dfp.getName(), dfp.getDecimalFormatSymbols());
    }
    else if (!dfp.getDecimalFormatSymbols().equals(oldDfs))
    {
      String themsg;
      if (dfp.getName().equals(new QName("")))
      {
        // "Only one default xsl:decimal-format declaration is allowed."
        themsg = XSLMessages.createWarning(
                          XSLTErrorResources.WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
                          new Object[0]);
      }
      else
      {
        // "xsl:decimal-format names must be unique. Name {0} has been duplicated."
        themsg = XSLMessages.createWarning(
                          XSLTErrorResources.WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
                          new Object[] {dfp.getName()});
      }

      error(themsg);   // Should we throw TransformerException instead?
    }

  }

  /**
   * Given a valid element decimal-format name, return the
   * decimalFormatSymbols with that name.
   * <p>It is an error to declare either the default decimal-format or
   * a decimal-format with a given name more than once (even with
   * different import precedence), unless it is declared every
   * time with the same value for all attributes (taking into
   * account any default values).</p>
   * <p>Which means, as far as I can tell, the decimal-format
   * properties are not additive.</p>
   *
   * @param name Qualified name of the decimal format to find 
   * @return DecimalFormatSymbols object matching the given name or
   * null if name is not found.
   */
  public DecimalFormatSymbols getDecimalFormatComposed(QName name)
  {
    return (DecimalFormatSymbols) m_decimalFormatSymbols.get(name);
  }

  /**
   * A list of all key declarations visible from this stylesheet and all
   * lesser stylesheets.
   * @serial
   */
  private Vector m_keyDecls;

  /**
   * Recompose the key declarations.
   *
   * @param keyDecl A KeyDeclaration to be added to the vector of key declarations.
   */
  void recomposeKeys(KeyDeclaration keyDecl)
  {
    m_keyDecls.addElement(keyDecl);
  }

  /**
   * Get the composed "xsl:key" properties.
   * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
   *
   * @return A vector of the composed "xsl:key" properties.
   */
  public Vector getKeysComposed()
  {
    return m_keyDecls;
  }

  /**
   * Composed set of all namespace aliases.
   * @serial
   */
  private Hashtable m_namespaceAliasComposed;

  /**
   * Recompose the namespace-alias declarations.
   *
   * @param nsAlias A NamespaceAlias object to add to the hashtable of namespace aliases.
   */
  void recomposeNamespaceAliases(NamespaceAlias nsAlias)
  {
    m_namespaceAliasComposed.put(nsAlias.getStylesheetNamespace(),
                                 nsAlias);
  }

  /**
   * Get the "xsl:namespace-alias" property.
   * Return the NamespaceAlias for a given namespace uri.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param uri non-null reference to namespace that is to be aliased.
   *
   * @return NamespaceAlias that matches uri, or null if no match.
   */
  public NamespaceAlias getNamespaceAliasComposed(String uri)
  {
    return (NamespaceAlias) ((null == m_namespaceAliasComposed) 
                    ? null : m_namespaceAliasComposed.get(uri));
  }

  /**
   * The "xsl:template" properties.
   * @serial
   */
  private TemplateList m_templateList;

  /**
   * Recompose the template declarations.
   *
   * @param template An ElemTemplate object to add to the template list.
   */
  void recomposeTemplates(ElemTemplate template)
  {
    m_templateList.setTemplate(template);
  }

  /**
   * Accessor method to retrieve the <code>TemplateList</code> associated with
   * this StylesheetRoot.
   * 
   * @return The composed <code>TemplateList</code>.
   */
  public final TemplateList getTemplateListComposed()
  {
    return m_templateList;
  }

  /**
   * Mutator method to set the <code>TemplateList</code> associated with this
   * StylesheetRoot.  This method should only be used by the compiler.  Normally,
   * the template list is built during the recompose process and should not be
   * altered by the user.
   * @param templateList The new <code>TemplateList</code> for this StylesheetRoot.
   */
  public final void setTemplateListComposed(TemplateList templateList)
  {
    m_templateList = templateList;
  }

  /**
   * Get an "xsl:template" property by node match. This looks in the imports as
   * well as this stylesheet.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @param xctxt non-null reference to XPath runtime execution context.
   * @param targetNode non-null reference of node that the template must match.
   * @param mode qualified name of the node, or null.
   * @param quietConflictWarnings true if conflict warnings should not be reported.
   *
   * @return reference to ElemTemplate that is the best match for targetNode, or 
   *         null if no match could be made.
   *
   * @throws TransformerException
   */
  public ElemTemplate getTemplateComposed(XPathContext xctxt,
                                          int targetNode,
                                          QName mode,
                                          boolean quietConflictWarnings,
                                          DTM dtm)
            throws TransformerException
  {
    return m_templateList.getTemplate(xctxt, targetNode, mode, 
                                      quietConflictWarnings,
                                      dtm);
  }
  
  /**
   * Get an "xsl:template" property by node match. This looks in the imports as
   * well as this stylesheet.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @param xctxt non-null reference to XPath runtime execution context.
   * @param targetNode non-null reference of node that the template must match.
   * @param mode qualified name of the node, or null.
   * @param maxImportLevel The maximum importCountComposed that we should consider or -1
   *        if we should consider all import levels.  This is used by apply-imports to
   *        access templates that have been overridden.
   * @param endImportLevel The count of composed imports
   * @param quietConflictWarnings true if conflict warnings should not be reported.
   *
   * @return reference to ElemTemplate that is the best match for targetNode, or 
   *         null if no match could be made.
   *
   * @throws TransformerException
   */
  public ElemTemplate getTemplateComposed(XPathContext xctxt,
                                          int targetNode,
                                          QName mode,
                                          int maxImportLevel, int endImportLevel,
                                          boolean quietConflictWarnings,
                                          DTM dtm)
            throws TransformerException
  {
    return m_templateList.getTemplate(xctxt, targetNode, mode, 
                                      maxImportLevel, endImportLevel,
                                      quietConflictWarnings,
                                      dtm);
  }

  /**
   * Get an "xsl:template" property. This looks in the imports as
   * well as this stylesheet.
   * @see <a href="http://www.w3.org/TR/xslt#section-Defining-Template-Rules">section-Defining-Template-Rules in XSLT Specification</a>
   *
   * @param qname non-null reference to qualified name of template.
   *
   * @return reference to named template, or null if not found.
   */
  public ElemTemplate getTemplateComposed(QName qname)
  {
    return m_templateList.getTemplate(qname);
  }
  
  /**
   * Composed set of all variables and params.
   * @serial
   */
  private Vector m_variables;

  /**
   * Recompose the top level variable and parameter declarations.
   *
   * @param elemVar A top level variable or parameter to be added to the Vector.
   */
  void recomposeVariables(ElemVariable elemVar)
  {
    // Don't overide higher priority variable        
    if (getVariableOrParamComposed(elemVar.getName()) == null)
    {
      elemVar.setIsTopLevel(true);        // Mark as a top-level variable or param
      elemVar.setIndex(m_variables.size());
      m_variables.addElement(elemVar);
    }
  }

  /**
   * Get an "xsl:variable" property.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @param qname Qualified name of variable or param
   *
   * @return The ElemVariable with the given qualified name
   */
  public ElemVariable getVariableOrParamComposed(QName qname)
  {
    if (null != m_variables)
    {
      int n = m_variables.size();

      for (int i = 0; i < n; i++)
      {
        ElemVariable var = (ElemVariable)m_variables.elementAt(i);
        if(var.getName().equals(qname))
          return var;
      }
    }

    return null;
  }

  /**
   * Get all global "xsl:variable" properties in scope for this stylesheet.
   * @see <a href="http://www.w3.org/TR/xslt#top-level-variables">top-level-variables in XSLT Specification</a>
   *
   * @return Vector of all variables and params in scope
   */
  public Vector getVariablesAndParamsComposed()
  {
    return m_variables;
  }

  /**
   * A list of properties that specify how to do space
   * stripping. This uses the same exact mechanism as Templates.
   * @serial
   */
  private TemplateList m_whiteSpaceInfoList;

  /**
   * Recompose the strip-space and preserve-space declarations.
   *
   * @param wsi A WhiteSpaceInfo element to add to the list of WhiteSpaceInfo elements.
   */
  void recomposeWhiteSpaceInfo(WhiteSpaceInfo wsi)
  {
    if (null == m_whiteSpaceInfoList)
      m_whiteSpaceInfoList = new TemplateList();

    m_whiteSpaceInfoList.setTemplate(wsi);
  }

  /**
   * Check to see if the caller should bother with check for
   * whitespace nodes.
   *
   * @return Whether the caller should bother with check for
   * whitespace nodes.
   */
  public boolean shouldCheckWhitespace()
  {
    return null != m_whiteSpaceInfoList;
  }

  /**
   * Get information about whether or not an element should strip whitespace.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @param support The XPath runtime state.
   * @param targetElement Element to check
   *
   * @return WhiteSpaceInfo for the given element
   *
   * @throws TransformerException
   */
  public WhiteSpaceInfo getWhiteSpaceInfo(
          XPathContext support, int targetElement, DTM dtm) throws TransformerException
  {

    if (null != m_whiteSpaceInfoList)
      return (WhiteSpaceInfo) m_whiteSpaceInfoList.getTemplate(support,
              targetElement, null, false, dtm);
    else
      return null;
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
          XPathContext support, int targetElement) throws TransformerException
  {
    if (null != m_whiteSpaceInfoList)
    {
      while(DTM.NULL != targetElement)
      {
        DTM dtm = support.getDTM(targetElement);
        WhiteSpaceInfo info = (WhiteSpaceInfo) m_whiteSpaceInfoList.getTemplate(support,
                targetElement, null, false, dtm);
        if(null != info)
          return info.getShouldStripSpace();
        
        int parent = dtm.getParent(targetElement);
        if(DTM.NULL != parent && DTM.ELEMENT_NODE == dtm.getNodeType(parent))
          targetElement = parent;
        else
          targetElement = DTM.NULL;
      }
    }
    return false;
  }
  
  /**
   * Get information about whether or not whitespace can be stripped.
   * @see <a href="http://www.w3.org/TR/xslt#strip">strip in XSLT Specification</a>
   *
   * @return true if the whitespace can be stripped.
   */
  public boolean canStripWhiteSpace()
  {
    return (null != m_whiteSpaceInfoList);
  }
  


  /**
   * The default template to use for text nodes if we don't find
   * anything else.  This is initialized in initDefaultRule().
   * @serial
   * @xsl.usage advanced
   */
  private ElemTemplate m_defaultTextRule;

  /**
   * Get the default template for text.
   *
   * @return the default template for text.
   * @xsl.usage advanced
   */
  public final ElemTemplate getDefaultTextRule()
  {
    return m_defaultTextRule;
  }

  /**
   * The default template to use if we don't find anything
   * else.  This is initialized in initDefaultRule().
   * @serial
   * @xsl.usage advanced
   */
  private ElemTemplate m_defaultRule;

  /**
   * Get the default template for elements.
   *
   * @return the default template for elements.
   * @xsl.usage advanced
   */
  public final ElemTemplate getDefaultRule()
  {
    return m_defaultRule;
  }

  /**
   * The default template to use for the root if we don't find
   * anything else.  This is initialized in initDefaultRule().
   * We kind of need this because the defaultRule isn't good
   * enough because it doesn't supply a document context.
   * For now, I default the root document element to "HTML".
   * Don't know if this is really a good idea or not.
   * I suspect it is not.
   * @serial
   * @xsl.usage advanced
   */
  private ElemTemplate m_defaultRootRule;

  /**
   * Get the default template for a root node.
   *
   * @return The default template for a root node.
   * @xsl.usage advanced
   */
  public final ElemTemplate getDefaultRootRule()
  {
    return m_defaultRootRule;
  }
  
  /**
   * The start rule to kick off the transformation.
   * @serial
   * @xsl.usage advanced
   */
  private ElemTemplate m_startRule;

  /**
   * Get the default template for a root node.
   *
   * @return The default template for a root node.
   * @xsl.usage advanced
   */
  public final ElemTemplate getStartRule()
  {
    return m_startRule;
  }


  /**
   * Used for default selection.
   * @serial
   */
  XPath m_selectDefault;

  /**
   * Create the default rule if needed.
   *
   * @throws TransformerException
   */
  private void initDefaultRule(ErrorListener errorListener) throws TransformerException
  {

    // Then manufacture a default
    m_defaultRule = new ElemTemplate();

    m_defaultRule.setStylesheet(this);

    XPath defMatch = new XPath("*", this, this, XPath.MATCH, errorListener);

    m_defaultRule.setMatch(defMatch);

    ElemApplyTemplates childrenElement = new ElemApplyTemplates();

    childrenElement.setIsDefaultTemplate(true);
    childrenElement.setSelect(m_selectDefault);
    m_defaultRule.appendChild(childrenElement);
    
    m_startRule = m_defaultRule;

    // -----------------------------
    m_defaultTextRule = new ElemTemplate();

    m_defaultTextRule.setStylesheet(this);

    defMatch = new XPath("text() | @*", this, this, XPath.MATCH, errorListener);

    m_defaultTextRule.setMatch(defMatch);

    ElemValueOf elemValueOf = new ElemValueOf();

    m_defaultTextRule.appendChild(elemValueOf);

    XPath selectPattern = new XPath(".", this, this, XPath.SELECT, errorListener);

    elemValueOf.setSelect(selectPattern);

    //--------------------------------
    m_defaultRootRule = new ElemTemplate();

    m_defaultRootRule.setStylesheet(this);

    defMatch = new XPath("/", this, this, XPath.MATCH, errorListener);

    m_defaultRootRule.setMatch(defMatch);

    childrenElement = new ElemApplyTemplates();

    childrenElement.setIsDefaultTemplate(true);
    m_defaultRootRule.appendChild(childrenElement);
    childrenElement.setSelect(m_selectDefault);
  }

  /**
   * This is a generic version of C.A.R Hoare's Quick Sort
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.  It was lifted from
   * the NodeSorter class but should probably be eliminated and replaced
   * with a call to Collections.sort when we migrate to Java2.<BR>
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param v       a vector of ElemTemplateElement elements 
   * @param lo0     left boundary of partition
   * @param hi0     right boundary of partition
   *
   */

  private void QuickSort2(Vector v, int lo0, int hi0)
    {
      int lo = lo0;
      int hi = hi0;

      if ( hi0 > lo0)
      {
        // Arbitrarily establishing partition element as the midpoint of
        // the array.
        ElemTemplateElement midNode = (ElemTemplateElement) v.elementAt( ( lo0 + hi0 ) / 2 );

        // loop through the array until indices cross
        while( lo <= hi )
        {
          // find the first element that is greater than or equal to
          // the partition element starting from the left Index.
          while( (lo < hi0) && (((ElemTemplateElement) v.elementAt(lo)).compareTo(midNode) < 0) )
          {
            ++lo;
          } // end while

          // find an element that is smaller than or equal to
          // the partition element starting from the right Index.
          while( (hi > lo0) && (((ElemTemplateElement) v.elementAt(hi)).compareTo(midNode) > 0) )          {
            --hi;
          }

          // if the indexes have not crossed, swap
          if( lo <= hi )
          {
            ElemTemplateElement node = (ElemTemplateElement) v.elementAt(lo);
            v.setElementAt(v.elementAt(hi), lo);
            v.setElementAt(node, hi);

            ++lo;
            --hi;
          }
        }

        // If the right index has not reached the left side of array
        // must now sort the left partition.
        if( lo0 < hi )
        {
          QuickSort2( v, lo0, hi );
        }

        // If the left index has not reached the right side of array
        // must now sort the right partition.
        if( lo < hi0 )
        {
          QuickSort2( v, lo, hi0 );
        }
      }
    } // end QuickSort2  */
    
    private transient ComposeState m_composeState;
    
    /**
     * Initialize a new ComposeState.
     */
    void initComposeState()
    {
      m_composeState = new ComposeState();
    }

    /**
     * Return class to track state global state during the compose() operation.
     * @return ComposeState reference, or null if endCompose has been called.
     */
    ComposeState getComposeState()
    {
      return m_composeState;
    }
    
    /**
     * Clear the compose state.
     */
    private void clearComposeState()
    {
      m_composeState = null;
    }

    private String m_extensionHandlerClass = 
        "org.apache.xalan.extensions.ExtensionHandlerExsltFunction";
    
    /**
     * This internal method allows the setting of the java class
     * to handle the extension function (if other than the default one).
     * 
     * @xsl.usage internal
     */
    public String setExtensionHandlerClass(String handlerClassName) {
        String oldvalue = m_extensionHandlerClass;
        m_extensionHandlerClass = handlerClassName;
        return oldvalue;
    } 
    /**
     * 
     * @xsl.usage internal
     */
    public String getExtensionHandlerClass() {
        return m_extensionHandlerClass;
    }
        
    /**
     * Class to track state global state during the compose() operation.
     */
    class ComposeState
    {
      ComposeState()
      {
        int size = m_variables.size();
        for (int i = 0; i < size; i++) 
        {
          ElemVariable ev = (ElemVariable)m_variables.elementAt(i);
          m_variableNames.addElement(ev.getName());
        }
        
      }
      
      private ExpandedNameTable m_ent = new ExpandedNameTable();
      
      /**
       * Given a qualified name, return an integer ID that can be 
       * quickly compared.
       *
       * @param qname a qualified name object, must not be null.
       *
       * @return the expanded-name id of the qualified name.
       */
      public int getQNameID(QName qname)
      {
        
        return m_ent.getExpandedTypeID(qname.getNamespace(), 
                                       qname.getLocalName(),
                                       // The type doesn't matter for our 
                                       // purposes. 
                                       org.apache.xml.dtm.DTM.ELEMENT_NODE);
      }
      
      /**
       * A Vector of the current params and QNames within the current template.
       * Set by ElemTemplate and used by ProcessorVariable.
       */
      private java.util.Vector m_variableNames = new java.util.Vector();
            
      /**
       * Add the name of a qualified name within the template.  The position in 
       * the vector is its ID.
       * @param qname A qualified name of a param or variable, should be non-null.
       * @return the index where the variable was added.
       */
      int addVariableName(final org.apache.xml.utils.QName qname)
      {
        int pos = m_variableNames.size();
        m_variableNames.addElement(qname);
        int frameSize = m_variableNames.size() - getGlobalsSize();
        if(frameSize > m_maxStackFrameSize)
          m_maxStackFrameSize++;
        return pos;
      }
      
      void resetStackFrameSize()
      {
        m_maxStackFrameSize = 0;
      }
      
      int getFrameSize()
      {
        return m_maxStackFrameSize;
      }
      
      /**
       * Get the current size of the stack frame.  Use this to record the position 
       * in a template element at startElement, so that it can be popped 
       * at endElement.
       */
      int getCurrentStackFrameSize()
      {
        return m_variableNames.size();
      }
      
      /**
       * Set the current size of the stack frame.
       */
      void setCurrentStackFrameSize(int sz)
      {
        m_variableNames.setSize(sz);
      }
      
      int getGlobalsSize()
      {
        return m_variables.size();
      }
      
      IntStack m_marks = new IntStack();
      
      void pushStackMark()
      {
        m_marks.push(getCurrentStackFrameSize());
      }
      
      void popStackMark()
      {
        int mark = m_marks.pop();
        setCurrentStackFrameSize(mark);
      }
      
      /**
       * Get the Vector of the current params and QNames to be collected 
       * within the current template.
       * @return A reference to the vector of variable names.  The reference 
       * returned is owned by this class, and so should not really be mutated, or 
       * stored anywhere.
       */
      java.util.Vector getVariableNames()
      {
        return m_variableNames;
      }
      
      private int m_maxStackFrameSize;

    }
    
    /**
     * @return Optimization flag
     */
    public boolean getOptimizer() {
        return m_optimizer;
    }

    /**
     * @param b Optimization flag
     */
    public void setOptimizer(boolean b) {
        m_optimizer = b;
    }

    /**
     * @return Incremental flag
     */
    public boolean getIncremental() {
        return m_incremental;
    }

    /**
     * @return source location flag
     */
    public boolean getSource_location() {
        return m_source_location;
    }

    /**
     * @param b Incremental flag
     */
    public void setIncremental(boolean b) {
        m_incremental = b;
    }

    /**
     * @param b Source location flag
     */
    public void setSource_location(boolean b) {
        m_source_location = b;
    }

}
