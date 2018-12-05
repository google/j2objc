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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class JarHandler extends URLStreamHandler {
    /**
     * Returns a connection to the jar file pointed by this <code>URL</code>
     * in the file system
     *
     * @return java.net.URLConnection A connection to the resource pointed by
     *         this url.
     * @param u
     *            java.net.URL The URL to which the connection is pointing to
     *
     * @throws IOException
     *             thrown if an IO error occurs when this method tries to
     *             establish connection.
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new JarURLConnectionImpl(u);
    }

    /**
     *
     * @param url
     *            URL the context URL
     * @param spec
     *            java.lang.String the spec string
     * @param start
     *            int the location to start parsing from
     * @param limit
     *            int the location where parsing ends
     */
    @Override
    protected void parseURL(URL url, String spec, int start, int limit) {
        String file = url.getFile();
        if (file == null) {
            file = "";
        }
        if (limit > start) {
            spec = spec.substring(start, limit);
        } else {
            spec = "";
        }
        if (spec.indexOf("!/") == -1 && (file.indexOf("!/") == -1)) {
            throw new NullPointerException("Cannot find \"!/\"");
        }
        if (file.isEmpty()) {
            file = spec;
        } else if (spec.charAt(0) == '/') {
            file = file.substring(0, file.indexOf('!') + 1) + spec;
        } else {
            int idx = file.indexOf('!');
            String tmpFile = file.substring(idx + 1, file.lastIndexOf('/') + 1) + spec;
            tmpFile = UrlUtils.canonicalizePath(tmpFile, true);
            file = file.substring(0, idx + 1) + tmpFile;
        }
        try {
            // check that the embedded url is valid
            new URL(file);
        } catch (MalformedURLException e) {
            throw new NullPointerException(e.toString());
        }
        setURL(url, "jar", "", -1, null, null, file, null, null);
    }

    /**
     * Build and return the externalized string representation of url.
     *
     * @return String the externalized string representation of url
     * @param url
     *            a URL
     */
    @Override
    protected String toExternalForm(URL url) {
        StringBuilder sb = new StringBuilder();
        sb.append("jar:");
        sb.append(url.getFile());
        String ref = url.getRef();
        if (ref != null) {
            sb.append(ref);
        }
        return sb.toString();
    }
}
