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

// $Id: TemplatesHandler.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.transform.sax;

import javax.xml.transform.Templates;
import org.xml.sax.ContentHandler;

/**
 * A SAX ContentHandler that may be used to process SAX
 * parse events (parsing transformation instructions) into a Templates object.
 *
 * <p>Note that TemplatesHandler does not need to implement LexicalHandler.</p>
 */
public interface TemplatesHandler extends ContentHandler {

    /**
     * When a TemplatesHandler object is used as a ContentHandler
     * for the parsing of transformation instructions, it creates a Templates object,
     * which the caller can get once the SAX events have been completed.
     *
     * @return The Templates object that was created during
     * the SAX event process, or null if no Templates object has
     * been created.
     *
     */
    public Templates getTemplates();

    /**
     * Set the base ID (URI or system ID) for the Templates object
     * created by this builder.  This must be set in order to
     * resolve relative URIs in the stylesheet.  This must be
     * called before the startDocument event.
     *
     * @param systemID Base URI for this stylesheet.
     */
    public void setSystemId(String systemID);

    /**
     * Get the base ID (URI or system ID) from where relative
     * URLs will be resolved.
     * @return The systemID that was set with {@link #setSystemId}.
     */
    public String getSystemId();
}
