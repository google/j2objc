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
 * $Id: TransformerImpl.java 475979 2006-11-16 23:32:48Z minchau $
 */
package org.apache.xalan.transformer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.extensions.ExtensionsTable;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemAttributeSet;
import org.apache.xalan.templates.ElemForEach;
import org.apache.xalan.templates.ElemSort;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetComposed;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.XUnresolvedVariable;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.serializer.ToSAXHandler;
import org.apache.xml.serializer.ToTextStream;
import org.apache.xml.serializer.ToXMLSAXHandler;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.ObjectPool;
import org.apache.xml.utils.ObjectStack;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.ThreadControllerWrapper;
import org.apache.xpath.Arg;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class implements the
 * {@link javax.xml.transform.Transformer} interface, and is the core
 * representation of the transformation execution.</p>
 * @xsl.usage advanced
 */
public class TransformerImpl extends Transformer
        implements Runnable, DTMWSFilter, ExtensionsProvider, org.apache.xml.serializer.SerializerTrace
{

  // Synch object to gaurd against setting values from the TrAX interface 
  // or reentry while the transform is going on.

  /** NEEDSDOC Field m_reentryGuard          */
  private Boolean m_reentryGuard = new Boolean(true);

  /**
   * This is null unless we own the stream.
   */
  private java.io.FileOutputStream m_outputStream = null;

  /** The thread that the transformer is running on. */
  private Thread m_transformThread;

  /** The base URL of the source tree. */
  private String m_urlOfSource = null;

  /** The Result object at the start of the transform, if any. */
  private Result m_outputTarget = null;

  /**
   * The output format object set by the user.  May be null.
   */
  private OutputProperties m_outputFormat;


  /**
   * The content handler for the source input tree.
   */
  ContentHandler m_inputContentHandler;

  /**
   * The content handler for the result tree.
   */
  private ContentHandler m_outputContentHandler = null;

  //  /*
  //   * Use member variable to store param variables as they're
  //   * being created, use member variable so we don't
  //   * have to create a new vector every time.
  //   */
  //  private Vector m_newVars = new Vector();

  /**
   * A pool of ResultTreeHandlers, for serialization of a subtree to text.
   *  Please note that each of these also holds onto a Text Serializer.  
   */
  private ObjectPool m_textResultHandlerObjectPool =
    new ObjectPool(ToTextStream.class);

  /**
   * Related to m_textResultHandlerObjectPool, this is a pool of
   * StringWriters, which are passed to the Text Serializers.
   * (I'm not sure if this is really needed any more.  -sb)      
   */
  private ObjectPool m_stringWriterObjectPool =
    new ObjectPool(StringWriter.class);

  /**
   * A static text format object, which can be used over and
   * over to create the text serializers.    
   */
  private OutputProperties m_textformat = new OutputProperties(Method.TEXT);

  // Commenteded out in response to problem reported by 
  // Nicola Brown <Nicola.Brown@jacobsrimell.com>
  //  /**
  //   * Flag to let us know if an exception should be reported inside the 
  //   * postExceptionFromThread method.  This is needed if the transform is 
  //   * being generated from SAX events, and thus there is no central place 
  //   * to report the exception from.  (An exception is usually picked up in 
  //   * the main thread from the transform thread in {@link #transform(Source source)} 
  //   * from {@link #getExceptionThrown()}. )
  //   */
  //  private boolean m_reportInPostExceptionFromThread = false;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplateElement.  Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the calling template. Note the use of an array 
   * for this limits the recursion depth to 4K.
   */
  ObjectStack m_currentTemplateElements 
      = new ObjectStack(XPathContext.RECURSIONLIMIT);
  
  /** The top of the currentTemplateElements stack. */
  //int m_currentTemplateElementsTop = 0;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplate that was matched.
   * Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched template
   */
  Stack m_currentMatchTemplates = new Stack();

  /**
   * A node vector used as a stack to track the current
   * node that was matched.
   * Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched
   * node. 
   */
  NodeVector m_currentMatchedNodes = new NodeVector();

  /**
   * The root of a linked set of stylesheets.
   */
  private StylesheetRoot m_stylesheetRoot = null;

  /**
   * If this is set to true, do not warn about pattern
   * match conflicts.
   */
  private boolean m_quietConflictWarnings = true;

  /**
   * The liason to the XML parser, so the XSL processor
   * can handle included files, and the like, and do the
   * initial parse of the XSL document.
   */
  private XPathContext m_xcontext;

  /**
   * Output handler to bottleneck SAX events.
   */
  private SerializationHandler m_serializationHandler;  

  /** The key manager, which manages xsl:keys. */
  private KeyManager m_keyManager = new KeyManager();

  /**
   * Stack for the purposes of flagging infinite recursion with
   * attribute sets.
   */
  Stack m_attrSetStack = null;

  /**
   * The table of counters for xsl:number support.
   */
  CountersTable m_countersTable = null;

  /**
   * Is > 0 when we're processing a for-each.
   */
  BoolStack m_currentTemplateRuleIsNull = new BoolStack();

  /**
   * Keeps track of the result delivered by any EXSLT <code>func:result</code>
   * instruction that has been executed for the currently active EXSLT
   * <code>func:function</code>
   */
  ObjectStack m_currentFuncResult = new ObjectStack();
  
  /**
   * The message manager, which manages error messages, warning
   * messages, and other types of message events.   
   */
  private MsgMgr m_msgMgr;

  /**
   * The flag for the setting of the optimize feature;
   * This flag should have the same value as the FEATURE_OPTIMIZE feature
   * which is set by the TransformerFactory.setAttribut() method before a
   * Transformer is created
   */    
  private boolean m_optimizer = true;

  /**
   * The flag for the setting of the incremental feature;
   * This flag should have the same value as the FEATURE_INCREMENTAL feature
   * which is set by the TransformerFactory.setAttribut() method before a
   * Transformer is created
   */    
  private boolean m_incremental = false;

  /**
   * The flag for the setting of the source_location feature;
   * This flag should have the same value as the FEATURE_SOURCE_LOCATION feature
   * which is set by the TransformerFactory.setAttribut() method before a
   * Transformer is created
   */  
  private boolean m_source_location = false;
    
  /**
   * The SAX error handler, where errors and warnings are sent.
   */
  private ErrorListener m_errorHandler =
    new org.apache.xml.utils.DefaultErrorHandler(false);

  /**
   * If the transform thread throws an exception, the exception needs to
   * be stashed away so that the main thread can pass it on to the
   * client. 
   */
  private Exception m_exceptionThrown = null;

  /**
   * This is needed for support of setSourceTreeDocForThread(Node doc),
   * which must be called in order for the transform thread's run
   * method to obtain the root of the source tree to be transformed.     
   */
  private int m_doc;

  /** Flag to to tell if the tranformer needs to be reset. */
  private boolean m_hasBeenReset = false;

  /** NEEDSDOC Field m_shouldReset          */
  private boolean m_shouldReset = true;

  /**
   * A stack of current template modes.
   */
  private Stack m_modes = new Stack();

  //==========================================================
  // SECTION: Constructor
  //==========================================================

  /**
   * Construct a TransformerImpl.
   *
   * @param stylesheet The root of the stylesheet tree.
   */
  public TransformerImpl(StylesheetRoot stylesheet)
   // throws javax.xml.transform.TransformerException    
  {
    m_optimizer = stylesheet.getOptimizer();
    m_incremental = stylesheet.getIncremental();
    m_source_location = stylesheet.getSource_location();  	
    setStylesheet(stylesheet);
    XPathContext xPath = new XPathContext(this);
    xPath.setIncremental(m_incremental);
    xPath.getDTMManager().setIncremental(m_incremental);
    xPath.setSource_location(m_source_location);
    xPath.getDTMManager().setSource_location(m_source_location);
    
    if (stylesheet.isSecureProcessing())
      xPath.setSecureProcessing(true);
    
    setXPathContext(xPath);
    getXPathContext().setNamespaceContext(stylesheet);
  }
  
  // ================ ExtensionsTable ===================

  /**
   * The table of ExtensionHandlers.
   */
  private ExtensionsTable m_extensionsTable = null;

  /**
   * Get the extensions table object. 
   *
   * @return The extensions table.
   */
  public ExtensionsTable getExtensionsTable()
  {
    return m_extensionsTable;
  }

  /**
   * If the stylesheet contains extensions, set the extensions table object.
   *
   *
   * @param sroot The stylesheet.
   * @throws javax.xml.transform.TransformerException
   */
  void setExtensionsTable(StylesheetRoot sroot)
       throws javax.xml.transform.TransformerException
  {
    try
    {
      if (sroot.getExtensions() != null)
        //only load extensions if secureProcessing is disabled
        if(!sroot.isSecureProcessing())
            m_extensionsTable = new ExtensionsTable(sroot);
    }
    catch (javax.xml.transform.TransformerException te)
    {te.printStackTrace();}
  }
  
  //== Implementation of the XPath ExtensionsProvider interface.
  
  public boolean functionAvailable(String ns, String funcName)
          throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().functionAvailable(ns, funcName);
  }
  
  public boolean elementAvailable(String ns, String elemName)
          throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().elementAvailable(ns, elemName);   
  }
   
  public Object extFunction(String ns, String funcName, 
                            Vector argVec, Object methodKey)
            throws javax.xml.transform.TransformerException
  {//System.out.println("TransImpl.extFunction() " + ns + " " + funcName +" " + getExtensionsTable());
    return getExtensionsTable().extFunction(ns, funcName, 
                                        argVec, methodKey,
                                        getXPathContext().getExpressionContext());   
  }

  public Object extFunction(FuncExtFunction extFunction, Vector argVec)
            throws javax.xml.transform.TransformerException
  {
    return getExtensionsTable().extFunction(extFunction, argVec,
                                            getXPathContext().getExpressionContext());   
  }
  
  //=========================

  /**
   * Reset the state.  This needs to be called after a process() call
   * is invoked, if the processor is to be used again.
   */
  public void reset()
  {

    if (!m_hasBeenReset && m_shouldReset)
    {
      m_hasBeenReset = true;

      if (this.m_outputStream != null)
      {
        try
        {
          m_outputStream.close();
        }
        catch (java.io.IOException ioe){}
      }

      m_outputStream = null;

      // I need to look more carefully at which of these really
      // needs to be reset.
      m_countersTable = null;

      m_xcontext.reset();
      
      m_xcontext.getVarStack().reset();
      resetUserParameters();
      

      m_currentTemplateElements.removeAllElements();     
      m_currentMatchTemplates.removeAllElements();
      m_currentMatchedNodes.removeAllElements();
      
      m_serializationHandler = null;      
      m_outputTarget = null;
      m_keyManager = new KeyManager();
      m_attrSetStack = null;
      m_countersTable = null;
      m_currentTemplateRuleIsNull = new BoolStack();
      // m_xmlSource = null; // android-removed
      m_doc = DTM.NULL;
      // m_isTransformDone = false; // android-removed
      m_transformThread = null;

      // m_inputContentHandler = null;
      // For now, reset the document cache each time.
      m_xcontext.getSourceTreeManager().reset();
    }

    //    m_reportInPostExceptionFromThread = false;
  }

  // ========= Transformer Interface Implementation ==========

  /**
   * Get the thread that the transform process is on.
   *
   * @return The thread that the transform process is on, or null.
   * @xsl.usage internal
   */
  public Thread getTransformThread()
  {
    return m_transformThread;
  }

  /**
   * Get the thread that the transform process is on.
   *
   * @param t The transform thread, may be null.
   * @xsl.usage internal
   */
  public void setTransformThread(Thread t)
  {
    m_transformThread = t;
  }

  /** NEEDSDOC Field m_hasTransformThreadErrorCatcher          */
  private boolean m_hasTransformThreadErrorCatcher = false;

  /**
   * Return true if the transform was initiated from the transform method,
   * otherwise it was probably done from a pure parse events.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean hasTransformThreadErrorCatcher()
  {
    return m_hasTransformThreadErrorCatcher;
  }
        
        /**
   * Process the source tree to SAX parse events.
   * @param source  The input for the source tree.
   *
   * @throws TransformerException
   */
  public void transform(Source source) throws TransformerException
  {
                transform(source, true); 
        }

  /**
   * Process the source tree to SAX parse events.
   * @param source  The input for the source tree.
   * @param shouldRelease  Flag indicating whether to release DTMManager.
   *
   * @throws TransformerException
   */
  public void transform(Source source, boolean shouldRelease) throws TransformerException
  {

    try
    {
        
      // Patch for bugzilla #13863.  If we don't reset the namespaceContext
      // then we will get a NullPointerException if transformer is reused 
      // (for stylesheets that use xsl:key).  Not sure if this should go 
      // here or in reset(). -is  
      if(getXPathContext().getNamespaceContext() == null){
         getXPathContext().setNamespaceContext(getStylesheet());
      }
      String base = source.getSystemId();
      
      // If no systemID of the source, use the base of the stylesheet.
      if(null == base)
      {
        base = m_stylesheetRoot.getBaseIdentifier();
      }

      // As a last resort, use the current user dir.
      if(null == base)
      {
        String currentDir = "";
        try {
          currentDir = System.getProperty("user.dir");
        }
        catch (SecurityException se) {}// user.dir not accessible from applet
              
        if (currentDir.startsWith(java.io.File.separator))
          base = "file://" + currentDir;
        else
          base = "file:///" + currentDir;
        
        base = base + java.io.File.separatorChar
               + source.getClass().getName();
      }
      setBaseURLOfSource(base);
      DTMManager mgr = m_xcontext.getDTMManager();
      /*
       * According to JAXP1.2, new SAXSource()/StreamSource()
       * should create an empty input tree, with a default root node. 
       * new DOMSource()creates an empty document using DocumentBuilder.
       * newDocument(); Use DocumentBuilder.newDocument() for all 3 situations,
       * since there is no clear spec. how to create an empty tree when
       * both SAXSource() and StreamSource() are used.
       */
      if ((source instanceof StreamSource && source.getSystemId()==null &&
         ((StreamSource)source).getInputStream()==null &&
         ((StreamSource)source).getReader()==null)||
         (source instanceof SAXSource &&
         ((SAXSource)source).getInputSource()==null &&
         ((SAXSource)source).getXMLReader()==null )||
         (source instanceof DOMSource && ((DOMSource)source).getNode()==null)){
        try {
          DocumentBuilderFactory builderF = 
                   DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = builderF.newDocumentBuilder();
          String systemID = source.getSystemId();
          source = new DOMSource(builder.newDocument());

          // Copy system ID from original, empty Source to new Source
          if (systemID != null) {
            source.setSystemId(systemID);
          }
        } catch (ParserConfigurationException e) {
          fatalError(e);
        }           
      }
      DTM dtm = mgr.getDTM(source, false, this, true, true);
      dtm.setDocumentBaseURI(base);
      
      boolean hardDelete = true;  // %REVIEW% I have to think about this. -sb

      try
      {
      	// NOTE: This will work because this is _NOT_ a shared DTM, and thus has
      	// only a single Document node. If it could ever be an RTF or other
      	// shared DTM, look at dtm.getDocumentRoot(nodeHandle).
        this.transformNode(dtm.getDocument());
      }
      finally
      {
        if (shouldRelease)
          mgr.release(dtm, hardDelete);
      }

      // Kick off the parse.  When the ContentHandler gets 
      // the startDocument event, it will call transformNode( node ).
      // reader.parse( xmlSource );
      // This has to be done to catch exceptions thrown from 
      // the transform thread spawned by the STree handler.
      Exception e = getExceptionThrown();

      if (null != e)
      {
        if (e instanceof javax.xml.transform.TransformerException)
        {
          throw (javax.xml.transform.TransformerException) e;
        }
        else if (e instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          fatalError(
              ((org.apache.xml.utils.WrappedRuntimeException) e).getException());
        }
        else
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else if (null != m_serializationHandler)
      {
        m_serializationHandler.endDocument();
      }
    }
    catch (org.apache.xml.utils.WrappedRuntimeException wre)
    {
      Throwable throwable = wre.getException();

      while (throwable
             instanceof org.apache.xml.utils.WrappedRuntimeException)
      {
        throwable =
          ((org.apache.xml.utils.WrappedRuntimeException) throwable).getException();
      }

      fatalError(throwable);
    }

    // Patch attributed to David Eisenberg <david@catcode.com>
    catch (org.xml.sax.SAXParseException spe)
    {
      fatalError(spe);
    }
    catch (org.xml.sax.SAXException se)
    {
      m_errorHandler.fatalError(new TransformerException(se));
    }
    finally
    {
      m_hasTransformThreadErrorCatcher = false;

      // This looks to be redundent to the one done in TransformNode.
      reset();
    }
  }
  
  private void fatalError(Throwable throwable) throws TransformerException
  {
    if (throwable instanceof org.xml.sax.SAXParseException)
      m_errorHandler.fatalError(new TransformerException(throwable.getMessage(),new SAXSourceLocator((org.xml.sax.SAXParseException)throwable)));
    else
      m_errorHandler.fatalError(new TransformerException(throwable));
    
  }

  /**
   * Get the base URL of the source.
   *
   *
   * NEEDSDOC @param base
   * @return The base URL of the source tree, or null.
   */
  public void setBaseURLOfSource(String base)
  {
    m_urlOfSource = base;
  }

  /**
   * Get an output property that is in effect for the
   * transformation.  The property specified may be a property
   * that was set with setOutputProperty, or it may be a
   * property specified in the stylesheet.
   *
   * NEEDSDOC @param qnameString
   *
   * @return The string value of the output property, or null
   * if no property was found.
   *
   * @throws IllegalArgumentException If the property is not supported.
   *
   * @see javax.xml.transform.OutputKeys
   */
  public String getOutputProperty(String qnameString)
          throws IllegalArgumentException
  {

    String value = null;
    OutputProperties props = getOutputFormat();

    value = props.getProperty(qnameString);

    if (null == value)
    {
      if (!OutputProperties.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); //"output property not recognized: "
                                           //+ qnameString);
    }

    return value;
  }

  /**
   * Get the value of a property, without using the default properties.  This
   * can be used to test if a property has been explicitly set by the stylesheet
   * or user.
   *
   * NEEDSDOC @param qnameString
   *
   * @return The value of the property, or null if not found.
   *
   * @throws IllegalArgumentException If the property is not supported,
   * and is not namespaced.
   */
  public String getOutputPropertyNoDefault(String qnameString)
          throws IllegalArgumentException
  {

    String value = null;
    OutputProperties props = getOutputFormat();

    value = (String) props.getProperties().get(qnameString);

    if (null == value)
    {
      if (!OutputProperties.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); //"output property not recognized: "
                                          // + qnameString);
    }

    return value;
  }

  /**
   * This method is used to set or override the value
   * of the effective xsl:output attribute values
   * specified in the stylesheet.
   * <p>
   * The recognized standard output properties are:
   * <ul>
   * <li>cdata-section-elements
   * <li>doctype-system
   * <li>doctype-public
   * <li>indent
   * <li>media-type
   * <li>method
   * <li>omit-xml-declaration
   * <li>standalone
   * <li>version
   * </ul>
   * <p>
   * For example:
   * <pre>
   *   tran.setOutputProperty("standalone", "yes");
   * </pre>
   * <p>
   * In the case of the cdata-section-elements property,
   * the value should be a whitespace separated list of
   * element names.  The element name is the local name
   * of the element, if it is in no namespace, or, the URI
   * in braces followed immediately by the local name
   * if the element is in that namespace. For example: 
   * <pre>
   * tran.setOutputProperty(
   *   "cdata-section-elements", 
   *   "elem1 {http://example.uri}elem2 elem3");
   * </pre>
   * <p>
   * The recognized Xalan extension elements are: 
   * <ul>
   * <li>content-handler
   * <li>entities
   * <li>indent-amount
   * <li>line-separator
   * <li>omit-meta-tag
   * <li>use-url-escaping
   * </ul>
   * <p>
   * These must be in the extension namespace of
   * "http://xml.apache.org/xalan".  This is accomplished
   * by putting the namespace URI in braces before the 
   * property name, for example:
   * <pre>
   *   tran.setOutputProperty(
   *     "{http://xml.apache.org/xalan}line-separator" ,
   *     "\n");
   * </pre> 
   *
   * @param name The property name.
   * @param value The requested value for the property.
   * @throws IllegalArgumentException if the property name is not legal.
   */
  public void setOutputProperty(String name, String value)
          throws IllegalArgumentException
  {

    synchronized (m_reentryGuard)
    {

      // Get the output format that was set by the user, otherwise get the 
      // output format from the stylesheet.
      if (null == m_outputFormat)
      {
        m_outputFormat =
          (OutputProperties) getStylesheet().getOutputComposed().clone();
      }

      if (!OutputProperties.isLegalPropertyKey(name))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); //"output property not recognized: "
                                           //+ name);

      m_outputFormat.setProperty(name, value);
    }
  }

  /**
   * Set the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * <p>If argument to this function is null, any properties
   * previously set will be removed.</p>
   *
   * @param oformat A set of output properties that will be
   * used to override any of the same properties in effect
   * for the transformation.
   *
   * @see javax.xml.transform.OutputKeys
   * @see java.util.Properties
   *
   * @throws IllegalArgumentException if any of the argument keys are not
   * recognized and are not namespace qualified.   
   */
  public void setOutputProperties(Properties oformat)
  		throws IllegalArgumentException
  {

    synchronized (m_reentryGuard)
    {
      if (null != oformat)
      {

        // See if an *explicit* method was set.
        String method = (String) oformat.get(OutputKeys.METHOD);

        if (null != method)
          m_outputFormat = new OutputProperties(method);
        else if(m_outputFormat==null)
          m_outputFormat = new OutputProperties();

        m_outputFormat.copyFrom(oformat);
        // copyFrom does not set properties that have been already set, so 
        // this must be called after, which is a bit in the reverse from 
        // what one might think.
        m_outputFormat.copyFrom(m_stylesheetRoot.getOutputProperties());
      }
      else {
        // if oformat is null JAXP says that any props previously set are removed
        // and we are to revert back to those in the templates object (i.e. Stylesheet).
        m_outputFormat = null;
      }
    }
  }

  /**
   * Get a copy of the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * <p>Note that mutation of the Properties object returned will not
   * effect the properties that the transformation contains.</p>
   *
   * @return  A copy of the set of output properties in effect
   * for the next transformation.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Properties getOutputProperties()
  {
    return (Properties) getOutputFormat().getProperties().clone();
  }

    /**
     * Create a result ContentHandler from a Result object, based
     * on the current OutputProperties.
     *
     * @param outputTarget Where the transform result should go,
     * should not be null.
     *
     * @return A valid ContentHandler that will create the
     * result tree when it is fed SAX events.
     *
     * @throws TransformerException
     */
    public SerializationHandler createSerializationHandler(Result outputTarget)
            throws TransformerException
    {
       SerializationHandler xoh =
        createSerializationHandler(outputTarget, getOutputFormat());
       return xoh;
    }

    /**
     * Create a ContentHandler from a Result object and an OutputProperties.
     *
     * @param outputTarget Where the transform result should go,
     * should not be null.
     * @param format The OutputProperties object that will contain
     * instructions on how to serialize the output.
     *
     * @return A valid ContentHandler that will create the
     * result tree when it is fed SAX events.
     *
     * @throws TransformerException
     */
    public SerializationHandler createSerializationHandler(
            Result outputTarget, OutputProperties format)
              throws TransformerException
    {

      SerializationHandler xoh;

      // If the Result object contains a Node, then create
      // a ContentHandler that will add nodes to the input node.
      org.w3c.dom.Node outputNode = null;

      if (outputTarget instanceof DOMResult)
      {
        outputNode = ((DOMResult) outputTarget).getNode();
        org.w3c.dom.Node nextSibling = ((DOMResult)outputTarget).getNextSibling();

        org.w3c.dom.Document doc;
        short type;

        if (null != outputNode)
        {
          type = outputNode.getNodeType();
          doc = (org.w3c.dom.Node.DOCUMENT_NODE == type)
                ? (org.w3c.dom.Document) outputNode
                : outputNode.getOwnerDocument();
        }
        else
        {
          boolean isSecureProcessing = m_stylesheetRoot.isSecureProcessing();
          doc = org.apache.xml.utils.DOMHelper.createDocument(isSecureProcessing);
          outputNode = doc;
          type = outputNode.getNodeType();

          ((DOMResult) outputTarget).setNode(outputNode);
        }

        DOMBuilder handler =
          (org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE == type)
          ? new DOMBuilder(doc, (org.w3c.dom.DocumentFragment) outputNode)
          : new DOMBuilder(doc, outputNode);
        
        if (nextSibling != null)
          handler.setNextSibling(nextSibling);
        
          String encoding = format.getProperty(OutputKeys.ENCODING);          
          xoh = new ToXMLSAXHandler(handler, (LexicalHandler)handler, encoding);
      }
      else if (outputTarget instanceof SAXResult)
      {
        ContentHandler handler = ((SAXResult) outputTarget).getHandler();
        
        if (null == handler)
           throw new IllegalArgumentException(
             "handler can not be null for a SAXResult"); 
             
        LexicalHandler lexHandler;
        if (handler instanceof LexicalHandler)     
            lexHandler = (LexicalHandler)  handler;
        else
            lexHandler = null;
            
        String encoding = format.getProperty(OutputKeys.ENCODING); 
        String method = format.getProperty(OutputKeys.METHOD);

        ToXMLSAXHandler toXMLSAXHandler = new ToXMLSAXHandler(handler, lexHandler, encoding);
        toXMLSAXHandler.setShouldOutputNSAttr(false);
        xoh = toXMLSAXHandler;   


        String publicID = format.getProperty(OutputKeys.DOCTYPE_PUBLIC); 
        String systemID = format.getProperty(OutputKeys.DOCTYPE_SYSTEM); 
        if (systemID != null)
            xoh.setDoctypeSystem(systemID);
        if (publicID != null)
            xoh.setDoctypePublic(publicID);
        
        if (handler instanceof TransformerClient) {
            XalanTransformState state = new XalanTransformState();
            ((TransformerClient)handler).setTransformState(state);
            ((ToSAXHandler)xoh).setTransformState(state);
        }

 
      }

      // Otherwise, create a ContentHandler that will serialize the
      // result tree to either a stream or a writer.
      else if (outputTarget instanceof StreamResult)
      {
        StreamResult sresult = (StreamResult) outputTarget;

        try
        {
          SerializationHandler serializer =
            (SerializationHandler) SerializerFactory.getSerializer(format.getProperties());

          if (null != sresult.getWriter())
            serializer.setWriter(sresult.getWriter());
          else if (null != sresult.getOutputStream())
            serializer.setOutputStream(sresult.getOutputStream());
          else if (null != sresult.getSystemId())
          {
            String fileURL = sresult.getSystemId();

            if (fileURL.startsWith("file:///"))
            {
              if (fileURL.substring(8).indexOf(":") >0)
                fileURL = fileURL.substring(8);
              else
                fileURL = fileURL.substring(7);
            }
            else if (fileURL.startsWith("file:/"))
            {
                if (fileURL.substring(6).indexOf(":") >0)
                    fileURL = fileURL.substring(6);
                  else
                    fileURL = fileURL.substring(5);            	
            }

            m_outputStream = new java.io.FileOutputStream(fileURL);

            serializer.setOutputStream(m_outputStream);
            
            xoh = serializer;
          }
          else
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null)); //"No output specified!");

          // handler = serializer.asContentHandler();

        //  this.setSerializer(serializer);

          xoh = serializer;  
        }
//        catch (UnsupportedEncodingException uee)
//        {
//          throw new TransformerException(uee);
//        }
        catch (IOException ioe)
        {
          throw new TransformerException(ioe);
        }
      }
      else
      {
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()})); //"Can't transform to a Result of type "
                                       //+ outputTarget.getClass().getName()
                                       //+ "!");
      }
      
      // before we forget, lets make the created handler hold a reference
      // to the current TransformImpl object
      xoh.setTransformer(this);

      SourceLocator srcLocator = getStylesheet();
      xoh.setSourceLocator(srcLocator);
      
      
      return xoh;

   
    }
        
        /**
   * Process the source tree to the output result.
   * @param xmlSource  The input for the source tree.
   * @param outputTarget The output source target.
   *
   * @throws TransformerException
   */
  public void transform(Source xmlSource, Result outputTarget)
          throws TransformerException
  {
                transform(xmlSource, outputTarget, true);
        }

  /**
   * Process the source tree to the output result.
   * @param xmlSource  The input for the source tree.
   * @param outputTarget The output source target.
   * @param shouldRelease  Flag indicating whether to release DTMManager. 
   *
   * @throws TransformerException
   */
  public void transform(Source xmlSource, Result outputTarget, boolean shouldRelease)
          throws TransformerException
  {

    synchronized (m_reentryGuard)
    {
      SerializationHandler xoh = createSerializationHandler(outputTarget);
      this.setSerializationHandler(xoh);        

      m_outputTarget = outputTarget;

      transform(xmlSource, shouldRelease);
    }
  }

  /**
   * Process the source node to the output result, if the
   * processor supports the "http://xml.org/trax/features/dom/input"
   * feature.
   * %REVIEW% Do we need a Node version of this?
   * @param node  The input source node, which can be any valid DTM node.
   * @param outputTarget The output source target.
   *
   * @throws TransformerException
   */
  public void transformNode(int node, Result outputTarget)
          throws TransformerException
  {
    

    SerializationHandler xoh = createSerializationHandler(outputTarget);
    this.setSerializationHandler(xoh);

    m_outputTarget = outputTarget;

    transformNode(node);
  }

  /**
   * Process the source node to the output result, if the
   * processor supports the "http://xml.org/trax/features/dom/input"
   * feature.
   * %REVIEW% Do we need a Node version of this?
   * @param node  The input source node, which can be any valid DTM node.
   *
   * @throws TransformerException
   */
  public void transformNode(int node) throws TransformerException
  {
    //dml
    setExtensionsTable(getStylesheet());
    // Make sure we're not writing to the same output content handler.
    synchronized (m_serializationHandler)
    {
      m_hasBeenReset = false;
      
      XPathContext xctxt = getXPathContext();
      DTM dtm = xctxt.getDTM(node);

      try
      {
        pushGlobalVars(node);

        // ==========
        // Give the top-level templates a chance to pass information into 
        // the context (this is mainly for setting up tables for extensions).
        StylesheetRoot stylesheet = this.getStylesheet();
        int n = stylesheet.getGlobalImportCount();

        for (int i = 0; i < n; i++)
        {
          StylesheetComposed imported = stylesheet.getGlobalImport(i);
          int includedCount = imported.getIncludeCountComposed();

          for (int j = -1; j < includedCount; j++)
          {
            Stylesheet included = imported.getIncludeComposed(j);

            included.runtimeInit(this);

            for (ElemTemplateElement child = included.getFirstChildElem();
                    child != null; child = child.getNextSiblingElem())
            {
              child.runtimeInit(this);
            }
          }
        }
        // ===========        
        // System.out.println("Calling applyTemplateToNode - "+Thread.currentThread().getName());
        DTMIterator dtmIter = new org.apache.xpath.axes.SelfIteratorNoPredicate();
        dtmIter.setRoot(node, xctxt);
        xctxt.pushContextNodeList(dtmIter);
        try
        {
          this.applyTemplateToNode(null, null, node);
        }
        finally
        {
          xctxt.popContextNodeList();
        }
        // m_stylesheetRoot.getStartRule().execute(this);

        // System.out.println("Done with applyTemplateToNode - "+Thread.currentThread().getName());
        if (null != m_serializationHandler)
        {
          m_serializationHandler.endDocument();
        }
      }
      catch (Exception se)
      {

        // System.out.println(Thread.currentThread().getName()+" threw an exception! "
        //                   +se.getMessage());
        // If an exception was thrown, we need to make sure that any waiting 
        // handlers can terminate, which I guess is best done by sending 
        // an endDocument.
        
        // SAXSourceLocator
        while(se instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          Exception e = ((org.apache.xml.utils.WrappedRuntimeException)se).getException();
          if(null != e)
            se = e;
        }
        
        if (null != m_serializationHandler)
        {
          try
          {
            if(se instanceof org.xml.sax.SAXParseException)
              m_serializationHandler.fatalError((org.xml.sax.SAXParseException)se);
            else if(se instanceof TransformerException)
            {
              TransformerException te = ((TransformerException)se);
              SAXSourceLocator sl = new SAXSourceLocator( te.getLocator() );
              m_serializationHandler.fatalError(new org.xml.sax.SAXParseException(te.getMessage(), sl, te)); 
            }
            else
            {
              m_serializationHandler.fatalError(new org.xml.sax.SAXParseException(se.getMessage(), new SAXSourceLocator(), se)); 
            }             
          }
          catch (Exception e){}
        }        
        
        if(se instanceof TransformerException)
        {
          m_errorHandler.fatalError((TransformerException)se);
        }
        else if(se instanceof org.xml.sax.SAXParseException)
        {
          m_errorHandler.fatalError(new TransformerException(se.getMessage(), 
                      new SAXSourceLocator((org.xml.sax.SAXParseException)se), 
                      se));
        }
        else
        {
          m_errorHandler.fatalError(new TransformerException(se));
        }
        
      }
      finally
      {
        this.reset();
      }
    }
  }

  /**
   * Get a SAX2 ContentHandler for the input.
   *
   * @return A valid ContentHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public ContentHandler getInputContentHandler()
  {
    return getInputContentHandler(false);
  }

  /**
   * Get a SAX2 ContentHandler for the input.
   *
   * @param doDocFrag true if a DocumentFragment should be created as
   * the root, rather than a Document.
   *
   * @return A valid ContentHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public ContentHandler getInputContentHandler(boolean doDocFrag)
  {

    if (null == m_inputContentHandler)
    {

      //      if(null == m_urlOfSource && null != m_stylesheetRoot)
      //        m_urlOfSource = m_stylesheetRoot.getBaseIdentifier();
      m_inputContentHandler = new TransformerHandlerImpl(this, doDocFrag,
              m_urlOfSource);
    }

    return m_inputContentHandler;
  }

  /**
   * Set the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * @param oformat A valid OutputProperties object (which will
   * not be mutated), or null.
   */
  public void setOutputFormat(OutputProperties oformat)
  {
    m_outputFormat = oformat;
  }

  /**
   * Get the output properties used for the transformation.
   *
   * @return the output format that was set by the user,
   * otherwise the output format from the stylesheet.
   */
  public OutputProperties getOutputFormat()
  {

    // Get the output format that was set by the user, otherwise get the 
    // output format from the stylesheet.
    OutputProperties format = (null == m_outputFormat)
                              ? getStylesheet().getOutputComposed()
                              : m_outputFormat;

    return format;
  }

  /**
   * Set a parameter for the templates.
   * 
   * @param name The name of the parameter.
   * @param namespace The namespace of the parameter.
   * @param value The value object.  This can be any valid Java object
   * -- it's up to the processor to provide the proper
   * coersion to the object, or simply pass it on for use
   * in extensions.
   */
  public void setParameter(String name, String namespace, Object value)
  {

    VariableStack varstack = getXPathContext().getVarStack();
    QName qname = new QName(namespace, name);
    XObject xobject = XObject.create(value, getXPathContext());
    
    StylesheetRoot sroot = m_stylesheetRoot;
    Vector vars = sroot.getVariablesAndParamsComposed();
    int i = vars.size();
    while (--i >= 0)
    {
      ElemVariable variable = (ElemVariable)vars.elementAt(i);
      if(variable.getXSLToken() == Constants.ELEMNAME_PARAMVARIABLE && 
         variable.getName().equals(qname))
      {
          varstack.setGlobalVariable(i, xobject);
      }
    }
  }

  /** NEEDSDOC Field m_userParams          */
  Vector m_userParams;

  /**
   * Set a parameter for the transformation.
   *
   * @param name The name of the parameter,
   *             which may have a namespace URI.
   * @param value The value object.  This can be any valid Java object
   * -- it's up to the processor to provide the proper
   * coersion to the object, or simply pass it on for use
   * in extensions.
   */
  public void setParameter(String name, Object value)
  {
    
    if (value == null) {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_SET_PARAM_VALUE, new Object[]{name}));
    }    

    StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);

    try
    {

      // The first string might be the namespace, or it might be 
      // the local name, if the namespace is null.
      String s1 = tokenizer.nextToken();
      String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

      if (null == m_userParams)
        m_userParams = new Vector();

      if (null == s2)
      {
        replaceOrPushUserParam(new QName(s1), XObject.create(value, getXPathContext()));
        setParameter(s1, null, value);
      }
      else
      {
        replaceOrPushUserParam(new QName(s1, s2), XObject.create(value, getXPathContext()));
        setParameter(s2, s1, value);
      }
    }
    catch (java.util.NoSuchElementException nsee)
    {

      // Should throw some sort of an error.
    }
  }

  /**
   * NEEDSDOC Method replaceOrPushUserParam 
   *
   *
   * NEEDSDOC @param qname
   * NEEDSDOC @param xval
   */
  private void replaceOrPushUserParam(QName qname, XObject xval)
  {

    int n = m_userParams.size();

    for (int i = n - 1; i >= 0; i--)
    {
      Arg arg = (Arg) m_userParams.elementAt(i);

      if (arg.getQName().equals(qname))
      {
        m_userParams.setElementAt(new Arg(qname, xval, true), i);

        return;
      }
    }

    m_userParams.addElement(new Arg(qname, xval, true));
  }

  /**
   * Get a parameter that was explicitly set with setParameter
   * or setParameters.
   *
   *
   * NEEDSDOC @param name
   * @return A parameter that has been set with setParameter
   * or setParameters,
   * *not* all the xsl:params on the stylesheet (which require
   * a transformation Source to be evaluated).
   */
  public Object getParameter(String name)
  {

    try
    {

      // VariableStack varstack = getXPathContext().getVarStack();
      // The first string might be the namespace, or it might be 
      // the local name, if the namespace is null.
      QName qname = QName.getQNameFromString(name);

      if (null == m_userParams)
        return null;

      int n = m_userParams.size();

      for (int i = n - 1; i >= 0; i--)
      {
        Arg arg = (Arg) m_userParams.elementAt(i);

        if (arg.getQName().equals(qname))
        {
          return arg.getVal().object();
        }
      }

      return null;
    }
    catch (java.util.NoSuchElementException nsee)
    {

      // Should throw some sort of an error.
      return null;
    }
  }
  
  /**
   * Reset parameters that the user specified for the transformation.
   * Called during transformer.reset() after we have cleared the 
   * variable stack. We need to make sure that user params are
   * reset so that the transformer object can be reused. 
   */
  private void resetUserParameters()
  {

    try
    {
      
      if (null == m_userParams)
        return;

      int n = m_userParams.size();
      for (int i = n - 1; i >= 0; i--)
      {
        Arg arg = (Arg) m_userParams.elementAt(i);
        QName name = arg.getQName();
        // The first string might be the namespace, or it might be 
        // the local name, if the namespace is null.
        String s1 = name.getNamespace();
        String s2 = name.getLocalPart();

        setParameter(s2, s1, arg.getVal().object());
        
      }
      
    }
    catch (java.util.NoSuchElementException nsee)
    {
      // Should throw some sort of an error.
      
    }
  }

  /**
   * Set a bag of parameters for the transformation. Note that
   * these will not be additive, they will replace the existing
   * set of parameters.
   *
   * NEEDSDOC @param params
   */
  public void setParameters(Properties params)
  {

    clearParameters();

    Enumeration names = params.propertyNames();

    while (names.hasMoreElements())
    {
      String name = params.getProperty((String) names.nextElement());
      StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);

      try
      {

        // The first string might be the namespace, or it might be 
        // the local name, if the namespace is null.
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

        if (null == s2)
          setParameter(s1, null, params.getProperty(name));
        else
          setParameter(s2, s1, params.getProperty(name));
      }
      catch (java.util.NoSuchElementException nsee)
      {

        // Should throw some sort of an error.
      }
    }
  }

  /**
   * Reset the parameters to a null list.
   */
  public void clearParameters()
  {

    synchronized (m_reentryGuard)
    {
      VariableStack varstack = new VariableStack();

      m_xcontext.setVarStack(varstack);

      m_userParams = null;
    }
  }


  /**
   * Internal -- push the global variables from the Stylesheet onto
   * the context's runtime variable stack.
   * <p>If we encounter a variable
   * that is already defined in the variable stack, we ignore it.  This
   * is because the second variable definition will be at a lower import
   * precedence.  Presumably, global"variables at the same import precedence
   * with the same name will have been caught during the recompose process.
   * <p>However, if we encounter a parameter that is already defined in the
   * variable stack, we need to see if this is a parameter whose value was
   * supplied by a setParameter call.  If so, we need to "receive" the one
   * already in the stack, ignoring this one.  If it is just an earlier
   * xsl:param or xsl:variable definition, we ignore it using the same
   * reasoning as explained above for the variable.
   *
   * @param contextNode The root of the source tree, can't be null.
   *
   * @throws TransformerException
   */
  protected void pushGlobalVars(int contextNode) throws TransformerException
  {

    XPathContext xctxt = m_xcontext;
    VariableStack vs = xctxt.getVarStack();
    StylesheetRoot sr = getStylesheet();
    Vector vars = sr.getVariablesAndParamsComposed();
    
    int i = vars.size();
    vs.link(i);

    while (--i >= 0)
    {
      ElemVariable v = (ElemVariable) vars.elementAt(i);

      // XObject xobj = v.getValue(this, contextNode);
      XObject xobj = new XUnresolvedVariable(v, contextNode, this,
                                     vs.getStackFrame(), 0, true);
      
      if(null == vs.elementAt(i))                               
        vs.setGlobalVariable(i, xobj);
    }

  }

  /**
   * Set an object that will be used to resolve URIs used in
   * document(), etc.
   * @param resolver An object that implements the URIResolver interface,
   * or null.
   */
  public void setURIResolver(URIResolver resolver)
  {

    synchronized (m_reentryGuard)
    {
      m_xcontext.getSourceTreeManager().setURIResolver(resolver);
    }
  }

  /**
   * Get an object that will be used to resolve URIs used in
   * document(), etc.
   *
   * @return An object that implements the URIResolver interface,
   * or null.
   */
  public URIResolver getURIResolver()
  {
    return m_xcontext.getSourceTreeManager().getURIResolver();
  }

  // ======== End Transformer Implementation ========  

  /**
   * Set the content event handler.
   *
   * NEEDSDOC @param handler
   * @throws java.lang.NullPointerException If the handler
   *            is null.
   * @see org.xml.sax.XMLReader#setContentHandler
   */
  public void setContentHandler(ContentHandler handler)
  {

    if (handler == null)
    {
      throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_CONTENT_HANDLER, null)); //"Null content handler");
    }
    else
    {
      m_outputContentHandler = handler;

      if (null == m_serializationHandler)
      {
        ToXMLSAXHandler h = new ToXMLSAXHandler();
        h.setContentHandler(handler);
        h.setTransformer(this);
        
        m_serializationHandler = h;
      }
      else
        m_serializationHandler.setContentHandler(handler);
    }
  }

  /**
   * Get the content event handler.
   *
   * @return The current content handler, or null if none was set.
   * @see org.xml.sax.XMLReader#getContentHandler
   */
  public ContentHandler getContentHandler()
  {
    return m_outputContentHandler;
  }

  /**
   * Given a stylesheet element, create a result tree fragment from it's
   * contents. The fragment will be built within the shared RTF DTM system
   * used as a variable stack.
   * @param templateParent The template element that holds the fragment.
   * @return the NodeHandle for the root node of the resulting RTF.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public int transformToRTF(ElemTemplateElement templateParent)
          throws TransformerException
  {
    // Retrieve a DTM to contain the RTF. At this writing, this may be a
    // multi-document DTM (SAX2RTFDTM).
    DTM dtmFrag = m_xcontext.getRTFDTM();
    return transformToRTF(templateParent,dtmFrag);
  }
  
  /**
   * Given a stylesheet element, create a result tree fragment from it's
   * contents. The fragment will also use the shared DTM system, but will
   * obtain its space from the global variable pool rather than the dynamic
   * variable stack. This allows late binding of XUnresolvedVariables without
   * the risk that their content will be discarded when the variable stack
   * is popped.
   * 
   * @param templateParent The template element that holds the fragment.
   * @return the NodeHandle for the root node of the resulting RTF.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public int transformToGlobalRTF(ElemTemplateElement templateParent)
          throws TransformerException
  {
    // Retrieve a DTM to contain the RTF. At this writing, this may be a
    // multi-document DTM (SAX2RTFDTM).
    DTM dtmFrag = m_xcontext.getGlobalRTFDTM();
    return transformToRTF(templateParent,dtmFrag);
  }
  
  /**
   * Given a stylesheet element, create a result tree fragment from it's
   * contents.
   * @param templateParent The template element that holds the fragment.
   * @param dtmFrag The DTM to write the RTF into
   * @return the NodeHandle for the root node of the resulting RTF.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  private int transformToRTF(ElemTemplateElement templateParent,DTM dtmFrag)
          throws TransformerException
  {

    XPathContext xctxt = m_xcontext;
    
    ContentHandler rtfHandler = dtmFrag.getContentHandler();

    // Obtain the ResultTreeFrag's root node.
    // NOTE: In SAX2RTFDTM, this value isn't available until after
    // the startDocument has been issued, so assignment has been moved
    // down a bit in the code.
    int resultFragment; // not yet reliably = dtmFrag.getDocument();

    // Save the current result tree handler.
    SerializationHandler savedRTreeHandler = this.m_serializationHandler;
 

    // And make a new handler for the RTF.
    ToSAXHandler h = new ToXMLSAXHandler();
    h.setContentHandler(rtfHandler);
    h.setTransformer(this);
    
    // Replace the old handler (which was already saved)
    m_serializationHandler = h;
 
    // use local variable for the current handler
    SerializationHandler rth = m_serializationHandler;

    try
    {
      rth.startDocument();
      
      // startDocument is "bottlenecked" in RTH. We need it acted upon immediately,
      // to set the DTM's state as in-progress, so that if the xsl:variable's body causes
      // further RTF activity we can keep that from bashing this DTM.
      rth.flushPending(); 
 
      try
      {

        // Do the transformation of the child elements.
        executeChildTemplates(templateParent, true);

        // Make sure everything is flushed!
        rth.flushPending();
        
        // Get the document ID. May not exist until the RTH has not only
        // received, but flushed, the startDocument, and may be invalid
        // again after the document has been closed (still debating that)
        // ... so waiting until just before the end seems simplest/safest. 
	resultFragment = dtmFrag.getDocument();      
      }
      finally
      {
        rth.endDocument();
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {

      // Restore the previous result tree handler.
      this.m_serializationHandler = savedRTreeHandler;
    }

    return resultFragment;
  }

  /**
   * Take the contents of a template element, process it, and
   * convert it to a string.
   *
   * @param elem The parent element whose children will be output
   * as a string.
   *
   * @return The stringized result of executing the elements children.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public String transformToString(ElemTemplateElement elem)
          throws TransformerException
  {
    ElemTemplateElement firstChild = elem.getFirstChildElem();
    if(null == firstChild)
      return "";
    if(elem.hasTextLitOnly() && m_optimizer)
    {
      return ((ElemTextLiteral)firstChild).getNodeValue();
    }

    // Save the current result tree handler.
    SerializationHandler savedRTreeHandler = this.m_serializationHandler;

    // Create a Serializer object that will handle the SAX events 
    // and build the ResultTreeFrag nodes.
    StringWriter sw = (StringWriter) m_stringWriterObjectPool.getInstance();

    m_serializationHandler =
        (ToTextStream) m_textResultHandlerObjectPool.getInstance();

      if (null == m_serializationHandler)
      {
        // if we didn't get one from the pool, go make a new one

        
        Serializer serializer = org.apache.xml.serializer.SerializerFactory.getSerializer(
            m_textformat.getProperties());
        m_serializationHandler = (SerializationHandler) serializer;
      } 

        m_serializationHandler.setTransformer(this);
        m_serializationHandler.setWriter(sw);
 
 
    String result;

    try
    {
        /* Don't call startDocument, the SerializationHandler  will
         * generate its own internal startDocument call anyways
         */
      // this.m_serializationHandler.startDocument();

      // Do the transformation of the child elements.
      executeChildTemplates(elem, true);
        this.m_serializationHandler.endDocument();

      result = sw.toString();
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      sw.getBuffer().setLength(0);

      try
      {
        sw.close();
      }
      catch (Exception ioe){}

      m_stringWriterObjectPool.freeInstance(sw);
      m_serializationHandler.reset();
      m_textResultHandlerObjectPool.freeInstance(m_serializationHandler);

      // Restore the previous result tree handler.
      m_serializationHandler = savedRTreeHandler;
    }

    return result;
  }

  /**
   * Given an element and mode, find the corresponding
   * template and process the contents.
   *
   * @param xslInstruction The calling element.
   * @param template The template to use if xsl:for-each, current template for apply-imports, or null.
   * @param child The source context node.
   * @throws TransformerException
   * @return true if applied a template, false if not.
   * @xsl.usage advanced
   */
  public boolean applyTemplateToNode(ElemTemplateElement xslInstruction,  // xsl:apply-templates or xsl:for-each
                                     ElemTemplate template, int child)
                                             throws TransformerException
  {

    DTM dtm = m_xcontext.getDTM(child);
    short nodeType = dtm.getNodeType(child);
    boolean isDefaultTextRule = false;
    boolean isApplyImports = false;
    
    isApplyImports = ((xslInstruction == null)
                                ? false
                                : xslInstruction.getXSLToken()
                                  == Constants.ELEMNAME_APPLY_IMPORTS);        

    if (null == template || isApplyImports)
    {
      int maxImportLevel, endImportLevel=0;

      if (isApplyImports)
      {
        maxImportLevel =
          template.getStylesheetComposed().getImportCountComposed() - 1;
        endImportLevel =
          template.getStylesheetComposed().getEndImportCountComposed();
      }
      else
      {
        maxImportLevel = -1;
      }

      // If we're trying an xsl:apply-imports at the top level (ie there are no
      // imported stylesheets), we need to indicate that there is no matching template.
      // The above logic will calculate a maxImportLevel of -1 which indicates
      // that we should find any template.  This is because a value of -1 for
      // maxImportLevel has a special meaning.  But we don't want that.
      // We want to match -no- templates. See bugzilla bug 1170.
      if (isApplyImports && (maxImportLevel == -1))
      {
        template = null;
      }
      else
      {

        // Find the XSL template that is the best match for the 
        // element.        
        XPathContext xctxt = m_xcontext;

        try
        {
          xctxt.pushNamespaceContext(xslInstruction);

          QName mode = this.getMode();
          
          if (isApplyImports)
            template = m_stylesheetRoot.getTemplateComposed(xctxt, child, mode,
                  maxImportLevel, endImportLevel, m_quietConflictWarnings, dtm);
          else
            template = m_stylesheetRoot.getTemplateComposed(xctxt, child, mode,
                  m_quietConflictWarnings, dtm);
          
        }
        finally
        {
          xctxt.popNamespaceContext();
        }
      }

      // If that didn't locate a node, fall back to a default template rule.
      // See http://www.w3.org/TR/xslt#built-in-rule.
      if (null == template)
      {
        switch (nodeType)
        {
        case DTM.DOCUMENT_FRAGMENT_NODE :
        case DTM.ELEMENT_NODE :
          template = m_stylesheetRoot.getDefaultRule();
          break;
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
        case DTM.ATTRIBUTE_NODE :
          template = m_stylesheetRoot.getDefaultTextRule();
          isDefaultTextRule = true;
          break;
        case DTM.DOCUMENT_NODE :
          template = m_stylesheetRoot.getDefaultRootRule();
          break;
        default :

          // No default rules for processing instructions and the like.
          return false;
        }
      }
    }

    // If we are processing the default text rule, then just clone 
    // the value directly to the result tree.
    try
    {
      pushElemTemplateElement(template);
      m_xcontext.pushCurrentNode(child);
      pushPairCurrentMatched(template, child);
      
      // Fix copy copy29 test.
      if (!isApplyImports) {
          DTMIterator cnl = new org.apache.xpath.NodeSetDTM(child, m_xcontext.getDTMManager());
          m_xcontext.pushContextNodeList(cnl);
      }

      if (isDefaultTextRule)
      {
        switch (nodeType)
        {
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
          ClonerToResultTree.cloneToResultTree(child, nodeType, 
                                        dtm, getResultTreeHandler(), false);
          break;
        case DTM.ATTRIBUTE_NODE :
          dtm.dispatchCharactersEvents(child, getResultTreeHandler(), false);
          break;
        }
      }
      else
      {

        // And execute the child templates.
        // 9/11/00: If template has been compiled, hand off to it
        // since much (most? all?) of the processing has been inlined.
        // (It would be nice if there was a single entry point that
        // worked for both... but the interpretive system works by
        // having the Tranformer execute the children, while the
        // compiled obviously has to run its own code. It's
        // also unclear that "execute" is really the right name for
        // that entry point.)
        m_xcontext.setSAXLocator(template);
        // m_xcontext.getVarStack().link();
        m_xcontext.getVarStack().link(template.m_frameSize);
        executeChildTemplates(template, true);
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      if (!isDefaultTextRule)
        m_xcontext.getVarStack().unlink();
      m_xcontext.popCurrentNode();
      if (!isApplyImports) {
          m_xcontext.popContextNodeList();
      }
      popCurrentMatched();
      
      popElemTemplateElement();
    }

    return true;
  }
  
  
  /**
   * Execute each of the children of a template element.  This method
   * is only for extension use.
   *
   * @param elem The ElemTemplateElement that contains the children
   * that should execute.
   * NEEDSDOC @param context
   * @param mode The current mode.
   * @param handler The ContentHandler to where the result events
   * should be fed.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public void executeChildTemplates(
          ElemTemplateElement elem, org.w3c.dom.Node context, QName mode, ContentHandler handler)
            throws TransformerException
  {

    XPathContext xctxt = m_xcontext;

    try
    {
      if(null != mode)
        pushMode(mode);
      xctxt.pushCurrentNode(xctxt.getDTMHandleFromNode(context));
      executeChildTemplates(elem, handler);
    }
    finally
    {
      xctxt.popCurrentNode();
      
      // I'm not sure where or why this was here.  It is clearly in 
      // error though, without a corresponding pushMode().
      if (null != mode)
        popMode();
    }
  }

  /**
   * Execute each of the children of a template element.
   *
   * @param elem The ElemTemplateElement that contains the children
   * that should execute.
   * @param shouldAddAttrs true if xsl:attributes should be executed.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public void executeChildTemplates(
          ElemTemplateElement elem, boolean shouldAddAttrs)
            throws TransformerException
  {

    // Does this element have any children?
    ElemTemplateElement t = elem.getFirstChildElem();

    if (null == t)
      return;      
    
    if(elem.hasTextLitOnly() && m_optimizer)
    {      
      char[] chars = ((ElemTextLiteral)t).getChars();
      try
      {
        // Have to push stuff on for tooling...
        this.pushElemTemplateElement(t);
        m_serializationHandler.characters(chars, 0, chars.length);
      }
      catch(SAXException se)
      {
        throw new TransformerException(se);
      }
      finally
      {
        this.popElemTemplateElement();
      }
      return;
    }

//    // Check for infinite loops if we have to.
//    boolean check = (m_stackGuard.m_recursionLimit > -1);
//
//    if (check)
//      getStackGuard().push(elem, xctxt.getCurrentNode());

    XPathContext xctxt = m_xcontext;
    xctxt.pushSAXLocatorNull();
    int currentTemplateElementsTop = m_currentTemplateElements.size();
    m_currentTemplateElements.push(null);

    try
    {
      // Loop through the children of the template, calling execute on 
      // each of them.
      for (; t != null; t = t.getNextSiblingElem())
      {
        if (!shouldAddAttrs
                && t.getXSLToken() == Constants.ELEMNAME_ATTRIBUTE)
          continue;

        xctxt.setSAXLocator(t);
        m_currentTemplateElements.setElementAt(t,currentTemplateElementsTop);
        t.execute(this);
      }
    }
    catch(RuntimeException re)
    {
    	TransformerException te = new TransformerException(re);
    	te.setLocator(t);
    	throw te;
    }
    finally
    {
      m_currentTemplateElements.pop();
      xctxt.popSAXLocator();
    }

    // Check for infinite loops if we have to
//    if (check)
//      getStackGuard().pop();
  }
    /**
      * Execute each of the children of a template element.
      *
      * @param elem The ElemTemplateElement that contains the children
      * that should execute.
      * @param handler The ContentHandler to where the result events
      * should be fed.
      *
      * @throws TransformerException
      * @xsl.usage advanced
      */
     public void executeChildTemplates(
             ElemTemplateElement elem, ContentHandler handler)
               throws TransformerException
     {

       SerializationHandler xoh = this.getSerializationHandler();

       // These may well not be the same!  In this case when calling
       // the Redirect extension, it has already set the ContentHandler
       // in the Transformer.
       SerializationHandler savedHandler = xoh;

       try
       {
         xoh.flushPending();

         // %REVIEW% Make sure current node is being pushed.
         LexicalHandler lex = null;
         if (handler instanceof LexicalHandler) {
            lex = (LexicalHandler) handler;
         }
         m_serializationHandler = new ToXMLSAXHandler(handler, lex, savedHandler.getEncoding());
         m_serializationHandler.setTransformer(this);
         executeChildTemplates(elem, true);
       }
       catch (TransformerException e)
       {
         throw e;
       }
       catch (SAXException se) {
       	 throw new TransformerException(se);
       }
       finally
       {
         m_serializationHandler = savedHandler;
    }
  }

  /**
   * Get the keys for the xsl:sort elements.
   * Note: Should this go into ElemForEach?
   *
   * @param foreach Valid ElemForEach element, not null.
   * @param sourceNodeContext The current node context in the source tree,
   * needed to evaluate the Attribute Value Templates.
   *
   * @return A Vector of NodeSortKeys, or null.
   *
   * @throws TransformerException
   * @xsl.usage advanced
   */
  public Vector processSortKeys(ElemForEach foreach, int sourceNodeContext)
          throws TransformerException
  {

    Vector keys = null;
    XPathContext xctxt = m_xcontext;
    int nElems = foreach.getSortElemCount();

    if (nElems > 0)
      keys = new Vector();

    // March backwards, collecting the sort keys.
    for (int i = 0; i < nElems; i++)
    {
      ElemSort sort = foreach.getSortElem(i);
      
      String langString =
        (null != sort.getLang())
        ? sort.getLang().evaluate(xctxt, sourceNodeContext, foreach) : null;
      String dataTypeString = sort.getDataType().evaluate(xctxt,
                                sourceNodeContext, foreach);

      if (dataTypeString.indexOf(":") >= 0)
        System.out.println(
          "TODO: Need to write the hooks for QNAME sort data type");
      else if (!(dataTypeString.equalsIgnoreCase(Constants.ATTRVAL_DATATYPE_TEXT))
               &&!(dataTypeString.equalsIgnoreCase(
                 Constants.ATTRVAL_DATATYPE_NUMBER)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_DATATYPE,
                                    dataTypeString });

      boolean treatAsNumbers =
        ((null != dataTypeString) && dataTypeString.equals(
        Constants.ATTRVAL_DATATYPE_NUMBER)) ? true : false;
      String orderString = sort.getOrder().evaluate(xctxt, sourceNodeContext,
                             foreach);

      if (!(orderString.equalsIgnoreCase(Constants.ATTRVAL_ORDER_ASCENDING))
              &&!(orderString.equalsIgnoreCase(
                Constants.ATTRVAL_ORDER_DESCENDING)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_ORDER,
                                    orderString });

      boolean descending =
        ((null != orderString) && orderString.equals(
        Constants.ATTRVAL_ORDER_DESCENDING)) ? true : false;
      AVT caseOrder = sort.getCaseOrder();
      boolean caseOrderUpper;

      if (null != caseOrder)
      {
        String caseOrderString = caseOrder.evaluate(xctxt, sourceNodeContext,
                                                    foreach);

        if (!(caseOrderString.equalsIgnoreCase(Constants.ATTRVAL_CASEORDER_UPPER))
                &&!(caseOrderString.equalsIgnoreCase(
                  Constants.ATTRVAL_CASEORDER_LOWER)))
          foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                        new Object[]{ Constants.ATTRNAME_CASEORDER,
                                      caseOrderString });

        caseOrderUpper =
          ((null != caseOrderString) && caseOrderString.equals(
          Constants.ATTRVAL_CASEORDER_UPPER)) ? true : false;
      }
      else
      {
        caseOrderUpper = false;
      }

      keys.addElement(new NodeSortKey(this, sort.getSelect(), treatAsNumbers,
                                      descending, langString, caseOrderUpper,
                                      foreach));
     }

    return keys;
  }

  //==========================================================
  // SECTION: TransformState implementation
  //==========================================================
  
  /**
   * Get the count of how many elements are 
   * active.
   * @return The number of active elements on 
   * the currentTemplateElements stack.
   */
  public int getCurrentTemplateElementsCount()
  {
  	return m_currentTemplateElements.size();
  }
  
  
  /**
   * Get the count of how many elements are 
   * active.
   * @return The number of active elements on 
   * the currentTemplateElements stack.
   */
  public ObjectStack getCurrentTemplateElements()
  {
  	return m_currentTemplateElements;
  }

  /**
   * Push the current template element.
   *
   * @param elem The current ElemTemplateElement (may be null, and then
   * set via setCurrentElement).
   */
  public void pushElemTemplateElement(ElemTemplateElement elem)
  {
    m_currentTemplateElements.push(elem);
  }

  /**
   * Pop the current template element.
   */
  public void popElemTemplateElement()
  {
    m_currentTemplateElements.pop();
  }

  /**
   * Set the top of the current template elements
   * stack.
   *
   * @param e The current ElemTemplateElement about to
   * be executed.
   */
  public void setCurrentElement(ElemTemplateElement e)
  {
    m_currentTemplateElements.setTop(e);
  }

  /**
   * Retrieves the current ElemTemplateElement that is
   * being executed.
   *
   * @return The current ElemTemplateElement that is executing,
   * should not normally be null.
   */
  public ElemTemplateElement getCurrentElement()
  {
    return (m_currentTemplateElements.size() > 0) ? 
        (ElemTemplateElement) m_currentTemplateElements.peek() : null;
  }

  /**
   * This method retrieves the current context node
   * in the source tree.
   *
   * @return The current context node (should never be null?).
   */
  public int getCurrentNode()
  {
    return m_xcontext.getCurrentNode();
  }
  
  /**
   * This method retrieves the xsl:template
   * that is in effect, which may be a matched template
   * or a named template.
   *
   * <p>Please note that the ElemTemplate returned may
   * be a default template, and thus may not have a template
   * defined in the stylesheet.</p>
   *
   * @return The current xsl:template, should not be null.
   */
  public ElemTemplate getCurrentTemplate()
  {

    ElemTemplateElement elem = getCurrentElement();

    while ((null != elem)
           && (elem.getXSLToken() != Constants.ELEMNAME_TEMPLATE))
    {
      elem = elem.getParentElem();
    }

    return (ElemTemplate) elem;
  }

  /**
   * Push both the current xsl:template or xsl:for-each onto the
   * stack, along with the child node that was matched.
   * (Note: should this only be used for xsl:templates?? -sb)
   *
   * @param template xsl:template or xsl:for-each.
   * @param child The child that was matched.
   */
  public void pushPairCurrentMatched(ElemTemplateElement template, int child)
  {
    m_currentMatchTemplates.push(template);
    m_currentMatchedNodes.push(child);
  }

  /**
   * Pop the elements that were pushed via pushPairCurrentMatched.
   */
  public void popCurrentMatched()
  {
    m_currentMatchTemplates.pop();
    m_currentMatchedNodes.pop();
  }

  /**
   * This method retrieves the xsl:template
   * that was matched.  Note that this may not be
   * the same thing as the current template (which
   * may be from getCurrentElement()), since a named
   * template may be in effect.
   *
   * @return The pushed template that was pushed via pushPairCurrentMatched.
   */
  public ElemTemplate getMatchedTemplate()
  {
    return (ElemTemplate) m_currentMatchTemplates.peek();
  }

  /**
   * Retrieves the node in the source tree that matched
   * the template obtained via getMatchedTemplate().
   *
   * @return The matched node that corresponds to the
   * match attribute of the current xsl:template.
   */
  public int getMatchedNode()
  {
    return m_currentMatchedNodes.peepTail();
  }

  /**
   * Get the current context node list.
   *
   * @return A reset clone of the context node list.
   */
  public DTMIterator getContextNodeList()
  {

    try
    {
      DTMIterator cnl = m_xcontext.getContextNodeList();

      return (cnl == null) ? null : (DTMIterator) cnl.cloneWithReset();
    }
    catch (CloneNotSupportedException cnse)
    {

      // should never happen.
      return null;
    }
  }

  /**
   * Get the TrAX Transformer object in effect.
   *
   * @return This object.
   */
  public Transformer getTransformer()
  {
    return this;
  }

  //==========================================================
  // SECTION: Accessor Functions
  //==========================================================

  /**
   * Set the stylesheet for this processor.  If this is set, then the
   * process calls that take only the input .xml will use
   * this instead of looking for a stylesheet PI.  Also,
   * setting the stylesheet is needed if you are going
   * to use the processor as a SAX ContentHandler.
   *
   * @param stylesheetRoot A non-null StylesheetRoot object,
   * or null if you wish to clear the stylesheet reference.
   */
  public void setStylesheet(StylesheetRoot stylesheetRoot)
  {
    m_stylesheetRoot = stylesheetRoot;
  }

  /**
   * Get the current stylesheet for this processor.
   *
   * @return The stylesheet that is associated with this
   * transformer.
   */
  public final StylesheetRoot getStylesheet()
  {
    return m_stylesheetRoot;
  }

  /**
   * Get quietConflictWarnings property. If the quietConflictWarnings
   * property is set to true, warnings about pattern conflicts won't be
   * printed to the diagnostics stream.
   *
   * @return True if this transformer should not report
   * template match conflicts.
   */
  public boolean getQuietConflictWarnings()
  {
    return m_quietConflictWarnings;
  }

  /**
   * Set the execution context for XPath.
   *
   * @param xcontext A non-null reference to the XPathContext
   * associated with this transformer.
   * @xsl.usage internal
   */
  public void setXPathContext(XPathContext xcontext)
  {
    m_xcontext = xcontext;
  }

  /**
   * Get the XPath context associated with this transformer.
   *
   * @return The XPathContext reference, never null.
   */
  public final XPathContext getXPathContext()
  {
    return m_xcontext;
  }

  /**
   * Get the SerializationHandler object.
   *
   * @return The current SerializationHandler, which may not
   * be the main result tree manager.
   */
  public SerializationHandler getResultTreeHandler()
  {
    return m_serializationHandler;
  }

  /**
   * Get the SerializationHandler object.
   *
   * @return The current SerializationHandler, which may not
   * be the main result tree manager.
   */
  public SerializationHandler getSerializationHandler()
  {
    return m_serializationHandler;
  }
  
  /**
   * Get the KeyManager object.
   *
   * @return A reference to the KeyManager object, which should
   * never be null.
   */
  public KeyManager getKeyManager()
  {
    return m_keyManager;
  }

  /**
   * Check to see if this is a recursive attribute definition.
   *
   * @param attrSet A non-null ElemAttributeSet reference.
   *
   * @return true if the attribute set is recursive.
   */
  public boolean isRecursiveAttrSet(ElemAttributeSet attrSet)
  {

    if (null == m_attrSetStack)
    {
      m_attrSetStack = new Stack();
    }

    if (!m_attrSetStack.empty())
    {
      int loc = m_attrSetStack.search(attrSet);

      if (loc > -1)
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Push an executing attribute set, so we can check for
   * recursive attribute definitions.
   *
   * @param attrSet A non-null ElemAttributeSet reference.
   */
  public void pushElemAttributeSet(ElemAttributeSet attrSet)
  {
    m_attrSetStack.push(attrSet);
  }

  /**
   * Pop the current executing attribute set.
   */
  public void popElemAttributeSet()
  {
    m_attrSetStack.pop();
  }

  /**
   * Get the table of counters, for optimized xsl:number support.
   *
   * @return The CountersTable, never null.
   */
  public CountersTable getCountersTable()
  {

    if (null == m_countersTable)
      m_countersTable = new CountersTable();

    return m_countersTable;
  }

  /**
   * Tell if the current template rule is null, i.e. if we are
   * directly within an apply-templates.  Used for xsl:apply-imports.
   *
   * @return True if the current template rule is null.
   */
  public boolean currentTemplateRuleIsNull()
  {
    return ((!m_currentTemplateRuleIsNull.isEmpty())
            && (m_currentTemplateRuleIsNull.peek() == true));
  }

  /**
   * Push true if the current template rule is null, false
   * otherwise.
   *
   * @param b True if the we are executing an xsl:for-each
   * (or xsl:call-template?).
   */
  public void pushCurrentTemplateRuleIsNull(boolean b)
  {
    m_currentTemplateRuleIsNull.push(b);
  }

  /**
   * Push true if the current template rule is null, false
   * otherwise.
   */
  public void popCurrentTemplateRuleIsNull()
  {
    m_currentTemplateRuleIsNull.pop();
  }

  /**
   * Push a funcion result for the currently active EXSLT
   * <code>func:function</code>.
   * 
   * @param val the result of executing an EXSLT
   * <code>func:result</code> instruction for the current
   * <code>func:function</code>.
   */
  public void pushCurrentFuncResult(Object val) {
    m_currentFuncResult.push(val);
  }

  /**
   * Pops the result of the currently active EXSLT <code>func:function</code>.
   * 
   * @return the value of the <code>func:function</code>
   */
  public Object popCurrentFuncResult() {
    return m_currentFuncResult.pop();
  }

  /**
   * Determines whether an EXSLT <code>func:result</code> instruction has been
   * executed for the currently active EXSLT <code>func:function</code>.
   * 
   * @return <code>true</code> if and only if a <code>func:result</code>
   * instruction has been executed
   */
  public boolean currentFuncResultSeen() {
    return !m_currentFuncResult.empty()
               && m_currentFuncResult.peek() != null;
  }

  /**
   * Return the message manager.
   *
   * @return The message manager, never null.
   */
  public MsgMgr getMsgMgr()
  {

    if (null == m_msgMgr)
      m_msgMgr = new MsgMgr(this);

    return m_msgMgr;
  }

  /**
   * Set the error event listener.
   *
   * @param listener The new error listener.
   * @throws IllegalArgumentException if
   */
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {

    synchronized (m_reentryGuard)
    {
      if (listener == null)
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_ERROR_HANDLER, null)); //"Null error handler");

      m_errorHandler = listener;
    }
  }

  /**
   * Get the current error event handler.
   *
   * @return The current error handler, which should never be null.
   */
  public ErrorListener getErrorListener()
  {
    return m_errorHandler;
  }

  /**
   * Look up the value of a feature.
   *
   * <p>The feature name is any fully-qualified URI.  It is
   * possible for an TransformerFactory to recognize a feature name but
   * to be unable to return its value; this is especially true
   * in the case of an adapter for a SAX1 Parser, which has
   * no way of knowing whether the underlying parser is
   * validating, for example.</p>
   *
   * <h3>Open issues:</h3>
   * <dl>
   *    <dt><h4>Should getFeature be changed to hasFeature?</h4></dt>
   *    <dd>Keith Visco writes: Should getFeature be changed to hasFeature?
   *        It returns a boolean which indicated whether the "state"
   *        of feature is "true or false". I assume this means whether
   *        or not a feature is supported? I know SAX is using "getFeature",
   *        but to me "hasFeature" is cleaner.</dd>
   * </dl>
   *
   * @param name The feature name, which is a fully-qualified
   *        URI.
   * @return The current state of the feature (true or false).
   * @throws org.xml.sax.SAXNotRecognizedException When the
   *            TransformerFactory does not recognize the feature name.
   * @throws org.xml.sax.SAXNotSupportedException When the
   *            TransformerFactory recognizes the feature name but
   *            cannot determine its value at this time.
   *
   * @throws SAXNotRecognizedException
   * @throws SAXNotSupportedException
   */
  public boolean getFeature(String name)
          throws SAXNotRecognizedException, SAXNotSupportedException
  {

    if ("http://xml.org/trax/features/sax/input".equals(name))
      return true;
    else if ("http://xml.org/trax/features/dom/input".equals(name))
      return true;

    throw new SAXNotRecognizedException(name);
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method getMode 
   *
   *
   * NEEDSDOC (getMode) @return
   */
  public QName getMode()
  {
    return m_modes.isEmpty() ? null : (QName) m_modes.peek();
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method pushMode 
   *
   *
   * NEEDSDOC @param mode
   */
  public void pushMode(QName mode)
  {
    m_modes.push(mode);
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method popMode 
   *
   */
  public void popMode()
  {
    m_modes.pop();
  }

  /**
   * Called by SourceTreeHandler to start the transformation
   *  in a separate thread
   *
   * NEEDSDOC @param priority
   */
  public void runTransformThread(int priority)
  {

    // used in SourceTreeHandler
    Thread t = ThreadControllerWrapper.runThread(this, priority);
    this.setTransformThread(t);
  }

  /**
   * Called by this.transform() if isParserEventsOnMain()==false.
   *  Similar with runTransformThread(), but no priority is set
   *  and setTransformThread is not set.
   */
  public void runTransformThread()
  {
    ThreadControllerWrapper.runThread(this, -1);
  }
  
  /**
   * Called by CoRoutineSAXParser. Launches the CoroutineSAXParser
   * in a thread, and prepares it to invoke the parser from that thread
   * upon request. 
   *  
   */
  public static void runTransformThread(Runnable runnable)
  {
    ThreadControllerWrapper.runThread(runnable, -1);
  }

  /**
   * Used by SourceTreeHandler to wait until the transform
   *   completes
   *
   * @throws SAXException
   */
  public void waitTransformThread() throws SAXException
  {

    // This is called to make sure the task is done.
    // It is possible that the thread has been reused -
    // but for a different transformation. ( what if we 
    // recycle the transformer ? Not a problem since this is
    // still in use. )
    Thread transformThread = this.getTransformThread();

    if (null != transformThread)
    {
      try
      {
        ThreadControllerWrapper.waitThread(transformThread, this);

        if (!this.hasTransformThreadErrorCatcher())
        {
          Exception e = this.getExceptionThrown();

          if (null != e)
          {
            e.printStackTrace();
            throw new org.xml.sax.SAXException(e);
          }
        }

        this.setTransformThread(null);
      }
      catch (InterruptedException ie){}
    }
  }

  /**
   * Get the exception thrown by the secondary thread (normally
   * the transform thread).
   *
   * @return The thrown exception, or null if no exception was
   * thrown.
   */
  public Exception getExceptionThrown()
  {
    return m_exceptionThrown;
  }

  /**
   * Set the exception thrown by the secondary thread (normally
   * the transform thread).
   *
   * @param e The thrown exception, or null if no exception was
   * thrown.
   */
  public void setExceptionThrown(Exception e)
  {
    m_exceptionThrown = e;
  }

  /**
   * This is just a way to set the document for run().
   *
   * @param doc A non-null reference to the root of the
   * tree to be transformed.
   */
  public void setSourceTreeDocForThread(int doc)
  {
    m_doc = doc;
  }

  /**
   * From a secondary thread, post the exception, so that
   * it can be picked up from the main thread.
   *
   * @param e The exception that was thrown.
   */
  void postExceptionFromThread(Exception e)
  {

    // Commented out in response to problem reported by Nicola Brown <Nicola.Brown@jacobsrimell.com>
    //    if(m_reportInPostExceptionFromThread)
    //    {
    //      // Consider re-throwing the exception if this flag is set.
    //      e.printStackTrace();
    //    }
    // %REVIEW Need DTM equivelent?    
    //    if (m_inputContentHandler instanceof SourceTreeHandler)
    //    {
    //      SourceTreeHandler sth = (SourceTreeHandler) m_inputContentHandler;
    //
    //      sth.setExceptionThrown(e);
    //    }
 //   ContentHandler ch = getContentHandler();

    //    if(ch instanceof SourceTreeHandler)
    //    {
    //      SourceTreeHandler sth = (SourceTreeHandler) ch;
    //      ((TransformerImpl)(sth.getTransformer())).postExceptionFromThread(e);
    //    }
    // m_isTransformDone = true; // android-removed
    m_exceptionThrown = e;
    ;  // should have already been reported via the error handler?

    synchronized (this)
    {

      // See message from me on 3/27/2001 to Patrick Moore.
      //      String msg = e.getMessage();
      // System.out.println(e.getMessage());
      // Is this really needed?  -sb
      notifyAll();

      //      if (null == msg)
      //      {
      //
      //        // m_throwNewError = false;
      //        e.printStackTrace();
      //      }
      // throw new org.apache.xml.utils.WrappedRuntimeException(e);
    }
  }

  /**
   * Run the transform thread.
   */
  public void run()
  {

    m_hasBeenReset = false;

    try
    {

      // int n = ((SourceTreeHandler)getInputContentHandler()).getDTMRoot();
      // transformNode(n);
      try
      {
        // m_isTransformDone = false; // android-removed
        
        // Should no longer be needed...
//          if(m_inputContentHandler instanceof TransformerHandlerImpl)
//          {
//            TransformerHandlerImpl thi = (TransformerHandlerImpl)m_inputContentHandler;
//            thi.waitForInitialEvents();
//          }

        transformNode(m_doc);
        
      }
      catch (Exception e)
      {
        // e.printStackTrace();

        // Strange that the other catch won't catch this...
        if (null != m_transformThread)
          postExceptionFromThread(e);   // Assume we're on the main thread
        else 
          throw new RuntimeException(e.getMessage());
      }
      finally
      {
        // m_isTransformDone = true; // android-removed

        if (m_inputContentHandler instanceof TransformerHandlerImpl)
        {
          ((TransformerHandlerImpl) m_inputContentHandler).clearCoRoutine();
        }

        //        synchronized (this)
        //        {
        //          notifyAll();
        //        }
      }
    }
    catch (Exception e)
    {

      // e.printStackTrace();
      if (null != m_transformThread)
        postExceptionFromThread(e);
      else 
        throw new RuntimeException(e.getMessage());         // Assume we're on the main thread.
    }
  }

  // Fragment re-execution interfaces for a tool.

  /**
   * Test whether whitespace-only text nodes are visible in the logical
   * view of <code>DTM</code>. Normally, this function
   * will be called by the implementation of <code>DTM</code>;
   * it is not normally called directly from
   * user code.
   *
   * @param elementHandle int Handle of the element.
   * @return one of NOTSTRIP, STRIP, or INHERIT.
   */
  public short getShouldStripSpace(int elementHandle, DTM dtm)
  {

    try
    {
      org.apache.xalan.templates.WhiteSpaceInfo info =
        m_stylesheetRoot.getWhiteSpaceInfo(m_xcontext, elementHandle, dtm);

      if (null == info)
      {
        return DTMWSFilter.INHERIT;
      }
      else
      {

        // System.out.println("getShouldStripSpace: "+info.getShouldStripSpace());
        return info.getShouldStripSpace()
               ? DTMWSFilter.STRIP : DTMWSFilter.NOTSTRIP;
      }
    }
    catch (TransformerException se)
    {
      return DTMWSFilter.INHERIT;
    }
  }
  /**
   * Initializer method.
   *
   * @param transformer non-null transformer instance
   * @param realHandler Content Handler instance
   */
   public void init(ToXMLSAXHandler h,Transformer transformer, ContentHandler realHandler)
   {
      h.setTransformer(transformer);
      h.setContentHandler(realHandler);
   }
      
   public void setSerializationHandler(SerializationHandler xoh)
   {
      m_serializationHandler = xoh;
   }
   
   
     
	/**
	 * Fire off characters, cdate events.
	 * @see org.apache.xml.serializer.SerializerTrace#fireGenerateEvent(int, char[], int, int)
	 */
	public void fireGenerateEvent(
		int eventType,
		char[] ch,
		int start,
		int length) {
	}

	/**
	 * Fire off startElement, endElement events.
	 * @see org.apache.xml.serializer.SerializerTrace#fireGenerateEvent(int, String, Attributes)
	 */
	public void fireGenerateEvent(
		int eventType,
		String name,
		Attributes atts) {
	}

	/**
	 * Fire off processingInstruction events.
	 */
	public void fireGenerateEvent(int eventType, String name, String data) {
	}

	/**
	 * Fire off comment and entity ref events.
	 * @see org.apache.xml.serializer.SerializerTrace#fireGenerateEvent(int, String)
	 */
	public void fireGenerateEvent(int eventType, String data) {
	}

	/**
	 * Fire off startDocument, endDocument events.
	 * @see org.apache.xml.serializer.SerializerTrace#fireGenerateEvent(int)
	 */
	public void fireGenerateEvent(int eventType) {
	}

    /**
     * @see org.apache.xml.serializer.SerializerTrace#hasTraceListeners()
     */
    public boolean hasTraceListeners() {
        return false;
    }

    /**
     * @return Incremental flag
     */
    public boolean getIncremental() {
        return m_incremental;
    }

    /**
     * @return Optimization flag
     */
    public boolean getOptimize() {
        return m_optimizer;
    }

    /**
     * @return Source location flag
     */
    public boolean getSource_location() {
        return m_source_location;
    }

}  // end TransformerImpl class

