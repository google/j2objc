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

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.harmony.security.fortress.Services;

/**
 * {@code Provider} is the abstract superclass for all security providers in the
 * Java security infrastructure.
 */
public abstract class Provider extends Properties {
    private static final long serialVersionUID = -4298000515446427739L;

    private String name;

    private double version;

    // String representation of the provider version number.
    private transient String versionString;

    private String info;

    //The provider preference order number.
    // Equals -1 for non registered provider.
    private transient int providerNumber = -1;

    // Contains "Service.Algorithm" and Provider.Service classes added using
    // putService()
    private transient LinkedHashMap<String, Service> serviceTable;

    // Contains "Service.Alias" and Provider.Service classes added using
    // putService()
    private transient LinkedHashMap<String, Service> aliasTable;

    // Contains "Service.Algorithm" and Provider.Service classes added using
    // put()
    private transient LinkedHashMap<String, Service> propertyServiceTable;

    // Contains "Service.Alias" and Provider.Service classes added using put()
    private transient LinkedHashMap<String, Service> propertyAliasTable;

    // The properties changed via put()
    private transient LinkedHashMap<Object, Object> changedProperties;

    // For getService(String type, String algorithm) optimization:
    // previous result
    private transient Provider.Service returnedService;
    // previous parameters
    private transient String lastAlgorithm;
    // last name
    private transient String lastServiceName;

    // For getServices() optimization:
    private transient Set<Service> lastServicesSet;

    // For getService(String type) optimization:
    private transient String lastType;
    // last Service found by type
    private transient Provider.Service lastServicesByType;

    /**
     * Constructs a new instance of {@code Provider} with its name, version and
     * description.
     *
     * @param name
     *            the name of the provider.
     * @param version
     *            the version of the provider.
     * @param info
     *            a description of the provider.
     */
    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
        versionString = String.valueOf(version);
        putProviderInfo();
    }

    /**
     * Returns the name of this provider.
     *
     * @return the name of this provider.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version number for the services being provided.
     *
     * @return the version number for the services being provided.
     */
    public double getVersion() {
        return version;
    }

    /**
     * Returns a description of the services being provided.
     *
     * @return a description of the services being provided.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Returns a string containing a concise, human-readable description of
     * this {@code Provider} including its name and its version.
     *
     * @return a printable representation for this {@code Provider}.
     */
    @Override
    public String toString() {
        return name + " version " + version;
    }

    /**
     * Clears all properties used to look up services implemented by this
     * {@code Provider}.
     */
    @Override
    public synchronized void clear() {
        super.clear();
        if (serviceTable != null) {
            serviceTable.clear();
        }
        if (propertyServiceTable != null) {
            propertyServiceTable.clear();
        }
        if (aliasTable != null) {
            aliasTable.clear();
        }
        if (propertyAliasTable != null) {
            propertyAliasTable.clear();
        }
        changedProperties = null;
        putProviderInfo();
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        servicesChanged();
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        Properties tmp = new Properties();
        tmp.load(inStream);
        myPutAll(tmp);
    }

    /**
     * Copies all from the provided map to this {@code Provider}.
     * @param t
     *            the mappings to copy to this provider.
     */
    @Override
    public synchronized void putAll(Map<?,?> t) {
        myPutAll(t);
    }

    private void myPutAll(Map<?,?> t) {
        if (changedProperties == null) {
            changedProperties = new LinkedHashMap<Object, Object>();
        }
        Iterator<? extends Map.Entry<?, ?>> it = t.entrySet().iterator();
        Object key;
        Object value;
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = it.next();
            key = entry.getKey();
            if (key instanceof String && ((String) key).startsWith("Provider.")) {
                // Provider service type is reserved
                continue;
            }
            value = entry.getValue();
            super.put(key, value);
            if (changedProperties.remove(key) == null) {
                removeFromPropertyServiceTable(key);
            }
            changedProperties.put(key, value);
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    @Override
    public synchronized Set<Map.Entry<Object,Object>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    /**
     * Maps the specified {@code key} property name to the specified {@code
     * value}.
     *
     * @param key
     *            the name of the property.
     * @param value
     *            the value of the property.
     * @return the value that was previously mapped to the specified {@code key}
     *         ,or {@code null} if it did not have one.
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        if (key instanceof String && ((String) key).startsWith("Provider.")) {
            // Provider service type is reserved
            return null;
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        if (changedProperties != null && changedProperties.remove(key) == null) {
            removeFromPropertyServiceTable(key);
        }
        if (changedProperties == null) {
            changedProperties = new LinkedHashMap<Object, Object>();
        }
        changedProperties.put(key, value);
        return super.put(key, value);
    }

    /**
     * Removes the specified {@code key} and its associated value from this
     * {@code Provider}.
     *
     * @param key
     *            the name of the property
     * @return the value that was mapped to the specified {@code key} ,or
     *         {@code null} if no mapping was present
     */
    @Override
    public synchronized Object remove(Object key) {
        if (key instanceof String && ((String) key).startsWith("Provider.")) {
            // Provider service type is reserved
            return null;
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
        if (changedProperties != null && changedProperties.remove(key) == null) {
            removeFromPropertyServiceTable(key);
            if (changedProperties.size() == 0) {
                changedProperties = null;
            }
        }
        return super.remove(key);
    }

    /**
     * Returns true if this provider implements the given algorithm. Caller
     * must specify the cryptographic service and specify constraints via the
     * attribute name and value.
     *
     * @param serv
     *            Crypto service.
     * @param alg
     *            Algorithm or type.
     * @param attribute
     *            The attribute name or {@code null}.
     * @param val
     *            The attribute value.
     * @return
     */
    boolean implementsAlg(String serv, String alg, String attribute, String val) {
        String servAlg = serv + "." + alg;
        String prop = getPropertyIgnoreCase(servAlg);
        if (prop == null) {
            alg = getPropertyIgnoreCase("Alg.Alias." + servAlg);
            if (alg != null) {
                servAlg = serv + "." + alg;
                prop = getPropertyIgnoreCase(servAlg);
            }
        }
        if (prop != null) {
            if (attribute == null) {
                return true;
            }
            return checkAttribute(servAlg, attribute, val);
        }
        return false;
    }

    /**
     * Returns true if this provider has the same value as is given for the
     * given attribute
     */
    private boolean checkAttribute(String servAlg, String attribute, String val) {

        String attributeValue = getPropertyIgnoreCase(servAlg + ' ' + attribute);
        if (attributeValue != null) {
            if (attribute.equalsIgnoreCase("KeySize")) {
                if (Integer.parseInt(attributeValue) >= Integer.parseInt(val)) {
                    return true;
                }
            } else { // other attributes
                if (attributeValue.equalsIgnoreCase(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * Set the provider preference order number.
     *
     * @param n
     */
    void setProviderNumber(int n) {
        providerNumber = n;
    }

    /**
     *
     * Get the provider preference order number.
     *
     * @return
     */
    int getProviderNumber() {
        return providerNumber;
    }

    /**
     * Get the service of the specified type
     *
     */
    synchronized Provider.Service getService(String type) {
        updatePropertyServiceTable();
        if (lastServicesByType != null && type.equals(lastType)) {
            return lastServicesByType;
        }
        Provider.Service service;
        for (Iterator<Service> it = getServices().iterator(); it.hasNext();) {
            service = it.next();
            if (type.equals(service.type)) {
                lastType = type;
                lastServicesByType = service;
                return service;
            }
        }
        return null;
    }

    /**
     * Returns the service with the specified {@code type} implementing the
     * specified {@code algorithm}, or {@code null} if no such implementation
     * exists.
     * <p>
     * If two services match the requested type and algorithm, the one added
     * with the {@link #putService(Service)} is returned (as opposed to the one
     * added via {@link #put(Object, Object)}.
     *
     * @param type
     *            the type of the service (for example {@code KeyPairGenerator})
     * @param algorithm
     *            the algorithm name (case insensitive)
     * @return the requested service, or {@code null} if no such implementation
     *         exists
     */
    public synchronized Provider.Service getService(String type,
            String algorithm) {
        if (type == null) {
            throw new NullPointerException("type == null");
        } else if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }

        if (type.equals(lastServiceName) && algorithm.equalsIgnoreCase(lastAlgorithm)) {
            return returnedService;
        }

        String key = key(type, algorithm);
        Object o = null;
        if (serviceTable != null) {
            o = serviceTable.get(key);
        }
        if (o == null && aliasTable != null) {
            o = aliasTable.get(key);
        }
        if (o == null) {
            updatePropertyServiceTable();
        }
        if (o == null && propertyServiceTable != null) {
            o = propertyServiceTable.get(key);
        }
        if (o == null && propertyAliasTable != null) {
            o = propertyAliasTable.get(key);
        }

        if (o != null) {
            lastServiceName = type;
            lastAlgorithm = algorithm;
            returnedService = (Provider.Service) o;
            return returnedService;
        }
        return null;
    }

    /**
     * Returns an unmodifiable {@code Set} of all services registered by this
     * provider.
     *
     * @return an unmodifiable {@code Set} of all services registered by this
     *         provider
     */
    public synchronized Set<Provider.Service> getServices() {
        updatePropertyServiceTable();
        if (lastServicesSet != null) {
            return lastServicesSet;
        }
        if (serviceTable != null) {
            lastServicesSet = new LinkedHashSet<Service>(serviceTable.values());
        } else {
            lastServicesSet = new LinkedHashSet<Service>();
        }
        if (propertyServiceTable != null) {
            lastServicesSet.addAll(propertyServiceTable.values());
        }
        lastServicesSet = Collections.unmodifiableSet(lastServicesSet);
        return lastServicesSet;
    }

    /**
     * Adds a {@code Service} to this {@code Provider}. If a service with the
     * same name was registered via this method, it is replace.
     *
     * @param s
     *            the {@code Service} to register
     */
    protected synchronized void putService(Provider.Service s) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        if ("Provider".equals(s.getType())) { // Provider service type cannot be added
            return;
        }
        servicesChanged();
        if (serviceTable == null) {
            serviceTable = new LinkedHashMap<String, Service>(128);
        }
        serviceTable.put(key(s.type, s.algorithm), s);
        if (s.aliases != null) {
            if (aliasTable == null) {
                aliasTable = new LinkedHashMap<String, Service>(256);
            }
            for (String alias : s.getAliases()) {
                aliasTable.put(key(s.type, alias), s);
            }
        }
        serviceInfoToProperties(s);
    }

    /**
     * Removes a previously registered {@code Service} from this {@code
     * Provider}.
     *
     * @param s
     *            the {@code Service} to remove
     * @throws NullPointerException
     *             if {@code s} is {@code null}
     */
    protected synchronized void removeService(Provider.Service s) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        servicesChanged();
        if (serviceTable != null) {
            serviceTable.remove(key(s.type, s.algorithm));
        }
        if (aliasTable != null && s.aliases != null) {
            for (String alias: s.getAliases()) {
                aliasTable.remove(key(s.type, alias));
            }
        }
        serviceInfoFromProperties(s);
    }

    /**
     * Add Service information to the provider's properties.
     */
    private void serviceInfoToProperties(Provider.Service s) {
        super.put(s.type + "." + s.algorithm, s.className);
        if (s.aliases != null) {
            for (Iterator<String> i = s.aliases.iterator(); i.hasNext();) {
                super.put("Alg.Alias." + s.type + "." + i.next(), s.algorithm);
            }
        }
        if (s.attributes != null) {
            for (Map.Entry<String, String> entry : s.attributes.entrySet()) {
                super.put(s.type + "." + s.algorithm + " " + entry.getKey(),
                        entry.getValue());
            }
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    /**
     * Remove Service information from the provider's properties.
     */
    private void serviceInfoFromProperties(Provider.Service s) {
        super.remove(s.type + "." + s.algorithm);
        if (s.aliases != null) {
            for (Iterator<String> i = s.aliases.iterator(); i.hasNext();) {
                super.remove("Alg.Alias." + s.type + "." + i.next());
            }
        }
        if (s.attributes != null) {
            for (Map.Entry<String, String> entry : s.attributes.entrySet()) {
                super.remove(s.type + "." + s.algorithm + " " + entry.getKey());
            }
        }
        if (providerNumber != -1) {
            // if registered then refresh Services
            Services.setNeedRefresh();
        }
    }

    // Remove property information from provider Services
    private void removeFromPropertyServiceTable(Object key) {
        if (key == null || !(key instanceof String)) {
            return;
        }
        String k = (String) key;
        if (k.startsWith("Provider.")) { // Provider service type is reserved
            return;
        }
        Provider.Service s;
        String serviceName;
        String algorithm = null;
        String attribute = null;
        int i;
        if (k.startsWith("Alg.Alias.")) { // Alg.Alias.<crypto_service>.<aliasName>=<standardName>
            String aliasName;
            String service_alias = k.substring(10);
            i = service_alias.indexOf('.');
            serviceName = service_alias.substring(0, i);
            aliasName = service_alias.substring(i + 1);
            if (propertyAliasTable != null) {
                propertyAliasTable.remove(key(serviceName, aliasName));
            }
            if (propertyServiceTable != null) {
                for (Iterator<Service> it = propertyServiceTable.values().iterator(); it
                        .hasNext();) {
                    s = it.next();
                    if (s.aliases.contains(aliasName)) {
                        s.aliases.remove(aliasName);
                        return;
                    }
                }
            }
            return;
        }
        int j = k.indexOf('.');
        if (j == -1) { // unknown format
            return;
        }

        i = k.indexOf(' ');
        if (i == -1) { // <crypto_service>.<algorithm_or_type>=<className>
            serviceName = k.substring(0, j);
            algorithm = k.substring(j + 1);
            if (propertyServiceTable != null) {
                Provider.Service ser = propertyServiceTable.remove(key(serviceName, algorithm));
                if (ser != null && propertyAliasTable != null
                        && ser.aliases != null) {
                    for (String alias : ser.aliases) {
                        propertyAliasTable.remove(key(serviceName, alias));
                    }
                }
            }
        } else {
            // <crypto_service>.<algorithm_or_type>
            // <attribute_name>=<attrValue>
            attribute = k.substring(i + 1);
            serviceName = k.substring(0, j);
            algorithm = k.substring(j + 1, i);
            if (propertyServiceTable != null) {
                Object o = propertyServiceTable.get(key(serviceName, algorithm));
                if (o != null) {
                    s = (Provider.Service) o;
                    s.attributes.remove(attribute);
                }
            }
        }
    }

    // Update provider Services if the properties was changed
    private void updatePropertyServiceTable() {
        Object _key;
        Object _value;
        Provider.Service s;
        String serviceName;
        String algorithm;
        if (changedProperties == null || changedProperties.isEmpty()) {
            return;
        }
        for (Iterator<Map.Entry<Object, Object>> it = changedProperties.entrySet().iterator(); it
                .hasNext();) {
            Map.Entry<Object, Object> entry = it.next();
            _key = entry.getKey();
            _value = entry.getValue();
            if (_key == null || _value == null || !(_key instanceof String)
                    || !(_value instanceof String)) {
                continue;
            }
            String key = (String) _key;
            String value = (String) _value;
            if (key.startsWith("Provider")) {
                // Provider service type is reserved
                continue;
            }
            int i;
            if (key.startsWith("Alg.Alias.")) {
                // Alg.Alias.<crypto_service>.<aliasName>=<standardName>
                String aliasName;
                String service_alias = key.substring(10);
                i = service_alias.indexOf('.');
                serviceName = service_alias.substring(0, i);
                aliasName = service_alias.substring(i + 1);
                algorithm = value;
                String propertyServiceTableKey = key(serviceName, algorithm);
                Object o = null;
                if (propertyServiceTable == null) {
                    propertyServiceTable = new LinkedHashMap<String, Service>(128);
                } else {
                    o = propertyServiceTable.get(propertyServiceTableKey);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.addAlias(aliasName);
                    if (propertyAliasTable == null) {
                        propertyAliasTable = new LinkedHashMap<String, Service>(256);
                    }
                    propertyAliasTable.put(key(serviceName, aliasName), s);
                } else {
                    String className = (String) changedProperties
                            .get(serviceName + "." + algorithm);
                    if (className != null) {
                        List<String> l = new ArrayList<String>();
                        l.add(aliasName);
                        s = new Provider.Service(this, serviceName, algorithm,
                                className, l, new HashMap<String, String>());
                        propertyServiceTable.put(propertyServiceTableKey, s);
                        if (propertyAliasTable == null) {
                            propertyAliasTable = new LinkedHashMap<String, Service>(256);
                        }
                        propertyAliasTable.put(key(serviceName, aliasName), s);
                    }
                }
                continue;
            }
            int j = key.indexOf('.');
            if (j == -1) { // unknown format
                continue;
            }
            i = key.indexOf(' ');
            if (i == -1) { // <crypto_service>.<algorithm_or_type>=<className>
                serviceName = key.substring(0, j);
                algorithm = key.substring(j + 1);
                String propertyServiceTableKey = key(serviceName, algorithm);
                Object o = null;
                if (propertyServiceTable != null) {
                    o = propertyServiceTable.get(propertyServiceTableKey);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.className = value;
                } else {
                    s = new Provider.Service(this, serviceName, algorithm,
                            value, Collections.<String>emptyList(),
                            Collections.<String,String>emptyMap());
                    if (propertyServiceTable == null) {
                        propertyServiceTable = new LinkedHashMap<String, Service>(128);
                    }
                    propertyServiceTable.put(propertyServiceTableKey, s);

                }
            } else {
                // <crypto_service>.<algorithm_or_type> <attribute_name>=<attrValue>
                serviceName = key.substring(0, j);
                algorithm = key.substring(j + 1, i);
                String attribute = key.substring(i + 1);
                String propertyServiceTableKey = key(serviceName, algorithm);
                Object o = null;
                if (propertyServiceTable != null) {
                    o = propertyServiceTable.get(propertyServiceTableKey);
                }
                if (o != null) {
                    s = (Provider.Service) o;
                    s.putAttribute(attribute, value);
                } else {
                    String className = (String) changedProperties
                            .get(serviceName + "." + algorithm);
                    if (className != null) {
                        Map<String, String> m = new HashMap<String, String>();
                        m.put(attribute, value);
                        s = new Provider.Service(this, serviceName, algorithm,
                                className, new ArrayList<String>(), m);
                        if (propertyServiceTable == null) {
                            propertyServiceTable = new LinkedHashMap<String, Service>(128);
                        }
                        propertyServiceTable.put(propertyServiceTableKey, s);
                    }
                }
            }
        }
        servicesChanged();
        changedProperties = null;
    }

    private void servicesChanged() {
        lastServicesByType = null;
        lastServiceName = null;
        lastServicesSet = null;
    }

    /**
     * These attributes should be placed in each Provider object:
     * Provider.id name, Provider.id version, Provider.id info,
     * Provider.id className
     */
    private void putProviderInfo() {
        super.put("Provider.id name", (name != null) ? name : "null");
        super.put("Provider.id version", versionString);
        super.put("Provider.id info", (info != null) ? info : "null");
        super.put("Provider.id className", this.getClass().getName());
    }

    /**
     * Returns the property with the specified key in the provider properties.
     * The name is not case-sensitive.
     */
    private String getPropertyIgnoreCase(String key) {
        String res = getProperty(key);
        if (res != null) {
            return res;
        }
        for (Enumeration<?> e = propertyNames(); e.hasMoreElements(); ) {
            String propertyName = (String) e.nextElement();
            if (key.equalsIgnoreCase(propertyName)) {
                return getProperty(propertyName);
            }
        }
        return null;
    }

    private static String key(String type, String algorithm) {
        return type + '.' + algorithm.toUpperCase(Locale.US);
    }

    /**
     * {@code Service} represents a service in the Java Security infrastructure.
     * Each service describes its type, the algorithm it implements, to which
     * provider it belongs and other properties.
     */
    public static class Service {
        // The provider
        private Provider provider;

        // The type of this service
        private String type;

        // The algorithm name
        private String algorithm;

        // The class implementing this service
        private String className;

        // The aliases
        private List<String> aliases;

        // The attributes
        private Map<String,String> attributes;

        // Service implementation
        private Class<?> implementation;

        // For newInstance() optimization
        private String lastClassName;

        /**
         * Constructs a new instance of {@code Service} with the given
         * attributes.
         *
         * @param provider
         *            the provider to which this service belongs.
         * @param type
         *            the type of this service (for example {@code
         *            KeyPairGenerator}).
         * @param algorithm
         *            the algorithm this service implements.
         * @param className
         *            the name of the class implementing this service.
         * @param aliases
         *            {@code List} of aliases for the algorithm name, or {@code
         *            null} if the implemented algorithm has no aliases.
         * @param attributes
         *            {@code Map} of additional attributes, or {@code null} if
         *            this {@code Service} has no attributed.
         * @throws NullPointerException
         *             if {@code provider, type, algorithm} or {@code className}
         *             is {@code null}.
         */
        public Service(Provider provider, String type, String algorithm,
                String className, List<String> aliases, Map<String, String> attributes) {
            if (provider == null) {
                throw new NullPointerException("provider == null");
            } else if (type == null) {
                throw new NullPointerException("type == null");
            } else if (algorithm == null) {
                throw new NullPointerException("algorithm == null");
            } else if (className == null) {
                throw new NullPointerException("className == null");
            }
            this.provider = provider;
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = ((aliases != null) && (aliases.size() == 0))
                    ? Collections.<String>emptyList() : aliases;
            this.attributes =
                    ((attributes != null) && (attributes.size() == 0))
                    ? Collections.<String,String>emptyMap() : attributes;
        }

        /**
         * Adds an alias.
         *
         * @param alias the alias to add
         */
        /*package*/ void addAlias(String alias) {
            if ((aliases == null) || (aliases.size() == 0)) {
                aliases = new ArrayList<String>();
            }
            aliases.add(alias);
        }

        /**
         * Puts a new attribute mapping.
         *
         * @param name the attribute name.
         * @param value the attribute value.
         */
        /*package*/ void putAttribute(String name, String value) {
            if ((attributes == null) || (attributes.size() == 0)) {
                attributes = new HashMap<String,String>();
            }
            attributes.put(name, value);
        }

        /**
         * Returns the type of this {@code Service}. For example {@code
         * KeyPairGenerator}.
         *
         * @return the type of this {@code Service}.
         */
        public final String getType() {
            return type;
        }

        /**
         * Returns the name of the algorithm implemented by this {@code
         * Service}.
         *
         * @return the name of the algorithm implemented by this {@code
         *         Service}.
         */
        public final String getAlgorithm() {
            return algorithm;
        }

        /**
         * Returns the {@code Provider} this {@code Service} belongs to.
         *
         * @return the {@code Provider} this {@code Service} belongs to.
         */
        public final Provider getProvider() {
            return provider;
        }

        /**
         * Returns the name of the class implementing this {@code Service}.
         *
         * @return the name of the class implementing this {@code Service}.
         */
        public final String getClassName() {
            return className;
        }

        /**
         * Returns the value of the attribute with the specified {@code name}.
         *
         * @param name
         *            the name of the attribute.
         * @return the value of the attribute, or {@code null} if no attribute
         *         with the given name is set.
         * @throws NullPointerException
         *             if {@code name} is {@code null}.
         */
        public final String getAttribute(String name) {
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            if (attributes == null) {
                return null;
            }
            return attributes.get(name);
        }

        List<String> getAliases() {
            if (aliases == null){
                aliases = new ArrayList<String>(0);
            }
            return aliases;
        }

        /**
         * Creates and returns a new instance of the implementation described by
         * this {@code Service}.
         *
         * @param constructorParameter
         *            the parameter that is used by the constructor, or {@code
         *            null} if the implementation does not declare a constructor
         *            parameter.
         * @return a new instance of the implementation described by this
         *         {@code Service}.
         * @throws NoSuchAlgorithmException
         *             if the instance could not be constructed.
         * @throws InvalidParameterException
         *             if the implementation does not support the specified
         *             {@code constructorParameter}.
         */
        public Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException {
            if (implementation == null || !className.equals(lastClassName)) {
                ClassLoader cl = provider.getClass().getClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                try {
                    implementation = Class.forName(className, true, cl);
                    lastClassName = className;
                } catch (Exception e) {
                    throw new NoSuchAlgorithmException(type + " " + algorithm + " implementation not found: " + e);
                }
            }
            if (constructorParameter == null) {
                try {
                    return implementation.newInstance();
                } catch (Exception e) {
                    throw new NoSuchAlgorithmException(
                            type + " " + algorithm + " implementation not found", e);
                }
            }
            if (!supportsParameter(constructorParameter)) {
                throw new InvalidParameterException(type + ": service cannot use the parameter");
            }

            Class[] parameterTypes = new Class[1];
            Object[] initargs = { constructorParameter };
            try {
                if (type.equalsIgnoreCase("CertStore")) {
                    parameterTypes[0] = Class.forName("java.security.cert.CertStoreParameters");
                } else {
                    parameterTypes[0] = constructorParameter.getClass();
                }
                return implementation.getConstructor(parameterTypes)
                        .newInstance(initargs);
            } catch (Exception e) {
                throw new NoSuchAlgorithmException(type + " " + algorithm
                        + " implementation not found", e);
            }
        }

        /**
         * Indicates whether this {@code Service} supports the specified
         * constructor parameter.
         *
         * @param parameter
         *            the parameter to test.
         * @return {@code true} if this {@code Service} supports the specified
         *         constructor parameter, {@code false} otherwise.
         */
        public boolean supportsParameter(Object parameter) {
            return true;
        }

        /**
         * Returns a string containing a concise, human-readable description of
         * this {@code Service}.
         *
         * @return a printable representation for this {@code Service}.
         */
        @Override
        public String toString() {
            String result = "Provider " + provider.getName() + " Service "
                    + type + "." + algorithm + " " + className;
            if (aliases != null) {
                result = result + "\nAliases " + aliases.toString();
            }
            if (attributes != null) {
                result = result + "\nAttributes " + attributes.toString();
            }
            return result;
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws NotActiveException, IOException, ClassNotFoundException {
        in.defaultReadObject();
        versionString = String.valueOf(version);
        providerNumber = -1;
    }
}
