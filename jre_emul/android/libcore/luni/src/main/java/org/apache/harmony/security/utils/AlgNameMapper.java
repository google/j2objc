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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.utils;

import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.harmony.security.asn1.ObjectIdentifier;
import org.apache.harmony.security.fortress.Services;

/**
 * Provides Algorithm Name to OID and OID to Algorithm Name mappings. Some known
 * mappings are hardcoded. Tries to obtain additional mappings from installed
 * providers during initialization.
 */
public class AlgNameMapper {
    private static AlgNameMapperSource source = null;

    private static volatile int cacheVersion = -1;

    // Will search OID mappings for these services
    private static final String[] serviceName = {
            "Cipher",
            "AlgorithmParameters",
            "Signature"
    };

    // These mappings CAN NOT be overridden
    // by the ones from available providers
    // during maps initialization
    // (source: http://asn1.elibel.tm.fr):
    private static final String[][] knownAlgMappings = {
        {"1.2.840.10040.4.1",       "DSA"},
        {"1.2.840.10040.4.3",       "SHA1withDSA"},
        {"1.2.840.113549.1.1.1",    "RSA"},
        // BEGIN android-removed
        // Dropping MD2
        // {"1.2.840.113549.1.1.2",    "MD2withRSA"},
        // END android-removed
        {"1.2.840.113549.1.1.4",    "MD5withRSA"},
        {"1.2.840.113549.1.1.5",    "SHA1withRSA"},
        {"1.2.840.113549.1.3.1",    "DiffieHellman"},
        {"1.2.840.113549.1.5.3",    "pbeWithMD5AndDES-CBC"},
        {"1.2.840.113549.1.12.1.3", "pbeWithSHAAnd3-KeyTripleDES-CBC"},
        {"1.2.840.113549.1.12.1.6", "pbeWithSHAAnd40BitRC2-CBC"},
        {"1.2.840.113549.3.2",      "RC2-CBC"},
        {"1.2.840.113549.3.3",      "RC2-EBC"},
        {"1.2.840.113549.3.4",      "RC4"},
        {"1.2.840.113549.3.5",      "RC4WithMAC"},
        {"1.2.840.113549.3.6",      "DESx-CBC"},
        {"1.2.840.113549.3.7",      "TripleDES-CBC"},
        {"1.2.840.113549.3.8",      "rc5CBC"},
        {"1.2.840.113549.3.9",      "RC5-CBC"},
        {"1.2.840.113549.3.10",     "DESCDMF"},
        {"2.23.42.9.11.4.1",        "ECDSA"},
    };
    // Maps alg name to OID
    private static final Map<String, String> alg2OidMap = new HashMap<String, String>();
    // Maps OID to alg name
    private static final Map<String, String> oid2AlgMap = new HashMap<String, String>();
    // Maps aliases to alg names
    private static final Map<String, String> algAliasesMap = new HashMap<String, String>();

    static {
        for (String[] element : knownAlgMappings) {
            String algUC = element[1].toUpperCase(Locale.US);
            alg2OidMap.put(algUC, element[0]);
            oid2AlgMap.put(element[0], algUC);
            // map upper case alg name to its original name
            algAliasesMap.put(algUC, element[1]);
        }
    }

    // No instances
    private AlgNameMapper() {
    }

    private static synchronized void checkCacheVersion() {
        int newCacheVersion = Services.getCacheVersion();
        if (newCacheVersion != cacheVersion) {
            //
            // Now search providers for mappings like
            // Alg.Alias.<service>.<OID-INTS-DOT-SEPARATED>=<alg-name>
            //  or
            // Alg.Alias.<service>.OID.<OID-INTS-DOT-SEPARATED>=<alg-name>
            //
            Provider[] pl = Security.getProviders();
            for (Provider element : pl) {
                selectEntries(element);
            }
            cacheVersion = newCacheVersion;
        }
    }

    /**
     * Returns OID for algName
     *
     * @param algName algorithm name to be mapped
     * @return OID as String
     */
    public static String map2OID(String algName) {
        checkCacheVersion();

        // alg2OidMap map contains upper case keys
        String result = alg2OidMap.get(algName.toUpperCase(Locale.US));
        if (result != null) {
            return result;
        }

        // Check our external source.
        AlgNameMapperSource s = source;
        if (s != null) {
            return s.mapNameToOid(algName);
        }

        return null;
    }

    /**
     * Returns algName for OID
     *
     * @param oid OID to be mapped
     * @return algorithm name
     */
    public static String map2AlgName(String oid) {
        checkCacheVersion();

        // oid2AlgMap map contains upper case values
        String algUC = oid2AlgMap.get(oid);
        // if not null there is always map UC->Orig
        if (algUC != null) {
            return algAliasesMap.get(algUC);
        }

        // Check our external source.
        AlgNameMapperSource s = source;
        if (s != null) {
            return s.mapOidToName(oid);
        }

        return null;
    }

    /**
     * Returns Algorithm name for given algorithm alias
     *
     * @param algName - alias
     * @return algorithm name
     */
    public static String getStandardName(String algName) {
        return algAliasesMap.get(algName.toUpperCase(Locale.US));
    }

    // Searches given provider for mappings like
    // Alg.Alias.<service>.<OID-INTS-DOT-SEPARATED>=<alg-name>
    //  or
    // Alg.Alias.<service>.OID.<OID-INTS-DOT-SEPARATED>=<alg-name>
    // Puts mappings found into appropriate internal maps
    private static void selectEntries(Provider p) {
        Set<Map.Entry<Object, Object>> entrySet = p.entrySet();
        for (String service : serviceName) {
            String keyPrfix2find = "Alg.Alias." + service + ".";
            for (Entry<Object, Object> me : entrySet) {
                String key = (String)me.getKey();
                if (key.startsWith(keyPrfix2find)) {
                    String alias = key.substring(keyPrfix2find.length());
                    String alg = (String)me.getValue();
                    String algUC = alg.toUpperCase(Locale.US);
                    if (isOID(alias)) {
                        if (alias.startsWith("OID.")) {
                            alias = alias.substring(4);
                        }
                        // Do not overwrite already known mappings
                        boolean oid2AlgContains = oid2AlgMap.containsKey(alias);
                        boolean alg2OidContains = alg2OidMap.containsKey(algUC);
                        if (!oid2AlgContains || !alg2OidContains) {
                            if (!oid2AlgContains) {
                                oid2AlgMap.put(alias, algUC);
                            }
                            if (!alg2OidContains) {
                                alg2OidMap.put(algUC, alias);
                            }
                            // map upper case alg name to its original name
                            algAliasesMap.put(algUC, alg);
                        }
                           // Do not override known standard names
                    } else if (!algAliasesMap.containsKey(alias.toUpperCase(Locale.US))) {
                        algAliasesMap.put(alias.toUpperCase(Locale.US), alg);
                    }
                }
            }
        }
    }

    /**
     * Checks if parameter represents OID
     *
     * @param alias alias to be checked
     * @return 'true' if parameter represents OID
     */
    public static boolean isOID(String alias) {
        return ObjectIdentifier.isOID(normalize(alias));
    }

    /**
     * Removes leading "OID." from oid String passed
     *
     * @param oid string that may contain leading "OID."
     * @return string passed without leading "OID."
     */
    public static String normalize(String oid) {
        return oid.startsWith("OID.")
            ? oid.substring(4)
            : oid;
    }

    public static void setSource(AlgNameMapperSource source) {
        AlgNameMapper.source = source;
    }
}
