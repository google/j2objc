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
 * $Id: ToStream.java 475894 2006-11-16 19:43:59Z minchau $
 */
package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This abstract class is a base class for other stream 
 * serializers (xml, html, text ...) that write output to a stream.
 * 
 * @xsl.usage internal
 */
abstract public class ToStream extends SerializerBase
{

    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";

    /** Stack to keep track of disabling output escaping. */
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();


    /**
     * The encoding information associated with this serializer.
     * Although initially there is no encoding,
     * there is a dummy EncodingInfo object that will say
     * that every character is in the encoding. This is useful
     * for a serializer that is in temporary output state and has
     * no associated encoding. A serializer in final output state
     * will have an encoding, and will worry about whether 
     * single chars or surrogate pairs of high/low chars form
     * characters in the output encoding. 
     */
    EncodingInfo m_encodingInfo = new EncodingInfo(null,null, '\u0000');
    
    /**
     * Stack to keep track of whether or not we need to
     * preserve whitespace.
     * 
     * Used to push/pop values used for the field m_ispreserve, but
     * m_ispreserve is only relevant if m_doIndent is true.
     * If m_doIndent is false this field has no impact.
     * 
     */
    protected BoolStack m_preserves = new BoolStack();

    /**
     * State flag to tell if preservation of whitespace
     * is important. 
     * 
     * Used only in shouldIndent() but only if m_doIndent is true.
     * If m_doIndent is false this flag has no impact.
     * 
     */
    protected boolean m_ispreserve = false;

    /**
     * State flag that tells if the previous node processed
     * was text, so we can tell if we should preserve whitespace.
     * 
     * Used in endDocument() and shouldIndent() but
     * only if m_doIndent is true. 
     * If m_doIndent is false this flag has no impact.
     */
    protected boolean m_isprevtext = false;
        
    private static final char[] s_systemLineSep;
    static {
        SecuritySupport ss = SecuritySupport.getInstance();
        s_systemLineSep = ss.getSystemProperty("line.separator").toCharArray();
    }
    
    /**
     * The system line separator for writing out line breaks.
     * The default value is from the system property,
     * but this value can be set through the xsl:output
     * extension attribute xalan:line-separator.
     */
    protected char[] m_lineSep = s_systemLineSep;
        
        
    /**
     * True if the the system line separator is to be used.
     */    
    protected boolean m_lineSepUse = true;    

    /**
     * The length of the line seperator, since the write is done
     * one character at a time.
     */
    protected int m_lineSepLen = m_lineSep.length;

    /**
     * Map that tells which characters should have special treatment, and it
     *  provides character to entity name lookup.
     */
    protected CharInfo m_charInfo;

    /** True if we control the buffer, and we should flush the output on endDocument. */
    boolean m_shouldFlush = true;

    /**
     * Add space before '/>' for XHTML.
     */
    protected boolean m_spaceBeforeClose = false;

    /**
     * Flag to signal that a newline should be added.
     * 
     * Used only in indent() which is called only if m_doIndent is true.
     * If m_doIndent is false this flag has no impact.
     */
    boolean m_startNewLine;

    /**
     * Tells if we're in an internal document type subset.
     */
    protected boolean m_inDoctype = false;

    /**
       * Flag to quickly tell if the encoding is UTF8.
       */
    boolean m_isUTF8 = false;


    /**
     * remembers if we are in between the startCDATA() and endCDATA() callbacks
     */
    protected boolean m_cdataStartCalled = false;
    
    /**
     * If this flag is true DTD entity references are not left as-is,
     * which is exiting older behavior.
     */
    private boolean m_expandDTDEntities = true;
  

    /**
     * Default constructor
     */
    public ToStream()
    {
    }

    /**
     * This helper method to writes out "]]>" when closing a CDATA section.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void closeCDATA() throws org.xml.sax.SAXException
    {
        try
        {
            m_writer.write(CDATA_DELIMITER_CLOSE);
            // write out a CDATA section closing "]]>"
            m_cdataTagOpen = false; // Remember that we have done so.
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Serializes the DOM node. Throws an exception only if an I/O
     * exception occurred while serializing.
     *
     * @param node Node to serialize.
     * @throws IOException An I/O exception occured while serializing
     */
    public void serialize(Node node) throws IOException
    {

        try
        {
            TreeWalker walker =
                new TreeWalker(this);

            walker.traverse(node);
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new WrappedRuntimeException(se);
        }
    }

    /**
     * Taken from XSLTC 
     */
    protected boolean m_escaping = true;

    /**
     * Flush the formatter's result stream.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void flushWriter() throws org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        if (null != writer)
        {
            try
            {
                if (writer instanceof WriterToUTF8Buffered)
                {
                    if (m_shouldFlush)
                         ((WriterToUTF8Buffered) writer).flush();
                    else
                         ((WriterToUTF8Buffered) writer).flushBuffer();
                }
                if (writer instanceof WriterToASCI)
                {
                    if (m_shouldFlush)
                        writer.flush();
                }
                else
                {
                    // Flush always. 
                    // Not a great thing if the writer was created 
                    // by this class, but don't have a choice.
                    writer.flush();
                }
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(ioe);
            }
        }
    }

    OutputStream m_outputStream;
    /**
     * Get the output stream where the events will be serialized to.
     *
     * @return reference to the result stream, or null of only a writer was
     * set.
     */
    public OutputStream getOutputStream()
    {
        return m_outputStream;
    }

    // Implement DeclHandler

    /**
     *   Report an element type declaration.
     *  
     *   <p>The content model will consist of the string "EMPTY", the
     *   string "ANY", or a parenthesised group, optionally followed
     *   by an occurrence indicator.  The model will be normalized so
     *   that all whitespace is removed,and will include the enclosing
     *   parentheses.</p>
     *  
     *   @param name The element type name.
     *   @param model The content model as a normalized string.
     *   @exception SAXException The application may raise an exception.
     */
    public void elementDecl(String name, String model) throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();

            writer.write("<!ELEMENT ");
            writer.write(name);
            writer.write(' ');
            writer.write(model);
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Report an internal entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param value The replacement text of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #externalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            DTDprolog();
            outputEntityDecl(name, value);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param value
     *
     * @throws org.xml.sax.SAXException
     */
    void outputEntityDecl(String name, String value) throws IOException
    {
        final java.io.Writer writer = m_writer;
        writer.write("<!ENTITY ");
        writer.write(name);
        writer.write(" \"");
        writer.write(value);
        writer.write("\">");
        writer.write(m_lineSep, 0, m_lineSepLen);
    }

    /**
     * Output a system-dependent line break.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void outputLineSep() throws IOException
    {

        m_writer.write(m_lineSep, 0, m_lineSepLen);
    }

    void setProp(String name, String val, boolean defaultVal) {
        if (val != null) {


            char first = getFirstCharLocName(name);
            switch (first) {
            case 'c':
                if (OutputKeys.CDATA_SECTION_ELEMENTS.equals(name)) {
                    String cdataSectionNames = val;
                    addCdataSectionElements(cdataSectionNames);
                }
                break;
            case 'd':
                if (OutputKeys.DOCTYPE_SYSTEM.equals(name)) {
                    this.m_doctypeSystem = val;
                } else if (OutputKeys.DOCTYPE_PUBLIC.equals(name)) {
                    this.m_doctypePublic = val;
                    if (val.startsWith("-//W3C//DTD XHTML"))
                        m_spaceBeforeClose = true;
                }
                break;
            case 'e':
                String newEncoding = val;
                if (OutputKeys.ENCODING.equals(name)) {
                    String possible_encoding = Encodings.getMimeEncoding(val);
                    if (possible_encoding != null) {
                        // if the encoding is being set, try to get the
                        // preferred
                        // mime-name and set it too.
                        super.setProp("mime-name", possible_encoding,
                                defaultVal);
                    }
                    final String oldExplicitEncoding = getOutputPropertyNonDefault(OutputKeys.ENCODING);
                    final String oldDefaultEncoding  = getOutputPropertyDefault(OutputKeys.ENCODING);
                    if ( (defaultVal && ( oldDefaultEncoding == null || !oldDefaultEncoding.equalsIgnoreCase(newEncoding)))
                            || ( !defaultVal && (oldExplicitEncoding == null || !oldExplicitEncoding.equalsIgnoreCase(newEncoding) ))) {
                       // We are trying to change the default or the non-default setting of the encoding to a different value
                       // from what it was
                       
                       EncodingInfo encodingInfo = Encodings.getEncodingInfo(newEncoding);
                       if (newEncoding != null && encodingInfo.name == null) {
                        // We tried to get an EncodingInfo for Object for the given
                        // encoding, but it came back with an internall null name
                        // so the encoding is not supported by the JDK, issue a message.
                        final String msg = Utils.messages.createMessage(
                                MsgKey.ER_ENCODING_NOT_SUPPORTED,new Object[]{ newEncoding });
                        
                        final String msg2 = 
                            "Warning: encoding \"" + newEncoding + "\" not supported, using "
                                   + Encodings.DEFAULT_MIME_ENCODING;
                        try {
                                // Prepare to issue the warning message
                                final Transformer tran = super.getTransformer();
                                if (tran != null) {
                                    final ErrorListener errHandler = tran
                                            .getErrorListener();
                                    // Issue the warning message
                                    if (null != errHandler
                                            && m_sourceLocator != null) {
                                        errHandler
                                                .warning(new TransformerException(
                                                        msg, m_sourceLocator));
                                        errHandler
                                                .warning(new TransformerException(
                                                        msg2, m_sourceLocator));
                                    } else {
                                        System.out.println(msg);
                                        System.out.println(msg2);
                                    }
                                } else {
                                    System.out.println(msg);
                                    System.out.println(msg2);
                                }
                            } catch (Exception e) {
                            }

                            // We said we are using UTF-8, so use it
                            newEncoding = Encodings.DEFAULT_MIME_ENCODING;
                            val = Encodings.DEFAULT_MIME_ENCODING; // to store the modified value into the properties a little later
                            encodingInfo = Encodings.getEncodingInfo(newEncoding);

                        } 
                       // The encoding was good, or was forced to UTF-8 above
                       
                       
                       // If there is already a non-default set encoding and we 
                       // are trying to set the default encoding, skip the this block
                       // as the non-default value is already the one to use.
                       if (defaultVal == false || oldExplicitEncoding == null) {
                           m_encodingInfo = encodingInfo;
                           if (newEncoding != null)
                               m_isUTF8 = newEncoding.equals(Encodings.DEFAULT_MIME_ENCODING);
                           
                           // if there was a previously set OutputStream
                           OutputStream os = getOutputStream();
                           if (os != null) {
                               Writer w = getWriter();
                               
                               // If the writer was previously set, but
                               // set by the user, or if the new encoding is the same
                               // as the old encoding, skip this block
                               String oldEncoding = getOutputProperty(OutputKeys.ENCODING);
                               if ((w == null || !m_writer_set_by_user) 
                                       && !newEncoding.equalsIgnoreCase(oldEncoding)) {
                                   // Make the change of encoding in our internal
                                   // table, then call setOutputStreamInternal
                                   // which will stomp on the old Writer (if any)
                                   // with a new Writer with the new encoding.
                                   super.setProp(name, val, defaultVal);
                                   setOutputStreamInternal(os,false);
                               }
                           }
                       }
                    }
                }
                break;
            case 'i':
                if (OutputPropertiesFactory.S_KEY_INDENT_AMOUNT.equals(name)) {
                    setIndentAmount(Integer.parseInt(val));
                } else if (OutputKeys.INDENT.equals(name)) {
                    boolean b = "yes".equals(val) ? true : false;
                    m_doIndent = b;
                }

                break;
            case 'l':
                if (OutputPropertiesFactory.S_KEY_LINE_SEPARATOR.equals(name)) {
                    m_lineSep = val.toCharArray();
                    m_lineSepLen = m_lineSep.length;
                }

                break;
            case 'm':
                if (OutputKeys.MEDIA_TYPE.equals(name)) {
                    m_mediatype = val;
                }
                break;
            case 'o':
                if (OutputKeys.OMIT_XML_DECLARATION.equals(name)) {
                    boolean b = "yes".equals(val) ? true : false;
                    this.m_shouldNotWriteXMLHeader = b;
                }
                break;
            case 's':
                // if standalone was explicitly specified
                if (OutputKeys.STANDALONE.equals(name)) {
                    if (defaultVal) {
                        setStandaloneInternal(val);
                    } else {
                        m_standaloneWasSpecified = true;
                        setStandaloneInternal(val);
                    }
                }

                break;
            case 'v':
                if (OutputKeys.VERSION.equals(name)) {
                    m_version = val;
                }
                break;
            default:
                break;

            } 
            super.setProp(name, val, defaultVal);
        }
    }
    /**
     * Specifies an output format for this serializer. It the
     * serializer has already been associated with an output format,
     * it will switch to the new format. This method should not be
     * called while the serializer is in the process of serializing
     * a document.
     *
     * @param format The output format to use
     */
    public void setOutputFormat(Properties format)
    {

        boolean shouldFlush = m_shouldFlush;
        
        if (format != null)
        {
            // Set the default values first,
            // and the non-default values after that, 
            // just in case there is some unexpected
            // residual values left over from over-ridden default values
            Enumeration propNames;
            propNames = format.propertyNames();
            while (propNames.hasMoreElements())
            {
                String key = (String) propNames.nextElement();
                // Get the value, possibly a default value
                String value = format.getProperty(key);
                // Get the non-default value (if any).
                String explicitValue = (String) format.get(key);
                if (explicitValue == null && value != null) {
                    // This is a default value
                    this.setOutputPropertyDefault(key,value);
                }
                if (explicitValue != null) {
                    // This is an explicit non-default value
                    this.setOutputProperty(key,explicitValue);
                }
            } 
        }   

        // Access this only from the Hashtable level... we don't want to 
        // get default properties.
        String entitiesFileName =
            (String) format.get(OutputPropertiesFactory.S_KEY_ENTITIES);

        if (null != entitiesFileName)
        {

            String method = 
                (String) format.get(OutputKeys.METHOD);
            
            m_charInfo = CharInfo.getCharInfo(entitiesFileName, method);
        }


         

        m_shouldFlush = shouldFlush;
    }

    /**
     * Returns the output format for this serializer.
     *
     * @return The output format in use
     */
    public Properties getOutputFormat() {
        Properties def = new Properties();
        {
            Set s = getOutputPropDefaultKeys();
            Iterator i = s.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String val = getOutputPropertyDefault(key);
                def.put(key, val);
            }
        }
        
        Properties props = new Properties(def);
        {
            Set s = getOutputPropKeys();
            Iterator i = s.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String val = getOutputPropertyNonDefault(key);
                if (val != null)
                    props.put(key, val);
            }
        }
        return props;
    }

    /**
     * Specifies a writer to which the document should be serialized.
     * This method should not be called while the serializer is in
     * the process of serializing a document.
     *
     * @param writer The output writer stream
     */
    public void setWriter(Writer writer)
    {        
        setWriterInternal(writer, true);
    }
    
    private boolean m_writer_set_by_user;
    private void setWriterInternal(Writer writer, boolean setByUser) {

        m_writer_set_by_user = setByUser;
        m_writer = writer;
        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if (m_tracer != null) {
            boolean noTracerYet = true;
            Writer w2 = m_writer;
            while (w2 instanceof WriterChain) {
                if (w2 instanceof SerializerTraceWriter) {
                    noTracerYet = false;
                    break;
                }
                w2 = ((WriterChain)w2).getWriter();
            }
            if (noTracerYet)
                m_writer = new SerializerTraceWriter(m_writer, m_tracer);
        }
    }
    
    /**
     * Set if the operating systems end-of-line line separator should
     * be used when serializing.  If set false NL character 
     * (decimal 10) is left alone, otherwise the new-line will be replaced on
     * output with the systems line separator. For example on UNIX this is
     * NL, while on Windows it is two characters, CR NL, where CR is the
     * carriage-return (decimal 13).
     *  
     * @param use_sytem_line_break True if an input NL is replaced with the 
     * operating systems end-of-line separator.
     * @return The previously set value of the serializer.
     */
    public boolean setLineSepUse(boolean use_sytem_line_break)
    {
        boolean oldValue = m_lineSepUse;
        m_lineSepUse = use_sytem_line_break;
        return oldValue;
    }

    /**
     * Specifies an output stream to which the document should be
     * serialized. This method should not be called while the
     * serializer is in the process of serializing a document.
     * <p>
     * The encoding specified in the output properties is used, or
     * if no encoding was specified, the default for the selected
     * output method.
     *
     * @param output The output stream
     */
    public void setOutputStream(OutputStream output)
    {
        setOutputStreamInternal(output, true);
    }
    
    private void setOutputStreamInternal(OutputStream output, boolean setByUser)
    {
        m_outputStream = output;
        String encoding = getOutputProperty(OutputKeys.ENCODING);        
        if (Encodings.DEFAULT_MIME_ENCODING.equalsIgnoreCase(encoding))
        {
            // We wrap the OutputStream with a writer, but
            // not one set by the user
            setWriterInternal(new WriterToUTF8Buffered(output), false);
        } else if (
                "WINDOWS-1250".equals(encoding)
                || "US-ASCII".equals(encoding)
                || "ASCII".equals(encoding))
        {
            setWriterInternal(new WriterToASCI(output), false);
        } else if (encoding != null) {
            Writer osw = null;
                try
                {
                    osw = Encodings.getWriter(output, encoding);
                }
                catch (UnsupportedEncodingException uee)
                {
                    osw = null;
                }

            
            if (osw == null) {
                System.out.println(
                    "Warning: encoding \""
                        + encoding
                        + "\" not supported"
                        + ", using "
                        + Encodings.DEFAULT_MIME_ENCODING);

                encoding = Encodings.DEFAULT_MIME_ENCODING;
                setEncoding(encoding);
                try {
                    osw = Encodings.getWriter(output, encoding);
                } catch (UnsupportedEncodingException e) {
                    // We can't really get here, UTF-8 is always supported
                    // This try-catch exists to make the compiler happy
                    e.printStackTrace();
                }
            }
            setWriterInternal(osw,false);
        }
        else {
            // don't have any encoding, but we have an OutputStream
            Writer osw = new OutputStreamWriter(output);
            setWriterInternal(osw,false);
        }
    }

    /**
     * @see SerializationHandler#setEscaping(boolean)
     */
    public boolean setEscaping(boolean escape)
    {
        final boolean temp = m_escaping;
        m_escaping = escape;
        return temp;

    }


    /**
     * Might print a newline character and the indentation amount
     * of the given depth.
     * 
     * @param depth the indentation depth (element nesting depth)
     *
     * @throws org.xml.sax.SAXException if an error occurs during writing.
     */
    protected void indent(int depth) throws IOException
    {

        if (m_startNewLine)
            outputLineSep();
        /* For m_indentAmount > 0 this extra test might be slower
         * but Xalan's default value is 0, so this extra test
         * will run faster in that situation.
         */
        if (m_indentAmount > 0)
            printSpace(depth * m_indentAmount);

    }
    
    /**
     * Indent at the current element nesting depth.
     * @throws IOException
     */
    protected void indent() throws IOException
    {
        indent(m_elemContext.m_currentElemDepth);
    }
    /**
     * Prints <var>n</var> spaces.
     * @param n         Number of spaces to print.
     *
     * @throws org.xml.sax.SAXException if an error occurs when writing.
     */
    private void printSpace(int n) throws IOException
    {
        final java.io.Writer writer = m_writer;
        for (int i = 0; i < n; i++)
        {
            writer.write(' ');
        }

    }

    /**
     * Report an attribute type declaration.
     *
     * <p>Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with
     * the separator "|" and all whitespace removed.</p>
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            final java.io.Writer writer = m_writer;
            DTDprolog();

            writer.write("<!ATTLIST ");
            writer.write(eName);
            writer.write(' ');

            writer.write(aName);
            writer.write(' ');
            writer.write(type);
            if (valueDefault != null)
            {
                writer.write(' ');
                writer.write(valueDefault);
            }

            //writer.write(" ");
            //writer.write(value);
            writer.write('>');
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Get the character stream where the events will be serialized to.
     *
     * @return Reference to the result Writer, or null.
     */
    public Writer getWriter()
    {
        return m_writer;
    }

    /**
     * Report a parsed external entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param publicId The declared public identifier of the entity, or
     *        null if none was declared.
     * @param systemId The declared system identifier of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #internalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
        try {
            DTDprolog();
            
            m_writer.write("<!ENTITY ");            
            m_writer.write(name);
            if (publicId != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(publicId);
  
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(systemId);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Tell if this character can be written without escaping.
     */
    protected boolean escapingNotNeeded(char ch)
    {
        final boolean ret;
        if (ch < 127)
        {
            // This is the old/fast code here, but is this 
            // correct for all encodings?
            if (ch >= CharInfo.S_SPACE || (CharInfo.S_LINEFEED == ch || 
                    CharInfo.S_CARRIAGERETURN == ch || CharInfo.S_HORIZONAL_TAB == ch))
                ret= true;
            else
                ret = false;
        }
        else {            
            ret = m_encodingInfo.isInEncoding(ch);
        }
        return ret;
    }

    /**
     * Once a surrogate has been detected, write out the pair of
     * characters if it is in the encoding, or if there is no
     * encoding, otherwise write out an entity reference
     * of the value of the unicode code point of the character
     * represented by the high/low surrogate pair.
     * <p>
     * An exception is thrown if there is no low surrogate in the pair,
     * because the array ends unexpectely, or if the low char is there
     * but its value is such that it is not a low surrogate.
     *
     * @param c the first (high) part of the surrogate, which
     * must be confirmed before calling this method.
     * @param ch Character array.
     * @param i position Where the surrogate was detected.
     * @param end The end index of the significant characters.
     * @return 0 if the pair of characters was written out as-is,
     * the unicode code point of the character represented by
     * the surrogate pair if an entity reference with that value
     * was written out. 
     * 
     * @throws IOException
     * @throws org.xml.sax.SAXException if invalid UTF-16 surrogate detected.
     */
    protected int writeUTF16Surrogate(char c, char ch[], int i, int end)
        throws IOException
    {
        int codePoint = 0;
        if (i + 1 >= end)
        {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] { Integer.toHexString((int) c)}));
        }
        
        final char high = c;
        final char low = ch[i+1];
        if (!Encodings.isLowUTF16Surrogate(low)) {
            throw new IOException(
                Utils.messages.createMessage(
                    MsgKey.ER_INVALID_UTF16_SURROGATE,
                    new Object[] {
                        Integer.toHexString((int) c)
                            + " "
                            + Integer.toHexString(low)}));
        }

        final java.io.Writer writer = m_writer;
                
        // If we make it to here we have a valid high, low surrogate pair
        if (m_encodingInfo.isInEncoding(c,low)) {
            // If the character formed by the surrogate pair
            // is in the encoding, so just write it out
            writer.write(ch,i,2);
        }
        else {
            // Don't know what to do with this char, it is
            // not in the encoding and not a high char in
            // a surrogate pair, so write out as an entity ref
            final String encoding = getEncoding();
            if (encoding != null) {
                /* The output encoding is known, 
                 * so somthing is wrong.
                  */
                codePoint = Encodings.toCodePoint(high, low);
                // not in the encoding, so write out a character reference
                writer.write('&');
                writer.write('#');
                writer.write(Integer.toString(codePoint));
                writer.write(';');
            } else {
                /* The output encoding is not known,
                 * so just write it out as-is.
                 */
                writer.write(ch, i, 2);
            }
        }
        // non-zero only if character reference was written out.
        return codePoint;
    }

    /**
     * Handle one of the default entities, return false if it
     * is not a default entity.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param fromTextNode true if the characters being processed
     * are from a text node, false if they are from an attribute value
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if the character was written, else i.
     *
     * @throws java.io.IOException
     */
    int accumDefaultEntity(
        java.io.Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {

        if (!escLF && CharInfo.S_LINEFEED == ch)
        {
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            // if this is text node character and a special one of those,
            // or if this is a character from attribute value and a special one of those
            if ((fromTextNode && m_charInfo.shouldMapTextChar(ch)) || (!fromTextNode && m_charInfo.shouldMapAttrChar(ch)))
            {
                String outputStringForChar = m_charInfo.getOutputStringForChar(ch);

                if (null != outputStringForChar)
                {
                    writer.write(outputStringForChar);
                }
                else
                    return i;
            }
            else
                return i;
        }

        return i + 1;

    }
    /**
     * Normalize the characters, but don't escape.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @param isCData true if a CDATA block should be built around the characters.
     * @param useSystemLineSeparator true if the operating systems 
     * end-of-line separator should be output rather than a new-line character.
     *
     * @throws IOException
     * @throws org.xml.sax.SAXException
     */
    void writeNormalizedChars(
        char ch[],
        int start,
        int length,
        boolean isCData,
        boolean useSystemLineSeparator)
        throws IOException, org.xml.sax.SAXException
    {
        final java.io.Writer writer = m_writer;
        int end = start + length;

        for (int i = start; i < end; i++)
        {
            char c = ch[i];

            if (CharInfo.S_LINEFEED == c && useSystemLineSeparator)
            {
                writer.write(m_lineSep, 0, m_lineSepLen);
            }
            else if (isCData && (!escapingNotNeeded(c)))
            {
                //                if (i != 0)
                if (m_cdataTagOpen)
                    closeCDATA();

                // This needs to go into a function... 
                if (Encodings.isHighUTF16Surrogate(c))
                {
                    writeUTF16Surrogate(c, ch, i, end);
                    i++ ; // process two input characters
                }
                else
                {
                    writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    writer.write(intStr);
                    writer.write(';');
                }

                //                if ((i != 0) && (i < (end - 1)))
                //                if (!m_cdataTagOpen && (i < (end - 1)))
                //                {
                //                    writer.write(CDATA_DELIMITER_OPEN);
                //                    m_cdataTagOpen = true;
                //                }
            }
            else if (
                isCData
                    && ((i < (end - 2))
                        && (']' == c)
                        && (']' == ch[i + 1])
                        && ('>' == ch[i + 2])))
            {
                writer.write(CDATA_CONTINUE);

                i += 2;
            }
            else
            {
                if (escapingNotNeeded(c))
                {
                    if (isCData && !m_cdataTagOpen)
                    {
                        writer.write(CDATA_DELIMITER_OPEN);
                        m_cdataTagOpen = true;
                    }
                    writer.write(c);
                }

                // This needs to go into a function... 
                else if (Encodings.isHighUTF16Surrogate(c))
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writeUTF16Surrogate(c, ch, i, end);
                    i++; // process two input characters
                }
                else
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    writer.write(intStr);
                    writer.write(';');
                }
            }
        }

    }

    /**
     * Ends an un-escaping section.
     *
     * @see #startNonEscaping
     *
     * @throws org.xml.sax.SAXException
     */
    public void endNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.pop();
    }

    /**
     * Starts an un-escaping section. All characters printed within an un-
     * escaping section are printed as is, without escaping special characters
     * into entity references. Only XML and HTML serializers need to support
     * this method.
     * <p> The contents of the un-escaping section will be delivered through the
     * regular <tt>characters</tt> event.
     *
     * @throws org.xml.sax.SAXException
     */
    public void startNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.push(true);
    }

    /**
     * Receive notification of cdata.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    protected void cdata(char ch[], int start, final int length)
        throws org.xml.sax.SAXException
    {

        try
        {
            final int old_start = start;
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }
            m_ispreserve = true;

            if (shouldIndent())
                indent();

            boolean writeCDataBrackets =
                (((length >= 1) && escapingNotNeeded(ch[start])));

            /* Write out the CDATA opening delimiter only if
             * we are supposed to, and if we are not already in
             * the middle of a CDATA section  
             */
            if (writeCDataBrackets && !m_cdataTagOpen)
            {
                m_writer.write(CDATA_DELIMITER_OPEN);
                m_cdataTagOpen = true;
            }

            // writer.write(ch, start, length);
            if (isEscapingDisabled())
            {
                charactersRaw(ch, start, length);
            }
            else
                writeNormalizedChars(ch, start, length, true, m_lineSepUse);

            /* used to always write out CDATA closing delimiter here,
             * but now we delay, so that we can merge CDATA sections on output.    
             * need to write closing delimiter later
             */
            if (writeCDataBrackets)
            {
                /* if the CDATA section ends with ] don't leave it open
                 * as there is a chance that an adjacent CDATA sections
                 * starts with ]>.  
                 * We don't want to merge ]] with > , or ] with ]> 
                 */
                if (ch[start + length - 1] == ']')
                    closeCDATA();
            }

            // time to fire off CDATA event
            if (m_tracer != null)
                super.fireCDATAEvent(ch, old_start, length);
        }
        catch (IOException ioe)
        {
            throw new org.xml.sax.SAXException(
                Utils.messages.createMessage(
                    MsgKey.ER_OIERROR,
                    null),
                ioe);
            //"IO error", ioe);
        }
    }

    /**
     * Tell if the character escaping should be disabled for the current state.
     *
     * @return true if the character escaping should be disabled.
     */
    private boolean isEscapingDisabled()
    {
        return m_disableOutputEscapingStates.peekOrFalse();
    }

    /**
     * If available, when the disable-output-escaping attribute is used,
     * output raw text without escaping.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void charactersRaw(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        if (m_inEntityRef)
            return;
        try
        {
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }

            m_ispreserve = true;

            m_writer.write(ch, start, length);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Receive notification of character data.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(final char chars[], final int start, final int length)
        throws org.xml.sax.SAXException
    {
        // It does not make sense to continue with rest of the method if the number of 
        // characters to read from array is 0.
        // Section 7.6.1 of XSLT 1.0 (http://www.w3.org/TR/xslt#value-of) suggest no text node
        // is created if string is empty.	
        if (length == 0 || (m_inEntityRef && !m_expandDTDEntities))
            return;
            
        m_docIsEmpty = false;
        
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }

        if (m_cdataStartCalled || m_elemContext.m_isCdataSection)
        {
            /* either due to startCDATA() being called or due to 
             * cdata-section-elements atribute, we need this as cdata
             */
            cdata(chars, start, length);

            return;
        }

        if (m_cdataTagOpen)
            closeCDATA();
        
        if (m_disableOutputEscapingStates.peekOrFalse() || (!m_escaping))
        {
            charactersRaw(chars, start, length);

            // time to fire off characters generation event
            if (m_tracer != null)
                super.fireCharEvent(chars, start, length);

            return;
        }

        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }

        
        try
        {
            int i;
            int startClean;
            
            // skip any leading whitspace 
            // don't go off the end and use a hand inlined version
            // of isWhitespace(ch)
            final int end = start + length;
            int lastDirtyCharProcessed = start - 1; // last non-clean character that was processed
													// that was processed
            final Writer writer = m_writer;
            boolean isAllWhitespace = true;

            // process any leading whitspace
            i = start;
            while (i < end && isAllWhitespace) {
                char ch1 = chars[i];

                if (m_charInfo.shouldMapTextChar(ch1)) {
                    // The character is supposed to be replaced by a String
                    // so write out the clean whitespace characters accumulated
                    // so far
                    // then the String.
                    writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                    String outputStringForChar = m_charInfo
                            .getOutputStringForChar(ch1);
                    writer.write(outputStringForChar);
                    // We can't say that everything we are writing out is
                    // all whitespace, we just wrote out a String.
                    isAllWhitespace = false;
                    lastDirtyCharProcessed = i; // mark the last non-clean
                    // character processed
                    i++;
                } else {
                    // The character is clean, but is it a whitespace ?
                    switch (ch1) {
                    // TODO: Any other whitespace to consider?
                    case CharInfo.S_SPACE:
                        // Just accumulate the clean whitespace
                        i++;
                        break;
                    case CharInfo.S_LINEFEED:
                        lastDirtyCharProcessed = processLineFeed(chars, i,
                                lastDirtyCharProcessed, writer);
                        i++;
                        break;
                    case CharInfo.S_CARRIAGERETURN:
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#13;");
                        lastDirtyCharProcessed = i;
                        i++;
                        break;
                    case CharInfo.S_HORIZONAL_TAB:
                        // Just accumulate the clean whitespace
                        i++;
                        break;
                    default:
                        // The character was clean, but not a whitespace
                        // so break the loop to continue with this character
                        // (we don't increment index i !!)
                        isAllWhitespace = false;
                        break;
                    }
                }
            }

            /* If there is some non-whitespace, mark that we may need
             * to preserve this. This is only important if we have indentation on.
             */            
            if (i < end || !isAllWhitespace) 
                m_ispreserve = true;
            
            
            for (; i < end; i++)
            {
                char ch = chars[i];
                
                if (m_charInfo.shouldMapTextChar(ch)) {
                    // The character is supposed to be replaced by a String
                    // e.g.   '&'  -->  "&amp;"
                    // e.g.   '<'  -->  "&lt;"
                    writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                    String outputStringForChar = m_charInfo.getOutputStringForChar(ch);
                    writer.write(outputStringForChar);
                    lastDirtyCharProcessed = i;
                }
                else {
                    if (ch <= 0x1F) {
                        // Range 0x00 through 0x1F inclusive
                        //
                        // This covers the non-whitespace control characters
                        // in the range 0x1 to 0x1F inclusive.
                        // It also covers the whitespace control characters in the same way:
                        // 0x9   TAB
                        // 0xA   NEW LINE
                        // 0xD   CARRIAGE RETURN
                        //
                        // We also cover 0x0 ... It isn't valid
                        // but we will output "&#0;" 
                        
                        // The default will handle this just fine, but this
                        // is a little performance boost to handle the more
                        // common TAB, NEW-LINE, CARRIAGE-RETURN
                        switch (ch) {

                        case CharInfo.S_HORIZONAL_TAB:
                            // Leave whitespace TAB as a real character
                            break;
                        case CharInfo.S_LINEFEED:
                            lastDirtyCharProcessed = processLineFeed(chars, i, lastDirtyCharProcessed, writer);
                            break;
                        case CharInfo.S_CARRIAGERETURN:
                        	writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        	writer.write("&#13;");
                        	lastDirtyCharProcessed = i;
                            // Leave whitespace carriage return as a real character
                            break;
                        default:
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                            writer.write("&#");
                            writer.write(Integer.toString(ch));
                            writer.write(';');
                            lastDirtyCharProcessed = i;
                            break;

                        }
                    }
                    else if (ch < 0x7F) {  
                        // Range 0x20 through 0x7E inclusive
                        // Normal ASCII chars, do nothing, just add it to
                        // the clean characters
                            
                    }
                    else if (ch <= 0x9F){
                        // Range 0x7F through 0x9F inclusive
                        // More control characters, including NEL (0x85)
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        lastDirtyCharProcessed = i;
                    }
                    else if (ch == CharInfo.S_LINE_SEPARATOR) {
                        // LINE SEPARATOR
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#8232;");
                        lastDirtyCharProcessed = i;
                    }
                    else if (m_encodingInfo.isInEncoding(ch)) {
                        // If the character is in the encoding, and
                        // not in the normal ASCII range, we also
                        // just leave it get added on to the clean characters
                        
                    }
                    else {
                        // This is a fallback plan, we should never get here
                        // but if the character wasn't previously handled
                        // (i.e. isn't in the encoding, etc.) then what
                        // should we do?  We choose to write out an entity
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        lastDirtyCharProcessed = i;
                    }
                }
            }
            
            // we've reached the end. Any clean characters at the
            // end of the array than need to be written out?
            startClean = lastDirtyCharProcessed + 1;
            if (i > startClean)
            {
                int lengthClean = i - startClean;
                m_writer.write(chars, startClean, lengthClean);
            }

            // For indentation purposes, mark that we've just writen text out
            m_isprevtext = true;
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // time to fire off characters generation event
        if (m_tracer != null)
            super.fireCharEvent(chars, start, length);
    }

	private int processLineFeed(final char[] chars, int i, int lastProcessed, final Writer writer) throws IOException {
		if (!m_lineSepUse 
		|| (m_lineSepLen ==1 && m_lineSep[0] == CharInfo.S_LINEFEED)){
		    // We are leaving the new-line alone, and it is just
		    // being added to the 'clean' characters,
			// so the last dirty character processed remains unchanged
		}
		else {
		    writeOutCleanChars(chars, i, lastProcessed);
		    writer.write(m_lineSep, 0, m_lineSepLen);
		    lastProcessed = i;
		}
		return lastProcessed;
	}

    private void writeOutCleanChars(final char[] chars, int i, int lastProcessed) throws IOException {
        int startClean;
        startClean = lastProcessed + 1;
        if (startClean < i)
        {
            int lengthClean = i - startClean;
            m_writer.write(chars, startClean, lengthClean);
        }
    }     
    /**
     * This method checks if a given character is between C0 or C1 range
     * of Control characters.
     * This method is added to support Control Characters for XML 1.1
     * If a given character is TAB (0x09), LF (0x0A) or CR (0x0D), this method
     * return false. Since they are whitespace characters, no special processing is needed.
     * 
     * @param ch
     * @return boolean
     */
    private static boolean isCharacterInC0orC1Range(char ch)
    {
        if(ch == 0x09 || ch == 0x0A || ch == 0x0D)
        	return false;
        else        	    	
        	return (ch >= 0x7F && ch <= 0x9F)|| (ch >= 0x01 && ch <= 0x1F);
    }
    /**
     * This method checks if a given character either NEL (0x85) or LSEP (0x2028)
     * These are new end of line charcters added in XML 1.1.  These characters must be
     * written as Numeric Character References (NCR) in XML 1.1 output document.
     * 
     * @param ch
     * @return boolean
     */
    private static boolean isNELorLSEPCharacter(char ch)
    {
        return (ch == 0x85 || ch == 0x2028);
    }
    /**
     * Process a dirty character and any preeceding clean characters
     * that were not yet processed.
     * @param chars array of characters being processed
     * @param end one (1) beyond the last character 
     * in chars to be processed
     * @param i the index of the dirty character
     * @param ch the character in chars[i]
     * @param lastDirty the last dirty character previous to i
     * @param fromTextNode true if the characters being processed are
     * from a text node, false if they are from an attribute value.
     * @return the index of the last character processed
     */
    private int processDirty(
        char[] chars, 
        int end,
        int i, 
        char ch,
        int lastDirty,
        boolean fromTextNode) throws IOException
    {
        int startClean = lastDirty + 1;
        // if we have some clean characters accumulated
        // process them before the dirty one.                   
        if (i > startClean)
        {
            int lengthClean = i - startClean;
            m_writer.write(chars, startClean, lengthClean);
        }

        // process the "dirty" character
        if (CharInfo.S_LINEFEED == ch && fromTextNode)
        {
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            startClean =
                accumDefaultEscape(
                    m_writer,
                    (char)ch,
                    i,
                    chars,
                    end,
                    fromTextNode,
                    false);
            i = startClean - 1;
        }
        // Return the index of the last character that we just processed 
        // which is a dirty character.
        return i;
    }

    /**
     * Receive notification of character data.
     *
     * @param s The string of characters to process.
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(String s) throws org.xml.sax.SAXException
    {
        if (m_inEntityRef && !m_expandDTDEntities)
            return;
        final int length = s.length();
        if (length > m_charsBuff.length)
        {
            m_charsBuff = new char[length * 2 + 1];
        }
        s.getChars(0, length, m_charsBuff, 0);
        characters(m_charsBuff, 0, length);
    }

    /**
     * Escape and writer.write a character.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param fromTextNode true if the characters being processed are
     * from a text node, false if the characters being processed are from
     * an attribute value.
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if a character was written, i+2 if two characters
     * were written out, else return i.
     *
     * @throws org.xml.sax.SAXException
     */
    private int accumDefaultEscape(
        Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean fromTextNode,
        boolean escLF)
        throws IOException
    {

        int pos = accumDefaultEntity(writer, ch, i, chars, len, fromTextNode, escLF);

        if (i == pos)
        {
            if (Encodings.isHighUTF16Surrogate(ch))
            {

                // Should be the UTF-16 low surrogate of the hig/low pair.
                char next;
                // Unicode code point formed from the high/low pair.
                int codePoint = 0;

                if (i + 1 >= len)
                {
                    throw new IOException(
                        Utils.messages.createMessage(
                            MsgKey.ER_INVALID_UTF16_SURROGATE,
                            new Object[] { Integer.toHexString(ch)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+ " ?");
                }
                else
                {
                    next = chars[++i];

                    if (!(Encodings.isLowUTF16Surrogate(next)))
                        throw new IOException(
                            Utils.messages.createMessage(
                                MsgKey
                                    .ER_INVALID_UTF16_SURROGATE,
                                new Object[] {
                                    Integer.toHexString(ch)
                                        + " "
                                        + Integer.toHexString(next)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+" "+Integer.toHexString(next));
                    codePoint = Encodings.toCodePoint(ch,next);
                }

                writer.write("&#");
                writer.write(Integer.toString(codePoint));
                writer.write(';');
                pos += 2; // count the two characters that went into writing out this entity
            }
            else
            {
                /*  This if check is added to support control characters in XML 1.1.
                 *  If a character is a Control Character within C0 and C1 range, it is desirable
                 *  to write it out as Numeric Character Reference(NCR) regardless of XML Version
                 *  being used for output document.
                 */ 
                if (isCharacterInC0orC1Range(ch) || isNELorLSEPCharacter(ch))
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else if ((!escapingNotNeeded(ch) || 
                    (  (fromTextNode && m_charInfo.shouldMapTextChar(ch))
                     || (!fromTextNode && m_charInfo.shouldMapAttrChar(ch)))) 
                && m_elemContext.m_currentElemDepth > 0)
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else
                {
                    writer.write(ch);
                }
                pos++;  // count the single character that was processed
            }

        }
        return pos;
    }

    /**
     * Receive notification of the beginning of an element, although this is a
     * SAX method additional namespace or attribute information can occur before
     * or after this call, that is associated with this element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name.
     * @param atts The attributes attached to the element, if any.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     * @see org.xml.sax.ContentHandler#endElement
     * @see org.xml.sax.AttributeList
     *
     * @throws org.xml.sax.SAXException
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String name,
        Attributes atts)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;

        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
            m_docIsEmpty = false;
        }
        else if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            if (m_needToOutputDocTypeDecl) {
                if(null != getDoctypeSystem()) {
                    outputDocTypeDecl(name, true);
                }
                m_needToOutputDocTypeDecl = false;
            }
        
            /* before we over-write the current elementLocalName etc.
             * lets close out the old one (if we still need to)
             */
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }

            if (namespaceURI != null)
                ensurePrefixIsDeclared(namespaceURI, name);
                
            m_ispreserve = false;

            if (shouldIndent() && m_startNewLine)
            {
                indent();
            }

            m_startNewLine = true;

            final java.io.Writer writer = m_writer;
            writer.write('<');
            writer.write(name);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // process the attributes now, because after this SAX call they might be gone
        if (atts != null)
            addAttributes(atts);
            
        m_elemContext = m_elemContext.push(namespaceURI,localName,name);
        m_isprevtext = false;

        if (m_tracer != null)
            firePseudoAttributes();
    }

    /**
      * Receive notification of the beginning of an element, additional
      * namespace or attribute information can occur before or after this call,
      * that is associated with this element.
      *
      *
      * @param elementNamespaceURI The Namespace URI, or the empty string if the
      *        element has no Namespace URI or if Namespace
      *        processing is not being performed.
      * @param elementLocalName The local name (without prefix), or the
      *        empty string if Namespace processing is not being
      *        performed.
      * @param elementName The element type name.
      * @throws org.xml.sax.SAXException Any SAX exception, possibly
      *            wrapping another exception.
      * @see org.xml.sax.ContentHandler#startElement
      * @see org.xml.sax.ContentHandler#endElement
      * @see org.xml.sax.AttributeList
      *
      * @throws org.xml.sax.SAXException
      */
    public void startElement(
        String elementNamespaceURI,
        String elementLocalName,
        String elementName)
        throws SAXException
    {
        startElement(elementNamespaceURI, elementLocalName, elementName, null);
    }

    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param closeDecl
     *
     * @throws java.io.IOException
     */
    void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            final java.io.Writer writer = m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(name);

            String doctypePublic = getDoctypePublic();
            if (null != doctypePublic)
            {
                writer.write(" PUBLIC \"");
                writer.write(doctypePublic);
                writer.write('\"');
            }

            String doctypeSystem = getDoctypeSystem();
            if (null != doctypeSystem)
            {
                if (null == doctypePublic)
                    writer.write(" SYSTEM \"");
                else
                    writer.write(" \"");

                writer.write(doctypeSystem);

                if (closeDecl)
                {
                    writer.write("\">");
                    writer.write(m_lineSep, 0, m_lineSepLen);
                    closeDecl = false; // done closing
                }
                else
                    writer.write('\"');
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Process the attributes, which means to write out the currently
     * collected attributes to the writer. The attributes are not
     * cleared by this method
     * 
     * @param writer the writer to write processed attributes to.
     * @param nAttrs the number of attributes in m_attributes 
     * to be processed
     *
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void processAttributes(java.io.Writer writer, int nAttrs) throws IOException, SAXException
    {
            /* real SAX attributes are not passed in, so process the 
             * attributes that were collected after the startElement call.
             * _attribVector is a "cheap" list for Stream serializer output
             * accumulated over a series of calls to attribute(name,value)
             */

            String encoding = getEncoding();
            for (int i = 0; i < nAttrs; i++)
            {
                // elementAt is JDK 1.1.8
                final String name = m_attributes.getQName(i);
                final String value = m_attributes.getValue(i);
                writer.write(' ');
                writer.write(name);
                writer.write("=\"");
                writeAttrString(writer, value, encoding);
                writer.write('\"');
            }
    }

    /**
     * Returns the specified <var>string</var> after substituting <VAR>specials</VAR>,
     * and UTF-16 surrogates for chracter references <CODE>&amp;#xnn</CODE>.
     *
     * @param   string      String to convert to XML format.
     * @param   encoding    CURRENTLY NOT IMPLEMENTED.
     *
     * @throws java.io.IOException
     */
    public void writeAttrString(
        Writer writer,
        String string,
        String encoding)
        throws IOException
    {
        final int len = string.length();
        if (len > m_attrBuff.length)
        {
           m_attrBuff = new char[len*2 + 1];             
        }
        string.getChars(0,len, m_attrBuff, 0);   
        final char[] stringChars = m_attrBuff;

        for (int i = 0; i < len; i++)
        {
            char ch = stringChars[i];
            
            if (m_charInfo.shouldMapAttrChar(ch)) {
                // The character is supposed to be replaced by a String
                // e.g.   '&'  -->  "&amp;"
                // e.g.   '<'  -->  "&lt;"
                accumDefaultEscape(writer, ch, i, stringChars, len, false, true);
            }
            else {
                if (0x0 <= ch && ch <= 0x1F) {
                    // Range 0x00 through 0x1F inclusive
                    // This covers the non-whitespace control characters
                    // in the range 0x1 to 0x1F inclusive.
                    // It also covers the whitespace control characters in the same way:
                    // 0x9   TAB
                    // 0xA   NEW LINE
                    // 0xD   CARRIAGE RETURN
                    //
                    // We also cover 0x0 ... It isn't valid
                    // but we will output "&#0;" 
                    
                    // The default will handle this just fine, but this
                    // is a little performance boost to handle the more
                    // common TAB, NEW-LINE, CARRIAGE-RETURN
                    switch (ch) {

                    case CharInfo.S_HORIZONAL_TAB:
                        writer.write("&#9;");
                        break;
                    case CharInfo.S_LINEFEED:
                        writer.write("&#10;");
                        break;
                    case CharInfo.S_CARRIAGERETURN:
                        writer.write("&#13;");
                        break;
                    default:
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(';');
                        break;

                    }
                }
                else if (ch < 0x7F) {   
                    // Range 0x20 through 0x7E inclusive
                    // Normal ASCII chars
                        writer.write(ch);
                }
                else if (ch <= 0x9F){
                    // Range 0x7F through 0x9F inclusive
                    // More control characters
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                else if (ch == CharInfo.S_LINE_SEPARATOR) {
                    // LINE SEPARATOR
                    writer.write("&#8232;");
                }
                else if (m_encodingInfo.isInEncoding(ch)) {
                    // If the character is in the encoding, and
                    // not in the normal ASCII range, we also
                    // just write it out
                    writer.write(ch);
                }
                else {
                    // This is a fallback plan, we should never get here
                    // but if the character wasn't previously handled
                    // (i.e. isn't in the encoding, etc.) then what
                    // should we do?  We choose to write out a character ref
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(';');
                }
                    
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *
     * @throws org.xml.sax.SAXException
     */
    public void endElement(String namespaceURI, String localName, String name)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;

        // namespaces declared at the current depth are no longer valid
        // so get rid of them    
        m_prefixMap.popNamespaces(m_elemContext.m_currentElemDepth, null);

        try
        {
            final java.io.Writer writer = m_writer;
            if (m_elemContext.m_startTagOpen)
            {
                if (m_tracer != null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    processAttributes(m_writer, nAttrs);
                    // clear attributes object for re-use with next element
                    m_attributes.clear();
                }
                if (m_spaceBeforeClose)
                    writer.write(" />");
                else
                    writer.write("/>");
                /* don't need to pop cdataSectionState because
                 * this element ended so quickly that we didn't get
                 * to push the state.
                 */

            }
            else
            {
                if (m_cdataTagOpen)
                    closeCDATA();

                if (shouldIndent())
                    indent(m_elemContext.m_currentElemDepth - 1);
                writer.write('<');
                writer.write('/');
                writer.write(name);
                writer.write('>');
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        if (!m_elemContext.m_startTagOpen && m_doIndent)
        {
            m_ispreserve = m_preserves.isEmpty() ? false : m_preserves.pop();
        }

        m_isprevtext = false;

        // fire off the end element event
        if (m_tracer != null)
            super.fireEndElem(name);
        m_elemContext = m_elemContext.m_prev;
    }

    /**
     * Receive notification of the end of an element.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *     wrapping another exception.
     */
    public void endElement(String name) throws org.xml.sax.SAXException
    {
        endElement(null, null, name);
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping
     * just before another element is about to start.
     * This call will close any open tags so that the prefix mapping
     * will not apply to the current element, but the up comming child.
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     * 
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * 
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * 
     */
    public void startPrefixMapping(String prefix, String uri)
        throws org.xml.sax.SAXException
    {
        // the "true" causes the flush of any open tags
        startPrefixMapping(prefix, uri, true);
    }

    /**
     * Handle a prefix/uri mapping, which is associated with a startElement()
     * that is soon to follow. Need to close any open start tag to make
     * sure than any name space attributes due to this event are associated wih
     * the up comming element, not the current one.
     * @see ExtendedContentHandler#startPrefixMapping
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @param shouldFlush true if any open tags need to be closed first, this
     * will impact which element the mapping applies to (open parent, or its up
     * comming child)
     * @return returns true if the call made a change to the current 
     * namespace information, false if it did not change anything, e.g. if the
     * prefix/namespace mapping was already in scope from before.
     * 
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     *
     *
     */
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws org.xml.sax.SAXException
    {

        /* Remember the mapping, and at what depth it was declared
         * This is one greater than the current depth because these
         * mappings will apply to the next depth. This is in
         * consideration that startElement() will soon be called
         */

        boolean pushed;
        int pushDepth;
        if (shouldFlush)
        {
            flushPending();
            // the prefix mapping applies to the child element (one deeper)
            pushDepth = m_elemContext.m_currentElemDepth + 1;
        }
        else
        {
            // the prefix mapping applies to the current element
            pushDepth = m_elemContext.m_currentElemDepth;
        }
        pushed = m_prefixMap.pushNamespace(prefix, uri, pushDepth);

        if (pushed)
        {
            /* Brian M.: don't know if we really needto do this. The
             * callers of this object should have injected both
             * startPrefixMapping and the attributes.  We are 
             * just covering our butt here.
             */
            String name;
            if (EMPTYSTRING.equals(prefix))
            {
                name = "xmlns";
                addAttributeAlways(XMLNS_URI, name, name, "CDATA", uri, false);
            }
            else
            {
                if (!EMPTYSTRING.equals(uri))
                    // hack for XSLTC attribset16 test
                { // that maps ns1 prefix to "" URI
                    name = "xmlns:" + prefix;

                    /* for something like xmlns:abc="w3.pretend.org"
                     *  the      uri is the value, that is why we pass it in the
                     * value, or 5th slot of addAttributeAlways()
                     */
                    addAttributeAlways(XMLNS_URI, prefix, name, "CDATA", uri, false);
                }
            }
        }
        return pushed;
    }

    /**
     * Receive notification of an XML comment anywhere in the document. This
     * callback will be used for comments inside or outside the document
     * element, including comments in the external DTD subset (if read).
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     */
    public void comment(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        int start_old = start;
        if (m_inEntityRef)
            return;
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }

        try
        {
            final int limit = start + length;
            boolean wasDash = false;
            if (m_cdataTagOpen)
                closeCDATA();
            
            if (shouldIndent())
                indent();
            
            final java.io.Writer writer = m_writer;    
            writer.write(COMMENT_BEGIN);
            // Detect occurrences of two consecutive dashes, handle as necessary.
            for (int i = start; i < limit; i++)
            {
                if (wasDash && ch[i] == '-')
                {
                    writer.write(ch, start, i - start);
                    writer.write(" -");
                    start = i + 1;
                }
                wasDash = (ch[i] == '-');
            }

            // if we have some chars in the comment
            if (length > 0)
            {
                // Output the remaining characters (if any)
                final int remainingChars = (limit - start);
                if (remainingChars > 0)
                    writer.write(ch, start, remainingChars);
                // Protect comment end from a single trailing dash
                if (ch[limit - 1] == '-')
                    writer.write(' ');
            }
            writer.write(COMMENT_END);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        /*
         * Don't write out any indentation whitespace now,
         * because there may be non-whitespace text after this.
         * 
         * Simply mark that at this point if we do decide
         * to indent that we should 
         * add a newline on the end of the current line before
         * the indentation at the start of the next line.
         */ 
        m_startNewLine = true;
        // time to generate comment event
        if (m_tracer != null)
            super.fireCommentEvent(ch, start_old,length);
    }

    /**
     * Report the end of a CDATA section.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     *
     *  @see  #startCDATA
     */
    public void endCDATA() throws org.xml.sax.SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        m_cdataStartCalled = false;
    }

    /**
     * Report the end of DTD declarations.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #startDTD
     */
    public void endDTD() throws org.xml.sax.SAXException
    {
        try
        {
            if (m_needToOutputDocTypeDecl)
            {
                outputDocTypeDecl(m_elemContext.m_elementName, false);
                m_needToOutputDocTypeDecl = false;
            }
            final java.io.Writer writer = m_writer;
            if (!m_inDoctype)
                writer.write("]>");
            else
            {
                writer.write('>');
            }

            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * End the scope of a prefix-URI Namespace mapping.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     * 
     * @param prefix The prefix that was being mapping.
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     */
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException
    { // do nothing
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     * 
     * Not sure how to get this invoked quite yet.
     * 
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #characters
     * 
     * @throws org.xml.sax.SAXException
     */
    public void ignorableWhitespace(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        if (0 == length)
            return;
        characters(ch, start, length);
    }

    /**
     * Receive notification of a skipped entity.
     * @see org.xml.sax.ContentHandler#skippedEntity
     * 
     * @param name The name of the skipped entity.  If it is a
     *       parameter                   entity, the name will begin with '%',
     * and if it is the external DTD subset, it will be the string
     * "[dtd]".
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping
     * another exception.
     */
    public void skippedEntity(String name) throws org.xml.sax.SAXException
    { // TODO: Should handle
    }

    /**
     * Report the start of a CDATA section.
     * 
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #endCDATA
     */
    public void startCDATA() throws org.xml.sax.SAXException
    {
        m_cdataStartCalled = true;
    }

    /**
     * Report the beginning of an entity.
     * 
     * The start and end of the document entity are not reported.
     * The start and end of the external DTD subset are reported
     * using the pseudo-name "[dtd]".  All other events must be
     * properly nested within start/end entity events.
     * 
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #endEntity
     * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
     */
    public void startEntity(String name) throws org.xml.sax.SAXException
    {
        if (name.equals("[dtd]"))
            m_inExternalDTD = true;

        if (!m_expandDTDEntities && !m_inExternalDTD) {
            /* Only leave the entity as-is if
             * we've been told not to expand them
             * and this is not the magic [dtd] name.
             */
            startNonEscaping();
            characters("&" + name + ';');
            endNonEscaping();
        }

        m_inEntityRef = true;
    }

    /**
     * For the enclosing elements starting tag write out
     * out any attributes followed by ">"
     *
     * @throws org.xml.sax.SAXException
     */
    protected void closeStartTag() throws SAXException
    {

        if (m_elemContext.m_startTagOpen)
        {

            try
            {
                if (m_tracer != null)
                    super.fireStartElem(m_elemContext.m_elementName);
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    processAttributes(m_writer, nAttrs);
                    // clear attributes object for re-use with next element
                    m_attributes.clear();
                }
                m_writer.write('>');
            }
            catch (IOException e)
            {
                throw new SAXException(e);
            }

            /* whether Xalan or XSLTC, we have the prefix mappings now, so
             * lets determine if the current element is specified in the cdata-
             * section-elements list.
             */
            if (m_CdataElems != null)
                m_elemContext.m_isCdataSection = isCdataSection();

            if (m_doIndent)
            {
                m_isprevtext = false;
                m_preserves.push(m_ispreserve);
            }
        }

    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * Any declarations are assumed to be in the internal subset unless
     * otherwise indicated.
     * 
     * @param name The document type name.
     * @param publicId The declared public identifier for the
     *        external DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the
     *        external DTD subset, or null if none was declared.
     * @throws org.xml.sax.SAXException The application may raise an
     *            exception.
     * @see #endDTD
     * @see #startEntity
     */
    public void startDTD(String name, String publicId, String systemId)
        throws org.xml.sax.SAXException
    {
        setDoctypeSystem(systemId);
        setDoctypePublic(publicId);

        m_elemContext.m_elementName = name;
        m_inDoctype = true;
    }

    /**
     * Returns the m_indentAmount.
     * @return int
     */
    public int getIndentAmount()
    {
        return m_indentAmount;
    }

    /**
     * Sets the m_indentAmount.
     * 
     * @param m_indentAmount The m_indentAmount to set
     */
    public void setIndentAmount(int m_indentAmount)
    {
        this.m_indentAmount = m_indentAmount;
    }

    /**
     * Tell if, based on space preservation constraints and the doIndent property,
     * if an indent should occur.
     *
     * @return True if an indent should occur.
     */
    protected boolean shouldIndent()
    {
        return m_doIndent && (!m_ispreserve && !m_isprevtext) && m_elemContext.m_currentElemDepth > 0;
    }

    /**
     * Searches for the list of qname properties with the specified key in the
     * property list. If the key is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The
     * method returns <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @param props the list of properties to search in.
     * 
     * Sets the vector of local-name/URI pairs of the cdata section elements
     * specified in the cdata-section-elements property.
     * 
     * This method is essentially a copy of getQNameProperties() from
     * OutputProperties. Eventually this method should go away and a call
     * to setCdataSectionElements(Vector v) should be made directly.
     */
    private void setCdataSectionElements(String key, Properties props)
    {

        String s = props.getProperty(key);

        if (null != s)
        {
            // Vector of URI/LocalName pairs
            Vector v = new Vector();
            int l = s.length();
            boolean inCurly = false;
            StringBuffer buf = new StringBuffer();

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
                            addCdataSectionElement(buf.toString(), v);
                            buf.setLength(0);
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
                addCdataSectionElement(buf.toString(), v);
                buf.setLength(0);
            }
            // call the official, public method to set the collected names
            setCdataSectionElements(v);
        }

    }

    /**
     * Adds a URI/LocalName pair of strings to the list.
     *
     * @param URI_and_localName String of the form "{uri}local" or "local" 
     * 
     * @return a QName object
     */
    private void addCdataSectionElement(String URI_and_localName, Vector v)
    {

        StringTokenizer tokenizer =
            new StringTokenizer(URI_and_localName, "{}", false);
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

        if (null == s2)
        {
            // add null URI and the local name
            v.addElement(null);
            v.addElement(s1);
        }
        else
        {
            // add URI, then local name
            v.addElement(s1);
            v.addElement(s2);
        }
    }

    /**
     * Remembers the cdata sections specified in the cdata-section-elements.
     * The "official way to set URI and localName pairs. 
     * This method should be used by both Xalan and XSLTC.
     * 
     * @param URI_and_localNames a vector of pairs of Strings (URI/local)
     */
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
        // convert to the new way.
        if (URI_and_localNames != null)
        {
            final int len = URI_and_localNames.size() - 1;
            if (len > 0)
            {
                final StringBuffer sb = new StringBuffer();
                for (int i = 0; i < len; i += 2)
                {
                    // whitspace separated "{uri1}local1 {uri2}local2 ..."
                    if (i != 0)
                        sb.append(' ');
                    final String uri = (String) URI_and_localNames.elementAt(i);
                    final String localName =
                        (String) URI_and_localNames.elementAt(i + 1);
                    if (uri != null)
                    {
                        // If there is no URI don't put this in, just the localName then.
                        sb.append('{');
                        sb.append(uri);
                        sb.append('}');
                    }
                    sb.append(localName);
                }
                m_StringOfCDATASections = sb.toString();
            }
        }
        initCdataElems(m_StringOfCDATASections);
    }

    /**
     * Makes sure that the namespace URI for the given qualified attribute name
     * is declared.
     * @param ns the namespace URI
     * @param rawName the qualified name 
     * @return returns null if no action is taken, otherwise it returns the
     * prefix used in declaring the namespace. 
     * @throws SAXException
     */
    protected String ensureAttributesNamespaceIsDeclared(
        String ns,
        String localName,
        String rawName)
        throws org.xml.sax.SAXException
    {

        if (ns != null && ns.length() > 0)
        {

            // extract the prefix in front of the raw name
            int index = 0;
            String prefixFromRawName =
                (index = rawName.indexOf(":")) < 0
                    ? ""
                    : rawName.substring(0, index);

            if (index > 0)
            {
                // we have a prefix, lets see if it maps to a namespace 
                String uri = m_prefixMap.lookupNamespace(prefixFromRawName);
                if (uri != null && uri.equals(ns))
                {
                    // the prefix in the raw name is already maps to the given namespace uri
                    // so we don't need to do anything
                    return null;
                }
                else
                {
                    // The uri does not map to the prefix in the raw name,
                    // so lets make the mapping.
                    this.startPrefixMapping(prefixFromRawName, ns, false);
                    this.addAttribute(
                        "http://www.w3.org/2000/xmlns/",
                        prefixFromRawName,
                        "xmlns:" + prefixFromRawName,
                        "CDATA",
                        ns, false);
                    return prefixFromRawName;
                }
            }
            else
            {
                // we don't have a prefix in the raw name.
                // Does the URI map to a prefix already?
                String prefix = m_prefixMap.lookupPrefix(ns);
                if (prefix == null)
                {
                    // uri is not associated with a prefix,
                    // so lets generate a new prefix to use
                    prefix = m_prefixMap.generateNextPrefix();
                    this.startPrefixMapping(prefix, ns, false);
                    this.addAttribute(
                        "http://www.w3.org/2000/xmlns/",
                        prefix,
                        "xmlns:" + prefix,
                        "CDATA",
                        ns, false);
                }

                return prefix;

            }
        }
        return null;
    }

    void ensurePrefixIsDeclared(String ns, String rawName)
        throws org.xml.sax.SAXException
    {

        if (ns != null && ns.length() > 0)
        {
            int index;
            final boolean no_prefix = ((index = rawName.indexOf(":")) < 0);
            String prefix = (no_prefix) ? "" : rawName.substring(0, index);

            if (null != prefix)
            {
                String foundURI = m_prefixMap.lookupNamespace(prefix);

                if ((null == foundURI) || !foundURI.equals(ns))
                {
                    this.startPrefixMapping(prefix, ns);

                    // Bugzilla1133: Generate attribute as well as namespace event.
                    // SAX does expect both.

                    this.addAttributeAlways(
                        "http://www.w3.org/2000/xmlns/",
                        no_prefix ? "xmlns" : prefix,  // local name
                        no_prefix ? "xmlns" : ("xmlns:"+ prefix), // qname
                        "CDATA",
                        ns,
                        false);
                }

            }
        }
    }

    /**
     * This method flushes any pending events, which can be startDocument()
     * closing the opening tag of an element, or closing an open CDATA section.
     */
    public void flushPending() throws SAXException
    {
            if (m_needToCallStartDocument)
            {
                startDocumentInternal();
                m_needToCallStartDocument = false;
            }
            if (m_elemContext.m_startTagOpen)
            {
                closeStartTag();
                m_elemContext.m_startTagOpen = false;
            }

            if (m_cdataTagOpen)
            {
                closeCDATA();
                m_cdataTagOpen = false;
            }
            if (m_writer != null) {
                try {
                    m_writer.flush();
                }
                catch(IOException e) {
                    // what? me worry?
                }
            }
    }

    public void setContentHandler(ContentHandler ch)
    {
        // this method is really only useful in the ToSAXHandler classes but it is
        // in the interface.  If the method defined here is ever called
        // we are probably in trouble.
    }

    /**
     * Adds the given attribute to the set of attributes, even if there is
     * no currently open element. This is useful if a SAX startPrefixMapping()
     * should need to add an attribute before the element name is seen.
     * 
     * This method is a copy of its super classes method, except that some
     * tracing of events is done.  This is so the tracing is only done for
     * stream serializers, not for SAX ones.
     *
     * @param uri the URI of the attribute
     * @param localName the local name of the attribute
     * @param rawName   the qualified name of the attribute
     * @param type the type of the attribute (probably CDATA)
     * @param value the value of the attribute
     * @param xslAttribute true if this attribute is coming from an xsl:attribute element.
     * @return true if the attribute value was added, 
     * false if the attribute already existed and the value was
     * replaced with the new value.
     */
    public boolean addAttributeAlways(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean xslAttribute)
    {
        boolean was_added;
        int index;
        if (uri == null || localName == null || uri.length() == 0)
            index = m_attributes.getIndex(rawName);
        else {
            index = m_attributes.getIndex(uri, localName);
        }

        if (index >= 0)
        {
            String old_value = null;
            if (m_tracer != null)
            {
                old_value = m_attributes.getValue(index);
                if (value.equals(old_value))
                    old_value = null;
            }

            /* We've seen the attribute before.
             * We may have a null uri or localName, but all we really
             * want to re-set is the value anyway.
             */
            m_attributes.setValue(index, value);
            was_added = false;
            if (old_value != null)
                firePseudoAttributes();

        }
        else
        {
            // the attribute doesn't exist yet, create it
            if (xslAttribute)
            {
                /*
                 * This attribute is from an xsl:attribute element so we take some care in
                 * adding it, e.g.
                 *   <elem1  foo:attr1="1" xmlns:foo="uri1">
                 *       <xsl:attribute name="foo:attr2">2</xsl:attribute>
                 *   </elem1>
                 * 
                 * We are adding attr1 and attr2 both as attributes of elem1,
                 * and this code is adding attr2 (the xsl:attribute ).
                 * We could have a collision with the prefix like in the example above.
                 */

                // In the example above, is there a prefix like foo ?
                final int colonIndex = rawName.indexOf(':');
                if (colonIndex > 0)
                {
                    String prefix = rawName.substring(0,colonIndex);
                    NamespaceMappings.MappingRecord existing_mapping = m_prefixMap.getMappingFromPrefix(prefix);

                    /* Before adding this attribute (foo:attr2),
                     * is the prefix for it (foo) already mapped at the current depth?
                     */
                    if (existing_mapping != null 
                    && existing_mapping.m_declarationDepth == m_elemContext.m_currentElemDepth
                    && !existing_mapping.m_uri.equals(uri))
                    {
                        /*
                         * There is an existing mapping of this prefix,
                         * it differs from the one we need,
                         * and unfortunately it is at the current depth so we 
                         * can not over-ride it.
                         */

                        /*
                         * Are we lucky enough that an existing other prefix maps to this URI ?
                         */
                        prefix = m_prefixMap.lookupPrefix(uri);
                        if (prefix == null)
                        {
                            /* Unfortunately there is no existing prefix that happens to map to ours,
                             * so to avoid a prefix collision we must generated a new prefix to use. 
                             * This is OK because the prefix URI mapping
                             * defined in the xsl:attribute is short in scope, 
                             * just the xsl:attribute element itself, 
                             * and at this point in serialization the body of the
                             * xsl:attribute, if any, is just a String. Right?
                             *   . . . I sure hope so - Brian M. 
                             */
                            prefix = m_prefixMap.generateNextPrefix();
                        }

                        rawName = prefix + ':' + localName;
                    }
                }

                try
                {
                    /* This is our last chance to make sure the namespace for this
                     * attribute is declared, especially if we just generated an alternate
                     * prefix to avoid a collision (the new prefix/rawName will go out of scope
                     * soon and be lost ...  last chance here.
                     */
                    String prefixUsed =
                        ensureAttributesNamespaceIsDeclared(
                            uri,
                            localName,
                            rawName);
                }
                catch (SAXException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            m_attributes.addAttribute(uri, localName, rawName, type, value);
            was_added = true;
            if (m_tracer != null)
                firePseudoAttributes();
        }
        return was_added;
    }

    /**
     * To fire off the pseudo characters of attributes, as they currently
     * exist. This method should be called everytime an attribute is added,
     * or when an attribute value is changed, or an element is created.
     */

    protected void firePseudoAttributes()
    {
        if (m_tracer != null)
        {
            try
            {
                // flush out the "<elemName" if not already flushed
                m_writer.flush();
                
                // make a StringBuffer to write the name="value" pairs to.
                StringBuffer sb = new StringBuffer();
                int nAttrs = m_attributes.getLength();
                if (nAttrs > 0)
                {
                    // make a writer that internally appends to the same
                    // StringBuffer
                    java.io.Writer writer =
                        new ToStream.WritertoStringBuffer(sb);

                    processAttributes(writer, nAttrs);
                    // Don't clear the attributes! 
                    // We only want to see what would be written out
                    // at this point, we don't want to loose them.
                }
                sb.append('>');  // the potential > after the attributes.
                // convert the StringBuffer to a char array and
                // emit the trace event that these characters "might"
                // be written                
                char ch[] = sb.toString().toCharArray();
                m_tracer.fireGenerateEvent(
                    SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                    ch,
                    0,
                    ch.length);                
            }
            catch (IOException ioe)
            {
                // ignore ?
            }
            catch (SAXException se)
            {
                // ignore ?
            }
        }
    }

    /**
     * This inner class is used only to collect attribute values
     * written by the method writeAttrString() into a string buffer.
     * In this manner trace events, and the real writing of attributes will use
     * the same code.
     */
    private class WritertoStringBuffer extends java.io.Writer
    {
        final private StringBuffer m_stringbuf;
        /**
         * @see java.io.Writer#write(char[], int, int)
         */
        WritertoStringBuffer(StringBuffer sb)
        {
            m_stringbuf = sb;
        }

        public void write(char[] arg0, int arg1, int arg2) throws IOException
        {
            m_stringbuf.append(arg0, arg1, arg2);
        }
        /**
         * @see java.io.Writer#flush()
         */
        public void flush() throws IOException
        {
        }
        /**
         * @see java.io.Writer#close()
         */
        public void close() throws IOException
        {
        }

        public void write(int i)
        {
            m_stringbuf.append((char) i);
        }
        
        public void write(String s)
        {
            m_stringbuf.append(s);
        }
    }

    /**
     * @see SerializationHandler#setTransformer(Transformer)
     */
    public void setTransformer(Transformer transformer) {
        super.setTransformer(transformer);
        if (m_tracer != null
         && !(m_writer instanceof SerializerTraceWriter)  )
            setWriterInternal(new SerializerTraceWriter(m_writer, m_tracer), false);        
        
        
    }
    /**
     * Try's to reset the super class and reset this class for 
     * re-use, so that you don't need to create a new serializer 
     * (mostly for performance reasons).
     * 
     * @return true if the class was successfuly reset.
     */
    public boolean reset()
    {
        boolean wasReset = false;
        if (super.reset())
        {
            resetToStream();
            wasReset = true;
        }
        return wasReset;
    }
    
    /**
     * Reset all of the fields owned by ToStream class
     *
     */
    private void resetToStream()
    {
         this.m_cdataStartCalled = false;
         /* The stream is being reset. It is one of
          * ToXMLStream, ToHTMLStream ... and this type can't be changed
          * so neither should m_charInfo which is associated with the
          * type of Stream. Just leave m_charInfo as-is for the next re-use.
          * 
          */
         // this.m_charInfo = null; // don't set to null 
         this.m_disableOutputEscapingStates.clear();
         // this.m_encodingInfo = null; // don't set to null
         
         this.m_escaping = true;
         // Leave m_format alone for now - Brian M.
         // this.m_format = null;
         this.m_expandDTDEntities = true; 
         this.m_inDoctype = false;
         this.m_ispreserve = false;
         this.m_isprevtext = false;
         this.m_isUTF8 = false; //  ?? used anywhere ??
         this.m_lineSep = s_systemLineSep;
         this.m_lineSepLen = s_systemLineSep.length;
         this.m_lineSepUse = true;
         // this.m_outputStream = null; // Don't reset it may be re-used
         this.m_preserves.clear();
         this.m_shouldFlush = true;
         this.m_spaceBeforeClose = false;
         this.m_startNewLine = false;
         this.m_writer_set_by_user = false;
    }        
    
    /**
      * Sets the character encoding coming from the xsl:output encoding stylesheet attribute.
      * @param encoding the character encoding
      */
     public void setEncoding(String encoding)
     {
         setOutputProperty(OutputKeys.ENCODING,encoding);
     }
     
    /**
     * Simple stack for boolean values.
     * 
     * This class is a copy of the one in org.apache.xml.utils. 
     * It exists to cut the serializers dependancy on that package.
     * A minor changes from that package are:
     * doesn't implement Clonable
     *  
     * @xsl.usage internal
     */
    static final class BoolStack
    {

      /** Array of boolean values          */
      private boolean m_values[];

      /** Array size allocated           */
      private int m_allocatedSize;

      /** Index into the array of booleans          */
      private int m_index;

      /**
       * Default constructor.  Note that the default
       * block size is very small, for small lists.
       */
      public BoolStack()
      {
        this(32);
      }

      /**
       * Construct a IntVector, using the given block size.
       *
       * @param size array size to allocate
       */
      public BoolStack(int size)
      {

        m_allocatedSize = size;
        m_values = new boolean[size];
        m_index = -1;
      }

      /**
       * Get the length of the list.
       *
       * @return Current length of the list
       */
      public final int size()
      {
        return m_index + 1;
      }

      /**
       * Clears the stack.
       *
       */
      public final void clear()
      {
        m_index = -1;
      }

      /**
       * Pushes an item onto the top of this stack.
       *
       *
       * @param val the boolean to be pushed onto this stack.
       * @return  the <code>item</code> argument.
       */
      public final boolean push(boolean val)
      {

        if (m_index == m_allocatedSize - 1)
          grow();

        return (m_values[++m_index] = val);
      }

      /**
       * Removes the object at the top of this stack and returns that
       * object as the value of this function.
       *
       * @return     The object at the top of this stack.
       * @throws  EmptyStackException  if this stack is empty.
       */
      public final boolean pop()
      {
        return m_values[m_index--];
      }

      /**
       * Removes the object at the top of this stack and returns the
       * next object at the top as the value of this function.
       *
       *
       * @return Next object to the top or false if none there
       */
      public final boolean popAndTop()
      {

        m_index--;

        return (m_index >= 0) ? m_values[m_index] : false;
      }

      /**
       * Set the item at the top of this stack  
       *
       *
       * @param b Object to set at the top of this stack
       */
      public final void setTop(boolean b)
      {
        m_values[m_index] = b;
      }

      /**
       * Looks at the object at the top of this stack without removing it
       * from the stack.
       *
       * @return     the object at the top of this stack.
       * @throws  EmptyStackException  if this stack is empty.
       */
      public final boolean peek()
      {
        return m_values[m_index];
      }

      /**
       * Looks at the object at the top of this stack without removing it
       * from the stack.  If the stack is empty, it returns false.
       *
       * @return     the object at the top of this stack.
       */
      public final boolean peekOrFalse()
      {
        return (m_index > -1) ? m_values[m_index] : false;
      }

      /**
       * Looks at the object at the top of this stack without removing it
       * from the stack.  If the stack is empty, it returns true.
       *
       * @return     the object at the top of this stack.
       */
      public final boolean peekOrTrue()
      {
        return (m_index > -1) ? m_values[m_index] : true;
      }

      /**
       * Tests if this stack is empty.
       *
       * @return  <code>true</code> if this stack is empty;
       *          <code>false</code> otherwise.
       */
      public boolean isEmpty()
      {
        return (m_index == -1);
      }

      /**
       * Grows the size of the stack
       *
       */
      private void grow()
      {

        m_allocatedSize *= 2;

        boolean newVector[] = new boolean[m_allocatedSize];

        System.arraycopy(m_values, 0, newVector, 0, m_index + 1);

        m_values = newVector;
      }
    }
    
    // Implement DTDHandler
    /**
     * If this method is called, the serializer is used as a
     * DTDHandler, which changes behavior how the serializer 
     * handles document entities. 
     * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
     */
    public void notationDecl(String name, String pubID, String sysID) throws SAXException {
        // TODO Auto-generated method stub
        try {
            DTDprolog();
            
            m_writer.write("<!NOTATION ");            
            m_writer.write(name);
            if (pubID != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
  
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * If this method is called, the serializer is used as a
     * DTDHandler, which changes behavior how the serializer 
     * handles document entities. 
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unparsedEntityDecl(String name, String pubID, String sysID, String notationName) throws SAXException {
        // TODO Auto-generated method stub
        try {
            DTDprolog();       
            
            m_writer.write("<!ENTITY ");            
            m_writer.write(name);
            if (pubID != null) {
                m_writer.write(" PUBLIC \"");
                m_writer.write(pubID);
  
            }
            else {
                m_writer.write(" SYSTEM \"");
                m_writer.write(sysID);
            }
            m_writer.write("\" NDATA ");
            m_writer.write(notationName);
            m_writer.write(" >");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }
    
    /**
     * A private helper method to output the 
     * @throws SAXException
     * @throws IOException
     */
    private void DTDprolog() throws SAXException, IOException {
        final java.io.Writer writer = m_writer;
        if (m_needToOutputDocTypeDecl)
        {
            outputDocTypeDecl(m_elemContext.m_elementName, false);
            m_needToOutputDocTypeDecl = false;
        }
        if (m_inDoctype)
        {
            writer.write(" [");
            writer.write(m_lineSep, 0, m_lineSepLen);
            m_inDoctype = false;
        }
    }
    
    /**
     * If set to false the serializer does not expand DTD entities,
     * but leaves them as is, the default value is true;
     */
    public void setDTDEntityExpansion(boolean expand) { 
        m_expandDTDEntities = expand;     
    }
        
    /**
     * Sets the end of line characters to be used during serialization
     * @param eolChars A character array corresponding to the characters to be used.
     */    
    public void setNewLine (char[] eolChars) {
        m_lineSep = eolChars;
        m_lineSepLen = eolChars.length;
    }

    /**
     * Remembers the cdata sections specified in the cdata-section-elements by appending the given
     * cdata section elements to the list. This method can be called multiple times, but once an
     * element is put in the list of cdata section elements it can not be removed.
     * This method should be used by both Xalan and XSLTC.
     * 
     * @param URI_and_localNames a whitespace separated list of element names, each element
     * is a URI in curly braces (optional) and a local name. An example of such a parameter is:
     * "{http://company.com}price {myURI2}book chapter"
     */
    public void addCdataSectionElements(String URI_and_localNames)
    {
        if (URI_and_localNames != null)
            initCdataElems(URI_and_localNames);
        if (m_StringOfCDATASections == null)
            m_StringOfCDATASections = URI_and_localNames;
        else
            m_StringOfCDATASections += (" " + URI_and_localNames);
    }
}
