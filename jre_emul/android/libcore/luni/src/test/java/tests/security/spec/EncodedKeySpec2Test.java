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

package tests.security.spec;

import junit.framework.TestCase;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncodedKeySpec2Test extends TestCase {

    /**
     * java.security.spec.EncodedKeySpec#getEncoded()
     */
    public void test_getEncoded() throws Exception {

               KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");

               keyGen.initialize(1024);
               KeyPair keys = keyGen.generateKeyPair();


               KeyFactory fact = KeyFactory.getInstance("DSA");


               // check public key encoding
               byte[] encoded = keys.getPublic().getEncoded();
               Key key = fact.generatePublic(new X509EncodedKeySpec(encoded));

               assertTrue("public key encodings were different",
                           isEqual(key, keys.getPublic()));

               // check private key encoding
               encoded = keys.getPrivate().getEncoded();
               key = fact.generatePrivate(new PKCS8EncodedKeySpec(encoded));

               assertTrue("private key encodings were different",
                           isEqual(key, keys.getPrivate()));
    }

    private boolean isEqual(Key key1, Key key2) {
        if (key1 instanceof DSAPublicKey && key2 instanceof DSAPublicKey) {
            DSAPublicKey dsa1 = ((DSAPublicKey) key1);
            DSAPublicKey dsa2 = ((DSAPublicKey) key2);
            return dsa1.getY().equals(dsa2.getY())
                    && dsa1.getParams().getG().equals(dsa2.getParams().getG())
                    && dsa1.getParams().getP().equals(dsa2.getParams().getP())
                    && dsa1.getParams().getQ().equals(dsa2.getParams().getQ());

        } else if (key1 instanceof DSAPrivateKey
                && key2 instanceof DSAPrivateKey) {
            DSAPrivateKey dsa1 = ((DSAPrivateKey) key1);
            DSAPrivateKey dsa2 = ((DSAPrivateKey) key2);
            return dsa1.getX().equals(dsa2.getX())
                    && dsa1.getParams().getG().equals(dsa2.getParams().getG())
                    && dsa1.getParams().getP().equals(dsa2.getParams().getP())
                    && dsa1.getParams().getQ().equals(dsa2.getParams().getQ());
        } else {
            return false;
        }
    }
}
