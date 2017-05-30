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

import com.google.j2objc.annotations.ObjectiveCName;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import libcore.util.EmptyArray;

public final class Types {
    private Types() {
    }

    // Holds a mapping from Java type names to native type codes.
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE;
    static {
        PRIMITIVE_TO_SIGNATURE = new HashMap<Class<?>, String>(9);
        PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
        PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
        PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
        PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
        PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
        PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
        PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
        PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
        PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");
    }

    @ObjectiveCName("getTypeArray:clone:")
    public static Type[] getTypeArray(ListOfTypes types, boolean clone) {
        if (types.length() == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = types.getResolvedTypes();
        return clone ? result.clone() : result;
    }

    @ObjectiveCName("getType:")
    public static Type getType(Type type) {
        if (type instanceof ParameterizedTypeImpl) {
            return ((ParameterizedTypeImpl)type).getResolvedType();
        }
        return type;
    }

    /**
     * Returns the internal name of {@code clazz} (also known as the descriptor).
     */
    @ObjectiveCName("getSignature:")
    public static String getSignature(Class<?> clazz) {
        String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        } else {
            // TODO: this separates packages with '.' rather than '/'
            return "L" + clazz.getName() + ";";
        }
    }

    /**
     * Returns the names of {@code types} separated by commas.
     */
    @ObjectiveCName("toString:")
    public static String toString(Class<?>[] types) {
        if (types.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        appendTypeName(result, types[0]);
        for (int i = 1; i < types.length; i++) {
            result.append(',');
            appendTypeName(result, types[i]);
        }
        return result.toString();
    }

    /**
     * Appends the best {@link #toString} name for {@code c} to {@code out}.
     * This works around the fact that {@link Class#getName} is lousy for
     * primitive arrays (it writes "[C" instead of "char[]") and {@link
     * Class#getCanonicalName()} is lousy for nested classes (it uses a "."
     * separator rather than a "$" separator).
     */
    @ObjectiveCName("appendTypeName:class:")
    public static void appendTypeName(StringBuilder out, Class<?> c) {
        int dimensions = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            dimensions++;
        }
        out.append(c.getName());
        for (int d = 0; d < dimensions; d++) {
            out.append("[]");
        }
    }

    /**
     * Appends names of the {@code types} to {@code out} separated by commas.
     */
    @ObjectiveCName("appendArrayGenericType:types:")
    public static void appendArrayGenericType(StringBuilder out, Type[] types) {
        if (types.length == 0) {
            return;
        }
        appendGenericType(out, types[0]);
        for (int i = 1; i < types.length; i++) {
            out.append(',');
            appendGenericType(out, types[i]);
        }
    }

    @ObjectiveCName("appendGenericType:type:")
    public static void appendGenericType(StringBuilder out, Type type) {
        if (type instanceof TypeVariable) {
            out.append(((TypeVariable) type).getName());
        } else if (type instanceof ParameterizedType) {
            out.append(type.toString());
        } else if (type instanceof GenericArrayType) {
            Type simplified = ((GenericArrayType) type).getGenericComponentType();
            appendGenericType(out, simplified);
            out.append("[]");
        } else if (type instanceof Class) {
            Class c = (Class<?>) type;
            if (c.isArray()){
                String as[] = c.getName().split("\\[");
                int len = as.length-1;
                if (as[len].length() > 1){
                    out.append(as[len].substring(1, as[len].length() - 1));
                } else {
                    char ch = as[len].charAt(0);
                    if (ch == 'I') {
                        out.append("int");
                    } else if (ch == 'B') {
                        out.append("byte");
                    } else if (ch == 'J') {
                        out.append("long");
                    } else if (ch == 'F') {
                        out.append("float");
                    } else if (ch == 'D') {
                        out.append("double");
                    } else if (ch == 'S') {
                        out.append("short");
                    } else if (ch == 'C') {
                        out.append("char");
                    } else if (ch == 'Z') {
                        out.append("boolean");
                    } else if (ch == 'V') {
                        out.append("void");
                    }
                }
                for (int i = 0; i < len; i++){
                    out.append("[]");
                }
            } else {
                out.append(c.getName());
            }
        }
    }
}
