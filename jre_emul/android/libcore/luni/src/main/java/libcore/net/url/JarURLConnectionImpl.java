/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package libcore.net.url;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import libcore.net.UriCodec;

/**
 * This subclass extends {@code URLConnection}.
 * <p>
 *
 * This class is responsible for connecting and retrieving resources from a Jar
 * file which can be anywhere that can be referred to by an URL.
 */
public class JarURLConnectionImpl extends JarURLConnection {

    private static final HashMap<URL, JarFile> jarCache = new HashMap<URL, JarFile>();

    private URL jarFileURL;

    private InputStream jarInput;

    private JarFile jarFile;

    private JarEntry jarEntry;

    private boolean closed;

    /**
     * @param url
     *            the URL of the JAR
     * @throws MalformedURLException
     *             if the URL is malformed
     * @throws IOException
     *             if there is a problem opening the connection.
     */
    public JarURLConnectionImpl(URL url) throws MalformedURLException, IOException {
        super(url);
        jarFileURL = getJarFileURL();
        jarFileURLConnection = jarFileURL.openConnection();
    }

    /**
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (!connected) {
            findJarFile(); // ensure the file can be found
            findJarEntry(); // ensure the entry, if any, can be found
            connected = true;
        }
    }

    /**
     * Returns the Jar file referred by this {@code URLConnection}.
     *
     * @throws IOException
     *             thrown if an IO error occurs while connecting to the
     *             resource.
     */
    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }

    /**
     * Returns the Jar file referred by this {@code URLConnection}
     *
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    private void findJarFile() throws IOException {
        if (getUseCaches()) {
            synchronized (jarCache) {
                jarFile = jarCache.get(jarFileURL);
            }
            if (jarFile == null) {
                JarFile jar = openJarFile();
                synchronized (jarCache) {
                    jarFile = jarCache.get(jarFileURL);
                    if (jarFile == null) {
                        jarCache.put(jarFileURL, jar);
                        jarFile = jar;
                    } else {
                        jar.close();
                    }
                }
            }
        } else {
            jarFile = openJarFile();
        }

        if (jarFile == null) {
            throw new IOException();
        }
    }

    private JarFile openJarFile() throws IOException {
        if (jarFileURL.getProtocol().equals("file")) {
            String decodedFile = UriCodec.decode(jarFileURL.getFile());
            return new JarFile(new File(decodedFile), true, ZipFile.OPEN_READ);
        } else {
            final InputStream is = jarFileURL.openConnection().getInputStream();
            try {
                FileOutputStream fos = null;
                JarFile result = null;
                try {
                    File tempJar = File.createTempFile("hyjar_", ".tmp", null);
                    tempJar.deleteOnExit();
                    fos = new FileOutputStream(tempJar);
                    byte[] buf = new byte[4096];
                    int nbytes = 0;
                    while ((nbytes = is.read(buf)) > -1) {
                        fos.write(buf, 0, nbytes);
                    }
                    fos.close();
                    return new JarFile(tempJar, true, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
                } catch (IOException e) {
                    return null;
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ex) {
                            return null;
                        }
                    }
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    /**
     * Returns the JarEntry of the entry referenced by this {@code
     * URLConnection}.
     *
     * @return the JarEntry referenced
     *
     * @throws IOException
     *             if an IO error occurs while getting the entry
     */
    @Override
    public JarEntry getJarEntry() throws IOException {
        connect();
        return jarEntry;

    }

    /**
     * Look up the JarEntry of the entry referenced by this {@code
     * URLConnection}.
     */
    private void findJarEntry() throws IOException {
        if (getEntryName() == null) {
            return;
        }
        jarEntry = jarFile.getJarEntry(getEntryName());
        if (jarEntry == null) {
            throw new FileNotFoundException(getEntryName());
        }
    }

    /**
     * Creates an input stream for reading from this URL Connection.
     *
     * @return the input stream
     *
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (closed) {
            throw new IllegalStateException("JarURLConnection InputStream has been closed");
        }
        connect();
        if (jarInput != null) {
            return jarInput;
        }
        if (jarEntry == null) {
            throw new IOException("Jar entry not specified");
        }
        return jarInput = new JarURLConnectionInputStream(jarFile
                .getInputStream(jarEntry), jarFile);
    }

    /**
     * Returns the content type of the resource. For jar file itself
     * "x-java/jar" should be returned, for jar entries the content type of the
     * entry should be returned. Returns non-null results ("content/unknown" for
     * unknown types).
     *
     * @return the content type
     */
    @Override
    public String getContentType() {
        if (url.getFile().endsWith("!/")) {
            // the type for jar file itself is always "x-java/jar"
            return "x-java/jar";
        }
        String cType = null;
        String entryName = getEntryName();

        if (entryName != null) {
            // if there is an Jar Entry, get the content type from the name
            cType = guessContentTypeFromName(entryName);
        } else {
            try {
                connect();
                cType = jarFileURLConnection.getContentType();
            } catch (IOException ioe) {
                // Ignore
            }
        }
        if (cType == null) {
            cType = "content/unknown";
        }
        return cType;
    }

    /**
     * Returns the content length of the resource. Test cases reveal that if the URL is referring to
     * a Jar file, this method answers a content-length returned by URLConnection. For a jar entry
     * it returns the entry's size if it can be represented as an {@code int}. Otherwise, it will
     * return -1.
     */
    @Override
    public int getContentLength() {
        try {
            connect();
            if (jarEntry == null) {
                return jarFileURLConnection.getContentLength();
            }
            return (int) getJarEntry().getSize();
        } catch (IOException e) {
            // Ignored
        }
        return -1;
    }

    /**
     * Returns the object pointed by this {@code URL}. If this URLConnection is
     * pointing to a Jar File (no Jar Entry), this method will return a {@code
     * JarFile} If there is a Jar Entry, it will return the object corresponding
     * to the Jar entry content type.
     *
     * @return a non-null object
     *
     * @throws IOException
     *             if an IO error occurred
     *
     * @see ContentHandler
     * @see ContentHandlerFactory
     * @see java.io.IOException
     * @see #setContentHandlerFactory(ContentHandlerFactory)
     */
    @Override
    public Object getContent() throws IOException {
        connect();
        // if there is no Jar Entry, return a JarFile
        if (jarEntry == null) {
            return jarFile;
        }
        return super.getContent();
    }

    /**
     * Returns the permission, in this case the subclass, FilePermission object
     * which represents the permission necessary for this URLConnection to
     * establish the connection.
     *
     * @return the permission required for this URLConnection.
     *
     * @throws IOException
     *             thrown when an IO exception occurs while creating the
     *             permission.
     */

    @Override
    public Permission getPermission() throws IOException {
        return jarFileURLConnection.getPermission();
    }

    @Override
    public boolean getUseCaches() {
        return jarFileURLConnection.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        jarFileURLConnection.setUseCaches(usecaches);
    }

    @Override
    public boolean getDefaultUseCaches() {
        return jarFileURLConnection.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        jarFileURLConnection.setDefaultUseCaches(defaultusecaches);
    }

    private class JarURLConnectionInputStream extends FilterInputStream {
        final JarFile jarFile;

        protected JarURLConnectionInputStream(InputStream in, JarFile file) {
            super(in);
            jarFile = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (!getUseCaches()) {
                closed = true;
                jarFile.close();
            }
        }
    }
}
