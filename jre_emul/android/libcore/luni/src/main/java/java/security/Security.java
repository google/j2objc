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

package java.security;

import com.google.j2objc.security.IosSecurityProvider;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.fortress.SecurityAccess;
import org.apache.harmony.security.fortress.Services;

/**
 * {@code Security} is the central class in the Java Security API. It manages
 * the list of security {@code Provider} that have been installed into this
 * runtime environment.
 */
public final class Security {

    // Security properties
    private static final Properties secprops = new Properties();

    static {
        registerDefaultProviders();
        Engine.door = new SecurityDoor();
    }

    /**
     * This class can't be instantiated.
     */
    private Security() {
    }

    // Register default providers
    private static void registerDefaultProviders() {
        // TODO(tball): enable as packages are implemented.
        secprops.put("security.provider.1", "com.google.j2objc.security.IosSecurityProvider");
        // secprops.put("security.provider.2", "org.apache.harmony.security.provider.cert.DRLCertFactory");
        // secprops.put("security.provider.3", "org.bouncycastle.jce.provider.BouncyCastleProvider");
        // secprops.put("security.provider.4", "org.apache.harmony.security.provider.crypto.CryptoProvider");
        // secprops.put("security.provider.5", "org.conscrypt.JSSEProvider");
    }

    /**
     * Returns value for the specified algorithm with the specified name.
     *
     * @param algName
     *            the name of the algorithm.
     * @param propName
     *            the name of the property.
     * @return value of the property.
     * @deprecated Use {@link AlgorithmParameters} and {@link KeyFactory} instead.
     */
    @Deprecated
    public static String getAlgorithmProperty(String algName, String propName) {
        if (algName == null || propName == null) {
            return null;
        }
        String prop = "Alg." + propName + "." + algName;
        Provider[] providers = getProviders();
        for (Provider provider : providers) {
            for (Enumeration<?> e = provider.propertyNames(); e.hasMoreElements();) {
                String propertyName = (String) e.nextElement();
                if (propertyName.equalsIgnoreCase(prop)) {
                    return provider.getProperty(propertyName);
                }
            }
        }
        return null;
    }

    /**
     * Insert the given {@code Provider} at the specified {@code position}. The
     * positions define the preference order in which providers are searched for
     * requested algorithms.
     *
     * @param provider
     *            the provider to insert.
     * @param position
     *            the position (starting from 1).
     * @return the actual position or {@code -1} if the given {@code provider}
     *         was already in the list. The actual position may be different
     *         from the desired position.
     */
    public static synchronized int insertProviderAt(Provider provider, int position) {
        // check that provider is not already
        // installed, else return -1; if (position <1) or (position > max
        // position) position = max position + 1; insert provider, shift up
        // one position for next providers; Note: The position is 1-based
        if (getProvider(provider.getName()) != null) {
            return -1;
        }
        int result = Services.insertProviderAt(provider, position);
        renumProviders();
        return result;
    }

    /**
     * Adds the given {@code provider} to the collection of providers at the
     * next available position.
     *
     * @param provider
     *            the provider to be added.
     * @return the actual position or {@code -1} if the given {@code provider}
     *         was already in the list.
     */
    public static int addProvider(Provider provider) {
        return insertProviderAt(provider, 0);
    }

    /**
     * Removes the {@code Provider} with the specified name form the collection
     * of providers. If the the {@code Provider} with the specified name is
     * removed, all provider at a greater position are shifted down one
     * position.
     *
     * <p>Returns silently if {@code name} is {@code null} or no provider with the
     * specified name is installed.
     *
     * @param name
     *            the name of the provider to remove.
     */
    public static synchronized void removeProvider(String name) {
        // It is not clear from spec.:
        // 1. if name is null, should we checkSecurityAccess or not?
        //    throw SecurityException or not?
        // 2. as 1 but provider is not installed
        // 3. behavior if name is empty string?

        Provider p;
        if ((name == null) || (name.length() == 0)) {
            return;
        }
        p = getProvider(name);
        if (p == null) {
            return;
        }
        Services.removeProvider(p.getProviderNumber());
        renumProviders();
        p.setProviderNumber(-1);
    }

    /**
     * Returns an array containing all installed providers. The providers are
     * ordered according their preference order.
     *
     * @return an array containing all installed providers.
     */
    public static synchronized Provider[] getProviders() {
        ArrayList<Provider> providers = Services.getProviders();
        return providers.toArray(new Provider[providers.size()]);
    }

    /**
     * Returns the {@code Provider} with the specified name. Returns {@code
     * null} if name is {@code null} or no provider with the specified name is
     * installed.
     *
     * @param name
     *            the name of the requested provider.
     * @return the provider with the specified name, maybe {@code null}.
     */
    public static synchronized Provider getProvider(String name) {
        return Services.getProvider(name);
    }

    /**
     * Returns the array of providers which meet the user supplied string
     * filter. The specified filter must be supplied in one of two formats:
     * <nl>
     * <li> CRYPTO_SERVICE_NAME.ALGORITHM_OR_TYPE
     * <p>
     * (for example: "MessageDigest.SHA")
     * <li> CRYPTO_SERVICE_NAME.ALGORITHM_OR_TYPE
     * ATTR_NAME:ATTR_VALUE
     * <p>
     * (for example: "Signature.MD2withRSA KeySize:512")
     * </nl>
     *
     * @param filter
     *            case-insensitive filter.
     * @return the providers which meet the user supplied string filter {@code
     *         filter}. A {@code null} value signifies that none of the
     *         installed providers meets the filter specification.
     * @throws InvalidParameterException
     *             if an unusable filter is supplied.
     * @throws NullPointerException
     *             if {@code filter} is {@code null}.
     */
    public static Provider[] getProviders(String filter) {
        if (filter == null) {
            throw new NullPointerException("filter == null");
        }
        if (filter.length() == 0) {
            throw new InvalidParameterException();
        }
        HashMap<String, String> hm = new HashMap<String, String>();
        int i = filter.indexOf(':');
        if ((i == filter.length() - 1) || (i == 0)) {
            throw new InvalidParameterException();
        }
        if (i < 1) {
            hm.put(filter, "");
        } else {
            hm.put(filter.substring(0, i), filter.substring(i + 1));
        }
        return getProviders(hm);
    }

    /**
     * Returns the array of providers which meet the user supplied set of
     * filters. The filter must be supplied in one of two formats:
     * <nl>
     * <li> CRYPTO_SERVICE_NAME.ALGORITHM_OR_TYPE
     * <p>
     * for example: "MessageDigest.SHA" The value associated with the key must
     * be an empty string. <li> CRYPTO_SERVICE_NAME.ALGORITHM_OR_TYPE
     * ATTR_NAME:ATTR_VALUE
     * <p>
     * for example: "Signature.MD2withRSA KeySize:512" where "KeySize:512" is
     * the value of the filter map entry.
     * </nl>
     *
     * @param filter
     *            case-insensitive filter.
     * @return the providers which meet the user supplied string filter {@code
     *         filter}. A {@code null} value signifies that none of the
     *         installed providers meets the filter specification.
     * @throws InvalidParameterException
     *             if an unusable filter is supplied.
     * @throws NullPointerException
     *             if {@code filter} is {@code null}.
     */
    public static synchronized Provider[] getProviders(Map<String,String> filter) {
        if (filter == null) {
            throw new NullPointerException("filter == null");
        }
        if (filter.isEmpty()) {
            return null;
        }
        ArrayList<Provider> result = new ArrayList<Provider>(Services.getProviders());
        Set<Entry<String, String>> keys = filter.entrySet();
        Map.Entry<String, String> entry;
        for (Iterator<Entry<String, String>> it = keys.iterator(); it.hasNext();) {
            entry = it.next();
            String key = entry.getKey();
            String val = entry.getValue();
            String attribute = null;
            int i = key.indexOf(' ');
            int j = key.indexOf('.');
            if (j == -1) {
                throw new InvalidParameterException();
            }
            if (i == -1) { // <crypto_service>.<algorithm_or_type>
                if (val.length() != 0) {
                    throw new InvalidParameterException();
                }
            } else { // <crypto_service>.<algorithm_or_type> <attribute_name>
                if (val.length() == 0) {
                    throw new InvalidParameterException();
                }
                attribute = key.substring(i + 1);
                if (attribute.trim().length() == 0) {
                    throw new InvalidParameterException();
                }
                key = key.substring(0, i);
            }
            String serv = key.substring(0, j);
            String alg = key.substring(j + 1);
            if (serv.length() == 0 || alg.length() == 0) {
                throw new InvalidParameterException();
            }
            filterProviders(result, serv, alg, attribute, val);
        }
        if (result.size() > 0) {
            return result.toArray(new Provider[result.size()]);
        }
        return null;
    }

    private static void filterProviders(ArrayList<Provider> providers, String service,
            String algorithm, String attribute, String attrValue) {
        Iterator<Provider> it = providers.iterator();
        while (it.hasNext()) {
            Provider p = it.next();
            if (!p.implementsAlg(service, algorithm, attribute, attrValue)) {
                it.remove();
            }
        }
    }

    /**
     * Returns the value of the security property named by the argument.
     *
     * @param key
     *            the name of the requested security property.
     * @return the value of the security property.
     */
    public static String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        String property = secprops.getProperty(key);
        if (property != null) {
            property = property.trim();
        }
        return property;
    }

    /**
     * Sets the value of the specified security property.
     */
    public static void setProperty(String key, String value) {
        Services.setNeedRefresh();
        secprops.put(key, value);
    }

    /**
     * Returns a {@code Set} of all registered algorithms for the specified
     * cryptographic service. {@code "Signature"}, {@code "Cipher"} and {@code
     * "KeyStore"} are examples for such kind of services.
     *
     * @param serviceName
     *            the case-insensitive name of the service.
     * @return a {@code Set} of all registered algorithms for the specified
     *         cryptographic service, or an empty {@code Set} if {@code
     *         serviceName} is {@code null} or if no registered provider
     *         provides the requested service.
     */
    public static Set<String> getAlgorithms(String serviceName) {
        Set<String> result = new HashSet<String>();
        // compatibility with RI
        if (serviceName == null) {
            return result;
        }
        for (Provider provider : getProviders()) {
            for (Provider.Service service: provider.getServices()) {
                if (service.getType().equalsIgnoreCase(serviceName)) {
                    result.add(service.getAlgorithm());
                }
            }
        }
        return result;
    }

    /**
     *
     * Update sequence numbers of all providers.
     *
     */
    private static void renumProviders() {
        ArrayList<Provider> providers = Services.getProviders();
        for (int i = 0; i < providers.size(); i++) {
            providers.get(i).setProviderNumber(i + 1);
        }
    }

    private static class SecurityDoor implements SecurityAccess {
        // Access to Security.renumProviders()
        public void renumProviders() {
            Security.renumProviders();
        }

        //  Access to Security.getAliases()
        public List<String> getAliases(Provider.Service s) {
            return s.getAliases();
        }

        // Access to Provider.getService()
        public Provider.Service getService(Provider p, String type) {
            return p.getService(type);
        }
    }

    // Unused iOS-specific reference to IosSecurityProvider to ensure it gets linked into apps.
    private static final Class<?> unused = IosSecurityProvider.class;
}
