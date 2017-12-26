/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to create implementations of XML Pull Parser defined in XMPULL V1 API.
 *
 * @see XmlPullParser
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Stefan Haustein
 */

public class XmlPullParserFactory {

    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected ArrayList parserClasses;
    protected ArrayList serializerClasses;

    /** Unused, but we have to keep it because it's public API. */
    protected String classNamesLocation = null;

    // features are kept there
    // TODO: This can't be made final because it's a public API.
    protected HashMap<String, Boolean> features = new HashMap<String, Boolean>();

    /**
     * Protected constructor to be called by factory implementations.
     */
    protected XmlPullParserFactory() {
        parserClasses = new ArrayList<String>();
        serializerClasses = new ArrayList<String>();

        try {
            parserClasses.add(Class.forName("org.kxml2.io.KXmlParser"));
            serializerClasses.add(Class.forName("org.kxml2.io.KXmlSerializer"));
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        }
    }

    /**
     * Set the features to be set when XML Pull Parser is created by this factory.
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @param name string with URI identifying feature
     * @param state if true feature will be set; if false will be ignored
     */
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        features.put(name, state);
    }


    /**
     * Return the current value of the feature with given name.
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @param name The name of feature to be retrieved.
     * @return The value of named feature.
     *     Unknown features are <string>always</strong> returned as false
     */
    public boolean getFeature(String name) {
        Boolean value = features.get(name);
        return value != null ? value.booleanValue() : false;
    }

    /**
     * Specifies that the parser produced by this factory will provide
     * support for XML namespaces.
     * By default the value of this is set to false.
     *
     * @param awareness true if the parser produced by this code
     *    will provide support for XML namespaces;  false otherwise.
     */
    public void setNamespaceAware(boolean awareness) {
        features.put (XmlPullParser.FEATURE_PROCESS_NAMESPACES, awareness);
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which are namespace aware
     * (it simply set feature XmlPullParser.FEATURE_PROCESS_NAMESPACES to true or false).
     *
     * @return  true if the factory is configured to produce parsers
     *    which are namespace aware; false otherwise.
     */
    public boolean isNamespaceAware() {
        return getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
    }

    /**
     * Specifies that the parser produced by this factory will be validating
     * (it simply set feature XmlPullParser.FEATURE_VALIDATION to true or false).
     *
     * By default the value of this is set to false.
     *
     * @param validating - if true the parsers created by this factory  must be validating.
     */
    public void setValidating(boolean validating) {
        features.put(XmlPullParser.FEATURE_VALIDATION, validating);
    }

    /**
     * Indicates whether or not the factory is configured to produce parsers
     * which validate the XML content during parse.
     *
     * @return   true if the factory is configured to produce parsers
     * which validate the XML content during parse; false otherwise.
     */

    public boolean isValidating() {
        return getFeature(XmlPullParser.FEATURE_VALIDATION);
    }

    /**
     * Creates a new instance of a XML Pull Parser
     * using the currently configured factory features.
     *
     * @return A new instance of a XML Pull Parser.
     */
    public XmlPullParser newPullParser() throws XmlPullParserException {
        final XmlPullParser pp = getParserInstance();
        for (Map.Entry<String, Boolean> entry : features.entrySet()) {
            // NOTE: This test is needed for compatibility reasons. We guarantee
            // that we only set a feature on a parser if its value is true.
            if (entry.getValue()) {
                pp.setFeature(entry.getKey(), entry.getValue());
            }
        }

        return pp;
    }

    private XmlPullParser getParserInstance() throws XmlPullParserException {
        ArrayList<Exception> exceptions = null;

        if (parserClasses != null && !parserClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : parserClasses) {
                try {
                    if (o != null) {
                        Class<?> parserClass = (Class<?>) o;
                        return (XmlPullParser) parserClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }

        throw newInstantiationException("Invalid parser class list", exceptions);
    }

    private XmlSerializer getSerializerInstance() throws XmlPullParserException {
        ArrayList<Exception> exceptions = null;

        if (serializerClasses != null && !serializerClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : serializerClasses) {
                try {
                    if (o != null) {
                        Class<?> serializerClass = (Class<?>) o;
                        return (XmlSerializer) serializerClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }

        throw newInstantiationException("Invalid serializer class list", exceptions);
    }

    private static XmlPullParserException newInstantiationException(String message,
            ArrayList<Exception> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return new XmlPullParserException(message);
        } else {
            XmlPullParserException exception = new XmlPullParserException(message);
            for (Exception ex : exceptions) {
                exception.addSuppressed(ex);
            }

            return exception;
        }
    }

    /**
     * Creates a new instance of a XML Serializer.
     *
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @return A new instance of a XML Serializer.
     * @throws XmlPullParserException if a parser cannot be created which satisfies the
     * requested configuration.
     */

    public XmlSerializer newSerializer() throws XmlPullParserException {
        return getSerializerInstance();
    }

    /**
     * Creates a new instance of a PullParserFactory that can be used
     * to create XML pull parsers. The factory will always return instances
     * of Android's built-in {@link XmlPullParser} and {@link XmlSerializer}.
     */
    public static XmlPullParserFactory newInstance () throws XmlPullParserException {
        return new XmlPullParserFactory();
    }

    /**
     * Creates a factory that always returns instances of Android's built-in
     * {@link XmlPullParser} and {@link XmlSerializer} implementation. This
     * <b>does not</b> support factories capable of creating arbitrary parser
     * and serializer implementations. Both arguments to this method are unused.
     */
    public static XmlPullParserFactory newInstance (String unused, Class unused2)
        throws XmlPullParserException {
        return newInstance();
    }
}
