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

package java.beans;

import java.lang.reflect.Method;

import org.apache.harmony.beans.BeansUtils;

public class IndexedPropertyDescriptor extends PropertyDescriptor {

    private Class<?> indexedPropertyType;

    private Method indexedGetter;

    private Method indexedSetter;

    /**
     * Constructs a new instance of <code>IndexedPropertyDescriptor</code>.
     *
     * @param propertyName
     *            the specified indexed property's name.
     * @param beanClass
     *            the bean class
     * @param getterName
     *            the name of the array getter
     * @param setterName
     *            the name of the array setter
     * @param indexedGetterName
     *            the name of the indexed getter.
     * @param indexedSetterName
     *            the name of the indexed setter.
     * @throws IntrospectionException
     */
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass,
            String getterName, String setterName, String indexedGetterName,
            String indexedSetterName) throws IntrospectionException {
        super(propertyName, beanClass, getterName, setterName);
        setIndexedByName(beanClass, indexedGetterName, indexedSetterName);
    }

    private void setIndexedByName(Class<?> beanClass, String indexedGetterName,
            String indexedSetterName) throws IntrospectionException {

        String theIndexedGetterName = indexedGetterName;
        if (theIndexedGetterName == null) {
            if (indexedSetterName != null) {
                setIndexedWriteMethod(beanClass, indexedSetterName);
            }
        } else {
            if (theIndexedGetterName.length() == 0) {
                theIndexedGetterName = "get" + name;
            }
            setIndexedReadMethod(beanClass, theIndexedGetterName);
            if (indexedSetterName != null) {
                setIndexedWriteMethod(beanClass, indexedSetterName,
                        indexedPropertyType);
            }
        }

        if (!isCompatible()) {
            throw new IntrospectionException(
                "Property type is incompatible with the indexed property type");
        }
    }

    private boolean isCompatible() {
        Class<?> propertyType = getPropertyType();

        if (propertyType == null) {
            return true;
        }
        Class<?> componentTypeOfProperty = propertyType.getComponentType();
        if (componentTypeOfProperty == null) {
            return false;
        }
        if (indexedPropertyType == null) {
            return false;
        }

        return componentTypeOfProperty.getName().equals(
                indexedPropertyType.getName());
    }

    /**
     * Constructs a new instance of <code>IndexedPropertyDescriptor</code>.
     *
     * @param propertyName
     *            the specified indexed property's name.
     * @param getter
     *            the array getter
     * @param setter
     *            the array setter
     * @param indexedGetter
     *            the indexed getter
     * @param indexedSetter
     *            the indexed setter
     * @throws IntrospectionException
     */
    public IndexedPropertyDescriptor(String propertyName, Method getter,
            Method setter, Method indexedGetter, Method indexedSetter)
            throws IntrospectionException {
        super(propertyName, getter, setter);
        if (indexedGetter != null) {
            internalSetIndexedReadMethod(indexedGetter);
            internalSetIndexedWriteMethod(indexedSetter, true);
        } else {
            internalSetIndexedWriteMethod(indexedSetter, true);
            internalSetIndexedReadMethod(indexedGetter);
        }

        if (!isCompatible()) {
            throw new IntrospectionException(
                "Property type is incompatible with the indexed property type");
        }
    }

    /**
     * Constructs a new instance of <code>IndexedPropertyDescriptor</code>.
     *
     * @param propertyName
     *            the specified indexed property's name.
     * @param beanClass
     *            the bean class.
     * @throws IntrospectionException
     */
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        super(propertyName, beanClass);
        setIndexedByName(beanClass, "get" //$NON-NLS-1$
                .concat(initialUpperCase(propertyName)), "set" //$NON-NLS-1$
                .concat(initialUpperCase(propertyName)));
    }

    /**
     * Sets the indexed getter as the specified method.
     *
     * @param indexedGetter
     *            the specified indexed getter.
     * @throws IntrospectionException
     */
    public void setIndexedReadMethod(Method indexedGetter)
            throws IntrospectionException {
        this.internalSetIndexedReadMethod(indexedGetter);
    }

    /**
     * Sets the indexed setter as the specified method.
     *
     * @param indexedSetter
     *            the specified indexed setter.
     * @throws IntrospectionException
     */
    public void setIndexedWriteMethod(Method indexedSetter)
            throws IntrospectionException {
        this.internalSetIndexedWriteMethod(indexedSetter, false);
    }

    /**
     * Obtains the indexed setter.
     *
     * @return the indexed setter.
     */
    public Method getIndexedWriteMethod() {
        return indexedSetter;
    }

    /**
     * Obtains the indexed getter.
     *
     * @return the indexed getter.
     */
    public Method getIndexedReadMethod() {
        return indexedGetter;
    }

    /**
     * Determines if this <code>IndexedPropertyDescriptor</code> is equal to
     * the specified object. Two <code>IndexedPropertyDescriptor</code> s are
     * equal if the reader, indexed reader, writer, indexed writer, property
     * types, indexed property type, property editor and flags are equal.
     *
     * @param obj
     * @return true if this indexed property descriptor is equal to the
     *         specified object.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IndexedPropertyDescriptor)) {
            return false;
        }

        IndexedPropertyDescriptor other = (IndexedPropertyDescriptor) obj;

        return (super.equals(other)
                && (indexedPropertyType == null ? other.indexedPropertyType == null
                        : indexedPropertyType.equals(other.indexedPropertyType))
                && (indexedGetter == null ? other.indexedGetter == null
                        : indexedGetter.equals(other.indexedGetter)) && (indexedSetter == null ? other.indexedSetter == null
                : indexedSetter.equals(other.indexedSetter)));
    }

    /**
     * HashCode of the IndexedPropertyDescriptor
     */
    @Override
    public int hashCode() {
        return super.hashCode() + BeansUtils.getHashCode(indexedPropertyType)
                + BeansUtils.getHashCode(indexedGetter)
                + BeansUtils.getHashCode(indexedSetter);
    }

    /**
     * Obtains the Class object of the indexed property type.
     *
     * @return the Class object of the indexed property type.
     */
    public Class<?> getIndexedPropertyType() {
        return indexedPropertyType;
    }

    private void setIndexedReadMethod(Class<?> beanClass, String indexedGetterName)
            throws IntrospectionException {
        Method getter;
        try {
            getter = beanClass.getMethod(indexedGetterName,
                    new Class[] { Integer.TYPE });
        } catch (NoSuchMethodException exception) {
            throw new IntrospectionException("No such indexed read method");
        } catch (SecurityException exception) {
            throw new IntrospectionException("Security violation accessing indexed read method");
        }
        internalSetIndexedReadMethod(getter);
    }

    private void internalSetIndexedReadMethod(Method indexGetter)
            throws IntrospectionException {
        // Clearing the indexed read method.
        if (indexGetter == null) {
            if (indexedSetter == null) {
                if (getPropertyType() != null) {
                    throw new IntrospectionException(
                        "Indexed read method is not compatible with indexed write method");
                }
                indexedPropertyType = null;
            }
            this.indexedGetter = null;
            return;
        }
        // Validate the indexed getter.
        if ((indexGetter.getParameterTypes().length != 1)
                || (indexGetter.getParameterTypes()[0] != Integer.TYPE)) {
            throw new IntrospectionException("Indexed read method must take a single int argument");
        }
        Class<?> indexedReadType = indexGetter.getReturnType();
        if (indexedReadType == Void.TYPE) {
            throw new IntrospectionException("Indexed read method must take a single int argument");
        } else if (indexedSetter != null
                && indexGetter.getReturnType() != indexedSetter
                        .getParameterTypes()[1]) {
            throw new IntrospectionException(
                "Indexed read method is not compatible with indexed write method");
        }

        // Set the indexed property type if not already set, confirm validity if
        // it is.
        if (this.indexedGetter == null) {
            indexedPropertyType = indexedReadType;
        } else {
            if (indexedPropertyType != indexedReadType) {
                throw new IntrospectionException(
                    "Indexed read method is not compatible with indexed write method");
            }
        }

        // Set the indexed getter
        this.indexedGetter = indexGetter;
    }

    private void setIndexedWriteMethod(Class<?> beanClass, String indexedSetterName)
            throws IntrospectionException {
        Method setter = null;
        try {
            setter = beanClass.getMethod(indexedSetterName, new Class[] {
                    Integer.TYPE, getPropertyType().getComponentType() });
        } catch (SecurityException e) {
            throw new IntrospectionException("Security violation accessing indexed write method");
        } catch (NoSuchMethodException e) {
            throw new IntrospectionException("No such indexed write method");
        }
        internalSetIndexedWriteMethod(setter, true);
    }

    private void setIndexedWriteMethod(Class<?> beanClass,
            String indexedSetterName, Class<?> argType)
            throws IntrospectionException {
        try {
            Method setter = beanClass.getMethod(indexedSetterName, new Class[] {
                    Integer.TYPE, argType });
            internalSetIndexedWriteMethod(setter, true);
        } catch (NoSuchMethodException exception) {
            throw new IntrospectionException("No such indexed write method");
        } catch (SecurityException exception) {
            throw new IntrospectionException("Security violation accessing indexed write method");
        }
    }

    private void internalSetIndexedWriteMethod(Method indexSetter,
            boolean initialize) throws IntrospectionException {
        // Clearing the indexed write method.
        if (indexSetter == null) {
            if (indexedGetter == null) {
                if (getPropertyType() != null) {
                    throw new IntrospectionException(
                        "Indexed method is not compatible with non indexed method");
                }
                indexedPropertyType = null;
            }
            this.indexedSetter = null;
            return;
        }

        // Validate the indexed write method.
        Class<?>[] indexedSetterArgs = indexSetter.getParameterTypes();
        if (indexedSetterArgs.length != 2) {
            throw new IntrospectionException("Indexed write method must take a two arguments");
        }
        if (indexedSetterArgs[0] != Integer.TYPE) {
            throw new IntrospectionException(
                "Indexed write method must take an int as its first argument");
        }

        // Set the indexed property type if not already set, confirm validity if
        // it is.
        Class<?> indexedWriteType = indexedSetterArgs[1];
        if (initialize && indexedGetter == null) {
            indexedPropertyType = indexedWriteType;
        } else {
            if (indexedPropertyType != indexedWriteType) {
                throw new IntrospectionException(
                    "Indexed write method is not compatible with indexed read method");
            }
        }

        // Set the indexed write method.
        this.indexedSetter = indexSetter;
    }

    private static String initialUpperCase(String string) {
        if (Character.isUpperCase(string.charAt(0))) {
            return string;
        }

        String initial = string.substring(0, 1).toUpperCase();
        return initial.concat(string.substring(1));
    }
}
