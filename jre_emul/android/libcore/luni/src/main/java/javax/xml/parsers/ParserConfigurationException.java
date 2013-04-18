
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

// $Id: ParserConfigurationException.java 569981 2007-08-27 03:59:07Z mrglavas $

package javax.xml.parsers;

/**
 * Indicates a serious configuration error.
 *
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 569981 $, $Date: 2007-08-26 20:59:07 -0700 (Sun, 26 Aug 2007) $
 */

public class ParserConfigurationException extends Exception {

    /**
     * Create a new <code>ParserConfigurationException</code> with no
     * detail message.
     */

    public ParserConfigurationException() {
    }

    /**
     * Create a new <code>ParserConfigurationException</code> with
     * the <code>String</code> specified as an error message.
     *
     * @param msg The error message for the exception.
     */

    public ParserConfigurationException(String msg) {
        super(msg);
    }

}
