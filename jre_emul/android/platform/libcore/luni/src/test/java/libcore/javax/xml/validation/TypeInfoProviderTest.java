/*
 * Copyright (C) 2022 The Android Open Source Project
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

package libcore.javax.xml.validation;

import static org.junit.Assert.assertNull;

import javax.xml.validation.TypeInfoProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.TypeInfo;

@RunWith(JUnit4.class)
public class TypeInfoProviderTest {
    @Test
    public void testConstructor() {
        TypeInfoProvider p = new TestTypeInfoProvider();
        assertNull(p.getElementTypeInfo() );

    }

    private static class TestTypeInfoProvider extends TypeInfoProvider {

        @Override
        public TypeInfo getElementTypeInfo() {
            return null;
        }

        @Override
        public TypeInfo getAttributeTypeInfo(int index) {
            return null;
        }

        @Override
        public boolean isIdAttribute(int index) {
            return false;
        }

        @Override
        public boolean isSpecified(int index) {
            return false;
        }
    }

}
