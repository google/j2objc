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
import java.lang.reflect.Modifier;

import org.apache.harmony.beans.BeansUtils;

/**
 * Describes one Java Bean property.
 *
 * NOTE: PropertyEditor support is not available in j2objc, since it depends
 * on core AWT classes.
 */
public class PropertyDescriptor extends FeatureDescriptor {
    private Method getter;

    private Method setter;

//    private Class<?> propertyEditorClass;

    boolean constrained;

    boolean bound;

    public PropertyDescriptor(String propertyName, Class<?> beanClass,
            String getterName, String setterName) throws IntrospectionException {
        super();
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        this.setName(propertyName);
        if (getterName != null) {
            if (getterName.length() == 0) {
                throw new IntrospectionException(
                        "read or write method cannot be empty.");
            }
            try {
                setReadMethod(beanClass, getterName);
            } catch (IntrospectionException e) {
                setReadMethod(beanClass, createDefaultMethodName(propertyName, "get"));
            }
        }
        if (setterName != null) {
            if (setterName.length() == 0) {
                throw new IntrospectionException(
                        "read or write method cannot be empty.");
            }
            setWriteMethod(beanClass, setterName);
        }
    }

    public PropertyDescriptor(String propertyName, Method getter, Method setter)
            throws IntrospectionException {
        super();
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        this.setName(propertyName);
        setReadMethod(getter);
        setWriteMethod(setter);
    }

    public PropertyDescriptor(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        this.setName(propertyName);
        try {
            setReadMethod(beanClass,
                    createDefaultMethodName(propertyName, "is"));
        } catch (Exception e) {
            setReadMethod(beanClass, createDefaultMethodName(propertyName,
                    "get")); //$NON-NLS-1$
        }

        setWriteMethod(beanClass, createDefaultMethodName(propertyName, "set"));
    }

    public void setWriteMethod(Method setter) throws IntrospectionException {
        if (setter != null) {
            int modifiers = setter.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                throw new IntrospectionException("Modifier for setter method should be public.");
            }
            Class<?>[] parameterTypes = setter.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new IntrospectionException(
                	"Number of parameters in setter method is not equal to 1.");
            }
            Class<?> parameterType = parameterTypes[0];
            Class<?> propertyType = getPropertyType();
            if (propertyType != null && !propertyType.equals(parameterType)) {
                throw new IntrospectionException(
                	"Parameter type in setter method does not corresponds to predefined.");
            }
        }
        this.setter = setter;
    }

    public void setReadMethod(Method getter) throws IntrospectionException {
        if (getter != null) {
            int modifiers = getter.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                throw new IntrospectionException("Modifier for getter method should be public.");
            }
            Class<?>[] parameterTypes = getter.getParameterTypes();
            if (parameterTypes.length != 0) {
                throw new IntrospectionException(
                	"Number of parameters in getter method is not equal to 0.");
            }
            Class<?> returnType = getter.getReturnType();
            if (returnType.equals(Void.TYPE)) {
                throw new IntrospectionException("getter cannot return <void>");
            }
            Class<?> propertyType = getPropertyType();
            if ((propertyType != null) && !returnType.equals(propertyType)) {
                throw new IntrospectionException(
                	"Parameter type in getter method does not corresponds to predefined.");
            }
        }
        this.getter = getter;
    }

    public Method getWriteMethod() {
        return setter;
    }

    public Method getReadMethod() {
        return getter;
    }

    @Override
    public boolean equals(Object object) {
        boolean result = object instanceof PropertyDescriptor;
        if (result) {
            PropertyDescriptor pd = (PropertyDescriptor) object;
            boolean gettersAreEqual = (this.getter == null)
                    && (pd.getReadMethod() == null) || (this.getter != null)
                    && (this.getter.equals(pd.getReadMethod()));
            boolean settersAreEqual = (this.setter == null)
                    && (pd.getWriteMethod() == null) || (this.setter != null)
                    && (this.setter.equals(pd.getWriteMethod()));
            boolean propertyTypesAreEqual = this.getPropertyType() == pd
                    .getPropertyType();
//            boolean propertyEditorClassesAreEqual = this
//                    .getPropertyEditorClass() == pd.getPropertyEditorClass();
            boolean boundPropertyAreEqual = this.isBound() == pd.isBound();
            boolean constrainedPropertyAreEqual = this.isConstrained() == pd
                    .isConstrained();
            result = gettersAreEqual && settersAreEqual
//                    && propertyTypesAreEqual && propertyEditorClassesAreEqual
                    && propertyTypesAreEqual
                    && boundPropertyAreEqual && constrainedPropertyAreEqual;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return BeansUtils.getHashCode(getter) + BeansUtils.getHashCode(setter)
                + BeansUtils.getHashCode(getPropertyType())
//                + BeansUtils.getHashCode(getPropertyEditorClass())
                + BeansUtils.getHashCode(isBound())
                + BeansUtils.getHashCode(isConstrained());
    }

//    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
//        this.propertyEditorClass = propertyEditorClass;
//    }

    public Class<?> getPropertyType() {
        Class<?> result = null;
        if (getter != null) {
            result = getter.getReturnType();
        } else if (setter != null) {
            Class<?>[] parameterTypes = setter.getParameterTypes();
            result = parameterTypes[0];
        }
        return result;
    }

//    public Class<?> getPropertyEditorClass() {
//        return propertyEditorClass;
//    }

    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public boolean isConstrained() {
        return constrained;
    }

    public boolean isBound() {
        return bound;
    }

    String createDefaultMethodName(String propertyName, String prefix) {
        String result = null;
        if (propertyName != null) {
            String bos = BeansUtils.toASCIIUpperCase(propertyName.substring(0, 1));
            String eos = propertyName.substring(1, propertyName.length());
            result = prefix + bos + eos;
        }
        return result;
    }

    void setReadMethod(Class<?> beanClass, String getterName)
            throws IntrospectionException {
        try {
            Method readMethod = beanClass.getMethod(getterName, new Class[] {});
            setReadMethod(readMethod);
        } catch (Exception e) {
            throw new IntrospectionException(e.getLocalizedMessage());
        }
    }

    void setWriteMethod(Class<?> beanClass, String setterName)
            throws IntrospectionException {
        Method writeMethod = null;
        try {
            if (getter != null) {
                writeMethod = beanClass.getMethod(setterName,
                        new Class[] { getter.getReturnType() });
            } else {
                Class<?> clazz = beanClass;
                Method[] methods = null;
                while (clazz != null && writeMethod == null) {
                    methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (setterName.equals(method.getName())) {
                            if (method.getParameterTypes().length == 1) {
                                writeMethod = method;
                                break;
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new IntrospectionException(e.getLocalizedMessage());
        }
        if (writeMethod == null) {
            throw new IntrospectionException("Method not found: " + setterName);
        }
        setWriteMethod(writeMethod);
    }

//    public PropertyEditor createPropertyEditor(Object bean) {
//        PropertyEditor editor;
//        if (propertyEditorClass == null) {
//            return null;
//        }
//        if (!PropertyEditor.class.isAssignableFrom(propertyEditorClass)) {
//            throw new ClassCastException(
//            	"Property editor is not assignable from the PropertyEditor interface");
//        }
//        try {
//            Constructor<?> constr;
//            try {
//                // try to look for the constructor with single Object argument
//                constr = propertyEditorClass.getConstructor(Object.class);
//                editor = (PropertyEditor) constr.newInstance(bean);
//            } catch (NoSuchMethodException e) {
//                // try no-argument constructor
//                constr = propertyEditorClass.getConstructor();
//                editor = (PropertyEditor) constr.newInstance();
//            }
//        } catch (Exception e) {
//            RuntimeException re = new RuntimeException("Unable to instantiate property editor", e);
//            throw re;
//        }
//        return editor;
//    }
}
