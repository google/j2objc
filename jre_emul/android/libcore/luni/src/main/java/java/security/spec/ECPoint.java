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

package java.security.spec;

import java.math.BigInteger;

/**
 * A Point on an Elliptic Curve in barycentric (or affine) coordinates.
 */
public class ECPoint {

    /**
     * The point on an Elliptic Curve at infinity.
     */
    public static final ECPoint POINT_INFINITY = new ECPoint();
    // affine X coordinate of this point
    private final BigInteger affineX;
    // affine Y coordinate of this point
    private final BigInteger affineY;

    // Private ctor for POINT_INFINITY
    private ECPoint() {
        affineX = null;
        affineY = null;
    }

    /**
     * Creates a new point at the specified coordinates.
     *
     * @param affineX
     *            the x-coordinate.
     * @param affineY
     *            the y-coordinate.
     */
    public ECPoint(BigInteger affineX, BigInteger affineY) {
        this.affineX = affineX;
        if (this.affineX == null) {
            throw new NullPointerException("affineX == null");
        }
        this.affineY = affineY;
        if (this.affineY == null) {
            throw new NullPointerException("affineY == null");
        }
    }

    /**
     * Returns the x-coordinate.
     *
     * @return the x-coordinate, or {@code null} for the infinite point.
     */
    public BigInteger getAffineX() {
        return affineX;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return the y-coordinate, or {@code null} fot the infinite point.
     */
    public BigInteger getAffineY() {
        return affineY;
    }

    /**
     * Returns whether the specified object and this elliptic curve point are
     * equal.
     *
     * @param other
     *            the object to compare.
     * @return {@code true} if the specified object and this elliptic curve
     *         point are equal, otherwise {@code false}.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ECPoint) {
            if (this.affineX != null) {
                ECPoint otherPoint = (ECPoint)other;
                // no need to check for null in this case
                return this.affineX.equals(otherPoint.affineX) &&
                       this.affineY.equals(otherPoint.affineY);
            } else {
                return other == POINT_INFINITY;
            }
        }
        return false;
    }

    /**
     * Returns the hashcode of this elliptic curve point.
     *
     * @return the hashcode of this elliptic curve point.
     */
    public int hashCode() {
        if (this.affineX != null) {
            return affineX.hashCode() * 31 + affineY.hashCode();
        }
        return 11;
    }
}
