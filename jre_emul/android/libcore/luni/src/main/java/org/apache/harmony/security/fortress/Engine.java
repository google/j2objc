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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Locale;


/**
 * This class implements common functionality for Provider supplied
 * classes. The usage pattern is to allocate static Engine instance
 * per service type and synchronize on that instance during calls to
 * {@code getInstance} and retreival of the selected {@code Provider}
 * and Service Provider Interface (SPI) results. Retreiving the
 * results with {@code getProvider} and {@code getSpi} sets the
 * internal {@code Engine} values to null to prevent memory leaks.
 *
 * <p>
 *
 * For example: <pre>   {@code
 *   public class Foo {
 *
 *       private static final Engine ENGINE = new Engine("Foo");
 *
 *       private final FooSpi spi;
 *       private final Provider provider;
 *       private final String algorithm;
 *
 *       protected Foo(FooSpi spi,
 *                     Provider provider,
 *                     String algorithm) {
 *           this.spi = spi;
 *           this.provider = provider;
 *           this.algorithm = algorithm;
 *       }
 *
 *       public static Foo getInstance(String algorithm) {
 *           Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
 *           return new Foo((FooSpi) sap.spi, sap.provider, algorithm);
 *       }
 *
 *       public static Foo getInstance(String algorithm, Provider provider) {
 *           Object spi = ENGINE.getInstance(algorithm, provider, null);
 *           return new Foo((FooSpi) spi, provider, algorithm);
 *       }
 *
 *       ...
 *
 * }</pre>
 */
public class Engine {

    /**
     * Access to package visible api in java.security
     */
    public static SecurityAccess door;

    /**
     * Service name such as Cipher or SSLContext
     */
    private final String serviceName;

    /**
     * Previous result for getInstance(String, Object) optimization.
     * Only this non-Provider version of getInstance is optimized
     * since the the Provider version does not require an expensive
     * Services.getService call.
     */
    private volatile ServiceCacheEntry serviceCache;

    private static final class ServiceCacheEntry {
        /** used to test for cache hit */
        private final String algorithm;
        /** used to test for cache validity */
        private final int cacheVersion;
        /** cached result */
        private final Provider.Service service;

        private ServiceCacheEntry(String algorithm,
                                  int cacheVersion,
                                  Provider.Service service) {
            this.algorithm = algorithm;
            this.cacheVersion = cacheVersion;
            this.service = service;
        }
    }

    public static final class SpiAndProvider {
        public final Object spi;
        public final Provider provider;
        private SpiAndProvider(Object spi, Provider provider) {
            this.spi = spi;
            this.provider = provider;
        }
    }

    /**
     * Creates a Engine object
     *
     * @param service
     */
    public Engine(String service) {
        this.serviceName = service;
    }

    /**
     * Finds the appropriate service implementation and returns an
     * {@code SpiAndProvider} instance containing a reference to SPI
     * and its {@code Provider}
     */
    public SpiAndProvider getInstance(String algorithm, Object param)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("Null algorithm name");
        }
        int newCacheVersion = Services.getCacheVersion();
        Provider.Service service;
        ServiceCacheEntry cacheEntry = this.serviceCache;
        if (cacheEntry != null
                && cacheEntry.algorithm.equalsIgnoreCase(algorithm)
                && newCacheVersion == cacheEntry.cacheVersion) {
            service = cacheEntry.service;
        } else {
            if (Services.isEmpty()) {
                throw notFound(serviceName, algorithm);
            }
            String name = this.serviceName + "." + algorithm.toUpperCase(Locale.US);
            service = Services.getService(name);
            if (service == null) {
                throw notFound(serviceName, algorithm);
            }
            this.serviceCache = new ServiceCacheEntry(algorithm, newCacheVersion, service);
        }
        return new SpiAndProvider(service.newInstance(param), service.getProvider());
    }

    /**
     * Finds the appropriate service implementation and returns and
     * instance of the class that implements corresponding Service
     * Provider Interface.
     */
    public Object getInstance(String algorithm, Provider provider, Object param)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("algorithm == null");
        }
        Provider.Service service = provider.getService(serviceName, algorithm);
        if (service == null) {
            throw notFound(serviceName, algorithm);
        }
        return service.newInstance(param);
    }

    private NoSuchAlgorithmException notFound(String serviceName, String algorithm)
            throws NoSuchAlgorithmException {
        throw new NoSuchAlgorithmException(serviceName + " " + algorithm
                                           + " implementation not found");
    }
}
