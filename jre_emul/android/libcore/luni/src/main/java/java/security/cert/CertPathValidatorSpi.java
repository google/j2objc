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

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;

/**
 * The <i>Service Provider Interface</i> (<b>SPI</b>) for the {@code
 * CertPathValidator} class to be implemented by security providers.
 */
public abstract class CertPathValidatorSpi {

    /**
     * Creates a new {@code CertPathValidatorSpi} instance.
     */
    public CertPathValidatorSpi() {
    }

    /**
     * Validates the {@code CertPath} with the algorithm of this {@code
     * CertPathValidator} using the specified algorithm parameters.
     *
     * @param certPath
     *            the certification path to be validated.
     * @param params
     *            the certification path validator algorithm parameters.
     * @return the validation result.
     * @throws CertPathValidatorException
     *             if the validation fails, or the algorithm of the specified
     *             certification path cannot be validated using the algorithm of
     *             this instance.
     * @throws InvalidAlgorithmParameterException
     *             if the specified algorithm parameters cannot be used with
     *             this algorithm.
     */
    public abstract CertPathValidatorResult engineValidate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException;

}
