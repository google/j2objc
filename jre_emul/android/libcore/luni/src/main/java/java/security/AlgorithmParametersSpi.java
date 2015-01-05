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
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

/**
 * {@code AlgorithmParametersSpi} is the Service Provider Interface (SPI)
 * definition for {@code AlgorithmParameters}.
 *
 * @see AlgorithmParameters
 */
public abstract class AlgorithmParametersSpi {

    /**
     * Initializes this {@code AlgorithmParametersSpi} with the specified
     * {@code AlgorithmParameterSpec}.
     *
     * @param paramSpec
     *            the parameter specification.
     * @throws InvalidParameterSpecException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized or the given {@code paramSpec} is not appropriate
     *             for initializing this {@code AlgorithmParametersSpi}.
     */
    protected abstract void engineInit(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException;

    /**
     * Initializes this {@code AlgorithmParametersSpi} with the specified
     * {@code byte[]} using the default decoding format for parameters. The
     * default encoding format is ASN.1.
     *
     * @param params
     *            the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized, or the parameter could not be encoded.
     */
    protected abstract void engineInit(byte[] params) throws IOException;

    /**
     * Initializes this {@code AlgorithmParametersSpi} with the specified
     * {@code byte[]} using the specified decoding format.
     *
     * @param params
     *            the encoded parameters.
     * @param format
     *            the name of the decoding format.
     * @throws IOException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized, or the parameter could not be encoded.
     */
    protected abstract void engineInit(byte[] params, String format)
            throws IOException;

    /**
     * Returns the {@code AlgorithmParameterSpec} for this {@code
     * AlgorithmParametersSpi}.
     *
     * @param paramSpec
     *            the type of the parameter specification in which this
     *            parameters should be converted.
     * @return the {@code AlgorithmParameterSpec} for this {@code
     *         AlgorithmParametersSpi}.
     * @throws InvalidParameterSpecException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized, or if this parameters could not be converted to
     *             the specified class.
     */
    protected abstract <T extends AlgorithmParameterSpec> T engineGetParameterSpec(
            Class<T> paramSpec) throws InvalidParameterSpecException;

    /**
     * Returns the parameters in their default encoding format. The default
     * encoding format is ASN.1.
     *
     * @return the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized, or if this parameters could not be encoded.
     */
    protected abstract byte[] engineGetEncoded() throws IOException;

    /**
     * Returns the parameters in the specified encoding format.
     *
     * @param format
     *            the name of the encoding format.
     * @return the encoded parameters.
     * @throws IOException
     *             if this {@code AlgorithmParametersSpi} has already been
     *             initialized, or if this parameters could not be encoded.
     */
    protected abstract byte[] engineGetEncoded(String format)
            throws IOException;

    /**
     * Returns a string containing a concise, human-readable description of this
     * {@code AlgorithmParametersSpi}.
     *
     * @return a printable representation for this {@code
     *         AlgorithmParametersSpi}.
     */
    protected abstract String engineToString();

}
