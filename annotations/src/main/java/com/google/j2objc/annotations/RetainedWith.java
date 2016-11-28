/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PLEASE READ THIS DOCUMENTATION BEFORE USING THIS ANNOTATION!
 * Note the criteria listed below which cannot be enforced by static analysis in
 * j2objc. Prefer using {@link Weak} over @RetainedWith if possible.
 *
 * Indicates that the annotated field (child) forms a direct reference cycle
 * with the referring object (parent). Adding this annotation informs J2ObjC of
 * the reference cycle between the pair of objects allowing both objects to be
 * deallocated once there are no external references to either object.
 *
 * {@literal @}RetainedWith can be applied when a parent object pairs itself
 * with a child object and cannot fully manage the child's lifecycle, usually
 * because the child is returned to the caller. It can also be applied when the
 * child has the same class as the parent, for example on an inverse field of an
 * invertible collection type.
 *
 * {@literal @}RetainedWith can only be applied where there is a two-object pair
 * with a cycle created by one reference from each object. Note: the two
 * references can be from the same declared field. When the references are from
 * different fields only one field should be given a @RetainedWith annotation,
 * and the {@literal @}RetainedWith field should point from parent to child.
 *
 * When applied carefully in the right circumstance this annotation is very
 * useful in preventing leaks from certain Java collection types without needing
 * to alter their behavior.
 *
 * The following criteria must be adhered to otherwise behavior will be
 * undefined:
 * - The child object must not reassign any references back to the parent
 *   object. Preferably references from child to parent are declared final.
 * - The child object must not contain any {@link Weak} references back to the
 *   parent object.
 * - The child object must not be available to other threads at the time of
 *   assignment. (Normally, the cycle is created upon construction of the child)
 *
 * @author Keith Stanger
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.CLASS)
public @interface RetainedWith {
}
