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

package org.apache.harmony.security.tests.support.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;

/**
 * Additional class for verification of CertPathValidatorSpi
 * and CertPathValidator
 *
 */

public class MyCertPathValidatorSpi extends CertPathValidatorSpi {
    private int sw = 0;
    public CertPathValidatorResult engineValidate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException {
        ++sw;
        if (certPath == null) {
            if ((sw % 2) == 0) {
                throw new CertPathValidatorException("certPath null");
            }
        }
        if (params == null) {
            if ((sw % 3) == 0) {
                throw new InvalidAlgorithmParameterException("params null");
            }
        }
        return null;
    }
}
