/*
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

package org.json;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// J2ObjC: stub version of the annotation used by org.json, so that package
// can be built separately from jre_emul without changing how its doc pages
// are generated.

/**
 * Denotes that a type use can never be null.
 *
 * <p>This is a marker annotation and it has no specific attributes.
 */
@Retention(SOURCE)
@Target({TYPE_USE})
@interface NonNull {}
