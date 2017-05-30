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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;


public final class TypeVariableImpl<D extends GenericDeclaration> implements TypeVariable<D> {
    private TypeVariableImpl<D> formalVar;
    private final GenericDeclaration declOfVarUser;
    private final String name;
    private D genericDeclaration;
    private ListOfTypes bounds;

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TypeVariable)) {
            return false;
        }
        TypeVariable<?> that = (TypeVariable<?>) o;
        return getName().equals(that.getName()) &&
                getGenericDeclaration().equals(that.getGenericDeclaration());
    }


    @Override
    public int hashCode() {
        return 31 * getName().hashCode() + getGenericDeclaration().hashCode();
    }

    /**
     * @param genericDecl declaration where a type variable is declared
     * @param name type variable name
     * @param bounds class and interface bounds
     */
    TypeVariableImpl(D genericDecl, String name, ListOfTypes bounds) {
        this.genericDeclaration = genericDecl;
        this.name = name;
        this.bounds = bounds;
        this.formalVar = this;
        this.declOfVarUser = null;
    }

    /**
     * @param genericDecl declaration where a type variable is used
     * @param name type variable name
     */
    TypeVariableImpl(D genericDecl, String name) {
        this.name = name;
        this.declOfVarUser = genericDecl;
    }

    static TypeVariable findFormalVar(GenericDeclaration layer, String name) {
        TypeVariable[] formalVars = layer.getTypeParameters();
        for (TypeVariable var : formalVars) {
            if (name.equals(var.getName())) {
                return var;
            }
        }
        // resolve() looks up the next level only, if null is returned
        return null;
    }

    private static GenericDeclaration nextLayer(GenericDeclaration decl) {
        if (decl instanceof Class) {
            // FIXME: Is the following hierarchy correct?:
            Class cl = (Class)decl;
            Method m = cl.getEnclosingMethod();
            decl = (GenericDeclaration) m != null ? m : cl.getEnclosingConstructor();
            if (decl != null) {
                return decl;
            }
            return cl.getEnclosingClass();
        } else if (decl instanceof Method) {
            return ((Method)decl).getDeclaringClass();
        } else if (decl instanceof Constructor) {
            return ((Constructor)decl).getDeclaringClass();
        } else {
            throw new AssertionError();
        }
    }

    void resolve() {
        if (formalVar != null) {
            return;
        }
        GenericDeclaration curLayer = declOfVarUser;
        TypeVariable var;
        while ((var = findFormalVar(curLayer, name)) == null) {
            curLayer = nextLayer(curLayer);
            if (curLayer == null) {
                throw new AssertionError("illegal type variable reference");
            }
        }
        formalVar = (TypeVariableImpl<D>) var;
        this.genericDeclaration = formalVar.genericDeclaration;
        this.bounds = formalVar.bounds;
    }

    public Type[] getBounds() {
        resolve();
        return bounds.getResolvedTypes().clone();
    }

    public D getGenericDeclaration() {
        resolve();
        return genericDeclaration;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
