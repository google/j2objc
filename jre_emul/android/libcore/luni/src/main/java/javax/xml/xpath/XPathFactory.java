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
// $Id: XPathFactory.java 888889 2009-12-09 17:43:18Z mrglavas $

package javax.xml.xpath;

/**
 * <p>An <code>XPathFactory</code> instance can be used to create
 * {@link javax.xml.xpath.XPath} objects.</p>
 *
 *<p>See {@link #newInstance(String uri)} for lookup mechanism.</p>
 *
 * @author  <a href="mailto:Norman.Walsh@Sun.com">Norman Walsh</a>
 * @author  <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 888889 $, $Date: 2009-12-09 09:43:18 -0800 (Wed, 09 Dec 2009) $
 * @since 1.5
 */
public abstract class XPathFactory {


    /**
     * <p>The default property name according to the JAXP spec.</p>
     */
    public static final String DEFAULT_PROPERTY_NAME = "javax.xml.xpath.XPathFactory";

    /**
     * <p>Default Object Model URI.</p>
     */
    public static final String DEFAULT_OBJECT_MODEL_URI = "http://java.sun.com/jaxp/xpath/dom";

    /**
     * <p>Protected constructor as {@link #newInstance()}, {@link #newInstance(String uri)}
     * or {@link #newInstance(String uri, String factoryClassName, ClassLoader classLoader)}
     * should be used to create a new instance of an <code>XPathFactory</code>.</p>
     */
    protected XPathFactory() {
    }

    /**
     * <p>Get a new <code>XPathFactory</code> instance using the default object model,
     * {@link #DEFAULT_OBJECT_MODEL_URI},
     * the W3C DOM.</p>
     *
     * <p>This method is functionally equivalent to:</p>
     * <pre>
     *   newInstance(DEFAULT_OBJECT_MODEL_URI)
     * </pre>
     *
     * <p>Since the implementation for the W3C DOM is always available, this method will never fail.</p>
     *
     * @return Instance of an <code>XPathFactory</code>.
     */
    public static final XPathFactory newInstance() {
        try {
            return newInstance(DEFAULT_OBJECT_MODEL_URI);
        }
        catch (XPathFactoryConfigurationException xpathFactoryConfigurationException) {
            throw new RuntimeException(
                "XPathFactory#newInstance() failed to create an XPathFactory for the default object model: "
                + DEFAULT_OBJECT_MODEL_URI
                + " with the XPathFactoryConfigurationException: "
                + xpathFactoryConfigurationException.toString()
            );
        }
    }

    /**
    * <p>Get a new <code>XPathFactory</code> instance using the specified object model.</p>
    *
    * <p>To find a <code>XPathFactory</code> object,
    * this method looks the following places in the following order where "the class loader" refers to the context class loader:</p>
    * <ol>
    *   <li>
    *     If the system property {@link #DEFAULT_PROPERTY_NAME} + ":uri" is present,
    *     where uri is the parameter to this method, then its value is read as a class name.
    *     The method will try to create a new instance of this class by using the class loader,
    *     and returns it if it is successfully created.
    *   </li>
    *   <li>
    *     ${java.home}/lib/jaxp.properties is read and the value associated with the key being the system property above is looked for.
    *     If present, the value is processed just like above.
    *   </li>
    *   <li>
    *     The class loader is asked for service provider provider-configuration files matching <code>javax.xml.xpath.XPathFactory</code>
    *     in the resource directory META-INF/services.
    *     See the JAR File Specification for file format and parsing rules.
    *     Each potential service provider is required to implement the method:
    *     <pre>
    *       {@link #isObjectModelSupported(String objectModel)}
    *     </pre>
    *     The first service provider found in class loader order that supports the specified object model is returned.
    *   </li>
    *   <li>
    *     Platform default <code>XPathFactory</code> is located in a platform specific way.
    *     There must be a platform default XPathFactory for the W3C DOM, i.e. {@link #DEFAULT_OBJECT_MODEL_URI}.
    *   </li>
    * </ol>
    * <p>If everything fails, an <code>XPathFactoryConfigurationException</code> will be thrown.</p>
    *
    * <p>Tip for Trouble-shooting:</p>
    * <p>See {@link java.util.Properties#load(java.io.InputStream)} for exactly how a property file is parsed.
    * In particular, colons ':' need to be escaped in a property file, so make sure the URIs are properly escaped in it.
    * For example:</p>
    * <pre>
    *   http\://java.sun.com/jaxp/xpath/dom=org.acme.DomXPathFactory
    * </pre>
    *
    * @param uri Identifies the underlying object model.
    *   The specification only defines the URI {@link #DEFAULT_OBJECT_MODEL_URI},
    *   <code>http://java.sun.com/jaxp/xpath/dom</code> for the W3C DOM,
    *   the org.w3c.dom package, and implementations are free to introduce other URIs for other object models.
    *
    * @return Instance of an <code>XPathFactory</code>.
    *
    * @throws XPathFactoryConfigurationException If the specified object model is unavailable.
    * @throws NullPointerException If <code>uri</code> is <code>null</code>.
    * @throws IllegalArgumentException If <code>uri.length() == 0</code>.
    */
    public static final XPathFactory newInstance(final String uri)
        throws XPathFactoryConfigurationException {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        if (uri.length() == 0) {
            throw new IllegalArgumentException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == \"\""
            );
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            //use the current class loader
            classLoader = XPathFactory.class.getClassLoader();
        }
        XPathFactory xpathFactory = new XPathFactoryFinder(classLoader).newFactory(uri);
        if (xpathFactory == null) {
            throw new XPathFactoryConfigurationException(
                "No XPathFactory implementation found for the object model: "
                + uri
            );
        }
        return xpathFactory;
    }

    /**
     * @return Instance of an <code>XPathFactory</code>.
     *
     * @throws XPathFactoryConfigurationException If the specified object model is unavailable.
     * @throws NullPointerException If <code>uri</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>uri.length() == 0</code>.
     */
    public static XPathFactory newInstance(String uri, String factoryClassName,
            ClassLoader classLoader) throws XPathFactoryConfigurationException {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        if (uri.length() == 0) {
            throw new IllegalArgumentException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == \"\""
            );
        }
        if (factoryClassName == null) {
            throw new XPathFactoryConfigurationException("factoryClassName cannot be null.");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        XPathFactory xpathFactory = new XPathFactoryFinder(classLoader).createInstance(factoryClassName);
        if (xpathFactory == null || !xpathFactory.isObjectModelSupported(uri)) {
            throw new XPathFactoryConfigurationException(
                "No XPathFactory implementation found for the object model: "
                + uri
            );
        }
        return xpathFactory;
    }

    /**
     * <p>Is specified object model supported by this <code>XPathFactory</code>?</p>
     *
     * @param objectModel Specifies the object model which the returned <code>XPathFactory</code> will understand.
     *
     * @return <code>true</code> if <code>XPathFactory</code> supports <code>objectModel</code>, else <code>false</code>.
     *
     * @throws NullPointerException If <code>objectModel</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>objectModel.length() == 0</code>.
     */
    public abstract boolean isObjectModelSupported(String objectModel);

    /**
     * <p>Set a feature for this <code>XPathFactory</code> and <code>XPath</code>s created by this factory.</p>
     *
     * <p>
     * Feature names are fully qualified {@link java.net.URI}s.
     * Implementations may define their own features.
     * An {@link XPathFactoryConfigurationException} is thrown if this <code>XPathFactory</code> or the <code>XPath</code>s
     *  it creates cannot support the feature.
     * It is possible for an <code>XPathFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     *
     * <p>
     * All implementations are required to support the {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} feature.
     * When the feature is <code>true</code>, any reference to  an external function is an error.
     * Under these conditions, the implementation must not call the {@link XPathFunctionResolver}
     * and must throw an {@link XPathFunctionException}.
     * </p>
     *
     * @param name Feature name.
     * @param value Is feature state <code>true</code> or <code>false</code>.
     *
     * @throws XPathFactoryConfigurationException if this <code>XPathFactory</code> or the <code>XPath</code>s
     *   it creates cannot support this feature.
     * @throws NullPointerException if <code>name</code> is <code>null</code>.
     */
    public abstract void setFeature(String name, boolean value)
        throws XPathFactoryConfigurationException;

    /**
     * <p>Get the state of the named feature.</p>
     *
     * <p>
     * Feature names are fully qualified {@link java.net.URI}s.
     * Implementations may define their own features.
     * An {@link XPathFactoryConfigurationException} is thrown if this <code>XPathFactory</code> or the <code>XPath</code>s
     * it creates cannot support the feature.
     * It is possible for an <code>XPathFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     *
     * @param name Feature name.
     *
     * @return State of the named feature.
     *
     * @throws XPathFactoryConfigurationException if this <code>XPathFactory</code> or the <code>XPath</code>s
     *   it creates cannot support this feature.
     * @throws NullPointerException if <code>name</code> is <code>null</code>.
     */
    public abstract boolean getFeature(String name)
        throws XPathFactoryConfigurationException;

    /**
     * <p>Establish a default variable resolver.</p>
     *
     * <p>Any <code>XPath</code> objects constructed from this factory will use
     * the specified resolver by default.</p>
     *
     * <p>A <code>NullPointerException</code> is thrown if <code>resolver</code> is <code>null</code>.</p>
     *
     * @param resolver Variable resolver.
     *
     *  @throws NullPointerException If <code>resolver</code> is <code>null</code>.
     */
    public abstract void setXPathVariableResolver(XPathVariableResolver resolver);

    /**
       * <p>Establish a default function resolver.</p>
       *
       * <p>Any <code>XPath</code> objects constructed from this factory will use
       * the specified resolver by default.</p>
       *
       * <p>A <code>NullPointerException</code> is thrown if <code>resolver</code> is <code>null</code>.</p>
       *
       * @param resolver XPath function resolver.
       *
       * @throws NullPointerException If <code>resolver</code> is <code>null</code>.
       */
    public abstract void setXPathFunctionResolver(XPathFunctionResolver resolver);

    /**
    * <p>Return a new <code>XPath</code> using the underlying object
    * model determined when the <code>XPathFactory</code> was instantiated.</p>
    *
    * @return New instance of an <code>XPath</code>.
    */
    public abstract XPath newXPath();
}
