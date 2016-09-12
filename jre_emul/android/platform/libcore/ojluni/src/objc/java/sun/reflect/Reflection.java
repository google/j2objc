/*
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.reflect;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Common utility routines used by both java.lang and
    java.lang.reflect */

public class Reflection {

    public static void ensureMemberAccess(Class currentClass,
                                          Class memberClass,
                                          Object target,
                                          int modifiers)
        throws IllegalAccessException
    {
        if (currentClass == null || memberClass == null) {
            throw new InternalError();
        }

        if (!verifyMemberAccess(currentClass, memberClass, target, modifiers)) {
            throw new IllegalAccessException("Class " + currentClass.getName() +
                                             " can not access a member of class " +
                                             memberClass.getName() +
                                             " with modifiers \"" +
                                             Modifier.toString(modifiers) +
                                             "\"");
        }
    }

    public static boolean verifyMemberAccess(Class currentClass,
                                             // Declaring class of field
                                             // or method
                                             Class  memberClass,
                                             // May be NULL in case of statics
                                             Object target,
                                             int    modifiers)
    {
        // Verify that currentClass can access a field, method, or
        // constructor of memberClass, where that member's access bits are
        // "modifiers".

        boolean gotIsSameClassPackage = false;
        boolean isSameClassPackage = false;

        if (currentClass == memberClass) {
            // Always succeeds
            return true;
        }

        /* ----- BEGIN j2objc -----
        if (!Modifier.isPublic(getClassAccessFlags(memberClass))) {*/
        if (!Modifier.isPublic(memberClass.getModifiers())) {
        // ----- END android -----
            isSameClassPackage = isSameClassPackage(currentClass, memberClass);
            gotIsSameClassPackage = true;
            if (!isSameClassPackage) {
                return false;
            }
        }

        // At this point we know that currentClass can access memberClass.

        if (Modifier.isPublic(modifiers)) {
            return true;
        }

        boolean successSoFar = false;

        if (Modifier.isProtected(modifiers)) {
            // See if currentClass is a subclass of memberClass
            if (isSubclassOf(currentClass, memberClass)) {
                successSoFar = true;
            }
        }

        if (!successSoFar && !Modifier.isPrivate(modifiers)) {
            if (!gotIsSameClassPackage) {
                isSameClassPackage = isSameClassPackage(currentClass,
                                                        memberClass);
                gotIsSameClassPackage = true;
            }

            if (isSameClassPackage) {
                successSoFar = true;
            }
        }

        if (!successSoFar) {
            return false;
        }

        if (Modifier.isProtected(modifiers)) {
            // Additional test for protected members: JLS 6.6.2
            Class targetClass = (target == null ? memberClass : target.getClass());
            if (targetClass != currentClass) {
                if (!gotIsSameClassPackage) {
                    isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                    gotIsSameClassPackage = true;
                }
                if (!isSameClassPackage) {
                    if (!isSubclassOf(targetClass, currentClass)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isSameClassPackage(Class c1, Class c2) {
        return isSameClassPackage(c1.getClassLoader(), c1.getName(),
                                  c2.getClassLoader(), c2.getName());
    }

    /** Returns true if two classes are in the same package; classloader
        and classname information is enough to determine a class's package */
    private static boolean isSameClassPackage(ClassLoader loader1, String name1,
                                              ClassLoader loader2, String name2)
    {
        if (loader1 != loader2) {
            return false;
        } else {
            int lastDot1 = name1.lastIndexOf('.');
            int lastDot2 = name2.lastIndexOf('.');
            if ((lastDot1 == -1) || (lastDot2 == -1)) {
                // One of the two doesn't have a package.  Only return true
                // if the other one also doesn't have a package.
                return (lastDot1 == lastDot2);
            } else {
                int idx1 = 0;
                int idx2 = 0;

                // Skip over '['s
                if (name1.charAt(idx1) == '[') {
                    do {
                        idx1++;
                    } while (name1.charAt(idx1) == '[');
                    if (name1.charAt(idx1) != 'L') {
                        // Something is terribly wrong.  Shouldn't be here.
                        throw new InternalError("Illegal class name " + name1);
                    }
                }
                if (name2.charAt(idx2) == '[') {
                    do {
                        idx2++;
                    } while (name2.charAt(idx2) == '[');
                    if (name2.charAt(idx2) != 'L') {
                        // Something is terribly wrong.  Shouldn't be here.
                        throw new InternalError("Illegal class name " + name2);
                    }
                }

                // Check that package part is identical
                int length1 = lastDot1 - idx1;
                int length2 = lastDot2 - idx2;

                if (length1 != length2) {
                    return false;
                }
                return name1.regionMatches(false, idx1, name2, idx2, length1);
            }
        }
    }

    static boolean isSubclassOf(Class queryClass,
                                Class ofClass)
    {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }
}
