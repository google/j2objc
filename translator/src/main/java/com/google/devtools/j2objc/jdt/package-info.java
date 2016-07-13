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

/**
 * This is a temporary package with classes that facilitate the migration
 * of j2objc's front-end from the Eclipse JDT compiler to javac.
 *
 * This package should only be referenced by the TreeConverter. All other
 * references should be made using the javax.lang.model.element and
 * javax.lang.model.type interfaces these classes implement.
 *
 * Once the migration is complete, the JDT TreeConverter and this package
 * should be deleted.
 */
package com.google.devtools.j2objc.jdt;