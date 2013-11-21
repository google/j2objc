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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.security.fortress;

import java.security.Provider;
import java.util.List;

/**
 *
 * This interface provides access to package visible api in java.security
 *
 */
public interface SecurityAccess {
    /**
     * Access to Security.renumProviders()
     *
     */
    public void renumProviders();

    /**
     * Access to Service.getAliases()
     * @param s
     * @return
     */
    public List<String> getAliases(Provider.Service s);

    /**
     * Access to Provider.getService(String type)
     * @param p
     * @param type
     * @return
     */
    public Provider.Service getService(Provider p, String type);
}
