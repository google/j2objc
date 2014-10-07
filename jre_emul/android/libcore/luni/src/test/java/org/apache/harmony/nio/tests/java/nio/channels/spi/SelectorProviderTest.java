/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.nio.tests.java.nio.channels.spi;

import java.nio.channels.spi.SelectorProvider;

import junit.framework.TestCase;

public class SelectorProviderTest extends TestCase {

    /**
     * SelectorProvider#openDatagramChannel()
     * SelectorProvider#openPipe()
     * SelectorProvider#openServerSocketChannel()
     * SelectorProvider#openSocketChannel()
     * SelectorProvider#openSelector()
     */
    public void test_open_methods() throws Exception {
        // calling #provider to see if it returns without Exception.
        assertNotNull(SelectorProvider.provider());

        // calling #inheritedChannel to see if this already throws an exception.
        SelectorProvider.provider().inheritedChannel();

        assertNotNull(SelectorProvider.provider().openDatagramChannel());
        assertNotNull(SelectorProvider.provider().openPipe());
        assertNotNull(SelectorProvider.provider().openServerSocketChannel());
        assertNotNull(SelectorProvider.provider().openSocketChannel());
        assertNotNull(SelectorProvider.provider().openSelector());
    }
}
