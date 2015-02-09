/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $Id: TransformerFactory.java 884963 2009-11-27 19:11:59Z mrglavas $

package javax.xml.transform;

/**
 * <p>A TransformerFactory instance can be used to create
 * {@link javax.xml.transform.Transformer} and
 * {@link javax.xml.transform.Templates} objects.</p>
 *
 * <p>The system property that determines which Factory implementation
 * to create is named <code>"javax.xml.transform.TransformerFactory"</code>.
 * This property names a concrete subclass of the
 * <code>TransformerFactory</code> abstract class. If the property is not
 * defined, a platform default is be used.</p>
 *
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 */
public abstract class TransformerFactory {

    /**
     * Default constructor is protected on purpose.
     */
    protected TransformerFactory() { }


    /**
     * <p>Get current state of canonicalization.</p>
     *
     * @return current state canonicalization control
     */
    /*
    public boolean getCanonicalization() {
        return canonicalState;
    }
    */

    /**
     * <p>Set canonicalization control to <code>true</code> or
     * </code>false</code>.</p>
     *
     * @param state of canonicalization
     */
    /*
    public void setCanonicalization(boolean state) {
        canonicalState = state;
    }
    */

    /**
     * Returns Android's implementation of {@code TransformerFactory}. Unlike
     * other Java implementations, this method does not consult system
     * properties, properties files, or the services API.
     *
     * @throws TransformerFactoryConfigurationError never. Included for API
     *     compatibility with other Java implementations.
     */
    public static TransformerFactory newInstance()
            throws TransformerFactoryConfigurationError {
        String className = "org.apache.xalan.processor.TransformerFactoryImpl";
        try {
            return (TransformerFactory) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new NoClassDefFoundError(className);
        }
    }

    /**
     * Returns an instance of the named implementation of {@code TransformerFactory}.
     *
     * @throws TransformerFactoryConfigurationError if {@code factoryClassName} is not available or
     *     cannot be instantiated.
     * @since 1.6
     */
    public static TransformerFactory newInstance(String factoryClassName, ClassLoader classLoader)
            throws TransformerFactoryConfigurationError {
        if (factoryClassName == null) {
            throw new TransformerFactoryConfigurationError("factoryClassName == null");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            Class<?> type = classLoader != null
                    ? classLoader.loadClass(factoryClassName)
                    : Class.forName(factoryClassName);
            return (TransformerFactory) type.newInstance();
        } catch (ClassNotFoundException e) {
            throw new TransformerFactoryConfigurationError(e);
        } catch (InstantiationException e) {
            throw new TransformerFactoryConfigurationError(e);
        } catch (IllegalAccessException e) {
            throw new TransformerFactoryConfigurationError(e);
        }
    }

    /**
     * <p>Process the <code>Source</code> into a <code>Transformer</code>
     * <code>Object</code>.  The <code>Source</code> is an XSLT document that
     * conforms to <a href="http://www.w3.org/TR/xslt">
     * XSL Transformations (XSLT) Version 1.0</a>.  Care must
     * be taken not to use this <code>Transformer</code> in multiple
     * <code>Thread</code>s running concurrently.
     * Different <code>TransformerFactories</code> can be used concurrently by
     * different <code>Thread</code>s.</p>
     *
     * @param source <code>Source </code> of XSLT document used to create
     *   <code>Transformer</code>.
     *   Examples of XML <code>Source</code>s include
     *   {@link javax.xml.transform.stream.StreamSource StreamSource},
     *   {@link javax.xml.transform.sax.SAXSource SAXSource} and
     *   {@link javax.xml.transform.dom.DOMSource DOMSource}.
     *
     * @return A <code>Transformer</code> object that may be used to perform
     *   a transformation in a single <code>Thread</code>, never
     *   <code>null</code>.
     *
     * @throws TransformerConfigurationException Thrown if there are errors when
     *    parsing the <code>Source</code> or it is not possible to create a
     *   <code>Transformer</code> instance.
     *
     * @see <a href="http://www.w3.org/TR/xslt">
     *   XSL Transformations (XSLT) Version 1.0</a>
     */
    public abstract Transformer newTransformer(Source source)
        throws TransformerConfigurationException;

    /**
     * <p>Create a new <code>Transformer</code> that performs a copy
     * of the <code>Source</code> to the <code>Result</code>.
     * i.e. the "<em>identity transform</em>".</p>
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     *
     * @exception TransformerConfigurationException Thrown if it is not
     *   possible to create a <code>Transformer</code> instance.
     */
    public abstract Transformer newTransformer()
        throws TransformerConfigurationException;

    /**
     * Process the Source into a Templates object, which is a
     * a compiled representation of the source. This Templates object
     * may then be used concurrently across multiple threads.  Creating
     * a Templates object allows the TransformerFactory to do detailed
     * performance optimization of transformation instructions, without
     * penalizing runtime transformation.
     *
     * @param source An object that holds a URL, input stream, etc.
     *
     * @return A Templates object capable of being used for transformation
     * purposes, never null.
     *
     * @exception TransformerConfigurationException May throw this during the
     * parse when it is constructing the Templates object and fails.
     */
    public abstract Templates newTemplates(Source source)
        throws TransformerConfigurationException;

    /**
     * <p>Get the stylesheet specification(s) associated with the
     * XML <code>Source</code> document via the
     * <a href="http://www.w3.org/TR/xml-stylesheet/">
     * xml-stylesheet processing instruction</a> that match the given criteria.
     * Note that it is possible to return several stylesheets, in which case
     * they are applied as if they were a list of imports or cascades in a
     * single stylesheet.</p>
     *
     * @param source The XML source document.
     * @param media The media attribute to be matched.  May be null, in which
     *      case the preferred templates will be used (i.e. alternate = no).
     * @param title The value of the title attribute to match.  May be null.
     * @param charset The value of the charset attribute to match.  May be null.
     *
     * @return A <code>Source</code> <code>Object</code> suitable for passing
     *   to the <code>TransformerFactory</code>.
     *
     * @throws TransformerConfigurationException An <code>Exception</code>
     *   is thrown if an error occurs during parsing of the
     *   <code>source</code>.
     *
     * @see <a href="http://www.w3.org/TR/xml-stylesheet/">
     *   Associating Style Sheets with XML documents Version 1.0</a>
     */
    public abstract Source getAssociatedStylesheet(
        Source source,
        String media,
        String title,
        String charset)
        throws TransformerConfigurationException;

    /**
     * Set an object that is used by default during the transformation
     * to resolve URIs used in document(), xsl:import, or xsl:include.
     *
     * @param resolver An object that implements the URIResolver interface,
     * or null.
     */
    public abstract void setURIResolver(URIResolver resolver);

    /**
     * Get the object that is used by default during the transformation
     * to resolve URIs used in document(), xsl:import, or xsl:include.
     *
     * @return The URIResolver that was set with setURIResolver.
     */
    public abstract URIResolver getURIResolver();

    //======= CONFIGURATION METHODS =======

    /**
     * <p>Set a feature for this <code>TransformerFactory</code> and <code>Transformer</code>s
     * or <code>Template</code>s created by this factory.</p>
     *
     * <p>
     * Feature names are fully qualified {@link java.net.URI}s.
     * Implementations may define their own features.
     * An {@link TransformerConfigurationException} is thrown if this <code>TransformerFactory</code> or the
     * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature.
     * It is possible for an <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     *
     * <p>All implementations are required to support the {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} feature.
     * When the feature is:</p>
     * <ul>
     *   <li>
     *     <code>true</code>: the implementation will limit XML processing to conform to implementation limits
     *     and behave in a secure fashion as defined by the implementation.
     *     Examples include resolving user defined style sheets and functions.
     *     If XML processing is limited for security reasons, it will be reported via a call to the registered
     *     {@link ErrorListener#fatalError(TransformerException exception)}.
     *     See {@link  #setErrorListener(ErrorListener listener)}.
     *   </li>
     *   <li>
     *     <code>false</code>: the implementation will processing XML according to the XML specifications without
     *     regard to possible implementation limits.
     *   </li>
     * </ul>
     *
     * @param name Feature name.
     * @param value Is feature state <code>true</code> or <code>false</code>.
     *
     * @throws TransformerConfigurationException if this <code>TransformerFactory</code>
     *   or the <code>Transformer</code>s or <code>Template</code>s it creates cannot support this feature.
     * @throws NullPointerException If the <code>name</code> parameter is null.
     */
    public abstract void setFeature(String name, boolean value)
        throws TransformerConfigurationException;

    /**
     * Look up the value of a feature.
     *
     * <p>
     * Feature names are fully qualified {@link java.net.URI}s.
     * Implementations may define their own features.
     * <code>false</code> is returned if this <code>TransformerFactory</code> or the
     * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature.
     * It is possible for an <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     *
     * @param name Feature name.
     *
     * @return The current state of the feature, <code>true</code> or <code>false</code>.
     *
     * @throws NullPointerException If the <code>name</code> parameter is null.
     */
    public abstract boolean getFeature(String name);

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.  An attribute in this context is defined to
     * be an option that the implementation provides.
     * An <code>IllegalArgumentException</code> is thrown if the underlying
     * implementation doesn't recognize the attribute.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public abstract void setAttribute(String name, Object value);

    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     * An <code>IllegalArgumentException</code> is thrown if the underlying
     * implementation doesn't recognize the attribute.
     *
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     */
    public abstract Object getAttribute(String name);

    /**
     * Set the error event listener for the TransformerFactory, which
     * is used for the processing of transformation instructions,
     * and not for the transformation itself.
     * An <code>IllegalArgumentException</code> is thrown if the
     * <code>ErrorListener</code> listener is <code>null</code>.
     *
     * @param listener The new error listener.
     */
    public abstract void setErrorListener(ErrorListener listener);

    /**
     * Get the error event handler for the TransformerFactory.
     *
     * @return The current error handler, which should never be null.
     */
    public abstract ErrorListener getErrorListener();

}

