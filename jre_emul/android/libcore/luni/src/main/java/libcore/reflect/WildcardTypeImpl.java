/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.reflect;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public final class WildcardTypeImpl implements WildcardType {

    private final ListOfTypes extendsBound, superBound;

    public WildcardTypeImpl(ListOfTypes extendsBound, ListOfTypes superBound) {
        this.extendsBound = extendsBound;
        this.superBound = superBound;
    }

    public Type[] getLowerBounds() throws TypeNotPresentException,
            MalformedParameterizedTypeException {
        return superBound.getResolvedTypes().clone();
    }

    public Type[] getUpperBounds() throws TypeNotPresentException,
            MalformedParameterizedTypeException {
        return extendsBound.getResolvedTypes().clone();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof WildcardType)) {
            return false;
        }
        WildcardType that = (WildcardType) o;
        return Arrays.equals(getLowerBounds(), that.getLowerBounds()) &&
                Arrays.equals(getUpperBounds(), that.getUpperBounds());
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(getLowerBounds()) +
                Arrays.hashCode(getUpperBounds());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("?");
        if ((extendsBound.length() == 1 && extendsBound.getResolvedTypes()[0] != Object.class)
                || extendsBound.length() > 1) {
            sb.append(" extends ").append(extendsBound);
        } else if (superBound.length() > 0) {
            sb.append(" super ").append(superBound);
        }
        return sb.toString();
    }
}
