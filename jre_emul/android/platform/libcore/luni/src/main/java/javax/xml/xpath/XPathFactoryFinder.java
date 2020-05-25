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
// $Id: XPathFactoryFinder.java 670432 2008-06-23 02:02:08Z mrglavas $

package javax.xml.xpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.validation.SchemaFactory;
import libcore.io.IoUtils;

/**
 * Implementation of {@link XPathFactory#newInstance(String)}.
 *
 * @author <a href="Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @version $Revision: 670432 $, $Date: 2008-06-22 19:02:08 -0700 (Sun, 22 Jun 2008) $
 * @since 1.5
 */
final class XPathFactoryFinder {

    /** debug support code. */
    private static boolean debug = false;

    /**
     * Default columns per line.
     */
    private static final int DEFAULT_LINE_LENGTH = 80;

    static {
        String val = System.getProperty("jaxp.debug");
        // Allow simply setting the prop to turn on debug
        debug = val != null && (! "false".equals(val));
    }

    /**
     * <p>Cache properties for performance. Use a static class to avoid double-checked
     * locking.</p>
     */
    private static class CacheHolder {

        private static Properties cacheProps = new Properties();

        static {
            String javah = System.getProperty("java.home");
            String configFile = javah + File.separator + "lib" + File.separator + "jaxp.properties";
            File f = new File(configFile);
            if (f.exists()) {
                if (debug) debugPrintln("Read properties file " + f);
                try (FileInputStream inputStream = new FileInputStream(f)) {
                    cacheProps.load(inputStream);
                } catch (Exception ex) {
                    if (debug) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * <p>Conditional debug printing.</p>
     *
     * @param msg to print
     */
    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("JAXP: " + msg);
        }
    }

    /**
     * <p><code>ClassLoader</code> to use to find <code>SchemaFactory</code>.</p>
     */
    private final ClassLoader classLoader;

    /**
     * <p>Constructor that specifies <code>ClassLoader</code> to use
     * to find <code>SchemaFactory</code>.</p>
     *
     * @param loader
     *      to be used to load resource, {@link SchemaFactory}, and
     *      {@code SchemaFactoryLoader} implementations during
     *      the resolution process.
     *      If this parameter is null, the default system class loader
     *      will be used.
     */
    public XPathFactoryFinder(ClassLoader loader) {
        this.classLoader = loader;
        if (debug) {
            debugDisplayClassLoader();
        }
    }

    private void debugDisplayClassLoader() {
        if (classLoader == Thread.currentThread().getContextClassLoader()) {
            debugPrintln("using thread context class loader (" + classLoader + ") for search");
            return;
        }

        if (classLoader==ClassLoader.getSystemClassLoader()) {
            debugPrintln("using system class loader (" + classLoader + ") for search");
            return;
        }

        debugPrintln("using class loader (" + classLoader + ") for search");
    }

    /**
     * <p>Creates a new {@link XPathFactory} object for the specified
     * schema language.</p>
     *
     * @param uri
     *       Identifies the underlying object model.
     *
     * @return <code>null</code> if the callee fails to create one.
     *
     * @throws NullPointerException
     *      If the parameter is null.
     */
    public XPathFactory newFactory(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        XPathFactory f = _newFactory(uri);
        if (debug) {
            if (f != null) {
                debugPrintln("factory '" + f.getClass().getName() + "' was found for " + uri);
            } else {
                debugPrintln("unable to find a factory for " + uri);
            }
        }
        return f;
    }

    /**
     * <p>Lookup a {@link XPathFactory} for the given object model.</p>
     *
     * @param uri identifies the object model.
     */
    private XPathFactory _newFactory(String uri) {
        XPathFactory xpf;
        String propertyName = SERVICE_CLASS.getName() + ":" + uri;

        // system property look up
        try {
            if (debug) debugPrintln("Looking up system property '"+propertyName+"'" );
            String r = System.getProperty(propertyName);
            if (r != null && r.length() > 0) {
                if (debug) debugPrintln("The value is '"+r+"'");
                xpf = createInstance(r);
                if(xpf!=null)    return xpf;
            } else if (debug) {
                debugPrintln("The property is undefined.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // try to read from $java.home/lib/jaxp.properties
        try {
            String factoryClassName = CacheHolder.cacheProps.getProperty(propertyName);
            if (debug) debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties");

            if (factoryClassName != null) {
                xpf = createInstance(factoryClassName);
                if(xpf != null){
                    return xpf;
                }
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }

        // try META-INF/services files
        for (URL resource : createServiceFileIterator()) {
            if (debug) debugPrintln("looking into " + resource);
            try {
                xpf = loadFromServicesFile(uri, resource.toExternalForm(), resource.openStream());
                if(xpf!=null)    return xpf;
            } catch(IOException e) {
                if( debug ) {
                    debugPrintln("failed to read "+resource);
                    e.printStackTrace();
                }
            }
        }

        // platform default
        if(uri.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI)) {
            if (debug) debugPrintln("attempting to use the platform default W3C DOM XPath lib");
            return createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
        }

        if (debug) debugPrintln("all things were tried, but none was found. bailing out.");
        return null;
    }

    /**
     * <p>Creates an instance of the specified and returns it.</p>
     *
     * @param className
     *      fully qualified class name to be instantiated.
     *
     * @return null
     *      if it fails. Error messages will be printed by this method.
     */
    XPathFactory createInstance( String className ) {
        try {
            if (debug) debugPrintln("instantiating "+className);
            Class clazz;
            if( classLoader!=null )
                clazz = classLoader.loadClass(className);
            else
                clazz = Class.forName(className);
            if(debug)       debugPrintln("loaded it from "+which(clazz));
            Object o = clazz.newInstance();

            if( o instanceof XPathFactory )
                return (XPathFactory)o;

            if (debug) debugPrintln(className+" is not assignable to "+SERVICE_CLASS.getName());
        }
        // The VM ran out of memory or there was some other serious problem. Re-throw.
        catch (VirtualMachineError vme) {
            throw vme;
        }
        // ThreadDeath should always be re-thrown
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable t) {
            if (debug) {
                debugPrintln("failed to instantiate "+className);
                t.printStackTrace();
            }
        }
        return null;
    }

    /** Searches for a XPathFactory for a given uri in a META-INF/services file. */
    private XPathFactory loadFromServicesFile(String uri, String resourceName, InputStream in) {

        if (debug) debugPrintln("Reading " + resourceName );

        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"), DEFAULT_LINE_LENGTH);
        } catch (java.io.UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(in), DEFAULT_LINE_LENGTH);
        }

        String factoryClassName;
        XPathFactory resultFactory = null;
        // See spec for provider-configuration files: http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Provider%20Configuration%20File
        while (true) {
            try {
                factoryClassName = rd.readLine();
            } catch (IOException x) {
                // No provider found
                break;
            }
            if (factoryClassName != null) {
                // Ignore comments in the provider-configuration file
                int hashIndex = factoryClassName.indexOf('#');
                if (hashIndex != -1) {
                    factoryClassName = factoryClassName.substring(0, hashIndex);
                }

                // Ignore leading and trailing whitespace
                factoryClassName = factoryClassName.trim();

                // If there's no text left or if this was a blank line, go to the next one.
                if (factoryClassName.length() == 0) {
                    continue;
                }

                try {
                    // Found the right XPathFactory if its isObjectModelSupported(String uri) method returns true.
                    XPathFactory foundFactory = createInstance(factoryClassName);
                    if (foundFactory.isObjectModelSupported(uri)) {
                        resultFactory = foundFactory;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
            else {
                break;
            }
        }

        IoUtils.closeQuietly(rd);

        return resultFactory;
    }

    /**
     * Returns an {@link Iterator} that enumerates all
     * the META-INF/services files that we care.
     */
    private Iterable<URL> createServiceFileIterator() {
        if (classLoader == null) {
            URL resource = XPathFactoryFinder.class.getClassLoader().getResource(SERVICE_ID);
            return Collections.singleton(resource);
        } else {
            try {
                Enumeration<URL> e = classLoader.getResources(SERVICE_ID);
                if (debug && !e.hasMoreElements()) {
                    debugPrintln("no "+SERVICE_ID+" file was found");
                }

                return Collections.list(e);
            } catch (IOException e) {
                if (debug) {
                    debugPrintln("failed to enumerate resources "+SERVICE_ID);
                    e.printStackTrace();
                }
                return Collections.emptySet();
            }
        }
    }

    private static final Class SERVICE_CLASS = XPathFactory.class;
    private static final String SERVICE_ID = "META-INF/services/" + SERVICE_CLASS.getName();

    private static String which( Class clazz ) {
        return which( clazz.getName(), clazz.getClassLoader() );
    }

    /**
     * <p>Search the specified classloader for the given classname.</p>
     *
     * @param classname the fully qualified name of the class to search for
     * @param loader the classloader to search
     *
     * @return the source location of the resource, or null if it wasn't found
     */
    private static String which(String classname, ClassLoader loader) {
        String classnameAsResource = classname.replace('.', '/') + ".class";
        if (loader==null) loader = ClassLoader.getSystemClassLoader();

        URL it = loader.getResource(classnameAsResource);
        return it != null ? it.toString() : null;
    }
}
