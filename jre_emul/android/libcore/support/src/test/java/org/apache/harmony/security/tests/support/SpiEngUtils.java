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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.support;

import java.security.Security;
import java.security.Provider;

/**
 * Additional class for verification spi-engine classes
 *
 */

public class SpiEngUtils {

    public static final String[] invalidValues = {
            "",
            "BadAlgorithm",
            "Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message Long message" };

    /**
     * Verification: is algorithm supported or not
     *
     * @param algorithm
     * @param service
     * @return
     */
    public static Provider isSupport(String algorithm, String service) {
        try {
            Provider[] provs = Security.getProviders(service.concat(".")
                    .concat(algorithm));
            if (provs == null) {
                return null;
            }
            return (provs.length == 0 ? null : provs[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public class MyProvider extends Provider {

        public MyProvider(String name, String info, String key, String clName) {
            super(name, 1.0, info);
            put(key, clName);
        }

    }

}