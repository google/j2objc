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
 * $Id: StylesheetHandler.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import java.util.Stack;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TemplatesHandler;

import org.apache.xalan.extensions.ExpressionVisitor;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemForEach;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.NamespaceSupport2;
import org.apache.xml.utils.NodeConsumer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.XMLCharacterRecognizer;
import org.apache.xpath.XPath;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.functions.Function;

import org.w3c.dom.Node;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Initializes and processes a stylesheet via SAX events.
 * This class acts as essentially a state machine, maintaining
 * a ContentHandler stack, and pushing appropriate content
 * handlers as parse events occur.
 * @xsl.usage advanced
 */
public class StylesheetHandler extends DefaultHandler
        implements TemplatesHandler, PrefixResolver, NodeConsumer
{


  /**
   * The function table of XPath and XSLT;
   */
  private FunctionTable m_funcTable = new FunctionTable();
  
  /**
   * The flag for the setting of the optimize feature;
   */
  private boolean m_optimize = true;
  
  /**
   * The flag for the setting of the incremental feature;
   */
  private boolean m_incremental = false;
  
  /**
   * The flag for the setting of the source_location feature;
   */
  private boolean m_source_location = false;
  
  /**
   * Create a StylesheetHandler object, creating a root stylesheet
   * as the target.
   *
   * @param processor non-null reference to the transformer factory that owns this handler.
   *
   * @throws TransformerConfigurationException if a StylesheetRoot
   * can not be constructed for some reason.
   */
  public StylesheetHandler(TransformerFactoryImpl processor)
          throws TransformerConfigurationException
  {
    Class func = org.apache.xalan.templates.FuncDocument.class;
    m_funcTable.installFunction("document", func);

    // func = new org.apache.xalan.templates.FuncKey();
    // FunctionTable.installFunction("key", func);
    func = org.apache.xalan.templates.FuncFormatNumb.class;

    m_funcTable.installFunction("format-number", func);

    m_optimize =((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_OPTIMIZE)).booleanValue();
    m_incremental = ((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_INCREMENTAL)).booleanValue();
    m_source_location = ((Boolean) processor.getAttribute(
            TransformerFactoryImpl.FEATURE_SOURCE_LOCATION)).booleanValue();
    // m_schema = new XSLTSchema();
    init(processor);
    
  }

  /**
   * Do common initialization.
   *
   * @param processor non-null reference to the transformer factory that owns this handler.
   */
  void init(TransformerFactoryImpl processor)
  {
    m_stylesheetProcessor = processor;

    // Set the initial content handler.
    m_processors.push(m_schema.getElementProcessor());
    this.pushNewNamespaceSupport();

    // m_includeStack.push(SystemIDResolver.getAbsoluteURI(this.getBaseIdentifier(), null));
    // initXPath(processor, null);
  }

  /**
   * Process an expression string into an XPath.
   * Must be public for access by the AVT class.
   *
   * @param str A non-null reference to a valid or invalid XPath expression string.
   *
   * @return A non-null reference to an XPath object that represents the string argument.
   *
   * @throws javax.xml.transform.TransformerException if the expression can not be processed.
   * @see <a href="http://www.w3.org/TR/xslt#section-Expressions">Section 4 Expressions in XSLT Specification</a>
   */
  public XPath createXPath(String str, ElemTemplateElement owningTemplate)
          throws javax.xml.transform.TransformerException
  {
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    XPath xpath = new XPath(str, owningTemplate, this, XPath.SELECT, handler, 
            m_funcTable);
    // Visit the expression, registering namespaces for any extension functions it includes.
    xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
    return xpath;
  }

  /**
   * Process an expression string into an XPath.
   *
   * @param str A non-null reference to a valid or invalid match pattern string.
   *
   * @return A non-null reference to an XPath object that represents the string argument.
   *
   * @throws javax.xml.transform.TransformerException if the pattern can not be processed.
   * @see <a href="http://www.w3.org/TR/xslt#patterns">Section 5.2 Patterns in XSLT Specification</a>
   */
  XPath createMatchPatternXPath(String str, ElemTemplateElement owningTemplate)
          throws javax.xml.transform.TransformerException
  {
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    XPath xpath = new XPath(str, owningTemplate, this, XPath.MATCH, handler, 
        m_funcTable);
    // Visit the expression, registering namespaces for any extension functions it includes.
    xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
    return xpath;    
  }

  /**
   * Given a namespace, get the corrisponding prefix from the current
   * namespace support context.
   *
   * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
   *
   * @return The associated Namespace URI, or null if the prefix
   *         is undeclared in this context.
   */
  public String getNamespaceForPrefix(String prefix)
  {
    return this.getNamespaceSupport().getURI(prefix);
  }

  /**
   * Given a namespace, get the corrisponding prefix.  This is here only
   * to support the {@link org.apache.xml.utils.PrefixResolver} interface,
   * and will throw an error if invoked on this object.
   *
   * @param prefix The prefix to look up, which may be an empty string ("") for the default Namespace.
   * @param context The node context from which to look up the URI.
   *
   * @return The associated Namespace URI, or null if the prefix
   *         is undeclared in this context.
   */
  public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context)
  {

    // Don't need to support this here.  Return the current URI for the prefix,
    // ignoring the context.
    assertion(true, "can't process a context node in StylesheetHandler!");

    return null;
  }

  /**
   * Utility function to see if the stack contains the given URL.
   *
   * @param stack non-null reference to a Stack.
   * @param url URL string on which an equality test will be performed.
   *
   * @return true if the stack contains the url argument.
   */
  private boolean stackContains(Stack stack, String url)
  {

    int n = stack.size();
    boolean contains = false;

    for (int i = 0; i < n; i++)
    {
      String url2 = (String) stack.elementAt(i);

      if (url2.equals(url))
      {
        contains = true;

        break;
      }
    }

    return contains;
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of the TRAX TemplatesBuilder interface.
  ////////////////////////////////////////////////////////////////////

  /**
   * When this object is used as a ContentHandler or ContentHandler, it will
   * create a Templates object, which the caller can get once
   * the SAX events have been completed.
   * @return The stylesheet object that was created during
   * the SAX event process, or null if no stylesheet has
   * been created.
   * 
   * Author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
   *
   *
   */
  public Templates getTemplates()
  {
    return getStylesheetRoot();
  }

  /**
   * Set the base ID (URL or system ID) for the stylesheet
   * created by this builder.  This must be set in order to
   * resolve relative URLs in the stylesheet.
   *
   * @param baseID Base URL for this stylesheet.
   */
  public void setSystemId(String baseID)
  {
    pushBaseIndentifier(baseID);
  }

  /**
   * Get the base ID (URI or system ID) from where relative
   * URLs will be resolved.
   *
   * @return The systemID that was set with {@link #setSystemId}.
   */
  public String getSystemId()
  {
    return this.getBaseIdentifier();
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of the EntityResolver interface.
  ////////////////////////////////////////////////////////////////////

  /**
   * Resolve an external entity.
   *
   * @param publicId The public identifer, or null if none is
   *                 available.
   * @param systemId The system identifier provided in the XML
   *                 document.
   * @return The new input source, or null to require the
   *         default behaviour.
   *
   * @throws org.xml.sax.SAXException if the entity can not be resolved.
   */
  public InputSource resolveEntity(String publicId, String systemId)
          throws org.xml.sax.SAXException
  {
    return getCurrentProcessor().resolveEntity(this, publicId, systemId);
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of DTDHandler interface.
  ////////////////////////////////////////////////////////////////////

  /**
   * Receive notification of a notation declaration.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass if they wish to keep track of the notations
   * declared in a document.</p>
   *
   * @param name The notation name.
   * @param publicId The notation public identifier, or null if not
   *                 available.
   * @param systemId The notation system identifier.
   * @see org.xml.sax.DTDHandler#notationDecl
   */
  public void notationDecl(String name, String publicId, String systemId)
  {
    getCurrentProcessor().notationDecl(this, name, publicId, systemId);
  }

  /**
   * Receive notification of an unparsed entity declaration.
   *
   * @param name The entity name.
   * @param publicId The entity public identifier, or null if not
   *                 available.
   * @param systemId The entity system identifier.
   * @param notationName The name of the associated notation.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void unparsedEntityDecl(String name, String publicId,
                                 String systemId, String notationName)
  {
    getCurrentProcessor().unparsedEntityDecl(this, name, publicId, systemId,
                                             notationName);
  }

  /**
   * Given a namespace URI, and a local name or a node type, get the processor
   * for the element, or return null if not allowed.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   *
   * @return A non-null reference to a element processor.
   *
   * @throws org.xml.sax.SAXException if the element is not allowed in the
   * found position in the stylesheet.
   */
  XSLTElementProcessor getProcessorFor(
          String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {

    XSLTElementProcessor currentProcessor = getCurrentProcessor();
    XSLTElementDef def = currentProcessor.getElemDef();
    XSLTElementProcessor elemProcessor = def.getProcessorFor(uri, localName);

    if (null == elemProcessor
            && !(currentProcessor instanceof ProcessorStylesheetDoc)
            && ((null == getStylesheet()
                || Double.valueOf(getStylesheet().getVersion()).doubleValue()
                   > Constants.XSLTVERSUPPORTED) 
                ||(!uri.equals(Constants.S_XSLNAMESPACEURL) &&
                            currentProcessor instanceof ProcessorStylesheetElement)
                || getElemVersion() > Constants.XSLTVERSUPPORTED
        ))
    {
      elemProcessor = def.getProcessorForUnknown(uri, localName);
    }

    if (null == elemProcessor)
      error(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_ALLOWED_IN_POSITION, new Object[]{rawName}),null);//rawName + " is not allowed in this position in the stylesheet!",
            
                
    return elemProcessor;
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of ContentHandler interface.
  ////////////////////////////////////////////////////////////////////

  /**
   * Receive a Locator object for document events.
   * This is called by the parser to push a locator for the
   * stylesheet being parsed. The stack needs to be popped
   * after the stylesheet has been parsed. We pop in
   * popStylesheet.
   *
   * @param locator A locator for all SAX document events.
   * @see org.xml.sax.ContentHandler#setDocumentLocator
   * @see org.xml.sax.Locator
   */
  public void setDocumentLocator(Locator locator)
  {

    // System.out.println("pushing locator for: "+locator.getSystemId());
    m_stylesheetLocatorStack.push(new SAXSourceLocator(locator));
  }

  /**
   * The level of the stylesheet we are at.
   */
  private int m_stylesheetLevel = -1;

  /**
   * Receive notification of the beginning of the document.
   *
   * @see org.xml.sax.ContentHandler#startDocument
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void startDocument() throws org.xml.sax.SAXException
  {
    m_stylesheetLevel++;
    pushSpaceHandling(false);
  }

  /** m_parsingComplete becomes true when the top-level stylesheet and all
   * its included/imported stylesheets have been been fully parsed, as an
   * indication that composition/optimization/compilation can begin.
   * @see isStylesheetParsingComplete  */
  private boolean m_parsingComplete = false;

  /**
   * Test whether the _last_ endDocument() has been processed.
   * This is needed as guidance for stylesheet optimization
   * and compilation engines, which generally don't want to start
   * until all included and imported stylesheets have been fully
   * parsed.
   *
   * @return true iff the complete stylesheet tree has been built.
   */
  public boolean isStylesheetParsingComplete()
  {
    return m_parsingComplete;
  }

  /**
   * Receive notification of the end of the document.
   *
   * @see org.xml.sax.ContentHandler#endDocument
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void endDocument() throws org.xml.sax.SAXException
  {

    try
    {
      if (null != getStylesheetRoot())
      {
        if (0 == m_stylesheetLevel)
          getStylesheetRoot().recompose();        
      }
      else
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_STYLESHEETROOT, null)); //"Did not find the stylesheet root!");

      XSLTElementProcessor elemProcessor = getCurrentProcessor();

      if (null != elemProcessor)
        elemProcessor.startNonText(this);

      m_stylesheetLevel--;			
      
      popSpaceHandling();

      // WARNING: This test works only as long as stylesheets are parsed
      // more or less recursively. If we switch to an iterative "work-list"
      // model, this will become true prematurely. In that case,
      // isStylesheetParsingComplete() will have to be adjusted to be aware
      // of the worklist.
      m_parsingComplete = (m_stylesheetLevel < 0);
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
  
  private java.util.Vector m_prefixMappings = new java.util.Vector();

  /**
   * Receive notification of the start of a Namespace mapping.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each element (such as allocating a new tree node or writing
   * output to a file).</p>
   *
   * @param prefix The Namespace prefix being declared.
   * @param uri The Namespace URI mapped to the prefix.
   * @see org.xml.sax.ContentHandler#startPrefixMapping
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void startPrefixMapping(String prefix, String uri)
          throws org.xml.sax.SAXException
  {

    // m_nsSupport.pushContext();
    // this.getNamespaceSupport().declarePrefix(prefix, uri);
    //m_prefixMappings.add(prefix); // JDK 1.2+ only -sc
    //m_prefixMappings.add(uri); // JDK 1.2+ only -sc
    m_prefixMappings.addElement(prefix); // JDK 1.1.x compat -sc
    m_prefixMappings.addElement(uri); // JDK 1.1.x compat -sc
  }

  /**
   * Receive notification of the end of a Namespace mapping.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each element (such as allocating a new tree node or writing
   * output to a file).</p>
   *
   * @param prefix The Namespace prefix being declared.
   * @see org.xml.sax.ContentHandler#endPrefixMapping
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException
  {

    // m_nsSupport.popContext();
  }

  /**
   * Flush the characters buffer.
   *
   * @throws org.xml.sax.SAXException
   */
  private void flushCharacters() throws org.xml.sax.SAXException
  {

    XSLTElementProcessor elemProcessor = getCurrentProcessor();

    if (null != elemProcessor)
      elemProcessor.startNonText(this);
  }

  /**
   * Receive notification of the start of an element.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param attributes The specified or defaulted attributes.
   *
   * @throws org.xml.sax.SAXException
   */
  public void startElement(
          String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    NamespaceSupport nssupport = this.getNamespaceSupport();
    nssupport.pushContext();
    
    int n = m_prefixMappings.size();

    for (int i = 0; i < n; i++) 
    {
      String prefix = (String)m_prefixMappings.elementAt(i++);
      String nsURI = (String)m_prefixMappings.elementAt(i);
      nssupport.declarePrefix(prefix, nsURI);
    }
    //m_prefixMappings.clear(); // JDK 1.2+ only -sc
    m_prefixMappings.removeAllElements(); // JDK 1.1.x compat -sc

    m_elementID++;

    // This check is currently done for all elements.  We should possibly consider
    // limiting this check to xsl:stylesheet elements only since that is all it really
    // applies to.  Also, it could be bypassed if m_shouldProcess is already true.
    // In other words, the next two statements could instead look something like this:
    // if (!m_shouldProcess)
    // {
    //   if (localName.equals(Constants.ELEMNAME_STYLESHEET_STRING) &&
    //       url.equals(Constants.S_XSLNAMESPACEURL))
    //   {
    //     checkForFragmentID(attributes);
    //     if (!m_shouldProcess)
    //       return;
    //   }
    //   else
    //     return;
    // } 
    // I didn't include this code statement at this time because in practice 
    // it is a small performance hit and I was waiting to see if its absence
    // caused a problem. - GLP

    checkForFragmentID(attributes);

    if (!m_shouldProcess)
      return;

    flushCharacters();
    
    pushSpaceHandling(attributes);

    XSLTElementProcessor elemProcessor = getProcessorFor(uri, localName,
                                           rawName);

    if(null != elemProcessor)  // defensive, for better multiple error reporting. -sb
    {
      this.pushProcessor(elemProcessor);
      elemProcessor.startElement(this, uri, localName, rawName, attributes);
    }
    else
    {
      m_shouldProcess = false;
      popSpaceHandling();
    }
                
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @see org.xml.sax.ContentHandler#endElement
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void endElement(String uri, String localName, String rawName)
          throws org.xml.sax.SAXException
  {

    m_elementID--;

    if (!m_shouldProcess)
      return;

    if ((m_elementID + 1) == m_fragmentID)
      m_shouldProcess = false;

    flushCharacters();
    
    popSpaceHandling();

    XSLTElementProcessor p = getCurrentProcessor();

    p.endElement(this, uri, localName, rawName);
    this.popProcessor();
    this.getNamespaceSupport().popContext();
  }

  /**
   * Receive notification of character data inside an element.
   *
   * @param ch The characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @see org.xml.sax.ContentHandler#characters
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void characters(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {

    if (!m_shouldProcess)
      return;

    XSLTElementProcessor elemProcessor = getCurrentProcessor();
    XSLTElementDef def = elemProcessor.getElemDef();

    if (def.getType() != XSLTElementDef.T_PCDATA)
      elemProcessor = def.getProcessorFor(null, "text()");

    if (null == elemProcessor)
    {

      // If it's whitespace, just ignore it, otherwise flag an error.
      if (!XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
        error(
          XSLMessages.createMessage(XSLTErrorResources.ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION, null),null);//"Non-whitespace text is not allowed in this position in the stylesheet!",
          
    }
    else
      elemProcessor.characters(this, ch, start, length);
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   *
   * @param ch The whitespace characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @see org.xml.sax.ContentHandler#ignorableWhitespace
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void ignorableWhitespace(char ch[], int start, int length)
          throws org.xml.sax.SAXException
  {

    if (!m_shouldProcess)
      return;

    getCurrentProcessor().ignorableWhitespace(this, ch, start, length);
  }

  /**
   * Receive notification of a processing instruction.
   *
   * <p>The Parser will invoke this method once for each processing
   * instruction found: note that processing instructions may occur
   * before or after the main document element.</p>
   *
   * <p>A SAX parser should never report an XML declaration (XML 1.0,
   * section 2.8) or a text declaration (XML 1.0, section 4.3.1)
   * using this method.</p>
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions for each
   * processing instruction, such as setting status variables or
   * invoking other methods.</p>
   *
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if
   *             none is supplied.
   * @see org.xml.sax.ContentHandler#processingInstruction
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void processingInstruction(String target, String data)
          throws org.xml.sax.SAXException
  {
    if (!m_shouldProcess)
      return;

    // Recreating Scott's kluge:
    // A xsl:for-each or xsl:apply-templates may have a special 
    // PI that tells us not to cache the document.  This PI 
    // should really be namespaced.
    //    String localName = getLocalName(target);
    //    String ns = m_stylesheet.getNamespaceFromStack(target);
    //
    // %REVIEW%: We need a better PI architecture
    
    String prefix="",ns="", localName=target;
    int colon=target.indexOf(':');
    if(colon>=0)
    {
      ns=getNamespaceForPrefix(prefix=target.substring(0,colon));
      localName=target.substring(colon+1);
    }

    try
    {
      // A xsl:for-each or xsl:apply-templates may have a special 
      // PI that tells us not to cache the document.  This PI 
      // should really be namespaced... but since the XML Namespaces
      // spec never defined namespaces as applying to PI's, and since
      // the testcase we're trying to support is inconsistant in whether
      // it binds the prefix, I'm going to make this sloppy for
      // testing purposes.
      if(
        "xalan-doc-cache-off".equals(target) ||
        "xalan:doc-cache-off".equals(target) ||
	   ("doc-cache-off".equals(localName) &&
	    ns.equals("org.apache.xalan.xslt.extensions.Redirect") )
	 )
      {
	if(!(m_elems.peek() instanceof ElemForEach))
          throw new TransformerException
	    ("xalan:doc-cache-off not allowed here!", 
	     getLocator());
        ElemForEach elem = (ElemForEach)m_elems.peek();

        elem.m_doc_cache_off = true;

	//System.out.println("JJK***** Recognized <? {"+ns+"}"+prefix+":"+localName+" "+data+"?>");
      }
    }
    catch(Exception e)
    {
      // JJK: Officially, unknown PIs can just be ignored.
      // Do we want to issue a warning?
    }


    flushCharacters();
    getCurrentProcessor().processingInstruction(this, target, data);
  }

  /**
   * Receive notification of a skipped entity.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions for each
   * processing instruction, such as setting status variables or
   * invoking other methods.</p>
   *
   * @param name The name of the skipped entity.
   * @see org.xml.sax.ContentHandler#processingInstruction
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void skippedEntity(String name) throws org.xml.sax.SAXException
  {

    if (!m_shouldProcess)
      return;

    getCurrentProcessor().skippedEntity(this, name);
  }

  /**
   * Warn the user of an problem.
   *
   * @param msg An key into the {@link org.apache.xalan.res.XSLTErrorResources}
   * table, that is one of the WG_ prefixed definitions.
   * @param args An array of arguments for the given warning.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#warning}
   * method chooses to flag this condition as an error.
   * @xsl.usage internal
   */
  public void warn(String msg, Object args[]) throws org.xml.sax.SAXException
  {

    String formattedMsg = XSLMessages.createWarning(msg, args);
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();

    try
    {
      if (null != handler)
        handler.warning(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Assert that a condition is true.  If it is not true, throw an error.
   *
   * @param condition false if an error should not be thrown, otherwise true.
   * @param msg Error message to be passed to the RuntimeException as an
   * argument.
   * @throws RuntimeException if the condition is not true.
   * @xsl.usage internal
   */
  private void assertion(boolean condition, String msg) throws RuntimeException
  {
    if (!condition)
      throw new RuntimeException(msg);
  }

  /**
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * @param msg An error message.
   * @param e An error which the SAXException should wrap.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#error}
   * method chooses to flag this condition as an error.
   * @xsl.usage internal
   */
  protected void error(String msg, Exception e)
          throws org.xml.sax.SAXException
  {

    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();
    TransformerException pe;

    if (!(e instanceof TransformerException))
    {
      pe = (null == e)
           ? new TransformerException(msg, locator)
           : new TransformerException(msg, locator, e);
    }
    else
      pe = (TransformerException) e;

    if (null != handler)
    {
      try
      {
        handler.error(pe);
      }
      catch (TransformerException te)
      {
        throw new org.xml.sax.SAXException(te);
      }
    }
    else
      throw new org.xml.sax.SAXException(pe);
  }

  /**
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * @param msg A key into the {@link org.apache.xalan.res.XSLTErrorResources}
   * table, that is one of the WG_ prefixed definitions.
   * @param args An array of arguments for the given warning.
   * @param e An error which the SAXException should wrap.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#error}
   * method chooses to flag this condition as an error.
   * @xsl.usage internal
   */
  protected void error(String msg, Object args[], Exception e)
          throws org.xml.sax.SAXException
  {

    String formattedMsg = XSLMessages.createMessage(msg, args);

    error(formattedMsg, e);
  }

  /**
   * Receive notification of a XSLT processing warning.
   *
   * @param e The warning information encoded as an exception.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#warning}
   * method chooses to flag this condition as an error.
   */
  public void warning(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {

    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();

    try
    {
      handler.warning(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Receive notification of a recoverable XSLT processing error.
   *
   * @param e The error information encoded as an exception.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#error}
   * method chooses to flag this condition as an error.
   */
  public void error(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {

    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();

    try
    {
      handler.error(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Report a fatal XSLT processing error.
   *
   * @param e The error information encoded as an exception.
   *
   * @throws org.xml.sax.SAXException that wraps a
   * {@link javax.xml.transform.TransformerException} if the current
   * {@link javax.xml.transform.ErrorListener#fatalError}
   * method chooses to flag this condition as an error.
   */
  public void fatalError(org.xml.sax.SAXParseException e)
          throws org.xml.sax.SAXException
  {

    String formattedMsg = e.getMessage();
    SAXSourceLocator locator = getLocator();
    ErrorListener handler = m_stylesheetProcessor.getErrorListener();

    try
    {
      handler.fatalError(new TransformerException(formattedMsg, locator));
    }
    catch (TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * If we have a URL to a XML fragment, this is set
   * to false until the ID is found.
   * (warning: I worry that this should be in a stack).
   */
  private boolean m_shouldProcess = true;

  /**
   * If we have a URL to a XML fragment, the value is stored
   * in this string, and the m_shouldProcess flag is set to
   * false until we match an ID with this string.
   * (warning: I worry that this should be in a stack).
   */
  private String m_fragmentIDString;

  /**
   * Keep track of the elementID, so we can tell when
   * is has completed.  This isn't a real ID, but rather
   * a nesting level.  However, it's good enough for
   * our purposes.
   * (warning: I worry that this should be in a stack).
   */
  private int m_elementID = 0;

  /**
   * The ID of the fragment that has been found
   * (warning: I worry that this should be in a stack).
   */
  private int m_fragmentID = 0;

  /**
   * Check to see if an ID attribute matched the #id, called
   * from startElement.
   *
   * @param attributes The specified or defaulted attributes.
   */
  private void checkForFragmentID(Attributes attributes)
  {

    if (!m_shouldProcess)
    {
      if ((null != attributes) && (null != m_fragmentIDString))
      {
        int n = attributes.getLength();

        for (int i = 0; i < n; i++)
        {
          String name = attributes.getQName(i);

          if (name.equals(Constants.ATTRNAME_ID))
          {
            String val = attributes.getValue(i);

            if (val.equalsIgnoreCase(m_fragmentIDString))
            {
              m_shouldProcess = true;
              m_fragmentID = m_elementID;
            }
          }
        }
      }
    }
  }

  /**
   *  The XSLT TransformerFactory for needed services.
   */
  private TransformerFactoryImpl m_stylesheetProcessor;

  /**
   * Get the XSLT TransformerFactoryImpl for needed services.
   * TODO: This method should be renamed.
   *
   * @return The TransformerFactoryImpl that owns this handler.
   */
  public TransformerFactoryImpl getStylesheetProcessor()
  {
    return m_stylesheetProcessor;
  }

  /**
   * If getStylesheetType returns this value, the current stylesheet
   *  is a root stylesheet.
   * @xsl.usage internal
   */
  public static final int STYPE_ROOT = 1;

  /**
   * If getStylesheetType returns this value, the current stylesheet
   *  is an included stylesheet.
   * @xsl.usage internal
   */
  public static final int STYPE_INCLUDE = 2;

  /**
   * If getStylesheetType returns this value, the current stylesheet
   *  is an imported stylesheet.
   * @xsl.usage internal
   */
  public static final int STYPE_IMPORT = 3;

  /** The current stylesheet type. */
  private int m_stylesheetType = STYPE_ROOT;

  /**
   * Get the type of stylesheet that should be built
   * or is being processed.
   *
   * @return one of STYPE_ROOT, STYPE_INCLUDE, or STYPE_IMPORT.
   */
  int getStylesheetType()
  {
    return m_stylesheetType;
  }

  /**
   * Set the type of stylesheet that should be built
   * or is being processed.
   *
   * @param type Must be one of STYPE_ROOT, STYPE_INCLUDE, or STYPE_IMPORT.
   */
  void setStylesheetType(int type)
  {
    m_stylesheetType = type;
  }

  /**
   * The stack of stylesheets being processed.
   */
  private Stack m_stylesheets = new Stack();

  /**
   * Return the stylesheet that this handler is constructing.
   *
   * @return The current stylesheet that is on top of the stylesheets stack,
   *  or null if no stylesheet is on the stylesheets stack.
   */
  Stylesheet getStylesheet()
  {
    return (m_stylesheets.size() == 0)
           ? null : (Stylesheet) m_stylesheets.peek();
  }

  /**
   * Return the last stylesheet that was popped off the stylesheets stack.
   *
   * @return The last popped stylesheet, or null.
   */
  Stylesheet getLastPoppedStylesheet()
  {
    return m_lastPoppedStylesheet;
  }

  /**
   * Return the stylesheet root that this handler is constructing.
   *
   * @return The root stylesheet of the stylesheets tree.
   */
  public StylesheetRoot getStylesheetRoot()
  {
    if (m_stylesheetRoot != null){
        m_stylesheetRoot.setOptimizer(m_optimize);
        m_stylesheetRoot.setIncremental(m_incremental);
        m_stylesheetRoot.setSource_location(m_source_location);  		
    }
    return m_stylesheetRoot;
  }

  /** The root stylesheet of the stylesheets tree. */
  StylesheetRoot m_stylesheetRoot;
        
        /** The last stylesheet that was popped off the stylesheets stack. */
  Stylesheet m_lastPoppedStylesheet;

  /**
   * Push the current stylesheet being constructed. If no other stylesheets
   * have been pushed onto the stack, assume the argument is a stylesheet
   * root, and also set the stylesheet root member.
   *
   * @param s non-null reference to a stylesheet.
   */
  public void pushStylesheet(Stylesheet s)
  {

    if (m_stylesheets.size() == 0)
      m_stylesheetRoot = (StylesheetRoot) s;

    m_stylesheets.push(s);
  }

  /**
   * Pop the last stylesheet pushed, and return the stylesheet that this
   * handler is constructing, and set the last popped stylesheet member.
   * Also pop the stylesheet locator stack.
   *
   * @return The stylesheet popped off the stack, or the last popped stylesheet.
   */
  Stylesheet popStylesheet()
  {

    // The stylesheetLocatorStack needs to be popped because
    // a locator was pushed in for this stylesheet by the SAXparser by calling
    // setDocumentLocator().
    if (!m_stylesheetLocatorStack.isEmpty())
      m_stylesheetLocatorStack.pop();

    if (!m_stylesheets.isEmpty())
      m_lastPoppedStylesheet = (Stylesheet) m_stylesheets.pop();

    // Shouldn't this be null if stylesheets is empty?  -sb
    return m_lastPoppedStylesheet;
  }

  /**
   * The stack of current processors.
   */
  private Stack m_processors = new Stack();

  /**
   * Get the current XSLTElementProcessor at the top of the stack.
   *
   * @return Valid XSLTElementProcessor, which should never be null.
   */
  XSLTElementProcessor getCurrentProcessor()
  {
    return (XSLTElementProcessor) m_processors.peek();
  }

  /**
   * Push the current XSLTElementProcessor onto the top of the stack.
   *
   * @param processor non-null reference to the current element processor.
   */
  void pushProcessor(XSLTElementProcessor processor)
  {
    m_processors.push(processor);
  }

  /**
   * Pop the current XSLTElementProcessor from the top of the stack.
   * @return the XSLTElementProcessor which was popped.
   */
  XSLTElementProcessor popProcessor()
  {
    return (XSLTElementProcessor) m_processors.pop();
  }

  /**
   * The root of the XSLT Schema, which tells us how to
   * transition content handlers, create elements, etc.
   * For the moment at least, this can't be static, since
   * the processors store state.
   */
  private XSLTSchema m_schema = new XSLTSchema();

  /**
   * Get the root of the XSLT Schema, which tells us how to
   * transition content handlers, create elements, etc.
   *
   * @return The root XSLT Schema, which should never be null.
   * @xsl.usage internal
   */
  public XSLTSchema getSchema()
  {
    return m_schema;
  }

  /**
   * The stack of elements, pushed and popped as events occur.
   */
  private Stack m_elems = new Stack();

  /**
   * Get the current ElemTemplateElement at the top of the stack.
   * @return Valid ElemTemplateElement, which may be null.
   */
  ElemTemplateElement getElemTemplateElement()
  {

    try
    {
      return (ElemTemplateElement) m_elems.peek();
    }
    catch (java.util.EmptyStackException ese)
    {
      return null;
    }
  }  

  /** An increasing number that is used to indicate the order in which this element
   *  was encountered during the parse of the XSLT tree.
   */
  private int m_docOrderCount = 0;

  /**
   * Returns the next m_docOrderCount number and increments the number for future use.
   */
  int nextUid()
  {
    return m_docOrderCount++;
  }

  /**
   * Push the current XSLTElementProcessor to the top of the stack.  As a
   * side-effect, set the document order index (simply because this is a
   * convenient place to set it).
   *
   * @param elem Should be a non-null reference to the intended current
   * template element.
   */
  void pushElemTemplateElement(ElemTemplateElement elem)
  {

    if (elem.getUid() == -1)
      elem.setUid(nextUid());

    m_elems.push(elem);
  }

  /**
   * Get the current XSLTElementProcessor from the top of the stack.
   * @return the ElemTemplateElement which was popped.
   */
  ElemTemplateElement popElemTemplateElement()
  {
    return (ElemTemplateElement) m_elems.pop();
  }

  /**
   * This will act as a stack to keep track of the
   * current include base.
   */
  Stack m_baseIdentifiers = new Stack();

  /**
   * Push a base identifier onto the base URI stack.
   *
   * @param baseID The current base identifier for this position in the
   * stylesheet, which may be a fragment identifier, or which may be null.
   * @see <a href="http://www.w3.org/TR/xslt#base-uri">
   * Section 3.2 Base URI of XSLT specification.</a>
   */
  void pushBaseIndentifier(String baseID)
  {

    if (null != baseID)
    {
      int posOfHash = baseID.indexOf('#');

      if (posOfHash > -1)
      {
        m_fragmentIDString = baseID.substring(posOfHash + 1);
        m_shouldProcess = false;
      }
      else
        m_shouldProcess = true;
    }
    else
      m_shouldProcess = true;

    m_baseIdentifiers.push(baseID);
  }

  /**
   * Pop a base URI from the stack.
   * @return baseIdentifier.
   */
  String popBaseIndentifier()
  {
    return (String) m_baseIdentifiers.pop();
  }

  /**
   * Return the base identifier.
   *
   * @return The base identifier of the current stylesheet.
   */
  public String getBaseIdentifier()
  {

    // Try to get the baseIdentifier from the baseIdentifier's stack,
    // which may not be the same thing as the value found in the
    // SourceLocators stack.
    String base = (String) (m_baseIdentifiers.isEmpty()
                            ? null : m_baseIdentifiers.peek());

    // Otherwise try the stylesheet.
    if (null == base)
    {
      SourceLocator locator = getLocator();

      base = (null == locator) ? "" : locator.getSystemId();
    }

    return base;
  }

  /**
   * The top of this stack should contain the currently processed
   * stylesheet SAX locator object.
   */
  private Stack m_stylesheetLocatorStack = new Stack();

  /**
   * Get the current stylesheet Locator object.
   *
   * @return non-null reference to the current locator object.
   */
  public SAXSourceLocator getLocator()
  {

    if (m_stylesheetLocatorStack.isEmpty())
    {
      SAXSourceLocator locator = new SAXSourceLocator();

      locator.setSystemId(this.getStylesheetProcessor().getDOMsystemID());

      return locator;

      // m_stylesheetLocatorStack.push(locator);
    }

    return ((SAXSourceLocator) m_stylesheetLocatorStack.peek());
  }

  /**
   * A stack of URL hrefs for imported stylesheets.  This is
   * used to diagnose circular imports.
   */
  private Stack m_importStack = new Stack();
  
  /**
   * A stack of Source objects obtained from a URIResolver,
   * for each element in this stack there is a 1-1 correspondence
   * with an element in the m_importStack.
   */
  private Stack m_importSourceStack = new Stack();

  /**
   * Push an import href onto the stylesheet stack.
   *
   * @param hrefUrl non-null reference to the URL for the current imported
   * stylesheet.
   */
  void pushImportURL(String hrefUrl)
  {
    m_importStack.push(hrefUrl);
  }
  
  /**
   * Push the Source of an import href onto the stylesheet stack,
   * obtained from a URIResolver, null if there is no URIResolver,
   * or if that resolver returned null.
   */
  void pushImportSource(Source sourceFromURIResolver)
  {
    m_importSourceStack.push(sourceFromURIResolver);
  }

  /**
   * See if the imported stylesheet stack already contains
   * the given URL.  Used to test for recursive imports.
   *
   * @param hrefUrl non-null reference to a URL string.
   *
   * @return true if the URL is on the import stack.
   */
  boolean importStackContains(String hrefUrl)
  {
    return stackContains(m_importStack, hrefUrl);
  }

  /**
   * Pop an import href from the stylesheet stack.
   *
   * @return non-null reference to the import URL that was popped.
   */
  String popImportURL()
  {
    return (String) m_importStack.pop();
  }
  
  String peekImportURL()
  {
    return (String) m_importStack.peek();
  }
  
  Source peekSourceFromURIResolver()
  {
    return (Source) m_importSourceStack.peek();
  }
  
  /**
   * Pop a Source from a user provided URIResolver, corresponding
   * to the URL popped from the m_importStack.
   */
  Source popImportSource()
  {
    return (Source) m_importSourceStack.pop();
  }

  /**
   * If this is set to true, we've already warned about using the
   * older XSLT namespace URL.
   */
  private boolean warnedAboutOldXSLTNamespace = false;

  /** Stack of NamespaceSupport objects. */
  Stack m_nsSupportStack = new Stack();

  /**
   * Push a new NamespaceSupport instance.
   */
  void pushNewNamespaceSupport()
  {
    m_nsSupportStack.push(new NamespaceSupport2());
  }

  /**
   * Pop the current NamespaceSupport object.
   *
   */
  void popNamespaceSupport()
  {
    m_nsSupportStack.pop();
  }

  /**
   * Get the current NamespaceSupport object.
   *
   * @return a non-null reference to the current NamespaceSupport object,
   * which is the top of the namespace support stack.
   */
  NamespaceSupport getNamespaceSupport()
  {
    return (NamespaceSupport) m_nsSupportStack.peek();
  }

  /**
   * The originating node if the current stylesheet is being created
   *  from a DOM.
   *  @see org.apache.xml.utils.NodeConsumer
   */
  private Node m_originatingNode;

  /**
   * Set the node that is originating the SAX event.
   *
   * @param n Reference to node that originated the current event.
   * @see org.apache.xml.utils.NodeConsumer
   */
  public void setOriginatingNode(Node n)
  {
    m_originatingNode = n;
  }

  /**
   * Set the node that is originating the SAX event.
   *
   * @return Reference to node that originated the current event.
   * @see org.apache.xml.utils.NodeConsumer
   */
  public Node getOriginatingNode()
  {
    return m_originatingNode;
  }
  
  /**
   * Stack of booleans that are pushed and popped in start/endElement depending 
   * on the value of xml:space=default/preserve.
   */
  private BoolStack m_spacePreserveStack = new BoolStack();
  
  /**
   * Return boolean value from the spacePreserve stack depending on the value 
   * of xml:space=default/preserve.
   * 
   * @return true if space should be preserved, false otherwise.
   */
  boolean isSpacePreserve()
  {
    return m_spacePreserveStack.peek();
  }
  
  /**
   * Pop boolean value from the spacePreserve stack.
   */
  void popSpaceHandling()
  {
    m_spacePreserveStack.pop();
  }
  
  /**
   * Push boolean value on to the spacePreserve stack.
   * 
   * @param b true if space should be preserved, false otherwise.
   */
  void pushSpaceHandling(boolean b)
    throws org.xml.sax.SAXParseException
  {
    m_spacePreserveStack.push(b);
  }
  
  /**
   * Push boolean value on to the spacePreserve stack depending on the value 
   * of xml:space=default/preserve.
   * 
   * @param attrs list of attributes that were passed to startElement.
   */
  void pushSpaceHandling(Attributes attrs)
    throws org.xml.sax.SAXParseException
  {    
    String value = attrs.getValue("xml:space");
    if(null == value)
    {
      m_spacePreserveStack.push(m_spacePreserveStack.peekOrFalse());
    }
    else if(value.equals("preserve"))
    {
      m_spacePreserveStack.push(true);
    }
    else if(value.equals("default"))
    {
      m_spacePreserveStack.push(false);
    }
    else
    {
      SAXSourceLocator locator = getLocator();
      ErrorListener handler = m_stylesheetProcessor.getErrorListener();
  
      try
      {
        handler.error(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_XMLSPACE_VALUE, null), locator)); //"Illegal value for xml:space", locator));
      }
      catch (TransformerException te)
      {
        throw new org.xml.sax.SAXParseException(te.getMessage(), locator, te);
      }
      m_spacePreserveStack.push(m_spacePreserveStack.peek());
    }
  }
  
  private double getElemVersion()
  {
    ElemTemplateElement elem = getElemTemplateElement();
    double version = -1; 
    while ((version == -1 || version == Constants.XSLTVERSUPPORTED) && elem != null)
    {
      try{
      version = Double.valueOf(elem.getXmlVersion()).doubleValue();
      }
      catch (Exception ex)
      {
        version = -1;
      }
      elem = elem.getParentElem();
      }
    return (version == -1)? Constants.XSLTVERSUPPORTED : version;
  }
    /**
     * @see PrefixResolver#handlesNullPrefixes()
     */
    public boolean handlesNullPrefixes() {
        return false;
    }

    /**
     * @return Optimization flag
     */
    public boolean getOptimize() {
        return m_optimize;
    }

    /**
     * @return Incremental flag
     */
    public boolean getIncremental() {
        return m_incremental;
    }

    /**
     * @return Source Location flag
     */
    public boolean getSource_location() {
        return m_source_location;
    }

}



