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

package java.net;

import java.io.IOException;

/**
 * This class converts the content of a certain format (i.e. a MIME type) into a
 * Java type object. It is created by {@code ContentHandlerFactory}. The data
 * values should be accessed via {@code URL} or {@code URLConnection}.
 *
 * @see ContentHandlerFactory
 * @see URL#getContent()
 * @see URLConnection#getContent()
 */
public abstract class ContentHandler {
    /**
     * Returns the object pointed by the specified URL connection {@code uConn}.
     *
     * @param uConn
     *            URL connection that points to the desired object.
     * @return object referred by {@code uConn}.
     * @throws IOException
     *             if an IO error occurs during the retrieval of the object
     */
    public abstract Object getContent(URLConnection uConn) throws IOException;

    /**
     * Returns the object pointed by the specified URL connection {@code uConn}.
     *
     * @param uConn
     *            URL connection that points to the desired object.
     * @param types
     *            list of acceptable content types.
     * @return resource object pointed by this URL or {@code null} if the
     *         content doesn't match one of the specified content types.
     * @throws IOException
     *             if an error occurred while obtaining the content.
     */
    // Class arg not generified in the spec.
    @SuppressWarnings("unchecked")
    public Object getContent(URLConnection uConn, Class[] types)
            throws IOException {
        Object content = getContent(uConn);
        for (int i = 0; i < types.length; i++) {
            if (types[i].isInstance(content)) {
                return content;
            }
        }
        return null;
    }
}
